"""
LangGraph 状态机构建
定义 Agent 工作流图
"""
from langgraph.graph import StateGraph, END
from app.core.state import SuiyuanAgentState
from app.agent.nodes import intent, campus_guide, resource, lost_found, general_chat
from app.utils.logger import app_logger


workflow = StateGraph(SuiyuanAgentState)

workflow.add_node("intent_router", intent.intent_recognition_node)
workflow.add_node("campus_guide", campus_guide.campus_guide_node)
workflow.add_node("resource_search", resource.resource_search_node)
workflow.add_node("lost_found", lost_found.lost_found_node)
workflow.add_node("general_chat", general_chat.general_chat_node)

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
