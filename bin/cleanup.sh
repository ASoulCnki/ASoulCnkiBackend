docker ps -a | grep redis | awk '{print $1}' | xargs docker rm -f
docker ps -a | grep mysql8 | awk '{print $1}' | xargs docker rm -f
rm -rf docker/mysql/data/*
rm -rf docker/redis/data/*
