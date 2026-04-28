"""
失物招领数据同步服务
负责从Java后端API同步数据到Milvus向量库
"""
import httpx
from typing import List, Dict
from app.config.settings import settings
from app.services.knowledge_base import KnowledgeBaseService
from app.repositories.lost_found_repo import LostFoundMilvusRepository
from app.utils.logger import app_logger


class LostFoundSyncService:
    """失物招领数据同步服务"""
    
    def __init__(self):
        self.java_api_base_url = settings.java_api_base_url
        self.kb_service = KnowledgeBaseService()
        self.lf_repo = LostFoundMilvusRepository()
    
    async def sync_all_from_java_api(self) -> Dict:
        """
        从Java后端API同步所有未解决的失物招领数据到Milvus
        
        Returns:
            同步结果统计
        """
        try:
            app_logger.info("开始从Java API同步失物招领数据...")
            
            # 调用Java后端API获取数据
            url = f"{self.java_api_base_url}/user/lost-found/all-for-sync"
            async with httpx.AsyncClient(timeout=30.0) as client:
                response = await client.get(url)
                
                if response.status_code != 200:
                    app_logger.error(f"Java API返回错误状态码: {response.status_code}")
                    return {"success": False, "error": "API调用失败"}
                
                data = response.json()
                
                if data.get('code') != 1:  # 假设成功码为1
                    app_logger.error(f"Java API返回业务错误: {data.get('msg')}")
                    return {"success": False, "error": data.get('msg')}
                
                lost_found_list = data.get('data', [])
                app_logger.info(f"从Java API获取到 {len(lost_found_list)} 条失物招领记录")
            
            # 同步到Milvus
            success_count = 0
            fail_count = 0
            
            for item in lost_found_list:
                try:
                    await self._sync_single_item(item)
                    success_count += 1
                except Exception as e:
                    app_logger.error(f"同步单条记录失败 (ID: {item.get('id')}): {e}")
                    fail_count += 1
            
            result = {
                "success": True,
                "total": len(lost_found_list),
                "success_count": success_count,
                "fail_count": fail_count
            }
            
            app_logger.info(f"同步完成: {result}")
            return result
            
        except Exception as e:
            app_logger.error(f"同步失物招领数据失败: {e}")
            return {"success": False, "error": str(e)}
    
    async def _sync_single_item(self, item: Dict):
        """
        同步单条失物招领记录
        
        Args:
            item: 失物招领数据字典
        """
        lf_id = item.get('id')
        
        # 构建用于向量化的文本（组合关键字段）
        type_text = "寻物启事" if item.get('type') == 0 else "招领启事"
        urgent_text = "[紧急]" if item.get('urgent') == 1 else ""
        
        vector_text = f"""
{urgent_text}{type_text}
标题: {item.get('title', '')}
描述: {item.get('description', '')}
地点: {item.get('location', '')}
时间: {item.get('createTime', '')}
联系方式: {item.get('phoneContact', '') or item.get('wechatContact', '')}
        """.strip()
        
        # 生成向量
        embedding = self.kb_service.get_embedding(vector_text)
        
        # 构建元数据
        metadata = {
            'type': item.get('type', 0),
            'status': item.get('status', 0),
            'title': item.get('title', ''),
            'description': item.get('description', ''),
            'location': item.get('location', ''),
            'phone_contact': item.get('phoneContact', ''),
            'wechat_contact': item.get('wechatContact', ''),
            'urgent': item.get('urgent', 0),
            'create_time': item.get('createTime', '')
        }
        
        # 插入到Milvus
        self.lf_repo.insert_lost_found(lf_id, vector_text, embedding, metadata)
    
    async def sync_single_item(self, item: Dict):
        """
        同步单条失物招领记录（供外部调用）
        
        Args:
            item: 失物招领数据字典
        """
        await self._sync_single_item(item)
    
    async def delete_item(self, lf_id: int):
        """
        从Milvus中删除指定的失物招领记录
        
        Args:
            lf_id: 失物招领ID
        """
        self.lf_repo.delete_lost_found(lf_id)
    
    async def update_item(self, item: Dict):
        """
        更新Milvus中的失物招领记录
        
        Args:
            item: 更新后的失物招领数据字典
        """
        lf_id = item.get('id')
        
        # 构建用于向量化的文本
        type_text = "寻物启事" if item.get('type') == 0 else "招领启事"
        urgent_text = "[紧急]" if item.get('urgent') == 1 else ""
        
        vector_text = f"""
{urgent_text}{type_text}
标题: {item.get('title', '')}
描述: {item.get('description', '')}
地点: {item.get('location', '')}
时间: {item.get('createTime', '')}
联系方式: {item.get('phoneContact', '') or item.get('wechatContact', '')}
        """.strip()
        
        # 生成向量
        embedding = self.kb_service.get_embedding(vector_text)
        
        # 构建元数据
        metadata = {
            'type': item.get('type', 0),
            'status': item.get('status', 0),
            'title': item.get('title', ''),
            'description': item.get('description', ''),
            'location': item.get('location', ''),
            'phone_contact': item.get('phoneContact', ''),
            'wechat_contact': item.get('wechatContact', ''),
            'urgent': item.get('urgent', 0),
            'create_time': item.get('createTime', '')
        }
        
        # 更新Milvus
        self.lf_repo.update_lost_found(lf_id, vector_text, embedding, metadata)
