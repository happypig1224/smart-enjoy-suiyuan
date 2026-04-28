"""
失物招领同步API路由
提供数据同步接口
"""
from fastapi import APIRouter, HTTPException
from app.services.lost_found_sync import LostFoundSyncService
from app.utils.logger import app_logger

router = APIRouter(prefix="/lost-found", tags=["失物招领"])
sync_service = LostFoundSyncService()


@router.post("/sync")
async def sync_lost_found_data():
    """
    手动触发失物招领数据同步
    从Java后端API同步所有未解决的记录到Milvus
    """
    try:
        app_logger.info("手动触发失物招领数据同步")
        result = await sync_service.sync_all_from_java_api()
        
        if not result.get('success'):
            raise HTTPException(status_code=500, detail=result.get('error'))
        
        return {
            "code": 1,
            "message": "同步成功",
            "data": result
        }
    except HTTPException:
        raise
    except Exception as e:
        app_logger.error(f"同步接口异常: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/sync/single")
async def sync_single_item(item: dict):
    """
    同步单条失物招领记录
    当Java后端新增或更新记录时调用
    """
    try:
        app_logger.info(f"同步单条失物招领记录: {item.get('id')}")
        await sync_service.sync_single_item(item)
        
        return {
            "code": 1,
            "message": "同步成功",
            "data": {"id": item.get('id')}
        }
    except Exception as e:
        app_logger.error(f"同步单条记录失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/sync/{lf_id}")
async def delete_item(lf_id: int):
    """
    删除失物招领记录
    当Java后端删除记录时调用
    """
    try:
        app_logger.info(f"删除失物招领记录: {lf_id}")
        await sync_service.delete_item(lf_id)
        
        return {
            "code": 1,
            "message": "删除成功",
            "data": {"id": lf_id}
        }
    except Exception as e:
        app_logger.error(f"删除记录失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/stats")
async def get_stats():
    """
    获取失物招领向量库统计信息
    """
    try:
        count = sync_service.lf_repo.get_entity_count()
        
        return {
            "code": 1,
            "message": "查询成功",
            "data": {
                "vector_count": count
            }
        }
    except Exception as e:
        app_logger.error(f"查询统计信息失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))
