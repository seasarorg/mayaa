@echo off
call mvn package 
copy /Y target\mayaa-1.1.25-SNAPSHOT.jar C:\Users\katochin\workspace_gae\petitcloud_mayaa\war\WEB-INF\lib
