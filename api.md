# 枝网查重系统API 文档(version 1.0)

### 惯例

1. 返回的code不为0时即为错误情况。
2. 非特殊说明，请求参数均为必选项，可选的参数会在解释中单独标注

## 目录

- [查重API](#查重API)
- [作文展API](#作文展API)
- [控制API](#控制API)

## 查重api

请求类型：POST

请求地址：/v1/api/check

Content-Type: application/json

### 请求参数

| 参数名称 | 释义 | 备注 |
| --- | --- | --- |
| text | 查重文本 | 长度在10-1000之间 |

### 响应参数

注：由于related中的reply进行复用，所以单独解释

| 参数名称 | 释义 | 备注 |
| --- | --- | --- |
| rate | 查重小作文的重复率 | 范围0-1，1为100% |
| message | 查询状态的相关信息，默认success | ​<br /> |
| code | 查询状态码 | 0以外的值为查询失败 |
| start_time | 查询范围的起始时间 | 10位时间戳(秒) |
| end_time | 查询范围的结束时间 | 10位时间戳(秒)，最后小作文收录的时间 |
| related | 重复文本的数组 | 该部分和其他接口复用，下文单独解释 |

#### related 部分

| 参数名称 | 释义 | 备注 |
| --- | --- | --- |
| rate | 原文从本评论中复制的占比 | 仅查重API使用 <br />范围0-1，1为100% |
| reply_url | 指向该条评论的链接 | 该链接可以根据typeid和oid自行构造 |
| reply.rpid | 该评论的回复ID | 0以外的值为查询失败 |
| reply.type_id | 评论的类型ID | 1：视频 <br />12：专栏 <br />11/17：动态 |
| reply.uid | 动态/视频/专栏 发布者的UID |  |
| reply.oid | 动态/视频/专栏 对应的ID | 对于视频是AV号<br />对于动态是动态ID<br />对于专栏是CV号 |
| reply.ctime | 评论的创建时间 | 10位时间戳(秒) |
| reply.mid | 评论发布者的UID |  |
| reply.m_name | 评论发布者的用户名 |  |
| reply.content | 评论正文 |  |
| reply.like_num | 评论获得的点赞数 |  |
| reply.origin_rpid | 该评论引用的评论的rpid | 如果是原创，该项为 -1 |
| reply.similar_count | 与该评论相似的评论数 |  |
| reply.similar_like_sum | 该评论赞数 + 所有相似评论赞数 | 如果该评论非原创，则不会累加 |

### 样例

请求样例

```json
{
  "text": "待查重文本，字数介于10-1000之间"
}
```

响应样例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "rate": 1,
    "start_time": 1606137506,
    "end_time": 1628654399,
    "related": [
      {
        "rate": 1,
        "reply": {
          "rpid": "5004265317",
          "type_id": 17,
          "dynamic_id": "552086903093256371",
          "mid": 5421504,
          "uid": 672328094,
          "oid": "552086903093256371",
          "ctime": 1627381220,
          "m_name": "恰柠檬儿丶",
          "content": "11111111111 0 \n1111111111111 0 111 0\n1111111111111111111",
          "like_num": 166,
          "origin_rpid": "-1",
          "similar_count": 0,
          "similar_like_sum": 166
        },
        "reply_url": " https://t.bilibili.com/552086903093256371/#reply5004265317"
      }
    ]
  }
}
```

## 作文展API

请求类型：GET

请求地址：/v1/api/check

### 请求参数

| 参数名称 | 释义 | 备注 |
| --- | --- | --- |
| pageSize | 每页展示的评论数 | 目前仅支持pageSize=10 |
| pageNum | 评论页码 | 从1开始 |
| timeRangeMode | 时间范围选择 | 0 全部时间<br />1 一周内<br />2 三天以内 |
| sortMode | 排序模式 | 0 总点赞数（参见related.reply.similar_like_num）<br />1 点赞数<br />2 相似小作文数（引用次数） |
| ids | 指定动态发布者（非评论） | 可选参数，默认为全部<br />传值为uid数组 例：ids=uid1,uid2..... |
| keywords | 指定关键词 | 可选参数，默认无限制条件<br />传值为关键词数组 例 keywords=k1,k2,...<br />每个关键词不超过10个字符 最多支持三个关键词 |

### 响应参数

| 参数名称 | 释义 | 备注 |
| --- | --- | --- |
| code | 见查重API |  |
| message | 见查重API |  |
| data.all_count | 查到评论的总数 | 用于计算页数 |
| data.replies | 见查重API |  |
| data.start_time | 评论起始时间 | 10位时间戳(秒) |
| data.end_time | 评论结束时间 | 10位时间戳(秒) |


### 样例

请求样例

```http
GET /v1/api/ranking/?pageSize=5&pageNum=1&timeRangeMode=0&sortMode=0&ids=&keywords= HTTP/1.1
```

响应样例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "replies": [
      {
        "rpid": "5004265317",
        "type_id": 17,
        "dynamic_id": "552086903093256371",
        "mid": 5421504,
        "uid": 672328094,
        "oid": "552086903093256371",
        "ctime": 1627381220,
        "m_name": "恰柠檬儿丶",
        "content": "11111111111 0 \n1111111111111 0 111 0\n1111111111111111111",
        "like_num": 166,
        "origin_rpid": "-1",
        "similar_count": 0,
        "similar_like_sum": 166
      },
    ],
    "all_count": 5379,
    "start_time": 1606137506,
    "end_time": 1628654399
  }
}
```


## 控制API



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