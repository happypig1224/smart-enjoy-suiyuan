from pymilvus import connections, Collection, FieldSchema, CollectionSchema, DataType

# 尝试连接到本地 Milvus 服务（需要先启动）
try:
    connections.connect("default", host="localhost", port="19530")
    print("成功连接到 Milvus 服务")
except Exception as e:
    print(f"连接失败: {e}")
