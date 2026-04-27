"""
搜索工具模块
提供各种检索功能
"""
from app.core.dependencies import get_kb_service
from app.utils.logger import app_logger


def search_knowledge_base(query: str, top_k: int = 2) -> str:
    """
    真实查询 Milvus 向量库 (校园指南 RAG)
    
    Args:
        query: 查询文本
        top_k: 返回结果数量
    
    Returns:
        检索结果文本
    """
    try:
        kb_service = get_kb_service()
        result = kb_service.search_documents(query=query, top_k=top_k)
        return f"[知识库检索结果]:\n{result}"
    except Exception as e:
        app_logger.error(f"Milvus 检索异常: {e}")
        return "知识库服务暂时不可用。"


def search_resources(query: str) -> str:
    """
    模拟查询学习资源数据库
    
    Args:
        query: 查询文本
    
    Returns:
        检索结果文本
    """
    return f"[资源库检索结果] 为您找到与'{query}'相关的资源：1.《Java核心技术》PDF (ID:101); 2.《高数期末真题》DOC (ID:102)。"


def search_lost_found(query: str) -> str:
    """
    模拟查询失物招领数据库
    
    Args:
        query: 查询文本
    
    Returns:
        检索结果文本
    """
    return f"[失物招领库检索结果] 为您找到与'{query}'相关的记录：昨天在二食堂有人捡到一个黑色钱包 (发布人联系方式: 138xxxx)。"
