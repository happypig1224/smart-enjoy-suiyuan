"""
测试 /mcp/stream 端点的详细错误
"""
import asyncio
import httpx
import json

async def test_stream_detailed():
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
    
    print("发送请求...")
    
    async with httpx.AsyncClient(timeout=httpx.Timeout(120.0)) as client:
        try:
            async with client.stream("POST", url, json=payload) as response:
                print(f"HTTP 状态码: {response.status_code}")
                print(f"Content-Type: {response.headers.get('content-type')}")
                print(f"Headers: {dict(response.headers)}")
                
                # 读取响应体看看有没有错误信息
                body = await response.aread()
                print(f"响应体: {body.decode('utf-8')[:500]}")
                
        except httpx.HTTPStatusError as e:
            print(f"HTTP 错误: {e}")
            print(f"响应体: {e.response.text[:500]}")
        except Exception as e:
            print(f"异常: {type(e).__name__}: {e}")

if __name__ == "__main__":
    asyncio.run(test_stream_detailed())
