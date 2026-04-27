"""
MCP 路由
处理 MCP 协议请求
"""
from fastapi import APIRouter
from app.api.schemas.request import McpRequest
from app.api.schemas.response import McpResponse
from app.agent.graph import suiyuan_agent
from app.utils.logger import app_logger
from langchain_core.messages import HumanMessage, AIMessage

router = APIRouter(prefix="/mcp", tags=["MCP"])


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
