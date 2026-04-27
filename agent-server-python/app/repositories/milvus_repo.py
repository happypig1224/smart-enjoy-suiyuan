"""
Milvus 数据访问层
负责与 Milvus 向量数据库的交互
"""
from typing import List, Optional
from pymilvus import connections, Collection, FieldSchema, CollectionSchema, DataType, utility
from app.config.settings import settings
from app.utils.logger import app_logger
from app.utils.exceptions import MilvusConnectionError, MilvusOperationError


class MilvusRepository:
    """Milvus 数据访问仓储"""
    
    def __init__(self):
        self.host = settings.milvus.host
        self.port = settings.milvus.port
        self.collection_name = settings.milvus.collection_name
        self.dim = settings.milvus.dim
        self.collection: Optional[Collection] = None
        
        self._connect()
        self._init_collection()
    
    def _connect(self):
        """连接到 Milvus 服务"""
        try:
            connections.connect("default", host=self.host, port=self.port)
            app_logger.info(f"成功连接到 Milvus: {self.host}:{self.port}")
        except Exception as e:
            app_logger.error(f"Milvus 连接失败: {e}")
            raise MilvusConnectionError(f"无法连接到 Milvus: {e}")
    
    def _init_collection(self):
        """初始化或加载集合"""
        try:
            if utility.has_collection(self.collection_name):
                app_logger.info(f"集合 {self.collection_name} 已存在，加载集合")
                self.collection = Collection(self.collection_name)
            else:
                app_logger.info(f"创建新集合 {self.collection_name}")
                self._create_collection()
        except Exception as e:
            app_logger.error(f"集合初始化失败: {e}")
            raise MilvusOperationError(f"集合初始化失败: {e}")
    
    def _create_collection(self):
        """创建新集合"""
        fields = [
            FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True),
            FieldSchema(name="text", dtype=DataType.VARCHAR, max_length=65535),
            FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=self.dim)
        ]
        schema = CollectionSchema(fields=fields, description="智享绥园校园知识库")
        
        self.collection = Collection(name=self.collection_name, schema=schema)
        
        # 创建索引
        index_params = {
            "metric_type": "COSINE",
            "index_type": "IVF_FLAT",
            "params": {"nlist": 128}
        }
        self.collection.create_index(field_name="embedding", index_params=index_params)
        app_logger.info(f"集合 {self.collection_name} 创建完成并建立索引")
    
    def insert(self, texts: List[str], embeddings: List[List[float]]) -> List[int]:
        """
        插入数据到 Milvus
        
        Args:
            texts: 文本列表
            embeddings: 向量列表
        
        Returns:
            插入的主键 ID 列表
        """
        try:
            data = [texts, embeddings]
            result = self.collection.insert(data)
            self.collection.flush()
            app_logger.info(f"成功插入 {len(result.primary_keys)} 条数据")
            return result.primary_keys
        except Exception as e:
            app_logger.error(f"数据插入失败: {e}")
            raise MilvusOperationError(f"数据插入失败: {e}")
    
    def search(
        self,
        query_vector: List[float],
        top_k: int = 3,
        output_fields: List[str] = None
    ) -> List:
        """
        向量相似度检索
        
        Args:
            query_vector: 查询向量
            top_k: 返回结果数量
            output_fields: 需要返回的字段
        
        Returns:
            检索结果列表
        """
        try:
            self.collection.load()
            
            if output_fields is None:
                output_fields = ["text"]
            
            search_params = {
                "metric_type": "COSINE",
                "params": {"nprobe": 10},
            }
            
            results = self.collection.search(
                data=[query_vector],
                anns_field="embedding",
                param=search_params,
                limit=top_k,
                output_fields=output_fields
            )
            
            app_logger.info(f"检索完成，返回 {len(results[0])} 条结果")
            return results
        except Exception as e:
            app_logger.error(f"Milvus 检索失败: {e}")
            raise MilvusOperationError(f"检索失败: {e}")
    
    def get_entity_count(self) -> int:
        """获取集合中的实体数量"""
        return self.collection.num_entities
