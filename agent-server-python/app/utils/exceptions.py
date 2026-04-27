"""
自定义异常类
"""


class AgentBaseError(Exception):
    """Agent 基础异常"""
    def __init__(self, message: str, code: int = 500):
        self.message = message
        self.code = code
        super().__init__(self.message)


class MilvusConnectionError(AgentBaseError):
    """Milvus 连接异常"""
    def __init__(self, message: str = "Milvus 连接失败"):
        super().__init__(message, code=503)


class MilvusOperationError(AgentBaseError):
    """Milvus 操作异常"""
    def __init__(self, message: str = "Milvus 操作失败"):
        super().__init__(message, code=500)


class KnowledgeBaseError(AgentBaseError):
    """知识库异常"""
    def __init__(self, message: str = "知识库服务异常"):
        super().__init__(message, code=500)


class EmbeddingError(AgentBaseError):
    """Embedding 生成异常"""
    def __init__(self, message: str = "Embedding 生成失败"):
        super().__init__(message, code=500)


class LLMError(AgentBaseError):
    """LLM 调用异常"""
    def __init__(self, message: str = "LLM 调用失败"):
        super().__init__(message, code=500)


class InvalidRequestError(AgentBaseError):
    """无效请求异常"""
    def __init__(self, message: str = "请求参数无效"):
        super().__init__(message, code=400)
