@echo off
set DIR=%~dp0

set CP=%DIR%/../lib/*
"%DIR%/../runtime/bin/java.exe" -cp "%CP%" tokyo.peya.langjal.cli.Main %*
