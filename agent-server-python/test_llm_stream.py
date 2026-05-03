"""
直接测试 LLM 流式输出
"""
import asyncio
from app.agent.tools.llm import create_streaming_llm
from langchain_core.messages import HumanMessage, SystemMessage

async def test_llm_stream():
    print("创建流式 LLM...")
    llm = create_streaming_llm()
    
    conversation = [
        SystemMessage(content="你是助手，简单回复。"),
        HumanMessage(content="你好，简单说句话")
    ]
    
    print("开始流式调用...")
    full_text = ""
    count = 0
    
    async for chunk in llm.astream(conversation):
        count += 1
        if hasattr(chunk, 'content') and chunk.content:
            content = chunk.content
            full_text += content
            print(f"[{count}] {content}", flush=True)
    
    print(f"\n总 token 数: {count}")
    print(f"完整回复: {full_text}")

if __name__ == "__main__":
    asyncio.run(test_llm_stream())
