-- MySQL dump 10.10
--
-- Host: localhost    Database: sharkdb
-- ------------------------------------------------------
-- Server version	5.0.27-community-nt-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `objectid`
--

DROP TABLE IF EXISTS `objectid`;
CREATE TABLE `objectid` (
  `nextoid` decimal(19,0) NOT NULL,
  PRIMARY KEY  (`nextoid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `objectid`
--

LOCK TABLES `objectid` WRITE;
/*!40000 ALTER TABLE `objectid` DISABLE KEYS */;
/*!40000 ALTER TABLE `objectid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkactivities`
--

DROP TABLE IF EXISTS `shkactivities`;
CREATE TABLE `shkactivities` (
  `Id` varchar(100) NOT NULL,
  `ActivitySetDefinitionId` varchar(90) default NULL,
  `ActivityDefinitionId` varchar(90) NOT NULL,
  `Process` decimal(19,0) NOT NULL,
  `TheResource` decimal(19,0) default NULL,
  `PDefName` varchar(200) NOT NULL,
  `ProcessId` varchar(200) NOT NULL,
  `ResourceId` varchar(100) default NULL,
  `State` decimal(19,0) NOT NULL,
  `BlockActivityId` varchar(100) default NULL,
  `Performer` varchar(100) default NULL,
  `IsPerformerAsynchronous` smallint(6) default NULL,
  `Priority` int(11) default NULL,
  `Name` varchar(254) default NULL,
  `Activated` bigint(20) NOT NULL,
  `ActivatedTZO` bigint(20) NOT NULL,
  `Accepted` bigint(20) default NULL,
  `AcceptedTZO` bigint(20) default NULL,
  `LastStateTime` bigint(20) NOT NULL,
  `LastStateTimeTZO` bigint(20) NOT NULL,
  `LimitTime` bigint(20) NOT NULL,
  `LimitTimeTZO` bigint(20) NOT NULL,
  `Description` varchar(254) default NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKActivities` (`Id`),
  KEY `SHKActivities_TheResource` (`TheResource`),
  KEY `SHKActivities_State` (`State`),
  KEY `I2_SHKActivities` (`Process`,`ActivitySetDefinitionId`,`ActivityDefinitionId`),
  KEY `I3_SHKActivities` (`Process`,`State`),
  CONSTRAINT `SHKActivities_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`),
  CONSTRAINT `SHKActivities_State` FOREIGN KEY (`State`) REFERENCES `shkactivitystates` (`oid`),
  CONSTRAINT `SHKActivities_TheResource` FOREIGN KEY (`TheResource`) REFERENCES `shkresourcestable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkactivities`
--

LOCK TABLES `shkactivities` WRITE;
/*!40000 ALTER TABLE `shkactivities` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkactivities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkactivitydata`
--

DROP TABLE IF EXISTS `shkactivitydata`;
CREATE TABLE `shkactivitydata` (
  `Activity` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) default NULL,
  `VariableValueDBL` double default NULL,
  `VariableValueLONG` bigint(20) default NULL,
  `VariableValueDATE` datetime default NULL,
  `VariableValueBOOL` smallint(6) default NULL,
  `IsResult` smallint(6) NOT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKActivityData` (`CNT`),
  UNIQUE KEY `I2_SHKActivityData` (`Activity`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKActivityData_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkactivitydata`
--

LOCK TABLES `shkactivitydata` WRITE;
/*!40000 ALTER TABLE `shkactivitydata` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkactivitydata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkactivitydatablobs`
--

DROP TABLE IF EXISTS `shkactivitydatablobs`;
CREATE TABLE `shkactivitydatablobs` (
  `ActivityDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKActivityDataBLOBs` (`ActivityDataWOB`,`OrdNo`),
  CONSTRAINT `SHKActivityDataBLOBs_ActivityDataWOB` FOREIGN KEY (`ActivityDataWOB`) REFERENCES `shkactivitydatawob` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkactivitydatablobs`
--

LOCK TABLES `shkactivitydatablobs` WRITE;
/*!40000 ALTER TABLE `shkactivitydatablobs` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkactivitydatablobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkactivitydatawob`
--

DROP TABLE IF EXISTS `shkactivitydatawob`;
CREATE TABLE `shkactivitydatawob` (
  `Activity` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) default NULL,
  `VariableValueDBL` double default NULL,
  `VariableValueLONG` bigint(20) default NULL,
  `VariableValueDATE` datetime default NULL,
  `VariableValueBOOL` smallint(6) default NULL,
  `IsResult` smallint(6) NOT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKActivityDataWOB` (`CNT`),
  UNIQUE KEY `I2_SHKActivityDataWOB` (`Activity`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKActivityDataWOB_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkactivitydatawob`
--

LOCK TABLES `shkactivitydatawob` WRITE;
/*!40000 ALTER TABLE `shkactivitydatawob` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkactivitydatawob` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkactivitystateeventaudits`
--

DROP TABLE IF EXISTS `shkactivitystateeventaudits`;
CREATE TABLE `shkactivitystateeventaudits` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKActivityStateEventAudits` (`KeyValue`),
  UNIQUE KEY `I2_SHKActivityStateEventAudits` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkactivitystateeventaudits`
--

LOCK TABLES `shkactivitystateeventaudits` WRITE;
/*!40000 ALTER TABLE `shkactivitystateeventaudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkactivitystateeventaudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkactivitystates`
--

DROP TABLE IF EXISTS `shkactivitystates`;
CREATE TABLE `shkactivitystates` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKActivityStates` (`KeyValue`),
  UNIQUE KEY `I2_SHKActivityStates` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkactivitystates`
--

LOCK TABLES `shkactivitystates` WRITE;
/*!40000 ALTER TABLE `shkactivitystates` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkactivitystates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkandjointable`
--

DROP TABLE IF EXISTS `shkandjointable`;
CREATE TABLE `shkandjointable` (
  `Process` decimal(19,0) NOT NULL,
  `BlockActivity` decimal(19,0) default NULL,
  `ActivityDefinitionId` varchar(90) NOT NULL,
  `Activity` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKAndJoinTable` (`CNT`),
  KEY `SHKAndJoinTable_BlockActivity` (`BlockActivity`),
  KEY `I2_SHKAndJoinTable` (`Process`,`BlockActivity`,`ActivityDefinitionId`),
  KEY `I3_SHKAndJoinTable` (`Activity`),
  CONSTRAINT `SHKAndJoinTable_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKAndJoinTable_BlockActivity` FOREIGN KEY (`BlockActivity`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKAndJoinTable_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkandjointable`
--

LOCK TABLES `shkandjointable` WRITE;
/*!40000 ALTER TABLE `shkandjointable` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkandjointable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkassignmenteventaudits`
--

DROP TABLE IF EXISTS `shkassignmenteventaudits`;
CREATE TABLE `shkassignmenteventaudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) NOT NULL,
  `ActivityName` varchar(254) default NULL,
  `ProcessId` varchar(100) NOT NULL,
  `ProcessName` varchar(254) default NULL,
  `ProcessFactoryName` varchar(200) NOT NULL,
  `ProcessFactoryVersion` varchar(20) NOT NULL,
  `ActivityDefinitionId` varchar(90) NOT NULL,
  `ActivityDefinitionName` varchar(90) default NULL,
  `ActivityDefinitionType` int(11) NOT NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionName` varchar(90) default NULL,
  `PackageId` varchar(90) NOT NULL,
  `OldResourceUsername` varchar(100) default NULL,
  `OldResourceName` varchar(100) default NULL,
  `NewResourceUsername` varchar(100) NOT NULL,
  `NewResourceName` varchar(100) default NULL,
  `IsAccepted` smallint(6) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKAssignmentEventAudits` (`CNT`),
  KEY `SHKAssignmentEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKAssignmentEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `shkeventtypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkassignmenteventaudits`
--

LOCK TABLES `shkassignmenteventaudits` WRITE;
/*!40000 ALTER TABLE `shkassignmenteventaudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkassignmenteventaudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkassignmentstable`
--

DROP TABLE IF EXISTS `shkassignmentstable`;
CREATE TABLE `shkassignmentstable` (
  `Activity` decimal(19,0) NOT NULL,
  `TheResource` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) NOT NULL,
  `ActivityProcessId` varchar(100) NOT NULL,
  `ActivityProcessDefName` varchar(200) NOT NULL,
  `ResourceId` varchar(100) NOT NULL,
  `IsAccepted` smallint(6) NOT NULL,
  `IsValid` smallint(6) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKAssignmentsTable` (`CNT`),
  UNIQUE KEY `I2_SHKAssignmentsTable` (`Activity`,`TheResource`),
  KEY `I3_SHKAssignmentsTable` (`TheResource`,`IsValid`),
  KEY `I4_SHKAssignmentsTable` (`ActivityId`),
  KEY `I5_SHKAssignmentsTable` (`ResourceId`),
  CONSTRAINT `SHKAssignmentsTable_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKAssignmentsTable_TheResource` FOREIGN KEY (`TheResource`) REFERENCES `shkresourcestable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkassignmentstable`
--

LOCK TABLES `shkassignmentstable` WRITE;
/*!40000 ALTER TABLE `shkassignmentstable` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkassignmentstable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkcounters`
--

DROP TABLE IF EXISTS `shkcounters`;
CREATE TABLE `shkcounters` (
  `name` varchar(100) NOT NULL,
  `the_number` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKCounters` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkcounters`
--

LOCK TABLES `shkcounters` WRITE;
/*!40000 ALTER TABLE `shkcounters` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkcounters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkcreateprocesseventaudits`
--

DROP TABLE IF EXISTS `shkcreateprocesseventaudits`;
CREATE TABLE `shkcreateprocesseventaudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ProcessId` varchar(100) NOT NULL,
  `ProcessName` varchar(254) default NULL,
  `ProcessFactoryName` varchar(200) NOT NULL,
  `ProcessFactoryVersion` varchar(20) NOT NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionName` varchar(90) default NULL,
  `PackageId` varchar(90) NOT NULL,
  `PActivityId` varchar(100) default NULL,
  `PProcessId` varchar(100) default NULL,
  `PProcessName` varchar(254) default NULL,
  `PProcessFactoryName` varchar(200) default NULL,
  `PProcessFactoryVersion` varchar(20) default NULL,
  `PActivityDefinitionId` varchar(90) default NULL,
  `PActivityDefinitionName` varchar(90) default NULL,
  `PProcessDefinitionId` varchar(90) default NULL,
  `PProcessDefinitionName` varchar(90) default NULL,
  `PPackageId` varchar(90) default NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKCreateProcessEventAudits` (`CNT`),
  KEY `SHKCreateProcessEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKCreateProcessEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `shkeventtypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkcreateprocesseventaudits`
--

LOCK TABLES `shkcreateprocesseventaudits` WRITE;
/*!40000 ALTER TABLE `shkcreateprocesseventaudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkcreateprocesseventaudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkdataeventaudits`
--

DROP TABLE IF EXISTS `shkdataeventaudits`;
CREATE TABLE `shkdataeventaudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) default NULL,
  `ActivityName` varchar(254) default NULL,
  `ProcessId` varchar(100) NOT NULL,
  `ProcessName` varchar(254) default NULL,
  `ProcessFactoryName` varchar(200) NOT NULL,
  `ProcessFactoryVersion` varchar(20) NOT NULL,
  `ActivityDefinitionId` varchar(90) default NULL,
  `ActivityDefinitionName` varchar(90) default NULL,
  `ActivityDefinitionType` int(11) default NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionName` varchar(90) default NULL,
  `PackageId` varchar(90) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKDataEventAudits` (`CNT`),
  KEY `SHKDataEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKDataEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `shkeventtypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkdataeventaudits`
--

LOCK TABLES `shkdataeventaudits` WRITE;
/*!40000 ALTER TABLE `shkdataeventaudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkdataeventaudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkdeadlines`
--

DROP TABLE IF EXISTS `shkdeadlines`;
CREATE TABLE `shkdeadlines` (
  `Process` decimal(19,0) NOT NULL,
  `Activity` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `TimeLimit` bigint(20) NOT NULL,
  `TimeLimitTZO` bigint(20) NOT NULL,
  `ExceptionName` varchar(100) NOT NULL,
  `IsSynchronous` smallint(6) NOT NULL,
  `IsExecuted` smallint(6) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKDeadlines` (`CNT`),
  KEY `I2_SHKDeadlines` (`Process`,`TimeLimit`),
  KEY `I3_SHKDeadlines` (`Activity`,`TimeLimit`),
  CONSTRAINT `SHKDeadlines_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKDeadlines_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkdeadlines`
--

LOCK TABLES `shkdeadlines` WRITE;
/*!40000 ALTER TABLE `shkdeadlines` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkdeadlines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkeventtypes`
--

DROP TABLE IF EXISTS `shkeventtypes`;
CREATE TABLE `shkeventtypes` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKEventTypes` (`KeyValue`),
  UNIQUE KEY `I2_SHKEventTypes` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkeventtypes`
--

LOCK TABLES `shkeventtypes` WRITE;
/*!40000 ALTER TABLE `shkeventtypes` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkeventtypes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkgroupgrouptable`
--

DROP TABLE IF EXISTS `shkgroupgrouptable`;
CREATE TABLE `shkgroupgrouptable` (
  `sub_gid` decimal(19,0) NOT NULL,
  `groupid` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKGroupGroupTable` (`sub_gid`,`groupid`),
  KEY `I2_SHKGroupGroupTable` (`groupid`),
  CONSTRAINT `SHKGroupGroupTable_groupid` FOREIGN KEY (`groupid`) REFERENCES `shkgrouptable` (`oid`),
  CONSTRAINT `SHKGroupGroupTable_sub_gid` FOREIGN KEY (`sub_gid`) REFERENCES `shkgrouptable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkgroupgrouptable`
--

LOCK TABLES `shkgroupgrouptable` WRITE;
/*!40000 ALTER TABLE `shkgroupgrouptable` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkgroupgrouptable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkgrouptable`
--

DROP TABLE IF EXISTS `shkgrouptable`;
CREATE TABLE `shkgrouptable` (
  `groupid` varchar(100) NOT NULL,
  `description` varchar(254) default NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKGroupTable` (`groupid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkgrouptable`
--

LOCK TABLES `shkgrouptable` WRITE;
/*!40000 ALTER TABLE `shkgrouptable` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkgrouptable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkgroupuser`
--

DROP TABLE IF EXISTS `shkgroupuser`;
CREATE TABLE `shkgroupuser` (
  `USERNAME` varchar(100) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKGroupUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkgroupuser`
--

LOCK TABLES `shkgroupuser` WRITE;
/*!40000 ALTER TABLE `shkgroupuser` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkgroupuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkgroupuserpacklevelpart`
--

DROP TABLE IF EXISTS `shkgroupuserpacklevelpart`;
CREATE TABLE `shkgroupuserpacklevelpart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKGroupUserPackLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKGroupUserPackLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKGroupUserPackLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `shkpacklevelparticipant` (`oid`),
  CONSTRAINT `SHKGroupUserPackLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shkgroupuser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkgroupuserpacklevelpart`
--

LOCK TABLES `shkgroupuserpacklevelpart` WRITE;
/*!40000 ALTER TABLE `shkgroupuserpacklevelpart` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkgroupuserpacklevelpart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkgroupuserproclevelpart`
--

DROP TABLE IF EXISTS `shkgroupuserproclevelpart`;
CREATE TABLE `shkgroupuserproclevelpart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKGroupUserProcLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKGroupUserProcLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKGroupUserProcLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `shkproclevelparticipant` (`oid`),
  CONSTRAINT `SHKGroupUserProcLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shkgroupuser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkgroupuserproclevelpart`
--

LOCK TABLES `shkgroupuserproclevelpart` WRITE;
/*!40000 ALTER TABLE `shkgroupuserproclevelpart` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkgroupuserproclevelpart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkneweventauditdata`
--

DROP TABLE IF EXISTS `shkneweventauditdata`;
CREATE TABLE `shkneweventauditdata` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) default NULL,
  `VariableValueDBL` float default NULL,
  `VariableValueLONG` bigint(20) default NULL,
  `VariableValueDATE` datetime default NULL,
  `VariableValueBOOL` smallint(6) default NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKNewEventAuditData` (`CNT`),
  UNIQUE KEY `I2_SHKNewEventAuditData` (`DataEventAudit`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKNewEventAuditData_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `shkdataeventaudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkneweventauditdata`
--

LOCK TABLES `shkneweventauditdata` WRITE;
/*!40000 ALTER TABLE `shkneweventauditdata` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkneweventauditdata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkneweventauditdatablobs`
--

DROP TABLE IF EXISTS `shkneweventauditdatablobs`;
CREATE TABLE `shkneweventauditdatablobs` (
  `NewEventAuditDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKNewEventAuditDataBLOBs` (`NewEventAuditDataWOB`,`OrdNo`),
  CONSTRAINT `SHKNewEventAuditDataBLOBs_NewEventAuditDataWOB` FOREIGN KEY (`NewEventAuditDataWOB`) REFERENCES `shkneweventauditdatawob` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkneweventauditdatablobs`
--

LOCK TABLES `shkneweventauditdatablobs` WRITE;
/*!40000 ALTER TABLE `shkneweventauditdatablobs` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkneweventauditdatablobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkneweventauditdatawob`
--

DROP TABLE IF EXISTS `shkneweventauditdatawob`;
CREATE TABLE `shkneweventauditdatawob` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) default NULL,
  `VariableValueDBL` float default NULL,
  `VariableValueLONG` bigint(20) default NULL,
  `VariableValueDATE` datetime default NULL,
  `VariableValueBOOL` smallint(6) default NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKNewEventAuditDataWOB` (`CNT`),
  UNIQUE KEY `I2_SHKNewEventAuditDataWOB` (`DataEventAudit`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKNewEventAuditDataWOB_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `shkdataeventaudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkneweventauditdatawob`
--

LOCK TABLES `shkneweventauditdatawob` WRITE;
/*!40000 ALTER TABLE `shkneweventauditdatawob` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkneweventauditdatawob` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shknextxpdlversions`
--

DROP TABLE IF EXISTS `shknextxpdlversions`;
CREATE TABLE `shknextxpdlversions` (
  `XPDLId` varchar(90) NOT NULL,
  `NextVersion` varchar(20) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKNextXPDLVersions` (`XPDLId`,`NextVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shknextxpdlversions`
--

LOCK TABLES `shknextxpdlversions` WRITE;
/*!40000 ALTER TABLE `shknextxpdlversions` DISABLE KEYS */;
/*!40000 ALTER TABLE `shknextxpdlversions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shknormaluser`
--

DROP TABLE IF EXISTS `shknormaluser`;
CREATE TABLE `shknormaluser` (
  `USERNAME` varchar(100) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKNormalUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shknormaluser`
--

LOCK TABLES `shknormaluser` WRITE;
/*!40000 ALTER TABLE `shknormaluser` DISABLE KEYS */;
/*!40000 ALTER TABLE `shknormaluser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkoldeventauditdata`
--

DROP TABLE IF EXISTS `shkoldeventauditdata`;
CREATE TABLE `shkoldeventauditdata` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) default NULL,
  `VariableValueDBL` float default NULL,
  `VariableValueLONG` bigint(20) default NULL,
  `VariableValueDATE` datetime default NULL,
  `VariableValueBOOL` smallint(6) default NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKOldEventAuditData` (`CNT`),
  UNIQUE KEY `I2_SHKOldEventAuditData` (`DataEventAudit`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKOldEventAuditData_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `shkdataeventaudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkoldeventauditdata`
--

LOCK TABLES `shkoldeventauditdata` WRITE;
/*!40000 ALTER TABLE `shkoldeventauditdata` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkoldeventauditdata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkoldeventauditdatablobs`
--

DROP TABLE IF EXISTS `shkoldeventauditdatablobs`;
CREATE TABLE `shkoldeventauditdatablobs` (
  `OldEventAuditDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKOldEventAuditDataBLOBs` (`OldEventAuditDataWOB`,`OrdNo`),
  CONSTRAINT `SHKOldEventAuditDataBLOBs_OldEventAuditDataWOB` FOREIGN KEY (`OldEventAuditDataWOB`) REFERENCES `shkoldeventauditdatawob` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkoldeventauditdatablobs`
--

LOCK TABLES `shkoldeventauditdatablobs` WRITE;
/*!40000 ALTER TABLE `shkoldeventauditdatablobs` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkoldeventauditdatablobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkoldeventauditdatawob`
--

DROP TABLE IF EXISTS `shkoldeventauditdatawob`;
CREATE TABLE `shkoldeventauditdatawob` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) default NULL,
  `VariableValueDBL` float default NULL,
  `VariableValueLONG` bigint(20) default NULL,
  `VariableValueDATE` datetime default NULL,
  `VariableValueBOOL` smallint(6) default NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKOldEventAuditDataWOB` (`CNT`),
  UNIQUE KEY `I2_SHKOldEventAuditDataWOB` (`DataEventAudit`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKOldEventAuditDataWOB_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `shkdataeventaudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkoldeventauditdatawob`
--

LOCK TABLES `shkoldeventauditdatawob` WRITE;
/*!40000 ALTER TABLE `shkoldeventauditdatawob` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkoldeventauditdatawob` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkpacklevelparticipant`
--

DROP TABLE IF EXISTS `shkpacklevelparticipant`;
CREATE TABLE `shkpacklevelparticipant` (
  `PARTICIPANT_ID` varchar(90) NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKPackLevelParticipant` (`PARTICIPANT_ID`,`PACKAGEOID`),
  KEY `SHKPackLevelParticipant_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKPackLevelParticipant_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `shkxpdlparticipantpackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkpacklevelparticipant`
--

LOCK TABLES `shkpacklevelparticipant` WRITE;
/*!40000 ALTER TABLE `shkpacklevelparticipant` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkpacklevelparticipant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkpacklevelxpdlapp`
--

DROP TABLE IF EXISTS `shkpacklevelxpdlapp`;
CREATE TABLE `shkpacklevelxpdlapp` (
  `APPLICATION_ID` varchar(90) NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLApp` (`APPLICATION_ID`,`PACKAGEOID`),
  KEY `SHKPackLevelXPDLApp_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKPackLevelXPDLApp_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `shkxpdlapplicationpackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkpacklevelxpdlapp`
--

LOCK TABLES `shkpacklevelxpdlapp` WRITE;
/*!40000 ALTER TABLE `shkpacklevelxpdlapp` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkpacklevelxpdlapp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkpacklevelxpdlapptaappdetail`
--

DROP TABLE IF EXISTS `shkpacklevelxpdlapptaappdetail`;
CREATE TABLE `shkpacklevelxpdlapptaappdetail` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppDetail` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppDetail_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetail_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappdetail` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetail_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkpacklevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkpacklevelxpdlapptaappdetail`
--

LOCK TABLES `shkpacklevelxpdlapptaappdetail` WRITE;
/*!40000 ALTER TABLE `shkpacklevelxpdlapptaappdetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkpacklevelxpdlapptaappdetail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkpacklevelxpdlapptaappdetusr`
--

DROP TABLE IF EXISTS `shkpacklevelxpdlapptaappdetusr`;
CREATE TABLE `shkpacklevelxpdlapptaappdetusr` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppDetUsr` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappdetailuser` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetUsr_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkpacklevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkpacklevelxpdlapptaappdetusr`
--

LOCK TABLES `shkpacklevelxpdlapptaappdetusr` WRITE;
/*!40000 ALTER TABLE `shkpacklevelxpdlapptaappdetusr` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkpacklevelxpdlapptaappdetusr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkpacklevelxpdlapptaappuser`
--

DROP TABLE IF EXISTS `shkpacklevelxpdlapptaappuser`;
CREATE TABLE `shkpacklevelxpdlapptaappuser` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppUser` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppUser_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppUser_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappuser` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppUser_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkpacklevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkpacklevelxpdlapptaappuser`
--

LOCK TABLES `shkpacklevelxpdlapptaappuser` WRITE;
/*!40000 ALTER TABLE `shkpacklevelxpdlapptaappuser` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkpacklevelxpdlapptaappuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkpacklevelxpdlapptoolagntapp`
--

DROP TABLE IF EXISTS `shkpacklevelxpdlapptoolagntapp`;
CREATE TABLE `shkpacklevelxpdlapptoolagntapp` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppToolAgntApp` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppToolAgntApp_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppToolAgntApp_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentapp` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppToolAgntApp_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkpacklevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkpacklevelxpdlapptoolagntapp`
--

LOCK TABLES `shkpacklevelxpdlapptoolagntapp` WRITE;
/*!40000 ALTER TABLE `shkpacklevelxpdlapptoolagntapp` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkpacklevelxpdlapptoolagntapp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocessdata`
--

DROP TABLE IF EXISTS `shkprocessdata`;
CREATE TABLE `shkprocessdata` (
  `Process` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) default NULL,
  `VariableValueDBL` double default NULL,
  `VariableValueLONG` bigint(20) default NULL,
  `VariableValueDATE` datetime default NULL,
  `VariableValueBOOL` smallint(6) default NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcessData` (`CNT`),
  UNIQUE KEY `I2_SHKProcessData` (`Process`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKProcessData_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkprocessdata`
--

LOCK TABLES `shkprocessdata` WRITE;
/*!40000 ALTER TABLE `shkprocessdata` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkprocessdata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocessdatablobs`
--

DROP TABLE IF EXISTS `shkprocessdatablobs`;
CREATE TABLE `shkprocessdatablobs` (
  `ProcessDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcessDataBLOBs` (`ProcessDataWOB`,`OrdNo`),
  CONSTRAINT `SHKProcessDataBLOBs_ProcessDataWOB` FOREIGN KEY (`ProcessDataWOB`) REFERENCES `shkprocessdatawob` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkprocessdatablobs`
--

LOCK TABLES `shkprocessdatablobs` WRITE;
/*!40000 ALTER TABLE `shkprocessdatablobs` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkprocessdatablobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocessdatawob`
--

DROP TABLE IF EXISTS `shkprocessdatawob`;
CREATE TABLE `shkprocessdatawob` (
  `Process` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) default NULL,
  `VariableValueDBL` double default NULL,
  `VariableValueLONG` bigint(20) default NULL,
  `VariableValueDATE` datetime default NULL,
  `VariableValueBOOL` smallint(6) default NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcessDataWOB` (`CNT`),
  UNIQUE KEY `I2_SHKProcessDataWOB` (`Process`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKProcessDataWOB_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkprocessdatawob`
--

LOCK TABLES `shkprocessdatawob` WRITE;
/*!40000 ALTER TABLE `shkprocessdatawob` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkprocessdatawob` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocessdefinitions`
--

DROP TABLE IF EXISTS `shkprocessdefinitions`;
CREATE TABLE `shkprocessdefinitions` (
  `Name` varchar(200) NOT NULL,
  `PackageId` varchar(90) NOT NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionCreated` bigint(20) NOT NULL,
  `ProcessDefinitionVersion` varchar(20) NOT NULL,
  `State` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcessDefinitions` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkprocessdefinitions`
--

LOCK TABLES `shkprocessdefinitions` WRITE;
/*!40000 ALTER TABLE `shkprocessdefinitions` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkprocessdefinitions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocesses`
--

DROP TABLE IF EXISTS `shkprocesses`;
CREATE TABLE `shkprocesses` (
  `SyncVersion` bigint(20) NOT NULL,
  `Id` varchar(100) NOT NULL,
  `ProcessDefinition` decimal(19,0) NOT NULL,
  `PDefName` varchar(200) NOT NULL,
  `ActivityRequesterId` varchar(100) default NULL,
  `ActivityRequesterProcessId` varchar(100) default NULL,
  `ResourceRequesterId` varchar(100) NOT NULL,
  `ExternalRequesterClassName` varchar(254) default NULL,
  `State` decimal(19,0) NOT NULL,
  `Priority` int(11) default NULL,
  `Name` varchar(254) default NULL,
  `Created` bigint(20) NOT NULL,
  `CreatedTZO` bigint(20) NOT NULL,
  `Started` bigint(20) default NULL,
  `StartedTZO` bigint(20) default NULL,
  `LastStateTime` bigint(20) NOT NULL,
  `LastStateTimeTZO` bigint(20) NOT NULL,
  `LimitTime` bigint(20) NOT NULL,
  `LimitTimeTZO` bigint(20) NOT NULL,
  `Description` varchar(254) default NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcesses` (`Id`),
  KEY `I2_SHKProcesses` (`ProcessDefinition`),
  KEY `I3_SHKProcesses` (`State`),
  KEY `I4_SHKProcesses` (`ActivityRequesterId`),
  KEY `I5_SHKProcesses` (`ResourceRequesterId`),
  CONSTRAINT `SHKProcesses_ProcessDefinition` FOREIGN KEY (`ProcessDefinition`) REFERENCES `shkprocessdefinitions` (`oid`),
  CONSTRAINT `SHKProcesses_State` FOREIGN KEY (`State`) REFERENCES `shkprocessstates` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkprocesses`
--

LOCK TABLES `shkprocesses` WRITE;
/*!40000 ALTER TABLE `shkprocesses` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkprocesses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocessrequesters`
--

DROP TABLE IF EXISTS `shkprocessrequesters`;
CREATE TABLE `shkprocessrequesters` (
  `Id` varchar(100) NOT NULL,
  `ActivityRequester` decimal(19,0) default NULL,
  `ResourceRequester` decimal(19,0) default NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcessRequesters` (`Id`),
  KEY `I2_SHKProcessRequesters` (`ActivityRequester`),
  KEY `I3_SHKProcessRequesters` (`ResourceRequester`),
  CONSTRAINT `SHKProcessRequesters_ActivityRequester` FOREIGN KEY (`ActivityRequester`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKProcessRequesters_ResourceRequester` FOREIGN KEY (`ResourceRequester`) REFERENCES `shkresourcestable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkprocessrequesters`
--

LOCK TABLES `shkprocessrequesters` WRITE;
/*!40000 ALTER TABLE `shkprocessrequesters` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkprocessrequesters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocessstateeventaudits`
--

DROP TABLE IF EXISTS `shkprocessstateeventaudits`;
CREATE TABLE `shkprocessstateeventaudits` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcessStateEventAudits` (`KeyValue`),
  UNIQUE KEY `I2_SHKProcessStateEventAudits` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkprocessstateeventaudits`
--

LOCK TABLES `shkprocessstateeventaudits` WRITE;
/*!40000 ALTER TABLE `shkprocessstateeventaudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkprocessstateeventaudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocessstates`
--

DROP TABLE IF EXISTS `shkprocessstates`;
CREATE TABLE `shkprocessstates` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcessStates` (`KeyValue`),
  UNIQUE KEY `I2_SHKProcessStates` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkprocessstates`
--

LOCK TABLES `shkprocessstates` WRITE;
/*!40000 ALTER TABLE `shkprocessstates` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkprocessstates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkproclevelparticipant`
--

DROP TABLE IF EXISTS `shkproclevelparticipant`;
CREATE TABLE `shkproclevelparticipant` (
  `PARTICIPANT_ID` varchar(90) NOT NULL,
  `PROCESSOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcLevelParticipant` (`PARTICIPANT_ID`,`PROCESSOID`),
  KEY `SHKProcLevelParticipant_PROCESSOID` (`PROCESSOID`),
  CONSTRAINT `SHKProcLevelParticipant_PROCESSOID` FOREIGN KEY (`PROCESSOID`) REFERENCES `shkxpdlparticipantprocess` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkproclevelparticipant`
--

LOCK TABLES `shkproclevelparticipant` WRITE;
/*!40000 ALTER TABLE `shkproclevelparticipant` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkproclevelparticipant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkproclevelxpdlapp`
--

DROP TABLE IF EXISTS `shkproclevelxpdlapp`;
CREATE TABLE `shkproclevelxpdlapp` (
  `APPLICATION_ID` varchar(90) NOT NULL,
  `PROCESSOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLApp` (`APPLICATION_ID`,`PROCESSOID`),
  KEY `SHKProcLevelXPDLApp_PROCESSOID` (`PROCESSOID`),
  CONSTRAINT `SHKProcLevelXPDLApp_PROCESSOID` FOREIGN KEY (`PROCESSOID`) REFERENCES `shkxpdlapplicationprocess` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkproclevelxpdlapp`
--

LOCK TABLES `shkproclevelxpdlapp` WRITE;
/*!40000 ALTER TABLE `shkproclevelxpdlapp` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkproclevelxpdlapp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkproclevelxpdlapptaappdetail`
--

DROP TABLE IF EXISTS `shkproclevelxpdlapptaappdetail`;
CREATE TABLE `shkproclevelxpdlapptaappdetail` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppDetail` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppDetail_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetail_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappdetail` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetail_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkproclevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkproclevelxpdlapptaappdetail`
--

LOCK TABLES `shkproclevelxpdlapptaappdetail` WRITE;
/*!40000 ALTER TABLE `shkproclevelxpdlapptaappdetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkproclevelxpdlapptaappdetail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkproclevelxpdlapptaappdetusr`
--

DROP TABLE IF EXISTS `shkproclevelxpdlapptaappdetusr`;
CREATE TABLE `shkproclevelxpdlapptaappdetusr` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppDetUsr` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappdetailuser` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetUsr_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkproclevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkproclevelxpdlapptaappdetusr`
--

LOCK TABLES `shkproclevelxpdlapptaappdetusr` WRITE;
/*!40000 ALTER TABLE `shkproclevelxpdlapptaappdetusr` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkproclevelxpdlapptaappdetusr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkproclevelxpdlapptaappuser`
--

DROP TABLE IF EXISTS `shkproclevelxpdlapptaappuser`;
CREATE TABLE `shkproclevelxpdlapptaappuser` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppUser` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppUser_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppUser_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappuser` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppUser_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkproclevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkproclevelxpdlapptaappuser`
--

LOCK TABLES `shkproclevelxpdlapptaappuser` WRITE;
/*!40000 ALTER TABLE `shkproclevelxpdlapptaappuser` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkproclevelxpdlapptaappuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkproclevelxpdlapptoolagntapp`
--

DROP TABLE IF EXISTS `shkproclevelxpdlapptoolagntapp`;
CREATE TABLE `shkproclevelxpdlapptoolagntapp` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppToolAgntApp` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppToolAgntApp_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppToolAgntApp_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentapp` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppToolAgntApp_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkproclevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkproclevelxpdlapptoolagntapp`
--

LOCK TABLES `shkproclevelxpdlapptoolagntapp` WRITE;
/*!40000 ALTER TABLE `shkproclevelxpdlapptoolagntapp` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkproclevelxpdlapptoolagntapp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkresourcestable`
--

DROP TABLE IF EXISTS `shkresourcestable`;
CREATE TABLE `shkresourcestable` (
  `Username` varchar(100) NOT NULL,
  `Name` varchar(100) default NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKResourcesTable` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkresourcestable`
--

LOCK TABLES `shkresourcestable` WRITE;
/*!40000 ALTER TABLE `shkresourcestable` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkresourcestable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkstateeventaudits`
--

DROP TABLE IF EXISTS `shkstateeventaudits`;
CREATE TABLE `shkstateeventaudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) default NULL,
  `ActivityName` varchar(254) default NULL,
  `ProcessId` varchar(100) NOT NULL,
  `ProcessName` varchar(254) default NULL,
  `ProcessFactoryName` varchar(200) NOT NULL,
  `ProcessFactoryVersion` varchar(20) NOT NULL,
  `ActivityDefinitionId` varchar(90) default NULL,
  `ActivityDefinitionName` varchar(90) default NULL,
  `ActivityDefinitionType` int(11) default NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionName` varchar(90) default NULL,
  `PackageId` varchar(90) NOT NULL,
  `OldProcessState` decimal(19,0) default NULL,
  `NewProcessState` decimal(19,0) default NULL,
  `OldActivityState` decimal(19,0) default NULL,
  `NewActivityState` decimal(19,0) default NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKStateEventAudits` (`CNT`),
  KEY `SHKStateEventAudits_TheType` (`TheType`),
  KEY `SHKStateEventAudits_OldProcessState` (`OldProcessState`),
  KEY `SHKStateEventAudits_NewProcessState` (`NewProcessState`),
  KEY `SHKStateEventAudits_OldActivityState` (`OldActivityState`),
  KEY `SHKStateEventAudits_NewActivityState` (`NewActivityState`),
  CONSTRAINT `SHKStateEventAudits_NewActivityState` FOREIGN KEY (`NewActivityState`) REFERENCES `shkactivitystateeventaudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_NewProcessState` FOREIGN KEY (`NewProcessState`) REFERENCES `shkprocessstateeventaudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_OldActivityState` FOREIGN KEY (`OldActivityState`) REFERENCES `shkactivitystateeventaudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_OldProcessState` FOREIGN KEY (`OldProcessState`) REFERENCES `shkprocessstateeventaudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `shkeventtypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkstateeventaudits`
--

LOCK TABLES `shkstateeventaudits` WRITE;
/*!40000 ALTER TABLE `shkstateeventaudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkstateeventaudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shktoolagentapp`
--

DROP TABLE IF EXISTS `shktoolagentapp`;
CREATE TABLE `shktoolagentapp` (
  `TOOL_AGENT_NAME` varchar(250) NOT NULL,
  `APP_NAME` varchar(90) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKToolAgentApp` (`TOOL_AGENT_NAME`,`APP_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shktoolagentapp`
--

LOCK TABLES `shktoolagentapp` WRITE;
/*!40000 ALTER TABLE `shktoolagentapp` DISABLE KEYS */;
/*!40000 ALTER TABLE `shktoolagentapp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shktoolagentappdetail`
--

DROP TABLE IF EXISTS `shktoolagentappdetail`;
CREATE TABLE `shktoolagentappdetail` (
  `APP_MODE` decimal(10,0) NOT NULL,
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppDetail` (`APP_MODE`,`TOOLAGENT_APPOID`),
  KEY `SHKToolAgentAppDetail_TOOLAGENT_APPOID` (`TOOLAGENT_APPOID`),
  CONSTRAINT `SHKToolAgentAppDetail_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `shktoolagentapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shktoolagentappdetail`
--

LOCK TABLES `shktoolagentappdetail` WRITE;
/*!40000 ALTER TABLE `shktoolagentappdetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `shktoolagentappdetail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shktoolagentappdetailuser`
--

DROP TABLE IF EXISTS `shktoolagentappdetailuser`;
CREATE TABLE `shktoolagentappdetailuser` (
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppDetailUser` (`TOOLAGENT_APPOID`,`USEROID`),
  KEY `SHKToolAgentAppDetailUser_USEROID` (`USEROID`),
  CONSTRAINT `SHKToolAgentAppDetailUser_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `shktoolagentappdetail` (`oid`),
  CONSTRAINT `SHKToolAgentAppDetailUser_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shktoolagentuser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shktoolagentappdetailuser`
--

LOCK TABLES `shktoolagentappdetailuser` WRITE;
/*!40000 ALTER TABLE `shktoolagentappdetailuser` DISABLE KEYS */;
/*!40000 ALTER TABLE `shktoolagentappdetailuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shktoolagentappuser`
--

DROP TABLE IF EXISTS `shktoolagentappuser`;
CREATE TABLE `shktoolagentappuser` (
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppUser` (`TOOLAGENT_APPOID`,`USEROID`),
  KEY `SHKToolAgentAppUser_USEROID` (`USEROID`),
  CONSTRAINT `SHKToolAgentAppUser_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `shktoolagentapp` (`oid`),
  CONSTRAINT `SHKToolAgentAppUser_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shktoolagentuser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shktoolagentappuser`
--

LOCK TABLES `shktoolagentappuser` WRITE;
/*!40000 ALTER TABLE `shktoolagentappuser` DISABLE KEYS */;
/*!40000 ALTER TABLE `shktoolagentappuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shktoolagentuser`
--

DROP TABLE IF EXISTS `shktoolagentuser`;
CREATE TABLE `shktoolagentuser` (
  `USERNAME` varchar(100) NOT NULL,
  `PWD` varchar(100) default NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKToolAgentUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shktoolagentuser`
--

LOCK TABLES `shktoolagentuser` WRITE;
/*!40000 ALTER TABLE `shktoolagentuser` DISABLE KEYS */;
/*!40000 ALTER TABLE `shktoolagentuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkusergrouptable`
--

DROP TABLE IF EXISTS `shkusergrouptable`;
CREATE TABLE `shkusergrouptable` (
  `userid` decimal(19,0) NOT NULL,
  `groupid` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKUserGroupTable` (`userid`,`groupid`),
  KEY `SHKUserGroupTable_groupid` (`groupid`),
  CONSTRAINT `SHKUserGroupTable_groupid` FOREIGN KEY (`groupid`) REFERENCES `shkgrouptable` (`oid`),
  CONSTRAINT `SHKUserGroupTable_userid` FOREIGN KEY (`userid`) REFERENCES `shkusertable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkusergrouptable`
--

LOCK TABLES `shkusergrouptable` WRITE;
/*!40000 ALTER TABLE `shkusergrouptable` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkusergrouptable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkuserpacklevelpart`
--

DROP TABLE IF EXISTS `shkuserpacklevelpart`;
CREATE TABLE `shkuserpacklevelpart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKUserPackLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKUserPackLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKUserPackLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `shkpacklevelparticipant` (`oid`),
  CONSTRAINT `SHKUserPackLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shknormaluser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkuserpacklevelpart`
--

LOCK TABLES `shkuserpacklevelpart` WRITE;
/*!40000 ALTER TABLE `shkuserpacklevelpart` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkuserpacklevelpart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkuserproclevelparticipant`
--

DROP TABLE IF EXISTS `shkuserproclevelparticipant`;
CREATE TABLE `shkuserproclevelparticipant` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKUserProcLevelParticipant` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKUserProcLevelParticipant_USEROID` (`USEROID`),
  CONSTRAINT `SHKUserProcLevelParticipant_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `shkproclevelparticipant` (`oid`),
  CONSTRAINT `SHKUserProcLevelParticipant_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shknormaluser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkuserproclevelparticipant`
--

LOCK TABLES `shkuserproclevelparticipant` WRITE;
/*!40000 ALTER TABLE `shkuserproclevelparticipant` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkuserproclevelparticipant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkusertable`
--

DROP TABLE IF EXISTS `shkusertable`;
CREATE TABLE `shkusertable` (
  `userid` varchar(100) NOT NULL,
  `firstname` varchar(50) default NULL,
  `lastname` varchar(50) default NULL,
  `passwd` varchar(50) NOT NULL,
  `email` varchar(254) default NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKUserTable` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkusertable`
--

LOCK TABLES `shkusertable` WRITE;
/*!40000 ALTER TABLE `shkusertable` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkusertable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdlapplicationpackage`
--

DROP TABLE IF EXISTS `shkxpdlapplicationpackage`;
CREATE TABLE `shkxpdlapplicationpackage` (
  `PACKAGE_ID` varchar(90) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLApplicationPackage` (`PACKAGE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdlapplicationpackage`
--

LOCK TABLES `shkxpdlapplicationpackage` WRITE;
/*!40000 ALTER TABLE `shkxpdlapplicationpackage` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdlapplicationpackage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdlapplicationprocess`
--

DROP TABLE IF EXISTS `shkxpdlapplicationprocess`;
CREATE TABLE `shkxpdlapplicationprocess` (
  `PROCESS_ID` varchar(90) NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLApplicationProcess` (`PROCESS_ID`,`PACKAGEOID`),
  KEY `SHKXPDLApplicationProcess_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKXPDLApplicationProcess_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `shkxpdlapplicationpackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdlapplicationprocess`
--

LOCK TABLES `shkxpdlapplicationprocess` WRITE;
/*!40000 ALTER TABLE `shkxpdlapplicationprocess` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdlapplicationprocess` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdldata`
--

DROP TABLE IF EXISTS `shkxpdldata`;
CREATE TABLE `shkxpdldata` (
  `XPDLContent` mediumblob NOT NULL,
  `XPDLClassContent` mediumblob NOT NULL,
  `XPDL` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLData` (`CNT`),
  UNIQUE KEY `I2_SHKXPDLData` (`XPDL`),
  CONSTRAINT `SHKXPDLData_XPDL` FOREIGN KEY (`XPDL`) REFERENCES `shkxpdls` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdldata`
--

LOCK TABLES `shkxpdldata` WRITE;
/*!40000 ALTER TABLE `shkxpdldata` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdldata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdlhistory`
--

DROP TABLE IF EXISTS `shkxpdlhistory`;
CREATE TABLE `shkxpdlhistory` (
  `XPDLId` varchar(90) NOT NULL,
  `XPDLVersion` varchar(20) NOT NULL,
  `XPDLClassVersion` bigint(20) NOT NULL,
  `XPDLUploadTime` datetime NOT NULL,
  `XPDLHistoryUploadTime` datetime NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLHistory` (`XPDLId`,`XPDLVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdlhistory`
--

LOCK TABLES `shkxpdlhistory` WRITE;
/*!40000 ALTER TABLE `shkxpdlhistory` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdlhistory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdlhistorydata`
--

DROP TABLE IF EXISTS `shkxpdlhistorydata`;
CREATE TABLE `shkxpdlhistorydata` (
  `XPDLContent` mediumblob NOT NULL,
  `XPDLClassContent` mediumblob NOT NULL,
  `XPDLHistory` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLHistoryData` (`CNT`),
  KEY `SHKXPDLHistoryData_XPDLHistory` (`XPDLHistory`),
  CONSTRAINT `SHKXPDLHistoryData_XPDLHistory` FOREIGN KEY (`XPDLHistory`) REFERENCES `shkxpdlhistory` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdlhistorydata`
--

LOCK TABLES `shkxpdlhistorydata` WRITE;
/*!40000 ALTER TABLE `shkxpdlhistorydata` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdlhistorydata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdlparticipantpackage`
--

DROP TABLE IF EXISTS `shkxpdlparticipantpackage`;
CREATE TABLE `shkxpdlparticipantpackage` (
  `PACKAGE_ID` varchar(90) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLParticipantPackage` (`PACKAGE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdlparticipantpackage`
--

LOCK TABLES `shkxpdlparticipantpackage` WRITE;
/*!40000 ALTER TABLE `shkxpdlparticipantpackage` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdlparticipantpackage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdlparticipantprocess`
--

DROP TABLE IF EXISTS `shkxpdlparticipantprocess`;
CREATE TABLE `shkxpdlparticipantprocess` (
  `PROCESS_ID` varchar(90) NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLParticipantProcess` (`PROCESS_ID`,`PACKAGEOID`),
  KEY `SHKXPDLParticipantProcess_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKXPDLParticipantProcess_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `shkxpdlparticipantpackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdlparticipantprocess`
--

LOCK TABLES `shkxpdlparticipantprocess` WRITE;
/*!40000 ALTER TABLE `shkxpdlparticipantprocess` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdlparticipantprocess` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdlreferences`
--

DROP TABLE IF EXISTS `shkxpdlreferences`;
CREATE TABLE `shkxpdlreferences` (
  `ReferredXPDLId` varchar(90) NOT NULL,
  `ReferringXPDL` decimal(19,0) NOT NULL,
  `ReferredXPDLNumber` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLReferences` (`ReferredXPDLId`,`ReferringXPDL`),
  KEY `SHKXPDLReferences_ReferringXPDL` (`ReferringXPDL`),
  CONSTRAINT `SHKXPDLReferences_ReferringXPDL` FOREIGN KEY (`ReferringXPDL`) REFERENCES `shkxpdls` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdlreferences`
--

LOCK TABLES `shkxpdlreferences` WRITE;
/*!40000 ALTER TABLE `shkxpdlreferences` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdlreferences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdls`
--

DROP TABLE IF EXISTS `shkxpdls`;
CREATE TABLE `shkxpdls` (
  `XPDLId` varchar(90) NOT NULL,
  `XPDLVersion` varchar(20) NOT NULL,
  `XPDLClassVersion` bigint(20) NOT NULL,
  `XPDLUploadTime` datetime NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY  (`oid`),
  UNIQUE KEY `I1_SHKXPDLS` (`XPDLId`,`XPDLVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `shkxpdls`
--

LOCK TABLES `shkxpdls` WRITE;
/*!40000 ALTER TABLE `shkxpdls` DISABLE KEYS */;
/*!40000 ALTER TABLE `shkxpdls` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-05-11  7:05:45
