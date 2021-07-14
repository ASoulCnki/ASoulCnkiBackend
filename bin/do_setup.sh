docker exec -it $(docker ps -a | grep mysql8 | awk '{print $1}') /bin/bash /etc/mysql/bin/setup.sh
