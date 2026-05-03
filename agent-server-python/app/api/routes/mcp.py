"""
MCP 路由
处理 MCP 协议请求
"""
from fastapi import APIRouter, Request, Body
from fastapi.responses import StreamingResponse

from app.agent.graph import suiyuan_agent
from app.api.schemas.request import McpRequest
from app.api.schemas.response import McpResponse
from app.agent.tools.llm import create_streaming_llm
from app.agent.tools.search import search_knowledge_base, search_resources, search_lost_found
from app.agent.nodes import intent
from app.utils.logger import app_logger
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage
import json
import asyncio

router = APIRouter(prefix="/mcp", tags=["MCP"])


async def generate_stream_async(query: str, user_id: int, session_id: int, history_list: list):
    """生成流式响应"""
    langchain_messages = []
    
    for msg in history_list:
        role = msg.get("role")
        content = msg.get("content")
        if role == "user":
            langchain_messages.append(HumanMessage(content=content))
        elif role == "assistant":
            langchain_messages.append(AIMessage(content=content))
    
    langchain_messages.append(HumanMessage(content=query))
    
    app_logger.info(f"开始流式响应生成...")
    
    try:
        conversation = None
        detected_intent = "general_chat"

        app_logger.info(f"开始意图识别: {query}")
        try:
            intent_result = intent.intent_recognition_node({
                "user_id": user_id,
                "session_id": session_id,
                "query": query,
                "messages": langchain_messages,
                "intent": "",
                "retrieved_context": "",
                "final_response": ""
            })
            detected_intent = intent_result.get("intent", "general_chat")
            app_logger.info(f"意图识别结果: {detected_intent}")
        except Exception as e:
            app_logger.warning(f"意图识别失败，使用默认意图: {e}")
            detected_intent = "general_chat"

        if detected_intent == "campus_guide":
            app_logger.info("开始检索校园知识库...")
            context = search_knowledge_base(query)
            sys_msg = SystemMessage(
                content=f"你是智享绥园校园助手。基于以下检索到的知识回答问题。\n知识库内容：{context}"
            )
            conversation = [sys_msg, HumanMessage(content=query)]
            
        elif detected_intent == "resource_search":
            app_logger.info("开始检索学习资源...")
            context = search_resources(query)
            sys_msg = SystemMessage(
                content=f"你是智享绥园资源推荐助手。\n检索结果：{context}"
            )
            conversation = [sys_msg, HumanMessage(content=query)]
            
        elif detected_intent == "lost_found":
            app_logger.info("开始检索失物招领信息...")
            context = search_lost_found(query)
            sys_msg = SystemMessage(
                content=f"你是智享绥园失物匹配助手。\n检索结果：{context}"
            )
            conversation = [sys_msg, HumanMessage(content=query)]
            
        else:
            sys_msg = SystemMessage(content="你是智享绥园平台的 AI 助手。请友好、简短地回答学生的日常问题。")
            conversation = [sys_msg] + langchain_messages
        
        streaming_llm = create_streaming_llm()
        app_logger.info(f"开始流式 LLM 调用，意图: {detected_intent}")
        
        full_response = ""
        chunk_count = 0
        
        async for chunk in streaming_llm.astream(conversation):
            if hasattr(chunk, 'content') and chunk.content:
                content = chunk.content
                full_response += content
                chunk_count += 1
                yield f"data: {json.dumps({'content': content}, ensure_ascii=False)}\n\n"
                await asyncio.sleep(0)
        
        app_logger.info(f"流式输出完成 - 总长度: {len(full_response)}, token数: {chunk_count}, 意图: {detected_intent}")
        
        yield "data: [DONE]\n\n"
        
    except Exception as e:
        app_logger.error(f"Agent 执行错误: {e}", exc_info=True)
        yield f"data: {json.dumps({'error': str(e)}, ensure_ascii=False)}\n\n"


@router.post("", response_model=McpResponse)
async def handle_mcp_request(request: McpRequest):
    """
    处理 MCP 请求
    
    Args:
        request: MCP 请求数据
    
    Returns:
        MCP 响应数据
    """
    if request.tool != "chat_agent":
        app_logger.warning(f"不支持的工具: {request.tool}")
        return McpResponse(code=400, result="", message="Unsupported tool")
    
    params = request.params
    query = params.get("query", "")
    user_id = params.get("userId", 0)
    session_id = params.get("sessionId", 0)
    history_list = params.get("history", [])
    
    if not query:
        app_logger.warning("查询内容为空")
        return McpResponse(code=400, result="", message="Query cannot be empty")
    
    app_logger.info(f"收到 MCP 请求 - 用户: {user_id}, 会话: {session_id}, 查询: {query}")
    
    langchain_messages = []

    for msg in history_list:
        role = msg.get("role")
        content = msg.get("content")
        if role == "user":
            langchain_messages.append(HumanMessage(content=content))
        elif role == "assistant":
            langchain_messages.append(AIMessage(content=content))
            
    langchain_messages.append(HumanMessage(content=query))
    
    initial_state = {
        "user_id": user_id,
        "session_id": session_id,
        "query": query,
        "messages": langchain_messages,
        "intent": "",
        "retrieved_context": "",
        "final_response": ""
    }
    
    try:
        final_state = suiyuan_agent.invoke(initial_state)
        
        reply = final_state.get("final_response", "抱歉，Agent 处理失败。")
        
        app_logger.info(f"MCP 请求处理成功")
        return McpResponse(code=200, result=reply, message="Success")
    
    except Exception as e:
        app_logger.error(f"Agent 执行错误: {e}", exc_info=True)
        return McpResponse(code=500, result="", message="Internal Agent Error")


@router.post("/stream")
async def handle_mcp_stream_request(body: dict = Body(...)):
    """
    处理 MCP 流式请求
    
    Args:
        body: 请求体字典
    
    Returns:
        SSE 流式响应
    """
    app_logger.info("="*60)
    app_logger.info("收到流式 MCP 请求，开始处理...")
    app_logger.info("="*60)
    
    try:
        app_logger.info(f"成功解析请求体: tool={body.get('tool')}")
        
        tool = body.get("tool", "")
        params = body.get("params", {})
        
        if tool != "chat_agent":
            app_logger.warning(f"不支持的工具: {tool}")
            return StreamingResponse(
                iter([f"data: {json.dumps({'error': 'Unsupported tool'})}\n\n"]),
                media_type="text/event-stream"
            )
        
        query = params.get("query", "")
        user_id = params.get("userId", 0)
        session_id = params.get("sessionId", 0)
        history_list = params.get("history", [])
        
        if not query:
            app_logger.warning("查询内容为空")
            return StreamingResponse(
                iter([f"data: {json.dumps({'error': 'Query cannot be empty'})}\n\n"]),
                media_type="text/event-stream"
            )
        
        app_logger.info(f"请求参数 - 用户: {user_id}, 会话: {session_id}, 查询: {query}")
        app_logger.info("开始生成流式响应...")
        
        return StreamingResponse(
            generate_stream_async(query, user_id, session_id, history_list),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache, no-transform",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no",
                "Transfer-Encoding": "chunked",
                "Content-Type": "text/event-stream; charset=utf-8"
            }
        )
        
    except Exception as e:
        app_logger.error(f"流式 MCP 请求错误: {e}", exc_info=True)
        return StreamingResponse(
            iter([f"data: {json.dumps({'error': str(e)})}\n\n"]),
            media_type="text/event-stream"
        )
