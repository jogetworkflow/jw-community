set MARIADB_HOME=.\mariadb-10.6.12-winx64
set MARIADB_INI_HOME=.\
start %MARIADB_HOME%\bin\mysqld --defaults-file="%MARIADB_HOME%\my.ini" --console
