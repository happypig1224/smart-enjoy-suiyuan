"""
应用主入口
FastAPI 应用启动文件
"""
import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config.settings import settings
from app.api.routes import mcp, lost_found_sync
from app.utils.logger import app_logger
from langchain_core.messages import HumanMessage, AIMessage


app = FastAPI(
    title=settings.app_name,
    version=settings.version,
    description="智享绥园 AI Agent 服务 (MCP Protocol)"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(mcp.router)
app.include_router(lost_found_sync.router, prefix="/api")


@app.on_event("startup")
async def startup_event():
    app_logger.info(f"{'='*50}")
    app_logger.info(f"{settings.app_name} v{settings.version} 启动中...")
    app_logger.info(f"服务地址: http://{settings.mcp.host}:{settings.mcp.port}")
    app_logger.info(f"{'='*50}")
    
    # 启动定时任务
    from app.task.scheduler import start_scheduler
    start_scheduler()
    
    app_logger.info("智享绥园 Agent Server 启动完成")


@app.on_event("shutdown")
async def shutdown_event():
    app_logger.info(f"{settings.app_name} 正在关闭...")
    
    # 停止定时任务
    from app.task.scheduler import stop_scheduler
    stop_scheduler()
    
    app_logger.info("智享绥园 Agent Server 已关闭")


@app.get("/")
async def root():
    return {
        "name": settings.app_name,
        "version": settings.version,
        "status": "running"
    }


@app.get("/health")
async def health_check():
    return {"status": "healthy"}


if __name__ == "__main__":
    app_logger.info(f"启动 MCP Agent Server on {settings.mcp.host}:{settings.mcp.port}...")
    uvicorn.run(
        "app.main:app",
        host=settings.mcp.host,
        port=settings.mcp.port,
        reload=settings.debug
    )
