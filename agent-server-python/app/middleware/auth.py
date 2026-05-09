import hmac
from fastapi import Request, HTTPException, Depends
from starlette.middleware.base import BaseHTTPMiddleware
from app.config.settings import settings
from app.utils.logger import app_logger


class ServiceAuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        if request.url.path.startswith("/mcp"):
            service_token = request.headers.get("X-Service-Token", "")
            expected_token = settings.mcp.service_token

            if not expected_token:
                app_logger.warning("MCP service token not configured")
                raise HTTPException(status_code=500, detail="Service token not configured")

            if not service_token or not hmac.compare_digest(service_token, expected_token):
                app_logger.warning(f"Unauthorized MCP access from {request.client.host}")
                raise HTTPException(status_code=401, detail="Unauthorized")

        response = await call_next(request)
        return response


async def verify_service_token(request: Request):
    service_token = request.headers.get("X-Service-Token", "")
    expected_token = settings.mcp.service_token

    if not expected_token:
        app_logger.warning("Service token not configured")
        raise HTTPException(status_code=500, detail="Service token not configured")

    if not service_token or not hmac.compare_digest(service_token, expected_token):
        app_logger.warning(f"Unauthorized access from {request.client.host}")
        raise HTTPException(status_code=401, detail="Unauthorized")
