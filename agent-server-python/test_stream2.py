import httpx
import asyncio
import time
import json

async def test_stream():
    """测试 Python Agent 的 /mcp/stream 端点"""
    url = "http://localhost:8000/mcp/stream"
    
    payload = {
        "tool": "chat_agent",
        "params": {
            "query": "你好，简单回复一下",
            "userId": 1,
            "sessionId": 1,
            "history": []
        }
    }
    
    print("=" * 60)
    print("测试 Python Agent /mcp/stream 端点 - 真正的流式输出")
    print("=" * 60)
    
    async with httpx.AsyncClient(timeout=None) as client:
        async with client.stream("POST", url, json=payload) as response:
            print(f"HTTP 状态码: {response.status_code}")
            print(f"Content-Type: {response.headers.get('content-type')}")
            print()
            
            start_time = time.time()
            chunk_count = 0
            first_chunk_time = None
            
            print("开始接收数据块：")
            print("-" * 60)
            
            async for line in response.aiter_lines():
                if line and line.strip():
                    elapsed = time.time() - start_time
                    
                    if first_chunk_time is None:
                        first_chunk_time = elapsed
                        print(f"\n[首包延迟: {elapsed:.2f}s]")
                    
                    chunk_count += 1
                    
                    # 只显示前80个字符
                    display_line = line[:80] + "..." if len(line) > 80 else line
                    print(f"[{elapsed:.2f}s] {display_line}")
            
            total_time = time.time() - start_time
            print()
            print("-" * 60)
            print(f"统计信息：")
            print(f"  - 首包延迟: {first_chunk_time:.2f}s" if first_chunk_time else "  - 首包延迟: N/A")
            print(f"  - 总耗时: {total_time:.2f}s")
            print(f"  - 数据块数: {chunk_count}")
            if first_chunk_time and chunk_count > 1:
                print(f"  - 平均每块间隔: {(total_time - first_chunk_time) / (chunk_count - 1):.3f}s")
            print("=" * 60)

if __name__ == "__main__":
    asyncio.run(test_stream())
