@echo off
chcp 65001 >nul
set /p DB_PASSWORD=请输入本机MySQL密码：
set DB_USERNAME=root
java -jar backend\luggage-system-0.0.1-SNAPSHOT.jar
pause