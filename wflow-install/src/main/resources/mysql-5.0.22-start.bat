set MYSQL_HOME=.\mysql-5.0.22-win32
set MYSQL_INI_HOME=.\
start %MYSQL_HOME%\bin\mysqld-nt --defaults-file="%MYSQL_HOME%/my.ini"
