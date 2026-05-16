"""
LLM 工具模块
初始化和管理大语言模型
"""
from langchain_community.chat_models.tongyi import ChatTongyi
from app.config.settings import settings
from app.utils.logger import app_logger


# 同步调用使用的 LLM 实例
llm = ChatTongyi(model=settings.dashscope.chat_model, request_timeout=30)

# 流式调用使用的 LLM 实例（禁用 streaming 以避免 merge_dicts 冲突）
def create_streaming_llm():
    """创建 LLM 实例（使用非流式调用避免 bug）"""
    return ChatTongyi(
        model=settings.dashscope.chat_model,
        streaming=False,  # 禁用流式以避免 merge_dicts 冲突
        request_timeout=60
    )

app_logger.info(f"LLM 初始化成功: {settings.dashscope.chat_model}")
