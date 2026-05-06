"""
日志配置模块
提供统一的日志管理
"""
import logging
import sys
from pathlib import Path
from typing import Optional


class TraceIdFilter(logging.Filter):
    def filter(self, record):
        from app.middleware.trace import get_trace_id
        record.trace_id = get_trace_id() or "-"
        return True


def setup_logger(
    name: str,
    log_file: Optional[str] = None,
    level: int = logging.INFO,
    format_string: Optional[str] = None
) -> logging.Logger:
    logger = logging.getLogger(name)
    logger.setLevel(level)

    if format_string is None:
        format_string = '%(asctime)s - [%(trace_id)s] - %(name)s - %(levelname)s - %(message)s'

    formatter = logging.Formatter(format_string)

    if logger.handlers:
        return logger

    trace_filter = TraceIdFilter()

    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(formatter)
    console_handler.addFilter(trace_filter)
    logger.addHandler(console_handler)

    if log_file:
        Path("logs").mkdir(exist_ok=True)
        file_handler = logging.FileHandler(f"logs/{log_file}", encoding='utf-8')
        file_handler.setFormatter(formatter)
        file_handler.addFilter(trace_filter)
        logger.addHandler(file_handler)

    return logger


# 创建应用级别的 logger
app_logger = setup_logger("suiyuan_agent", "app.log")
