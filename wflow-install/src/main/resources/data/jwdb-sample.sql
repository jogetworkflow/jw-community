-- MySQL dump 10.16  Distrib 10.3.9-MariaDB, for Win32 (AMD64)
--
-- Host: localhost    Database: jwdb
-- ------------------------------------------------------
-- Server version	10.3.9-MariaDB

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
-- Table structure for table `app_app`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_app` (
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `published` bit(1) DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `license` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `meta` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_app`
--

LOCK TABLES `app_app` WRITE;
/*!40000 ALTER TABLE `app_app` DISABLE KEYS */;
INSERT INTO `app_app` VALUES ('appcenter',1,'App Center','','2019-10-06 22:11:27','2019-10-06 22:11:32','oRIgWuw8ed5OmS98TSZFxocskOFXU0v3VPneM0k80NqSBK2r6RhNzTNTryZkuj4W',NULL,NULL),('crm_community',1,'CRM Community','','2019-10-06 22:06:02','2019-10-06 22:06:13','Vfe6Df5AdlrGEPqFYgTt8XuGJ4XiTt2NnBLEovw3qoQ=',NULL,NULL);
/*!40000 ALTER TABLE `app_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_builder`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_builder` (
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `json` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `idx_name` (`name`),
  KEY `idx_type` (`type`),
  CONSTRAINT `FK_idup4nrrc79iy4kc46wf5919j` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_builder`
--

LOCK TABLES `app_builder` WRITE;
/*!40000 ALTER TABLE `app_builder` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_builder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_datalist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_datalist` (
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `json` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK5E9247A6462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FK5E9247A6462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_datalist`
--

LOCK TABLES `app_datalist` WRITE;
/*!40000 ALTER TABLE `app_datalist` DISABLE KEYS */;
INSERT INTO `app_datalist` VALUES ('crm_community',1,'crm_account_list','Account Listing',NULL,'{\"id\":\"crm_account_list\",\"name\":\"Account Listing\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[{\"id\":\"rowAction_0\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_list\",\"target\":\"_self\",\"hrefParam\":\"d-6304176-fn_account\",\"hrefColumn\":\"id\",\"label\":\"Contacts\",\"confirmation\":\"\",\"visible\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"},{\"id\":\"rowAction_1\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"type\":\"text\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"fk_account\",\"hrefColumn\":\"id\",\"label\":\"New Contact\",\"confirmation\":\"\"},\"name\":\"Data List Hyperlink\",\"label\":\"Hyperlink\"}],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"label\":\"Add Account\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_account\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_0\",\"label\":\"Account Name\",\"name\":\"accountName\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_account\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"id\",\"label\":\"ID\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"ID\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_1\",\"name\":\"accountName\",\"label\":\"Account Name\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_5\",\"name\":\"country\",\"label\":\"Country\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_4\",\"name\":\"state\",\"label\":\"State\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}]}','2019-10-07 05:06:05','2019-10-07 05:06:05'),('crm_community',1,'crm_contact_list','Contact List',NULL,'{\"id\":\"crm_contact_list\",\"name\":\"Contact List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"label\":\"Add Contact\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_contact\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_2\",\"name\":\"account\",\"filterParamName\":\"d-6304176-fn_account\",\"label\":\"Account\",\"type\":{\"className\":\"org.joget.plugin.enterprise.SelectBoxDataListFilterType\",\"properties\":{\"multiple\":\"autocomplete\",\"size\":\"\",\"defaultValue\":\"\",\"options\":[],\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"idColumn\":\"\",\"labelColumn\":\"accountName\",\"groupingColumn\":\"\",\"extraCondition\":\"\",\"addEmptyOption\":\"\",\"emptyLabel\":\"\",\"useAjax\":\"\"}}}}},{\"id\":\"filter_0\",\"name\":\"fullName\",\"label\":\"First Name\",\"type\":{\"className\":\"org.joget.apps.datalist.lib.TextFieldDataListFilterType\",\"properties\":{\"defaultValue\":\"\"}}},{\"id\":\"filter_1\",\"label\":\"Last Name\",\"name\":\"lastName\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_contact\"}},\"columns\":[{\"id\":\"column_2\",\"name\":\"account\",\"label\":\"Account\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"account\",\"label\":\"Account\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"org.joget.plugin.enterprise.OptionsValueFormatter\",\"properties\":{\"options\":[],\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"idColumn\":\"\",\"labelColumn\":\"accountName\",\"groupingColumn\":\"\",\"extraCondition\":\"\",\"addEmptyOption\":\"\",\"emptyLabel\":\"\",\"useAjax\":\"\"}}}}},{\"id\":\"column_0\",\"name\":\"fullName\",\"label\":\"First Name\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"contact_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Full Name\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_4\",\"name\":\"lastName\",\"label\":\"Last Name\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}]}','2019-10-07 05:06:05','2019-10-07 05:06:05'),('crm_community',1,'crm_opportunity_list','Opportunity List',NULL,'{\"id\":\"crm_opportunity_list\",\"name\":\"Opportunity List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"dateCreated\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"href\":\"opportunity_new\",\"target\":\"_self\",\"label\":\"Add Opportuntity\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"id\":\"action_0\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"type\":\"text\",\"properties\":{\"formDefId\":\"crm_opportunity\"},\"name\":\"Form Row Delete\",\"label\":\"Delete Row\"}],\"filters\":[{\"id\":\"filter_1\",\"name\":\"account\",\"label\":\"Account\",\"type\":{\"className\":\"org.joget.plugin.enterprise.SelectBoxDataListFilterType\",\"properties\":{\"multiple\":\"textfield\",\"size\":\"\",\"defaultValue\":\"\",\"options\":[],\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"idColumn\":\"\",\"labelColumn\":\"accountName\",\"groupingColumn\":\"\",\"extraCondition\":\"\",\"addEmptyOption\":\"\",\"emptyLabel\":\"\",\"useAjax\":\"\"}}}}},{\"id\":\"filter_0\",\"label\":\"Title\",\"name\":\"title\"}],\"binder\":{\"name\":\"\",\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_opportunity\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"title\",\"label\":\"Title\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"opportunity_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"Title\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_4\",\"name\":\"account\",\"label\":\"Account\",\"sortable\":\"true\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"account_new\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"account\",\"label\":\"Account\",\"confirmation\":\"\",\"visible\":\"\"}},\"format\":{\"className\":\"org.joget.plugin.enterprise.OptionsValueFormatter\",\"properties\":{\"options\":[],\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"idColumn\":\"\",\"labelColumn\":\"accountName\",\"groupingColumn\":\"\",\"extraCondition\":\"\",\"addEmptyOption\":\"\",\"emptyLabel\":\"\",\"useAjax\":\"\"}}}}},{\"id\":\"column_2\",\"name\":\"amount\",\"label\":\"Amount\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_3\",\"name\":\"stage\",\"label\":\"Stage\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"},{\"id\":\"column_1\",\"name\":\"dateModified\",\"label\":\"Date Modified\",\"sortable\":\"true\",\"filterable\":\"true\",\"action\":\"\",\"formats\":\";\"}]}','2019-10-07 05:06:06','2019-10-07 05:06:06'),('crm_community',1,'Proposal','Proposal List',NULL,'{\"id\":\"Proposal\",\"name\":\"Proposal List\",\"pageSize\":\"0\",\"order\":\"1\",\"orderBy\":\"refNo\",\"showPageSizeSelector\":\"true\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"buttonPosition\":\"bottomLeft\",\"checkboxPosition\":\"left\",\"useSession\":\"\",\"considerFilterWhenGetTotal\":\"\",\"hidePageSize\":\"\",\"description\":\"\",\"showDataWhenFilterSet\":\"\",\"rowActions\":[],\"actions\":[{\"name\":\"Data List Hyperlink Action\",\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"label\":\"Hyperlink\",\"type\":\"text\",\"id\":\"action_0\",\"properties\":{\"href\":\"proposal_process\",\"target\":\"_self\",\"label\":\"Submit New Proposal\",\"confirmation\":\"\",\"visible\":\"true\",\"datalist_type\":\"action\"}},{\"name\":\"Form Row Delete Action\",\"className\":\"org.joget.apps.datalist.lib.FormRowDeleteDataListAction\",\"label\":\"Delete\",\"type\":\"text\",\"id\":\"action_1\",\"properties\":{\"label\":\"Delete\",\"formDefId\":\"crm_proposal_form\",\"confirmation\":\"Are you sure?\",\"deleteGridData\":\"true\",\"deleteSubformData\":\"true\",\"deleteFiles\":\"true\",\"abortRelatedRunningProcesses\":\"true\",\"datalist_type\":\"action\"}}],\"filters\":[{\"id\":\"filter_2\",\"name\":\"account\",\"label\":\"Account\"},{\"id\":\"filter_1\",\"name\":\"title\",\"label\":\"Title\"},{\"id\":\"filter_0\",\"name\":\"status\",\"label\":\"Status\"}],\"binder\":{\"className\":\"org.joget.apps.datalist.lib.FormRowDataListBinder\",\"properties\":{\"formDefId\":\"crm_proposal_approval_form\",\"extraCondition\":\"\"}},\"columns\":[{\"id\":\"column_0\",\"name\":\"refNo\",\"label\":\"#\",\"sortable\":\"false\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"\",\"properties\":{}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_1\",\"label\":\"Account\",\"displayLabel\":\"Account\",\"name\":\"account\"},{\"id\":\"column_2\",\"datalist_type\":\"column\",\"name\":\"title\",\"label\":\"Title\",\"sortable\":\"false\",\"hidden\":\"false\",\"exclude_export\":\"\",\"width\":\"\",\"style\":\"\",\"alignment\":\"\",\"headerAlignment\":\"\",\"action\":{\"className\":\"org.joget.apps.datalist.lib.HyperlinkDataListAction\",\"properties\":{\"href\":\"ViewProposal\",\"target\":\"_self\",\"hrefParam\":\"id\",\"hrefColumn\":\"id\",\"label\":\"View\",\"confirmation\":\"\"}},\"format\":{\"className\":\"\",\"properties\":{}}},{\"id\":\"column_3\",\"label\":\"Description\",\"displayLabel\":\"Description\",\"name\":\"description\"},{\"id\":\"column_4\",\"label\":\"Status\",\"displayLabel\":\"Status\",\"name\":\"status\"},{\"id\":\"column_5\",\"label\":\"Date Modified\",\"displayLabel\":\"Date Modified\",\"name\":\"dateModified\"}]}','2019-10-07 05:06:06','2019-10-07 05:06:06');
/*!40000 ALTER TABLE `app_datalist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_env_variable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_env_variable` (
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `value` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `remarks` text COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK740A62EC462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FK740A62EC462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_env_variable`
--

LOCK TABLES `app_env_variable` WRITE;
/*!40000 ALTER TABLE `app_env_variable` DISABLE KEYS */;
INSERT INTO `app_env_variable` VALUES ('crm_community',1,'AppName','Customer Relationship Management',NULL),('crm_community',1,'refNo','',NULL);
/*!40000 ALTER TABLE `app_env_variable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_fd`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_fd` (
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `createdBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `createdByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `createdBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `createdByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_country` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_address` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_accountName` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_city` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_state` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `createdBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `createdByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_lastName` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_address` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_city` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_fullName` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_photo` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_state` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_account` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_addressAvailable` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `createdBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `createdByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_amount` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_stage` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_description` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_source` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_newAccount` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_title` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_account` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `createdBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `createdByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedBy` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `modifiedByName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_refNo` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_comments` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_notes` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_attachment` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_description` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_title` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_account` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `c_status` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `formId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `tableName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `json` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`formId`),
  KEY `FK45957822462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FK45957822462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_form`
--

LOCK TABLES `app_form` WRITE;
/*!40000 ALTER TABLE `app_form` DISABLE KEYS */;
INSERT INTO `app_form` VALUES ('appcenter',1,'landing','Published Apps','2019-10-07 05:11:28','2019-10-07 05:11:28','appcenter','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"noPermissionMessage\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"name\":\"Published Apps\",\"description\":\"\",\"postProcessorRunOn\":\"both\",\"permission\":{\"className\":\"\",\"properties\":{}},\"id\":\"landing\",\"postProcessor\":{\"className\":\"\",\"properties\":{}},\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"tableName\":\"appcenter\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"autoPopulate\":\"\",\"id\":\"admin_div\",\"label\":\"\",\"value\":\"<style>\\n#appcenter_admin {\\n    position: absolute;\\n    top: -40px;\\n    text-align: center;\\n    display: block;\\n    margin: auto;\\n    width: 100%;\\n}\\n#appcenter_admin a {\\n    background: white;\\n    border: solid 1px #ddd;\\n    cursor: pointer;\\n    text-decoration: none;\\n    color: #555;\\n    margin-right: 5px;\\n    padding: 10px;\\n    box-shadow: 0 1px 1.5px 1px rgba(0,0,0,.12);\\n    border-radius: 4px;\\n    font-size: 14px;\\n    font-weight: 400;\\n    line-height: 1.42857143;\\n    display: inline-block;\\n}\\n@media (max-width:540px) {\\n    #appcenter_admin {\\n        display: none;\\n    }\\n}\\n<\\/style>\\n<div id=\\\"appcenter_admin\\\">\\n    <a href=\\\"#\\\" onclick=\\\"appCreate();return false\\\">#i18n.Design New App#<\\/a>\\n    <a href=\\\"#\\\" onclick=\\\"appImport();return false\\\">#i18n.Import App#<\\/a>\\n    <a href=\\\"#\\\" onclick=\'AdminBar.showQuickOverlay(\\\"\\/jw\\/web\\/desktop\\/marketplace\\/app?url=\\\" + encodeURIComponent(\\\"https:\\/\\/marketplace.joget.org\\\"));return false\'>#i18n.Download from Marketplace#<\\/a>\\n<\\/div>\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"readonly\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"permissionReadonly\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.BeanShellPermission\",\"properties\":{\"script\":\"import org.joget.workflow.util.WorkflowUtil;\\nreturn !WorkflowUtil.isCurrentUserAnonymous() && WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN);\\n\"}},\"comment\":\"Display admin buttons\",\"id\":\"admin_section\",\"label\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"readonlyLabel\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"id\":\"published_apps\",\"value\":\"<div id=\\\"main-action-help\\\"><i class=\\\"fa fa-info-circle\\\"><\\/i><\\/div>\\n<div id=\\\"search\\\"><\\/div>\\n<ul id=\\\"apps\\\"><\\/ul>\\n<p>\\n<script src=\\\"\\/jw\\/js\\/appCenter7.js\\\"><\\/script>\\n<script>\\nAppCenter.searchFilter($(\\\"#search\\\"), $(\\\"#apps\\\")); \\nAppCenter.loadPublishedApps(\\\"#apps\\\");\\n<\\/script>\\n<\\/p>\",\"label\":\"\",\"autoPopulate\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"readonly\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"permissionReadonly\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"comment\":\"Load and display published app userviews\",\"id\":\"apps_section\",\"label\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"readonlyLabel\":\"\"}}]}',NULL),('crm_community',1,'crm_account','Account Form','2019-10-07 05:06:03','2019-10-07 05:06:03','crm_account','{\n    \"className\": \"org.joget.apps.form.model.Form\",\n    \"properties\": {\n        \"id\": \"crm_account\",\n        \"loadBinder\": {\n            \"className\": \"org.joget.apps.form.lib.WorkflowFormBinder\",\n            \"properties\": {}\n        },\n        \"tableName\": \"crm_account\",\n        \"description\": \"\",\n        \"name\": \"Account Form\",\n        \"storeBinder\": {\n            \"className\": \"org.joget.apps.form.lib.WorkflowFormBinder\",\n            \"properties\": {}\n        }\n    },\n    \"elements\": [\n        {\n            \"elements\": [\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"id\",\n                                \"label\": \"Account ID\",\n                                \"size\": \"\",\n                                \"readonly\": \"\",\n                                \"validator\": {\n                                    \"className\": \"org.joget.apps.form.lib.DefaultValidator\",\n                                    \"properties\": {\n                                        \"mandatory\": \"true\",\n                                        \"type\": \"\"\n                                    }\n                                },\n                                \"workflowVariable\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"accountName\",\n                                \"label\": \"Account Name\",\n                                \"size\": \"\",\n                                \"readonly\": \"\",\n                                \"validator\": {\n                                    \"className\": \"org.joget.apps.form.lib.DefaultValidator\",\n                                    \"properties\": {\n                                        \"mandatory\": \"true\",\n                                        \"type\": \"\"\n                                    }\n                                },\n                                \"workflowVariable\": \"\"\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"99%\"\n                    }\n                }\n            ],\n            \"className\": \"org.joget.apps.form.model.Section\",\n            \"properties\": {\n                \"id\": \"account_details\",\n                \"loadBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"visibilityControl\": \"\",\n                \"visibilityValue\": \"\",\n                \"storeBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"label\": \"Account Details\"\n            }\n        },\n        {\n            \"elements\": [\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextArea\",\n                            \"properties\": {\n                                \"id\": \"address\",\n                                \"cols\": \"20\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"Address\",\n                                \"readonly\": \"\",\n                                \"rows\": \"5\"\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"49%\"\n                    }\n                },\n                {\n                    \"elements\": [\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"city\",\n                                \"workflowVariable\": \"\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"City\",\n                                \"readonly\": \"\",\n                                \"size\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.TextField\",\n                            \"properties\": {\n                                \"id\": \"state\",\n                                \"workflowVariable\": \"\",\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"State\",\n                                \"readonly\": \"\",\n                                \"size\": \"\"\n                            }\n                        },\n                        {\n                            \"className\": \"org.joget.apps.form.lib.SelectBox\",\n                            \"properties\": {\n                                \"id\": \"country\",\n                                \"workflowVariable\": \"\",\n                                \"optionsBinder\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"validator\": {\n                                    \"className\": \"\",\n                                    \"properties\": {}\n                                },\n                                \"label\": \"Country\",\n                                \"multiple\": \"\",\n                                \"readonly\": \"\",\n                                \"size\": \"\",\n                                \"options\": [\n                                    {\n                                        \"value\": \"\",\n                                        \"label\": \"\"\n                                    },\n                                    {\n                                        \"value\": \"local\",\n                                        \"label\": \"Local\"\n                                    },\n                                    {\n                                        \"value\": \"international\",\n                                        \"label\": \"International\"\n                                    }\n                                ]\n                            }\n                        }\n                    ],\n                    \"className\": \"org.joget.apps.form.model.Column\",\n                    \"properties\": {\n                        \"width\": \"49%\"\n                    }\n                }\n            ],\n            \"className\": \"org.joget.apps.form.model.Section\",\n            \"properties\": {\n                \"id\": \"address\",\n                \"loadBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"visibilityControl\": \"\",\n                \"visibilityValue\": \"\",\n                \"storeBinder\": {\n                    \"className\": \"\",\n                    \"properties\": {}\n                },\n                \"label\": \"Address Details\"\n            }\n        }\n    ]\n}',NULL),('crm_community',1,'crm_contact','Contact Form','2019-10-07 05:06:03','2019-10-07 05:06:03','crm_contact','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_contact\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_contact\",\"name\":\"Contact Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"account\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"extraCondition\":\"\",\"labelColumn\":\"accountName\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[]}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"fullName\",\"workflowVariable\":\"\",\"readonlyLabel\":\"\",\"maxlength\":\"\",\"encryption\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"message\":\"\",\"custom-regex\":\"\",\"mandatory\":\"true\",\"type\":\"\"}},\"value\":\"\",\"label\":\"First Name\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"lastName\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Last Name\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"addressAvailable\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Address Available\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"no\",\"label\":\"No\"},{\"value\":\"yes\",\"label\":\"Yes\"}]}},{\"className\":\"org.joget.apps.form.lib.FileUpload\",\"properties\":{\"id\":\"photo\",\"label\":\"Photo\",\"readonly\":\"\",\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"contact_details\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Contact Details\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"id\":\"address\",\"cols\":\"20\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{}},\"label\":\"Address\",\"readonly\":\"\",\"rows\":\"5\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"city\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"City\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"state\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"State\",\"readonly\":\"\",\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"address_details\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"addressAvailable\",\"visibilityValue\":\"yes\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Address Details\"}}]}',NULL),('crm_community',1,'crm_opportunity','Opportunity Form','2019-10-07 05:06:04','2019-10-07 05:06:04','crm_opportunity','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_opportunity\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_opportunity\",\"name\":\"Opportunity Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"title\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"mandatory\":\"true\",\"type\":\"\"}},\"label\":\"Title\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"id\":\"description\",\"cols\":\"15\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Description\",\"readonly\":\"\",\"rows\":\"5\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"id\":\"amount\",\"workflowVariable\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Amount\",\"readonly\":\"\",\"size\":\"\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"stage\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Stage\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"\",\"label\":\"\"},{\"value\":\"open\",\"label\":\"Open\"},{\"value\":\"won\",\"label\":\"Won\"},{\"value\":\"lost\",\"label\":\"Lost\"}]}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"source\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Source\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[{\"value\":\"\",\"label\":\"\"},{\"value\":\"direct\",\"label\":\"Direct\"},{\"value\":\"indirect\",\"label\":\"Indirect\"}]}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"opportunity\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Opportunity\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"newAccount\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{}},\"label\":\"New Account\",\"readonly\":\"\",\"multiple\":\"\",\"options\":[{\"value\":\"yes\",\"label\":\"Yes\"},{\"value\":\"no\",\"label\":\"No\"}],\"size\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}},{\"elements\":[],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"49%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"accountChoice\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"id\":\"account\",\"workflowVariable\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"extraCondition\":\"\",\"labelColumn\":\"accountName\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\",\"multiple\":\"\",\"readonly\":\"\",\"size\":\"\",\"options\":[]}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"account_existing\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"newAccount\",\"visibilityValue\":\"no\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Existing Account\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"account\",\"formDefId\":\"crm_account\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"account\",\"readonly\":\"\"}},{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"id\":\"script1\",\"validator\":{\"className\":\"\",\"properties\":{}},\"value\":\"<script>\\nvar val = $(\\\"#account_crm_accountid\\\").val();\\nif (val != \'\') {\\n    $(\\\"#newAccount\\\").val(\\\"no\\\");\\n    $(\\\"#newAccount\\\").trigger(\\\"change\\\");\\n}\\n<\\/script>\",\"label\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"account_new\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"newAccount\",\"visibilityValue\":\"yes\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Account\"}}]}',NULL),('crm_community',1,'crm_proposal_approval_form','Proposal Approval Form','2019-10-07 05:06:04','2019-10-07 05:06:04','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Approval Form\",\"id\":\"crm_proposal_approval_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"proposal\",\"label\":\"\",\"formDefId\":\"crm_proposal_form\",\"readonly\":\"true\",\"readonlyLabel\":\"\",\"noframe\":\"true\",\"parentSubFormId\":\"\",\"subFormParentId\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}}}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Approval\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"\",\"properties\":{}},\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"type\":\"\",\"mandatory\":\"true\"}},\"multiple\":\"\",\"options\":[{\"label\":\"Approved\",\"value\":\"approved\"},{\"label\":\"Resubmit\",\"value\":\"resubmit\"},{\"label\":\"Rejected\",\"value\":\"rejected\"}],\"workflowVariable\":\"status\",\"id\":\"status\",\"label\":\"Status\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"comments\",\"label\":\"Approver Comments\",\"rows\":\"5\",\"cols\":\"20\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section2\",\"label\":\"Approver Action\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),('crm_community',1,'crm_proposal_form','Proposal Form','2019-10-07 05:06:04','2019-10-07 05:06:04','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Form\",\"id\":\"crm_proposal_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.IdGeneratorField\",\"properties\":{\"hidden\":\"true\",\"format\":\"????\",\"workflowVariable\":\"\",\"envVariable\":\"refNo\",\"id\":\"refNo\",\"label\":\"Reference No\"}},{\"className\":\"org.joget.apps.form.lib.SelectBox\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"optionsBinder\":{\"className\":\"org.joget.apps.form.lib.FormOptionsBinder\",\"properties\":{\"formDefId\":\"crm_account\",\"labelColumn\":\"accountName\",\"extraCondition\":\"\"}},\"validator\":{\"className\":\"\",\"properties\":{}},\"multiple\":\"\",\"options\":[],\"workflowVariable\":\"\",\"id\":\"account\",\"label\":\"Account\"}},{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{\"readonly\":\"\",\"size\":\"\",\"validator\":{\"className\":\"org.joget.apps.form.lib.DefaultValidator\",\"properties\":{\"type\":\"\",\"mandatory\":\"true\"}},\"workflowVariable\":\"\",\"id\":\"title\",\"label\":\"Title\"}},{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"workflowVariable\":\"\",\"id\":\"description\",\"label\":\"Description\",\"placeholder\":\"\",\"rows\":\"5\",\"value\":\"\",\"cols\":\"60\",\"readonlyLabel\":\"\"}},{\"className\":\"org.joget.apps.form.lib.FileUpload\",\"properties\":{\"attachment\":\"true\",\"readonly\":\"\",\"size\":\"\",\"id\":\"attachment\",\"label\":\"Attachment\"}},{\"className\":\"org.joget.apps.form.lib.CustomHTML\",\"properties\":{\"id\":\"field6\",\"value\":\"<i>Tasks and emails will be forwarded to \'admin\' user for approval. \\r\\nYou can change the settings <a href=\\\"/jw/web/console/app/crm_community/1/processes/process1\\\" target=\\\"_blank\\\">here</a><br/></i>\",\"label\":\"&nbsp;\",\"autoPopulate\":\"\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section1\",\"label\":\"Proposal Form\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL),('crm_community',1,'crm_proposal_resubmit_form','Proposal Resubmit Form','2019-10-07 05:06:04','2019-10-07 05:06:04','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"id\":\"crm_proposal_resubmit_form\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\",\"name\":\"Proposal Resubmit Form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"approval\",\"formDefId\":\"crm_proposal_approval_form\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"\",\"readonly\":\"true\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section1\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Proposal Resubmit\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"id\":\"proposal\",\"formDefId\":\"crm_proposal_form\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"subFormParentId\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"\",\"parentSubFormId\":\"\",\"readonly\":\"\"}},{\"className\":\"org.joget.apps.form.lib.HiddenField\",\"properties\":{\"id\":\"status\",\"workflowVariable\":\"status\",\"value\":\"pending\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section2\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityControl\":\"\",\"visibilityValue\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"label\":\"Proposal Resubmission\"}}]}',NULL),('crm_community',1,'crm_proposal_sending_form','Proposal Sending Form','2019-10-07 05:06:04','2019-10-07 05:06:04','crm_proposal','{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"name\":\"Proposal Sending Form\",\"id\":\"crm_proposal_sending_form\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},\"tableName\":\"crm_proposal\"},\"elements\":[{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.SubForm\",\"properties\":{\"parentSubFormId\":\"\",\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}},\"readonly\":\"true\",\"formDefId\":\"crm_proposal_approval_form\",\"subFormParentId\":\"\",\"id\":\"approval\",\"label\":\"\",\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\",\"properties\":{}}}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"id\":\"section1\",\"label\":\"Send Proposal\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"permission\":{\"className\":\"\",\"properties\":{}},\"permissionReadonly\":\"\",\"comment\":\"\"}},{\"elements\":[{\"elements\":[{\"className\":\"org.joget.apps.form.lib.TextArea\",\"properties\":{\"readonly\":\"\",\"validator\":{\"className\":\"\",\"properties\":{}},\"id\":\"notes\",\"label\":\"Notes\",\"rows\":\"5\",\"cols\":\"20\"}}],\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"99%\"}}],\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{\"visibilityControl\":\"\",\"loadBinder\":{\"className\":\"\",\"properties\":{}},\"id\":\"section2\",\"label\":\"\",\"storeBinder\":{\"className\":\"\",\"properties\":{}},\"visibilityValue\":\"\"}}]}',NULL);
/*!40000 ALTER TABLE `app_form` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_form_data_audit_trail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_form_data_audit_trail` (
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appVersion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `formId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `tableName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `action` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `data` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `messageKey` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `locale` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `message` text COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`ouid`),
  KEY `FKEE346FE9462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FKEE346FE9462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_message`
--

LOCK TABLES `app_message` WRITE;
/*!40000 ALTER TABLE `app_message` DISABLE KEYS */;
INSERT INTO `app_message` VALUES ('appcenter',1,'<i class=\'fa fa-home\'></i> Home_zh_CN','<i class=\'fa fa-home\'></i> Home','zh_CN','<i class=\'fa fa-home\'></i> È¶ñÈ°µ'),('appcenter',1,'<i class=\'fa fa-home\'></i> Home_zh_TW','<i class=\'fa fa-home\'></i> Home','zh_TW','<i class=\'fa fa-home\'></i> È¶ñÈ†Å'),('appcenter',1,'App Center_zh_CN','App Center','zh_CN','Â∫îÁî®‰∏≠ÂøÉ'),('appcenter',1,'App Center_zh_TW','App Center','zh_TW','ÊáâÁî®‰∏≠ÂøÉ'),('appcenter',1,'Design New App_zh_CN','Design New App','zh_CN','ËÆæËÆ°Êñ∞Â∫îÁî®Á®ãÂ∫è'),('appcenter',1,'Design New App_zh_TW','Design New App','zh_TW','Ë®≠Ë®àÊñ∞ÊáâÁî®Á®ãÂ∫è'),('appcenter',1,'Download from Marketplace_zh_CN','Download from Marketplace','zh_CN','Â∫îÁî®Â∏ÇÂú∫'),('appcenter',1,'Download from Marketplace_zh_TW','Download from Marketplace','zh_TW','ÊáâÁî®Â∏ÇÂ†¥'),('appcenter',1,'Faster, Simpler Digital Transformation_zh_CN','Faster, Simpler Digital Transformation','zh_CN','Êõ¥Âø´, Êõ¥ÁÆÄÂçïÂú∞ÂÆûÁé∞ ‰ºÅ‰∏öÊï∞Â≠óÂåñËΩ¨Âûã'),('appcenter',1,'Faster, Simpler Digital Transformation_zh_TW','Faster, Simpler Digital Transformation','zh_TW','Êõ¥Âø´, Êõ¥Á∞°ÂñÆÂú∞ÂØ¶Áèæ ‰ºÅÊ•≠Êï∏Â≠óÂåñËΩâÂûã'),('appcenter',1,'Import App_zh_CN','Import App','zh_CN','ÂØºÂÖ•Â∫îÁî®'),('appcenter',1,'Import App_zh_TW','Import App','zh_TW','Â∞éÂÖ•ÊáâÁî®'),('appcenter',1,'Powered by Joget_zh_CN','Powered by Joget','zh_CN','Áî±JogetÊäÄÊúØÊîØÊåÅ'),('appcenter',1,'Powered by Joget_zh_TW','Powered by Joget','zh_TW','Áî±JogetÊäÄË°ìÊîØÊåÅ'),('appcenter',1,'Published Apps_zh_CN','Published Apps','zh_CN','Êú¨Âú∞Â∑≤ÂèëÂ∏É'),('appcenter',1,'Published Apps_zh_TW','Published Apps','zh_TW','Êú¨Âú∞Â∑≤ÁôºÂ∏É');
/*!40000 ALTER TABLE `app_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_package` (
  `packageId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  `appId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appVersion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`packageId`,`packageVersion`),
  KEY `FK852EA428462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FK852EA428462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package`
--

LOCK TABLES `app_package` WRITE;
/*!40000 ALTER TABLE `app_package` DISABLE KEYS */;
INSERT INTO `app_package` VALUES ('crm_community',1,'CRM Community','2019-10-07 05:06:11','2019-10-07 05:06:13','crm_community',1);
/*!40000 ALTER TABLE `app_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package_activity_form`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_package_activity_form` (
  `processDefId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `activityDefId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `formId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `formUrl` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `formIFrameStyle` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `autoContinue` bit(1) DEFAULT NULL,
  `disableSaveAsDraft` bit(1) DEFAULT NULL,
  PRIMARY KEY (`processDefId`,`activityDefId`,`packageId`,`packageVersion`),
  KEY `FKA8D741D5F255BCC` (`packageId`,`packageVersion`),
  CONSTRAINT `FKA8D741D5F255BCC` FOREIGN KEY (`packageId`, `packageVersion`) REFERENCES `app_package` (`packageId`, `packageVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_activity_form`
--

LOCK TABLES `app_package_activity_form` WRITE;
/*!40000 ALTER TABLE `app_package_activity_form` DISABLE KEYS */;
INSERT INTO `app_package_activity_form` VALUES ('process1','activity1','crm_community',1,'process1::activity1','SINGLE','crm_proposal_resubmit_form',NULL,NULL,'','\0'),('process1','approve_proposal','crm_community',1,'process1::approve_proposal','SINGLE','crm_proposal_approval_form',NULL,NULL,'','\0'),('process1','runProcess','crm_community',1,'process1::runProcess','SINGLE','crm_proposal_form',NULL,NULL,'','\0'),('process1','send_proposal','crm_community',1,'process1::send_proposal','SINGLE','crm_proposal_sending_form',NULL,NULL,'','\0');
/*!40000 ALTER TABLE `app_package_activity_form` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package_activity_plugin`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_package_activity_plugin` (
  `processDefId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `activityDefId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pluginName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pluginProperties` text COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`processDefId`,`activityDefId`,`packageId`,`packageVersion`),
  KEY `FKADE8644C5F255BCC` (`packageId`,`packageVersion`),
  CONSTRAINT `FKADE8644C5F255BCC` FOREIGN KEY (`packageId`, `packageVersion`) REFERENCES `app_package` (`packageId`, `packageVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_activity_plugin`
--

LOCK TABLES `app_package_activity_plugin` WRITE;
/*!40000 ALTER TABLE `app_package_activity_plugin` DISABLE KEYS */;
INSERT INTO `app_package_activity_plugin` VALUES ('process1','tool1','crm_community',1,'process1::tool1','org.joget.apps.app.lib.EmailTool','{\"toSpecific\":\"\",\"toParticipantId\":\"approver\",\"cc\":\"\",\"bcc\":\"\",\"subject\":\"Proposal Approved: #form.crm_proposal.title#\",\"message\":\"Proposal Approved\\n\\nRef No: #form.crm_proposal.refNo#\\nTitle: #form.crm_proposal.title#\",\"isHtml\":\"\",\"from\":\"\",\"host\":\"\",\"port\":\"\",\"security\":\"\",\"username\":\"\",\"password\":\"\",\"formDefId\":\"\",\"fields\":[],\"files\":[],\"icsAttachement\":\"\"}'),('process1','tool2','crm_community',1,'process1::tool2','org.joget.apps.app.lib.EmailTool','{\"toSpecific\":\"\",\"toParticipantId\":\"requester\",\"cc\":\"\",\"bcc\":\"\",\"subject\":\"Proposal Rejected: #form.crm_proposal.title#\",\"message\":\"Proposal Rejected\\n\\nRef No: #form.crm_proposal.refNo#\\nTitle: #form.crm_proposal.title#\",\"isHtml\":\"\",\"from\":\"\",\"host\":\"\",\"port\":\"\",\"security\":\"\",\"username\":\"\",\"password\":\"\",\"formDefId\":\"\",\"fields\":[],\"files\":[],\"icsAttachement\":\"\"}');
/*!40000 ALTER TABLE `app_package_activity_plugin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_package_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_package_participant` (
  `processDefId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `participantId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageVersion` bigint(20) NOT NULL,
  `ouid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `value` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `pluginProperties` text COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`processDefId`,`participantId`,`packageId`,`packageVersion`),
  KEY `FK6D7BF59C5F255BCC` (`packageId`,`packageVersion`),
  CONSTRAINT `FK6D7BF59C5F255BCC` FOREIGN KEY (`packageId`, `packageVersion`) REFERENCES `app_package` (`packageId`, `packageVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_package_participant`
--

LOCK TABLES `app_package_participant` WRITE;
/*!40000 ALTER TABLE `app_package_participant` DISABLE KEYS */;
INSERT INTO `app_package_participant` VALUES ('process1','approver','crm_community',1,'process1::approver','requesterHod',NULL,NULL),('process1','processStartWhiteList','crm_community',1,'process1::processStartWhiteList','role','loggedInUser',NULL),('process1','requester','crm_community',1,'process1::requester','requester','runProcess',NULL);
/*!40000 ALTER TABLE `app_package_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_plugin_default`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_plugin_default` (
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `pluginName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pluginDescription` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `pluginProperties` text COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FK7A835713462EF4C7` (`appId`,`appVersion`),
  CONSTRAINT `FK7A835713462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `uuid` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `activityDefId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `activityName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `processUid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK5E33D79C918F93D` (`processUid`),
  CONSTRAINT `FK5E33D79C918F93D` FOREIGN KEY (`processUid`) REFERENCES `app_report_process` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `instanceId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `performer` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `state` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `nameOfAcceptedUser` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `assignmentUsers` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `due` datetime DEFAULT NULL,
  `createdTime` datetime DEFAULT NULL,
  `startedTime` datetime DEFAULT NULL,
  `finishTime` datetime DEFAULT NULL,
  `delay` bigint(20) DEFAULT NULL,
  `timeConsumingFromCreatedTime` bigint(20) DEFAULT NULL,
  `timeConsumingFromStartedTime` bigint(20) DEFAULT NULL,
  `activityUid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `processInstanceId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`instanceId`),
  KEY `FK9C6ABDD8B06E2043` (`activityUid`),
  KEY `FK9C6ABDD8D4610A90` (`processInstanceId`),
  CONSTRAINT `FK9C6ABDD8B06E2043` FOREIGN KEY (`activityUid`) REFERENCES `app_report_activity` (`uuid`),
  CONSTRAINT `FK9C6ABDD8D4610A90` FOREIGN KEY (`processInstanceId`) REFERENCES `app_report_process_instance` (`instanceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `uuid` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appVersion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `uuid` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `packageName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `packageVersion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appUid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FKBD580A19E475ABC` (`appUid`),
  CONSTRAINT `FKBD580A19E475ABC` FOREIGN KEY (`appUid`) REFERENCES `app_report_app` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `uuid` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `processDefId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `processName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `packageUid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FKDAFFF442D40695DD` (`packageUid`),
  CONSTRAINT `FKDAFFF442D40695DD` FOREIGN KEY (`packageUid`) REFERENCES `app_report_package` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `instanceId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `requester` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `state` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `due` datetime DEFAULT NULL,
  `startedTime` datetime DEFAULT NULL,
  `finishTime` datetime DEFAULT NULL,
  `delay` bigint(20) DEFAULT NULL,
  `timeConsumingFromStartedTime` bigint(20) DEFAULT NULL,
  `processUid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`instanceId`),
  KEY `FK351D7BF2918F93D` (`processUid`),
  CONSTRAINT `FK351D7BF2918F93D` FOREIGN KEY (`processUid`) REFERENCES `app_report_process` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `filesize` bigint(20) DEFAULT NULL,
  `permissionClass` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `permissionProperties` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  CONSTRAINT `FK_nnvkg0h6yy8o3f4yjhd20ury0` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_resource`
--

LOCK TABLES `app_resource` WRITE;
/*!40000 ALTER TABLE `app_resource` DISABLE KEYS */;
INSERT INTO `app_resource` VALUES ('appcenter',1,'background-beach.jpg',190718,NULL,'{\"hashvariable\":\"#appResource.background-beach.jpg#\",\"permission\":{\"className\":\"\",\"properties\":{}}}'),('appcenter',1,'background-city.jpg',195420,NULL,'{\"hashvariable\":\"#appResource.background-city.jpg#\",\"permission\":{\"className\":\"\",\"properties\":{}}}'),('appcenter',1,'background-industrial.jpg',208549,NULL,'{\"hashvariable\":\"#appResource.background-industrial.jpg#\",\"permission\":{\"className\":\"\",\"properties\":{}}}'),('appcenter',1,'banner.png',396721,NULL,'{\"hashvariable\":\"#appResource.banner.png#\",\"permission\":{\"className\":\"\",\"properties\":{}}}'),('appcenter',1,'logo.png',7205,NULL,'{\"hashvariable\":\"#appResource.logo.png#\",\"permission\":{\"className\":\"\",\"properties\":{}}}'),('crm_community',1,'crm-icon7.png',29514,NULL,'{\"permission\": { \"className\": \"\", \"properties\": {}}}');
/*!40000 ALTER TABLE `app_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_userview`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_userview` (
  `appId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `appVersion` bigint(20) NOT NULL,
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `json` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `dateCreated` datetime DEFAULT NULL,
  `dateModified` datetime DEFAULT NULL,
  PRIMARY KEY (`appId`,`appVersion`,`id`),
  KEY `FKE411D54E462EF4C7` (`appId`,`appVersion`),
  KEY `idx_name` (`name`),
  CONSTRAINT `FKE411D54E462EF4C7` FOREIGN KEY (`appId`, `appVersion`) REFERENCES `app_app` (`appId`, `appVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_userview`
--

LOCK TABLES `app_userview` WRITE;
/*!40000 ALTER TABLE `app_userview` DISABLE KEYS */;
INSERT INTO `app_userview` VALUES ('appcenter',1,'v','Joget DX',NULL,'{\"className\":\"org.joget.apps.userview.model.Userview\",\"properties\":{\"id\":\"v\",\"name\":\"Joget DX\",\"description\":\"\",\"welcomeMessage\":\"#date.EEE, d MMM yyyy#\",\"logoutText\":\"Logout\",\"footerMessage\":\"#i18n.Powered by Joget#\"},\"setting\":{\"properties\":{\"theme\":{\"className\":\"org.joget.plugin.enterprise.UniversalTheme\",\"properties\":{\"horizontal_menu\":\"horizontal_inline\",\"themeScheme\":\"light\",\"primaryColor\":\"custom\",\"customPrimary\":\"#0084F0\",\"customPrimaryDark\":\"#555555\",\"customPrimaryLight\":\"#FFFFFF\",\"accentColor\":\"BLUE\",\"buttonColor\":\"GREY\",\"buttonTextColor\":\"WHITE\",\"menuFontColor\":\"BLACK\",\"fontColor\":\"WHITE\",\"fav_icon\":\"\",\"logo\":\"\\/jw\\/images\\/v3\\/logo.png\",\"profile\":\"\",\"userImage\":\"\",\"inbox\":\"all\",\"homeUrl\":\"\",\"shortcutLinkLabel\":\"Shortcut\",\"shortcut\":[],\"userMenu\":[],\"enableResponsiveSwitch\":\"true\",\"removeAssignmentTitle\":\"\",\"homeAttractBanner\":\"<div id=\\\"banner\\\">\\n    <h1>#i18n.Faster, Simpler Digital Transformation#<\\/h1>\\n<\\/div>\\n<div id=\\\"brand_logo\\\">\\n    <img src=\\\"#appResource.logo.png#\\\">\\n<\\/div>\\n<div id=\\\"clock\\\"><\\/div>\",\"css\":\"@import url(\\/jw\\/css\\/appCenter7.css);\\n#banner {\\n    background: url(#appResource.background-beach.jpg#);\\n    background-size: cover;\\n}\\n\",\"js\":\"$(function(){\\n    var backgrounds = [\\\"#appResource.background-city.jpg#\\\",\\\"#appResource.background-beach.jpg#\\\", \\\"#appResource.background-industrial.jpg#\\\"];\\n    AppCenter.rotateBackgroundStart(backgrounds, 300);\\n});\",\"subheader\":\"\",\"subfooter\":\"\",\"disableHelpGuide\":\"\",\"disablePwa\":\"\",\"disablePush\":\"\",\"urlsToCache\":\"\\/web\\/json\\/apps\\/published\\/userviews?appCenter=true\",\"loginPageTop\":\"\",\"loginPageBottom\":\"\"}},\"userviewDescription\":\"\",\"userview_thumbnail\":\"\",\"hideThisUserviewInAppCenter\":\"true\",\"permission\":{\"className\":\"\",\"properties\":{}},\"__\":\"\",\"__\":\"\"}},\"categories\":[{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-EE74E4F4426241BD9BC3BC73B1D24AC7\",\"label\":\"<i class=\'fa fa-home\'><\\/i> Home\"},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"28F7ADF73F204C8BBAAD32CF26587AE2\",\"customId\":\"home\",\"label\":\"App Center\",\"formId\":\"landing\",\"showInPopupDialog\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"\",\"redirectUrlOnCancel\":\"\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}]}]}','2019-10-07 05:11:28','2019-10-07 05:11:28'),('appcenter',1,'v2','Joget DX Platform',NULL,'{\"className\":\"org.joget.apps.userview.model.Userview\",\"categories\":[{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"menus\":[{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"formId\":\"landing\",\"customHeader\":\"\",\"loadDataWithKey\":\"\",\"redirectUrlOnCancel\":\"\",\"fieldPassoverMethod\":\"append\",\"redirectTargetOnCancel\":\"top\",\"keyName\":\"\",\"customFooter\":\"\",\"label\":\"App Center\",\"paramName\":\"\",\"customId\":\"home\",\"messageShowAfterComplete\":\"\",\"userviewCacheDuration\":\"20\",\"showInPopupDialog\":\"\",\"submitButtonLabel\":\"\",\"readonly\":\"\",\"cancelButtonLabel\":\"\",\"userviewCacheScope\":\"\",\"id\":\"28F7ADF73F204C8BBAAD32CF26587AE2\",\"redirectUrlAfterComplete\":\"\",\"fieldPassover\":\"\",\"readonlyLabel\":\"\"}}],\"properties\":{\"id\":\"category-EE74E4F4426241BD9BC3BC73B1D24AC7\",\"label\":\"<i class=\'fa fa-home\'><\\/i> Home\"}}],\"properties\":{\"logoutText\":\"Logout\",\"welcomeMessage\":\"#date.EEE, d MMM yyyy#\",\"name\":\"Joget DX Platform\",\"description\":\"\",\"footerMessage\":\"#i18n.Powered by Joget#\",\"id\":\"v2\"},\"setting\":{\"properties\":{\"__\":\"\",\"__\":\"\",\"userviewDescription\":\"\",\"hideThisUserviewInAppCenter\":\"true\",\"userview_thumbnail\":\"\",\"theme\":{\"className\":\"org.joget.plugin.enterprise.UniversalTheme\",\"properties\":{\"horizontal_menu\":\"horizontal_inline\",\"themeScheme\":\"light\",\"primaryColor\":\"custom\",\"customPrimary\":\"#FF7BAC\",\"customPrimaryDark\":\"#FF7BAC\",\"customPrimaryLight\":\"#FFFFFF\",\"accentColor\":\"BLUE\",\"buttonColor\":\"GREY\",\"buttonTextColor\":\"WHITE\",\"menuFontColor\":\"BLACK\",\"fontColor\":\"WHITE\",\"fav_icon\":\"\",\"logo\":\"\\/jw\\/images\\/v3\\/logo.png\",\"profile\":\"\",\"userImage\":\"\",\"inbox\":\"all\",\"shortcutLinkLabel\":\"Shortcut\",\"shortcut\":[],\"userMenu\":[],\"enableResponsiveSwitch\":\"true\",\"removeAssignmentTitle\":\"\",\"homeAttractBanner\":\"<div id=\\\"banner\\\">\\n    <h1>#i18n.Faster, Simpler Digital Transformation#<\\/h1>\\n<\\/div>\\n\",\"css\":\"@import url(\\/jw\\/css\\/appCenter7_banner.css);\\n#banner {\\n    background: url(#appResource.banner.png#) no-repeat center;\\n    background-size: cover;\\n}\\n\",\"js\":\"$(function() {\\n    AppCenter.showNotifications = false;\\n});\",\"subheader\":\"\",\"subfooter\":\"\",\"disableHelpGuide\":\"\",\"disablePwa\":\"\",\"disablePush\":\"\",\"urlsToCache\":\"\\/web\\/json\\/apps\\/published\\/userviews?appCenter=true\",\"loginPageTop\":\"\",\"loginPageBottom\":\"\"}},\"permission\":{\"className\":\"\",\"properties\":{}}}}}','2019-10-07 05:11:29','2019-10-07 05:11:29'),('crm_community',1,'crm_userview_sales','Customer Relationship Management',NULL,'{\"className\":\"org.joget.apps.userview.model.Userview\",\"properties\":{\"id\":\"crm_userview_sales\",\"name\":\"Customer Relationship Management\",\"description\":\"\",\"welcomeMessage\":\"Sales Force Automation\",\"logoutText\":\"Logout\",\"footerMessage\":\"Powered by Joget\"},\"layout\":{\"className\":\"org.joget.apps.userview.model.UserviewLayout\",\"properties\":{\"customHeader\":\"\",\"customFooter\":\"\",\"beforeMenu\":\"\",\"afterMenu\":\"\",\"theme\":{\"className\":\"org.joget.apps.userview.lib.DefaultTheme\",\"properties\":{\"css\":\"\",\"js\":\"\"}}}},\"categories\":[{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-9BE91A55FAAC4B5098841EA9E1994BE6\",\"label\":\"Home\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.plugin.enterprise.AnonymousUserviewPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"id\":\"welcome\",\"customId\":\"welcome\",\"label\":\"Welcome\",\"content\":\"<div id=\\\"left_content\\\">\\n<div style=\\\"margin-right: 10px;\\\">\\n<h4 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-size: inherit; line-height: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; font-weight: bold; color: #042c54;\\\">More Leads, More Sales, More Customers<\\/h4>\\n<h1 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font: inherit; vertical-align: baseline; color: #1f4282;\\\"><span style=\\\"font-size: large;\\\"><strong>Business&nbsp;Customer Relationship Management<\\/strong><\\/span><\\/h1>\\n<p style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\">CRM helps your business communicate with prospects, share sales information, close deals and keep customers happy.<img src=\\\"http:\\/\\/www.joget.org\\/images\\/demo\\/phone_pad.png\\\" alt=\\\"\\\" width=\\\"382\\\" height=\\\"302\\\" \\/><\\/p>\\n<\\/div>\\n<\\/div>\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-7650DEEFC4CC4332AC25871B65BBDD48\",\"label\":\"Accounts\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"id\":\"384344BD3E2946D097C6F5F17540C377\",\"customId\":\"account_list\",\"label\":\"Account List\",\"datalistId\":\"crm_account_list\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"selectionType\":\"multiple\",\"checkboxPosition\":\"left\",\"customHeader\":\"<h2>Account List<\\/h2>\",\"customFooter\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"account_form\",\"customId\":\"account_new\",\"label\":\"New Account\",\"formId\":\"crm_account\",\"showInPopupDialog\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"account_list\",\"redirectUrlOnCancel\":\"account_list\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-E77D2050680D4DB0A85A5C0C3AC1C083\",\"label\":\"Contacts\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"id\":\"D86B740C970C4B08B4D5CCD3DC0E9503\",\"customId\":\"contact_list\",\"label\":\"Contact List\",\"datalistId\":\"crm_contact_list\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"selectionType\":\"multiple\",\"checkboxPosition\":\"left\",\"customHeader\":\"<h2>Contact List<\\/h2>\",\"customFooter\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"contact-form\",\"customId\":\"contact_new\",\"label\":\"New Contact\",\"formId\":\"crm_contact\",\"showInPopupDialog\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"contact_list\",\"redirectUrlOnCancel\":\"contact_list\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-A12DBDB14B4447A984E6095B77F28B42\",\"label\":\"Opportunities\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"id\":\"A074397ABEA94CF78E2E8FA0843AB97B\",\"customId\":\"opportunity_list\",\"label\":\"Opportunity List\",\"datalistId\":\"crm_opportunity_list\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"selectionType\":\"multiple\",\"checkboxPosition\":\"left\",\"customHeader\":\"<h2>Opportunity List<\\/h2>\",\"customFooter\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"0C7E36768A2F46BB945CEC50E62E0BE8\",\"customId\":\"opportunity_new\",\"label\":\"New Opportunity\",\"formId\":\"crm_opportunity\",\"showInPopupDialog\":\"\",\"readonly\":\"\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"opportunity_list\",\"redirectUrlOnCancel\":\"opportunity_list\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"permission\":{\"className\":\"org.joget.apps.userview.model.UserviewPermission\",\"properties\":{}}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-78EC0B8A1E8E483A93770714BB0D6F6E\",\"label\":\"Proposal Process\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}}},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.DataListMenu\",\"properties\":{\"id\":\"9E98D32002434ABFAABA3649DCA300F5\",\"customId\":\"view_all_proposal\",\"label\":\"View All Proposals\",\"datalistId\":\"Proposal\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"selectionType\":\"multiple\",\"checkboxPosition\":\"left\",\"customHeader\":\"<h2>View All Proposals<\\/h2>\",\"customFooter\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.InboxMenu\",\"properties\":{\"id\":\"AA1445B29D904408B3F2B1B36E469E16\",\"customId\":\"workflow_inbox\",\"label\":\"Task Inbox\",\"appFilter\":\"process\",\"processId\":\"process1\",\"rowCount\":\"true\",\"buttonPosition\":\"topLeft\",\"list-customHeader\":\"<h2>Task Inbox<\\/h2>\\r\\nReminder to administrator for email notification to work:<br\\/>\\r\\n<ul>\\r\\n\\r\\n<li>Input the all users users email address in <a href=\\\"\\/jw\\/web\\/console\\/directory\\/users\\\" target=\\\"_blank\\\">Setup Users<\\/a>.<\\/li>\\r\\n<li>Input your email SMTP credentials into the <a href=\\\"\\/jw\\/web\\/console\\/setting\\/general\\\" target=\\\"_blank\\\">General Settings<\\/a>.<\\/li>\\r\\n<\\/ul>\",\"list-customFooter\":\"\",\"assignment-customHeader\":\"\",\"assignment-customFooter\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}},{\"className\":\"org.joget.apps.userview.lib.RunProcess\",\"properties\":{\"id\":\"2D27B3875F234315A7A3562BD0E35AB2\",\"customId\":\"proposal_process\",\"label\":\"Submit New Proposal\",\"processDefId\":\"process1\",\"runProcessDirectly\":\"Yes\",\"showInPopupDialog\":\"\",\"runProcessSubmitLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"view_all_proposal\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"keyName\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}]},{\"menus\":[{\"className\":\"org.joget.apps.userview.lib.HtmlPage\",\"properties\":{\"id\":\"765269E3926049B0A21D16581EE188DF\",\"customId\":\"about\",\"label\":\"About\",\"content\":\"<div id=\\\"left_content\\\">\\n<div style=\\\"float: left; width: 400px; margin-right: 10px;\\\">\\n<h4 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-size: inherit; line-height: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; font-weight: bold; color: #042c54;\\\">More Leads, More Sales, More Customers<\\/h4>\\n<h1 style=\\\"margin: 0px; padding: 0px; border-width: 0px; font: inherit; vertical-align: baseline; color: #1f4282;\\\"><span style=\\\"font-size: large;\\\"><strong>Business&nbsp;Customer Relationship Management<\\/strong><\\/span><\\/h1>\\n<p style=\\\"margin: 0px; padding: 30px 0px 0px; border-width: 0px; font-family: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-size: inherit; font-size-adjust: inherit; font-stretch: inherit; vertical-align: baseline; color: #363636; line-height: 15px;\\\">CRM helps your business communicate with prospects, share sales information, close deals and keep customers happy.<img src=\\\"http:\\/\\/www.joget.org\\/images\\/demo\\/phone_pad.png\\\" alt=\\\"\\\" width=\\\"382\\\" height=\\\"302\\\" \\/><\\/p>\\n<\\/div>\\n<\\/div>\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}],\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-8739F2859D894A339404C2404CB9004E\",\"label\":\"a\",\"hide\":\"\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"comment\":\"\"}},{\"className\":\"org.joget.apps.userview.model.UserviewCategory\",\"properties\":{\"id\":\"category-1AFAC018AFA848F2970403061E49EE72\",\"label\":\"Hidden\",\"hide\":\"yes\",\"permission\":{\"className\":\"org.joget.apps.userview.lib.LoggedInUserPermission\",\"properties\":{}},\"comment\":\"\"},\"menus\":[{\"className\":\"org.joget.apps.userview.lib.FormMenu\",\"properties\":{\"id\":\"1A2E6106918040F484C342E1BB12B2A3\",\"customId\":\"ViewProposal\",\"label\":\"View Proposal\",\"formId\":\"crm_proposal_approval_form\",\"showInPopupDialog\":\"\",\"readonly\":\"Yes\",\"readonlyLabel\":\"\",\"messageShowAfterComplete\":\"\",\"redirectUrlAfterComplete\":\"view_all_proposal\",\"redirectUrlOnCancel\":\"view_all_proposal\",\"redirectTargetOnCancel\":\"top\",\"fieldPassover\":\"\",\"fieldPassoverMethod\":\"append\",\"paramName\":\"\",\"submitButtonLabel\":\"\",\"cancelButtonLabel\":\"\",\"customHeader\":\"\",\"customFooter\":\"\",\"keyName\":\"\",\"loadDataWithKey\":\"\",\"userviewCacheScope\":\"\",\"userviewCacheDuration\":\"20\"}}]}],\"setting\":{\"className\":\"org.joget.apps.userview.model.UserviewSetting\",\"properties\":{\"theme\":{\"className\":\"org.joget.plugin.enterprise.ProgressiveTheme\",\"properties\":{\"horizontal_menu\":\"true\",\"themeScheme\":\"light\",\"primaryColor\":\"DARKROYALBLUE\",\"accentColor\":\"BLUE\",\"buttonColor\":\"GREY\",\"buttonTextColor\":\"WHITE\",\"menuFontColor\":\"BLACK\",\"fontColor\":\"WHITE\",\"fav_icon\":\"\",\"logo\":\"\",\"profile\":\"\",\"userImage\":\"\",\"inbox\":\"current\",\"shortcutLinkLabel\":\"Shortcut\",\"shortcut\":[],\"userMenu\":[],\"enableResponsiveSwitch\":\"true\",\"removeAssignmentTitle\":\"true\",\"homeAttractBanner\":\"\",\"css\":\"\",\"js\":\"\",\"subheader\":\"\",\"subfooter\":\"\",\"disableHelpGuide\":\"\",\"disablePwa\":\"\",\"disablePush\":\"\",\"urlsToCache\":\"\",\"loginPageTop\":\"\",\"loginPageBottom\":\"\"}},\"userviewDescription\":\"\",\"userview_thumbnail\":\"#appResource.crm-icon7.png#\",\"hideThisUserviewInAppCenter\":\"\",\"permission\":{\"className\":\"\",\"properties\":{}},\"__\":\"\",\"__\":\"\"}}}','2019-10-07 05:06:06','2019-10-07 05:06:06');
/*!40000 ALTER TABLE `app_userview` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_department`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_department` (
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `organizationId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `hod` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `parentId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKEEE8AA4418CEBAE1` (`organizationId`),
  KEY `FKEEE8AA44EF6BB2B7` (`parentId`),
  KEY `FKEEE8AA4480DB1449` (`hod`),
  CONSTRAINT `FKEEE8AA4418CEBAE1` FOREIGN KEY (`organizationId`) REFERENCES `dir_organization` (`id`),
  CONSTRAINT `FKEEE8AA4480DB1449` FOREIGN KEY (`hod`) REFERENCES `dir_employment` (`id`),
  CONSTRAINT `FKEEE8AA44EF6BB2B7` FOREIGN KEY (`parentId`) REFERENCES `dir_department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `userId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `employeeCode` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `role` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `gradeId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `departmentId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `organizationId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKC6620ADE716AE35F` (`departmentId`),
  KEY `FKC6620ADE14CE02E9` (`gradeId`),
  KEY `FKC6620ADECE539211` (`userId`),
  KEY `FKC6620ADE18CEBAE1` (`organizationId`),
  CONSTRAINT `FKC6620ADE14CE02E9` FOREIGN KEY (`gradeId`) REFERENCES `dir_grade` (`id`),
  CONSTRAINT `FKC6620ADE18CEBAE1` FOREIGN KEY (`organizationId`) REFERENCES `dir_organization` (`id`),
  CONSTRAINT `FKC6620ADE716AE35F` FOREIGN KEY (`departmentId`) REFERENCES `dir_department` (`id`),
  CONSTRAINT `FKC6620ADECE539211` FOREIGN KEY (`userId`) REFERENCES `dir_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `employmentId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `reportToId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`employmentId`,`reportToId`),
  KEY `FK53622945F4068416` (`reportToId`),
  KEY `FK536229452787E613` (`employmentId`),
  CONSTRAINT `FK536229452787E613` FOREIGN KEY (`employmentId`) REFERENCES `dir_employment` (`id`),
  CONSTRAINT `FK53622945F4068416` FOREIGN KEY (`reportToId`) REFERENCES `dir_employment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `organizationId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKBC9A49A518CEBAE1` (`organizationId`),
  CONSTRAINT `FKBC9A49A518CEBAE1` FOREIGN KEY (`organizationId`) REFERENCES `dir_organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `organizationId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKBC9A804D18CEBAE1` (`organizationId`),
  CONSTRAINT `FKBC9A804D18CEBAE1` FOREIGN KEY (`organizationId`) REFERENCES `dir_organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `parentId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK55A15FA5961BD498` (`parentId`),
  CONSTRAINT `FK55A15FA5961BD498` FOREIGN KEY (`parentId`) REFERENCES `dir_organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `firstName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lastName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `timeZone` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `locale` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `algorithm` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `loginAttempt` int(11) DEFAULT NULL,
  `failedloginAttempt` int(11) DEFAULT NULL,
  `lastLogedInDate` datetime DEFAULT NULL,
  `lockOutDate` datetime DEFAULT NULL,
  `lastPasswordChangeDate` datetime DEFAULT NULL,
  `requiredPasswordChange` bit(1) DEFAULT NULL,
  `noPasswordExpiration` bit(1) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `groupId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `userId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`userId`,`groupId`),
  KEY `FK2F0367FD159B6639` (`groupId`),
  KEY `FK2F0367FDCE539211` (`userId`),
  CONSTRAINT `FK2F0367FD159B6639` FOREIGN KEY (`groupId`) REFERENCES `dir_group` (`id`),
  CONSTRAINT `FK2F0367FDCE539211` FOREIGN KEY (`userId`) REFERENCES `dir_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dir_user_group`
--

LOCK TABLES `dir_user_group` WRITE;
/*!40000 ALTER TABLE `dir_user_group` DISABLE KEYS */;
INSERT INTO `dir_user_group` VALUES ('G-001','clark'),('G-001','david'),('G-001','jack'),('G-003','julia'),('G-001','roy'),('G-001','sasha'),('G-003','sasha'),('G-002','terry'),('G-001','tina');
/*!40000 ALTER TABLE `dir_user_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dir_user_meta`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dir_user_meta` (
  `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `meta_key` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `meta_value` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`username`,`meta_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `salt` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `updatedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `replacementUser` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `processIds` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `roleId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `userId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`userId`,`roleId`),
  KEY `FK5C5FE738C8FE3CA7` (`roleId`),
  KEY `FK5C5FE738CE539211` (`userId`),
  CONSTRAINT `FK5C5FE738C8FE3CA7` FOREIGN KEY (`roleId`) REFERENCES `dir_role` (`id`),
  CONSTRAINT `FK5C5FE738CE539211` FOREIGN KEY (`userId`) REFERENCES `dir_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
-- Table structure for table `shkactivities`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkactivities` (
  `Id` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ActivitySetDefinitionId` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityDefinitionId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `Process` decimal(19,0) NOT NULL,
  `TheResource` decimal(19,0) DEFAULT NULL,
  `PDefName` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessId` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `ResourceId` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `State` decimal(19,0) NOT NULL,
  `BlockActivityId` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `Performer` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IsPerformerAsynchronous` smallint(6) DEFAULT NULL,
  `Priority` int(11) DEFAULT NULL,
  `Name` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `Activated` bigint(20) NOT NULL,
  `ActivatedTZO` bigint(20) NOT NULL,
  `Accepted` bigint(20) DEFAULT NULL,
  `AcceptedTZO` bigint(20) DEFAULT NULL,
  `LastStateTime` bigint(20) NOT NULL,
  `LastStateTimeTZO` bigint(20) NOT NULL,
  `LimitTime` bigint(20) NOT NULL,
  `LimitTimeTZO` bigint(20) NOT NULL,
  `Description` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivities` (`Id`),
  KEY `SHKActivities_TheResource` (`TheResource`),
  KEY `SHKActivities_State` (`State`),
  KEY `I2_SHKActivities` (`Process`,`ActivitySetDefinitionId`,`ActivityDefinitionId`),
  KEY `I3_SHKActivities` (`Process`,`State`),
  CONSTRAINT `SHKActivities_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`),
  CONSTRAINT `SHKActivities_State` FOREIGN KEY (`State`) REFERENCES `shkactivitystates` (`oid`),
  CONSTRAINT `SHKActivities_TheResource` FOREIGN KEY (`TheResource`) REFERENCES `shkresourcestable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkactivitydata` (
  `Activity` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob DEFAULT NULL,
  `VariableValueXML` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `VariableValueVCHAR` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
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
  CONSTRAINT `SHKActivityData_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkactivitydatablobs` (
  `ActivityDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityDataBLOBs` (`ActivityDataWOB`,`OrdNo`),
  CONSTRAINT `SHKActivityDataBLOBs_ActivityDataWOB` FOREIGN KEY (`ActivityDataWOB`) REFERENCES `shkactivitydatawob` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkactivitydatawob` (
  `Activity` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `VariableValueVCHAR` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
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
  CONSTRAINT `SHKActivityDataWOB_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkactivitystateeventaudits` (
  `KeyValue` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `Name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityStateEventAudits` (`KeyValue`),
  UNIQUE KEY `I2_SHKActivityStateEventAudits` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkactivitystates` (
  `KeyValue` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `Name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKActivityStates` (`KeyValue`),
  UNIQUE KEY `I2_SHKActivityStates` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shkactivitystates`
--

LOCK TABLES `shkactivitystates` WRITE;
/*!40000 ALTER TABLE `shkactivitystates` DISABLE KEYS */;
INSERT INTO `shkactivitystates` VALUES ('open.running','open.running',1000001,0),('open.not_running.not_started','open.not_running.not_started',1000003,0),('open.not_running.suspended','open.not_running.suspended',1000005,0),('closed.completed','closed.completed',1000007,0),('closed.terminated','closed.terminated',1000009,0),('closed.aborted','closed.aborted',1000011,0);
/*!40000 ALTER TABLE `shkactivitystates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkandjointable`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkandjointable` (
  `Process` decimal(19,0) NOT NULL,
  `BlockActivity` decimal(19,0) DEFAULT NULL,
  `ActivityDefinitionId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `Activity` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKAndJoinTable` (`CNT`),
  KEY `SHKAndJoinTable_BlockActivity` (`BlockActivity`),
  KEY `I2_SHKAndJoinTable` (`Process`,`BlockActivity`,`ActivityDefinitionId`),
  KEY `I3_SHKAndJoinTable` (`Activity`),
  CONSTRAINT `SHKAndJoinTable_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKAndJoinTable_BlockActivity` FOREIGN KEY (`BlockActivity`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKAndJoinTable_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkassignmenteventaudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ProcessId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ProcessFactoryName` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessFactoryVersion` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityDefinitionId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityDefinitionType` int(11) NOT NULL,
  `ProcessDefinitionId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PackageId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `OldResourceUsername` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `OldResourceName` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NewResourceUsername` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `NewResourceName` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IsAccepted` smallint(6) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKAssignmentEventAudits` (`CNT`),
  KEY `SHKAssignmentEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKAssignmentEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `shkeventtypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkassignmentstable` (
  `Activity` decimal(19,0) NOT NULL,
  `TheResource` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityProcessId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityProcessDefName` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `ResourceId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
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
  CONSTRAINT `FK_183e6adufsi558hl5p4dqkqsx` FOREIGN KEY (`ActivityId`) REFERENCES `shkactivities` (`Id`),
  CONSTRAINT `FK_rnb6mhntls567xpifcfvygkuu` FOREIGN KEY (`ActivityProcessId`) REFERENCES `shkprocesses` (`Id`),
  CONSTRAINT `SHKAssignmentsTable_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKAssignmentsTable_TheResource` FOREIGN KEY (`TheResource`) REFERENCES `shkresourcestable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkcounters` (
  `name` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `the_number` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKCounters` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shkcounters`
--

LOCK TABLES `shkcounters` WRITE;
/*!40000 ALTER TABLE `shkcounters` DISABLE KEYS */;
INSERT INTO `shkcounters` VALUES ('_xpdldata_',2,1000204,0);
/*!40000 ALTER TABLE `shkcounters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkcreateprocesseventaudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkcreateprocesseventaudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ProcessId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ProcessFactoryName` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessFactoryVersion` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessDefinitionId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PackageId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `PActivityId` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PProcessId` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PProcessName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PProcessFactoryName` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PProcessFactoryVersion` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PActivityDefinitionId` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PActivityDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PProcessDefinitionId` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PProcessDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PPackageId` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKCreateProcessEventAudits` (`CNT`),
  KEY `SHKCreateProcessEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKCreateProcessEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `shkeventtypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkdataeventaudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ProcessId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ProcessFactoryName` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessFactoryVersion` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityDefinitionId` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityDefinitionType` int(11) DEFAULT NULL,
  `ProcessDefinitionId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PackageId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKDataEventAudits` (`CNT`),
  KEY `SHKDataEventAudits_TheType` (`TheType`),
  CONSTRAINT `SHKDataEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `shkeventtypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkdeadlines` (
  `Process` decimal(19,0) NOT NULL,
  `Activity` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `TimeLimit` bigint(20) NOT NULL,
  `TimeLimitTZO` bigint(20) NOT NULL,
  `ExceptionName` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `IsSynchronous` smallint(6) NOT NULL,
  `IsExecuted` smallint(6) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKDeadlines` (`CNT`),
  KEY `I2_SHKDeadlines` (`Process`,`TimeLimit`),
  KEY `I3_SHKDeadlines` (`Activity`,`TimeLimit`),
  CONSTRAINT `SHKDeadlines_Activity` FOREIGN KEY (`Activity`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKDeadlines_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkeventtypes` (
  `KeyValue` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `Name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKEventTypes` (`KeyValue`),
  UNIQUE KEY `I2_SHKEventTypes` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkgroupgrouptable` (
  `sub_gid` decimal(19,0) NOT NULL,
  `groupid` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupGroupTable` (`sub_gid`,`groupid`),
  KEY `I2_SHKGroupGroupTable` (`groupid`),
  CONSTRAINT `SHKGroupGroupTable_groupid` FOREIGN KEY (`groupid`) REFERENCES `shkgrouptable` (`oid`),
  CONSTRAINT `SHKGroupGroupTable_sub_gid` FOREIGN KEY (`sub_gid`) REFERENCES `shkgrouptable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkgrouptable` (
  `groupid` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `description` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupTable` (`groupid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkgroupuser` (
  `USERNAME` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkgroupuserpacklevelpart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupUserPackLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKGroupUserPackLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKGroupUserPackLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `shkpacklevelparticipant` (`oid`),
  CONSTRAINT `SHKGroupUserPackLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shkgroupuser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkgroupuserproclevelpart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKGroupUserProcLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKGroupUserProcLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKGroupUserProcLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `shkproclevelparticipant` (`oid`),
  CONSTRAINT `SHKGroupUserProcLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shkgroupuser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkneweventauditdata` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob DEFAULT NULL,
  `VariableValueXML` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `VariableValueVCHAR` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
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
  CONSTRAINT `SHKNewEventAuditData_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `shkdataeventaudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkneweventauditdatablobs` (
  `NewEventAuditDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNewEventAuditDataBLOBs` (`NewEventAuditDataWOB`,`OrdNo`),
  CONSTRAINT `SHKNewEventAuditDataBLOBs_NewEventAuditDataWOB` FOREIGN KEY (`NewEventAuditDataWOB`) REFERENCES `shkneweventauditdatawob` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkneweventauditdatawob` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `VariableValueVCHAR` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
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
  CONSTRAINT `SHKNewEventAuditDataWOB_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `shkdataeventaudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shknextxpdlversions` (
  `XPDLId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `NextVersion` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNextXPDLVersions` (`XPDLId`,`NextVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shknextxpdlversions`
--

LOCK TABLES `shknextxpdlversions` WRITE;
/*!40000 ALTER TABLE `shknextxpdlversions` DISABLE KEYS */;
INSERT INTO `shknextxpdlversions` VALUES ('crm_community','2',1000201,0);
/*!40000 ALTER TABLE `shknextxpdlversions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shknormaluser`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shknormaluser` (
  `USERNAME` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKNormalUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkoldeventauditdata` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob DEFAULT NULL,
  `VariableValueXML` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `VariableValueVCHAR` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
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
  CONSTRAINT `SHKOldEventAuditData_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `shkdataeventaudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkoldeventauditdatablobs` (
  `OldEventAuditDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKOldEventAuditDataBLOBs` (`OldEventAuditDataWOB`,`OrdNo`),
  CONSTRAINT `SHKOldEventAuditDataBLOBs_OldEventAuditDataWOB` FOREIGN KEY (`OldEventAuditDataWOB`) REFERENCES `shkoldeventauditdatawob` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkoldeventauditdatawob` (
  `DataEventAudit` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `VariableValueVCHAR` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
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
  CONSTRAINT `SHKOldEventAuditDataWOB_DataEventAudit` FOREIGN KEY (`DataEventAudit`) REFERENCES `shkdataeventaudits` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkpacklevelparticipant` (
  `PARTICIPANT_ID` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelParticipant` (`PARTICIPANT_ID`,`PACKAGEOID`),
  KEY `SHKPackLevelParticipant_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKPackLevelParticipant_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `shkxpdlparticipantpackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkpacklevelxpdlapp` (
  `APPLICATION_ID` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLApp` (`APPLICATION_ID`,`PACKAGEOID`),
  KEY `SHKPackLevelXPDLApp_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKPackLevelXPDLApp_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `shkxpdlapplicationpackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkpacklevelxpdlapptaappdetail` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppDetail` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppDetail_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetail_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappdetail` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetail_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkpacklevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkpacklevelxpdlapptaappdetusr` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppDetUsr` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappdetailuser` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppDetUsr_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkpacklevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkpacklevelxpdlapptaappuser` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppTAAppUser` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppTAAppUser_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppUser_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappuser` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppTAAppUser_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkpacklevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkpacklevelxpdlapptoolagntapp` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKPackLevelXPDLAppToolAgntApp` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKPackLevelXPDLAppToolAgntApp_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKPackLevelXPDLAppToolAgntApp_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentapp` (`oid`),
  CONSTRAINT `SHKPackLevelXPDLAppToolAgntApp_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkpacklevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkprocessdata` (
  `Process` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValue` mediumblob DEFAULT NULL,
  `VariableValueXML` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `VariableValueVCHAR` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
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
  CONSTRAINT `SHKProcessData_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkprocessdatablobs` (
  `ProcessDataWOB` decimal(19,0) NOT NULL,
  `VariableValue` mediumblob DEFAULT NULL,
  `OrdNo` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessDataBLOBs` (`ProcessDataWOB`,`OrdNo`),
  CONSTRAINT `SHKProcessDataBLOBs_ProcessDataWOB` FOREIGN KEY (`ProcessDataWOB`) REFERENCES `shkprocessdatawob` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkprocessdatawob` (
  `Process` decimal(19,0) NOT NULL,
  `VariableDefinitionId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `VariableType` int(11) NOT NULL,
  `VariableValueXML` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `VariableValueVCHAR` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
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
  CONSTRAINT `SHKProcessDataWOB_Process` FOREIGN KEY (`Process`) REFERENCES `shkprocesses` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkprocessdefinitions` (
  `Name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `PackageId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessDefinitionId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessDefinitionCreated` bigint(20) NOT NULL,
  `ProcessDefinitionVersion` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `State` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessDefinitions` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shkprocessdefinitions`
--

LOCK TABLES `shkprocessdefinitions` WRITE;
/*!40000 ALTER TABLE `shkprocessdefinitions` DISABLE KEYS */;
INSERT INTO `shkprocessdefinitions` VALUES ('crm_community#1#process1','crm_community','process1',1570424771039,'1',0,1000205,0);
/*!40000 ALTER TABLE `shkprocessdefinitions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkprocesses`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkprocesses` (
  `SyncVersion` bigint(20) NOT NULL,
  `Id` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessDefinition` decimal(19,0) NOT NULL,
  `PDefName` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityRequesterId` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityRequesterProcessId` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ResourceRequesterId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ExternalRequesterClassName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `State` decimal(19,0) NOT NULL,
  `Priority` int(11) DEFAULT NULL,
  `Name` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `Created` bigint(20) NOT NULL,
  `CreatedTZO` bigint(20) NOT NULL,
  `Started` bigint(20) DEFAULT NULL,
  `StartedTZO` bigint(20) DEFAULT NULL,
  `LastStateTime` bigint(20) NOT NULL,
  `LastStateTimeTZO` bigint(20) NOT NULL,
  `LimitTime` bigint(20) NOT NULL,
  `LimitTimeTZO` bigint(20) NOT NULL,
  `Description` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcesses` (`Id`),
  KEY `I2_SHKProcesses` (`ProcessDefinition`),
  KEY `I3_SHKProcesses` (`State`),
  KEY `I4_SHKProcesses` (`ActivityRequesterId`),
  KEY `I5_SHKProcesses` (`ResourceRequesterId`),
  CONSTRAINT `SHKProcesses_ProcessDefinition` FOREIGN KEY (`ProcessDefinition`) REFERENCES `shkprocessdefinitions` (`oid`),
  CONSTRAINT `SHKProcesses_State` FOREIGN KEY (`State`) REFERENCES `shkprocessstates` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkprocessrequesters` (
  `Id` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityRequester` decimal(19,0) DEFAULT NULL,
  `ResourceRequester` decimal(19,0) DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessRequesters` (`Id`),
  KEY `I2_SHKProcessRequesters` (`ActivityRequester`),
  KEY `I3_SHKProcessRequesters` (`ResourceRequester`),
  CONSTRAINT `SHKProcessRequesters_ActivityRequester` FOREIGN KEY (`ActivityRequester`) REFERENCES `shkactivities` (`oid`),
  CONSTRAINT `SHKProcessRequesters_ResourceRequester` FOREIGN KEY (`ResourceRequester`) REFERENCES `shkresourcestable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkprocessstateeventaudits` (
  `KeyValue` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `Name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessStateEventAudits` (`KeyValue`),
  UNIQUE KEY `I2_SHKProcessStateEventAudits` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkprocessstates` (
  `KeyValue` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `Name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcessStates` (`KeyValue`),
  UNIQUE KEY `I2_SHKProcessStates` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shkprocessstates`
--

LOCK TABLES `shkprocessstates` WRITE;
/*!40000 ALTER TABLE `shkprocessstates` DISABLE KEYS */;
INSERT INTO `shkprocessstates` VALUES ('open.running','open.running',1000000,0),('open.not_running.not_started','open.not_running.not_started',1000002,0),('open.not_running.suspended','open.not_running.suspended',1000004,0),('closed.completed','closed.completed',1000006,0),('closed.terminated','closed.terminated',1000008,0),('closed.aborted','closed.aborted',1000010,0);
/*!40000 ALTER TABLE `shkprocessstates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkproclevelparticipant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkproclevelparticipant` (
  `PARTICIPANT_ID` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `PROCESSOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelParticipant` (`PARTICIPANT_ID`,`PROCESSOID`),
  KEY `SHKProcLevelParticipant_PROCESSOID` (`PROCESSOID`),
  CONSTRAINT `SHKProcLevelParticipant_PROCESSOID` FOREIGN KEY (`PROCESSOID`) REFERENCES `shkxpdlparticipantprocess` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkproclevelxpdlapp` (
  `APPLICATION_ID` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `PROCESSOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLApp` (`APPLICATION_ID`,`PROCESSOID`),
  KEY `SHKProcLevelXPDLApp_PROCESSOID` (`PROCESSOID`),
  CONSTRAINT `SHKProcLevelXPDLApp_PROCESSOID` FOREIGN KEY (`PROCESSOID`) REFERENCES `shkxpdlapplicationprocess` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkproclevelxpdlapptaappdetail` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppDetail` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppDetail_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetail_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappdetail` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetail_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkproclevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkproclevelxpdlapptaappdetusr` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppDetUsr` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetUsr_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappdetailuser` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppDetUsr_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkproclevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkproclevelxpdlapptaappuser` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppTAAppUser` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppTAAppUser_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppUser_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentappuser` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppTAAppUser_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkproclevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkproclevelxpdlapptoolagntapp` (
  `XPDL_APPOID` decimal(19,0) NOT NULL,
  `TOOLAGENTOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKProcLevelXPDLAppToolAgntApp` (`XPDL_APPOID`,`TOOLAGENTOID`),
  KEY `SHKProcLevelXPDLAppToolAgntApp_TOOLAGENTOID` (`TOOLAGENTOID`),
  CONSTRAINT `SHKProcLevelXPDLAppToolAgntApp_TOOLAGENTOID` FOREIGN KEY (`TOOLAGENTOID`) REFERENCES `shktoolagentapp` (`oid`),
  CONSTRAINT `SHKProcLevelXPDLAppToolAgntApp_XPDL_APPOID` FOREIGN KEY (`XPDL_APPOID`) REFERENCES `shkproclevelxpdlapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkresourcestable` (
  `Username` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `Name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKResourcesTable` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shkresourcestable`
--

LOCK TABLES `shkresourcestable` WRITE;
/*!40000 ALTER TABLE `shkresourcestable` DISABLE KEYS */;
INSERT INTO `shkresourcestable` VALUES ('roleAnonymous',NULL,1000200,0);
/*!40000 ALTER TABLE `shkresourcestable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkstateeventaudits`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkstateeventaudits` (
  `RecordedTime` bigint(20) NOT NULL,
  `RecordedTimeTZO` bigint(20) NOT NULL,
  `TheUsername` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `TheType` decimal(19,0) NOT NULL,
  `ActivityId` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ProcessId` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessName` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ProcessFactoryName` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessFactoryVersion` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `ActivityDefinitionId` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ActivityDefinitionType` int(11) DEFAULT NULL,
  `ProcessDefinitionId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `ProcessDefinitionName` varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PackageId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
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
  CONSTRAINT `SHKStateEventAudits_NewActivityState` FOREIGN KEY (`NewActivityState`) REFERENCES `shkactivitystateeventaudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_NewProcessState` FOREIGN KEY (`NewProcessState`) REFERENCES `shkprocessstateeventaudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_OldActivityState` FOREIGN KEY (`OldActivityState`) REFERENCES `shkactivitystateeventaudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_OldProcessState` FOREIGN KEY (`OldProcessState`) REFERENCES `shkprocessstateeventaudits` (`oid`),
  CONSTRAINT `SHKStateEventAudits_TheType` FOREIGN KEY (`TheType`) REFERENCES `shkeventtypes` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shktoolagentapp` (
  `TOOL_AGENT_NAME` varchar(250) COLLATE utf8_unicode_ci NOT NULL,
  `APP_NAME` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentApp` (`TOOL_AGENT_NAME`,`APP_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shktoolagentappdetail` (
  `APP_MODE` decimal(10,0) NOT NULL,
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppDetail` (`APP_MODE`,`TOOLAGENT_APPOID`),
  KEY `SHKToolAgentAppDetail_TOOLAGENT_APPOID` (`TOOLAGENT_APPOID`),
  CONSTRAINT `SHKToolAgentAppDetail_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `shktoolagentapp` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shktoolagentappdetailuser` (
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppDetailUser` (`TOOLAGENT_APPOID`,`USEROID`),
  KEY `SHKToolAgentAppDetailUser_USEROID` (`USEROID`),
  CONSTRAINT `SHKToolAgentAppDetailUser_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `shktoolagentappdetail` (`oid`),
  CONSTRAINT `SHKToolAgentAppDetailUser_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shktoolagentuser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shktoolagentappuser` (
  `TOOLAGENT_APPOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentAppUser` (`TOOLAGENT_APPOID`,`USEROID`),
  KEY `SHKToolAgentAppUser_USEROID` (`USEROID`),
  CONSTRAINT `SHKToolAgentAppUser_TOOLAGENT_APPOID` FOREIGN KEY (`TOOLAGENT_APPOID`) REFERENCES `shktoolagentapp` (`oid`),
  CONSTRAINT `SHKToolAgentAppUser_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shktoolagentuser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shktoolagentuser` (
  `USERNAME` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `PWD` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKToolAgentUser` (`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkusergrouptable` (
  `userid` decimal(19,0) NOT NULL,
  `groupid` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKUserGroupTable` (`userid`,`groupid`),
  KEY `SHKUserGroupTable_groupid` (`groupid`),
  CONSTRAINT `SHKUserGroupTable_groupid` FOREIGN KEY (`groupid`) REFERENCES `shkgrouptable` (`oid`),
  CONSTRAINT `SHKUserGroupTable_userid` FOREIGN KEY (`userid`) REFERENCES `shkusertable` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkuserpacklevelpart` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKUserPackLevelPart` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKUserPackLevelPart_USEROID` (`USEROID`),
  CONSTRAINT `SHKUserPackLevelPart_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `shkpacklevelparticipant` (`oid`),
  CONSTRAINT `SHKUserPackLevelPart_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shknormaluser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkuserproclevelparticipant` (
  `PARTICIPANTOID` decimal(19,0) NOT NULL,
  `USEROID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKUserProcLevelParticipant` (`PARTICIPANTOID`,`USEROID`),
  KEY `SHKUserProcLevelParticipant_USEROID` (`USEROID`),
  CONSTRAINT `SHKUserProcLevelParticipant_PARTICIPANTOID` FOREIGN KEY (`PARTICIPANTOID`) REFERENCES `shkproclevelparticipant` (`oid`),
  CONSTRAINT `SHKUserProcLevelParticipant_USEROID` FOREIGN KEY (`USEROID`) REFERENCES `shknormaluser` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkusertable` (
  `userid` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `firstname` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lastname` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `passwd` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(254) COLLATE utf8_unicode_ci DEFAULT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKUserTable` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdlapplicationpackage` (
  `PACKAGE_ID` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLApplicationPackage` (`PACKAGE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdlapplicationprocess` (
  `PROCESS_ID` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLApplicationProcess` (`PROCESS_ID`,`PACKAGEOID`),
  KEY `SHKXPDLApplicationProcess_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKXPDLApplicationProcess_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `shkxpdlapplicationpackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdldata` (
  `XPDLContent` longblob DEFAULT NULL,
  `XPDLClassContent` longblob DEFAULT NULL,
  `XPDL` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLData` (`CNT`),
  UNIQUE KEY `I2_SHKXPDLData` (`XPDL`),
  CONSTRAINT `SHKXPDLData_XPDL` FOREIGN KEY (`XPDL`) REFERENCES `shkxpdls` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shkxpdldata`
--

LOCK TABLES `shkxpdldata` WRITE;
/*!40000 ALTER TABLE `shkxpdldata` DISABLE KEYS */;
INSERT INTO `shkxpdldata` VALUES ('<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<Package xmlns=\"http://www.wfmc.org/2002/XPDL1.0\" xmlns:xpdl=\"http://www.wfmc.org/2002/XPDL1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" Id=\"crm_community\" Name=\"CRM Community\" xsi:schemaLocation=\"http://www.wfmc.org/2002/XPDL1.0 http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd\">\r\n    <PackageHeader>\r\n        <XPDLVersion>1.0</XPDLVersion>\r\n        <Vendor/>\r\n        <Created/>\r\n    </PackageHeader>\r\n    <Script Type=\"text/javascript\"/>\r\n    <Participants>\r\n        <Participant Id=\"requester\" Name=\"Requester\">\r\n            <ParticipantType Type=\"ROLE\"/>\r\n        </Participant>\r\n        <Participant Id=\"approver\" Name=\"Approver\">\r\n            <ParticipantType Type=\"ROLE\"/>\r\n        </Participant>\r\n    </Participants>\r\n    <Applications>\r\n        <Application Id=\"default_application\"/>\r\n    </Applications>\r\n    <WorkflowProcesses>\r\n        <WorkflowProcess Id=\"process1\" Name=\"Proposal Approval Process\">\r\n            <ProcessHeader DurationUnit=\"h\"/>\r\n            <DataFields>\r\n                <DataField Id=\"status\" IsArray=\"FALSE\">\r\n                    <DataType>\r\n                        <BasicType Type=\"STRING\"/>\r\n                    </DataType>\r\n                </DataField>\r\n            </DataFields>\r\n            <Activities>\r\n                <Activity Id=\"approve_proposal\" Name=\"Approve Proposal\">\r\n                    <Implementation>\r\n                        <No/>\r\n                    </Implementation>\r\n                    <Performer>approver</Performer>\r\n                    <TransitionRestrictions>\r\n                        <TransitionRestriction>\r\n                            <Join Type=\"XOR\"/>\r\n                        </TransitionRestriction>\r\n                    </TransitionRestrictions>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"205.0000228881836,56.76666259765625\"/>\r\n                    </ExtendedAttributes>\r\n                </Activity>\r\n                <Activity Id=\"approval\" Name=\"Approval\">\r\n                    <Route/>\r\n                    <Performer>approver</Performer>\r\n                    <TransitionRestrictions>\r\n                        <TransitionRestriction>\r\n                            <Split Type=\"XOR\">\r\n                                <TransitionRefs>\r\n                                    <TransitionRef Id=\"transition3\"/>\r\n                                    <TransitionRef Id=\"transition6\"/>\r\n                                    <TransitionRef Id=\"transition5\"/>\r\n                                </TransitionRefs>\r\n                            </Split>\r\n                        </TransitionRestriction>\r\n                    </TransitionRestrictions>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"430,62.79999084472655\"/>\r\n                    </ExtendedAttributes>\r\n                </Activity>\r\n                <Activity Id=\"activity1\" Name=\"Resubmit Proposal\">\r\n                    <Implementation>\r\n                        <No/>\r\n                    </Implementation>\r\n                    <Performer>requester</Performer>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"397,20.787493896484378\"/>\r\n                    </ExtendedAttributes>\r\n                </Activity>\r\n                <Activity Id=\"send_proposal\" Name=\"Send Proposal\">\r\n                    <Implementation>\r\n                        <No/>\r\n                    </Implementation>\r\n                    <Performer>requester</Performer>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"681.0000381469727,99.78333282470703\"/>\r\n                    </ExtendedAttributes>\r\n                </Activity>\r\n                <Activity Id=\"parallel\" Name=\"Parallel\">\r\n                    <Route/>\r\n                    <Performer>approver</Performer>\r\n                    <TransitionRestrictions>\r\n                        <TransitionRestriction>\r\n                            <Split Type=\"AND\">\r\n                                <TransitionRefs>\r\n                                    <TransitionRef Id=\"transition7\"/>\r\n                                    <TransitionRef Id=\"transition8\"/>\r\n                                </TransitionRefs>\r\n                            </Split>\r\n                        </TransitionRestriction>\r\n                    </TransitionRestrictions>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"588,61.599993896484364\"/>\r\n                    </ExtendedAttributes>\r\n                </Activity>\r\n                <Activity Id=\"tool1\" Name=\"Send Approval Notification\">\r\n                    <Implementation>\r\n                        <Tool Id=\"default_application\"/>\r\n                    </Implementation>\r\n                    <Performer>approver</Performer>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"735,63.974993896484364\"/>\r\n                    </ExtendedAttributes>\r\n                </Activity>\r\n                <Activity Id=\"tool2\" Name=\"Send Reject Notification\">\r\n                    <Implementation>\r\n                        <Tool Id=\"default_application\"/>\r\n                    </Implementation>\r\n                    <Performer>approver</Performer>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"approver\"/>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"394,168.39999694824218\"/>\r\n                    </ExtendedAttributes>\r\n                </Activity>\r\n                <Activity Id=\"route1\" Name=\"Route 1\">\r\n                    <Route/>\r\n                    <Performer>requester</Performer>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_PARTICIPANT_ID\" Value=\"requester\"/>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_OFFSET\" Value=\"228.0000228881836,22\"/>\r\n                    </ExtendedAttributes>\r\n                </Activity>\r\n            </Activities>\r\n            <Transitions>\r\n                <Transition From=\"approve_proposal\" Id=\"transition2\" To=\"approval\">\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\r\n                    </ExtendedAttributes>\r\n                </Transition>\r\n                <Transition From=\"approval\" Id=\"transition3\" To=\"activity1\">\r\n                    <Condition Type=\"CONDITION\">status===\'resubmit\'</Condition>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\r\n                    </ExtendedAttributes>\r\n                </Transition>\r\n                <Transition From=\"activity1\" Id=\"transition4\" To=\"approve_proposal\">\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\r\n                    </ExtendedAttributes>\r\n                </Transition>\r\n                <Transition From=\"approval\" Id=\"transition6\" Name=\"approved\" To=\"parallel\">\r\n                    <Condition Type=\"OTHERWISE\"/>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\r\n                    </ExtendedAttributes>\r\n                </Transition>\r\n                <Transition From=\"parallel\" Id=\"transition7\" To=\"send_proposal\">\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\r\n                    </ExtendedAttributes>\r\n                </Transition>\r\n                <Transition From=\"parallel\" Id=\"transition8\" To=\"tool1\">\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\r\n                    </ExtendedAttributes>\r\n                </Transition>\r\n                <Transition From=\"approval\" Id=\"transition5\" To=\"tool2\">\r\n                    <Condition Type=\"CONDITION\">status===\'rejected\'</Condition>\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\r\n                    </ExtendedAttributes>\r\n                </Transition>\r\n                <Transition From=\"route1\" Id=\"transition1\" To=\"approve_proposal\">\r\n                    <ExtendedAttributes>\r\n                        <ExtendedAttribute Name=\"JaWE_GRAPH_TRANSITION_STYLE\" Value=\"NO_ROUTING_ORTHOGONAL\"/>\r\n                    </ExtendedAttributes>\r\n                </Transition>\r\n            </Transitions>\r\n            <ExtendedAttributes>\r\n                <ExtendedAttribute Name=\"JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER\" Value=\"requester;approver\"/>\r\n                <ExtendedAttribute Name=\"JaWE_GRAPH_START_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route1,X_OFFSET=87,Y_OFFSET=28,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULT\"/>\r\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool1,X_OFFSET=901,Y_OFFSET=74,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\r\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=send_proposal,X_OFFSET=849,Y_OFFSET=110,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\r\n                <ExtendedAttribute Name=\"JaWE_GRAPH_END_OF_WORKFLOW\" Value=\"JaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool2,X_OFFSET=579,Y_OFFSET=180,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT\"/>\r\n            </ExtendedAttributes>\r\n        </WorkflowProcess>\r\n    </WorkflowProcesses>\r\n    <ExtendedAttributes>\r\n        <ExtendedAttribute Name=\"EDITING_TOOL\" Value=\"Web Workflow Designer\"/>\r\n        <ExtendedAttribute Name=\"EDITING_TOOL_VERSION\" Value=\"5.0-pre-alpha\"/>\r\n    </ExtendedAttributes>\r\n</Package>\r\n','¨Ì\0sr\0\'org.enhydra.shark.xpdl.elements.Package~+Vm≈Ä~˜\0Z\0isTransientL\0extPkgRefsToIdst\0.Lorg/enhydra/shark/utilities/SequencedHashMap;L\0internalVersiont\0Ljava/lang/String;L\0\nnamespacest\0,Lorg/enhydra/shark/xpdl/elements/Namespaces;xr\0(org.enhydra.shark.xpdl.XMLComplexElement>≤æΩÓ(⁄Ã\0\0xr\05org.enhydra.shark.xpdl.XMLBaseForCollectionAndComplexÂ‡áÓ·ı2\0L\0\nelementMapq\0~\0L\0elementst\0Ljava/util/ArrayList;xr\0!org.enhydra.shark.xpdl.XMLElement#+Bø#˜åº\0Z\0\nisReadOnlyZ\0\nisRequiredL\0nameq\0~\0L\0originalElementHashCodet\0Ljava/lang/Integer;L\0parentt\0#Lorg/enhydra/shark/xpdl/XMLElement;L\0valueq\0~\0xpt\0Packagesr\0java.lang.Integer‚†§˜Åá8\0I\0valuexr\0java.lang.NumberÜ¨ïî‡ã\0\0xp14—pt\0\0sr\0,org.enhydra.shark.utilities.SequencedHashMap.Í\"ì©\"&\0\0xpw\0\0\0\rt\0Idsr\0#org.enhydra.shark.xpdl.XMLAttribute#c›Ä˛ÀM;\0L\0choicesq\0~\0xq\0~\0q\0~\0sq\0~\0h[Âq\0~\0\nt\0\rcrm_communitypt\0Namesq\0~\0\0q\0~\0sq\0~\0,Øíq\0~\0\nt\0\rCRM Communitypt\0\rPackageHeadersr\0-org.enhydra.shark.xpdl.elements.PackageHeadervÈí,ÌíÔ\0\0xq\0~\0\0q\0~\0sq\0~\0Mïq\0~\0\nt\0\0sq\0~\0w\0\0\0t\0XPDLVersionsr\0+org.enhydra.shark.xpdl.elements.XPDLVersion˜\"}◊Y.≈w\0\0xr\0\'org.enhydra.shark.xpdl.XMLSimpleElement¬m›¿éÛ\0\0xq\0~\0q\0~\0!sq\0~\07#ú¿q\0~\0t\01.0t\0Vendorsr\0&org.enhydra.shark.xpdl.elements.Vendor t¯ÍE≥:\0\0xq\0~\0#q\0~\0\'sq\0~\0[›0∑q\0~\0t\0\0t\0Createdsr\0\'org.enhydra.shark.xpdl.elements.Createdz ÙdKª|[\0\0xq\0~\0#q\0~\0,sq\0~\0cÎ`Ûq\0~\0t\0\0t\0Descriptionsr\0+org.enhydra.shark.xpdl.elements.Description€∞73Ê8˚\0\0xq\0~\0#\0q\0~\01sq\0~\0:[∂q\0~\0t\0\0t\0\rDocumentationsr\0-org.enhydra.shark.xpdl.elements.Documentation`ƒ9ª◊yí\0\0xq\0~\0#\0q\0~\06sq\0~\0^Ö•≥q\0~\0t\0\0t\0PriorityUnitsr\0,org.enhydra.shark.xpdl.elements.PriorityUnitò≥õËõ¯‰Â\0\0xq\0~\0#\0q\0~\0;sq\0~\0Z/óq\0~\0t\0\0t\0CostUnitsr\0(org.enhydra.shark.xpdl.elements.CostUnit‹éŸ=H·å\0\0xq\0~\0#\0q\0~\0@sq\0~\0B%-q\0~\0t\0\0xsr\0java.util.ArrayListxÅ“ô«aù\0I\0sizexp\0\0\0w\0\0\0q\0~\0$q\0~\0)q\0~\0.q\0~\03q\0~\08q\0~\0=q\0~\0Bxt\0RedefinableHeadersr\01org.enhydra.shark.xpdl.elements.RedefinableHeaderôøMÎœ™\'H\0\0xq\0~\0\0q\0~\0Gsq\0~\0£fßq\0~\0\nt\0\0sq\0~\0w\0\0\0t\0PublicationStatussq\0~\0\0q\0~\0Msq\0~\0M`ïsq\0~\0It\0\0sq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0UNDER_REVISIONt\0RELEASEDt\0\nUNDER_TESTxt\0Authorsr\0&org.enhydra.shark.xpdl.elements.Author5 ƒf·ßÜ\0\0xq\0~\0#\0q\0~\0Usq\0~\0>∆¸Ìq\0~\0It\0\0t\0Versionsr\0\'org.enhydra.shark.xpdl.elements.Version9=3¥~ªJQ\0\0xq\0~\0#\0q\0~\0Zsq\0~\0rW‚»q\0~\0It\0\0t\0Codepagesr\0(org.enhydra.shark.xpdl.elements.Codepage9$m˜eΩ\rG\0\0xq\0~\0#\0q\0~\0_sq\0~\0/˜üYq\0~\0It\0\0t\0\nCountrykeysr\0*org.enhydra.shark.xpdl.elements.Countrykeyóã.Å–“—\0\0xq\0~\0#\0q\0~\0dsq\0~\0p\"Ê˜q\0~\0It\0\0t\0Responsiblessr\0,org.enhydra.shark.xpdl.elements.Responsibles$ÔÍ{S∆\0\0xr\0$org.enhydra.shark.xpdl.XMLCollectionËèjƒãmÌ∞\0\0xq\0~\0\0q\0~\0isq\0~\0NP/{q\0~\0It\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~\0Nq\0~\0Wq\0~\0\\q\0~\0aq\0~\0fq\0~\0lxt\0ConformanceClasssr\00org.enhydra.shark.xpdl.elements.ConformanceClass◊Ïy0|k´î\0\0xq\0~\0\0q\0~\0rsq\0~\0mÑk∂q\0~\0\nt\0\0sq\0~\0w\0\0\0t\0GraphConformancesq\0~\0\0q\0~\0xsq\0~\0@\'†Óq\0~\0tq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0FULL_BLOCKEDt\0LOOP_BLOCKEDt\0NON_BLOCKEDxxsq\0~\0E\0\0\0w\0\0\0q\0~\0yxt\0Scriptsr\0&org.enhydra.shark.xpdl.elements.ScriptQ¶jÁS„8\0\0xq\0~\0\0q\0~\0Äsq\0~\0Ö@Rq\0~\0\nt\0\0sq\0~\0w\0\0\0t\0Typesq\0~\0q\0~\0Üsq\0~\0FC/q\0~\0Çt\0text/javascriptpt\0Versionsq\0~\0\0q\0~\0äsq\0~\0;Oª”q\0~\0Çt\0\0pt\0Grammarsq\0~\0\0q\0~\0ésq\0~\0/ªòq\0~\0Çt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\0áq\0~\0ãq\0~\0èxt\0ExternalPackagessr\00org.enhydra.shark.xpdl.elements.ExternalPackagesw¿\"+≈®®â\0\0xq\0~\0k\0q\0~\0ìsq\0~\0hûf0q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0TypeDeclarationssr\00org.enhydra.shark.xpdl.elements.TypeDeclarations\r· Ox5Õ\0\0xq\0~\0k\0q\0~\0ösq\0~\0w~.Àq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Participantssr\0,org.enhydra.shark.xpdl.elements.Participantsh`∑g8J\0\0xq\0~\0k\0q\0~\0°sq\0~\0QB%q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0+org.enhydra.shark.xpdl.elements.Participanto$îÛrc§è\0\0xr\0+org.enhydra.shark.xpdl.XMLCollectionElementC¢x”vÅr\0\0xq\0~\0t\0Participantsq\0~\0f`íêq\0~\0£t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0X∑uq\0~\0™t\0	requesterpq\0~\0sq\0~\0\0q\0~\0sq\0~\0Y√\Zóq\0~\0™t\0	Requesterpt\0ParticipantTypesr\0/org.enhydra.shark.xpdl.elements.ParticipantType>àn•›Ö®˜\0\0xq\0~\0q\0~\0µsq\0~\0,$ƒq\0~\0™t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0YjÄq\0~\0∑t\0ROLEsq\0~\0E\0\0\0w\0\0\0t\0RESOURCE_SETt\0RESOURCEt\0ROLEt\0ORGANIZATIONAL_UNITt\0HUMANt\0SYSTEMxxsq\0~\0E\0\0\0w\0\0\0q\0~\0ªxt\0Descriptionsq\0~\02\0q\0~\0∆sq\0~\0;ó!q\0~\0™t\0\0t\0ExternalReferencesr\01org.enhydra.shark.xpdl.elements.ExternalReferenceÜbË∑¡Qœ\0\0xq\0~\0\0q\0~\0 sq\0~\0zŸ%uq\0~\0™t\0\0sq\0~\0w\0\0\0t\0xrefsq\0~\0\0q\0~\0–sq\0~\0p◊“«q\0~\0Ãt\0\0pt\0locationsq\0~\0q\0~\0‘sq\0~\0bàÎ7q\0~\0Ãt\0\0pt\0	namespacesq\0~\0\0q\0~\0ÿsq\0~\0%yﬁÿq\0~\0Ãt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\0—q\0~\0’q\0~\0Ÿxt\0ExtendedAttributessr\02org.enhydra.shark.xpdl.elements.ExtendedAttributesÚ∞Oıü⁄UF\0L\0extAttribsStringq\0~\0xq\0~\0k\0q\0~\0›sq\0~\0∏µèq\0~\0™t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~\0Øq\0~\0≤q\0~\0∑q\0~\0«q\0~\0Ãq\0~\0ﬂxsq\0~\0®t\0Participantsq\0~\0}√ïq\0~\0£t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0{–úq\0~\0Ât\0approverpq\0~\0sq\0~\0\0q\0~\0sq\0~\0\'yq\0~\0Ât\0Approverpt\0ParticipantTypesq\0~\0∂q\0~\0sq\0~\0E`–ˇq\0~\0Ât\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0%‘®Çq\0~\0Òt\0ROLEsq\0~\0E\0\0\0w\0\0\0q\0~\0øq\0~\0¿q\0~\0¡q\0~\0¬q\0~\0√q\0~\0ƒxxsq\0~\0E\0\0\0w\0\0\0q\0~\0ıxt\0Descriptionsq\0~\02\0q\0~\0˙sq\0~\0Dﬁjíq\0~\0Ât\0\0t\0ExternalReferencesq\0~\0À\0q\0~\0˛sq\0~\0jå›≥q\0~\0Ât\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0)(5q\0~\0ˇt\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0)82*q\0~\0ˇt\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0Weq\0~\0ˇt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~q\0~	xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\rsq\0~\0hpSq\0~\0Ât\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~\0Íq\0~\0Ìq\0~\0Òq\0~\0˚q\0~\0ˇq\0~xxt\0Applicationssr\0,org.enhydra.shark.xpdl.elements.Applications¬Ä¥ﬁˆº\0\0xq\0~\0k\0q\0~sq\0~\0HÅq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0+org.enhydra.shark.xpdl.elements.Applicationv	¢R˘˛S\0\0xq\0~\0©t\0Applicationsq\0~\0&N#Eq\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0e(€q\0~t\0default_applicationpq\0~\0sq\0~\0\0q\0~\0sq\0~\0(˝˜q\0~t\0\0pt\0Descriptionsq\0~\02\0q\0~\'sq\0~\0b†aq\0~t\0\0t\0Choicesr\00org.enhydra.shark.xpdl.elements.ApplicationTypesè?˚!≠Æ¶\0\0xr\0\'org.enhydra.shark.xpdl.XMLComplexChoice§|±∫\"Ù˙\0L\0choicesq\0~\0L\0choosenq\0~\0	xq\0~\0q\0~+sq\0~\0-\Z?q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sr\00org.enhydra.shark.xpdl.elements.FormalParametersp∫ƒB«√ÅZ\0\0xq\0~\0k\0t\0FormalParameterssq\0~\0i$2ïq\0~.t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~\0À\0t\0ExternalReferencesq\0~\0>\'`ˇq\0~.t\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0\\†^q\0~9t\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0!/q\0~9t\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0i‡:q\0~9t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~>q\0~Aq\0~Dxxq\0~3t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Hsq\0~\0l≈¥aq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~!q\0~$q\0~(q\0~.q\0~Ixxt\0\nDataFieldssr\0*org.enhydra.shark.xpdl.elements.DataFieldsΩµí ¶‘˙U\0\0xq\0~\0k\0q\0~Osq\0~\0B»Í˛q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0WorkflowProcessessr\01org.enhydra.shark.xpdl.elements.WorkflowProcessespé_¢0,\0\0xq\0~\0k\0q\0~Vsq\0~\0B¥ß„q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0/org.enhydra.shark.xpdl.elements.WorkflowProcess%·v0“◊L\0\0xq\0~\0©t\0WorkflowProcesssq\0~\0ç-˛q\0~Xt\0\0sq\0~\0w\0\0\0\rq\0~\0sq\0~\0q\0~\0sq\0~\0cêΩsq\0~^t\0process1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0jÊ∫q\0~^t\0Proposal Approval Processpt\0AccessLevelsq\0~\0\0q\0~isq\0~\0V≤¸q\0~^q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0PUBLICt\0PRIVATExt\0\rProcessHeadersr\0-org.enhydra.shark.xpdl.elements.ProcessHeaderåLÆÉC-)ø\0\0xq\0~\0q\0~osq\0~\0fØ\r q\0~^t\0\0sq\0~\0w\0\0\0t\0DurationUnitsq\0~\0\0q\0~usq\0~\07><q\0~qt\0hsq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0Yt\0Mt\0Dt\0ht\0mt\0sxt\0Createdsq\0~\0-\0q\0~Äsq\0~\0CﬁAÓq\0~qt\0\0t\0Descriptionsq\0~\02\0q\0~Ñsq\0~\0Åü;q\0~qt\0\0t\0Prioritysr\0(org.enhydra.shark.xpdl.elements.Priority`ˆNn>b\0\0xq\0~\0#\0q\0~àsq\0~\0^ºq\0~qt\0\0t\0Limitsr\0%org.enhydra.shark.xpdl.elements.Limit¸¢Ë1ò”ó\0\0xq\0~\0#\0q\0~çsq\0~\03=ïq\0~qt\0\0t\0	ValidFromsr\0)org.enhydra.shark.xpdl.elements.ValidFromc≈Ö|ÉL<\0\0xq\0~\0#\0q\0~ísq\0~\0›Ó-q\0~qt\0\0t\0ValidTosr\0\'org.enhydra.shark.xpdl.elements.ValidToû¡∂´M…\0\0xq\0~\0#\0q\0~ósq\0~\0°¡\nq\0~qt\0\0t\0TimeEstimationsr\0.org.enhydra.shark.xpdl.elements.TimeEstimation≈Ä£\'3\0\0xq\0~\0\0q\0~úsq\0~\0`xÄq\0~qt\0\0sq\0~\0w\0\0\0t\0WaitingTimesr\0+org.enhydra.shark.xpdl.elements.WaitingTimeNì‰Œ†/\0\0xq\0~\0#\0q\0~¢sq\0~\0;Æ=§q\0~ût\0\0t\0WorkingTimesr\0+org.enhydra.shark.xpdl.elements.WorkingTimeæ~±ÉÊ◊\0\0xq\0~\0#\0q\0~ßsq\0~\0Vozq\0~ût\0\0t\0Durationsr\0(org.enhydra.shark.xpdl.elements.Duration© ÙCÙÎË\0\0xq\0~\0#\0q\0~¨sq\0~\0^N\",q\0~ût\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~§q\0~©q\0~Æxxsq\0~\0E\0\0\0w\0\0\0q\0~vq\0~Åq\0~Öq\0~äq\0~èq\0~îq\0~ôq\0~ûxt\0RedefinableHeadersq\0~\0H\0q\0~≥sq\0~\0]Öâq\0~^t\0\0sq\0~\0w\0\0\0q\0~\0Msq\0~\0\0q\0~\0Msq\0~\0åÃq\0~¥q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\0Rq\0~\0Sq\0~\0Txt\0Authorsq\0~\0V\0q\0~ªsq\0~\09NÚ_q\0~¥t\0\0t\0Versionsq\0~\0[\0q\0~øsq\0~\0æ¯q\0~¥t\0\0t\0Codepagesq\0~\0`\0q\0~√sq\0~\0gÇV≥q\0~¥t\0\0t\0\nCountrykeysq\0~\0e\0q\0~«sq\0~\0ü ;q\0~¥t\0\0t\0Responsiblessq\0~\0j\0q\0~Àsq\0~\0XçΩq\0~¥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~∏q\0~ºq\0~¿q\0~ƒq\0~»q\0~Ãxt\0FormalParameterssq\0~2\0q\0~“sq\0~\09Cåq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0\nDataFieldssq\0~P\0q\0~ÿsq\0~\0L–Ω”q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0)org.enhydra.shark.xpdl.elements.DataFieldI“3.~ë≠˜\0\0xq\0~\0©t\0	DataFieldsq\0~\0#àÊq\0~Ÿt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0A?})q\0~ﬂt\0statuspq\0~\0sq\0~\0\0q\0~\0sq\0~\0(πk	q\0~ﬂt\0\0pt\0IsArraysq\0~\0\0q\0~Ísq\0~\0B$Íq\0~ﬂt\0FALSEsq\0~\0E\0\0\0w\0\0\0t\0TRUEt\0FALSExt\0DataTypesr\0(org.enhydra.shark.xpdl.elements.DataTypeÜ\'4sM\0\0xq\0~\0q\0~Òsq\0~\01´∂q\0~ﬂt\0\0sq\0~\0w\0\0\0t\0	DataTypessr\0)org.enhydra.shark.xpdl.elements.DataTypesµpcH,ﬁ!ì\0Z\0\risInitializedxq\0~-q\0~˜sq\0~\01w∆¨q\0~Ût\0\0sq\0~\0E\0\0\0	w\0\0\0	sr\0)org.enhydra.shark.xpdl.elements.BasicType∑)®Ωw1øé\0\0xq\0~\0t\0	BasicTypesq\0~\0eÂq\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0q\0~\0Üsq\0~\0wOØq\0~˛t\0STRINGsq\0~\0E\0\0\0w\0\0\0t\0STRINGt\0FLOATt\0INTEGERt\0	REFERENCEt\0DATETIMEt\0BOOLEANt\0	PERFORMERxxsq\0~\0E\0\0\0w\0\0\0q\0~xsr\0,org.enhydra.shark.xpdl.elements.DeclaredTypedR.\\^Ë9í\0\0xq\0~\0t\0DeclaredTypesq\0~\0˘4)q\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0¥”\\q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~xsr\0*org.enhydra.shark.xpdl.elements.SchemaType&1oSH™Ö\0\0xq\0~\0t\0\nSchemaTypesq\0~\0Hﬁ™bq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~\0Àt\0ExternalReferencesq\0~\0M(Á·q\0~˘t\0\0sq\0~\0w\0\0\0q\0~\0–sq\0~\0\0q\0~\0–sq\0~\0¢⁄q\0~ t\0\0pq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0->ƒäq\0~ t\0\0pq\0~\0ÿsq\0~\0\0q\0~\0ÿsq\0~\0Êıq\0~ t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~%q\0~(q\0~+xsr\0*org.enhydra.shark.xpdl.elements.RecordTypeı%®Ω©ÎK\0\0\0xq\0~\0kt\0\nRecordTypesq\0~\0\Zì˙q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.UnionType¥⁄Ô5PŒG˙\0\0xq\0~\0kt\0	UnionTypesq\0~\0Ë/Îq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0/org.enhydra.shark.xpdl.elements.EnumerationType™⁄ﬁf3b\0\0xq\0~\0kt\0EnumerationTypesq\0~\0H©≥àq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.ArrayTypeg¨$\0≠N@\0\0xq\0~\0t\0	ArrayTypesq\0~\0/˜›&q\0~˘t\0\0sq\0~\0w\0\0\0t\0\nLowerIndexsq\0~\0q\0~Jsq\0~\0-ÅpÓq\0~Et\0\0pt\0\nUpperIndexsq\0~\0q\0~Nsq\0~\0ƒÖ¬q\0~Et\0\0pq\0~˜sq\0~¯q\0~˜sq\0~\0ztLAq\0~Et\0\0ppxsq\0~\0E\0\0\0w\0\0\0q\0~Kq\0~Oq\0~Rxsr\0(org.enhydra.shark.xpdl.elements.ListTypeü\"”ü\nÆ\0\0xq\0~\0t\0ListTypesq\0~\0k{†ƒq\0~˘t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯q\0~˜sq\0~\0M€ªEq\0~Wt\0\0ppxsq\0~\0E\0\0\0w\0\0\0q\0~\\xxq\0~˛xsq\0~\0E\0\0\0w\0\0\0q\0~˘xt\0InitialValuesr\0,org.enhydra.shark.xpdl.elements.InitialValuej,z˚îúR\0\0xq\0~\0#\0q\0~asq\0~\0jÕ©q\0~ﬂt\0\0t\0Lengthsr\0&org.enhydra.shark.xpdl.elements.LengthMW+-Ã©W‹\0\0xq\0~\0#\0q\0~fsq\0~\0N$ùaq\0~ﬂt\0\0t\0Descriptionsq\0~\02\0q\0~ksq\0~\0xoi·q\0~ﬂt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~osq\0~\0h˛Ù:q\0~ﬂt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~‰q\0~Áq\0~Îq\0~Ûq\0~cq\0~hq\0~lq\0~pxxt\0Participantssq\0~\0¢\0q\0~vsq\0~\0$ˆx`q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Applicationssq\0~\0q\0~|sq\0~\0>Úµq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ActivitySetssr\0,org.enhydra.shark.xpdl.elements.ActivitySets–qV[4™Ó÷\0\0xq\0~\0k\0q\0~Çsq\0~\0?õÕùq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0\nActivitiessr\0*org.enhydra.shark.xpdl.elements.Activities&G^·æl˛P\0\0xq\0~\0k\0q\0~âsq\0~\0vÏ—òq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0(org.enhydra.shark.xpdl.elements.Activity√tá45\Z9ï\0\0xq\0~\0©t\0Activitysq\0~\0xUlïq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0xV.q\0~ët\0approve_proposalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0Ó¨‰q\0~ët\0Approve Proposalpt\0Descriptionsq\0~\02\0q\0~úsq\0~\0c•~Gq\0~ët\0\0t\0Limitsq\0~é\0q\0~†sq\0~\0Õi‘q\0~ët\0\0q\0~\0Üsr\0-org.enhydra.shark.xpdl.elements.ActivityTypese≈Ω{ˆûËË\0\0xq\0~-q\0~\0Üsq\0~\0KØèúq\0~ët\0\0sq\0~\0E\0\0\0w\0\0\0sr\0%org.enhydra.shark.xpdl.elements.Route0eÓ\r≤G›\0\0xq\0~\0t\0Routesq\0~\0*GìÅq\0~•t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0.org.enhydra.shark.xpdl.elements.Implementationúrñí^%ä\0\0xq\0~\0t\0Implementationsq\0~\0bXó»q\0~•t\0\0sq\0~\0w\0\0\0q\0~\0Üsr\03org.enhydra.shark.xpdl.elements.ImplementationTypes\rü‰TŸ°9\0\0xq\0~-q\0~\0Üsq\0~\0(¡ûq\0~±t\0\0sq\0~\0E\0\0\0w\0\0\0sr\0\"org.enhydra.shark.xpdl.elements.No{ïÖ⁄.\0\0xq\0~\0t\0Nosq\0~\0&LHq\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0%org.enhydra.shark.xpdl.elements.ToolsCø÷g©ë\0\0xq\0~\0k\0t\0Toolssq\0~\00+õq\0~∑t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0\'org.enhydra.shark.xpdl.elements.SubFlow;Oısá7:$\0\0xq\0~\0t\0SubFlowsq\0~\0¡Ñq\0~∑t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0A≤Ûq\0~ t\0\0pt\0	Executionsq\0~\0\0q\0~“sq\0~\0¡ä≤q\0~ q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ASYNCHRt\0SYNCHRxt\0ActualParameterssr\00org.enhydra.shark.xpdl.elements.ActualParametersÎå˜µ_ÏK˛\0\0xq\0~\0k\0q\0~ÿsq\0~\0=•4Bq\0~ t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~œq\0~”q\0~⁄xxq\0~ºxsq\0~\0E\0\0\0w\0\0\0q\0~∑xsr\0-org.enhydra.shark.xpdl.elements.BlockActivityíq cƒÍF\0\0xq\0~\0t\0\rBlockActivitysq\0~\0#û:Nq\0~•t\0\0sq\0~\0w\0\0\0t\0BlockIdsq\0~\0q\0~Ásq\0~\0r¢P]q\0~‚t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~Ëxxq\0~±t\0	Performersr\0)org.enhydra.shark.xpdl.elements.Performer¶\"1%ËÃƒ\0\0xq\0~\0#\0q\0~Ïsq\0~\0zæ–°q\0~ët\0approvert\0	StartModesr\0)org.enhydra.shark.xpdl.elements.StartModenhÖÑ◊ÛS\0\0xq\0~\0\0q\0~Òsq\0~\0n-Ïêq\0~ët\0\0sq\0~\0w\0\0\0t\0Modesr\00org.enhydra.shark.xpdl.elements.StartFinishModes~Á6zÂXã\'\0\0xq\0~-\0q\0~˜sq\0~\0<3ÁQq\0~Ût\0\0sq\0~\0E\0\0\0w\0\0\0sr\0,org.enhydra.shark.xpdl.XMLEmptyChoiceElementÔ2‘;Œ3ı_\0\0xq\0~\0\0t\0XMLEmptyChoiceElementsq\0~\0r¶”.q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0)org.enhydra.shark.xpdl.elements.AutomaticÈt?˚_Äê\0\0xq\0~\0t\0	Automaticsq\0~\05”ó†q\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsr\0&org.enhydra.shark.xpdl.elements.Manual∑v™âÓ[ÿ§\0\0xq\0~\0t\0Manualsq\0~\0AeÎgq\0~˘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~˛xsq\0~\0E\0\0\0w\0\0\0q\0~˘xt\0\nFinishModesr\0*org.enhydra.shark.xpdl.elements.FinishModeˆÉ¯õúºÛ{\0\0xq\0~\0\0q\0~sq\0~\0u§!Mq\0~ët\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\01_≥tq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0n∫à3q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\04‚êq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0v`q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Prioritysq\0~â\0q\0~0sq\0~\07,Q!q\0~ët\0\0t\0	Deadlinessr\0)org.enhydra.shark.xpdl.elements.Deadlines>Ωç…ú√⁄\0\0xq\0~\0k\0q\0~4sq\0~\0\rmºÁq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsr\05org.enhydra.shark.xpdl.elements.SimulationInformation\"ì|I™£í\0\0xq\0~\0\0q\0~;sq\0~\0f	¿∏q\0~ët\0\0sq\0~\0w\0\0\0t\0\rInstantiationsq\0~\0\0q\0~Asq\0~\0RKzøq\0~=q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ONCEt\0MULTIPLExt\0Costsr\0$org.enhydra.shark.xpdl.elements.Costœ˘¿ﬁÚëû\0\0xq\0~\0#q\0~Gsq\0~\0ªúéq\0~=t\0\0t\0TimeEstimationsq\0~ùq\0~Lsq\0~\0g—Ç\Zq\0~=t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Qsq\0~\0OuÇXq\0~Mt\0\0t\0WorkingTimesq\0~®\0q\0~Usq\0~\0Fbáãq\0~Mt\0\0t\0Durationsq\0~≠\0q\0~Ysq\0~\0f<%q\0~Mt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Rq\0~Vq\0~Zxxsq\0~\0E\0\0\0w\0\0\0q\0~Bq\0~Iq\0~Mxt\0Iconsr\0$org.enhydra.shark.xpdl.elements.Icon´TŒU(ˇ}6\0\0xq\0~\0#\0q\0~_sq\0~\0vL¨¡q\0~ët\0\0t\0\rDocumentationsq\0~\07\0q\0~dsq\0~\0Hóq\0~ët\0\0t\0TransitionRestrictionssr\06org.enhydra.shark.xpdl.elements.TransitionRestrictionsC)∑◊Äi;\0\0xq\0~\0k\0q\0~hsq\0~\0GDÔq\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\05org.enhydra.shark.xpdl.elements.TransitionRestrictionN˚ƒáπ}˛\0\0xq\0~\0t\0TransitionRestrictionsq\0~\0B®éGq\0~jt\0\0sq\0~\0w\0\0\0t\0Joinsr\0$org.enhydra.shark.xpdl.elements.Join⁄ï”©x)Û5\0\0xq\0~\0\0q\0~usq\0~\0Eíæ¬q\0~pt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0);q\0~wt\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0ANDt\0XORxxsq\0~\0E\0\0\0w\0\0\0q\0~{xt\0Splitsr\0%org.enhydra.shark.xpdl.elements.Split‹ÌØ~—ØWS\0\0xq\0~\0\0q\0~Çsq\0~\0ú’bq\0~pt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0	¿®`q\0~Ñq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssr\0.org.enhydra.shark.xpdl.elements.TransitionRefs†Ô≥—ä·ƒÎ\0\0xq\0~\0k\0q\0~ãsq\0~\0Ytc4q\0~Ñt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~àq\0~çxxsq\0~\0E\0\0\0w\0\0\0q\0~wq\0~Ñxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~îsq\0~\0kÔè6q\0~ët\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\01org.enhydra.shark.xpdl.elements.ExtendedAttributeª¬\\±ÏF\0\0xq\0~\0t\0ExtendedAttributesq\0~\0,mÛßq\0~ït\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0)-∫q\0~õt\0JaWE_GRAPH_PARTICIPANT_IDpt\0Valuesq\0~\0\0q\0~£sq\0~\0Y»’q\0~õt\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~†q\0~§xsq\0~öt\0ExtendedAttributesq\0~\0nG∆ñq\0~ït\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0-t‡2q\0~®t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0iÕ2§q\0~®t\0#205.0000228881836,56.76666259765625pxsq\0~\0E\0\0\0w\0\0\0q\0~≠q\0~∞xxpxsq\0~\0E\0\0\0w\0\0\0q\0~ñq\0~ôq\0~ùq\0~°q\0~•q\0~Óq\0~Ûq\0~q\0~1q\0~6q\0~=q\0~aq\0~eq\0~jq\0~ïxsq\0~êt\0Activitysq\0~\0v(q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0?Aq\0~µt\0approvalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0&ø\nq\0~µt\0Approvalpt\0Descriptionsq\0~\02\0q\0~¿sq\0~\0,@ûq\0~µt\0\0t\0Limitsq\0~é\0q\0~ƒsq\0~\0,T†q\0~µt\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0:õÖZq\0~µt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0d™/Pq\0~»t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0H\Zƒ†q\0~»t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0&…4Wq\0~“t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0@ƒÅq\0~◊t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0u˚£}q\0~◊t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0k⁄F¶q\0~◊t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0^ºq\0~Át\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\02RJ¿q\0~Áq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Úsq\0~\0\ZöÁºq\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Ïq\0~Ôq\0~Ûxxq\0~€xsq\0~\0E\0\0\0w\0\0\0q\0~◊xsq\0~·t\0\rBlockActivitysq\0~\0#L}?q\0~»t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0RK‹q\0~˙t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~ˇxxq\0~Ãt\0	Performersq\0~Ì\0q\0~sq\0~\0rßq\0~µt\0approvert\0	StartModesq\0~Ú\0q\0~sq\0~\0?Õ±˘q\0~µt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0(Ü∫q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0ûÛXq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0HÜ\ZÃq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0q‹…q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~#sq\0~\0‰∞Nq\0~µt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0xîYq\0~$t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0M5(Ëq\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0ÃÃhq\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0Dwmq\0~(t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~,xsq\0~\0E\0\0\0w\0\0\0q\0~(xt\0Prioritysq\0~â\0q\0~?sq\0~\0kÃAtq\0~µt\0\0t\0	Deadlinessq\0~5\0q\0~Csq\0~\0ZFçq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Isq\0~\0ydÇq\0~µt\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0B –q\0~Jq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Qsq\0~\0Óçq\0~Jt\0\0t\0TimeEstimationsq\0~ùq\0~Usq\0~\0BøΩœq\0~Jt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Zsq\0~\0 zÊnq\0~Vt\0\0t\0WorkingTimesq\0~®\0q\0~^sq\0~\0 n¨˚q\0~Vt\0\0t\0Durationsq\0~≠\0q\0~bsq\0~\0XÓF˝q\0~Vt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~[q\0~_q\0~cxxsq\0~\0E\0\0\0w\0\0\0q\0~Nq\0~Rq\0~Vxt\0Iconsq\0~`\0q\0~hsq\0~\0g\0ŸÍq\0~µt\0\0t\0\rDocumentationsq\0~\07\0q\0~lsq\0~\0>\ZÚq\0~µt\0\0t\0TransitionRestrictionssq\0~i\0q\0~psq\0~\0E‚uzq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\02ˆ:q\0~qt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~{sq\0~\0bÚ›àq\0~vt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0BÈ)q\0~|q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~Äxt\0Splitsq\0~É\0q\0~Ñsq\0~\0s‹èq\0~vt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0q1)q\0~Öt\0XORsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~çsq\0~\0[§.q\0~Öt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0-org.enhydra.shark.xpdl.elements.TransitionRefÉ%-ÀÒaö\0\0xq\0~\0©t\0\rTransitionRefsq\0~\0<iÉŸq\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0|7¢îq\0~ît\0transition3pxsq\0~\0E\0\0\0w\0\0\0q\0~ôxsq\0~ìt\0\rTransitionRefsq\0~\04bBq\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0	ô„ÿq\0~ùt\0transition6pxsq\0~\0E\0\0\0w\0\0\0q\0~¢xsq\0~ìt\0\rTransitionRefsq\0~\0yûŒyq\0~ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\08|‹q\0~¶t\0transition5pxsq\0~\0E\0\0\0w\0\0\0q\0~´xxxsq\0~\0E\0\0\0w\0\0\0q\0~âq\0~éxxsq\0~\0E\0\0\0w\0\0\0q\0~|q\0~Öxxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~±sq\0~\0o\nïq\0~µt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0.óq\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0S¢Q˚q\0~∑t\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0Cùºèq\0~∑t\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~ºq\0~øxsq\0~öt\0ExtendedAttributesq\0~\0K[Bnq\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\03I2q\0~√t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0r•≥)q\0~√t\0430,62.79999084472655pxsq\0~\0E\0\0\0w\0\0\0q\0~»q\0~Àxxpxsq\0~\0E\0\0\0w\0\0\0q\0~∫q\0~Ωq\0~¡q\0~≈q\0~»q\0~q\0~q\0~$q\0~@q\0~Dq\0~Jq\0~iq\0~mq\0~qq\0~≤xsq\0~êt\0Activitysq\0~\0\\Ú6q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\09Ëkq\0~–t\0	activity1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0M´qÙq\0~–t\0Resubmit Proposalpt\0Descriptionsq\0~\02\0q\0~€sq\0~\06)Úq\0~–t\0\0t\0Limitsq\0~é\0q\0~ﬂsq\0~\0iÛ¯‚q\0~–t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0%Ã&Jq\0~–t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\03~◊÷q\0~„t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0\'Úq\0~„t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0r‘Îzq\0~Ìt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\06nmq\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0sÎÃ`q\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0F˜\\Xq\0~Út\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0≠‡«q\0~t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0VÓy#q\0~q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~\rsq\0~\0{K\'Rq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~\nq\0~xxq\0~ˆxsq\0~\0E\0\0\0w\0\0\0q\0~Úxsq\0~·t\0\rBlockActivitysq\0~\0¢±ˇq\0~„t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0{ı¬q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\Zxxq\0~Ìt\0	Performersq\0~Ì\0q\0~sq\0~\0^wõ(q\0~–t\0	requestert\0	StartModesq\0~Ú\0q\0~\"sq\0~\0|§Vq\0~–t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0nõ-!q\0~#t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\00hCÇq\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0VUúq\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\01±’q\0~\'t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~+xsq\0~\0E\0\0\0w\0\0\0q\0~\'xt\0\nFinishModesq\0~\0q\0~>sq\0~\0Z‘AQq\0~–t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0(R}⁄q\0~?t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0H|Îq\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0\'Ø˛Vq\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0CÅöÚq\0~Ct\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~Gxsq\0~\0E\0\0\0w\0\0\0q\0~Cxt\0Prioritysq\0~â\0q\0~Zsq\0~\0oˆ˝>q\0~–t\0\0t\0	Deadlinessq\0~5\0q\0~^sq\0~\0@˝Øªq\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~dsq\0~\0x§ºq\0~–t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0Wa˚Kq\0~eq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~lsq\0~\0&„îq\0~et\0\0t\0TimeEstimationsq\0~ùq\0~psq\0~\0\"ãºq\0~et\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~usq\0~\0q\0~qt\0\0t\0WorkingTimesq\0~®\0q\0~ysq\0~\0\'Ëq\0~qt\0\0t\0Durationsq\0~≠\0q\0~}sq\0~\0Íb6q\0~qt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~vq\0~zq\0~~xxsq\0~\0E\0\0\0w\0\0\0q\0~iq\0~mq\0~qxt\0Iconsq\0~`\0q\0~Ésq\0~\0e∂¢Çq\0~–t\0\0t\0\rDocumentationsq\0~\07\0q\0~ásq\0~\0BZUÅq\0~–t\0\0t\0TransitionRestrictionssq\0~i\0q\0~ãsq\0~\0%-\"9q\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~ësq\0~\0i\Zxq\0~–t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0:öq\0~ít\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0m~˝.q\0~ót\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0ç0#q\0~ót\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~úq\0~üxsq\0~öt\0ExtendedAttributesq\0~\0¡K®q\0~ít\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0H| √q\0~£t\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0RÒ#oq\0~£t\0397,20.787493896484378pxsq\0~\0E\0\0\0w\0\0\0q\0~®q\0~´xxpxsq\0~\0E\0\0\0w\0\0\0q\0~’q\0~ÿq\0~‹q\0~‡q\0~„q\0~q\0~#q\0~?q\0~[q\0~_q\0~eq\0~Ñq\0~àq\0~åq\0~íxsq\0~êt\0Activitysq\0~\07ò:q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0!˝–≤q\0~∞t\0\rsend_proposalpq\0~\0sq\0~\0\0q\0~\0sq\0~\0\"ëújq\0~∞t\0\rSend Proposalpt\0Descriptionsq\0~\02\0q\0~ªsq\0~\0¬⁄√q\0~∞t\0\0t\0Limitsq\0~é\0q\0~øsq\0~\0Psjèq\0~∞t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0\'u‹vq\0~∞t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0	©f>q\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0†°\nq\0~√t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0_LÜq\0~Õt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0VSwq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0	cíq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\008Ëq\0~“t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0>w ”q\0~‚t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0s÷∆\Zq\0~‚q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Ìsq\0~\0,ú>q\0~‚t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Áq\0~Íq\0~Óxxq\0~÷xsq\0~\0E\0\0\0w\0\0\0q\0~“xsq\0~·t\0\rBlockActivitysq\0~\0(o§q\0~√t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0(N¬éq\0~ıt\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~˙xxq\0~Õt\0	Performersq\0~Ì\0q\0~˛sq\0~\0mÌ\'ãq\0~∞t\0	requestert\0	StartModesq\0~Ú\0q\0~sq\0~\0 Ã`q\0~∞t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0uVèÆq\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0-b∫Ôq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0¡ç“q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0¬ˇæq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~sq\0~\0+|Âq\0~∞t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0Òá@q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0Jß‚ªq\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0yl,±q\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\08ˆ>“q\0~#t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~\'xsq\0~\0E\0\0\0w\0\0\0q\0~#xt\0Prioritysq\0~â\0q\0~:sq\0~\0pkÿ:q\0~∞t\0\0t\0	Deadlinessq\0~5\0q\0~>sq\0~\0,N∂q\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Dsq\0~\0=„q\0~∞t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\04N›q\0~Eq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~Lsq\0~\0:0Óiq\0~Et\0\0t\0TimeEstimationsq\0~ùq\0~Psq\0~\0]^gq\0~Et\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~Usq\0~\0/Ÿ≥q\0~Qt\0\0t\0WorkingTimesq\0~®\0q\0~Ysq\0~\0-Rìq\0~Qt\0\0t\0Durationsq\0~≠\0q\0~]sq\0~\0$S§yq\0~Qt\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~Vq\0~Zq\0~^xxsq\0~\0E\0\0\0w\0\0\0q\0~Iq\0~Mq\0~Qxt\0Iconsq\0~`\0q\0~csq\0~\0+Úq\0~∞t\0\0t\0\rDocumentationsq\0~\07\0q\0~gsq\0~\0i¢@™q\0~∞t\0\0t\0TransitionRestrictionssq\0~i\0q\0~ksq\0~\0\ZË.«q\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~qsq\0~\0#°1èq\0~∞t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0eWAhq\0~rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0>ÑÉ4q\0~wt\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\01	»q\0~wt\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~|q\0~xsq\0~öt\0ExtendedAttributesq\0~\09NI~q\0~rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0fÕ>q\0~Ét\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0|J±q\0~Ét\0#681.0000381469727,99.78333282470703pxsq\0~\0E\0\0\0w\0\0\0q\0~àq\0~ãxxpxsq\0~\0E\0\0\0w\0\0\0q\0~µq\0~∏q\0~ºq\0~¿q\0~√q\0~ˇq\0~q\0~q\0~;q\0~?q\0~Eq\0~dq\0~hq\0~lq\0~rxsq\0~êt\0Activitysq\0~\0Bò¯q\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0PŸCq\0~êt\0parallelpq\0~\0sq\0~\0\0q\0~\0sq\0~\0@5Wq\0~êt\0Parallelpt\0Descriptionsq\0~\02\0q\0~õsq\0~\0N÷öãq\0~êt\0\0t\0Limitsq\0~é\0q\0~üsq\0~\0g∫gêq\0~êt\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0x†¶«q\0~êt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0YßÇ	q\0~£t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0#_Ytq\0~£t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0Ì<hq\0~≠t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0j6Ãq\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0TFOq\0~≤t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0V:(q\0~≤t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0(iBˇq\0~¬t\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0sÇ°hq\0~¬q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~Õsq\0~\0^…˜iq\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~«q\0~ q\0~Œxxq\0~∂xsq\0~\0E\0\0\0w\0\0\0q\0~≤xsq\0~·t\0\rBlockActivitysq\0~\0AûN‚q\0~£t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0#Çíîq\0~’t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~⁄xxq\0~ßt\0	Performersq\0~Ì\0q\0~ﬁsq\0~\0EmÓmq\0~êt\0approvert\0	StartModesq\0~Ú\0q\0~‚sq\0~\0T;q\0~êt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0g‘ñ+q\0~„t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0îﬁDq\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0aoÕ∑q\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0πX§q\0~Át\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~Îxsq\0~\0E\0\0\0w\0\0\0q\0~Áxt\0\nFinishModesq\0~\0q\0~˛sq\0~\0^5‹bq\0~êt\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0dÈ_q\0~ˇt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0}Iï¯q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0\'E‡<q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0»lÄq\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Prioritysq\0~â\0q\0~\Zsq\0~\0gU\Zq\0~êt\0\0t\0	Deadlinessq\0~5\0q\0~sq\0~\0VÇ•@q\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~$sq\0~\0^ŒÎ¨q\0~êt\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0§ ôq\0~%q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~,sq\0~\0WÓî®q\0~%t\0\0t\0TimeEstimationsq\0~ùq\0~0sq\0~\0ì∞q\0~%t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~5sq\0~\0=øâq\0~1t\0\0t\0WorkingTimesq\0~®\0q\0~9sq\0~\0¶∆—q\0~1t\0\0t\0Durationsq\0~≠\0q\0~=sq\0~\0\'ŒI9q\0~1t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~6q\0~:q\0~>xxsq\0~\0E\0\0\0w\0\0\0q\0~)q\0~-q\0~1xt\0Iconsq\0~`\0q\0~Csq\0~\0n÷cq\0~êt\0\0t\0\rDocumentationsq\0~\07\0q\0~Gsq\0~\0^X®q\0~êt\0\0t\0TransitionRestrictionssq\0~i\0q\0~Ksq\0~\0lz÷¸q\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ot\0TransitionRestrictionsq\0~\0n{°Xq\0~Lt\0\0sq\0~\0w\0\0\0t\0Joinsq\0~v\0q\0~Vsq\0~\05˛<∆q\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0)NéÓq\0~Wq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxxsq\0~\0E\0\0\0w\0\0\0q\0~[xt\0Splitsq\0~É\0q\0~_sq\0~\0H◊s£q\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0{ïjÄq\0~`t\0ANDsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~q\0~Äxt\0TransitionRefssq\0~å\0q\0~hsq\0~\0?Iq8q\0~`t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~ìt\0\rTransitionRefsq\0~\0AÖ5îq\0~it\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\05±\\-q\0~nt\0transition7pxsq\0~\0E\0\0\0w\0\0\0q\0~sxsq\0~ìt\0\rTransitionRefsq\0~\0:–Ûq\0~it\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ﬁM\0q\0~wt\0transition8pxsq\0~\0E\0\0\0w\0\0\0q\0~|xxxsq\0~\0E\0\0\0w\0\0\0q\0~dq\0~ixxsq\0~\0E\0\0\0w\0\0\0q\0~Wq\0~`xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Çsq\0~\09(ÕÀq\0~êt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0qu´oq\0~Ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0iı\rõq\0~àt\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\05ô(-q\0~àt\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~çq\0~êxsq\0~öt\0ExtendedAttributesq\0~\0K‘‰Bq\0~Ét\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0¶\0Ñq\0~ît\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0V~^>q\0~ît\0588,61.599993896484364pxsq\0~\0E\0\0\0w\0\0\0q\0~ôq\0~úxxpxsq\0~\0E\0\0\0w\0\0\0q\0~ïq\0~òq\0~úq\0~†q\0~£q\0~ﬂq\0~„q\0~ˇq\0~q\0~q\0~%q\0~Dq\0~Hq\0~Lq\0~Éxsq\0~êt\0Activitysq\0~\0*Z‹úq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ul˚Zq\0~°t\0tool1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0F¿€aq\0~°t\0\ZSend Approval Notificationpt\0Descriptionsq\0~\02\0q\0~¨sq\0~\0a≈Qq\0~°t\0\0t\0Limitsq\0~é\0q\0~∞sq\0~\0Enlµq\0~°t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\09,ˆ∏q\0~°t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0yw9/q\0~¥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\02Ò¬•q\0~¥t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0Läq\0~æt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0Zã¥çq\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0\Zı@≤q\0~√t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0$org.enhydra.shark.xpdl.elements.Tool\\6ñ&∂+GÅ\0\0xq\0~\0©t\0Toolsq\0~\03‹ºëq\0~Õt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0>Óe<q\0~‘t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0*œz q\0~‘q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0APPLICATIONt\0	PROCEDURExt\0ActualParameterssq\0~Ÿ\0q\0~·sq\0~\0It?˘q\0~‘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~Ásq\0~\09Oq\0~‘t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Îsq\0~\0o°íq\0~‘t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~Ÿq\0~‹q\0~‚q\0~Ëq\0~Ïxxsq\0~…t\0SubFlowsq\0~\0&-]£q\0~√t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0BŸíßq\0~Út\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0EÒy≠q\0~Úq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~˝sq\0~\0u1Aqq\0~Út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~˜q\0~˙q\0~˛xxq\0~Õxsq\0~\0E\0\0\0w\0\0\0q\0~√xsq\0~·t\0\rBlockActivitysq\0~\09=⁄1q\0~¥t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\07;q\0~t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~\nxxq\0~æt\0	Performersq\0~Ì\0q\0~sq\0~\0y\rHq\0~°t\0approvert\0	StartModesq\0~Ú\0q\0~sq\0~\0HÏ—q\0~°t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0QsR{q\0~t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0@¯?q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0K„~q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0cﬁú6q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~xsq\0~\0E\0\0\0w\0\0\0q\0~xt\0\nFinishModesq\0~\0q\0~.sq\0~\0pˇ±}q\0~°t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0s}.]q\0~/t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0?û)Æq\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0Voñ2q\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0h∆\'}q\0~3t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~7xsq\0~\0E\0\0\0w\0\0\0q\0~3xt\0Prioritysq\0~â\0q\0~Jsq\0~\0¡5q\0~°t\0\0t\0	Deadlinessq\0~5\0q\0~Nsq\0~\0\\éIÄq\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~Tsq\0~\0v˙ö%q\0~°t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0/ﬁ∂Hq\0~Uq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~\\sq\0~\0#H¿Ôq\0~Ut\0\0t\0TimeEstimationsq\0~ùq\0~`sq\0~\0\r§_Oq\0~Ut\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~esq\0~\0/≠◊lq\0~at\0\0t\0WorkingTimesq\0~®\0q\0~isq\0~\0uX\'q\0~at\0\0t\0Durationsq\0~≠\0q\0~msq\0~\0±°”q\0~at\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~fq\0~jq\0~nxxsq\0~\0E\0\0\0w\0\0\0q\0~Yq\0~]q\0~axt\0Iconsq\0~`\0q\0~ssq\0~\0?@ ≠q\0~°t\0\0t\0\rDocumentationsq\0~\07\0q\0~wsq\0~\0~ólûq\0~°t\0\0t\0TransitionRestrictionssq\0~i\0q\0~{sq\0~\0Q‚…q\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Åsq\0~\0L@E›q\0~°t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0SıMSq\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0lÿ%Úq\0~át\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0	ı©ûq\0~át\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~åq\0~èxsq\0~öt\0ExtendedAttributesq\0~\0⁄àq\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0}<Wïq\0~ìt\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0PÊ\Zuq\0~ìt\0735,63.974993896484364pxsq\0~\0E\0\0\0w\0\0\0q\0~òq\0~õxxpxsq\0~\0E\0\0\0w\0\0\0q\0~¶q\0~©q\0~≠q\0~±q\0~¥q\0~q\0~q\0~/q\0~Kq\0~Oq\0~Uq\0~tq\0~xq\0~|q\0~Çxsq\0~êt\0Activitysq\0~\0t˚Æq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0o‘ÖŸq\0~†t\0tool2pq\0~\0sq\0~\0\0q\0~\0sq\0~\0 Ö‰q\0~†t\0Send Reject Notificationpt\0Descriptionsq\0~\02\0q\0~´sq\0~\0?˛çq\0~†t\0\0t\0Limitsq\0~é\0q\0~Øsq\0~\0_Sn™q\0~†t\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0tãnq\0~†t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0‹Øq\0~≥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\0¶aØq\0~≥t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0Tƒk/q\0~Ωt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0Y◊\"q\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0lHBxq\0~¬t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~”t\0Toolsq\0~\0r0\nÂq\0~Ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0xÉy q\0~“t\0default_applicationpq\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0l®≥9q\0~“q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~ﬂq\0~‡xt\0ActualParameterssq\0~Ÿ\0q\0~›sq\0~\0cèHq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0Descriptionsq\0~\02\0q\0~„sq\0~\0#hq\0~“t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~Ásq\0~\0Qõ9ªq\0~“t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xpxsq\0~\0E\0\0\0w\0\0\0q\0~◊q\0~⁄q\0~ﬁq\0~‰q\0~Ëxxsq\0~…t\0SubFlowsq\0~\0O@ÌGq\0~¬t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\08*jìq\0~Ót\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0bZsq\0~Óq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~˘sq\0~\0LÂèÀq\0~Ót\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~Ûq\0~ˆq\0~˙xxq\0~Ãxsq\0~\0E\0\0\0w\0\0\0q\0~¬xsq\0~·t\0\rBlockActivitysq\0~\0ái≈q\0~≥t\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0oı4q\0~	t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~	xxq\0~Ωt\0	Performersq\0~Ì\0q\0~	\nsq\0~\01˘gq\0~†t\0approvert\0	StartModesq\0~Ú\0q\0~	sq\0~\0oJ§ùq\0~†t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0P<!q\0~	t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0#”\\ıq\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0u÷Ó|q\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0r\r?Íq\0~	t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	xsq\0~\0E\0\0\0w\0\0\0q\0~	xt\0\nFinishModesq\0~\0q\0~	*sq\0~\0n\'´iq\0~†t\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0W™f¨q\0~	+t\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0YTBq\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0\"4ƒq\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0◊¥‡q\0~	/t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	3xsq\0~\0E\0\0\0w\0\0\0q\0~	/xt\0Prioritysq\0~â\0q\0~	Fsq\0~\0\rÌ˝q\0~†t\0\0t\0	Deadlinessq\0~5\0q\0~	Jsq\0~\0VıFq\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~	Psq\0~\0\rfRVq\0~†t\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0>•áÆq\0~	Qq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~	Xsq\0~\0[:êmq\0~	Qt\0\0t\0TimeEstimationsq\0~ùq\0~	\\sq\0~\0*Ââq\0~	Qt\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~	asq\0~\0Æ„aq\0~	]t\0\0t\0WorkingTimesq\0~®\0q\0~	esq\0~\0$ûçq\0~	]t\0\0t\0Durationsq\0~≠\0q\0~	isq\0~\0œ¸q\0~	]t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~	bq\0~	fq\0~	jxxsq\0~\0E\0\0\0w\0\0\0q\0~	Uq\0~	Yq\0~	]xt\0Iconsq\0~`\0q\0~	osq\0~\0\rvœq\0~†t\0\0t\0\rDocumentationsq\0~\07\0q\0~	ssq\0~\0o!HÎq\0~†t\0\0t\0TransitionRestrictionssq\0~i\0q\0~	wsq\0~\0LRstq\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~	}sq\0~\0R°8-q\0~†t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0l6p¶q\0~	~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0u∑¯q\0~	Ét\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0Sf#·q\0~	Ét\0approverpxsq\0~\0E\0\0\0w\0\0\0q\0~	àq\0~	ãxsq\0~öt\0ExtendedAttributesq\0~\0∏I!q\0~	~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0@T©0q\0~	èt\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0s-µq\0~	èt\0394,168.39999694824218pxsq\0~\0E\0\0\0w\0\0\0q\0~	îq\0~	óxxpxsq\0~\0E\0\0\0w\0\0\0q\0~•q\0~®q\0~¨q\0~∞q\0~≥q\0~	q\0~	q\0~	+q\0~	Gq\0~	Kq\0~	Qq\0~	pq\0~	tq\0~	xq\0~	~xsq\0~êt\0Activitysq\0~\06P\Zq\0~ãt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0bÏ∆cq\0~	út\0route1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0Lö‚\\q\0~	út\0Route 1pt\0Descriptionsq\0~\02\0q\0~	ßsq\0~\0)‚ﬂ÷q\0~	út\0\0t\0Limitsq\0~é\0q\0~	´sq\0~\02±6˚q\0~	út\0\0q\0~\0Üsq\0~§q\0~\0Üsq\0~\0§7q\0~	út\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~©t\0Routesq\0~\0\n∑û]q\0~	Øt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~∞t\0Implementationsq\0~\05îÏ≤q\0~	Øt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~∂q\0~\0Üsq\0~\0)q\0~	πt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~ªt\0Nosq\0~\0?\r8Yq\0~	æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~¬\0t\0Toolssq\0~\0	˛ìq\0~	æt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~…t\0SubFlowsq\0~\0ùêbq\0~	æt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0]b\0Æq\0~	Œt\0\0pq\0~“sq\0~\0\0q\0~“sq\0~\0˚$´q\0~	Œq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~÷q\0~◊xt\0ActualParameterssq\0~Ÿ\0q\0~	Ÿsq\0~\0r;”q\0~	Œt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxsq\0~\0E\0\0\0w\0\0\0q\0~	”q\0~	÷q\0~	⁄xxq\0~	¬xsq\0~\0E\0\0\0w\0\0\0q\0~	æxsq\0~·t\0\rBlockActivitysq\0~\0Òªrq\0~	Øt\0\0sq\0~\0w\0\0\0q\0~Ásq\0~\0q\0~Ásq\0~\0H[¨q\0~	·t\0\0pxsq\0~\0E\0\0\0w\0\0\0q\0~	Êxxq\0~	≥t\0	Performersq\0~Ì\0q\0~	Ísq\0~\0\Z◊Œq\0~	út\0	requestert\0	StartModesq\0~Ú\0q\0~	Ósq\0~\0)\"a‰q\0~	út\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0!Äuôq\0~	Ôt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\0|îhq\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\0I!—nq\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0t~Wq\0~	Ût\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~	˜xsq\0~\0E\0\0\0w\0\0\0q\0~	Ûxt\0\nFinishModesq\0~\0q\0~\n\nsq\0~\0Hƒîq\0~	út\0\0sq\0~\0w\0\0\0q\0~˜sq\0~¯\0q\0~˜sq\0~\0\"qÜ∞q\0~\nt\0\0sq\0~\0E\0\0\0w\0\0\0sq\0~˝\0t\0XMLEmptyChoiceElementsq\0~\00ó¢q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0	Automaticsq\0~\01√`6q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xsq\0~t\0Manualsq\0~\0nÅﬁ∏q\0~\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xxq\0~\nxsq\0~\0E\0\0\0w\0\0\0q\0~\nxt\0Prioritysq\0~â\0q\0~\n&sq\0~\0ñÆéq\0~	út\0\0t\0	Deadlinessq\0~5\0q\0~\n*sq\0~\0ú®˙q\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0SimulationInformationsq\0~<\0q\0~\n0sq\0~\08Ù®Oq\0~	út\0\0sq\0~\0w\0\0\0q\0~Asq\0~\0\0q\0~Asq\0~\0X`≈˜q\0~\n1q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~Eq\0~Fxt\0Costsq\0~Hq\0~\n8sq\0~\0r(‚´q\0~\n1t\0\0t\0TimeEstimationsq\0~ùq\0~\n<sq\0~\0ÑOlq\0~\n1t\0\0sq\0~\0w\0\0\0t\0WaitingTimesq\0~£\0q\0~\nAsq\0~\0Ø)Cq\0~\n=t\0\0t\0WorkingTimesq\0~®\0q\0~\nEsq\0~\0F>‹q\0~\n=t\0\0t\0Durationsq\0~≠\0q\0~\nIsq\0~\0s6á\nq\0~\n=t\0\0xsq\0~\0E\0\0\0w\0\0\0q\0~\nBq\0~\nFq\0~\nJxxsq\0~\0E\0\0\0w\0\0\0q\0~\n5q\0~\n9q\0~\n=xt\0Iconsq\0~`\0q\0~\nOsq\0~\0TÓY/q\0~	út\0\0t\0\rDocumentationsq\0~\07\0q\0~\nSsq\0~\05Íá¿q\0~	út\0\0t\0TransitionRestrictionssq\0~i\0q\0~\nWsq\0~\0$Iq\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0\0w\0\0\0\0xt\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n]sq\0~\0DÌÿq\0~	út\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0\ZÒi‡q\0~\n^t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0I%G™q\0~\nct\0JaWE_GRAPH_PARTICIPANT_IDpq\0~£sq\0~\0\0q\0~£sq\0~\0Y§æq\0~\nct\0	requesterpxsq\0~\0E\0\0\0w\0\0\0q\0~\nhq\0~\nkxsq\0~öt\0ExtendedAttributesq\0~\0C[≤q\0~\n^t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\nlüSq\0~\not\0JaWE_GRAPH_OFFSETpq\0~£sq\0~\0\0q\0~£sq\0~\0ië£q\0~\not\0228.0000228881836,22pxsq\0~\0E\0\0\0w\0\0\0q\0~\ntq\0~\nwxxpxsq\0~\0E\0\0\0w\0\0\0q\0~	°q\0~	§q\0~	®q\0~	¨q\0~	Øq\0~	Îq\0~	Ôq\0~\nq\0~\n\'q\0~\n+q\0~\n1q\0~\nPq\0~\nTq\0~\nXq\0~\n^xxt\0Transitionssr\0+org.enhydra.shark.xpdl.elements.TransitionsÌ9>‰¿/iØ\0\0xq\0~\0k\0q\0~\n|sq\0~\0aNí]q\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0*org.enhydra.shark.xpdl.elements.Transitiontßx∏Ö\0\0xq\0~\0©t\0\nTransitionsq\0~\0\"±œóq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\01Óq\0~\nÑt\0transition2pq\0~\0sq\0~\0\0q\0~\0sq\0~\0ZÜêRq\0~\nÑt\0\0pt\0Fromsq\0~\0q\0~\nèsq\0~\0lõÅq\0~\nÑt\0approve_proposalpt\0Tosq\0~\0q\0~\nìsq\0~\0j«ìÙq\0~\nÑt\0approvalpt\0	Conditionsr\0)org.enhydra.shark.xpdl.elements.Condition€‡D··Z;|\0\0xq\0~\0\0q\0~\nósq\0~\0vg!‚q\0~\nÑt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0Fƒ-Ωq\0~\nôq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pt\0	CONDITIONt\0	OTHERWISEt\0	EXCEPTIONt\0DEFAULTEXCEPTIONxxsq\0~\0E\0\0\0w\0\0\0q\0~\nùxt\0Descriptionsq\0~\02\0q\0~\n•sq\0~\0JN¢q\0~\nÑt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n©sq\0~\0~ X˘q\0~\nÑt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0aVÈ˘q\0~\n™t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0|\'›±q\0~\nØt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0H[ç!q\0~\nØt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\n¥q\0~\n∑xxpxsq\0~\0E\0\0\0w\0\0\0q\0~\nâq\0~\nåq\0~\nêq\0~\nîq\0~\nôq\0~\n¶q\0~\n™xsq\0~\nÉt\0\nTransitionsq\0~\0[¥r‹q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0›zq\0~\nºt\0transition3pq\0~\0sq\0~\0\0q\0~\0sq\0~\0[’asq\0~\nºt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0\r{Qrq\0~\nºt\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\0Osœ9q\0~\nºt\0	activity1pt\0	Conditionsq\0~\nò\0q\0~\nÕsq\0~\0#è⁄Gq\0~\nºt\0status===\'resubmit\'sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0t®ÔÖq\0~\nŒt\0	CONDITIONsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~\n“xt\0Descriptionsq\0~\02\0q\0~\n◊sq\0~\0%Û¸.q\0~\nºt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~\n€sq\0~\0U›œ\Zq\0~\nºt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0Yb∞q\0~\n‹t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\01ÚxAq\0~\n·t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0\'zQVq\0~\n·t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~\nÊq\0~\nÈxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\n¡q\0~\nƒq\0~\n«q\0~\n q\0~\nŒq\0~\nÿq\0~\n‹xsq\0~\nÉt\0\nTransitionsq\0~\0Û\'µq\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0À?<q\0~\nÓt\0transition4pq\0~\0sq\0~\0\0q\0~\0sq\0~\0aI§q\0~\nÓt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\06Ö`q\0~\nÓt\0	activity1pq\0~\nìsq\0~\0q\0~\nìsq\0~\0óZÅq\0~\nÓt\0approve_proposalpt\0	Conditionsq\0~\nò\0q\0~\nˇsq\0~\0∫ºq\0~\nÓt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0¬~7q\0~\0q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~xt\0Descriptionsq\0~\02\0q\0~sq\0~\07/l‹q\0~\nÓt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0DÉLÙq\0~\nÓt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0A5‡ˆq\0~\rt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0N\r∑q\0~t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0#·qŸq\0~t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~\Zxxpxsq\0~\0E\0\0\0w\0\0\0q\0~\nÛq\0~\nˆq\0~\n˘q\0~\n¸q\0~\0q\0~	q\0~\rxsq\0~\nÉt\0\nTransitionsq\0~\0@,∑•q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0Nà•q\0~t\0transition6pq\0~\0sq\0~\0\0q\0~\0sq\0~\0%!˝Oq\0~t\0approvedpq\0~\nèsq\0~\0q\0~\nèsq\0~\0.p›q\0~t\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\0\rf:Éq\0~t\0parallelpt\0	Conditionsq\0~\nò\0q\0~0sq\0~\0\0ïÁ—q\0~t\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0(•Ç(q\0~1t\0	OTHERWISEsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~5xt\0Descriptionsq\0~\02\0q\0~:sq\0~\0´ùq\0~t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~>sq\0~\0> K¬q\0~t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0P≥∏q\0~?t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\02L¥q\0~Dt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0C-Ï8q\0~Dt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~Iq\0~Lxxpxsq\0~\0E\0\0\0w\0\0\0q\0~$q\0~\'q\0~*q\0~-q\0~1q\0~;q\0~?xsq\0~\nÉt\0\nTransitionsq\0~\0ªÀ©q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0\'Åÿ⁄q\0~Qt\0transition7pq\0~\0sq\0~\0\0q\0~\0sq\0~\0(Â; q\0~Qt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0Sæ)πq\0~Qt\0parallelpq\0~\nìsq\0~\0q\0~\nìsq\0~\0K^q\0~Qt\0\rsend_proposalpt\0	Conditionsq\0~\nò\0q\0~bsq\0~\0nE[¸q\0~Qt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0LîDlq\0~cq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~gxt\0Descriptionsq\0~\02\0q\0~ksq\0~\0be©q\0~Qt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~osq\0~\0c_Âˆq\0~Qt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\03∏cœq\0~pt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\08	ïq\0~ut\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0n-Á6q\0~ut\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~zq\0~}xxpxsq\0~\0E\0\0\0w\0\0\0q\0~Vq\0~Yq\0~\\q\0~_q\0~cq\0~lq\0~pxsq\0~\nÉt\0\nTransitionsq\0~\0E™—#q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0e„q\0~Çt\0transition8pq\0~\0sq\0~\0\0q\0~\0sq\0~\0\rÕq\0~Çt\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\05¸tBq\0~Çt\0parallelpq\0~\nìsq\0~\0q\0~\nìsq\0~\0\ròè…q\0~Çt\0tool1pt\0	Conditionsq\0~\nò\0q\0~ìsq\0~\0Ω6pq\0~Çt\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0I5°q\0~îq\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~òxt\0Descriptionsq\0~\02\0q\0~úsq\0~\0\0\Z7âq\0~Çt\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~†sq\0~\0bùGÑq\0~Çt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0‘®Ëq\0~°t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0˘Ò_q\0~¶t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0xvHùq\0~¶t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~´q\0~Æxxpxsq\0~\0E\0\0\0w\0\0\0q\0~áq\0~äq\0~çq\0~êq\0~îq\0~ùq\0~°xsq\0~\nÉt\0\nTransitionsq\0~\0AÖ68q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0rƒq\0~≥t\0transition5pq\0~\0sq\0~\0\0q\0~\0sq\0~\0qg¥dq\0~≥t\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0s$Uq\0~≥t\0approvalpq\0~\nìsq\0~\0q\0~\nìsq\0~\0;1:q\0~≥t\0tool2pt\0	Conditionsq\0~\nò\0q\0~ƒsq\0~\0ªhÜq\0~≥t\0status===\'rejected\'sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0HÊÚ¨q\0~≈t\0	CONDITIONsq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~…xt\0Descriptionsq\0~\02\0q\0~Œsq\0~\0\n2úkq\0~≥t\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~“sq\0~\0F:››q\0~≥t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0J.©“q\0~”t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0môcæq\0~ÿt\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0,Q^q\0~ÿt\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~›q\0~‡xxpxsq\0~\0E\0\0\0w\0\0\0q\0~∏q\0~ªq\0~æq\0~¡q\0~≈q\0~œq\0~”xsq\0~\nÉt\0\nTransitionsq\0~\0Z\0®*q\0~\n~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0&≥ã+q\0~Ât\0transition1pq\0~\0sq\0~\0\0q\0~\0sq\0~\0ezñ¶q\0~Ât\0\0pq\0~\nèsq\0~\0q\0~\nèsq\0~\0e_ÍÅq\0~Ât\0route1pq\0~\nìsq\0~\0q\0~\nìsq\0~\0C}·q\0~Ât\0approve_proposalpt\0	Conditionsq\0~\nò\0q\0~ˆsq\0~\0:‰3€q\0~Ât\0\0sq\0~\0w\0\0\0q\0~\0Üsq\0~\0\0q\0~\0Üsq\0~\0 ı›‰q\0~˜q\0~\0Psq\0~\0E\0\0\0w\0\0\0q\0~\0Pq\0~\n†q\0~\n°q\0~\n¢q\0~\n£xxsq\0~\0E\0\0\0w\0\0\0q\0~˚xt\0Descriptionsq\0~\02\0q\0~ˇsq\0~\0,u$q\0~Ât\0\0t\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0=“\r°q\0~Ât\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0P+q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0C¸â¬q\0~	t\0JaWE_GRAPH_TRANSITION_STYLEpq\0~£sq\0~\0\0q\0~£sq\0~\0iwï	q\0~	t\0NO_ROUTING_ORTHOGONALpxsq\0~\0E\0\0\0w\0\0\0q\0~q\0~xxpxsq\0~\0E\0\0\0w\0\0\0q\0~Íq\0~Ìq\0~q\0~Ûq\0~˜q\0~\0q\0~xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~sq\0~\0Üõq\0~^t\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0 ¶sΩq\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0}èuÀq\0~t\0%JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDERpq\0~£sq\0~\0\0q\0~£sq\0~\0eXÎ‰q\0~t\0requester;approverpxsq\0~\0E\0\0\0w\0\0\0q\0~!q\0~$xsq\0~öt\0ExtendedAttributesq\0~\0s∫q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0`4Yq\0~(t\0JaWE_GRAPH_START_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0fπq\0~(t\0ûJaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route1,X_OFFSET=87,Y_OFFSET=28,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~-q\0~0xsq\0~öt\0ExtendedAttributesq\0~\0D∂ƒTq\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0RlZÑq\0~4t\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0:ä\\q\0~4t\0õJaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool1,X_OFFSET=901,Y_OFFSET=74,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~9q\0~<xsq\0~öt\0ExtendedAttributesq\0~\0lÚ‚~q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0î–q\0~@t\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0.Œ¸q\0~@t\0•JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=send_proposal,X_OFFSET=849,Y_OFFSET=110,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~Eq\0~Hxsq\0~öt\0ExtendedAttributesq\0~\0h≤Ÿ	q\0~t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0®\'sq\0~Lt\0\ZJaWE_GRAPH_END_OF_WORKFLOWpq\0~£sq\0~\0\0q\0~£sq\0~\0-¸¿Iq\0~Lt\0úJaWE_GRAPH_PARTICIPANT_ID=approver,CONNECTING_ACTIVITY_ID=tool2,X_OFFSET=579,Y_OFFSET=180,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULTpxsq\0~\0E\0\0\0w\0\0\0q\0~Qq\0~Txxpxsq\0~\0E\0\0\0\rw\0\0\0\rq\0~cq\0~fq\0~jq\0~qq\0~¥q\0~”q\0~Ÿq\0~wq\0~}q\0~Ñq\0~ãq\0~\n~q\0~xxt\0ExtendedAttributessq\0~\0ﬁ\0q\0~Ysq\0~\0±”óq\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sq\0~öt\0ExtendedAttributesq\0~\0Ø\'Bq\0~Zt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0ËŸÉq\0~_t\0EDITING_TOOLpq\0~£sq\0~\0\0q\0~£sq\0~\0[Kˆ¶q\0~_t\0Web Workflow Designerpxsq\0~\0E\0\0\0w\0\0\0q\0~dq\0~gxsq\0~öt\0ExtendedAttributesq\0~\0@Í\'q\0~Zt\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0;a@Sq\0~kt\0EDITING_TOOL_VERSIONpq\0~£sq\0~\0\0q\0~£sq\0~\0fò≤}q\0~kt\0\r5.0-pre-alphapxsq\0~\0E\0\0\0w\0\0\0q\0~pq\0~sxxpxsq\0~\0E\0\0\0\rw\0\0\0\rq\0~\0q\0~\0q\0~\0q\0~\0Iq\0~\0tq\0~\0Çq\0~\0ïq\0~\0úq\0~\0£q\0~q\0~Qq\0~Xq\0~Zx\0sq\0~\0w\0\0\0\0xt\01sr\0*org.enhydra.shark.xpdl.elements.Namespaces|ö—<.Rßñ\0\0xq\0~\0kt\0\nNamespacessq\0~\0G¬D∆q\0~\0\nt\0\0sq\0~\0w\0\0\0\0xsq\0~\0E\0\0\0w\0\0\0sr\0)org.enhydra.shark.xpdl.elements.NamespaceËz¬¬_\0\0xq\0~\0t\0	Namespacesq\0~\0	oπ|q\0~{t\0\0sq\0~\0w\0\0\0q\0~\0sq\0~\0q\0~\0sq\0~\0[d9Fq\0~Çt\0xpdlpq\0~\0‘sq\0~\0q\0~\0‘sq\0~\0,ãq\0~Çt\0 http://www.wfmc.org/2002/XPDL1.0pxsq\0~\0E\0\0\0w\0\0\0q\0~áq\0~äxx',1000202,1,1000203,0);
/*!40000 ALTER TABLE `shkxpdldata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shkxpdlhistory`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdlhistory` (
  `XPDLId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `XPDLVersion` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `XPDLClassVersion` bigint(20) NOT NULL,
  `XPDLUploadTime` datetime NOT NULL,
  `XPDLHistoryUploadTime` datetime NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLHistory` (`XPDLId`,`XPDLVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdlhistorydata` (
  `XPDLContent` mediumblob NOT NULL,
  `XPDLClassContent` mediumblob NOT NULL,
  `XPDLHistory` decimal(19,0) NOT NULL,
  `CNT` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLHistoryData` (`CNT`),
  KEY `SHKXPDLHistoryData_XPDLHistory` (`XPDLHistory`),
  CONSTRAINT `SHKXPDLHistoryData_XPDLHistory` FOREIGN KEY (`XPDLHistory`) REFERENCES `shkxpdlhistory` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdlparticipantpackage` (
  `PACKAGE_ID` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLParticipantPackage` (`PACKAGE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdlparticipantprocess` (
  `PROCESS_ID` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `PACKAGEOID` decimal(19,0) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLParticipantProcess` (`PROCESS_ID`,`PACKAGEOID`),
  KEY `SHKXPDLParticipantProcess_PACKAGEOID` (`PACKAGEOID`),
  CONSTRAINT `SHKXPDLParticipantProcess_PACKAGEOID` FOREIGN KEY (`PACKAGEOID`) REFERENCES `shkxpdlparticipantpackage` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdlreferences` (
  `ReferredXPDLId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `ReferringXPDL` decimal(19,0) NOT NULL,
  `ReferredXPDLNumber` int(11) NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLReferences` (`ReferredXPDLId`,`ReferringXPDL`),
  KEY `SHKXPDLReferences_ReferringXPDL` (`ReferringXPDL`),
  CONSTRAINT `SHKXPDLReferences_ReferringXPDL` FOREIGN KEY (`ReferringXPDL`) REFERENCES `shkxpdls` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shkxpdls` (
  `XPDLId` varchar(90) COLLATE utf8_unicode_ci NOT NULL,
  `XPDLVersion` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `XPDLClassVersion` bigint(20) NOT NULL,
  `XPDLUploadTime` datetime NOT NULL,
  `oid` decimal(19,0) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`oid`),
  UNIQUE KEY `I1_SHKXPDLS` (`XPDLId`,`XPDLVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shkxpdls`
--

LOCK TABLES `shkxpdls` WRITE;
/*!40000 ALTER TABLE `shkxpdls` DISABLE KEYS */;
INSERT INTO `shkxpdls` VALUES ('crm_community','1',1184704391000,'2019-10-07 05:06:10',1000202,0);
/*!40000 ALTER TABLE `shkxpdls` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_audit_trail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_audit_trail` (
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `clazz` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `method` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `message` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `appId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appVersion` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_audit_trail`
--

LOCK TABLES `wf_audit_trail` WRITE;
/*!40000 ALTER TABLE `wf_audit_trail` DISABLE KEYS */;
INSERT INTO `wf_audit_trail` VALUES ('8a80828f6da49b7d016da49c31380000','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','crm_community','2019-10-07 05:06:02',NULL,NULL),('8a80828f6da49b7d016da49c34f10001','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=false}','2019-10-07 05:06:03',NULL,NULL),('8a80828f6da49b7d016da49c35400002','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_account\", appId:\"crm_community\", appVersion:\"1\", name:\"Account Form\", dateCreated:\"Mon Oct 07 05:06:03 GMT 2019\", dateModified:\"Mon Oct 07 05:06:03 GMT 2019\"}','2019-10-07 05:06:03',NULL,NULL),('8a80828f6da49b7d016da49c35bc0003','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_contact\", appId:\"crm_community\", appVersion:\"1\", name:\"Contact Form\", dateCreated:\"Mon Oct 07 05:06:03 GMT 2019\", dateModified:\"Mon Oct 07 05:06:03 GMT 2019\"}','2019-10-07 05:06:03',NULL,NULL),('8a80828f6da49b7d016da49c384c0004','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_opportunity\", appId:\"crm_community\", appVersion:\"1\", name:\"Opportunity Form\", dateCreated:\"Mon Oct 07 05:06:04 GMT 2019\", dateModified:\"Mon Oct 07 05:06:04 GMT 2019\"}','2019-10-07 05:06:04',NULL,NULL),('8a80828f6da49b7d016da49c38f80005','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_proposal_approval_form\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal Approval Form\", dateCreated:\"Mon Oct 07 05:06:04 GMT 2019\", dateModified:\"Mon Oct 07 05:06:04 GMT 2019\"}','2019-10-07 05:06:04',NULL,NULL),('8a80828f6da49b7d016da49c39360006','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_proposal_form\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal Form\", dateCreated:\"Mon Oct 07 05:06:04 GMT 2019\", dateModified:\"Mon Oct 07 05:06:04 GMT 2019\"}','2019-10-07 05:06:04',NULL,NULL),('8a80828f6da49b7d016da49c39850007','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_proposal_resubmit_form\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal Resubmit Form\", dateCreated:\"Mon Oct 07 05:06:04 GMT 2019\", dateModified:\"Mon Oct 07 05:06:04 GMT 2019\"}','2019-10-07 05:06:04',NULL,NULL),('8a80828f6da49b7d016da49c39d30008','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"crm_proposal_sending_form\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal Sending Form\", dateCreated:\"Mon Oct 07 05:06:04 GMT 2019\", dateModified:\"Mon Oct 07 05:06:04 GMT 2019\"}','2019-10-07 05:06:04',NULL,NULL),('8a80828f6da49b7d016da49c3dca0009','roleAnonymous','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"crm_account_list\", appId:\"crm_community\", appVersion:\"1\", name:\"Account Listing\", dateCreated:\"Mon Oct 07 05:06:05 GMT 2019\", dateModified:\"Mon Oct 07 05:06:05 GMT 2019\"}','2019-10-07 05:06:05',NULL,NULL),('8a80828f6da49b7d016da49c3e09000a','roleAnonymous','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"crm_contact_list\", appId:\"crm_community\", appVersion:\"1\", name:\"Contact List\", dateCreated:\"Mon Oct 07 05:06:05 GMT 2019\", dateModified:\"Mon Oct 07 05:06:05 GMT 2019\"}','2019-10-07 05:06:05',NULL,NULL),('8a80828f6da49b7d016da49c3eb5000b','roleAnonymous','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"crm_opportunity_list\", appId:\"crm_community\", appVersion:\"1\", name:\"Opportunity List\", dateCreated:\"Mon Oct 07 05:06:06 GMT 2019\", dateModified:\"Mon Oct 07 05:06:06 GMT 2019\"}','2019-10-07 05:06:06',NULL,NULL),('8a80828f6da49b7d016da49c3ef3000c','roleAnonymous','org.joget.apps.app.dao.DatalistDefinitionDaoImpl','add','{id:\"Proposal\", appId:\"crm_community\", appVersion:\"1\", name:\"Proposal List\", dateCreated:\"Mon Oct 07 05:06:06 GMT 2019\", dateModified:\"Mon Oct 07 05:06:06 GMT 2019\"}','2019-10-07 05:06:06',NULL,NULL),('8a80828f6da49b7d016da49c3f8f000d','roleAnonymous','org.joget.apps.app.dao.UserviewDefinitionDaoImpl','add','{id:\"crm_userview_sales\", appId:\"crm_community\", appVersion:\"1\", name:\"Customer Relationship Management\", dateCreated:\"Mon Oct 07 05:06:06 GMT 2019\", dateModified:\"Mon Oct 07 05:06:06 GMT 2019\"}','2019-10-07 05:06:06',NULL,NULL),('8a80828f6da49b7d016da49c401c000e','roleAnonymous','org.joget.apps.app.dao.EnvironmentVariableDaoImpl','add','{id:\"AppName\", appId:\"crm_community\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:06:06','crm_community','1'),('8a80828f6da49b7d016da49c4d6a000f','roleAnonymous','org.joget.apps.app.dao.EnvironmentVariableDaoImpl','add','{id:\"refNo\", appId:\"crm_community\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:06:09','crm_community','1'),('8a80828f6da49b7d016da49c4d990010','roleAnonymous','org.joget.apps.app.dao.AppResourceDaoImpl','add','{id:\"crm-icon7.png\", appId:\"crm_community\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:06:09','crm_community','1'),('8a80828f6da49b7d016da49c51ee0011','roleAnonymous','org.joget.workflow.model.service.WorkflowManagerImpl','processUpload','','2019-10-07 05:06:11','crm_community','1'),('8a80828f6da49b7d016da49c54cd0012','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm_community','2019-10-07 05:06:11','crm_community','1'),('8a80828f6da49b7d016da49c55790013','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm_community','2019-10-07 05:06:11','crm_community','1'),('8a80828f6da49b7d016da49c57fa0014','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm_community','2019-10-07 05:06:12','crm_community','1'),('8a80828f6da49b7d016da49c58960015','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm_community','2019-10-07 05:06:12','crm_community','1'),('8a80828f6da49b7d016da49c59510016','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityForm','crm_community','2019-10-07 05:06:12','crm_community','1'),('8a80828f6da49b7d016da49c598f0017','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','crm_community','2019-10-07 05:06:13','crm_community','1'),('8a80828f6da49b7d016da49c59ce0018','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppActivityPlugin','crm_community','2019-10-07 05:06:13','crm_community','1'),('8a80828f6da49b7d016da49c5a3c0019','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm_community','2019-10-07 05:06:13','crm_community','1'),('8a80828f6da49b7d016da49c5a99001a','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm_community','2019-10-07 05:06:13','crm_community','1'),('8a80828f6da49b7d016da49c5ae8001b','roleAnonymous','org.joget.apps.app.dao.PackageDefinitionDaoImpl','addAppParticipant','crm_community','2019-10-07 05:06:13','crm_community','1'),('8a80828f6da49b7d016da49c5b06001c','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=false}','2019-10-07 05:06:13','crm_community','1'),('8a80828f6da49b7d016da49c5c5e001d','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=crm_community, version=1, published=true}','2019-10-06 22:06:13','crm_community','1'),('8a80828f6da49b7d016da4a12691001e','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','appcenter','2019-10-07 05:11:27',NULL,NULL),('8a80828f6da49b7d016da4a129bd001f','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=appcenter, version=1, published=false}','2019-10-07 05:11:28',NULL,NULL),('8a80828f6da49b7d016da4a12a490020','roleAnonymous','org.joget.apps.app.dao.FormDefinitionDaoImpl','add','{id:\"landing\", appId:\"appcenter\", appVersion:\"1\", name:\"Published Apps\", dateCreated:\"Mon Oct 07 05:11:28 GMT 2019\", dateModified:\"Mon Oct 07 05:11:28 GMT 2019\"}','2019-10-07 05:11:28',NULL,NULL),('8a80828f6da49b7d016da4a12bc10021','roleAnonymous','org.joget.apps.app.dao.UserviewDefinitionDaoImpl','add','{id:\"v\", appId:\"appcenter\", appVersion:\"1\", name:\"Joget DX\", dateCreated:\"Mon Oct 07 05:11:28 GMT 2019\", dateModified:\"Mon Oct 07 05:11:28 GMT 2019\"}','2019-10-07 05:11:28',NULL,NULL),('8a80828f6da49b7d016da4a12c2e0022','roleAnonymous','org.joget.apps.app.dao.UserviewDefinitionDaoImpl','add','{id:\"v2\", appId:\"appcenter\", appVersion:\"1\", name:\"Joget DX Platform\", dateCreated:\"Mon Oct 07 05:11:29 GMT 2019\", dateModified:\"Mon Oct 07 05:11:29 GMT 2019\"}','2019-10-07 05:11:29',NULL,NULL),('8a80828f6da49b7d016da4a12e310023','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"<i class=\'fa fa-home\'></i> Home_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:29','appcenter','1'),('8a80828f6da49b7d016da4a12e9f0024','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"<i class=\'fa fa-home\'></i> Home_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:29','appcenter','1'),('8a80828f6da49b7d016da4a130540025','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"App Center_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:30','appcenter','1'),('8a80828f6da49b7d016da4a1312f0026','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"App Center_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:30','appcenter','1'),('8a80828f6da49b7d016da4a1316e0027','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Design New App_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:30','appcenter','1'),('8a80828f6da49b7d016da4a132c60028','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Design New App_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:30','appcenter','1'),('8a80828f6da49b7d016da4a133040029','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Download from Marketplace_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:30','appcenter','1'),('8a80828f6da49b7d016da4a133bf002a','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Download from Marketplace_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:31','appcenter','1'),('8a80828f6da49b7d016da4a1348b002b','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Faster, Simpler Digital Transformation_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:31','appcenter','1'),('8a80828f6da49b7d016da4a13508002c','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Faster, Simpler Digital Transformation_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:31','appcenter','1'),('8a80828f6da49b7d016da4a135c3002d','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Import App_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:31','appcenter','1'),('8a80828f6da49b7d016da4a13621002e','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Import App_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:31','appcenter','1'),('8a80828f6da49b7d016da4a136dc002f','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Powered by Joget_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:31','appcenter','1'),('8a80828f6da49b7d016da4a1372b0030','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Powered by Joget_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:31','appcenter','1'),('8a80828f6da49b7d016da4a137e60031','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Published Apps_zh_CN\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:32','appcenter','1'),('8a80828f6da49b7d016da4a138540032','roleAnonymous','org.joget.apps.app.dao.MessageDaoImpl','add','{id:\"Published Apps_zh_TW\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:32','appcenter','1'),('8a80828f6da49b7d016da4a138820033','roleAnonymous','org.joget.apps.app.dao.AppResourceDaoImpl','add','{id:\"background-beach.jpg\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:32','appcenter','1'),('8a80828f6da49b7d016da4a138820034','roleAnonymous','org.joget.apps.app.dao.AppResourceDaoImpl','add','{id:\"background-city.jpg\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:32','appcenter','1'),('8a80828f6da49b7d016da4a138d00035','roleAnonymous','org.joget.apps.app.dao.AppResourceDaoImpl','add','{id:\"background-industrial.jpg\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:32','appcenter','1'),('8a80828f6da49b7d016da4a138d00036','roleAnonymous','org.joget.apps.app.dao.AppResourceDaoImpl','add','{id:\"banner.png\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:32','appcenter','1'),('8a80828f6da49b7d016da4a138d00037','roleAnonymous','org.joget.apps.app.dao.AppResourceDaoImpl','add','{id:\"logo.png\", appId:\"appcenter\", appVersion:\"1\", name:\"null\", dateCreated:\"null\", dateModified:\"null\"}','2019-10-07 05:11:32','appcenter','1'),('8a80828f6da49b7d016da4a13a180038','roleAnonymous','org.joget.apps.app.dao.AppDefinitionDaoImpl','saveOrUpdate','{id=appcenter, version=1, published=true}','2019-10-06 22:11:32','appcenter','1');
/*!40000 ALTER TABLE `wf_audit_trail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_process_link`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wf_process_link` (
  `processId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `parentProcessId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `originProcessId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`processId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `activityInstanceId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `processInstanceId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `priority` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `createdTime` datetime DEFAULT NULL,
  `startedTime` datetime DEFAULT NULL,
  `dateLimit` bigint(20) DEFAULT NULL,
  `due` datetime DEFAULT NULL,
  `delay` bigint(20) DEFAULT NULL,
  `finishTime` datetime DEFAULT NULL,
  `timeConsumingFromDateCreated` bigint(20) DEFAULT NULL,
  `timeConsumingFromDateStarted` bigint(20) DEFAULT NULL,
  `performer` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `nameOfAcceptedUser` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `state` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `packageId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `processDefId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `activityDefId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `assignmentUsers` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `appId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appVersion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`activityInstanceId`),
  KEY `FKB943CCA47A4E8F48` (`packageId`),
  KEY `FKB943CCA4A39D6461` (`processDefId`),
  KEY `FKB943CCA4CB863F` (`activityDefId`),
  CONSTRAINT `FKB943CCA47A4E8F48` FOREIGN KEY (`packageId`) REFERENCES `wf_report_package` (`packageId`),
  CONSTRAINT `FKB943CCA4A39D6461` FOREIGN KEY (`processDefId`) REFERENCES `wf_report_process` (`processDefId`),
  CONSTRAINT `FKB943CCA4CB863F` FOREIGN KEY (`activityDefId`) REFERENCES `wf_report_activity` (`activityDefId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `activityDefId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `activityName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `priority` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`activityDefId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `packageId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `packageName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`packageId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `processDefId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `processName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `version` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`processDefId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `messageKey` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `locale` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `message` text COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
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
  `id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `property` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `value` text COLLATE utf8_unicode_ci DEFAULT NULL,
  `ordering` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_setup`
--

LOCK TABLES `wf_setup` WRITE;
/*!40000 ALTER TABLE `wf_setup` DISABLE KEYS */;
INSERT INTO `wf_setup` VALUES ('8a80828f6da49b7d016da4a13a380039','defaultUserview','appcenter/v',NULL);
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

-- Dump completed on 2019-10-06 22:12:42
