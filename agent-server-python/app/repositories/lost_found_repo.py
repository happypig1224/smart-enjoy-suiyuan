"""
失物招领向量数据访问层
"""
from typing import List, Dict, Optional
from pymilvus import Collection, FieldSchema, CollectionSchema, DataType, utility
from app.config.settings import settings
from app.repositories.milvus_connection_manager import MilvusConnectionManager
from app.utils.logger import app_logger
from app.utils.exceptions import MilvusConnectionError, MilvusOperationError


class LostFoundMilvusRepository:
    """失物招领Milvus数据访问仓储"""
    
    def __init__(self):
        self.host = settings.milvus.host
        self.port = settings.milvus.port
        self.collection_name = "lost_found_embeddings"
        self.dim = settings.milvus.dim
        self.collection: Optional[Collection] = None
        
        MilvusConnectionManager.get_instance().ensure_connection()
        self._init_collection()
    
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
        """创建失物招领专用集合"""
        fields = [
            FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=False),
            FieldSchema(name="lf_id", dtype=DataType.INT64, description="原始失物招领ID"),
            FieldSchema(name="type", dtype=DataType.INT8, description="类型: 0-寻物, 1-招领"),
            FieldSchema(name="status", dtype=DataType.INT8, description="状态: 0-未解决, 1-已解决"),
            FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=500, description="标题"),
            FieldSchema(name="description", dtype=DataType.VARCHAR, max_length=65535, description="描述"),
            FieldSchema(name="location", dtype=DataType.VARCHAR, max_length=500, description="地点"),
            FieldSchema(name="phone_contact", dtype=DataType.VARCHAR, max_length=50, description="电话"),
            FieldSchema(name="wechat_contact", dtype=DataType.VARCHAR, max_length=200, description="微信"),
            FieldSchema(name="urgent", dtype=DataType.INT8, description="紧急程度"),
            FieldSchema(name="create_time", dtype=DataType.VARCHAR, max_length=50, description="创建时间"),
            FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=self.dim)
        ]
        schema = CollectionSchema(fields=fields, description="智享绥园失物招领向量库")
        
        self.collection = Collection(name=self.collection_name, schema=schema)
        
        index_params = {
            "metric_type": "COSINE",
            "index_type": "IVF_FLAT",
            "params": {"nlist": 128}
        }
        self.collection.create_index(field_name="embedding", index_params=index_params)
        
        self.collection.create_index(field_name="lf_id", index_params={"index_type": "STL_SORT"})
        
        app_logger.info(f"失物招领集合 {self.collection_name} 创建完成并建立索引")
    
    def insert_lost_found(self, lf_id: int, text: str, embedding: List[float], 
                          metadata: Dict) -> int:
        """插入失物招领数据到Milvus，返回记录ID"""
        try:
            data = [
                [lf_id],  
                [lf_id],  
                [metadata.get('type', 0)],  
                [metadata.get('status', 0)],  
                [metadata.get('title', '')],  
                [metadata.get('description', '')],  
                [metadata.get('location', '')],  
                [metadata.get('phone_contact', '')],  
                [metadata.get('wechat_contact', '')],  
                [metadata.get('urgent', 0)],  
                [metadata.get('create_time', '')],  
                [embedding]  
            ]
            
            result = self.collection.insert(data)
            self.collection.flush()
            
            app_logger.info(f"成功插入失物招领数据，ID: {lf_id}")
            return lf_id
        except Exception as e:
            app_logger.error(f"失物招领数据插入失败: {e}")
            raise MilvusOperationError(f"数据插入失败: {e}")
    
    def search_lost_found(
        self,
        query_vector: List[float],
        top_k: int = 5,
        filter_expr: str = None
    ) -> List[Dict]:
        """向量相似度检索失物招领，支持过滤条件"""
        try:
            self.collection.load()
            
            output_fields = [
                "lf_id", "type", "status", "title", "description",
                "location", "phone_contact", "wechat_contact",
                "urgent", "create_time"
            ]
            
            search_params = {
                "metric_type": "COSINE",
                "params": {"nprobe": 10},
            }
            
            results = self.collection.search(
                data=[query_vector],
                anns_field="embedding",
                param=search_params,
                limit=top_k,
                expr=filter_expr,  
                output_fields=output_fields
            )
            
            parsed_results = []
            for hits in results:
                for hit in hits:
                    item = {
                        'lf_id': hit.entity.get('lf_id'),
                        'type': hit.entity.get('type'),
                        'status': hit.entity.get('status'),
                        'title': hit.entity.get('title'),
                        'description': hit.entity.get('description'),
                        'location': hit.entity.get('location'),
                        'phone_contact': hit.entity.get('phone_contact'),
                        'wechat_contact': hit.entity.get('wechat_contact'),
                        'urgent': hit.entity.get('urgent'),
                        'create_time': hit.entity.get('create_time'),
                        'score': hit.score  # 相似度分数
                    }
                    parsed_results.append(item)
            
            app_logger.info(f"失物招领检索完成，返回 {len(parsed_results)} 条结果")
            return parsed_results
        except Exception as e:
            app_logger.error(f"Milvus 检索失败: {e}")
            raise MilvusOperationError(f"检索失败: {e}")
    
    def delete_lost_found(self, lf_id: int) -> bool:
        """删除指定失物招领记录，返回是否成功"""
        try:
            expr = f"lf_id == {lf_id}"
            self.collection.delete(expr)
            self.collection.flush()
            
            app_logger.info(f"成功删除失物招领记录，ID: {lf_id}")
            return True
        except Exception as e:
            app_logger.error(f"删除失物招领记录失败: {e}")
            return False
    
    def update_lost_found(self, lf_id: int, text: str, embedding: List[float],
                          metadata: Dict    ) -> bool:
        """更新失物招领记录（先删后插），返回是否成功"""
        try:
            self.delete_lost_found(lf_id)
            
            self.insert_lost_found(lf_id, text, embedding, metadata)
            
            app_logger.info(f"成功更新失物招领记录，ID: {lf_id}")
            return True
        except Exception as e:
            app_logger.error(f"更新失物招领记录失败: {e}")
            return False
    
    def get_entity_count(self) -> int:
        """获取集合中的实体数量"""
        return self.collection.num_entities
    
    def clear_all(self):
        """清空集合（慎用）"""
        try:
            self.collection.drop()
            self._create_collection()
            app_logger.warning("失物招领集合已清空")
        except Exception as e:
            app_logger.error(f"清空集合失败: {e}")
            raise
