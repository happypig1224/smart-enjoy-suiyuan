"""
意图识别节点
使用规则引擎 + LLM 兜底的方式识别用户意图
"""
import re
from langchain_core.messages import HumanMessage
from app.core.state import SuiyuanAgentState
from app.agent.tools.llm import llm
from app.utils.logger import app_logger


def intent_recognition_node(state: SuiyuanAgentState) -> dict:
    """
    意图识别节点（漏斗式识别）
    
    Args:
        state: Agent 状态
    
    Returns:
        包含意图的状态更新
    """
    query = state["query"]
    app_logger.info(f"开始意图识别: {query}")
    
    if re.search(r'(丢了|捡到|遗失|寻物|招领|钱包|饭卡|钥匙)', query):
        app_logger.info("规则匹配: lost_found")
        return {"intent": "lost_found"}
    
    if re.search(r'(资料|复习|PPT|笔记|真题|教程|试卷)', query):
        app_logger.info("规则匹配: resource_search")
        return {"intent": "resource_search"}
    
    if re.search(r'(规定|几点|怎么去|地图|校规|放假)', query):
        app_logger.info("规则匹配: campus_guide")
        return {"intent": "campus_guide"}
    
    app_logger.info("规则未匹配，使用 LLM 识别")
    prompt = f"""
    判断用户输入的意图，严格输出以下四个词之一：campus_guide, resource_search, lost_found, general_chat。
    用户输入：{query}
    """
    response = llm.invoke([HumanMessage(content=prompt)])
    intent = response.content.strip().lower()
    
    if intent not in ["campus_guide", "resource_search", "lost_found", "general_chat"]:
        app_logger.warning(f"LLM 返回无效意图: {intent}，使用默认值 general_chat")
        intent = "general_chat"
    
    app_logger.info(f"意图识别结果: {intent}")
    return {"intent": intent}
