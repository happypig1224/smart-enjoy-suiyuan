"""
应用主入口
FastAPI 应用启动文件
"""
import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config.settings import settings
from app.api.routes import mcp
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


@app.on_event("startup")
async def startup_event():
    """应用启动事件"""
    app_logger.info(f"{'='*50}")
    app_logger.info(f"{settings.app_name} v{settings.version} 启动中...")
    app_logger.info(f"服务地址: http://{settings.mcp.host}:{settings.mcp.port}")
    app_logger.info(f"{'='*50}")


@app.on_event("shutdown")
async def shutdown_event():
    """应用关闭事件"""
    app_logger.info(f"{settings.app_name} 正在关闭...")


@app.get("/")
async def root():
    """根路径"""
    return {
        "name": settings.app_name,
        "version": settings.version,
        "status": "running"
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "healthy"}


if __name__ == "__main__":
    app_logger.info(f"启动 MCP Agent Server on {settings.mcp.host}:{settings.mcp.port}...")
    uvicorn.run(
        "app.main:app",
        host=settings.mcp.host,
        port=settings.mcp.port,
        reload=settings.debug
    )
