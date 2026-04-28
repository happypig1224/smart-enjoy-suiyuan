"""
知识库服务层
封装知识库相关业务逻辑
"""
from typing import List
import dashscope
from http import HTTPStatus
from app.config.settings import settings
from app.repositories.milvus_repo import MilvusRepository
from app.utils.logger import app_logger
from app.utils.exceptions import EmbeddingError, KnowledgeBaseError


class KnowledgeBaseService:
    """知识库服务"""
    
    def __init__(self):
        self.milvus_repo = MilvusRepository()
        dashscope.api_key = settings.dashscope.api_key
    
    def get_embedding(self, text: str) -> List[float]:
        """
        调用阿里百炼 API 生成文本向量
        
        Args:
            text: 输入文本
        
        Returns:
            向量列表
        """
        try:
            resp = dashscope.TextEmbedding.call(
                model=settings.dashscope.embedding_model,
                input=text
            )
            
            if resp.status_code == HTTPStatus.OK:
                return resp.output["embeddings"][0]["embedding"]
            else:
                raise EmbeddingError(f"Embedding API 返回错误: {resp.message}")
        except EmbeddingError:
            raise
        except Exception as e:
            app_logger.error(f"Embedding 生成失败: {e}")
            raise EmbeddingError(f"Embedding 生成失败: {e}")
    
    def insert_document(self, text: str, doc_type: str = "campus_guide") -> List[int]:
        """
        插入文档到知识库
        
        Args:
            text: 文档文本
            doc_type: 文档类型（campus_guide/resource）
        
        Returns:
            插入的主键 ID 列表
        """
        try:
            app_logger.info(f"开始插入文档: {text[:50]}...")
            
            embedding = self.get_embedding(text)
            
            ids = self.milvus_repo.insert([text], [embedding], [doc_type])
            
            app_logger.info(f"文档插入成功，ID: {ids}")
            return ids
        except Exception as e:
            app_logger.error(f"文档插入失败: {e}")
            raise KnowledgeBaseError(f"文档插入失败: {e}")
    
    def search_documents(self, query: str, top_k: int = 3, doc_type: str = None) -> str:
        """
        检索知识库文档
        
        Args:
            query: 查询文本
            top_k: 返回结果数量
            doc_type: 文档类型过滤（可选）
        
        Returns:
            检索到的文档文本（格式化）
        """
        try:
            app_logger.info(f"开始检索知识库: {query}")
            
            query_vector = self.get_embedding(query)
            
            # 构建过滤表达式
            filter_expr = None
            if doc_type:
                filter_expr = f"doc_type == '{doc_type}'"
            
            results = self.milvus_repo.search(query_vector, top_k, filter_expr=filter_expr)
            
            contexts = []
            for hits in results:
                for hit in hits:
                    contexts.append(hit.entity.get("text"))
            
            if contexts:
                result_text = "\n\n".join(contexts)
                app_logger.info(f"检索成功，找到 {len(contexts)} 条相关文档")
                return result_text
            else:
                app_logger.info("未找到相关文档")
                return "没有找到相关知识。"
        except Exception as e:
            app_logger.error(f"知识库检索失败: {e}")
            return "知识库服务暂时不可用。"
    
    def get_entity_count(self) -> int:
        """获取知识库文档数量"""
        return self.milvus_repo.get_entity_count()
