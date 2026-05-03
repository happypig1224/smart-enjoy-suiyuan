import requests
import json
import time

# 测试 Python Agent 的 /mcp/stream 端点
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
print("测试 Python Agent /mcp/stream 端点 - 流式输出")
print("=" * 60)

try:
    response = requests.post(url, json=payload, stream=True)
    print(f"HTTP 状态码: {response.status_code}")
    print(f"Content-Type: {response.headers.get('Content-Type')}")
    print()
    
    start_time = time.time()
    chunk_count = 0
    first_chunk_time = None
    
    print("开始接收数据块：")
    print("-" * 60)
    
    for line in response.iter_lines():
        if line:
            line_str = line.decode('utf-8')
            elapsed = time.time() - start_time
            
            if first_chunk_time is None:
                first_chunk_time = elapsed
                print(f"\n[首包延迟: {elapsed:.2f}s]")
            
            chunk_count += 1
            print(f"[{elapsed:.2f}s] {line_str[:100]}...")
    
    total_time = time.time() - start_time
    print()
    print("-" * 60)
    print(f"统计信息：")
    print(f"  - 首包延迟: {first_chunk_time:.2f}s")
    print(f"  - 总耗时: {total_time:.2f}s")
    print(f"  - 数据块数: {chunk_count}")
    print(f"  - 平均每块间隔: {(total_time - first_chunk_time) / max(chunk_count - 1, 1):.3f}s")
    
except Exception as e:
    print(f"错误: {e}")
    import traceback
    traceback.print_exc()
