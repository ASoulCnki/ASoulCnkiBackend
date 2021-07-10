# 枝网查重系统API 文档(version 1.0)

惯例：

返回的code不为0时即为错误情况。

## 查重api

1. 直接使用评论文本查重

请求类型：post
Url: /v1/api/check

requestData:

```json
{
  "text": "待查重文本"
}
```

reponseBody:

```json
{
  "code": 0,
  "message": "",
  "data": {
    "rate": 0.1145141919780,
    "start_time": 1624237336,
    "end_time": 1624238336,
    "related": [
      [
        1.0,
        {
          "content": "嘉然的脚小小的香香的，不像手经常使用来得灵活，但有一种独特的可爱的笨拙，嫩嫩的脚丫光滑细腻，凌莹剔透，看得见皮肤下面细细的血管与指甲之下粉白的月牙。再高冷的女生小脚也是敏感的害羞的，轻轻挠一挠，她就摇身一变成为娇滴滴的女孩，脚丫像是一把钥匙，轻轻掌握它就能打开女孩子的心灵。",
          "ctime": 1606452746,
          "like_num": 3,
          "m_name": "病名为向晚",
          "mid": 610280758,
          "oid": 462160368203664988,
          "rpid": 3751510417,
          "type_id": 17
        },
        "https://t.bilibili.com/462160368203664988/#reply3751510417"
      ]
    ]
  }
}
```



## 控制api



1. Train 使用json数据训练内存对比库

发起train请求，需要提前在进程工作目录的data目录下存放bilibili_cnki_reply.json文件。系统将使用该文件训练内存对比库。

请求类型：post
Url: /v1/api/data/train

requestData:

```json
{
  "secure_key" : "123456"
}
```

需要指定secure_key

reponseBody:

```json
{
    "code": 0,
    "message": "success",
    "data": {
        "start_time": 1606137506,
        "end_time": 1625365198
    }
}
```

返回当前对比库数据的时间范围。

2. Pull Data 使用数据库数据训练内存对比库

发起pull data请求，系统将根据制定的起始时间拉取评论数据。 指定为0即可拉取全部数据。

请求类型：post
Url: /v1/api/data/pull

requestData:

```json
{
  "secure_key" : "123456",
  "start_time" : 0
}
```

请求需要指定secure_key以及需要拉取数据的起始时间。



reponseBody:

```json
{
    "code": 0,
    "message": "success",
    "data": {
        "start_time": 1606137506,
        "end_time": 1625365198
    }
}
```

返回当前对比库数据的时间范围。

3. Reset 重置内存对比库

发起reset请求，重置内存中的对比库。

请求类型：post

Url: /v1/api/data/reset

requestData:

```json
{
  "secure_key" : "123456"
}
```

请求需要指定secure_key。



reponseBody:

```json
{
    "code": 0,
    "message": "success",
    "data": {
        "start_time": 1606137506,
        "end_time": 1625365198
    }
}
```

返回当前对比库数据的时间范围。