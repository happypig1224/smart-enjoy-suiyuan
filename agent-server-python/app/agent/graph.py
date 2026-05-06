"""
LangGraph 状态机构建
定义 Agent 工作流图
"""
from langgraph.graph import StateGraph, END
from app.core.state import SuiyuanAgentState
from app.agent.nodes import intent, campus_guide, resource, lost_found, general_chat
from app.utils.logger import app_logger


def safe_intent_router(state):
    try:
        return intent.intent_recognition_node(state)
    except Exception as e:
        app_logger.error(f"意图识别异常: {e}", exc_info=True)
        return {"intent": "general_chat"}


def safe_campus_guide(state):
    try:
        return campus_guide.campus_guide_node(state)
    except Exception as e:
        app_logger.error(f"校园指南节点异常: {e}", exc_info=True)
        return {"final_response": "抱歉，校园指南服务暂时不可用，请稍后重试。"}


def safe_resource_search(state):
    try:
        return resource.resource_search_node(state)
    except Exception as e:
        app_logger.error(f"资源检索节点异常: {e}", exc_info=True)
        return {"final_response": "抱歉，资源检索服务暂时不可用，请稍后重试。"}


def safe_lost_found(state):
    try:
        return lost_found.lost_found_node(state)
    except Exception as e:
        app_logger.error(f"失物招领节点异常: {e}", exc_info=True)
        return {"final_response": "抱歉，失物招领服务暂时不可用，请稍后重试。"}


def safe_general_chat(state):
    try:
        return general_chat.general_chat_node(state)
    except Exception as e:
        app_logger.error(f"通用对话节点异常: {e}", exc_info=True)
        return {"final_response": "抱歉，AI服务暂时不可用，请稍后重试。"}


workflow = StateGraph(SuiyuanAgentState)

workflow.add_node("intent_router", safe_intent_router)
workflow.add_node("campus_guide", safe_campus_guide)
workflow.add_node("resource_search", safe_resource_search)
workflow.add_node("lost_found", safe_lost_found)
workflow.add_node("general_chat", safe_general_chat)

workflow.set_entry_point("intent_router")


def route_by_intent(state: SuiyuanAgentState) -> str:
    """
    条件路由函数
    
    Args:
        state: Agent 状态
    
    Returns:
        下一个节点名称
    """
    return state["intent"]


workflow.add_conditional_edges(
    "intent_router",
    route_by_intent,
    {
        "campus_guide": "campus_guide",
        "resource_search": "resource_search",
        "lost_found": "lost_found",
        "general_chat": "general_chat"
    }
)

workflow.add_edge("campus_guide", END)
workflow.add_edge("resource_search", END)
workflow.add_edge("lost_found", END)
workflow.add_edge("general_chat", END)

suiyuan_agent = workflow.compile()
app_logger.info("LangGraph Agent 编译完成")
