"""
Milvus连接管理器
单例模式管理Milvus连接，避免连接泄露
"""
import threading
from pymilvus import connections, utility
from app.config.settings import settings
from app.utils.logger import app_logger
from app.utils.exceptions import MilvusConnectionError


class MilvusConnectionManager:
    _instance = None
    _lock = threading.Lock()
    _connected = False

    @classmethod
    def get_instance(cls):
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = cls()
        return cls._instance

    def ensure_connection(self):
        if not self._connected:
            with self._lock:
                if not self._connected:
                    try:
                        connections.connect(
                            "default",
                            host=settings.milvus.host,
                            port=settings.milvus.port
                        )
                        self._connected = True
                        app_logger.info(f"Milvus连接已建立: {settings.milvus.host}:{settings.milvus.port}")
                    except Exception as e:
                        app_logger.error(f"Milvus连接失败: {e}")
                        raise MilvusConnectionError(f"无法连接到Milvus: {e}")

    def disconnect(self):
        if self._connected:
            with self._lock:
                if self._connected:
                    try:
                        connections.disconnect("default")
                        self._connected = False
                        app_logger.info("Milvus连接已释放")
                    except Exception as e:
                        app_logger.error(f"Milvus断开连接失败: {e}")

    @property
    def is_connected(self):
        return self._connected
