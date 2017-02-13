@echo off

rem setlocal 
rem set "CURRENT_DIR=%cd%"

set java=java
set jarpath=db-autotest-core-0.0.1.jar io.mycat.db.autotest.server.AutoTestServer

set allparam=

:param
set str=%1
if "%str%"=="" (
    goto end
)
set allparam=%allparam% %str%
shift /0
goto param

:end
if "%allparam%"=="" (
    goto eof
)

rem remove left right blank
:intercept_left
if "%allparam:~0,1%"==" " set "allparam=%allparam:~1%"&goto intercept_left

:intercept_right
if "%allparam:~-1%"==" " set "allparam=%allparam:~0,-1%"&goto intercept_right

:eof
echo "%java% -jar %jarpath% %allparam%"
%java% -jar %jarpath% %allparam%
