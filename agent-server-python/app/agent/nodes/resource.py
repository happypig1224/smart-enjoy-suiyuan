"""
资源搜索节点
检索学习资源并生成推荐
"""
from langchain_core.messages import SystemMessage, HumanMessage
from app.core.state import SuiyuanAgentState
from app.agent.tools.llm import llm
from app.agent.tools.search import search_resources
from app.utils.logger import app_logger


def resource_search_node(state: SuiyuanAgentState) -> dict:
    """
    学习资源搜索节点
    
    Args:
        state: Agent 状态
    
    Returns:
        包含检索上下文和回复的状态更新
    """
    query = state["query"]
    app_logger.info(f"执行资源搜索: {query}")
    
    context = search_resources(query)
    
    sys_msg = SystemMessage(
        content=f"你是智享绥园资源推荐助手。向用户友好地展示检索到的资源。\n检索结果：{context}"
    )
    
    response = llm.invoke([sys_msg, HumanMessage(content=query)])
    
    app_logger.info(f"资源搜索完成")
    return {
        "retrieved_context": context,
        "final_response": response.content
    }
