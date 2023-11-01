-- MariaDB dump 10.19  Distrib 10.11.2-MariaDB, for osx10.18 (arm64)
--
-- Host: localhost    Database: jwdb
-- ------------------------------------------------------
-- Server version	10.11.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValue` mediumblob DEFAULT NULL,
  `VariableValueXML` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValue` mediumblob DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityDataBLOBs` (`ActivityDataWOB`,`OrdNo`),
  CONSTRAINT `SHKActivityDataBLOBs_ActivityDataWOB` FOREIGN KEY (`ActivityDataWOB`) REFERENCES `SHKActivityDataWOB` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValueXML` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKActivityStates`
--

LOCK TABLES `SHKActivityStates` WRITE;
/*!40000 ALTER TABLE `SHKActivityStates` DISABLE KEYS */;
INSERT INTO `SHKActivityStates` VALUES
('open.running','open.running',1000001,0),
('open.not_running.not_started','open.not_running.not_started',1000003,0),
('open.not_running.suspended','open.not_running.suspended',1000005,0),
('closed.completed','closed.completed',1000007,0),
('closed.terminated','closed.terminated',1000009,0),
('closed.aborted','closed.aborted',1000011,0);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKCounters`
--

LOCK TABLES `SHKCounters` WRITE;
/*!40000 ALTER TABLE `SHKCounters` DISABLE KEYS */;
INSERT INTO `SHKCounters` VALUES
('_xpdldata_',201,29504246,0);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValue` mediumblob DEFAULT NULL,
  `VariableValueXML` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValue` mediumblob DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNewEventAuditDataBLOBs` (`NewEventAuditDataWOB`,`OrdNo`),
  CONSTRAINT `SHKNewEventAuditDataBLOBs_NewEventAuditDataWOB` FOREIGN KEY (`NewEventAuditDataWOB`) REFERENCES `SHKNewEventAuditDataWOB` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValueXML` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKNextXPDLVersions`
--

LOCK TABLES `SHKNextXPDLVersions` WRITE;
/*!40000 ALTER TABLE `SHKNextXPDLVersions` DISABLE KEYS */;
INSERT INTO `SHKNextXPDLVersions` VALUES
('crm_community','2',1000201,0);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValue` mediumblob DEFAULT NULL,
  `VariableValueXML` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValue` mediumblob DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKOldEventAuditDataBLOBs` (`OldEventAuditDataWOB`,`OrdNo`),
  CONSTRAINT `SHKOldEventAuditDataBLOBs_OldEventAuditDataWOB` FOREIGN KEY (`OldEventAuditDataWOB`) REFERENCES `SHKOldEventAuditDataWOB` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValueXML` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValue` mediumblob DEFAULT NULL,
  `VariableValueXML` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValue` mediumblob DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessDataBLOBs` (`ProcessDataWOB`,`OrdNo`),
  CONSTRAINT `SHKProcessDataBLOBs_ProcessDataWOB` FOREIGN KEY (`ProcessDataWOB`) REFERENCES `SHKProcessDataWOB` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `VariableValueXML` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessDefinitions`
--

LOCK TABLES `SHKProcessDefinitions` WRITE;
/*!40000 ALTER TABLE `SHKProcessDefinitions` DISABLE KEYS */;
INSERT INTO `SHKProcessDefinitions` VALUES
('crm_community#1#process1','crm_community','process1',1698817125163,'1',0,1000204,0);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKProcessStates`
--

LOCK TABLES `SHKProcessStates` WRITE;
/*!40000 ALTER TABLE `SHKProcessStates` DISABLE KEYS */;
INSERT INTO `SHKProcessStates` VALUES
('open.running','open.running',1000000,0),
('open.not_running.not_started','open.not_running.not_started',1000002,0),
('open.not_running.suspended','open.not_running.suspended',1000004,0),
('closed.completed','closed.completed',1000006,0),
('closed.terminated','closed.terminated',1000008,0),
('closed.aborted','closed.aborted',1000010,0);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKResourcesTable`
--

LOCK TABLES `SHKResourcesTable` WRITE;
/*!40000 ALTER TABLE `SHKResourcesTable` DISABLE KEYS */;
INSERT INTO `SHKResourcesTable` VALUES
('roleSystem',NULL,1000200,0);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `XPDLContent` longblob DEFAULT NULL,
  `XPDLClassContent` longblob DEFAULT NULL,
  `XPDL` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLData` (`CNT`),
  UNIQUE KEY `I2_SHKXPDLData` (`XPDL`),
  CONSTRAINT `SHKXPDLData_XPDL` FOREIGN KEY (`XPDL`) REFERENCES `SHKXPDLS` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLData`
--

LOCK TABLES `SHKXPDLData` WRITE;
/*!40000 ALTER TABLE `SHKXPDLData` DISABLE KEYS */;
INSERT INTO `SHKXPDLData` VALUES
('<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Package xmlns=\"http://www.wfmc.org/2002/XPDL1.0\" xmlns:xpdl=\"http://www.wfmc.org/2002/XPDL1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" Id=\"crm_community\" Name=\"CRM Community\" xsi:schemaLocation=\"http://www.wfmc.org/2002/XPDL1.0 http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd\">\n    <PackageHeader>\n        <XPDLVersion>1.0</XPDLVersion>\n        <Vendor/>\n        <Created/>\n    </PackageHeader>\n    <Script Type=\"text/javascript\"/>\n    <Participants>\n        <Participant Id=\"requester\" Name=\"Requester\">\n            <ParticipantType Type=\"ROLE\"/>\n        </Participant>\n        <Participant Id=\"approver\" Name=\"Approver\">\n            <ParticipantType Type=\"ROLE\"/>\n        </Participant>\n    </Participants>\n    <Applications>\n        <Application Id=\"default_application\"/>\n    </Applications>\n    <WorkflowProcesses>\n        <WorkflowProcess Id=\"process1\" Name=\"Proposal Approval Process\">\n            <ProcessHeader DurationUnit=\"h\"/>\n            <DataFields>\n                <DataField Id=\"status\" IsArray=\"FALSE\">\n                    <DataType>\n                        <BasicType Type=\"STRING\"/>\n                    </DataType>\n                </DataField>\n            </DataFields>\n            <Activities>\n                <Activity Id=\"approve_proposal\" Name=\"Approve Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Join Type=\"XOR\"/>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"205.0000228881836,56.76666259765625\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"approval\" Name=\"Approval\">\n                    <Route/>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Split Type=\"XOR\">\n                                <TransitionRefs>\n                                    <TransitionRef Id=\"transition3\"/>\n                                    <TransitionRef Id=\"transition6\"/>\n                                    <TransitionRef Id=\"transition5\"/>\n                                </TransitionRefs>\n                            </Split>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"430,62.79999084472655\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"activity1\" Name=\"Resubmit Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"397,20.787493896484378\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"send_proposal\" Name=\"Send Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"681.0000381469727,99.78333282470703\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"parallel\" Name=\"Parallel\">\n                    <Route/>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Split Type=\"AND\">\n                                <TransitionRefs>\n                                    <TransitionRef Id=\"transition7\"/>\n                                    <TransitionRef Id=\"transition8\"/>\n                                </TransitionRefs>\n                            </Split>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"588,61.599993896484364\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"tool1\" Name=\"Send Approval Notification\">\n                    <Implementation>\n                        <Tool Id=\"default_application\"/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"735,63.974993896484364\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"tool2\" Name=\"Send Reject Notification\">\n                    <Implementation>\n                        <Tool Id=\"default_application\"/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"394,168.39999694824218\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"route1\" Name=\"Route 1\">\n                    <Route/>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"228.0000228881836,22\"/>\n                    </ExtendedAttributes>\n                </Activity>\n            </Activities>\n            <Transitions>\n                <Transition From=\"approve_proposal\" Id=\"transition2\" To=\"approval\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"approval\" Id=\"transition3\" To=\"activity1\">\n                    <Condition Type=\"CONDITION\">status===\'resubmit\'</Condition>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"activity1\" Id=\"transition4\" To=\"approve_proposal\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"approval\" Id=\"transition6\" Name=\"approved\" To=\"parallel\">\n                    <Condition Type=\"OTHERWISE\"/>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"parallel\" Id=\"transition7\" To=\"send_proposal\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"parallel\" Id=\"transition8\" To=\"tool1\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"approval\" Id=\"transition5\" To=\"tool2\">\n                    <Condition Type=\"CONDITION\">status===\'rejected\'</Condition>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"route1\" Id=\"transition1\" To=\"approve_proposal\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n            </Transitions>\n            <ExtendedAttributes>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER\" Value=\"requester;approver\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_START_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route1,X_OFFSET=87,Y_OFFSET=28,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULT\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool1,X_OFFSET=901,Y_OFFSET=74,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=send_proposal,X_OFFSET=849,Y_OFFSET=110,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool2,X_OFFSET=579,Y_OFFSET=180,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\n            </ExtendedAttributes>\n        </WorkflowProcess>\n    </WorkflowProcesses>\n    <ExtendedAttributes>\n        <ExtendedAttribute Name=\"EDITING_TOOL\" Value=\"Web Workflow Designer\"/>\n        <ExtendedAttribute Name=\"EDITING_TOOL_VERSION\" Value=\"5.0-pre-alpha\"/>\n    </ExtendedAttributes>\n</Package>\n','¨Ì\0sr\0\'org.enhydra.shark.xpdl.elements.Package~+Vm≈Ä~˜\0Z\0isTransientL\0extPkgRefsToIdst\0.Lorg/enhydra/shark/utilities/SequencedHashMap;L\0internalVersiont\0Ljava/lang/String;L\0\nnamespacest\0,Lorg/enhydra/shark/xpdl/elements/Namespaces;xr\0(org.enhydra.shark.xpdl.XMLComplexElement>≤æΩÓ(⁄Ã\0\0xr\05org.enhydra.shark.xpdl.XMLBaseForCollectionAndComplexÂ‡áÓ·ı2\0L\0\nelementMapq\0~\0L\0elementst\0Ljava/util/ArrayList;xr\0!org.enhydra.shark.xpdl.XMLElement#+Bø#˜åº\0Z\0\nisReadOnlyZ\0\nisRequiredL\0nameq\0~\0L\0originalElementHashCodet\0Ljava/lang/Integer;L\0parentt\0#Lorg/enhydra/shark/xpdl/XMLElement;L\0valueq\0~\0xpt\0Packagesr\0java.lang.Integer‚†§˜Åá8\0I\0valuexr\0java.lang.NumberÜ¨ïî‡ã\0\0xp&;1pt\0\0sr\0,org.enhydra.shark.utilities.SequencedHashMap.Í\"ì©\"&\0\0xpw\0\0\0\rt\0Idsr\0#org.enhydra.shark.xpdl.XMLAttribute#c›Ä˛ÀM;\0L\0choicesq\0~\0xq\0~\0q\0~\0sq\0~\0I{OYq\0~\0\nt\0\rcrm_communitypt\0Namesq\0~\0\0q\0~\0sq\0~\06Éıæq\0~\0\nt\0\rCRM Communitypt\0\rPackageHeadersr\0-org.enhydra.shark.xpdl.elements.PackageHeadervÈí,ÌíÔ\0\0xq\0~\0\0q\0~\0sq\0~\0;B¸q\0~\0\nt\0\0sq\0~\0w\0\0\0t\0XPDLVersionsr\0+org.enhydra.shark.xpdl.elements.XPDLVersion˜\"}◊Y.≈w\0\0xr\0\'org.enhydra.shark.xpdl.XMLSimpleElement¬m›¿éÛ\0\0xq\0~\0q\0~\0!sq\0~\0\0ÒåJq\0~\0t\01.0t\0Vendorsr\0&org.enhydra.shark.xpdl.elements.Vendor t¯ÍE≥:\0\0xq\0~\0#q\0~\0\'sq\0~\0!;Sbq\0~\0t\0\0t\0Createdsr\0\'org.enhydra.shark.xpdl.elements.Createdz ÙdKª|[\0\0xq\0~\0#q\0~\0,sq\0~\0!?eq\0~\0t\0\0t\0Descriptionsr\0+org.enhydra.shark.xpdl.elements.Description€∞73Ê8˚\0\0xq\0~\0#\0q\0~\01sq\0~\05≤∞+q\0~\0t\0\0t\0\rDocumentationsr\0-org.enhydra.shark.xpdl.elements.Documentation`ƒ9ª◊yí\0\0xq\0~\0#\0q\0~\06sq\0~\0\r˛µq\0~\0t\0\0t\0PriorityUnitsr\0,org.enhydra.shark.xpdl.elements.PriorityUnitò≥õËõ¯‰Â\0\0xq\0~\0#\0q\0~\0;sq\0~\0^sëŸq\0~\0t\0\0t\0CostUnitsr\0(org.enhydra.shark.xpdl.elements.CostUnit‹éŸ=H·å\0\0xq\0~\0#\0q\0~\0@sq\0~\0a0Iq\0~\0t\0\0xsr\0java.util.ArrayListxÅ“ô«aù\0I\0sizexp\0\0\0w\0\0\0q\0~\0$q\0~\0)q\0~\0.q\0~\03q\0~\08q\0~\0=q\0~\0Bxt\0RedefinableHeadersr\01org.enhydra.shark.xpdl.elements.RedefinableHeaderôøMÎœ™\'H\0\0xq\0~\0\0q\0~\0Gsq\0~\0¨Kmq\0~\0\nt\0\0sq\0~\0w\0\0\0t\0PublicationStatussq\0~\0\0q\0~\0Msq\0~\0Ì≠q\0~\0It\0\0sq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0UNDER_REVISIONt\0RELEASEDt\0\nUNDER_TESTxt\0Authorsr\0&org.enhydra.shark.xpdl.elements.Author5 ƒf·ßÜ\0\0xq\0~\0#\0q\0~\0Usq\0~\0h›n˙q\0~\0It\0\0t\0Versionsr\0\'org.enhydra.shark.xpdl.elements.Version9=3¥~ªJQ\0\0xq\0~\0#\0q\0~\0Zsq\0~\0l+æq\0~\0It\0\0t\0Codepagesr\0(org.enhydra.shark.xpdl.elements.Codepage9$m˜eΩ\rG\0\0xq\0~\0#\0q\0~\0_sq\0~\0 9q\0~\0It\0\0t\0\nCountrykeysr\0*org.enhydra.shark.xpdl.elements.Countrykeyóã.Å–“—\0\0xq\0~\0#\0q\0~\0dsq\0~\0jhó1q\0~\0It\0\0t\0Responsiblessr\0,org.enhydra.shark.xpdl.elements.Responsibles$ÔÍ{S∆\0\0xr\0$org.enhydra.shark.xpdl.XMLCollectionËèjƒãmÌ∞\0\0xq\0~\0\0q\0~\0isq\0~\0y¸zÓq\0~\0It\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~\0Nq\0~\0Wq\0~\0\\q\0~\0aq\0~\0fq\0~\0lxt\0ConformanceClasssr\00org.enhydra.shark.xpdl.elements.ConformanceClass◊Ïy0|k´î\0\0xq\0~\0\0q\0~\0rsq\0~\0πÉ2q\0~\0\nt\0\0sq\0~\0w\0\0\0t\0GraphConformancesq\0~\0\0q\0~\0xsq\0~\0»mq\0~\0tq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0FULL_BLOCKEDt\0LOOP_BLOCKEDt\0NON_BLOCKEDxxsq\0~\0E\0\0\0w\0\0\0q\0~\0yxt\0Scriptsr\0&org.enhydra.shark.xpdl.elements.ScriptQ¶jÁS„8\0\0xq\0~\0\0q\0~\0Äsq\0~\0[˚ÎÃq\0~\0\nt\0\0sq\0~\0w\0\0\0t\0Typesq\0~\0q\0~\0Üsq\0~\0úSíq\0~\0Çt\0text/javascriptpt\0Versionsq\0~\0\0q\0~\0äsq\0~\0JjM¥q\0~\0Çt\0\0pt\0Grammarsq\0~\0\0q\0~\0ésq\0~\0$9⁄q\0~\0Çt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\0áq\0~\0ãq\0~\0èxt\0ExternalPackagessr\00org.enhydra.shark.xpdl.elements.ExternalPackagesw¿\"+≈®®â\0\0xq\0~\0k\0q\0~\0ìsq\0~\0KG(q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0TypeDeclarationssr\00org.enhydra.shark.xpdl.elements.TypeDeclarations\r· Ox5Õ\0\0xq\0~\0k\0q\0~\0ösq\0~\0nÖ\0‹q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Participantssr\0,org.enhydra.shark.xpdl.elements.Participantsh`∑g8J\0\0xq\0~\0k\0q\0~\0°sq\0~\0àWq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0+org.enhydra.shark.xpdl.elements.Participanto$îÛrc§è\0\0xr\0+org.enhydra.shark.xpdl.XMLCollectionElementC¢x”vÅr\0\0xq\0~\0t\0Participantsq\0~\0Y´`:q\0~\0£t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0wπä\"q\0~\0™t\0	requesterpq\0~\0sq\0~\0\0q\0~\0sq\0~\0WöÃJq\0~\0™t\0	Requesterpt\0ParticipantTypesr\0/org.enhydra.shark.xpdl.elements.ParticipantType>àn•›Ö®˜\0\0xq\0~\0q\0~\0µsq\0~\0.≤Àˆq\0~\0™t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0\Zı\Zq\0~\0∑t\0ROLEsq\0~\0E\0\0\0w\0\0\0t\0RESOURCE_SETt\0RESOURCEt\0ROLEt\0ORGANIZATIONAL_UNITt\0HUMANt\0SYSTEMxxsq\0~\0E\0\0\0w\0\0\0q\0~\0ªxt\0Descriptionsq\0~\02\0q\0~\0∆sq\0~\0› q\0~\0™t\0\0t\0ExternalReferencesr\01org.enhydra.shark.xpdl.elements.ExternalReferenceÜbË∑¡Qœ\0\0xq\0~\0\0q\0~\0 sq\0~\0§tq\0~\0™t\0\0sq\0~\0w\0\0\0t\0xrefsq\0~\0\0q\0~\0–sq\0~\0!q·æq\0~\0Ãt\0\0pt\0locationsq\0~\0q\0~\0‘sq\0~\0Ê*dq\0~\0Ãt\0\0pt\0	namespacesq\0~\0\0q\0~\0ÿsq\0~\0cU®Åq\0~\0Ãt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\0—q\0~\0’q\0~\0Ÿxt\0ExtendedAttributessr\02org.enhydra.shark.xpdl.elements.ExtendedAttributesÚ∞Oıü⁄UF\0L\0extAttribsStringq\0~\0xq\0~\0k\0q\0~\0›sq\0~\0Yk¸†q\0~\0™t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~\0Øq\0~\0≤q\0~\0∑q\0~\0«q\0~\0Ãq\0~\0ﬂxsq\0~\0®t\0Participantsq\0~\0UÆ;ªq\0~\0£t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0mœPq\0~\0Ât\0approverpq\0~\0sq\0~\0\0q\0~\0sq\0~\0Òê/q\0~\0Ât\0Approverpt\0ParticipantTypesq\0~\0∂q\0~\0sq\0~\0˘Ò\rq\0~\0Ât\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0&™Ä\nq\0~\0Òt\0ROLEsq\0~\0E\0\0\0w\0\0\0q\0~\0øq\0~\0¿q\0~\0¡q\0~\0¬q\0~\0√q\0~\0ƒxxsq\0~\0E\0\0\0w\0\0\0q\0~\0ıxt\0Descriptionsq\0~\02\0q\0~\0˙sq\0~\0}áq\0~\0Ât\0\0t\0ExternalReferencesq\0~\0À\0q\0~\0˛sq\0~\0\Zúq\0~\0Ât\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0Gπv8q\0~\0ˇt\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\05·q\0~\0ˇt\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0_Rwq\0~\0ˇt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~q\0~	xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\rsq\0~\0irq\0~\0Ât\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~\0Íq\0~\0Ìq\0~\0Òq\0~\0˚q\0~\0ˇq\0~xxt\0Applicationssr\0,org.enhydra.shark.xpdl.elements.Applications¬Ä¥ﬁˆº\0\0xq\0~\0k\0q\0~sq\0~\07ﬂrq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0+org.enhydra.shark.xpdl.elements.Applicationv	¢R˘˛S\0\0xq\0~\0©t\0Applicationsq\0~\0A›\"¥q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0h¡Œóq\0~t\0default_applicationpq\0~\0sq\0~\0\0q\0~\0sq\0~\0$|•|q\0~t\0\0pt\0Descriptionsq\0~\02\0q\0~\'sq\0~\0 ÿÑ’q\0~t\0\0t\0Choicesr\00org.enhydra.shark.xpdl.elements.ApplicationTypesè?˚!≠Æ¶\0\0xr\0\'org.enhydra.shark.xpdl.XMLComplexChoice§|±∫\"Ù˙\0L\0choicesq\0~\0L\0choosenq\0~\0	xq\0~\0q\0~+sq\0~\0y≠”pq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sr\00org.enhydra.shark.xpdl.elements.FormalParametersp∫ƒB«√ÅZ\0\0xq\0~\0k\0t\0FormalParameterssq\0~\0YØæq\0~.t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~\0À\0t\0ExternalReferencesq\0~\0-9Â#q\0~.t\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0I£\"}q\0~9t\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0^Ç^jq\0~9t\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0n>[q\0~9t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~>q\0~Aq\0~Dxxq\0~3t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Hsq\0~\0t—Hq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~!q\0~$q\0~(q\0~.q\0~Ixxt\0\nDataFieldssr\0*org.enhydra.shark.xpdl.elements.DataFieldsΩµí ¶‘˙U\0\0xq\0~\0k\0q\0~Osq\0~\0D-’q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0WorkflowProcessessr\01org.enhydra.shark.xpdl.elements.WorkflowProcessespé_¢0,\0\0xq\0~\0k\0q\0~Vsq\0~\0Bl°Æq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0/org.enhydra.shark.xpdl.elements.WorkflowProcess%·v0“◊L\0\0xq\0~\0©t\0WorkflowProcesssq\0~\0WÈ≤;q\0~Xt\0\0sq\0~\0w\0\0\0\rq\0~\0sq\0~\0q\0~\0sq\0~\0#ßπüq\0~^t\0process1pq\0~\0sq\0~\0\0q\0~\0sq\0~\01±1q\0~^t\0Proposal Approval Processpt\0AccessLevelsq\0~\0\0q\0~isq\0~\06Üöõq\0~^q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0PUBLICt\0PRIVATExt\0\rProcessHeadersr\0-org.enhydra.shark.xpdl.elements.ProcessHeaderåLÆÉC-)ø\0\0xq\0~\0q\0~osq\0~\0q<B–q\0~^t\0\0sq\0~\0w\0\0\0t\0DurationUnitsq\0~\0\0q\0~usq\0~\0kﬁ^¶q\0~qt\0hsq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0Yt\0Mt\0Dt\0ht\0mt\0sxt\0Createdsq\0~\0-\0q\0~Äsq\0~\0◊Á’q\0~qt\0\0t\0Descriptionsq\0~\02\0q\0~Ñsq\0~\0}¯ˆq\0~qt\0\0t\0Prioritysr\0(org.enhydra.shark.xpdl.elements.Priority`ˆNn>b\0\0xq\0~\0#\0q\0~àsq\0~\0næDq\0~qt\0\0t\0Limitsr\0%org.enhydra.shark.xpdl.elements.Limit¸¢Ë1ò”ó\0\0xq\0~\0#\0q\0~çsq\0~\0wåG[q\0~qt\0\0t\0	ValidFromsr\0)org.enhydra.shark.xpdl.elements.ValidFromc≈Ö|ÉL<\0\0xq\0~\0#\0q\0~ísq\0~\07•Ô0q\0~qt\0\0t\0ValidTosr\0\'org.enhydra.shark.xpdl.elements.ValidToû¡∂´M…\0\0xq\0~\0#\0q\0~ósq\0~\0ußqzq\0~qt\0\0t\0TimeEstimationsr\0.org.enhydra.shark.xpdl.elements.TimeEstimation≈Ä£\'3\0\0xq\0~\0\0q\0~úsq\0~\0k˝î•q\0~qt\0\0sq\0~\0w\0\0\0t\0WaitingTimesr\0+org.enhydra.shark.xpdl.elements.WaitingTimeNì‰Œ†/\0\0xq\0~\0#\0q\0~¢sq\0~\0~^†q\0~ût\0\0t\0WorkingTimesr\0+org.enhydra.shark.xpdl.elements.WorkingTimeæ~±ÉÊ◊\0\0xq\0~\0#\0q\0~ßsq\0~\0f\Zäq\0~ût\0\0t\0Durationsr\0(org.enhydra.shark.xpdl.elements.Duration© ÙCÙÎË\0\0xq\0~\0#\0q\0~¨sq\0~\0(xçíq\0~ût\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~§q\0~©q\0~Æxxsq\0~\0E\0\0\0w\0\0\0q\0~vq\0~Åq\0~Öq\0~äq\0~èq\0~îq\0~ôq\0~ûxt\0RedefinableHeadersq\0~\0H\0q\0~≥sq\0~\0/\'lÑq\0~^t\0\0sq\0~\0w\0\0\0q\0~\0Msq\0~\0\0q\0~\0Msq\0~\0$rg.q\0~¥q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\0Rq\0~\0Sq\0~\0Txt\0Authorsq\0~\0V\0q\0~ªsq\0~\0∫Avq\0~¥t\0\0t\0Versionsq\0~\0[\0q\0~øsq\0~\0~BZÆq\0~¥t\0\0t\0Codepagesq\0~\0`\0q\0~√sq\0~\0jZÆq\0~¥t\0\0t\0\nCountrykeysq\0~\0e\0q\0~«sq\0~\0]È’q\0~¥t\0\0t\0Responsiblessq\0~\0j\0q\0~Àsq\0~\0PÔq\0~¥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~∏q\0~ºq\0~¿q\0~ƒq\0~»q\0~Ãxt\0FormalParameterssq\0~2\0q\0~“sq\0~\0q„˛»q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0\nDataFieldssq\0~P\0q\0~ÿsq\0~\01„\npq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0)org.enhydra.shark.xpdl.elements.DataFieldI“3.~ë≠˜\0\0xq\0~\0©t\0	DataFieldsq\0~\0Kd±lq\0~Ÿt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0_RÈiq\0~ﬂt\0statuspq\0~\0sq\0~\0\0q\0~\0sq\0~\0f…º∆q\0~ﬂt\0\0pt\0IsArraysq\0~\0\0q\0~Ísq\0~\04‘Ùq\0~ﬂt\0FALSEsq\0~\0E\0\0\0w\0\0\0t\0TRUEt\0FALSExt\0DataTypesr\0(org.enhydra.shark.xpdl.elements.DataTypeÜ\'4sM\0\0xq\0~\0q\0~Òsq\0~\0pÓmïq\0~ﬂt\0\0sq\0~\0w\0\0\0t\0	DataTypessr\0)org.enhydra.shark.xpdl.elements.DataTypesµpcH,ﬁ!ì\0Z\0\risInitializedxq\0~-q\0~˜sq\0~\0Eﬁh-q\0~Ût\0\0sq\0~\0E\0\0\0	w\0\0\0	sr\0)org.enhydra.shark.xpdl.elements.BasicType∑)®Ωw1øé\0\0xq\0~\0t\0	BasicTypesq\0~\0e≠óq\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0,$üq\0~˛t\0STRINGsq\0~\0E\0\0\0w\0\0\0t\0STRINGt\0FLOATt\0INTEGERt\0	REFERENCEt\0DATETIMEt\0BOOLEANt\0	PERFORMERxxsq\0~\0E\0\0\0w\0\0\0q\0~xsr\0,org.enhydra.shark.xpdl.elements.DeclaredTypedR.\\^Ë9í\0\0xq\0~\0t\0DeclaredTypesq\0~\0-r™q\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0>ÑÏÇq\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~xsr\0*org.enhydra.shark.xpdl.elements.SchemaType&1oSH™Ö\0\0xq\0~\0t\0\nSchemaTypesq\0~\03êÃ{q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~\0Àt\0ExternalReferencesq\0~\0YrJq\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0:©üÏq\0~ t\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0#PÑ«q\0~ t\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0ñg¬q\0~ t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~%q\0~(q\0~+xsr\0*org.enhydra.shark.xpdl.elements.RecordTypeı%®Ω©ÎK\0\0\0xq\0~\0kt\0\nRecordTypesq\0~\0__;q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.UnionType¥⁄Ô5PŒG˙\0\0xq\0~\0kt\0	UnionTypesq\0~\0)â¸q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0/org.enhydra.shark.xpdl.elements.EnumerationType™⁄ﬁf3b\0\0xq\0~\0kt\0EnumerationTypesq\0~\0ÆV®q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.ArrayTypeg¨$\0≠N@\0\0xq\0~\0t\0	ArrayTypesq\0~\0+£à»q\0~˘t\0\0sq\0~\0w\0\0\0t\0\nLowerIndexsq\0~\0q\0~Jsq\0~\0\r£q\0~Et\0\0pt\0\nUpperIndexsq\0~\0q\0~Nsq\0~\0XgIñq\0~Et\0\0pq\0~˜sq\0~¯q\0~˜sq\0~\0$\0_$q\0~Et\0\0ppxsq\0~\0E\0\0\0w\0\0\0q\0~Kq\0~Oq\0~Rxsr\0(org.enhydra.shark.xpdl.elements.ListTypeü\"”ü\nÆ\0\0xq\0~\0t\0ListTypesq\0~\0Rf§q\0~˘t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯q\0~˜sq\0~\0¿rtq\0~Wt\0\0ppxsq\0~\0E\0\0\0w\0\0\0q\0~\\xxq\0~˛xsq\0~\0E\0\0\0w\0\0\0q\0~˘xt\0InitialValuesr\0,org.enhydra.shark.xpdl.elements.InitialValuej,z˚îúR\0\0xq\0~\0#\0q\0~asq\0~\0l	±√q\0~ﬂt\0\0t\0Lengthsr\0&org.enhydra.shark.xpdl.elements.LengthMW+-Ã©W‹\0\0xq\0~\0#\0q\0~fsq\0~\0JV:q\0~ﬂt\0\0t\0Descriptionsq\0~\02\0q\0~ksq\0~\0J±±q\0~ﬂt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~osq\0~\0GoäAq\0~ﬂt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~‰q\0~Áq\0~Îq\0~Ûq\0~cq\0~hq\0~lq\0~pxxt\0Participantssq\0~\0¢\0q\0~vsq\0~\0$¡:q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Applicationssq\0~\0q\0~|sq\0~\0{ªeq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ActivitySetssr\0,org.enhydra.shark.xpdl.elements.ActivitySets–qV[4™Ó÷\0\0xq\0~\0k\0q\0~Çsq\0~\0˙sq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0\nActivitiessr\0*org.enhydra.shark.xpdl.elements.Activities&G^·æl˛P\0\0xq\0~\0k\0q\0~âsq\0~\0<c76q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0(org.enhydra.shark.xpdl.elements.Activity√tá45\Z9ï\0\0xq\0~\0©t\0Activitysq\0~\0&’Ìeq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\0–Jçq\0~ët\0approve_proposalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0%©Êfq\0~ët\0Approve Proposalpt\0Descriptionsq\0~\02\0q\0~úsq\0~\00çq\0~ët\0\0t\0Limitsq\0~é\0q\0~†sq\0~\0œ}aq\0~ët\0\0q\0~\0Üsr\0-org.enhydra.shark.xpdl.elements.ActivityTypese≈Ω{ˆûËË\0\0xq\0~-q\0~\0Üsq\0~\0xxq\0~ët\0\0sq\0~\0E\0\0\0w\0\0\0sr\0%org.enhydra.shark.xpdl.elements.Route0eÓ\r≤G›\0\0xq\0~\0t\0Routesq\0~\0vKGq\0~•t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0.org.enhydra.shark.xpdl.elements.Implementationúrñí^%ä\0\0xq\0~\0t\0Implementationsq\0~\0.M¬çq\0~•t\0\0sq\0~\0w\0\0\0q\0~\0Üsr\03org.enhydra.shark.xpdl.elements.ImplementationTypes\rü‰TŸ°9\0\0xq\0~-q\0~\0Üsq\0~\0@ñPq\0~±t\0\0sq\0~\0E\0\0\0w\0\0\0sr\0\"org.enhydra.shark.xpdl.elements.No{ïÖ⁄.\0\0xq\0~\0t\0Nosq\0~\0ÄÏq\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0%org.enhydra.shark.xpdl.elements.ToolsCø÷g©ë\0\0xq\0~\0k\0t\0Toolssq\0~\0(f3Hq\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0\'org.enhydra.shark.xpdl.elements.SubFlow;Oısá7:$\0\0xq\0~\0t\0SubFlowsq\0~\0h≈cq\0~∑t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0∂k*q\0~ t\0\0pt\0	Executionsq\0~\0\0q\0~“sq\0~\0•á1q\0~ q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ASYNCHRt\0SYNCHRxt\0ActualParameterssr\00org.enhydra.shark.xpdl.elements.ActualParametersÎå˜µ_ÏK˛\0\0xq\0~\0k\0q\0~ÿsq\0~\0@q\0~ t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~œq\0~”q\0~⁄xxq\0~ºxsq\0~\0E\0\0\0w\0\0\0q\0~∑xsr\0-org.enhydra.shark.xpdl.elements.BlockActivityíq cƒÍF\0\0xq\0~\0t\0\rBlockActivitysq\0~\0ÇÇàq\0~•t\0\0sq\0~\0w\0\0\0t\0BlockIdsq\0~\0q\0~Ásq\0~\0  Viq\0~‚t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~Ëxxq\0~±t\0	Performersr\0)org.enhydra.shark.xpdl.elements.Performer¶\"1%ËÃƒ\0\0xq\0~\0#\0q\0~Ïsq\0~\0Ú—Nq\0~ët\0approvert\0	StartModesr\0)org.enhydra.shark.xpdl.elements.StartModenhÖÑ◊ÛS\0\0xq\0~\0\0q\0~Òsq\0~\0Î∞\'q\0~ët\0\0sq\0~\0w\0\0\0t\0Modesr\00org.enhydra.shark.xpdl.elements.StartFinishModes~Á6zÂXã\'\0\0xq\0~-\0q\0~˜sq\0~\0nˆ–q\0~Ût\0\0sq\0~\0E\0\0\0w\0\0\0sr\0,org.enhydra.shark.xpdl.XMLEmptyChoiceElementÔ2‘;Œ3ı_\0\0xq\0~\0\0t\0XMLEmptyChoiceElementsq\0~\0_Ñq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.AutomaticÈt?˚_Äê\0\0xq\0~\0t\0	Automaticsq\0~\02\'Oq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0&org.enhydra.shark.xpdl.elements.Manual∑v™âÓ[ÿ§\0\0xq\0~\0t\0Manualsq\0~\0.q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~˛xsq\0~\0E\0\0\0w\0\0\0q\0~˘xt\0\nFinishModesr\0*org.enhydra.shark.xpdl.elements.FinishModeˆÉ¯õúºÛ{\0\0xq\0~\0\0q\0~sq\0~\0XºÎ+q\0~ët\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0\'ﬂ_q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0@“-oq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0yMèq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0Mo⁄|q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Prioritysq\0~â\0q\0~0sq\0~\0Ï79q\0~ët\0\0t\0	Deadlinessr\0)org.enhydra.shark.xpdl.elements.Deadlines>Ωç…ú√⁄\0\0xq\0~\0k\0q\0~4sq\0~\0EŸûq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsr\05org.enhydra.shark.xpdl.elements.SimulationInformation\"ì|I™£í\0\0xq\0~\0\0q\0~;sq\0~\0P$£`q\0~ët\0\0sq\0~\0w\0\0\0t\0\rInstantiationsq\0~\0\0q\0~Asq\0~\0c+/\"q\0~=q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ONCEt\0MULTIPLExt\0Costsr\0$org.enhydra.shark.xpdl.elements.Costœ˘¿ﬁÚëû\0\0xq\0~\0#q\0~Gsq\0~\0ôÅq\0~=t\0\0t\0TimeEstimationsq\0~ùq\0~Lsq\0~\0u†j˜q\0~=t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Qsq\0~\0\0ÍËÄq\0~Mt\0\0t\0WorkingTimesq\0~®\0q\0~Usq\0~\0:ïÄq\0~Mt\0\0t\0Durationsq\0~≠\0q\0~Ysq\0~\0pƒ–q\0~Mt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Rq\0~Vq\0~Zxxsq\0~\0E\0\0\0w\0\0\0q\0~Bq\0~Iq\0~Mxt\0Iconsr\0$org.enhydra.shark.xpdl.elements.Icon´TŒU(ˇ}6\0\0xq\0~\0#\0q\0~_sq\0~\0ˇÙq\0~ët\0\0t\0\rDocumentationsq\0~\07\0q\0~dsq\0~\0Qwπ]q\0~ët\0\0t\0TransitionRestrictionssr\06org.enhydra.shark.xpdl.elements.TransitionRestrictionsC)∑◊Äi;\0\0xq\0~\0k\0q\0~hsq\0~\0˜òfq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\05org.enhydra.shark.xpdl.elements.TransitionRestrictionN˚ƒáπ}˛\0\0xq\0~\0t\0TransitionRestrictionsq\0~\06Ñ|lq\0~jt\0\0sq\0~\0w\0\0\0t\0Joinsr\0$org.enhydra.shark.xpdl.elements.Join⁄ï”©x)Û5\0\0xq\0~\0\0q\0~usq\0~\0Jß*‚q\0~pt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0º¬sq\0~wt\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ANDt\0XORxxsq\0~\0E\0\0\0w\0\0\0q\0~{xt\0Splitsr\0%org.enhydra.shark.xpdl.elements.Split‹ÌØ~—ØWS\0\0xq\0~\0\0q\0~Çsq\0~\0´ÙNq\0~pt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0\nŸúÀq\0~Ñq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssr\0.org.enhydra.shark.xpdl.elements.TransitionRefs†Ô≥—ä·ƒÎ\0\0xq\0~\0k\0q\0~ãsq\0~\0yZGHq\0~Ñt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~àq\0~çxxsq\0~\0E\0\0\0w\0\0\0q\0~wq\0~Ñxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~îsq\0~\0qPÍq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\01org.enhydra.shark.xpdl.elements.ExtendedAttributeª¬\\±ÏF\0\0xq\0~\0t\0ExtendedAttributesq\0~\0Æ0q\0~ït\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0+‹√Ûq\0~õt\0JaWE_GRAPH_PARTICIPANT_IDpt\0Valuesq\0~\0\0q\0~£sq\0~\0Lmq\0~õt\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~†q\0~§xsq\0~öt\0ExtendedAttributesq\0~\0[Qá∆q\0~ït\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\02ñ™£q\0~®t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0ò$Yq\0~®t\0#205.0000228881836,56.76666259765625pxsq\0~\0E\0\0\0w\0\0\0q\0~≠q\0~∞xxpxsq\0~\0E\0\0\0w\0\0\0q\0~ñq\0~ôq\0~ùq\0~°q\0~•q\0~Óq\0~Ûq\0~q\0~1q\0~6q\0~=q\0~aq\0~eq\0~jq\0~ïxsq\0~êt\0Activitysq\0~\0^Î∞q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0`]µ¨q\0~µt\0approvalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0YÊ2\nq\0~µt\0Approvalpt\0Descriptionsq\0~\02\0q\0~¿sq\0~\0^\"X\'q\0~µt\0\0t\0Limitsq\0~é\0q\0~ƒsq\0~\0`&Vq\0~µt\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0‹qâq\0~µt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0qBv⁄q\0~»t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0=m˚µq\0~»t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0m±≈Îq\0~“t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0õ±Ìq\0~◊t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0u\rÒbq\0~◊t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0\'o%q\0~◊t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Díæq\0~Át\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0ÃŒq\0~Áq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Úsq\0~\0–®Bq\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Ïq\0~Ôq\0~Ûxxq\0~€xsq\0~\0E\0\0\0w\0\0\0q\0~◊xsq\0~·t\0\rBlockActivitysq\0~\0KÅêŸq\0~»t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0öΩÏq\0~˙t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~ˇxxq\0~Ãt\0	Performersq\0~Ì\0q\0~sq\0~\0wN	q\0~µt\0approvert\0	StartModesq\0~Ú\0q\0~sq\0~\0fÕ(\Zq\0~µt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0!\r{Bq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0)hRq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0Tq¢wq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0eXÿxq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~#sq\0~\0H,”q\0~µt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0B‡≤|q\0~$t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0W}È#q\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0qÄ;q\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\00\"ëQq\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~,xsq\0~\0E\0\0\0w\0\0\0q\0~(xt\0Prioritysq\0~â\0q\0~?sq\0~\0nÂd+q\0~µt\0\0t\0	Deadlinessq\0~5\0q\0~Csq\0~\0VÈ¨%q\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Isq\0~\0Héõq\0~µt\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0lã&”q\0~Jq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Qsq\0~\0)\n√Uq\0~Jt\0\0t\0TimeEstimationsq\0~ùq\0~Usq\0~\02ô⁄’q\0~Jt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Zsq\0~\0FÈKq\0~Vt\0\0t\0WorkingTimesq\0~®\0q\0~^sq\0~\0s‚Âq\0~Vt\0\0t\0Durationsq\0~≠\0q\0~bsq\0~\0˘áπq\0~Vt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~[q\0~_q\0~cxxsq\0~\0E\0\0\0w\0\0\0q\0~Nq\0~Rq\0~Vxt\0Iconsq\0~`\0q\0~hsq\0~\0p ªÅq\0~µt\0\0t\0\rDocumentationsq\0~\07\0q\0~lsq\0~\0A/Pbq\0~µt\0\0t\0TransitionRestrictionssq\0~i\0q\0~psq\0~\0jÉhq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0pöéq\0~qt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~{sq\0~\0 }‚q\0~vt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0n·ôÁq\0~|q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~Äxt\0Splitsq\0~É\0q\0~Ñsq\0~\0\0º˘„q\0~vt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0\\1“êq\0~Öt\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~çsq\0~\0ÛM˙q\0~Öt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0-org.enhydra.shark.xpdl.elements.TransitionRefÉ%-ÀÒaö\0\0xq\0~\0©t\0\rTransitionRefsq\0~\0MŒ†q\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0E¯GWq\0~ît\0transition3pxsq\0~\0E\0\0\0w\0\0\0q\0~ôxsq\0~ìt\0\rTransitionRefsq\0~\0˜˚q\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0(Õ€çq\0~ùt\0transition6pxsq\0~\0E\0\0\0w\0\0\0q\0~¢xsq\0~ìt\0\rTransitionRefsq\0~\0ª–Xq\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0~ˇÁq\0~¶t\0transition5pxsq\0~\0E\0\0\0w\0\0\0q\0~´xxxsq\0~\0E\0\0\0w\0\0\0q\0~âq\0~éxxsq\0~\0E\0\0\0w\0\0\0q\0~|q\0~Öxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~±sq\0~\0En3q\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0é çq\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0…pbq\0~∑t\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0uUˇ§q\0~∑t\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~ºq\0~øxsq\0~öt\0ExtendedAttributesq\0~\0g#Ôq\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Å`$q\0~√t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0DêY÷q\0~√t\0430,62.79999084472655pxsq\0~\0E\0\0\0w\0\0\0q\0~»q\0~Àxxpxsq\0~\0E\0\0\0w\0\0\0q\0~∫q\0~Ωq\0~¡q\0~≈q\0~»q\0~q\0~q\0~$q\0~@q\0~Dq\0~Jq\0~iq\0~mq\0~qq\0~≤xsq\0~êt\0Activitysq\0~\0b6?q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0.[iºq\0~–t\0	activity1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0aô]`q\0~–t\0Resubmit Proposalpt\0Descriptionsq\0~\02\0q\0~€sq\0~\0uçë7q\0~–t\0\0t\0Limitsq\0~é\0q\0~ﬂsq\0~\03ï8πq\0~–t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0<…3:q\0~–t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0ûq\0~„t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0ç$bq\0~„t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0«Ö˙q\0~Ìt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0a‚4ªq\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0v9Úq\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0ãcq\0~Út\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0904Úq\0~t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0IòÉq\0~q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~\rsq\0~\0wÿﬁq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~\nq\0~xxq\0~ˆxsq\0~\0E\0\0\0w\0\0\0q\0~Úxsq\0~·t\0\rBlockActivitysq\0~\0bA˛¡q\0~„t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0Zn¡ﬂq\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\Zxxq\0~Ìt\0	Performersq\0~Ì\0q\0~sq\0~\0/ÿôëq\0~–t\0	requestert\0	StartModesq\0~Ú\0q\0~\"sq\0~\0&húq\0~–t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0K≤Hfq\0~#t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\03ÀÌ*q\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0$ezq\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0 l∫¡q\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~+xsq\0~\0E\0\0\0w\0\0\0q\0~\'xt\0\nFinishModesq\0~\0q\0~>sq\0~\0Q5Ø\rq\0~–t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0={™ºq\0~?t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0k¡e\rq\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0UjáÉq\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0VÚ∫•q\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~Gxsq\0~\0E\0\0\0w\0\0\0q\0~Cxt\0Prioritysq\0~â\0q\0~Zsq\0~\0%äk1q\0~–t\0\0t\0	Deadlinessq\0~5\0q\0~^sq\0~\0EÉãÄq\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~dsq\0~\0DTZ,q\0~–t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0∞Áëq\0~eq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~lsq\0~\0q«µq\0~et\0\0t\0TimeEstimationsq\0~ùq\0~psq\0~\0(çúq\0~et\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~usq\0~\0Np7àq\0~qt\0\0t\0WorkingTimesq\0~®\0q\0~ysq\0~\0N¸Õ∏q\0~qt\0\0t\0Durationsq\0~≠\0q\0~}sq\0~\0ãìΩq\0~qt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~vq\0~zq\0~~xxsq\0~\0E\0\0\0w\0\0\0q\0~iq\0~mq\0~qxt\0Iconsq\0~`\0q\0~Ésq\0~\0%@ì}q\0~–t\0\0t\0\rDocumentationsq\0~\07\0q\0~ásq\0~\0j√,*q\0~–t\0\0t\0TransitionRestrictionssq\0~i\0q\0~ãsq\0~\0BzΩ«q\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~ësq\0~\0°ÿNq\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0XÎ>Uq\0~ít\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\00KMq\0~ót\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0Í€—q\0~ót\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~úq\0~üxsq\0~öt\0ExtendedAttributesq\0~\0c–∆–q\0~ít\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0b»Eiq\0~£t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0\n\Zq\0~£t\0397,20.787493896484378pxsq\0~\0E\0\0\0w\0\0\0q\0~®q\0~´xxpxsq\0~\0E\0\0\0w\0\0\0q\0~’q\0~ÿq\0~‹q\0~‡q\0~„q\0~q\0~#q\0~?q\0~[q\0~_q\0~eq\0~Ñq\0~àq\0~åq\0~íxsq\0~êt\0Activitysq\0~\0Dki⁄q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0n\ZØ—q\0~∞t\0\rsend_proposalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0!h—¡q\0~∞t\0\rSend Proposalpt\0Descriptionsq\0~\02\0q\0~ªsq\0~\0´;àq\0~∞t\0\0t\0Limitsq\0~é\0q\0~øsq\0~\0@hq\0~∞t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0˙Åcq\0~∞t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0{{≠‰q\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0#Tæxq\0~√t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0 ¸\\\'q\0~Õt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0hE ¬q\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0Nw§£q\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0H÷dkq\0~“t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\nâ2q\0~‚t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0Kfëq\0~‚q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Ìsq\0~\08*ÅÔq\0~‚t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Áq\0~Íq\0~Óxxq\0~÷xsq\0~\0E\0\0\0w\0\0\0q\0~“xsq\0~·t\0\rBlockActivitysq\0~\0C§OΩq\0~√t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0Œ{oq\0~ıt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~˙xxq\0~Õt\0	Performersq\0~Ì\0q\0~˛sq\0~\0·∫)q\0~∞t\0	requestert\0	StartModesq\0~Ú\0q\0~sq\0~\0}®tq\0~∞t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0¨q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0PLd\'q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0Ôlq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0*bMq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~sq\0~\0Ö#Éq\0~∞t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\08€àq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0<?Æ¶q\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0p>ﬁq\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0õJGq\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~\'xsq\0~\0E\0\0\0w\0\0\0q\0~#xt\0Prioritysq\0~â\0q\0~:sq\0~\0ë;q\0~∞t\0\0t\0	Deadlinessq\0~5\0q\0~>sq\0~\0N\ZÀÒq\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Dsq\0~\0N⁄Ìq\0~∞t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0€_Ìq\0~Eq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Lsq\0~\0<y€q\0~Et\0\0t\0TimeEstimationsq\0~ùq\0~Psq\0~\0·oNq\0~Et\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Usq\0~\0\0 +Ñq\0~Qt\0\0t\0WorkingTimesq\0~®\0q\0~Ysq\0~\0\\≤(Gq\0~Qt\0\0t\0Durationsq\0~≠\0q\0~]sq\0~\0>\"`´q\0~Qt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Vq\0~Zq\0~^xxsq\0~\0E\0\0\0w\0\0\0q\0~Iq\0~Mq\0~Qxt\0Iconsq\0~`\0q\0~csq\0~\0,´Û>q\0~∞t\0\0t\0\rDocumentationsq\0~\07\0q\0~gsq\0~\0}Ï{$q\0~∞t\0\0t\0TransitionRestrictionssq\0~i\0q\0~ksq\0~\00QîŒq\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~qsq\0~\0€çWq\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0‚§q\0~rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ñ–Åq\0~wt\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0X:Ñ⁄q\0~wt\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~|q\0~xsq\0~öt\0ExtendedAttributesq\0~\0M1Óq\0~rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0!W?ﬂq\0~Ét\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0g·¨q\0~Ét\0#681.0000381469727,99.78333282470703pxsq\0~\0E\0\0\0w\0\0\0q\0~àq\0~ãxxpxsq\0~\0E\0\0\0w\0\0\0q\0~µq\0~∏q\0~ºq\0~¿q\0~√q\0~ˇq\0~q\0~q\0~;q\0~?q\0~Eq\0~dq\0~hq\0~lq\0~rxsq\0~êt\0Activitysq\0~\0wπNq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0^\\\0q\0~êt\0parallelpq\0~\0sq\0~\0\0q\0~\0sq\0~\0Mtˇ©q\0~êt\0Parallelpt\0Descriptionsq\0~\02\0q\0~õsq\0~\0U≈àq\0~êt\0\0t\0Limitsq\0~é\0q\0~üsq\0~\0iyäq\0~êt\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0[ú\0/q\0~êt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\015Bq\0~£t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0H∆q\0~£t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0:.)°q\0~≠t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0À≥q\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0I+ıq\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0«q\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Tÿñ}q\0~¬t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0Jäq\0~¬q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Õsq\0~\0s‡Aq\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~«q\0~ q\0~Œxxq\0~∂xsq\0~\0E\0\0\0w\0\0\0q\0~≤xsq\0~·t\0\rBlockActivitysq\0~\0j1Hπq\0~£t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0zJ@¸q\0~’t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~⁄xxq\0~ßt\0	Performersq\0~Ì\0q\0~ﬁsq\0~\0=ˇ	q\0~êt\0approvert\0	StartModesq\0~Ú\0q\0~‚sq\0~\0Lâ;q\0~êt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0,\ZtÉq\0~„t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0A,˜q\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\09I~Úq\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0)ËÙöq\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~Îxsq\0~\0E\0\0\0w\0\0\0q\0~Áxt\0\nFinishModesq\0~\0q\0~˛sq\0~\0y√pq\0~êt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0XqShq\0~ˇt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0tÜÛÑq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\06∫K¶q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0	6q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Prioritysq\0~â\0q\0~\Zsq\0~\0_ØÉœq\0~êt\0\0t\0	Deadlinessq\0~5\0q\0~sq\0~\0ˆQmq\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~$sq\0~\0xıﬁ.q\0~êt\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0Ab‘q\0~%q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~,sq\0~\0bpVÃq\0~%t\0\0t\0TimeEstimationsq\0~ùq\0~0sq\0~\0L#÷q\0~%t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~5sq\0~\0\ZÒΩ“q\0~1t\0\0t\0WorkingTimesq\0~®\0q\0~9sq\0~\0KdN¸q\0~1t\0\0t\0Durationsq\0~≠\0q\0~=sq\0~\0+B∑jq\0~1t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~6q\0~:q\0~>xxsq\0~\0E\0\0\0w\0\0\0q\0~)q\0~-q\0~1xt\0Iconsq\0~`\0q\0~Csq\0~\0√Ÿzq\0~êt\0\0t\0\rDocumentationsq\0~\07\0q\0~Gsq\0~\0hÀÙ}q\0~êt\0\0t\0TransitionRestrictionssq\0~i\0q\0~Ksq\0~\0±T6q\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0Àòéq\0~Lt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~Vsq\0~\09\Z› q\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0Y®OXq\0~Wq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~[xt\0Splitsq\0~É\0q\0~_sq\0~\0€Ωüq\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0HKÇq\0~`t\0ANDsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~hsq\0~\0\'ãl}q\0~`t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ìt\0\rTransitionRefsq\0~\0ö]îq\0~it\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0d\"~q\0~nt\0transition7pxsq\0~\0E\0\0\0w\0\0\0q\0~sxsq\0~ìt\0\rTransitionRefsq\0~\0IÀ;Iq\0~it\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\09ò	qq\0~wt\0transition8pxsq\0~\0E\0\0\0w\0\0\0q\0~|xxxsq\0~\0E\0\0\0w\0\0\0q\0~dq\0~ixxsq\0~\0E\0\0\0w\0\0\0q\0~Wq\0~`xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Çsq\0~\0?ˆZçq\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0f)q\0~Ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0X›∑/q\0~àt\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0\\“kq\0~àt\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~çq\0~êxsq\0~öt\0ExtendedAttributesq\0~\0C∞◊«q\0~Ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0w2◊∂q\0~ît\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0B„r∞q\0~ît\0588,61.599993896484364pxsq\0~\0E\0\0\0w\0\0\0q\0~ôq\0~úxxpxsq\0~\0E\0\0\0w\0\0\0q\0~ïq\0~òq\0~úq\0~†q\0~£q\0~ﬂq\0~„q\0~ˇq\0~q\0~q\0~%q\0~Dq\0~Hq\0~Lq\0~Éxsq\0~êt\0Activitysq\0~\0\r¶SDq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0:¨O∞q\0~°t\0tool1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0HÌ≥ÿq\0~°t\0\ZSend Approval Notificationpt\0Descriptionsq\0~\02\0q\0~¨sq\0~\0)ÉRq\0~°t\0\0t\0Limitsq\0~é\0q\0~∞sq\0~\0p,√fq\0~°t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0O/ÇTq\0~°t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0jyã\Zq\0~¥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0YŸg≥q\0~¥t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0O¯∏q\0~æt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0|ˆæîq\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0Zq€Kq\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0$org.enhydra.shark.xpdl.elements.Tool\\6ñ&∂+GÅ\0\0xq\0~\0©t\0Toolsq\0~\0ºv>q\0~Õt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Yù˜éq\0~‘t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0Ω;ôq\0~‘q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0APPLICATIONt\0	PROCEDURExt\0ActualParameterssq\0~Ÿ\0q\0~·sq\0~\0[\Zq\0~‘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~Ásq\0~\0NÊ·Rq\0~‘t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Îsq\0~\06ìc6q\0~‘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~Ÿq\0~‹q\0~‚q\0~Ëq\0~Ïxxsq\0~…t\0SubFlowsq\0~\0HáÀIq\0~√t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Bµ™q\0~Út\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0˛æ»q\0~Úq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~˝sq\0~\0_Åóq\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~˜q\0~˙q\0~˛xxq\0~Õxsq\0~\0E\0\0\0w\0\0\0q\0~√xsq\0~·t\0\rBlockActivitysq\0~\0)*“Eq\0~¥t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0F (~q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\nxxq\0~æt\0	Performersq\0~Ì\0q\0~sq\0~\08÷÷åq\0~°t\0approvert\0	StartModesq\0~Ú\0q\0~sq\0~\0k?îÜq\0~°t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0x€^q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0Ãêq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0(34q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0p∫Fòq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~.sq\0~\0^Ì9•q\0~°t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0&ù≤pq\0~/t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0HÀÓq\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0 GnWq\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0\n©Æ5q\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~7xsq\0~\0E\0\0\0w\0\0\0q\0~3xt\0Prioritysq\0~â\0q\0~Jsq\0~\0.Áíaq\0~°t\0\0t\0	Deadlinessq\0~5\0q\0~Nsq\0~\0hobËq\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Tsq\0~\0sAúdq\0~°t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\04^\\?q\0~Uq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~\\sq\0~\0&8¥Oq\0~Ut\0\0t\0TimeEstimationsq\0~ùq\0~`sq\0~\05”ÍBq\0~Ut\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~esq\0~\0Jé‚ q\0~at\0\0t\0WorkingTimesq\0~®\0q\0~isq\0~\0˜Íq\0~at\0\0t\0Durationsq\0~≠\0q\0~msq\0~\0oé∑˜q\0~at\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~fq\0~jq\0~nxxsq\0~\0E\0\0\0w\0\0\0q\0~Yq\0~]q\0~axt\0Iconsq\0~`\0q\0~ssq\0~\0E%¡æq\0~°t\0\0t\0\rDocumentationsq\0~\07\0q\0~wsq\0~\0xá¥ÿq\0~°t\0\0t\0TransitionRestrictionssq\0~i\0q\0~{sq\0~\0KƒkÇq\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Åsq\0~\0QÔ\\q\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0n 6q\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0p˙ Òq\0~át\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0Eª}q\0~át\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~åq\0~èxsq\0~öt\0ExtendedAttributesq\0~\0o/Xq\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\0‡q\0~ìt\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0!ºQXq\0~ìt\0735,63.974993896484364pxsq\0~\0E\0\0\0w\0\0\0q\0~òq\0~õxxpxsq\0~\0E\0\0\0w\0\0\0q\0~¶q\0~©q\0~≠q\0~±q\0~¥q\0~q\0~q\0~/q\0~Kq\0~Oq\0~Uq\0~tq\0~xq\0~|q\0~Çxsq\0~êt\0Activitysq\0~\0?qlq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ûê·q\0~†t\0tool2pq\0~\0sq\0~\0\0q\0~\0sq\0~\0)Çñ5q\0~†t\0Send Reject Notificationpt\0Descriptionsq\0~\02\0q\0~´sq\0~\0)BØÙq\0~†t\0\0t\0Limitsq\0~é\0q\0~Øsq\0~\0j7q\0~†t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0]*—Dq\0~†t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0`§◊Íq\0~≥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0\\%˘q\0~≥t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0®ò!q\0~Ωt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0»Aq\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0CﬁTåq\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~”t\0Toolsq\0~\0p,îq\0~Ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0|õq\0~“t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0cvCq\0~“q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~ﬂq\0~‡xt\0ActualParameterssq\0~Ÿ\0q\0~›sq\0~\0E8\\Àq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~„sq\0~\0F\n…”q\0~“t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Ásq\0~\0IëI\nq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~◊q\0~⁄q\0~ﬁq\0~‰q\0~Ëxxsq\0~…t\0SubFlowsq\0~\00@p#q\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0zcüöq\0~Ót\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0a9Wq\0~Óq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~˘sq\0~\0PrS»q\0~Ót\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Ûq\0~ˆq\0~˙xxq\0~Ãxsq\0~\0E\0\0\0w\0\0\0q\0~¬xsq\0~·t\0\rBlockActivitysq\0~\0c\0‡çq\0~≥t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0y<8q\0~	t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~	xxq\0~Ωt\0	Performersq\0~Ì\0q\0~	\nsq\0~\0,c¶yq\0~†t\0approvert\0	StartModesq\0~Ú\0q\0~	sq\0~\0nÕL.q\0~†t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0\n≠›Úq\0~	t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0ìq\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0\'â¢q\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0/y1Àq\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	xsq\0~\0E\0\0\0w\0\0\0q\0~	xt\0\nFinishModesq\0~\0q\0~	*sq\0~\0\\8÷æq\0~†t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0{ÖN3q\0~	+t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0vl≠åq\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0PçKq\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0π€’q\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	3xsq\0~\0E\0\0\0w\0\0\0q\0~	/xt\0Prioritysq\0~â\0q\0~	Fsq\0~\0wGVŸq\0~†t\0\0t\0	Deadlinessq\0~5\0q\0~	Jsq\0~\0dTÖpq\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~	Psq\0~\0\0Ã0dq\0~†t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0R˘$€q\0~	Qq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~	Xsq\0~\0≈A√q\0~	Qt\0\0t\0TimeEstimationsq\0~ùq\0~	\\sq\0~\0_˙(Nq\0~	Qt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~	asq\0~\0>‘l≈q\0~	]t\0\0t\0WorkingTimesq\0~®\0q\0~	esq\0~\0%êX8q\0~	]t\0\0t\0Durationsq\0~≠\0q\0~	isq\0~\0ÓŒq\0~	]t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~	bq\0~	fq\0~	jxxsq\0~\0E\0\0\0w\0\0\0q\0~	Uq\0~	Yq\0~	]xt\0Iconsq\0~`\0q\0~	osq\0~\0X<q\0~†t\0\0t\0\rDocumentationsq\0~\07\0q\0~	ssq\0~\0˜ŸQq\0~†t\0\0t\0TransitionRestrictionssq\0~i\0q\0~	wsq\0~\0$\0èq\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~	}sq\0~\0By‘Qq\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0y∞ºÚq\0~	~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Öƒq\0~	Ét\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\01Îkq\0~	Ét\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~	àq\0~	ãxsq\0~öt\0ExtendedAttributesq\0~\0$˝√q\0~	~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0{Ü˘q\0~	èt\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0BHËq\0~	èt\0394,168.39999694824218pxsq\0~\0E\0\0\0w\0\0\0q\0~	îq\0~	óxxpxsq\0~\0E\0\0\0w\0\0\0q\0~•q\0~®q\0~¨q\0~∞q\0~≥q\0~	q\0~	q\0~	+q\0~	Gq\0~	Kq\0~	Qq\0~	pq\0~	tq\0~	xq\0~	~xsq\0~êt\0Activitysq\0~\0( †Ïq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0æx\\q\0~	út\0route1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0 ö”q\0~	út\0Route 1pt\0Descriptionsq\0~\02\0q\0~	ßsq\0~\0˝`q\0~	út\0\0t\0Limitsq\0~é\0q\0~	´sq\0~\0W∞cq\0~	út\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0*#ß≠q\0~	út\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0&YV8q\0~	Øt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0V›Knq\0~	Øt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0i~q\0~	πt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0+.´úq\0~	æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0G™¿Wq\0~	æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0{e˜q\0~	æt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\\˚Ørq\0~	Œt\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0Wù:q\0~	Œq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~	Ÿsq\0~\0n]_q\0~	Œt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~	”q\0~	÷q\0~	⁄xxq\0~	¬xsq\0~\0E\0\0\0w\0\0\0q\0~	æxsq\0~·t\0\rBlockActivitysq\0~\0k)Ωq\0~	Øt\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0j(0q\0~	·t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~	Êxxq\0~	≥t\0	Performersq\0~Ì\0q\0~	Ísq\0~\0T®·q\0~	út\0	requestert\0	StartModesq\0~Ú\0q\0~	Ósq\0~\05M⁄mq\0~	út\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0™p°q\0~	Ôt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\01»ä‹q\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0Œ jq\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\08Dáq\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	˜xsq\0~\0E\0\0\0w\0\0\0q\0~	Ûxt\0\nFinishModesq\0~\0q\0~\n\nsq\0~\0wµıÈq\0~	út\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0ﬁ©q\0~\nt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0GCÌ¬q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\06B q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0ZUEΩq\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~\nxsq\0~\0E\0\0\0w\0\0\0q\0~\nxt\0Prioritysq\0~â\0q\0~\n&sq\0~\0{öOÜq\0~	út\0\0t\0	Deadlinessq\0~5\0q\0~\n*sq\0~\0#ÔÄ q\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~\n0sq\0~\0‚\"äq\0~	út\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0uÍ˜&q\0~\n1q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~\n8sq\0~\0\\%`bq\0~\n1t\0\0t\0TimeEstimationsq\0~ùq\0~\n<sq\0~\0R¸q\0~\n1t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~\nAsq\0~\0Ò∆fq\0~\n=t\0\0t\0WorkingTimesq\0~®\0q\0~\nEsq\0~\05Ä@yq\0~\n=t\0\0t\0Durationsq\0~≠\0q\0~\nIsq\0~\0B—\0€q\0~\n=t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~\nBq\0~\nFq\0~\nJxxsq\0~\0E\0\0\0w\0\0\0q\0~\n5q\0~\n9q\0~\n=xt\0Iconsq\0~`\0q\0~\nOsq\0~\0[L.œq\0~	út\0\0t\0\rDocumentationsq\0~\07\0q\0~\nSsq\0~\0Bó6q\0~	út\0\0t\0TransitionRestrictionssq\0~i\0q\0~\nWsq\0~\0u£ïq\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n]sq\0~\0?>\"q\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\03G\"|q\0~\n^t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Ceëdq\0~\nct\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0n‰*xq\0~\nct\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~\nhq\0~\nkxsq\0~öt\0ExtendedAttributesq\0~\0,Œ;q\0~\n^t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ÚK`q\0~\not\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0zÛq\0~\not\0228.0000228881836,22pxsq\0~\0E\0\0\0w\0\0\0q\0~\ntq\0~\nwxxpxsq\0~\0E\0\0\0w\0\0\0q\0~	°q\0~	§q\0~	®q\0~	¨q\0~	Øq\0~	Îq\0~	Ôq\0~\nq\0~\n\'q\0~\n+q\0~\n1q\0~\nPq\0~\nTq\0~\nXq\0~\n^xxt\0Transitionssr\0+org.enhydra.shark.xpdl.elements.TransitionsÌ9>‰¿/iØ\0\0xq\0~\0k\0q\0~\n|sq\0~\0œ¯Hq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0*org.enhydra.shark.xpdl.elements.Transitiontßx∏Ö\0\0xq\0~\0©t\0\nTransitionsq\0~\0H±•ˇq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Fæ#q\0~\nÑt\0transition2pq\0~\0sq\0~\0\0q\0~\0sq\0~\0LZÄq\0~\nÑt\0\0pt\0Fromsq\0~\0q\0~\nèsq\0~\0WH˘ﬁq\0~\nÑt\0approve_proposalpt\0Tosq\0~\0q\0~\nìsq\0~\0J{Ú<q\0~\nÑt\0approvalpt\0	Conditionsr\0)org.enhydra.shark.xpdl.elements.Condition€‡D··Z;|\0\0xq\0~\0\0q\0~\nósq\0~\0Xöãq\0~\nÑt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0HÅ^q\0~\nôq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0	CONDITIONt\0	OTHERWISEt\0	EXCEPTIONt\0DEFAULTEXCEPTIONxxsq\0~\0E\0\0\0w\0\0\0q\0~\nùxt\0Descriptionsq\0~\02\0q\0~\n•sq\0~\0]˛Ô`q\0~\nÑt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n©sq\0~\0\"&q\0~\nÑt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0pqõq\0~\n™t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0 Õëq\0~\nØt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0!]Ò?q\0~\nØt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\n¥q\0~\n∑xxpxsq\0~\0E\0\0\0w\0\0\0q\0~\nâq\0~\nåq\0~\nêq\0~\nîq\0~\nôq\0~\n¶q\0~\n™xsq\0~\nÉt\0\nTransitionsq\0~\0e∆q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Åõ≈q\0~\nºt\0transition3pq\0~\0sq\0~\0\0q\0~\0sq\0~\0N7q\0~\nºt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0Y(q\0~\nºt\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\0\0åÍ◊q\0~\nºt\0	activity1pt\0	Conditionsq\0~\nò\0q\0~\nÕsq\0~\0M)7q\0~\nºt\0status===\'resubmit\'sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0ß}\nq\0~\nŒt\0	CONDITIONsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~\n“xt\0Descriptionsq\0~\02\0q\0~\n◊sq\0~\0|\Zˆ˚q\0~\nºt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n€sq\0~\0LÁ?q\0~\nºt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0c¨‰˛q\0~\n‹t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\02⁄6¿q\0~\n·t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0|çóq\0~\n·t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\nÊq\0~\nÈxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\n¡q\0~\nƒq\0~\n«q\0~\n q\0~\nŒq\0~\nÿq\0~\n‹xsq\0~\nÉt\0\nTransitionsq\0~\0]xqq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0B◊Vq\0~\nÓt\0transition4pq\0~\0sq\0~\0\0q\0~\0sq\0~\0*]y&q\0~\nÓt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0Y˜ıQq\0~\nÓt\0	activity1pq\0~\nìsq\0~\0q\0~\nìsq\0~\0:¯¥ôq\0~\nÓt\0approve_proposalpt\0	Conditionsq\0~\nò\0q\0~\nˇsq\0~\04≥]®q\0~\nÓt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0Rà{q\0~\0q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Descriptionsq\0~\02\0q\0~sq\0~\0-´Q¶q\0~\nÓt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0l˛â|q\0~\nÓt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\03%qq\0~\rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0 sXçq\0~t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0uÚ!Íq\0~t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~\Zxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\nÛq\0~\nˆq\0~\n˘q\0~\n¸q\0~\0q\0~	q\0~\rxsq\0~\nÉt\0\nTransitionsq\0~\0VlC√q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0~\0,8q\0~t\0transition6pq\0~\0sq\0~\0\0q\0~\0sq\0~\0q‡µq\0~t\0approvedpq\0~\nèsq\0~\0q\0~\nèsq\0~\0GÂ¸“q\0~t\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\0}Ëcìq\0~t\0parallelpt\0	Conditionsq\0~\nò\0q\0~0sq\0~\0-7 q\0~t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0E‹†q\0~1t\0	OTHERWISEsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~5xt\0Descriptionsq\0~\02\0q\0~:sq\0~\0{∑»q\0~t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~>sq\0~\0mÙîWq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0&=Áq\0~?t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0a&q\0~Dt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0Z¶øq\0~Dt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~Iq\0~Lxxpxsq\0~\0E\0\0\0w\0\0\0q\0~$q\0~\'q\0~*q\0~-q\0~1q\0~;q\0~?xsq\0~\nÉt\0\nTransitionsq\0~\0.èZ¯q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Gf^]q\0~Qt\0transition7pq\0~\0sq\0~\0\0q\0~\0sq\0~\0+v›ˆq\0~Qt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0QæÀq\0~Qt\0parallelpq\0~\nìsq\0~\0q\0~\nìsq\0~\0D«TΩq\0~Qt\0\rsend_proposalpt\0	Conditionsq\0~\nò\0q\0~bsq\0~\0˙\n˙q\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0¸(q\0~cq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~gxt\0Descriptionsq\0~\02\0q\0~ksq\0~\0u‡ﬁVq\0~Qt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~osq\0~\0hd4qq\0~Qt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0cvKxq\0~pt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0wõÏq\0~ut\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0¢ªÓq\0~ut\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~zq\0~}xxpxsq\0~\0E\0\0\0w\0\0\0q\0~Vq\0~Yq\0~\\q\0~_q\0~cq\0~lq\0~pxsq\0~\nÉt\0\nTransitionsq\0~\06¬∏q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\"=“%q\0~Çt\0transition8pq\0~\0sq\0~\0\0q\0~\0sq\0~\0YUE≤q\0~Çt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0g>¶ìq\0~Çt\0parallelpq\0~\nìsq\0~\0q\0~\nìsq\0~\0W¶Ècq\0~Çt\0tool1pt\0	Conditionsq\0~\nò\0q\0~ìsq\0~\0ÑÊq\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0múDªq\0~îq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~òxt\0Descriptionsq\0~\02\0q\0~úsq\0~\0\rëUöq\0~Çt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~†sq\0~\0~\ZÈOq\0~Çt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0O®‹≈q\0~°t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0=ÀèÆq\0~¶t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0-»jq\0~¶t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~´q\0~Æxxpxsq\0~\0E\0\0\0w\0\0\0q\0~áq\0~äq\0~çq\0~êq\0~îq\0~ùq\0~°xsq\0~\nÉt\0\nTransitionsq\0~\0ZÒ¬>q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0<Jê§q\0~≥t\0transition5pq\0~\0sq\0~\0\0q\0~\0sq\0~\0A¸¿»q\0~≥t\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0S6sQq\0~≥t\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\02· fq\0~≥t\0tool2pt\0	Conditionsq\0~\nò\0q\0~ƒsq\0~\0»\n˚q\0~≥t\0status===\'rejected\'sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0u_Ëq\0~≈t\0	CONDITIONsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~…xt\0Descriptionsq\0~\02\0q\0~Œsq\0~\0N]£>q\0~≥t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~“sq\0~\0iÄ¿Ωq\0~≥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0OÃ¶ßq\0~”t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ÏÙ7q\0~ÿt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0T‡0q\0~ÿt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~›q\0~‡xxpxsq\0~\0E\0\0\0w\0\0\0q\0~∏q\0~ªq\0~æq\0~¡q\0~≈q\0~œq\0~”xsq\0~\nÉt\0\nTransitionsq\0~\0-òæOq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0.ê€Qq\0~Ât\0transition1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0#ÉÍq\0~Ât\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0ÀÃ≈q\0~Ât\0route1pq\0~\nìsq\0~\0q\0~\nìsq\0~\0wˆÕGq\0~Ât\0approve_proposalpt\0	Conditionsq\0~\nò\0q\0~ˆsq\0~\0KÅR¯q\0~Ât\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0u©q\0~˜q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~˚xt\0Descriptionsq\0~\02\0q\0~ˇsq\0~\0S(yUq\0~Ât\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0oGB”q\0~Ât\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0y^M∆q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0:tﬂyq\0~	t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0x•Êgq\0~	t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~xxpxsq\0~\0E\0\0\0w\0\0\0q\0~Íq\0~Ìq\0~q\0~Ûq\0~˜q\0~\0q\0~xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0f„£ßq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0¥s”q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0/ÔYßq\0~t\0%JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDERpq\0~£sq\0~\0\0q\0~£sq\0~\0Z¯æ\\q\0~t\0requester;approverpxsq\0~\0E\0\0\0w\0\0\0q\0~!q\0~$xsq\0~öt\0ExtendedAttributesq\0~\0/«wq\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0˝†q\0~(t\0JaWE_GRAPH_START_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0RäPôq\0~(t\0ûJaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route1,X_OFFSET=87,Y_OFFSET=28,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~-q\0~0xsq\0~öt\0ExtendedAttributesq\0~\0M q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ä,aq\0~4t\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0s‹Sq\0~4t\0õJaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool1,X_OFFSET=901,Y_OFFSET=74,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~9q\0~<xsq\0~öt\0ExtendedAttributesq\0~\0sãT2q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0F◊˝«q\0~@t\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0Û(Xq\0~@t\0•JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=send_proposal,X_OFFSET=849,Y_OFFSET=110,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~Eq\0~Hxsq\0~öt\0ExtendedAttributesq\0~\0V…ä1q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0 ~q\0~Lt\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0äùÂq\0~Lt\0úJaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool2,X_OFFSET=579,Y_OFFSET=180,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~Qq\0~Txxpxsq\0~\0E\0\0\0\rw\0\0\0\rq\0~cq\0~fq\0~jq\0~qq\0~¥q\0~”q\0~Ÿq\0~wq\0~}q\0~Ñq\0~ãq\0~\n~q\0~xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Ysq\0~\0U˚îq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0pä=pq\0~Zt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0*›⁄q\0~_t\0EDITING_TOOLpq\0~£sq\0~\0\0q\0~£sq\0~\0~√Y4q\0~_t\0Web Workflow Designerpxsq\0~\0E\0\0\0w\0\0\0q\0~dq\0~gxsq\0~öt\0ExtendedAttributesq\0~\03xÒZq\0~Zt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0∏;¯q\0~kt\0EDITING_TOOL_VERSIONpq\0~£sq\0~\0\0q\0~£sq\0~\0VÒfÏq\0~kt\0\r5.0-pre-alphapxsq\0~\0E\0\0\0w\0\0\0q\0~pq\0~sxxpxsq\0~\0E\0\0\0\rw\0\0\0\rq\0~\0q\0~\0q\0~\0q\0~\0Iq\0~\0tq\0~\0Çq\0~\0ïq\0~\0úq\0~\0£q\0~q\0~Qq\0~Xq\0~Zx\0sq\0~\0w\0\0\0\0xt\01sr\0*org.enhydra.shark.xpdl.elements.Namespaces|ö—<.Rßñ\0\0xq\0~\0kt\0\nNamespacessq\0~\0\nd0¥q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0)org.enhydra.shark.xpdl.elements.NamespaceËz¬¬_\0\0xq\0~\0t\0	Namespacesq\0~\0Q…dq\0~{t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0p¸“¿q\0~Çt\0xpdlpq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0\'ÿ°®q\0~Çt\0 http://www.wfmc.org/2002/XPDL1.0pxsq\0~\0E\0\0\0w\0\0\0q\0~áq\0~äxx',1000202,1,1000203,0);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SHKXPDLS`
--

LOCK TABLES `SHKXPDLS` WRITE;
/*!40000 ALTER TABLE `SHKXPDLS` DISABLE KEYS */;
INSERT INTO `SHKXPDLS` VALUES
('crm_community','1',1184650391000,'2023-11-01 13:38:44',1000202,0);
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
  `license` text DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  `meta` longtext DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_app`
--

LOCK TABLES `app_app` WRITE;
/*!40000 ALTER TABLE `app_app` DISABLE KEYS */;
INSERT INTO `app_app` VALUES
('appcenter',1,'App Center','','2023-11-01 13:39:15','2023-11-01 13:39:15','oRIgWuw8ed5OmS98TSZFxocskOFXU0v3VPneM0k80NqSBK2r6RhNzTNTryZkuj4W',NULL,NULL),
('crm_community',1,'CRM Community','','2023-11-01 13:38:34','2023-11-01 13:38:46','Vfe6Df5AdlrGEPqFYgTt8XuGJ4XiTt2NnBLEovw3qoQ=',NULL,NULL);
/*!40000 ALTER TABLE `app_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_builder`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_builder` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `json` longtext DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `idx_name` (`name`),
  KEY `idx_type` (`type`),
  CONSTRAINT `FK_idup4nrrc79iy4kc46wf5919j` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_builder`
--

LOCK TABLES `app_builder` WRITE;
/*!40000 ALTER TABLE `app_builder` DISABLE KEYS */;
INSERT INTO `app_builder` VALUES
('appcenter',1,'INTERNAL_TAGGING','Tagging','internal','2023-11-01 13:39:15','2023-11-01 13:39:15','{\n    \"datas\": {\n        \"form\": {},\n        \"list\": {},\n        \"userview\": {\n            \"v\": [\"t07\"],\n            \"v3\": [\"t08\"]\n        }\n    },\n    \"labels\": {\n        \"t01\": {\"color\": \"red\"},\n        \"t02\": {\"color\": \"pink\"},\n        \"t03\": {\"color\": \"orange\"},\n        \"t04\": {\"color\": \"yellow\"},\n        \"t05\": {\"color\": \"green\"},\n        \"t06\": {\"color\": \"lime\"},\n        \"t07\": {\n            \"color\": \"blue\",\n            \"label\": \"Default\"\n        },\n        \"t08\": {\n            \"color\": \"sky\",\n            \"label\": \"Glass\"\n        },\n        \"t09\": {\"color\": \"purple\"},\n        \"t10\": {\"color\": \"black\"}\n    }\n}',NULL),
('appcenter',1,'up-0497CD0F62BD44DEEAF1294D58166A1A','Design App','INTERNAL_USERVIEW_PAGE','2023-11-01 13:39:15','2023-11-01 13:39:15','{\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-color\":\"#343A40\",\"textContent\":\"<i class=\\\"fas fa-pencil-ruler\\\"><\\/i> Design App\",\"id\":\"4F8DD38005EF4A1D6E90DB9905DB85B1\",\"tagName\":\"h2\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.LabelComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#5B6DD0\",\"style-font-size\":\"15px\",\"textContent\":\"<div class=\\\"fs-5 text-dark fw-bold text-hover-primary text-dark lh-base text-gray-600\\\">You can create a plain new app or using existing template.<\\/div>\",\"id\":\"B29BC24932A1434C38A57D6762E392F4\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-tablet-flex\":\"0 0 80%\",\"id\":\"250FBB9B445C4ED51FACA40E02FD68FD\",\"style-tablet-max-width\":\"80%\"}},{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.Link\",\"properties\":{\"style-position\":\"relative\",\"style-color\":\"#009EF7\",\"style-float\":\"right\",\"label\":\"View All Templates\",\"type\":\"pagecomponent\",\"url\":\"templates\",\"target\":\"self\",\"style-font-weight\":\"500\",\"style-top\":\"0px\",\"userviewCacheDuration\":\"20\",\"userviewCacheScope\":\"\",\"id\":\"B2E40400A1B0460E2EAD915F31D05A40\",\"style-mobile-float\":\"left\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"9A60610B505C43E7600120C6E0A0049B\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"5px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"80%\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"AC0E8473AD5A458AE62FF9731C57904C\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HorizontalRuleComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"2rem\",\"style-border-style\":\"dashed\",\"id\":\"2467CC0560574D034B27F12A07668BDB\",\"style-border-color\":\"#E4E6EF\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<div class=\\\"card data-row\\\">\\n    <div class=\\\"card-body\\\">\\n        <div style=\\\"\\\" class=\\\"ph_columns column_5 column_body column_img body_column_5 card-text mb-1\\\">\\n            <div style=\\\"background-image:url(\'#request.contextPath#/images/appCenterCreateApp.png\');height:0;padding-bottom:50%;width:100%;background-size:cover;background-repeat: no-repeat;display:inline-block;\\\"><\\/div>\\n        <\\/div> \\n        <div style=\\\"\\\" class=\\\"ph_columns column_0 column_body column_name body_column_0 card-text mb-1\\\">\\n            Create Your App\\n        <\\/div>     \\n        <div style=\\\"\\\" class=\\\"ph_columns column_6 column_body column_desc body_column_6 card-text mb-1\\\">\\n            <p>Create your own enterprise application with Joget. Get started now!<\\/p>\\n        <\\/div>\\n        <div class=\\\"card-actions\\\">\\n            <a style=\\\"\\\" href=\\\"javascript:appCreate(\'\');\\\" target=\\\"_self\\\" class=\\\"ph_rowActions rowAction_0 rowaction_body body_rowAction_0  btn btn-sm btn-primary link_rowAction_0\\\">Create New App<\\/a>\\n        <\\/div>\\n    <\\/div>\\n<\\/div>\",\"color\":\"#348fe2\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"\",\"id\":\"EC89DBB912E2451322834E357EFEF5F9\",\"title\":\"TOTAL VISITS\",\"customId\":\"\",\"value\":\"3,291,922\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-flex\":\"0 0 33.33%\",\"style-max-width\":\"33.33%\"}},{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"cacheAllLinks\":\"\",\"customHeader\":\"\",\"style-border-bottom-width\":\"0px\",\"keyName\":\"\",\"customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"List\",\"customId\":\"newTemplate\",\"buttonPosition\":\"bottomLeft\",\"datalistId\":\"list_newTemplate\",\"checkboxPosition\":\"no\",\"selectionType\":\"multiple\",\"userviewCacheDuration\":\"20\",\"cacheListAction\":\"\",\"userviewCacheScope\":\"\",\"style-border-left-width\":\"0px\",\"style-border-right-width\":\"0px\",\"id\":\"85384F9C698948E1CB5A00FB08FE4A3F\",\"rowCount\":\"\",\"style-border-top-width\":\"0px\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"15px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"33.33%\",\"style-tablet-max-width\":\"\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"B28AB2BFC72B4764DE5EACAF8BFD5C3C\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-padding-right\":\"15px\",\"style-margin-bottom\":\"50px\",\"id\":\"894EDF7ED18C446DEF9B2A341D113819\",\"customId\":\"\",\"style-padding-left\":\"15px\"}},{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-color\":\"#343A40\",\"style-font-size\":\"1.35rem\",\"textContent\":\"Community\",\"id\":\"4F3CD71D50784C33052FC55E464B0B46\",\"tagName\":\"h5\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.LabelComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#5B6DD0\",\"style-font-size\":\"15px\",\"textContent\":\"<div class=\\\"fs-5 text-dark fw-bold text-hover-primary text-dark lh-base text-gray-600\\\"><span>Joget provides an all encompassing ecosystem that supports community learning and adoption.<\\/span><\\/div>\",\"id\":\"19826EDB85514108BF420DC7B5C8C34D\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HorizontalRuleComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"2rem\",\"style-border-style\":\"dashed\",\"id\":\"C693D3BA71754746C740445CD1FABE88\",\"style-border-color\":\"#E4E6EF\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<div class=\\\"main-wrapper\\\">\\r\\n\\t<a href=\\\"https://marketplace.joget.org/\\\" target=\\\"_blank\\\"><i class=\\\"material-icons fas fa-cart-arrow-down\\\"><\\/i><\\/a>\\r\\n<\\/div>\\r\\n<style>\\r\\n.main-wrapper {\\r\\n\\t\\tborder-radius: 4px;\\r\\n\\t\\tpadding: 10px;\\r\\n\\t\\tposition: relative;\\r\\n\\t\\ttext-align: center;\\r\\n}\\r\\n\\r\\ni.material-icons {\\r\\n\\t\\tfont-size: 1.5rem;\\r\\n\\t\\tcolor: white;\\r\\n\\t\\tposition: relative;\\r\\n\\t\\tborder-radius: 50%;\\r\\n\\t\\tpadding: 5px;\\r\\n\\t\\tmargin: 3px;\\r\\n\\t\\twidth:80px;\\r\\n\\t\\theight:80px;\\r\\n\\t\\ttext-align:center;\\r\\n\\t\\tbox-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24);\\r\\n\\t\\ttransition: color 0.2s ease, background-color 0.2s ease, transform 0.3s ease;\\r\\n}\\r\\n\\r\\ni.material-icons:after {\\r\\n\\t\\tcontent: \\\"\\\";\\r\\n\\t\\twidth: 100%;\\r\\n\\t\\theight: 100%;\\r\\n\\t\\tborder: solid 2px;\\r\\n\\t\\ttransform: scale(0.8);\\r\\n\\t\\tposition: absolute;\\r\\n\\t\\ttop: -3px;\\r\\n\\t\\tleft: -10px;\\r\\n\\t\\tborder-radius: 50%;\\r\\n\\t\\ttransition: all 0.3s ease;\\r\\n}\\r\\n\\r\\ni.material-icons:hover:after {\\r\\n\\t\\ttransform: scale(1);\\r\\n\\t\\tbox-shadow: 10px 0 20px rgba(0, 0, 0, 0.19), 6px 0 6px rgba(0, 0, 0, 0.23);\\r\\n}\\r\\n\\r\\ni.material-icons:nth-of-type(1) {\\r\\n\\t\\tbackground-color:#009EF7;\\r\\n}\\r\\n\\r\\ni.material-icons:nth-of-type(1):hover {\\r\\n\\t\\tcolor: #009EF7;\\r\\n}\\r\\n\\r\\ni.material-icons:nth-of-type(1):after {\\r\\n\\t\\tborder-color: #009EF7;\\r\\n}\\r\\n\\r\\ni.material-icons:hover {\\r\\n\\t\\tbackground-color: transparent;\\r\\n\\t\\ttransform: rotate(90deg);\\r\\n\\t\\tcursor: pointer;\\r\\n\\t\\tbox-shadow: none;\\r\\n}\\r\\n\\r\\n\\ti.material-icons {\\r\\n\\t\\tpadding:20px;\\r\\n\\t\\tmargin:10px;\\r\\n\\t\\tfont-size:2.1rem;\\r\\n\\t}\\r\\n\\ti.material-icons:after {\\r\\n\\t\\tborder-width:3px;\\r\\n\\t\\ttop:-3px;\\r\\n\\t\\tleft:-3px;\\r\\n}\\r\\n<\\/style>\",\"id\":\"9AD7CD678EB24D8E6984BEF7A5FAD79E\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-text-align\":\"center\",\"style-color\":\"#343A40\",\"style-font-size\":\"21px\",\"textContent\":\"Marketplace\",\"id\":\"E6C15176381741ADAC0D487A6738FBD1\",\"tagName\":\"h1\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ParagraphComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"5px\",\"style-margin-bottom\":\"5px\",\"textContent\":\"<p class=\\\"CDesc\\\" style=\\\"text-align: center;\\\">Download ready made apps, plugins, templates and more from the Joget Marketplace.<\\/p>\",\"id\":\"74D99A11DD9B415E72D30379ECF0E049\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"id\":\"DA2EAD04C86444C6D7C8BAAFF45BA375\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-tablet-flex\":\"0 0 33%\",\"style-tablet-max-width\":\"33%\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<div class=\\\"main-wrapper\\\">\\r\\n    <a class=\\\"d-block overlay\\\" data-fslightbox=\\\"lightbox-hot-sales\\\" href=\\\"https://answers.joget.org/\\\" target=\\\"_blank\\\">\\r\\n    <i class=\\\"material-icons far fa-comment\\\"><\\/i><\\/a><\\/div>\",\"id\":\"BE3E1641501F40F06DA2BB6362C16C56\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-text-align\":\"center\",\"style-color\":\"#343A40\",\"style-font-size\":\"21px\",\"textContent\":\"Community\",\"id\":\"CE99D936BFB149AE96C55AF34975972C\",\"tagName\":\"h1\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ParagraphComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"5px\",\"style-margin-bottom\":\"5px\",\"style-margin-left\":\"15px\",\"style-margin-right\":\"15px\",\"textContent\":\"<p class=\\\"CDesc\\\" style=\\\"text-align: center;\\\">Join the Community Q&amp;A Site to ask questions, get answers, and help others in the Joget Community.<\\/p>\",\"id\":\"8AD6FCE6BBFB49627220DA6E4DE65FAB\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"id\":\"621CF7BC07664DD825852D222B919A16\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-tablet-flex\":\"0 0 33%\",\"style-tablet-max-width\":\"33%\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<div class=\\\"main-wrapper\\\">\\r\\n    <a href=\\\"https://community.joget.org/\\\" target=\\\"_blank\\\">\\r\\n        <i class=\\\"material-icons fas fa-book\\\"><\\/i>\\r\\n    <\\/a>\\r\\n<\\/div>\",\"id\":\"B20A5F30800944CB296076241494CF21\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-text-align\":\"center\",\"style-color\":\"#343A40\",\"style-font-size\":\"21px\",\"textContent\":\"Knowledge Base\",\"id\":\"808A20B26280467A8F31182BDBF78FFD\",\"tagName\":\"h1\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ParagraphComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"5px\",\"style-margin-bottom\":\"5px\",\"style-margin-left\":\"15px\",\"style-margin-right\":\"15px\",\"textContent\":\"<p class=\\\"CDesc\\\" style=\\\"text-align: center;\\\">Access additional tutorials, user and developer reference, samples and other documentation.<\\/p>\",\"id\":\"E9EAF4281A684F0ECA39C4B324978987\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"id\":\"BE41109C1CBB492E7474017E93D04CED\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-tablet-flex\":\"0 0 33%\",\"style-tablet-max-width\":\"33%\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<div class=\\\"main-wrapper\\\">\\n    <a href=\\\"https://github.com/jogetworkflow/jw-community\\\" target=\\\"_blank\\\">\\n    <i class=\\\"material-icons fas fa-code-branch\\\"><\\/i><\\/a><\\/div>\",\"id\":\"C616412A3FB44FA6A2C7B4949907656B\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-text-align\":\"center\",\"style-color\":\"#343A40\",\"style-font-size\":\"21px\",\"textContent\":\"Open Source\",\"id\":\"11B4B133289A468D5F1F6729E1E1F429\",\"tagName\":\"h1\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ParagraphComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"5px\",\"style-margin-bottom\":\"5px\",\"style-margin-left\":\"15px\",\"style-margin-right\":\"15px\",\"textContent\":\"<p class=\\\"CDesc\\\" style=\\\"text-align: center;\\\">Join the open source GitHub project to access and contribute to the source code.<\\/p>\",\"id\":\"E8239437D9154F869462544888F2B1B3\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"id\":\"761E8C1614324F8D0A868BA9A49DED5F\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-tablet-flex\":\"0 0 50%\",\"style-tablet-max-width\":\"50%\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<div class=\\\"main-wrapper\\\">\\r\\n    <a href=\\\"https://translate.joget.org/\\\" target=\\\"_blank\\\">\\r\\n\\t<i class=\\\"material-icons zmdi zmdi-translate\\\"><\\/i>\\r\\n\\t<\\/a>\\r\\n<\\/div>\",\"id\":\"DFE07F6328DC48E5F6AD604E5A3DF988\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-text-align\":\"center\",\"style-color\":\"#343A40\",\"style-font-size\":\"21px\",\"textContent\":\"Translations\",\"id\":\"97A86FEEDC2148C97BD9BA7DF81B3ACC\",\"tagName\":\"h1\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ParagraphComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"5px\",\"style-margin-bottom\":\"5px\",\"style-margin-left\":\"15px\",\"style-margin-right\":\"15px\",\"textContent\":\"<p style=\\\"text-align: center;\\\">Get translations for more than 20 languages, and contribute to the translation effort in Joget Translations.<\\/p>\",\"id\":\"C0C8B006494F4856EB0E78D247563076\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"id\":\"0B8F084D0ECB450CBB9309079DBA3F08\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-tablet-flex\":\"0 0 50%\",\"style-tablet-max-width\":\"50%\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"5px\",\"style-margin-bottom\":\"50px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"33%\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"33%\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"33%\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"50%\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"50%\"}],\"css-mobile-stack-columns\":\"true\",\"style-mobile-margin-bottom\":\"25px\",\"id\":\"5F7E5572513C4A9C7FFDCD2B12791585\",\"customId\":\"\"}},{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-color\":\"#343A40\",\"style-font-size\":\"1.35rem\",\"textContent\":\"Tutorial\",\"id\":\"CFEB99EA9DBF4F1E786EA7CD85A51C5C\",\"tagName\":\"h5\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.LabelComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#5B6DD0\",\"style-font-size\":\"15px\",\"textContent\":\"<div class=\\\"fs-5 text-dark fw-bold text-hover-primary text-dark lh-base text-gray-600\\\">\\n<div class=\\\"fs-5 text-dark fw-bold text-hover-primary text-dark lh-base text-gray-600\\\">Get a quick overview of Joget from the introductory and overview video tutorials or learn the in-depth workings of Joget by signing up for a free course at the Joget Academy.<\\/div>\\n<\\/div>\",\"id\":\"DF5069B71D404BC8374EC367B647B0DD\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HorizontalRuleComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"2rem\",\"style-border-style\":\"dashed\",\"id\":\"77827FCA7C044260398026C2CE008735\",\"style-border-color\":\"#E4E6EF\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-color\":\"#626970\",\"style-font-size\":\"1.1rem\",\"textContent\":\"<i class=\\\"zmdi zmdi-collection-video\\\"><\\/i> Watch Introduction Videos - Build Your First App\",\"id\":\"F368BE55138E43561E62C8719624804F\",\"tagName\":\"h5\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<iframe class=\\\"embed-responsive-item card-rounded h-275px w-100\\\" src=\\\"https://www.youtube.com/embed/BXeqa6qkvUM\\\" allowfullscreen=\\\"allowfullscreen\\\" data-gtm-yt-inspected-11=\\\"true\\\" id=\\\"video1\\\" data-gtm-yt-inspected-31871541_91=\\\"true\\\" title=\\\"Part 1 -Designing a New App\\\" style=\\\"height: 250px;border-radius: 10px;border: none;\\\"><\\/iframe>\",\"id\":\"4A83CCC18A77400FE1477577F2DC9D5D\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#7E8299\",\"style-font-size\":\"15px\",\"textContent\":\"Part 1 \\u2013 Designing a New App\",\"id\":\"D3E7B68AD8434AFA1162D0A1BD2F1E8C\",\"tagName\":\"h1\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"id\":\"9769BF334EAC41ADE04701449ECC1D4B\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"1C34EE3EBE9848CA9290D525732FFC66\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<iframe class=\\\"embed-responsive-item card-rounded h-275px w-100\\\" style=\\\"height: 250px;border-radius: 10px;border: none;\\\" src=\\\"https://www.youtube.com/embed/ANmu9ckiqvI\\\" allowfullscreen=\\\"allowfullscreen\\\" data-gtm-yt-inspected-11=\\\"true\\\" id=\\\"video2\\\" data-gtm-yt-inspected-31871541_91=\\\"true\\\" title=\\\"Part 2 - Designing a Form\\\"><\\/iframe>\\n\",\"id\":\"B75441D88B5B4DE0DA659877F679F871\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#7E8299\",\"style-font-size\":\"15px\",\"textContent\":\"Part 2 \\u2013 Designing a Form\",\"id\":\"6BC20E13D28541740A42F6801EA6B4C0\",\"tagName\":\"h1\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-padding-right\":\"0px\",\"style-mobile-padding-left\":\"0px\",\"id\":\"22036E48F4ED428A2C395D577F77A952\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"0C81E19C7D414DB77A1E910D5AEB3F0E\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"gutter\":\"5px\",\"columns\":[{\"style-max-width\":\"\"},{\"style-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"DBF708B6F3964ED6E1E3527F592BE463\"}},{\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<iframe class=\\\"embed-responsive-item card-rounded h-275px w-100\\\" style=\\\"height: 250px;border-radius: 10px;border: none;\\\" src=\\\"https://www.youtube.com/embed/Z_mlAfVS_oQ\\\" allowfullscreen=\\\"allowfullscreen\\\" data-gtm-yt-inspected-11=\\\"true\\\" id=\\\"video3\\\" data-gtm-yt-inspected-31871541_91=\\\"true\\\" title=\\\"Part 3 - Designing a List\\\"><\\/iframe>\",\"id\":\"3D7D506DFDFB477B1432541ACDADD45E\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#7E8299\",\"style-font-size\":\"15px\",\"textContent\":\"Part 3 \\u2013 Designing a List\",\"id\":\"CEF6F57620B24C09FA742C1958AB45F8\",\"tagName\":\"h1\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-padding-right\":\"0px\",\"style-mobile-padding-left\":\"0px\",\"id\":\"604F03F4688F4730C6A45B78891BE099\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"A9E8AB80485642DF4FD5BFEE67659E62\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<iframe class=\\\"embed-responsive-item card-rounded h-275px w-100\\\" style=\\\"height: 250px;border-radius: 10px;border: none;\\\" src=\\\"https://www.youtube.com/embed/FxD6olBFwhc\\\" allowfullscreen=\\\"allowfullscreen\\\" data-gtm-yt-inspected-11=\\\"true\\\" id=\\\"video4\\\" data-gtm-yt-inspected-31871541_91=\\\"true\\\" title=\\\"Part 4 - Designing a UI\\\"><\\/iframe>\",\"id\":\"38DEE40749A24DEF344BD00C6D0BA8C7\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#7E8299\",\"style-font-size\":\"15px\",\"textContent\":\"Part 4 \\u2013 Designing a Userview\",\"id\":\"1B8606420FA14065796051FF56C37FD4\",\"tagName\":\"h1\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-padding-right\":\"0px\",\"style-mobile-padding-left\":\"0px\",\"id\":\"91ABA03A45E14746D71E5568CB4DFB69\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"48B7C16AF0F446B7B16218678EFB0FF9\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"gutter\":\"5px\",\"columns\":[{\"style-max-width\":\"\"},{\"style-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"2E35DA7D3E3F41BE9FFFE6B3095E6095\"}},{\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.HtmlCodeComponent\",\"properties\":{\"attr-data-events-listening\":[],\"textContent\":\"<iframe class=\\\"embed-responsive-item card-rounded h-275px w-100\\\" style=\\\"height: 250px;border-radius: 10px;border: none;\\\" src=\\\"https://www.youtube.com/embed/nzM_UtLzutA\\\" allowfullscreen=\\\"allowfullscreen\\\" data-gtm-yt-inspected-11=\\\"true\\\" id=\\\"video5\\\" data-gtm-yt-inspected-31871541_91=\\\"true\\\" title=\\\"Part 5 - Designing a Process\\\"><\\/iframe>\\r\\n\",\"id\":\"898D066A4D744E109E7A08C98761EFFE\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#7E8299\",\"style-font-size\":\"15px\",\"textContent\":\"Part 5 \\u2013 Designing a Process\",\"id\":\"D497C8FCFBDB46D19559CACA1167EDFC\",\"tagName\":\"h1\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-padding-right\":\"0px\",\"style-mobile-padding-left\":\"0px\",\"id\":\"8BE14AADA62E46422F552810778B9BBE\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"262F4A346DAF444DBBC40BB513EFD3E6\"}},{\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"EE3EA7E194C54CBAD7411C0A92223D90\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"gutter\":\"5px\",\"columns\":[{\"style-max-width\":\"\"},{\"style-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"FE6369799C864C319066FB1AAFAE1FA3\"}},{\"elements\":[{\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.ImageComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-margin-left\":\"0px\",\"attr-width\":\"100%\",\"attr-height\":\"100%\",\"attr-src\":\"/jw/images/appCenterAcademy.png\",\"id\":\"C9F5838828F049A21F098E81DC4A009A\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"id\":\"A99D175BE9164067C28453894C6E7C53\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-flex\":\"0 0 40%\",\"id\":\"9C75B6DE0177452D50EB1BB613D33D64\",\"style-max-width\":\"40%\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-color\":\"#343A40\",\"style-font-size\":\"22px\",\"textContent\":\"<span class=\\\"vc_icon_element-icon fa fa-graduation-cap\\\"><\\/span> Learn with Joget Academy\",\"id\":\"0EF2DB5B37ED49EDA9D6EA6AC824833D\",\"tagName\":\"h5\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.LabelComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"style-color\":\"#5B6DD0\",\"style-font-size\":\"15px\",\"textContent\":\"<div class=\\\"fs-5 text-dark fw-bold text-hover-primary text-dark lh-base text-gray-600\\\">\\n<div class=\\\"fs-5 text-dark fw-bold text-hover-primary text-dark lh-base text-gray-600\\\"><span>Visit the Joget Academy and sign up for an online learning course.<\\/span><\\/div>\\n<\\/div>\",\"id\":\"93868F769F3D4CB314B88907E74A5B3C\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.Link\",\"properties\":{\"style-font-weight\":\"500\",\"userviewCacheDuration\":\"20\",\"userviewCacheScope\":\"\",\"style-color\":\"#009EF7\",\"style-font-size\":\"15px\",\"id\":\"32A9F7E3F580445877435EDE1CFF395B\",\"label\":\"Start Now\",\"type\":\"pagecomponent\",\"style-display\":\"block\",\"url\":\"https://academy.joget.org/lms/courses\",\"target\":\"blank\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-margin-top\":\"0px\",\"style-margin-top\":\"10%\",\"id\":\"115791FD766249624E4777C255B1BD31\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"D1F302CD595B4B4D50B779CCE33C9F14\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"5px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"40%\",\"style-tablet-max-width\":\"\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"3B68A17E8CC04B5EFBCC70FA8D8D586D\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-padding-right\":\"10px\",\"style-margin-top\":\"20px\",\"style-padding-bottom\":\"10px\",\"style-border-style\":\"dashed\",\"style-margin-bottom\":\"20px\",\"id\":\"AED245A047CB49A6405198CF2B1AFCE7\",\"style-border-color\":\"#E4E6EF\",\"style-padding-top\":\"10px\",\"customId\":\"\",\"style-border-radius\":\"10px\",\"style-padding-left\":\"10px\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-padding-bottom\":\"0px\",\"style-mobile-padding-right\":\"0px\",\"style-mobile-padding-left\":\"0px\",\"id\":\"DFE52E6DC6594F5D7A1208E35DFCCFBA\",\"customId\":\"\",\"style-mobile-padding-top\":\"0px\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"id\":\"1525FB813CA84CC70A23890EEDEE9E54\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"id\":\"E28DDE2DA6A24615F1F18665B87591C1\",\"style-padding-top\":\"25px\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-background-color\":\"#FFFFFF\",\"style-padding-bottom\":\"0px\",\"style-mobile-margin-left\":\"0%\",\"id\":\"CF9BE1662CEA47717282164F140A8E5A\",\"style-padding-top\":\"0px\",\"customId\":\"\",\"style-tablet-margin-right\":\"0%\",\"style-tablet-margin-left\":\"0%\",\"style-border-radius\":\"10px\",\"style-mobile-margin-right\":\"0%\"}},{\"className\":\"menu-component\"}],\"className\":\"org.joget.apps.userview.model.UserviewPage\",\"properties\":{\"id\":\"up-0497CD0F62BD44DEEAF1294D58166A1A\"}}',NULL),
('appcenter',1,'up-4E2A517C3275429806518BAA8D29AB1F','Administration','INTERNAL_USERVIEW_PAGE','2023-11-01 13:39:15','2023-11-01 13:39:15','{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-color\":\"#3B4348\",\"textContent\":\"<i class=\\\"fas fa-digital-tachograph\\\"><\\/i> Administration\",\"id\":\"61316B62895446C4E93BFE40ED935E01\",\"tagName\":\"h3\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"25px\",\"style-color\":\"#000000\",\"textContent\":\"<i class=\\\"fas fa-tachometer-alt\\\"><\\/i> Monitor\",\"id\":\"AE25821C2F234BABD548048DEC4B2A33\",\"tagName\":\"h5\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<!--{sampleWidth: \'100%\', tags: \'light-bg grid\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}};\\n            background-color:{{bgColor}}; \\n            background-image:url(\'{{bgImg}}\');\\\">\\n    <div class=\\\"card-header\\\" \\n         style=\\\"background-color:transparent;\\\">\\n        <div class=\\\"row\\\">\\n            <div class=\\\"header-title col-8\\\" style=\\\"color:#000;font-weight:600;\\\">\\n                {{header||STATUS}}\\n            <\\/div>\\n            <div class=\\\"header-tag col-4 text-right pl-0\\\">\\n                <span class=\\\"badge\\\" \\n                      style=\\\"background-color:{{tagColor||#1ab394}}; \\n                             color:#fff;\\\">\\n                    {{headerTag||Daily}}\\n                <\\/span>\\n                {{reload}}\\n            <\\/div>\\n        <\\/div>    \\n    <\\/div>  \\n    <div class=\\\"card-body row d-flex flex-wrap pb-0\\\">\\n        {{repeat||[{value: \\\"12\\\", title: \\\"Messages\\\", color: \\\"#ED5565\\\"},{value: \\\"28\\\", title: \\\"Posts\\\", color: \\\"#1ab394\\\"},{value: \\\"15\\\", title: \\\"Comments\\\", color: \\\"#23c6c8\\\"},{value: \\\"20\\\", title: \\\"News\\\", color: \\\"#1c84c6\\\"}]}}\\n            <div class=\\\"col-4\\\"\\n                 style=\\\"margin-bottom: 15px\\\">\\n                <a href=\\\"{{repeat.link}}\\\" \\n                   class=\\\"btn\\\"\\n                   style=\\\"background-color:{{repeat.color}};\\n                          color:#ffffff;\\n                          padding: 5px 10px;\\n                          margin-right:20px;\\n                          font-weight: 600;\\n                          opacity:1;\\\">\\n                   {{repeat.value}}\\n                <\\/a>  \\n                {{repeat.title}}\\n            <\\/div>\\n        {{repeat}}\\n    <\\/div>\\n<\\/div>\\n<!---->\",\"color\":\"#348fe2\",\"headerTag\":\"Last 4 hours\",\"tagColor\":\"#0BAB73\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/monitor/apm\');\",\"bgImg\":\"\",\"title\":\"TOTAL VISITS\",\"customId\":\"\",\"reload\":\"\",\"bgColor\":\"\",\"repeat\":[{\"color\":\"#348FE2\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/monitor/apm\');\",\"title\":\"Total Transactions\",\"value\":\"#stats.transactionCount#\"},{\"color\":\"#ECBB0D\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/monitor/apm\');\",\"title\":\"Total Slow Traces\",\"value\":\"#stats.slowTrace#\"},{\"color\":\"#E2344F\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/monitor/apm\');\",\"title\":\"Total Error Count\",\"value\":\"#stats.errorCount#\"}],\"header\":\"Performance Data\",\"id\":\"9A46CFB43B564FB0834DBD24B25F5E15\",\"value\":\"3,291,922\",\"height\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"5px\",\"style-margin-bottom\":\"15px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"BCC203BF2F024AFE154A9E9D1A6FF2E7\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<!--{sampleWidth: \'350px\', tags: \'light-bg header tag complex\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}};\\n            background-color:{{bgColor}}; \\n            background-image:url(\'{{bgImg}}\');\\\">\\n    <div class=\\\"card-header\\\" \\n         style=\\\"background-color:transparent;\\\">\\n        <div class=\\\"row\\\">\\n            <div class=\\\"header-title col-8\\\" style=\\\"color:#000\\\">\\n                {{header||VISITORS}}\\n            <\\/div>\\n            <div class=\\\"header-tag col-4 text-right pl-0\\\">\\n                <span class=\\\"badge\\\" \\n                      style=\\\"background-color:{{tagColor||#1ab394}}; \\n                      color:#fff;\\\">\\n                    {{headerTag||Daily}}\\n                <\\/span>\\n                {{reload}}\\n            <\\/div>\\n        <\\/div>    \\n    <\\/div>\\n    <div class=\\\"card-body row\\\">\\n        {{repeat||[{value: \\\"406,42\\\", subvalue: \\\"33% <i class=\\\\\\\"fa fa-level-up\\\\\\\"><\\/i>\\\", subtitle: \\\"Rapid pace\\\", color: \\\"#1ab394\\\"},{value: \\\"206,12\\\", subvalue: \\\"10% <i class=\\\\\\\"fa fa-level-up\\\\\\\"><\\/i>\\\", subtitle: \\\"Slow pace\\\", color: \\\"#8753de\\\"}]}}\\n            <div class=\\\"stats-info col-6\\\">\\n                <p style=\\\"font-size: 24px;\\n                color: rgba(0,0,0,.8);\\n                          margin-bottom: 0;\\\">\\n                    {{repeat.value}}\\n                <\\/p>\\n                <p style=\\\"color:{{repeat.color}}\\\">\\n                    <span style=\\\"font-weight:bold;\\\">\\n                        {{repeat.subvalue}}\\n                    <\\/span> \\n                    <span style=\\\"font-size:100%;\\\">\\n                        {{repeat.subtitle}}\\n                    <\\/span>\\n                <\\/p>\\n            <\\/div>\\n        {{repeat}}\\n    <\\/div>\\n<\\/div>\\n<!---->\",\"color\":\"#348fe2\",\"headerTag\":\"Latest\",\"tagColor\":\"#0BAB73\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/monitor/running\');\",\"bgImg\":\"\",\"title\":\"TOTAL VISITS\",\"customId\":\"\",\"style-font-weight\":\"500\",\"reload\":\"\",\"bgColor\":\"\",\"style-margin-bottom\":\"15px\",\"repeat\":[{\"color\":\"#119C5D\",\"subvalue\":\"\",\"subtitle\":\"Running Process\",\"value\":\"#stats.runningProcess#\"},{\"color\":\"#B2B2B2\",\"subvalue\":\"\",\"subtitle\":\"Completed Process\",\"value\":\"#stats.completedProcess#\"}],\"header\":\"All Process\",\"id\":\"E213F291CC434CD89583F4D4B616D8EB\",\"value\":\"3,291,922\",\"height\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<!--{sampleWidth: \'250px\', tags: \'dark-bg icon\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}}; \\n            background-color:{{color||#ff5b57}};\\n            background-image:url(\'{{bgImg}}\');  \\n            color:#fff;\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"card-body text-center\\\">\\n        <div class=\\\"stats-icon\\\" \\n             style=\\\"font-size: 42px; \\n                    color: #fff; \\n                    line-height:0;\\\">\\n            {{icon||<i class=\\\"fas fa-globe\\\"><\\/i>}}\\n        <\\/div>\\n        <div class=\\\"stats-info\\\">\\n            <p style=\\\"font-size: 18px; \\n                      font-weight: 600; \\n                      margin-bottom: 0;\\\">\\n                {{value||3,291,922}}\\n            <\\/p>\\n            <h4 style=\\\"font-size: 12px; \\n                       font-weight:bold; \\n                       margin: 5px 0;\\\">\\n                {{title||VISITS}}\\n            <\\/h4>\\n            {{description||<div style=\\\"font-size:80%;\\\">daily<\\/div>}}\\n        <\\/div>\\n    <\\/div>\\n<\\/div>\\n<!---->\",\"color\":\"#009EF7\",\"icon\":\"<i class=\\\"fas fa-scroll\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/monitor/slogs\');\",\"style-height\":\"100%\",\"description\":\"Click to View Logs\",\"bgImg\":\"\",\"title\":\"System Log\",\"customId\":\"\",\"reload\":\"\",\"id\":\"AA855960088C49D046A9B063C87AE4FC\",\"value\":\"<br>\",\"height\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<!--{sampleWidth: \'250px\', tags: \'dark-bg icon\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}}; \\n            background-color:{{color||#ff5b57}};\\n            background-image:url(\'{{bgImg}}\');  \\n            color:#fff;\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"card-body text-center\\\">\\n        <div class=\\\"stats-icon\\\" \\n             style=\\\"font-size: 42px; \\n                    color: #fff; \\n                    line-height:0;\\\">\\n            {{icon||<i class=\\\"fas fa-globe\\\"><\\/i>}}\\n        <\\/div>\\n        <div class=\\\"stats-info\\\">\\n            <p style=\\\"font-size: 18px; \\n                      font-weight: 600; \\n                      margin-bottom: 0;\\\">\\n                {{value||3,291,922}}\\n            <\\/p>\\n            <h4 style=\\\"font-size: 12px; \\n                       font-weight:bold; \\n                       margin: 5px 0;\\\">\\n                {{title||VISITS}}\\n            <\\/h4>\\n            {{description||<div style=\\\"font-size:80%;\\\">daily<\\/div>}}\\n        <\\/div>\\n    <\\/div>\\n<\\/div>\\n<!---->\",\"color\":\"#080655\",\"icon\":\"<i class=\\\"fas fa-server\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/monitor/apm\');\",\"style-height\":\"100%\",\"description\":\"Cluster Nodes Found\",\"bgImg\":\"\",\"title\":\"#stats.nodeCount#\",\"customId\":\"\",\"reload\":\"\",\"id\":\"13C1564E98D84E1BF6C0F65A79423E8C\",\"value\":\"<br>\",\"height\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"gutter\":\"5px\",\"columns\":[{\"style-max-width\":\"\"},{\"style-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"794DA766B69D466F3C25A3F591C2C3FF\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-flex\":\"0 0 40%\",\"style-max-width\":\"40%\"}},{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<!--{sampleWidth: \'350px\', tags: \'light-bg header tag progress\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}};\\n            background-color:{{bgColor}}; \\n            background-image:url(\'{{bgImg}}\');\\\">\\n    <div class=\\\"card-header\\\" \\n         style=\\\"background-color:transparent;\\\">\\n        <div class=\\\"row\\\">\\n            <div class=\\\"header-title col-8\\\" style=\\\"color:#000;font-weight:600;\\\">\\n                {{header||VISITORS}}\\n            <\\/div>\\n            <div class=\\\"header-tag col-4 text-right pl-0\\\">\\n                <span class=\\\"badge\\\" \\n                      style=\\\"background-color:{{tagColor||#1ab394}}; \\n                             color:#fff;\\\">\\n                    {{headerTag||Daily}}\\n                <\\/span>\\n                {{reload}}\\n            <\\/div>\\n        <\\/div>    \\n    <\\/div> \\n    <div class=\\\"pt-3 pb-2\\\">\\n        {{repeat||[{value: \\\"10/200 GB\\\", title: \\\"Memory\\\", color: \\\"#ED5565\\\", \'progressbar-value\' : \'60\'},{value: \\\"20 GB\\\", title: \\\"Bandwidth\\\", color: \\\"#23c6c8\\\", \'progressbar-value\' : \'50\'},{value: \\\"73%\\\", title: \\\"Activity\\\", color: \\\"#1c84c6\\\", \'progressbar-value\' : \'73\'}]}}\\n            <div class=\\\"px-3 pb-2\\\">\\n                <div class=\\\"mb-1\\\">\\n                    <span>\\n                        {{repeat.title}}\\n                    <\\/span>\\n                    <small class=\\\"float-right\\\">\\n                        {{repeat.value}}\\n                    <\\/small>\\n                <\\/div>\\n                <div class=\\\"progress progress-small\\\">\\n                    <div class=\\\"progress-bar\\\" \\n                         style=\\\"width: {{repeat.progressbar-value}}%; \\n                                transition: width .6s ease; \\n                                background-color:{{repeat.color}};\\\">\\n                    <\\/div>\\n                <\\/div>\\n            <\\/div>\\n        {{repeat}}\\n    <\\/div>\\n<\\/div>\\n<!---->\",\"color\":\"#348fe2\",\"headerTag\":\"Latest\",\"tagColor\":\"#0BAB73\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/monitor/governance\');\",\"style-height\":\"98%\",\"bgImg\":\"\",\"title\":\"TOTAL VISITS\",\"customId\":\"\",\"reload\":\"\",\"bgColor\":\"\",\"repeat\":[{\"color\":\"#A9A9A9\",\"progressbar-value\":\"#stats.ResponseTimesCheck#\",\"title\":\"Response Times\",\"value\":\"#stats.ResponseTimesCheck#  %\"},{\"color\":\"#A9A9A9\",\"progressbar-value\":\"#stats.MemoryUsageCheck#\",\"title\":\"Memory Usage\",\"value\":\"#stats.MemoryUsageCheck#  %\"},{\"color\":\"#A9A9A9\",\"progressbar-value\":\"#stats.CpuUsageCheck#\",\"title\":\"CPU Usage\",\"value\":\"#stats.CpuUsageCheck#  %\"},{\"color\":\"#A9A9A9\",\"progressbar-value\":\"#stats.DbConnectionsCheck#\",\"title\":\"DB Connections\",\"value\":\"#stats.DbConnectionsCheck#  %\"},{\"color\":\"#A9A9A9\",\"progressbar-value\":\"#stats.ErrorRateCheck#\",\"title\":\"Error Rate\",\"value\":\"#stats.ErrorRateCheck#  %\"}],\"header\":\"Governance Health Check (%)\",\"id\":\"FF13DE7F3F4249C90D7FDA32EAE9A358\",\"value\":\"3,291,922\",\"height\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-flex\":\"0 0 60%\",\"style-max-width\":\"60%\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"5px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"40%\",\"style-tablet-max-width\":\"\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"60%\",\"style-tablet-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"CD6B604C45F24C869CC610CBCED7C1CA\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"25px\",\"style-color\":\"#000000\",\"textContent\":\"<i class=\\\"fas fa-th\\\"><\\/i> Apps\",\"id\":\"2918970A648D440752EA19A4ADF0E9C8\",\"tagName\":\"h5\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"linkLabel\":\"View All Apps\",\"template\":\"<!--{sampleWidth: \'250px\', tags: \'dark-bg link pie\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}}; \\n            background-color:{{color||#8753de}}; \\n            background-image:url(\'{{bgImg}}\'); \\n            color:#fff;\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"card-body row\\\">\\n        <div class=\\\"stats-icon col-4 pl-0 d-flex align-items-center\\\">\\n            <div id=\\\"chart-{{id}}\\\"\\n                 style=\\\"color:#000\\\" \\n                 data-chart-type=\\\"pie\\\" \\n                 data-chart-custom=\\\"{{chartConfig}}\\\" \\n                 data-chart-colors=\\\"{{chartColors}}\\\"  \\n                 data-chart-labels=\\\"{{chartLabels||Item 1,Item 2,Item 3,Item 4}}\\\"\\n                 data-chart-values=\\\"{{chartData||[25, 66, 41, 89]}}\\\">\\n            <\\/div>\\n        <\\/div>\\n        <div class=\\\"stats-info col-8 text-right\\\">\\n            <p style=\\\"font-size: 24px; \\n                      font-weight: 600; \\n                      color: #000;\\n                      margin-bottom: 0;\\\">\\n                {{value||3,291,922}}\\n            <\\/p>\\n            <h4 style=\\\"font-size: 15px;\\n                       margin: 5px 0;\\n                       color: rgba(0,0,0,.6);\\\">\\n                {{title||TOTAL VISITORS}}\\n            <\\/h4>\\n        <\\/div>\\n    <\\/div>\\n    <div class=\\\"card-footer text-right\\\" \\n                style=\\\"padding:5px 15px; \\n                       background:rgba(0,0,0,.6); \\n                       color:rgba(255,255,255,0.6) !important;\\\">\\n        <a style=\\\"color: inherit; \\n                  font-size:12px;\\\" \\n           href=\\\"{{link||#}}\\\">\\n            {{linkLabel||View Detail}} \\n            <i class=\\\"fas fa-arrow-alt-circle-right\\\"><\\/i>\\n        <\\/a>\\n    <\\/div>\\n    <script>\\n        $(function(){\\n            loadScript(\\\"{{contextPath}}/js/apexcharts-sparkline.js\\\", function(){\\n                $(\\\"#chart-{{id}}\\\").acSparkline();\\n            });\\n        });\\n    <\\/script>\\n<\\/div>\\n<!---->\",\"color\":\"#FFFFFF\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/desktop/apps\');\",\"bgImg\":\"\",\"title\":\"Total Apps\",\"customId\":\"1\",\"chartData\":\"{data: [#stats.publishedAppCount#,#stats.unpublishedAppCount#]}\",\"chartConfig\":\"\",\"reload\":\"\",\"id\":\"8FF3FFF7314E4E46779C1F72CB4DA113\",\"value\":\"#stats.app.total#\",\"chartLabels\":\"Publish App, UnPublished App\",\"chartColors\":\"#50cd89,#ffc700\",\"height\":\"100%\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<!--{sampleWidth: \'250px\', tags: \'light-bg pie\'}-->\\n<div class=\\\"card flex-row align-items-center align-items-stretch\\\" \\n     style=\\\"height:{{height}};\\n            background-color:{{bgColor}}; \\n            background-image:url(\'{{bgImg}}\');\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"stats-icon col-4 py-3 pr-0 d-flex align-items-center\\\">\\n        <div id=\\\"chart-{{id}}\\\" \\n             data-chart-type=\\\"pie\\\" \\n             data-chart-custom=\\\"{{chartConfig}}\\\" \\n             data-chart-config=\\\"{theme:{monochrome: {enabled: true,shadeTo: \'light\', color: \'{{color||#ff5b57}}\', shadeIntensity: 0.6}}}\\\"  \\n             data-chart-labels=\\\"{{chartLabels||Item 1,Item 2,Item 3,Item 4}}\\\"\\n             data-chart-values=\\\"{{chartData||[25, 66, 41, 89]}}\\\">\\n        <\\/div>\\n    <\\/div>\\n    <div class=\\\"stats-info py-3 col-8 text-right\\\">\\n        <p style=\\\"font-size: 24px; \\n                  color: rgba(0,0,0,.8);\\n                  font-weight: 600; \\n                  margin-bottom: 10px;\\\">\\n            {{value||3,291,922}}\\n        <\\/p>\\n        <h4 style=\\\"font-size: 15px;\\n                   color: rgba(0,0,0,.6);\\n                   margin: 5px 0;\\\">\\n            {{title||TOTAL VISITORS}}\\n        <\\/h4>\\n    <\\/div>\\n    <script>\\n        $(function(){\\n            loadScript(\\\"{{contextPath}}/js/apexcharts-sparkline.js\\\", function(){\\n                $(\\\"#chart-{{id}}\\\").acSparkline();\\n            });\\n        });\\n    <\\/script>\\n<\\/div>\\n<!---->\",\"color\":\"#3F84F4\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"\",\"bgImg\":\"\",\"title\":\"Total Forms\",\"customId\":\"\",\"chartData\":\"{data:[#stats.form.countPerApp#]}\",\"chartConfig\":\"\",\"reload\":\"\",\"bgColor\":\"\",\"id\":\"642ED203501E45361102BB1E99CC80B9\",\"value\":\"#stats.form.total#\",\"chartLabels\":\"#stats.appList#\",\"chartColors\":\"\",\"height\":\"100%\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<!--{sampleWidth: \'250px\', tags: \'light-bg pie\'}-->\\n<div class=\\\"card flex-row align-items-center align-items-stretch\\\" \\n     style=\\\"height:{{height}};\\n            background-color:{{bgColor}}; \\n            background-image:url(\'{{bgImg}}\');\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"stats-icon col-4 py-3 pr-0 d-flex align-items-center\\\">\\n        <div id=\\\"chart-{{id}}\\\" \\n             data-chart-type=\\\"pie\\\" \\n             data-chart-custom=\\\"{{chartConfig}}\\\" \\n             data-chart-config=\\\"{theme:{monochrome: {enabled: true,shadeTo: \'light\', color: \'{{color||#ff5b57}}\', shadeIntensity: 0.6}}}\\\"  \\n             data-chart-labels=\\\"{{chartLabels||Item 1,Item 2,Item 3,Item 4}}\\\"\\n             data-chart-values=\\\"{{chartData||[25, 66, 41, 89]}}\\\">\\n        <\\/div>\\n    <\\/div>\\n    <div class=\\\"stats-info py-3 col-8 text-right\\\">\\n        <p style=\\\"font-size: 24px;\\n                    color: rgba(0,0,0,.8);\\n                  font-weight: 600; \\n                  margin-bottom: 10px;\\\">\\n            {{value||3,291,922}}\\n        <\\/p>\\n        <h4 style=\\\"font-size: 15px;\\n                   color: rgba(0,0,0,.6);\\n                   margin: 5px 0;\\\">\\n            {{title||TOTAL VISITORS}}\\n        <\\/h4>\\n    <\\/div>\\n    <script>\\n        $(function(){\\n            loadScript(\\\"{{contextPath}}/js/apexcharts-sparkline.js\\\", function(){\\n                $(\\\"#chart-{{id}}\\\").acSparkline();\\n            });\\n        });\\n    <\\/script>\\n<\\/div>\\n<!---->\",\"color\":\"#6638B6\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"\",\"bgImg\":\"\",\"title\":\"Total Lists\",\"customId\":\"\",\"chartData\":\"{data:[#stats.list.countPerApp#]}\",\"chartConfig\":\"\",\"reload\":\"\",\"bgColor\":\"\",\"id\":\"7C84A4735ACE41910D97582AE29F05D6\",\"value\":\"#stats.list.total#\",\"chartLabels\":\"#stats.appList#\",\"chartColors\":\"\",\"height\":\"100%\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"template\":\"<!--{sampleWidth: \'250px\', tags: \'light-bg pie\'}-->\\n<div class=\\\"card flex-row align-items-center align-items-stretch\\\" \\n     style=\\\"height:{{height}};\\n            background-color:{{bgColor}}; \\n            background-image:url(\'{{bgImg}}\');\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"stats-icon col-4 py-3 pr-0 d-flex align-items-center\\\">\\n        <div id=\\\"chart-{{id}}\\\" \\n             data-chart-type=\\\"pie\\\" \\n             data-chart-custom=\\\"{{chartConfig}}\\\" \\n             data-chart-config=\\\"{theme:{monochrome: {enabled: true,shadeTo: \'light\', color: \'{{color||#ff5b57}}\', shadeIntensity: 0.6}}}\\\"  \\n             data-chart-labels=\\\"{{chartLabels||Item 1,Item 2,Item 3,Item 4}}\\\"\\n             data-chart-values=\\\"{{chartData||[25, 66, 41, 89]}}\\\">\\n        <\\/div>\\n    <\\/div>\\n    <div class=\\\"stats-info py-3 col-8 text-right\\\">\\n        <p style=\\\"font-size: 24px; \\n        color: rgba(0,0,0,.8);\\n                  font-weight: 600; \\n                  margin-bottom: 10px;\\\">\\n            {{value||3,291,922}}\\n        <\\/p>\\n        <h4 style=\\\"font-size: 15px;\\n                   color: rgba(0,0,0,.6);\\n                   margin: 5px 0;\\\">\\n            {{title||TOTAL VISITORS}}\\n        <\\/h4>\\n    <\\/div>\\n    <script>\\n        $(function(){\\n            loadScript(\\\"{{contextPath}}/js/apexcharts-sparkline.js\\\", function(){\\n                $(\\\"#chart-{{id}}\\\").acSparkline();\\n            });\\n        });\\n    <\\/script>\\n<\\/div>\\n<!---->\",\"color\":\"#FFC107\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"\",\"bgImg\":\"\",\"title\":\"Total UIs\",\"customId\":\"\",\"chartData\":\"{data:[#stats.UI.countPerApp#]}\",\"chartConfig\":\"\",\"reload\":\"\",\"bgColor\":\"\",\"id\":\"24667F714AE04B6CF1825FD92A25A3C7\",\"value\":\"#stats.UI.total#\",\"chartLabels\":\"#stats.appList#\",\"chartColors\":\"\",\"height\":\"100%\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"5px\",\"style-margin-bottom\":\"15px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"0AAA340B0C694E6285CA9FC83B9CB308\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"25px\",\"style-color\":\"#000000\",\"textContent\":\"<i class=\\\"fas fa-users\\\"><\\/i> Users\",\"id\":\"3E8D0FCDBF9D44423CDE018F0F2F2E7C\",\"tagName\":\"h5\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"linkLabel\":\"View All Users\",\"template\":\"<!--{sampleWidth: \'250px\', tags: \'dark-bg link donut\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}}; \\n            background-color:{{color||#8753de}}; \\n            background-image:url(\'{{bgImg}}\'); \\n            color:#fff;\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"card-body row\\\">\\n        <div class=\\\"stats-info col-8\\\">\\n            <h4 style=\\\"font-size: 15px;\\n                       margin: 5px 0;\\n                        color: rgba(0,0,0,.6);\\\">\\n                {{title||TOTAL VISITORS}}\\n            <\\/h4>\\n            <p style=\\\"font-size: 24px; \\n                      font-weight: 600; \\n                      color:#000;\\n                      margin-bottom: 0;\\\">\\n                {{value||3,291,922}}\\n            <\\/p>\\n        <\\/div>\\n        <div class=\\\"stats-icon col-4 pl-0 d-flex align-items-center\\\"\\n             style=\\\"color:#000\\\">\\n            <div id=\\\"chart-{{id}}\\\" \\n                 data-chart-type=\\\"donut\\\" \\n                 data-chart-custom=\\\"{{chartConfig}}\\\" \\n                 data-chart-colors=\\\"{{chartColors}}\\\"   \\n                 data-chart-config=\\\"{stroke:{width: 1}}\\\"  \\n                 data-chart-labels=\\\"{{chartLabels||Item 1,Item 2,Item 3,Item 4}}\\\"\\n                 data-chart-values=\\\"{{chartData||[25, 66, 41, 89]}}\\\">\\n            <\\/div>\\n        <\\/div>\\n    <\\/div>\\n    <div class=\\\"card-footer text-right\\\" \\n                style=\\\"padding:5px 15px; \\n                       background:rgba(0,0,0,.6); \\n                       color:rgba(255,255,255,0.6) !important;\\\">\\n        <a style=\\\"color: inherit; \\n                  font-size:12px;\\\" \\n           href=\\\"{{link||#}}\\\">\\n            {{linkLabel||View Detail}} \\n            <i class=\\\"fas fa-arrow-alt-circle-right\\\"><\\/i>\\n        <\\/a>\\n    <\\/div>\\n    <script>\\n        $(function(){\\n            loadScript(\\\"{{contextPath}}/js/apexcharts-sparkline.js\\\", function(){\\n                $(\\\"#chart-{{id}}\\\").acSparkline();\\n            });\\n        });\\n    <\\/script>\\n<\\/div>\\n<!---->\",\"color\":\"#FFFFFF\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/directory/users\');\",\"bgImg\":\"\",\"title\":\"Total Users\",\"customId\":\"\",\"chartData\":\"{data: [#stats.user.active#,#stats.user.inactive#]}\",\"chartConfig\":\"\",\"reload\":\"\",\"id\":\"8FD4AAB012DC44587255D037E3C1D1DC\",\"value\":\"#stats.user.total#\",\"chartLabels\":\"Active User, Inactive User\",\"chartColors\":\"#7239ea,#009EF7\",\"height\":\"100%\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"linkLabel\":\"View All Groups\",\"template\":\"<!--{sampleWidth: \'250px\', tags: \'dark-bg link bar\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}}; \\n            background-color:{{color||#8753de}}; \\n            background-image:url(\'{{bgImg}}\'); \\n            color:#fff;\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"card-body row\\\">\\n        <div class=\\\"stats-info col-8\\\">\\n            <h4 style=\\\"font-size: 15px;\\n                       margin: 5px 0;\\n                       color: rgba(255,255,255,.7);\\\">\\n                {{title||TOTAL VISITORS}}\\n            <\\/h4>\\n            <p style=\\\"font-size: 24px; \\n                      font-weight: 600; \\n                      margin-bottom: 0;\\\">\\n                {{value||3,291,922}}\\n            <\\/p>\\n        <\\/div>\\n        <div class=\\\"stats-icon col-4 pl-0 d-flex align-items-center\\\">\\n            <div id=\\\"chart-{{id}}\\\"\\n                 style=\\\"color:#000\\\" \\n                 data-chart-type=\\\"bar\\\" \\n                 data-chart-custom=\\\"{{chartConfig}}\\\" \\n                 data-chart-color=\\\"#fff\\\"\\n                 data-chart-labels=\\\"{{chartLabels||Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec}}\\\"\\n                 data-chart-values=\\\"{{chartData||[{data:[25, 66, 41, 89, 63, 25, 44, 12, 36, 9, 54, 33]}]}}\\\">\\n            <\\/div>\\n        <\\/div>\\n    <\\/div>\\n    <div class=\\\"card-footer text-right\\\" \\n                style=\\\"padding:5px 15px; \\n                       background:rgba(0,0,0,.6); \\n                       color:rgba(255,255,255,0.6) !important;\\\">\\n        <a style=\\\"color: inherit; \\n                  font-size:12px;\\\" \\n           href=\\\"{{link||#}}\\\">\\n            {{linkLabel||View Detail}} \\n            <i class=\\\"fas fa-arrow-alt-circle-right\\\"><\\/i>\\n        <\\/a>\\n    <\\/div>\\n    <script>\\n        $(function(){\\n            loadScript(\\\"{{contextPath}}/js/apexcharts-sparkline.js\\\", function(){\\n                $(\\\"#chart-{{id}}\\\").acSparkline();\\n            });\\n        });\\n    <\\/script>\\n<\\/div>\\n<!---->\",\"color\":\"#1D325E\",\"icon\":\"<i class=\\\"fas fa-globe\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/directory/groups\');\",\"style-height\":\"100%\",\"bgImg\":\"\",\"title\":\"Total Groups\",\"customId\":\"\",\"chartData\":\"[{data:[#stats.group.countPerGroup#]}]\",\"chartConfig\":\"\",\"reload\":\"\",\"id\":\"4BBD819578914F8AFA97A55420FB2D36\",\"value\":\"#stats.group.total#\",\"chartLabels\":\"#stats.groupList#\",\"height\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},{\"elements\":[],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"5px\",\"style-margin-bottom\":\"15px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"},{\"style-mobile-max-width\":\"\",\"style-max-width\":\"\",\"style-tablet-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"5FF2FCA311A14C3C45C25F5D12EA3837\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-margin-top\":\"25px\",\"style-color\":\"#000000\",\"textContent\":\"<i class=\\\"fas fa-cogs\\\"><\\/i> Settings\",\"id\":\"10DC2D3359884080BCF09222D7B54A9E\",\"tagName\":\"h5\",\"customId\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"linkLabel\":\"Click to View More Settings\",\"template\":\"<!--{sampleWidth: \'250px\', tags: \'dark-bg icon\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}}; \\n            background-color:{{color||#ff5b57}};\\n            background-image:url(\'{{bgImg}}\');  \\n            color:#fff;\\\">\\n    <div style=\\\"position: absolute;\\n                top: 5px;\\n                right: 5px;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"card-body text-center\\\">\\n        <div class=\\\"stats-icon\\\" \\n             style=\\\"font-size: 42px; \\n                    color: #fff; \\n                    line-height:0;\\\">\\n            {{icon||<i class=\\\"fas fa-globe\\\"><\\/i>}}\\n        <\\/div>\\n        <div class=\\\"stats-info\\\">\\n            <p style=\\\"font-size: 18px; \\n                      font-weight: 600; \\n                      margin-bottom: 0;\\\">\\n                {{value||3,291,922}}\\n            <\\/p>\\n            <h4 style=\\\"font-size: 12px; \\n                       font-weight:bold; \\n                       margin: 5px 0;\\\">\\n                {{title||VISITS}}\\n            <\\/h4>\\n        <\\/div>\\n    <\\/div>\\n    <div class=\\\"card-footer text-right\\\" \\n                style=\\\"padding:5px 15px; \\n                       background:rgba(0,0,0,.6); \\n                       color:rgba(255,255,255,0.6) !important;\\\">\\n        <a style=\\\"color: inherit; \\n                  font-size:12px;\\\" \\n           href=\\\"{{link||#}}\\\">\\n            {{linkLabel||View Detail}} \\n            <i class=\\\"fas fa-arrow-alt-circle-right\\\"><\\/i>\\n        <\\/a>\\n    <\\/div>\\n<\\/div>\\n<!---->\",\"color\":\"#5734E2\",\"icon\":\"<i class=\\\"fas fa-cogs\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/setting/general\');\",\"style-height\":\"100%\",\"description\":\"\",\"bgImg\":\"\",\"title\":\"General Settings\",\"customId\":\"\",\"reload\":\"\",\"id\":\"7711DBF482644DCC04F151AC7DAD98DF\",\"value\":\"<br>\",\"height\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},{\"elements\":[{\"className\":\"org.joget.plugin.enterprise.InformationTileComponent\",\"properties\":{\"attr-data-events-listening\":[],\"linkLabel\":\"System Information\",\"template\":\"<!--{sampleWidth: \'250px\', tags: \'dark-bg icon link\'}-->\\n<div class=\\\"card\\\" \\n     style=\\\"height:{{height}}; \\n            background-color:{{color||#8753de}}; \\n            background-image:url(\'{{bgImg}}\'); \\n            color:#fff;\\\">\\n    <div style=\\\"position: absolute;\\n                z-index: 1;\\\">\\n        {{reload}}\\n    <\\/div>\\n    <div class=\\\"card-body row\\\">\\n        <div class=\\\"stats-info col-12\\\">\\n            <h4 style=\\\"font-size: 20px;\\n                    font-weight: 600;\\n                       margin: 0px 0;\\n                       margin-bottom: 15px;\\n                       color: rgba(255,255,255,.7);\\\">\\n                {{title||TOTAL VISITORS}}\\n            <\\/h4>\\n            <p style=\\\"font-size: 13px; \\n                      font-weight: 600; \\n                      margin-bottom: 0px;\\\">\\n                {{value||3,291,922}}\\n            <\\/p>\\n        <\\/div>\\n    <\\/div>\\n    <div class=\\\"card-footer text-right\\\" \\n                style=\\\"padding:5px 15px; \\n                       background:rgba(0,0,0,.6); \\n                       color:rgba(255,255,255,0.6) !important;\\\">\\n        <a style=\\\"color: inherit; \\n                  font-size:12px;\\\" \\n           href=\\\"{{link||#}}\\\">\\n            {{linkLabel||View Detail}} \\n            <i class=\\\"fas fa-arrow-alt-circle-right\\\"><\\/i>\\n        <\\/a>\\n    <\\/div>\\n<\\/div>\\n<!---->\",\"color\":\"#348FE2\",\"icon\":\"<i class=\\\"zmdi zmdi-globe-alt\\\"><\\/i>\",\"link\":\"javascript:AdminBar.showQuickOverlay(\'#request.baseURL#/web/console/setting/sysinfo\');\",\"style-height\":\"100%\",\"bgImg\":\"\",\"title\":\"Version\",\"customId\":\"\",\"reload\":\"\",\"bgColor\":\"\",\"id\":\"833C998482894E3D11E5AA01FA76291A\",\"value\":\"@@enterprise.console.footer.message@@<br>@@console.footer.label.revision@@\",\"height\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{}},{\"elements\":[],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"id\":\"20DB6C4EFAB24BDFB4E28B244528DA38\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"gutter\":\"5px\",\"columns\":[{\"style-max-width\":\"\"},{\"style-max-width\":\"\"},{\"style-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"6BE4850CA13040D6D88E42244DF067AA\"}},{\"className\":\"menu-component\"},{\"elements\":[{\"elements\":[],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-flex\":\"0 0 100%\",\"style-max-width\":\"100%\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"gutter\":\"5px\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"100%\",\"style-tablet-max-width\":\"\"}],\"css-mobile-stack-columns\":\"true\",\"id\":\"7F88094D03C249DB76B9FC711B7C87F7\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.model.UserviewPage\",\"properties\":{\"id\":\"up-4E2A517C3275429806518BAA8D29AB1F\"}}',NULL),
('appcenter',1,'up-8C70B71371B942B6D48A7E9B4C1DB8D1','Home','INTERNAL_USERVIEW_PAGE','2023-11-01 13:39:15','2023-11-01 13:39:15','{\"elements\":[{\"elements\":[{\"elements\":[{\"elements\":[{\"elements\":[],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-margin-top\":\"15px\",\"style-margin-top\":\"30px\",\"style-text-align\":\"center\",\"style-margin-bottom\":\"20px\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{\"hidden\":\"true\"},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"style-color\":\"#FFFFFF\",\"style-mobile-margin-bottom\":\"15px\",\"style-font-size\":\"13px\",\"attr-data-events-triggering\":[],\"id\":\"8358CCF066424B1AF09EBF61A96EAFC5\",\"customId\":\"assignment_msg\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-position\":\"absolute\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"style-color\":\"#FFFFFF\",\"style-line-height\":\"40px\",\"style-font-size\":\"20px\",\"textContent\":\" \",\"attr-data-events-triggering\":[],\"tagName\":\"h3\",\"customId\":\"clock\",\"style-top\":\"35px\",\"style-margin-top\":\"0px\",\"style-right\":\"15px\",\"style-text-align\":\"left\",\"style-margin-bottom\":\"0px\",\"id\":\"E92400A7CBAD40DC6E28B620D44D34D6\"}},{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.ButtonComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"css-display-type\":\"btn btn-light\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"style-color\":\"#7E8299\",\"style-margin-right\":\"10px\",\"textContent\":\"#i18n.Design New App#\",\"attr-data-events-triggering\":[],\"attr-href\":\"design_app\",\"id\":\"8EFC9D21190A4BFBE31A945826FF6B34\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ButtonComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"css-display-type\":\"btn btn-light\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"style-color\":\"#7E8299\",\"style-margin-right\":\"15px\",\"attr-onclick\":\"appImport();return false;\",\"textContent\":\"#i18n.Import App#\",\"attr-data-events-triggering\":[],\"attr-href\":\"#\",\"id\":\"10862931081E4DF47D44CCDFC696CAD6\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ButtonComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"css-display-type\":\"btn btn-light\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"style-color\":\"#7E8299\",\"attr-onclick\":\"AdminBar.showQuickOverlay(\\\"#request.contextPath#/web/desktop/marketplace/app?url=\\\" + encodeURIComponent(\\\"#platform.marketplaceUrl#\\\"));return false\",\"textContent\":\"#i18n.Download from Marketplace#\",\"attr-data-events-triggering\":[],\"attr-href\":\"#\",\"id\":\"07863BFB349747ED1A6E45812F7C5F97\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-text-align\":\"center\",\"hidden\":\"true\",\"style-margin-bottom\":\"10px\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{\"hidden\":\"true\"},\"58F9FBEA76264BE037991759BC1645A2\":{\"hidden\":\"\"}},\"attr-data-events-triggering\":[],\"id\":\"96141190357145C9478235142772AEC5\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ScriptComponent\",\"properties\":{\"attr-data-events-listening\":[],\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"attr-data-events-triggering\":[],\"id\":\"66465E19B19A4481B8BD32BD9BBC0D83\",\"customId\":\"\",\"script\":\"$(function(){\\n    window[\\\"ajaxContentPlaceholder\\\"][\\\"#request.contextPath#/web/userview/appcenter/v/_/home\\\"] = \\\"dashboard\\\";\\n    \\n    var updateClock = function(clock, day){\\n        var date = new Date();\\n        var ampm = date.getHours() < 12 ? \'AM\' : \'PM\';\\n        var hours = date.getHours() == 0\\n                  ? 12\\n                  : date.getHours() > 12\\n                    ? date.getHours() - 12\\n                    : date.getHours();\\n        var minutes = date.getMinutes() < 10 \\n                    ? \'0\' + date.getMinutes() \\n                    : date.getMinutes();\\n        var style = $(clock).find(\\\"style\\\"); \\n        var dstyle = $(day).find(\\\"style\\\"); \\n        $(clock).text(hours + \\\":\\\" + minutes + \\\" \\\" + ampm);\\n        $(day).text(date.toLocaleDateString(\\\"en-US\\\", { weekday: \'long\', year: \'numeric\', month: \'long\', day: \'numeric\' }));\\n        $(clock).append(style);\\n        $(day).append(dstyle);\\n    };\\n    \\n    var clock = $(\\\"#clock\\\");\\n    var day = $(\\\"#date\\\")\\n    if (clock.length > 0) {\\n        updateClock(clock, day);\\n        window.setInterval(function() {\\n            updateClock(clock, day);\\n        }, 10000);\\n    }\\n    \\n    $(\\\".inbox-notification\\\").off(\\\"inbox_notification_updated\\\");\\n    $(\\\".inbox-notification\\\").on(\\\"inbox_notification_updated\\\", function(){\\n        var style = $(\\\"#assignment_msg\\\").find(\\\"style\\\");        \\n        $(\\\"#assignment_msg\\\").html(\'<h3 style=\\\"color:#fff;\\\">Hello #currentUser.firstName#, <\\/h3><a style=\\\"color:#fff;opacity: 0.75 !important;\\\" href=\\\"#request.baseURL#/web/userview/appcenter/home/_/_ja_inbox\\\"><h5>\' + $(\\\".inbox-notification .dropdown-menu-title span\\\").text() + \'<h5><\\/a>\');\\n        $(\\\"#assignment_msg\\\").append(style);\\n    });\\n    \\n    $(\\\".inbox-notification\\\").trigger(\\\"inbox_notification_updated\\\");\\n});\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"attr-data-events-triggering\":[],\"id\":\"970F3C4619204E5D5F9253C3A2B9D8A3\",\"customId\":\"inbox_container\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-top\":\"15px\",\"style-position\":\"absolute\",\"style-color\":\"#FFFFFF\",\"style-left\":\"30px\",\"textContent\":\"<i class=\\\"zmdi zmdi-apps\\\"><\\/i> App Center\",\"id\":\"41B14E1F2C034F11E6F29E4A157C3FEC\",\"tagName\":\"h1\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-top\":\"15px\",\"style-right\":\"15px\",\"style-position\":\"absolute\",\"style-text-align\":\"center\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"style-color\":\"#FFFFFF\",\"style-font-size\":\"20px\",\"textContent\":\" \",\"id\":\"681B2A6A3E604CDA411A34255B7BD9E3\",\"tagName\":\"h6\",\"customId\":\"date\"}},{\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"className\":\"menu-component\"}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-custom\":\"padding:20px;\",\"style-flex\":\"0 0 100%\",\"style-max-width\":\"100%\",\"style-mobile-padding-top\":\"80px\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"col-0-style-custom\":\"padding:20px;\",\"col-1-style-tablet-custom\":\"padding:80px 40px 40px 20px;\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"100%\",\"style-tablet-max-width\":\"\"}],\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"col-1-style-custom\":\"padding:80px 80px 40px 20px;\",\"css-mobile-stack-columns\":\"true\",\"attr-data-events-triggering\":[],\"customId\":\"home_column\",\"col-0-style-mobile-padding-top\":\"80px\",\"gutter\":\"\",\"id\":\"74DC9C5E101D4BB4D955B3B9C5FE48D3\",\"col-1-style-mobile-custom\":\"padding: 0px 20px 20px;\"}}],\"className\":\"org.joget.apps.userview.model.UserviewPage\",\"properties\":{\"id\":\"up-8C70B71371B942B6D48A7E9B4C1DB8D1\"}}',NULL),
('appcenter',1,'up-95447DD6260B48B2E00C1354A179CD44','Templates','INTERNAL_USERVIEW_PAGE','2023-11-01 13:39:15','2023-11-01 13:39:15','{\"elements\":[{\"className\":\"menu-component\"}],\"className\":\"org.joget.apps.userview.model.UserviewPage\",\"properties\":{\"id\":\"up-95447DD6260B48B2E00C1354A179CD44\"}}',NULL);
/*!40000 ALTER TABLE `app_builder` ENABLE KEYS */;
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
  `description` text DEFAULT NULL,
  `json` longtext DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK5E9247A6462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FK5E9247A6462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_datalist`
--

LOCK TABLES `app_datalist` WRITE;
/*!40000 ALTER TABLE `app_datalist` DISABLE KEYS */;
INSERT INTO `app_datalist` VALUES
('appcenter',1,'applist','App List',NULL,'{\"actions\":[],\"binder\":{\"className\":\"org.joget.apps.datalist.lib.JsonApiDatalistBinder\",\"properties\":{\"jsonUrl\":\"#request.baseURL#\\/web\\/json\\/apps\\/published\\/userviews?appCenter=true\",\"requestType\":\"\",\"headers\":[],\"copyCookies\":\"true\",\"multirowBaseObject\":\"apps.userviews\",\"totalRowCountObject\":\"\",\"primaryKey\":\"id\",\"handlePaging\":\"true\",\"debugMode\":\"\",\"sampleResponse\":\"{\\n    \\\"apps\\\":[\\n        {\\n            \\\"name\\\": \\\"Customer Relationship Management\\\",\\n            \\\"userviews\\\":[\\n                {\\n                    \\\"imageUrl\\\":\\\"\\/jw\\/web\\/app\\/crm\\/resources\\/crm.png\\\",\\n                    \\\"name\\\":\\\"CRM\\\",\\n                    \\\"id\\\": \\\"crm_userview_sales\\\",\\n                    \\\"category\\\":\\\"\\\",\\n                    \\\"version\\\":1,\\n                    \\\"url\\\": \\\"\\/jw\\/web\\/userview\\/crm\\/crm_userview_sales\\\"\\n                }\\n            ],\\n            \\\"id\\\":\\\"crm\\\",\\n            \\\"version\\\":1\\n        }\\n    ]\\n}\"}},\"card-style-background-color\":\"#F5F8FA\",\"card-style-border-radius\":\"10px\",\"card-style-border-width\":\"0px\",\"card-style-hover-background-color\":\"#F5F8FA\",\"card-style-hover-border-color\":\"#FFFFFF\",\"card-style-hover-custom\":\"transition: all 0.2s ease-out;\\r\\nbox-shadow: 0px 4px 8px rgba(38, 38, 38, 0.2);\\r\\ntop: -5px;\\r\\nborder: 1px solid #cccccc;\\r\\nbackground-color: white;\",\"card-style-max-height\":\"200px\",\"card-style-min-height\":\"200px\",\"card-style-padding-bottom\":\"0px\",\"card-style-padding-left\":\"0px\",\"card-style-padding-right\":\"0px\",\"card-style-padding-top\":\"2.25rem\",\"card-style-text-align\":\"center\",\"column_title\":[{\"id\":\"column_2\",\"name\":\"name\",\"label\":\"name\",\"filterable\":true,\"hidden\":\"false\",\"sortable\":\"false\",\"datalist_type\":\"column\",\"renderHtml\":\"\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"format\":{\"className\":\"\",\"properties\":{}},\"style-font-size\":\"16px\",\"style-color\":\"#495057\",\"style-line-height\":\"20px\",\"style-custom\":\"text-overflow: ellipsis;\\r\\noverflow: hidden;\\r\\ndisplay: -webkit-box;\\r\\n-webkit-line-clamp: 2;\\r\\n-webkit-box-orient: vertical;\"}],\"columns\":[],\"considerFilterWhenGetTotal\":\"\",\"description\":\"\",\"disableResponsive\":\"\",\"filters\":[],\"hidePageSize\":\"true\",\"id\":\"applist\",\"name\":\"App List\",\"order\":\"2\",\"orderBy\":\"\",\"pageSize\":\"100\",\"pageSizeSelectorOptions\":\"10\",\"permission_rules\":[{\"permission\":{\"className\":\"org.joget.plugin.enterprise.AdminUserviewPermission\",\"properties\":{}},\"permission_key\":\"9D3F667778064DF2976BDBE2C629EA8D\",\"permission_name\":\"Is Admin\"}],\"responsiveMode\":\"\",\"rowAction_card\":[{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"id\":\"rowAction_2\",\"properties\":{\"confirmation\":\"\",\"datalist_type\":\"row_action\",\"header_label\":\"\",\"href\":\"#request.baseURL#\\/web\\/userview\",\"hrefColumn\":\"apps.id;id\",\"hrefParam\":\";\",\"label\":\"Hyperlink\",\"link-css-display-type\":\"btn btn-sm btn-primary\",\"rules\":[],\"target\":\"_blank\",\"style-custom\":\"box-shadow: none;\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{}}}}],\"rowActions\":[{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"hidden\":\"true\",\"id\":\"rowAction_0\",\"label\":\"Hyperlink\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{\"hidden\":\"\"}},\"properties\":{\"confirmation\":\"\",\"datalist_type\":\"row_action\",\"header_label\":\"\",\"href\":\"#request.baseURL#\\/web\\/console\\/app\",\"hrefColumn\":\"apps.id;apps.version;builders\",\"hrefParam\":\";;\",\"label\":\"<i class=\\\"fas fa-pen\\\"><\\/i> \",\"link-css-display-type\":\"btn btn-sm btn-primary\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{}},\"rules\":[],\"style-border-radius\":\"50%\",\"style-custom\":\"transition: 0.2s display ease-in-out;\",\"style-display\":\"none\",\"style-height\":\"30px\",\"style-position\":\"absolute\",\"style-right\":\"10px\",\"style-top\":\"10px\",\"style-width\":\"30px\",\"target\":\"_blank\",\"hidden\":\"true\"}}],\"searchPopup\":\"\",\"showDataWhenFilterSet\":\"\",\"template\":{\"className\":\"org.joget.apps.datalist.lib.AppIconTemplate\",\"properties\":{\"columns_mobile\":\"col-12\",\"columns_tablet\":\"col-md-6\",\"columns_desktop\":\"col-lg-2\",\"splitListByColumn\":\"\",\"enableSuperApp\":\"\"}},\"useSession\":\"\",\"column_image\":[{\"action\":{\"className\":\"\",\"properties\":{}},\"alignment\":\"\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"filterable\":true,\"format\":{\"className\":\"org.joget.apps.datalist.lib.ImageFormatter\",\"properties\":{\"defaultImage\":\"#request.baseURL#\\/web\\/userview\\/screenshot\\/appcenter\\/v\",\"height\":\"90px\",\"imageSrc\":\"\",\"imagefullsize\":\"\",\"width\":\"90px\"}},\"headerAlignment\":\"\",\"hidden\":\"false\",\"id\":\"column_0\",\"label\":\"imageUrl\",\"name\":\"imageUrl\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{\"exclude_export\":\"true\",\"hidden\":\"false\",\"include_export\":\"\"}},\"renderHtml\":\"\",\"sortable\":\"false\",\"style\":\"\",\"style-border-radius\":\"10px\",\"width\":\"\"}]}','2023-11-01 13:39:15','2023-11-01 13:39:15'),
('appcenter',1,'list_appList','List - Template List',NULL,'{\"actions\":[],\"binder\":{\"className\":\"org.joget.apps.datalist.lib.JsonApiDatalistBinder\",\"properties\":{\"copyCookies\":\"true\",\"debugMode\":\"\",\"handlePaging\":\"true\",\"headers\":[],\"jsonUrl\":\"#request.baseURL#\\/web\\/json\\/marketplace\\/list?type=Template&search={name}\",\"multirowBaseObject\":\"response\",\"primaryKey\":\"id\",\"requestType\":\"\",\"sampleResponse\":\"[\\n      {\\n        \\\"brief\\\": \\\"\\\",\\n        \\\"img\\\": \\\"\\\",\\n        \\\"developedBy\\\": \\\"\\\",\\n        \\\"joget_version\\\": \\\"\\\",\\n        \\\"count\\\": \\\"\\\",\\n        \\\"rating\\\": \\\"\\\",\\n        \\\"edition\\\": \\\"\\\",\\n        \\\"isNew\\\": \\\"\\\",\\n        \\\"version\\\": \\\"\\\",\\n        \\\"license\\\": \\\"\\\",\\n        \\\"contributor\\\": \\\"\\\",\\n        \\\"size\\\": \\\"\\\",\\n        \\\"price\\\": \\\"\\\",\\n        \\\"name\\\": \\\"\\\",\\n        \\\"id\\\": \\\"-v1\\\",\\n        \\\"tag\\\": \\\"\\\",\\n        \\\"category\\\": \\\"\\\",\\n        \\\"subcategory\\\": \\\"\\\",\\n        \\\"desc\\\": \\\"\\\"\\n      }\\n]\",\"totalRowCountObject\":\"\"}},\"buttonPosition\":\"bottomLeft\",\"card-style-margin-top\":\"10px\",\"checkboxPosition\":\"left\",\"column_title\":[{\"action\":{\"className\":\"\",\"properties\":{}},\"alignment\":\"\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"format\":{\"className\":\"\",\"properties\":{}},\"headerAlignment\":\"\",\"hidden\":\"false\",\"id\":\"column_0\",\"label\":\"App Name\",\"name\":\"name\",\"renderHtml\":\"\",\"sortable\":\"false\",\"style\":\"\",\"style-color\":\"#181C32\",\"width\":\"\"}],\"columns\":[{\"action\":{\"className\":\"\",\"properties\":{}},\"alignment\":\"\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"format\":{\"className\":\"org.joget.plugin.enterprise.BeanShellFormatter\",\"properties\":{\"script\":\"return value + \\\" <i class=\\\\\\\"fas fa-star\\\\\\\" style=\\\\\\\"color:#FDDA0D;\\\\\\\"><\\/i>\\\" \"}},\"headerAlignment\":\"\",\"hidden\":\"false\",\"id\":\"column_2\",\"label\":\"Rating\",\"name\":\"rating\",\"renderHtml\":\"\",\"sortable\":\"false\",\"style\":\"\",\"width\":\"\"},{\"action\":{\"className\":\"\",\"properties\":{}},\"alignment\":\"\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"format\":{\"className\":\"org.joget.plugin.enterprise.BeanShellFormatter\",\"properties\":{\"script\":\"return \\\"by\\\" + (value.equals(\\\"Joget, Inc.\\\")?\\\"<span class=\\\\\\\"badge badge-light-primary\\\\\\\">Joget, Inc.<\\/span>\\\":\\\"<span class=\\\\\\\"badge badge-light-info\\\\\\\">\\\"+value+\\\"<\\/span>\\\")\"}},\"headerAlignment\":\"\",\"hidden\":\"false\",\"id\":\"column_1\",\"label\":\"Design By\",\"name\":\"developedBy\",\"renderHtml\":\"\",\"sortable\":\"false\",\"style\":\"\",\"width\":\"\"},{\"action\":{\"className\":\"\",\"properties\":{}},\"alignment\":\"\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"filterable\":true,\"format\":{\"className\":\"org.joget.apps.datalist.lib.ImageFormatter\",\"properties\":{\"defaultImage\":\"\",\"height\":\"0;padding-bottom:50%\",\"imageSrc\":\"\",\"imagefullsize\":\"\",\"width\":\"100%\"}},\"headerAlignment\":\"\",\"hidden\":\"true\",\"id\":\"column_5\",\"include_export\":\"\",\"label\":\"Image\",\"name\":\"img\",\"renderHtml\":\"\",\"sortable\":\"false\",\"style\":\"\",\"style-border-radius\":\"10px\",\"style-display\":\"none\",\"style-max-width\":\"100%\",\"width\":\"\"},{\"action\":{\"className\":\"\",\"properties\":{}},\"alignment\":\"\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"filterable\":true,\"format\":{\"className\":\"\",\"properties\":{}},\"headerAlignment\":\"\",\"hidden\":\"false\",\"id\":\"column_6\",\"label\":\"desc\",\"name\":\"desc\",\"renderHtml\":\"\",\"sortable\":\"false\",\"style\":\"\",\"style-margin-top\":\"25px\",\"width\":\"\"},{\"action\":{\"className\":\"\",\"properties\":{}},\"alignment\":\"\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"format\":{\"className\":\"org.joget.plugin.enterprise.BeanShellFormatter\",\"properties\":{\"script\":\"return \\\"<i class=\\\\\\\"zmdi zmdi-download\\\\\\\"><\\/i> \\\" + value;\"}},\"headerAlignment\":\"\",\"hidden\":\"false\",\"id\":\"column_3\",\"label\":\"No Of Download\",\"name\":\"count\",\"renderHtml\":\"\",\"sortable\":\"false\",\"style\":\"\",\"width\":\"\"}],\"considerFilterWhenGetTotal\":\"\",\"description\":\"\",\"filters\":[{\"datalist_type\":\"filter\",\"filterParamName\":\"d-6451840-fn_name\",\"id\":\"filter_0\",\"label\":\"Search\",\"name\":\"name\",\"type\":{\"className\":\"org.joget.apps.datalist.lib.TextFieldDataListFilterType\",\"properties\":{}}}],\"hidePageSize\":\"\",\"id\":\"list_appList\",\"name\":\"List - Template List\",\"order\":\"2\",\"orderBy\":\"name\",\"pageSize\":\"15\",\"pageSizeSelectorOptions\":\"15,30,60,90\",\"responsiveMode\":\"\",\"rowActions\":[{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"id\":\"rowAction_0\",\"properties\":{\"confirmation\":\"\",\"datalist_type\":\"row_action\",\"header_label\":\"\",\"href\":\"javascript:appCreate(\'{id}\');\",\"hrefColumn\":\"\",\"hrefParam\":\"\",\"label\":\"Use This Template\",\"link-css-display-type\":\"btn btn-sm btn-primary\",\"rules\":[],\"target\":\"_self\"}},{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"id\":\"rowAction_1\",\"properties\":{\"confirmation\":\"\",\"datalist_type\":\"row_action\",\"header_label\":\"\",\"href\":\"javascript:AdminBar.showQuickOverlay(\'#request.contextPath#\\/web\\/desktop\\/marketplace\\/app?url=\' + encodeURIComponent(\'#platform.marketplaceUrl#\\/jw\\/web\\/userview\\/mp\\/mpp\\/_\\/vad?id={id}\'))\",\"hrefColumn\":\"\",\"hrefParam\":\"\",\"label\":\"More Info\",\"link-css-display-type\":\"btn btn-sm btn-light\",\"rules\":[],\"style-margin-left\":\"10px\",\"style-padding-left\":\"30px\",\"style-padding-right\":\"30px\",\"target\":\"_self\"}}],\"showDataWhenFilterSet\":\"\",\"showPageSizeSelector\":\"true\",\"template\":{\"className\":\"org.joget.apps.datalist.lib.SimpleCardTemplate\",\"properties\":{\"image\":\"\",\"inlineLabel\":\"\",\"footerMsg\":\"\",\"cardAction\":\"\",\"columns_mobile\":\"col-12\",\"columns_tablet\":\"col-md-6\",\"columns_desktop\":\"col-lg-4\"}},\"useSession\":\"\"}','2023-11-01 13:39:15','2023-11-01 13:39:15'),
('appcenter',1,'list_newTemplate','List - New Template',NULL,'{\"considerFilterWhenGetTotal\":\"\",\"template\":{\"className\":\"org.joget.apps.datalist.lib.SimpleCardTemplate\",\"properties\":{\"image\":\"\",\"inlineLabel\":\"\",\"footerMsg\":\"\",\"cardAction\":\"\",\"columns_mobile\":\"col-12\",\"columns_tablet\":\"col-md-6\",\"columns_desktop\":\"col-lg-6\"}},\"useSession\":\"\",\"responsiveMode\":\"\",\"showPageSizeSelector\":\"true\",\"columns\":[{\"filterable\":true,\"hidden\":\"false\",\"include_export\":\"\",\"renderHtml\":\"\",\"format\":{\"className\":\"org.joget.apps.datalist.lib.ImageFormatter\",\"properties\":{\"imageSrc\":\"\",\"imagefullsize\":\"\",\"height\":\"0;padding-bottom:50%\",\"width\":\"100%\",\"defaultImage\":\"\"}},\"label\":\"Image\",\"sortable\":\"false\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"headerAlignment\":\"\",\"name\":\"img\",\"width\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"style\":\"\",\"id\":\"column_5\",\"alignment\":\"\",\"style-max-width\":\"100%\",\"style-border-radius\":\"10px\"},{\"hidden\":\"false\",\"renderHtml\":\"\",\"style-color\":\"#181C32\",\"format\":{\"className\":\"\",\"properties\":{}},\"label\":\"App Name\",\"sortable\":\"false\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"headerAlignment\":\"\",\"name\":\"name\",\"width\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"style\":\"\",\"id\":\"column_0\",\"alignment\":\"\"},{\"filterable\":true,\"hidden\":\"false\",\"renderHtml\":\"\",\"format\":{\"className\":\"\",\"properties\":{}},\"label\":\"desc\",\"sortable\":\"false\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"style-margin-top\":\"25px\",\"headerAlignment\":\"\",\"name\":\"desc\",\"width\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"style\":\"\",\"id\":\"column_6\",\"alignment\":\"\"}],\"rowActions\":[{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"id\":\"rowAction_0\",\"properties\":{\"hrefColumn\":\"\",\"hrefParam\":\"\",\"rules\":[],\"confirmation\":\"\",\"href\":\"javascript:appCreate(\'{id}\');\",\"label\":\"Use This Template\",\"datalist_type\":\"row_action\",\"link-css-display-type\":\"btn btn-sm btn-primary\",\"header_label\":\"\",\"target\":\"_self\"}},{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"id\":\"rowAction_1\",\"properties\":{\"style-padding-right\":\"30px\",\"hrefColumn\":\"\",\"style-margin-left\":\"10px\",\"hrefParam\":\"\",\"rules\":[],\"confirmation\":\"\",\"label\":\"More Info\",\"datalist_type\":\"row_action\",\"header_label\":\"\",\"style-padding-left\":\"30px\",\"target\":\"_self\",\"href\":\"javascript:AdminBar.showQuickOverlay(\'#request.contextPath#\\/web\\/desktop\\/marketplace\\/app?url=\' + encodeURIComponent(\'#platform.marketplaceUrl#\\/jw\\/web\\/userview\\/mp\\/mpp\\/_\\/vad?id={id}\'))\",\"link-css-display-type\":\"btn btn-sm btn-light\"}}],\"description\":\"\",\"orderBy\":\"name\",\"pageSize\":\"0\",\"filters\":[],\"showDataWhenFilterSet\":\"\",\"buttonPosition\":\"bottomLeft\",\"hidePageSize\":\"true\",\"pageSizeSelectorOptions\":\"15,30,60,90\",\"checkboxPosition\":\"left\",\"column_title\":[],\"name\":\"List - New Template\",\"card-style-margin-top\":\"10px\",\"id\":\"list_newTemplate\",\"binder\":{\"className\":\"org.joget.apps.datalist.lib.JsonApiDatalistBinder\",\"properties\":{\"jsonUrl\":\"#request.baseURL#\\/web\\/json\\/marketplace\\/list?type=Template&isNew=true&rows=2&sort=name&desc=false\",\"requestType\":\"\",\"headers\":[],\"copyCookies\":\"true\",\"multirowBaseObject\":\"response\",\"totalRowCountObject\":\"\",\"primaryKey\":\"id\",\"handlePaging\":\"\",\"debugMode\":\"\",\"sampleResponse\":\"[\\n      {\\n        \\\"brief\\\": \\\"\\\",\\n        \\\"img\\\": \\\"\\\",\\n        \\\"developedBy\\\": \\\"\\\",\\n        \\\"joget_version\\\": \\\"\\\",\\n        \\\"count\\\": \\\"\\\",\\n        \\\"rating\\\": \\\"\\\",\\n        \\\"edition\\\": \\\"\\\",\\n        \\\"isNew\\\": \\\"\\\",\\n        \\\"version\\\": \\\"\\\",\\n        \\\"license\\\": \\\"\\\",\\n        \\\"contributor\\\": \\\"\\\",\\n        \\\"size\\\": \\\"\\\",\\n        \\\"price\\\": \\\"\\\",\\n        \\\"name\\\": \\\"\\\",\\n        \\\"id\\\": \\\"-v1\\\",\\n        \\\"tag\\\": \\\"\\\",\\n        \\\"category\\\": \\\"\\\",\\n        \\\"subcategory\\\": \\\"\\\",\\n        \\\"desc\\\": \\\"\\\"\\n      }\\n]\"}},\"actions\":[],\"order\":\"2\"}','2023-11-01 13:39:15','2023-11-01 13:39:15'),
('crm_community',1,'crm_account_list','Account Listing',NULL,'{\"id\":\"crm_account_list\",\"name\":\"Account Listing\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[{\"id\":\"rowAction_0\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_list\",\"target\":\"_self\",\"hrefParam\":\"d-6304176-fn_account\",\"hrefColumn\":\"id\",\"label\":\"Contacts\",\"confirmation\":\"\",\"visible\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"},{\"id\":\"rowAction_1\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"fk_account\",\"hrefColumn\":\"id\",\"label\":\"New Contact\",\"confirmation\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"}],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"label\":\"Add Account\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_account\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_0\",\"label\":\"Account Name\",\"name\":\"accountName\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_account\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"id\",\"label\":\"ID\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"ID\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_1\",\"name\":\"accountName\",\"label\":\"Account Name\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_5\",\"name\":\"country\",\"label\":\"Country\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_4\",\"name\":\"state\",\"label\":\"State\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}]}','2023-11-01 13:38:43','2023-11-01 13:38:43'),
('crm_community',1,'crm_contact_list','Contact List',NULL,'{\"id\":\"crm_contact_list\",\"name\":\"Contact List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"label\":\"Add Contact\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_contact\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_2\",\"name\":\"account\",\"filterParamName\":\"d-6304176-fn_account\",\"label\":\"Account\",\"type\":{\"className\":\"\",\"properties\":{}},\"datalist_type\":\"filter\",\"hidden\":\"\"},{\"id\":\"filter_0\",\"name\":\"fullName\",\"label\":\"First Name\",\"type\":{\"className\":\"org.joget.apps.datalist.lib.TextFieldDataListFilterType\",\"properties\":{\"defaultValue\":\"\"}},\"filterParamName\":\"d-6304176-fn_fullName\"},{\"id\":\"filter_1\",\"label\":\"Last Name\",\"name\":\"lastName\",\"filterParamName\":\"d-6304176-fn_lastName\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_contact\"}},\"columns\":[{\"id\":\"column_2\",\"name\":\"account\",\"label\":\"Account\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"account\",\"label\":\"Account\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}},\"datalist_type\":\"column\",\"renderHtml\":\"\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\"},{\"id\":\"column_0\",\"name\":\"fullName\",\"label\":\"First Name\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Full Name\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_4\",\"name\":\"lastName\",\"label\":\"Last Name\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}],\"template\":{\"className\":\"\",\"properties\":{}}}','2023-11-01 13:38:43','2023-11-01 13:38:43'),
('crm_community',1,'crm_opportunity_list','Opportunity List',NULL,'{\"id\":\"crm_opportunity_list\",\"name\":\"Opportunity List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"opportunity_new\",\"target\":\"_self\",\"hrefParam\":\"\",\"hrefColumn\":\"\",\"label\":\"Add Opportunity\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_opportunity\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_1\",\"name\":\"account\",\"label\":\"Account\",\"type\":{\"className\":\"\",\"properties\":{}},\"filterParamName\":\"d-4412661-fn_account\",\"datalist_type\":\"filter\",\"hidden\":\"\"},{\"id\":\"filter_0\",\"label\":\"Title\",\"name\":\"title\",\"filterParamName\":\"d-4412661-fn_title\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_opportunity\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"title\",\"label\":\"Title\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"opportunity_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Title\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_4\",\"name\":\"account\",\"label\":\"Account\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"account\",\"label\":\"Account\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}},\"datalist_type\":\"column\",\"renderHtml\":\"\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\"},{\"id\":\"column_2\",\"name\":\"amount\",\"label\":\"Amount\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_3\",\"name\":\"stage\",\"label\":\"Stage\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_1\",\"name\":\"dateModified\",\"label\":\"Date Modified\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}],\"template\":{\"className\":\"\",\"properties\":{}}}','2023-11-01 13:38:43','2023-11-01 13:38:43'),
('crm_community',1,'Proposal','Proposal List',NULL,'{\"id\":\"Proposal\",\"name\":\"Proposal List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"refNo\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_0\",\"properties\":{\"href\":\"proposal_process\",\"target\":\"_self\",\"label\":\"Submit New Proposal\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"name\":\"Form Row Delete Action\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"label\":\"Delete\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"label\":\"Delete\",\"formDefId\":\"crm_proposal_form\",\"confirmation\":\"Are you sure?\",\"deleteGridData\":\"true\",\"deleteSubformData\":\"true\",\"deleteFiles\":\"true\",\"abortRelatedRunningProcesses\":\"true\",\"datalist_type\":\"action\"}}],\"filters\":[{\"id\":\"filter_2\",\"name\":\"account\",\"label\":\"Account\",\"filterParamName\":\"d-3959580-fn_account\",\"datalist_type\":\"filter\",\"type\":{\"className\":\"\",\"properties\":{}},\"hidden\":\"\"},{\"id\":\"filter_1\",\"name\":\"title\",\"label\":\"Title\",\"filterParamName\":\"d-3959580-fn_title\"},{\"id\":\"filter_0\",\"name\":\"status\",\"label\":\"Status\",\"filterParamName\":\"d-3959580-fn_status\"}],\"binder\":{\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_proposal_approval_form\",\"extraCondition\":\"\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"refNo\",\"label\":\"#\",\"sortable\":\"false\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_1\",\"label\":\"Account\",\"displayLabel\":\"Account\",\"name\":\"account\",\"datalist_type\":\"column\",\"sortable\":\"false\",\"renderHtml\":\"\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_2\",\"datalist_type\":\"column\",\"name\":\"title\",\"label\":\"Title\",\"sortable\":\"false\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"ViewProposal\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"View\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_3\",\"label\":\"Description\",\"displayLabel\":\"Description\",\"name\":\"description\"},{\"id\":\"column_4\",\"label\":\"Status\",\"displayLabel\":\"Status\",\"name\":\"status\"},{\"id\":\"column_5\",\"label\":\"Date Modified\",\"displayLabel\":\"Date Modified\",\"name\":\"dateModified\"}],\"template\":{\"className\":\"\",\"properties\":{}}}','2023-11-01 13:38:43','2023-11-01 13:38:43');
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
  `value` text DEFAULT NULL,
  `remarks` text DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK740A62EC462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FK740A62EC462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_env_variable`
--

LOCK TABLES `app_env_variable` WRITE;
/*!40000 ALTER TABLE `app_env_variable` DISABLE KEYS */;
INSERT INTO `app_env_variable` VALUES
('crm_community',1,'AppName','Customer Relationship Management',NULL),
('crm_community',1,'refNo','4',NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_fd`
--

LOCK TABLES `app_fd` WRITE;
/*!40000 ALTER TABLE `app_fd` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_fd` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_fd_crm_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_fd_crm_account` (
  `id` varchar(255) NOT NULL,
  `dateCreated` datetime(6) DEFAULT NULL,
  `dateModified` datetime(6) DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `createdByName` varchar(255) DEFAULT NULL,
  `modifiedBy` varchar(255) DEFAULT NULL,
  `modifiedByName` varchar(255) DEFAULT NULL,
  `c_country` longtext DEFAULT NULL,
  `c_address` longtext DEFAULT NULL,
  `c_accountName` longtext DEFAULT NULL,
  `c_city` longtext DEFAULT NULL,
  `c_state` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_datecreated` (`dateCreated`),
  KEY `idx_createdby` (`createdBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `dateCreated` datetime(6) DEFAULT NULL,
  `dateModified` datetime(6) DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `createdByName` varchar(255) DEFAULT NULL,
  `modifiedBy` varchar(255) DEFAULT NULL,
  `modifiedByName` varchar(255) DEFAULT NULL,
  `c_lastName` longtext DEFAULT NULL,
  `c_address` longtext DEFAULT NULL,
  `c_city` longtext DEFAULT NULL,
  `c_fullName` longtext DEFAULT NULL,
  `c_photo` longtext DEFAULT NULL,
  `c_state` longtext DEFAULT NULL,
  `c_account` longtext DEFAULT NULL,
  `c_addressAvailable` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_datecreated` (`dateCreated`),
  KEY `idx_createdby` (`createdBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `dateCreated` datetime(6) DEFAULT NULL,
  `dateModified` datetime(6) DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `createdByName` varchar(255) DEFAULT NULL,
  `modifiedBy` varchar(255) DEFAULT NULL,
  `modifiedByName` varchar(255) DEFAULT NULL,
  `c_amount` longtext DEFAULT NULL,
  `c_stage` longtext DEFAULT NULL,
  `c_description` longtext DEFAULT NULL,
  `c_source` longtext DEFAULT NULL,
  `c_newAccount` longtext DEFAULT NULL,
  `c_title` longtext DEFAULT NULL,
  `c_account` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_datecreated` (`dateCreated`),
  KEY `idx_createdby` (`createdBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `dateCreated` datetime(6) DEFAULT NULL,
  `dateModified` datetime(6) DEFAULT NULL,
  `createdBy` varchar(255) DEFAULT NULL,
  `createdByName` varchar(255) DEFAULT NULL,
  `modifiedBy` varchar(255) DEFAULT NULL,
  `modifiedByName` varchar(255) DEFAULT NULL,
  `c_refNo` longtext DEFAULT NULL,
  `c_comments` longtext DEFAULT NULL,
  `c_notes` longtext DEFAULT NULL,
  `c_attachment` longtext DEFAULT NULL,
  `c_description` longtext DEFAULT NULL,
  `c_title` longtext DEFAULT NULL,
  `c_account` longtext DEFAULT NULL,
  `c_status` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_datecreated` (`dateCreated`),
  KEY `idx_createdby` (`createdBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `json` longtext DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`formId`),
  KEY `FK45957822462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FK45957822462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_form`
--

LOCK TABLES `app_form` WRITE;
/*!40000 ALTER TABLE `app_form` DISABLE KEYS */;
INSERT INTO `app_form` VALUES
('crm_community',1,'crm_account','Account Form','2023-11-01 13:38:34','2023-11-01 13:38:34','crm_account','{\n    \"className\": \"org.joget.apps.form.model.Form\",\n    \"properties\": {\n        \"id\": \"crm_account\",\n        \"loadBinder\": {\n            \"className\": \"org.joget.apps.form.lib.WorkflowFormBinder\",\n            \"properties\": {}\n        },\n        \"tableName\": \"crm_account\",\n        \"description\": \"\",\n        \"name\": \"Account Form\",\n        \"storeBinder\": {\n            \"className\": \"org.joget.apps.form.lib.WorkflowFormBinder\",\n            \"properties\": {}\n        }\n    },\n    \"elements\": [\n        {\n            \"elements\": [\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"id\",\n                                \"label\": \"Account ID\",\n                                \"size\": \"\",\n                                \"readonly\": \"\",\n                                \"validator\": {\n                                    \"className\": \"org.joget.apps.form.lib.DefaultValidator\",\n                                    \"properties\": {\n                                        \"mandatory\": \"true\",\n                                        \"type\": \"\"\n                                    }\n                                },\n                                \"workflowVariable\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"accountName\",\n                                \"label\": \"Account Name\",\n                                \"size\": \"\",\n                                \"readonly\": \"\",\n                                \"validator\": {\n                                    \"className\": \"org.joget.apps.form.lib.DefaultValidator\",\n                                    \"properties\": {\n                                        \"mandatory\": \"true\",\n                                        \"type\": \"\"\n                                    }\n                                },\n                                \"workflowVariable\": \"\"\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"99%\"\n                    }\n                }\n            ],\n            \"className\": \"org.joget.apps.form.model.Section\",\n            \"properties\": {\n                \"id\": \"account_details\",\n                \"loadBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"visibilityControl\": \"\",\n                \"visibilityValue\": \"\",\n                \"storeBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"label\": \"Account Details\"\n            }\n        },\n        {\n            \"elements\": [\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextArea\",\n                            \"properties\": {\n                                \"id\": \"address\",\n                                \"cols\": \"20\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"Address\",\n                                \"readonly\": \"\",\n                                \"rows\": \"5\"\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"49%\"\n                    }\n                },\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"city\",\n                                \"workflowVariable\": \"\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"City\",\n                                \"readonly\": \"\",\n                                \"size\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"state\",\n                                \"workflowVariable\": \"\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"State\",\n                                \"readonly\": \"\",\n                                \"size\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.SelectBox\",\n                            \"properties\": {\n                                \"id\": \"country\",\n                                \"workflowVariable\": \"\",\n                                \"optionsBinder\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"Country\",\n                                \"multiple\": \"\",\n                                \"readonly\": \"\",\n                                \"size\": \"\",\n                                \"options\": [\n                                    {\n                                        \"value\": \"\",\n                                        \"label\": \"\"\n                                    },\n                                    {\n                                        \"value\": \"local\",\n                                        \"label\": \"Local\"\n                                    },\n                                    {\n                                        \"value\": \"international\",\n                                        \"label\": \"International\"\n                                    }\n                                ]\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"49%\"\n                    }\n                }\n            ],\n            \"className\": \"org.joget.apps.form.model.Section\",\n            \"properties\": {\n                \"id\": \"address\",\n                \"loadBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"visibilityControl\": \"\",\n                \"visibilityValue\": \"\",\n                \"storeBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"label\": \"Address Details\"\n            }\n        }\n    ]\n}',NULL),
('crm_community',1,'crm_contact','Contact Form','2023-11-01 13:38:41','2023-11-01 13:38:41','crm_contact','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_contact\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_contact\",\"name\":\"Contact Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"account\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"extraCondition\":\"\",\"labelColumn\":\"accountName\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[]}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"fullName\",\"workflowVariable\":\"\",\"readonlyLabel\":\"\",\"maxlength\":\"\",\"encryption\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"message\":\"\",\"custom-regex\":\"\",\"mandatory\":\"true\",\"type\":\"\"}},\"value\":\"\",\"label\":\"First Name\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"lastName\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Last Name\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"addressAvailable\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Address Available\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"no\",\"label\":\"No\"},{\"value\":\"yes\",\"label\":\"Yes\"}]}},{\"className\":\"org.joget.apps.form.lib.FileUpload\",\"properties\":{\"id\":\"photo\",\"label\":\"Photo\",\"readonly\":\"\",\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"contact_details\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Contact Details\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"id\":\"address\",\"cols\":\"20\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{}},\"label\":\"Address\",\"readonly\":\"\",\"rows\":\"5\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"city\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"City\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"state\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"State\",\"readonly\":\"\",\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"address_details\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"addressAvailable\",\"visibilityValue\":\"yes\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Address Details\"}}]}',NULL),
('crm_community',1,'crm_opportunity','Opportunity Form','2023-11-01 13:38:41','2023-11-01 13:38:41','crm_opportunity','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_opportunity\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_opportunity\",\"name\":\"Opportunity Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"title\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"mandatory\":\"true\",\"type\":\"\"}},\"label\":\"Title\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"id\":\"description\",\"cols\":\"15\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Description\",\"readonly\":\"\",\"rows\":\"5\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"amount\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Amount\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"stage\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Stage\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"\",\"label\":\"\"},{\"value\":\"open\",\"label\":\"Open\"},{\"value\":\"won\",\"label\":\"Won\"},{\"value\":\"lost\",\"label\":\"Lost\"}]}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"source\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Source\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"\",\"label\":\"\"},{\"value\":\"direct\",\"label\":\"Direct\"},{\"value\":\"indirect\",\"label\":\"Indirect\"}]}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"opportunity\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Opportunity\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"newAccount\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{}},\"label\":\"New Account\",\"readonly\":\"\",\"multiple\":\"\",\"options\":[{\"value\":\"yes\",\"label\":\"Yes\"},{\"value\":\"no\",\"label\":\"No\"}],\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"accountChoice\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"account\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"extraCondition\":\"\",\"labelColumn\":\"accountName\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[]}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"account_existing\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"newAccount\",\"visibilityValue\":\"no\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Existing Account\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"account\",\"formDefId\":\"crm_account\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"account\",\"readonly\":\"\"}},{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"id\":\"script1\",\"validator\":{\"className\":\"\",\"properties\":{}},\"value\":\"<script>\\nvar val = $(\\\"#account_crm_accountid\\\").val();\\nif (val != \'\') {\\n    $(\\\"#newAccount\\\").val(\\\"no\\\");\\n    $(\\\"#newAccount\\\").trigger(\\\"change\\\");\\n}\\n<\\/script>\",\"label\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"account_new\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"newAccount\",\"visibilityValue\":\"yes\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\"}}]}',NULL),
('crm_community',1,'crm_proposal_approval_form','Proposal Approval Form','2023-11-01 13:38:41','2023-11-01 13:38:41','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Approval Form\",\"id\":\"crm_proposal_approval_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"proposal\",\"label\":\"\",\"formDefId\":\"crm_proposal_form\",\"readonly\":\"true\",\"readonlyLabel\":\"\",\"noframe\":\"\",\"parentSubFormId\":\"\",\"subFormParentId\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"permissionHidden\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Approval\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"type\":\"\",\"mandatory\":\"true\"}},\"multiple\":\"\",\"options\":[{\"label\":\"Approved\",\"value\":\"approved\"},{\"label\":\"Resubmit\",\"value\":\"resubmit\"},{\"label\":\"Rejected\",\"value\":\"rejected\"}],\"workflowVariable\":\"status\",\"id\":\"status\",\"label\":\"Status\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"comments\",\"label\":\"Approver Comments\",\"rows\":\"5\",\"cols\":\"20\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section2\",\"label\":\"Approver Action\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),
('crm_community',1,'crm_proposal_form','Proposal Form','2023-11-01 13:38:41','2023-11-01 13:38:41','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Form\",\"id\":\"crm_proposal_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.IdGeneratorField\",\"properties\":{\"hidden\":\"true\",\"format\":\"????\",\"workflowVariable\":\"\",\"envVariable\":\"refNo\",\"id\":\"refNo\",\"label\":\"Reference No\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"labelColumn\":\"accountName\",\"extraCondition\":\"\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"multiple\":\"\",\"options\":[],\"workflowVariable\":\"\",\"id\":\"account\",\"label\":\"Account\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"type\":\"\",\"mandatory\":\"true\"}},\"workflowVariable\":\"\",\"id\":\"title\",\"label\":\"Title\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"workflowVariable\":\"\",\"id\":\"description\",\"label\":\"Description\",\"placeholder\":\"\",\"rows\":\"5\",\"value\":\"\",\"cols\":\"60\",\"readonlyLabel\":\"\"}},{\"className\":\"org.joget.apps.form.lib.FileUpload\",\"properties\":{\"attachment\":\"true\",\"readonly\":\"\",\"size\":\"\",\"id\":\"attachment\",\"label\":\"Attachment\"}},{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"id\":\"field6\",\"value\":\"#i18n.form_hints#\",\"label\":\"&nbsp;\",\"autoPopulate\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Form\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),
('crm_community',1,'crm_proposal_resubmit_form','Proposal Resubmit Form','2023-11-01 13:38:41','2023-11-01 13:38:41','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_proposal_resubmit_form\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\",\"name\":\"Proposal Resubmit Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"approval\",\"formDefId\":\"crm_proposal_approval_form\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"\",\"readonly\":\"true\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section1\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Proposal Resubmit\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"proposal\",\"formDefId\":\"crm_proposal_form\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"\",\"readonly\":\"\"}},{\"className\":\"org.joget.apps.form.lib.HiddenField\",\"properties\":{\"id\":\"status\",\"workflowVariable\":\"status\",\"value\":\"pending\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section2\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Proposal Resubmission\"}}]}',NULL),
('crm_community',1,'crm_proposal_sending_form','Proposal Sending Form','2023-11-01 13:38:41','2023-11-01 13:38:41','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Sending Form\",\"id\":\"crm_proposal_sending_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"parentSubFormId\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"readonly\":\"true\",\"formDefId\":\"crm_proposal_approval_form\",\"subFormParentId\":\"\",\"id\":\"approval\",\"label\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"readonlyLabel\":\"\",\"noframe\":\"\",\"permissionHidden\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section1\",\"label\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"permission\":{\"className\":\"\",\"properties\":{}},\"permissionReadonly\":\"\",\"comment\":\"\",\"join\":\"\",\"reverse\":\"\",\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"regex\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"notes\",\"label\":\"Notes\",\"rows\":\"5\",\"cols\":\"20\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section2\",\"label\":\"Others\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"join\":\"\",\"reverse\":\"\",\"regex\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"permissionReadonly\":\"\",\"comment\":\"\"}}]}',NULL);
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
  `data` longtext DEFAULT NULL,
  `datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `message` text DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`ouid`),
  KEY `FKEE346FE9462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FKEE346FE9462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_message`
--

LOCK TABLES `app_message` WRITE;
/*!40000 ALTER TABLE `app_message` DISABLE KEYS */;
INSERT INTO `app_message` VALUES
('appcenter',1,'<i class=\'fa fa-home\'></i> Home_zh_CN','<i class=\'fa fa-home\'></i> Home','zh_CN','<i class=\'fa fa-home\'></i> È¶ñÈ°µ'),
('appcenter',1,'<i class=\'fa fa-home\'></i> Home_zh_TW','<i class=\'fa fa-home\'></i> Home','zh_TW','<i class=\'fa fa-home\'></i> È¶ñÈ†Å'),
('appcenter',1,'All Apps_zh_CN','All Apps','zh_CN','ÊâÄÊúâÂ∫îÁî®'),
('appcenter',1,'All Apps_zh_TW','All Apps','zh_TW','ÊâÄÊúâÊáâÁî®'),
('appcenter',1,'App Center_zh_CN','App Center','zh_CN','Â∫îÁî®‰∏≠ÂøÉ'),
('appcenter',1,'App Center_zh_TW','App Center','zh_TW','ÊáâÁî®‰∏≠ÂøÉ'),
('appcenter',1,'Design New App_zh_CN','Design New App','zh_CN','ËÆæËÆ°Êñ∞Â∫îÁî®Á®ãÂ∫è'),
('appcenter',1,'Design New App_zh_TW','Design New App','zh_TW','Ë®≠Ë®àÊñ∞ÊáâÁî®Á®ãÂ∫è'),
('appcenter',1,'Download from Marketplace_zh_CN','Download from Marketplace','zh_CN','Â∫îÁî®Â∏ÇÂú∫'),
('appcenter',1,'Download from Marketplace_zh_TW','Download from Marketplace','zh_TW','ÊáâÁî®Â∏ÇÂ†¥'),
('appcenter',1,'Faster, Simpler Digital Transformation_zh_CN','Faster, Simpler Digital Transformation','zh_CN','Êõ¥Âø´, Êõ¥ÁÆÄÂçïÂú∞ÂÆûÁé∞ ‰ºÅ‰∏öÊï∞Â≠óÂåñËΩ¨Âûã'),
('appcenter',1,'Faster, Simpler Digital Transformation_zh_TW','Faster, Simpler Digital Transformation','zh_TW','Êõ¥Âø´, Êõ¥Á∞°ÂñÆÂú∞ÂØ¶Áèæ ‰ºÅÊ•≠Êï∏Â≠óÂåñËΩâÂûã'),
('appcenter',1,'Import App_zh_CN','Import App','zh_CN','ÂØºÂÖ•Â∫îÁî®'),
('appcenter',1,'Import App_zh_TW','Import App','zh_TW','Â∞éÂÖ•ÊáâÁî®'),
('appcenter',1,'Incorrect Username and/or Password_zh_CN','Incorrect Username and/or Password','zh_CN','ÊÇ®ÁöÑÁôªÂΩïÊú™ÊàêÂäü'),
('appcenter',1,'Incorrect Username and/or Password_zh_TW','Incorrect Username and/or Password','zh_TW','ÊÇ®ÁöÑÁôªÈåÑÊú™ÊàêÂäü'),
('appcenter',1,'Login_zh_CN','Login','zh_CN','ÁôªÂΩï'),
('appcenter',1,'Login_zh_TW','Login','zh_TW','ÁôªÈåÑ'),
('appcenter',1,'Monitor_zh_CN','Monitor','zh_CN','ÁõëÊéß'),
('appcenter',1,'Monitor_zh_TW','Monitor','zh_TW','Áõ£Êéß'),
('appcenter',1,'Password_zh_CN','Password','zh_CN','ÂØÜÁ†Å'),
('appcenter',1,'Password_zh_TW','Password','zh_TW','ÂØÜÁ¢º'),
('appcenter',1,'Powered by Joget_zh_CN','Powered by Joget','zh_CN','Áî±JogetÊäÄÊúØÊîØÊåÅ'),
('appcenter',1,'Powered by Joget_zh_TW','Powered by Joget','zh_TW','Áî±JogetÊäÄË°ìÊîØÊåÅ'),
('appcenter',1,'Published Apps_zh_CN','Published Apps','zh_CN','Êú¨Âú∞Â∑≤ÂèëÂ∏É'),
('appcenter',1,'Published Apps_zh_TW','Published Apps','zh_TW','Êú¨Âú∞Â∑≤ÁôºÂ∏É'),
('appcenter',1,'Settings_zh_CN','Settings','zh_CN','ËÆæÁΩÆ'),
('appcenter',1,'Settings_zh_TW','Settings','zh_TW','Ë®≠ÁΩÆ'),
('appcenter',1,'Username_zh_CN','Username','zh_CN','Áî®Êà∑Âêç'),
('appcenter',1,'Username_zh_TW','Username','zh_TW','Áî®Êà∂Âêç'),
('appcenter',1,'Users_zh_CN','Users','zh_CN','Áî®Êà∑'),
('appcenter',1,'Users_zh_TW','Users','zh_TW','Áî®Êà∂'),
('crm_community',1,'About_zh_CN','About','zh_CN','ÂÖ≥‰∫é'),
('crm_community',1,'aboutpage_en_US','aboutpage','en_US','<h4 style=\"margin: 0px; padding: 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-size: inherit; line-height: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; font-weight: bold; color: #042c54;\">More Leads, More Sales, More Customers</h4>\n<h1 style=\"margin: 0px; padding: 0px; border-width: 0px; font: inherit; vertical-align: baseline; color: #1f4282;\"><span style=\"font-size: large;\"><strong>Business&nbsp;Customer Relationship Management</strong></span></h1>\n<p style=\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\">CRM helps your business communicate with prospects, share sales information, close deals and keep customers happy.<img src=\"http://www.joget.org/images/demo/phone_pad.png\" alt=\"\" width=\"382\" height=\"302\" /></p>'),
('crm_community',1,'aboutpage_zh_CN','aboutpage','zh_CN','<h4 style=\"margin: 0px; padding: 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-size: inherit; line-height: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; font-weight: bold; color: #042c54;\">Êõ¥Â§öÁ∫øÁ¥¢ÔºåÊõ¥Â§öÈîÄÂîÆÔºåÊõ¥Â§öÂÆ¢Êà∑</h4>\n<h1 style=\"margin: 0px; padding: 0px; border-width: 0px; font: inherit; vertical-align: baseline; color: #1f4282;\"><span style=\"font-size: large;\"><strong>ÂÆ¢Êà∑ÂÖ≥Á≥ªÁÆ°ÁêÜ (CRM)</strong></span></h1>\n<p style=\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\">CRMÂ∏ÆÂä©ÊÇ®ÁöÑ‰ºÅ‰∏ö‰∏éÊΩúÂú®ÂÆ¢Êà∑ËøõË°åÊ≤üÈÄöÔºåÂÖ±‰∫´ÈîÄÂîÆ‰ø°ÊÅØÔºåËææÊàê‰∫§ÊòìÂπ∂‰øùÊåÅÂÆ¢Êà∑Êª°ÊÑè„ÄÇ<img src=\"http://www.joget.org/images/demo/phone_pad.png\" alt=\"\" width=\"382\" height=\"302\" /></p>'),
('crm_community',1,'Account Details_zh_CN','Account Details','zh_CN','ÂÆ¢Êà∑ËØ¶ÊÉÖ'),
('crm_community',1,'Account ID_zh_CN','Account ID','zh_CN','ÂÆ¢Êà∑ID'),
('crm_community',1,'Account List_zh_CN','Account List','zh_CN','ÂÆ¢Êà∑ÂàóË°®'),
('crm_community',1,'Account Name_zh_CN','Account Name','zh_CN','ÂÆ¢Êà∑ÂêçÁß∞'),
('crm_community',1,'Account_zh_CN','Account','zh_CN','ÂÆ¢Êà∑'),
('crm_community',1,'Accounts_zh_CN','Accounts','zh_CN','ÂÆ¢Êà∑'),
('crm_community',1,'Add Account_zh_CN','Add Account','zh_CN','Â¢ûÂä†ÂÆ¢Êà∑'),
('crm_community',1,'Add Contact_zh_CN','Add Contact','zh_CN','Â¢ûÂä†ËÅîÁ≥ª‰∫∫'),
('crm_community',1,'Add Opportunity_zh_CN','Add Opportunity','zh_CN','Â¢ûÂä†Êú∫‰ºö'),
('crm_community',1,'Address Available_zh_CN','Address Available','zh_CN','ÂèØÁî®Âú∞ÂùÄ'),
('crm_community',1,'Address Details_zh_CN','Address Details','zh_CN','ËØ¶ÁªÜÂú∞ÂùÄ'),
('crm_community',1,'Address_zh_CN','Address','zh_CN','Âú∞ÂùÄ'),
('crm_community',1,'Amount_zh_CN','Amount','zh_CN','ÈáëÈ¢ù'),
('crm_community',1,'applicationName_en_US','applicationName','en_US','Customer Relationship Management'),
('crm_community',1,'applicationName_zh_CN','applicationName','zh_CN','ÂÆ¢Êà∑ÂÖ≥Á≥ªÁÆ°ÁêÜ (CRM)'),
('crm_community',1,'Approved_zh_CN','Approved','zh_CN','ÊâπÂáÜ'),
('crm_community',1,'Approver Action_zh_CN','Approver Action','zh_CN','ÂÆ°Êâπ‰∫∫ÂÜ≥Á≠ñ'),
('crm_community',1,'Approver Comments_zh_CN','Approver Comments','zh_CN','ÂÆ°Êâπ‰∫∫ËØÑËÆ∫'),
('crm_community',1,'Attachment_zh_CN','Attachment','zh_CN','Êñá‰ª∂‰∏ä‰º†'),
('crm_community',1,'City_zh_CN','City','zh_CN','ÂüéÂ∏Ç'),
('crm_community',1,'Contact Details_zh_CN','Contact Details','zh_CN','ËØ¶ÁªÜËÅîÁ≥ªÊñπÂºè'),
('crm_community',1,'Contact List_zh_CN','Contact List','zh_CN','ËÅîÁ≥ª‰∫∫ÂàóË°®'),
('crm_community',1,'Contacts_zh_CN','Contacts','zh_CN','ËÅîÁ≥ª‰∫∫'),
('crm_community',1,'Country_zh_CN','Country','zh_CN','ÂõΩÂÆ∂'),
('crm_community',1,'Date Modified_zh_CN','Date Modified','zh_CN','Êõ¥Êñ∞Êó•Êúü'),
('crm_community',1,'Delete Row_zh_CN','Delete Row','zh_CN','Âà†Èô§Ë°å'),
('crm_community',1,'Delete_zh_CN','Delete','zh_CN','Âà†Èô§'),
('crm_community',1,'Description_zh_CN','Description','zh_CN','ÊèèËø∞'),
('crm_community',1,'Direct_zh_CN','Direct','zh_CN','Áõ¥Êé•'),
('crm_community',1,'Existing Account_zh_CN','Existing Account','zh_CN','Áé∞ÊúâÂÆ¢Êà∑'),
('crm_community',1,'First Name_zh_CN','First Name','zh_CN','Âêç'),
('crm_community',1,'form_hints_en_US','form_hints','en_US','<i>Tasks and emails will be forwarded to \'admin\' user for approval. \nYou can change the settings <a href=\"/jw/web/console/app/crm_community/1/processes/process1\" target=\"_blank\">here</a><br/></i>'),
('crm_community',1,'form_hints_zh_CN','form_hints','zh_CN','<i>‰ªªÂä°ÂíåÁîµÂ≠êÈÇÆ‰ª∂Â∞ÜËΩ¨ÂèëÁªô‚ÄúÁÆ°ÁêÜÂëò‚ÄùÁî®Êà∑‰ª•‰æõÊâπÂáÜ„ÄÇ<br/>\nÊÇ®ÂèØ‰ª•Âú®<a href=\"/jw/web/console/app/crm_community/1/processes/process1\" target=\"_blank\">Ê≠§Â§Ñ</a> Êõ¥ÊîπËÆæÁΩÆ„ÄÇ</i>'),
('crm_community',1,'Full Name_zh_CN','Full Name','zh_CN','ÂßìÂêç'),
('crm_community',1,'Hidden_zh_CN','Hidden','zh_CN','ÈöêËóè'),
('crm_community',1,'Home_zh_CN','Home','zh_CN','È¶ñÈ°µ'),
('crm_community',1,'Hyperlink_zh_CN','Hyperlink','zh_CN','ËøûÁªì'),
('crm_community',1,'Indirect_zh_CN','Indirect','zh_CN','Èó¥Êé•'),
('crm_community',1,'International_zh_CN','International','zh_CN','ÂõΩÈôÖÁöÑ'),
('crm_community',1,'Last Name_zh_CN','Last Name','zh_CN','Âßì'),
('crm_community',1,'Local_zh_CN','Local','zh_CN','Êú¨Âú∞ÁöÑ'),
('crm_community',1,'Lost_zh_CN','Lost','zh_CN','Â§±Âéª'),
('crm_community',1,'New Account_zh_CN','New Account','zh_CN','Êñ∞ÂÆ¢Êà∑'),
('crm_community',1,'New Contact_zh_CN','New Contact','zh_CN','Êñ∞ËÅîÁ≥ª‰∫∫'),
('crm_community',1,'New Opportunity_zh_CN','New Opportunity','zh_CN','Êñ∞Êú∫‰ºö'),
('crm_community',1,'No_zh_CN','No','zh_CN','Âê¶'),
('crm_community',1,'Notes_zh_CN','Notes','zh_CN','ËØ¥Êòé'),
('crm_community',1,'Open_zh_CN','Open','zh_CN','ÂºÄÂßã'),
('crm_community',1,'Opportunities_zh_CN','Opportunities','zh_CN','Êú∫‰ºö'),
('crm_community',1,'Opportunity List_zh_CN','Opportunity List','zh_CN','Êú∫‰ºöÂàóË°®'),
('crm_community',1,'Opportunity_zh_CN','Opportunity','zh_CN','Êú∫‰ºö'),
('crm_community',1,'page_Proposals Inbox_header_en_US','page_Proposals Inbox_header','en_US','Reminder to administrator for email notification to work:<br/>\n<ul>\n\n<li>Input the all users users email address in <a href=\"/jw/web/console/directory/users\" target=\"_blank\">Setup Users</a>.</li>\n<li>Input your email SMTP credentials into the <a href=\"/jw/web/console/setting/general\" target=\"_blank\">General Settings</a>.</li>\n</ul>'),
('crm_community',1,'page_Proposals Inbox_header_zh_CN','page_Proposals Inbox_header','zh_CN','ÁªôJogetÁÆ°ÁêÜÂëò‰ΩøÁî®ÁîµÂ≠êÈÇÆ‰ª∂ÁöÑÊèêÁ§∫Ôºö<br/>\n<ul>\n\n<li>Âú® <a href=\"/jw/web/console/directory/users\" target=\"_blank\">Á≥ªÁªüËÆæÁΩÆ > ËÆæÁΩÆÁî®Êà∑</a> ‰∏≠ËæìÂÖ•ÊâÄÊúâÁî®Êà∑ÁöÑÁîµÂ≠êÈÇÆ‰ª∂Âú∞ÂùÄ„ÄÇ</li>\n<li>Âú® <a href=\"/jw/web/console/setting/general\" target=\"_blank\">Á≥ªÁªüËÆæÁΩÆ > Â∏∏ËßÑËÆæÁΩÆ</a> ‰∏≠ËæìÂÖ•ÊÇ®ÁöÑJoget SMTPÊúçÂä°Âô®ÈÖçÁΩÆ‰ª•ÂèëÈÄÅÁîµÂ≠êÈÇÆ‰ª∂„ÄÇ</li>\n</ ul>\n'),
('crm_community',1,'Photo_zh_CN','Photo','zh_CN','ÂõæÁâá‰∏ä‰º†'),
('crm_community',1,'Proposal Approval_zh_CN','Proposal Approval','zh_CN','ÂÆ°Êâπ'),
('crm_community',1,'Proposal Form_zh_CN','Proposal Form','zh_CN','Âª∫ËÆÆ‰π¶'),
('crm_community',1,'Proposal Process_zh_CN','Proposal Process','zh_CN','Âª∫ËÆÆ‰π¶ÊµÅÁ®ã'),
('crm_community',1,'Proposal Resubmission_zh_CN','Proposal Resubmission','zh_CN','ÈÄÄÂõûÁöÑÂª∫ËÆÆÂçï'),
('crm_community',1,'Proposal Resubmit_zh_CN','Proposal Resubmit','zh_CN','ÈáçÊñ∞Êèê‰∫§'),
('crm_community',1,'Reference No_zh_CN','Reference No','zh_CN','ÂèÇËÄÉÁºñÂè∑'),
('crm_community',1,'Rejected_zh_CN','Rejected','zh_CN','ÊãíÁªù'),
('crm_community',1,'Resubmit_zh_CN','Resubmit','zh_CN','ÈáçÊñ∞Êèê‰∫§'),
('crm_community',1,'Send Proposal_zh_CN','Send Proposal','zh_CN','ÂèëÈÄÅÂª∫ËÆÆ‰π¶'),
('crm_community',1,'Source_zh_CN','Source','zh_CN','Êù•Ê∫ê'),
('crm_community',1,'Stage_zh_CN','Stage','zh_CN','Èò∂ÊÆµ'),
('crm_community',1,'State_zh_CN','State','zh_CN','ÁúÅ‰ªΩ/Áõ¥ËæñÂ∏Ç'),
('crm_community',1,'Status_zh_CN','Status','zh_CN','Áä∂ÊÄÅ'),
('crm_community',1,'Submit New Proposal_zh_CN','Submit New Proposal','zh_CN','Êèê‰∫§Êñ∞Âª∫ËÆÆ‰π¶'),
('crm_community',1,'Task Inbox_zh_CN','Task Inbox','zh_CN','‰ªªÂä°Êî∂‰ª∂ÁÆ±'),
('crm_community',1,'Title_zh_CN','Title','zh_CN','ËÅå‰Ωç'),
('crm_community',1,'View All Proposals_zh_CN','View All Proposals','zh_CN','ÊâÄÊúâÂª∫ËÆÆ‰π¶'),
('crm_community',1,'View Proposal_zh_CN','View Proposal','zh_CN','ËßÇÁúãÂª∫ËÆÆ‰π¶'),
('crm_community',1,'View_zh_CN','View','zh_CN','ËßÇÁúã'),
('crm_community',1,'Welcome_zh_CN','Welcome','zh_CN','Ê¨¢Ëøé'),
('crm_community',1,'Won_zh_CN','Won','zh_CN','Ëµ¢Âæó'),
('crm_community',1,'Yes_zh_CN','Yes','zh_CN','ÊòØ');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package`
--

LOCK TABLES `app_package` WRITE;
/*!40000 ALTER TABLE `app_package` DISABLE KEYS */;
INSERT INTO `app_package` VALUES
('crm_community',1,'CRM Community','2023-11-01 13:38:45','2023-11-01 13:38:46','crm_community',1);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_activity_form`
--

LOCK TABLES `app_package_activity_form` WRITE;
/*!40000 ALTER TABLE `app_package_activity_form` DISABLE KEYS */;
INSERT INTO `app_package_activity_form` VALUES
('process1','activity1','crm_community',1,'process1::activity1','SINGLE','crm_proposal_resubmit_form',NULL,NULL,'','\0'),
('process1','approve_proposal','crm_community',1,'process1::approve_proposal','SINGLE','crm_proposal_approval_form',NULL,NULL,'','\0'),
('process1','runProcess','crm_community',1,'process1::runProcess','SINGLE','crm_proposal_form',NULL,NULL,'','\0'),
('process1','send_proposal','crm_community',1,'process1::send_proposal','SINGLE','crm_proposal_sending_form',NULL,NULL,'','\0');
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
  `pluginProperties` text DEFAULT NULL,
  PRIMARY KEY (`processDefId`,`activityDefId`,`packageId`,`packageVersion`),
  KEY `FKADE8644C5F255BCC` (`packageId`,`packageVersion`),
  CONSTRAINT `FKADE8644C5F255BCC` FOREIGN KEY (`packageId`, `packageVersion`) REFERENCES `app_package` (`packageId`, `packageVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_activity_plugin`
--

LOCK TABLES `app_package_activity_plugin` WRITE;
/*!40000 ALTER TABLE `app_package_activity_plugin` DISABLE KEYS */;
INSERT INTO `app_package_activity_plugin` VALUES
('process1','tool1','crm_community',1,'process1::tool1','org.joget.apps.app.lib.EmailTool','{\"cc\":\"\",\"toSpecific\":\"\",\"bcc\":\"\",\"icsAttachement\":\"\",\"subject\":\"Proposal Approved: #form.crm_proposal.title#\",\"formDefId\":\"\",\"retryCount\":\"\",\"toParticipantId\":\"requester\",\"message\":\"Proposal Approved\\n\\nRef No: #form.crm_proposal.refNo#\\nTitle: #form.crm_proposal.title#\",\"p12\":\"\",\"security\":\"\",\"password\":\"\",\"storepass\":\"\",\"port\":\"\",\"isHtml\":\"\",\"host\":\"\",\"files\":[],\"alias\":\"\",\"from\":\"\",\"retryInterval\":\"\",\"fields\":[],\"username\":\"\"}'),
('process1','tool2','crm_community',1,'process1::tool2','org.joget.apps.app.lib.EmailTool','{\"cc\":\"\",\"toSpecific\":\"\",\"bcc\":\"\",\"icsAttachement\":\"\",\"subject\":\"Proposal Rejected: #form.crm_proposal.title#\",\"formDefId\":\"\",\"toParticipantId\":\"requester\",\"message\":\"Proposal Rejected\\n\\nRef No: #form.crm_proposal.refNo#\\nTitle: #form.crm_proposal.title#\",\"security\":\"\",\"password\":\"\",\"port\":\"\",\"isHtml\":\"\",\"host\":\"\",\"files\":[],\"from\":\"\",\"fields\":[],\"username\":\"\"}');
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
  `value` text DEFAULT NULL,
  `pluginProperties` text DEFAULT NULL,
  PRIMARY KEY (`processDefId`,`participantId`,`packageId`,`packageVersion`),
  KEY `FK6D7BF59C5F255BCC` (`packageId`,`packageVersion`),
  CONSTRAINT `FK6D7BF59C5F255BCC` FOREIGN KEY (`packageId`, `packageVersion`) REFERENCES `app_package` (`packageId`, `packageVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_participant`
--

LOCK TABLES `app_package_participant` WRITE;
/*!40000 ALTER TABLE `app_package_participant` DISABLE KEYS */;
INSERT INTO `app_package_participant` VALUES
('process1','approver','crm_community',1,'process1::approver','requesterHod',NULL,NULL),
('process1','processStartWhiteList','crm_community',1,'process1::processStartWhiteList','role','loggedInUser',NULL),
('process1','requester','crm_community',1,'process1::requester','requester','runProcess',NULL);
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
  `pluginDescription` text DEFAULT NULL,
  `pluginProperties` text DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK7A835713462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FK7A835713462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `assignmentUsers` text DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `permissionProperties` longtext DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  CONSTRAINT `FK_nnvkg0h6yy8o3f4yjhd20ury0` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_resource`
--

LOCK TABLES `app_resource` WRITE;
/*!40000 ALTER TABLE `app_resource` DISABLE KEYS */;
INSERT INTO `app_resource` VALUES
('crm_community',1,'crm.png',32567,NULL,'{\"hashvariable\":\"#appResource.crm.png#\",\"permission\":{\"className\":\"\",\"properties\":{}}}');
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
  `description` text DEFAULT NULL,
  `json` longtext DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FKE411D54E462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FKE411D54E462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_userview`
--

LOCK TABLES `app_userview` WRITE;
/*!40000 ALTER TABLE `app_userview` DISABLE KEYS */;
INSERT INTO `app_userview` VALUES
('appcenter',1,'home','App Center',NULL,'{\"className\":\"org.joget.apps.userview.model.Userview\",\"categories\":[{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"cacheAllLinks\":\"\",\"customHeader\":\"\",\"style-custom\":\"min-height:50vh;\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{}},\"keyName\":\"\",\"customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"<i class=\\\"zmdi zmdi-apps\\\"><\\/i> Home\",\"customId\":\"home\",\"buttonPosition\":\"bottomLeft\",\"datalistId\":\"applist\",\"checkboxPosition\":\"left\",\"selectionType\":\"multiple\",\"userviewCacheDuration\":\"20\",\"cacheListAction\":\"\",\"userviewCacheScope\":\"\",\"id\":\"8C70B71371B942B6D48A7E9B4C1DB8D1\",\"rowCount\":\"\"}}],\"properties\":{\"hide\":\"\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{\"hide\":\"\",\"permissionDeny\":\"\"}},\"comment\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"id\":\"category-CB52FE21C5EE44FC35A4BDB7A4DCDF53\",\"label\":\"Home\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"userviewCacheDuration\":\"20\",\"userviewCacheScope\":\"\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{}},\"enableOffline\":\"\",\"id\":\"0497CD0F62BD44DEEAF1294D58166A1A\",\"label\":\"<i class=\\\"fas fa-pencil-ruler\\\"><\\/i> Design App\",\"customId\":\"design_app\",\"content\":\"<p>_<\\/p>\",\"style-display\":\"none\"}}],\"properties\":{\"hide\":\"\",\"permissionComment\":\"\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{}},\"comment\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"id\":\"category-FE6D0DD0AECE43CBDC2A86613544B6F8\",\"label\":\"New Category\",\"permissionDeny\":\"true\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"cacheAllLinks\":\"\",\"customHeader\":\"<h2><i class=\\\"far fa-file-archive\\\"><\\/i> Templates<\\/h2>\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{}},\"keyName\":\"\",\"customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"<i class=\\\"far fa-file-archive\\\"><\\/i> Templates\",\"customId\":\"templates\",\"buttonPosition\":\"bottomLeft\",\"datalistId\":\"list_appList\",\"checkboxPosition\":\"no\",\"selectionType\":\"multiple\",\"userviewCacheDuration\":\"20\",\"cacheListAction\":\"\",\"userviewCacheScope\":\"\",\"style-tablet-padding-left\":\"5px\",\"style-tablet-padding-right\":\"5px\",\"id\":\"95447DD6260B48B2E00C1354A179CD44\",\"rowCount\":\"\"}}],\"properties\":{\"hide\":\"\",\"permissionComment\":\"\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{}},\"comment\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"id\":\"category-B3C929DFC00D492F6EBA14DD622AFD2B\",\"label\":\"New Category\",\"permissionDeny\":\"true\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"userviewCacheDuration\":\"20\",\"userviewCacheScope\":\"\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{}},\"enableOffline\":\"\",\"id\":\"4E2A517C3275429806518BAA8D29AB1F\",\"label\":\"<i class=\\\"fas fa-digital-tachograph\\\"><\\/i> Administration\",\"customId\":\"admin\",\"content\":\"<p>_<\\/p>\",\"style-display\":\"none\"}}],\"properties\":{\"hide\":\"\",\"permissionComment\":\"\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{}},\"comment\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"id\":\"category-2779360D1CCF46282248D2F8D12758FE\",\"label\":\"New Category\",\"permissionDeny\":\"true\"}}],\"properties\":{\"logoutText\":\"Logout\",\"welcomeMessage\":\"Empowering Open Innovation\",\"name\":\"App Center\",\"description\":\"\",\"footerMessage\":\"#i18n.Powered by Joget#\",\"id\":\"home\"},\"setting\":{\"properties\":{\"__\":\"\",\"__\":\"\",\"userviewDescription\":\"\",\"userviewId\":\"home\",\"hideThisUserviewInAppCenter\":\"true\",\"permission_rules\":[{\"permission_key\":\"58F9FBEA76264BE037991759BC1645A2\",\"permission\":{\"className\":\"org.joget.plugin.enterprise.AdminUserviewPermission\",\"properties\":{}},\"permission_name\":\"Is Admin\"}],\"userview_thumbnail\":\"\",\"userview_category\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"theme\":{\"className\":\"org.joget.apps.userview.lib.AjaxUniversalTheme\",\"properties\":{\"dx8navBadge\":\"\",\"css\":\"<\\/style>\\n<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"#request.baseURL?html#/css/appCenter8.css?build=#platform.build?html#\\\" />\\n<style>\\n#dataList_applist > form > div > div.cards.row > div > div > h5 > div{\\n    box-shadow: rgba(17, 17, 26, 0.1) 0px 1px 0px;\\n    border-radius: 20%;\\n}\\n#home #dataList_applist .card-icon:hover .card-actions a{\\n    display:block !important;\\n}\",\"breadcrumb-style-display\":\"none\",\"footer-style-display\":\"none\",\"loginBackground\":\"\",\"disablePush\":\"\",\"dx8navLinkColor\":\"#7E8299\",\"removeAssignmentTitle\":\"\",\"shortcutLinkLabel\":\"Shortcut\",\"disableHelpGuide\":\"\",\"logo\":\"/jw/images/appCenterLogo.png\",\"enableResponsiveSwitch\":\"true\",\"dx8headerFontColor\":\"\",\"fav_icon\":\"\",\"dx8navLinkIcon\":\"#7E8299\",\"horizontal_menu\":\"\",\"profile\":\"\",\"homeAttractBanner\":\"\",\"dx8headingBgColor\":\"\",\"dx8linkColor\":\"#01A9F4\",\"homeUrl\":\"\",\"dx8linkActiveColor\":\"#007BFF\",\"disablePwa\":\"\",\"dx8footerColor\":\"\",\"subheader\":\"\",\"dx8contentbackground\":\"\",\"loginPageBottom\":\"<\\/div>\\n<\\/div>\\n<div class=\\\"d-flex flex-lg-row-fluid w-lg-50 bgi-size-cover bgi-position-center order-1 order-lg-2\\\" style=\\\"background-image: url(#request.contextPath#/images/appCenterLoginPageBackground.png);background-size: cover;\\\">\\n\\t\\t\\t\\t\\t<!--begin::Content-->\\n\\t\\t\\t\\t\\t<div class=\\\"d-flex flex-column flex-center py-7 py-lg-15 px-5 px-md-15 w-100\\\">\\n\\t\\t\\t\\t\\t\\t<!--begin::Logo-->\\n\\t\\t\\t\\t\\t\\t<img alt=\\\"Logo\\\" src=\\\"#request.contextPath#/images/appCenterLoginPageLogo.png\\\" class=\\\"h-60px h-lg-75px d-lg-block mx-auto w-275px w-md-50 w-xl-500px mb-10 mb-lg-20\\\">\\n\\t\\t\\t\\t\\t\\t<!--end::Logo-->\\n\\t\\t\\t\\t\\t\\t<!--begin::Image-->\\n\\t\\t\\t\\t\\t\\t<img class=\\\"d-none d-lg-block mx-auto w-275px w-md-50 w-xl-500px mb-10 mb-lg-20\\\" src=\\\"#request.contextPath#/images/appCenterLoginPageContent.png\\\" alt=\\\"\\\">\\n\\t\\t\\t\\t\\t\\t<!--end::Image-->\\n\\t\\t\\t\\t\\t\\t<!--begin::Title-->\\n\\t\\t\\t\\t\\t\\t<h1 class=\\\"d-none d-lg-block text-white fs-2qx fw-bolder text-center mb-7\\\">Empowering Open Innovation<\\/h1>\\n\\t\\t\\t\\t\\t\\t<!--end::Title-->\\n\\t\\t\\t\\t\\t\\t<!--begin::Text-->\\n\\t\\t\\t\\t\\t\\t<div class=\\\"d-none d-lg-block text-white fs-base text-center\\\"><strong>Joget DX<\\/strong> combines the best of <strong>process automation, workflow management<\\/strong> and <strong>low code application development<\\/strong> in a simple, flexible and open platform. Joget DX is a modern open source low-code application development platform to build enterprise web apps and automate workflows on the Cloud and Mobile<\\/div>\\n\\t\\t\\t\\t\\t\\t<!--end::Text-->\\n\\t\\t\\t\\t\\t<\\/div>\\n\\t\\t\\t\\t\\t<!--end::Content-->\\n\\t\\t\\t\\t<\\/div>\\n<\\/div>\\n<style>\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > img:nth-child(2){\\n  width: 550px;\\n    height: 350px;\\n    margin: 40px 0px;\\n    margin-bottom: 5.5rem !important;\\n-webkit-filter: drop-shadow(5px 5px 5px #222);\\n    filter: drop-shadow(5px 5px 5px #222);\\n}\\nbody#login #loginForm{\\n position: relative;\\n}\\nbody#login #page #content main{\\npadding-left: 0%;\\n    padding-right: 0%;\\nheight: 100%;\\n}\\n.w-lg-50 {\\n    width: 50%!important;\\n}\\n.d-flex {\\n    height: 100%;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > img.h-60px.h-lg-75px{\\n  width: 200px;\\n height: 55px;\\nmargin-top: 4rem!important;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > img:nth-child(3){\\nwidth: 540px!important;\\nmargin-bottom: 5.5rem!important;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div{\\n    padding-top: 3rem;\\n    padding-bottom: 3rem;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > h2{\\n padding-left: 20px;\\nmargin-bottom: 40px;\\n}\\n#loginForm > table{\\nwidth: 50%;\\n}\\nbody#login #loginForm table td input[type=\'submit\']{\\nwidth: 100%;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > h1{\\nmargin-bottom: 20px;\\n}\\nlabel{\\n    font-weight: 500;\\n}\\n#content > main > div > div.d-flex.flex-column.flex-lg-row-fluid.w-lg-50.p-10.order-2.order-lg-1 > div > div{\\n    position: relative;\\n    top: 23%;\\n}\\nbody#login #loginForm table td input[type=\'submit\'] {\\n    background-color: #32ae93 !important;\\n    font-size: 16px !important;\\n    font-weight: 500 !important;\\n}\\n#login #page{\\nmax-width: 100%;\\n}\\n\\n@media (max-width: 992px) {\\n.w-lg-50 {\\n    width: 100% !important;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2{\\nheight: 20% !important;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > img.h-60px.h-lg-75px{\\nmargin-top: 0rem !important;\\n}\\n}\\n<\\/style>\\n<script>\\n$( document ).ready(function() {\\n   $(\'#loginForm > table > tbody > tr:nth-child(3) > td:nth-child(2) > input\').val(\'Sign In\');\\n});\\n<\\/script>\",\"dx8backgroundImage\":\"\",\"dx8navBadgeText\":\"\",\"dx8colorSchema\":\"#f1f1f1;#FFFFFF;#8D9BCA;#1EA0CB;#253037;#19242b\",\"dx8background\":\"#F5F8FA\",\"dx8navScrollbarTrack\":\"\",\"dx8headerColor\":\"\",\"dx8colorScheme\":\"rgb(249, 250, 251);rgb(59, 67, 72);rgb(3, 169, 244);rgb(110, 206, 251);rgb(255, 255, 255);rgb(255, 255, 255)\",\"dx8navBackground\":\"#FFFFFF\",\"js\":\"<\\/script>\\n<script type=\\\"text/javascript\\\" src=\\\"#request.baseURL?html#/js/appCenter8.js?build=#platform.build?html#\\\"><\\/script>\\n<script>\",\"welcome-message-style-font-weight\":\"500\",\"dx8navActiveIconColor\":\"\",\"dx8navScrollbarThumb\":\"\",\"loginPageTop\":\"<div class=\\\"d-flex flex-column flex-lg-row flex-column-fluid\\\">\\n<div class=\\\"d-flex flex-column flex-lg-row-fluid w-lg-50 p-10 order-2 order-lg-1\\\">\\n<div class=\\\"d-flex flex-center flex-column flex-lg-row-fluid\\\">\",\"urlsToCache\":\"\",\"userMenu\":[],\"dx8buttonColor\":\"\",\"shortcut\":[],\"userImage\":\"\",\"fontControl\":\"\",\"dx8navActiveLinkBackground\":\"\",\"dx8fontColor\":\"\",\"dx8navLinkBackground\":\"\",\"welcome-message-style-font-size\":\"20px\",\"dx8buttonBackground\":\"\",\"dx8contentFontColor\":\"\",\"subfooter\":\"\",\"dx8footerBackground\":\"\",\"brand-name-style-display\":\"none\",\"dx8headingFontColor\":\"\",\"dx8primaryColor\":\"\",\"darkMode\":\"true\",\"dx8navActiveLinkColor\":\"#000000\",\"inbox\":\"all\"}},\"userviewName\":\"App Center\"}}}','2023-11-01 13:39:15','2023-11-01 13:39:15'),
('crm_community',1,'crm_userview_sales','CRM',NULL,'{\n\"categories\": [\n{\n\"className\": \"org.joget.apps.userview.model.UserviewCategory\",\n\"menus\": [{\n\"className\": \"org.joget.apps.userview.lib.HtmlPage\",\n\"properties\": {\n\"content\": \"<div id=\\\"left_content\\\">\\n<div style=\\\"margin-right: 10px;\\\">\\n<h4 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-size: inherit; line-height: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; font-weight: bold; color: #042c54;\\\">More Leads, More Sales, More Customers<\\/h4>\\n<h1 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font: inherit; vertical-align: baseline; color: #1f4282;\\\"><span style=\\\"font-size: large;\\\"><strong>Business&nbsp;Customer Relationship Management<\\/strong><\\/span><\\/h1>\\n<p style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\">CRM helps your business communicate with prospects, share sales information, close deals and keep customers happy.<img src=\\\"http://www.joget.org/images/demo/phone_pad.png\\\" alt=\\\"\\\" width=\\\"382\\\" height=\\\"302\\\"><\\/p>\\n<\\/div>\\n<\\/div>\",\n\"customId\": \"welcome\",\n\"enableOffline\": \"\",\n\"id\": \"welcome\",\n\"label\": \"Welcome\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n}],\n\"permission\": {\n\"className\": \"org.joget.apps.userview.model.UserviewPermission\",\n\"properties\": {}\n},\n\"properties\": {\n\"hide\": \"\",\n\"id\": \"category-9BE91A55FAAC4B5098841EA9E1994BE6\",\n\"label\": \"Home\",\n\"permission\": {\n\"className\": \"org.joget.plugin.enterprise.AnonymousUserviewPermission\",\n\"properties\": {}\n}\n}\n},\n{\n\"className\": \"org.joget.apps.userview.model.UserviewCategory\",\n\"menus\": [\n{\n\"className\": \"org.joget.apps.userview.lib.DataListMenu\",\n\"properties\": {\n\"buttonPosition\": \"topLeft\",\n\"cacheAllLinks\": \"\",\n\"cacheListAction\": \"\",\n\"checkboxPosition\": \"left\",\n\"customFooter\": \"\",\n\"customHeader\": \"<h2>#i18n.Account List#<\\/h2>\",\n\"customId\": \"account_list\",\n\"datalistId\": \"crm_account_list\",\n\"enableOffline\": \"\",\n\"id\": \"384344BD3E2946D097C6F5F17540C377\",\n\"keyName\": \"\",\n\"label\": \"Account List\",\n\"rowCount\": \"true\",\n\"selectionType\": \"multiple\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n},\n{\n\"className\": \"org.joget.apps.userview.lib.FormMenu\",\n\"properties\": {\n\"cancelButtonLabel\": \"\",\n\"customFooter\": \"\",\n\"customHeader\": \"\",\n\"customId\": \"account_new\",\n\"fieldPassover\": \"\",\n\"fieldPassoverMethod\": \"append\",\n\"formId\": \"crm_account\",\n\"id\": \"account_form\",\n\"keyName\": \"\",\n\"label\": \"New Account\",\n\"loadDataWithKey\": \"\",\n\"messageShowAfterComplete\": \"\",\n\"paramName\": \"\",\n\"readonly\": \"\",\n\"readonlyLabel\": \"\",\n\"redirectTargetOnCancel\": \"top\",\n\"redirectUrlAfterComplete\": \"account_list\",\n\"redirectUrlOnCancel\": \"account_list\",\n\"showInPopupDialog\": \"\",\n\"submitButtonLabel\": \"\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n}\n],\n\"permission\": {\n\"className\": \"org.joget.apps.userview.model.UserviewPermission\",\n\"properties\": {}\n},\n\"properties\": {\n\"hide\": \"\",\n\"id\": \"category-7650DEEFC4CC4332AC25871B65BBDD48\",\n\"label\": \"Accounts\",\n\"permission\": {\n\"className\": \"org.joget.apps.userview.lib.LoggedInUserPermission\",\n\"properties\": {}\n}\n}\n},\n{\n\"className\": \"org.joget.apps.userview.model.UserviewCategory\",\n\"menus\": [\n{\n\"className\": \"org.joget.apps.userview.lib.DataListMenu\",\n\"properties\": {\n\"buttonPosition\": \"topLeft\",\n\"cacheAllLinks\": \"\",\n\"cacheListAction\": \"\",\n\"checkboxPosition\": \"left\",\n\"customFooter\": \"\",\n\"customHeader\": \"<h2>#i18n.Contact List#<\\/h2>\",\n\"customId\": \"contact_list\",\n\"datalistId\": \"crm_contact_list\",\n\"enableOffline\": \"\",\n\"id\": \"D86B740C970C4B08B4D5CCD3DC0E9503\",\n\"keyName\": \"\",\n\"label\": \"Contact List\",\n\"rowCount\": \"true\",\n\"selectionType\": \"multiple\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n},\n{\n\"className\": \"org.joget.apps.userview.lib.FormMenu\",\n\"properties\": {\n\"cancelButtonLabel\": \"\",\n\"customFooter\": \"\",\n\"customHeader\": \"\",\n\"customId\": \"contact_new\",\n\"fieldPassover\": \"\",\n\"fieldPassoverMethod\": \"append\",\n\"formId\": \"crm_contact\",\n\"id\": \"contact-form\",\n\"keyName\": \"\",\n\"label\": \"New Contact\",\n\"loadDataWithKey\": \"\",\n\"messageShowAfterComplete\": \"\",\n\"paramName\": \"\",\n\"readonly\": \"\",\n\"readonlyLabel\": \"\",\n\"redirectTargetOnCancel\": \"top\",\n\"redirectUrlAfterComplete\": \"contact_list\",\n\"redirectUrlOnCancel\": \"contact_list\",\n\"showInPopupDialog\": \"\",\n\"submitButtonLabel\": \"\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n}\n],\n\"permission\": {\n\"className\": \"org.joget.apps.userview.model.UserviewPermission\",\n\"properties\": {}\n},\n\"properties\": {\n\"hide\": \"\",\n\"id\": \"category-E77D2050680D4DB0A85A5C0C3AC1C083\",\n\"label\": \"Contacts\",\n\"permission\": {\n\"className\": \"org.joget.apps.userview.lib.LoggedInUserPermission\",\n\"properties\": {}\n}\n}\n},\n{\n\"className\": \"org.joget.apps.userview.model.UserviewCategory\",\n\"menus\": [\n{\n\"className\": \"org.joget.apps.userview.lib.DataListMenu\",\n\"properties\": {\n\"buttonPosition\": \"topLeft\",\n\"cacheAllLinks\": \"\",\n\"cacheListAction\": \"\",\n\"checkboxPosition\": \"left\",\n\"customFooter\": \"\",\n\"customHeader\": \"<h2>#i18n.Opportunity List#<\\/h2>\",\n\"customId\": \"opportunity_list\",\n\"datalistId\": \"crm_opportunity_list\",\n\"enableOffline\": \"\",\n\"id\": \"A074397ABEA94CF78E2E8FA0843AB97B\",\n\"keyName\": \"\",\n\"label\": \"Opportunity List\",\n\"rowCount\": \"true\",\n\"selectionType\": \"multiple\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n},\n{\n\"className\": \"org.joget.apps.userview.lib.FormMenu\",\n\"properties\": {\n\"cancelButtonLabel\": \"\",\n\"customFooter\": \"\",\n\"customHeader\": \"\",\n\"customId\": \"opportunity_new\",\n\"fieldPassover\": \"\",\n\"fieldPassoverMethod\": \"append\",\n\"formId\": \"crm_opportunity\",\n\"id\": \"0C7E36768A2F46BB945CEC50E62E0BE8\",\n\"keyName\": \"\",\n\"label\": \"New Opportunity\",\n\"loadDataWithKey\": \"\",\n\"messageShowAfterComplete\": \"\",\n\"paramName\": \"\",\n\"readonly\": \"\",\n\"readonlyLabel\": \"\",\n\"redirectTargetOnCancel\": \"top\",\n\"redirectUrlAfterComplete\": \"opportunity_list\",\n\"redirectUrlOnCancel\": \"opportunity_list\",\n\"showInPopupDialog\": \"\",\n\"submitButtonLabel\": \"\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n}\n],\n\"permission\": {\n\"className\": \"org.joget.apps.userview.model.UserviewPermission\",\n\"properties\": {}\n},\n\"properties\": {\n\"hide\": \"\",\n\"id\": \"category-A12DBDB14B4447A984E6095B77F28B42\",\n\"label\": \"Opportunities\",\n\"permission\": {\n\"className\": \"org.joget.apps.userview.lib.LoggedInUserPermission\",\n\"properties\": {}\n}\n}\n},\n{\n\"className\": \"org.joget.apps.userview.model.UserviewCategory\",\n\"menus\": [\n{\n\"className\": \"org.joget.apps.userview.lib.DataListMenu\",\n\"properties\": {\n\"buttonPosition\": \"topLeft\",\n\"cacheAllLinks\": \"\",\n\"cacheListAction\": \"\",\n\"checkboxPosition\": \"left\",\n\"customFooter\": \"\",\n\"customHeader\": \"<h2>#i18n.View All Proposals#<\\/h2>\",\n\"customId\": \"view_all_proposal\",\n\"datalistId\": \"Proposal\",\n\"enableOffline\": \"\",\n\"id\": \"9E98D32002434ABFAABA3649DCA300F5\",\n\"keyName\": \"\",\n\"label\": \"View All Proposals\",\n\"rowCount\": \"true\",\n\"selectionType\": \"multiple\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n},\n{\n\"className\": \"org.joget.apps.userview.lib.InboxMenu\",\n\"properties\": {\n\"appFilter\": \"process\",\n\"assignment-customFooter\": \"\",\n\"assignment-customHeader\": \"\",\n\"buttonPosition\": \"topLeft\",\n\"cacheAllLinks\": \"\",\n\"customId\": \"workflow_inbox\",\n\"enableOffline\": \"\",\n\"id\": \"AA1445B29D904408B3F2B1B36E469E16\",\n\"label\": \"Task Inbox\",\n\"list-customFooter\": \"\",\n\"list-customHeader\": \"<h2>#i18n.Task Inbox#<\\/h2>\\r\\n#i18n.page_Proposals Inbox_header#\",\n\"processId\": \"process1\",\n\"rowCount\": \"true\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n},\n{\n\"className\": \"org.joget.apps.userview.lib.RunProcess\",\n\"properties\": {\n\"customId\": \"proposal_process\",\n\"fieldPassover\": \"\",\n\"fieldPassoverMethod\": \"append\",\n\"id\": \"2D27B3875F234315A7A3562BD0E35AB2\",\n\"keyName\": \"\",\n\"label\": \"Submit New Proposal\",\n\"messageShowAfterComplete\": \"\",\n\"paramName\": \"\",\n\"processDefId\": \"process1\",\n\"redirectUrlAfterComplete\": \"view_all_proposal\",\n\"runProcessDirectly\": \"Yes\",\n\"runProcessSubmitLabel\": \"\",\n\"showInPopupDialog\": \"\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n}\n],\n\"properties\": {\n\"hide\": \"\",\n\"id\": \"category-78EC0B8A1E8E483A93770714BB0D6F6E\",\n\"label\": \"Proposal Process\",\n\"permission\": {\n\"className\": \"org.joget.apps.userview.lib.LoggedInUserPermission\",\n\"properties\": {}\n}\n}\n},\n{\n\"className\": \"org.joget.apps.userview.model.UserviewCategory\",\n\"menus\": [{\n\"className\": \"org.joget.apps.userview.lib.HtmlPage\",\n\"properties\": {\n\"content\": \"<div id=\\\"left_content\\\">\\n<div style=\\\"float: left; width: 400px; margin-right: 10px;\\\">#i18n.aboutpage#<\\/div>\\n<\\/div>\",\n\"customId\": \"about\",\n\"enableOffline\": \"\",\n\"id\": \"765269E3926049B0A21D16581EE188DF\",\n\"label\": \"About\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n}],\n\"properties\": {\n\"comment\": \"\",\n\"hide\": \"\",\n\"id\": \"category-8739F2859D894A339404C2404CB9004E\",\n\"label\": \"a\",\n\"permission\": {\n\"className\": \"org.joget.apps.userview.lib.LoggedInUserPermission\",\n\"properties\": {}\n}\n}\n},\n{\n\"className\": \"org.joget.apps.userview.model.UserviewCategory\",\n\"menus\": [{\n\"className\": \"org.joget.apps.userview.lib.FormMenu\",\n\"properties\": {\n\"cancelButtonLabel\": \"\",\n\"customFooter\": \"\",\n\"customHeader\": \"\",\n\"customId\": \"ViewProposal\",\n\"fieldPassover\": \"\",\n\"fieldPassoverMethod\": \"append\",\n\"formId\": \"crm_proposal_approval_form\",\n\"id\": \"1A2E6106918040F484C342E1BB12B2A3\",\n\"keyName\": \"\",\n\"label\": \"View Proposal\",\n\"loadDataWithKey\": \"\",\n\"messageShowAfterComplete\": \"\",\n\"paramName\": \"\",\n\"readonly\": \"Yes\",\n\"readonlyLabel\": \"\",\n\"redirectTargetOnCancel\": \"top\",\n\"redirectUrlAfterComplete\": \"view_all_proposal\",\n\"redirectUrlOnCancel\": \"view_all_proposal\",\n\"showInPopupDialog\": \"\",\n\"submitButtonLabel\": \"\",\n\"userviewCacheDuration\": \"20\",\n\"userviewCacheScope\": \"\"\n}\n}],\n\"properties\": {\n\"comment\": \"\",\n\"hide\": \"yes\",\n\"id\": \"category-1AFAC018AFA848F2970403061E49EE72\",\n\"label\": \"Hidden\",\n\"permission\": {\n\"className\": \"org.joget.apps.userview.lib.LoggedInUserPermission\",\n\"properties\": {}\n}\n}\n}\n],\n\"className\": \"org.joget.apps.userview.model.Userview\",\n\"layout\": {\n\"className\": \"org.joget.apps.userview.model.UserviewLayout\",\n\"properties\": {\n\"afterMenu\": \"\",\n\"beforeMenu\": \"\",\n\"customFooter\": \"\",\n\"customHeader\": \"\",\n\"theme\": {\n\"className\": \"org.joget.apps.userview.lib.DefaultTheme\",\n\"properties\": {\n\"css\": \"\",\n\"js\": \"\"\n}\n}\n}\n},\n\"properties\": {\n\"description\": \"\",\n\"footerMessage\": \"Powered by Joget\",\n\"id\": \"crm_userview_sales\",\n\"logoutText\": \"Logout\",\n\"name\": \"#i18n.applicationName#\",\n\"welcomeMessage\": \"Sales Force Automation\"\n},\n\"setting\": {\n\"className\": \"org.joget.apps.userview.model.UserviewSetting\",\n\"properties\": {\n\"__\": \"\",\n\"hideThisUserviewInAppCenter\": \"\",\n\"permission\": {\n\"className\": \"\",\n\"properties\": {}\n},\n\"__\": \"\",\n\"theme\": {\n\"className\": \"org.joget.plugin.enterprise.Dx8ColorAdminTheme\",\n\"properties\": {\n\"accentColor\": \"BLUE\",\n\"buttonColor\": \"GREY\",\n\"buttonTextColor\": \"WHITE\",\n\"css\": \"\",\n\"disableHelpGuide\": \"\",\n\"disablePush\": \"\",\n\"disablePwa\": \"\",\n\"displayCategoryLabel\": \"\",\n\"dx8background\": \"\",\n\"dx8backgroundImage\": \"\",\n\"dx8buttonBackground\": \"\",\n\"dx8buttonColor\": \"\",\n\"dx8colorScheme\": \"rgb(233, 233, 233);rgb(255, 255, 255);rgb(157, 235, 249);rgb(0, 114, 82);rgb(0, 131, 143);rgb(0, 86, 98)\",\n\"dx8contentFontColor\": \"\",\n\"dx8contentbackground\": \"\",\n\"dx8fontColor\": \"\",\n\"dx8footerBackground\": \"\",\n\"dx8footerColor\": \"\",\n\"dx8headerColor\": \"\",\n\"dx8headerFontColor\": \"\",\n\"dx8headingBgColor\": \"\",\n\"dx8headingFontColor\": \"\",\n\"dx8linkActiveColor\": \"\",\n\"dx8linkColor\": \"\",\n\"dx8navActiveIconColor\": \"\",\n\"dx8navActiveLinkBackground\": \"\",\n\"dx8navActiveLinkColor\": \"\",\n\"dx8navBackground\": \"\",\n\"dx8navBadge\": \"\",\n\"dx8navBadgeText\": \"\",\n\"dx8navLinkBackground\": \"\",\n\"dx8navLinkColor\": \"\",\n\"dx8navLinkIcon\": \"\",\n\"dx8navScrollbarThumb\": \"\",\n\"dx8primaryColor\": \"\",\n\"enableResponsiveSwitch\": \"true\",\n\"fav_icon\": \"\",\n\"fontColor\": \"WHITE\",\n\"fontControl\": \"\",\n\"homeAttractBanner\": \"\",\n\"homeUrl\": \"\",\n\"horizontal_menu\": \"\",\n\"inbox\": \"current\",\n\"js\": \"\",\n\"loginBackground\": \"\",\n\"loginPageBottom\": \"\",\n\"loginPageTop\": \"\",\n\"logo\": \"\",\n\"menuFontColor\": \"BLACK\",\n\"primaryColor\": \"DARKROYALBLUE\",\n\"profile\": \"\",\n\"removeAssignmentTitle\": \"\",\n\"shortcut\": [],\n\"shortcutLinkLabel\": \"Shortcut\",\n\"subfooter\": \"\",\n\"subheader\": \"\",\n\"themeScheme\": \"light\",\n\"urlsToCache\": \"\",\n\"userImage\": \"\",\n\"userMenu\": []\n}\n},\n\"userviewDescription\": \"\",\n\"userviewId\": \"crm_userview_sales\",\n\"userviewName\": \"CRM\",\n\"userview_category\": \"\",\n\"userview_thumbnail\": \"#appResource.crm.png#\"\n}\n}\n}','2023-11-01 13:38:43','2023-11-01 13:38:43');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_department`
--

LOCK TABLES `dir_department` WRITE;
/*!40000 ALTER TABLE `dir_department` DISABLE KEYS */;
INSERT INTO `dir_department` VALUES
('D-001','CEO Office','','ORG-001','4028808127f4ef840127f5efdbfb004f',NULL),
('D-002','Human Resource and Admin','','ORG-001','4028808127f4ef840127f5f41d4b0091',NULL),
('D-003','Finance','','ORG-001','4028808127f4ef840127f606242400b3',NULL),
('D-004','Marketing','','ORG-001','4028808127f4ef840127f5f20f36007a',NULL),
('D-005','Product Development','','ORG-001','4028808127f4ef840127f5f04dc2005a',NULL),
('D-006','Training and Consulting','','ORG-001','4028808127f4ef840127f5f7c5b500a5',NULL),
('D-007','Support and Services','','ORG-001','4028808127fb4d350127ff78d63300d1',NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_employment`
--

LOCK TABLES `dir_employment` WRITE;
/*!40000 ALTER TABLE `dir_employment` DISABLE KEYS */;
INSERT INTO `dir_employment` VALUES
('4028808127f4ef840127f5efdbfb004f','terry',NULL,NULL,NULL,NULL,'G-001','D-001','ORG-001'),
('4028808127f4ef840127f5f04dc2005a','clark',NULL,NULL,NULL,NULL,'G-002','D-005','ORG-001'),
('4028808127f4ef840127f5f11cf60068','cat',NULL,NULL,NULL,NULL,'G-003','D-005','ORG-001'),
('4028808127f4ef840127f5f194e20071','tana',NULL,NULL,NULL,NULL,'G-003','D-005','ORG-001'),
('4028808127f4ef840127f5f20f36007a','roy',NULL,NULL,NULL,NULL,'G-002','D-004','ORG-001'),
('4028808127f4ef840127f5f319720088','etta',NULL,NULL,NULL,NULL,'G-003','D-004','ORG-001'),
('4028808127f4ef840127f5f41d4b0091','sasha',NULL,NULL,NULL,NULL,'G-002','D-002','ORG-001'),
('4028808127f4ef840127f5f7c5b500a5','jack',NULL,NULL,NULL,NULL,'G-002','D-006','ORG-001'),
('4028808127f4ef840127f606242400b3','tina',NULL,NULL,NULL,NULL,'G-002','D-003','ORG-001'),
('4028808127fb4d350127ff78d63300d1','david',NULL,NULL,NULL,NULL,'G-002','D-007','ORG-001'),
('4028808127fb4d350127ff84074600f2','julia',NULL,NULL,NULL,NULL,'G-003','D-002','ORG-001');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_employment_report_to`
--

LOCK TABLES `dir_employment_report_to` WRITE;
/*!40000 ALTER TABLE `dir_employment_report_to` DISABLE KEYS */;
INSERT INTO `dir_employment_report_to` VALUES
('4028808127f4ef840127f5f04dc2005a','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f5f04e9b005f'),
('4028808127f4ef840127f5f20f36007a','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f5f20fb7007f'),
('4028808127f4ef840127f5f41d4b0091','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f5f48eda009e'),
('4028808127f4ef840127f5f7c5b500a5','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f5f7c60b00aa'),
('4028808127f4ef840127f606242400b3','4028808127f4ef840127f5efdbfb004f','4028808127f4ef840127f60624c100b8'),
('4028808127fb4d350127ff78d63300d1','4028808127f4ef840127f5efdbfb004f','4028808127fb4d350127ff78d6fe00d6');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_grade`
--

LOCK TABLES `dir_grade` WRITE;
/*!40000 ALTER TABLE `dir_grade` DISABLE KEYS */;
INSERT INTO `dir_grade` VALUES
('G-001','Board Members','','ORG-001'),
('G-002','Managers','','ORG-001'),
('G-003','Executives','','ORG-001');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_group`
--

LOCK TABLES `dir_group` WRITE;
/*!40000 ALTER TABLE `dir_group` DISABLE KEYS */;
INSERT INTO `dir_group` VALUES
('G-001','Managers','',NULL),
('G-002','CxO','',NULL),
('G-003','hrAdmin','',NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_organization`
--

LOCK TABLES `dir_organization` WRITE;
/*!40000 ALTER TABLE `dir_organization` DISABLE KEYS */;
INSERT INTO `dir_organization` VALUES
('ORG-001','Joget.Org','',NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_role`
--

LOCK TABLES `dir_role` WRITE;
/*!40000 ALTER TABLE `dir_role` DISABLE KEYS */;
INSERT INTO `dir_role` VALUES
('ROLE_ADMIN','Admin','Administrator'),
('ROLE_USER','User','Normal User');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user`
--

LOCK TABLES `dir_user` WRITE;
/*!40000 ALTER TABLE `dir_user` DISABLE KEYS */;
INSERT INTO `dir_user` VALUES
('admin','admin','21232f297a57a5a743894a0e4a801fc3','Admin','Admin',NULL,1,'0',NULL),
('cat','cat','5f4dcc3b5aa765d61d8327deb882cf99','Cat','Grant','',1,'',NULL),
('clark','clark','5f4dcc3b5aa765d61d8327deb882cf99','Clark','Kent','',1,'',NULL),
('david','david','5f4dcc3b5aa765d61d8327deb882cf99','David','Cain','',1,'',NULL),
('etta','etta','5f4dcc3b5aa765d61d8327deb882cf99','Etta','Candy','',1,'',NULL),
('jack','jack','5f4dcc3b5aa765d61d8327deb882cf99','Jack','Drake','',1,'',NULL),
('julia','julia','5f4dcc3b5aa765d61d8327deb882cf99','Julia','Kapatelis','',1,'',NULL),
('roy','roy','5f4dcc3b5aa765d61d8327deb882cf99','Roy','Harper','',1,'',NULL),
('sasha','sasha','5f4dcc3b5aa765d61d8327deb882cf99','Sasha','Bordeaux','',1,'',NULL),
('tana','tana','5f4dcc3b5aa765d61d8327deb882cf99','Tana','Moon','',1,'',NULL),
('terry','terry','5f4dcc3b5aa765d61d8327deb882cf99','Terry','Berg','',1,'',NULL),
('tina','tina','5f4dcc3b5aa765d61d8327deb882cf99','Tina','Magee','',1,'',NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_group`
--

LOCK TABLES `dir_user_group` WRITE;
/*!40000 ALTER TABLE `dir_user_group` DISABLE KEYS */;
INSERT INTO `dir_user_group` VALUES
('G-001','clark'),
('G-001','david'),
('G-001','jack'),
('G-003','julia'),
('G-001','roy'),
('G-001','sasha'),
('G-003','sasha'),
('G-002','terry'),
('G-001','tina');
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
  `meta_value` longtext DEFAULT NULL,
  PRIMARY KEY (`username`,`meta_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `appId` varchar(4000) DEFAULT NULL,
  `processIds` varchar(4000) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_replacement_user` (`replacementUser`),
  KEY `idx_start` (`startDate`),
  KEY `idx_end` (`endDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_role`
--

LOCK TABLES `dir_user_role` WRITE;
/*!40000 ALTER TABLE `dir_user_role` DISABLE KEYS */;
INSERT INTO `dir_user_role` VALUES
('ROLE_ADMIN','admin'),
('ROLE_USER','cat'),
('ROLE_USER','clark'),
('ROLE_USER','david'),
('ROLE_USER','etta'),
('ROLE_USER','jack'),
('ROLE_USER','julia'),
('ROLE_USER','roy'),
('ROLE_USER','sasha'),
('ROLE_USER','tana'),
('ROLE_USER','terry'),
('ROLE_USER','tina');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `objectid`
--

LOCK TABLES `objectid` WRITE;
/*!40000 ALTER TABLE `objectid` DISABLE KEYS */;
INSERT INTO `objectid` VALUES
(1000400);
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
  `message` text DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `appVersion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_audit_trail`
--

LOCK TABLES `wf_audit_trail` WRITE;
/*!40000 ALTER TABLE `wf_audit_trail` DISABLE KEYS */;
INSERT INTO `wf_audit_trail` VALUES
('ff8080818b8963bb018b896403700000','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=false}','2023-11-01 13:38:34',NULL,NULL),
('ff8080818b8963bb018b8964039e0001','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:35',NULL,NULL),
('ff8080818b8963bb018b89641d600002','roleSystem','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_account\", appId:\"crm_community\", appVersion:\"1\", name:\"Account Form\", dateCreated:\"Wed Nov 01 13:38:34 MYT 2023\", dateModified:\"Wed Nov 01 13:38:34 MYT 2023\"}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641d820003','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641d950004','roleSystem','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_contact\", appId:\"crm_community\", appVersion:\"1\", name:\"Contact Form\", dateCreated:\"Wed Nov 01 13:38:41 MYT 2023\", dateModified:\"Wed Nov 01 13:38:41 MYT 2023\"}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641d9e0005','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641dba0006','roleSystem','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_opportunity\", appId:\"crm_community\", appVersion:\"1\", name:\"Opportunity Form\", dateCreated:\"Wed Nov 01 13:38:41 MYT 2023\", dateModified:\"Wed Nov 01 13:38:41 MYT 2023\"}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641dc40007','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641dd10008','roleSystem','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_proposal_approval_form\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal Approval Form\", dateCreated:\"Wed Nov 01 13:38:41 MYT 2023\", dateModified:\"Wed Nov 01 13:38:41 MYT 2023\"}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641dd70009','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641de6000a','roleSystem','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_proposal_form\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal Form\", dateCreated:\"Wed Nov 01 13:38:41 MYT 2023\", dateModified:\"Wed Nov 01 13:38:41 MYT 2023\"}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641df0000b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641dfa000c','roleSystem','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_proposal_resubmit_form\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal Resubmit Form\", dateCreated:\"Wed Nov 01 13:38:41 MYT 2023\", dateModified:\"Wed Nov 01 13:38:41 MYT 2023\"}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641e04000d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89641e10000e','roleSystem','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_proposal_sending_form\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal Sending Form\", dateCreated:\"Wed Nov 01 13:38:41 MYT 2023\", dateModified:\"Wed Nov 01 13:38:41 MYT 2023\"}','2023-11-01 13:38:41',NULL,NULL),
('ff8080818b8963bb018b89642445000f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964245a0010','roleSystem','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"crm_account_list\", appId:\"crm_community\", appVersion:\"1\", name:\"Account Listing\", dateCreated:\"Wed Nov 01 13:38:43 MYT 2023\", dateModified:\"Wed Nov 01 13:38:43 MYT 2023\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424640011','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424710012','roleSystem','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"crm_contact_list\", appId:\"crm_community\", appVersion:\"1\", name:\"Contact List\", dateCreated:\"Wed Nov 01 13:38:43 MYT 2023\", dateModified:\"Wed Nov 01 13:38:43 MYT 2023\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424800013','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424920014','roleSystem','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"crm_opportunity_list\", appId:\"crm_community\", appVersion:\"1\", name:\"Opportunity List\", dateCreated:\"Wed Nov 01 13:38:43 MYT 2023\", dateModified:\"Wed Nov 01 13:38:43 MYT 2023\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424990015','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424aa0016','roleSystem','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"Proposal\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal List\", dateCreated:\"Wed Nov 01 13:38:43 MYT 2023\", dateModified:\"Wed Nov 01 13:38:43 MYT 2023\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424bd0017','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424d70018','roleSystem','org.joget.apps.app.dao.UserviewDefinitionDaoImpl','add','{id:\"crm_userview_sales\", appId:\"crm_community\", appVersion:\"1\", name:\"CRM\", dateCreated:\"Wed Nov 01 13:38:43 MYT 2023\", dateModified:\"Wed Nov 01 13:38:43 MYT 2023\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424e30019','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424ec001a','roleSystem','org.joget.apps.app.dao.EnvironmentVariableDaoImpl','add','{id:\"AppName\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424f7001b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896424f9001c','roleSystem','org.joget.apps.app.dao.EnvironmentVariableDaoImpl','add','{id:\"refNo\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642514001d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642519001e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"About_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642521001f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425220020','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"aboutpage_en_US\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964252b0021','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964252f0022','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"aboutpage_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964253a0023','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964253e0024','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Account Details_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425450025','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425470026','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Account ID_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425510027','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425580028','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Account List_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964255d0029','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642560002a','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Account Name_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642566002b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964256c002c','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Account_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642575002d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642576002e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Accounts_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642579002f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964257a0030','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Add Account_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425820031','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425830032','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Add Contact_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425870033','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425880034','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Add Opportunity_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964258c0035','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964258d0036','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Address Available_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425950037','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425960038','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Address Details_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964259a0039','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964259c003a','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Address_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425a0003b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425a3003c','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Amount_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425ad003d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425af003e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"applicationName_en_US\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425b3003f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425b60040','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"applicationName_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425c30041','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425c50042','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Approved_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425c90043','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425cd0044','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Approver Action_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425d10045','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425d30046','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Approver Comments_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425d90047','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425dc0048','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Attachment_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425e10049','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425e2004a','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"City_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425e6004b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425e8004c','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Contact Details_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425ef004d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425f2004e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Contact List_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425f9004f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425fa0050','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Contacts_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896425ff0051','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426050052','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Country_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426120053','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426150054','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Date Modified_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964261e0055','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426220056','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Delete Row_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426260057','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426280058','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Delete_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964262c0059','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642632005a','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Description_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642636005b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964263a005c','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Direct_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964263d005d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642640005e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Existing Account_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642645005f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426480060','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"First Name_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964264d0061','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426500062','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"form_hints_en_US\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426540063','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426560064','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"form_hints_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964265e0065','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426660066','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Full Name_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964266c0067','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964266f0068','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Hidden_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426710069','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642673006a','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Home_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b89642678006b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964267a006c','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Hyperlink_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964267c006d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964267d006e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Indirect_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b8964268d006f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426930070','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"International_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426980071','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426990072','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Last Name_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:43',NULL,NULL),
('ff8080818b8963bb018b896426a00073','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426a30074','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Local_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426a80075','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426a90076','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Lost_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426ae0077','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426b00078','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"New Account_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426b40079','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426b5007a','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"New Contact_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426bc007b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426c1007c','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"New Opportunity_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426cc007d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426cf007e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"No_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426da007f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426dd0080','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Notes_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426e10081','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426e30082','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Open_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426e90083','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426eb0084','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Opportunities_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426ef0085','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426f10086','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Opportunity List_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426f60087','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426f70088','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Opportunity_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426fc0089','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896426fe008a','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"page_Proposals Inbox_header_en_US\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642701008b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642702008c','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"page_Proposals Inbox_header_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642705008d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642706008e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Photo_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642709008f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964270a0090','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Proposal Approval_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964270c0091','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964270d0092','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Proposal Form_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427100093','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427110094','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Proposal Process_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427190095','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964271a0096','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Proposal Resubmission_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964271e0097','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427210098','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Proposal Resubmit_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964272d0099','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642735009a','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Reference No_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964273d009b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964273f009c','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Rejected_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642743009d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642745009e','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Resubmit_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642749009f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964274a00a0','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Send Proposal_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964275000a1','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964275500a2','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Source_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964275b00a3','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964275e00a4','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Stage_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964276800a5','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964276b00a6','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"State_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964277300a7','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964277900a8','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Status_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964278000a9','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964278700aa','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Submit New Proposal_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964279500ab','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964279700ac','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Task Inbox_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964279c00ad','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b8964279e00ae','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Title_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427a400af','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427a600b0','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"View All Proposals_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427aa00b1','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427ab00b2','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"View Proposal_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427b000b3','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427b300b4','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"View_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427b800b5','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427ba00b6','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Welcome_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427bc00b7','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427bd00b8','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Won_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427c800b9','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427cb00ba','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Yes_zh_CN\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427da00bb','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b896427e000bc','roleSystem','org.joget.apps.app.dao.AppResourceDaoImpl','add','{id:\"crm.png\", appId:\"crm_community\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:38:44',NULL,NULL),
('ff8080818b8963bb018b89642b3700bd','roleSystem','org.joget.workflow.model.service.WorkflowManagerImpl','processUpload','','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642d4700be','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642da500bf','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642db100c0','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642db400c1','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm_community','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e1300c2','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e1500c3','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm_community','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e2200c4','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e2400c5','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm_community','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e3700c6','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e4100c7','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm_community','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e4b00c8','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e4d00c9','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm_community','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e5b00ca','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e6800cb','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e6b00cc','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','crm_community','2023-11-01 13:38:45','crm_community','1'),
('ff8080818b8963bb018b89642e7300cd','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642e7c00ce','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642e7f00cf','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','crm_community','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642e9800d0','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642ea300d1','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642ea700d2','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm_community','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642eb300d3','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642eb900d4','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642ebd00d5','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm_community','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642ec700d6','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642edb00d7','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642edd00d8','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm_community','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89642eee00d9','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=false}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b8964324300da','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=true}','2023-11-01 13:38:46','crm_community','1'),
('ff8080818b8963bb018b89649ff200db','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b89649ff800dc','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a09a00dd','roleSystem','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"applist\", appId:\"appcenter\", appVersion:\"1\", name:\"App List\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a0a000de','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a0ad00df','roleSystem','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"list_appList\", appId:\"appcenter\", appVersion:\"1\", name:\"List - Template List\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a0b400e0','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a0c000e1','roleSystem','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"list_newTemplate\", appId:\"appcenter\", appVersion:\"1\", name:\"List - New Template\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a0cd00e2','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a0df00e3','roleSystem','org.joget.apps.app.dao.UserviewDefinitionDaoImpl','add','{id:\"home\", appId:\"appcenter\", appVersion:\"1\", name:\"App Center\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a0f000e4','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a0fc00e5','roleSystem','org.joget.apps.app.dao.BuilderDefinitionDaoImpl','add','{id:\"INTERNAL_TAGGING\", appId:\"appcenter\", appVersion:\"1\", name:\"Tagging\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a10300e6','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a12100e7','roleSystem','org.joget.apps.app.dao.BuilderDefinitionDaoImpl','add','{id:\"up-0497CD0F62BD44DEEAF1294D58166A1A\", appId:\"appcenter\", appVersion:\"1\", name:\"Design App\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a12700e8','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a13800e9','roleSystem','org.joget.apps.app.dao.BuilderDefinitionDaoImpl','add','{id:\"up-4E2A517C3275429806518BAA8D29AB1F\", appId:\"appcenter\", appVersion:\"1\", name:\"Administration\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a13c00ea','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a14c00eb','roleSystem','org.joget.apps.app.dao.BuilderDefinitionDaoImpl','add','{id:\"up-8C70B71371B942B6D48A7E9B4C1DB8D1\", appId:\"appcenter\", appVersion:\"1\", name:\"Home\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a15200ec','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a16200ed','roleSystem','org.joget.apps.app.dao.BuilderDefinitionDaoImpl','add','{id:\"up-95447DD6260B48B2E00C1354A179CD44\", appId:\"appcenter\", appVersion:\"1\", name:\"Templates\", dateCreated:\"Wed Nov 01 13:39:15 MYT 2023\", dateModified:\"Wed Nov 01 13:39:15 MYT 2023\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a17200ee','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a17500ef','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"<i class=\'fa fa-home\'></i> Home_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a17700f0','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a17900f1','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"<i class=\'fa fa-home\'></i> Home_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a17b00f2','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a17b00f3','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"All Apps_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a17e00f4','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a17f00f5','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"All Apps_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a18100f6','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a18200f7','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"App Center_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a18a00f8','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a19300f9','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"App Center_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a19600fa','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a19900fb','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Design New App_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1a000fc','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1a200fd','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Design New App_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1a600fe','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1a800ff','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Download from Marketplace_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1b60100','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1be0101','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Download from Marketplace_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1d70102','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1da0103','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Faster, Simpler Digital Transformation_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1dd0104','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1df0105','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Faster, Simpler Digital Transformation_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1e00106','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1e20107','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Import App_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1e50108','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1e60109','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Import App_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1ed010a','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1f0010b','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Incorrect Username and/or Password_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1f4010c','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1f5010d','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Incorrect Username and/or Password_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1f7010e','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1f8010f','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Login_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1fa0110','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1fb0111','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Login_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1fd0112','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1fd0113','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Monitor_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a1ff0114','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2000115','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Monitor_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2020116','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2060117','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Password_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2130118','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2140119','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Password_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a21a011a','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a21b011b','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Powered by Joget_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a21e011c','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a220011d','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Powered by Joget_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a221011e','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a222011f','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Published Apps_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2240120','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2250121','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Published Apps_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2300122','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2350123','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Settings_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a23a0124','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a23c0125','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Settings_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2420126','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2460127','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Username_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a24c0128','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a24f0129','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Username_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a253012a','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a253012b','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Users_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a255012c','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','updateDateModified','{id=appcenter, version=1, published=false}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a260012d','roleSystem','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Users_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"\", dateCreated:\"null\", dateModified:\"null\"}','2023-11-01 13:39:15',NULL,NULL),
('ff8080818b8963bb018b8964a2f0012e','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=appcenter, version=1, published=true}','2023-11-01 13:39:15','appcenter','1'),
('ff8080818b8963bb018b8964a31c0130','roleSystem','org.joget.commons.util.SetupManager','saveSetting','defaultUserview','2023-11-01 13:39:15','appcenter','1');
/*!40000 ALTER TABLE `wf_audit_trail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_history_activity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_history_activity` (
  `activityId` varchar(255) NOT NULL,
  `activityName` varchar(255) DEFAULT NULL,
  `activityDefId` varchar(255) DEFAULT NULL,
  `activated` bigint(20) DEFAULT NULL,
  `accepted` bigint(20) DEFAULT NULL,
  `lastStateTime` bigint(20) DEFAULT NULL,
  `limitDuration` varchar(255) DEFAULT NULL,
  `participantId` varchar(255) DEFAULT NULL,
  `assignmentUsers` varchar(255) DEFAULT NULL,
  `performer` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `due` datetime DEFAULT NULL,
  `variables` text DEFAULT NULL,
  `processId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`activityId`),
  KEY `FK_7mmrnb28ugrdxpf0dpw35y73u` (`processId`),
  CONSTRAINT `FK_7mmrnb28ugrdxpf0dpw35y73u` FOREIGN KEY (`processId`) REFERENCES `wf_history_process` (`processId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_history_activity`
--

LOCK TABLES `wf_history_activity` WRITE;
/*!40000 ALTER TABLE `wf_history_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_history_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_history_process`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_history_process` (
  `processId` varchar(255) NOT NULL,
  `processName` varchar(255) DEFAULT NULL,
  `processRequesterId` varchar(255) DEFAULT NULL,
  `resourceRequesterId` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `processDefId` varchar(255) DEFAULT NULL,
  `started` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `lastStateTime` bigint(20) DEFAULT NULL,
  `limitDuration` varchar(255) DEFAULT NULL,
  `due` datetime DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `variables` text DEFAULT NULL,
  PRIMARY KEY (`processId`),
  CONSTRAINT `FK_prxyxtqy6byfrq3l5qght53l6` FOREIGN KEY (`processId`) REFERENCES `wf_process_link_history` (`processId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_history_process`
--

LOCK TABLES `wf_history_process` WRITE;
/*!40000 ALTER TABLE `wf_history_process` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_history_process` ENABLE KEYS */;
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
  PRIMARY KEY (`processId`),
  KEY `idx_origin` (`originProcessId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_process_link`
--

LOCK TABLES `wf_process_link` WRITE;
/*!40000 ALTER TABLE `wf_process_link` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_process_link` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_process_link_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_process_link_history` (
  `processId` varchar(255) NOT NULL,
  `parentProcessId` varchar(255) DEFAULT NULL,
  `originProcessId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`processId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_process_link_history`
--

LOCK TABLES `wf_process_link_history` WRITE;
/*!40000 ALTER TABLE `wf_process_link_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_process_link_history` ENABLE KEYS */;
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
  `assignmentUsers` text DEFAULT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `appVersion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`activityInstanceId`),
  KEY `FKB943CCA47A4E8F48` (`packageId`),
  KEY `FKB943CCA4A39D6461` (`processDefId`),
  KEY `FKB943CCA4CB863F` (`activityDefId`),
  CONSTRAINT `FKB943CCA47A4E8F48` FOREIGN KEY (`packageId`) REFERENCES `wf_report_package` (`packageId`),
  CONSTRAINT `FKB943CCA4A39D6461` FOREIGN KEY (`processDefId`) REFERENCES `wf_report_process` (`processDefId`),
  CONSTRAINT `FKB943CCA4CB863F` FOREIGN KEY (`activityDefId`) REFERENCES `wf_report_activity` (`activityDefId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `message` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `value` text DEFAULT NULL,
  `ordering` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_setup`
--

LOCK TABLES `wf_setup` WRITE;
/*!40000 ALTER TABLE `wf_setup` DISABLE KEYS */;
INSERT INTO `wf_setup` VALUES
('4028c4ea79850c7c0179850cc3880001','deleteProcessOnCompletion','archive',NULL),
('ff8080818b8963bb018b8964a314012f','defaultUserview','appcenter/home',NULL);
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

-- Dump completed on 2023-11-01 13:39:36
