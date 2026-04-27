"""
请求数据模型
"""
from pydantic import BaseModel
from typing import Dict, Any


class McpRequest(BaseModel):
    """MCP 请求模型"""
    tool: str  # 例如: "chat_agent"
    params: Dict[str, Any]  # 包含 query, userId, sessionId 等


class ChatParams(BaseModel):
    """聊天参数模型"""
    query: str
    userId: int = 0
    sessionId: int = 0
