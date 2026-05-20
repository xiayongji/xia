@echo off
docker cp smart-home-microservices/init-databases.sql docker-mysql-1:/init-databases.sql
docker exec docker-mysql-1 mysql -u root -proot < /init-databases.sql
echo Database initialization completed!
pause
