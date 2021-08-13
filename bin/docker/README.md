创建索引
```
PUT /as?pretty
{
  
    "settings" : {
        "number_of_shards" : "5",
        "analysis" : {
        "analyzer": {
          "default": {
            "type": "ik_smart"
          }
        }
        },
        "number_of_replicas" : "0"
      }
    }
```

启动logstash后需要进入容器查看是否可以连接es7和mysql8

docker-compose 中采用的是`depends_on`, 如果不行可以采用`links`

安装Ruby环境

`docker exec -it --user root logstash7 /bin/bash`

`yum install ruby -y`

`cd /usr/share/logstash/config/`

`logstash -f jdbc.conf --path.data=/root/`

---------------或者--------------------------

`nohup logstash -f jdbc.conf --path.data=/root/ > logstash.log &`