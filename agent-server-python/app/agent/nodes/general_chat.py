"""
通用对话节点
处理日常对话
"""
from langchain_core.messages import SystemMessage, HumanMessage
from app.core.state import SuiyuanAgentState
from app.agent.tools.llm import llm
from app.utils.logger import app_logger

def general_chat_node(state: SuiyuanAgentState) -> dict:
    """
    通用对话节点

    Args:
        state: Agent 状态

    Returns:
        包含回复的状态更新
    """
    sys_msg = SystemMessage(content="你是智享绥园平台的 AI 助手。请友好、简短地回答学生的日常问题。")

    conversation = [sys_msg] + state["messages"]

    response = llm.invoke(conversation)

    return {"final_response": response.content}