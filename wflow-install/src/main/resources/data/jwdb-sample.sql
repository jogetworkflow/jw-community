-- MySQL dump 10.13  Distrib 5.6.28, for osx10.8 (x86_64)
--
-- Host: localhost    Database: jwdb
-- ------------------------------------------------------
-- Server version	5.6.28

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
-- Table structure for table `SHKActivities`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKActivities` (
  `Id` varchar(100) NOT NULL,
  `ActivitySetDefinitionId` varchar(90) DEFAULT NULL,
  `ActivityDefinitionId` varchar(90) NOT NULL,
  `Process` decimal(19,0) NOT NULL,
  `TheResource` decimal(19,0) DEFAULT NULL,
  `PDefName` varchar(200) NOT NULL,
  `ProcessId` varchar(200) NOT NULL,
  `ResourceId` varchar(100) DEFAULT NULL,
  `State` decimal(19,0) NOT NULL,
  `BlockActivityId` varchar(100) DEFAULT NULL,
  `Performer` varchar(100) DEFAULT NULL,
  `IsPerformerAsynchronous` smallint(6) DEFAULT NULL,
  `Priority` int(11) DEFAULT NULL,
  `Name` varchar(254) DEFAULT NULL,
  `Activated` bigint(20) NOT NULL,
  `ActivatedTZO` bigint(20) NOT NULL,
  `Accepted` bigint(20) DEFAULT NULL,
  `AcceptedTZO` bigint(20) DEFAULT NULL,
  `LastStateTime` bigint(20) NOT NULL,
  `LastStateTimeTZO` bigint(20) NOT NULL,
  `LimitTime` bigint(20) NOT NULL,
  `LimitTimeTZO` bigint(20) NOT NULL,
  `Description` varchar(254) DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivities` (`Id`),
  KEY `SHKActivities_TheResource` (`TheResource`),
  KEY `SHKActivities_State` (`State`),
  KEY `I2_SHKActivities` (`Process`,`ActivitySetDefinitionId`,`ActivityDefinitionId`),
  KEY `I3_SHKActivities` (`Process`,`State`),
  CONSTRAINT `SHKActivities_Process` FOREIGN KEY (`Process`) REFERENCES `SHKProcesses` (`oid`),
  CONSTRAINT `SHKActivities_State` FOREIGN KEY (`State`) REFERENCES `SHKActivityStates` (`oid`),
  CONSTRAINT `SHKActivities_TheResource` FOREIGN KEY (`TheResource`) REFERENCES `SHKResourcesTable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKActivities`
--

LOCK TABLES `SHKActivities` WRITE;
/*!40000 ALTER TABLE `SHKActivities` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKActivities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKActivityData`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKActivityData` (
  `Activity` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) DEFAULT NULL,
  `VariableValueDBL` double DEFAULT NULL,
  `VariableValueLONG` bigint(20) DEFAULT NULL,
  `VariableValueDATE` datetime DEFAULT NULL,
  `VariableValueBOOL` smallint(6) DEFAULT NULL,
  `IsResult` smallint(6) NOT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityData` (`CNT`),
  UNIQUE KEY `I2_SHKActivityData` (`Activity`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKActivityData_Activity` FOREIGN KEY (`Activity`) REFERENCES `SHKActivities` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKActivityData`
--

LOCK TABLES `SHKActivityData` WRITE;
/*!40000 ALTER TABLE `SHKActivityData` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKActivityData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKActivityDataBLOBs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKActivityDataBLOBs` (
  `ActivityDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityDataBLOBs` (`ActivityDataWOB`,`OrdNo`),
  CONSTRAINT `SHKActivityDataBLOBs_ActivityDataWOB` FOREIGN KEY (`ActivityDataWOB`) REFERENCES `SHKActivityDataWOB` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKActivityDataBLOBs`
--

LOCK TABLES `SHKActivityDataBLOBs` WRITE;
/*!40000 ALTER TABLE `SHKActivityDataBLOBs` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKActivityDataBLOBs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKActivityDataWOB`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKActivityDataWOB` (
  `Activity` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) DEFAULT NULL,
  `VariableValueDBL` double DEFAULT NULL,
  `VariableValueLONG` bigint(20) DEFAULT NULL,
  `VariableValueDATE` datetime DEFAULT NULL,
  `VariableValueBOOL` smallint(6) DEFAULT NULL,
  `IsResult` smallint(6) NOT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityDataWOB` (`CNT`),
  UNIQUE KEY `I2_SHKActivityDataWOB` (`Activity`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKActivityDataWOB_Activity` FOREIGN KEY (`Activity`) REFERENCES `SHKActivities` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKActivityDataWOB`
--

LOCK TABLES `SHKActivityDataWOB` WRITE;
/*!40000 ALTER TABLE `SHKActivityDataWOB` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKActivityDataWOB` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKActivityStateEventAudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKActivityStateEventAudits` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityStateEventAudits` (`KeyValue`),
  UNIQUE KEY `I2_SHKActivityStateEventAudits` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKActivityStateEventAudits`
--

LOCK TABLES `SHKActivityStateEventAudits` WRITE;
/*!40000 ALTER TABLE `SHKActivityStateEventAudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKActivityStateEventAudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKActivityStates`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKActivityStates` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityStates` (`KeyValue`),
  UNIQUE KEY `I2_SHKActivityStates` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKActivityStates`
--

LOCK TABLES `SHKActivityStates` WRITE;
/*!40000 ALTER TABLE `SHKActivityStates` DISABLE KEYS */;
INSERT INTO `SHKActivityStates` VALUES ('open.running','open.running',1000001,0),('open.not_running.not_started','open.not_running.not_started',1000003,0),('open.not_running.suspended','open.not_running.suspended',1000005,0),('closed.completed','closed.completed',1000007,0),('closed.terminated','closed.terminated',1000009,0),('closed.aborted','closed.aborted',1000011,0);
/*!40000 ALTER TABLE `SHKActivityStates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKAndJoinTable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKAndJoinTable` (
  `Process` decimal(19,0) NOT NULL,
  `BlockActivity` decimal(19,0) DEFAULT NULL,
  `ActivityDefinitionId` varchar(90) NOT NULL,
  `Activity` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKAndJoinTable` (`CNT`),
  KEY `SHKAndJoinTable_BlockActivity` (`BlockActivity`),
  KEY `I2_SHKAndJoinTable` (`Process`,`BlockActivity`,`ActivityDefinitionId`),
  KEY `I3_SHKAndJoinTable` (`Activity`),
  CONSTRAINT `SHKAndJoinTable_Activity` FOREIGN KEY (`Activity`) REFERENCES `SHKActivities` (`oid`),
  CONSTRAINT `SHKAndJoinTable_BlockActivity` FOREIGN KEY (`BlockActivity`) REFERENCES `SHKActivities` (`oid`),
  CONSTRAINT `SHKAndJoinTable_Process` FOREIGN KEY (`Process`) REFERENCES `SHKProcesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKAndJoinTable`
--

LOCK TABLES `SHKAndJoinTable` WRITE;
/*!40000 ALTER TABLE `SHKAndJoinTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKAndJoinTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKAssignmentEventAudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKAssignmentEventAudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) NOT NULL,
  `ActivityName` varchar(254) DEFAULT NULL,
  `ProcessId` varchar(100) NOT NULL,
  `ProcessName` varchar(254) DEFAULT NULL,
  `ProcessFactoryName` varchar(200) NOT NULL,
  `ProcessFactoryVersion` varchar(20) NOT NULL,
  `ActivityDefinitionId` varchar(90) NOT NULL,
  `ActivityDefinitionName` varchar(90) DEFAULT NULL,
  `ActivityDefinitionType` int(11) NOT NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionName` varchar(90) DEFAULT NULL,
  `PackageId` varchar(90) NOT NULL,
  `OldResourceUsername` varchar(100) DEFAULT NULL,
  `OldResourceName` varchar(100) DEFAULT NULL,
  `NewResourceUsername` varchar(100) NOT NULL,
  `NewResourceName` varchar(100) DEFAULT NULL,
  `IsAccepted` smallint(6) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKAssignmentEventAudits` (`CNT`),
  KEY `SHKAssignmentEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKAssignmentEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `SHKEventTypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKAssignmentEventAudits`
--

LOCK TABLES `SHKAssignmentEventAudits` WRITE;
/*!40000 ALTER TABLE `SHKAssignmentEventAudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKAssignmentEventAudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKAssignmentsTable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKAssignmentsTable` (
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
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKAssignmentsTable` (`CNT`),
  UNIQUE KEY `I2_SHKAssignmentsTable` (`Activity`,`TheResource`),
  KEY `I3_SHKAssignmentsTable` (`TheResource`,`IsValid`),
  KEY `I4_SHKAssignmentsTable` (`ActivityId`),
  KEY `I5_SHKAssignmentsTable` (`ResourceId`),
  KEY `FK_rnb6mhntls567xpifcfvygkuu` (`ActivityProcessId`),
  CONSTRAINT `FK_183e6adufsi558hl5p4dqkqsx` FOREIGN KEY (`ActivityId`) REFERENCES `SHKActivities` (`Id`),
  CONSTRAINT `FK_rnb6mhntls567xpifcfvygkuu` FOREIGN KEY (`ActivityProcessId`) REFERENCES `SHKProcesses` (`Id`),
  CONSTRAINT `SHKAssignmentsTable_Activity` FOREIGN KEY (`Activity`) REFERENCES `SHKActivities` (`oid`),
  CONSTRAINT `SHKAssignmentsTable_TheResource` FOREIGN KEY (`TheResource`) REFERENCES `SHKResourcesTable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKAssignmentsTable`
--

LOCK TABLES `SHKAssignmentsTable` WRITE;
/*!40000 ALTER TABLE `SHKAssignmentsTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKAssignmentsTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKCounters`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKCounters` (
  `name` varchar(100) NOT NULL,
  `the_number` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKCounters` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKCounters`
--

LOCK TABLES `SHKCounters` WRITE;
/*!40000 ALTER TABLE `SHKCounters` DISABLE KEYS */;
INSERT INTO `SHKCounters` VALUES ('_xpdldata_',2,1000204,0);
/*!40000 ALTER TABLE `SHKCounters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKCreateProcessEventAudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKCreateProcessEventAudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ProcessId` varchar(100) NOT NULL,
  `ProcessName` varchar(254) DEFAULT NULL,
  `ProcessFactoryName` varchar(200) NOT NULL,
  `ProcessFactoryVersion` varchar(20) NOT NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionName` varchar(90) DEFAULT NULL,
  `PackageId` varchar(90) NOT NULL,
  `PActivityId` varchar(100) DEFAULT NULL,
  `PProcessId` varchar(100) DEFAULT NULL,
  `PProcessName` varchar(254) DEFAULT NULL,
  `PProcessFactoryName` varchar(200) DEFAULT NULL,
  `PProcessFactoryVersion` varchar(20) DEFAULT NULL,
  `PActivityDefinitionId` varchar(90) DEFAULT NULL,
  `PActivityDefinitionName` varchar(90) DEFAULT NULL,
  `PProcessDefinitionId` varchar(90) DEFAULT NULL,
  `PProcessDefinitionName` varchar(90) DEFAULT NULL,
  `PPackageId` varchar(90) DEFAULT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKCreateProcessEventAudits` (`CNT`),
  KEY `SHKCreateProcessEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKCreateProcessEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `SHKEventTypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKCreateProcessEventAudits`
--

LOCK TABLES `SHKCreateProcessEventAudits` WRITE;
/*!40000 ALTER TABLE `SHKCreateProcessEventAudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKCreateProcessEventAudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKDataEventAudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKDataEventAudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) DEFAULT NULL,
  `ActivityName` varchar(254) DEFAULT NULL,
  `ProcessId` varchar(100) NOT NULL,
  `ProcessName` varchar(254) DEFAULT NULL,
  `ProcessFactoryName` varchar(200) NOT NULL,
  `ProcessFactoryVersion` varchar(20) NOT NULL,
  `ActivityDefinitionId` varchar(90) DEFAULT NULL,
  `ActivityDefinitionName` varchar(90) DEFAULT NULL,
  `ActivityDefinitionType` int(11) DEFAULT NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionName` varchar(90) DEFAULT NULL,
  `PackageId` varchar(90) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKDataEventAudits` (`CNT`),
  KEY `SHKDataEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKDataEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `SHKEventTypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKDataEventAudits`
--

LOCK TABLES `SHKDataEventAudits` WRITE;
/*!40000 ALTER TABLE `SHKDataEventAudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKDataEventAudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKDeadlines`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKDeadlines` (
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
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKDeadlines` (`CNT`),
  KEY `I2_SHKDeadlines` (`Process`,`TimeLimit`),
  KEY `I3_SHKDeadlines` (`Activity`,`TimeLimit`),
  CONSTRAINT `SHKDeadlines_Activity` FOREIGN KEY (`Activity`) REFERENCES `SHKActivities` (`oid`),
  CONSTRAINT `SHKDeadlines_Process` FOREIGN KEY (`Process`) REFERENCES `SHKProcesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKDeadlines`
--

LOCK TABLES `SHKDeadlines` WRITE;
/*!40000 ALTER TABLE `SHKDeadlines` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKDeadlines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKEventTypes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKEventTypes` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKEventTypes` (`KeyValue`),
  UNIQUE KEY `I2_SHKEventTypes` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKEventTypes`
--

LOCK TABLES `SHKEventTypes` WRITE;
/*!40000 ALTER TABLE `SHKEventTypes` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKEventTypes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKGroupGroupTable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKGroupGroupTable` (
  `sub_gid` decimal(19,0) NOT NULL,
  `groupid` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupGroupTable` (`sub_gid`,`groupid`),
  KEY `I2_SHKGroupGroupTable` (`groupid`),
  CONSTRAINT `SHKGroupGroupTable_groupid` FOREIGN KEY (`groupid`) REFERENCES `SHKGroupTable` (`oid`),
  CONSTRAINT `SHKGroupGroupTable_sub_gid` FOREIGN KEY (`sub_gid`) REFERENCES `SHKGroupTable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKGroupGroupTable`
--

LOCK TABLES `SHKGroupGroupTable` WRITE;
/*!40000 ALTER TABLE `SHKGroupGroupTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKGroupGroupTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKGroupTable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKGroupTable` (
  `groupid` varchar(100) NOT NULL,
  `description` varchar(254) DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupTable` (`groupid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKGroupTable`
--

LOCK TABLES `SHKGroupTable` WRITE;
/*!40000 ALTER TABLE `SHKGroupTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKGroupTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKGroupUser`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKGroupUser` (
  `USERNAME` varchar(100) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKGroupUser`
--

LOCK TABLES `SHKGroupUser` WRITE;
/*!40000 ALTER TABLE `SHKGroupUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKGroupUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKGroupUserPackLevelPart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKGroupUserPackLevelPart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupUserPackLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKGroupUserPackLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKGroupUserPackLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `SHKPackLevelParticipant` (`oid`),
  CONSTRAINT `SHKGroupUserPackLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `SHKGroupUser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKGroupUserPackLevelPart`
--

LOCK TABLES `SHKGroupUserPackLevelPart` WRITE;
/*!40000 ALTER TABLE `SHKGroupUserPackLevelPart` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKGroupUserPackLevelPart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKGroupUserProcLevelPart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKGroupUserProcLevelPart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupUserProcLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKGroupUserProcLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKGroupUserProcLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `SHKProcLevelParticipant` (`oid`),
  CONSTRAINT `SHKGroupUserProcLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `SHKGroupUser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKGroupUserProcLevelPart`
--

LOCK TABLES `SHKGroupUserProcLevelPart` WRITE;
/*!40000 ALTER TABLE `SHKGroupUserProcLevelPart` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKGroupUserProcLevelPart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKNewEventAuditData`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKNewEventAuditData` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) DEFAULT NULL,
  `VariableValueDBL` float DEFAULT NULL,
  `VariableValueLONG` bigint(20) DEFAULT NULL,
  `VariableValueDATE` datetime DEFAULT NULL,
  `VariableValueBOOL` smallint(6) DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNewEventAuditData` (`CNT`),
  UNIQUE KEY `I2_SHKNewEventAuditData` (`DataEventAudit`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKNewEventAuditData_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `SHKDataEventAudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKNewEventAuditData`
--

LOCK TABLES `SHKNewEventAuditData` WRITE;
/*!40000 ALTER TABLE `SHKNewEventAuditData` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKNewEventAuditData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKNewEventAuditDataBLOBs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKNewEventAuditDataBLOBs` (
  `NewEventAuditDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNewEventAuditDataBLOBs` (`NewEventAuditDataWOB`,`OrdNo`),
  CONSTRAINT `SHKNewEventAuditDataBLOBs_NewEventAuditDataWOB` FOREIGN KEY (`NewEventAuditDataWOB`) REFERENCES `SHKNewEventAuditDataWOB` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKNewEventAuditDataBLOBs`
--

LOCK TABLES `SHKNewEventAuditDataBLOBs` WRITE;
/*!40000 ALTER TABLE `SHKNewEventAuditDataBLOBs` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKNewEventAuditDataBLOBs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKNewEventAuditDataWOB`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKNewEventAuditDataWOB` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) DEFAULT NULL,
  `VariableValueDBL` float DEFAULT NULL,
  `VariableValueLONG` bigint(20) DEFAULT NULL,
  `VariableValueDATE` datetime DEFAULT NULL,
  `VariableValueBOOL` smallint(6) DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNewEventAuditDataWOB` (`CNT`),
  UNIQUE KEY `I2_SHKNewEventAuditDataWOB` (`DataEventAudit`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKNewEventAuditDataWOB_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `SHKDataEventAudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKNewEventAuditDataWOB`
--

LOCK TABLES `SHKNewEventAuditDataWOB` WRITE;
/*!40000 ALTER TABLE `SHKNewEventAuditDataWOB` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKNewEventAuditDataWOB` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKNextXPDLVersions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKNextXPDLVersions` (
  `XPDLId` varchar(90) NOT NULL,
  `NextVersion` varchar(20) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNextXPDLVersions` (`XPDLId`,`NextVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKNextXPDLVersions`
--

LOCK TABLES `SHKNextXPDLVersions` WRITE;
/*!40000 ALTER TABLE `SHKNextXPDLVersions` DISABLE KEYS */;
INSERT INTO `SHKNextXPDLVersions` VALUES ('crm','2',1000201,0);
/*!40000 ALTER TABLE `SHKNextXPDLVersions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKNormalUser`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKNormalUser` (
  `USERNAME` varchar(100) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNormalUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKNormalUser`
--

LOCK TABLES `SHKNormalUser` WRITE;
/*!40000 ALTER TABLE `SHKNormalUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKNormalUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKOldEventAuditData`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKOldEventAuditData` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) DEFAULT NULL,
  `VariableValueDBL` float DEFAULT NULL,
  `VariableValueLONG` bigint(20) DEFAULT NULL,
  `VariableValueDATE` datetime DEFAULT NULL,
  `VariableValueBOOL` smallint(6) DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKOldEventAuditData` (`CNT`),
  UNIQUE KEY `I2_SHKOldEventAuditData` (`DataEventAudit`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKOldEventAuditData_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `SHKDataEventAudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKOldEventAuditData`
--

LOCK TABLES `SHKOldEventAuditData` WRITE;
/*!40000 ALTER TABLE `SHKOldEventAuditData` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKOldEventAuditData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKOldEventAuditDataBLOBs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKOldEventAuditDataBLOBs` (
  `OldEventAuditDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKOldEventAuditDataBLOBs` (`OldEventAuditDataWOB`,`OrdNo`),
  CONSTRAINT `SHKOldEventAuditDataBLOBs_OldEventAuditDataWOB` FOREIGN KEY (`OldEventAuditDataWOB`) REFERENCES `SHKOldEventAuditDataWOB` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKOldEventAuditDataBLOBs`
--

LOCK TABLES `SHKOldEventAuditDataBLOBs` WRITE;
/*!40000 ALTER TABLE `SHKOldEventAuditDataBLOBs` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKOldEventAuditDataBLOBs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKOldEventAuditDataWOB`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKOldEventAuditDataWOB` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) DEFAULT NULL,
  `VariableValueDBL` float DEFAULT NULL,
  `VariableValueLONG` bigint(20) DEFAULT NULL,
  `VariableValueDATE` datetime DEFAULT NULL,
  `VariableValueBOOL` smallint(6) DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKOldEventAuditDataWOB` (`CNT`),
  UNIQUE KEY `I2_SHKOldEventAuditDataWOB` (`DataEventAudit`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKOldEventAuditDataWOB_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `SHKDataEventAudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKOldEventAuditDataWOB`
--

LOCK TABLES `SHKOldEventAuditDataWOB` WRITE;
/*!40000 ALTER TABLE `SHKOldEventAuditDataWOB` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKOldEventAuditDataWOB` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKPackLevelParticipant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKPackLevelParticipant` (
  `PARTICIPANT_ID` varchar(90) NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelParticipant` (`PARTICIPANT_ID`,`PACKAGEOID`),
  KEY `SHKPackLevelParticipant_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKPackLevelParticipant_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `SHKXPDLParticipantPackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKPackLevelParticipant`
--

LOCK TABLES `SHKPackLevelParticipant` WRITE;
/*!40000 ALTER TABLE `SHKPackLevelParticipant` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKPackLevelParticipant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKPackLevelXPDLApp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKPackLevelXPDLApp` (
  `APPLICATION_ID` varchar(90) NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLApp` (`APPLICATION_ID`,`PACKAGEOID`),
  KEY `SHKPackLevelXPDLApp_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKPackLevelXPDLApp_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `SHKXPDLApplicationPackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKPackLevelXPDLApp`
--

LOCK TABLES `SHKPackLevelXPDLApp` WRITE;
/*!40000 ALTER TABLE `SHKPackLevelXPDLApp` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKPackLevelXPDLApp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKPackLevelXPDLAppTAAppDetUsr`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKPackLevelXPDLAppTAAppDetUsr` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppDetUsr` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `SHKToolAgentAppDetailUser` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetUsr_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `SHKPackLevelXPDLApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKPackLevelXPDLAppTAAppDetUsr`
--

LOCK TABLES `SHKPackLevelXPDLAppTAAppDetUsr` WRITE;
/*!40000 ALTER TABLE `SHKPackLevelXPDLAppTAAppDetUsr` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKPackLevelXPDLAppTAAppDetUsr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKPackLevelXPDLAppTAAppDetail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKPackLevelXPDLAppTAAppDetail` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppDetail` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppDetail_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetail_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `SHKToolAgentAppDetail` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetail_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `SHKPackLevelXPDLApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKPackLevelXPDLAppTAAppDetail`
--

LOCK TABLES `SHKPackLevelXPDLAppTAAppDetail` WRITE;
/*!40000 ALTER TABLE `SHKPackLevelXPDLAppTAAppDetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKPackLevelXPDLAppTAAppDetail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKPackLevelXPDLAppTAAppUser`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKPackLevelXPDLAppTAAppUser` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppUser` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppUser_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppUser_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `SHKToolAgentAppUser` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppUser_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `SHKPackLevelXPDLApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKPackLevelXPDLAppTAAppUser`
--

LOCK TABLES `SHKPackLevelXPDLAppTAAppUser` WRITE;
/*!40000 ALTER TABLE `SHKPackLevelXPDLAppTAAppUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKPackLevelXPDLAppTAAppUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKPackLevelXPDLAppToolAgntApp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKPackLevelXPDLAppToolAgntApp` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppToolAgntApp` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppToolAgntApp_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppToolAgntApp_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `SHKToolAgentApp` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppToolAgntApp_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `SHKPackLevelXPDLApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKPackLevelXPDLAppToolAgntApp`
--

LOCK TABLES `SHKPackLevelXPDLAppToolAgntApp` WRITE;
/*!40000 ALTER TABLE `SHKPackLevelXPDLAppToolAgntApp` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKPackLevelXPDLAppToolAgntApp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcLevelParticipant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcLevelParticipant` (
  `PARTICIPANT_ID` varchar(90) NOT NULL,
  `PROCESSOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelParticipant` (`PARTICIPANT_ID`,`PROCESSOID`),
  KEY `SHKProcLevelParticipant_PROCESSOID` (`PROCESSOID`),
  CONSTRAINT `SHKProcLevelParticipant_PROCESSOID` FOREIGN KEY (`PROCESSOID`) REFERENCES `SHKXPDLParticipantProcess` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcLevelParticipant`
--

LOCK TABLES `SHKProcLevelParticipant` WRITE;
/*!40000 ALTER TABLE `SHKProcLevelParticipant` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcLevelParticipant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcLevelXPDLApp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcLevelXPDLApp` (
  `APPLICATION_ID` varchar(90) NOT NULL,
  `PROCESSOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLApp` (`APPLICATION_ID`,`PROCESSOID`),
  KEY `SHKProcLevelXPDLApp_PROCESSOID` (`PROCESSOID`),
  CONSTRAINT `SHKProcLevelXPDLApp_PROCESSOID` FOREIGN KEY (`PROCESSOID`) REFERENCES `SHKXPDLApplicationProcess` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcLevelXPDLApp`
--

LOCK TABLES `SHKProcLevelXPDLApp` WRITE;
/*!40000 ALTER TABLE `SHKProcLevelXPDLApp` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcLevelXPDLApp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcLevelXPDLAppTAAppDetUsr`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcLevelXPDLAppTAAppDetUsr` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppDetUsr` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `SHKToolAgentAppDetailUser` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetUsr_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `SHKProcLevelXPDLApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcLevelXPDLAppTAAppDetUsr`
--

LOCK TABLES `SHKProcLevelXPDLAppTAAppDetUsr` WRITE;
/*!40000 ALTER TABLE `SHKProcLevelXPDLAppTAAppDetUsr` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcLevelXPDLAppTAAppDetUsr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcLevelXPDLAppTAAppDetail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcLevelXPDLAppTAAppDetail` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppDetail` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppDetail_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetail_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `SHKToolAgentAppDetail` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetail_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `SHKProcLevelXPDLApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcLevelXPDLAppTAAppDetail`
--

LOCK TABLES `SHKProcLevelXPDLAppTAAppDetail` WRITE;
/*!40000 ALTER TABLE `SHKProcLevelXPDLAppTAAppDetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcLevelXPDLAppTAAppDetail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcLevelXPDLAppTAAppUser`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcLevelXPDLAppTAAppUser` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppUser` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppUser_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppUser_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `SHKToolAgentAppUser` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppUser_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `SHKProcLevelXPDLApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcLevelXPDLAppTAAppUser`
--

LOCK TABLES `SHKProcLevelXPDLAppTAAppUser` WRITE;
/*!40000 ALTER TABLE `SHKProcLevelXPDLAppTAAppUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcLevelXPDLAppTAAppUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcLevelXPDLAppToolAgntApp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcLevelXPDLAppToolAgntApp` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppToolAgntApp` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppToolAgntApp_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppToolAgntApp_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `SHKToolAgentApp` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppToolAgntApp_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `SHKProcLevelXPDLApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcLevelXPDLAppToolAgntApp`
--

LOCK TABLES `SHKProcLevelXPDLAppToolAgntApp` WRITE;
/*!40000 ALTER TABLE `SHKProcLevelXPDLAppToolAgntApp` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcLevelXPDLAppToolAgntApp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcessData`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcessData` (
  `Process` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) DEFAULT NULL,
  `VariableValueDBL` double DEFAULT NULL,
  `VariableValueLONG` bigint(20) DEFAULT NULL,
  `VariableValueDATE` datetime DEFAULT NULL,
  `VariableValueBOOL` smallint(6) DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessData` (`CNT`),
  UNIQUE KEY `I2_SHKProcessData` (`Process`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKProcessData_Process` FOREIGN KEY (`Process`) REFERENCES `SHKProcesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessData`
--

LOCK TABLES `SHKProcessData` WRITE;
/*!40000 ALTER TABLE `SHKProcessData` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcessData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcessDataBLOBs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcessDataBLOBs` (
  `ProcessDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessDataBLOBs` (`ProcessDataWOB`,`OrdNo`),
  CONSTRAINT `SHKProcessDataBLOBs_ProcessDataWOB` FOREIGN KEY (`ProcessDataWOB`) REFERENCES `SHKProcessDataWOB` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessDataBLOBs`
--

LOCK TABLES `SHKProcessDataBLOBs` WRITE;
/*!40000 ALTER TABLE `SHKProcessDataBLOBs` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcessDataBLOBs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcessDataWOB`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcessDataWOB` (
  `Process` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text,
  `VariableValueVCHAR` varchar(4000) DEFAULT NULL,
  `VariableValueDBL` double DEFAULT NULL,
  `VariableValueLONG` bigint(20) DEFAULT NULL,
  `VariableValueDATE` datetime DEFAULT NULL,
  `VariableValueBOOL` smallint(6) DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessDataWOB` (`CNT`),
  UNIQUE KEY `I2_SHKProcessDataWOB` (`Process`,`VariableDefinitionId`,`OrdNo`),
  CONSTRAINT `SHKProcessDataWOB_Process` FOREIGN KEY (`Process`) REFERENCES `SHKProcesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessDataWOB`
--

LOCK TABLES `SHKProcessDataWOB` WRITE;
/*!40000 ALTER TABLE `SHKProcessDataWOB` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcessDataWOB` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcessDefinitions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcessDefinitions` (
  `Name` varchar(200) NOT NULL,
  `PackageId` varchar(90) NOT NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionCreated` bigint(20) NOT NULL,
  `ProcessDefinitionVersion` varchar(20) NOT NULL,
  `State` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessDefinitions` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessDefinitions`
--

LOCK TABLES `SHKProcessDefinitions` WRITE;
/*!40000 ALTER TABLE `SHKProcessDefinitions` DISABLE KEYS */;
INSERT INTO `SHKProcessDefinitions` VALUES ('crm#1#process1','crm','process1',1499849773099,'1',0,1000205,0);
/*!40000 ALTER TABLE `SHKProcessDefinitions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcessRequesters`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcessRequesters` (
  `Id` varchar(100) NOT NULL,
  `ActivityRequester` decimal(19,0) DEFAULT NULL,
  `ResourceRequester` decimal(19,0) DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessRequesters` (`Id`),
  KEY `I2_SHKProcessRequesters` (`ActivityRequester`),
  KEY `I3_SHKProcessRequesters` (`ResourceRequester`),
  CONSTRAINT `SHKProcessRequesters_ActivityRequester` FOREIGN KEY (`ActivityRequester`) REFERENCES `SHKActivities` (`oid`),
  CONSTRAINT `SHKProcessRequesters_ResourceRequester` FOREIGN KEY (`ResourceRequester`) REFERENCES `SHKResourcesTable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessRequesters`
--

LOCK TABLES `SHKProcessRequesters` WRITE;
/*!40000 ALTER TABLE `SHKProcessRequesters` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcessRequesters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcessStateEventAudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcessStateEventAudits` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessStateEventAudits` (`KeyValue`),
  UNIQUE KEY `I2_SHKProcessStateEventAudits` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessStateEventAudits`
--

LOCK TABLES `SHKProcessStateEventAudits` WRITE;
/*!40000 ALTER TABLE `SHKProcessStateEventAudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcessStateEventAudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcessStates`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcessStates` (
  `KeyValue` varchar(30) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessStates` (`KeyValue`),
  UNIQUE KEY `I2_SHKProcessStates` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessStates`
--

LOCK TABLES `SHKProcessStates` WRITE;
/*!40000 ALTER TABLE `SHKProcessStates` DISABLE KEYS */;
INSERT INTO `SHKProcessStates` VALUES ('open.running','open.running',1000000,0),('open.not_running.not_started','open.not_running.not_started',1000002,0),('open.not_running.suspended','open.not_running.suspended',1000004,0),('closed.completed','closed.completed',1000006,0),('closed.terminated','closed.terminated',1000008,0),('closed.aborted','closed.aborted',1000010,0);
/*!40000 ALTER TABLE `SHKProcessStates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcesses`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKProcesses` (
  `SyncVersion` bigint(20) NOT NULL,
  `Id` varchar(100) NOT NULL,
  `ProcessDefinition` decimal(19,0) NOT NULL,
  `PDefName` varchar(200) NOT NULL,
  `ActivityRequesterId` varchar(100) DEFAULT NULL,
  `ActivityRequesterProcessId` varchar(100) DEFAULT NULL,
  `ResourceRequesterId` varchar(100) NOT NULL,
  `ExternalRequesterClassName` varchar(254) DEFAULT NULL,
  `State` decimal(19,0) NOT NULL,
  `Priority` int(11) DEFAULT NULL,
  `Name` varchar(254) DEFAULT NULL,
  `Created` bigint(20) NOT NULL,
  `CreatedTZO` bigint(20) NOT NULL,
  `Started` bigint(20) DEFAULT NULL,
  `StartedTZO` bigint(20) DEFAULT NULL,
  `LastStateTime` bigint(20) NOT NULL,
  `LastStateTimeTZO` bigint(20) NOT NULL,
  `LimitTime` bigint(20) NOT NULL,
  `LimitTimeTZO` bigint(20) NOT NULL,
  `Description` varchar(254) DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcesses` (`Id`),
  KEY `I2_SHKProcesses` (`ProcessDefinition`),
  KEY `I3_SHKProcesses` (`State`),
  KEY `I4_SHKProcesses` (`ActivityRequesterId`),
  KEY `I5_SHKProcesses` (`ResourceRequesterId`),
  CONSTRAINT `SHKProcesses_ProcessDefinition` FOREIGN KEY (`ProcessDefinition`) REFERENCES `SHKProcessDefinitions` (`oid`),
  CONSTRAINT `SHKProcesses_State` FOREIGN KEY (`State`) REFERENCES `SHKProcessStates` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcesses`
--

LOCK TABLES `SHKProcesses` WRITE;
/*!40000 ALTER TABLE `SHKProcesses` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKProcesses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKResourcesTable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKResourcesTable` (
  `Username` varchar(100) NOT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKResourcesTable` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKResourcesTable`
--

LOCK TABLES `SHKResourcesTable` WRITE;
/*!40000 ALTER TABLE `SHKResourcesTable` DISABLE KEYS */;
INSERT INTO `SHKResourcesTable` VALUES ('roleAnonymous',NULL,1000200,0);
/*!40000 ALTER TABLE `SHKResourcesTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKStateEventAudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKStateEventAudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) DEFAULT NULL,
  `ActivityName` varchar(254) DEFAULT NULL,
  `ProcessId` varchar(100) NOT NULL,
  `ProcessName` varchar(254) DEFAULT NULL,
  `ProcessFactoryName` varchar(200) NOT NULL,
  `ProcessFactoryVersion` varchar(20) NOT NULL,
  `ActivityDefinitionId` varchar(90) DEFAULT NULL,
  `ActivityDefinitionName` varchar(90) DEFAULT NULL,
  `ActivityDefinitionType` int(11) DEFAULT NULL,
  `ProcessDefinitionId` varchar(90) NOT NULL,
  `ProcessDefinitionName` varchar(90) DEFAULT NULL,
  `PackageId` varchar(90) NOT NULL,
  `OldProcessState` decimal(19,0) DEFAULT NULL,
  `NewProcessState` decimal(19,0) DEFAULT NULL,
  `OldActivityState` decimal(19,0) DEFAULT NULL,
  `NewActivityState` decimal(19,0) DEFAULT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKStateEventAudits` (`CNT`),
  KEY `SHKStateEventAudits_TheType` (`TheType`),
  KEY `SHKStateEventAudits_OldProcessState` (`OldProcessState`),
  KEY `SHKStateEventAudits_NewProcessState` (`NewProcessState`),
  KEY `SHKStateEventAudits_OldActivityState` (`OldActivityState`),
  KEY `SHKStateEventAudits_NewActivityState` (`NewActivityState`),
  CONSTRAINT `SHKStateEventAudits_NewActivityState` FOREIGN KEY (`NewActivityState`) REFERENCES `SHKActivityStateEventAudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_NewProcessState` FOREIGN KEY (`NewProcessState`) REFERENCES `SHKProcessStateEventAudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_OldActivityState` FOREIGN KEY (`OldActivityState`) REFERENCES `SHKActivityStateEventAudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_OldProcessState` FOREIGN KEY (`OldProcessState`) REFERENCES `SHKProcessStateEventAudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `SHKEventTypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKStateEventAudits`
--

LOCK TABLES `SHKStateEventAudits` WRITE;
/*!40000 ALTER TABLE `SHKStateEventAudits` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKStateEventAudits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKToolAgentApp`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKToolAgentApp` (
  `TOOL_AGENT_NAME` varchar(250) NOT NULL,
  `APP_NAME` varchar(90) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentApp` (`TOOL_AGENT_NAME`,`APP_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKToolAgentApp`
--

LOCK TABLES `SHKToolAgentApp` WRITE;
/*!40000 ALTER TABLE `SHKToolAgentApp` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKToolAgentApp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKToolAgentAppDetail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKToolAgentAppDetail` (
  `APP_MODE` decimal(10,0) NOT NULL,
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppDetail` (`APP_MODE`,`TOOLAGENT_APPOID`),
  KEY `SHKToolAgentAppDetail_TOOLAGENT_APPOID` (`TOOLAGENT_APPOID`),
  CONSTRAINT `SHKToolAgentAppDetail_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `SHKToolAgentApp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKToolAgentAppDetail`
--

LOCK TABLES `SHKToolAgentAppDetail` WRITE;
/*!40000 ALTER TABLE `SHKToolAgentAppDetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKToolAgentAppDetail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKToolAgentAppDetailUser`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKToolAgentAppDetailUser` (
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppDetailUser` (`TOOLAGENT_APPOID`,`USEROID`),
  KEY `SHKToolAgentAppDetailUser_USEROID` (`USEROID`),
  CONSTRAINT `SHKToolAgentAppDetailUser_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `SHKToolAgentAppDetail` (`oid`),
  CONSTRAINT `SHKToolAgentAppDetailUser_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `SHKToolAgentUser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKToolAgentAppDetailUser`
--

LOCK TABLES `SHKToolAgentAppDetailUser` WRITE;
/*!40000 ALTER TABLE `SHKToolAgentAppDetailUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKToolAgentAppDetailUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKToolAgentAppUser`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKToolAgentAppUser` (
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppUser` (`TOOLAGENT_APPOID`,`USEROID`),
  KEY `SHKToolAgentAppUser_USEROID` (`USEROID`),
  CONSTRAINT `SHKToolAgentAppUser_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `SHKToolAgentApp` (`oid`),
  CONSTRAINT `SHKToolAgentAppUser_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `SHKToolAgentUser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKToolAgentAppUser`
--

LOCK TABLES `SHKToolAgentAppUser` WRITE;
/*!40000 ALTER TABLE `SHKToolAgentAppUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKToolAgentAppUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKToolAgentUser`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKToolAgentUser` (
  `USERNAME` varchar(100) NOT NULL,
  `PWD` varchar(100) DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKToolAgentUser`
--

LOCK TABLES `SHKToolAgentUser` WRITE;
/*!40000 ALTER TABLE `SHKToolAgentUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKToolAgentUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKUserGroupTable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKUserGroupTable` (
  `userid` decimal(19,0) NOT NULL,
  `groupid` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKUserGroupTable` (`userid`,`groupid`),
  KEY `SHKUserGroupTable_groupid` (`groupid`),
  CONSTRAINT `SHKUserGroupTable_groupid` FOREIGN KEY (`groupid`) REFERENCES `SHKGroupTable` (`oid`),
  CONSTRAINT `SHKUserGroupTable_userid` FOREIGN KEY (`userid`) REFERENCES `SHKUserTable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKUserGroupTable`
--

LOCK TABLES `SHKUserGroupTable` WRITE;
/*!40000 ALTER TABLE `SHKUserGroupTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKUserGroupTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKUserPackLevelPart`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKUserPackLevelPart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKUserPackLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKUserPackLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKUserPackLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `SHKPackLevelParticipant` (`oid`),
  CONSTRAINT `SHKUserPackLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `SHKNormalUser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKUserPackLevelPart`
--

LOCK TABLES `SHKUserPackLevelPart` WRITE;
/*!40000 ALTER TABLE `SHKUserPackLevelPart` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKUserPackLevelPart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKUserProcLevelParticipant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKUserProcLevelParticipant` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKUserProcLevelParticipant` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKUserProcLevelParticipant_USEROID` (`USEROID`),
  CONSTRAINT `SHKUserProcLevelParticipant_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `SHKProcLevelParticipant` (`oid`),
  CONSTRAINT `SHKUserProcLevelParticipant_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `SHKNormalUser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKUserProcLevelParticipant`
--

LOCK TABLES `SHKUserProcLevelParticipant` WRITE;
/*!40000 ALTER TABLE `SHKUserProcLevelParticipant` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKUserProcLevelParticipant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKUserTable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKUserTable` (
  `userid` varchar(100) NOT NULL,
  `firstname` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `passwd` varchar(50) NOT NULL,
  `email` varchar(254) DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKUserTable` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKUserTable`
--

LOCK TABLES `SHKUserTable` WRITE;
/*!40000 ALTER TABLE `SHKUserTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKUserTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLApplicationPackage`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLApplicationPackage` (
  `PACKAGE_ID` varchar(90) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLApplicationPackage` (`PACKAGE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLApplicationPackage`
--

LOCK TABLES `SHKXPDLApplicationPackage` WRITE;
/*!40000 ALTER TABLE `SHKXPDLApplicationPackage` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKXPDLApplicationPackage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLApplicationProcess`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLApplicationProcess` (
  `PROCESS_ID` varchar(90) NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLApplicationProcess` (`PROCESS_ID`,`PACKAGEOID`),
  KEY `SHKXPDLApplicationProcess_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKXPDLApplicationProcess_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `SHKXPDLApplicationPackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLApplicationProcess`
--

LOCK TABLES `SHKXPDLApplicationProcess` WRITE;
/*!40000 ALTER TABLE `SHKXPDLApplicationProcess` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKXPDLApplicationProcess` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLData`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLData` (
  `XPDLContent` longblob,
  `XPDLClassContent` longblob,
  `XPDL` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLData` (`CNT`),
  UNIQUE KEY `I2_SHKXPDLData` (`XPDL`),
  CONSTRAINT `SHKXPDLData_XPDL` FOREIGN KEY (`XPDL`) REFERENCES `SHKXPDLS` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLData`
--

LOCK TABLES `SHKXPDLData` WRITE;
/*!40000 ALTER TABLE `SHKXPDLData` DISABLE KEYS */;
INSERT INTO `SHKXPDLData` VALUES ('<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Package xmlns=\"http://www.wfmc.org/2002/XPDL1.0\" xmlns:xpdl=\"http://www.wfmc.org/2002/XPDL1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" Id=\"crm\" Name=\"CRM\" xsi:schemaLocation=\"http://www.wfmc.org/2002/XPDL1.0 http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd\">\n    <PackageHeader>\n        <XPDLVersion>1.0</XPDLVersion>\n        <Vendor/>\n        <Created/>\n    </PackageHeader>\n    <Script Type=\"text/javascript\"/>\n    <Participants>\n        <Participant Id=\"requester\" Name=\"Requester\">\n            <ParticipantType Type=\"ROLE\"/>\n        </Participant>\n        <Participant Id=\"approver\" Name=\"Approver\">\n            <ParticipantType Type=\"ROLE\"/>\n        </Participant>\n    </Participants>\n    <Applications>\n        <Application Id=\"default_application\"/>\n    </Applications>\n    <WorkflowProcesses>\n        <WorkflowProcess Id=\"process1\" Name=\"Proposal Approval Process\">\n            <ProcessHeader DurationUnit=\"h\"/>\n            <DataFields>\n                <DataField Id=\"status\" IsArray=\"FALSE\">\n                    <DataType>\n                        <BasicType Type=\"STRING\"/>\n                    </DataType>\n                </DataField>\n            </DataFields>\n            <Activities>\n                <Activity Id=\"approve_proposal\" Name=\"Approve Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Join Type=\"XOR\"/>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"261,21.79547119140625\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"activity1\" Name=\"Resubmit Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"255,21.784095764160156\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"send_proposal\" Name=\"Send Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>requester</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Join Type=\"XOR\"/>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"778.977294921875,21.772727966308594\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"tool1\" Name=\"Email Approved Notification\">\n                    <Implementation>\n                        <Tool Id=\"default_application\"/>\n                    </Implementation>\n                    <Performer>requester</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Join Type=\"XOR\"/>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"622.9886474609375,21.965911865234375\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"tool2\" Name=\"Email Rejected Notification\">\n                    <Implementation>\n                        <Tool Id=\"default_application\"/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"631.977294921875,23.386383056640625\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"route3\">\n                    <Route/>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Split Type=\"XOR\">\n                                <TransitionRefs>\n                                    <TransitionRef Id=\"transition5\"/>\n                                    <TransitionRef Id=\"transition10\"/>\n                                    <TransitionRef Id=\"transition12\"/>\n                                </TransitionRefs>\n                            </Split>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"449,24.79547119140625\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"tool3\" Name=\"Email Resubmit Notification\">\n                    <Implementation>\n                        <Tool Id=\"default_application\"/>\n                    </Implementation>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"403,22\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"tool4\" Name=\"Email Request For Approval\">\n                    <Implementation>\n                        <Tool Id=\"default_application\"/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Join Type=\"XOR\"/>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"116,22\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"route2\">\n                    <Route/>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"128,34\"/>\n                    </ExtendedAttributes>\n                </Activity>\n            </Activities>\n            <Transitions>\n                <Transition From=\"approve_proposal\" Id=\"transition2\" To=\"route3\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"route3\" Id=\"transition5\" To=\"tool2\">\n                    <Condition Type=\"CONDITION\">status === \'Rejected\'</Condition>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                        <ExtendedAttribute Name=\"PBUILDER_TRANSITION_CONDITIONS\" Value=\"[{&quot;join&quot;:&quot;&amp;&amp;&quot;,&quot;variable&quot;:&quot;status&quot;,&quot;operator&quot;:&quot;===&quot;,&quot;value&quot;:&quot;Rejected&quot;}]\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"tool1\" Id=\"transition9\" To=\"send_proposal\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"route3\" Id=\"transition10\" To=\"tool3\">\n                    <Condition Type=\"CONDITION\">status === \'Resubmit\'</Condition>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                        <ExtendedAttribute Name=\"PBUILDER_TRANSITION_CONDITIONS\" Value=\"[{&quot;join&quot;:&quot;&amp;&amp;&quot;,&quot;variable&quot;:&quot;status&quot;,&quot;operator&quot;:&quot;===&quot;,&quot;value&quot;:&quot;Resubmit&quot;}]\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"tool3\" Id=\"transition11\" To=\"activity1\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"route3\" Id=\"transition12\" Name=\"Approved\" To=\"tool1\">\n                    <Condition Type=\"OTHERWISE\"/>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"tool4\" Id=\"transition13\" To=\"approve_proposal\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"activity1\" Id=\"transition8\" To=\"tool4\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"route2\" Id=\"transition14\" To=\"tool4\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n            </Transitions>\n            <ExtendedAttributes>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER\" Value=\"requester;approver\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=send_proposal,X_OFFSET=866,Y_OFFSET=34,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool2,X_OFFSET=792,Y_OFFSET=36,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_START_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route2,X_OFFSET=56,Y_OFFSET=46,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULT\"/>\n            </ExtendedAttributes>\n        </WorkflowProcess>\n    </WorkflowProcesses>\n    <ExtendedAttributes>\n        <ExtendedAttribute Name=\"EDITING_TOOL\" Value=\"Web Workflow Designer\"/>\n        <ExtendedAttribute Name=\"EDITING_TOOL_VERSION\" Value=\"5.0-pre-alpha\"/>\n    </ExtendedAttributes>\n</Package>\n','¨Ì\0sr\0\'org.enhydra.shark.xpdl.elements.Package~+Vm≈Ä~˜\0Z\0isTransientL\0extPkgRefsToIdst\0.Lorg/enhydra/shark/utilities/SequencedHashMap;L\0internalVersiont\0Ljava/lang/String;L\0\nnamespacest\0,Lorg/enhydra/shark/xpdl/elements/Namespaces;xr\0(org.enhydra.shark.xpdl.XMLComplexElement>≤æΩÓ(⁄Ã\0\0xr\05org.enhydra.shark.xpdl.XMLBaseForCollectionAndComplexÂ‡áÓ·ı2\0L\0\nelementMapq\0~\0L\0elementst\0Ljava/util/ArrayList;xr\0!org.enhydra.shark.xpdl.XMLElement#+Bø#˜åº\0Z\0\nisReadOnlyZ\0\nisRequiredL\0nameq\0~\0L\0originalElementHashCodet\0Ljava/lang/Integer;L\0parentt\0#Lorg/enhydra/shark/xpdl/XMLElement;L\0valueq\0~\0xpt\0Packagesr\0java.lang.Integer‚†§˜Åá8\0I\0valuexr\0java.lang.NumberÜ¨ïî‡ã\0\0xp]»ìpt\0\0sr\0,org.enhydra.shark.utilities.SequencedHashMap.Í\"ì©\"&\0\0xpw\0\0\0\rt\0Idsr\0#org.enhydra.shark.xpdl.XMLAttribute#c›Ä˛ÀM;\0L\0choicesq\0~\0xq\0~\0q\0~\0sq\0~\0\'‘∏q\0~\0\nt\0crmpt\0Namesq\0~\0\0q\0~\0sq\0~\0\Zr˘Ëq\0~\0\nt\0CRMpt\0\rPackageHeadersr\0-org.enhydra.shark.xpdl.elements.PackageHeadervÈí,ÌíÔ\0\0xq\0~\0\0q\0~\0sq\0~\0w–{\nq\0~\0\nt\0\0sq\0~\0w\0\0\0t\0XPDLVersionsr\0+org.enhydra.shark.xpdl.elements.XPDLVersion˜\"}◊Y.≈w\0\0xr\0\'org.enhydra.shark.xpdl.XMLSimpleElement¬m›¿éÛ\0\0xq\0~\0q\0~\0!sq\0~\0YÌrq\0~\0t\01.0t\0Vendorsr\0&org.enhydra.shark.xpdl.elements.Vendor t¯ÍE≥:\0\0xq\0~\0#q\0~\0\'sq\0~\0:ıvq\0~\0t\0\0t\0Createdsr\0\'org.enhydra.shark.xpdl.elements.Createdz ÙdKª|[\0\0xq\0~\0#q\0~\0,sq\0~\0C¶R	q\0~\0t\0\0t\0Descriptionsr\0+org.enhydra.shark.xpdl.elements.Description€∞73Ê8˚\0\0xq\0~\0#\0q\0~\01sq\0~\0.V¬ßq\0~\0t\0\0t\0\rDocumentationsr\0-org.enhydra.shark.xpdl.elements.Documentation`ƒ9ª◊yí\0\0xq\0~\0#\0q\0~\06sq\0~\0#‹≈q\0~\0t\0\0t\0PriorityUnitsr\0,org.enhydra.shark.xpdl.elements.PriorityUnitò≥õËõ¯‰Â\0\0xq\0~\0#\0q\0~\0;sq\0~\0vk•Ûq\0~\0t\0\0t\0CostUnitsr\0(org.enhydra.shark.xpdl.elements.CostUnit‹éŸ=H·å\0\0xq\0~\0#\0q\0~\0@sq\0~\0¨ó-q\0~\0t\0\0xsr\0java.util.ArrayListxÅ“ô«aù\0I\0sizexp\0\0\0w\0\0\0q\0~\0$q\0~\0)q\0~\0.q\0~\03q\0~\08q\0~\0=q\0~\0Bxt\0RedefinableHeadersr\01org.enhydra.shark.xpdl.elements.RedefinableHeaderôøMÎœ™\'H\0\0xq\0~\0\0q\0~\0Gsq\0~\0‘≥^q\0~\0\nt\0\0sq\0~\0w\0\0\0t\0PublicationStatussq\0~\0\0q\0~\0Msq\0~\0L°V©q\0~\0It\0\0sq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0UNDER_REVISIONt\0RELEASEDt\0\nUNDER_TESTxt\0Authorsr\0&org.enhydra.shark.xpdl.elements.Author5 ƒf·ßÜ\0\0xq\0~\0#\0q\0~\0Usq\0~\0æ&1q\0~\0It\0\0t\0Versionsr\0\'org.enhydra.shark.xpdl.elements.Version9=3¥~ªJQ\0\0xq\0~\0#\0q\0~\0Zsq\0~\0q\'Tq\0~\0It\0\0t\0Codepagesr\0(org.enhydra.shark.xpdl.elements.Codepage9$m˜eΩ\rG\0\0xq\0~\0#\0q\0~\0_sq\0~\0C¬4q\0~\0It\0\0t\0\nCountrykeysr\0*org.enhydra.shark.xpdl.elements.Countrykeyóã.Å–“—\0\0xq\0~\0#\0q\0~\0dsq\0~\0‰‹q\0~\0It\0\0t\0Responsiblessr\0,org.enhydra.shark.xpdl.elements.Responsibles$ÔÍ{S∆\0\0xr\0$org.enhydra.shark.xpdl.XMLCollectionËèjƒãmÌ∞\0\0xq\0~\0\0q\0~\0isq\0~\0vÿ“Øq\0~\0It\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~\0Nq\0~\0Wq\0~\0\\q\0~\0aq\0~\0fq\0~\0lxt\0ConformanceClasssr\00org.enhydra.shark.xpdl.elements.ConformanceClass◊Ïy0|k´î\0\0xq\0~\0\0q\0~\0rsq\0~\0°õˇq\0~\0\nt\0\0sq\0~\0w\0\0\0t\0GraphConformancesq\0~\0\0q\0~\0xsq\0~\02å\\q\0~\0tq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0FULL_BLOCKEDt\0LOOP_BLOCKEDt\0NON_BLOCKEDxxsq\0~\0E\0\0\0w\0\0\0q\0~\0yxt\0Scriptsr\0&org.enhydra.shark.xpdl.elements.ScriptQ¶jÁS„8\0\0xq\0~\0\0q\0~\0Äsq\0~\0(Ñh›q\0~\0\nt\0\0sq\0~\0w\0\0\0t\0Typesq\0~\0q\0~\0Üsq\0~\0yä»q\0~\0Çt\0text/javascriptpt\0Versionsq\0~\0\0q\0~\0äsq\0~\0úÑ[q\0~\0Çt\0\0pt\0Grammarsq\0~\0\0q\0~\0ésq\0~\0mëq\0~\0Çt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\0áq\0~\0ãq\0~\0èxt\0ExternalPackagessr\00org.enhydra.shark.xpdl.elements.ExternalPackagesw¿\"+≈®®â\0\0xq\0~\0k\0q\0~\0ìsq\0~\0ó\\q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0TypeDeclarationssr\00org.enhydra.shark.xpdl.elements.TypeDeclarations\r· Ox5Õ\0\0xq\0~\0k\0q\0~\0ösq\0~\0ç¨q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Participantssr\0,org.enhydra.shark.xpdl.elements.Participantsh`∑g8J\0\0xq\0~\0k\0q\0~\0°sq\0~\0_wyóq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0+org.enhydra.shark.xpdl.elements.Participanto$îÛrc§è\0\0xr\0+org.enhydra.shark.xpdl.XMLCollectionElementC¢x”vÅr\0\0xq\0~\0t\0Participantsq\0~\0¢‘q\0~\0£t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\06≤E(q\0~\0™t\0	requesterpq\0~\0sq\0~\0\0q\0~\0sq\0~\0gùP£q\0~\0™t\0	Requesterpt\0ParticipantTypesr\0/org.enhydra.shark.xpdl.elements.ParticipantType>àn•›Ö®˜\0\0xq\0~\0q\0~\0µsq\0~\0aõaq\0~\0™t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0Q$hq\0~\0∑t\0ROLEsq\0~\0E\0\0\0w\0\0\0t\0RESOURCE_SETt\0RESOURCEt\0ROLEt\0ORGANIZATIONAL_UNITt\0HUMANt\0SYSTEMxxsq\0~\0E\0\0\0w\0\0\0q\0~\0ªxt\0Descriptionsq\0~\02\0q\0~\0∆sq\0~\0?@jÏq\0~\0™t\0\0t\0ExternalReferencesr\01org.enhydra.shark.xpdl.elements.ExternalReferenceÜbË∑¡Qœ\0\0xq\0~\0\0q\0~\0 sq\0~\02U=Ôq\0~\0™t\0\0sq\0~\0w\0\0\0t\0xrefsq\0~\0\0q\0~\0–sq\0~\0/|Wq\0~\0Ãt\0\0pt\0locationsq\0~\0q\0~\0‘sq\0~\0]\nï2q\0~\0Ãt\0\0pt\0	namespacesq\0~\0\0q\0~\0ÿsq\0~\0a!ìuq\0~\0Ãt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\0—q\0~\0’q\0~\0Ÿxt\0ExtendedAttributessr\02org.enhydra.shark.xpdl.elements.ExtendedAttributesÚ∞Oıü⁄UF\0L\0extAttribsStringq\0~\0xq\0~\0k\0q\0~\0›sq\0~\0z\0p˚q\0~\0™t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~\0Øq\0~\0≤q\0~\0∑q\0~\0«q\0~\0Ãq\0~\0ﬂxsq\0~\0®t\0Participantsq\0~\0?òÂ…q\0~\0£t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Oo\\≥q\0~\0Ât\0approverpq\0~\0sq\0~\0\0q\0~\0sq\0~\0Rœstq\0~\0Ât\0Approverpt\0ParticipantTypesq\0~\0∂q\0~\0sq\0~\0+1V~q\0~\0Ât\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0Õ5q\0~\0Òt\0ROLEsq\0~\0E\0\0\0w\0\0\0q\0~\0øq\0~\0¿q\0~\0¡q\0~\0¬q\0~\0√q\0~\0ƒxxsq\0~\0E\0\0\0w\0\0\0q\0~\0ıxt\0Descriptionsq\0~\02\0q\0~\0˙sq\0~\0w¿«∞q\0~\0Ât\0\0t\0ExternalReferencesq\0~\0À\0q\0~\0˛sq\0~\0^Ω^Ôq\0~\0Ât\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0ûa‡q\0~\0ˇt\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0\0lCq\0~\0ˇt\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0w∆tq\0~\0ˇt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~q\0~	xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\rsq\0~\0Cr§\\q\0~\0Ât\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~\0Íq\0~\0Ìq\0~\0Òq\0~\0˚q\0~\0ˇq\0~xxt\0Applicationssr\0,org.enhydra.shark.xpdl.elements.Applications¬Ä¥ﬁˆº\0\0xq\0~\0k\0q\0~sq\0~\0w|]∆q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0+org.enhydra.shark.xpdl.elements.Applicationv	¢R˘˛S\0\0xq\0~\0©t\0Applicationsq\0~\0˘‘q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0A”ë∫q\0~t\0default_applicationpq\0~\0sq\0~\0\0q\0~\0sq\0~\0^ªjËq\0~t\0\0pt\0Descriptionsq\0~\02\0q\0~\'sq\0~\0L ˚Uq\0~t\0\0t\0Choicesr\00org.enhydra.shark.xpdl.elements.ApplicationTypesè?˚!≠Æ¶\0\0xr\0\'org.enhydra.shark.xpdl.XMLComplexChoice§|±∫\"Ù˙\0L\0choicesq\0~\0L\0choosenq\0~\0	xq\0~\0q\0~+sq\0~\0h¥mq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sr\00org.enhydra.shark.xpdl.elements.FormalParametersp∫ƒB«√ÅZ\0\0xq\0~\0k\0t\0FormalParameterssq\0~\0>ªwq\0~.t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~\0À\0t\0ExternalReferencesq\0~\0U\"Z”q\0~.t\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0Èqq\0~9t\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0cœóq\0~9t\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0Q}q\0~9t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~>q\0~Aq\0~Dxxq\0~3t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Hsq\0~\0_bHq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~!q\0~$q\0~(q\0~.q\0~Ixxt\0\nDataFieldssr\0*org.enhydra.shark.xpdl.elements.DataFieldsΩµí ¶‘˙U\0\0xq\0~\0k\0q\0~Osq\0~\0>O}Nq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0WorkflowProcessessr\01org.enhydra.shark.xpdl.elements.WorkflowProcessespé_¢0,\0\0xq\0~\0k\0q\0~Vsq\0~\0DòfZq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0/org.enhydra.shark.xpdl.elements.WorkflowProcess%·v0“◊L\0\0xq\0~\0©t\0WorkflowProcesssq\0~\00DøÎq\0~Xt\0\0sq\0~\0w\0\0\0\rq\0~\0sq\0~\0q\0~\0sq\0~\0Á>q\0~^t\0process1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0k”ÃGq\0~^t\0Proposal Approval Processpt\0AccessLevelsq\0~\0\0q\0~isq\0~\01ØÑmq\0~^q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0PUBLICt\0PRIVATExt\0\rProcessHeadersr\0-org.enhydra.shark.xpdl.elements.ProcessHeaderåLÆÉC-)ø\0\0xq\0~\0q\0~osq\0~\00·q\0~^t\0\0sq\0~\0w\0\0\0t\0DurationUnitsq\0~\0\0q\0~usq\0~\0û_kq\0~qt\0hsq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0Yt\0Mt\0Dt\0ht\0mt\0sxt\0Createdsq\0~\0-\0q\0~Äsq\0~\0lZÎq\0~qt\0\0t\0Descriptionsq\0~\02\0q\0~Ñsq\0~\0!t·q\0~qt\0\0t\0Prioritysr\0(org.enhydra.shark.xpdl.elements.Priority`ˆNn>b\0\0xq\0~\0#\0q\0~àsq\0~\0-∞M)q\0~qt\0\0t\0Limitsr\0%org.enhydra.shark.xpdl.elements.Limit¸¢Ë1ò”ó\0\0xq\0~\0#\0q\0~çsq\0~\0F>*Ûq\0~qt\0\0t\0	ValidFromsr\0)org.enhydra.shark.xpdl.elements.ValidFromc≈Ö|ÉL<\0\0xq\0~\0#\0q\0~ísq\0~\0zÊûq\0~qt\0\0t\0ValidTosr\0\'org.enhydra.shark.xpdl.elements.ValidToû¡∂´M…\0\0xq\0~\0#\0q\0~ósq\0~\0\0—)Aq\0~qt\0\0t\0TimeEstimationsr\0.org.enhydra.shark.xpdl.elements.TimeEstimation≈Ä£\'3\0\0xq\0~\0\0q\0~úsq\0~\0/ßıwq\0~qt\0\0sq\0~\0w\0\0\0t\0WaitingTimesr\0+org.enhydra.shark.xpdl.elements.WaitingTimeNì‰Œ†/\0\0xq\0~\0#\0q\0~¢sq\0~\0˘;¬q\0~ût\0\0t\0WorkingTimesr\0+org.enhydra.shark.xpdl.elements.WorkingTimeæ~±ÉÊ◊\0\0xq\0~\0#\0q\0~ßsq\0~\0]Uq\0~ût\0\0t\0Durationsr\0(org.enhydra.shark.xpdl.elements.Duration© ÙCÙÎË\0\0xq\0~\0#\0q\0~¨sq\0~\0T\r¨ñq\0~ût\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~§q\0~©q\0~Æxxsq\0~\0E\0\0\0w\0\0\0q\0~vq\0~Åq\0~Öq\0~äq\0~èq\0~îq\0~ôq\0~ûxt\0RedefinableHeadersq\0~\0H\0q\0~≥sq\0~\0Dë˜-q\0~^t\0\0sq\0~\0w\0\0\0q\0~\0Msq\0~\0\0q\0~\0Msq\0~\0ÁÛVq\0~¥q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\0Rq\0~\0Sq\0~\0Txt\0Authorsq\0~\0V\0q\0~ªsq\0~\00∑±\nq\0~¥t\0\0t\0Versionsq\0~\0[\0q\0~øsq\0~\0	g“ñq\0~¥t\0\0t\0Codepagesq\0~\0`\0q\0~√sq\0~\0BÑdq\0~¥t\0\0t\0\nCountrykeysq\0~\0e\0q\0~«sq\0~\0h”BMq\0~¥t\0\0t\0Responsiblessq\0~\0j\0q\0~Àsq\0~\0e·Åºq\0~¥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~∏q\0~ºq\0~¿q\0~ƒq\0~»q\0~Ãxt\0FormalParameterssq\0~2\0q\0~“sq\0~\0R%Ùq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0\nDataFieldssq\0~P\0q\0~ÿsq\0~\03`ñúq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0)org.enhydra.shark.xpdl.elements.DataFieldI“3.~ë≠˜\0\0xq\0~\0©t\0	DataFieldsq\0~\0n ;q\0~Ÿt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\01¶îq\0~ﬂt\0statuspq\0~\0sq\0~\0\0q\0~\0sq\0~\0ûÆMq\0~ﬂt\0\0pt\0IsArraysq\0~\0\0q\0~Ísq\0~\0W4BÚq\0~ﬂt\0FALSEsq\0~\0E\0\0\0w\0\0\0t\0TRUEt\0FALSExt\0DataTypesr\0(org.enhydra.shark.xpdl.elements.DataTypeÜ\'4sM\0\0xq\0~\0q\0~Òsq\0~\0QÉ˚»q\0~ﬂt\0\0sq\0~\0w\0\0\0t\0	DataTypessr\0)org.enhydra.shark.xpdl.elements.DataTypesµpcH,ﬁ!ì\0Z\0\risInitializedxq\0~-q\0~˜sq\0~\0oøÄ⁄q\0~Ût\0\0sq\0~\0E\0\0\0	w\0\0\0	sr\0)org.enhydra.shark.xpdl.elements.BasicType∑)®Ωw1øé\0\0xq\0~\0t\0	BasicTypesq\0~\0DYù≠q\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0TÁπq\0~˛t\0STRINGsq\0~\0E\0\0\0w\0\0\0t\0STRINGt\0FLOATt\0INTEGERt\0	REFERENCEt\0DATETIMEt\0BOOLEANt\0	PERFORMERxxsq\0~\0E\0\0\0w\0\0\0q\0~xsr\0,org.enhydra.shark.xpdl.elements.DeclaredTypedR.\\^Ë9í\0\0xq\0~\0t\0DeclaredTypesq\0~\0\ZtU‡q\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0	ﬁØ$q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~xsr\0*org.enhydra.shark.xpdl.elements.SchemaType&1oSH™Ö\0\0xq\0~\0t\0\nSchemaTypesq\0~\0bˇGq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~\0Àt\0ExternalReferencesq\0~\0h$•q\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0P«íq\0~ t\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0,†ßq\0~ t\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0:Í§q\0~ t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~%q\0~(q\0~+xsr\0*org.enhydra.shark.xpdl.elements.RecordTypeı%®Ω©ÎK\0\0\0xq\0~\0kt\0\nRecordTypesq\0~\0rÛÌjq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.UnionType¥⁄Ô5PŒG˙\0\0xq\0~\0kt\0	UnionTypesq\0~\0Hñq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0/org.enhydra.shark.xpdl.elements.EnumerationType™⁄ﬁf3b\0\0xq\0~\0kt\0EnumerationTypesq\0~\0W‰q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.ArrayTypeg¨$\0≠N@\0\0xq\0~\0t\0	ArrayTypesq\0~\0zÚìiq\0~˘t\0\0sq\0~\0w\0\0\0t\0\nLowerIndexsq\0~\0q\0~Jsq\0~\0©‡q\0~Et\0\0pt\0\nUpperIndexsq\0~\0q\0~Nsq\0~\0M•Eq\0~Et\0\0pq\0~˜sq\0~¯q\0~˜sq\0~\0Zç◊pq\0~Et\0\0ppxsq\0~\0E\0\0\0w\0\0\0q\0~Kq\0~Oq\0~Rxsr\0(org.enhydra.shark.xpdl.elements.ListTypeü\"”ü\nÆ\0\0xq\0~\0t\0ListTypesq\0~\04änìq\0~˘t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯q\0~˜sq\0~\0hâKq\0~Wt\0\0ppxsq\0~\0E\0\0\0w\0\0\0q\0~\\xxq\0~˛xsq\0~\0E\0\0\0w\0\0\0q\0~˘xt\0InitialValuesr\0,org.enhydra.shark.xpdl.elements.InitialValuej,z˚îúR\0\0xq\0~\0#\0q\0~asq\0~\0yÚwq\0~ﬂt\0\0t\0Lengthsr\0&org.enhydra.shark.xpdl.elements.LengthMW+-Ã©W‹\0\0xq\0~\0#\0q\0~fsq\0~\0M\0úpq\0~ﬂt\0\0t\0Descriptionsq\0~\02\0q\0~ksq\0~\0*ôùµq\0~ﬂt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~osq\0~\0pak|q\0~ﬂt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~‰q\0~Áq\0~Îq\0~Ûq\0~cq\0~hq\0~lq\0~pxxt\0Participantssq\0~\0¢\0q\0~vsq\0~\0A”{óq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Applicationssq\0~\0q\0~|sq\0~\0(÷˛pq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ActivitySetssr\0,org.enhydra.shark.xpdl.elements.ActivitySets–qV[4™Ó÷\0\0xq\0~\0k\0q\0~Çsq\0~\0Rn2q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0\nActivitiessr\0*org.enhydra.shark.xpdl.elements.Activities&G^·æl˛P\0\0xq\0~\0k\0q\0~âsq\0~\0eç≠ïq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0	w\0\0\0	sr\0(org.enhydra.shark.xpdl.elements.Activity√tá45\Z9ï\0\0xq\0~\0©t\0Activitysq\0~\0ﬂ˝xq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0(Ã—Vq\0~ët\0approve_proposalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0pêßq\0~ët\0Approve Proposalpt\0Descriptionsq\0~\02\0q\0~úsq\0~\0\'µzàq\0~ët\0\0t\0Limitsq\0~é\0q\0~†sq\0~\0wñq\0~ët\0\0q\0~\0Üsr\0-org.enhydra.shark.xpdl.elements.ActivityTypese≈Ω{ˆûËË\0\0xq\0~-q\0~\0Üsq\0~\0dVnSq\0~ët\0\0sq\0~\0E\0\0\0w\0\0\0sr\0%org.enhydra.shark.xpdl.elements.Route0eÓ\r≤G›\0\0xq\0~\0t\0Routesq\0~\0a&/÷q\0~•t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0.org.enhydra.shark.xpdl.elements.Implementationúrñí^%ä\0\0xq\0~\0t\0Implementationsq\0~\0mÀx@q\0~•t\0\0sq\0~\0w\0\0\0q\0~\0Üsr\03org.enhydra.shark.xpdl.elements.ImplementationTypes\rü‰TŸ°9\0\0xq\0~-q\0~\0Üsq\0~\0z‚®q\0~±t\0\0sq\0~\0E\0\0\0w\0\0\0sr\0\"org.enhydra.shark.xpdl.elements.No{ïÖ⁄.\0\0xq\0~\0t\0Nosq\0~\0-ë¡Qq\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0%org.enhydra.shark.xpdl.elements.ToolsCø÷g©ë\0\0xq\0~\0k\0t\0Toolssq\0~\0}ô™q\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0\'org.enhydra.shark.xpdl.elements.SubFlow;Oısá7:$\0\0xq\0~\0t\0SubFlowsq\0~\0K&}!q\0~∑t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0!K?±q\0~ t\0\0pt\0	Executionsq\0~\0\0q\0~“sq\0~\0s˘Äq\0~ q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ASYNCHRt\0SYNCHRxt\0ActualParameterssr\00org.enhydra.shark.xpdl.elements.ActualParametersÎå˜µ_ÏK˛\0\0xq\0~\0k\0q\0~ÿsq\0~\07B.7q\0~ t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~œq\0~”q\0~⁄xxq\0~ºxsq\0~\0E\0\0\0w\0\0\0q\0~∑xsr\0-org.enhydra.shark.xpdl.elements.BlockActivityíq cƒÍF\0\0xq\0~\0t\0\rBlockActivitysq\0~\0OuÇãq\0~•t\0\0sq\0~\0w\0\0\0t\0BlockIdsq\0~\0q\0~Ásq\0~\07;äcq\0~‚t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~Ëxxq\0~±t\0	Performersr\0)org.enhydra.shark.xpdl.elements.Performer¶\"1%ËÃƒ\0\0xq\0~\0#\0q\0~Ïsq\0~\0*⁄˝q\0~ët\0approvert\0	StartModesr\0)org.enhydra.shark.xpdl.elements.StartModenhÖÑ◊ÛS\0\0xq\0~\0\0q\0~Òsq\0~\0åzq\0~ët\0\0sq\0~\0w\0\0\0t\0Modesr\00org.enhydra.shark.xpdl.elements.StartFinishModes~Á6zÂXã\'\0\0xq\0~-\0q\0~˜sq\0~\0o–ﬂq\0~Ût\0\0sq\0~\0E\0\0\0w\0\0\0sr\0,org.enhydra.shark.xpdl.XMLEmptyChoiceElementÔ2‘;Œ3ı_\0\0xq\0~\0\0t\0XMLEmptyChoiceElementsq\0~\0∏q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.AutomaticÈt?˚_Äê\0\0xq\0~\0t\0	Automaticsq\0~\0{[÷Mq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0&org.enhydra.shark.xpdl.elements.Manual∑v™âÓ[ÿ§\0\0xq\0~\0t\0Manualsq\0~\0%~Òtq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~˛xsq\0~\0E\0\0\0w\0\0\0q\0~˘xt\0\nFinishModesr\0*org.enhydra.shark.xpdl.elements.FinishModeˆÉ¯õúºÛ{\0\0xq\0~\0\0q\0~sq\0~\0\nç`\Zq\0~ët\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\01sÙq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0		5,q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0`qX´q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0ftåq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Prioritysq\0~â\0q\0~0sq\0~\0`∂rˇq\0~ët\0\0t\0	Deadlinessr\0)org.enhydra.shark.xpdl.elements.Deadlines>Ωç…ú√⁄\0\0xq\0~\0k\0q\0~4sq\0~\0 Vãêq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsr\05org.enhydra.shark.xpdl.elements.SimulationInformation\"ì|I™£í\0\0xq\0~\0\0q\0~;sq\0~\0J;1q\0~ët\0\0sq\0~\0w\0\0\0t\0\rInstantiationsq\0~\0\0q\0~Asq\0~\0ﬁÇ√q\0~=q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ONCEt\0MULTIPLExt\0Costsr\0$org.enhydra.shark.xpdl.elements.Costœ˘¿ﬁÚëû\0\0xq\0~\0#q\0~Gsq\0~\0[,(≠q\0~=t\0\0t\0TimeEstimationsq\0~ùq\0~Lsq\0~\0O≤2Sq\0~=t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Qsq\0~\0DËY\'q\0~Mt\0\0t\0WorkingTimesq\0~®\0q\0~Usq\0~\08‹„q\0~Mt\0\0t\0Durationsq\0~≠\0q\0~Ysq\0~\0=q\0~Mt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Rq\0~Vq\0~Zxxsq\0~\0E\0\0\0w\0\0\0q\0~Bq\0~Iq\0~Mxt\0Iconsr\0$org.enhydra.shark.xpdl.elements.Icon´TŒU(ˇ}6\0\0xq\0~\0#\0q\0~_sq\0~\0\\Òß≠q\0~ët\0\0t\0\rDocumentationsq\0~\07\0q\0~dsq\0~\0ZVÏuq\0~ët\0\0t\0TransitionRestrictionssr\06org.enhydra.shark.xpdl.elements.TransitionRestrictionsC)∑◊Äi;\0\0xq\0~\0k\0q\0~hsq\0~\0Õø◊q\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\05org.enhydra.shark.xpdl.elements.TransitionRestrictionN˚ƒáπ}˛\0\0xq\0~\0t\0TransitionRestrictionsq\0~\0Q*q\0~jt\0\0sq\0~\0w\0\0\0t\0Joinsr\0$org.enhydra.shark.xpdl.elements.Join⁄ï”©x)Û5\0\0xq\0~\0\0q\0~usq\0~\0}ºÇ^q\0~pt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0Xbñæq\0~wt\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ANDt\0XORxxsq\0~\0E\0\0\0w\0\0\0q\0~{xt\0Splitsr\0%org.enhydra.shark.xpdl.elements.Split‹ÌØ~—ØWS\0\0xq\0~\0\0q\0~Çsq\0~\0s;q\0~pt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0Ùq\0~Ñq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssr\0.org.enhydra.shark.xpdl.elements.TransitionRefs†Ô≥—ä·ƒÎ\0\0xq\0~\0k\0q\0~ãsq\0~\0\n≥À∏q\0~Ñt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~àq\0~çxxsq\0~\0E\0\0\0w\0\0\0q\0~wq\0~Ñxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~îsq\0~\0y—Õq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\01org.enhydra.shark.xpdl.elements.ExtendedAttributeª¬\\±ÏF\0\0xq\0~\0t\0ExtendedAttributesq\0~\0aq\0~ït\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0<6!q\0~õt\0JaWE_GRAPH_PARTICIPANT_IDpt\0Valuesq\0~\0\0q\0~£sq\0~\0Nπq\0~õt\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~†q\0~§xsq\0~öt\0ExtendedAttributesq\0~\0WXÍ¥q\0~ït\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\06©Xbq\0~®t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0C—˝®q\0~®t\0261,21.79547119140625pxsq\0~\0E\0\0\0w\0\0\0q\0~≠q\0~∞xxpxsq\0~\0E\0\0\0w\0\0\0q\0~ñq\0~ôq\0~ùq\0~°q\0~•q\0~Óq\0~Ûq\0~q\0~1q\0~6q\0~=q\0~aq\0~eq\0~jq\0~ïxsq\0~êt\0Activitysq\0~\0 ¥2åq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Gﬂv¡q\0~µt\0	activity1pq\0~\0sq\0~\0\0q\0~\0sq\0~\00©adq\0~µt\0Resubmit Proposalpt\0Descriptionsq\0~\02\0q\0~¿sq\0~\0L?U[q\0~µt\0\0t\0Limitsq\0~é\0q\0~ƒsq\0~\0\0œ›…q\0~µt\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0Óøq\0~µt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0z∑Lq\0~»t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0Lî[ñq\0~»t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\02KˆXq\0~“t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0F©)–q\0~◊t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0æ\ZÔq\0~◊t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0hòπEq\0~◊t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0{8q\0~Át\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0\nd5‡q\0~Áq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Úsq\0~\0|˚/!q\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Ïq\0~Ôq\0~Ûxxq\0~€xsq\0~\0E\0\0\0w\0\0\0q\0~◊xsq\0~·t\0\rBlockActivitysq\0~\0Q„jq\0~»t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\07∑ó…q\0~˙t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~ˇxxq\0~“t\0	Performersq\0~Ì\0q\0~sq\0~\0◊Íq\0~µt\0	requestert\0	StartModesq\0~Ú\0q\0~sq\0~\09Ÿvq\0~µt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0Obëq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0X\0¶q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0Apf\\q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\03ŸÚq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~#sq\0~\0\\\Z?ﬂq\0~µt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0Lñ q\0~$t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0f∂€‹q\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0^¥®]q\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0RƒvIq\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~,xsq\0~\0E\0\0\0w\0\0\0q\0~(xt\0Prioritysq\0~â\0q\0~?sq\0~\0\ZŒ¥q\0~µt\0\0t\0	Deadlinessq\0~5\0q\0~Csq\0~\0J.ûq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Isq\0~\01#!Iq\0~µt\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0@§\Zq\0~Jq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Qsq\0~\0lkz‘q\0~Jt\0\0t\0TimeEstimationsq\0~ùq\0~Usq\0~\0RãØ^q\0~Jt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Zsq\0~\0z\nı/q\0~Vt\0\0t\0WorkingTimesq\0~®\0q\0~^sq\0~\05	€q\0~Vt\0\0t\0Durationsq\0~≠\0q\0~bsq\0~\0.?mÛq\0~Vt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~[q\0~_q\0~cxxsq\0~\0E\0\0\0w\0\0\0q\0~Nq\0~Rq\0~Vxt\0Iconsq\0~`\0q\0~hsq\0~\0!A÷5q\0~µt\0\0t\0\rDocumentationsq\0~\07\0q\0~lsq\0~\0œ¸øq\0~µt\0\0t\0TransitionRestrictionssq\0~i\0q\0~psq\0~\0=W,q\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~vsq\0~\0h\0Âäq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0/áÒäq\0~wt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0%Átºq\0~|t\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0$∂@q\0~|t\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~Åq\0~Ñxsq\0~öt\0ExtendedAttributesq\0~\0\nAˆÎq\0~wt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\Z⁄]àq\0~àt\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0Üò˚q\0~àt\0255,21.784095764160156pxsq\0~\0E\0\0\0w\0\0\0q\0~çq\0~êxxpxsq\0~\0E\0\0\0w\0\0\0q\0~∫q\0~Ωq\0~¡q\0~≈q\0~»q\0~q\0~q\0~$q\0~@q\0~Dq\0~Jq\0~iq\0~mq\0~qq\0~wxsq\0~êt\0Activitysq\0~\0∆¿7q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0^;/q\0~ït\0\rsend_proposalpq\0~\0sq\0~\0\0q\0~\0sq\0~\05äjq\0~ït\0\rSend Proposalpt\0Descriptionsq\0~\02\0q\0~†sq\0~\0]K≥Yq\0~ït\0\0t\0Limitsq\0~é\0q\0~§sq\0~\0m∫©Kq\0~ït\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0K€≥q\0~ït\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0rÑæxq\0~®t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0r’\n\nq\0~®t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0JùÍq\0~≤t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0Yàôâq\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0(ãæq\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0TwU+q\0~∑t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0vÁ®\Zq\0~«t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0k>#ûq\0~«q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~“sq\0~\0! <q\0~«t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Ãq\0~œq\0~”xxq\0~ªxsq\0~\0E\0\0\0w\0\0\0q\0~∑xsq\0~·t\0\rBlockActivitysq\0~\0O±˜:q\0~®t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0›!Æq\0~⁄t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~ﬂxxq\0~≤t\0	Performersq\0~Ì\0q\0~„sq\0~\0e¿xq\0~ït\0	requestert\0	StartModesq\0~Ú\0q\0~Ásq\0~\06MHq\0~ït\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0vÖ*úq\0~Ët\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0*iƒ0q\0~Ït\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0t¢y„q\0~Ït\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0)º{q\0~Ït\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~Ïxt\0\nFinishModesq\0~\0q\0~sq\0~\0b∂Qq\0~ït\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0öQ q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0iBÂsq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0\n⁄Fˇq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\09ê⁄q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Prioritysq\0~â\0q\0~sq\0~\0)\0;zq\0~ït\0\0t\0	Deadlinessq\0~5\0q\0~#sq\0~\0W*Tq\0~ït\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~)sq\0~\0¢s˜q\0~ït\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\06mXq\0~*q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~1sq\0~\0ûnYq\0~*t\0\0t\0TimeEstimationsq\0~ùq\0~5sq\0~\0p5¢Tq\0~*t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~:sq\0~\0lJnq\0~6t\0\0t\0WorkingTimesq\0~®\0q\0~>sq\0~\00Âäôq\0~6t\0\0t\0Durationsq\0~≠\0q\0~Bsq\0~\0]‰÷zq\0~6t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~;q\0~?q\0~Cxxsq\0~\0E\0\0\0w\0\0\0q\0~.q\0~2q\0~6xt\0Iconsq\0~`\0q\0~Hsq\0~\0\0û¯êq\0~ït\0\0t\0\rDocumentationsq\0~\07\0q\0~Lsq\0~\0NóÖ◊q\0~ït\0\0t\0TransitionRestrictionssq\0~i\0q\0~Psq\0~\0R∫ﬁq\0~ït\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0´Pq\0~Qt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~[sq\0~\0a\nB°q\0~Vt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0D\\£q\0~\\t\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~`xt\0Splitsq\0~É\0q\0~esq\0~\0W†.îq\0~Vt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0–∑◊q\0~fq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~msq\0~\0?¸Û¶q\0~ft\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~jq\0~nxxsq\0~\0E\0\0\0w\0\0\0q\0~\\q\0~fxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~usq\0~\0)ñ4ıq\0~ït\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0-ã[q\0~vt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Jëq\0~{t\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0P®!´q\0~{t\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~Äq\0~Éxsq\0~öt\0ExtendedAttributesq\0~\0=TÍ¬q\0~vt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0E[…`q\0~át\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0\Zj#÷q\0~át\0#778.977294921875,21.772727966308594pxsq\0~\0E\0\0\0w\0\0\0q\0~åq\0~èxxpxsq\0~\0E\0\0\0w\0\0\0q\0~öq\0~ùq\0~°q\0~•q\0~®q\0~‰q\0~Ëq\0~q\0~ q\0~$q\0~*q\0~Iq\0~Mq\0~Qq\0~vxsq\0~êt\0Activitysq\0~\0^¸Iq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Wd«‰q\0~ît\0tool1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0∆\Zq\0~ît\0Email Approved Notificationpt\0Descriptionsq\0~\02\0q\0~üsq\0~\0\nMŒãq\0~ît\0\0t\0Limitsq\0~é\0q\0~£sq\0~\0Ar8áq\0~ît\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0=≤(Tq\0~ît\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0˝î=q\0~ßt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0x (øq\0~ßt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0(∆ë<q\0~±t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0\ZÏ¯q\0~∂t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0t6Ã:q\0~∂t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0$org.enhydra.shark.xpdl.elements.Tool\\6ñ&∂+GÅ\0\0xq\0~\0©t\0Toolsq\0~\0œ>q\0~¿t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0„?≥q\0~«t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0\'¬+q\0~«q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0APPLICATIONt\0	PROCEDURExt\0ActualParameterssq\0~Ÿ\0q\0~‘sq\0~\0ÚÒœq\0~«t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~⁄sq\0~\0Ps”q\0~«t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~ﬁsq\0~\00 “q\0~«t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~Ãq\0~œq\0~’q\0~€q\0~ﬂxxsq\0~…t\0SubFlowsq\0~\0\r*w”q\0~∂t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0yH;q\0~Ât\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0B◊¿q\0~Âq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~sq\0~\0\0BHºq\0~Ât\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Íq\0~Ìq\0~Òxxq\0~¿xsq\0~\0E\0\0\0w\0\0\0q\0~∂xsq\0~·t\0\rBlockActivitysq\0~\0^à3àq\0~ßt\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0kêÚq\0~¯t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~˝xxq\0~±t\0	Performersq\0~Ì\0q\0~sq\0~\0íÑq\0~ît\0	requestert\0	StartModesq\0~Ú\0q\0~sq\0~\0m\r\"sq\0~ît\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0rX)q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0TÚ®q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0b»Ì¬q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0Vˆ2ôq\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~\nxt\0\nFinishModesq\0~\0q\0~!sq\0~\035)Úq\0~ît\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0(\"éq\0~\"t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0HRIäq\0~&t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0O	{¬q\0~&t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0R£q\0~&t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~*xsq\0~\0E\0\0\0w\0\0\0q\0~&xt\0Prioritysq\0~â\0q\0~=sq\0~\0SÖT‡q\0~ît\0\0t\0	Deadlinessq\0~5\0q\0~Asq\0~\0	¡Yq\0~ît\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Gsq\0~\0\r‰íq\0~ît\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0N¥<q\0~Hq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Osq\0~\0b¡·q\0~Ht\0\0t\0TimeEstimationsq\0~ùq\0~Ssq\0~\0aÖ·q\0~Ht\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Xsq\0~\0{≥À$q\0~Tt\0\0t\0WorkingTimesq\0~®\0q\0~\\sq\0~\02o:q\0~Tt\0\0t\0Durationsq\0~≠\0q\0~`sq\0~\0Y‰Æ¥q\0~Tt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Yq\0~]q\0~axxsq\0~\0E\0\0\0w\0\0\0q\0~Lq\0~Pq\0~Txt\0Iconsq\0~`\0q\0~fsq\0~\0DI‰q\0~ît\0\0t\0\rDocumentationsq\0~\07\0q\0~jsq\0~\0!∆¢q\0~ît\0\0t\0TransitionRestrictionssq\0~i\0q\0~nsq\0~\0\'ã·q\0~ît\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0)œ1dq\0~ot\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~ysq\0~\0A`∞\"q\0~tt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0E¡<\Zq\0~zt\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~~xt\0Splitsq\0~É\0q\0~Ésq\0~\0‚È.q\0~tt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0@vÆGq\0~Ñq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~ãsq\0~\0”ÁÀq\0~Ñt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~àq\0~åxxsq\0~\0E\0\0\0w\0\0\0q\0~zq\0~Ñxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~ìsq\0~\0[fjTq\0~ît\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0!œq\0~ît\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0G\"ãq\0~ôt\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0G≠q\0~ôt\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~ûq\0~°xsq\0~öt\0ExtendedAttributesq\0~\0añ”q\0~ît\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0nÙ\\“q\0~•t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0OÜx˝q\0~•t\0$622.9886474609375,21.965911865234375pxsq\0~\0E\0\0\0w\0\0\0q\0~™q\0~≠xxpxsq\0~\0E\0\0\0w\0\0\0q\0~ôq\0~úq\0~†q\0~§q\0~ßq\0~q\0~q\0~\"q\0~>q\0~Bq\0~Hq\0~gq\0~kq\0~oq\0~îxsq\0~êt\0Activitysq\0~\0Fú⁄q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\0∫˛q\0~≤t\0tool2pq\0~\0sq\0~\0\0q\0~\0sq\0~\0Ióãq\0~≤t\0Email Rejected Notificationpt\0Descriptionsq\0~\02\0q\0~Ωsq\0~\0I“ßq\0~≤t\0\0t\0Limitsq\0~é\0q\0~¡sq\0~\0ytcq\0~≤t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0U¯‡€q\0~≤t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0$≤ú¢q\0~≈t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0o%Æyq\0~≈t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\06ì◊Âq\0~œt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0$ˇ‘q\0~‘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0ÿeq\0~‘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~∆t\0Toolsq\0~\0\Zã%q\0~ﬁt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0 Bôq\0~‰t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0|ﬁ°uq\0~‰q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~“q\0~”xt\0ActualParameterssq\0~Ÿ\0q\0~Ôsq\0~\0%Îãq\0~‰t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~ısq\0~\0IÍg±q\0~‰t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~˘sq\0~\0|ù\nq\0~‰t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~Èq\0~Ïq\0~q\0~ˆq\0~˙xxsq\0~…t\0SubFlowsq\0~\0V˙˘q\0~‘t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0~3\"q\0~\0t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0\"Jêq\0~\0q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~sq\0~\0púeGq\0~\0t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~q\0~xxq\0~ﬁxsq\0~\0E\0\0\0w\0\0\0q\0~‘xsq\0~·t\0\rBlockActivitysq\0~\0AˆÇüq\0~≈t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0~ÿ(†q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~xxq\0~œt\0	Performersq\0~Ì\0q\0~sq\0~\0v¨!q\0~≤t\0approvert\0	StartModesq\0~Ú\0q\0~ sq\0~\0e4Y˘q\0~≤t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0£Õ∫q\0~!t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0v\Z{q\0~%t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\09Çπq\0~%t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\08Sg˘q\0~%t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~)xsq\0~\0E\0\0\0w\0\0\0q\0~%xt\0\nFinishModesq\0~\0q\0~<sq\0~\0¬q\0~≤t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\00dÿq\0~=t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0T™‰Ÿq\0~At\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0Tºíq\0~At\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0	;¯Èq\0~At\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~Exsq\0~\0E\0\0\0w\0\0\0q\0~Axt\0Prioritysq\0~â\0q\0~Xsq\0~\0U∑{Ÿq\0~≤t\0\0t\0	Deadlinessq\0~5\0q\0~\\sq\0~\0V∏—öq\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~bsq\0~\0bD\rQq\0~≤t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\04nM@q\0~cq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~jsq\0~\0ZÈÅßq\0~ct\0\0t\0TimeEstimationsq\0~ùq\0~nsq\0~\0JÕøaq\0~ct\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~ssq\0~\0°Ïq\0~ot\0\0t\0WorkingTimesq\0~®\0q\0~wsq\0~\0NcUıq\0~ot\0\0t\0Durationsq\0~≠\0q\0~{sq\0~\0Xë\'q\0~ot\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~tq\0~xq\0~|xxsq\0~\0E\0\0\0w\0\0\0q\0~gq\0~kq\0~oxt\0Iconsq\0~`\0q\0~Åsq\0~\0Äã„q\0~≤t\0\0t\0\rDocumentationsq\0~\07\0q\0~Ösq\0~\0ŒßÇq\0~≤t\0\0t\0TransitionRestrictionssq\0~i\0q\0~âsq\0~\0+÷âsq\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~èsq\0~\0{ûΩq\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0x¥Ôq\0~êt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0≤Xq\0~ït\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0/¿,‰q\0~ït\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~öq\0~ùxsq\0~öt\0ExtendedAttributesq\0~\0f)(q\0~êt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\09:óæq\0~°t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\01™Mq\0~°t\0#631.977294921875,23.386383056640625pxsq\0~\0E\0\0\0w\0\0\0q\0~¶q\0~©xxpxsq\0~\0E\0\0\0w\0\0\0q\0~∑q\0~∫q\0~æq\0~¬q\0~≈q\0~q\0~!q\0~=q\0~Yq\0~]q\0~cq\0~Çq\0~Üq\0~äq\0~êxsq\0~êt\0Activitysq\0~\0#‡…q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\"Íq\0~Æt\0route3pq\0~\0sq\0~\0\0q\0~\0sq\0~\00e£q\0~Æt\0\0pt\0Descriptionsq\0~\02\0q\0~πsq\0~\0]èÙvq\0~Æt\0\0t\0Limitsq\0~é\0q\0~Ωsq\0~\0>.–>q\0~Æt\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0>Zöq\0~Æt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0&„ßÚq\0~¡t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0ÌÀq\0~¡t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0L\nDüq\0~Àt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0(±q\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0Vˆ›q\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0yâjÄq\0~–t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0gπ˜íq\0~‡t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0Gh‚q\0~‡q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Îsq\0~\0c,-q\0~‡t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Âq\0~Ëq\0~Ïxxq\0~‘xsq\0~\0E\0\0\0w\0\0\0q\0~–xsq\0~·t\0\rBlockActivitysq\0~\0Q√ø¢q\0~¡t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0yn◊oq\0~Ût\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~¯xxq\0~≈t\0	Performersq\0~Ì\0q\0~¸sq\0~\0yñJªq\0~Æt\0approvert\0	StartModesq\0~Ú\0q\0~\0sq\0~\0zò$»q\0~Æt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\06Í∫ñq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0*‡yƒq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0[vçq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0#Q¯íq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~sq\0~\0\0lŒªq\0~Æt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0)Îπ+q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0Y⁄|>q\0~!t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0l…¥ıq\0~!t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0:à\"ˆq\0~!t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~%xsq\0~\0E\0\0\0w\0\0\0q\0~!xt\0Prioritysq\0~â\0q\0~8sq\0~\0r˘‡q\0~Æt\0\0t\0	Deadlinessq\0~5\0q\0~<sq\0~\0x/Pq\0~Æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Bsq\0~\0Y`=˝q\0~Æt\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0\"+µq\0~Cq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Jsq\0~\0\'–Ïq\0~Ct\0\0t\0TimeEstimationsq\0~ùq\0~Nsq\0~\0%˝tﬂq\0~Ct\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Ssq\0~\0}™:Hq\0~Ot\0\0t\0WorkingTimesq\0~®\0q\0~Wsq\0~\0~ÔÀq\0~Ot\0\0t\0Durationsq\0~≠\0q\0~[sq\0~\0Y∏hûq\0~Ot\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Tq\0~Xq\0~\\xxsq\0~\0E\0\0\0w\0\0\0q\0~Gq\0~Kq\0~Oxt\0Iconsq\0~`\0q\0~asq\0~\0≠§˙q\0~Æt\0\0t\0\rDocumentationsq\0~\07\0q\0~esq\0~\0;˘¥=q\0~Æt\0\0t\0TransitionRestrictionssq\0~i\0q\0~isq\0~\0Béö>q\0~Æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0eLÖLq\0~jt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~tsq\0~\0Q_~.q\0~ot\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0D∂n∫q\0~uq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~yxt\0Splitsq\0~É\0q\0~}sq\0~\0£Ôøq\0~ot\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0\0ƒl\"q\0~~t\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~Üsq\0~\0*@Ã\Zq\0~~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0-org.enhydra.shark.xpdl.elements.TransitionRefÉ%-ÀÒaö\0\0xq\0~\0©t\0\rTransitionRefsq\0~\0L>áxq\0~át\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\04êL≠q\0~çt\0transition5pxsq\0~\0E\0\0\0w\0\0\0q\0~íxsq\0~åt\0\rTransitionRefsq\0~\0Tv:âq\0~át\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0!\nNq\0~ñt\0transition10pxsq\0~\0E\0\0\0w\0\0\0q\0~õxsq\0~åt\0\rTransitionRefsq\0~\07q\0~át\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0/Ù⁄©q\0~üt\0transition12pxsq\0~\0E\0\0\0w\0\0\0q\0~§xxxsq\0~\0E\0\0\0w\0\0\0q\0~Çq\0~áxxsq\0~\0E\0\0\0w\0\0\0q\0~uq\0~~xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~™sq\0~\0Xhˆiq\0~Æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0DöSq\0~´t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Cÿ)®q\0~∞t\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0-\ZΩq\0~∞t\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~µq\0~∏xsq\0~öt\0ExtendedAttributesq\0~\0TY2yq\0~´t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0a˙º£q\0~ºt\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0cÌ\\]q\0~ºt\0449,24.79547119140625pxsq\0~\0E\0\0\0w\0\0\0q\0~¡q\0~ƒxxpxsq\0~\0E\0\0\0w\0\0\0q\0~≥q\0~∂q\0~∫q\0~æq\0~¡q\0~˝q\0~q\0~q\0~9q\0~=q\0~Cq\0~bq\0~fq\0~jq\0~´xsq\0~êt\0Activitysq\0~\06rhoq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0%riq\0~…t\0tool3pq\0~\0sq\0~\0\0q\0~\0sq\0~\0+•^\"q\0~…t\0Email Resubmit Notificationpt\0Descriptionsq\0~\02\0q\0~‘sq\0~\0é‡Qq\0~…t\0\0t\0Limitsq\0~é\0q\0~ÿsq\0~\0àXIq\0~…t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\03{Áq\0~…t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\02IwËq\0~‹t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0Z≠Öòq\0~‹t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0≥ƒîq\0~Êt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0^ UÊq\0~Ît\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0\'/è˝q\0~Ît\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~∆t\0Toolsq\0~\03“¥›q\0~ıt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0c’)q\0~˚t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0=\\Mπq\0~˚q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~“q\0~”xt\0ActualParameterssq\0~Ÿ\0q\0~	sq\0~\0\nÓ•äq\0~˚t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~	sq\0~\0\ZiïNq\0~˚t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~	sq\0~\0<ªæÇq\0~˚t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~	\0q\0~	q\0~	q\0~	\rq\0~	xxsq\0~…t\0SubFlowsq\0~\0O≈q\0~Ît\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0üÆq\0~	t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0]ÃT¶q\0~	q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~	\"sq\0~\0«hÖq\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~	q\0~	q\0~	#xxq\0~ıxsq\0~\0E\0\0\0w\0\0\0q\0~Îxsq\0~·t\0\rBlockActivitysq\0~\0UÚiˇq\0~‹t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0)aíIq\0~	*t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~	/xxq\0~Êt\0	Performersq\0~Ì\0q\0~	3sq\0~\07ößq\0~…t\0	requestert\0	StartModesq\0~Ú\0q\0~	7sq\0~\0+â[§q\0~…t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0mÚrªq\0~	8t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0}@Òq\0~	<t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0∫∑q\0~	<t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0(=pq\0~	<t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	@xsq\0~\0E\0\0\0w\0\0\0q\0~	<xt\0\nFinishModesq\0~\0q\0~	Ssq\0~\0_[Çƒq\0~…t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0%ÍKYq\0~	Tt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0BÃ.°q\0~	Xt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\04o{q\0~	Xt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0- y(q\0~	Xt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	\\xsq\0~\0E\0\0\0w\0\0\0q\0~	Xxt\0Prioritysq\0~â\0q\0~	osq\0~\0@òbùq\0~…t\0\0t\0	Deadlinessq\0~5\0q\0~	ssq\0~\0cı	q\0~…t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~	ysq\0~\0,z-	q\0~…t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0ΩËóq\0~	zq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~	Åsq\0~\0≤˛◊q\0~	zt\0\0t\0TimeEstimationsq\0~ùq\0~	Ösq\0~\0ulÈUq\0~	zt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~	äsq\0~\0êîq\0~	Üt\0\0t\0WorkingTimesq\0~®\0q\0~	ésq\0~\0eó8Sq\0~	Üt\0\0t\0Durationsq\0~≠\0q\0~	ísq\0~\0sE6pq\0~	Üt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~	ãq\0~	èq\0~	ìxxsq\0~\0E\0\0\0w\0\0\0q\0~	~q\0~	Çq\0~	Üxt\0Iconsq\0~`\0q\0~	òsq\0~\0aqOq\0~…t\0\0t\0\rDocumentationsq\0~\07\0q\0~	úsq\0~\0u‚‰Üq\0~…t\0\0t\0TransitionRestrictionssq\0~i\0q\0~	†sq\0~\0)k…q\0~…t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~	¶sq\0~\0sGJ\"q\0~…t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0o\0x1q\0~	ßt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0R=˘\\q\0~	¨t\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0pWÓ¢q\0~	¨t\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~	±q\0~	¥xsq\0~öt\0ExtendedAttributesq\0~\0øq\0~	ßt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0k<=úq\0~	∏t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0VˆŒæq\0~	∏t\0403,22pxsq\0~\0E\0\0\0w\0\0\0q\0~	Ωq\0~	¿xxpxsq\0~\0E\0\0\0w\0\0\0q\0~Œq\0~—q\0~’q\0~Ÿq\0~‹q\0~	4q\0~	8q\0~	Tq\0~	pq\0~	tq\0~	zq\0~	ôq\0~	ùq\0~	°q\0~	ßxsq\0~êt\0Activitysq\0~\0õ<q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0.°Bòq\0~	≈t\0tool4pq\0~\0sq\0~\0\0q\0~\0sq\0~\0`õDq\0~	≈t\0\ZEmail Request For Approvalpt\0Descriptionsq\0~\02\0q\0~	–sq\0~\0∫î…q\0~	≈t\0\0t\0Limitsq\0~é\0q\0~	‘sq\0~\0|Y*ﬁq\0~	≈t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0=¬Åq\0~	≈t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0/j∆q\0~	ÿt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0gæ‘>q\0~	ÿt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0RÕq\0~	‚t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0Wvq\0~	Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0\0Ãºq\0~	Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~∆t\0Toolsq\0~\0EVòq\0~	Òt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0)œ_{q\0~	˜t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\01{˝£q\0~	˜q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~“q\0~”xt\0ActualParameterssq\0~Ÿ\0q\0~\nsq\0~\0TM#mq\0~	˜t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~\nsq\0~\06–ÙXq\0~	˜t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\nsq\0~\0G∑4Àq\0~	˜t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~	¸q\0~	ˇq\0~\nq\0~\n	q\0~\n\rxxsq\0~…t\0SubFlowsq\0~\0¬áq\0~	Át\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0cZ\Zùq\0~\nt\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0\'yq\0~\nq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~\nsq\0~\0B0k∂q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~\nq\0~\nq\0~\nxxq\0~	Òxsq\0~\0E\0\0\0w\0\0\0q\0~	Áxsq\0~·t\0\rBlockActivitysq\0~\0GœÊÇq\0~	ÿt\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0tÚÇq\0~\n&t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\n+xxq\0~	‚t\0	Performersq\0~Ì\0q\0~\n/sq\0~\0J∆q\0~	≈t\0approvert\0	StartModesq\0~Ú\0q\0~\n3sq\0~\0Z0ˇ»q\0~	≈t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0bs˘˙q\0~\n4t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0[Ívq\0~\n8t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0P∆â£q\0~\n8t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0;Do^q\0~\n8t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~\n<xsq\0~\0E\0\0\0w\0\0\0q\0~\n8xt\0\nFinishModesq\0~\0q\0~\nOsq\0~\0„· q\0~	≈t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0J|Í:q\0~\nPt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0—q\0~\nTt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0#9œq\0~\nTt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0?z;Åq\0~\nTt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~\nXxsq\0~\0E\0\0\0w\0\0\0q\0~\nTxt\0Prioritysq\0~â\0q\0~\nksq\0~\0£çXq\0~	≈t\0\0t\0	Deadlinessq\0~5\0q\0~\nosq\0~\0–j–q\0~	≈t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~\nusq\0~\0yl⁄ q\0~	≈t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0§@øq\0~\nvq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~\n}sq\0~\0y∆^q\0~\nvt\0\0t\0TimeEstimationsq\0~ùq\0~\nÅsq\0~\0n™q\0~\nvt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~\nÜsq\0~\0\rÃﬂºq\0~\nÇt\0\0t\0WorkingTimesq\0~®\0q\0~\näsq\0~\08X◊q\0~\nÇt\0\0t\0Durationsq\0~≠\0q\0~\nésq\0~\0uﬁΩaq\0~\nÇt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~\náq\0~\nãq\0~\nèxxsq\0~\0E\0\0\0w\0\0\0q\0~\nzq\0~\n~q\0~\nÇxt\0Iconsq\0~`\0q\0~\nîsq\0~\0#q\0~	≈t\0\0t\0\rDocumentationsq\0~\07\0q\0~\nòsq\0~\0y›c‡q\0~	≈t\0\0t\0TransitionRestrictionssq\0~i\0q\0~\núsq\0~\0CKq\0~	≈t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0~U[≥q\0~\nùt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~\nßsq\0~\0]8Ó{q\0~\n¢t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0YÜßq\0~\n®t\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~\n¨xt\0Splitsq\0~É\0q\0~\n±sq\0~\0F™“q\0~\n¢t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0ˆëºq\0~\n≤q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~\nπsq\0~\0ß˛Øq\0~\n≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~\n∂q\0~\n∫xxsq\0~\0E\0\0\0w\0\0\0q\0~\n®q\0~\n≤xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n¡sq\0~\0ˆÀ\"q\0~	≈t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0e‡óŒq\0~\n¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0nsq\0~\n«t\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0$oÊq\0~\n«t\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~\nÃq\0~\nœxsq\0~öt\0ExtendedAttributesq\0~\0\\_Æ≠q\0~\n¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0=`q\0~\n”t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0\\˜Êßq\0~\n”t\0116,22pxsq\0~\0E\0\0\0w\0\0\0q\0~\nÿq\0~\n€xxpxsq\0~\0E\0\0\0w\0\0\0q\0~	 q\0~	Õq\0~	—q\0~	’q\0~	ÿq\0~\n0q\0~\n4q\0~\nPq\0~\nlq\0~\npq\0~\nvq\0~\nïq\0~\nôq\0~\nùq\0~\n¬xsq\0~êt\0Activitysq\0~\0\0€æÏq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Ôh…q\0~\n‡t\0route2pq\0~\0sq\0~\0\0q\0~\0sq\0~\0!–Búq\0~\n‡t\0\0pt\0Descriptionsq\0~\02\0q\0~\nÎsq\0~\0m\"›q\0~\n‡t\0\0t\0Limitsq\0~é\0q\0~\nÔsq\0~\0Ã=)q\0~\n‡t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0\"Ω¿‚q\0~\n‡t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0Z¢ãq\0~\nÛt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0tT∑q\0~\nÛt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0joMaq\0~\n˝t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0&*q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0rÉä¨q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0&ûÊ‡q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\06ä≥q\0~t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0Y¨6q\0~q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~sq\0~\07qq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~\Zq\0~xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xsq\0~·t\0\rBlockActivitysq\0~\0fÏHïq\0~\nÛt\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0MTq\0~%t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~*xxq\0~\n˜t\0	Performersq\0~Ì\0q\0~.sq\0~\0=πw”q\0~\n‡t\0	requestert\0	StartModesq\0~Ú\0q\0~2sq\0~\0*1fåq\0~\n‡t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0.;øq\0~3t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0Q◊Pq\0~7t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0=òuhq\0~7t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0(ﬂ,q\0~7t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~;xsq\0~\0E\0\0\0w\0\0\0q\0~7xt\0\nFinishModesq\0~\0q\0~Nsq\0~\0<Mq\0~\n‡t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0W™Öq\0~Ot\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0u¯“Iq\0~St\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0`G?q\0~St\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0Sõq\0~St\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~Wxsq\0~\0E\0\0\0w\0\0\0q\0~Sxt\0Prioritysq\0~â\0q\0~jsq\0~\0o\Z˜6q\0~\n‡t\0\0t\0	Deadlinessq\0~5\0q\0~nsq\0~\0\\√\Zq\0~\n‡t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~tsq\0~\0%©ûq\0~\n‡t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0J±Ÿóq\0~uq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~|sq\0~\0r*4∞q\0~ut\0\0t\0TimeEstimationsq\0~ùq\0~Äsq\0~\06}Ÿq\0~ut\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Ösq\0~\0<5—q\0~Åt\0\0t\0WorkingTimesq\0~®\0q\0~âsq\0~\0ZÖ*‡q\0~Åt\0\0t\0Durationsq\0~≠\0q\0~çsq\0~\0y)\n¥q\0~Åt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Üq\0~äq\0~éxxsq\0~\0E\0\0\0w\0\0\0q\0~yq\0~}q\0~Åxt\0Iconsq\0~`\0q\0~ìsq\0~\0\'îΩÉq\0~\n‡t\0\0t\0\rDocumentationsq\0~\07\0q\0~ósq\0~\0X˘ñq\0~\n‡t\0\0t\0TransitionRestrictionssq\0~i\0q\0~õsq\0~\0+ÿu»q\0~\n‡t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~°sq\0~\0\Z¶≠q\0~\n‡t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0O˚q\0~¢t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0u\0°∞q\0~ßt\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0h#\nq\0~ßt\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~¨q\0~Øxsq\0~öt\0ExtendedAttributesq\0~\0Js]}q\0~¢t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0m¨´_q\0~≥t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0—ä{q\0~≥t\0128,34pxsq\0~\0E\0\0\0w\0\0\0q\0~∏q\0~ªxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\nÂq\0~\nËq\0~\nÏq\0~\nq\0~\nÛq\0~/q\0~3q\0~Oq\0~kq\0~oq\0~uq\0~îq\0~òq\0~úq\0~¢xxt\0Transitionssr\0+org.enhydra.shark.xpdl.elements.TransitionsÌ9>‰¿/iØ\0\0xq\0~\0k\0q\0~¿sq\0~\0?Xmpq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0	w\0\0\0	sr\0*org.enhydra.shark.xpdl.elements.Transitiontßx∏Ö\0\0xq\0~\0©t\0\nTransitionsq\0~\0(âeÓq\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0yS‡q\0~»t\0transition2pq\0~\0sq\0~\0\0q\0~\0sq\0~\0]≥¸‚q\0~»t\0\0pt\0Fromsq\0~\0q\0~”sq\0~\0?*†q\0~»t\0approve_proposalpt\0Tosq\0~\0q\0~◊sq\0~\0Æ\Zäq\0~»t\0route3pt\0	Conditionsr\0)org.enhydra.shark.xpdl.elements.Condition€‡D··Z;|\0\0xq\0~\0\0q\0~€sq\0~\0>vTØq\0~»t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\08\";Îq\0~›q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0	CONDITIONt\0	OTHERWISEt\0	EXCEPTIONt\0DEFAULTEXCEPTIONxxsq\0~\0E\0\0\0w\0\0\0q\0~·xt\0Descriptionsq\0~\02\0q\0~Èsq\0~\0P∞/q\0~»t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Ìsq\0~\0¶Æ7q\0~»t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0nùeq\0~Ót\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0MÔ!πq\0~Ût\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0M+I®q\0~Ût\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~¯q\0~˚xxpxsq\0~\0E\0\0\0w\0\0\0q\0~Õq\0~–q\0~‘q\0~ÿq\0~›q\0~Íq\0~Óxsq\0~«t\0\nTransitionsq\0~\0l–“q\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0yëÒ«q\0~\0t\0transition5pq\0~\0sq\0~\0\0q\0~\0sq\0~\0/ù9q\0~\0t\0\0pq\0~”sq\0~\0q\0~”sq\0~\0µˇRq\0~\0t\0route3pq\0~◊sq\0~\0q\0~◊sq\0~\08cıîq\0~\0t\0tool2pt\0	Conditionsq\0~‹\0q\0~sq\0~\0E8 —q\0~\0t\0status === \'Rejected\'sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0f‘‚Kq\0~t\0	CONDITIONsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~‰q\0~Âq\0~Êq\0~Áxxsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Descriptionsq\0~\02\0q\0~sq\0~\0A<dPq\0~\0t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0z|“q\0~\0t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0p3\\#q\0~ t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0|πfìq\0~%t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\05ßQYq\0~%t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~*q\0~-xsq\0~öt\0ExtendedAttributesq\0~\0gx±q\0~ t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\r@j9q\0~1t\0PBUILDER_TRANSITION_CONDITIONSpq\0~£sq\0~\0\0q\0~£sq\0~\0:z¸q\0~1t\0G[{\"join\":\"&&\",\"variable\":\"status\",\"operator\":\"===\",\"value\":\"Rejected\"}]pxsq\0~\0E\0\0\0w\0\0\0q\0~6q\0~9xxpxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~q\0~q\0~q\0~q\0~q\0~ xsq\0~«t\0\nTransitionsq\0~\05ÿªsq\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\02Ïq\0~>t\0transition9pq\0~\0sq\0~\0\0q\0~\0sq\0~\0qÕ_]q\0~>t\0\0pq\0~”sq\0~\0q\0~”sq\0~\0Ö(q\0~>t\0tool1pq\0~◊sq\0~\0q\0~◊sq\0~\0\Zô*q\0~>t\0\rsend_proposalpt\0	Conditionsq\0~‹\0q\0~Osq\0~\0æq4q\0~>t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0=Gî\"q\0~Pq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~‰q\0~Âq\0~Êq\0~Áxxsq\0~\0E\0\0\0w\0\0\0q\0~Txt\0Descriptionsq\0~\02\0q\0~Xsq\0~\0r€âiq\0~>t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\\sq\0~\0i˜ÿq\0~>t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0i≠m˙q\0~]t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0_†Œâq\0~bt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\05ÿêq\0~bt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~gq\0~jxxpxsq\0~\0E\0\0\0w\0\0\0q\0~Cq\0~Fq\0~Iq\0~Lq\0~Pq\0~Yq\0~]xsq\0~«t\0\nTransitionsq\0~\0NÌá\\q\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0zÅ≥\Zq\0~ot\0transition10pq\0~\0sq\0~\0\0q\0~\0sq\0~\0dâ∏ªq\0~ot\0\0pq\0~”sq\0~\0q\0~”sq\0~\0L≠·∆q\0~ot\0route3pq\0~◊sq\0~\0q\0~◊sq\0~\0†∫\\q\0~ot\0tool3pt\0	Conditionsq\0~‹\0q\0~Äsq\0~\0F¸q\0~ot\0status === \'Resubmit\'sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0e|nŸq\0~Åt\0	CONDITIONsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~‰q\0~Âq\0~Êq\0~Áxxsq\0~\0E\0\0\0w\0\0\0q\0~Öxt\0Descriptionsq\0~\02\0q\0~äsq\0~\0uûJpq\0~ot\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~ésq\0~\0\\{xq\0~ot\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0a0q\0~èt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\rƒœq\0~ît\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0\\…8Xq\0~ît\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~ôq\0~úxsq\0~öt\0ExtendedAttributesq\0~\0x0Z£q\0~èt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Xå®tq\0~†t\0PBUILDER_TRANSITION_CONDITIONSpq\0~£sq\0~\0\0q\0~£sq\0~\0)÷$q\0~†t\0G[{\"join\":\"&&\",\"variable\":\"status\",\"operator\":\"===\",\"value\":\"Resubmit\"}]pxsq\0~\0E\0\0\0w\0\0\0q\0~•q\0~®xxpxsq\0~\0E\0\0\0w\0\0\0q\0~tq\0~wq\0~zq\0~}q\0~Åq\0~ãq\0~èxsq\0~«t\0\nTransitionsq\0~\0<»¬åq\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0{¥4Æq\0~≠t\0transition11pq\0~\0sq\0~\0\0q\0~\0sq\0~\0cK¿§q\0~≠t\0\0pq\0~”sq\0~\0q\0~”sq\0~\0F◊rÙq\0~≠t\0tool3pq\0~◊sq\0~\0q\0~◊sq\0~\0{•ΩRq\0~≠t\0	activity1pt\0	Conditionsq\0~‹\0q\0~æsq\0~\0m¢„q\0~≠t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0d–3Ëq\0~øq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~‰q\0~Âq\0~Êq\0~Áxxsq\0~\0E\0\0\0w\0\0\0q\0~√xt\0Descriptionsq\0~\02\0q\0~«sq\0~\02IM\rq\0~≠t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Àsq\0~\0∫Â\0q\0~≠t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0dF)Æq\0~Ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\njq\0~—t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0T\rSq\0~—t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~÷q\0~Ÿxxpxsq\0~\0E\0\0\0w\0\0\0q\0~≤q\0~µq\0~∏q\0~ªq\0~øq\0~»q\0~Ãxsq\0~«t\0\nTransitionsq\0~\0îq\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0AT…8q\0~ﬁt\0transition12pq\0~\0sq\0~\0\0q\0~\0sq\0~\0jÒrq\0~ﬁt\0Approvedpq\0~”sq\0~\0q\0~”sq\0~\0TJW“q\0~ﬁt\0route3pq\0~◊sq\0~\0q\0~◊sq\0~\0>_Ïq\0~ﬁt\0tool1pt\0	Conditionsq\0~‹\0q\0~Ôsq\0~\0KÏàñq\0~ﬁt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0M≤˛q\0~t\0	OTHERWISEsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~‰q\0~Âq\0~Êq\0~Áxxsq\0~\0E\0\0\0w\0\0\0q\0~Ùxt\0Descriptionsq\0~\02\0q\0~˘sq\0~\0cì6q\0~ﬁt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~˝sq\0~\0n”DLq\0~ﬁt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0zp^q\0~˛t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0+ë÷‘q\0~\rt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0<mLEq\0~\rt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\rq\0~\rxxpxsq\0~\0E\0\0\0w\0\0\0q\0~„q\0~Êq\0~Èq\0~Ïq\0~q\0~˙q\0~˛xsq\0~«t\0\nTransitionsq\0~\0qåÉbq\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0_ÆfWq\0~\rt\0transition13pq\0~\0sq\0~\0\0q\0~\0sq\0~\0x«:Õq\0~\rt\0\0pq\0~”sq\0~\0q\0~”sq\0~\0˛q\0~\rt\0tool4pq\0~◊sq\0~\0q\0~◊sq\0~\0.ûT?q\0~\rt\0approve_proposalpt\0	Conditionsq\0~‹\0q\0~\r!sq\0~\0mE1$q\0~\rt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0ïcñq\0~\r\"q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~‰q\0~Âq\0~Êq\0~Áxxsq\0~\0E\0\0\0w\0\0\0q\0~\r&xt\0Descriptionsq\0~\02\0q\0~\r*sq\0~\0Zv◊q\0~\rt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\r.sq\0~\0&®q\0~\rt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0Ô8Áq\0~\r/t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0>yë¢q\0~\r4t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0D—ùq\0~\r4t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\r9q\0~\r<xxpxsq\0~\0E\0\0\0w\0\0\0q\0~\rq\0~\rq\0~\rq\0~\rq\0~\r\"q\0~\r+q\0~\r/xsq\0~«t\0\nTransitionsq\0~\0W≥ª§q\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ióüq\0~\rAt\0transition8pq\0~\0sq\0~\0\0q\0~\0sq\0~\0zärq\0~\rAt\0\0pq\0~”sq\0~\0q\0~”sq\0~\03ˇâµq\0~\rAt\0	activity1pq\0~◊sq\0~\0q\0~◊sq\0~\0A˘ÍÈq\0~\rAt\0tool4pt\0	Conditionsq\0~‹\0q\0~\rRsq\0~\00Æ˘q\0~\rAt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0S|ùq\0~\rSq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~‰q\0~Âq\0~Êq\0~Áxxsq\0~\0E\0\0\0w\0\0\0q\0~\rWxt\0Descriptionsq\0~\02\0q\0~\r[sq\0~\0qso<q\0~\rAt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\r_sq\0~\0[»Û„q\0~\rAt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0^(ﬁq\0~\r`t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0sªNhq\0~\ret\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\00O\"eq\0~\ret\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\rjq\0~\rmxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\rFq\0~\rIq\0~\rLq\0~\rOq\0~\rSq\0~\r\\q\0~\r`xsq\0~«t\0\nTransitionsq\0~\02√I„q\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0,Ï,q\0~\rrt\0transition14pq\0~\0sq\0~\0\0q\0~\0sq\0~\0-\ZPàq\0~\rrt\0\0pq\0~”sq\0~\0q\0~”sq\0~\0Mn≈Ωq\0~\rrt\0route2pq\0~◊sq\0~\0q\0~◊sq\0~\0dè\Zvq\0~\rrt\0tool4pt\0	Conditionsq\0~‹\0q\0~\rÉsq\0~\0æàôq\0~\rrt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0>FÊæq\0~\rÑq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~‰q\0~Âq\0~Êq\0~Áxxsq\0~\0E\0\0\0w\0\0\0q\0~\ràxt\0Descriptionsq\0~\02\0q\0~\råsq\0~\0\"Ü™q\0~\rrt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\rêsq\0~\0]Áçbq\0~\rrt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0!¨q\0~\rët\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0sâ q\0~\rñt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0F%Hq\0~\rñt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\rõq\0~\rûxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\rwq\0~\rzq\0~\r}q\0~\rÄq\0~\rÑq\0~\rçq\0~\rëxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\r£sq\0~\0 b!ïq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0{ê©nq\0~\r§t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0W2Éq\0~\r©t\0%JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDERpq\0~£sq\0~\0\0q\0~£sq\0~\0ﬂÅq\0~\r©t\0requester;approverpxsq\0~\0E\0\0\0w\0\0\0q\0~\rÆq\0~\r±xsq\0~öt\0ExtendedAttributesq\0~\0G¢⁄/q\0~\r§t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\09¿lq\0~\rµt\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0>Âq\0~\rµt\0§JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=send_proposal,X_OFFSET=866,Y_OFFSET=34,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~\r∫q\0~\rΩxsq\0~öt\0ExtendedAttributesq\0~\0/∏¸q\0~\r§t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0~\Z◊Üq\0~\r¡t\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0yôlâq\0~\r¡t\0õJaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool2,X_OFFSET=792,Y_OFFSET=36,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~\r∆q\0~\r…xsq\0~öt\0ExtendedAttributesq\0~\0Í˝Iq\0~\r§t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0m¢©q\0~\rÕt\0JaWE_GRAPH_START_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0E,Âﬁq\0~\rÕt\0ûJaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route2,X_OFFSET=56,Y_OFFSET=46,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~\r“q\0~\r’xxpxsq\0~\0E\0\0\0\rw\0\0\0\rq\0~cq\0~fq\0~jq\0~qq\0~¥q\0~”q\0~Ÿq\0~wq\0~}q\0~Ñq\0~ãq\0~¬q\0~\r§xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\r⁄sq\0~\0WóTQq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0xúÂq\0~\r€t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0/“∑q\0~\r‡t\0EDITING_TOOLpq\0~£sq\0~\0\0q\0~£sq\0~\0\0Ò\'q\0~\r‡t\0Web Workflow Designerpxsq\0~\0E\0\0\0w\0\0\0q\0~\rÂq\0~\rËxsq\0~öt\0ExtendedAttributesq\0~\0~®–øq\0~\r€t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\"à;q\0~\rÏt\0EDITING_TOOL_VERSIONpq\0~£sq\0~\0\0q\0~£sq\0~\0JE\\§q\0~\rÏt\0\r5.0-pre-alphapxsq\0~\0E\0\0\0w\0\0\0q\0~\rÒq\0~\rÙxxpxsq\0~\0E\0\0\0\rw\0\0\0\rq\0~\0q\0~\0q\0~\0q\0~\0Iq\0~\0tq\0~\0Çq\0~\0ïq\0~\0úq\0~\0£q\0~q\0~Qq\0~Xq\0~\r€x\0sq\0~\0w\0\0\0\0xt\01sr\0*org.enhydra.shark.xpdl.elements.Namespaces|ö—<.Rßñ\0\0xq\0~\0kt\0\nNamespacessq\0~\0,Ω)q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0)org.enhydra.shark.xpdl.elements.NamespaceËz¬¬_\0\0xq\0~\0t\0	Namespacesq\0~\0\"Æ≠q\0~\r¸t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0yb°q\0~t\0xpdlpq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0ﬂÎ´q\0~t\0 http://www.wfmc.org/2002/XPDL1.0pxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~xx',1000202,1,1000203,0);
/*!40000 ALTER TABLE `SHKXPDLData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLHistory`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLHistory` (
  `XPDLId` varchar(90) NOT NULL,
  `XPDLVersion` varchar(20) NOT NULL,
  `XPDLClassVersion` bigint(20) NOT NULL,
  `XPDLUploadTime` datetime NOT NULL,
  `XPDLHistoryUploadTime` datetime NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLHistory` (`XPDLId`,`XPDLVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLHistory`
--

LOCK TABLES `SHKXPDLHistory` WRITE;
/*!40000 ALTER TABLE `SHKXPDLHistory` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKXPDLHistory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLHistoryData`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLHistoryData` (
  `XPDLContent` mediumblob NOT NULL,
  `XPDLClassContent` mediumblob NOT NULL,
  `XPDLHistory` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLHistoryData` (`CNT`),
  KEY `SHKXPDLHistoryData_XPDLHistory` (`XPDLHistory`),
  CONSTRAINT `SHKXPDLHistoryData_XPDLHistory` FOREIGN KEY (`XPDLHistory`) REFERENCES `SHKXPDLHistory` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLHistoryData`
--

LOCK TABLES `SHKXPDLHistoryData` WRITE;
/*!40000 ALTER TABLE `SHKXPDLHistoryData` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKXPDLHistoryData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLParticipantPackage`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLParticipantPackage` (
  `PACKAGE_ID` varchar(90) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLParticipantPackage` (`PACKAGE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLParticipantPackage`
--

LOCK TABLES `SHKXPDLParticipantPackage` WRITE;
/*!40000 ALTER TABLE `SHKXPDLParticipantPackage` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKXPDLParticipantPackage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLParticipantProcess`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLParticipantProcess` (
  `PROCESS_ID` varchar(90) NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLParticipantProcess` (`PROCESS_ID`,`PACKAGEOID`),
  KEY `SHKXPDLParticipantProcess_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKXPDLParticipantProcess_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `SHKXPDLParticipantPackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLParticipantProcess`
--

LOCK TABLES `SHKXPDLParticipantProcess` WRITE;
/*!40000 ALTER TABLE `SHKXPDLParticipantProcess` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKXPDLParticipantProcess` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLReferences`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLReferences` (
  `ReferredXPDLId` varchar(90) NOT NULL,
  `ReferringXPDL` decimal(19,0) NOT NULL,
  `ReferredXPDLNumber` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLReferences` (`ReferredXPDLId`,`ReferringXPDL`),
  KEY `SHKXPDLReferences_ReferringXPDL` (`ReferringXPDL`),
  CONSTRAINT `SHKXPDLReferences_ReferringXPDL` FOREIGN KEY (`ReferringXPDL`) REFERENCES `SHKXPDLS` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLReferences`
--

LOCK TABLES `SHKXPDLReferences` WRITE;
/*!40000 ALTER TABLE `SHKXPDLReferences` DISABLE KEYS */;
/*!40000 ALTER TABLE `SHKXPDLReferences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLS`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SHKXPDLS` (
  `XPDLId` varchar(90) NOT NULL,
  `XPDLVersion` varchar(20) NOT NULL,
  `XPDLClassVersion` bigint(20) NOT NULL,
  `XPDLUploadTime` datetime NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLS` (`XPDLId`,`XPDLVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLS`
--

LOCK TABLES `SHKXPDLS` WRITE;
/*!40000 ALTER TABLE `SHKXPDLS` DISABLE KEYS */;
INSERT INTO `SHKXPDLS` VALUES ('crm','1',1184650391000,'2017-07-12 08:56:13',1000202,0);
/*!40000 ALTER TABLE `SHKXPDLS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_app`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_app` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `published` bit(1) DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `license` text,
  `description` longtext,
  `meta` longtext,
  PRIMARY KEY (`appId`,`appVersion`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_app`
--

LOCK TABLES `app_app` WRITE;
/*!40000 ALTER TABLE `app_app` DISABLE KEYS */;
INSERT INTO `app_app` VALUES ('appcenter',1,'App Center','','2017-07-12 08:56:19','2017-07-12 16:56:19','oRIgWuw8ed5OmS98TSZFxocskOFXU0v3VPneM0k80NqSBK2r6RhNzTNTryZkuj4W',NULL,NULL),('crm',1,'Customer Relationship Management','','2017-07-12 08:56:11','2017-07-12 16:56:14','Vfe6Df5AdlrGEPqFYgTt8XuGJ4XiTt2NnBLEovw3qoQ=',NULL,NULL);
/*!40000 ALTER TABLE `app_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_datalist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_datalist` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` text,
  `json` longtext,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK5E9247A6462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FK5E9247A6462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_datalist`
--

LOCK TABLES `app_datalist` WRITE;
/*!40000 ALTER TABLE `app_datalist` DISABLE KEYS */;
INSERT INTO `app_datalist` VALUES ('crm',1,'crm_account_list','Account Listing',NULL,'{\"id\":\"crm_account_list\",\"name\":\"Account Listing\",\"pageSize\":\"10\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[{\"id\":\"rowAction_0\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_list\",\"target\":\"_self\",\"hrefParam\":\"d-6304176-fn_account\",\"hrefColumn\":\"id\",\"label\":\"Contacts\",\"confirmation\":\"\",\"visible\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"},{\"id\":\"rowAction_1\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"fk_account\",\"hrefColumn\":\"id\",\"label\":\"New Contact\",\"confirmation\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"}],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"label\":\"Add\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"label\":\"Delete\",\"formDefId\":\"crm_account\",\"confirmation\":\"Are you sure?\",\"deleteGridData\":\"\",\"deleteSubformData\":\"\",\"abortRelatedRunningProcesses\":\"\",\"datalist_type\":\"action\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_1\",\"name\":\"id\",\"label\":\"ID\"},{\"id\":\"filter_0\",\"label\":\"Account Name\",\"name\":\"accountName\"},{\"id\":\"filter_2\",\"name\":\"country\",\"label\":\"Country\"},{\"id\":\"filter_3\",\"name\":\"state\",\"label\":\"State\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_account\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"id\",\"label\":\"ID\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"ID\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_1\",\"name\":\"accountName\",\"label\":\"Account Name\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_5\",\"name\":\"country\",\"label\":\"Country\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_4\",\"name\":\"state\",\"label\":\"State\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}]}','2017-07-12 08:56:12','2017-07-12 08:56:12'),('crm',1,'crm_contact_list','Contact List',NULL,'{\"id\":\"crm_contact_list\",\"name\":\"Contact List\",\"pageSize\":\"10\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[{\"id\":\"rowAction_0\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Edit\",\"confirmation\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"}],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"label\":\"Add\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"label\":\"Delete\",\"formDefId\":\"crm_contact\",\"confirmation\":\"Are you sure?\",\"deleteGridData\":\"\",\"deleteSubformData\":\"\",\"abortRelatedRunningProcesses\":\"\",\"datalist_type\":\"action\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_2\",\"name\":\"account\",\"filterParamName\":\"d-6304176-fn_account\",\"label\":\"Account\",\"type\":{\"className\":\"org.joget.plugin.enterprise.SelectBoxDataListFilterType\",\"properties\":{\"multiple\":\"autocomplete\",\"size\":\"\",\"defaultValue\":\"\",\"options\":[],\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"idColumn\":\"\",\"labelColumn\":\"accountName\",\"groupingColumn\":\"\",\"extraCondition\":\"\",\"addEmptyOption\":\"\",\"emptyLabel\":\"\",\"useAjax\":\"\"}}}}},{\"id\":\"filter_0\",\"name\":\"fullName\",\"label\":\"First Name\",\"type\":{\"className\":\"org.joget.apps.datalist.lib.TextFieldDataListFilterType\",\"properties\":{\"defaultValue\":\"\"}}},{\"id\":\"filter_1\",\"label\":\"Last Name\",\"name\":\"lastName\"},{\"id\":\"filter_3\",\"name\":\"address\",\"label\":\"Address\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_contact\"}},\"columns\":[{\"id\":\"column_2\",\"name\":\"account\",\"label\":\"Account\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"account\",\"label\":\"Account\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"org.joget.plugin.enterprise.OptionsValueFormatter\",\"properties\":{\"options\":[],\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"idColumn\":\"\",\"labelColumn\":\"accountName\",\"groupingColumn\":\"\",\"extraCondition\":\"\",\"addEmptyOption\":\"\",\"emptyLabel\":\"\",\"useAjax\":\"\"}}}}},{\"id\":\"column_0\",\"name\":\"fullName\",\"label\":\"First Name\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Full Name\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_4\",\"name\":\"lastName\",\"label\":\"Last Name\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_1\",\"label\":\"Address\",\"displayLabel\":\"Address\",\"name\":\"address\"}]}','2017-07-12 08:56:12','2017-07-12 08:56:12'),('crm',1,'crm_opportunity_list','Opportunity List',NULL,'{\"id\":\"crm_opportunity_list\",\"name\":\"Opportunity List\",\"pageSize\":\"10\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"opportunity_new\",\"target\":\"_self\",\"label\":\"Add\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"label\":\"Delete\",\"formDefId\":\"crm_opportunity\",\"confirmation\":\"Are you sure?\",\"deleteGridData\":\"\",\"deleteSubformData\":\"\",\"abortRelatedRunningProcesses\":\"\",\"datalist_type\":\"action\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_0\",\"label\":\"Title\",\"name\":\"title\"},{\"id\":\"filter_1\",\"name\":\"account\",\"label\":\"Account\",\"type\":{\"className\":\"org.joget.plugin.enterprise.SelectBoxDataListFilterType\",\"properties\":{\"multiple\":\"textfield\",\"size\":\"\",\"defaultValue\":\"\",\"options\":[],\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"idColumn\":\"\",\"labelColumn\":\"accountName\",\"groupingColumn\":\"\",\"extraCondition\":\"\",\"addEmptyOption\":\"\",\"emptyLabel\":\"\",\"useAjax\":\"\"}}}}},{\"id\":\"filter_2\",\"name\":\"stage\",\"label\":\"Stage\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_opportunity\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"title\",\"label\":\"Title\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"opportunity_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Title\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_4\",\"name\":\"account\",\"label\":\"Account\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"account\",\"label\":\"Account\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"org.joget.plugin.enterprise.OptionsValueFormatter\",\"properties\":{\"options\":[],\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"idColumn\":\"\",\"labelColumn\":\"accountName\",\"groupingColumn\":\"\",\"extraCondition\":\"\",\"addEmptyOption\":\"\",\"emptyLabel\":\"\",\"useAjax\":\"\"}}}}},{\"id\":\"column_2\",\"name\":\"amount\",\"label\":\"Amount\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_3\",\"name\":\"stage\",\"label\":\"Stage\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_5\",\"label\":\"Source\",\"displayLabel\":\"Source\",\"name\":\"source\"},{\"id\":\"column_1\",\"name\":\"dateModified\",\"label\":\"Date Modified\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}]}','2017-07-12 08:56:12','2017-07-12 08:56:12'),('crm',1,'Proposal','Proposals List',NULL,'{\"id\":\"Proposal\",\"name\":\"Proposals List\",\"pageSize\":\"10\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"proposal_process\",\"target\":\"_self\",\"label\":\"New Proposal Process\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"name\":\"Form Row Delete Action\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"label\":\"Delete\",\"type\":\"text\",\"id\":\"action_0\",\"properties\":{\"label\":\"Delete\",\"formDefId\":\"crm_proposal_form\",\"confirmation\":\"Are you sure?\",\"deleteGridData\":\"true\",\"deleteSubformData\":\"true\",\"abortRelatedRunningProcesses\":\"true\",\"datalist_type\":\"action\"}}],\"filters\":[{\"id\":\"filter_2\",\"datalist_type\":\"filter\",\"name\":\"refNo\",\"filterParamName\":\"d-3959580-fn_refNo\",\"label\":\"Ref No.\",\"type\":{\"className\":\"org.joget.apps.datalist.lib.TextFieldDataListFilterType\",\"properties\":{\"defaultValue\":\"\"}}},{\"id\":\"filter_1\",\"name\":\"account\",\"label\":\"Account\"},{\"id\":\"filter_0\",\"name\":\"title\",\"label\":\"Title\"}],\"binder\":{\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_proposal_approval_form\",\"extraCondition\":\"\"}},\"columns\":[{\"id\":\"column_0\",\"datalist_type\":\"column\",\"name\":\"refNo\",\"label\":\"Ref No.\",\"sortable\":\"true\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_1\",\"label\":\"Account\",\"displayLabel\":\"Account\",\"name\":\"account\"},{\"id\":\"column_2\",\"datalist_type\":\"column\",\"name\":\"title\",\"label\":\"Title\",\"sortable\":\"false\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"proposal_form_\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_3\",\"label\":\"Description\",\"displayLabel\":\"Description\",\"name\":\"description\"},{\"id\":\"column_4\",\"datalist_type\":\"column\",\"name\":\"status\",\"label\":\"Status\",\"sortable\":\"false\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"format\":{\"className\":\"org.joget.apps.datalist.lib.DefaultFormatter\",\"properties\":{\"formatting\":\"b\",\"size\":\"\",\"color\":\"\",\"formDefId\":\"\",\"field\":\"\"}}},{\"id\":\"column_5\",\"label\":\"Date Created\",\"displayLabel\":\"Date Created\",\"name\":\"dateCreated\"}]}','2017-07-12 08:56:12','2017-07-12 08:56:12');
/*!40000 ALTER TABLE `app_datalist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_env_variable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_env_variable` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) NOT NULL,
  `value` text,
  `remarks` text,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK740A62EC462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FK740A62EC462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_env_variable`
--

LOCK TABLES `app_env_variable` WRITE;
/*!40000 ALTER TABLE `app_env_variable` DISABLE KEYS */;
INSERT INTO `app_env_variable` VALUES ('crm',1,'AppName','Customer Relationship Management',NULL),('crm',1,'refNo','',NULL);
/*!40000 ALTER TABLE `app_env_variable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_fd`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_fd` (
  `id` varchar(255) NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_fd`
--

LOCK TABLES `app_fd` WRITE;
/*!40000 ALTER TABLE `app_fd` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_fd` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_fd_appcenter`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_fd_appcenter` (
  `id` varchar(255) NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_fd_appcenter`
--

LOCK TABLES `app_fd_appcenter` WRITE;
/*!40000 ALTER TABLE `app_fd_appcenter` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_fd_appcenter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_fd_crm_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_fd_crm_account` (
  `id` varchar(255) NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `c_country` longtext,
  `c_address` longtext,
  `c_accountName` longtext,
  `c_city` longtext,
  `c_state` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_fd_crm_account`
--

LOCK TABLES `app_fd_crm_account` WRITE;
/*!40000 ALTER TABLE `app_fd_crm_account` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_fd_crm_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_fd_crm_contact`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_fd_crm_contact` (
  `id` varchar(255) NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `c_lastName` longtext,
  `c_address` longtext,
  `c_city` longtext,
  `c_fullName` longtext,
  `c_photo` longtext,
  `c_state` longtext,
  `c_account` longtext,
  `c_addressAvailable` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_fd_crm_contact`
--

LOCK TABLES `app_fd_crm_contact` WRITE;
/*!40000 ALTER TABLE `app_fd_crm_contact` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_fd_crm_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_fd_crm_opportunity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_fd_crm_opportunity` (
  `id` varchar(255) NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `c_amount` longtext,
  `c_stage` longtext,
  `c_description` longtext,
  `c_source` longtext,
  `c_newAccount` longtext,
  `c_title` longtext,
  `c_account` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_fd_crm_opportunity`
--

LOCK TABLES `app_fd_crm_opportunity` WRITE;
/*!40000 ALTER TABLE `app_fd_crm_opportunity` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_fd_crm_opportunity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_fd_crm_proposal`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_fd_crm_proposal` (
  `id` varchar(255) NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `c_refNo` longtext,
  `c_comments` longtext,
  `c_notes` longtext,
  `c_attachment` longtext,
  `c_description` longtext,
  `c_title` longtext,
  `c_account` longtext,
  `c_status` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_fd_crm_proposal`
--

LOCK TABLES `app_fd_crm_proposal` WRITE;
/*!40000 ALTER TABLE `app_fd_crm_proposal` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_fd_crm_proposal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_form`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_form` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `formId` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `tableName` varchar(255) DEFAULT NULL,
  `json` longtext,
  `description` longtext,
  PRIMARY KEY (`appId`,`appVersion`,`formId`),
  KEY `FK45957822462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FK45957822462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_form`
--

LOCK TABLES `app_form` WRITE;
/*!40000 ALTER TABLE `app_form` DISABLE KEYS */;
INSERT INTO `app_form` VALUES ('appcenter',1,'landing','Published Apps','2017-07-12 08:56:19','2017-07-12 08:56:19','appcenter','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"noPermissionMessage\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"name\":\"Published Apps\",\"description\":\"\",\"postProcessorRunOn\":\"both\",\"permission\":{\"className\":\"\",\"properties\":{}},\"id\":\"landing\",\"postProcessor\":{\"className\":\"\",\"properties\":{}},\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"tableName\":\"appcenter\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"autoPopulate\":\"\",\"id\":\"admin_div\",\"label\":\"\",\"value\":\"<style>\\n#appcenter_admin {\\n    text-align: center;\\n}\\n#appcenter_admin a {\\n    background: white;\\n    border: solid 1px #ddd;\\n    cursor: pointer;\\n    text-decoration: none;\\n    color: #555;\\n    margin-right: 5px;\\n    padding: 10px;\\n    box-shadow: 0 1px 1.5px 1px rgba(0,0,0,.12);\\n    border-radius: 4px;\\n    font-size: 14px;\\n    font-weight: 400;\\n    line-height: 1.42857143;\\n    display: inline-block;\\n}\\n@media (max-width:540px) {\\n    #appcenter_admin a {\\n        margin-top: -20px;\\n        margin-bottom: 20px;\\n    }\\n}\\n</style>\\n<div id=\\\"appcenter_admin\\\">\\n    <a href=\\\"#\\\" onclick=\\\"appCreate();return false\\\">Design New App</a>\\n    <a href=\\\"#\\\" onclick=\\\"appImport();return false\\\">Import App</a>\\n    <a href=\\\"#\\\" onclick=\'AdminBar.showQuickOverlay(\\\"/jw/web/desktop/marketplace/app?url=\\\" + encodeURIComponent(\\\"https://marketplace.joget.org\\\"));return false\'>Download from Marketplace</a>\\n</div>\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"regex\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"permission\":{\"className\":\"org.joget.plugin.enterprise.AdminUserviewPermission\",\"properties\":{}},\"id\":\"admin_section\",\"label\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"autoPopulate\":\"\",\"id\":\"published_apps\",\"label\":\"\",\"value\":\"<link rel=\\\"stylesheet\\\" href=\\\"/jw/css/appCenter.css\\\" />\\n<div id=\\\"main-action-help\\\"><i class=\\\"icon-info-sign\\\"></i></div>\\n<div id=\\\"search\\\"></div>\\n<div id=\\\"apps\\\"></div>\\n<p>\\n<script src=\\\"/jw/js/appCenter.js\\\"></script>\\n<script>\\nAppCenter.searchFilter($(\\\"#search\\\"), $(\\\"#apps\\\")); \\nAppCenter.loadPublishedApps(\\\"#apps\\\");\\n</script>\\n</p>\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"regex\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"permission\":{\"className\":\"\",\"properties\":{}},\"id\":\"apps_section\",\"label\":\"\",\"join\":\"\",\"reverse\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}}}}]}',NULL),('crm',1,'crm_account','Account Form','2017-07-12 08:56:12','2017-07-12 08:56:12','crm_account','{\n    \"className\": \"org.joget.apps.form.model.Form\",\n    \"properties\": {\n        \"id\": \"crm_account\",\n        \"loadBinder\": {\n            \"className\": \"org.joget.apps.form.lib.WorkflowFormBinder\",\n            \"properties\": {}\n        },\n        \"tableName\": \"crm_account\",\n        \"description\": \"\",\n        \"name\": \"Account Form\",\n        \"storeBinder\": {\n            \"className\": \"org.joget.apps.form.lib.WorkflowFormBinder\",\n            \"properties\": {}\n        }\n    },\n    \"elements\": [\n        {\n            \"elements\": [\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"id\",\n                                \"label\": \"Account ID\",\n                                \"size\": \"\",\n                                \"readonly\": \"\",\n                                \"validator\": {\n                                    \"className\": \"org.joget.apps.form.lib.DefaultValidator\",\n                                    \"properties\": {\n                                        \"mandatory\": \"true\",\n                                        \"type\": \"\"\n                                    }\n                                },\n                                \"workflowVariable\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"accountName\",\n                                \"label\": \"Account Name\",\n                                \"size\": \"\",\n                                \"readonly\": \"\",\n                                \"validator\": {\n                                    \"className\": \"org.joget.apps.form.lib.DefaultValidator\",\n                                    \"properties\": {\n                                        \"mandatory\": \"true\",\n                                        \"type\": \"\"\n                                    }\n                                },\n                                \"workflowVariable\": \"\"\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"99%\"\n                    }\n                }\n            ],\n            \"className\": \"org.joget.apps.form.model.Section\",\n            \"properties\": {\n                \"id\": \"account_details\",\n                \"loadBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"visibilityControl\": \"\",\n                \"visibilityValue\": \"\",\n                \"storeBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"label\": \"Account Details\"\n            }\n        },\n        {\n            \"elements\": [\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextArea\",\n                            \"properties\": {\n                                \"id\": \"address\",\n                                \"cols\": \"20\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"Address\",\n                                \"readonly\": \"\",\n                                \"rows\": \"5\"\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"49%\"\n                    }\n                },\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"city\",\n                                \"workflowVariable\": \"\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"City\",\n                                \"readonly\": \"\",\n                                \"size\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"state\",\n                                \"workflowVariable\": \"\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"State\",\n                                \"readonly\": \"\",\n                                \"size\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.SelectBox\",\n                            \"properties\": {\n                                \"id\": \"country\",\n                                \"workflowVariable\": \"\",\n                                \"optionsBinder\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"Country\",\n                                \"multiple\": \"\",\n                                \"readonly\": \"\",\n                                \"size\": \"\",\n                                \"options\": [\n                                    {\n                                        \"value\": \"\",\n                                        \"label\": \"\"\n                                    },\n                                    {\n                                        \"value\": \"local\",\n                                        \"label\": \"Local\"\n                                    },\n                                    {\n                                        \"value\": \"international\",\n                                        \"label\": \"International\"\n                                    }\n                                ]\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"49%\"\n                    }\n                }\n            ],\n            \"className\": \"org.joget.apps.form.model.Section\",\n            \"properties\": {\n                \"id\": \"address\",\n                \"loadBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"visibilityControl\": \"\",\n                \"visibilityValue\": \"\",\n                \"storeBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"label\": \"Address Details\"\n            }\n        }\n    ]\n}',NULL),('crm',1,'crm_contact','Contact Form','2017-07-12 08:56:12','2017-07-12 08:56:12','crm_contact','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_contact\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_contact\",\"name\":\"Contact Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"account\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"extraCondition\":\"\",\"labelColumn\":\"accountName\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[]}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"fullName\",\"workflowVariable\":\"\",\"readonlyLabel\":\"\",\"maxlength\":\"\",\"encryption\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"message\":\"\",\"custom-regex\":\"\",\"mandatory\":\"true\",\"type\":\"\"}},\"value\":\"\",\"label\":\"First Name\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"lastName\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Last Name\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"addressAvailable\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Address Available\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"no\",\"label\":\"No\"},{\"value\":\"yes\",\"label\":\"Yes\"}]}},{\"className\":\"org.joget.apps.form.lib.FileUpload\",\"properties\":{\"id\":\"photo\",\"label\":\"Photo\",\"readonly\":\"\",\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"contact_details\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Contact Details\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"id\":\"address\",\"cols\":\"20\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{}},\"label\":\"Address\",\"readonly\":\"\",\"rows\":\"5\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"city\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"City\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"state\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"State\",\"readonly\":\"\",\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"address_details\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"addressAvailable\",\"visibilityValue\":\"yes\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Address Details\"}}]}',NULL),('crm',1,'crm_opportunity','Opportunity Form','2017-07-12 08:56:12','2017-07-12 08:56:12','crm_opportunity','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Opportunity Form\",\"id\":\"crm_opportunity\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_opportunity\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"type\":\"\",\"mandatory\":\"true\"}},\"workflowVariable\":\"\",\"id\":\"title\",\"label\":\"Title\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"description\",\"label\":\"Description\",\"rows\":\"5\",\"cols\":\"15\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"workflowVariable\":\"\",\"id\":\"amount\",\"label\":\"Amount\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"controlField\":\"\",\"size\":\"\",\"readonly\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"options\":[{\"label\":\"Open\",\"value\":\"open\",\"grouping\":\"\"},{\"label\":\"Won\",\"value\":\"won\",\"grouping\":\"\"},{\"label\":\"Lost\",\"value\":\"lost\",\"grouping\":\"\"}],\"multiple\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"workflowVariable\":\"\",\"id\":\"stage\",\"label\":\"Stage\",\"value\":\"\",\"readonlyLabel\":\"\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"source\",\"label\":\"Source\",\"options\":[{\"value\":\"direct\",\"label\":\"Direct\",\"grouping\":\"\"},{\"value\":\"indirect\",\"label\":\"Indirect\",\"grouping\":\"\"}],\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"value\":\"\",\"multiple\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"controlField\":\"\",\"size\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"workflowVariable\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"opportunity\",\"label\":\"Opportunity\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{}},\"multiple\":\"\",\"options\":[{\"label\":\"Yes\",\"value\":\"yes\"},{\"label\":\"No\",\"value\":\"no\"}],\"workflowVariable\":\"\",\"id\":\"newAccount\",\"label\":\"New Account\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"accountChoice\",\"label\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"labelColumn\":\"accountName\",\"extraCondition\":\"\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"multiple\":\"\",\"options\":[],\"workflowVariable\":\"\",\"id\":\"account\",\"label\":\"Account\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"newAccount\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"account_existing\",\"label\":\"Existing Account\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"no\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"parentSubFormId\":\"account\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"readonly\":\"\",\"formDefId\":\"crm_account\",\"subFormParentId\":\"\",\"id\":\"account\",\"label\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}}}},{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"script1\",\"label\":\"\",\"value\":\"<script>\\nvar val = $(\\\"#account_crm_accountid\\\").val();\\nif (val != \'\') {\\n    $(\\\"#newAccount\\\").val(\\\"no\\\");\\n    $(\\\"#newAccount\\\").trigger(\\\"change\\\");\\n}\\n</script>\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"newAccount\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"account_new\",\"label\":\"Account\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"yes\"}}]}',NULL),('crm',1,'crm_proposal_approval_form','Proposal Approval Form','2017-07-12 08:56:12','2017-07-12 08:56:12','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Approval Form\",\"id\":\"crm_proposal_approval_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"parentSubFormId\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"readonly\":\"true\",\"formDefId\":\"crm_proposal_form\",\"subFormParentId\":\"\",\"id\":\"proposal\",\"label\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}}}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Approval\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"status\",\"label\":\"Status\",\"options\":[{\"value\":\"Approved\",\"label\":\"Approved\",\"grouping\":\"\"},{\"value\":\"Resubmit\",\"label\":\"Resubmit\",\"grouping\":\"\"},{\"value\":\"Rejected\",\"label\":\"Rejected\",\"grouping\":\"\"}],\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"value\":\"\",\"multiple\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"mandatory\":\"true\",\"type\":\"\",\"custom-regex\":\"\",\"message\":\"\"}},\"controlField\":\"\",\"size\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"workflowVariable\":\"status\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"comments\",\"label\":\"Approver Comments\",\"rows\":\"5\",\"cols\":\"20\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section2\",\"label\":\"Approver Action\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),('crm',1,'crm_proposal_form','Proposal Form','2017-07-12 08:56:12','2017-07-12 08:56:12','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Form\",\"id\":\"crm_proposal_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.IdGeneratorField\",\"properties\":{\"hidden\":\"true\",\"format\":\"????\",\"workflowVariable\":\"\",\"envVariable\":\"refNo\",\"id\":\"refNo\",\"label\":\"Reference No\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"labelColumn\":\"accountName\",\"extraCondition\":\"\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"multiple\":\"\",\"options\":[],\"workflowVariable\":\"\",\"id\":\"account\",\"label\":\"Account\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"type\":\"\",\"mandatory\":\"true\"}},\"workflowVariable\":\"\",\"id\":\"title\",\"label\":\"Title\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"workflowVariable\":\"\",\"id\":\"description\",\"label\":\"Description\",\"placeholder\":\"\",\"rows\":\"5\",\"value\":\"\",\"cols\":\"60\",\"readonlyLabel\":\"\"}},{\"className\":\"org.joget.apps.form.lib.FileUpload\",\"properties\":{\"attachment\":\"true\",\"readonly\":\"\",\"size\":\"\",\"id\":\"attachment\",\"label\":\"Attachment\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Form\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),('crm',1,'crm_proposal_resubmit_form','Proposal Resubmit Form','2017-07-12 08:56:12','2017-07-12 08:56:12','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Resubmit Form\",\"id\":\"crm_proposal_resubmit_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"parentSubFormId\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"readonly\":\"true\",\"formDefId\":\"crm_proposal_approval_form\",\"subFormParentId\":\"\",\"id\":\"approval\",\"label\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}}}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Resubmit\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"parentSubFormId\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"readonly\":\"\",\"formDefId\":\"crm_proposal_form\",\"subFormParentId\":\"\",\"id\":\"proposal\",\"label\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}}}},{\"className\":\"org.joget.apps.form.lib.HiddenField\",\"properties\":{\"id\":\"status\",\"value\":\"Pending\",\"useDefaultWhenEmpty\":\"\",\"workflowVariable\":\"status\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section2\",\"label\":\"Proposal Resubmission\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),('crm',1,'crm_proposal_sending_form','Proposal Sending Form','2017-07-12 08:56:12','2017-07-12 08:56:12','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_proposal_sending_form\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\",\"name\":\"Proposal Sending Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"approval\",\"formDefId\":\"crm_proposal_approval_form\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"\",\"readonly\":\"true\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section1\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Proposal Sending\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"id\":\"notes\",\"cols\":\"20\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Notes\",\"readonly\":\"\",\"rows\":\"5\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section2\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"\"}}]}',NULL);
/*!40000 ALTER TABLE `app_form` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_form_data_audit_trail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_form_data_audit_trail` (
  `id` varchar(255) NOT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `appVersion` varchar(255) DEFAULT NULL,
  `formId` varchar(255) DEFAULT NULL,
  `tableName` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `action` varchar(255) DEFAULT NULL,
  `data` longtext,
  `datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_form_data_audit_trail`
--

LOCK TABLES `app_form_data_audit_trail` WRITE;
/*!40000 ALTER TABLE `app_form_data_audit_trail` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_form_data_audit_trail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_message` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) NOT NULL,
  `messageKey` varchar(255) DEFAULT NULL,
  `locale` varchar(255) DEFAULT NULL,
  `message` text,
  PRIMARY KEY (`appId`,`appVersion`,`ouid`),
  KEY `FKEE346FE9462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FKEE346FE9462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_message`
--

LOCK TABLES `app_message` WRITE;
/*!40000 ALTER TABLE `app_message` DISABLE KEYS */;
INSERT INTO `app_message` VALUES ('appcenter',1,'<i class=\'icon-home\'></i> Home_zh_CN','<i class=\'icon-home\'></i> Home','zh_CN','È¶ñÈ°µ'),('appcenter',1,'<i class=\'icon-home\'></i> Home_zh_TW','<i class=\'icon-home\'></i> Home','zh_TW','È¶ñÈ†Å'),('appcenter',1,'App Center_zh_CN','App Center','zh_CN','Â∫îÁî®‰∏≠ÂøÉ'),('appcenter',1,'App Center_zh_TW','App Center','zh_TW','ÊáâÁî®‰∏≠ÂøÉ'),('appcenter',1,'Published Apps_zh_CN','Published Apps','zh_CN','Êú¨Âú∞Â∑≤ÂèëÂ∏É'),('appcenter',1,'Published Apps_zh_TW','Published Apps','zh_TW','Êú¨Âú∞Â∑≤ÁôºÂ∏É');
/*!40000 ALTER TABLE `app_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_package` (
  `packageId` varchar(255) NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `appVersion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`packageId`,`packageVersion`),
  KEY `FK852EA428462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FK852EA428462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package`
--

LOCK TABLES `app_package` WRITE;
/*!40000 ALTER TABLE `app_package` DISABLE KEYS */;
INSERT INTO `app_package` VALUES ('crm',1,'Customer Relationship Management','2017-07-12 08:56:14','2017-07-12 08:56:14','crm',1);
/*!40000 ALTER TABLE `app_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package_activity_form`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_package_activity_form` (
  `processDefId` varchar(255) NOT NULL,
  `activityDefId` varchar(255) NOT NULL,
  `packageId` varchar(255) NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `formId` varchar(255) DEFAULT NULL,
  `formUrl` varchar(255) DEFAULT NULL,
  `formIFrameStyle` varchar(255) DEFAULT NULL,
  `autoContinue` bit(1) DEFAULT NULL,
  `disableSaveAsDraft` bit(1) DEFAULT NULL,
  PRIMARY KEY (`processDefId`,`activityDefId`,`packageId`,`packageVersion`),
  KEY `FKA8D741D5F255BCC` (`packageId`,`packageVersion`),
  CONSTRAINT `FKA8D741D5F255BCC` FOREIGN KEY (`packageId`, `packageVersion`) REFERENCES `app_package` (`packageId`, `packageVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_activity_form`
--

LOCK TABLES `app_package_activity_form` WRITE;
/*!40000 ALTER TABLE `app_package_activity_form` DISABLE KEYS */;
INSERT INTO `app_package_activity_form` VALUES ('process1','activity1','crm',1,'process1::activity1','SINGLE','crm_proposal_resubmit_form',NULL,NULL,'\0',''),('process1','approve_proposal','crm',1,'process1::approve_proposal','SINGLE','crm_proposal_approval_form',NULL,NULL,'\0',''),('process1','runProcess','crm',1,'process1::runProcess','SINGLE','crm_proposal_form',NULL,NULL,'\0','\0'),('process1','send_proposal','crm',1,'process1::send_proposal','SINGLE','crm_proposal_sending_form',NULL,NULL,'\0','');
/*!40000 ALTER TABLE `app_package_activity_form` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package_activity_plugin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_package_activity_plugin` (
  `processDefId` varchar(255) NOT NULL,
  `activityDefId` varchar(255) NOT NULL,
  `packageId` varchar(255) NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) DEFAULT NULL,
  `pluginName` varchar(255) DEFAULT NULL,
  `pluginProperties` text,
  PRIMARY KEY (`processDefId`,`activityDefId`,`packageId`,`packageVersion`),
  KEY `FKADE8644C5F255BCC` (`packageId`,`packageVersion`),
  CONSTRAINT `FKADE8644C5F255BCC` FOREIGN KEY (`packageId`, `packageVersion`) REFERENCES `app_package` (`packageId`, `packageVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_activity_plugin`
--

LOCK TABLES `app_package_activity_plugin` WRITE;
/*!40000 ALTER TABLE `app_package_activity_plugin` DISABLE KEYS */;
INSERT INTO `app_package_activity_plugin` VALUES ('process1','tool1','crm',1,'process1::tool1','org.joget.apps.app.lib.EmailTool','{\"toSpecific\":\"#performer.runProcess.email#\",\"toParticipantId\":\"\",\"cc\":\"\",\"bcc\":\"\",\"subject\":\"Proposal Approved: #form.crm_proposal.title#\",\"message\":\"Proposal Approved\\n\\nRef No: #form.crm_proposal.refNo#\\nTitle: #form.crm_proposal.title#\",\"isHtml\":\"\",\"from\":\"\",\"host\":\"\",\"port\":\"\",\"security\":\"\",\"username\":\"\",\"password\":\"\",\"formDefId\":\"\",\"fields\":[],\"files\":[]}'),('process1','tool2','crm',1,'process1::tool2','org.joget.apps.app.lib.EmailTool','{\"toSpecific\":\"#performer.runProcess.email#\",\"toParticipantId\":\"\",\"cc\":\"\",\"bcc\":\"\",\"subject\":\"Proposal Rejected: #form.crm_proposal.title#\",\"message\":\"Proposal Rejected\\n\\nRef No: #form.crm_proposal.refNo#\\nTitle: #form.crm_proposal.title#\",\"isHtml\":\"\",\"from\":\"\",\"host\":\"\",\"port\":\"\",\"security\":\"\",\"username\":\"\",\"password\":\"\",\"formDefId\":\"\",\"fields\":[],\"files\":[]}'),('process1','tool3','crm',1,'process1::tool3','org.joget.apps.app.lib.EmailTool','{\"toSpecific\":\"#performer.runProcess.email#\",\"toParticipantId\":\"\",\"cc\":\"\",\"bcc\":\"\",\"subject\":\"Resubmit Proposal: #form.crm_proposal.title#\",\"message\":\"Approver has requested you to resubmit your proposal.\\n\\nRef No: #form.crm_proposal.refNo#\\nTitle: #form.crm_proposal.title#\",\"isHtml\":\"\",\"from\":\"\",\"host\":\"\",\"port\":\"\",\"security\":\"\",\"username\":\"\",\"password\":\"\",\"formDefId\":\"\",\"fields\":[],\"files\":[]}'),('process1','tool4','crm',1,'process1::tool4','org.joget.apps.app.lib.EmailTool','{\"toSpecific\":\"\",\"toParticipantId\":\"approver\",\"cc\":\"\",\"bcc\":\"\",\"subject\":\"Proposal For Approval: #form.crm_proposal.title#\",\"message\":\"Requester has requested you to approve the proposal.\\n\\nRef No: #form.crm_proposal.refNo#\\nTitle: #form.crm_proposal.title#\",\"isHtml\":\"\",\"from\":\"\",\"host\":\"\",\"port\":\"\",\"security\":\"\",\"username\":\"\",\"password\":\"\",\"formDefId\":\"\",\"fields\":[],\"files\":[]}');
/*!40000 ALTER TABLE `app_package_activity_plugin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_package_participant` (
  `processDefId` varchar(255) NOT NULL,
  `participantId` varchar(255) NOT NULL,
  `packageId` varchar(255) NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `value` text,
  `pluginProperties` text,
  PRIMARY KEY (`processDefId`,`participantId`,`packageId`,`packageVersion`),
  KEY `FK6D7BF59C5F255BCC` (`packageId`,`packageVersion`),
  CONSTRAINT `FK6D7BF59C5F255BCC` FOREIGN KEY (`packageId`, `packageVersion`) REFERENCES `app_package` (`packageId`, `packageVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_participant`
--

LOCK TABLES `app_package_participant` WRITE;
/*!40000 ALTER TABLE `app_package_participant` DISABLE KEYS */;
INSERT INTO `app_package_participant` VALUES ('process1','approver','crm',1,'process1::approver','user','admin',NULL),('process1','processStartWhiteList','crm',1,'process1::processStartWhiteList','role','loggedInUser',NULL),('process1','requester','crm',1,'process1::requester','requester','runProcess',NULL);
/*!40000 ALTER TABLE `app_package_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_plugin_default`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_plugin_default` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) NOT NULL,
  `pluginName` varchar(255) DEFAULT NULL,
  `pluginDescription` text,
  `pluginProperties` text,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK7A835713462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FK7A835713462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_plugin_default`
--

LOCK TABLES `app_plugin_default` WRITE;
/*!40000 ALTER TABLE `app_plugin_default` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_plugin_default` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_report_activity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_report_activity` (
  `uuid` varchar(255) NOT NULL,
  `activityDefId` varchar(255) DEFAULT NULL,
  `activityName` varchar(255) DEFAULT NULL,
  `processUid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK5E33D79C918F93D` (`processUid`),
  CONSTRAINT `FK5E33D79C918F93D` FOREIGN KEY (`processUid`) REFERENCES `app_report_process` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_report_activity`
--

LOCK TABLES `app_report_activity` WRITE;
/*!40000 ALTER TABLE `app_report_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_report_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_report_activity_instance`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_report_activity_instance` (
  `instanceId` varchar(255) NOT NULL,
  `performer` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `nameOfAcceptedUser` varchar(255) DEFAULT NULL,
  `assignmentUsers` text,
  `due` datetime DEFAULT NULL,
  `createdTime` datetime DEFAULT NULL,
  `startedTime` datetime DEFAULT NULL,
  `finishTime` datetime DEFAULT NULL,
  `delay` bigint(20) DEFAULT NULL,
  `timeConsumingFromCreatedTime` bigint(20) DEFAULT NULL,
  `timeConsumingFromStartedTime` bigint(20) DEFAULT NULL,
  `activityUid` varchar(255) DEFAULT NULL,
  `processInstanceId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`instanceId`),
  KEY `FK9C6ABDD8B06E2043` (`activityUid`),
  KEY `FK9C6ABDD8D4610A90` (`processInstanceId`),
  CONSTRAINT `FK9C6ABDD8B06E2043` FOREIGN KEY (`activityUid`) REFERENCES `app_report_activity` (`uuid`),
  CONSTRAINT `FK9C6ABDD8D4610A90` FOREIGN KEY (`processInstanceId`) REFERENCES `app_report_process_instance` (`instanceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_report_activity_instance`
--

LOCK TABLES `app_report_activity_instance` WRITE;
/*!40000 ALTER TABLE `app_report_activity_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_report_activity_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_report_app`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_report_app` (
  `uuid` varchar(255) NOT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `appVersion` varchar(255) DEFAULT NULL,
  `appName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_report_app`
--

LOCK TABLES `app_report_app` WRITE;
/*!40000 ALTER TABLE `app_report_app` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_report_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_report_package`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_report_package` (
  `uuid` varchar(255) NOT NULL,
  `packageId` varchar(255) DEFAULT NULL,
  `packageName` varchar(255) DEFAULT NULL,
  `packageVersion` varchar(255) DEFAULT NULL,
  `appUid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FKBD580A19E475ABC` (`appUid`),
  CONSTRAINT `FKBD580A19E475ABC` FOREIGN KEY (`appUid`) REFERENCES `app_report_app` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_report_package`
--

LOCK TABLES `app_report_package` WRITE;
/*!40000 ALTER TABLE `app_report_package` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_report_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_report_process`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_report_process` (
  `uuid` varchar(255) NOT NULL,
  `processDefId` varchar(255) DEFAULT NULL,
  `processName` varchar(255) DEFAULT NULL,
  `packageUid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FKDAFFF442D40695DD` (`packageUid`),
  CONSTRAINT `FKDAFFF442D40695DD` FOREIGN KEY (`packageUid`) REFERENCES `app_report_package` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_report_process`
--

LOCK TABLES `app_report_process` WRITE;
/*!40000 ALTER TABLE `app_report_process` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_report_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_report_process_instance`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_report_process_instance` (
  `instanceId` varchar(255) NOT NULL,
  `requester` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `due` datetime DEFAULT NULL,
  `startedTime` datetime DEFAULT NULL,
  `finishTime` datetime DEFAULT NULL,
  `delay` bigint(20) DEFAULT NULL,
  `timeConsumingFromStartedTime` bigint(20) DEFAULT NULL,
  `processUid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`instanceId`),
  KEY `FK351D7BF2918F93D` (`processUid`),
  CONSTRAINT `FK351D7BF2918F93D` FOREIGN KEY (`processUid`) REFERENCES `app_report_process` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_report_process_instance`
--

LOCK TABLES `app_report_process_instance` WRITE;
/*!40000 ALTER TABLE `app_report_process_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_report_process_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_resource`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_resource` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) NOT NULL,
  `filesize` bigint(20) DEFAULT NULL,
  `permissionClass` varchar(255) DEFAULT NULL,
  `permissionProperties` longtext,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  CONSTRAINT `FK_nnvkg0h6yy8o3f4yjhd20ury0` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_resource`
--

LOCK TABLES `app_resource` WRITE;
/*!40000 ALTER TABLE `app_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_userview`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_userview` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` text,
  `json` longtext,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FKE411D54E462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FKE411D54E462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_userview`
--

LOCK TABLES `app_userview` WRITE;
/*!40000 ALTER TABLE `app_userview` DISABLE KEYS */;
INSERT INTO `app_userview` VALUES ('appcenter',1,'v','Joget Workflow',NULL,'{\"className\":\"org.joget.apps.userview.model.Userview\",\"properties\":{\"id\":\"v\",\"name\":\"Joget Workflow\",\"description\":\"\",\"welcomeMessage\":\"#date.EEE, d MMM yyyy#\",\"logoutText\":\"Logout\",\"footerMessage\":\"Powered by Joget\"},\"setting\":{\"properties\":{\"theme\":{\"className\":\"org.joget.plugin.enterprise.UniversalTheme\",\"properties\":{\"horizontal_menu\":\"horizontal_inline\",\"themeScheme\":\"light\",\"primaryColor\":\"custom\",\"customPrimary\":\"#7CA6D8\",\"customPrimaryDark\":\"\",\"customPrimaryLight\":\"\",\"accentColor\":\"BLUE\",\"fontColor\":\"WHITE\",\"fav_icon\":\"\",\"logo\":\"\\/jw\\/images\\/v3\\/logo.png\",\"profile\":\"\",\"inbox\":\"all\",\"shortcutLinkLabel\":\"Shortcut\",\"shortcut\":[],\"userMenu\":[],\"enableResponsiveSwitch\":\"true\",\"removeAssignmentTitle\":\"\",\"homeAttractBanner\":\"<div id=\\\"banner\\\"><h1>App Center<\\/h1><\\/div>\",\"css\":\"\\/* banner image and title *\\/\\n#banner {\\n    background: url(\\/jw\\/home\\/banner.jpg);\\n    background-size: cover;\\n    height: 240px;\\n    width: 100%;\\n}\\n#banner h1 {\\n    margin-top: 0px;\\n    padding-top: 20px;\\n    color: white;\\n    text-align: center;\\n}\\nbody#home ul.breadcrumb {\\n    display: none;\\n}\\n\\n\\/* admin buttons *\\/\\n#appcenter_admin {\\n    position: absolute;\\n    top: -120px;\\n    text-align: center;\\n    display: block;\\n    margin: auto;\\n    width: 100%;\\n}\\n#admin_section, #admin_section .form-column, #admin_section .form-cell, #admin_section .form-cell-value {\\n    padding: 0px;\\n    margin: 0px;\\n    height: 0px;\\n}\\n@media (max-width: 540px) {\\n    #appcenter_admin {    \\n        top: -150px;\\n    }\\n}\\n\",\"js\":\"\",\"loginPageTop\":\"\",\"loginPageBottom\":\"\"}},\"userviewDescription\":\"\",\"hideThisUserviewInAppCenter\":\"true\",\"permission\":{\"className\":\"\",\"properties\":{}}}},\"categories\":[{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-EE74E4F4426241BD9BC3BC73B1D24AC7\",\"label\":\"<i class=\'icon-home\'><\\/i> Home\"},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"28F7ADF73F204C8BBAAD32CF26587AE2\",\"customId\":\"home\",\"label\":\"Published Apps\",\"formId\":\"landing\",\"showInPopupDialog\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"\",\"redirectUrlOnCancel\":\"\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}]}]}','2017-07-12 08:56:19','2017-07-12 08:56:19'),('crm',1,'crm_userview_sales','Customer Relationship Management',NULL,'{\"className\":\"org.joget.apps.userview.model.Userview\",\"properties\":{\"id\":\"crm_userview_sales\",\"name\":\"Customer Relationship Management\",\"description\":\"\",\"welcomeMessage\":\"Sales Force Automation\",\"logoutText\":\"Logout\",\"footerMessage\":\"Powered by Joget\"},\"layout\":{\"className\":\"org.joget.apps.userview.model.UserviewLayout\",\"properties\":{\"customHeader\":\"\",\"customFooter\":\"\",\"beforeMenu\":\"\",\"afterMenu\":\"\",\"theme\":{\"className\":\"org.joget.apps.userview.lib.DefaultTheme\",\"properties\":{\"css\":\"\",\"js\":\"\"}}}},\"categories\":[{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-9BE91A55FAAC4B5098841EA9E1994BE6\",\"label\":\"Home\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.plugin.enterprise.AnonymousUserviewPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"id\":\"welcome\",\"customId\":\"welcome\",\"label\":\"Welcome\",\"content\":\"<div id=\\\"left_content\\\">\\n<div style=\\\"float: left; width: 400px; margin-right: 10px;\\\">\\n<h4 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-size: inherit; line-height: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; font-weight: bold; color: #042c54;\\\">More Leads, More Sales, More Customers<\\/h4>\\n<h1 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font: inherit; vertical-align: baseline; color: #1f4282;\\\"><span style=\\\"font-size: large;\\\"><strong>Business&nbsp;Customer Relationship Management<\\/strong><\\/span><\\/h1>\\n<p style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\">CRM helps your business communicate with prospects, share sales information, close deals and keep customers happy.<img src=\\\"http:\\/\\/www.joget.org\\/images\\/demo\\/phone_pad.png\\\" alt=\\\"\\\" width=\\\"382\\\" height=\\\"302\\\" \\/><\\/p>\\n<p style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\"><\\/p>\\n<p style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\"><strong>Tips<\\/strong><\\/p>\\n<ul>\\n<li style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\">You can now store the global <a href=\\\"\\/jw\\/web\\/console\\/setting\\/general\\\">email SMTP configuration<\\/a> in Joget Workflow v6 System Settings.<\\/li>\\n<li style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\">Do remember to provide an email address for \'admin\' and other users in <a href=\\\"\\/jw\\/web\\/console\\/directory\\/users\\\">Setup Users<\\/a> for your new installation.<\\/li>\\n<\\/ul>\\n<p style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\"><\\/p>\\n<\\/div>\\n<\\/div>\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-7650DEEFC4CC4332AC25871B65BBDD48\",\"label\":\"Accounts\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"id\":\"384344BD3E2946D097C6F5F17540C377\",\"customId\":\"account_list\",\"label\":\"Account List\",\"datalistId\":\"crm_account_list\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"selectionType\":\"multiple\",\"checkboxPosition\":\"left\",\"customHeader\":\"<h2>Account List<\\/h2>\",\"customFooter\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"account_form\",\"customId\":\"account_new\",\"label\":\"New Account\",\"formId\":\"crm_account\",\"showInPopupDialog\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"account_list\",\"redirectUrlOnCancel\":\"account_list\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-E77D2050680D4DB0A85A5C0C3AC1C083\",\"label\":\"Contacts\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"id\":\"D86B740C970C4B08B4D5CCD3DC0E9503\",\"customId\":\"contact_list\",\"label\":\"Contact List\",\"datalistId\":\"crm_contact_list\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"selectionType\":\"multiple\",\"checkboxPosition\":\"left\",\"customHeader\":\"<h2>Contact List<\\/h2>\",\"customFooter\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"contact-form\",\"customId\":\"contact_new\",\"label\":\"New Contact\",\"formId\":\"crm_contact\",\"showInPopupDialog\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"true\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"contact_list\",\"redirectUrlOnCancel\":\"contact_list\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-A12DBDB14B4447A984E6095B77F28B42\",\"label\":\"Opportunities\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"id\":\"A074397ABEA94CF78E2E8FA0843AB97B\",\"customId\":\"opportunity_list\",\"label\":\"Opportunities List\",\"datalistId\":\"crm_opportunity_list\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"selectionType\":\"multiple\",\"checkboxPosition\":\"left\",\"customHeader\":\"<h2>Opportunities List<\\/h2>\",\"customFooter\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"0C7E36768A2F46BB945CEC50E62E0BE8\",\"customId\":\"opportunity_new\",\"label\":\"New Opportunity\",\"formId\":\"crm_opportunity\",\"showInPopupDialog\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"opportunity_list\",\"redirectUrlOnCancel\":\"opportunity_list\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-78EC0B8A1E8E483A93770714BB0D6F6E\",\"label\":\"Proposal Process\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.InboxMenu\",\"properties\":{\"id\":\"AA1445B29D904408B3F2B1B36E469E16\",\"customId\":\"proposalinbox\",\"label\":\"Proposals Inbox\",\"appFilter\":\"all\",\"processId\":\"\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"list-customHeader\":\"<h1>Proposals Inbox<\\/h1>\",\"list-customFooter\":\"\",\"assignment-customHeader\":\"\",\"assignment-customFooter\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"id\":\"8EEE29E4A7434017B95C4128DA4CAE43\",\"customId\":\"proposal_all\",\"label\":\"Proposals Lists\",\"datalistId\":\"Proposal\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"selectionType\":\"multiple\",\"checkboxPosition\":\"left\",\"customHeader\":\"<h1>Proposals Lists<\\/h1>\",\"customFooter\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.RunProcess\",\"properties\":{\"id\":\"2D27B3875F234315A7A3562BD0E35AB2\",\"customId\":\"proposal_process\",\"label\":\"New Proposal Process\",\"processDefId\":\"process1\",\"runProcessDirectly\":\"Yes\",\"showInPopupDialog\":\"\",\"runProcessSubmitLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"proposalinbox\",\"fieldPassover\":\"proposalinbox\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}]},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-25EBE99EB6B04DD8B6A73B6AD564057D\",\"label\":\"Hidden\",\"hide\":\"yes\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"comment\":\"\"},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"65DDACF79C3C47FE82AB4D25ED9F5A3D\",\"customId\":\"proposal_form_\",\"label\":\"Proposal Form\",\"formId\":\"crm_proposal_sending_form\",\"showInPopupDialog\":\"\",\"readonly\":\"Yes\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"proposal_all\",\"redirectUrlOnCancel\":\"proposal_all\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}]},{\"menus\":[{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"id\":\"ED8EA9FDB086486C989E6460ED5BA59D\",\"customId\":\"jogetworkflowresources\",\"label\":\"Joget Workflow Resources\",\"content\":\"<h1>Joget Workflow Resources<\\/h1>\\n<ul>\\n<li><a href=\\\"http:\\/\\/joget.org\\\">Joget Workflow Products<\\/a><\\/li>\\n<li><a href=\\\"https:\\/\\/www.youtube.com\\/user\\/jogetworkflow\\\">Joget Workflow Videos<\\/a><\\/li>\\n<li><a href=\\\"https:\\/\\/cloud.joget.com\\\">Joget Workflow On-Demand Cloud<\\/a><\\/li>\\n<li><a href=\\\"https:\\/\\/marketplace.joget.org\\\">Joget Workflow Marketplace<\\/a><\\/li>\\n<li><a href=\\\"https:\\/\\/translate.joget.org\\\">Joget Workflow Translations<\\/a><\\/li>\\n<li><a href=\\\"http:\\/\\/answer.joget.org\\\">Joget Workflow Answers<\\/a><\\/li>\\n<li><a href=\\\"http:\\/\\/community.joget.org\\\">Joget Workflow Knowledge Base<\\/a><\\/li>\\n<li><a href=\\\"http:\\/\\/joget.org\\/buy-enterprise-edition-license\\/\\\">Purchase Joget Workflow Enterprise License<\\/a><\\/li>\\n<li><a href=\\\"https:\\/\\/play.google.com\\/store\\/apps\\/details?id=org.joget.mobile&amp;hl=en\\\">Joget Workflow Mobile Android App<\\/a><\\/li>\\n<li><a href=\\\"https:\\/\\/itunes.apple.com\\/us\\/app\\/joget-workflow-mobile\\/id1084995640?mt=8\\\">Joget Workflow Mobile iOS App<\\/a><\\/li>\\n<\\/ul>\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"id\":\"31A2257FC59F452BAD3A5A947E6C0692\",\"customId\":\"applicense\",\"label\":\"App License\",\"content\":\"<div style=\\\"font-size: 24px; font-weight: bold; font-family: inherit;\\\">#envVariable.AppName# License<\\/div>\\n<hr size=\\\"1\\\" \\/>\\n<p><\\/p>\\n<pre style=\\\"display: block; padding: 9.5px; margin: 0 0 10px; font-size: 13px; line-height: 20px; word-break: break-all; word-wrap: break-word; white-space: pre-wrap; background-color: #f5f5f5; border: 1px solid rgba(0,0,0,0.15); -webkit-border-radius: 4px; -moz-border-radius: 4px; border-radius: 4px; font-family: Monaco,Menlo,Consolas,Courier New,monospace;\\\">Copyright #date.yyyy# Joget, Inc.\\n\\nLicensed under the Apache License, Version 2.0 (the \\\"License\\\");\\nyou may not use this file except in compliance with the License.\\nYou may obtain a copy of the License at\\n\\n    <a href=\\\"http:\\/\\/www.apache.org\\/licenses\\/LICENSE-2.0\\\" target=\\\"_blank\\\" rel=\\\"noopener noreferrer\\\">http:\\/\\/www.apache.org\\/licenses\\/LICENSE-2.0\\n<\\/a>\\nUnless required by applicable law or agreed to in writing, software\\ndistributed under the License is distributed on an \\\"AS IS\\\" BASIS,\\nWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\\nSee the License for the specific language governing permissions and\\nlimitations under the License.<\\/pre>\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-8739F2859D894A339404C2404CB9004E\",\"hide\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"label\":\"<i class=\\\"fa fa-info-circle\\\" aria-hidden=\\\"true\\\"><\\/i>Info\"}}],\"setting\":{\"className\":\"org.joget.apps.userview.model.UserviewSetting\",\"properties\":{\"theme\":{\"className\":\"org.joget.plugin.enterprise.UniversalTheme\",\"properties\":{\"horizontal_menu\":\"true\",\"themeScheme\":\"light\",\"primaryColor\":\"DARKROYALBLUE\",\"accentColor\":\"BLUE\",\"buttonColor\":\"GREY\",\"buttonTextColor\":\"WHITE\",\"menuFontColor\":\"BLACK\",\"fontColor\":\"WHITE\",\"fav_icon\":\"\",\"logo\":\"\",\"profile\":\"\",\"userImage\":\"no\",\"inbox\":\"all\",\"shortcutLinkLabel\":\"Shortcut\",\"shortcut\":[],\"userMenu\":[],\"enableResponsiveSwitch\":\"true\",\"removeAssignmentTitle\":\"true\",\"homeAttractBanner\":\"\",\"css\":\"\",\"js\":\"\",\"subheader\":\"\",\"subfooter\":\"\",\"disableHelpGuide\":\"\",\"loginPageTop\":\"\",\"loginPageBottom\":\"\"}},\"userviewDescription\":\"\",\"hideThisUserviewInAppCenter\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"__\":\"\"}}}','2017-07-12 08:56:12','2017-07-12 08:56:12');
/*!40000 ALTER TABLE `app_userview` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_department`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_department` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `organizationId` varchar(255) DEFAULT NULL,
  `hod` varchar(255) DEFAULT NULL,
  `parentId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKEEE8AA4418CEBAE1` (`organizationId`),
  KEY `FKEEE8AA44EF6BB2B7` (`parentId`),
  KEY `FKEEE8AA4480DB1449` (`hod`),
  CONSTRAINT `FKEEE8AA4418CEBAE1` FOREIGN KEY (`organizationId`) REFERENCES `dir_organization` (`id`),
  CONSTRAINT `FKEEE8AA4480DB1449` FOREIGN KEY (`hod`) REFERENCES `dir_employment` (`id`),
  CONSTRAINT `FKEEE8AA44EF6BB2B7` FOREIGN KEY (`parentId`) REFERENCES `dir_department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_department`
--

LOCK TABLES `dir_department` WRITE;
/*!40000 ALTER TABLE `dir_department` DISABLE KEYS */;
INSERT INTO `dir_department` VALUES ('D-001','CEO Office','','ORG-001','4028808127f4ef840127f5efdbfb004f',NULL),('D-002','Human Resource and Admin','','ORG-001','4028808127f4ef840127f5f41d4b0091',NULL),('D-003','Finance','','ORG-001','4028808127f4ef840127f606242400b3',NULL),('D-004','Marketing','','ORG-001','4028808127f4ef840127f5f20f36007a',NULL),('D-005','Product Development','','ORG-001','4028808127f4ef840127f5f04dc2005a',NULL),('D-006','Training and Consulting','','ORG-001','4028808127f4ef840127f5f7c5b500a5',NULL),('D-007','Support and Services','','ORG-001','4028808127fb4d350127ff78d63300d1',NULL);
/*!40000 ALTER TABLE `dir_department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_employment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_employment` (
  `id` varchar(255) NOT NULL,
  `userId` varchar(255) DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `employeeCode` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `gradeId` varchar(255) DEFAULT NULL,
  `departmentId` varchar(255) DEFAULT NULL,
  `organizationId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKC6620ADE716AE35F` (`departmentId`),
  KEY `FKC6620ADE14CE02E9` (`gradeId`),
  KEY `FKC6620ADECE539211` (`userId`),
  KEY `FKC6620ADE18CEBAE1` (`organizationId`),
  CONSTRAINT `FKC6620ADE14CE02E9` FOREIGN KEY (`gradeId`) REFERENCES `dir_grade` (`id`),
  CONSTRAINT `FKC6620ADE18CEBAE1` FOREIGN KEY (`organizationId`) REFERENCES `dir_organization` (`id`),
  CONSTRAINT `FKC6620ADE716AE35F` FOREIGN KEY (`departmentId`) REFERENCES `dir_department` (`id`),
  CONSTRAINT `FKC6620ADECE539211` FOREIGN KEY (`userId`) REFERENCES `dir_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_employment`
--

LOCK TABLES `dir_employment` WRITE;
/*!40000 ALTER TABLE `dir_employment` DISABLE KEYS */;
INSERT INTO `dir_employment` VALUES ('4028808127f4ef840127f5efdbfb004f','terry',NULL,NULL,NULL,NULL,'G-001','D-001','ORG-001'),('4028808127f4ef840127f5f04dc2005a','clark',NULL,NULL,NULL,NULL,'G-002','D-005','ORG-001'),('4028808127f4ef840127f5f11cf60068','cat',NULL,NULL,NULL,NULL,'G-003','D-005','ORG-001'),('4028808127f4ef840127f5f194e20071','tana',NULL,NULL,NULL,NULL,'G-003','D-005','ORG-001'),('4028808127f4ef840127f5f20f36007a','roy',NULL,NULL,NULL,NULL,'G-002','D-004','ORG-001'),('4028808127f4ef840127f5f319720088','etta',NULL,NULL,NULL,NULL,'G-003','D-004','ORG-001'),('4028808127f4ef840127f5f41d4b0091','sasha',NULL,NULL,NULL,NULL,'G-002','D-002','ORG-001'),('4028808127f4ef840127f5f7c5b500a5','jack',NULL,NULL,NULL,NULL,'G-002','D-006','ORG-001'),('4028808127f4ef840127f606242400b3','tina',NULL,NULL,NULL,NULL,'G-002','D-003','ORG-001'),('4028808127fb4d350127ff78d63300d1','david',NULL,NULL,NULL,NULL,'G-002','D-007','ORG-001'),('4028808127fb4d350127ff84074600f2','julia',NULL,NULL,NULL,NULL,'G-003','D-002','ORG-001');
/*!40000 ALTER TABLE `dir_employment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_employment_report_to`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_employment_report_to` (
  `employmentId` varchar(255) NOT NULL,
  `reportToId` varchar(255) NOT NULL,
  `id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`employmentId`,`reportToId`),
  KEY `FK53622945F4068416` (`reportToId`),
  KEY `FK536229452787E613` (`employmentId`),
  CONSTRAINT `FK536229452787E613` FOREIGN KEY (`employmentId`) REFERENCES `dir_employment` (`id`),
  CONSTRAINT `FK53622945F4068416` FOREIGN KEY (`reportToId`) REFERENCES `dir_employment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_employment_report_to`
--

LOCK TABLES `dir_employment_report_to` WRITE;
/*!40000 ALTER TABLE `dir_employment_report_to` DISABLE KEYS */;
INSERT INTO `dir_employment_report_to` VALUES ('4028808127f4ef840127f5f04dc2005a','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f5f04e9b005f'),('4028808127f4ef840127f5f20f36007a','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f5f20fb7007f'),('4028808127f4ef840127f5f41d4b0091','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f5f48eda009e'),('4028808127f4ef840127f5f7c5b500a5','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f5f7c60b00aa'),('4028808127f4ef840127f606242400b3','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f60624c100b8'),('4028808127fb4d350127ff78d63300d1','4028808127f4ef840127f5efdbfb004f','4028808127fb4d350127ff78d6fe00d6');
/*!40000 ALTER TABLE `dir_employment_report_to` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_grade`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_grade` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `organizationId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKBC9A49A518CEBAE1` (`organizationId`),
  CONSTRAINT `FKBC9A49A518CEBAE1` FOREIGN KEY (`organizationId`) REFERENCES `dir_organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_grade`
--

LOCK TABLES `dir_grade` WRITE;
/*!40000 ALTER TABLE `dir_grade` DISABLE KEYS */;
INSERT INTO `dir_grade` VALUES ('G-001','Board Members','','ORG-001'),('G-002','Managers','','ORG-001'),('G-003','Executives','','ORG-001');
/*!40000 ALTER TABLE `dir_grade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_group` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `organizationId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKBC9A804D18CEBAE1` (`organizationId`),
  CONSTRAINT `FKBC9A804D18CEBAE1` FOREIGN KEY (`organizationId`) REFERENCES `dir_organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_group`
--

LOCK TABLES `dir_group` WRITE;
/*!40000 ALTER TABLE `dir_group` DISABLE KEYS */;
INSERT INTO `dir_group` VALUES ('G-001','Managers','',NULL),('G-002','CxO','',NULL),('G-003','hrAdmin','',NULL);
/*!40000 ALTER TABLE `dir_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_organization`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_organization` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `parentId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK55A15FA5961BD498` (`parentId`),
  CONSTRAINT `FK55A15FA5961BD498` FOREIGN KEY (`parentId`) REFERENCES `dir_organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_organization`
--

LOCK TABLES `dir_organization` WRITE;
/*!40000 ALTER TABLE `dir_organization` DISABLE KEYS */;
INSERT INTO `dir_organization` VALUES ('ORG-001','Joget.Org','',NULL);
/*!40000 ALTER TABLE `dir_organization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_role` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_role`
--

LOCK TABLES `dir_role` WRITE;
/*!40000 ALTER TABLE `dir_role` DISABLE KEYS */;
INSERT INTO `dir_role` VALUES ('ROLE_ADMIN','Admin','Administrator'),('ROLE_USER','User','Normal User');
/*!40000 ALTER TABLE `dir_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_user` (
  `id` varchar(255) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `firstName` varchar(255) DEFAULT NULL,
  `lastName` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `timeZone` varchar(255) DEFAULT NULL,
  `locale` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user`
--

LOCK TABLES `dir_user` WRITE;
/*!40000 ALTER TABLE `dir_user` DISABLE KEYS */;
INSERT INTO `dir_user` VALUES ('admin','admin','21232f297a57a5a743894a0e4a801fc3','Admin','Admin',NULL,1,'0',NULL),('cat','cat','5f4dcc3b5aa765d61d8327deb882cf99','Cat','Grant','',1,'',NULL),('clark','clark','5f4dcc3b5aa765d61d8327deb882cf99','Clark','Kent','',1,'',NULL),('david','david','5f4dcc3b5aa765d61d8327deb882cf99','David','Cain','',1,'',NULL),('etta','etta','5f4dcc3b5aa765d61d8327deb882cf99','Etta','Candy','',1,'',NULL),('jack','jack','5f4dcc3b5aa765d61d8327deb882cf99','Jack','Drake','',1,'',NULL),('julia','julia','5f4dcc3b5aa765d61d8327deb882cf99','Julia','Kapatelis','',1,'',NULL),('roy','roy','5f4dcc3b5aa765d61d8327deb882cf99','Roy','Harper','',1,'',NULL),('sasha','sasha','5f4dcc3b5aa765d61d8327deb882cf99','Sasha','Bordeaux','',1,'',NULL),('tana','tana','5f4dcc3b5aa765d61d8327deb882cf99','Tana','Moon','',1,'',NULL),('terry','terry','5f4dcc3b5aa765d61d8327deb882cf99','Terry','Berg','',1,'',NULL),('tina','tina','5f4dcc3b5aa765d61d8327deb882cf99','Tina','Magee','',1,'',NULL);
/*!40000 ALTER TABLE `dir_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user_extra`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_user_extra` (
  `username` varchar(255) NOT NULL,
  `algorithm` varchar(255) DEFAULT NULL,
  `loginAttempt` int(11) DEFAULT NULL,
  `failedloginAttempt` int(11) DEFAULT NULL,
  `lastLogedInDate` datetime DEFAULT NULL,
  `lockOutDate` datetime DEFAULT NULL,
  `lastPasswordChangeDate` datetime DEFAULT NULL,
  `requiredPasswordChange` bit(1) DEFAULT NULL,
  `noPasswordExpiration` bit(1) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_extra`
--

LOCK TABLES `dir_user_extra` WRITE;
/*!40000 ALTER TABLE `dir_user_extra` DISABLE KEYS */;
/*!40000 ALTER TABLE `dir_user_extra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_user_group` (
  `groupId` varchar(255) NOT NULL,
  `userId` varchar(255) NOT NULL,
  PRIMARY KEY (`userId`,`groupId`),
  KEY `FK2F0367FD159B6639` (`groupId`),
  KEY `FK2F0367FDCE539211` (`userId`),
  CONSTRAINT `FK2F0367FD159B6639` FOREIGN KEY (`groupId`) REFERENCES `dir_group` (`id`),
  CONSTRAINT `FK2F0367FDCE539211` FOREIGN KEY (`userId`) REFERENCES `dir_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_group`
--

LOCK TABLES `dir_user_group` WRITE;
/*!40000 ALTER TABLE `dir_user_group` DISABLE KEYS */;
INSERT INTO `dir_user_group` VALUES ('G-001','clark'),('G-001','david'),('G-001','jack'),('G-001','roy'),('G-001','sasha'),('G-001','tina'),('G-002','terry'),('G-003','julia'),('G-003','sasha');
/*!40000 ALTER TABLE `dir_user_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user_meta`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_user_meta` (
  `username` varchar(255) NOT NULL,
  `meta_key` varchar(255) NOT NULL,
  `meta_value` longtext,
  PRIMARY KEY (`username`,`meta_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_meta`
--

LOCK TABLES `dir_user_meta` WRITE;
/*!40000 ALTER TABLE `dir_user_meta` DISABLE KEYS */;
/*!40000 ALTER TABLE `dir_user_meta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user_password_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_user_password_history` (
  `id` varchar(255) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `salt` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `updatedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_password_history`
--

LOCK TABLES `dir_user_password_history` WRITE;
/*!40000 ALTER TABLE `dir_user_password_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `dir_user_password_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user_replacement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_user_replacement` (
  `id` varchar(255) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `replacementUser` varchar(255) DEFAULT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `processIds` varchar(255) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_replacement`
--

LOCK TABLES `dir_user_replacement` WRITE;
/*!40000 ALTER TABLE `dir_user_replacement` DISABLE KEYS */;
/*!40000 ALTER TABLE `dir_user_replacement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_user_role` (
  `roleId` varchar(255) NOT NULL,
  `userId` varchar(255) NOT NULL,
  PRIMARY KEY (`userId`,`roleId`),
  KEY `FK5C5FE738C8FE3CA7` (`roleId`),
  KEY `FK5C5FE738CE539211` (`userId`),
  CONSTRAINT `FK5C5FE738C8FE3CA7` FOREIGN KEY (`roleId`) REFERENCES `dir_role` (`id`),
  CONSTRAINT `FK5C5FE738CE539211` FOREIGN KEY (`userId`) REFERENCES `dir_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_role`
--

LOCK TABLES `dir_user_role` WRITE;
/*!40000 ALTER TABLE `dir_user_role` DISABLE KEYS */;
INSERT INTO `dir_user_role` VALUES ('ROLE_ADMIN','admin'),('ROLE_USER','cat'),('ROLE_USER','clark'),('ROLE_USER','david'),('ROLE_USER','etta'),('ROLE_USER','jack'),('ROLE_USER','julia'),('ROLE_USER','roy'),('ROLE_USER','sasha'),('ROLE_USER','tana'),('ROLE_USER','terry'),('ROLE_USER','tina');
/*!40000 ALTER TABLE `dir_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `objectid`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `objectid` (
  `nextoid` decimal(19,0) NOT NULL,
  PRIMARY KEY (`nextoid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `objectid`
--

LOCK TABLES `objectid` WRITE;
/*!40000 ALTER TABLE `objectid` DISABLE KEYS */;
INSERT INTO `objectid` VALUES (1000400);
/*!40000 ALTER TABLE `objectid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_audit_trail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_audit_trail` (
  `id` varchar(255) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `clazz` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `message` text,
  `timestamp` datetime DEFAULT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `appVersion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_audit_trail`
--

LOCK TABLES `wf_audit_trail` WRITE;
/*!40000 ALTER TABLE `wf_audit_trail` DISABLE KEYS */;
INSERT INTO `wf_audit_trail` VALUES ('ff8080815d360307015d36034a580000','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm, version=1, published=false}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034a630001','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id=crm_account, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034a6a0002','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id=crm_contact, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034a720003','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id=crm_opportunity, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034a7c0004','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id=crm_proposal_approval_form, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034a810005','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id=crm_proposal_form, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034a870006','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id=crm_proposal_resubmit_form, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034a900007','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id=crm_proposal_sending_form, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034d640008','roleAnonymous','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id=crm_account_list, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034d6a0009','roleAnonymous','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id=crm_contact_list, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034d71000a','roleAnonymous','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id=crm_opportunity_list, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034d77000b','roleAnonymous','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id=Proposal, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034d7f000c','roleAnonymous','org.joget.apps.app.dao.UserviewDefinitionDaoImpl','add','{id=crm_userview_sales, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034d84000d','roleAnonymous','org.joget.apps.app.dao.EnvironmentVariableDaoImpl','add','{id=AppName, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36034d8a000e','roleAnonymous','org.joget.apps.app.dao.EnvironmentVariableDaoImpl','add','{id=refNo, appId=crm, appVersion=1}','2017-07-12 08:56:12',NULL,NULL),('ff8080815d360307015d36035033000f','roleAnonymous','org.joget.workflow.model.service.WorkflowManagerImpl','processUpload','','2017-07-12 08:56:13','crm','1'),('ff8080815d360307015d3603544b0010','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d3603545d0011','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d360354710012','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d3603547f0013','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d360354910014','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d360354a20015','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d360354b40016','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d360354c60017','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d360354d60018','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d360354e90019','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d360354f6001a','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d36035505001b','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm','2017-07-12 08:56:14','crm','1'),('ff8080815d360307015d3603553f001c','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm, version=1, published=true}','2017-07-12 16:56:14','crm','1'),('ff8080815d360307015d36036784001d','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=appcenter, version=1, published=false}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d36036788001e','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id=landing, appId=appcenter, appVersion=1}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d360367c6001f','roleAnonymous','org.joget.apps.app.dao.UserviewDefinitionDaoImpl','add','{id=v, appId=appcenter, appVersion=1}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d360367ca0020','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id=<i class=\'icon-home\'></i> Home_zh_CN, appId=appcenter, appVersion=1}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d360367ce0021','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id=<i class=\'icon-home\'></i> Home_zh_TW, appId=appcenter, appVersion=1}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d360367d40022','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id=App Center_zh_CN, appId=appcenter, appVersion=1}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d360367d90023','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id=App Center_zh_TW, appId=appcenter, appVersion=1}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d360367dd0024','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id=Published Apps_zh_CN, appId=appcenter, appVersion=1}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d360367e00025','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id=Published Apps_zh_TW, appId=appcenter, appVersion=1}','2017-07-12 08:56:19',NULL,NULL),('ff8080815d360307015d360367f00026','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=appcenter, version=1, published=true}','2017-07-12 16:56:19','appcenter','1');
/*!40000 ALTER TABLE `wf_audit_trail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_process_link`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_process_link` (
  `processId` varchar(255) NOT NULL,
  `parentProcessId` varchar(255) DEFAULT NULL,
  `originProcessId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`processId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_process_link`
--

LOCK TABLES `wf_process_link` WRITE;
/*!40000 ALTER TABLE `wf_process_link` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_process_link` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_report`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_report` (
  `activityInstanceId` varchar(255) NOT NULL,
  `processInstanceId` varchar(255) DEFAULT NULL,
  `priority` varchar(255) DEFAULT NULL,
  `createdTime` datetime DEFAULT NULL,
  `startedTime` datetime DEFAULT NULL,
  `dateLimit` bigint(20) DEFAULT NULL,
  `due` datetime DEFAULT NULL,
  `delay` bigint(20) DEFAULT NULL,
  `finishTime` datetime DEFAULT NULL,
  `timeConsumingFromDateCreated` bigint(20) DEFAULT NULL,
  `timeConsumingFromDateStarted` bigint(20) DEFAULT NULL,
  `performer` varchar(255) DEFAULT NULL,
  `nameOfAcceptedUser` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `packageId` varchar(255) DEFAULT NULL,
  `processDefId` varchar(255) DEFAULT NULL,
  `activityDefId` varchar(255) DEFAULT NULL,
  `assignmentUsers` text,
  `appId` varchar(255) DEFAULT NULL,
  `appVersion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`activityInstanceId`),
  KEY `FKB943CCA47A4E8F48` (`packageId`),
  KEY `FKB943CCA4A39D6461` (`processDefId`),
  KEY `FKB943CCA4CB863F` (`activityDefId`),
  CONSTRAINT `FKB943CCA47A4E8F48` FOREIGN KEY (`packageId`) REFERENCES `wf_report_package` (`packageId`),
  CONSTRAINT `FKB943CCA4A39D6461` FOREIGN KEY (`processDefId`) REFERENCES `wf_report_process` (`processDefId`),
  CONSTRAINT `FKB943CCA4CB863F` FOREIGN KEY (`activityDefId`) REFERENCES `wf_report_activity` (`activityDefId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_report`
--

LOCK TABLES `wf_report` WRITE;
/*!40000 ALTER TABLE `wf_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_report_activity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_report_activity` (
  `activityDefId` varchar(255) NOT NULL,
  `activityName` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `priority` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`activityDefId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_report_activity`
--

LOCK TABLES `wf_report_activity` WRITE;
/*!40000 ALTER TABLE `wf_report_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_report_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_report_package`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_report_package` (
  `packageId` varchar(255) NOT NULL,
  `packageName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`packageId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_report_package`
--

LOCK TABLES `wf_report_package` WRITE;
/*!40000 ALTER TABLE `wf_report_package` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_report_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_report_process`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_report_process` (
  `processDefId` varchar(255) NOT NULL,
  `processName` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`processDefId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_report_process`
--

LOCK TABLES `wf_report_process` WRITE;
/*!40000 ALTER TABLE `wf_report_process` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_report_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_resource_bundle_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_resource_bundle_message` (
  `id` varchar(255) NOT NULL,
  `messageKey` varchar(255) DEFAULT NULL,
  `locale` varchar(255) DEFAULT NULL,
  `message` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_resource_bundle_message`
--

LOCK TABLES `wf_resource_bundle_message` WRITE;
/*!40000 ALTER TABLE `wf_resource_bundle_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_resource_bundle_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_setup`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_setup` (
  `id` varchar(255) NOT NULL,
  `property` varchar(255) DEFAULT NULL,
  `value` text,
  `ordering` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_setup`
--

LOCK TABLES `wf_setup` WRITE;
/*!40000 ALTER TABLE `wf_setup` DISABLE KEYS */;
INSERT INTO `wf_setup` VALUES ('ff8080815d360307015d3603680c0027','defaultUserview','appcenter/v',NULL);
/*!40000 ALTER TABLE `wf_setup` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-07-12 16:58:22
