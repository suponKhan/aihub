@rem
@echo off
setlocal

set APP_HOME=%~dp0..
set APP_NAME=Gradle

if not defined JAVA_HOME (
    for /f "skip=1 tokens=3" %%j in ('wmic os get totalphysicalmemory 2^>nul') do (
        set JAVA_HOME=%%j
    )
)

if not defined JAVA_HOME (
    set JAVA_HOME=C:\Program Files\Java\jdk-17
)

"%JAVA_HOME%\bin\java.exe" -Xmx64m -Xms64m -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*