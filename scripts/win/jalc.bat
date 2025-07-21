@echo off
set DIR=%~dp0
"%DIR%/../runtime\bin\java.exe" -jar "%DIR%/../tools\jalc.jar" %*
