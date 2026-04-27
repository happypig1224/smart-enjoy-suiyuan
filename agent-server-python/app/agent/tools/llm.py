"""
LLM 工具模块
初始化和管理大语言模型
"""
from langchain_community.chat_models.tongyi import ChatTongyi
from app.config.settings import settings
from app.utils.logger import app_logger


llm = ChatTongyi(model=settings.dashscope.chat_model)
app_logger.info(f"LLM 初始化成功: {settings.dashscope.chat_model}")
