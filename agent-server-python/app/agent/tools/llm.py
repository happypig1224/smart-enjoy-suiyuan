"""
LLM 工具模块
初始化和管理大语言模型
"""
from langchain_community.chat_models.tongyi import ChatTongyi
from app.config.settings import settings
from app.utils.logger import app_logger


# 同步调用使用的 LLM 实例
llm = ChatTongyi(model=settings.dashscope.chat_model)

# 流式调用使用的 LLM 实例（需要在编译时启用 streaming）
def create_streaming_llm():
    """创建支持流式调用的 LLM 实例"""
    return ChatTongyi(
        model=settings.dashscope.chat_model,
        streaming=True
    )

app_logger.info(f"LLM 初始化成功: {settings.dashscope.chat_model}")
