from kb_manager import kb_manager

kb_manager.init_collection()

docs = [
    "绥化学院图书馆的开放时间是周一到周日 08:00 - 22:30，节假日闭馆。进入图书馆必须刷学生校园卡。",
    "如果学生校园卡丢失，请携带学生证立即前往笃信楼306室的一卡通中心办理挂失和补办。"
]

for doc in docs:
    kb_manager.insert_document(doc)