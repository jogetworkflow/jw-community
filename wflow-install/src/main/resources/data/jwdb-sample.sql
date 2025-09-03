/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.7.2-MariaDB, for osx10.20 (arm64)
--
-- Host: localhost    Database: jwdb
-- ------------------------------------------------------
-- Server version	11.7.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `SHKActivities`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('_xpdldata_',1001,29504246,0);
/*!40000 ALTER TABLE `SHKCounters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKCreateProcessEventAudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('crm_community#1#process1','crm_community','process1',1756886143769,'1',0,1000204,0);
/*!40000 ALTER TABLE `SHKProcessDefinitions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKProcessRequesters`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Package xmlns=\"http://www.wfmc.org/2002/XPDL1.0\" xmlns:xpdl=\"http://www.wfmc.org/2002/XPDL1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" Id=\"crm_community\" Name=\"CRM Community\" xsi:schemaLocation=\"http://www.wfmc.org/2002/XPDL1.0 http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd\">\n    <PackageHeader>\n        <XPDLVersion>1.0</XPDLVersion>\n        <Vendor/>\n        <Created/>\n    </PackageHeader>\n    <Script Type=\"text/javascript\"/>\n    <Participants>\n        <Participant Id=\"requester\" Name=\"Requester\">\n            <ParticipantType Type=\"ROLE\"/>\n        </Participant>\n        <Participant Id=\"approver\" Name=\"Approver\">\n            <ParticipantType Type=\"ROLE\"/>\n        </Participant>\n    </Participants>\n    <Applications>\n        <Application Id=\"default_application\"/>\n    </Applications>\n    <WorkflowProcesses>\n        <WorkflowProcess Id=\"process1\" Name=\"Proposal Approval Process\">\n            <ProcessHeader DurationUnit=\"h\"/>\n            <DataFields>\n                <DataField Id=\"status\" IsArray=\"FALSE\">\n                    <DataType>\n                        <BasicType Type=\"STRING\"/>\n                    </DataType>\n                </DataField>\n            </DataFields>\n            <Activities>\n                <Activity Id=\"approve_proposal\" Name=\"Approve Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Join Type=\"XOR\"/>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"205.0000228881836,56.76666259765625\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"approval\" Name=\"Approval\">\n                    <Route/>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Split Type=\"XOR\">\n                                <TransitionRefs>\n                                    <TransitionRef Id=\"transition3\"/>\n                                    <TransitionRef Id=\"transition6\"/>\n                                    <TransitionRef Id=\"transition5\"/>\n                                </TransitionRefs>\n                            </Split>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"430,62.79999084472655\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"activity1\" Name=\"Resubmit Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"397,20.787493896484378\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"send_proposal\" Name=\"Send Proposal\">\n                    <Implementation>\n                        <No/>\n                    </Implementation>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"681.0000381469727,99.78333282470703\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"parallel\" Name=\"Parallel\">\n                    <Route/>\n                    <Performer>approver</Performer>\n                    <TransitionRestrictions>\n                        <TransitionRestriction>\n                            <Split Type=\"AND\">\n                                <TransitionRefs>\n                                    <TransitionRef Id=\"transition7\"/>\n                                    <TransitionRef Id=\"transition8\"/>\n                                </TransitionRefs>\n                            </Split>\n                        </TransitionRestriction>\n                    </TransitionRestrictions>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"588,61.599993896484364\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"tool1\" Name=\"Send Approval Notification\">\n                    <Implementation>\n                        <Tool Id=\"default_application\"/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"735,63.974993896484364\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"tool2\" Name=\"Send Reject Notification\">\n                    <Implementation>\n                        <Tool Id=\"default_application\"/>\n                    </Implementation>\n                    <Performer>approver</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"394,168.39999694824218\"/>\n                    </ExtendedAttributes>\n                </Activity>\n                <Activity Id=\"route1\" Name=\"Route 1\">\n                    <Route/>\n                    <Performer>requester</Performer>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"228.0000228881836,22\"/>\n                    </ExtendedAttributes>\n                </Activity>\n            </Activities>\n            <Transitions>\n                <Transition From=\"approve_proposal\" Id=\"transition2\" To=\"approval\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"approval\" Id=\"transition3\" To=\"activity1\">\n                    <Condition Type=\"CONDITION\">status===\'resubmit\'</Condition>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"activity1\" Id=\"transition4\" To=\"approve_proposal\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"approval\" Id=\"transition6\" Name=\"approved\" To=\"parallel\">\n                    <Condition Type=\"OTHERWISE\"/>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"parallel\" Id=\"transition7\" To=\"send_proposal\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"parallel\" Id=\"transition8\" To=\"tool1\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"approval\" Id=\"transition5\" To=\"tool2\">\n                    <Condition Type=\"CONDITION\">status===\'rejected\'</Condition>\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n                <Transition From=\"route1\" Id=\"transition1\" To=\"approve_proposal\">\n                    <ExtendedAttributes>\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\n                    </ExtendedAttributes>\n                </Transition>\n            </Transitions>\n            <ExtendedAttributes>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER\" Value=\"requester;approver\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_START_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route1,X_OFFSET=87,Y_OFFSET=28,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULT\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool1,X_OFFSET=901,Y_OFFSET=74,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=send_proposal,X_OFFSET=849,Y_OFFSET=110,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool2,X_OFFSET=579,Y_OFFSET=180,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\n            </ExtendedAttributes>\n        </WorkflowProcess>\n    </WorkflowProcesses>\n    <ExtendedAttributes>\n        <ExtendedAttribute Name=\"EDITING_TOOL\" Value=\"Web Workflow Designer\"/>\n        <ExtendedAttribute Name=\"EDITING_TOOL_VERSION\" Value=\"5.0-pre-alpha\"/>\n    </ExtendedAttributes>\n</Package>\n','¨Ì\0sr\0\'org.enhydra.shark.xpdl.elements.Package~+Vm≈Ä~˜\0Z\0isTransientL\0extPkgRefsToIdst\0.Lorg/enhydra/shark/utilities/SequencedHashMap;L\0internalVersiont\0Ljava/lang/String;L\0\nnamespacest\0,Lorg/enhydra/shark/xpdl/elements/Namespaces;xr\0(org.enhydra.shark.xpdl.XMLComplexElement>≤æΩÓ(⁄Ã\0\0xr\05org.enhydra.shark.xpdl.XMLBaseForCollectionAndComplexÂ‡áÓ·ı2\0L\0\nelementMapq\0~\0L\0elementst\0Ljava/util/ArrayList;xr\0!org.enhydra.shark.xpdl.XMLElement#+Bø#˜åº\0Z\0\nisReadOnlyZ\0\nisRequiredL\0nameq\0~\0L\0originalElementHashCodet\0Ljava/lang/Integer;L\0parentt\0#Lorg/enhydra/shark/xpdl/XMLElement;L\0valueq\0~\0xpt\0Packagesr\0java.lang.Integer‚†§˜Åá8\0I\0valuexr\0java.lang.NumberÜ¨ïî‡ã\0\0xpa{Dpt\0\0sr\0,org.enhydra.shark.utilities.SequencedHashMap.Í\"ì©\"&\0\0xpw\0\0\0\rt\0Idsr\0#org.enhydra.shark.xpdl.XMLAttribute#c›Ä˛ÀM;\0L\0choicesq\0~\0xq\0~\0q\0~\0sq\0~\0eprq\0~\0\nt\0\rcrm_communitypt\0Namesq\0~\0\0q\0~\0sq\0~\0&›pnq\0~\0\nt\0\rCRM Communitypt\0\rPackageHeadersr\0-org.enhydra.shark.xpdl.elements.PackageHeadervÈí,ÌíÔ\0\0xq\0~\0\0q\0~\0sq\0~\0K.q\0~\0\nt\0\0sq\0~\0w\0\0\0t\0XPDLVersionsr\0+org.enhydra.shark.xpdl.elements.XPDLVersion˜\"}◊Y.≈w\0\0xr\0\'org.enhydra.shark.xpdl.XMLSimpleElement¬m›¿éÛ\0\0xq\0~\0q\0~\0!sq\0~\0i˘Ê◊q\0~\0t\01.0t\0Vendorsr\0&org.enhydra.shark.xpdl.elements.Vendor t¯ÍE≥:\0\0xq\0~\0#q\0~\0\'sq\0~\0_åz:q\0~\0t\0\0t\0Createdsr\0\'org.enhydra.shark.xpdl.elements.Createdz ÙdKª|[\0\0xq\0~\0#q\0~\0,sq\0~\0\'√±q\0~\0t\0\0t\0Descriptionsr\0+org.enhydra.shark.xpdl.elements.Description€∞73Ê8˚\0\0xq\0~\0#\0q\0~\01sq\0~\0uøØq\0~\0t\0\0t\0\rDocumentationsr\0-org.enhydra.shark.xpdl.elements.Documentation`ƒ9ª◊yí\0\0xq\0~\0#\0q\0~\06sq\0~\0VK—%q\0~\0t\0\0t\0PriorityUnitsr\0,org.enhydra.shark.xpdl.elements.PriorityUnitò≥õËõ¯‰Â\0\0xq\0~\0#\0q\0~\0;sq\0~\0)©pq\0~\0t\0\0t\0CostUnitsr\0(org.enhydra.shark.xpdl.elements.CostUnit‹éŸ=H·å\0\0xq\0~\0#\0q\0~\0@sq\0~\0 |\r@q\0~\0t\0\0xsr\0java.util.ArrayListxÅ“ô«aù\0I\0sizexp\0\0\0w\0\0\0q\0~\0$q\0~\0)q\0~\0.q\0~\03q\0~\08q\0~\0=q\0~\0Bxt\0RedefinableHeadersr\01org.enhydra.shark.xpdl.elements.RedefinableHeaderôøMÎœ™\'H\0\0xq\0~\0\0q\0~\0Gsq\0~\04è…{q\0~\0\nt\0\0sq\0~\0w\0\0\0t\0PublicationStatussq\0~\0\0q\0~\0Msq\0~\0ë÷q\0~\0It\0\0sq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0UNDER_REVISIONt\0RELEASEDt\0\nUNDER_TESTxt\0Authorsr\0&org.enhydra.shark.xpdl.elements.Author5 ƒf·ßÜ\0\0xq\0~\0#\0q\0~\0Usq\0~\08mÀ˜q\0~\0It\0\0t\0Versionsr\0\'org.enhydra.shark.xpdl.elements.Version9=3¥~ªJQ\0\0xq\0~\0#\0q\0~\0Zsq\0~\0l≈∑q\0~\0It\0\0t\0Codepagesr\0(org.enhydra.shark.xpdl.elements.Codepage9$m˜eΩ\rG\0\0xq\0~\0#\0q\0~\0_sq\0~\0M˚˘;q\0~\0It\0\0t\0\nCountrykeysr\0*org.enhydra.shark.xpdl.elements.Countrykeyóã.Å–“—\0\0xq\0~\0#\0q\0~\0dsq\0~\0]Îê†q\0~\0It\0\0t\0Responsiblessr\0,org.enhydra.shark.xpdl.elements.Responsibles$ÔÍ{S∆\0\0xr\0$org.enhydra.shark.xpdl.XMLCollectionËèjƒãmÌ∞\0\0xq\0~\0\0q\0~\0isq\0~\0`«MÛq\0~\0It\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~\0Nq\0~\0Wq\0~\0\\q\0~\0aq\0~\0fq\0~\0lxt\0ConformanceClasssr\00org.enhydra.shark.xpdl.elements.ConformanceClass◊Ïy0|k´î\0\0xq\0~\0\0q\0~\0rsq\0~\0Jë)gq\0~\0\nt\0\0sq\0~\0w\0\0\0t\0GraphConformancesq\0~\0\0q\0~\0xsq\0~\0H“;}q\0~\0tq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0FULL_BLOCKEDt\0LOOP_BLOCKEDt\0NON_BLOCKEDxxsq\0~\0E\0\0\0w\0\0\0q\0~\0yxt\0Scriptsr\0&org.enhydra.shark.xpdl.elements.ScriptQ¶jÁS„8\0\0xq\0~\0\0q\0~\0Äsq\0~\0,ñÛ6q\0~\0\nt\0\0sq\0~\0w\0\0\0t\0Typesq\0~\0q\0~\0Üsq\0~\0oè=“q\0~\0Çt\0text/javascriptpt\0Versionsq\0~\0\0q\0~\0äsq\0~\06_˙çq\0~\0Çt\0\0pt\0Grammarsq\0~\0\0q\0~\0ésq\0~\0&a@q\0~\0Çt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\0áq\0~\0ãq\0~\0èxt\0ExternalPackagessr\00org.enhydra.shark.xpdl.elements.ExternalPackagesw¿\"+≈®®â\0\0xq\0~\0k\0q\0~\0ìsq\0~\0Gßê›q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0TypeDeclarationssr\00org.enhydra.shark.xpdl.elements.TypeDeclarations\r· Ox5Õ\0\0xq\0~\0k\0q\0~\0ösq\0~\0íïq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Participantssr\0,org.enhydra.shark.xpdl.elements.Participantsh`∑g8J\0\0xq\0~\0k\0q\0~\0°sq\0~\0yâ^$q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0+org.enhydra.shark.xpdl.elements.Participanto$îÛrc§è\0\0xr\0+org.enhydra.shark.xpdl.XMLCollectionElementC¢x”vÅr\0\0xq\0~\0t\0Participantsq\0~\0Øq\0~\0£t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0.◊∆Rq\0~\0™t\0	requesterpq\0~\0sq\0~\0\0q\0~\0sq\0~\05Ø!>q\0~\0™t\0	Requesterpt\0ParticipantTypesr\0/org.enhydra.shark.xpdl.elements.ParticipantType>àn•›Ö®˜\0\0xq\0~\0q\0~\0µsq\0~\0O¡ã≈q\0~\0™t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\04·[q\0~\0∑t\0ROLEsq\0~\0E\0\0\0w\0\0\0t\0RESOURCE_SETt\0RESOURCEt\0ROLEt\0ORGANIZATIONAL_UNITt\0HUMANt\0SYSTEMxxsq\0~\0E\0\0\0w\0\0\0q\0~\0ªxt\0Descriptionsq\0~\02\0q\0~\0∆sq\0~\0fEÅÆq\0~\0™t\0\0t\0ExternalReferencesr\01org.enhydra.shark.xpdl.elements.ExternalReferenceÜbË∑¡Qœ\0\0xq\0~\0\0q\0~\0 sq\0~\0*Ø˙âq\0~\0™t\0\0sq\0~\0w\0\0\0t\0xrefsq\0~\0\0q\0~\0–sq\0~\0is√∫q\0~\0Ãt\0\0pt\0locationsq\0~\0q\0~\0‘sq\0~\0Fb…ˆq\0~\0Ãt\0\0pt\0	namespacesq\0~\0\0q\0~\0ÿsq\0~\0`xeq\0~\0Ãt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\0—q\0~\0’q\0~\0Ÿxt\0ExtendedAttributessr\02org.enhydra.shark.xpdl.elements.ExtendedAttributesÚ∞Oıü⁄UF\0L\0extAttribsStringq\0~\0xq\0~\0k\0q\0~\0›sq\0~\0YN∞“q\0~\0™t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~\0Øq\0~\0≤q\0~\0∑q\0~\0«q\0~\0Ãq\0~\0ﬂxsq\0~\0®t\0Participantsq\0~\0~™áq\0~\0£t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0_ Räq\0~\0Ât\0approverpq\0~\0sq\0~\0\0q\0~\0sq\0~\0qkÁq\0~\0Ât\0Approverpt\0ParticipantTypesq\0~\0∂q\0~\0sq\0~\0]‚ù5q\0~\0Ât\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0M5çq\0~\0Òt\0ROLEsq\0~\0E\0\0\0w\0\0\0q\0~\0øq\0~\0¿q\0~\0¡q\0~\0¬q\0~\0√q\0~\0ƒxxsq\0~\0E\0\0\0w\0\0\0q\0~\0ıxt\0Descriptionsq\0~\02\0q\0~\0˙sq\0~\0g/óq\0~\0Ât\0\0t\0ExternalReferencesq\0~\0À\0q\0~\0˛sq\0~\0%¥∆q\0~\0Ât\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0lv-pq\0~\0ˇt\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0π∆.q\0~\0ˇt\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0Ö_q\0~\0ˇt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~q\0~	xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\rsq\0~\0\n‚q\0~\0Ât\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~\0Íq\0~\0Ìq\0~\0Òq\0~\0˚q\0~\0ˇq\0~xxt\0Applicationssr\0,org.enhydra.shark.xpdl.elements.Applications¬Ä¥ﬁˆº\0\0xq\0~\0k\0q\0~sq\0~\0;0‰ìq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0+org.enhydra.shark.xpdl.elements.Applicationv	¢R˘˛S\0\0xq\0~\0©t\0Applicationsq\0~\0f&+Fq\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0w¬Àq\0~t\0default_applicationpq\0~\0sq\0~\0\0q\0~\0sq\0~\0—°6q\0~t\0\0pt\0Descriptionsq\0~\02\0q\0~\'sq\0~\0\"Åbq\0~t\0\0t\0Choicesr\00org.enhydra.shark.xpdl.elements.ApplicationTypesè?˚!≠Æ¶\0\0xr\0\'org.enhydra.shark.xpdl.XMLComplexChoice§|±∫\"Ù˙\0L\0choicesq\0~\0L\0choosenq\0~\0	xq\0~\0q\0~+sq\0~\0¬<7q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sr\00org.enhydra.shark.xpdl.elements.FormalParametersp∫ƒB«√ÅZ\0\0xq\0~\0k\0t\0FormalParameterssq\0~\0¯G˙q\0~.t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~\0À\0t\0ExternalReferencesq\0~\0@S2Xq\0~.t\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0êæ°q\0~9t\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0-¿q\0~9t\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0]øZ®q\0~9t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~>q\0~Aq\0~Dxxq\0~3t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Hsq\0~\0ßbµq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~!q\0~$q\0~(q\0~.q\0~Ixxt\0\nDataFieldssr\0*org.enhydra.shark.xpdl.elements.DataFieldsΩµí ¶‘˙U\0\0xq\0~\0k\0q\0~Osq\0~\0Kå¯®q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0WorkflowProcessessr\01org.enhydra.shark.xpdl.elements.WorkflowProcessespé_¢0,\0\0xq\0~\0k\0q\0~Vsq\0~\0.#»?q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0/org.enhydra.shark.xpdl.elements.WorkflowProcess%·v0“◊L\0\0xq\0~\0©t\0WorkflowProcesssq\0~\0jè6q\0~Xt\0\0sq\0~\0w\0\0\0\rq\0~\0sq\0~\0q\0~\0sq\0~\0IqÒÓq\0~^t\0process1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0oö™¨q\0~^t\0Proposal Approval Processpt\0AccessLevelsq\0~\0\0q\0~isq\0~\0-Ñ∫gq\0~^q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0PUBLICt\0PRIVATExt\0\rProcessHeadersr\0-org.enhydra.shark.xpdl.elements.ProcessHeaderåLÆÉC-)ø\0\0xq\0~\0q\0~osq\0~\0_¥€fq\0~^t\0\0sq\0~\0w\0\0\0t\0DurationUnitsq\0~\0\0q\0~usq\0~\0MË¶q\0~qt\0hsq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0Yt\0Mt\0Dt\0ht\0mt\0sxt\0Createdsq\0~\0-\0q\0~Äsq\0~\0w∫≈q\0~qt\0\0t\0Descriptionsq\0~\02\0q\0~Ñsq\0~\0¬q√q\0~qt\0\0t\0Prioritysr\0(org.enhydra.shark.xpdl.elements.Priority`ˆNn>b\0\0xq\0~\0#\0q\0~àsq\0~\0T˙∂q\0~qt\0\0t\0Limitsr\0%org.enhydra.shark.xpdl.elements.Limit¸¢Ë1ò”ó\0\0xq\0~\0#\0q\0~çsq\0~\0d4q\0~qt\0\0t\0	ValidFromsr\0)org.enhydra.shark.xpdl.elements.ValidFromc≈Ö|ÉL<\0\0xq\0~\0#\0q\0~ísq\0~\0’<£q\0~qt\0\0t\0ValidTosr\0\'org.enhydra.shark.xpdl.elements.ValidToû¡∂´M…\0\0xq\0~\0#\0q\0~ósq\0~\0xıìq\0~qt\0\0t\0TimeEstimationsr\0.org.enhydra.shark.xpdl.elements.TimeEstimation≈Ä£\'3\0\0xq\0~\0\0q\0~úsq\0~\0YUﬂqq\0~qt\0\0sq\0~\0w\0\0\0t\0WaitingTimesr\0+org.enhydra.shark.xpdl.elements.WaitingTimeNì‰Œ†/\0\0xq\0~\0#\0q\0~¢sq\0~\0t]5´q\0~ût\0\0t\0WorkingTimesr\0+org.enhydra.shark.xpdl.elements.WorkingTimeæ~±ÉÊ◊\0\0xq\0~\0#\0q\0~ßsq\0~\0I–/ßq\0~ût\0\0t\0Durationsr\0(org.enhydra.shark.xpdl.elements.Duration© ÙCÙÎË\0\0xq\0~\0#\0q\0~¨sq\0~\0)úçq\0~ût\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~§q\0~©q\0~Æxxsq\0~\0E\0\0\0w\0\0\0q\0~vq\0~Åq\0~Öq\0~äq\0~èq\0~îq\0~ôq\0~ûxt\0RedefinableHeadersq\0~\0H\0q\0~≥sq\0~\0npféq\0~^t\0\0sq\0~\0w\0\0\0q\0~\0Msq\0~\0\0q\0~\0Msq\0~\0sÊÜq\0~¥q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\0Rq\0~\0Sq\0~\0Txt\0Authorsq\0~\0V\0q\0~ªsq\0~\0;¯RUq\0~¥t\0\0t\0Versionsq\0~\0[\0q\0~øsq\0~\0n`lSq\0~¥t\0\0t\0Codepagesq\0~\0`\0q\0~√sq\0~\0I3q\0~¥t\0\0t\0\nCountrykeysq\0~\0e\0q\0~«sq\0~\0¸Ã>q\0~¥t\0\0t\0Responsiblessq\0~\0j\0q\0~Àsq\0~\0foOnq\0~¥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~∏q\0~ºq\0~¿q\0~ƒq\0~»q\0~Ãxt\0FormalParameterssq\0~2\0q\0~“sq\0~\0\0µq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0\nDataFieldssq\0~P\0q\0~ÿsq\0~\0Aõnhq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0)org.enhydra.shark.xpdl.elements.DataFieldI“3.~ë≠˜\0\0xq\0~\0©t\0	DataFieldsq\0~\0K2ﬁΩq\0~Ÿt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0~ô;q\0~ﬂt\0statuspq\0~\0sq\0~\0\0q\0~\0sq\0~\0máﬂq\0~ﬂt\0\0pt\0IsArraysq\0~\0\0q\0~Ísq\0~\09Õ¥Dq\0~ﬂt\0FALSEsq\0~\0E\0\0\0w\0\0\0t\0TRUEt\0FALSExt\0DataTypesr\0(org.enhydra.shark.xpdl.elements.DataTypeÜ\'4sM\0\0xq\0~\0q\0~Òsq\0~\0oéÜ q\0~ﬂt\0\0sq\0~\0w\0\0\0t\0	DataTypessr\0)org.enhydra.shark.xpdl.elements.DataTypesµpcH,ﬁ!ì\0Z\0\risInitializedxq\0~-q\0~˜sq\0~\0uïÙq\0~Ût\0\0sq\0~\0E\0\0\0	w\0\0\0	sr\0)org.enhydra.shark.xpdl.elements.BasicType∑)®Ωw1øé\0\0xq\0~\0t\0	BasicTypesq\0~\0FÚ™‚q\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0hS∑q\0~˛t\0STRINGsq\0~\0E\0\0\0w\0\0\0t\0STRINGt\0FLOATt\0INTEGERt\0	REFERENCEt\0DATETIMEt\0BOOLEANt\0	PERFORMERxxsq\0~\0E\0\0\0w\0\0\0q\0~xsr\0,org.enhydra.shark.xpdl.elements.DeclaredTypedR.\\^Ë9í\0\0xq\0~\0t\0DeclaredTypesq\0~\0\0K>∆q\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0#≤¡q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~xsr\0*org.enhydra.shark.xpdl.elements.SchemaType&1oSH™Ö\0\0xq\0~\0t\0\nSchemaTypesq\0~\0pƒ  q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~\0Àt\0ExternalReferencesq\0~\0)h⁄dq\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0plÅq\0~ t\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0EÉy?q\0~ t\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0@>eq\0~ t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~%q\0~(q\0~+xsr\0*org.enhydra.shark.xpdl.elements.RecordTypeı%®Ω©ÎK\0\0\0xq\0~\0kt\0\nRecordTypesq\0~\0|îmìq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.UnionType¥⁄Ô5PŒG˙\0\0xq\0~\0kt\0	UnionTypesq\0~\0oo~‰q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0/org.enhydra.shark.xpdl.elements.EnumerationType™⁄ﬁf3b\0\0xq\0~\0kt\0EnumerationTypesq\0~\01˚∏∑q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.ArrayTypeg¨$\0≠N@\0\0xq\0~\0t\0	ArrayTypesq\0~\0#⁄˚q\0~˘t\0\0sq\0~\0w\0\0\0t\0\nLowerIndexsq\0~\0q\0~Jsq\0~\0|}Êq\0~Et\0\0pt\0\nUpperIndexsq\0~\0q\0~Nsq\0~\0hÒÀ”q\0~Et\0\0pq\0~˜sq\0~¯q\0~˜sq\0~\0£¯zq\0~Et\0\0ppxsq\0~\0E\0\0\0w\0\0\0q\0~Kq\0~Oq\0~Rxsr\0(org.enhydra.shark.xpdl.elements.ListTypeü\"”ü\nÆ\0\0xq\0~\0t\0ListTypesq\0~\0w’Æ∆q\0~˘t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯q\0~˜sq\0~\0c+ËËq\0~Wt\0\0ppxsq\0~\0E\0\0\0w\0\0\0q\0~\\xxq\0~˛xsq\0~\0E\0\0\0w\0\0\0q\0~˘xt\0InitialValuesr\0,org.enhydra.shark.xpdl.elements.InitialValuej,z˚îúR\0\0xq\0~\0#\0q\0~asq\0~\0b\rq\0~ﬂt\0\0t\0Lengthsr\0&org.enhydra.shark.xpdl.elements.LengthMW+-Ã©W‹\0\0xq\0~\0#\0q\0~fsq\0~\0@Ûq\0~ﬂt\0\0t\0Descriptionsq\0~\02\0q\0~ksq\0~\0D‡nhq\0~ﬂt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~osq\0~\0x∞≤¥q\0~ﬂt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~‰q\0~Áq\0~Îq\0~Ûq\0~cq\0~hq\0~lq\0~pxxt\0Participantssq\0~\0¢\0q\0~vsq\0~\08«Yq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Applicationssq\0~\0q\0~|sq\0~\0$wÎq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ActivitySetssr\0,org.enhydra.shark.xpdl.elements.ActivitySets–qV[4™Ó÷\0\0xq\0~\0k\0q\0~Çsq\0~\0*—“q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0\nActivitiessr\0*org.enhydra.shark.xpdl.elements.Activities&G^·æl˛P\0\0xq\0~\0k\0q\0~âsq\0~\00£q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0(org.enhydra.shark.xpdl.elements.Activity√tá45\Z9ï\0\0xq\0~\0©t\0Activitysq\0~\0Ωî2q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Heq\0~ët\0approve_proposalpq\0~\0sq\0~\0\0q\0~\0sq\0~\01ø^+q\0~ët\0Approve Proposalpt\0Descriptionsq\0~\02\0q\0~úsq\0~\0÷∂Ìq\0~ët\0\0t\0Limitsq\0~é\0q\0~†sq\0~\0hÒ;q\0~ët\0\0q\0~\0Üsr\0-org.enhydra.shark.xpdl.elements.ActivityTypese≈Ω{ˆûËË\0\0xq\0~-q\0~\0Üsq\0~\0r˙†q\0~ët\0\0sq\0~\0E\0\0\0w\0\0\0sr\0%org.enhydra.shark.xpdl.elements.Route0eÓ\r≤G›\0\0xq\0~\0t\0Routesq\0~\09¨Œq\0~•t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0.org.enhydra.shark.xpdl.elements.Implementationúrñí^%ä\0\0xq\0~\0t\0Implementationsq\0~\0\rÀﬁq\0~•t\0\0sq\0~\0w\0\0\0q\0~\0Üsr\03org.enhydra.shark.xpdl.elements.ImplementationTypes\rü‰TŸ°9\0\0xq\0~-q\0~\0Üsq\0~\0WÕuq\0~±t\0\0sq\0~\0E\0\0\0w\0\0\0sr\0\"org.enhydra.shark.xpdl.elements.No{ïÖ⁄.\0\0xq\0~\0t\0Nosq\0~\0z„—q\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0%org.enhydra.shark.xpdl.elements.ToolsCø÷g©ë\0\0xq\0~\0k\0t\0Toolssq\0~\06\'XÅq\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0\'org.enhydra.shark.xpdl.elements.SubFlow;Oısá7:$\0\0xq\0~\0t\0SubFlowsq\0~\0cüWoq\0~∑t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\02Uµq\0~ t\0\0pt\0	Executionsq\0~\0\0q\0~“sq\0~\0W=≈¸q\0~ q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ASYNCHRt\0SYNCHRxt\0ActualParameterssr\00org.enhydra.shark.xpdl.elements.ActualParametersÎå˜µ_ÏK˛\0\0xq\0~\0k\0q\0~ÿsq\0~\0[Rl q\0~ t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~œq\0~”q\0~⁄xxq\0~ºxsq\0~\0E\0\0\0w\0\0\0q\0~∑xsr\0-org.enhydra.shark.xpdl.elements.BlockActivityíq cƒÍF\0\0xq\0~\0t\0\rBlockActivitysq\0~\0Bol‡q\0~•t\0\0sq\0~\0w\0\0\0t\0BlockIdsq\0~\0q\0~Ásq\0~\0]çLtq\0~‚t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~Ëxxq\0~±t\0	Performersr\0)org.enhydra.shark.xpdl.elements.Performer¶\"1%ËÃƒ\0\0xq\0~\0#\0q\0~Ïsq\0~\0d¶pq\0~ët\0approvert\0	StartModesr\0)org.enhydra.shark.xpdl.elements.StartModenhÖÑ◊ÛS\0\0xq\0~\0\0q\0~Òsq\0~\0,Zt~q\0~ët\0\0sq\0~\0w\0\0\0t\0Modesr\00org.enhydra.shark.xpdl.elements.StartFinishModes~Á6zÂXã\'\0\0xq\0~-\0q\0~˜sq\0~\0kyq\0~Ût\0\0sq\0~\0E\0\0\0w\0\0\0sr\0,org.enhydra.shark.xpdl.XMLEmptyChoiceElementÔ2‘;Œ3ı_\0\0xq\0~\0\0t\0XMLEmptyChoiceElementsq\0~\0G»H(q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.AutomaticÈt?˚_Äê\0\0xq\0~\0t\0	Automaticsq\0~\0u‚-Ëq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0&org.enhydra.shark.xpdl.elements.Manual∑v™âÓ[ÿ§\0\0xq\0~\0t\0Manualsq\0~\0u[≈q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~˛xsq\0~\0E\0\0\0w\0\0\0q\0~˘xt\0\nFinishModesr\0*org.enhydra.shark.xpdl.elements.FinishModeˆÉ¯õúºÛ{\0\0xq\0~\0\0q\0~sq\0~\092ﬂÀq\0~ët\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0<æYÕq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0r?q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0kRîdq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0_∆\\q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Prioritysq\0~â\0q\0~0sq\0~\0^±*≠q\0~ët\0\0t\0	Deadlinessr\0)org.enhydra.shark.xpdl.elements.Deadlines>Ωç…ú√⁄\0\0xq\0~\0k\0q\0~4sq\0~\0cã,Œq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsr\05org.enhydra.shark.xpdl.elements.SimulationInformation\"ì|I™£í\0\0xq\0~\0\0q\0~;sq\0~\0uoq\0~ët\0\0sq\0~\0w\0\0\0t\0\rInstantiationsq\0~\0\0q\0~Asq\0~\0qŒ‹kq\0~=q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ONCEt\0MULTIPLExt\0Costsr\0$org.enhydra.shark.xpdl.elements.Costœ˘¿ﬁÚëû\0\0xq\0~\0#q\0~Gsq\0~\0&˝tΩq\0~=t\0\0t\0TimeEstimationsq\0~ùq\0~Lsq\0~\0™—q\0~=t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Qsq\0~\0+≥qq\0~Mt\0\0t\0WorkingTimesq\0~®\0q\0~Usq\0~\0,ôÕûq\0~Mt\0\0t\0Durationsq\0~≠\0q\0~Ysq\0~\0aå,q\0~Mt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Rq\0~Vq\0~Zxxsq\0~\0E\0\0\0w\0\0\0q\0~Bq\0~Iq\0~Mxt\0Iconsr\0$org.enhydra.shark.xpdl.elements.Icon´TŒU(ˇ}6\0\0xq\0~\0#\0q\0~_sq\0~\0+„tºq\0~ët\0\0t\0\rDocumentationsq\0~\07\0q\0~dsq\0~\0Ëéq\0~ët\0\0t\0TransitionRestrictionssr\06org.enhydra.shark.xpdl.elements.TransitionRestrictionsC)∑◊Äi;\0\0xq\0~\0k\0q\0~hsq\0~\0y»Pq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\05org.enhydra.shark.xpdl.elements.TransitionRestrictionN˚ƒáπ}˛\0\0xq\0~\0t\0TransitionRestrictionsq\0~\0l	^q\0~jt\0\0sq\0~\0w\0\0\0t\0Joinsr\0$org.enhydra.shark.xpdl.elements.Join⁄ï”©x)Û5\0\0xq\0~\0\0q\0~usq\0~\0S‰îq\0~pt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0:V≠Rq\0~wt\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ANDt\0XORxxsq\0~\0E\0\0\0w\0\0\0q\0~{xt\0Splitsr\0%org.enhydra.shark.xpdl.elements.Split‹ÌØ~—ØWS\0\0xq\0~\0\0q\0~Çsq\0~\0T7‘∆q\0~pt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0p∏Íq\0~Ñq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssr\0.org.enhydra.shark.xpdl.elements.TransitionRefs†Ô≥—ä·ƒÎ\0\0xq\0~\0k\0q\0~ãsq\0~\0‚‹q\0~Ñt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~àq\0~çxxsq\0~\0E\0\0\0w\0\0\0q\0~wq\0~Ñxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~îsq\0~\0xì∆/q\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\01org.enhydra.shark.xpdl.elements.ExtendedAttributeª¬\\±ÏF\0\0xq\0~\0t\0ExtendedAttributesq\0~\0A`,Úq\0~ït\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0{*˚Üq\0~õt\0JaWE_GRAPH_PARTICIPANT_IDpt\0Valuesq\0~\0\0q\0~£sq\0~\0N;£q\0~õt\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~†q\0~§xsq\0~öt\0ExtendedAttributesq\0~\0/πq\0~ït\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0oXªq\0~®t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0C7q\0~®t\0#205.0000228881836,56.76666259765625pxsq\0~\0E\0\0\0w\0\0\0q\0~≠q\0~∞xxpxsq\0~\0E\0\0\0w\0\0\0q\0~ñq\0~ôq\0~ùq\0~°q\0~•q\0~Óq\0~Ûq\0~q\0~1q\0~6q\0~=q\0~aq\0~eq\0~jq\0~ïxsq\0~êt\0Activitysq\0~\0UÇ4√q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0:ÃœÖq\0~µt\0approvalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0lq\0~µt\0Approvalpt\0Descriptionsq\0~\02\0q\0~¿sq\0~\0(Õ ¯q\0~µt\0\0t\0Limitsq\0~é\0q\0~ƒsq\0~\0z%∫#q\0~µt\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0>≈≠àq\0~µt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0˘æ¨q\0~»t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0@•>Åq\0~»t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0Ü`q\0~“t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0<6Åq\0~◊t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0O{Ùtq\0~◊t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0&Cä,q\0~◊t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0aÿ0q\0~Át\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0gån√q\0~Áq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Úsq\0~\0wDÓq\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Ïq\0~Ôq\0~Ûxxq\0~€xsq\0~\0E\0\0\0w\0\0\0q\0~◊xsq\0~·t\0\rBlockActivitysq\0~\0MÏ<q\0~»t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0A@˘Èq\0~˙t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~ˇxxq\0~Ãt\0	Performersq\0~Ì\0q\0~sq\0~\0Eæmtq\0~µt\0approvert\0	StartModesq\0~Ú\0q\0~sq\0~\0⁄?Øq\0~µt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\08Z¯–q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0c‡J4q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0üÄq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0MAüq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~#sq\0~\0\\lÏ¸q\0~µt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0=ººØq\0~$t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0^àöq\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0}◊ëµq\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0d{ﬂq\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~,xsq\0~\0E\0\0\0w\0\0\0q\0~(xt\0Prioritysq\0~â\0q\0~?sq\0~\0F^lq\0~µt\0\0t\0	Deadlinessq\0~5\0q\0~Csq\0~\0X‹”ˇq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Isq\0~\0«´hq\0~µt\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\06RÅq\0~Jq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Qsq\0~\0\0ëáœq\0~Jt\0\0t\0TimeEstimationsq\0~ùq\0~Usq\0~\0>ÏÔ	q\0~Jt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Zsq\0~\0\Z‘üWq\0~Vt\0\0t\0WorkingTimesq\0~®\0q\0~^sq\0~\08¨7‡q\0~Vt\0\0t\0Durationsq\0~≠\0q\0~bsq\0~\04èp≈q\0~Vt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~[q\0~_q\0~cxxsq\0~\0E\0\0\0w\0\0\0q\0~Nq\0~Rq\0~Vxt\0Iconsq\0~`\0q\0~hsq\0~\0mBE˙q\0~µt\0\0t\0\rDocumentationsq\0~\07\0q\0~lsq\0~\0SRA\"q\0~µt\0\0t\0TransitionRestrictionssq\0~i\0q\0~psq\0~\0\nòoüq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0hU±q\0~qt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~{sq\0~\0|üº/q\0~vt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0t|)Ùq\0~|q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~Äxt\0Splitsq\0~É\0q\0~Ñsq\0~\0o€fòq\0~vt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\06Gsq\0~Öt\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~çsq\0~\07∏œ,q\0~Öt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0-org.enhydra.shark.xpdl.elements.TransitionRefÉ%-ÀÒaö\0\0xq\0~\0©t\0\rTransitionRefsq\0~\0j\'ïÊq\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0;ıÿWq\0~ît\0transition3pxsq\0~\0E\0\0\0w\0\0\0q\0~ôxsq\0~ìt\0\rTransitionRefsq\0~\0y\Zq\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0 äçÃq\0~ùt\0transition6pxsq\0~\0E\0\0\0w\0\0\0q\0~¢xsq\0~ìt\0\rTransitionRefsq\0~\0v‘¥ûq\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0cvÌsq\0~¶t\0transition5pxsq\0~\0E\0\0\0w\0\0\0q\0~´xxxsq\0~\0E\0\0\0w\0\0\0q\0~âq\0~éxxsq\0~\0E\0\0\0w\0\0\0q\0~|q\0~Öxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~±sq\0~\0Eﬁ/Qq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0\\i8Xq\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0hﬂ€åq\0~∑t\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0(ÎLôq\0~∑t\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~ºq\0~øxsq\0~öt\0ExtendedAttributesq\0~\0\"K‚Oq\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\07uµ¶q\0~√t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0!`q\0~√t\0430,62.79999084472655pxsq\0~\0E\0\0\0w\0\0\0q\0~»q\0~Àxxpxsq\0~\0E\0\0\0w\0\0\0q\0~∫q\0~Ωq\0~¡q\0~≈q\0~»q\0~q\0~q\0~$q\0~@q\0~Dq\0~Jq\0~iq\0~mq\0~qq\0~≤xsq\0~êt\0Activitysq\0~\0SNq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0EÖA˛q\0~–t\0	activity1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0.9Õ¯q\0~–t\0Resubmit Proposalpt\0Descriptionsq\0~\02\0q\0~€sq\0~\04˚ïq\0~–t\0\0t\0Limitsq\0~é\0q\0~ﬂsq\0~\0·Àq\0~–t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0.eíôq\0~–t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0N”Ã†q\0~„t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0&hÛq\0~„t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0=J}Úq\0~Ìt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0?π¡òq\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0o_¨q\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0Œ*êq\0~Út\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0`ÖsVq\0~t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0AÀﬂq\0~q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~\rsq\0~\0lB@q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~\nq\0~xxq\0~ˆxsq\0~\0E\0\0\0w\0\0\0q\0~Úxsq\0~·t\0\rBlockActivitysq\0~\0|	rq\0~„t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\07]±q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\Zxxq\0~Ìt\0	Performersq\0~Ì\0q\0~sq\0~\0(]ãq\0~–t\0	requestert\0	StartModesq\0~Ú\0q\0~\"sq\0~\0IB¢\rq\0~–t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0~lË¯q\0~#t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0%±*q\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0˘uq\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0lGî+q\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~+xsq\0~\0E\0\0\0w\0\0\0q\0~\'xt\0\nFinishModesq\0~\0q\0~>sq\0~\05ô”Åq\0~–t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0e^£˘q\0~?t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0«Î›q\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0Jº€Øq\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0mi¬q\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~Gxsq\0~\0E\0\0\0w\0\0\0q\0~Cxt\0Prioritysq\0~â\0q\0~Zsq\0~\0GZﬁq\0~–t\0\0t\0	Deadlinessq\0~5\0q\0~^sq\0~\0tpL.q\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~dsq\0~\0XΩê¨q\0~–t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\06m\\\0q\0~eq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~lsq\0~\0\'Vˆ–q\0~et\0\0t\0TimeEstimationsq\0~ùq\0~psq\0~\0Q1œ(q\0~et\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~usq\0~\0eΩRq\0~qt\0\0t\0WorkingTimesq\0~®\0q\0~ysq\0~\09l¿πq\0~qt\0\0t\0Durationsq\0~≠\0q\0~}sq\0~\0)úQ2q\0~qt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~vq\0~zq\0~~xxsq\0~\0E\0\0\0w\0\0\0q\0~iq\0~mq\0~qxt\0Iconsq\0~`\0q\0~Ésq\0~\0vãì¶q\0~–t\0\0t\0\rDocumentationsq\0~\07\0q\0~ásq\0~\0{QG®q\0~–t\0\0t\0TransitionRestrictionssq\0~i\0q\0~ãsq\0~\0$g9sq\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~ësq\0~\0oπ˘q\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\05ìç5q\0~ít\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Ê–xq\0~ót\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0sÆôq\0~ót\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~úq\0~üxsq\0~öt\0ExtendedAttributesq\0~\0kìù{q\0~ít\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0B¿_q\0~£t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0h\")q\0~£t\0397,20.787493896484378pxsq\0~\0E\0\0\0w\0\0\0q\0~®q\0~´xxpxsq\0~\0E\0\0\0w\0\0\0q\0~’q\0~ÿq\0~‹q\0~‡q\0~„q\0~q\0~#q\0~?q\0~[q\0~_q\0~eq\0~Ñq\0~àq\0~åq\0~íxsq\0~êt\0Activitysq\0~\0kÜÈmq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0:ó4£q\0~∞t\0\rsend_proposalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0âŸ#q\0~∞t\0\rSend Proposalpt\0Descriptionsq\0~\02\0q\0~ªsq\0~\0\\Òu<q\0~∞t\0\0t\0Limitsq\0~é\0q\0~øsq\0~\0M~\n·q\0~∞t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0Ô_¢q\0~∞t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0C˝Ã}q\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0r´q\0~√t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0)f0∆q\0~Õt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0Bühq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0o, \rq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0k)q\0~“t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0sÔƒ¶q\0~‚t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0J	Eåq\0~‚q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Ìsq\0~\0C¸íbq\0~‚t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Áq\0~Íq\0~Óxxq\0~÷xsq\0~\0E\0\0\0w\0\0\0q\0~“xsq\0~·t\0\rBlockActivitysq\0~\0qÏ&q\0~√t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0∂–Zq\0~ıt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~˙xxq\0~Õt\0	Performersq\0~Ì\0q\0~˛sq\0~\0ì”eq\0~∞t\0	requestert\0	StartModesq\0~Ú\0q\0~sq\0~\0;€=q\0~∞t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0%Uˆñq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0e{)q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\04Rª6q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\00∑ëq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~sq\0~\0[’4Áq\0~∞t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0gZÌeq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0Õ¶èq\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0Nı£¨q\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0f‡tq\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~\'xsq\0~\0E\0\0\0w\0\0\0q\0~#xt\0Prioritysq\0~â\0q\0~:sq\0~\0?:ˇq\0~∞t\0\0t\0	Deadlinessq\0~5\0q\0~>sq\0~\0Y%œ°q\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Dsq\0~\0:.ÔÍq\0~∞t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0saæõq\0~Eq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Lsq\0~\0À%ßq\0~Et\0\0t\0TimeEstimationsq\0~ùq\0~Psq\0~\0l‰©∏q\0~Et\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Usq\0~\0!xZqq\0~Qt\0\0t\0WorkingTimesq\0~®\0q\0~Ysq\0~\0_≠£q\0~Qt\0\0t\0Durationsq\0~≠\0q\0~]sq\0~\07M˚q\0~Qt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Vq\0~Zq\0~^xxsq\0~\0E\0\0\0w\0\0\0q\0~Iq\0~Mq\0~Qxt\0Iconsq\0~`\0q\0~csq\0~\0TWéåq\0~∞t\0\0t\0\rDocumentationsq\0~\07\0q\0~gsq\0~\07Ì•q\0~∞t\0\0t\0TransitionRestrictionssq\0~i\0q\0~ksq\0~\0u# Pq\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~qsq\0~\0R‹„öq\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0#ÒÈ{q\0~rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0náhq\0~wt\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0R1]q\0~wt\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~|q\0~xsq\0~öt\0ExtendedAttributesq\0~\0Sµ9ñq\0~rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0#®™q\0~Ét\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0%·q\0~Ét\0#681.0000381469727,99.78333282470703pxsq\0~\0E\0\0\0w\0\0\0q\0~àq\0~ãxxpxsq\0~\0E\0\0\0w\0\0\0q\0~µq\0~∏q\0~ºq\0~¿q\0~√q\0~ˇq\0~q\0~q\0~;q\0~?q\0~Eq\0~dq\0~hq\0~lq\0~rxsq\0~êt\0Activitysq\0~\0ƒ4ƒq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0r˜L∫q\0~êt\0parallelpq\0~\0sq\0~\0\0q\0~\0sq\0~\0o«÷cq\0~êt\0Parallelpt\0Descriptionsq\0~\02\0q\0~õsq\0~\0\rCU…q\0~êt\0\0t\0Limitsq\0~é\0q\0~üsq\0~\0iÑZ≠q\0~êt\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0]R1Mq\0~êt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0˜ÄJq\0~£t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0\ra\0q\0~£t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0Pó˛q\0~≠t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0Éö¯q\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0,F¨Úq\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0\\≥›q\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\04C/íq\0~¬t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\04ó7∏q\0~¬q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Õsq\0~\0-/,‰q\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~«q\0~ q\0~Œxxq\0~∂xsq\0~\0E\0\0\0w\0\0\0q\0~≤xsq\0~·t\0\rBlockActivitysq\0~\0o∂g_q\0~£t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0B$ÍÑq\0~’t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~⁄xxq\0~ßt\0	Performersq\0~Ì\0q\0~ﬁsq\0~\0OÉ/èq\0~êt\0approvert\0	StartModesq\0~Ú\0q\0~‚sq\0~\0bóq\0~êt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0GOb4q\0~„t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0(z-¶q\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0\\P.rq\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\03, q\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~Îxsq\0~\0E\0\0\0w\0\0\0q\0~Áxt\0\nFinishModesq\0~\0q\0~˛sq\0~\0b{·âq\0~êt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0˜}q\0~ˇt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0\Züq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0*ÿkq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0S¬	q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Prioritysq\0~â\0q\0~\Zsq\0~\0lÈl†q\0~êt\0\0t\0	Deadlinessq\0~5\0q\0~sq\0~\0bç‹aq\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~$sq\0~\03µπ€q\0~êt\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0.Îf.q\0~%q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~,sq\0~\0	¿ì?q\0~%t\0\0t\0TimeEstimationsq\0~ùq\0~0sq\0~\0¢(≤q\0~%t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~5sq\0~\0«2ºq\0~1t\0\0t\0WorkingTimesq\0~®\0q\0~9sq\0~\0nË˝¸q\0~1t\0\0t\0Durationsq\0~≠\0q\0~=sq\0~\0cº“uq\0~1t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~6q\0~:q\0~>xxsq\0~\0E\0\0\0w\0\0\0q\0~)q\0~-q\0~1xt\0Iconsq\0~`\0q\0~Csq\0~\0wOëq\0~êt\0\0t\0\rDocumentationsq\0~\07\0q\0~Gsq\0~\0U?èq\0~êt\0\0t\0TransitionRestrictionssq\0~i\0q\0~Ksq\0~\0|ë…q\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0u∂Ìq\0~Lt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~Vsq\0~\0BÄ’)q\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0A5±q\0~Wq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~[xt\0Splitsq\0~É\0q\0~_sq\0~\0(yÛq\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0W¥q\0~`t\0ANDsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~hsq\0~\0Ÿ^Dq\0~`t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ìt\0\rTransitionRefsq\0~\0Y˝∏éq\0~it\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\0èˆãq\0~nt\0transition7pxsq\0~\0E\0\0\0w\0\0\0q\0~sxsq\0~ìt\0\rTransitionRefsq\0~\0=æZ∂q\0~it\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0^Øûﬂq\0~wt\0transition8pxsq\0~\0E\0\0\0w\0\0\0q\0~|xxxsq\0~\0E\0\0\0w\0\0\0q\0~dq\0~ixxsq\0~\0E\0\0\0w\0\0\0q\0~Wq\0~`xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Çsq\0~\0yªUTq\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0xπK<q\0~Ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0@@sq\0~àt\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0#Â”rq\0~àt\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~çq\0~êxsq\0~öt\0ExtendedAttributesq\0~\0j|‘q\0~Ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0X¬:q\0~ît\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0[Ç™≤q\0~ît\0588,61.599993896484364pxsq\0~\0E\0\0\0w\0\0\0q\0~ôq\0~úxxpxsq\0~\0E\0\0\0w\0\0\0q\0~ïq\0~òq\0~úq\0~†q\0~£q\0~ﬂq\0~„q\0~ˇq\0~q\0~q\0~%q\0~Dq\0~Hq\0~Lq\0~Éxsq\0~êt\0Activitysq\0~\0VÒúÛq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0coπDq\0~°t\0tool1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0?Óº%q\0~°t\0\ZSend Approval Notificationpt\0Descriptionsq\0~\02\0q\0~¨sq\0~\0vµãÎq\0~°t\0\0t\0Limitsq\0~é\0q\0~∞sq\0~\08,xµq\0~°t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0b‡}Gq\0~°t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0(%Í™q\0~¥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0rï∆ñq\0~¥t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0)ß·°q\0~æt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0HÃ´óq\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0O;hûq\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0$org.enhydra.shark.xpdl.elements.Tool\\6ñ&∂+GÅ\0\0xq\0~\0©t\0Toolsq\0~\0±:Éq\0~Õt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ZÚΩ»q\0~‘t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0X\0‚q\0~‘q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0APPLICATIONt\0	PROCEDURExt\0ActualParameterssq\0~Ÿ\0q\0~·sq\0~\0$îx€q\0~‘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~Ásq\0~\0gXrq\0~‘t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Îsq\0~\0ùq\0~‘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~Ÿq\0~‹q\0~‚q\0~Ëq\0~Ïxxsq\0~…t\0SubFlowsq\0~\0∆¶ôq\0~√t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0z˜©q\0~Út\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0(«dÇq\0~Úq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~˝sq\0~\0<¨ñúq\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~˜q\0~˙q\0~˛xxq\0~Õxsq\0~\0E\0\0\0w\0\0\0q\0~√xsq\0~·t\0\rBlockActivitysq\0~\0\Z¯˛q\0~¥t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0Hmf∑q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\nxxq\0~æt\0	Performersq\0~Ì\0q\0~sq\0~\0[¯Lq\0~°t\0approvert\0	StartModesq\0~Ú\0q\0~sq\0~\0›ç≈q\0~°t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0Yw≠q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0{+Fq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0^≤‚q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0j†¸q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~.sq\0~\0QúU[q\0~°t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0rŒq\0~/t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0Jioüq\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0\\¿≤pq\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0cª|hq\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~7xsq\0~\0E\0\0\0w\0\0\0q\0~3xt\0Prioritysq\0~â\0q\0~Jsq\0~\0B˛ô4q\0~°t\0\0t\0	Deadlinessq\0~5\0q\0~Nsq\0~\0CÍcq\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Tsq\0~\0\Z`z\\q\0~°t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0\"ÄDq\0~Uq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~\\sq\0~\0Åq\0~Ut\0\0t\0TimeEstimationsq\0~ùq\0~`sq\0~\0„ˇÒq\0~Ut\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~esq\0~\0H◊Àq\0~at\0\0t\0WorkingTimesq\0~®\0q\0~isq\0~\0<ÄÉ{q\0~at\0\0t\0Durationsq\0~≠\0q\0~msq\0~\0 •‚=q\0~at\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~fq\0~jq\0~nxxsq\0~\0E\0\0\0w\0\0\0q\0~Yq\0~]q\0~axt\0Iconsq\0~`\0q\0~ssq\0~\08°çØq\0~°t\0\0t\0\rDocumentationsq\0~\07\0q\0~wsq\0~\0`Ûˇq\0~°t\0\0t\0TransitionRestrictionssq\0~i\0q\0~{sq\0~\0X,?ﬁq\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Åsq\0~\0WóöÏq\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\03˛V\0q\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0,t\'?q\0~át\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0h{L˛q\0~át\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~åq\0~èxsq\0~öt\0ExtendedAttributesq\0~\0àCq\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0^ìÙq\0~ìt\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0Sƒ\n‘q\0~ìt\0735,63.974993896484364pxsq\0~\0E\0\0\0w\0\0\0q\0~òq\0~õxxpxsq\0~\0E\0\0\0w\0\0\0q\0~¶q\0~©q\0~≠q\0~±q\0~¥q\0~q\0~q\0~/q\0~Kq\0~Oq\0~Uq\0~tq\0~xq\0~|q\0~Çxsq\0~êt\0Activitysq\0~\0aÍ∞Óq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0+S∂q\0~†t\0tool2pq\0~\0sq\0~\0\0q\0~\0sq\0~\0\"‡u\0q\0~†t\0Send Reject Notificationpt\0Descriptionsq\0~\02\0q\0~´sq\0~\0X…õq\0~†t\0\0t\0Limitsq\0~é\0q\0~Øsq\0~\0+`ÍÂq\0~†t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0πBßq\0~†t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0>Pl%q\0~≥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0hÀ7eq\0~≥t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0D–ﬂ[q\0~Ωt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0)q\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0,•vXq\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~”t\0Toolsq\0~\0JØ!´q\0~Ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0M~ﬁÈq\0~“t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0B—«Ãq\0~“q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~ﬂq\0~‡xt\0ActualParameterssq\0~Ÿ\0q\0~›sq\0~\0\\OÄq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~„sq\0~\02L[2q\0~“t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Ásq\0~\0]ñ©q\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~◊q\0~⁄q\0~ﬁq\0~‰q\0~Ëxxsq\0~…t\0SubFlowsq\0~\0d∂q\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0_\"Fq\0~Ót\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0tiJq\0~Óq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~˘sq\0~\0d»jq\0~Ót\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Ûq\0~ˆq\0~˙xxq\0~Ãxsq\0~\0E\0\0\0w\0\0\0q\0~¬xsq\0~·t\0\rBlockActivitysq\0~\0<XÂ\\q\0~≥t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0¯˛Éq\0~	t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~	xxq\0~Ωt\0	Performersq\0~Ì\0q\0~	\nsq\0~\0\'c˘Øq\0~†t\0approvert\0	StartModesq\0~Ú\0q\0~	sq\0~\0\0cR±q\0~†t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0{j5‰q\0~	t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\06∂¡q\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\02[q\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0?Ó™”q\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	xsq\0~\0E\0\0\0w\0\0\0q\0~	xt\0\nFinishModesq\0~\0q\0~	*sq\0~\0m@M	q\0~†t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0kŒ«oq\0~	+t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0Uçrˇq\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0<»[Qq\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0õ¯Cq\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	3xsq\0~\0E\0\0\0w\0\0\0q\0~	/xt\0Prioritysq\0~â\0q\0~	Fsq\0~\0jº*q\0~†t\0\0t\0	Deadlinessq\0~5\0q\0~	Jsq\0~\0T≤!q\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~	Psq\0~\0*ﬁ˙	q\0~†t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0~”VÒq\0~	Qq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~	Xsq\0~\0!˚RÌq\0~	Qt\0\0t\0TimeEstimationsq\0~ùq\0~	\\sq\0~\0d%q\0~	Qt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~	asq\0~\09Wï=q\0~	]t\0\0t\0WorkingTimesq\0~®\0q\0~	esq\0~\0]W(8q\0~	]t\0\0t\0Durationsq\0~≠\0q\0~	isq\0~\0&¿uEq\0~	]t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~	bq\0~	fq\0~	jxxsq\0~\0E\0\0\0w\0\0\0q\0~	Uq\0~	Yq\0~	]xt\0Iconsq\0~`\0q\0~	osq\0~\0c©Xéq\0~†t\0\0t\0\rDocumentationsq\0~\07\0q\0~	ssq\0~\0f“«ªq\0~†t\0\0t\0TransitionRestrictionssq\0~i\0q\0~	wsq\0~\0 5±q\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~	}sq\0~\0\'ÔÌq\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0D…ãhq\0~	~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ÒRÄq\0~	Ét\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0KK¬cq\0~	Ét\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~	àq\0~	ãxsq\0~öt\0ExtendedAttributesq\0~\0Hÿ„1q\0~	~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0@¬£âq\0~	èt\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0E¢úCq\0~	èt\0394,168.39999694824218pxsq\0~\0E\0\0\0w\0\0\0q\0~	îq\0~	óxxpxsq\0~\0E\0\0\0w\0\0\0q\0~•q\0~®q\0~¨q\0~∞q\0~≥q\0~	q\0~	q\0~	+q\0~	Gq\0~	Kq\0~	Qq\0~	pq\0~	tq\0~	xq\0~	~xsq\0~êt\0Activitysq\0~\0PoNq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0W¶Jôq\0~	út\0route1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0_°¶q\0~	út\0Route 1pt\0Descriptionsq\0~\02\0q\0~	ßsq\0~\0\n*àèq\0~	út\0\0t\0Limitsq\0~é\0q\0~	´sq\0~\0[;]\rq\0~	út\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0sN˝Bq\0~	út\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0!\r\\q\0~	Øt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0ΩÜq\0~	Øt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0~ooÕq\0~	πt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0zœ)Øq\0~	æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\03i•Fq\0~	æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0 w®øq\0~	æt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0%c∫{q\0~	Œt\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0&‚m)q\0~	Œq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~	Ÿsq\0~\0X_ø&q\0~	Œt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~	”q\0~	÷q\0~	⁄xxq\0~	¬xsq\0~\0E\0\0\0w\0\0\0q\0~	æxsq\0~·t\0\rBlockActivitysq\0~\0E∆¬q\0~	Øt\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0}xeq\0~	·t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~	Êxxq\0~	≥t\0	Performersq\0~Ì\0q\0~	Ísq\0~\0H∆Ω∆q\0~	út\0	requestert\0	StartModesq\0~Ú\0q\0~	Ósq\0~\0mEùwq\0~	út\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0.@ÄÀq\0~	Ôt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\00Ä	q\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0\'¶éWq\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0Uåw„q\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	˜xsq\0~\0E\0\0\0w\0\0\0q\0~	Ûxt\0\nFinishModesq\0~\0q\0~\n\nsq\0~\0\Z@m†q\0~	út\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0qÜ)q\0~\nt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\00–˜∞q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0‰)Àq\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0,%Qq\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~\nxsq\0~\0E\0\0\0w\0\0\0q\0~\nxt\0Prioritysq\0~â\0q\0~\n&sq\0~\00SÆ\'q\0~	út\0\0t\0	Deadlinessq\0~5\0q\0~\n*sq\0~\0	≤Íq\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~\n0sq\0~\0-âi±q\0~	út\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0)\nÁ›q\0~\n1q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~\n8sq\0~\0ÖFMq\0~\n1t\0\0t\0TimeEstimationsq\0~ùq\0~\n<sq\0~\0NQ:’q\0~\n1t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~\nAsq\0~\0(ÛOq\0~\n=t\0\0t\0WorkingTimesq\0~®\0q\0~\nEsq\0~\0V9(Éq\0~\n=t\0\0t\0Durationsq\0~≠\0q\0~\nIsq\0~\0x†´\'q\0~\n=t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~\nBq\0~\nFq\0~\nJxxsq\0~\0E\0\0\0w\0\0\0q\0~\n5q\0~\n9q\0~\n=xt\0Iconsq\0~`\0q\0~\nOsq\0~\0?‡°tq\0~	út\0\0t\0\rDocumentationsq\0~\07\0q\0~\nSsq\0~\0â©q\0~	út\0\0t\0TransitionRestrictionssq\0~i\0q\0~\nWsq\0~\0s’¯q\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n]sq\0~\0mw≠Bq\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0)cé\Zq\0~\n^t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\rQÄ/q\0~\nct\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0xÎ√Úq\0~\nct\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~\nhq\0~\nkxsq\0~öt\0ExtendedAttributesq\0~\0zùWq\0~\n^t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0;‹—‹q\0~\not\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0:\rnpq\0~\not\0228.0000228881836,22pxsq\0~\0E\0\0\0w\0\0\0q\0~\ntq\0~\nwxxpxsq\0~\0E\0\0\0w\0\0\0q\0~	°q\0~	§q\0~	®q\0~	¨q\0~	Øq\0~	Îq\0~	Ôq\0~\nq\0~\n\'q\0~\n+q\0~\n1q\0~\nPq\0~\nTq\0~\nXq\0~\n^xxt\0Transitionssr\0+org.enhydra.shark.xpdl.elements.TransitionsÌ9>‰¿/iØ\0\0xq\0~\0k\0q\0~\n|sq\0~\0v MFq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0*org.enhydra.shark.xpdl.elements.Transitiontßx∏Ö\0\0xq\0~\0©t\0\nTransitionsq\0~\0M÷q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0-≤hq\0~\nÑt\0transition2pq\0~\0sq\0~\0\0q\0~\0sq\0~\04¨Ω˚q\0~\nÑt\0\0pt\0Fromsq\0~\0q\0~\nèsq\0~\0Miq\0~\nÑt\0approve_proposalpt\0Tosq\0~\0q\0~\nìsq\0~\0~Ó 6q\0~\nÑt\0approvalpt\0	Conditionsr\0)org.enhydra.shark.xpdl.elements.Condition€‡D··Z;|\0\0xq\0~\0\0q\0~\nósq\0~\0!‘;Eq\0~\nÑt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0t9XYq\0~\nôq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0	CONDITIONt\0	OTHERWISEt\0	EXCEPTIONt\0DEFAULTEXCEPTIONxxsq\0~\0E\0\0\0w\0\0\0q\0~\nùxt\0Descriptionsq\0~\02\0q\0~\n•sq\0~\0|éìíq\0~\nÑt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n©sq\0~\0-∫ùq\0~\nÑt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0kt?Ωq\0~\n™t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0FÓ—q\0~\nØt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\03)d>q\0~\nØt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\n¥q\0~\n∑xxpxsq\0~\0E\0\0\0w\0\0\0q\0~\nâq\0~\nåq\0~\nêq\0~\nîq\0~\nôq\0~\n¶q\0~\n™xsq\0~\nÉt\0\nTransitionsq\0~\0\nÔ;≥q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\0ÿô0q\0~\nºt\0transition3pq\0~\0sq\0~\0\0q\0~\0sq\0~\0äi¡q\0~\nºt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0@¨tÑq\0~\nºt\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\0pzÎdq\0~\nºt\0	activity1pt\0	Conditionsq\0~\nò\0q\0~\nÕsq\0~\0{K˘¿q\0~\nºt\0status===\'resubmit\'sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0X`∫yq\0~\nŒt\0	CONDITIONsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~\n“xt\0Descriptionsq\0~\02\0q\0~\n◊sq\0~\0fK⁄q\0~\nºt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n€sq\0~\0$˘Òñq\0~\nºt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0?à—Jq\0~\n‹t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0PêÈ√q\0~\n·t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0_⁄Ó^q\0~\n·t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\nÊq\0~\nÈxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\n¡q\0~\nƒq\0~\n«q\0~\n q\0~\nŒq\0~\nÿq\0~\n‹xsq\0~\nÉt\0\nTransitionsq\0~\0J¢é;q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ÿu{q\0~\nÓt\0transition4pq\0~\0sq\0~\0\0q\0~\0sq\0~\0lpó\'q\0~\nÓt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\03Y§q\0~\nÓt\0	activity1pq\0~\nìsq\0~\0q\0~\nìsq\0~\0d–=˝q\0~\nÓt\0approve_proposalpt\0	Conditionsq\0~\nò\0q\0~\nˇsq\0~\02˝ƒq\0~\nÓt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0I˙Aq\0~\0q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Descriptionsq\0~\02\0q\0~sq\0~\0,†≤q\0~\nÓt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0D¡q\0~\nÓt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0;EpÖq\0~\rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\06\"“q\0~t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0])Ieq\0~t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~\Zxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\nÛq\0~\nˆq\0~\n˘q\0~\n¸q\0~\0q\0~	q\0~\rxsq\0~\nÉt\0\nTransitionsq\0~\0fmÔq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0I◊rq\0~t\0transition6pq\0~\0sq\0~\0\0q\0~\0sq\0~\0V¯Àéq\0~t\0approvedpq\0~\nèsq\0~\0q\0~\nèsq\0~\0v<zq\0~t\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\0A\r–´q\0~t\0parallelpt\0	Conditionsq\0~\nò\0q\0~0sq\0~\0_‰€Bq\0~t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0J&®q\0~1t\0	OTHERWISEsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~5xt\0Descriptionsq\0~\02\0q\0~:sq\0~\0e:/»q\0~t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~>sq\0~\0u,Zq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0]jÉtq\0~?t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0f“´‹q\0~Dt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0h]–öq\0~Dt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~Iq\0~Lxxpxsq\0~\0E\0\0\0w\0\0\0q\0~$q\0~\'q\0~*q\0~-q\0~1q\0~;q\0~?xsq\0~\nÉt\0\nTransitionsq\0~\0&‰ﬂﬁq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0y∆q\0~Qt\0transition7pq\0~\0sq\0~\0\0q\0~\0sq\0~\0Z<â7q\0~Qt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0/âUq\0~Qt\0parallelpq\0~\nìsq\0~\0q\0~\nìsq\0~\0\\c[!q\0~Qt\0\rsend_proposalpt\0	Conditionsq\0~\nò\0q\0~bsq\0~\0Z&ßÅq\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0;À±q\0~cq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~gxt\0Descriptionsq\0~\02\0q\0~ksq\0~\0W«0‰q\0~Qt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~osq\0~\0iån\Zq\0~Qt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0e	NÁq\0~pt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0+é8]q\0~ut\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0yıû@q\0~ut\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~zq\0~}xxpxsq\0~\0E\0\0\0w\0\0\0q\0~Vq\0~Yq\0~\\q\0~_q\0~cq\0~lq\0~pxsq\0~\nÉt\0\nTransitionsq\0~\0rx«q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0zwü:q\0~Çt\0transition8pq\0~\0sq\0~\0\0q\0~\0sq\0~\03g|)q\0~Çt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0<£r8q\0~Çt\0parallelpq\0~\nìsq\0~\0q\0~\nìsq\0~\0fµ˘!q\0~Çt\0tool1pt\0	Conditionsq\0~\nò\0q\0~ìsq\0~\03Sé¬q\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0∆\"q\0~îq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~òxt\0Descriptionsq\0~\02\0q\0~úsq\0~\0]Ûójq\0~Çt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~†sq\0~\0U}.…q\0~Çt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0e\"†uq\0~°t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0v}‡q\0~¶t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0h©‰q\0~¶t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~´q\0~Æxxpxsq\0~\0E\0\0\0w\0\0\0q\0~áq\0~äq\0~çq\0~êq\0~îq\0~ùq\0~°xsq\0~\nÉt\0\nTransitionsq\0~\04ËÑq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0$KÎq\0~≥t\0transition5pq\0~\0sq\0~\0\0q\0~\0sq\0~\0ﬂâq\0~≥t\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0hÂ¬ˇq\0~≥t\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\0D–;~q\0~≥t\0tool2pt\0	Conditionsq\0~\nò\0q\0~ƒsq\0~\0>H˛q\0~≥t\0status===\'rejected\'sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0x$ó˘q\0~≈t\0	CONDITIONsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~…xt\0Descriptionsq\0~\02\0q\0~Œsq\0~\0=ÍXHq\0~≥t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~“sq\0~\0CO¨ƒq\0~≥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0/~˜q\0~”t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ÃEtq\0~ÿt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0y•Ktq\0~ÿt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~›q\0~‡xxpxsq\0~\0E\0\0\0w\0\0\0q\0~∏q\0~ªq\0~æq\0~¡q\0~≈q\0~œq\0~”xsq\0~\nÉt\0\nTransitionsq\0~\0æuIq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0MŸ§q\0~Ât\0transition1pq\0~\0sq\0~\0\0q\0~\0sq\0~\06‡ZCq\0~Ât\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0XSq\0~Ât\0route1pq\0~\nìsq\0~\0q\0~\nìsq\0~\0eMSuq\0~Ât\0approve_proposalpt\0	Conditionsq\0~\nò\0q\0~ˆsq\0~\0VeÎq\0~Ât\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0Ñ\rq\0~˜q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~˚xt\0Descriptionsq\0~\02\0q\0~ˇsq\0~\0Xπ„q\0~Ât\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\08ﬁ»iq\0~Ât\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0%úq\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0<Fföq\0~	t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\063≠Cq\0~	t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~xxpxsq\0~\0E\0\0\0w\0\0\0q\0~Íq\0~Ìq\0~q\0~Ûq\0~˜q\0~\0q\0~xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0mîøVq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0h∑\n˚q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0=bÀTq\0~t\0%JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDERpq\0~£sq\0~\0\0q\0~£sq\0~\00…©Âq\0~t\0requester;approverpxsq\0~\0E\0\0\0w\0\0\0q\0~!q\0~$xsq\0~öt\0ExtendedAttributesq\0~\0\\_œq\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0#fdq\0~(t\0JaWE_GRAPH_START_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0\'0$ﬂq\0~(t\0ûJaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route1,X_OFFSET=87,Y_OFFSET=28,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~-q\0~0xsq\0~öt\0ExtendedAttributesq\0~\0ZÀ7]q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0‡ÊÏq\0~4t\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0g≥Rq\0~4t\0õJaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool1,X_OFFSET=901,Y_OFFSET=74,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~9q\0~<xsq\0~öt\0ExtendedAttributesq\0~\0-Wh=q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0.•”q\0~@t\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\05iá´q\0~@t\0•JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=send_proposal,X_OFFSET=849,Y_OFFSET=110,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~Eq\0~Hxsq\0~öt\0ExtendedAttributesq\0~\0\"_wq\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\"ŸÆq\0~Lt\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0çdq\0~Lt\0úJaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool2,X_OFFSET=579,Y_OFFSET=180,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~Qq\0~Txxpxsq\0~\0E\0\0\0\rw\0\0\0\rq\0~cq\0~fq\0~jq\0~qq\0~¥q\0~”q\0~Ÿq\0~wq\0~}q\0~Ñq\0~ãq\0~\n~q\0~xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Ysq\0~\0UWßq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0∏q\0~Zt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0k‡±ÿq\0~_t\0EDITING_TOOLpq\0~£sq\0~\0\0q\0~£sq\0~\0Gãtq\0~_t\0Web Workflow Designerpxsq\0~\0E\0\0\0w\0\0\0q\0~dq\0~gxsq\0~öt\0ExtendedAttributesq\0~\0\n åÙq\0~Zt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0@!Œ>q\0~kt\0EDITING_TOOL_VERSIONpq\0~£sq\0~\0\0q\0~£sq\0~\0.°…ìq\0~kt\0\r5.0-pre-alphapxsq\0~\0E\0\0\0w\0\0\0q\0~pq\0~sxxpxsq\0~\0E\0\0\0\rw\0\0\0\rq\0~\0q\0~\0q\0~\0q\0~\0Iq\0~\0tq\0~\0Çq\0~\0ïq\0~\0úq\0~\0£q\0~q\0~Qq\0~Xq\0~Zx\0sq\0~\0w\0\0\0\0xt\01sr\0*org.enhydra.shark.xpdl.elements.Namespaces|ö—<.Rßñ\0\0xq\0~\0kt\0\nNamespacessq\0~\0|^£[q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0)org.enhydra.shark.xpdl.elements.NamespaceËz¬¬_\0\0xq\0~\0t\0	Namespacesq\0~\0<4ááq\0~{t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\06¿h<q\0~Çt\0xpdlpq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0ßµ^q\0~Çt\0 http://www.wfmc.org/2002/XPDL1.0pxsq\0~\0E\0\0\0w\0\0\0q\0~áq\0~äxx',1000202,1,1000203,0);
/*!40000 ALTER TABLE `SHKXPDLData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SHKXPDLHistory`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('crm_community','1',1184650391000,'2025-09-03 15:55:43',1000202,0);
/*!40000 ALTER TABLE `SHKXPDLS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_app`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
  `createdBy` varchar(255) DEFAULT NULL,
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
('appcenter',1,'App Center',0x01,'2025-09-03 15:55:49','2025-09-03 15:55:49','oRIgWuw8ed5OmS98TSZFxocskOFXU0v3VPneM0k80NqSBK2r6RhNzTNTryZkuj4W',NULL,NULL,'roleSystem'),
('crm_community',1,'CRM Community',0x01,'2025-09-03 15:55:40','2025-09-03 15:55:45','Vfe6Df5AdlrGEPqFYgTt8XuGJ4XiTt2NnBLEovw3qoQ=',NULL,NULL,'roleSystem');
/*!40000 ALTER TABLE `app_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_builder`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
('appcenter',1,'INTERNAL_TAGGING','Tagging','internal','2025-04-13 01:05:12','2025-04-13 01:05:12','{\n    \"datas\": {\n        \"form\": {},\n        \"list\": {},\n        \"userview\": {\n            \"v\": [\"t07\"],\n            \"v3\": [\"t08\"]\n        }\n    },\n    \"labels\": {\n        \"t01\": {\"color\": \"red\"},\n        \"t02\": {\"color\": \"pink\"},\n        \"t03\": {\"color\": \"orange\"},\n        \"t04\": {\"color\": \"yellow\"},\n        \"t05\": {\"color\": \"green\"},\n        \"t06\": {\"color\": \"lime\"},\n        \"t07\": {\n            \"color\": \"blue\",\n            \"label\": \"Default\"\n        },\n        \"t08\": {\n            \"color\": \"sky\",\n            \"label\": \"Glass\"\n        },\n        \"t09\": {\"color\": \"purple\"},\n        \"t10\": {\"color\": \"black\"}\n    }\n}',NULL),
('appcenter',1,'up-8C70B71371B942B6D48A7E9B4C1DB8D1','Home','INTERNAL_USERVIEW_PAGE','2025-04-13 01:05:12','2025-04-13 01:08:37','{\"elements\":[{\"elements\":[{\"elements\":[{\"elements\":[{\"elements\":[],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-mobile-margin-top\":\"15px\",\"style-margin-top\":\"30px\",\"style-text-align\":\"center\",\"style-margin-bottom\":\"20px\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{\"hidden\":\"true\"},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"style-color\":\"#FFFFFF\",\"style-mobile-margin-bottom\":\"15px\",\"style-font-size\":\"13px\",\"attr-data-events-triggering\":[],\"id\":\"8358CCF066424B1AF09EBF61A96EAFC5\",\"customId\":\"assignment_msg\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-position\":\"absolute\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"style-color\":\"#FFFFFF\",\"style-line-height\":\"40px\",\"style-font-size\":\"20px\",\"textContent\":\" \",\"attr-data-events-triggering\":[],\"tagName\":\"h3\",\"customId\":\"clock\",\"style-top\":\"35px\",\"style-margin-top\":\"0px\",\"style-right\":\"15px\",\"style-text-align\":\"left\",\"style-margin-bottom\":\"0px\",\"id\":\"E92400A7CBAD40DC6E28B620D44D34D6\"}},{\"elements\":[{\"className\":\"org.joget.apps.userview.lib.component.ButtonComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"css-display-type\":\"btn btn-light\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"style-color\":\"#7E8299\",\"style-margin-right\":\"10px\",\"attr-onclick\":\"appCreate();return false;\",\"textContent\":\"#i18n.Design New App#\",\"attr-data-events-triggering\":[],\"attr-href\":\"#\",\"id\":\"8EFC9D21190A4BFBE31A945826FF6B34\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ButtonComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"css-display-type\":\"btn btn-light\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"style-color\":\"#7E8299\",\"style-margin-right\":\"15px\",\"attr-onclick\":\"appImport();return false;\",\"textContent\":\"#i18n.Import App#\",\"attr-data-events-triggering\":[],\"attr-href\":\"#\",\"id\":\"10862931081E4DF47D44CCDFC696CAD6\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ButtonComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-font-weight\":\"500\",\"css-display-type\":\"btn btn-light\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"style-color\":\"#7E8299\",\"attr-onclick\":\"AdminBar.showQuickOverlay(\\\"#request.contextPath#/web/desktop/marketplace/app?url=\\\" + encodeURIComponent(\\\"#platform.marketplaceUrl#\\\"));return false\",\"textContent\":\"#i18n.Download from Marketplace#\",\"attr-data-events-triggering\":[],\"attr-href\":\"#\",\"id\":\"07863BFB349747ED1A6E45812F7C5F97\",\"customId\":\"\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-text-align\":\"center\",\"hidden\":\"true\",\"style-margin-bottom\":\"10px\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{\"hidden\":\"true\"},\"58F9FBEA76264BE037991759BC1645A2\":{\"hidden\":\"\"},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{\"hidden\":\"true\"}},\"attr-data-events-triggering\":[],\"id\":\"96141190357145C9478235142772AEC5\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.ScriptComponent\",\"properties\":{\"attr-data-events-listening\":[],\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"attr-data-events-triggering\":[],\"id\":\"66465E19B19A4481B8BD32BD9BBC0D83\",\"customId\":\"\",\"script\":\"$(function(){\\n    window[\\\"ajaxContentPlaceholder\\\"][\\\"#request.contextPath#/web/userview/appcenter/v/_/home\\\"] = \\\"dashboard\\\";\\n    \\n    var updateClock = function(clock, day){\\n        var date = new Date();\\n        var ampm = date.getHours() < 12 ? \'AM\' : \'PM\';\\n        var hours = date.getHours() == 0\\n                  ? 12\\n                  : date.getHours() > 12\\n                    ? date.getHours() - 12\\n                    : date.getHours();\\n        var minutes = date.getMinutes() < 10 \\n                    ? \'0\' + date.getMinutes() \\n                    : date.getMinutes();\\n        var style = $(clock).find(\\\"style\\\"); \\n        var dstyle = $(day).find(\\\"style\\\"); \\n        $(clock).text(hours + \\\":\\\" + minutes + \\\" \\\" + ampm);\\n        $(day).text(date.toLocaleDateString(\\\"en-US\\\", { weekday: \'long\', year: \'numeric\', month: \'long\', day: \'numeric\' }));\\n        $(clock).append(style);\\n        $(day).append(dstyle);\\n    };\\n    \\n    var clock = $(\\\"#clock\\\");\\n    var day = $(\\\"#date\\\")\\n    if (clock.length > 0) {\\n        updateClock(clock, day);\\n        window.setInterval(function() {\\n            updateClock(clock, day);\\n        }, 10000);\\n    }\\n    \\n    $(\\\".inbox-notification\\\").off(\\\"inbox_notification_updated\\\");\\n    $(\\\".inbox-notification\\\").on(\\\"inbox_notification_updated\\\", function(){\\n        var style = $(\\\"#assignment_msg\\\").find(\\\"style\\\");        \\n        $(\\\"#assignment_msg\\\").html(\'<h3 style=\\\"color:#fff;\\\">Hello #currentUser.firstName#, <\\/h3><a style=\\\"color:#fff;opacity: 0.75 !important;\\\" href=\\\"#request.baseURL#/web/userview/appcenter/home/_/_ja_inbox\\\"><h5>\' + $(\\\".inbox-notification .dropdown-menu-title span\\\").text() + \'<h5><\\/a>\');\\n        $(\\\"#assignment_msg\\\").append(style);\\n    });\\n    \\n    $(\\\".inbox-notification\\\").trigger(\\\"inbox_notification_updated\\\");\\n});\"}}],\"className\":\"org.joget.apps.userview.lib.component.ContainerComponent\",\"properties\":{\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"attr-data-events-triggering\":[],\"id\":\"970F3C4619204E5D5F9253C3A2B9D8A3\",\"customId\":\"inbox_container\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-top\":\"15px\",\"style-position\":\"absolute\",\"style-color\":\"#FFFFFF\",\"style-left\":\"30px\",\"permission_rules\":{\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"textContent\":\"<i class=\\\"zmdi zmdi-apps\\\"><\\/i> App Center\",\"id\":\"41B14E1F2C034F11E6F29E4A157C3FEC\",\"tagName\":\"h1\",\"customId\":\"\"}},{\"className\":\"org.joget.apps.userview.lib.component.HeadingComponent\",\"properties\":{\"attr-data-events-listening\":[],\"style-top\":\"15px\",\"style-right\":\"15px\",\"style-position\":\"absolute\",\"style-text-align\":\"center\",\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"style-color\":\"#FFFFFF\",\"style-font-size\":\"20px\",\"textContent\":\" \",\"id\":\"681B2A6A3E604CDA411A34255B7BD9E3\",\"tagName\":\"h6\",\"customId\":\"date\"}},{\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{}},\"className\":\"menu-component\"}],\"className\":\"org.joget.apps.userview.lib.component.ColumnComponent\",\"properties\":{\"style-custom\":\"padding:20px;\",\"permission_rules\":{\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"style-flex\":\"0 0 100%\",\"style-background-image\":\"#request.contextPath#/images/appCenterBanner.png\",\"style-max-width\":\"100%\",\"style-mobile-padding-top\":\"80px\"}}],\"className\":\"org.joget.apps.userview.lib.component.ColumnsComponent\",\"properties\":{\"attr-data-events-listening\":[],\"col-0-style-custom\":\"padding:20px;\",\"col-1-style-tablet-custom\":\"padding:80px 40px 40px 20px;\",\"columns\":[{\"style-mobile-max-width\":\"\",\"style-max-width\":\"100%\",\"style-tablet-max-width\":\"\"}],\"permission_rules\":{\"F6E97C06864E4F62843B259A8C9AB1AD\":{},\"58F9FBEA76264BE037991759BC1645A2\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"col-1-style-custom\":\"padding:80px 80px 40px 20px;\",\"css-mobile-stack-columns\":\"true\",\"attr-data-events-triggering\":[],\"customId\":\"home_column\",\"col-0-style-mobile-padding-top\":\"80px\",\"gutter\":\"\",\"id\":\"74DC9C5E101D4BB4D955B3B9C5FE48D3\",\"col-0-style-background-image\":\"#request.contextPath#/images/appCenterBanner.png\",\"col-1-style-mobile-custom\":\"padding: 0px 20px 20px;\"}}],\"className\":\"org.joget.apps.userview.model.UserviewPage\",\"properties\":{\"id\":\"up-8C70B71371B942B6D48A7E9B4C1DB8D1\"}}',NULL);
/*!40000 ALTER TABLE `app_builder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_datalist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
('appcenter',1,'applist','App List',NULL,'{\"actions\":[],\"binder\":{\"className\":\"org.joget.apps.datalist.lib.JsonApiDatalistBinder\",\"properties\":{\"jsonUrl\":\"#request.baseURL#\\/web\\/json\\/apps\\/published\\/userviews?appCenter=true\",\"requestType\":\"\",\"headers\":[],\"copyCookies\":\"true\",\"multirowBaseObject\":\"apps.userviews\",\"totalRowCountObject\":\"\",\"primaryKey\":\"id\",\"handlePaging\":\"true\",\"handleFilters\":\"true\",\"debugMode\":\"\",\"sampleResponse\":\"\",\"joinObjectKeysAndValues\":\"\"}},\"card-style-background-color\":\"#F5F8FA\",\"card-style-border-radius\":\"10px\",\"card-style-border-width\":\"0px\",\"card-style-hover-background-color\":\"#F5F8FA\",\"card-style-hover-border-color\":\"#FFFFFF\",\"card-style-hover-custom\":\"transition: all 0.2s ease-out;\\r\\nbox-shadow: 0px 4px 8px rgba(38, 38, 38, 0.2);\\r\\ntop: -5px;\\r\\nborder: 1px solid #cccccc;\\r\\nbackground-color: white;\",\"card-style-max-height\":\"200px\",\"card-style-min-height\":\"200px\",\"card-style-padding-bottom\":\"0px\",\"card-style-padding-left\":\"0px\",\"card-style-padding-right\":\"0px\",\"card-style-padding-top\":\"2.25rem\",\"card-style-text-align\":\"center\",\"column_title\":[{\"id\":\"column_2\",\"name\":\"name\",\"label\":\"name\",\"filterable\":true,\"hidden\":\"false\",\"sortable\":\"false\",\"datalist_type\":\"column\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{}}}],\"columns\":[],\"considerFilterWhenGetTotal\":\"\",\"description\":\"\",\"disableResponsive\":\"\",\"filters\":[{\"id\":\"filter_0\",\"name\":\"apps.name\",\"label\":\"App Name\",\"filterParamName\":\"d-1331669-fn_apps.name\",\"type\":{\"className\":\"org.joget.apps.datalist.lib.TextFieldDataListFilterType\",\"properties\":{\"defaultValue\":\"\",\"filterSelection\":\"String\",\"searchFields\":\"\"}},\"datalist_type\":\"filter\",\"hidden\":\"\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{}}}],\"hidePageSize\":\"true\",\"id\":\"applist\",\"name\":\"App List\",\"order\":\"2\",\"orderBy\":\"name\",\"pageSize\":\"100\",\"pageSizeSelectorOptions\":\"10\",\"permission_rules\":[{\"permission\":{\"className\":\"org.joget.plugin.enterprise.AdminUserviewPermission\",\"properties\":{}},\"permission_key\":\"9D3F667778064DF2976BDBE2C629EA8D\",\"permission_name\":\"Is Admin\"}],\"responsiveMode\":\"\",\"rowAction_card\":[{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"id\":\"rowAction_2\",\"properties\":{\"confirmation\":\"\",\"datalist_type\":\"row_action\",\"header_label\":\"\",\"href\":\"#request.baseURL#\\/web\\/userview\",\"hrefColumn\":\"apps.id;id\",\"hrefParam\":\";\",\"label\":\"Hyperlink\",\"link-css-display-type\":\"btn btn-sm btn-primary\",\"rules\":[],\"target\":\"_blank\",\"style-custom\":\"box-shadow: none;\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{}}}}],\"rowActions\":[{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"hidden\":\"true\",\"id\":\"rowAction_0\",\"label\":\"Hyperlink\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{\"hidden\":\"\"}},\"properties\":{\"confirmation\":\"\",\"datalist_type\":\"row_action\",\"header_label\":\"\",\"href\":\"#request.baseURL#\\/web\\/console\\/app\",\"hrefColumn\":\"apps.id;apps.version;builders\",\"hrefParam\":\";;\",\"label\":\"<i class=\\\"fas fa-pen\\\"><\\/i> \",\"link-css-display-type\":\"btn btn-sm btn-primary\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{}},\"rules\":[{\"join\":\"AND\",\"field\":\"apps.editable\",\"operator\":\"=\",\"value\":\"true\"}],\"style-border-radius\":\"50%\",\"style-custom\":\"transition: 0.2s display ease-in-out;\",\"style-display\":\"none\",\"style-height\":\"30px\",\"style-position\":\"absolute\",\"style-right\":\"10px\",\"style-top\":\"10px\",\"style-width\":\"30px\",\"target\":\"_blank\",\"hidden\":\"\"}}],\"searchPopup\":\"\",\"showDataWhenFilterSet\":\"\",\"template\":{\"className\":\"org.joget.apps.datalist.lib.AppIconTemplate\",\"properties\":{\"columns_mobile\":\"col-12\",\"columns_tablet\":\"col-md-6\",\"columns_desktop\":\"col-lg-2\",\"splitListByColumn\":\"\",\"enableSuperApp\":\"\"}},\"useSession\":\"\",\"column_image\":[{\"action\":{\"className\":\"\",\"properties\":{}},\"alignment\":\"\",\"datalist_type\":\"column\",\"exclude_export\":\"\",\"filterable\":true,\"format\":{\"className\":\"org.joget.apps.datalist.lib.ImageFormatter\",\"properties\":{\"defaultImage\":\"#request.baseURL#\\/web\\/userview\\/screenshot\\/appcenter\\/v\",\"height\":\"90px\",\"imageSrc\":\"\",\"imagefullsize\":\"\",\"width\":\"90px\"}},\"headerAlignment\":\"\",\"hidden\":\"false\",\"id\":\"column_0\",\"label\":\"imageUrl\",\"name\":\"imageUrl\",\"permission_rules\":{\"9D3F667778064DF2976BDBE2C629EA8D\":{\"exclude_export\":\"true\",\"hidden\":\"false\",\"include_export\":\"\"}},\"renderHtml\":\"\",\"sortable\":\"false\",\"style\":\"\",\"style-border-radius\":\"10px\",\"width\":\"\"}]}','2025-04-13 01:05:12','2025-04-13 01:09:34'),
('crm_community',1,'crm_account_list','Account Listing',NULL,'{\"id\":\"crm_account_list\",\"name\":\"Account Listing\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[{\"id\":\"rowAction_0\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_list\",\"target\":\"_self\",\"hrefParam\":\"d-6304176-fn_account\",\"hrefColumn\":\"id\",\"label\":\"Contacts\",\"confirmation\":\"\",\"visible\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"},{\"id\":\"rowAction_1\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"fk_account\",\"hrefColumn\":\"id\",\"label\":\"New Contact\",\"confirmation\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"}],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"label\":\"Add Account\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_account\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_0\",\"label\":\"Account Name\",\"name\":\"accountName\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_account\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"id\",\"label\":\"ID\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"ID\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_1\",\"name\":\"accountName\",\"label\":\"Account Name\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_5\",\"name\":\"country\",\"label\":\"Country\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_4\",\"name\":\"state\",\"label\":\"State\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}]}','2024-04-18 14:03:07','2024-04-18 14:03:07'),
('crm_community',1,'crm_contact_list','Contact List',NULL,'{\"id\":\"crm_contact_list\",\"name\":\"Contact List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"label\":\"Add Contact\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_contact\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_2\",\"name\":\"account\",\"filterParamName\":\"d-6304176-fn_account\",\"label\":\"Account\",\"type\":{\"className\":\"\",\"properties\":{}},\"datalist_type\":\"filter\",\"hidden\":\"\"},{\"id\":\"filter_0\",\"name\":\"fullName\",\"label\":\"First Name\",\"type\":{\"className\":\"org.joget.apps.datalist.lib.TextFieldDataListFilterType\",\"properties\":{\"defaultValue\":\"\"}},\"filterParamName\":\"d-6304176-fn_fullName\"},{\"id\":\"filter_1\",\"label\":\"Last Name\",\"name\":\"lastName\",\"filterParamName\":\"d-6304176-fn_lastName\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_contact\"}},\"columns\":[{\"id\":\"column_2\",\"name\":\"account\",\"label\":\"Account\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"account\",\"label\":\"Account\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}},\"datalist_type\":\"column\",\"renderHtml\":\"\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\"},{\"id\":\"column_0\",\"name\":\"fullName\",\"label\":\"First Name\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Full Name\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_4\",\"name\":\"lastName\",\"label\":\"Last Name\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}],\"template\":{\"className\":\"\",\"properties\":{}}}','2024-04-18 14:03:07','2024-04-18 14:03:07'),
('crm_community',1,'crm_opportunity_list','Opportunity List',NULL,'{\"id\":\"crm_opportunity_list\",\"name\":\"Opportunity List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"opportunity_new\",\"target\":\"_self\",\"hrefParam\":\"\",\"hrefColumn\":\"\",\"label\":\"Add Opportunity\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_opportunity\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_1\",\"name\":\"account\",\"label\":\"Account\",\"type\":{\"className\":\"\",\"properties\":{}},\"filterParamName\":\"d-4412661-fn_account\",\"datalist_type\":\"filter\",\"hidden\":\"\"},{\"id\":\"filter_0\",\"label\":\"Title\",\"name\":\"title\",\"filterParamName\":\"d-4412661-fn_title\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_opportunity\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"title\",\"label\":\"Title\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"opportunity_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Title\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_4\",\"name\":\"account\",\"label\":\"Account\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"account\",\"label\":\"Account\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}},\"datalist_type\":\"column\",\"renderHtml\":\"\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\"},{\"id\":\"column_2\",\"name\":\"amount\",\"label\":\"Amount\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_3\",\"name\":\"stage\",\"label\":\"Stage\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_1\",\"name\":\"dateModified\",\"label\":\"Date Modified\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}],\"template\":{\"className\":\"\",\"properties\":{}}}','2024-04-18 14:03:07','2024-04-18 14:03:07'),
('crm_community',1,'Proposal','Proposal List',NULL,'{\"id\":\"Proposal\",\"name\":\"Proposal List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"refNo\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_0\",\"properties\":{\"href\":\"proposal_process\",\"target\":\"_self\",\"label\":\"Submit New Proposal\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"name\":\"Form Row Delete Action\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"label\":\"Delete\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"label\":\"Delete\",\"formDefId\":\"crm_proposal_form\",\"confirmation\":\"Are you sure?\",\"deleteGridData\":\"true\",\"deleteSubformData\":\"true\",\"deleteFiles\":\"true\",\"abortRelatedRunningProcesses\":\"true\",\"datalist_type\":\"action\"}}],\"filters\":[{\"id\":\"filter_2\",\"name\":\"account\",\"label\":\"Account\",\"filterParamName\":\"d-3959580-fn_account\",\"datalist_type\":\"filter\",\"type\":{\"className\":\"\",\"properties\":{}},\"hidden\":\"\"},{\"id\":\"filter_1\",\"name\":\"title\",\"label\":\"Title\",\"filterParamName\":\"d-3959580-fn_title\"},{\"id\":\"filter_0\",\"name\":\"status\",\"label\":\"Status\",\"filterParamName\":\"d-3959580-fn_status\"}],\"binder\":{\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_proposal_approval_form\",\"extraCondition\":\"\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"refNo\",\"label\":\"#\",\"sortable\":\"false\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_1\",\"label\":\"Account\",\"displayLabel\":\"Account\",\"name\":\"account\",\"datalist_type\":\"column\",\"sortable\":\"false\",\"renderHtml\":\"\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_2\",\"datalist_type\":\"column\",\"name\":\"title\",\"label\":\"Title\",\"sortable\":\"false\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"ViewProposal\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"View\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_3\",\"label\":\"Description\",\"displayLabel\":\"Description\",\"name\":\"description\"},{\"id\":\"column_4\",\"label\":\"Status\",\"displayLabel\":\"Status\",\"name\":\"status\"},{\"id\":\"column_5\",\"label\":\"Date Modified\",\"displayLabel\":\"Date Modified\",\"name\":\"dateModified\"}],\"template\":{\"className\":\"\",\"properties\":{}}}','2024-04-18 14:03:07','2024-04-18 14:03:07');
/*!40000 ALTER TABLE `app_datalist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_env_variable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('crm_community',1,'crm_account','Account Form','2024-04-18 14:03:02','2024-04-18 14:03:02','crm_account','{\n    \"className\": \"org.joget.apps.form.model.Form\",\n    \"properties\": {\n        \"id\": \"crm_account\",\n        \"loadBinder\": {\n            \"className\": \"org.joget.apps.form.lib.WorkflowFormBinder\",\n            \"properties\": {}\n        },\n        \"tableName\": \"crm_account\",\n        \"description\": \"\",\n        \"name\": \"Account Form\",\n        \"storeBinder\": {\n            \"className\": \"org.joget.apps.form.lib.WorkflowFormBinder\",\n            \"properties\": {}\n        }\n    },\n    \"elements\": [\n        {\n            \"elements\": [\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"id\",\n                                \"label\": \"Account ID\",\n                                \"size\": \"\",\n                                \"readonly\": \"\",\n                                \"validator\": {\n                                    \"className\": \"org.joget.apps.form.lib.DefaultValidator\",\n                                    \"properties\": {\n                                        \"mandatory\": \"true\",\n                                        \"type\": \"\"\n                                    }\n                                },\n                                \"workflowVariable\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"accountName\",\n                                \"label\": \"Account Name\",\n                                \"size\": \"\",\n                                \"readonly\": \"\",\n                                \"validator\": {\n                                    \"className\": \"org.joget.apps.form.lib.DefaultValidator\",\n                                    \"properties\": {\n                                        \"mandatory\": \"true\",\n                                        \"type\": \"\"\n                                    }\n                                },\n                                \"workflowVariable\": \"\"\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"99%\"\n                    }\n                }\n            ],\n            \"className\": \"org.joget.apps.form.model.Section\",\n            \"properties\": {\n                \"id\": \"account_details\",\n                \"loadBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"visibilityControl\": \"\",\n                \"visibilityValue\": \"\",\n                \"storeBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"label\": \"Account Details\"\n            }\n        },\n        {\n            \"elements\": [\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextArea\",\n                            \"properties\": {\n                                \"id\": \"address\",\n                                \"cols\": \"20\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"Address\",\n                                \"readonly\": \"\",\n                                \"rows\": \"5\"\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"49%\"\n                    }\n                },\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"city\",\n                                \"workflowVariable\": \"\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"City\",\n                                \"readonly\": \"\",\n                                \"size\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"state\",\n                                \"workflowVariable\": \"\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"State\",\n                                \"readonly\": \"\",\n                                \"size\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.SelectBox\",\n                            \"properties\": {\n                                \"id\": \"country\",\n                                \"workflowVariable\": \"\",\n                                \"optionsBinder\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"Country\",\n                                \"multiple\": \"\",\n                                \"readonly\": \"\",\n                                \"size\": \"\",\n                                \"options\": [\n                                    {\n                                        \"value\": \"\",\n                                        \"label\": \"\"\n                                    },\n                                    {\n                                        \"value\": \"local\",\n                                        \"label\": \"Local\"\n                                    },\n                                    {\n                                        \"value\": \"international\",\n                                        \"label\": \"International\"\n                                    }\n                                ]\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"49%\"\n                    }\n                }\n            ],\n            \"className\": \"org.joget.apps.form.model.Section\",\n            \"properties\": {\n                \"id\": \"address\",\n                \"loadBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"visibilityControl\": \"\",\n                \"visibilityValue\": \"\",\n                \"storeBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"label\": \"Address Details\"\n            }\n        }\n    ]\n}',NULL),
('crm_community',1,'crm_contact','Contact Form','2024-04-18 14:03:04','2024-04-18 14:03:04','crm_contact','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_contact\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_contact\",\"name\":\"Contact Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"account\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"extraCondition\":\"\",\"labelColumn\":\"accountName\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[]}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"fullName\",\"workflowVariable\":\"\",\"readonlyLabel\":\"\",\"maxlength\":\"\",\"encryption\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"message\":\"\",\"custom-regex\":\"\",\"mandatory\":\"true\",\"type\":\"\"}},\"value\":\"\",\"label\":\"First Name\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"lastName\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Last Name\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"addressAvailable\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Address Available\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"no\",\"label\":\"No\"},{\"value\":\"yes\",\"label\":\"Yes\"}]}},{\"className\":\"org.joget.apps.form.lib.FileUpload\",\"properties\":{\"id\":\"photo\",\"label\":\"Photo\",\"readonly\":\"\",\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"contact_details\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Contact Details\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"id\":\"address\",\"cols\":\"20\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{}},\"label\":\"Address\",\"readonly\":\"\",\"rows\":\"5\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"city\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"City\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"state\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"State\",\"readonly\":\"\",\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"address_details\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"addressAvailable\",\"visibilityValue\":\"yes\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Address Details\"}}]}',NULL),
('crm_community',1,'crm_opportunity','Opportunity Form','2024-04-18 14:03:04','2024-04-18 14:03:04','crm_opportunity','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_opportunity\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_opportunity\",\"name\":\"Opportunity Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"title\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"mandatory\":\"true\",\"type\":\"\"}},\"label\":\"Title\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"id\":\"description\",\"cols\":\"15\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Description\",\"readonly\":\"\",\"rows\":\"5\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"amount\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Amount\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"stage\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Stage\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"\",\"label\":\"\"},{\"value\":\"open\",\"label\":\"Open\"},{\"value\":\"won\",\"label\":\"Won\"},{\"value\":\"lost\",\"label\":\"Lost\"}]}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"source\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Source\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"\",\"label\":\"\"},{\"value\":\"direct\",\"label\":\"Direct\"},{\"value\":\"indirect\",\"label\":\"Indirect\"}]}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"opportunity\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Opportunity\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"newAccount\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{}},\"label\":\"New Account\",\"readonly\":\"\",\"multiple\":\"\",\"options\":[{\"value\":\"yes\",\"label\":\"Yes\"},{\"value\":\"no\",\"label\":\"No\"}],\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"accountChoice\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"account\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"extraCondition\":\"\",\"labelColumn\":\"accountName\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[]}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"account_existing\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"newAccount\",\"visibilityValue\":\"no\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Existing Account\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"account\",\"formDefId\":\"crm_account\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"account\",\"readonly\":\"\"}},{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"id\":\"script1\",\"validator\":{\"className\":\"\",\"properties\":{}},\"value\":\"<script>\\nvar val = $(\\\"#account_crm_accountid\\\").val();\\nif (val != \'\') {\\n    $(\\\"#newAccount\\\").val(\\\"no\\\");\\n    $(\\\"#newAccount\\\").trigger(\\\"change\\\");\\n}\\n<\\/script>\",\"label\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"account_new\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"newAccount\",\"visibilityValue\":\"yes\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\"}}]}',NULL),
('crm_community',1,'crm_proposal_approval_form','Proposal Approval Form','2024-04-18 14:03:04','2024-04-18 14:03:04','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Approval Form\",\"id\":\"crm_proposal_approval_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"proposal\",\"label\":\"\",\"formDefId\":\"crm_proposal_form\",\"readonly\":\"true\",\"readonlyLabel\":\"\",\"noframe\":\"\",\"parentSubFormId\":\"\",\"subFormParentId\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"permissionHidden\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Approval\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"type\":\"\",\"mandatory\":\"true\"}},\"multiple\":\"\",\"options\":[{\"label\":\"Approved\",\"value\":\"approved\"},{\"label\":\"Resubmit\",\"value\":\"resubmit\"},{\"label\":\"Rejected\",\"value\":\"rejected\"}],\"workflowVariable\":\"status\",\"id\":\"status\",\"label\":\"Status\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"comments\",\"label\":\"Approver Comments\",\"rows\":\"5\",\"cols\":\"20\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section2\",\"label\":\"Approver Action\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),
('crm_community',1,'crm_proposal_form','Proposal Form','2024-04-18 14:03:05','2024-04-18 14:03:05','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Form\",\"id\":\"crm_proposal_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.IdGeneratorField\",\"properties\":{\"hidden\":\"true\",\"format\":\"????\",\"workflowVariable\":\"\",\"envVariable\":\"refNo\",\"id\":\"refNo\",\"label\":\"Reference No\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"labelColumn\":\"accountName\",\"extraCondition\":\"\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"multiple\":\"\",\"options\":[],\"workflowVariable\":\"\",\"id\":\"account\",\"label\":\"Account\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"type\":\"\",\"mandatory\":\"true\"}},\"workflowVariable\":\"\",\"id\":\"title\",\"label\":\"Title\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"workflowVariable\":\"\",\"id\":\"description\",\"label\":\"Description\",\"placeholder\":\"\",\"rows\":\"5\",\"value\":\"\",\"cols\":\"60\",\"readonlyLabel\":\"\"}},{\"className\":\"org.joget.apps.form.lib.FileUpload\",\"properties\":{\"attachment\":\"true\",\"readonly\":\"\",\"size\":\"\",\"id\":\"attachment\",\"label\":\"Attachment\"}},{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"id\":\"field6\",\"value\":\"#i18n.form_hints#\",\"label\":\"&nbsp;\",\"autoPopulate\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Form\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),
('crm_community',1,'crm_proposal_resubmit_form','Proposal Resubmit Form','2024-04-18 14:03:05','2024-04-18 14:03:05','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_proposal_resubmit_form\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\",\"name\":\"Proposal Resubmit Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"approval\",\"formDefId\":\"crm_proposal_approval_form\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"\",\"readonly\":\"true\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section1\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Proposal Resubmit\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"proposal\",\"formDefId\":\"crm_proposal_form\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"\",\"readonly\":\"\"}},{\"className\":\"org.joget.apps.form.lib.HiddenField\",\"properties\":{\"id\":\"status\",\"workflowVariable\":\"status\",\"value\":\"pending\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section2\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Proposal Resubmission\"}}]}',NULL),
('crm_community',1,'crm_proposal_sending_form','Proposal Sending Form','2024-04-18 14:03:05','2024-04-18 14:03:05','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Sending Form\",\"id\":\"crm_proposal_sending_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"parentSubFormId\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"readonly\":\"true\",\"formDefId\":\"crm_proposal_approval_form\",\"subFormParentId\":\"\",\"id\":\"approval\",\"label\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"readonlyLabel\":\"\",\"noframe\":\"\",\"permissionHidden\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section1\",\"label\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"permission\":{\"className\":\"\",\"properties\":{}},\"permissionReadonly\":\"\",\"comment\":\"\",\"join\":\"\",\"reverse\":\"\",\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"regex\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"notes\",\"label\":\"Notes\",\"rows\":\"5\",\"cols\":\"20\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section2\",\"label\":\"Others\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"join\":\"\",\"reverse\":\"\",\"regex\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"permissionReadonly\":\"\",\"comment\":\"\"}}]}',NULL);
/*!40000 ALTER TABLE `app_form` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_form_data_audit_trail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('crm_community',1,'form_hints_en_US','form_hints','en_US','<i>Tasks and emails will be forwarded to \'admin\' user for approval. \nYou can change the settings <a href=\"#request.contextPath?html#/web/console/app/crm_community/1/processes/process1\" target=\"_blank\">here</a><br/></i>'),
('crm_community',1,'form_hints_zh_CN','form_hints','zh_CN','<i>‰ªªÂä°ÂíåÁîµÂ≠êÈÇÆ‰ª∂Â∞ÜËΩ¨ÂèëÁªô‚ÄúÁÆ°ÁêÜÂëò‚ÄùÁî®Êà∑‰ª•‰æõÊâπÂáÜ„ÄÇ<br/>\nÊÇ®ÂèØ‰ª•Âú®<a href=\"#request.contextPath?html#/web/console/app/crm_community/1/processes/process1\" target=\"_blank\">Ê≠§Â§Ñ</a> Êõ¥ÊîπËÆæÁΩÆ„ÄÇ</i>'),
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
('crm_community',1,'page_Proposals Inbox_header_en_US','page_Proposals Inbox_header','en_US','Reminder to administrator for email notification to work:<br/>\n<ul>\n\n<li>Input the all users users email address in <a href=\"#request.contextPath?html#/web/console/directory/users\" target=\"_blank\">Setup Users</a>.</li>\n<li>Input your email SMTP credentials into the <a href=\"#request.contextPath?html#/web/console/setting/general\" target=\"_blank\">General Settings</a>.</li>\n</ul>'),
('crm_community',1,'page_Proposals Inbox_header_zh_CN','page_Proposals Inbox_header','zh_CN','ÁªôJogetÁÆ°ÁêÜÂëò‰ΩøÁî®ÁîµÂ≠êÈÇÆ‰ª∂ÁöÑÊèêÁ§∫Ôºö<br/>\n<ul>\n\n<li>Âú® <a href=\"#request.contextPath?html#/web/console/directory/users\" target=\"_blank\">Á≥ªÁªüËÆæÁΩÆ > ËÆæÁΩÆÁî®Êà∑</a> ‰∏≠ËæìÂÖ•ÊâÄÊúâÁî®Êà∑ÁöÑÁîµÂ≠êÈÇÆ‰ª∂Âú∞ÂùÄ„ÄÇ</li>\n<li>Âú® <a href=\"#request.contextPath?html#/web/console/setting/general\" target=\"_blank\">Á≥ªÁªüËÆæÁΩÆ > Â∏∏ËßÑËÆæÁΩÆ</a> ‰∏≠ËæìÂÖ•ÊÇ®ÁöÑJoget SMTPÊúçÂä°Âô®ÈÖçÁΩÆ‰ª•ÂèëÈÄÅÁîµÂ≠êÈÇÆ‰ª∂„ÄÇ</li>\n</ ul>\n'),
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('crm_community',1,'CRM Community','2025-09-03 15:55:43','2025-09-03 15:55:43','crm_community',1);
/*!40000 ALTER TABLE `app_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package_activity_form`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
('process1','activity1','crm_community',1,'process1::activity1','SINGLE','crm_proposal_resubmit_form',NULL,NULL,0x01,0x00),
('process1','approve_proposal','crm_community',1,'process1::approve_proposal','SINGLE','crm_proposal_approval_form',NULL,NULL,0x01,0x00),
('process1','runProcess','crm_community',1,'process1::runProcess','SINGLE','crm_proposal_form',NULL,NULL,0x01,0x00),
('process1','send_proposal','crm_community',1,'process1::send_proposal','SINGLE','crm_proposal_sending_form',NULL,NULL,0x01,0x00);
/*!40000 ALTER TABLE `app_package_activity_form` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package_activity_plugin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_package_activity_plugin` (
  `processDefId` varchar(255) NOT NULL,
  `activityDefId` varchar(255) NOT NULL,
  `packageId` varchar(255) NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) DEFAULT NULL,
  `pluginName` varchar(255) DEFAULT NULL,
  `pluginProperties` longtext DEFAULT NULL,
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
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_package_participant` (
  `processDefId` varchar(255) NOT NULL,
  `participantId` varchar(255) NOT NULL,
  `packageId` varchar(255) NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `value` text DEFAULT NULL,
  `pluginProperties` longtext DEFAULT NULL,
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
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_plugin_default` (
  `appId` varchar(255) NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) NOT NULL,
  `pluginName` varchar(255) DEFAULT NULL,
  `pluginDescription` text DEFAULT NULL,
  `pluginProperties` longtext DEFAULT NULL,
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('appcenter',1,'home','App Center',NULL,'{\"className\":\"org.joget.apps.userview.model.Userview\",\"categories\":[{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"cacheAllLinks\":\"\",\"customHeader\":\"\",\"style-custom\":\"min-height:50vh;\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{},\"672748e2-473c-589e-fa09-02076835d1d0\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"keyName\":\"\",\"customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"<i class=\\\"zmdi zmdi-apps\\\"><\\/i> Home\",\"customId\":\"home\",\"buttonPosition\":\"bottomLeft\",\"datalistId\":\"applist\",\"checkboxPosition\":\"left\",\"selectionType\":\"multiple\",\"userviewCacheDuration\":\"20\",\"cacheListAction\":\"\",\"userviewCacheScope\":\"\",\"id\":\"8C70B71371B942B6D48A7E9B4C1DB8D1\",\"rowCount\":\"\"}}],\"properties\":{\"hide\":\"\",\"permission_rules\":{\"58F9FBEA76264BE037991759BC1645A2\":{\"hide\":\"\",\"permissionDeny\":\"\"},\"672748e2-473c-589e-fa09-02076835d1d0\":{},\"8db6b45e-e6ff-8830-256f-337603d1bd21\":{}},\"comment\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"id\":\"category-CB52FE21C5EE44FC35A4BDB7A4DCDF53\",\"label\":\"Home\"}}],\"properties\":{\"logoutText\":\"Logout\",\"welcomeMessage\":\"Empowering Open Innovation\",\"name\":\"App Center\",\"description\":\"\",\"footerMessage\":\"#i18n.Powered by Joget#\",\"id\":\"home\"},\"setting\":{\"properties\":{\"__\":\"\",\"userviewDescription\":\"\",\"userviewId\":\"home\",\"hideThisUserviewInAppCenter\":\"true\",\"permission_rules\":[{\"permission_key\":\"8db6b45e-e6ff-8830-256f-337603d1bd21\",\"permission\":{\"className\":\"org.joget.plugin.enterprise.SystemManagerUserviewPermission\",\"properties\":{}},\"permission_name\":\"Is System Manager\"},{\"permission_key\":\"672748e2-473c-589e-fa09-02076835d1d0\",\"permission\":{\"className\":\"org.joget.plugin.enterprise.AppCreatorUserviewPermission\",\"properties\":{}},\"permission_name\":\"Is App Creator\"},{\"permission_key\":\"58F9FBEA76264BE037991759BC1645A2\",\"permission\":{\"className\":\"org.joget.plugin.enterprise.AdminUserviewPermission\",\"properties\":{}},\"permission_name\":\"Is Admin\"}],\"userview_thumbnail\":\"\",\"userview_category\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"theme\":{\"className\":\"org.joget.apps.userview.lib.AjaxUniversalTheme\",\"properties\":{\"dx8navBadge\":\"\",\"css\":\"<\\/style>\\n<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"#request.baseURL?html#/css/appCenter8.css?build=#platform.build?html#\\\" />\\n<style>\\n#dataList_applist > form > div > div.cards.row > div > div > h5 > div{\\n    box-shadow: rgba(17, 17, 26, 0.1) 0px 1px 0px;\\n    border-radius: 20%;\\n}\\n#home #dataList_applist .card-icon:hover .card-actions a{\\n    display:block !important;\\n}\",\"breadcrumb-style-display\":\"none\",\"footer-style-display\":\"none\",\"loginBackground\":\"\",\"disablePush\":\"\",\"dx8navLinkColor\":\"#7E8299\",\"removeAssignmentTitle\":\"\",\"shortcutLinkLabel\":\"Shortcut\",\"disableHelpGuide\":\"\",\"logo\":\"#request.contextPath#/images/appCenterLogo.png\",\"enableResponsiveSwitch\":\"true\",\"dx8headerFontColor\":\"\",\"fav_icon\":\"\",\"dx8navLinkIcon\":\"#7E8299\",\"horizontal_menu\":\"\",\"profile\":\"\",\"homeAttractBanner\":\"\",\"dx8headingBgColor\":\"\",\"dx8linkColor\":\"#01A9F4\",\"homeUrl\":\"\",\"dx8linkActiveColor\":\"#007BFF\",\"disablePwa\":\"\",\"dx8footerColor\":\"\",\"subheader\":\"\",\"dx8contentbackground\":\"\",\"loginPageBottom\":\"<\\/div>\\n<\\/div>\\n<div class=\\\"d-flex flex-lg-row-fluid w-lg-50 bgi-size-cover bgi-position-center order-1 order-lg-2\\\" style=\\\"background-image: url(#request.contextPath#/images/appCenterLoginPageBackground.png);background-size: cover;\\\">\\n\\t\\t\\t\\t\\t<!--begin::Content-->\\n\\t\\t\\t\\t\\t<div class=\\\"d-flex flex-column flex-center py-7 py-lg-15 px-5 px-md-15 w-100\\\">\\n\\t\\t\\t\\t\\t\\t<!--begin::Logo-->\\n\\t\\t\\t\\t\\t\\t<img alt=\\\"Logo\\\" src=\\\"#request.contextPath#/images/appCenterLoginPageLogo.png\\\" class=\\\"h-60px h-lg-75px d-lg-block mx-auto w-275px w-md-50 w-xl-500px mb-10 mb-lg-20\\\">\\n\\t\\t\\t\\t\\t\\t<!--end::Logo-->\\n\\t\\t\\t\\t\\t\\t<!--begin::Image-->\\n\\t\\t\\t\\t\\t\\t<img class=\\\"d-none d-lg-block mx-auto w-275px w-md-50 w-xl-500px mb-10 mb-lg-20\\\" src=\\\"#request.contextPath#/images/appCenterLoginPageContent.png\\\" alt=\\\"\\\">\\n\\t\\t\\t\\t\\t\\t<!--end::Image-->\\n\\t\\t\\t\\t\\t\\t<!--begin::Title-->\\n\\t\\t\\t\\t\\t\\t<h1 class=\\\"d-none d-lg-block text-white fs-2qx fw-bolder text-center mb-7\\\">Empowering Open Innovation<\\/h1>\\n\\t\\t\\t\\t\\t\\t<!--end::Title-->\\n\\t\\t\\t\\t\\t\\t<!--begin::Text-->\\n\\t\\t\\t\\t\\t\\t<div class=\\\"d-none d-lg-block text-white fs-base text-center\\\"><strong>Joget DX<\\/strong> combines the best of <strong>process automation, workflow management<\\/strong> and <strong>low code application development<\\/strong> in a simple, flexible and open platform. Joget DX is a modern open source low-code application development platform to build enterprise web apps and automate workflows on the Cloud and Mobile<\\/div>\\n\\t\\t\\t\\t\\t\\t<!--end::Text-->\\n\\t\\t\\t\\t\\t<\\/div>\\n\\t\\t\\t\\t\\t<!--end::Content-->\\n\\t\\t\\t\\t<\\/div>\\n<\\/div>\\n<style>\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > img:nth-child(2){\\n  width: 550px;\\n    height: 350px;\\n    margin: 40px 0px;\\n    margin-bottom: 5.5rem !important;\\n-webkit-filter: drop-shadow(5px 5px 5px #222);\\n    filter: drop-shadow(5px 5px 5px #222);\\n}\\nbody#login #loginForm{\\n position: relative;\\n}\\nbody#login #page #content main{\\npadding-left: 0%;\\n    padding-right: 0%;\\nheight: 100%;\\n}\\n.w-lg-50 {\\n    width: 50%!important;\\n}\\n.d-flex {\\n    height: 100%;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > img.h-60px.h-lg-75px{\\n  width: 200px;\\n height: 55px;\\nmargin-top: 4rem!important;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > img:nth-child(3){\\nwidth: 540px!important;\\nmargin-bottom: 5.5rem!important;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div{\\n    padding-top: 3rem;\\n    padding-bottom: 3rem;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > h2{\\n padding-left: 20px;\\nmargin-bottom: 40px;\\n}\\n#loginForm > table{\\nwidth: 50%;\\n}\\nbody#login #loginForm table td input[type=\'submit\']{\\nwidth: 100%;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > h1{\\nmargin-bottom: 20px;\\n}\\nlabel{\\n    font-weight: 500;\\n}\\n#content > main > div > div.d-flex.flex-column.flex-lg-row-fluid.w-lg-50.p-10.order-2.order-lg-1 > div > div{\\n    position: relative;\\n    top: 23%;\\n}\\nbody#login #loginForm table td input[type=\'submit\'] {\\n    background-color: #32ae93 !important;\\n    font-size: 16px !important;\\n    font-weight: 500 !important;\\n}\\n#login #page{\\nmax-width: 100%;\\n}\\n\\n@media (max-width: 992px) {\\n.w-lg-50 {\\n    width: 100% !important;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2{\\nheight: 20% !important;\\n}\\n#content > main > div > div.d-flex.flex-lg-row-fluid.w-lg-50.bgi-size-cover.bgi-position-center.order-1.order-lg-2 > div > img.h-60px.h-lg-75px{\\nmargin-top: 0rem !important;\\n}\\n}\\n<\\/style>\\n<script>\\n$( document ).ready(function() {\\n   $(\'#loginForm > table > tbody > tr:nth-child(3) > td:nth-child(2) > input\').val(\'Sign In\');\\n});\\n<\\/script>\",\"dx8backgroundImage\":\"\",\"dx8navBadgeText\":\"\",\"dx8colorSchema\":\"#f1f1f1;#FFFFFF;#8D9BCA;#1EA0CB;#253037;#19242b\",\"dx8background\":\"#F5F8FA\",\"dx8navScrollbarTrack\":\"\",\"dx8headerColor\":\"\",\"dx8colorScheme\":\"rgb(249, 250, 251);rgb(59, 67, 72);rgb(3, 169, 244);rgb(110, 206, 251);rgb(255, 255, 255);rgb(255, 255, 255)\",\"dx8navBackground\":\"#FFFFFF\",\"js\":\"<\\/script>\\n<script type=\\\"text/javascript\\\" src=\\\"#request.baseURL?html#/js/appCenter8.js?build=#platform.build?html#\\\"><\\/script>\\n<script>\",\"welcome-message-style-font-weight\":\"500\",\"dx8navActiveIconColor\":\"\",\"dx8navScrollbarThumb\":\"\",\"loginPageTop\":\"<div class=\\\"d-flex flex-column flex-lg-row flex-column-fluid\\\">\\n<div class=\\\"d-flex flex-column flex-lg-row-fluid w-lg-50 p-10 order-2 order-lg-1\\\">\\n<div class=\\\"d-flex flex-center flex-column flex-lg-row-fluid\\\">\",\"urlsToCache\":\"\",\"userMenu\":[],\"dx8buttonColor\":\"\",\"shortcut\":[],\"userImage\":\"\",\"fontControl\":\"\",\"dx8navActiveLinkBackground\":\"\",\"dx8fontColor\":\"\",\"dx8navLinkBackground\":\"\",\"welcome-message-style-font-size\":\"20px\",\"dx8buttonBackground\":\"\",\"dx8contentFontColor\":\"\",\"subfooter\":\"\",\"dx8footerBackground\":\"\",\"brand-name-style-display\":\"none\",\"dx8headingFontColor\":\"\",\"dx8primaryColor\":\"\",\"darkMode\":\"\",\"dx8navActiveLinkColor\":\"#000000\",\"inbox\":\"all\"}},\"userviewName\":\"App Center\"}}}','2025-04-13 01:05:12','2025-04-13 01:08:37'),
('crm_community',1,'crm_userview_sales','CRM',NULL,'{\"layout\":{\"className\":\"org.joget.apps.userview.model.UserviewLayout\",\"properties\":{\"customHeader\":\"\",\"afterMenu\":\"\",\"beforeMenu\":\"\",\"customFooter\":\"\",\"theme\":{\"className\":\"org.joget.apps.userview.lib.DefaultTheme\",\"properties\":{\"css\":\"\",\"js\":\"\"}}}},\"className\":\"org.joget.apps.userview.model.Userview\",\"categories\":[{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"userviewCacheDuration\":\"20\",\"userviewCacheScope\":\"\",\"enableOffline\":\"\",\"id\":\"welcome\",\"label\":\"Welcome\",\"customId\":\"welcome\",\"content\":\"<div id=\\\"left_content\\\">\\n<div style=\\\"margin-right: 10px;\\\">\\n<h4 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-size: inherit; line-height: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; font-weight: bold; color: #042c54;\\\">More Leads, More Sales, More Customers<\\/h4>\\n<h1 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font: inherit; vertical-align: baseline; color: #1f4282;\\\"><span style=\\\"font-size: large;\\\"><strong>Business&nbsp;Customer Relationship Management<\\/strong><\\/span><\\/h1>\\n<p style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\">CRM helps your business communicate with prospects, share sales information, close deals and keep customers happy.<img src=\\\"http://www.joget.org/images/demo/phone_pad.png\\\" alt=\\\"\\\" width=\\\"382\\\" height=\\\"302\\\"><\\/p>\\n<\\/div>\\n<\\/div>\"}}],\"properties\":{\"hide\":\"\",\"permission\":{\"className\":\"org.joget.plugin.enterprise.AnonymousUserviewPermission\",\"properties\":{}},\"id\":\"category-9BE91A55FAAC4B5098841EA9E1994BE6\",\"label\":\"Home\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"cacheAllLinks\":\"\",\"customHeader\":\"<h2>#i18n.Account List#<\\/h2>\",\"keyName\":\"\",\"customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"Account List\",\"customId\":\"account_list\",\"buttonPosition\":\"topLeft\",\"datalistId\":\"crm_account_list\",\"checkboxPosition\":\"left\",\"selectionType\":\"multiple\",\"userviewCacheDuration\":\"20\",\"cacheListAction\":\"\",\"userviewCacheScope\":\"\",\"id\":\"384344BD3E2946D097C6F5F17540C377\",\"rowCount\":\"true\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"formId\":\"crm_account\",\"customHeader\":\"\",\"loadDataWithKey\":\"\",\"redirectUrlOnCancel\":\"account_list\",\"fieldPassoverMethod\":\"append\",\"redirectTargetOnCancel\":\"top\",\"keyName\":\"\",\"customFooter\":\"\",\"label\":\"New Account\",\"paramName\":\"\",\"customId\":\"account_new\",\"messageShowAfterComplete\":\"\",\"userviewCacheDuration\":\"20\",\"showInPopupDialog\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"readonly\":\"\",\"userviewCacheScope\":\"\",\"id\":\"account_form\",\"fieldPassover\":\"\",\"redirectUrlAfterComplete\":\"account_list\",\"readonlyLabel\":\"\"}}],\"properties\":{\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"id\":\"category-7650DEEFC4CC4332AC25871B65BBDD48\",\"label\":\"Accounts\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"cacheAllLinks\":\"\",\"customHeader\":\"<h2>#i18n.Contact List#<\\/h2>\",\"keyName\":\"\",\"customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"Contact List\",\"customId\":\"contact_list\",\"buttonPosition\":\"topLeft\",\"datalistId\":\"crm_contact_list\",\"checkboxPosition\":\"left\",\"selectionType\":\"multiple\",\"userviewCacheDuration\":\"20\",\"cacheListAction\":\"\",\"userviewCacheScope\":\"\",\"id\":\"D86B740C970C4B08B4D5CCD3DC0E9503\",\"rowCount\":\"true\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"formId\":\"crm_contact\",\"customHeader\":\"\",\"loadDataWithKey\":\"\",\"redirectUrlOnCancel\":\"contact_list\",\"fieldPassoverMethod\":\"append\",\"redirectTargetOnCancel\":\"top\",\"keyName\":\"\",\"customFooter\":\"\",\"label\":\"New Contact\",\"paramName\":\"\",\"customId\":\"contact_new\",\"messageShowAfterComplete\":\"\",\"userviewCacheDuration\":\"20\",\"showInPopupDialog\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"readonly\":\"\",\"userviewCacheScope\":\"\",\"id\":\"contact-form\",\"fieldPassover\":\"\",\"redirectUrlAfterComplete\":\"contact_list\",\"readonlyLabel\":\"\"}}],\"properties\":{\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"id\":\"category-E77D2050680D4DB0A85A5C0C3AC1C083\",\"label\":\"Contacts\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"cacheAllLinks\":\"\",\"customHeader\":\"<h2>#i18n.Opportunity List#<\\/h2>\",\"keyName\":\"\",\"customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"Opportunity List\",\"customId\":\"opportunity_list\",\"buttonPosition\":\"topLeft\",\"datalistId\":\"crm_opportunity_list\",\"checkboxPosition\":\"left\",\"selectionType\":\"multiple\",\"userviewCacheDuration\":\"20\",\"cacheListAction\":\"\",\"userviewCacheScope\":\"\",\"id\":\"A074397ABEA94CF78E2E8FA0843AB97B\",\"rowCount\":\"true\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"formId\":\"crm_opportunity\",\"customHeader\":\"\",\"loadDataWithKey\":\"\",\"redirectUrlOnCancel\":\"opportunity_list\",\"fieldPassoverMethod\":\"append\",\"redirectTargetOnCancel\":\"top\",\"keyName\":\"\",\"customFooter\":\"\",\"label\":\"New Opportunity\",\"paramName\":\"\",\"customId\":\"opportunity_new\",\"messageShowAfterComplete\":\"\",\"userviewCacheDuration\":\"20\",\"showInPopupDialog\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"readonly\":\"\",\"userviewCacheScope\":\"\",\"id\":\"0C7E36768A2F46BB945CEC50E62E0BE8\",\"fieldPassover\":\"\",\"redirectUrlAfterComplete\":\"opportunity_list\",\"readonlyLabel\":\"\"}}],\"properties\":{\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"id\":\"category-A12DBDB14B4447A984E6095B77F28B42\",\"label\":\"Opportunities\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"cacheAllLinks\":\"\",\"customHeader\":\"<h2>#i18n.View All Proposals#<\\/h2>\",\"keyName\":\"\",\"customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"View All Proposals\",\"customId\":\"view_all_proposal\",\"buttonPosition\":\"topLeft\",\"datalistId\":\"Proposal\",\"checkboxPosition\":\"left\",\"selectionType\":\"multiple\",\"userviewCacheDuration\":\"20\",\"cacheListAction\":\"\",\"userviewCacheScope\":\"\",\"id\":\"9E98D32002434ABFAABA3649DCA300F5\",\"rowCount\":\"true\"}},{\"className\":\"org.joget.apps.userview.lib.InboxMenu\",\"properties\":{\"appFilter\":\"process\",\"list-customFooter\":\"\",\"cacheAllLinks\":\"\",\"assignment-customHeader\":\"\",\"assignment-customFooter\":\"\",\"enableOffline\":\"\",\"label\":\"Task Inbox\",\"customId\":\"workflow_inbox\",\"buttonPosition\":\"topLeft\",\"userviewCacheDuration\":\"20\",\"processId\":\"process1\",\"userviewCacheScope\":\"\",\"id\":\"AA1445B29D904408B3F2B1B36E469E16\",\"rowCount\":\"true\",\"list-customHeader\":\"<h2>#i18n.Task Inbox#<\\/h2>\\r\\n#i18n.page_Proposals Inbox_header#\"}},{\"className\":\"org.joget.apps.userview.lib.RunProcess\",\"properties\":{\"runProcessDirectly\":\"Yes\",\"fieldPassoverMethod\":\"append\",\"keyName\":\"\",\"label\":\"Submit New Proposal\",\"paramName\":\"\",\"customId\":\"proposal_process\",\"messageShowAfterComplete\":\"\",\"userviewCacheDuration\":\"20\",\"showInPopupDialog\":\"\",\"userviewCacheScope\":\"\",\"runProcessSubmitLabel\":\"\",\"id\":\"2D27B3875F234315A7A3562BD0E35AB2\",\"fieldPassover\":\"\",\"processDefId\":\"process1\",\"redirectUrlAfterComplete\":\"view_all_proposal\"}}],\"properties\":{\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"id\":\"category-78EC0B8A1E8E483A93770714BB0D6F6E\",\"label\":\"Proposal Process\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"userviewCacheDuration\":\"20\",\"userviewCacheScope\":\"\",\"enableOffline\":\"\",\"id\":\"765269E3926049B0A21D16581EE188DF\",\"label\":\"About\",\"customId\":\"about\",\"content\":\"<div id=\\\"left_content\\\">\\n<div style=\\\"float: left; width: 400px; margin-right: 10px;\\\">#i18n.aboutpage#<\\/div>\\n<\\/div>\"}}],\"properties\":{\"hide\":\"\",\"comment\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"id\":\"category-8739F2859D894A339404C2404CB9004E\",\"label\":\"a\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"formId\":\"crm_proposal_approval_form\",\"customHeader\":\"\",\"loadDataWithKey\":\"\",\"redirectUrlOnCancel\":\"view_all_proposal\",\"fieldPassoverMethod\":\"append\",\"redirectTargetOnCancel\":\"top\",\"keyName\":\"\",\"customFooter\":\"\",\"label\":\"View Proposal\",\"paramName\":\"\",\"customId\":\"ViewProposal\",\"messageShowAfterComplete\":\"\",\"userviewCacheDuration\":\"20\",\"showInPopupDialog\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"readonly\":\"Yes\",\"userviewCacheScope\":\"\",\"id\":\"1A2E6106918040F484C342E1BB12B2A3\",\"fieldPassover\":\"\",\"redirectUrlAfterComplete\":\"view_all_proposal\",\"readonlyLabel\":\"\"}}],\"properties\":{\"hide\":\"yes\",\"comment\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"id\":\"category-1AFAC018AFA848F2970403061E49EE72\",\"label\":\"Hidden\"}}],\"properties\":{\"logoutText\":\"Logout\",\"welcomeMessage\":\"Sales Force Automation\",\"name\":\"#i18n.applicationName#\",\"description\":\"\",\"footerMessage\":\"Powered by Joget\",\"id\":\"crm_userview_sales\"},\"setting\":{\"className\":\"org.joget.apps.userview.model.UserviewSetting\",\"properties\":{\"__\":\"\",\"userviewDescription\":\"\",\"userviewId\":\"crm_userview_sales\",\"hideThisUserviewInAppCenter\":\"\",\"userview_thumbnail\":\"#appResource.crm.png#\",\"userview_category\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"theme\":{\"className\":\"org.joget.plugin.enterprise.Dx8ColorAdminTheme\",\"properties\":{\"dx8navBadge\":\"\",\"css\":\"\",\"primaryColor\":\"DARKROYALBLUE\",\"loginBackground\":\"\",\"disablePush\":\"\",\"dx8navLinkColor\":\"\",\"removeAssignmentTitle\":\"\",\"shortcutLinkLabel\":\"Shortcut\",\"disableHelpGuide\":\"\",\"logo\":\"\",\"enableResponsiveSwitch\":\"true\",\"dx8headerFontColor\":\"\",\"fav_icon\":\"\",\"dx8navLinkIcon\":\"\",\"horizontal_menu\":\"\",\"accentColor\":\"BLUE\",\"profile\":\"\",\"homeAttractBanner\":\"\",\"dx8headingBgColor\":\"\",\"dx8linkColor\":\"\",\"homeUrl\":\"\",\"dx8linkActiveColor\":\"\",\"disablePwa\":\"\",\"dx8footerColor\":\"\",\"subheader\":\"\",\"dx8contentbackground\":\"\",\"loginPageBottom\":\"\",\"dx8backgroundImage\":\"\",\"themeScheme\":\"light\",\"dx8navBadgeText\":\"\",\"dx8background\":\"\",\"dx8headerColor\":\"\",\"dx8colorScheme\":\"rgb(233, 233, 233);rgb(255, 255, 255);rgb(157, 235, 249);rgb(0, 114, 82);rgb(0, 131, 143);rgb(0, 86, 98)\",\"dx8navBackground\":\"\",\"buttonColor\":\"GREY\",\"displayCategoryLabel\":\"\",\"js\":\"\",\"dx8navActiveIconColor\":\"\",\"dx8navScrollbarThumb\":\"\",\"loginPageTop\":\"\",\"urlsToCache\":\"\",\"userMenu\":[],\"dx8buttonColor\":\"\",\"shortcut\":[],\"userImage\":\"\",\"fontControl\":\"\",\"buttonTextColor\":\"WHITE\",\"dx8navActiveLinkBackground\":\"\",\"dx8fontColor\":\"\",\"menuFontColor\":\"BLACK\",\"dx8navLinkBackground\":\"\",\"dx8buttonBackground\":\"\",\"dx8contentFontColor\":\"\",\"subfooter\":\"\",\"dx8footerBackground\":\"\",\"dx8headingFontColor\":\"\",\"dx8primaryColor\":\"\",\"dx8navActiveLinkColor\":\"\",\"inbox\":\"current\",\"fontColor\":\"WHITE\"}},\"userviewName\":\"CRM\"}}}','2024-04-18 14:03:07','2024-04-18 14:03:07');
/*!40000 ALTER TABLE `app_userview` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_department`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('ROLE_APP_CREATOR','App Creator','App Creator'),
('ROLE_SYSTEM_MANAGER','System Manager','System Manager'),
('ROLE_USER','User','Normal User');
/*!40000 ALTER TABLE `dir_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('G-001','roy'),
('G-001','sasha'),
('G-001','tina'),
('G-002','terry'),
('G-003','julia'),
('G-003','sasha');
/*!40000 ALTER TABLE `dir_user_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user_meta`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dir_user_replacement` (
  `id` varchar(255) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `replacementUser` varchar(255) DEFAULT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `processIds` varchar(255) DEFAULT NULL,
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
(1001200);
/*!40000 ALTER TABLE `objectid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_audit_trail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
('ff808081990e92e701990e9306820000','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=false, createdBy=roleSystem}','2025-09-03 15:55:40',NULL,NULL),
('ff808081990e92e701990e9313310001','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=false, createdBy=roleSystem}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313ca0002','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313cc0003','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313d10004','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313d20005','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313d50006','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313d70007','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313d90008','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313db0009','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e9313dc000a','roleSystem','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','{id=crm_community, version=1}','2025-09-03 15:55:43','crm_community','1'),
('ff808081990e92e701990e93145b000b','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdateAndReturn','{id=crm_community, version=1, published=false, createdBy=roleSystem}','2025-09-03 15:55:44','crm_community','1'),
('ff808081990e92e701990e9319a7000c','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=true, createdBy=roleSystem}','2025-09-03 15:55:45','crm_community','1'),
('ff808081990e92e701990e9327f9000d','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=appcenter, version=1, published=false, createdBy=roleSystem}','2025-09-03 15:55:49',NULL,NULL),
('ff808081990e92e701990e9328c3000e','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdateAndReturn','{id=appcenter, version=1, published=false, createdBy=roleSystem}','2025-09-03 15:55:49',NULL,NULL),
('ff808081990e92e701990e9328e0000f','roleSystem','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=appcenter, version=1, published=true, createdBy=roleSystem}','2025-09-03 15:55:49',NULL,NULL),
('ff808081990e92e701990e9328f20010','roleSystem','org.joget.commons.util.SetupManager','saveSetting','defaultUserview','2025-09-03 15:55:49',NULL,NULL);
/*!40000 ALTER TABLE `wf_audit_trail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_history_activity`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
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
  `variables` longtext DEFAULT NULL,
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
/*!40101 SET character_set_client = utf8mb4 */;
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
  `variables` longtext DEFAULT NULL,
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = utf8mb4 */;
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
('0edd9889-afe2-410b-86d6-66ee0292ba07','CACHE_LAST_CLEAR_jwdb_FORM_COLUMNS_crm_contact','1756886143259',NULL),
('19680856-e857-4337-97e9-ad0942eec8b5','CACHE_LAST_CLEAR_jwdb_FORM_COLUMNS_crm_opportunity','1756886143277',NULL),
('2d80081a-e167-417e-ad34-0896ddec409c','CACHE_LAST_CLEAR_jwdb_FORM_INDEXES_crm_proposal','1756886143337',NULL),
('4028c4ea79850c7c0179850cc3880001','deleteProcessOnCompletion','archive',NULL),
('5ad927ed-6835-4266-ac88-0c6a824f4c9d','setup_lastUpdated','1756886149360',NULL),
('5b8036a0-109b-4dc8-95c5-3b391467e440','defaultUserview','appcenter/home',NULL),
('5f979d84-0e70-4669-8446-f2a2b79854d2','CACHE_LAST_CLEAR_jwdb_FORM_COLUMNS_crm_proposal','1756886143335',NULL),
('6b509e14-b9e7-4af9-93b5-ef26cdfa003a','CACHE_LAST_CLEAR_jwdb_FORM_INDEXES_crm_account','1756886143235',NULL),
('903de565-f5ee-410d-aa09-0163622d2612','CACHE_LAST_CLEAR_jwdb_FORM_INDEXES_crm_contact','1756886143261',NULL),
('918bbdf8-b792-47a9-929c-e9d1c791937e','CACHE_LAST_CLEAR_jwdb_FORM_INDEXES_crm_opportunity','1756886143280',NULL),
('dbe4ec78-201b-439f-bdb9-60c8d5efcc04','CACHE_LAST_CLEAR_jwdb_FORM_COLUMNS_crm_account','1756886143231',NULL);
/*!40000 ALTER TABLE `wf_setup` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-09-03 15:56:05
