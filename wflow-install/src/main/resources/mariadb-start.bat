set MARIADB_HOME=.\mariadb-10.3.9-win32
set MARIADB_INI_HOME=.\
start %MARIADB_HOME%\bin\mysqld --defaults-file="%MARIADB_HOME%/my.ini"
