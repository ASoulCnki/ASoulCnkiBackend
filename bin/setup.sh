#!/bin/bash
echo '1.开始导入数据....'
#导入数据
mysql -proot -h 0.0.0.0 </etc/mysql/sql/cnki.sql
mysql -proot -h 0.0.0.0 </etc/mysql/sql/reply_data.sql
echo '2.导入数据完毕....'

echo '3.检查数据完整性....'
count=$(mysql -proot -h 0.0.0.0 </etc/mysql/sql/check.sql | sed -n '2p')
if [ $count = 1000 ]; then
  echo '4.导入成功'
else
  echo '4.导入失败'
fi
