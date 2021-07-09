
# A-SOUL评论区小作文 枝网查重系统  后端

源项目：https://github.com/stream2000/ASoulCnki
网站地址：https://asoulcnki.asia

## 部署
### 1 系统要求
1. Java 8
2. Java堆内存1500M及其以上
### 2 运行
#### 2.1 依赖json文件运行  
1. 修改application-demo.yml中的secure.key
2. 将bilibili_cnki_reply.json放入data文件夹(json数据文件可以联系项目管理者获取)  
3. 运行springboot后端
4. 调用后端train接口训练数据 训练需要较长时间(约一分钟)  
示例请求(python):
```python
import requests
CONTROL_SECURE_KEY = "123456" #注意修改为application-demo.yml中的secure.key
base_url = "http://localhost:8000/v1/api/data/train"
r = requests.post(base_url, json={'secure_key': CONTROL_SECURE_KEY, 'start_time': 0})
print(r.json())
```
#### 2.2 依赖数据库运行  
TODO
