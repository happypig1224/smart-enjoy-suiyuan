"""
应用配置
使用 Pydantic Settings 管理配置项
"""
from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional
from pathlib import Path
from dotenv import load_dotenv

# 获取 .env 文件的绝对路径并提前加载
ENV_FILE = Path(__file__).parent.parent.parent / ".env"
load_dotenv(ENV_FILE, encoding="utf-8")


class MilvusConfig(BaseSettings):
    """Milvus 向量数据库配置"""
    model_config = SettingsConfigDict(env_prefix="MILVUS_")
    
    host: str = "localhost"
    port: int = 19530
    collection_name: str = "smart_enjoy_suiyuan"
    dim: int = 1536


class DashScopeConfig(BaseSettings):
    """阿里云百炼配置"""
    model_config = SettingsConfigDict(env_prefix="DASHSCOPE_")
    
    api_key: str = ""
    chat_model: str = "qwen-plus"
    embedding_model: str = "text-embedding-v2"


class MCPConfig(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="MCP_")
    
    host: str = "127.0.0.1"
    port: int = 8000
    service_token: str = ""
    cors_origin: str = "http://localhost:8080"


class AppSettings(BaseSettings):
    """应用全局配置"""
    model_config = SettingsConfigDict(
        case_sensitive=False
    )
    
    app_name: str = "Suiyuan AI Agent Server"
    version: str = "1.0.0"
    debug: bool = False
    java_api_base_url: str = "http://localhost:8080"
    
    milvus: MilvusConfig = MilvusConfig()
    dashscope: DashScopeConfig = DashScopeConfig()
    mcp: MCPConfig = MCPConfig()


settings = AppSettings()
