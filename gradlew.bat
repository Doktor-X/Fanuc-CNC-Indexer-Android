@echo off
set DIRNAME=%~dp0
set JAVA_EXE=java.exe
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
