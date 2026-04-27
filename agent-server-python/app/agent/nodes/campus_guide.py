"""
校园指南节点
基于 RAG 检索校园知识并生成回答
"""
from langchain_core.messages import SystemMessage, HumanMessage
from app.core.state import SuiyuanAgentState
from app.agent.tools.llm import llm
from app.agent.tools.search import search_knowledge_base
from app.utils.logger import app_logger


def campus_guide_node(state: SuiyuanAgentState) -> dict:
    """
    校园指南 RAG 节点
    
    Args:
        state: Agent 状态
    
    Returns:
        包含检索上下文和回复的状态更新
    """
    query = state["query"]
    app_logger.info(f"执行校园指南检索: {query}")
    
    context = search_knowledge_base(query)
    
    sys_msg = SystemMessage(
        content=f"你是智享绥园校园助手。基于以下检索到的知识回答问题。如果知识库没提到，就说不知道。\n知识库内容：{context}"
    )
    
    response = llm.invoke([sys_msg, HumanMessage(content=query)])
    
    app_logger.info(f"校园指南检索完成")
    return {
        "retrieved_context": context,
        "final_response": response.content
    }
