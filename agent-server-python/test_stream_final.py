"""
测试 Python Agent 流式输出
使用 httpx 进行真正的流式请求
"""
import httpx
import time
import json

def test_stream_endpoint():
    """测试 /mcp/stream 端点"""
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
    print("测试 Python Agent /mcp/stream 端点")
    print("=" * 60)
    
    try:
        # 使用 httpx 进行流式请求
        with httpx.Client(timeout=httpx.Timeout(120.0)) as client:
            with client.stream("POST", url, json=payload) as response:
                print(f"HTTP 状态码: {response.status_code}")
                print(f"Content-Type: {response.headers.get('content-type')}")
                print(f"Transfer-Encoding: {response.headers.get('transfer-encoding')}")
                print()
                
                start_time = time.time()
                chunk_count = 0
                first_chunk_time = None
                full_response = ""
                
                print("开始接收数据块：")
                print("-" * 60)
                
                for line in response.iter_lines():
                    if line and line.strip():
                        elapsed = time.time() - start_time
                        
                        if first_chunk_time is None:
                            first_chunk_time = elapsed
                            print(f"\n[首包延迟: {elapsed:.2f}s]")
                        
                        chunk_count += 1
                        
                        # 解析 SSE 数据
                        if line.startswith("data:"):
                            data = line[5:].strip()
                            if data == "[DONE]":
                                print(f"[{elapsed:.2f}s] [完成]")
                                break
                            elif data.startswith("{"):
                                try:
                                    parsed = json.loads(data)
                                    content = parsed.get("content", "")
                                    full_response += content
                                    # 只显示前60个字符
                                    display = content[:60] + ("..." if len(content) > 60 else "")
                                    print(f"[{elapsed:.2f}s] {display}")
                                except json.JSONDecodeError:
                                    print(f"[{elapsed:.2f}s] {line[:80]}")
                            else:
                                print(f"[{elapsed:.2f}s] {line[:80]}")
                
                total_time = time.time() - start_time
                print()
                print("-" * 60)
                print("统计信息：")
                print(f"  - 首包延迟: {first_chunk_time:.2f}s" if first_chunk_time else "  - 首包延迟: N/A")
                print(f"  - 总耗时: {total_time:.2f}s")
                print(f"  - 数据块数: {chunk_count}")
                print(f"  - 完整回复长度: {len(full_response)} 字符")
                print()
                print("完整回复内容：")
                print(full_response)
                print("=" * 60)
                
                # 判断是否成功
                if first_chunk_time and total_time > first_chunk_time:
                    print("✅ 流式输出测试通过！内容逐块到达。")
                elif chunk_count > 0:
                    print("✅ 收到数据，但可能是批量到达而非流式。")
                else:
                    print("❌ 未收到任何数据块。")
    
    except Exception as e:
        print(f" 测试失败: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_stream_endpoint()
