# A-SOUL评论区小作文 枝网查重系统 后端

[![Publish Docker](https://github.com/ASoulCnki/ASoulCnkiBackend/actions/workflows/build-docker.yml/badge.svg?branch=master)](https://github.com/ASoulCnki/ASoulCnkiBackend/actions/workflows/build-docker.yml)

源项目：https://github.com/ASoulCnki/ASoulCnki
网站地址：https://asoulcnki.asia

## 部署

### 1 系统要求

1. Java 8
2. Java堆内存1500M及其以上

### 2 运行

#### 2.1 依赖json文件运行

#### 2.1.1 （方法一）快速开始
1. 将 [bilibili_cnki_reply.json](https://drive.google.com/file/d/151oz560vj2T2uwxYrRbxq1NPYwvx_dNf/view?usp=sharing) 放入项目根目录下的 data 文件夹
2. 运行 src/test/asia/asoulcnki.api/StartTrainTest.testStartTrain 开始训练
3. 训练完毕后，运行 ApiApplication 启动服务，即可正常使用

#### 2.1.2 （方法二）使用其他语言调接口训练
1. 修改application-demo.yml中的secure.key
2. 将bilibili_cnki_reply.json([样本](https://drive.google.com/file/d/151oz560vj2T2uwxYrRbxq1NPYwvx_dNf/view?usp=sharing))放入data文件夹
3. 运行springboot后端
4. 调用后端train接口训练数据 训练需要较长时间(约一分钟)  
   [示例请求](./dev/start_train.py)(python):

```python
import requests
CONTROL_SECURE_KEY = "123456" #注意修改为application-demo.yml中的secure.key
base_url = "http://localhost:8000/v1/api/data/train"
r = requests.post(base_url, json={'secure_key': CONTROL_SECURE_KEY, 'start_time': 0})
print(r.json())
```

#### 2.2 依赖数据库运行

需要预先安装docker

```shell
sh bin/start.sh # 启动docker
sh bin/do_setup.sh # 初始化数据库, 需要等待mysql初始化完成，约10s
```

如果想清理数据文件, 执行下面命令即可

```shell
sh bin/cleanup.sh 
```

在初始化数据库完成后，启动项目，可以按照2.1的方式训练数据，也可以参照api文档从数据库拉取数据来训练

#### 2.3 使用docker运行

```shell
# 构建镜像
mvn clean package docker:build
# 或者
# docker pull registry.cn-hangzhou.aliyuncs.com/asoulcnki/api:latest

docker run -e PROFILES=demo -p 8000:8000 -d -v host-path-to-data-dir:/opt/data registry.cn-hangzhou.aliyuncs.com/asoulcnki/api:latest
 # 暴露本机 8000 端口，启动docker
```

## API文档

参见[API文档](./api.md)
