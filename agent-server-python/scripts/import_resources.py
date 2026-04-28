# scripts/import_resources.py
from app.services.knowledge_base import KnowledgeBaseService

kb_service = KnowledgeBaseService()

# 示例：导入学习资源
resources = [
    "《Java核心技术卷I》PDF电子版，包含基础知识、对象定向编程、泛型、集合框架等内容，适合Java初学者和中级开发者。",
    "《高等数学期末复习真题》2023-2024学年，包含选择题、填空题、计算题共50道，附带详细答案解析。",
    "Python编程入门教程，从基础语法到实战项目，包含视频讲解和代码示例，适合零基础学习者。"
]

for resource in resources:
    kb_service.insert_document(resource, doc_type="resource")
    print(f"已导入: {resource[:30]}...")
