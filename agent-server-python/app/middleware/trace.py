import uuid
import contextvars
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request

_trace_id_var: contextvars.ContextVar[str] = contextvars.ContextVar("trace_id", default="")


def get_trace_id() -> str:
    return _trace_id_var.get()


class TraceIdMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        trace_id = request.headers.get("X-Trace-Id", "")
        if not trace_id:
            trace_id = uuid.uuid4().hex

        _trace_id_var.set(trace_id)

        response = await call_next(request)
        response.headers["X-Trace-Id"] = trace_id
        return response
