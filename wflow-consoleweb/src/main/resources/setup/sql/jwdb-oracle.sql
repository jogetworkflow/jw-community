--------------------------------------------------------
--  DDL for Table APP_APP
--------------------------------------------------------

  CREATE TABLE "APP_APP" 
   (	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" NUMBER(19,0), 
	"NAME" VARCHAR2(255 CHAR), 
	"PUBLISHED" NUMBER(1,0), 
	"DATECREATED" TIMESTAMP (6), 
	"DATEMODIFIED" TIMESTAMP (6),
	"LICENSE" VARCHAR2(4000)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_DATALIST
--------------------------------------------------------

  CREATE TABLE "APP_DATALIST" 
   (	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" NUMBER(19,0), 
	"ID" VARCHAR2(255 CHAR), 
	"NAME" VARCHAR2(255 CHAR), 
	"DESCRIPTION" CLOB, 
	"JSON" CLOB, 
	"DATECREATED" TIMESTAMP (6), 
	"DATEMODIFIED" TIMESTAMP (6)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_ENV_VARIABLE
--------------------------------------------------------

  CREATE TABLE "APP_ENV_VARIABLE" 
   (	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" NUMBER(19,0), 
	"ID" VARCHAR2(255 CHAR), 
	"VALUE" CLOB, 
	"REMARKS" CLOB
   ) ;
--------------------------------------------------------
--  DDL for Table APP_FD
--------------------------------------------------------

  CREATE TABLE "APP_FD" 
   (	"ID" VARCHAR2(255 CHAR), 
	"DATECREATED" TIMESTAMP (6), 
	"DATEMODIFIED" TIMESTAMP (6)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_FORM
--------------------------------------------------------

  CREATE TABLE "APP_FORM" 
   (	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" NUMBER(19,0), 
	"FORMID" VARCHAR2(255 CHAR), 
	"NAME" VARCHAR2(255 CHAR), 
	"DATECREATED" TIMESTAMP (6), 
	"DATEMODIFIED" TIMESTAMP (6), 
	"TABLENAME" VARCHAR2(255 CHAR), 
	"JSON" CLOB
   ) ;
--------------------------------------------------------
--  DDL for Table APP_MESSAGE
--------------------------------------------------------

  CREATE TABLE "APP_MESSAGE" 
   (	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" NUMBER(19,0), 
	"OUID" VARCHAR2(255 CHAR), 
	"MESSAGEKEY" VARCHAR2(255 CHAR), 
	"LOCALE" VARCHAR2(255 CHAR), 
	"MESSAGE" CLOB
   ) ;
--------------------------------------------------------
--  DDL for Table APP_PACKAGE
--------------------------------------------------------

  CREATE TABLE "APP_PACKAGE" 
   (	"PACKAGEID" VARCHAR2(255 CHAR), 
	"PACKAGEVERSION" NUMBER(19,0), 
	"NAME" VARCHAR2(255 CHAR), 
	"DATECREATED" TIMESTAMP (6), 
	"DATEMODIFIED" TIMESTAMP (6), 
	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" NUMBER(19,0)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_PACKAGE_ACTIVITY_FORM
--------------------------------------------------------

  CREATE TABLE "APP_PACKAGE_ACTIVITY_FORM" 
   (	"PROCESSDEFID" VARCHAR2(255 CHAR), 
	"ACTIVITYDEFID" VARCHAR2(255 CHAR), 
	"PACKAGEID" VARCHAR2(255 CHAR), 
	"PACKAGEVERSION" NUMBER(19,0), 
	"OUID" VARCHAR2(255 CHAR), 
	"TYPE" VARCHAR2(255 CHAR), 
	"FORMID" VARCHAR2(255 CHAR), 
	"FORMURL" VARCHAR2(255 CHAR), 
	"FORMIFRAMESTYLE" VARCHAR2(255 CHAR), 
	"AUTOCONTINUE" NUMBER(1,0)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_PACKAGE_ACTIVITY_PLUGIN
--------------------------------------------------------

  CREATE TABLE "APP_PACKAGE_ACTIVITY_PLUGIN" 
   (	"PROCESSDEFID" VARCHAR2(255 CHAR), 
	"ACTIVITYDEFID" VARCHAR2(255 CHAR), 
	"PACKAGEID" VARCHAR2(255 CHAR), 
	"PACKAGEVERSION" NUMBER(19,0), 
	"OUID" VARCHAR2(255 CHAR), 
	"PLUGINNAME" VARCHAR2(255 CHAR), 
	"PLUGINPROPERTIES" CLOB
   ) ;
--------------------------------------------------------
--  DDL for Table APP_PACKAGE_PARTICIPANT
--------------------------------------------------------

  CREATE TABLE "APP_PACKAGE_PARTICIPANT" 
   (	"PROCESSDEFID" VARCHAR2(255 CHAR), 
	"PARTICIPANTID" VARCHAR2(255 CHAR), 
	"PACKAGEID" VARCHAR2(255 CHAR), 
	"PACKAGEVERSION" NUMBER(19,0), 
	"OUID" VARCHAR2(255 CHAR), 
	"TYPE" VARCHAR2(255 CHAR), 
	"VALUE" CLOB, 
	"PLUGINPROPERTIES" CLOB
   ) ;
--------------------------------------------------------
--  DDL for Table APP_PLUGIN_DEFAULT
--------------------------------------------------------

  CREATE TABLE "APP_PLUGIN_DEFAULT" 
   (	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" NUMBER(19,0), 
	"ID" VARCHAR2(255 CHAR), 
	"PLUGINNAME" VARCHAR2(255 CHAR), 
	"PLUGINDESCRIPTION" CLOB, 
	"PLUGINPROPERTIES" CLOB
   ) ;
--------------------------------------------------------
--  DDL for Table APP_REPORT_ACTIVITY
--------------------------------------------------------

  CREATE TABLE "APP_REPORT_ACTIVITY" 
   (	"UUID" VARCHAR2(255 CHAR), 
	"ACTIVITYDEFID" VARCHAR2(255 CHAR), 
	"ACTIVITYNAME" VARCHAR2(255 CHAR), 
	"PROCESSUID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_REPORT_ACTIVITY_INSTANCE
--------------------------------------------------------

  CREATE TABLE "APP_REPORT_ACTIVITY_INSTANCE" 
   (	"INSTANCEID" VARCHAR2(255 CHAR), 
	"PERFORMER" VARCHAR2(255 CHAR), 
	"STATE" VARCHAR2(255 CHAR), 
	"STATUS" VARCHAR2(255 CHAR), 
	"NAMEOFACCEPTEDUSER" VARCHAR2(255 CHAR), 
	"ASSIGNMENTUSERS" CLOB, 
	"DUE" TIMESTAMP (6), 
	"CREATEDTIME" TIMESTAMP (6), 
	"STARTEDTIME" TIMESTAMP (6), 
	"FINISHTIME" TIMESTAMP (6), 
	"DELAY" NUMBER(19,0), 
	"TIMECONSUMINGFROMCREATEDTIME" NUMBER(19,0), 
	"TIMECONSUMINGFROMSTARTEDTIME" NUMBER(19,0), 
	"ACTIVITYUID" VARCHAR2(255 CHAR), 
	"PROCESSINSTANCEID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_REPORT_APP
--------------------------------------------------------

  CREATE TABLE "APP_REPORT_APP" 
   (	"UUID" VARCHAR2(255 CHAR), 
	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" VARCHAR2(255 CHAR), 
	"APPNAME" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_REPORT_PACKAGE
--------------------------------------------------------

  CREATE TABLE "APP_REPORT_PACKAGE" 
   (	"UUID" VARCHAR2(255 CHAR), 
	"PACKAGEID" VARCHAR2(255 CHAR), 
	"PACKAGENAME" VARCHAR2(255 CHAR), 
	"PACKAGEVERSION" VARCHAR2(255 CHAR), 
	"APPUID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_REPORT_PROCESS
--------------------------------------------------------

  CREATE TABLE "APP_REPORT_PROCESS" 
   (	"UUID" VARCHAR2(255 CHAR), 
	"PROCESSDEFID" VARCHAR2(255 CHAR), 
	"PROCESSNAME" VARCHAR2(255 CHAR), 
	"PACKAGEUID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_REPORT_PROCESS_INSTANCE
--------------------------------------------------------

  CREATE TABLE "APP_REPORT_PROCESS_INSTANCE" 
   (	"INSTANCEID" VARCHAR2(255 CHAR), 
	"REQUESTER" VARCHAR2(255 CHAR), 
	"STATE" VARCHAR2(255 CHAR), 
	"DUE" TIMESTAMP (6), 
	"STARTEDTIME" TIMESTAMP (6), 
	"FINISHTIME" TIMESTAMP (6), 
	"DELAY" NUMBER(19,0), 
	"TIMECONSUMINGFROMSTARTEDTIME" NUMBER(19,0), 
	"PROCESSUID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table APP_USERVIEW
--------------------------------------------------------

  CREATE TABLE "APP_USERVIEW" 
   (	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" NUMBER(19,0), 
	"ID" VARCHAR2(255 CHAR), 
	"NAME" VARCHAR2(255 CHAR), 
	"DESCRIPTION" CLOB, 
	"JSON" CLOB, 
	"DATECREATED" TIMESTAMP (6), 
	"DATEMODIFIED" TIMESTAMP (6)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_DEPARTMENT
--------------------------------------------------------

  CREATE TABLE "DIR_DEPARTMENT" 
   (	"ID" VARCHAR2(255 CHAR), 
	"NAME" VARCHAR2(255 CHAR), 
	"DESCRIPTION" VARCHAR2(255 CHAR), 
	"ORGANIZATIONID" VARCHAR2(255 CHAR), 
	"HOD" VARCHAR2(255 CHAR), 
	"PARENTID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_EMPLOYMENT
--------------------------------------------------------

  CREATE TABLE "DIR_EMPLOYMENT" 
   (	"ID" VARCHAR2(255 CHAR), 
	"USERID" VARCHAR2(255 CHAR), 
	"STARTDATE" DATE, 
	"ENDDATE" DATE, 
	"EMPLOYEECODE" VARCHAR2(255 CHAR), 
	"ROLE" VARCHAR2(255 CHAR), 
	"GRADEID" VARCHAR2(255 CHAR), 
	"DEPARTMENTID" VARCHAR2(255 CHAR), 
	"ORGANIZATIONID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_EMPLOYMENT_REPORT_TO
--------------------------------------------------------

  CREATE TABLE "DIR_EMPLOYMENT_REPORT_TO" 
   (	"ID" VARCHAR2(255 CHAR), 
	"EMPLOYMENTID" VARCHAR2(255 CHAR), 
	"REPORTTOID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_GRADE
--------------------------------------------------------

  CREATE TABLE "DIR_GRADE" 
   (	"ID" VARCHAR2(255 CHAR), 
	"NAME" VARCHAR2(255 CHAR), 
	"DESCRIPTION" VARCHAR2(255 CHAR), 
	"ORGANIZATIONID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_GROUP
--------------------------------------------------------

  CREATE TABLE "DIR_GROUP" 
   (	"ID" VARCHAR2(255 CHAR), 
	"NAME" VARCHAR2(255 CHAR), 
	"DESCRIPTION" VARCHAR2(255 CHAR), 
	"ORGANIZATIONID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_ORGANIZATION
--------------------------------------------------------

  CREATE TABLE "DIR_ORGANIZATION" 
   (	"ID" VARCHAR2(255 CHAR), 
	"NAME" VARCHAR2(255 CHAR), 
	"DESCRIPTION" VARCHAR2(255 CHAR), 
	"PARENTID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_ROLE
--------------------------------------------------------

  CREATE TABLE "DIR_ROLE" 
   (	"ID" VARCHAR2(255 CHAR), 
	"NAME" VARCHAR2(255 CHAR), 
	"DESCRIPTION" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_USER
--------------------------------------------------------

  CREATE TABLE "DIR_USER" 
   (	"ID" VARCHAR2(255 CHAR), 
	"USERNAME" VARCHAR2(255 CHAR), 
	"PASSWORD" VARCHAR2(255 CHAR), 
	"FIRSTNAME" VARCHAR2(255 CHAR), 
	"LASTNAME" VARCHAR2(255 CHAR), 
	"EMAIL" VARCHAR2(255 CHAR), 
	"TIMEZONE" VARCHAR2(255 CHAR), 
	"LOCALE" VARCHAR2(255 CHAR), 
	"ACTIVE" NUMBER(10,0)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_USER_EXTRA
--------------------------------------------------------

  CREATE TABLE "DIR_USER_EXTRA" 
   (	"USERNAME" VARCHAR2(255 CHAR), 
	"ALGORITHM" VARCHAR2(255 CHAR), 
	"LOGINATTEMPT" NUMBER(10,0), 
	"FAILEDLOGINATTEMPT" NUMBER(10,0), 
	"LASTLOGEDINDATE" TIMESTAMP (6), 
	"LOCKOUTDATE" TIMESTAMP (6), 
	"LASTPASSWORDCHANGEDATE" TIMESTAMP (6), 
	"REQUIREDPASSWORDCHANGE" NUMBER(10,0),
	"NOPASSWORDEXPIRATION" NUMBER(10,0)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_USER_PASSWORD_HISTORY
--------------------------------------------------------

  CREATE TABLE "DIR_USER_PASSWORD_HISTORY" 
   (	"ID" VARCHAR2(255 CHAR), 
	"USERNAME" VARCHAR2(255 CHAR), 
	"SALT" VARCHAR2(255 CHAR), 
	"PASSWORD" VARCHAR2(255 CHAR), 
	"UPDATEDDATE" TIMESTAMP (6)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_USER_GROUP
--------------------------------------------------------

  CREATE TABLE "DIR_USER_GROUP" 
   (	"GROUPID" VARCHAR2(255 CHAR), 
	"USERID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table DIR_USER_ROLE
--------------------------------------------------------

  CREATE TABLE "DIR_USER_ROLE" 
   (	"ROLEID" VARCHAR2(255 CHAR), 
	"USERID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table OBJECTID
--------------------------------------------------------

  CREATE TABLE "OBJECTID" 
   (	"NEXTOID" NUMBER(19,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKACTIVITIES
--------------------------------------------------------

  CREATE TABLE "SHKACTIVITIES" 
   (	"ID" VARCHAR2(100), 
	"ACTIVITYSETDEFINITIONID" VARCHAR2(90), 
	"ACTIVITYDEFINITIONID" VARCHAR2(90), 
	"PROCESS" NUMBER(19,0), 
	"THERESOURCE" NUMBER(19,0), 
	"PDEFNAME" VARCHAR2(200), 
	"PROCESSID" VARCHAR2(200), 
	"RESOURCEID" VARCHAR2(100), 
	"STATE" NUMBER(19,0), 
	"BLOCKACTIVITYID" VARCHAR2(100), 
	"PERFORMER" VARCHAR2(100), 
	"ISPERFORMERASYNCHRONOUS" NUMBER(22,0), 
	"PRIORITY" NUMBER(22,0), 
	"NAME" VARCHAR2(254), 
	"ACTIVATED" NUMBER(22,0), 
	"ACTIVATEDTZO" NUMBER(22,0), 
	"ACCEPTED" NUMBER(22,0), 
	"ACCEPTEDTZO" NUMBER(22,0), 
	"LASTSTATETIME" NUMBER(22,0), 
	"LASTSTATETIMETZO" NUMBER(22,0), 
	"LIMITTIME" NUMBER(22,0), 
	"LIMITTIMETZO" NUMBER(22,0), 
	"DESCRIPTION" VARCHAR2(254), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKACTIVITYDATA
--------------------------------------------------------

  CREATE TABLE "SHKACTIVITYDATA" 
   (	"ACTIVITY" NUMBER(19,0), 
	"VARIABLEDEFINITIONID" VARCHAR2(100), 
	"VARIABLETYPE" NUMBER(22,0), 
	"VARIABLEVALUE" BLOB, 
	"VARIABLEVALUEXML" VARCHAR2(4000), 
	"VARIABLEVALUEVCHAR" VARCHAR2(4000), 
	"VARIABLEVALUEDBL" NUMBER(22,0), 
	"VARIABLEVALUELONG" NUMBER(22,0), 
	"VARIABLEVALUEDATE" DATE, 
	"VARIABLEVALUEBOOL" NUMBER(22,0), 
	"ISRESULT" NUMBER(22,0), 
	"ORDNO" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKACTIVITYDATABLOBS
--------------------------------------------------------

  CREATE TABLE "SHKACTIVITYDATABLOBS" 
   (	"ACTIVITYDATAWOB" NUMBER(19,0), 
	"VARIABLEVALUE" BLOB, 
	"ORDNO" NUMBER(22,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKACTIVITYDATAWOB
--------------------------------------------------------

  CREATE TABLE "SHKACTIVITYDATAWOB" 
   (	"ACTIVITY" NUMBER(19,0), 
	"VARIABLEDEFINITIONID" VARCHAR2(100), 
	"VARIABLETYPE" NUMBER(22,0), 
	"VARIABLEVALUEXML" VARCHAR2(4000), 
	"VARIABLEVALUEVCHAR" VARCHAR2(4000), 
	"VARIABLEVALUEDBL" NUMBER(22,0), 
	"VARIABLEVALUELONG" NUMBER(22,0), 
	"VARIABLEVALUEDATE" DATE, 
	"VARIABLEVALUEBOOL" NUMBER(22,0), 
	"ISRESULT" NUMBER(22,0), 
	"ORDNO" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKACTIVITYSTATEEVENTAUDITS
--------------------------------------------------------

  CREATE TABLE "SHKACTIVITYSTATEEVENTAUDITS" 
   (	"KEYVALUE" VARCHAR2(30), 
	"NAME" VARCHAR2(50), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKACTIVITYSTATES
--------------------------------------------------------

  CREATE TABLE "SHKACTIVITYSTATES" 
   (	"KEYVALUE" VARCHAR2(30), 
	"NAME" VARCHAR2(50), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKANDJOINTABLE
--------------------------------------------------------

  CREATE TABLE "SHKANDJOINTABLE" 
   (	"PROCESS" NUMBER(19,0), 
	"BLOCKACTIVITY" NUMBER(19,0), 
	"ACTIVITYDEFINITIONID" VARCHAR2(90), 
	"ACTIVITY" NUMBER(19,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKASSIGNMENTEVENTAUDITS
--------------------------------------------------------

  CREATE TABLE "SHKASSIGNMENTEVENTAUDITS" 
   (	"RECORDEDTIME" NUMBER(22,0), 
	"RECORDEDTIMETZO" NUMBER(22,0), 
	"THEUSERNAME" VARCHAR2(100), 
	"THETYPE" NUMBER(19,0), 
	"ACTIVITYID" VARCHAR2(100), 
	"ACTIVITYNAME" VARCHAR2(254), 
	"PROCESSID" VARCHAR2(100), 
	"PROCESSNAME" VARCHAR2(254), 
	"PROCESSFACTORYNAME" VARCHAR2(200), 
	"PROCESSFACTORYVERSION" VARCHAR2(20), 
	"ACTIVITYDEFINITIONID" VARCHAR2(90), 
	"ACTIVITYDEFINITIONNAME" VARCHAR2(90), 
	"ACTIVITYDEFINITIONTYPE" NUMBER(22,0), 
	"PROCESSDEFINITIONID" VARCHAR2(90), 
	"PROCESSDEFINITIONNAME" VARCHAR2(90), 
	"PACKAGEID" VARCHAR2(90), 
	"OLDRESOURCEUSERNAME" VARCHAR2(100), 
	"OLDRESOURCENAME" VARCHAR2(100), 
	"NEWRESOURCEUSERNAME" VARCHAR2(100), 
	"NEWRESOURCENAME" VARCHAR2(100), 
	"ISACCEPTED" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKASSIGNMENTSTABLE
--------------------------------------------------------

  CREATE TABLE "SHKASSIGNMENTSTABLE" 
   (	"ACTIVITY" NUMBER(19,0), 
	"THERESOURCE" NUMBER(19,0), 
	"ACTIVITYID" VARCHAR2(100), 
	"ACTIVITYPROCESSID" VARCHAR2(100), 
	"ACTIVITYPROCESSDEFNAME" VARCHAR2(200), 
	"RESOURCEID" VARCHAR2(100), 
	"ISACCEPTED" NUMBER(22,0), 
	"ISVALID" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKCOUNTERS
--------------------------------------------------------

  CREATE TABLE "SHKCOUNTERS" 
   (	"NAME" VARCHAR2(100), 
	"THE_NUMBER" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKCREATEPROCESSEVENTAUDITS
--------------------------------------------------------

  CREATE TABLE "SHKCREATEPROCESSEVENTAUDITS" 
   (	"RECORDEDTIME" NUMBER(22,0), 
	"RECORDEDTIMETZO" NUMBER(22,0), 
	"THEUSERNAME" VARCHAR2(100), 
	"THETYPE" NUMBER(19,0), 
	"PROCESSID" VARCHAR2(100), 
	"PROCESSNAME" VARCHAR2(254), 
	"PROCESSFACTORYNAME" VARCHAR2(200), 
	"PROCESSFACTORYVERSION" VARCHAR2(20), 
	"PROCESSDEFINITIONID" VARCHAR2(90), 
	"PROCESSDEFINITIONNAME" VARCHAR2(90), 
	"PACKAGEID" VARCHAR2(90), 
	"PACTIVITYID" VARCHAR2(100), 
	"PPROCESSID" VARCHAR2(100), 
	"PPROCESSNAME" VARCHAR2(254), 
	"PPROCESSFACTORYNAME" VARCHAR2(200), 
	"PPROCESSFACTORYVERSION" VARCHAR2(20), 
	"PACTIVITYDEFINITIONID" VARCHAR2(90), 
	"PACTIVITYDEFINITIONNAME" VARCHAR2(90), 
	"PPROCESSDEFINITIONID" VARCHAR2(90), 
	"PPROCESSDEFINITIONNAME" VARCHAR2(90), 
	"PPACKAGEID" VARCHAR2(90), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKDATAEVENTAUDITS
--------------------------------------------------------

  CREATE TABLE "SHKDATAEVENTAUDITS" 
   (	"RECORDEDTIME" NUMBER(22,0), 
	"RECORDEDTIMETZO" NUMBER(22,0), 
	"THEUSERNAME" VARCHAR2(100), 
	"THETYPE" NUMBER(19,0), 
	"ACTIVITYID" VARCHAR2(100), 
	"ACTIVITYNAME" VARCHAR2(254), 
	"PROCESSID" VARCHAR2(100), 
	"PROCESSNAME" VARCHAR2(254), 
	"PROCESSFACTORYNAME" VARCHAR2(200), 
	"PROCESSFACTORYVERSION" VARCHAR2(20), 
	"ACTIVITYDEFINITIONID" VARCHAR2(90), 
	"ACTIVITYDEFINITIONNAME" VARCHAR2(90), 
	"ACTIVITYDEFINITIONTYPE" NUMBER(22,0), 
	"PROCESSDEFINITIONID" VARCHAR2(90), 
	"PROCESSDEFINITIONNAME" VARCHAR2(90), 
	"PACKAGEID" VARCHAR2(90), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKDEADLINES
--------------------------------------------------------

  CREATE TABLE "SHKDEADLINES" 
   (	"PROCESS" NUMBER(19,0), 
	"ACTIVITY" NUMBER(19,0), 
	"CNT" NUMBER(19,0), 
	"TIMELIMIT" NUMBER(22,0), 
	"TIMELIMITTZO" NUMBER(22,0), 
	"EXCEPTIONNAME" VARCHAR2(100), 
	"ISSYNCHRONOUS" NUMBER(22,0), 
	"ISEXECUTED" NUMBER(22,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKEVENTTYPES
--------------------------------------------------------

  CREATE TABLE "SHKEVENTTYPES" 
   (	"KEYVALUE" VARCHAR2(30), 
	"NAME" VARCHAR2(50), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKGROUPGROUPTABLE
--------------------------------------------------------

  CREATE TABLE "SHKGROUPGROUPTABLE" 
   (	"SUB_GID" NUMBER(19,0), 
	"GROUPID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKGROUPTABLE
--------------------------------------------------------

  CREATE TABLE "SHKGROUPTABLE" 
   (	"GROUPID" VARCHAR2(100), 
	"DESCRIPTION" VARCHAR2(254), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKGROUPUSER
--------------------------------------------------------

  CREATE TABLE "SHKGROUPUSER" 
   (	"USERNAME" VARCHAR2(100), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKGROUPUSERPACKLEVELPART
--------------------------------------------------------

  CREATE TABLE "SHKGROUPUSERPACKLEVELPART" 
   (	"PARTICIPANTOID" NUMBER(19,0), 
	"USEROID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKGROUPUSERPROCLEVELPART
--------------------------------------------------------

  CREATE TABLE "SHKGROUPUSERPROCLEVELPART" 
   (	"PARTICIPANTOID" NUMBER(19,0), 
	"USEROID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKNEWEVENTAUDITDATA
--------------------------------------------------------

  CREATE TABLE "SHKNEWEVENTAUDITDATA" 
   (	"DATAEVENTAUDIT" NUMBER(19,0), 
	"VARIABLEDEFINITIONID" VARCHAR2(100), 
	"VARIABLETYPE" NUMBER(22,0), 
	"VARIABLEVALUE" BLOB, 
	"VARIABLEVALUEXML" VARCHAR2(4000), 
	"VARIABLEVALUEVCHAR" VARCHAR2(4000), 
	"VARIABLEVALUEDBL" NUMBER(22,0), 
	"VARIABLEVALUELONG" NUMBER(22,0), 
	"VARIABLEVALUEDATE" DATE, 
	"VARIABLEVALUEBOOL" NUMBER(22,0), 
	"ORDNO" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKNEWEVENTAUDITDATABLOBS
--------------------------------------------------------

  CREATE TABLE "SHKNEWEVENTAUDITDATABLOBS" 
   (	"NEWEVENTAUDITDATAWOB" NUMBER(19,0), 
	"VARIABLEVALUE" BLOB, 
	"ORDNO" NUMBER(22,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKNEWEVENTAUDITDATAWOB
--------------------------------------------------------

  CREATE TABLE "SHKNEWEVENTAUDITDATAWOB" 
   (	"DATAEVENTAUDIT" NUMBER(19,0), 
	"VARIABLEDEFINITIONID" VARCHAR2(100), 
	"VARIABLETYPE" NUMBER(22,0), 
	"VARIABLEVALUEXML" VARCHAR2(4000), 
	"VARIABLEVALUEVCHAR" VARCHAR2(4000), 
	"VARIABLEVALUEDBL" NUMBER(22,0), 
	"VARIABLEVALUELONG" NUMBER(22,0), 
	"VARIABLEVALUEDATE" DATE, 
	"VARIABLEVALUEBOOL" NUMBER(22,0), 
	"ORDNO" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKNEXTXPDLVERSIONS
--------------------------------------------------------

  CREATE TABLE "SHKNEXTXPDLVERSIONS" 
   (	"XPDLID" VARCHAR2(90), 
	"NEXTVERSION" VARCHAR2(20), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKNORMALUSER
--------------------------------------------------------

  CREATE TABLE "SHKNORMALUSER" 
   (	"USERNAME" VARCHAR2(100), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKOLDEVENTAUDITDATA
--------------------------------------------------------

  CREATE TABLE "SHKOLDEVENTAUDITDATA" 
   (	"DATAEVENTAUDIT" NUMBER(19,0), 
	"VARIABLEDEFINITIONID" VARCHAR2(100), 
	"VARIABLETYPE" NUMBER(22,0), 
	"VARIABLEVALUE" BLOB, 
	"VARIABLEVALUEXML" VARCHAR2(4000), 
	"VARIABLEVALUEVCHAR" VARCHAR2(4000), 
	"VARIABLEVALUEDBL" NUMBER(22,0), 
	"VARIABLEVALUELONG" NUMBER(22,0), 
	"VARIABLEVALUEDATE" DATE, 
	"VARIABLEVALUEBOOL" NUMBER(22,0), 
	"ORDNO" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKOLDEVENTAUDITDATABLOBS
--------------------------------------------------------

  CREATE TABLE "SHKOLDEVENTAUDITDATABLOBS" 
   (	"OLDEVENTAUDITDATAWOB" NUMBER(19,0), 
	"VARIABLEVALUE" BLOB, 
	"ORDNO" NUMBER(22,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKOLDEVENTAUDITDATAWOB
--------------------------------------------------------

  CREATE TABLE "SHKOLDEVENTAUDITDATAWOB" 
   (	"DATAEVENTAUDIT" NUMBER(19,0), 
	"VARIABLEDEFINITIONID" VARCHAR2(100), 
	"VARIABLETYPE" NUMBER(22,0), 
	"VARIABLEVALUEXML" VARCHAR2(4000), 
	"VARIABLEVALUEVCHAR" VARCHAR2(4000), 
	"VARIABLEVALUEDBL" NUMBER(22,0), 
	"VARIABLEVALUELONG" NUMBER(22,0), 
	"VARIABLEVALUEDATE" DATE, 
	"VARIABLEVALUEBOOL" NUMBER(22,0), 
	"ORDNO" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPACKLEVELPARTICIPANT
--------------------------------------------------------

  CREATE TABLE "SHKPACKLEVELPARTICIPANT" 
   (	"PARTICIPANT_ID" VARCHAR2(90), 
	"PACKAGEOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPACKLEVELXPDLAPP
--------------------------------------------------------

  CREATE TABLE "SHKPACKLEVELXPDLAPP" 
   (	"APPLICATION_ID" VARCHAR2(90), 
	"PACKAGEOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPACKLEVELXPDLAPPTAAPPDETAIL
--------------------------------------------------------

  CREATE TABLE "SHKPACKLEVELXPDLAPPTAAPPDETAIL" 
   (	"XPDL_APPOID" NUMBER(19,0), 
	"TOOLAGENTOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPACKLEVELXPDLAPPTAAPPDETUSR
--------------------------------------------------------

  CREATE TABLE "SHKPACKLEVELXPDLAPPTAAPPDETUSR" 
   (	"XPDL_APPOID" NUMBER(19,0), 
	"TOOLAGENTOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPACKLEVELXPDLAPPTAAPPUSER
--------------------------------------------------------

  CREATE TABLE "SHKPACKLEVELXPDLAPPTAAPPUSER" 
   (	"XPDL_APPOID" NUMBER(19,0), 
	"TOOLAGENTOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPACKLEVELXPDLAPPTOOLAGNTAPP
--------------------------------------------------------

  CREATE TABLE "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" 
   (	"XPDL_APPOID" NUMBER(19,0), 
	"TOOLAGENTOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCESSDATA
--------------------------------------------------------

  CREATE TABLE "SHKPROCESSDATA" 
   (	"PROCESS" NUMBER(19,0), 
	"VARIABLEDEFINITIONID" VARCHAR2(100), 
	"VARIABLETYPE" NUMBER(22,0), 
	"VARIABLEVALUE" BLOB, 
	"VARIABLEVALUEXML" VARCHAR2(4000), 
	"VARIABLEVALUEVCHAR" VARCHAR2(4000), 
	"VARIABLEVALUEDBL" NUMBER(22,0), 
	"VARIABLEVALUELONG" NUMBER(22,0), 
	"VARIABLEVALUEDATE" DATE, 
	"VARIABLEVALUEBOOL" NUMBER(22,0), 
	"ORDNO" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCESSDATABLOBS
--------------------------------------------------------

  CREATE TABLE "SHKPROCESSDATABLOBS" 
   (	"PROCESSDATAWOB" NUMBER(19,0), 
	"VARIABLEVALUE" BLOB, 
	"ORDNO" NUMBER(22,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCESSDATAWOB
--------------------------------------------------------

  CREATE TABLE "SHKPROCESSDATAWOB" 
   (	"PROCESS" NUMBER(19,0), 
	"VARIABLEDEFINITIONID" VARCHAR2(100), 
	"VARIABLETYPE" NUMBER(22,0), 
	"VARIABLEVALUEXML" VARCHAR2(4000), 
	"VARIABLEVALUEVCHAR" VARCHAR2(4000), 
	"VARIABLEVALUEDBL" NUMBER(22,0), 
	"VARIABLEVALUELONG" NUMBER(22,0), 
	"VARIABLEVALUEDATE" DATE, 
	"VARIABLEVALUEBOOL" NUMBER(22,0), 
	"ORDNO" NUMBER(22,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCESSDEFINITIONS
--------------------------------------------------------

  CREATE TABLE "SHKPROCESSDEFINITIONS" 
   (	"NAME" VARCHAR2(200), 
	"PACKAGEID" VARCHAR2(90), 
	"PROCESSDEFINITIONID" VARCHAR2(90), 
	"PROCESSDEFINITIONCREATED" NUMBER(22,0), 
	"PROCESSDEFINITIONVERSION" VARCHAR2(20), 
	"STATE" NUMBER(22,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCESSES
--------------------------------------------------------

  CREATE TABLE "SHKPROCESSES" 
   (	"SYNCVERSION" NUMBER(22,0), 
	"ID" VARCHAR2(100), 
	"PROCESSDEFINITION" NUMBER(19,0), 
	"PDEFNAME" VARCHAR2(200), 
	"ACTIVITYREQUESTERID" VARCHAR2(100), 
	"ACTIVITYREQUESTERPROCESSID" VARCHAR2(100), 
	"RESOURCEREQUESTERID" VARCHAR2(100), 
	"EXTERNALREQUESTERCLASSNAME" VARCHAR2(254), 
	"STATE" NUMBER(19,0), 
	"PRIORITY" NUMBER(22,0), 
	"NAME" VARCHAR2(254), 
	"CREATED" NUMBER(22,0), 
	"CREATEDTZO" NUMBER(22,0), 
	"STARTED" NUMBER(22,0), 
	"STARTEDTZO" NUMBER(22,0), 
	"LASTSTATETIME" NUMBER(22,0), 
	"LASTSTATETIMETZO" NUMBER(22,0), 
	"LIMITTIME" NUMBER(22,0), 
	"LIMITTIMETZO" NUMBER(22,0), 
	"DESCRIPTION" VARCHAR2(254), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCESSREQUESTERS
--------------------------------------------------------

  CREATE TABLE "SHKPROCESSREQUESTERS" 
   (	"ID" VARCHAR2(100), 
	"ACTIVITYREQUESTER" NUMBER(19,0), 
	"RESOURCEREQUESTER" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCESSSTATEEVENTAUDITS
--------------------------------------------------------

  CREATE TABLE "SHKPROCESSSTATEEVENTAUDITS" 
   (	"KEYVALUE" VARCHAR2(30), 
	"NAME" VARCHAR2(50), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCESSSTATES
--------------------------------------------------------

  CREATE TABLE "SHKPROCESSSTATES" 
   (	"KEYVALUE" VARCHAR2(30), 
	"NAME" VARCHAR2(50), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCLEVELPARTICIPANT
--------------------------------------------------------

  CREATE TABLE "SHKPROCLEVELPARTICIPANT" 
   (	"PARTICIPANT_ID" VARCHAR2(90), 
	"PROCESSOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCLEVELXPDLAPP
--------------------------------------------------------

  CREATE TABLE "SHKPROCLEVELXPDLAPP" 
   (	"APPLICATION_ID" VARCHAR2(90), 
	"PROCESSOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCLEVELXPDLAPPTAAPPDETAIL
--------------------------------------------------------

  CREATE TABLE "SHKPROCLEVELXPDLAPPTAAPPDETAIL" 
   (	"XPDL_APPOID" NUMBER(19,0), 
	"TOOLAGENTOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCLEVELXPDLAPPTAAPPDETUSR
--------------------------------------------------------

  CREATE TABLE "SHKPROCLEVELXPDLAPPTAAPPDETUSR" 
   (	"XPDL_APPOID" NUMBER(19,0), 
	"TOOLAGENTOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCLEVELXPDLAPPTAAPPUSER
--------------------------------------------------------

  CREATE TABLE "SHKPROCLEVELXPDLAPPTAAPPUSER" 
   (	"XPDL_APPOID" NUMBER(19,0), 
	"TOOLAGENTOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKPROCLEVELXPDLAPPTOOLAGNTAPP
--------------------------------------------------------

  CREATE TABLE "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" 
   (	"XPDL_APPOID" NUMBER(19,0), 
	"TOOLAGENTOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKRESOURCESTABLE
--------------------------------------------------------

  CREATE TABLE "SHKRESOURCESTABLE" 
   (	"USERNAME" VARCHAR2(100), 
	"NAME" VARCHAR2(100), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKSTATEEVENTAUDITS
--------------------------------------------------------

  CREATE TABLE "SHKSTATEEVENTAUDITS" 
   (	"RECORDEDTIME" NUMBER(22,0), 
	"RECORDEDTIMETZO" NUMBER(22,0), 
	"THEUSERNAME" VARCHAR2(100), 
	"THETYPE" NUMBER(19,0), 
	"ACTIVITYID" VARCHAR2(100), 
	"ACTIVITYNAME" VARCHAR2(254), 
	"PROCESSID" VARCHAR2(100), 
	"PROCESSNAME" VARCHAR2(254), 
	"PROCESSFACTORYNAME" VARCHAR2(200), 
	"PROCESSFACTORYVERSION" VARCHAR2(20), 
	"ACTIVITYDEFINITIONID" VARCHAR2(90), 
	"ACTIVITYDEFINITIONNAME" VARCHAR2(90), 
	"ACTIVITYDEFINITIONTYPE" NUMBER(22,0), 
	"PROCESSDEFINITIONID" VARCHAR2(90), 
	"PROCESSDEFINITIONNAME" VARCHAR2(90), 
	"PACKAGEID" VARCHAR2(90), 
	"OLDPROCESSSTATE" NUMBER(19,0), 
	"NEWPROCESSSTATE" NUMBER(19,0), 
	"OLDACTIVITYSTATE" NUMBER(19,0), 
	"NEWACTIVITYSTATE" NUMBER(19,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKTOOLAGENTAPP
--------------------------------------------------------

  CREATE TABLE "SHKTOOLAGENTAPP" 
   (	"TOOL_AGENT_NAME" VARCHAR2(250), 
	"APP_NAME" VARCHAR2(90), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKTOOLAGENTAPPDETAIL
--------------------------------------------------------

  CREATE TABLE "SHKTOOLAGENTAPPDETAIL" 
   (	"APP_MODE" NUMBER(10,0), 
	"TOOLAGENT_APPOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKTOOLAGENTAPPDETAILUSER
--------------------------------------------------------

  CREATE TABLE "SHKTOOLAGENTAPPDETAILUSER" 
   (	"TOOLAGENT_APPOID" NUMBER(19,0), 
	"USEROID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKTOOLAGENTAPPUSER
--------------------------------------------------------

  CREATE TABLE "SHKTOOLAGENTAPPUSER" 
   (	"TOOLAGENT_APPOID" NUMBER(19,0), 
	"USEROID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKTOOLAGENTUSER
--------------------------------------------------------

  CREATE TABLE "SHKTOOLAGENTUSER" 
   (	"USERNAME" VARCHAR2(100), 
	"PWD" VARCHAR2(100), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKUSERGROUPTABLE
--------------------------------------------------------

  CREATE TABLE "SHKUSERGROUPTABLE" 
   (	"USERID" NUMBER(19,0), 
	"GROUPID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKUSERPACKLEVELPART
--------------------------------------------------------

  CREATE TABLE "SHKUSERPACKLEVELPART" 
   (	"PARTICIPANTOID" NUMBER(19,0), 
	"USEROID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKUSERPROCLEVELPARTICIPANT
--------------------------------------------------------

  CREATE TABLE "SHKUSERPROCLEVELPARTICIPANT" 
   (	"PARTICIPANTOID" NUMBER(19,0), 
	"USEROID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKUSERTABLE
--------------------------------------------------------

  CREATE TABLE "SHKUSERTABLE" 
   (	"USERID" VARCHAR2(100), 
	"FIRSTNAME" VARCHAR2(50), 
	"LASTNAME" VARCHAR2(50), 
	"PASSWD" VARCHAR2(50), 
	"EMAIL" VARCHAR2(254), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLAPPLICATIONPACKAGE
--------------------------------------------------------

  CREATE TABLE "SHKXPDLAPPLICATIONPACKAGE" 
   (	"PACKAGE_ID" VARCHAR2(90), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLAPPLICATIONPROCESS
--------------------------------------------------------

  CREATE TABLE "SHKXPDLAPPLICATIONPROCESS" 
   (	"PROCESS_ID" VARCHAR2(90), 
	"PACKAGEOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLDATA
--------------------------------------------------------

  CREATE TABLE "SHKXPDLDATA" 
   (	"XPDLCONTENT" BLOB, 
	"XPDLCLASSCONTENT" BLOB, 
	"XPDL" NUMBER(19,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLHISTORY
--------------------------------------------------------

  CREATE TABLE "SHKXPDLHISTORY" 
   (	"XPDLID" VARCHAR2(90), 
	"XPDLVERSION" VARCHAR2(20), 
	"XPDLCLASSVERSION" NUMBER(22,0), 
	"XPDLUPLOADTIME" DATE, 
	"XPDLHISTORYUPLOADTIME" DATE, 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLHISTORYDATA
--------------------------------------------------------

  CREATE TABLE "SHKXPDLHISTORYDATA" 
   (	"XPDLCONTENT" BLOB, 
	"XPDLCLASSCONTENT" BLOB, 
	"XPDLHISTORY" NUMBER(19,0), 
	"CNT" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLPARTICIPANTPACKAGE
--------------------------------------------------------

  CREATE TABLE "SHKXPDLPARTICIPANTPACKAGE" 
   (	"PACKAGE_ID" VARCHAR2(90), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLPARTICIPANTPROCESS
--------------------------------------------------------

  CREATE TABLE "SHKXPDLPARTICIPANTPROCESS" 
   (	"PROCESS_ID" VARCHAR2(90), 
	"PACKAGEOID" NUMBER(19,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLREFERENCES
--------------------------------------------------------

  CREATE TABLE "SHKXPDLREFERENCES" 
   (	"REFERREDXPDLID" VARCHAR2(90), 
	"REFERRINGXPDL" NUMBER(19,0), 
	"REFERREDXPDLNUMBER" NUMBER(22,0), 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table SHKXPDLS
--------------------------------------------------------

  CREATE TABLE "SHKXPDLS" 
   (	"XPDLID" VARCHAR2(90), 
	"XPDLVERSION" VARCHAR2(20), 
	"XPDLCLASSVERSION" NUMBER(22,0), 
	"XPDLUPLOADTIME" DATE, 
	"OID" NUMBER(19,0), 
	"VERSION" NUMBER(22,0)
   ) ;
--------------------------------------------------------
--  DDL for Table WF_AUDIT_TRAIL
--------------------------------------------------------

  CREATE TABLE "WF_AUDIT_TRAIL" 
   (	"ID" VARCHAR2(255 CHAR), 
	"USERNAME" VARCHAR2(255 CHAR), 
	"APPID" VARCHAR2(255 CHAR), 
	"APPVERSION" VARCHAR2(255 CHAR), 
	"CLAZZ" VARCHAR2(255 CHAR), 
	"METHOD" VARCHAR2(255 CHAR), 
	"MESSAGE" CLOB, 
	"TIMESTAMP" TIMESTAMP (6)
   ) ;
--------------------------------------------------------
--  DDL for Table WF_PROCESS_LINK
--------------------------------------------------------

  CREATE TABLE "WF_PROCESS_LINK" 
   (	"PROCESSID" VARCHAR2(255 CHAR), 
	"PARENTPROCESSID" VARCHAR2(255 CHAR), 
	"ORIGINPROCESSID" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  DDL for Table WF_RESOURCE_BUNDLE_MESSAGE
--------------------------------------------------------

  CREATE TABLE "WF_RESOURCE_BUNDLE_MESSAGE" 
   (	"ID" VARCHAR2(255 CHAR), 
	"MESSAGEKEY" VARCHAR2(255 CHAR), 
	"LOCALE" VARCHAR2(255 CHAR), 
	"MESSAGE" CLOB
   ) ;
--------------------------------------------------------
--  DDL for Table WF_SETUP
--------------------------------------------------------

  CREATE TABLE "WF_SETUP" 
   (	"ID" VARCHAR2(255 CHAR), 
	"PROPERTY" VARCHAR2(255 CHAR), 
	"VALUE" CLOB, 
	"ORDERING" NUMBER(10,0)
   ) ;

---------------------------------------------------
--   DATA FOR TABLE SHKUSERTABLE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKUSERTABLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKDATAEVENTAUDITS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKDATAEVENTAUDITS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKGROUPUSER
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKGROUPUSER
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_REPORT_ACTIVITY
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_REPORT_ACTIVITY
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_USER_GROUP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE DIR_USER_GROUP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKNEWEVENTAUDITDATAWOB
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKNEWEVENTAUDITDATAWOB
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKCREATEPROCESSEVENTAUDITS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKCREATEPROCESSEVENTAUDITS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKTOOLAGENTUSER
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKTOOLAGENTUSER
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKGROUPTABLE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKGROUPTABLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKACTIVITYDATAWOB
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKACTIVITYDATAWOB
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKGROUPUSERPACKLEVELPART
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKGROUPUSERPACKLEVELPART
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCLEVELXPDLAPPTAAPPUSER
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCLEVELXPDLAPPTAAPPUSER
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_APP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_APP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCLEVELPARTICIPANT
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCLEVELPARTICIPANT
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLHISTORY
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLHISTORY
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKOLDEVENTAUDITDATA
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKOLDEVENTAUDITDATA
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKTOOLAGENTAPP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKTOOLAGENTAPP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKTOOLAGENTAPPDETAILUSER
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKTOOLAGENTAPPDETAILUSER
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKANDJOINTABLE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKANDJOINTABLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE WF_PROCESS_LINK
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE WF_PROCESS_LINK
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_USERVIEW
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_USERVIEW
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_USER_ROLE
--   FILTER = none used
---------------------------------------------------
Insert into DIR_USER_ROLE (ROLEID,USERID) values ('ROLE_ADMIN','admin');

---------------------------------------------------
--   END DATA FOR TABLE DIR_USER_ROLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKSTATEEVENTAUDITS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKSTATEEVENTAUDITS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKDEADLINES
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKDEADLINES
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_PLUGIN_DEFAULT
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_PLUGIN_DEFAULT
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_REPORT_PROCESS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_REPORT_PROCESS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKOLDEVENTAUDITDATAWOB
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKOLDEVENTAUDITDATAWOB
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_GROUP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE DIR_GROUP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKNORMALUSER
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKNORMALUSER
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKEVENTTYPES
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKEVENTTYPES
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKACTIVITYDATABLOBS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKACTIVITYDATABLOBS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_ROLE
--   FILTER = none used
---------------------------------------------------
Insert into DIR_ROLE (ID,NAME,DESCRIPTION) values ('ROLE_ADMIN','Admin','Administrator');
Insert into DIR_ROLE (ID,NAME,DESCRIPTION) values ('ROLE_USER','User','Normal User');

---------------------------------------------------
--   END DATA FOR TABLE DIR_ROLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPACKLEVELXPDLAPPTOOLAGNTAPP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPACKLEVELXPDLAPPTOOLAGNTAPP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCESSDEFINITIONS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCESSDEFINITIONS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKTOOLAGENTAPPDETAIL
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKTOOLAGENTAPPDETAIL
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPACKLEVELXPDLAPP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPACKLEVELXPDLAPP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCESSES
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCESSES
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPACKLEVELPARTICIPANT
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPACKLEVELPARTICIPANT
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_REPORT_PROCESS_INSTANCE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_REPORT_PROCESS_INSTANCE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKGROUPUSERPROCLEVELPART
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKGROUPUSERPROCLEVELPART
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCESSREQUESTERS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCESSREQUESTERS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCESSDATA
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCESSDATA
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCESSDATAWOB
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCESSDATAWOB
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKASSIGNMENTSTABLE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKASSIGNMENTSTABLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_REPORT_APP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_REPORT_APP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLREFERENCES
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLREFERENCES
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_FORM
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_FORM
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_PACKAGE_PARTICIPANT
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_PACKAGE_PARTICIPANT
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCLEVELXPDLAPPTOOLAGNTAPP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCLEVELXPDLAPPTOOLAGNTAPP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_DATALIST
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_DATALIST
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKACTIVITYDATA
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKACTIVITYDATA
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE WF_SETUP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE WF_SETUP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKASSIGNMENTEVENTAUDITS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKASSIGNMENTEVENTAUDITS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_FD
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_FD
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_ENV_VARIABLE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_ENV_VARIABLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_EMPLOYMENT_REPORT_TO
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE DIR_EMPLOYMENT_REPORT_TO
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_PACKAGE_ACTIVITY_FORM
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_PACKAGE_ACTIVITY_FORM
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPACKLEVELXPDLAPPTAAPPDETUSR
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPACKLEVELXPDLAPPTAAPPDETUSR
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCLEVELXPDLAPPTAAPPDETUSR
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCLEVELXPDLAPPTAAPPDETUSR
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKUSERPACKLEVELPART
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKUSERPACKLEVELPART
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_REPORT_PACKAGE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_REPORT_PACKAGE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKNEWEVENTAUDITDATABLOBS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKNEWEVENTAUDITDATABLOBS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKUSERPROCLEVELPARTICIPANT
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKUSERPROCLEVELPARTICIPANT
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE WF_AUDIT_TRAIL
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE WF_AUDIT_TRAIL
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKOLDEVENTAUDITDATABLOBS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKOLDEVENTAUDITDATABLOBS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCLEVELXPDLAPP
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCLEVELXPDLAPP
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_EMPLOYMENT
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE DIR_EMPLOYMENT
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPACKLEVELXPDLAPPTAAPPUSER
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPACKLEVELXPDLAPPTAAPPUSER
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLAPPLICATIONPROCESS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLAPPLICATIONPROCESS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE WF_RESOURCE_BUNDLE_MESSAGE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE WF_RESOURCE_BUNDLE_MESSAGE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKCOUNTERS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKCOUNTERS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_PACKAGE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_PACKAGE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKNEWEVENTAUDITDATA
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKNEWEVENTAUDITDATA
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKGROUPGROUPTABLE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKGROUPGROUPTABLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE OBJECTID
--   FILTER = none used
---------------------------------------------------
Insert into OBJECTID (NEXTOID) values (1000200);

---------------------------------------------------
--   END DATA FOR TABLE OBJECTID
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKRESOURCESTABLE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKRESOURCESTABLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_REPORT_ACTIVITY_INSTANCE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_REPORT_ACTIVITY_INSTANCE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_GRADE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE DIR_GRADE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKTOOLAGENTAPPUSER
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKTOOLAGENTAPPUSER
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_USER
--   FILTER = none used
---------------------------------------------------
Insert into DIR_USER (ID,USERNAME,PASSWORD,FIRSTNAME,LASTNAME,EMAIL,TIMEZONE,ACTIVE) values ('admin','admin','21232f297a57a5a743894a0e4a801fc3','Admin','admin','admin@email.domain','',1);

---------------------------------------------------
--   END DATA FOR TABLE DIR_USER
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_ORGANIZATION
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE DIR_ORGANIZATION
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLPARTICIPANTPROCESS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLPARTICIPANTPROCESS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_PACKAGE_ACTIVITY_PLUGIN
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_PACKAGE_ACTIVITY_PLUGIN
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLAPPLICATIONPACKAGE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLAPPLICATIONPACKAGE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCLEVELXPDLAPPTAAPPDETAIL
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCLEVELXPDLAPPTAAPPDETAIL
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPACKLEVELXPDLAPPTAAPPDETAIL
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPACKLEVELXPDLAPPTAAPPDETAIL
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCESSSTATES
--   FILTER = none used
---------------------------------------------------
Insert into SHKPROCESSSTATES (KEYVALUE,NAME,OID,VERSION) values ('open.running','open.running',1000000,0);
Insert into SHKPROCESSSTATES (KEYVALUE,NAME,OID,VERSION) values ('open.not_running.not_started','open.not_running.not_started',1000002,0);
Insert into SHKPROCESSSTATES (KEYVALUE,NAME,OID,VERSION) values ('open.not_running.suspended','open.not_running.suspended',1000004,0);
Insert into SHKPROCESSSTATES (KEYVALUE,NAME,OID,VERSION) values ('closed.completed','closed.completed',1000006,0);
Insert into SHKPROCESSSTATES (KEYVALUE,NAME,OID,VERSION) values ('closed.terminated','closed.terminated',1000008,0);
Insert into SHKPROCESSSTATES (KEYVALUE,NAME,OID,VERSION) values ('closed.aborted','closed.aborted',1000010,0);

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCESSSTATES
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLHISTORYDATA
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLHISTORYDATA
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLDATA
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLDATA
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCESSDATABLOBS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCESSDATABLOBS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKXPDLPARTICIPANTPACKAGE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKXPDLPARTICIPANTPACKAGE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKNEXTXPDLVERSIONS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKNEXTXPDLVERSIONS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE APP_MESSAGE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE APP_MESSAGE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKACTIVITYSTATES
--   FILTER = none used
---------------------------------------------------
Insert into SHKACTIVITYSTATES (KEYVALUE,NAME,OID,VERSION) values ('open.running','open.running',1000001,0);
Insert into SHKACTIVITYSTATES (KEYVALUE,NAME,OID,VERSION) values ('open.not_running.not_started','open.not_running.not_started',1000003,0);
Insert into SHKACTIVITYSTATES (KEYVALUE,NAME,OID,VERSION) values ('open.not_running.suspended','open.not_running.suspended',1000005,0);
Insert into SHKACTIVITYSTATES (KEYVALUE,NAME,OID,VERSION) values ('closed.completed','closed.completed',1000007,0);
Insert into SHKACTIVITYSTATES (KEYVALUE,NAME,OID,VERSION) values ('closed.terminated','closed.terminated',1000009,0);
Insert into SHKACTIVITYSTATES (KEYVALUE,NAME,OID,VERSION) values ('closed.aborted','closed.aborted',1000011,0);

---------------------------------------------------
--   END DATA FOR TABLE SHKACTIVITYSTATES
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKACTIVITYSTATEEVENTAUDITS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKACTIVITYSTATEEVENTAUDITS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKPROCESSSTATEEVENTAUDITS
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKPROCESSSTATEEVENTAUDITS
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKUSERGROUPTABLE
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKUSERGROUPTABLE
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE SHKACTIVITIES
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE SHKACTIVITIES
---------------------------------------------------

---------------------------------------------------
--   DATA FOR TABLE DIR_DEPARTMENT
--   FILTER = none used
---------------------------------------------------

---------------------------------------------------
--   END DATA FOR TABLE DIR_DEPARTMENT
---------------------------------------------------
--------------------------------------------------------
--  Constraints for Table APP_APP
--------------------------------------------------------

  ALTER TABLE "APP_APP" MODIFY ("APPID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_APP" MODIFY ("APPVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_APP" ADD PRIMARY KEY ("APPID", "APPVERSION") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_DATALIST
--------------------------------------------------------

  ALTER TABLE "APP_DATALIST" MODIFY ("APPID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_DATALIST" MODIFY ("APPVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_DATALIST" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_DATALIST" ADD PRIMARY KEY ("APPID", "APPVERSION", "ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_ENV_VARIABLE
--------------------------------------------------------

  ALTER TABLE "APP_ENV_VARIABLE" MODIFY ("APPID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_ENV_VARIABLE" MODIFY ("APPVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_ENV_VARIABLE" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_ENV_VARIABLE" ADD PRIMARY KEY ("APPID", "APPVERSION", "ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_FD
--------------------------------------------------------

  ALTER TABLE "APP_FD" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_FD" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_FORM
--------------------------------------------------------

  ALTER TABLE "APP_FORM" MODIFY ("APPID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_FORM" MODIFY ("APPVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_FORM" MODIFY ("FORMID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_FORM" ADD PRIMARY KEY ("APPID", "APPVERSION", "FORMID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_MESSAGE
--------------------------------------------------------

  ALTER TABLE "APP_MESSAGE" MODIFY ("APPID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_MESSAGE" MODIFY ("APPVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_MESSAGE" MODIFY ("OUID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_MESSAGE" ADD PRIMARY KEY ("APPID", "APPVERSION", "OUID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_PACKAGE
--------------------------------------------------------

  ALTER TABLE "APP_PACKAGE" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE" MODIFY ("PACKAGEVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE" ADD PRIMARY KEY ("PACKAGEID", "PACKAGEVERSION") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_PACKAGE_ACTIVITY_FORM
--------------------------------------------------------

  ALTER TABLE "APP_PACKAGE_ACTIVITY_FORM" MODIFY ("PROCESSDEFID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_ACTIVITY_FORM" MODIFY ("ACTIVITYDEFID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_ACTIVITY_FORM" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_ACTIVITY_FORM" MODIFY ("PACKAGEVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_ACTIVITY_FORM" ADD PRIMARY KEY ("PROCESSDEFID", "ACTIVITYDEFID", "PACKAGEID", "PACKAGEVERSION") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_PACKAGE_ACTIVITY_PLUGIN
--------------------------------------------------------

  ALTER TABLE "APP_PACKAGE_ACTIVITY_PLUGIN" MODIFY ("PROCESSDEFID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_ACTIVITY_PLUGIN" MODIFY ("ACTIVITYDEFID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_ACTIVITY_PLUGIN" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_ACTIVITY_PLUGIN" MODIFY ("PACKAGEVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_ACTIVITY_PLUGIN" ADD PRIMARY KEY ("PROCESSDEFID", "ACTIVITYDEFID", "PACKAGEID", "PACKAGEVERSION") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_PACKAGE_PARTICIPANT
--------------------------------------------------------

  ALTER TABLE "APP_PACKAGE_PARTICIPANT" MODIFY ("PROCESSDEFID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_PARTICIPANT" MODIFY ("PARTICIPANTID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_PARTICIPANT" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_PARTICIPANT" MODIFY ("PACKAGEVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PACKAGE_PARTICIPANT" ADD PRIMARY KEY ("PROCESSDEFID", "PARTICIPANTID", "PACKAGEID", "PACKAGEVERSION") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_PLUGIN_DEFAULT
--------------------------------------------------------

  ALTER TABLE "APP_PLUGIN_DEFAULT" MODIFY ("APPID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PLUGIN_DEFAULT" MODIFY ("APPVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PLUGIN_DEFAULT" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_PLUGIN_DEFAULT" ADD PRIMARY KEY ("APPID", "APPVERSION", "ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_REPORT_ACTIVITY
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_ACTIVITY" MODIFY ("UUID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_REPORT_ACTIVITY" ADD PRIMARY KEY ("UUID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_REPORT_ACTIVITY_INSTANCE
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_ACTIVITY_INSTANCE" MODIFY ("INSTANCEID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_REPORT_ACTIVITY_INSTANCE" ADD PRIMARY KEY ("INSTANCEID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_REPORT_APP
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_APP" MODIFY ("UUID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_REPORT_APP" ADD PRIMARY KEY ("UUID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_REPORT_PACKAGE
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_PACKAGE" MODIFY ("UUID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_REPORT_PACKAGE" ADD PRIMARY KEY ("UUID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_REPORT_PROCESS
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_PROCESS" MODIFY ("UUID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_REPORT_PROCESS" ADD PRIMARY KEY ("UUID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_REPORT_PROCESS_INSTANCE
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_PROCESS_INSTANCE" MODIFY ("INSTANCEID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_REPORT_PROCESS_INSTANCE" ADD PRIMARY KEY ("INSTANCEID") ENABLE;
--------------------------------------------------------
--  Constraints for Table APP_USERVIEW
--------------------------------------------------------

  ALTER TABLE "APP_USERVIEW" MODIFY ("APPID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_USERVIEW" MODIFY ("APPVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "APP_USERVIEW" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "APP_USERVIEW" ADD PRIMARY KEY ("APPID", "APPVERSION", "ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_DEPARTMENT
--------------------------------------------------------

  ALTER TABLE "DIR_DEPARTMENT" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_DEPARTMENT" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_EMPLOYMENT
--------------------------------------------------------

  ALTER TABLE "DIR_EMPLOYMENT" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_EMPLOYMENT" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_EMPLOYMENT_REPORT_TO
--------------------------------------------------------

  ALTER TABLE "DIR_EMPLOYMENT_REPORT_TO" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_EMPLOYMENT_REPORT_TO" ADD PRIMARY KEY ("ID") ENABLE;
 
  ALTER TABLE "DIR_EMPLOYMENT_REPORT_TO" ADD UNIQUE ("EMPLOYMENTID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_GRADE
--------------------------------------------------------

  ALTER TABLE "DIR_GRADE" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_GRADE" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_GROUP
--------------------------------------------------------

  ALTER TABLE "DIR_GROUP" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_GROUP" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_ORGANIZATION
--------------------------------------------------------

  ALTER TABLE "DIR_ORGANIZATION" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_ORGANIZATION" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_ROLE
--------------------------------------------------------

  ALTER TABLE "DIR_ROLE" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_ROLE" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_USER
--------------------------------------------------------

  ALTER TABLE "DIR_USER" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_USER" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_USER_EXTRA
--------------------------------------------------------

  ALTER TABLE "DIR_USER_EXTRA" MODIFY ("USERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_USER_EXTRA" ADD PRIMARY KEY ("USERNAME") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_USER_PASSWORD_HISTORY
--------------------------------------------------------

  ALTER TABLE "DIR_USER_PASSWORD_HISTORY" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_USER_PASSWORD_HISTORY" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_USER_GROUP
--------------------------------------------------------

  ALTER TABLE "DIR_USER_GROUP" MODIFY ("GROUPID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_USER_GROUP" MODIFY ("USERID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_USER_GROUP" ADD PRIMARY KEY ("USERID", "GROUPID") ENABLE;
--------------------------------------------------------
--  Constraints for Table DIR_USER_ROLE
--------------------------------------------------------

  ALTER TABLE "DIR_USER_ROLE" MODIFY ("ROLEID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_USER_ROLE" MODIFY ("USERID" NOT NULL ENABLE);
 
  ALTER TABLE "DIR_USER_ROLE" ADD PRIMARY KEY ("USERID", "ROLEID") ENABLE;
--------------------------------------------------------
--  Constraints for Table OBJECTID
--------------------------------------------------------

  ALTER TABLE "OBJECTID" ADD CONSTRAINT "PK_OBJECTID" PRIMARY KEY ("NEXTOID") ENABLE;
 
  ALTER TABLE "OBJECTID" MODIFY ("NEXTOID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKACTIVITIES
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITIES" ADD CONSTRAINT "SHKACTIVITIES_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("ACTIVITYDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("PROCESS" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("PDEFNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("PROCESSID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("STATE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("ACTIVATED" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("ACTIVATEDTZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("LASTSTATETIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("LASTSTATETIMETZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("LIMITTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("LIMITTIMETZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITIES" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKACTIVITYDATA
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITYDATA" ADD CONSTRAINT "SHKACTIVITYDATA_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKACTIVITYDATA" MODIFY ("ACTIVITY" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATA" MODIFY ("VARIABLEDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATA" MODIFY ("VARIABLETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATA" MODIFY ("ISRESULT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATA" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATA" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATA" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATA" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKACTIVITYDATABLOBS
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITYDATABLOBS" ADD CONSTRAINT "SHKACTIVITYDATABLOBS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKACTIVITYDATABLOBS" MODIFY ("ACTIVITYDATAWOB" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATABLOBS" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATABLOBS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATABLOBS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKACTIVITYDATAWOB
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITYDATAWOB" ADD CONSTRAINT "SHKACTIVITYDATAWOB_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKACTIVITYDATAWOB" MODIFY ("ACTIVITY" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATAWOB" MODIFY ("VARIABLEDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATAWOB" MODIFY ("VARIABLETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATAWOB" MODIFY ("ISRESULT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATAWOB" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATAWOB" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATAWOB" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYDATAWOB" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKACTIVITYSTATEEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITYSTATEEVENTAUDITS" ADD CONSTRAINT "SHKACTIVITYSTATEEVENTAUD5" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKACTIVITYSTATEEVENTAUDITS" MODIFY ("KEYVALUE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYSTATEEVENTAUDITS" MODIFY ("NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYSTATEEVENTAUDITS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYSTATEEVENTAUDITS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKACTIVITYSTATES
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITYSTATES" ADD CONSTRAINT "SHKACTIVITYSTATES_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKACTIVITYSTATES" MODIFY ("KEYVALUE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYSTATES" MODIFY ("NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYSTATES" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKACTIVITYSTATES" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKANDJOINTABLE
--------------------------------------------------------

  ALTER TABLE "SHKANDJOINTABLE" ADD CONSTRAINT "SHKANDJOINTABLE_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKANDJOINTABLE" MODIFY ("PROCESS" NOT NULL ENABLE);
 
  ALTER TABLE "SHKANDJOINTABLE" MODIFY ("ACTIVITYDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKANDJOINTABLE" MODIFY ("ACTIVITY" NOT NULL ENABLE);
 
  ALTER TABLE "SHKANDJOINTABLE" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKANDJOINTABLE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKANDJOINTABLE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKASSIGNMENTEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" ADD CONSTRAINT "SHKASSIGNMENTEVENTAUDIT17" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("RECORDEDTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("RECORDEDTIMETZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("THEUSERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("THETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("ACTIVITYID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("PROCESSID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("PROCESSFACTORYNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("PROCESSFACTORYVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("ACTIVITYDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("ACTIVITYDEFINITIONTYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("PROCESSDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("NEWRESOURCEUSERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("ISACCEPTED" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKASSIGNMENTSTABLE
--------------------------------------------------------

  ALTER TABLE "SHKASSIGNMENTSTABLE" ADD CONSTRAINT "SHKASSIGNMENTSTABLE_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("ACTIVITY" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("THERESOURCE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("ACTIVITYID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("ACTIVITYPROCESSID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("ACTIVITYPROCESSDEFNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("RESOURCEID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("ISACCEPTED" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("ISVALID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKCOUNTERS
--------------------------------------------------------

  ALTER TABLE "SHKCOUNTERS" ADD CONSTRAINT "SHKCOUNTERS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKCOUNTERS" MODIFY ("NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCOUNTERS" MODIFY ("THE_NUMBER" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCOUNTERS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCOUNTERS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKCREATEPROCESSEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" ADD CONSTRAINT "SHKCREATEPROCESSEVENTAUD8" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("RECORDEDTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("RECORDEDTIMETZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("THEUSERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("THETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("PROCESSID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("PROCESSFACTORYNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("PROCESSFACTORYVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("PROCESSDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKDATAEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKDATAEVENTAUDITS" ADD CONSTRAINT "SHKDATAEVENTAUDITS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("RECORDEDTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("RECORDEDTIMETZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("THEUSERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("THETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("PROCESSID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("PROCESSFACTORYNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("PROCESSFACTORYVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("PROCESSDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDATAEVENTAUDITS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKDEADLINES
--------------------------------------------------------

  ALTER TABLE "SHKDEADLINES" ADD CONSTRAINT "SHKDEADLINES_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("PROCESS" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("ACTIVITY" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("TIMELIMIT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("TIMELIMITTZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("EXCEPTIONNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("ISSYNCHRONOUS" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("ISEXECUTED" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKDEADLINES" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKEVENTTYPES
--------------------------------------------------------

  ALTER TABLE "SHKEVENTTYPES" ADD CONSTRAINT "SHKEVENTTYPES_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKEVENTTYPES" MODIFY ("KEYVALUE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKEVENTTYPES" MODIFY ("NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKEVENTTYPES" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKEVENTTYPES" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKGROUPGROUPTABLE
--------------------------------------------------------

  ALTER TABLE "SHKGROUPGROUPTABLE" ADD CONSTRAINT "SHKGROUPGROUPTABLE_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKGROUPGROUPTABLE" MODIFY ("SUB_GID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPGROUPTABLE" MODIFY ("GROUPID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPGROUPTABLE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPGROUPTABLE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKGROUPTABLE
--------------------------------------------------------

  ALTER TABLE "SHKGROUPTABLE" ADD CONSTRAINT "SHKGROUPTABLE_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKGROUPTABLE" MODIFY ("GROUPID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPTABLE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPTABLE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKGROUPUSER
--------------------------------------------------------

  ALTER TABLE "SHKGROUPUSER" ADD CONSTRAINT "SHKGROUPUSER_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKGROUPUSER" MODIFY ("USERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPUSER" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPUSER" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKGROUPUSERPACKLEVELPART
--------------------------------------------------------

  ALTER TABLE "SHKGROUPUSERPACKLEVELPART" ADD CONSTRAINT "SHKGROUPUSERPACKLEVELPA20" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKGROUPUSERPACKLEVELPART" MODIFY ("PARTICIPANTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPUSERPACKLEVELPART" MODIFY ("USEROID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPUSERPACKLEVELPART" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPUSERPACKLEVELPART" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKGROUPUSERPROCLEVELPART
--------------------------------------------------------

  ALTER TABLE "SHKGROUPUSERPROCLEVELPART" ADD CONSTRAINT "SHKGROUPUSERPROCLEVELPA24" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKGROUPUSERPROCLEVELPART" MODIFY ("PARTICIPANTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPUSERPROCLEVELPART" MODIFY ("USEROID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPUSERPROCLEVELPART" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKGROUPUSERPROCLEVELPART" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKNEWEVENTAUDITDATA
--------------------------------------------------------

  ALTER TABLE "SHKNEWEVENTAUDITDATA" ADD CONSTRAINT "SHKNEWEVENTAUDITDATA_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKNEWEVENTAUDITDATA" MODIFY ("DATAEVENTAUDIT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATA" MODIFY ("VARIABLEDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATA" MODIFY ("VARIABLETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATA" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATA" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATA" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATA" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKNEWEVENTAUDITDATABLOBS
--------------------------------------------------------

  ALTER TABLE "SHKNEWEVENTAUDITDATABLOBS" ADD CONSTRAINT "SHKNEWEVENTAUDITDATABLO33" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKNEWEVENTAUDITDATABLOBS" MODIFY ("NEWEVENTAUDITDATAWOB" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATABLOBS" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATABLOBS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATABLOBS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKNEWEVENTAUDITDATAWOB
--------------------------------------------------------

  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" ADD CONSTRAINT "SHKNEWEVENTAUDITDATAWOB30" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" MODIFY ("DATAEVENTAUDIT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" MODIFY ("VARIABLEDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" MODIFY ("VARIABLETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKNEXTXPDLVERSIONS
--------------------------------------------------------

  ALTER TABLE "SHKNEXTXPDLVERSIONS" ADD CONSTRAINT "SHKNEXTXPDLVERSIONS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKNEXTXPDLVERSIONS" MODIFY ("XPDLID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEXTXPDLVERSIONS" MODIFY ("NEXTVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEXTXPDLVERSIONS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNEXTXPDLVERSIONS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKNORMALUSER
--------------------------------------------------------

  ALTER TABLE "SHKNORMALUSER" ADD CONSTRAINT "SHKNORMALUSER_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKNORMALUSER" MODIFY ("USERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNORMALUSER" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKNORMALUSER" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKOLDEVENTAUDITDATA
--------------------------------------------------------

  ALTER TABLE "SHKOLDEVENTAUDITDATA" ADD CONSTRAINT "SHKOLDEVENTAUDITDATA_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKOLDEVENTAUDITDATA" MODIFY ("DATAEVENTAUDIT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATA" MODIFY ("VARIABLEDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATA" MODIFY ("VARIABLETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATA" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATA" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATA" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATA" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKOLDEVENTAUDITDATABLOBS
--------------------------------------------------------

  ALTER TABLE "SHKOLDEVENTAUDITDATABLOBS" ADD CONSTRAINT "SHKOLDEVENTAUDITDATABLO25" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKOLDEVENTAUDITDATABLOBS" MODIFY ("OLDEVENTAUDITDATAWOB" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATABLOBS" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATABLOBS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATABLOBS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKOLDEVENTAUDITDATAWOB
--------------------------------------------------------

  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" ADD CONSTRAINT "SHKOLDEVENTAUDITDATAWOB22" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" MODIFY ("DATAEVENTAUDIT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" MODIFY ("VARIABLEDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" MODIFY ("VARIABLETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPACKLEVELPARTICIPANT
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELPARTICIPANT" ADD CONSTRAINT "SHKPACKLEVELPARTICIPANT_7" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELPARTICIPANT" MODIFY ("PARTICIPANT_ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELPARTICIPANT" MODIFY ("PACKAGEOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELPARTICIPANT" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELPARTICIPANT" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPACKLEVELXPDLAPP
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPP" ADD CONSTRAINT "SHKPACKLEVELXPDLAPP_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPP" MODIFY ("APPLICATION_ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPP" MODIFY ("PACKAGEOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPP" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPP" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPACKLEVELXPDLAPPTAAPPDETAIL
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETAIL" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP21" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETAIL" MODIFY ("XPDL_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETAIL" MODIFY ("TOOLAGENTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETAIL" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETAIL" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPACKLEVELXPDLAPPTAAPPDETUSR
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETUSR" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP29" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETUSR" MODIFY ("XPDL_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETUSR" MODIFY ("TOOLAGENTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETUSR" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETUSR" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPACKLEVELXPDLAPPTAAPPUSER
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPUSER" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP25" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPUSER" MODIFY ("XPDL_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPUSER" MODIFY ("TOOLAGENTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPUSER" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPUSER" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPACKLEVELXPDLAPPTOOLAGNTAPP
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTOOL17" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" MODIFY ("XPDL_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" MODIFY ("TOOLAGENTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCESSDATA
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSDATA" ADD CONSTRAINT "SHKPROCESSDATA_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSDATA" MODIFY ("PROCESS" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATA" MODIFY ("VARIABLEDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATA" MODIFY ("VARIABLETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATA" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATA" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATA" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATA" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCESSDATABLOBS
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSDATABLOBS" ADD CONSTRAINT "SHKPROCESSDATABLOBS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSDATABLOBS" MODIFY ("PROCESSDATAWOB" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATABLOBS" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATABLOBS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATABLOBS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCESSDATAWOB
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSDATAWOB" ADD CONSTRAINT "SHKPROCESSDATAWOB_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSDATAWOB" MODIFY ("PROCESS" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATAWOB" MODIFY ("VARIABLEDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATAWOB" MODIFY ("VARIABLETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATAWOB" MODIFY ("ORDNO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATAWOB" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATAWOB" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDATAWOB" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCESSDEFINITIONS
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSDEFINITIONS" ADD CONSTRAINT "SHKPROCESSDEFINITIONS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSDEFINITIONS" MODIFY ("NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDEFINITIONS" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDEFINITIONS" MODIFY ("PROCESSDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDEFINITIONS" MODIFY ("PROCESSDEFINITIONCREATED" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDEFINITIONS" MODIFY ("PROCESSDEFINITIONVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDEFINITIONS" MODIFY ("STATE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDEFINITIONS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSDEFINITIONS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCESSES
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSES" ADD CONSTRAINT "SHKPROCESSES_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("SYNCVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("PROCESSDEFINITION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("PDEFNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("RESOURCEREQUESTERID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("STATE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("CREATED" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("CREATEDTZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("LASTSTATETIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("LASTSTATETIMETZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("LIMITTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("LIMITTIMETZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSES" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCESSREQUESTERS
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSREQUESTERS" ADD CONSTRAINT "SHKPROCESSREQUESTERS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSREQUESTERS" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSREQUESTERS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSREQUESTERS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCESSSTATEEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSSTATEEVENTAUDITS" ADD CONSTRAINT "SHKPROCESSSTATEEVENTAUDI2" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSSTATEEVENTAUDITS" MODIFY ("KEYVALUE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSSTATEEVENTAUDITS" MODIFY ("NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSSTATEEVENTAUDITS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSSTATEEVENTAUDITS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCESSSTATES
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSSTATES" ADD CONSTRAINT "SHKPROCESSSTATES_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSSTATES" MODIFY ("KEYVALUE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSSTATES" MODIFY ("NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSSTATES" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCESSSTATES" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCLEVELPARTICIPANT
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELPARTICIPANT" ADD CONSTRAINT "SHKPROCLEVELPARTICIPANT_4" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELPARTICIPANT" MODIFY ("PARTICIPANT_ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELPARTICIPANT" MODIFY ("PROCESSOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELPARTICIPANT" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELPARTICIPANT" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCLEVELXPDLAPP
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPP" ADD CONSTRAINT "SHKPROCLEVELXPDLAPP_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPP" MODIFY ("APPLICATION_ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPP" MODIFY ("PROCESSOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPP" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPP" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCLEVELXPDLAPPTAAPPDETAIL
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETAIL" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP37" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETAIL" MODIFY ("XPDL_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETAIL" MODIFY ("TOOLAGENTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETAIL" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETAIL" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCLEVELXPDLAPPTAAPPDETUSR
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETUSR" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP45" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETUSR" MODIFY ("XPDL_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETUSR" MODIFY ("TOOLAGENTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETUSR" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETUSR" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCLEVELXPDLAPPTAAPPUSER
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPUSER" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP41" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPUSER" MODIFY ("XPDL_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPUSER" MODIFY ("TOOLAGENTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPUSER" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPUSER" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKPROCLEVELXPDLAPPTOOLAGNTAPP
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTOOL33" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" MODIFY ("XPDL_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" MODIFY ("TOOLAGENTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKRESOURCESTABLE
--------------------------------------------------------

  ALTER TABLE "SHKRESOURCESTABLE" ADD CONSTRAINT "SHKRESOURCESTABLE_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKRESOURCESTABLE" MODIFY ("USERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKRESOURCESTABLE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKRESOURCESTABLE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKSTATEEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKSTATEEVENTAUDITS" ADD CONSTRAINT "SHKSTATEEVENTAUDITS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("RECORDEDTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("RECORDEDTIMETZO" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("THEUSERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("THETYPE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("PROCESSID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("PROCESSFACTORYNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("PROCESSFACTORYVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("PROCESSDEFINITIONID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("PACKAGEID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKTOOLAGENTAPP
--------------------------------------------------------

  ALTER TABLE "SHKTOOLAGENTAPP" ADD CONSTRAINT "SHKTOOLAGENTAPP_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKTOOLAGENTAPP" MODIFY ("TOOL_AGENT_NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPP" MODIFY ("APP_NAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPP" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPP" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKTOOLAGENTAPPDETAIL
--------------------------------------------------------

  ALTER TABLE "SHKTOOLAGENTAPPDETAIL" ADD CONSTRAINT "SHKTOOLAGENTAPPDETAIL_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAIL" MODIFY ("APP_MODE" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAIL" MODIFY ("TOOLAGENT_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAIL" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAIL" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKTOOLAGENTAPPDETAILUSER
--------------------------------------------------------

  ALTER TABLE "SHKTOOLAGENTAPPDETAILUSER" ADD CONSTRAINT "SHKTOOLAGENTAPPDETAILUS13" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAILUSER" MODIFY ("TOOLAGENT_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAILUSER" MODIFY ("USEROID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAILUSER" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAILUSER" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKTOOLAGENTAPPUSER
--------------------------------------------------------

  ALTER TABLE "SHKTOOLAGENTAPPUSER" ADD CONSTRAINT "SHKTOOLAGENTAPPUSER_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKTOOLAGENTAPPUSER" MODIFY ("TOOLAGENT_APPOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPUSER" MODIFY ("USEROID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPUSER" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTAPPUSER" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKTOOLAGENTUSER
--------------------------------------------------------

  ALTER TABLE "SHKTOOLAGENTUSER" ADD CONSTRAINT "SHKTOOLAGENTUSER_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKTOOLAGENTUSER" MODIFY ("USERNAME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTUSER" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKTOOLAGENTUSER" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKUSERGROUPTABLE
--------------------------------------------------------

  ALTER TABLE "SHKUSERGROUPTABLE" ADD CONSTRAINT "SHKUSERGROUPTABLE_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKUSERGROUPTABLE" MODIFY ("USERID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERGROUPTABLE" MODIFY ("GROUPID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERGROUPTABLE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERGROUPTABLE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKUSERPACKLEVELPART
--------------------------------------------------------

  ALTER TABLE "SHKUSERPACKLEVELPART" ADD CONSTRAINT "SHKUSERPACKLEVELPART_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKUSERPACKLEVELPART" MODIFY ("PARTICIPANTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERPACKLEVELPART" MODIFY ("USEROID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERPACKLEVELPART" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERPACKLEVELPART" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKUSERPROCLEVELPARTICIPANT
--------------------------------------------------------

  ALTER TABLE "SHKUSERPROCLEVELPARTICIPANT" ADD CONSTRAINT "SHKUSERPROCLEVELPARTICI14" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKUSERPROCLEVELPARTICIPANT" MODIFY ("PARTICIPANTOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERPROCLEVELPARTICIPANT" MODIFY ("USEROID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERPROCLEVELPARTICIPANT" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERPROCLEVELPARTICIPANT" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKUSERTABLE
--------------------------------------------------------

  ALTER TABLE "SHKUSERTABLE" ADD CONSTRAINT "SHKUSERTABLE_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKUSERTABLE" MODIFY ("USERID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERTABLE" MODIFY ("PASSWD" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERTABLE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKUSERTABLE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLAPPLICATIONPACKAGE
--------------------------------------------------------

  ALTER TABLE "SHKXPDLAPPLICATIONPACKAGE" ADD CONSTRAINT "SHKXPDLAPPLICATIONPACKAG1" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLAPPLICATIONPACKAGE" MODIFY ("PACKAGE_ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLAPPLICATIONPACKAGE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLAPPLICATIONPACKAGE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLAPPLICATIONPROCESS
--------------------------------------------------------

  ALTER TABLE "SHKXPDLAPPLICATIONPROCESS" ADD CONSTRAINT "SHKXPDLAPPLICATIONPROCES4" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLAPPLICATIONPROCESS" MODIFY ("PROCESS_ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLAPPLICATIONPROCESS" MODIFY ("PACKAGEOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLAPPLICATIONPROCESS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLAPPLICATIONPROCESS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLDATA
--------------------------------------------------------

  ALTER TABLE "SHKXPDLDATA" ADD CONSTRAINT "SHKXPDLDATA_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLDATA" MODIFY ("XPDLCONTENT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLDATA" MODIFY ("XPDLCLASSCONTENT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLDATA" MODIFY ("XPDL" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLDATA" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLDATA" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLDATA" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLHISTORY
--------------------------------------------------------

  ALTER TABLE "SHKXPDLHISTORY" ADD CONSTRAINT "SHKXPDLHISTORY_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLHISTORY" MODIFY ("XPDLID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORY" MODIFY ("XPDLVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORY" MODIFY ("XPDLCLASSVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORY" MODIFY ("XPDLUPLOADTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORY" MODIFY ("XPDLHISTORYUPLOADTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORY" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORY" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLHISTORYDATA
--------------------------------------------------------

  ALTER TABLE "SHKXPDLHISTORYDATA" ADD CONSTRAINT "SHKXPDLHISTORYDATA_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLHISTORYDATA" MODIFY ("XPDLCONTENT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORYDATA" MODIFY ("XPDLCLASSCONTENT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORYDATA" MODIFY ("XPDLHISTORY" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORYDATA" MODIFY ("CNT" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORYDATA" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLHISTORYDATA" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLPARTICIPANTPACKAGE
--------------------------------------------------------

  ALTER TABLE "SHKXPDLPARTICIPANTPACKAGE" ADD CONSTRAINT "SHKXPDLPARTICIPANTPACKAG1" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLPARTICIPANTPACKAGE" MODIFY ("PACKAGE_ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLPARTICIPANTPACKAGE" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLPARTICIPANTPACKAGE" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLPARTICIPANTPROCESS
--------------------------------------------------------

  ALTER TABLE "SHKXPDLPARTICIPANTPROCESS" ADD CONSTRAINT "SHKXPDLPARTICIPANTPROCE10" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLPARTICIPANTPROCESS" MODIFY ("PROCESS_ID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLPARTICIPANTPROCESS" MODIFY ("PACKAGEOID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLPARTICIPANTPROCESS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLPARTICIPANTPROCESS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLREFERENCES
--------------------------------------------------------

  ALTER TABLE "SHKXPDLREFERENCES" ADD CONSTRAINT "SHKXPDLREFERENCES_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLREFERENCES" MODIFY ("REFERREDXPDLID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLREFERENCES" MODIFY ("REFERRINGXPDL" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLREFERENCES" MODIFY ("REFERREDXPDLNUMBER" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLREFERENCES" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLREFERENCES" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table SHKXPDLS
--------------------------------------------------------

  ALTER TABLE "SHKXPDLS" ADD CONSTRAINT "SHKXPDLS_OID" PRIMARY KEY ("OID") ENABLE;
 
  ALTER TABLE "SHKXPDLS" MODIFY ("XPDLID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLS" MODIFY ("XPDLVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLS" MODIFY ("XPDLCLASSVERSION" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLS" MODIFY ("XPDLUPLOADTIME" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLS" MODIFY ("OID" NOT NULL ENABLE);
 
  ALTER TABLE "SHKXPDLS" MODIFY ("VERSION" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table WF_AUDIT_TRAIL
--------------------------------------------------------

  ALTER TABLE "WF_AUDIT_TRAIL" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "WF_AUDIT_TRAIL" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table WF_PROCESS_LINK
--------------------------------------------------------

  ALTER TABLE "WF_PROCESS_LINK" MODIFY ("PROCESSID" NOT NULL ENABLE);
 
  ALTER TABLE "WF_PROCESS_LINK" ADD PRIMARY KEY ("PROCESSID") ENABLE;
--------------------------------------------------------
--  Constraints for Table WF_RESOURCE_BUNDLE_MESSAGE
--------------------------------------------------------

  ALTER TABLE "WF_RESOURCE_BUNDLE_MESSAGE" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "WF_RESOURCE_BUNDLE_MESSAGE" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table WF_SETUP
--------------------------------------------------------

  ALTER TABLE "WF_SETUP" MODIFY ("ID" NOT NULL ENABLE);
 
  ALTER TABLE "WF_SETUP" ADD PRIMARY KEY ("ID") ENABLE;
--------------------------------------------------------
--  DDL for Index I1_SHKACTIVITIES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKACTIVITIES" ON "SHKACTIVITIES" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKACTIVITYDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKACTIVITYDATA" ON "SHKACTIVITYDATA" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKACTIVITYDATABLOBS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKACTIVITYDATABLOBS" ON "SHKACTIVITYDATABLOBS" ("ACTIVITYDATAWOB", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKACTIVITYDATAWOB
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKACTIVITYDATAWOB" ON "SHKACTIVITYDATAWOB" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKACTIVITYSTATEEVENT3
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKACTIVITYSTATEEVENT3" ON "SHKACTIVITYSTATEEVENTAUDITS" ("KEYVALUE") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKACTIVITYSTATES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKACTIVITYSTATES" ON "SHKACTIVITYSTATES" ("KEYVALUE") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKANDJOINTABLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKANDJOINTABLE" ON "SHKANDJOINTABLE" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKASSIGNMENTEVENTAU15
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKASSIGNMENTEVENTAU15" ON "SHKASSIGNMENTEVENTAUDITS" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKASSIGNMENTSTABLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKASSIGNMENTSTABLE" ON "SHKASSIGNMENTSTABLE" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKCOUNTERS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKCOUNTERS" ON "SHKCOUNTERS" ("NAME") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKCREATEPROCESSEVENT6
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKCREATEPROCESSEVENT6" ON "SHKCREATEPROCESSEVENTAUDITS" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKDATAEVENTAUDITS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKDATAEVENTAUDITS" ON "SHKDATAEVENTAUDITS" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKDEADLINES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKDEADLINES" ON "SHKDEADLINES" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKEVENTTYPES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKEVENTTYPES" ON "SHKEVENTTYPES" ("KEYVALUE") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKGROUPGROUPTABLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKGROUPGROUPTABLE" ON "SHKGROUPGROUPTABLE" ("SUB_GID", "GROUPID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKGROUPTABLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKGROUPTABLE" ON "SHKGROUPTABLE" ("GROUPID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKGROUPUSER
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKGROUPUSER" ON "SHKGROUPUSER" ("USERNAME") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKGROUPUSERPACKLEVE17
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKGROUPUSERPACKLEVE17" ON "SHKGROUPUSERPACKLEVELPART" ("PARTICIPANTOID", "USEROID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKGROUPUSERPROCLEVE21
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKGROUPUSERPROCLEVE21" ON "SHKGROUPUSERPROCLEVELPART" ("PARTICIPANTOID", "USEROID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKNEWEVENTAUDITDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKNEWEVENTAUDITDATA" ON "SHKNEWEVENTAUDITDATA" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKNEWEVENTAUDITDATA27
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKNEWEVENTAUDITDATA27" ON "SHKNEWEVENTAUDITDATAWOB" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKNEWEVENTAUDITDATA31
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKNEWEVENTAUDITDATA31" ON "SHKNEWEVENTAUDITDATABLOBS" ("NEWEVENTAUDITDATAWOB", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKNEXTXPDLVERSIONS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKNEXTXPDLVERSIONS" ON "SHKNEXTXPDLVERSIONS" ("XPDLID", "NEXTVERSION") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKNORMALUSER
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKNORMALUSER" ON "SHKNORMALUSER" ("USERNAME") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKOLDEVENTAUDITDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKOLDEVENTAUDITDATA" ON "SHKOLDEVENTAUDITDATA" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKOLDEVENTAUDITDATA19
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKOLDEVENTAUDITDATA19" ON "SHKOLDEVENTAUDITDATAWOB" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKOLDEVENTAUDITDATA23
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKOLDEVENTAUDITDATA23" ON "SHKOLDEVENTAUDITDATABLOBS" ("OLDEVENTAUDITDATAWOB", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPACKLEVELPARTICIPA5
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPACKLEVELPARTICIPA5" ON "SHKPACKLEVELPARTICIPANT" ("PARTICIPANT_ID", "PACKAGEOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPACKLEVELXPDLAPP
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPACKLEVELXPDLAPP" ON "SHKPACKLEVELXPDLAPP" ("APPLICATION_ID", "PACKAGEOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPACKLEVELXPDLAPPT14
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPACKLEVELXPDLAPPT14" ON "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" ("XPDL_APPOID", "TOOLAGENTOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPACKLEVELXPDLAPPT18
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPACKLEVELXPDLAPPT18" ON "SHKPACKLEVELXPDLAPPTAAPPDETAIL" ("XPDL_APPOID", "TOOLAGENTOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPACKLEVELXPDLAPPT22
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPACKLEVELXPDLAPPT22" ON "SHKPACKLEVELXPDLAPPTAAPPUSER" ("XPDL_APPOID", "TOOLAGENTOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPACKLEVELXPDLAPPT26
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPACKLEVELXPDLAPPT26" ON "SHKPACKLEVELXPDLAPPTAAPPDETUSR" ("XPDL_APPOID", "TOOLAGENTOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCESSDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCESSDATA" ON "SHKPROCESSDATA" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCESSDATABLOBS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCESSDATABLOBS" ON "SHKPROCESSDATABLOBS" ("PROCESSDATAWOB", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCESSDATAWOB
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCESSDATAWOB" ON "SHKPROCESSDATAWOB" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCESSDEFINITIONS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCESSDEFINITIONS" ON "SHKPROCESSDEFINITIONS" ("NAME") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCESSES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCESSES" ON "SHKPROCESSES" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCESSREQUESTERS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCESSREQUESTERS" ON "SHKPROCESSREQUESTERS" ("ID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCESSSTATEEVENTA0
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCESSSTATEEVENTA0" ON "SHKPROCESSSTATEEVENTAUDITS" ("KEYVALUE") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCESSSTATES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCESSSTATES" ON "SHKPROCESSSTATES" ("KEYVALUE") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCLEVELPARTICIPA2
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCLEVELPARTICIPA2" ON "SHKPROCLEVELPARTICIPANT" ("PARTICIPANT_ID", "PROCESSOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCLEVELXPDLAPP
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCLEVELXPDLAPP" ON "SHKPROCLEVELXPDLAPP" ("APPLICATION_ID", "PROCESSOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCLEVELXPDLAPPT30
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCLEVELXPDLAPPT30" ON "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" ("XPDL_APPOID", "TOOLAGENTOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCLEVELXPDLAPPT34
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCLEVELXPDLAPPT34" ON "SHKPROCLEVELXPDLAPPTAAPPDETAIL" ("XPDL_APPOID", "TOOLAGENTOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCLEVELXPDLAPPT38
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCLEVELXPDLAPPT38" ON "SHKPROCLEVELXPDLAPPTAAPPUSER" ("XPDL_APPOID", "TOOLAGENTOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKPROCLEVELXPDLAPPT42
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKPROCLEVELXPDLAPPT42" ON "SHKPROCLEVELXPDLAPPTAAPPDETUSR" ("XPDL_APPOID", "TOOLAGENTOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKRESOURCESTABLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKRESOURCESTABLE" ON "SHKRESOURCESTABLE" ("USERNAME") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKSTATEEVENTAUDITS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKSTATEEVENTAUDITS" ON "SHKSTATEEVENTAUDITS" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKTOOLAGENTAPP
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKTOOLAGENTAPP" ON "SHKTOOLAGENTAPP" ("TOOL_AGENT_NAME", "APP_NAME") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKTOOLAGENTAPPDETAI10
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKTOOLAGENTAPPDETAI10" ON "SHKTOOLAGENTAPPDETAILUSER" ("TOOLAGENT_APPOID", "USEROID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKTOOLAGENTAPPDETAIL
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKTOOLAGENTAPPDETAIL" ON "SHKTOOLAGENTAPPDETAIL" ("APP_MODE", "TOOLAGENT_APPOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKTOOLAGENTAPPUSER
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKTOOLAGENTAPPUSER" ON "SHKTOOLAGENTAPPUSER" ("TOOLAGENT_APPOID", "USEROID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKTOOLAGENTUSER
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKTOOLAGENTUSER" ON "SHKTOOLAGENTUSER" ("USERNAME") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKUSERGROUPTABLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKUSERGROUPTABLE" ON "SHKUSERGROUPTABLE" ("USERID", "GROUPID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKUSERPACKLEVELPART
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKUSERPACKLEVELPART" ON "SHKUSERPACKLEVELPART" ("PARTICIPANTOID", "USEROID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKUSERPROCLEVELPART11
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKUSERPROCLEVELPART11" ON "SHKUSERPROCLEVELPARTICIPANT" ("PARTICIPANTOID", "USEROID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKUSERTABLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKUSERTABLE" ON "SHKUSERTABLE" ("USERID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLAPPLICATIONPAC0
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLAPPLICATIONPAC0" ON "SHKXPDLAPPLICATIONPACKAGE" ("PACKAGE_ID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLAPPLICATIONPRO2
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLAPPLICATIONPRO2" ON "SHKXPDLAPPLICATIONPROCESS" ("PROCESS_ID", "PACKAGEOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLDATA" ON "SHKXPDLDATA" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLHISTORY
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLHISTORY" ON "SHKXPDLHISTORY" ("XPDLID", "XPDLVERSION") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLHISTORYDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLHISTORYDATA" ON "SHKXPDLHISTORYDATA" ("CNT") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLPARTICIPANTPAC0
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLPARTICIPANTPAC0" ON "SHKXPDLPARTICIPANTPACKAGE" ("PACKAGE_ID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLPARTICIPANTPRO8
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLPARTICIPANTPRO8" ON "SHKXPDLPARTICIPANTPROCESS" ("PROCESS_ID", "PACKAGEOID") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLREFERENCES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLREFERENCES" ON "SHKXPDLREFERENCES" ("REFERREDXPDLID", "REFERRINGXPDL") 
  ;
--------------------------------------------------------
--  DDL for Index I1_SHKXPDLS
--------------------------------------------------------

  CREATE UNIQUE INDEX "I1_SHKXPDLS" ON "SHKXPDLS" ("XPDLID", "XPDLVERSION") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKACTIVITIES
--------------------------------------------------------

  CREATE INDEX "I2_SHKACTIVITIES" ON "SHKACTIVITIES" ("PROCESS", "ACTIVITYSETDEFINITIONID", "ACTIVITYDEFINITIONID") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKACTIVITYDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKACTIVITYDATA" ON "SHKACTIVITYDATA" ("ACTIVITY", "VARIABLEDEFINITIONID", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKACTIVITYDATAWOB
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKACTIVITYDATAWOB" ON "SHKACTIVITYDATAWOB" ("ACTIVITY", "VARIABLEDEFINITIONID", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKACTIVITYSTATEEVENT4
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKACTIVITYSTATEEVENT4" ON "SHKACTIVITYSTATEEVENTAUDITS" ("NAME") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKACTIVITYSTATES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKACTIVITYSTATES" ON "SHKACTIVITYSTATES" ("NAME") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKANDJOINTABLE
--------------------------------------------------------

  CREATE INDEX "I2_SHKANDJOINTABLE" ON "SHKANDJOINTABLE" ("PROCESS", "BLOCKACTIVITY", "ACTIVITYDEFINITIONID") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKASSIGNMENTSTABLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKASSIGNMENTSTABLE" ON "SHKASSIGNMENTSTABLE" ("ACTIVITY", "THERESOURCE") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKDEADLINES
--------------------------------------------------------

  CREATE INDEX "I2_SHKDEADLINES" ON "SHKDEADLINES" ("PROCESS", "TIMELIMIT") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKEVENTTYPES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKEVENTTYPES" ON "SHKEVENTTYPES" ("NAME") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKGROUPGROUPTABLE
--------------------------------------------------------

  CREATE INDEX "I2_SHKGROUPGROUPTABLE" ON "SHKGROUPGROUPTABLE" ("GROUPID") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKNEWEVENTAUDITDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKNEWEVENTAUDITDATA" ON "SHKNEWEVENTAUDITDATA" ("DATAEVENTAUDIT", "VARIABLEDEFINITIONID", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKNEWEVENTAUDITDATA28
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKNEWEVENTAUDITDATA28" ON "SHKNEWEVENTAUDITDATAWOB" ("DATAEVENTAUDIT", "VARIABLEDEFINITIONID", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKOLDEVENTAUDITDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKOLDEVENTAUDITDATA" ON "SHKOLDEVENTAUDITDATA" ("DATAEVENTAUDIT", "VARIABLEDEFINITIONID", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKOLDEVENTAUDITDATA20
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKOLDEVENTAUDITDATA20" ON "SHKOLDEVENTAUDITDATAWOB" ("DATAEVENTAUDIT", "VARIABLEDEFINITIONID", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKPROCESSDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKPROCESSDATA" ON "SHKPROCESSDATA" ("PROCESS", "VARIABLEDEFINITIONID", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKPROCESSDATAWOB
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKPROCESSDATAWOB" ON "SHKPROCESSDATAWOB" ("PROCESS", "VARIABLEDEFINITIONID", "ORDNO") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKPROCESSES
--------------------------------------------------------

  CREATE INDEX "I2_SHKPROCESSES" ON "SHKPROCESSES" ("PROCESSDEFINITION") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKPROCESSREQUESTERS
--------------------------------------------------------

  CREATE INDEX "I2_SHKPROCESSREQUESTERS" ON "SHKPROCESSREQUESTERS" ("ACTIVITYREQUESTER") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKPROCESSSTATEEVENTA1
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKPROCESSSTATEEVENTA1" ON "SHKPROCESSSTATEEVENTAUDITS" ("NAME") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKPROCESSSTATES
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKPROCESSSTATES" ON "SHKPROCESSSTATES" ("NAME") 
  ;
--------------------------------------------------------
--  DDL for Index I2_SHKXPDLDATA
--------------------------------------------------------

  CREATE UNIQUE INDEX "I2_SHKXPDLDATA" ON "SHKXPDLDATA" ("XPDL") 
  ;
--------------------------------------------------------
--  DDL for Index I3_SHKACTIVITIES
--------------------------------------------------------

  CREATE INDEX "I3_SHKACTIVITIES" ON "SHKACTIVITIES" ("PROCESS", "STATE") 
  ;
--------------------------------------------------------
--  DDL for Index I3_SHKANDJOINTABLE
--------------------------------------------------------

  CREATE INDEX "I3_SHKANDJOINTABLE" ON "SHKANDJOINTABLE" ("ACTIVITY") 
  ;
--------------------------------------------------------
--  DDL for Index I3_SHKASSIGNMENTSTABLE
--------------------------------------------------------

  CREATE INDEX "I3_SHKASSIGNMENTSTABLE" ON "SHKASSIGNMENTSTABLE" ("THERESOURCE", "ISVALID") 
  ;
--------------------------------------------------------
--  DDL for Index I3_SHKDEADLINES
--------------------------------------------------------

  CREATE INDEX "I3_SHKDEADLINES" ON "SHKDEADLINES" ("ACTIVITY", "TIMELIMIT") 
  ;
--------------------------------------------------------
--  DDL for Index I3_SHKPROCESSES
--------------------------------------------------------

  CREATE INDEX "I3_SHKPROCESSES" ON "SHKPROCESSES" ("STATE") 
  ;
--------------------------------------------------------
--  DDL for Index I3_SHKPROCESSREQUESTERS
--------------------------------------------------------

  CREATE INDEX "I3_SHKPROCESSREQUESTERS" ON "SHKPROCESSREQUESTERS" ("RESOURCEREQUESTER") 
  ;
--------------------------------------------------------
--  DDL for Index I4_SHKASSIGNMENTSTABLE
--------------------------------------------------------

  CREATE INDEX "I4_SHKASSIGNMENTSTABLE" ON "SHKASSIGNMENTSTABLE" ("ACTIVITYID") 
  ;
--------------------------------------------------------
--  DDL for Index I4_SHKPROCESSES
--------------------------------------------------------

  CREATE INDEX "I4_SHKPROCESSES" ON "SHKPROCESSES" ("ACTIVITYREQUESTERID") 
  ;
--------------------------------------------------------
--  DDL for Index I5_SHKASSIGNMENTSTABLE
--------------------------------------------------------

  CREATE INDEX "I5_SHKASSIGNMENTSTABLE" ON "SHKASSIGNMENTSTABLE" ("RESOURCEID") 
  ;
--------------------------------------------------------
--  DDL for Index I5_SHKPROCESSES
--------------------------------------------------------

  CREATE INDEX "I5_SHKPROCESSES" ON "SHKPROCESSES" ("RESOURCEREQUESTERID") 
  ;

--------------------------------------------------------
--  Ref Constraints for Table APP_DATALIST
--------------------------------------------------------

  ALTER TABLE "APP_DATALIST" ADD CONSTRAINT "FK5E9247A6462EF4C7" FOREIGN KEY ("APPID", "APPVERSION")
	  REFERENCES "APP_APP" ("APPID", "APPVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_ENV_VARIABLE
--------------------------------------------------------

  ALTER TABLE "APP_ENV_VARIABLE" ADD CONSTRAINT "FK740A62EC462EF4C7" FOREIGN KEY ("APPID", "APPVERSION")
	  REFERENCES "APP_APP" ("APPID", "APPVERSION") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table APP_FORM
--------------------------------------------------------

  ALTER TABLE "APP_FORM" ADD CONSTRAINT "FK45957822462EF4C7" FOREIGN KEY ("APPID", "APPVERSION")
	  REFERENCES "APP_APP" ("APPID", "APPVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_MESSAGE
--------------------------------------------------------

  ALTER TABLE "APP_MESSAGE" ADD CONSTRAINT "FKEE346FE9462EF4C7" FOREIGN KEY ("APPID", "APPVERSION")
	  REFERENCES "APP_APP" ("APPID", "APPVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_PACKAGE
--------------------------------------------------------

  ALTER TABLE "APP_PACKAGE" ADD CONSTRAINT "FK852EA428462EF4C7" FOREIGN KEY ("APPID", "APPVERSION")
	  REFERENCES "APP_APP" ("APPID", "APPVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_PACKAGE_ACTIVITY_FORM
--------------------------------------------------------

  ALTER TABLE "APP_PACKAGE_ACTIVITY_FORM" ADD CONSTRAINT "FKA8D741D5F255BCC" FOREIGN KEY ("PACKAGEID", "PACKAGEVERSION")
	  REFERENCES "APP_PACKAGE" ("PACKAGEID", "PACKAGEVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_PACKAGE_ACTIVITY_PLUGIN
--------------------------------------------------------

  ALTER TABLE "APP_PACKAGE_ACTIVITY_PLUGIN" ADD CONSTRAINT "FKADE8644C5F255BCC" FOREIGN KEY ("PACKAGEID", "PACKAGEVERSION")
	  REFERENCES "APP_PACKAGE" ("PACKAGEID", "PACKAGEVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_PACKAGE_PARTICIPANT
--------------------------------------------------------

  ALTER TABLE "APP_PACKAGE_PARTICIPANT" ADD CONSTRAINT "FK6D7BF59C5F255BCC" FOREIGN KEY ("PACKAGEID", "PACKAGEVERSION")
	  REFERENCES "APP_PACKAGE" ("PACKAGEID", "PACKAGEVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_PLUGIN_DEFAULT
--------------------------------------------------------

  ALTER TABLE "APP_PLUGIN_DEFAULT" ADD CONSTRAINT "FK7A835713462EF4C7" FOREIGN KEY ("APPID", "APPVERSION")
	  REFERENCES "APP_APP" ("APPID", "APPVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_REPORT_ACTIVITY
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_ACTIVITY" ADD CONSTRAINT "FK5E33D79C918F93D" FOREIGN KEY ("PROCESSUID")
	  REFERENCES "APP_REPORT_PROCESS" ("UUID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_REPORT_ACTIVITY_INSTANCE
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_ACTIVITY_INSTANCE" ADD CONSTRAINT "FK9C6ABDD8B06E2043" FOREIGN KEY ("ACTIVITYUID")
	  REFERENCES "APP_REPORT_ACTIVITY" ("UUID") ENABLE;
 
  ALTER TABLE "APP_REPORT_ACTIVITY_INSTANCE" ADD CONSTRAINT "FK9C6ABDD8D4610A90" FOREIGN KEY ("PROCESSINSTANCEID")
	  REFERENCES "APP_REPORT_PROCESS_INSTANCE" ("INSTANCEID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table APP_REPORT_PACKAGE
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_PACKAGE" ADD CONSTRAINT "FKBD580A19E475ABC" FOREIGN KEY ("APPUID")
	  REFERENCES "APP_REPORT_APP" ("UUID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_REPORT_PROCESS
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_PROCESS" ADD CONSTRAINT "FKDAFFF442D40695DD" FOREIGN KEY ("PACKAGEUID")
	  REFERENCES "APP_REPORT_PACKAGE" ("UUID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_REPORT_PROCESS_INSTANCE
--------------------------------------------------------

  ALTER TABLE "APP_REPORT_PROCESS_INSTANCE" ADD CONSTRAINT "FK351D7BF2918F93D" FOREIGN KEY ("PROCESSUID")
	  REFERENCES "APP_REPORT_PROCESS" ("UUID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table APP_USERVIEW
--------------------------------------------------------

  ALTER TABLE "APP_USERVIEW" ADD CONSTRAINT "FKE411D54E462EF4C7" FOREIGN KEY ("APPID", "APPVERSION")
	  REFERENCES "APP_APP" ("APPID", "APPVERSION") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table DIR_DEPARTMENT
--------------------------------------------------------

  ALTER TABLE "DIR_DEPARTMENT" ADD CONSTRAINT "FKEEE8AA4418CEBAE1" FOREIGN KEY ("ORGANIZATIONID")
	  REFERENCES "DIR_ORGANIZATION" ("ID") ON DELETE CASCADE ENABLE;
 
  ALTER TABLE "DIR_DEPARTMENT" ADD CONSTRAINT "FKEEE8AA4480DB1449" FOREIGN KEY ("HOD")
	  REFERENCES "DIR_EMPLOYMENT" ("ID") ENABLE;
 
  ALTER TABLE "DIR_DEPARTMENT" ADD CONSTRAINT "FKEEE8AA44EF6BB2B7" FOREIGN KEY ("PARENTID")
	  REFERENCES "DIR_DEPARTMENT" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table DIR_EMPLOYMENT
--------------------------------------------------------

  ALTER TABLE "DIR_EMPLOYMENT" ADD CONSTRAINT "FKC6620ADE14CE02E9" FOREIGN KEY ("GRADEID")
	  REFERENCES "DIR_GRADE" ("ID") ENABLE;
 
  ALTER TABLE "DIR_EMPLOYMENT" ADD CONSTRAINT "FKC6620ADE18CEBAE1" FOREIGN KEY ("ORGANIZATIONID")
	  REFERENCES "DIR_ORGANIZATION" ("ID") ON DELETE CASCADE ENABLE;
 
  ALTER TABLE "DIR_EMPLOYMENT" ADD CONSTRAINT "FKC6620ADE716AE35F" FOREIGN KEY ("DEPARTMENTID")
	  REFERENCES "DIR_DEPARTMENT" ("ID") ENABLE;
 
  ALTER TABLE "DIR_EMPLOYMENT" ADD CONSTRAINT "FKC6620ADECE539211" FOREIGN KEY ("USERID")
	  REFERENCES "DIR_USER" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table DIR_EMPLOYMENT_REPORT_TO
--------------------------------------------------------

  ALTER TABLE "DIR_EMPLOYMENT_REPORT_TO" ADD CONSTRAINT "FK536229452787E613" FOREIGN KEY ("EMPLOYMENTID")
	  REFERENCES "DIR_EMPLOYMENT" ("ID") ENABLE;
 
  ALTER TABLE "DIR_EMPLOYMENT_REPORT_TO" ADD CONSTRAINT "FK53622945F4068416" FOREIGN KEY ("REPORTTOID")
	  REFERENCES "DIR_EMPLOYMENT" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table DIR_GRADE
--------------------------------------------------------

  ALTER TABLE "DIR_GRADE" ADD CONSTRAINT "FKBC9A49A518CEBAE1" FOREIGN KEY ("ORGANIZATIONID")
	  REFERENCES "DIR_ORGANIZATION" ("ID") ON DELETE CASCADE ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table DIR_GROUP
--------------------------------------------------------

  ALTER TABLE "DIR_GROUP" ADD CONSTRAINT "FKBC9A804D18CEBAE1" FOREIGN KEY ("ORGANIZATIONID")
	  REFERENCES "DIR_ORGANIZATION" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table DIR_ORGANIZATION
--------------------------------------------------------

  ALTER TABLE "DIR_ORGANIZATION" ADD CONSTRAINT "FK55A15FA5961BD498" FOREIGN KEY ("PARENTID")
	  REFERENCES "DIR_ORGANIZATION" ("ID") ENABLE;


--------------------------------------------------------
--  Ref Constraints for Table DIR_USER_GROUP
--------------------------------------------------------

  ALTER TABLE "DIR_USER_GROUP" ADD CONSTRAINT "FK2F0367FD159B6639" FOREIGN KEY ("GROUPID")
	  REFERENCES "DIR_GROUP" ("ID") ENABLE;
 
  ALTER TABLE "DIR_USER_GROUP" ADD CONSTRAINT "FK2F0367FDCE539211" FOREIGN KEY ("USERID")
	  REFERENCES "DIR_USER" ("ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table DIR_USER_ROLE
--------------------------------------------------------

  ALTER TABLE "DIR_USER_ROLE" ADD CONSTRAINT "FK5C5FE738C8FE3CA7" FOREIGN KEY ("ROLEID")
	  REFERENCES "DIR_ROLE" ("ID") ENABLE;
 
  ALTER TABLE "DIR_USER_ROLE" ADD CONSTRAINT "FK5C5FE738CE539211" FOREIGN KEY ("USERID")
	  REFERENCES "DIR_USER" ("ID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKACTIVITIES
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITIES" ADD CONSTRAINT "SHKACTIVITIES_PROCESS" FOREIGN KEY ("PROCESS")
	  REFERENCES "SHKPROCESSES" ("OID") ENABLE;
 
  ALTER TABLE "SHKACTIVITIES" ADD CONSTRAINT "SHKACTIVITIES_STATE" FOREIGN KEY ("STATE")
	  REFERENCES "SHKACTIVITYSTATES" ("OID") ENABLE;
 
  ALTER TABLE "SHKACTIVITIES" ADD CONSTRAINT "SHKACTIVITIES_THERESOURCE" FOREIGN KEY ("THERESOURCE")
	  REFERENCES "SHKRESOURCESTABLE" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKACTIVITYDATA
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITYDATA" ADD CONSTRAINT "SHKACTIVITYDATA_ACTIVITY" FOREIGN KEY ("ACTIVITY")
	  REFERENCES "SHKACTIVITIES" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKACTIVITYDATABLOBS
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITYDATABLOBS" ADD CONSTRAINT "SHKACTIVITYDATABLOBS_ACT7" FOREIGN KEY ("ACTIVITYDATAWOB")
	  REFERENCES "SHKACTIVITYDATAWOB" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKACTIVITYDATAWOB
--------------------------------------------------------

  ALTER TABLE "SHKACTIVITYDATAWOB" ADD CONSTRAINT "SHKACTIVITYDATAWOB_ACTIV6" FOREIGN KEY ("ACTIVITY")
	  REFERENCES "SHKACTIVITIES" ("OID") ENABLE;


--------------------------------------------------------
--  Ref Constraints for Table SHKANDJOINTABLE
--------------------------------------------------------

  ALTER TABLE "SHKANDJOINTABLE" ADD CONSTRAINT "SHKANDJOINTABLE_ACTIVITY" FOREIGN KEY ("ACTIVITY")
	  REFERENCES "SHKACTIVITIES" ("OID") ENABLE;
 
  ALTER TABLE "SHKANDJOINTABLE" ADD CONSTRAINT "SHKANDJOINTABLE_BLOCKACT8" FOREIGN KEY ("BLOCKACTIVITY")
	  REFERENCES "SHKACTIVITIES" ("OID") ENABLE;
 
  ALTER TABLE "SHKANDJOINTABLE" ADD CONSTRAINT "SHKANDJOINTABLE_PROCESS" FOREIGN KEY ("PROCESS")
	  REFERENCES "SHKPROCESSES" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKASSIGNMENTEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKASSIGNMENTEVENTAUDITS" ADD CONSTRAINT "SHKASSIGNMENTEVENTAUDIT16" FOREIGN KEY ("THETYPE")
	  REFERENCES "SHKEVENTTYPES" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKASSIGNMENTSTABLE
--------------------------------------------------------

  ALTER TABLE "SHKASSIGNMENTSTABLE" ADD CONSTRAINT "SHKASSIGNMENTSTABLE_ACTI3" FOREIGN KEY ("ACTIVITY")
	  REFERENCES "SHKACTIVITIES" ("OID") ENABLE;
 
  ALTER TABLE "SHKASSIGNMENTSTABLE" ADD CONSTRAINT "SHKASSIGNMENTSTABLE_THER4" FOREIGN KEY ("THERESOURCE")
	  REFERENCES "SHKRESOURCESTABLE" ("OID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKCREATEPROCESSEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKCREATEPROCESSEVENTAUDITS" ADD CONSTRAINT "SHKCREATEPROCESSEVENTAUD7" FOREIGN KEY ("THETYPE")
	  REFERENCES "SHKEVENTTYPES" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKDATAEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKDATAEVENTAUDITS" ADD CONSTRAINT "SHKDATAEVENTAUDITS_THET14" FOREIGN KEY ("THETYPE")
	  REFERENCES "SHKEVENTTYPES" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKDEADLINES
--------------------------------------------------------

  ALTER TABLE "SHKDEADLINES" ADD CONSTRAINT "SHKDEADLINES_ACTIVITY" FOREIGN KEY ("ACTIVITY")
	  REFERENCES "SHKACTIVITIES" ("OID") ENABLE;
 
  ALTER TABLE "SHKDEADLINES" ADD CONSTRAINT "SHKDEADLINES_PROCESS" FOREIGN KEY ("PROCESS")
	  REFERENCES "SHKPROCESSES" ("OID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKGROUPGROUPTABLE
--------------------------------------------------------

  ALTER TABLE "SHKGROUPGROUPTABLE" ADD CONSTRAINT "SHKGROUPGROUPTABLE_GROUP1" FOREIGN KEY ("GROUPID")
	  REFERENCES "SHKGROUPTABLE" ("OID") ENABLE;
 
  ALTER TABLE "SHKGROUPGROUPTABLE" ADD CONSTRAINT "SHKGROUPGROUPTABLE_SUB_G0" FOREIGN KEY ("SUB_GID")
	  REFERENCES "SHKGROUPTABLE" ("OID") ENABLE;


--------------------------------------------------------
--  Ref Constraints for Table SHKGROUPUSERPACKLEVELPART
--------------------------------------------------------

  ALTER TABLE "SHKGROUPUSERPACKLEVELPART" ADD CONSTRAINT "SHKGROUPUSERPACKLEVELPA18" FOREIGN KEY ("PARTICIPANTOID")
	  REFERENCES "SHKPACKLEVELPARTICIPANT" ("OID") ENABLE;
 
  ALTER TABLE "SHKGROUPUSERPACKLEVELPART" ADD CONSTRAINT "SHKGROUPUSERPACKLEVELPA19" FOREIGN KEY ("USEROID")
	  REFERENCES "SHKGROUPUSER" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKGROUPUSERPROCLEVELPART
--------------------------------------------------------

  ALTER TABLE "SHKGROUPUSERPROCLEVELPART" ADD CONSTRAINT "SHKGROUPUSERPROCLEVELPA22" FOREIGN KEY ("PARTICIPANTOID")
	  REFERENCES "SHKPROCLEVELPARTICIPANT" ("OID") ENABLE;
 
  ALTER TABLE "SHKGROUPUSERPROCLEVELPART" ADD CONSTRAINT "SHKGROUPUSERPROCLEVELPA23" FOREIGN KEY ("USEROID")
	  REFERENCES "SHKGROUPUSER" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKNEWEVENTAUDITDATA
--------------------------------------------------------

  ALTER TABLE "SHKNEWEVENTAUDITDATA" ADD CONSTRAINT "SHKNEWEVENTAUDITDATA_DA26" FOREIGN KEY ("DATAEVENTAUDIT")
	  REFERENCES "SHKDATAEVENTAUDITS" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKNEWEVENTAUDITDATABLOBS
--------------------------------------------------------

  ALTER TABLE "SHKNEWEVENTAUDITDATABLOBS" ADD CONSTRAINT "SHKNEWEVENTAUDITDATABLO32" FOREIGN KEY ("NEWEVENTAUDITDATAWOB")
	  REFERENCES "SHKNEWEVENTAUDITDATAWOB" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKNEWEVENTAUDITDATAWOB
--------------------------------------------------------

  ALTER TABLE "SHKNEWEVENTAUDITDATAWOB" ADD CONSTRAINT "SHKNEWEVENTAUDITDATAWOB29" FOREIGN KEY ("DATAEVENTAUDIT")
	  REFERENCES "SHKDATAEVENTAUDITS" ("OID") ENABLE;


--------------------------------------------------------
--  Ref Constraints for Table SHKOLDEVENTAUDITDATA
--------------------------------------------------------

  ALTER TABLE "SHKOLDEVENTAUDITDATA" ADD CONSTRAINT "SHKOLDEVENTAUDITDATA_DA18" FOREIGN KEY ("DATAEVENTAUDIT")
	  REFERENCES "SHKDATAEVENTAUDITS" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKOLDEVENTAUDITDATABLOBS
--------------------------------------------------------

  ALTER TABLE "SHKOLDEVENTAUDITDATABLOBS" ADD CONSTRAINT "SHKOLDEVENTAUDITDATABLO24" FOREIGN KEY ("OLDEVENTAUDITDATAWOB")
	  REFERENCES "SHKOLDEVENTAUDITDATAWOB" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKOLDEVENTAUDITDATAWOB
--------------------------------------------------------

  ALTER TABLE "SHKOLDEVENTAUDITDATAWOB" ADD CONSTRAINT "SHKOLDEVENTAUDITDATAWOB21" FOREIGN KEY ("DATAEVENTAUDIT")
	  REFERENCES "SHKDATAEVENTAUDITS" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPACKLEVELPARTICIPANT
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELPARTICIPANT" ADD CONSTRAINT "SHKPACKLEVELPARTICIPANT_6" FOREIGN KEY ("PACKAGEOID")
	  REFERENCES "SHKXPDLPARTICIPANTPACKAGE" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPACKLEVELXPDLAPP
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPP" ADD CONSTRAINT "SHKPACKLEVELXPDLAPP_PACK5" FOREIGN KEY ("PACKAGEOID")
	  REFERENCES "SHKXPDLAPPLICATIONPACKAGE" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPACKLEVELXPDLAPPTAAPPDETAIL
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETAIL" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP19" FOREIGN KEY ("XPDL_APPOID")
	  REFERENCES "SHKPACKLEVELXPDLAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETAIL" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP20" FOREIGN KEY ("TOOLAGENTOID")
	  REFERENCES "SHKTOOLAGENTAPPDETAIL" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPACKLEVELXPDLAPPTAAPPDETUSR
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETUSR" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP27" FOREIGN KEY ("XPDL_APPOID")
	  REFERENCES "SHKPACKLEVELXPDLAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPDETUSR" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP28" FOREIGN KEY ("TOOLAGENTOID")
	  REFERENCES "SHKTOOLAGENTAPPDETAILUSER" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPACKLEVELXPDLAPPTAAPPUSER
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPUSER" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP23" FOREIGN KEY ("XPDL_APPOID")
	  REFERENCES "SHKPACKLEVELXPDLAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTAAPPUSER" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTAAP24" FOREIGN KEY ("TOOLAGENTOID")
	  REFERENCES "SHKTOOLAGENTAPPUSER" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPACKLEVELXPDLAPPTOOLAGNTAPP
--------------------------------------------------------

  ALTER TABLE "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTOOL15" FOREIGN KEY ("XPDL_APPOID")
	  REFERENCES "SHKPACKLEVELXPDLAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKPACKLEVELXPDLAPPTOOLAGNTAPP" ADD CONSTRAINT "SHKPACKLEVELXPDLAPPTOOL16" FOREIGN KEY ("TOOLAGENTOID")
	  REFERENCES "SHKTOOLAGENTAPP" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCESSDATA
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSDATA" ADD CONSTRAINT "SHKPROCESSDATA_PROCESS" FOREIGN KEY ("PROCESS")
	  REFERENCES "SHKPROCESSES" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCESSDATABLOBS
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSDATABLOBS" ADD CONSTRAINT "SHKPROCESSDATABLOBS_PROC5" FOREIGN KEY ("PROCESSDATAWOB")
	  REFERENCES "SHKPROCESSDATAWOB" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCESSDATAWOB
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSDATAWOB" ADD CONSTRAINT "SHKPROCESSDATAWOB_PROCESS" FOREIGN KEY ("PROCESS")
	  REFERENCES "SHKPROCESSES" ("OID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKPROCESSES
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSES" ADD CONSTRAINT "SHKPROCESSES_PROCESSDEFI0" FOREIGN KEY ("PROCESSDEFINITION")
	  REFERENCES "SHKPROCESSDEFINITIONS" ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSES" ADD CONSTRAINT "SHKPROCESSES_STATE" FOREIGN KEY ("STATE")
	  REFERENCES "SHKPROCESSSTATES" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCESSREQUESTERS
--------------------------------------------------------

  ALTER TABLE "SHKPROCESSREQUESTERS" ADD CONSTRAINT "SHKPROCESSREQUESTERS_ACT1" FOREIGN KEY ("ACTIVITYREQUESTER")
	  REFERENCES "SHKACTIVITIES" ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCESSREQUESTERS" ADD CONSTRAINT "SHKPROCESSREQUESTERS_RES2" FOREIGN KEY ("RESOURCEREQUESTER")
	  REFERENCES "SHKRESOURCESTABLE" ("OID") ENABLE;


--------------------------------------------------------
--  Ref Constraints for Table SHKPROCLEVELPARTICIPANT
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELPARTICIPANT" ADD CONSTRAINT "SHKPROCLEVELPARTICIPANT_3" FOREIGN KEY ("PROCESSOID")
	  REFERENCES "SHKXPDLPARTICIPANTPROCESS" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCLEVELXPDLAPP
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPP" ADD CONSTRAINT "SHKPROCLEVELXPDLAPP_PROC6" FOREIGN KEY ("PROCESSOID")
	  REFERENCES "SHKXPDLAPPLICATIONPROCESS" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCLEVELXPDLAPPTAAPPDETAIL
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETAIL" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP35" FOREIGN KEY ("XPDL_APPOID")
	  REFERENCES "SHKPROCLEVELXPDLAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETAIL" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP36" FOREIGN KEY ("TOOLAGENTOID")
	  REFERENCES "SHKTOOLAGENTAPPDETAIL" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCLEVELXPDLAPPTAAPPDETUSR
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETUSR" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP43" FOREIGN KEY ("XPDL_APPOID")
	  REFERENCES "SHKPROCLEVELXPDLAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPDETUSR" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP44" FOREIGN KEY ("TOOLAGENTOID")
	  REFERENCES "SHKTOOLAGENTAPPDETAILUSER" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCLEVELXPDLAPPTAAPPUSER
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPUSER" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP39" FOREIGN KEY ("XPDL_APPOID")
	  REFERENCES "SHKPROCLEVELXPDLAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTAAPPUSER" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTAAP40" FOREIGN KEY ("TOOLAGENTOID")
	  REFERENCES "SHKTOOLAGENTAPPUSER" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKPROCLEVELXPDLAPPTOOLAGNTAPP
--------------------------------------------------------

  ALTER TABLE "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTOOL31" FOREIGN KEY ("XPDL_APPOID")
	  REFERENCES "SHKPROCLEVELXPDLAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKPROCLEVELXPDLAPPTOOLAGNTAPP" ADD CONSTRAINT "SHKPROCLEVELXPDLAPPTOOL32" FOREIGN KEY ("TOOLAGENTOID")
	  REFERENCES "SHKTOOLAGENTAPP" ("OID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKSTATEEVENTAUDITS
--------------------------------------------------------

  ALTER TABLE "SHKSTATEEVENTAUDITS" ADD CONSTRAINT "SHKSTATEEVENTAUDITS_NEW11" FOREIGN KEY ("NEWPROCESSSTATE")
	  REFERENCES "SHKPROCESSSTATEEVENTAUDITS" ("OID") ENABLE;
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" ADD CONSTRAINT "SHKSTATEEVENTAUDITS_NEW13" FOREIGN KEY ("NEWACTIVITYSTATE")
	  REFERENCES "SHKACTIVITYSTATEEVENTAUDITS" ("OID") ENABLE;
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" ADD CONSTRAINT "SHKSTATEEVENTAUDITS_OLD10" FOREIGN KEY ("OLDPROCESSSTATE")
	  REFERENCES "SHKPROCESSSTATEEVENTAUDITS" ("OID") ENABLE;
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" ADD CONSTRAINT "SHKSTATEEVENTAUDITS_OLD12" FOREIGN KEY ("OLDACTIVITYSTATE")
	  REFERENCES "SHKACTIVITYSTATEEVENTAUDITS" ("OID") ENABLE;
 
  ALTER TABLE "SHKSTATEEVENTAUDITS" ADD CONSTRAINT "SHKSTATEEVENTAUDITS_THET9" FOREIGN KEY ("THETYPE")
	  REFERENCES "SHKEVENTTYPES" ("OID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKTOOLAGENTAPPDETAIL
--------------------------------------------------------

  ALTER TABLE "SHKTOOLAGENTAPPDETAIL" ADD CONSTRAINT "SHKTOOLAGENTAPPDETAIL_TO7" FOREIGN KEY ("TOOLAGENT_APPOID")
	  REFERENCES "SHKTOOLAGENTAPP" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKTOOLAGENTAPPDETAILUSER
--------------------------------------------------------

  ALTER TABLE "SHKTOOLAGENTAPPDETAILUSER" ADD CONSTRAINT "SHKTOOLAGENTAPPDETAILUS11" FOREIGN KEY ("TOOLAGENT_APPOID")
	  REFERENCES "SHKTOOLAGENTAPPDETAIL" ("OID") ENABLE;
 
  ALTER TABLE "SHKTOOLAGENTAPPDETAILUSER" ADD CONSTRAINT "SHKTOOLAGENTAPPDETAILUS12" FOREIGN KEY ("USEROID")
	  REFERENCES "SHKTOOLAGENTUSER" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKTOOLAGENTAPPUSER
--------------------------------------------------------

  ALTER TABLE "SHKTOOLAGENTAPPUSER" ADD CONSTRAINT "SHKTOOLAGENTAPPUSER_TOOL8" FOREIGN KEY ("TOOLAGENT_APPOID")
	  REFERENCES "SHKTOOLAGENTAPP" ("OID") ENABLE;
 
  ALTER TABLE "SHKTOOLAGENTAPPUSER" ADD CONSTRAINT "SHKTOOLAGENTAPPUSER_USER9" FOREIGN KEY ("USEROID")
	  REFERENCES "SHKTOOLAGENTUSER" ("OID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKUSERGROUPTABLE
--------------------------------------------------------

  ALTER TABLE "SHKUSERGROUPTABLE" ADD CONSTRAINT "SHKUSERGROUPTABLE_GROUPID" FOREIGN KEY ("GROUPID")
	  REFERENCES "SHKGROUPTABLE" ("OID") ENABLE;
 
  ALTER TABLE "SHKUSERGROUPTABLE" ADD CONSTRAINT "SHKUSERGROUPTABLE_USERID" FOREIGN KEY ("USERID")
	  REFERENCES "SHKUSERTABLE" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKUSERPACKLEVELPART
--------------------------------------------------------

  ALTER TABLE "SHKUSERPACKLEVELPART" ADD CONSTRAINT "SHKUSERPACKLEVELPART_PA15" FOREIGN KEY ("PARTICIPANTOID")
	  REFERENCES "SHKPACKLEVELPARTICIPANT" ("OID") ENABLE;
 
  ALTER TABLE "SHKUSERPACKLEVELPART" ADD CONSTRAINT "SHKUSERPACKLEVELPART_US16" FOREIGN KEY ("USEROID")
	  REFERENCES "SHKNORMALUSER" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKUSERPROCLEVELPARTICIPANT
--------------------------------------------------------

  ALTER TABLE "SHKUSERPROCLEVELPARTICIPANT" ADD CONSTRAINT "SHKUSERPROCLEVELPARTICI12" FOREIGN KEY ("PARTICIPANTOID")
	  REFERENCES "SHKPROCLEVELPARTICIPANT" ("OID") ENABLE;
 
  ALTER TABLE "SHKUSERPROCLEVELPARTICIPANT" ADD CONSTRAINT "SHKUSERPROCLEVELPARTICI13" FOREIGN KEY ("USEROID")
	  REFERENCES "SHKNORMALUSER" ("OID") ENABLE;


--------------------------------------------------------
--  Ref Constraints for Table SHKXPDLAPPLICATIONPROCESS
--------------------------------------------------------

  ALTER TABLE "SHKXPDLAPPLICATIONPROCESS" ADD CONSTRAINT "SHKXPDLAPPLICATIONPROCES3" FOREIGN KEY ("PACKAGEOID")
	  REFERENCES "SHKXPDLAPPLICATIONPACKAGE" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKXPDLDATA
--------------------------------------------------------

  ALTER TABLE "SHKXPDLDATA" ADD CONSTRAINT "SHKXPDLDATA_XPDL" FOREIGN KEY ("XPDL")
	  REFERENCES "SHKXPDLS" ("OID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKXPDLHISTORYDATA
--------------------------------------------------------

  ALTER TABLE "SHKXPDLHISTORYDATA" ADD CONSTRAINT "SHKXPDLHISTORYDATA_XPDLH0" FOREIGN KEY ("XPDLHISTORY")
	  REFERENCES "SHKXPDLHISTORY" ("OID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table SHKXPDLPARTICIPANTPROCESS
--------------------------------------------------------

  ALTER TABLE "SHKXPDLPARTICIPANTPROCESS" ADD CONSTRAINT "SHKXPDLPARTICIPANTPROCES9" FOREIGN KEY ("PACKAGEOID")
	  REFERENCES "SHKXPDLPARTICIPANTPACKAGE" ("OID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table SHKXPDLREFERENCES
--------------------------------------------------------

  ALTER TABLE "SHKXPDLREFERENCES" ADD CONSTRAINT "SHKXPDLREFERENCES_REFERR1" FOREIGN KEY ("REFERRINGXPDL")
	  REFERENCES "SHKXPDLS" ("OID") ENABLE;






