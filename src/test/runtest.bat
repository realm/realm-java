@echo off
echo %1
call mvn test -P!generate-code -Dtest=%1 | grep EXCEPTION
call mvn test -P!generate-code -Dtest=%1 | grep EXCEPTION
call mvn test -P!generate-code -Dtest=%1 | grep EXCEPTION
