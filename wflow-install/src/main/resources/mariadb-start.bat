set MARIADB_HOME=.\mariadb-10.2.10-win32
set MARIADB_INI_HOME=.\
start %MARIADB_HOME%\bin\mysqld --defaults-file="%MARIADB_HOME%/my.ini"
