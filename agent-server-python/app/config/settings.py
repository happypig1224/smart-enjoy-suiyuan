"""
应用配置管理模块
使用 Pydantic Settings 管理所有配置项
支持从环境变量和 .env 文件读取配置
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
    dim: int = 1536  # text-embedding-v2 的维度


class DashScopeConfig(BaseSettings):
    """阿里云百炼配置"""
    model_config = SettingsConfigDict(env_prefix="DASHSCOPE_")
    
    api_key: str
    chat_model: str = "qwen-plus"
    embedding_model: str = "text-embedding-v2"


class MCPConfig(BaseSettings):
    """MCP 服务配置"""
    model_config = SettingsConfigDict(env_prefix="MCP_")
    
    host: str = "0.0.0.0"
    port: int = 8000


class AppSettings(BaseSettings):
    """应用全局配置"""
    model_config = SettingsConfigDict(
        case_sensitive=False
    )
    
    app_name: str = "Suiyuan AI Agent Server"
    version: str = "1.0.0"
    debug: bool = False
    java_api_base_url: str = "http://localhost:8080"
    
    # 子配置
    milvus: MilvusConfig = MilvusConfig()
    dashscope: DashScopeConfig = DashScopeConfig()
    mcp: MCPConfig = MCPConfig()


# 全局配置实例
settings = AppSettings()
