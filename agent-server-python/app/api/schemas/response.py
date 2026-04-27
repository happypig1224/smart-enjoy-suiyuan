"""
响应数据模型
"""
from pydantic import BaseModel


class McpResponse(BaseModel):
    """MCP 响应模型"""
    code: int
    result: str
    message: str
