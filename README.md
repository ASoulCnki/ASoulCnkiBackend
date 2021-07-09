
# A-SOUL评论区小作文 枝网查重系统  后端

源项目：https://github.com/stream2000/ASoulCnki
网站地址：https://asoulcnki.asia

## 本地部署方式

1. 设置数据库并修改application-demo.yml中的数据库地址为对应的数据库。

2. 设置redis并修改application-demo.yml中的redis地址为对应的地址。 

3. 下载数据文件（可以联系项目管理者获取）

4. 启动应用并使用Train Api将数据导入内存中。 

5. 使用nginx或其他方式启动代理前端文件，即可访问网站。当然这不是必须的，可以直接用postman之类的工具访问api。 