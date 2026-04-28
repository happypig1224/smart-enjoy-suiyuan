"""
搜索工具模块
提供各种检索功能
"""
from app.core.dependencies import get_kb_service
from app.repositories.lost_found_repo import LostFoundMilvusRepository
from app.services.knowledge_base import KnowledgeBaseService
from app.utils.logger import app_logger


def search_knowledge_base(query: str, top_k: int = 2) -> str:
    """
    校园指南RAG检索
    
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


def search_resources(query: str, top_k: int = 3) -> str:
    """
    学习资源数据库 RAG 检索
    
    Args:
        query: 查询文本
        top_k: 返回结果数量
    
    Returns:
        检索结果文本
    """
    try:
        app_logger.info(f"开始检索学习资源: {query}")
        
        kb_service = get_kb_service()
        
        # 检索时过滤 doc_type == 'resource'
        result = kb_service.search_documents(query=query, top_k=top_k, doc_type="resource")
        
        if not result or result == "没有找到相关知识。":
            return f"[学习资源检索结果]\n暂未找到与'{query}'相关的学习资源。\n\n 建议：\n1. 尝试更换关键词（如 'Java'、'高数'、'Python'）\n2. 在论坛发帖求助其他同学"
        
        return f"[学习资源检索结果]\n为您找到以下相关资源：\n\n{result}\n\n 提示：请根据实际需求下载学习。"
    except Exception as e:
        app_logger.error(f"学习资源检索异常: {e}")
        return "学习资源服务暂时不可用，请稍后重试。"


def search_lost_found(query: str, top_k: int = 5) -> str:
    """
    失物招领RAG检索
    
    Args:
        query: 查询文本
        top_k: 返回结果数量
    
    Returns:
        检索结果文本
    """
    try:
        app_logger.info(f"开始检索失物招领: {query}")
        
        # 初始化服务
        kb_service = KnowledgeBaseService()
        lf_repo = LostFoundMilvusRepository()
        
        # 生成查询向量
        query_vector = kb_service.get_embedding(query)
        
        # 构建过滤条件：只检索未解决的记录
        filter_expr = "status == 0"
        
        # 执行向量检索
        results = lf_repo.search_lost_found(
            query_vector=query_vector,
            top_k=top_k,
            filter_expr=filter_expr
        )
        
        if not results:
            app_logger.info("未找到匹配的失物招领记录")
            return "暂未找到匹配的失物招领记录。您可以稍后再试，或在社区发布相关信息。"
        
        # 格式化结果
        formatted_results = []
        for idx, item in enumerate(results, 1):
            type_text = "寻物启事" if item['type'] == 0 else "招领启事"
            urgent_mark = "[紧急] " if item['urgent'] == 1 else ""
            
            contact_info = ""
            if item['phone_contact']:
                contact_info += f"电话: {item['phone_contact']} "
            if item['wechat_contact']:
                contact_info += f"微信: {item['wechat_contact']}"
            
            result_text = f"""{idx}. {urgent_mark}{type_text}
   标题: {item['title']}
   地点: {item['location']}
   描述: {item['description']}
   时间: {item['create_time']}
   {contact_info}
   匹配度: {item['score']:.2%}"""
            
            formatted_results.append(result_text)
        
        result_summary = "\n\n".join(formatted_results)
        
        app_logger.info(f"失物招领检索完成，找到 {len(results)} 条匹配记录")
        
        return f"[失物招领匹配结果]\n为您找到以下相关记录：\n\n{result_summary}\n\n 提示：请尽快联系发布者，核实物品信息。"
        
    except Exception as e:
        app_logger.error(f"失物招领检索失败: {e}")
        return "失物招领服务暂时不可用，请稍后重试。"
