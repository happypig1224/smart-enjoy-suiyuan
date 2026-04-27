"""
失物招领节点
检索失物招领信息并生成匹配结果
"""
from langchain_core.messages import SystemMessage, HumanMessage
from app.core.state import SuiyuanAgentState
from app.agent.tools.llm import llm
from app.agent.tools.search import search_lost_found
from app.utils.logger import app_logger


def lost_found_node(state: SuiyuanAgentState) -> dict:
    """
    失物匹配节点
    
    Args:
        state: Agent 状态
    
    Returns:
        包含检索上下文和回复的状态更新
    """
    query = state["query"]
    app_logger.info(f"执行失物招领检索: {query}")
    
    context = search_lost_found(query)
    
    sys_msg = SystemMessage(
        content=f"你是智享绥园失物匹配助手。向用户提供失物招领线索。如果找到匹配项，请提醒用户尽快联系。\n检索结果：{context}"
    )
    
    response = llm.invoke([sys_msg, HumanMessage(content=query)])
    
    app_logger.info(f"失物招领检索完成")
    return {
        "retrieved_context": context,
        "final_response": response.content
    }
