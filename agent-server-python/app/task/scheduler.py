"""
定时任务调度器
定期同步失物招领数据
"""
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.interval import IntervalTrigger
from app.services.lost_found_sync import LostFoundSyncService
from app.utils.logger import app_logger

scheduler = AsyncIOScheduler()
sync_service = LostFoundSyncService()


async def scheduled_sync_lost_found():
    """定时同步失物招领数据"""
    try:
        app_logger.info("开始定时同步失物招领数据...")
        result = await sync_service.sync_all_from_java_api()
        
        if result.get('success'):
            app_logger.info(
                f"定时同步完成: 总计{result.get('total')}条, "
                f"成功{result.get('success_count')}条, "
                f"失败{result.get('fail_count')}条"
            )
        else:
            app_logger.error(f"定时同步失败: {result.get('error')}")
    except Exception as e:
        app_logger.error(f"定时同步异常: {e}")


def start_scheduler():
    """启动定时任务调度器"""
    
    # 每10分钟同步一次失物招领数据
    scheduler.add_job(
        func=scheduled_sync_lost_found,
        trigger=IntervalTrigger(minutes=10),
        id='sync_lost_found',
        name='同步失物招领数据',
        replace_existing=True
    )
    
    scheduler.start()
    app_logger.info("定时任务调度器已启动")


def stop_scheduler():
    """停止定时任务调度器"""
    scheduler.shutdown()
    app_logger.info("定时任务调度器已停止")
