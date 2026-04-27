"""
Agent 状态定义
定义 LangGraph 工作流的状态结构
"""
from typing import TypedDict, List, Annotated
import operator
from langchain_core.messages import BaseMessage


class SuiyuanAgentState(TypedDict):
    """
    绥园 Agent 状态
    
    Attributes:
        user_id: 用户ID
        session_id: 会话ID
        query: 用户查询内容
        messages: 对话历史消息列表
        intent: 识别的意图 (campus_guide/resource_search/lost_found/general_chat)
        retrieved_context: RAG 或工具检索到的上下文
        final_response: 最终生成的回复
    """
    user_id: int
    session_id: int
    query: str
    messages: Annotated[List[BaseMessage], operator.add]
    intent: str
    retrieved_context: str
    final_response: str
