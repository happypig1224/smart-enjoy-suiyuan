"""
依赖注入模块
提供全局单例服务
"""
from functools import lru_cache
from app.services.knowledge_base import KnowledgeBaseService


@lru_cache()
def get_kb_service() -> KnowledgeBaseService:
    """
    获取知识库服务单例
    
    Returns:
        KnowledgeBaseService 实例
    """
    return KnowledgeBaseService()
