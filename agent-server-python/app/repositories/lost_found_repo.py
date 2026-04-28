"""
失物招领向量数据访问层
专门用于失物招领信息的向量存储和检索
"""
from typing import List, Dict, Optional
from pymilvus import connections, Collection, FieldSchema, CollectionSchema, DataType, utility
from app.config.settings import settings
from app.utils.logger import app_logger
from app.utils.exceptions import MilvusConnectionError, MilvusOperationError


class LostFoundMilvusRepository:
    """失物招领Milvus数据访问仓储"""
    
    def __init__(self):
        self.host = settings.milvus.host
        self.port = settings.milvus.port
        self.collection_name = "lost_found_embeddings"  # 独立的集合名
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
        
        # 创建向量索引
        index_params = {
            "metric_type": "COSINE",
            "index_type": "IVF_FLAT",
            "params": {"nlist": 128}
        }
        self.collection.create_index(field_name="embedding", index_params=index_params)
        
        # 为lf_id创建标量索引以支持过滤
        self.collection.create_index(field_name="lf_id", index_params={"index_type": "STL_SORT"})
        
        app_logger.info(f"失物招领集合 {self.collection_name} 创建完成并建立索引")
    
    def insert_lost_found(self, lf_id: int, text: str, embedding: List[float], 
                          metadata: Dict) -> int:
        """
        插入失物招领数据到 Milvus
        
        Args:
            lf_id: 原始失物招领ID
            text: 用于向量化的文本（标题+描述+地点的组合）
            embedding: 向量
            metadata: 元数据字典，包含type, status, location等
        
        Returns:
            插入的主键 ID
        """
        try:
            data = [
                [lf_id],  # id (使用原始ID作为主键)
                [lf_id],  # lf_id
                [metadata.get('type', 0)],  # type
                [metadata.get('status', 0)],  # status
                [metadata.get('title', '')],  # title
                [metadata.get('description', '')],  # description
                [metadata.get('location', '')],  # location
                [metadata.get('phone_contact', '')],  # phone_contact
                [metadata.get('wechat_contact', '')],  # wechat_contact
                [metadata.get('urgent', 0)],  # urgent
                [metadata.get('create_time', '')],  # create_time
                [embedding]  # embedding
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
        """
        向量相似度检索失物招领
        
        Args:
            query_vector: 查询向量
            top_k: 返回结果数量
            filter_expr: Milvus过滤表达式，如 "status == 0 and type == 1"
        
        Returns:
            检索结果列表，每个元素包含元数据和相似度分数
        """
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
                expr=filter_expr,  # 支持过滤
                output_fields=output_fields
            )
            
            # 解析结果
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
        """
        删除指定的失物招领记录
        
        Args:
            lf_id: 原始失物招领ID
        
        Returns:
            是否删除成功
        """
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
                          metadata: Dict) -> bool:
        """
        更新失物招领记录（先删除再插入）
        
        Args:
            lf_id: 原始失物招领ID
            text: 用于向量化的文本
            embedding: 新的向量
            metadata: 更新的元数据
        
        Returns:
            是否更新成功
        """
        try:
            # 先删除旧记录
            self.delete_lost_found(lf_id)
            
            # 再插入新记录
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
