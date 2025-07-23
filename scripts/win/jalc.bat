@echo off
set DIR=%~dp0

set CP=%DIR%/../tools/jalc.jar;%DIR%/../tools/lib/*
"%DIR%/../runtime/bin/java.exe" -cp "%CP%" tokyo.peya.langjal.cli.Main %*
