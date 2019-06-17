SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKActivityStateEventAudits](
	[KeyValue] [nvarchar](30) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKActivityStateEventAudits_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKActivityStateEventAudits] ON [SHKActivityStateEventAudits] 
(
	[KeyValue] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKActivityStateEventAudits] ON [SHKActivityStateEventAudits] 
(
	[Name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKActivityStates](
	[KeyValue] [nvarchar](30) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKActivityStates_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKActivityStates] ON [SHKActivityStates] 
(
	[KeyValue] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKActivityStates] ON [SHKActivityStates] 
(
	[Name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [wf_resource_bundle_message](
	[id] [nvarchar](255) NOT NULL,
	[messageKey] [nvarchar](255) NULL,
	[locale] [nvarchar](255) NULL,
	[message] [ntext] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [wf_setup](
	[id] [nvarchar](255) NOT NULL,
	[property] [nvarchar](255) NULL,
	[value] [ntext] NULL,
	[ordering] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKCounters](
	[name] [nvarchar](100) NOT NULL,
	[the_number] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKCounters_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKCounters] ON [SHKCounters] 
(
	[name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_organization](
	[id] [nvarchar](255) NOT NULL,
	[name] [nvarchar](255) NULL,
	[description] [nvarchar](255) NULL,
	[parentId] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKEventTypes](
	[KeyValue] [nvarchar](30) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKEventTypes_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKEventTypes] ON [SHKEventTypes] 
(
	[KeyValue] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKEventTypes] ON [SHKEventTypes] 
(
	[Name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_role](
	[id] [nvarchar](255) NOT NULL,
	[name] [nvarchar](255) NULL,
	[description] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_user](
	[id] [nvarchar](255) NOT NULL,
	[username] [nvarchar](255) NULL,
	[password] [nvarchar](255) NULL,
	[firstName] [nvarchar](255) NULL,
	[lastName] [nvarchar](255) NULL,
	[email] [nvarchar](255) NULL,
	[timeZone] [nvarchar](255) NULL,
	[locale] [nvarchar](255) NULL,
	[active] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKGroupTable](
	[groupid] [nvarchar](100) NOT NULL,
	[description] [nvarchar](254) NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKGroupTable_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKGroupTable] ON [SHKGroupTable] 
(
	[groupid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKGroupUser](
	[USERNAME] [nvarchar](100) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKGroupUser_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKGroupUser] ON [SHKGroupUser] 
(
	[USERNAME] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKNextXPDLVersions](
	[XPDLId] [nvarchar](90) NOT NULL,
	[NextVersion] [nvarchar](20) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKNextXPDLVersions_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKNextXPDLVersions] ON [SHKNextXPDLVersions] 
(
	[XPDLId] ASC,
	[NextVersion] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKNormalUser](
	[USERNAME] [nvarchar](100) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKNormalUser_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKNormalUser] ON [SHKNormalUser] 
(
	[USERNAME] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [wf_process_link](
	[processId] [nvarchar](255) NOT NULL,
	[parentProcessId] [nvarchar](255) NULL,
	[originProcessId] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[processId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_fd](
	[id] [nvarchar](255) NOT NULL,
	[dateCreated] [datetime] NULL,
	[dateModified] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_app](
	[appId] [nvarchar](255) NOT NULL,
	[appVersion] [numeric](19, 0) NOT NULL,
	[name] [nvarchar](255) NULL,
	[license] [ntext] NULL,
	[published] [tinyint] NULL,
	[dateCreated] [datetime] NULL,
	[dateModified] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[appId] ASC,
	[appVersion] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [wf_audit_trail](
	[id] [nvarchar](255) NOT NULL,
	[username] [nvarchar](255) NULL,
	[appId] [nvarchar](255) NULL,
	[appVersion] [nvarchar](255) NULL,
	[clazz] [nvarchar](255) NULL,
	[method] [nvarchar](255) NULL,
	[message] [ntext] NULL,
	[timestamp] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcessDefinitions](
	[Name] [nvarchar](200) NOT NULL,
	[PackageId] [nvarchar](90) NOT NULL,
	[ProcessDefinitionId] [nvarchar](90) NOT NULL,
	[ProcessDefinitionCreated] [bigint] NOT NULL,
	[ProcessDefinitionVersion] [nvarchar](20) NOT NULL,
	[State] [int] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcessDefinitions_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcessDefinitions] ON [SHKProcessDefinitions] 
(
	[Name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcessStateEventAudits](
	[KeyValue] [nvarchar](30) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcessStateEventAudits_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcessStateEventAudits] ON [SHKProcessStateEventAudits] 
(
	[KeyValue] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKProcessStateEventAudits] ON [SHKProcessStateEventAudits] 
(
	[Name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcessStates](
	[KeyValue] [nvarchar](30) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcessStates_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcessStates] ON [SHKProcessStates] 
(
	[KeyValue] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKProcessStates] ON [SHKProcessStates] 
(
	[Name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKToolAgentApp](
	[TOOL_AGENT_NAME] [nvarchar](250) NOT NULL,
	[APP_NAME] [nvarchar](90) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKToolAgentApp_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKToolAgentApp] ON [SHKToolAgentApp] 
(
	[TOOL_AGENT_NAME] ASC,
	[APP_NAME] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_report_app](
	[uuid] [nvarchar](255) NOT NULL,
	[appId] [nvarchar](255) NULL,
	[appVersion] [nvarchar](255) NULL,
	[appName] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[uuid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKResourcesTable](
	[Username] [nvarchar](100) NOT NULL,
	[Name] [nvarchar](100) NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKResourcesTable_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKResourcesTable] ON [SHKResourcesTable] 
(
	[Username] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKToolAgentUser](
	[USERNAME] [nvarchar](100) NOT NULL,
	[PWD] [nvarchar](100) NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKToolAgentUser_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKToolAgentUser] ON [SHKToolAgentUser] 
(
	[USERNAME] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKUserTable](
	[userid] [nvarchar](100) NOT NULL,
	[firstname] [nvarchar](50) NULL,
	[lastname] [nvarchar](50) NULL,
	[passwd] [nvarchar](50) NOT NULL,
	[email] [nvarchar](254) NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKUserTable_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKUserTable] ON [SHKUserTable] 
(
	[userid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLApplicationPackage](
	[PACKAGE_ID] [nvarchar](90) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLApplicationPackage_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLApplicationPackage] ON [SHKXPDLApplicationPackage] 
(
	[PACKAGE_ID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLHistory](
	[XPDLId] [nvarchar](90) NOT NULL,
	[XPDLVersion] [nvarchar](20) NOT NULL,
	[XPDLClassVersion] [bigint] NOT NULL,
	[XPDLUploadTime] [datetime] NOT NULL,
	[XPDLHistoryUploadTime] [datetime] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLHistory_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLHistory] ON [SHKXPDLHistory] 
(
	[XPDLId] ASC,
	[XPDLVersion] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLS](
	[XPDLId] [nvarchar](90) NOT NULL,
	[XPDLVersion] [nvarchar](20) NOT NULL,
	[XPDLClassVersion] [bigint] NOT NULL,
	[XPDLUploadTime] [datetime] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLS_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLS] ON [SHKXPDLS] 
(
	[XPDLId] ASC,
	[XPDLVersion] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [ObjectId](
	[nextoid] [decimal](19, 0) NOT NULL,
 CONSTRAINT [PK__ObjectId__44CA3770] PRIMARY KEY CLUSTERED 
(
	[nextoid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLParticipantPackage](
	[PACKAGE_ID] [nvarchar](90) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLParticipantPackage_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLParticipantPackage] ON [SHKXPDLParticipantPackage] 
(
	[PACKAGE_ID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKActivityDataBLOBs](
	[ActivityDataWOB] [decimal](19, 0) NOT NULL,
	[VariableValue] [image] NULL,
	[OrdNo] [int] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKActivityDataBLOBs_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKActivityDataBLOBs] ON [SHKActivityDataBLOBs] 
(
	[ActivityDataWOB] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;

CREATE TABLE [SHKStateEventAudits](
	[RecordedTime] [bigint] NOT NULL,
	[RecordedTimeTZO] [bigint] NOT NULL,
	[TheUsername] [nvarchar](100) NOT NULL,
	[TheType] [decimal](19, 0) NOT NULL,
	[ActivityId] [nvarchar](100) NULL,
	[ActivityName] [nvarchar](254) NULL,
	[ProcessId] [nvarchar](100) NOT NULL,
	[ProcessName] [nvarchar](254) NULL,
	[ProcessFactoryName] [nvarchar](200) NOT NULL,
	[ProcessFactoryVersion] [nvarchar](20) NOT NULL,
	[ActivityDefinitionId] [nvarchar](90) NULL,
	[ActivityDefinitionName] [nvarchar](90) NULL,
	[ActivityDefinitionType] [int] NULL,
	[ProcessDefinitionId] [nvarchar](90) NOT NULL,
	[ProcessDefinitionName] [nvarchar](90) NULL,
	[PackageId] [nvarchar](90) NOT NULL,
	[OldProcessState] [decimal](19, 0) NULL,
	[NewProcessState] [decimal](19, 0) NULL,
	[OldActivityState] [decimal](19, 0) NULL,
	[NewActivityState] [decimal](19, 0) NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKStateEventAudits_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKStateEventAudits] ON [SHKStateEventAudits] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKActivities](
	[Id] [nvarchar](100) NOT NULL,
	[ActivitySetDefinitionId] [nvarchar](90) NULL,
	[ActivityDefinitionId] [nvarchar](90) NOT NULL,
	[Process] [decimal](19, 0) NOT NULL,
	[TheResource] [decimal](19, 0) NULL,
	[PDefName] [nvarchar](200) NOT NULL,
	[ProcessId] [nvarchar](200) NOT NULL,
	[ResourceId] [nvarchar](100) NULL,
	[State] [decimal](19, 0) NOT NULL,
	[BlockActivityId] [nvarchar](100) NULL,
	[Performer] [nvarchar](100) NULL,
	[IsPerformerAsynchronous] [tinyint] NULL,
	[Priority] [int] NULL,
	[Name] [nvarchar](254) NULL,
	[Activated] [bigint] NOT NULL,
	[ActivatedTZO] [bigint] NOT NULL,
	[Accepted] [bigint] NULL,
	[AcceptedTZO] [bigint] NULL,
	[LastStateTime] [bigint] NOT NULL,
	[LastStateTimeTZO] [bigint] NOT NULL,
	[LimitTime] [bigint] NOT NULL,
	[LimitTimeTZO] [bigint] NOT NULL,
	[Description] [nvarchar](254) NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKActivities_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKActivities] ON [SHKActivities] 
(
	[Id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I2_SHKActivities] ON [SHKActivities] 
(
	[Process] ASC,
	[ActivitySetDefinitionId] ASC,
	[ActivityDefinitionId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I3_SHKActivities] ON [SHKActivities] 
(
	[Process] ASC,
	[State] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_employment](
	[id] [nvarchar](255) NOT NULL,
	[userId] [nvarchar](255) NULL,
	[startDate] [datetime] NULL,
	[endDate] [datetime] NULL,
	[employeeCode] [nvarchar](255) NULL,
	[role] [nvarchar](255) NULL,
	[gradeId] [nvarchar](255) NULL,
	[departmentId] [nvarchar](255) NULL,
	[organizationId] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_department](
	[id] [nvarchar](255) NOT NULL,
	[name] [nvarchar](255) NULL,
	[description] [nvarchar](255) NULL,
	[organizationId] [nvarchar](255) NULL,
	[hod] [nvarchar](255) NULL,
	[parentId] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_employment_report_to](
	[id] [nvarchar](255) NOT NULL,
	[employmentId] [nvarchar](255) NULL,
	[reportToId] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[employmentId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKOldEventAuditDataWOB](
	[DataEventAudit] [decimal](19, 0) NOT NULL,
	[VariableDefinitionId] [nvarchar](100) NOT NULL,
	[VariableType] [int] NOT NULL,
	[VariableValueXML] [xml] NULL,
	[VariableValueVCHAR] [nvarchar](4000) NULL,
	[VariableValueDBL] [float] NULL,
	[VariableValueLONG] [bigint] NULL,
	[VariableValueDATE] [datetime] NULL,
	[VariableValueBOOL] [tinyint] NULL,
	[OrdNo] [int] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKOldEventAuditDataWOB_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKOldEventAuditDataWOB] ON [SHKOldEventAuditDataWOB] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKOldEventAuditDataWOB] ON [SHKOldEventAuditDataWOB] 
(
	[DataEventAudit] ASC,
	[VariableDefinitionId] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKNewEventAuditData](
	[DataEventAudit] [decimal](19, 0) NOT NULL,
	[VariableDefinitionId] [nvarchar](100) NOT NULL,
	[VariableType] [int] NOT NULL,
	[VariableValue] [image] NULL,
	[VariableValueXML] [xml] NULL,
	[VariableValueVCHAR] [nvarchar](4000) NULL,
	[VariableValueDBL] [float] NULL,
	[VariableValueLONG] [bigint] NULL,
	[VariableValueDATE] [datetime] NULL,
	[VariableValueBOOL] [tinyint] NULL,
	[OrdNo] [int] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKNewEventAuditData_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKNewEventAuditData] ON [SHKNewEventAuditData] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKNewEventAuditData] ON [SHKNewEventAuditData] 
(
	[DataEventAudit] ASC,
	[VariableDefinitionId] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKOldEventAuditData](
	[DataEventAudit] [decimal](19, 0) NOT NULL,
	[VariableDefinitionId] [nvarchar](100) NOT NULL,
	[VariableType] [int] NOT NULL,
	[VariableValue] [image] NULL,
	[VariableValueXML] [xml] NULL,
	[VariableValueVCHAR] [nvarchar](4000) NULL,
	[VariableValueDBL] [float] NULL,
	[VariableValueLONG] [bigint] NULL,
	[VariableValueDATE] [datetime] NULL,
	[VariableValueBOOL] [tinyint] NULL,
	[OrdNo] [int] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKOldEventAuditData_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKOldEventAuditData] ON [SHKOldEventAuditData] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKOldEventAuditData] ON [SHKOldEventAuditData] 
(
	[DataEventAudit] ASC,
	[VariableDefinitionId] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKNewEventAuditDataWOB](
	[DataEventAudit] [decimal](19, 0) NOT NULL,
	[VariableDefinitionId] [nvarchar](100) NOT NULL,
	[VariableType] [int] NOT NULL,
	[VariableValueXML] [xml] NULL,
	[VariableValueVCHAR] [nvarchar](4000) NULL,
	[VariableValueDBL] [float] NULL,
	[VariableValueLONG] [bigint] NULL,
	[VariableValueDATE] [datetime] NULL,
	[VariableValueBOOL] [tinyint] NULL,
	[OrdNo] [int] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKNewEventAuditDataWOB_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKNewEventAuditDataWOB] ON [SHKNewEventAuditDataWOB] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKNewEventAuditDataWOB] ON [SHKNewEventAuditDataWOB] 
(
	[DataEventAudit] ASC,
	[VariableDefinitionId] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_user_group](
	[groupId] [nvarchar](255) NOT NULL,
	[userId] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[userId] ASC,
	[groupId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_grade](
	[id] [nvarchar](255) NOT NULL,
	[name] [nvarchar](255) NULL,
	[description] [nvarchar](255) NULL,
	[organizationId] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_group](
	[id] [nvarchar](255) NOT NULL,
	[name] [nvarchar](255) NULL,
	[description] [nvarchar](255) NULL,
	[organizationId] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKAssignmentEventAudits](
	[RecordedTime] [bigint] NOT NULL,
	[RecordedTimeTZO] [bigint] NOT NULL,
	[TheUsername] [nvarchar](100) NOT NULL,
	[TheType] [decimal](19, 0) NOT NULL,
	[ActivityId] [nvarchar](100) NOT NULL,
	[ActivityName] [nvarchar](254) NULL,
	[ProcessId] [nvarchar](100) NOT NULL,
	[ProcessName] [nvarchar](254) NULL,
	[ProcessFactoryName] [nvarchar](200) NOT NULL,
	[ProcessFactoryVersion] [nvarchar](20) NOT NULL,
	[ActivityDefinitionId] [nvarchar](90) NOT NULL,
	[ActivityDefinitionName] [nvarchar](90) NULL,
	[ActivityDefinitionType] [int] NOT NULL,
	[ProcessDefinitionId] [nvarchar](90) NOT NULL,
	[ProcessDefinitionName] [nvarchar](90) NULL,
	[PackageId] [nvarchar](90) NOT NULL,
	[OldResourceUsername] [nvarchar](100) NULL,
	[OldResourceName] [nvarchar](100) NULL,
	[NewResourceUsername] [nvarchar](100) NOT NULL,
	[NewResourceName] [nvarchar](100) NULL,
	[IsAccepted] [tinyint] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKAssignmentEventAudits_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKAssignmentEventAudits] ON [SHKAssignmentEventAudits] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKDataEventAudits](
	[RecordedTime] [bigint] NOT NULL,
	[RecordedTimeTZO] [bigint] NOT NULL,
	[TheUsername] [nvarchar](100) NOT NULL,
	[TheType] [decimal](19, 0) NOT NULL,
	[ActivityId] [nvarchar](100) NULL,
	[ActivityName] [nvarchar](254) NULL,
	[ProcessId] [nvarchar](100) NOT NULL,
	[ProcessName] [nvarchar](254) NULL,
	[ProcessFactoryName] [nvarchar](200) NOT NULL,
	[ProcessFactoryVersion] [nvarchar](20) NOT NULL,
	[ActivityDefinitionId] [nvarchar](90) NULL,
	[ActivityDefinitionName] [nvarchar](90) NULL,
	[ActivityDefinitionType] [int] NULL,
	[ProcessDefinitionId] [nvarchar](90) NOT NULL,
	[ProcessDefinitionName] [nvarchar](90) NULL,
	[PackageId] [nvarchar](90) NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKDataEventAudits_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKDataEventAudits] ON [SHKDataEventAudits] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKCreateProcessEventAudits](
	[RecordedTime] [bigint] NOT NULL,
	[RecordedTimeTZO] [bigint] NOT NULL,
	[TheUsername] [nvarchar](100) NOT NULL,
	[TheType] [decimal](19, 0) NOT NULL,
	[ProcessId] [nvarchar](100) NOT NULL,
	[ProcessName] [nvarchar](254) NULL,
	[ProcessFactoryName] [nvarchar](200) NOT NULL,
	[ProcessFactoryVersion] [nvarchar](20) NOT NULL,
	[ProcessDefinitionId] [nvarchar](90) NOT NULL,
	[ProcessDefinitionName] [nvarchar](90) NULL,
	[PackageId] [nvarchar](90) NOT NULL,
	[PActivityId] [nvarchar](100) NULL,
	[PProcessId] [nvarchar](100) NULL,
	[PProcessName] [nvarchar](254) NULL,
	[PProcessFactoryName] [nvarchar](200) NULL,
	[PProcessFactoryVersion] [nvarchar](20) NULL,
	[PActivityDefinitionId] [nvarchar](90) NULL,
	[PActivityDefinitionName] [nvarchar](90) NULL,
	[PProcessDefinitionId] [nvarchar](90) NULL,
	[PProcessDefinitionName] [nvarchar](90) NULL,
	[PPackageId] [nvarchar](90) NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKCreateProcessEventAudits_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKCreateProcessEventAudits] ON [SHKCreateProcessEventAudits] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dir_user_role](
	[roleId] [nvarchar](255) NOT NULL,
	[userId] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[userId] ASC,
	[roleId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKGroupGroupTable](
	[sub_gid] [decimal](19, 0) NOT NULL,
	[groupid] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKGroupGroupTable_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKGroupGroupTable] ON [SHKGroupGroupTable] 
(
	[sub_gid] ASC,
	[groupid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I2_SHKGroupGroupTable] ON [SHKGroupGroupTable] 
(
	[groupid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKUserGroupTable](
	[userid] [decimal](19, 0) NOT NULL,
	[groupid] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKUserGroupTable_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKUserGroupTable] ON [SHKUserGroupTable] 
(
	[userid] ASC,
	[groupid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKGroupUserPackLevelPart](
	[PARTICIPANTOID] [decimal](19, 0) NOT NULL,
	[USEROID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKGroupUserPackLevelPart_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKGroupUserPackLevelPart] ON [SHKGroupUserPackLevelPart] 
(
	[PARTICIPANTOID] ASC,
	[USEROID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKGroupUserProcLevelPart](

	[PARTICIPANTOID] [decimal](19, 0) NOT NULL,
	[USEROID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKGroupUserProcLevelPart_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKGroupUserProcLevelPart] ON [SHKGroupUserProcLevelPart] 
(
	[PARTICIPANTOID] ASC,
	[USEROID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKNewEventAuditDataBLOBs](
	[NewEventAuditDataWOB] [decimal](19, 0) NOT NULL,
	[VariableValue] [image] NULL,
	[OrdNo] [int] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKNewEventAuditDataBLOBs_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKNewEventAuditDataBLOBs] ON [SHKNewEventAuditDataBLOBs] 
(
	[NewEventAuditDataWOB] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKUserPackLevelPart](
	[PARTICIPANTOID] [decimal](19, 0) NOT NULL,
	[USEROID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKUserPackLevelPart_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKUserPackLevelPart] ON [SHKUserPackLevelPart] 
(
	[PARTICIPANTOID] ASC,
	[USEROID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKUserProcLevelParticipant](
	[PARTICIPANTOID] [decimal](19, 0) NOT NULL,
	[USEROID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKUserProcLevelParticipant_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKUserProcLevelParticipant] ON [SHKUserProcLevelParticipant] 
(
	[PARTICIPANTOID] ASC,
	[USEROID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKOldEventAuditDataBLOBs](
	[OldEventAuditDataWOB] [decimal](19, 0) NOT NULL,
	[VariableValue] [image] NULL,
	[OrdNo] [int] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKOldEventAuditDataBLOBs_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKOldEventAuditDataBLOBs] ON [SHKOldEventAuditDataBLOBs] 
(
	[OldEventAuditDataWOB] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_plugin_default](
	[appId] [nvarchar](255) NOT NULL,
	[appVersion] [numeric](19, 0) NOT NULL,
	[id] [nvarchar](255) NOT NULL,
	[pluginName] [nvarchar](255) NULL,
	[pluginDescription] [ntext] NULL,
	[pluginProperties] [ntext] NULL,
PRIMARY KEY CLUSTERED 
(
	[appId] ASC,
	[appVersion] ASC,
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_userview](
	[appId] [nvarchar](255) NOT NULL,
	[appVersion] [numeric](19, 0) NOT NULL,
	[id] [nvarchar](255) NOT NULL,
	[name] [nvarchar](255) NULL,
	[description] [ntext] NULL,
	[json] [ntext] NULL,
	[dateCreated] [datetime] NULL,
	[dateModified] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[appId] ASC,
	[appVersion] ASC,
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_datalist](
	[appId] [nvarchar](255) NOT NULL,
	[appVersion] [numeric](19, 0) NOT NULL,
	[id] [nvarchar](255) NOT NULL,
	[name] [nvarchar](255) NULL,
	[description] [ntext] NULL,
	[json] [ntext] NULL,
	[dateCreated] [datetime] NULL,
	[dateModified] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[appId] ASC,
	[appVersion] ASC,
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_env_variable](
	[appId] [nvarchar](255) NOT NULL,
	[appVersion] [numeric](19, 0) NOT NULL,
	[id] [nvarchar](255) NOT NULL,
	[value] [ntext] NULL,
	[remarks] [ntext] NULL,
PRIMARY KEY CLUSTERED 
(
	[appId] ASC,
	[appVersion] ASC,
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_form](
	[appId] [nvarchar](255) NOT NULL,
	[appVersion] [numeric](19, 0) NOT NULL,
	[formId] [nvarchar](255) NOT NULL,
	[name] [nvarchar](255) NULL,
	[dateCreated] [datetime] NULL,
	[dateModified] [datetime] NULL,
	[tableName] [nvarchar](255) NULL,
	[json] [ntext] NULL,
PRIMARY KEY CLUSTERED 
(
	[appId] ASC,
	[appVersion] ASC,
	[formId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_message](
	[appId] [nvarchar](255) NOT NULL,
	[appVersion] [numeric](19, 0) NOT NULL,
	[ouid] [nvarchar](255) NOT NULL,
	[messageKey] [nvarchar](255) NULL,
	[locale] [nvarchar](255) NULL,
	[message] [ntext] NULL,
PRIMARY KEY CLUSTERED 
(
	[appId] ASC,
	[appVersion] ASC,
	[ouid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_package](
	[packageId] [nvarchar](255) NOT NULL,
	[packageVersion] [numeric](19, 0) NOT NULL,
	[name] [nvarchar](255) NULL,
	[dateCreated] [datetime] NULL,
	[dateModified] [datetime] NULL,
	[appId] [nvarchar](255) NULL,
	[appVersion] [numeric](19, 0) NULL,
PRIMARY KEY CLUSTERED 
(
	[packageId] ASC,
	[packageVersion] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKPackLevelXPDLAppToolAgntApp](
	[XPDL_APPOID] [decimal](19, 0) NOT NULL,
	[TOOLAGENTOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKPackLevelXPDLAppToolAgntApp_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKPackLevelXPDLAppToolAgntApp] ON [SHKPackLevelXPDLAppToolAgntApp] 
(
	[XPDL_APPOID] ASC,
	[TOOLAGENTOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKPackLevelXPDLAppTAAppUser](
	[XPDL_APPOID] [decimal](19, 0) NOT NULL,
	[TOOLAGENTOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKPackLevelXPDLAppTAAppUser_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKPackLevelXPDLAppTAAppUser] ON [SHKPackLevelXPDLAppTAAppUser] 
(
	[XPDL_APPOID] ASC,
	[TOOLAGENTOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKPackLevelXPDLAppTAAppDetail](
	[XPDL_APPOID] [decimal](19, 0) NOT NULL,
	[TOOLAGENTOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKPackLevelXPDLAppTAAppDetail_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKPackLevelXPDLAppTAAppDetail] ON [SHKPackLevelXPDLAppTAAppDetail] 
(
	[XPDL_APPOID] ASC,
	[TOOLAGENTOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKPackLevelXPDLAppTAAppDetUsr](
	[XPDL_APPOID] [decimal](19, 0) NOT NULL,
	[TOOLAGENTOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKPackLevelXPDLAppTAAppDetUsr_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKPackLevelXPDLAppTAAppDetUsr] ON [SHKPackLevelXPDLAppTAAppDetUsr] 
(
	[XPDL_APPOID] ASC,
	[TOOLAGENTOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_package_activity_form](
	[processDefId] [nvarchar](255) NOT NULL,
	[activityDefId] [nvarchar](255) NOT NULL,
	[packageId] [nvarchar](255) NOT NULL,
	[packageVersion] [numeric](19, 0) NOT NULL,
	[ouid] [nvarchar](255) NULL,
	[type] [nvarchar](255) NULL,
	[formId] [nvarchar](255) NULL,
	[formUrl] [nvarchar](255) NULL,
	[formIFrameStyle] [nvarchar](255) NULL,
	[autoContinue] [tinyint] NULL,
PRIMARY KEY CLUSTERED 
(
	[processDefId] ASC,
	[activityDefId] ASC,
	[packageId] ASC,
	[packageVersion] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_package_activity_plugin](
	[processDefId] [nvarchar](255) NOT NULL,
	[activityDefId] [nvarchar](255) NOT NULL,
	[packageId] [nvarchar](255) NOT NULL,
	[packageVersion] [numeric](19, 0) NOT NULL,
	[ouid] [nvarchar](255) NULL,
	[pluginName] [nvarchar](255) NULL,
	[pluginProperties] [ntext] NULL,
PRIMARY KEY CLUSTERED 
(
	[processDefId] ASC,
	[activityDefId] ASC,
	[packageId] ASC,
	[packageVersion] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_package_participant](
	[processDefId] [nvarchar](255) NOT NULL,
	[participantId] [nvarchar](255) NOT NULL,
	[packageId] [nvarchar](255) NOT NULL,
	[packageVersion] [numeric](19, 0) NOT NULL,
	[ouid] [nvarchar](255) NULL,
	[type] [nvarchar](255) NULL,
	[value] [ntext] NULL,
	[pluginProperties] [ntext] NULL,
PRIMARY KEY CLUSTERED 
(
	[processDefId] ASC,
	[participantId] ASC,
	[packageId] ASC,
	[packageVersion] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcLevelXPDLAppToolAgntApp](
	[XPDL_APPOID] [decimal](19, 0) NOT NULL,
	[TOOLAGENTOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcLevelXPDLAppToolAgntApp_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcLevelXPDLAppToolAgntApp] ON [SHKProcLevelXPDLAppToolAgntApp] 
(
	[XPDL_APPOID] ASC,
	[TOOLAGENTOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcLevelXPDLAppTAAppUser](
	[XPDL_APPOID] [decimal](19, 0) NOT NULL,
	[TOOLAGENTOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcLevelXPDLAppTAAppUser_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcLevelXPDLAppTAAppUser] ON [SHKProcLevelXPDLAppTAAppUser] 
(
	[XPDL_APPOID] ASC,
	[TOOLAGENTOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcLevelXPDLAppTAAppDetail](
	[XPDL_APPOID] [decimal](19, 0) NOT NULL,
	[TOOLAGENTOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcLevelXPDLAppTAAppDetail_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcLevelXPDLAppTAAppDetail] ON [SHKProcLevelXPDLAppTAAppDetail] 
(
	[XPDL_APPOID] ASC,
	[TOOLAGENTOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcLevelXPDLAppTAAppDetUsr](
	[XPDL_APPOID] [decimal](19, 0) NOT NULL,
	[TOOLAGENTOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcLevelXPDLAppTAAppDetUsr_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcLevelXPDLAppTAAppDetUsr] ON [SHKProcLevelXPDLAppTAAppDetUsr] 
(
	[XPDL_APPOID] ASC,
	[TOOLAGENTOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcessDataBLOBs](
	[ProcessDataWOB] [decimal](19, 0) NOT NULL,
	[VariableValue] [image] NULL,
	[OrdNo] [int] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcessDataBLOBs_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcessDataBLOBs] ON [SHKProcessDataBLOBs] 
(
	[ProcessDataWOB] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcesses](
	[SyncVersion] [bigint] NOT NULL,
	[Id] [nvarchar](100) NOT NULL,
	[ProcessDefinition] [decimal](19, 0) NOT NULL,
	[PDefName] [nvarchar](200) NOT NULL,
	[ActivityRequesterId] [nvarchar](100) NULL,
	[ActivityRequesterProcessId] [nvarchar](100) NULL,
	[ResourceRequesterId] [nvarchar](100) NOT NULL,
	[ExternalRequesterClassName] [nvarchar](254) NULL,
	[State] [decimal](19, 0) NOT NULL,
	[Priority] [int] NULL,
	[Name] [nvarchar](254) NULL,
	[Created] [bigint] NOT NULL,
	[CreatedTZO] [bigint] NOT NULL,
	[Started] [bigint] NULL,
	[StartedTZO] [bigint] NULL,
	[LastStateTime] [bigint] NOT NULL,
	[LastStateTimeTZO] [bigint] NOT NULL,
	[LimitTime] [bigint] NOT NULL,
	[LimitTimeTZO] [bigint] NOT NULL,
	[Description] [nvarchar](254) NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcesses_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcesses] ON [SHKProcesses] 
(
	[Id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I2_SHKProcesses] ON [SHKProcesses] 
(
	[ProcessDefinition] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I3_SHKProcesses] ON [SHKProcesses] 
(
	[State] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I4_SHKProcesses] ON [SHKProcesses] 
(
	[ActivityRequesterId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I5_SHKProcesses] ON [SHKProcesses] 
(
	[ResourceRequesterId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_report_activity_instance](
	[instanceId] [nvarchar](255) NOT NULL,
	[performer] [nvarchar](255) NULL,
	[state] [nvarchar](255) NULL,
	[status] [nvarchar](255) NULL,
	[nameOfAcceptedUser] [nvarchar](255) NULL,
	[assignmentUsers] [ntext] NULL,
	[due] [datetime] NULL,
	[createdTime] [datetime] NULL,
	[startedTime] [datetime] NULL,
	[finishTime] [datetime] NULL,
	[delay] [numeric](19, 0) NULL,
	[timeConsumingFromCreatedTime] [numeric](19, 0) NULL,
	[timeConsumingFromStartedTime] [numeric](19, 0) NULL,
	[activityUid] [nvarchar](255) NULL,
	[processInstanceId] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[instanceId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcessDataWOB](
	[Process] [decimal](19, 0) NOT NULL,
	[VariableDefinitionId] [nvarchar](100) NOT NULL,
	[VariableType] [int] NOT NULL,
	[VariableValueXML] [xml] NULL,
	[VariableValueVCHAR] [nvarchar](4000) NULL,
	[VariableValueDBL] [real] NULL,
	[VariableValueLONG] [bigint] NULL,
	[VariableValueDATE] [datetime] NULL,
	[VariableValueBOOL] [tinyint] NULL,
	[OrdNo] [int] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcessDataWOB_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcessDataWOB] ON [SHKProcessDataWOB] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKProcessDataWOB] ON [SHKProcessDataWOB] 
(
	[Process] ASC,
	[VariableDefinitionId] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcessData](
	[Process] [decimal](19, 0) NOT NULL,
	[VariableDefinitionId] [nvarchar](100) NOT NULL,
	[VariableType] [int] NOT NULL,
	[VariableValue] [image] NULL,
	[VariableValueXML] [xml] NULL,
	[VariableValueVCHAR] [nvarchar](4000) NULL,
	[VariableValueDBL] [real] NULL,
	[VariableValueLONG] [bigint] NULL,
	[VariableValueDATE] [datetime] NULL,
	[VariableValueBOOL] [tinyint] NULL,
	[OrdNo] [int] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcessData_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcessData] ON [SHKProcessData] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKProcessData] ON [SHKProcessData] 
(
	[Process] ASC,
	[VariableDefinitionId] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKDeadlines](
	[Process] [decimal](19, 0) NOT NULL,
	[Activity] [decimal](19, 0) NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[TimeLimit] [bigint] NOT NULL,
	[TimeLimitTZO] [bigint] NOT NULL,
	[ExceptionName] [nvarchar](100) NOT NULL,
	[IsSynchronous] [tinyint] NOT NULL,
	[IsExecuted] [tinyint] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKDeadlines_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKDeadlines] ON [SHKDeadlines] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I2_SHKDeadlines] ON [SHKDeadlines] 
(
	[Process] ASC,
	[TimeLimit] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I3_SHKDeadlines] ON [SHKDeadlines] 
(
	[Activity] ASC,
	[TimeLimit] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKAndJoinTable](
	[Process] [decimal](19, 0) NOT NULL,
	[BlockActivity] [decimal](19, 0) NULL,
	[ActivityDefinitionId] [nvarchar](90) NOT NULL,
	[Activity] [decimal](19, 0) NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKAndJoinTable_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKAndJoinTable] ON [SHKAndJoinTable] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I2_SHKAndJoinTable] ON [SHKAndJoinTable] 
(
	[Process] ASC,
	[BlockActivity] ASC,
	[ActivityDefinitionId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I3_SHKAndJoinTable] ON [SHKAndJoinTable] 
(
	[Activity] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_report_package](
	[uuid] [nvarchar](255) NOT NULL,
	[packageId] [nvarchar](255) NULL,
	[packageName] [nvarchar](255) NULL,
	[packageVersion] [nvarchar](255) NULL,
	[appUid] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[uuid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcessRequesters](
	[Id] [nvarchar](100) NOT NULL,
	[ActivityRequester] [decimal](19, 0) NULL,
	[ResourceRequester] [decimal](19, 0) NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcessRequesters_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcessRequesters] ON [SHKProcessRequesters] 
(
	[Id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I2_SHKProcessRequesters] ON [SHKProcessRequesters] 
(
	[ActivityRequester] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I3_SHKProcessRequesters] ON [SHKProcessRequesters] 
(
	[ResourceRequester] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKAssignmentsTable](
	[Activity] [decimal](19, 0) NOT NULL,
	[TheResource] [decimal](19, 0) NOT NULL,
	[ActivityId] [nvarchar](100) NOT NULL,
	[ActivityProcessId] [nvarchar](100) NOT NULL,
	[ActivityProcessDefName] [nvarchar](200) NOT NULL,
	[ResourceId] [nvarchar](100) NOT NULL,
	[IsAccepted] [tinyint] NOT NULL,
	[IsValid] [tinyint] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKAssignmentsTable_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKAssignmentsTable] ON [SHKAssignmentsTable] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKAssignmentsTable] ON [SHKAssignmentsTable] 
(
	[Activity] ASC,
	[TheResource] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I3_SHKAssignmentsTable] ON [SHKAssignmentsTable] 
(
	[TheResource] ASC,
	[IsValid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I4_SHKAssignmentsTable] ON [SHKAssignmentsTable] 
(
	[ActivityId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE NONCLUSTERED INDEX [I5_SHKAssignmentsTable] ON [SHKAssignmentsTable] 
(
	[ResourceId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_report_process](
	[uuid] [nvarchar](255) NOT NULL,
	[processDefId] [nvarchar](255) NULL,
	[processName] [nvarchar](255) NULL,
	[packageUid] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[uuid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_report_process_instance](
	[instanceId] [nvarchar](255) NOT NULL,
	[requester] [nvarchar](255) NULL,
	[state] [nvarchar](255) NULL,
	[due] [datetime] NULL,
	[startedTime] [datetime] NULL,
	[finishTime] [datetime] NULL,
	[delay] [numeric](19, 0) NULL,
	[timeConsumingFromStartedTime] [numeric](19, 0) NULL,
	[processUid] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[instanceId] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [app_report_activity](
	[uuid] [nvarchar](255) NOT NULL,
	[activityDefId] [nvarchar](255) NULL,
	[activityName] [nvarchar](255) NULL,
	[processUid] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[uuid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKToolAgentAppDetail](
	[APP_MODE] [decimal](10, 0) NOT NULL,
	[TOOLAGENT_APPOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKToolAgentAppDetail_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKToolAgentAppDetail] ON [SHKToolAgentAppDetail] 
(
	[APP_MODE] ASC,
	[TOOLAGENT_APPOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKToolAgentAppUser](
	[TOOLAGENT_APPOID] [decimal](19, 0) NOT NULL,
	[USEROID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKToolAgentAppUser_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKToolAgentAppUser] ON [SHKToolAgentAppUser] 
(
	[TOOLAGENT_APPOID] ASC,
	[USEROID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKToolAgentAppDetailUser](
	[TOOLAGENT_APPOID] [decimal](19, 0) NOT NULL,
	[USEROID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKToolAgentAppDetailUser_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKToolAgentAppDetailUser] ON [SHKToolAgentAppDetailUser] 
(
	[TOOLAGENT_APPOID] ASC,
	[USEROID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLApplicationProcess](
	[PROCESS_ID] [nvarchar](90) NOT NULL,
	[PACKAGEOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLApplicationProcess_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLApplicationProcess] ON [SHKXPDLApplicationProcess] 
(
	[PROCESS_ID] ASC,
	[PACKAGEOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKPackLevelXPDLApp](
	[APPLICATION_ID] [nvarchar](90) NOT NULL,
	[PACKAGEOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKPackLevelXPDLApp_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKPackLevelXPDLApp] ON [SHKPackLevelXPDLApp] 
(
	[APPLICATION_ID] ASC,
	[PACKAGEOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcLevelXPDLApp](
	[APPLICATION_ID] [nvarchar](90) NOT NULL,
	[PROCESSOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcLevelXPDLApp_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcLevelXPDLApp] ON [SHKProcLevelXPDLApp] 
(
	[APPLICATION_ID] ASC,
	[PROCESSOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLHistoryData](
	[XPDLContent] [image] NOT NULL,
	[XPDLClassContent] [image] NOT NULL,
	[XPDLHistory] [decimal](19, 0) NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLHistoryData_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLHistoryData] ON [SHKXPDLHistoryData] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLParticipantProcess](
	[PROCESS_ID] [nvarchar](90) NOT NULL,
	[PACKAGEOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLParticipantProcess_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLParticipantProcess] ON [SHKXPDLParticipantProcess] 
(
	[PROCESS_ID] ASC,
	[PACKAGEOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKPackLevelParticipant](
	[PARTICIPANT_ID] [nvarchar](90) NOT NULL,
	[PACKAGEOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKPackLevelParticipant_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKPackLevelParticipant] ON [SHKPackLevelParticipant] 
(
	[PARTICIPANT_ID] ASC,
	[PACKAGEOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKProcLevelParticipant](
	[PARTICIPANT_ID] [nvarchar](90) NOT NULL,
	[PROCESSOID] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKProcLevelParticipant_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKProcLevelParticipant] ON [SHKProcLevelParticipant] 
(
	[PARTICIPANT_ID] ASC,
	[PROCESSOID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLReferences](
	[ReferredXPDLId] [nvarchar](90) NOT NULL,
	[ReferringXPDL] [decimal](19, 0) NOT NULL,
	[ReferredXPDLNumber] [int] NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLReferences_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLReferences] ON [SHKXPDLReferences] 
(
	[ReferredXPDLId] ASC,
	[ReferringXPDL] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKXPDLData](
	[XPDLContent] [image] NOT NULL,
	[XPDLClassContent] [image] NOT NULL,
	[XPDL] [decimal](19, 0) NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKXPDLData_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKXPDLData] ON [SHKXPDLData] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKXPDLData] ON [SHKXPDLData] 
(
	[XPDL] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKActivityData](
	[Activity] [decimal](19, 0) NOT NULL,
	[VariableDefinitionId] [nvarchar](100) NOT NULL,
	[VariableType] [int] NOT NULL,
	[VariableValue] [image] NULL,
	[VariableValueXML] [xml] NULL,
	[VariableValueVCHAR] [nvarchar](4000) NULL,
	[VariableValueDBL] [real] NULL,
	[VariableValueLONG] [bigint] NULL,
	[VariableValueDATE] [datetime] NULL,
	[VariableValueBOOL] [tinyint] NULL,
	[IsResult] [tinyint] NOT NULL,
	[OrdNo] [int] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKActivityData_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKActivityData] ON [SHKActivityData] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKActivityData] ON [SHKActivityData] 
(
	[Activity] ASC,
	[VariableDefinitionId] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [SHKActivityDataWOB](
	[Activity] [decimal](19, 0) NOT NULL,
	[VariableDefinitionId] [nvarchar](100) NOT NULL,
	[VariableType] [int] NOT NULL,
	[VariableValueXML] [xml] NULL,
	[VariableValueVCHAR] [nvarchar](4000) NULL,
	[VariableValueDBL] [real] NULL,
	[VariableValueLONG] [bigint] NULL,
	[VariableValueDATE] [datetime] NULL,
	[VariableValueBOOL] [tinyint] NULL,
	[IsResult] [tinyint] NOT NULL,
	[OrdNo] [int] NOT NULL,
	[CNT] [decimal](19, 0) NOT NULL,
	[oid] [decimal](19, 0) NOT NULL,
	[version] [int] NOT NULL,
 CONSTRAINT [SHKActivityDataWOB_oid] PRIMARY KEY CLUSTERED 
(
	[oid] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

;

CREATE UNIQUE NONCLUSTERED INDEX [I1_SHKActivityDataWOB] ON [SHKActivityDataWOB] 
(
	[CNT] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;

CREATE UNIQUE NONCLUSTERED INDEX [I2_SHKActivityDataWOB] ON [SHKActivityDataWOB] 
(
	[Activity] ASC,
	[VariableDefinitionId] ASC,
	[OrdNo] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
;
ALTER TABLE [dir_organization]  WITH CHECK ADD  CONSTRAINT [FK55A15FA5961BD498] FOREIGN KEY([parentId])
REFERENCES [dir_organization] ([id])
;
ALTER TABLE [dir_organization] CHECK CONSTRAINT [FK55A15FA5961BD498]
;
ALTER TABLE [SHKActivityDataBLOBs]  WITH CHECK ADD  CONSTRAINT [SHKActivityDataBLOBs_ActivityDataWOB] FOREIGN KEY([ActivityDataWOB])
REFERENCES [SHKActivityDataWOB] ([oid])
;
ALTER TABLE [SHKActivityDataBLOBs] CHECK CONSTRAINT [SHKActivityDataBLOBs_ActivityDataWOB]
;
ALTER TABLE [SHKStateEventAudits]  WITH CHECK ADD  CONSTRAINT [SHKStateEventAudits_NewActivityState] FOREIGN KEY([NewActivityState])
REFERENCES [SHKActivityStateEventAudits] ([oid])
;
ALTER TABLE [SHKStateEventAudits] CHECK CONSTRAINT [SHKStateEventAudits_NewActivityState]
;
ALTER TABLE [SHKStateEventAudits]  WITH CHECK ADD  CONSTRAINT [SHKStateEventAudits_NewProcessState] FOREIGN KEY([NewProcessState])
REFERENCES [SHKProcessStateEventAudits] ([oid])
;
ALTER TABLE [SHKStateEventAudits] CHECK CONSTRAINT [SHKStateEventAudits_NewProcessState]
;
ALTER TABLE [SHKStateEventAudits]  WITH CHECK ADD  CONSTRAINT [SHKStateEventAudits_OldActivityState] FOREIGN KEY([OldActivityState])
REFERENCES [SHKActivityStateEventAudits] ([oid])
;
ALTER TABLE [SHKStateEventAudits] CHECK CONSTRAINT [SHKStateEventAudits_OldActivityState]
;
ALTER TABLE [SHKStateEventAudits]  WITH CHECK ADD  CONSTRAINT [SHKStateEventAudits_OldProcessState] FOREIGN KEY([OldProcessState])
REFERENCES [SHKProcessStateEventAudits] ([oid])
;
ALTER TABLE [SHKStateEventAudits] CHECK CONSTRAINT [SHKStateEventAudits_OldProcessState]
;
ALTER TABLE [SHKStateEventAudits]  WITH CHECK ADD  CONSTRAINT [SHKStateEventAudits_TheType] FOREIGN KEY([TheType])
REFERENCES [SHKEventTypes] ([oid])
;
ALTER TABLE [SHKStateEventAudits] CHECK CONSTRAINT [SHKStateEventAudits_TheType]
;
ALTER TABLE [SHKActivities]  WITH CHECK ADD  CONSTRAINT [SHKActivities_Process] FOREIGN KEY([Process])
REFERENCES [SHKProcesses] ([oid])
;
ALTER TABLE [SHKActivities] CHECK CONSTRAINT [SHKActivities_Process]
;
ALTER TABLE [SHKActivities]  WITH CHECK ADD  CONSTRAINT [SHKActivities_State] FOREIGN KEY([State])
REFERENCES [SHKActivityStates] ([oid])
;
ALTER TABLE [SHKActivities] CHECK CONSTRAINT [SHKActivities_State]
;
ALTER TABLE [SHKActivities]  WITH CHECK ADD  CONSTRAINT [SHKActivities_TheResource] FOREIGN KEY([TheResource])
REFERENCES [SHKResourcesTable] ([oid])
;
ALTER TABLE [SHKActivities] CHECK CONSTRAINT [SHKActivities_TheResource]
;
ALTER TABLE [dir_employment]  WITH CHECK ADD  CONSTRAINT [FKC6620ADE14CE02E9] FOREIGN KEY([gradeId])
REFERENCES [dir_grade] ([id])
;
ALTER TABLE [dir_employment] CHECK CONSTRAINT [FKC6620ADE14CE02E9]
;
ALTER TABLE [dir_employment]  WITH CHECK ADD  CONSTRAINT [FKC6620ADE18CEBAE1] FOREIGN KEY([organizationId])
REFERENCES [dir_organization] ([id])
ON DELETE CASCADE
;
ALTER TABLE [dir_employment] CHECK CONSTRAINT [FKC6620ADE18CEBAE1]
;
ALTER TABLE [dir_employment]  WITH CHECK ADD  CONSTRAINT [FKC6620ADE716AE35F] FOREIGN KEY([departmentId])
REFERENCES [dir_department] ([id])
;
ALTER TABLE [dir_employment] CHECK CONSTRAINT [FKC6620ADE716AE35F]
;
ALTER TABLE [dir_employment]  WITH CHECK ADD  CONSTRAINT [FKC6620ADECE539211] FOREIGN KEY([userId])
REFERENCES [dir_user] ([id])
;
ALTER TABLE [dir_employment] CHECK CONSTRAINT [FKC6620ADECE539211]
;
ALTER TABLE [dir_department]  WITH CHECK ADD  CONSTRAINT [FKEEE8AA4418CEBAE1] FOREIGN KEY([organizationId])
REFERENCES [dir_organization] ([id])
ON DELETE CASCADE
;
ALTER TABLE [dir_department] CHECK CONSTRAINT [FKEEE8AA4418CEBAE1]
;
ALTER TABLE [dir_department]  WITH CHECK ADD  CONSTRAINT [FKEEE8AA4480DB1449] FOREIGN KEY([hod])
REFERENCES [dir_employment] ([id])
;
ALTER TABLE [dir_department] CHECK CONSTRAINT [FKEEE8AA4480DB1449]
;
ALTER TABLE [dir_department]  WITH CHECK ADD  CONSTRAINT [FKEEE8AA44EF6BB2B7] FOREIGN KEY([parentId])
REFERENCES [dir_department] ([id])
;
ALTER TABLE [dir_department] CHECK CONSTRAINT [FKEEE8AA44EF6BB2B7]
;
ALTER TABLE [dir_employment_report_to]  WITH CHECK ADD  CONSTRAINT [FK536229452787E613] FOREIGN KEY([employmentId])
REFERENCES [dir_employment] ([id])
;
ALTER TABLE [dir_employment_report_to] CHECK CONSTRAINT [FK536229452787E613]
;
ALTER TABLE [dir_employment_report_to]  WITH CHECK ADD  CONSTRAINT [FK53622945F4068416] FOREIGN KEY([reportToId])
REFERENCES [dir_employment] ([id])
;
ALTER TABLE [dir_employment_report_to] CHECK CONSTRAINT [FK53622945F4068416]
;
ALTER TABLE [SHKOldEventAuditDataWOB]  WITH CHECK ADD  CONSTRAINT [SHKOldEventAuditDataWOB_DataEventAudit] FOREIGN KEY([DataEventAudit])
REFERENCES [SHKDataEventAudits] ([oid])
;
ALTER TABLE [SHKOldEventAuditDataWOB] CHECK CONSTRAINT [SHKOldEventAuditDataWOB_DataEventAudit]
;
ALTER TABLE [SHKNewEventAuditData]  WITH CHECK ADD  CONSTRAINT [SHKNewEventAuditData_DataEventAudit] FOREIGN KEY([DataEventAudit])
REFERENCES [SHKDataEventAudits] ([oid])
;
ALTER TABLE [SHKNewEventAuditData] CHECK CONSTRAINT [SHKNewEventAuditData_DataEventAudit]
;
ALTER TABLE [SHKOldEventAuditData]  WITH CHECK ADD  CONSTRAINT [SHKOldEventAuditData_DataEventAudit] FOREIGN KEY([DataEventAudit])
REFERENCES [SHKDataEventAudits] ([oid])
;
ALTER TABLE [SHKOldEventAuditData] CHECK CONSTRAINT [SHKOldEventAuditData_DataEventAudit]
;
ALTER TABLE [SHKNewEventAuditDataWOB]  WITH CHECK ADD  CONSTRAINT [SHKNewEventAuditDataWOB_DataEventAudit] FOREIGN KEY([DataEventAudit])
REFERENCES [SHKDataEventAudits] ([oid])
;
ALTER TABLE [SHKNewEventAuditDataWOB] CHECK CONSTRAINT [SHKNewEventAuditDataWOB_DataEventAudit]
;
ALTER TABLE [dir_user_group]  WITH CHECK ADD  CONSTRAINT [FK2F0367FD159B6639] FOREIGN KEY([groupId])
REFERENCES [dir_group] ([id])
;
ALTER TABLE [dir_user_group] CHECK CONSTRAINT [FK2F0367FD159B6639]
;
ALTER TABLE [dir_user_group]  WITH CHECK ADD  CONSTRAINT [FK2F0367FDCE539211] FOREIGN KEY([userId])
REFERENCES [dir_user] ([id])
;
ALTER TABLE [dir_user_group] CHECK CONSTRAINT [FK2F0367FDCE539211]
;
ALTER TABLE [dir_grade]  WITH CHECK ADD  CONSTRAINT [FKBC9A49A518CEBAE1] FOREIGN KEY([organizationId])
REFERENCES [dir_organization] ([id])
ON DELETE CASCADE
;
ALTER TABLE [dir_grade] CHECK CONSTRAINT [FKBC9A49A518CEBAE1]
;
ALTER TABLE [dir_group]  WITH CHECK ADD  CONSTRAINT [FKBC9A804D18CEBAE1] FOREIGN KEY([organizationId])
REFERENCES [dir_organization] ([id])
;
ALTER TABLE [dir_group] CHECK CONSTRAINT [FKBC9A804D18CEBAE1]
;
ALTER TABLE [SHKAssignmentEventAudits]  WITH CHECK ADD  CONSTRAINT [SHKAssignmentEventAudits_TheType] FOREIGN KEY([TheType])
REFERENCES [SHKEventTypes] ([oid])
;
ALTER TABLE [SHKAssignmentEventAudits] CHECK CONSTRAINT [SHKAssignmentEventAudits_TheType]
;
ALTER TABLE [SHKDataEventAudits]  WITH CHECK ADD  CONSTRAINT [SHKDataEventAudits_TheType] FOREIGN KEY([TheType])
REFERENCES [SHKEventTypes] ([oid])
;
ALTER TABLE [SHKDataEventAudits] CHECK CONSTRAINT [SHKDataEventAudits_TheType]
;
ALTER TABLE [SHKCreateProcessEventAudits]  WITH CHECK ADD  CONSTRAINT [SHKCreateProcessEventAudits_TheType] FOREIGN KEY([TheType])
REFERENCES [SHKEventTypes] ([oid])
;
ALTER TABLE [SHKCreateProcessEventAudits] CHECK CONSTRAINT [SHKCreateProcessEventAudits_TheType]
;
ALTER TABLE [dir_user_role]  WITH CHECK ADD  CONSTRAINT [FK5C5FE738C8FE3CA7] FOREIGN KEY([roleId])
REFERENCES [dir_role] ([id])
;
ALTER TABLE [dir_user_role] CHECK CONSTRAINT [FK5C5FE738C8FE3CA7]
;
ALTER TABLE [dir_user_role]  WITH CHECK ADD  CONSTRAINT [FK5C5FE738CE539211] FOREIGN KEY([userId])
REFERENCES [dir_user] ([id])
;
ALTER TABLE [dir_user_role] CHECK CONSTRAINT [FK5C5FE738CE539211]
;
ALTER TABLE [SHKGroupGroupTable]  WITH CHECK ADD  CONSTRAINT [SHKGroupGroupTable_groupid] FOREIGN KEY([groupid])
REFERENCES [SHKGroupTable] ([oid])
;
ALTER TABLE [SHKGroupGroupTable] CHECK CONSTRAINT [SHKGroupGroupTable_groupid]
;
ALTER TABLE [SHKGroupGroupTable]  WITH CHECK ADD  CONSTRAINT [SHKGroupGroupTable_sub_gid] FOREIGN KEY([sub_gid])
REFERENCES [SHKGroupTable] ([oid])
;
ALTER TABLE [SHKGroupGroupTable] CHECK CONSTRAINT [SHKGroupGroupTable_sub_gid]
;
ALTER TABLE [SHKUserGroupTable]  WITH CHECK ADD  CONSTRAINT [SHKUserGroupTable_groupid] FOREIGN KEY([groupid])
REFERENCES [SHKGroupTable] ([oid])
;
ALTER TABLE [SHKUserGroupTable] CHECK CONSTRAINT [SHKUserGroupTable_groupid]
;
ALTER TABLE [SHKUserGroupTable]  WITH CHECK ADD  CONSTRAINT [SHKUserGroupTable_userid] FOREIGN KEY([userid])
REFERENCES [SHKUserTable] ([oid])
;
ALTER TABLE [SHKUserGroupTable] CHECK CONSTRAINT [SHKUserGroupTable_userid]
;
ALTER TABLE [SHKGroupUserPackLevelPart]  WITH CHECK ADD  CONSTRAINT [SHKGroupUserPackLevelPart_PARTICIPANTOID] FOREIGN KEY([PARTICIPANTOID])
REFERENCES [SHKPackLevelParticipant] ([oid])
;
ALTER TABLE [SHKGroupUserPackLevelPart] CHECK CONSTRAINT [SHKGroupUserPackLevelPart_PARTICIPANTOID]
;
ALTER TABLE [SHKGroupUserPackLevelPart]  WITH CHECK ADD  CONSTRAINT [SHKGroupUserPackLevelPart_USEROID] FOREIGN KEY([USEROID])
REFERENCES [SHKGroupUser] ([oid])
;
ALTER TABLE [SHKGroupUserPackLevelPart] CHECK CONSTRAINT [SHKGroupUserPackLevelPart_USEROID]
;
ALTER TABLE [SHKGroupUserProcLevelPart]  WITH CHECK ADD  CONSTRAINT [SHKGroupUserProcLevelPart_PARTICIPANTOID] FOREIGN KEY([PARTICIPANTOID])
REFERENCES [SHKProcLevelParticipant] ([oid])
;
ALTER TABLE [SHKGroupUserProcLevelPart] CHECK CONSTRAINT [SHKGroupUserProcLevelPart_PARTICIPANTOID]
;
ALTER TABLE [SHKGroupUserProcLevelPart]  WITH CHECK ADD  CONSTRAINT [SHKGroupUserProcLevelPart_USEROID] FOREIGN KEY([USEROID])
REFERENCES [SHKGroupUser] ([oid])
;
ALTER TABLE [SHKGroupUserProcLevelPart] CHECK CONSTRAINT [SHKGroupUserProcLevelPart_USEROID]
;
ALTER TABLE [SHKNewEventAuditDataBLOBs]  WITH CHECK ADD  CONSTRAINT [SHKNewEventAuditDataBLOBs_NewEventAuditDataWOB] FOREIGN KEY([NewEventAuditDataWOB])
REFERENCES [SHKNewEventAuditDataWOB] ([oid])
;
ALTER TABLE [SHKNewEventAuditDataBLOBs] CHECK CONSTRAINT [SHKNewEventAuditDataBLOBs_NewEventAuditDataWOB]
;
ALTER TABLE [SHKUserPackLevelPart]  WITH CHECK ADD  CONSTRAINT [SHKUserPackLevelPart_PARTICIPANTOID] FOREIGN KEY([PARTICIPANTOID])
REFERENCES [SHKPackLevelParticipant] ([oid])
;
ALTER TABLE [SHKUserPackLevelPart] CHECK CONSTRAINT [SHKUserPackLevelPart_PARTICIPANTOID]
;
ALTER TABLE [SHKUserPackLevelPart]  WITH CHECK ADD  CONSTRAINT [SHKUserPackLevelPart_USEROID] FOREIGN KEY([USEROID])
REFERENCES [SHKNormalUser] ([oid])
;
ALTER TABLE [SHKUserPackLevelPart] CHECK CONSTRAINT [SHKUserPackLevelPart_USEROID]
;
ALTER TABLE [SHKUserProcLevelParticipant]  WITH CHECK ADD  CONSTRAINT [SHKUserProcLevelParticipant_PARTICIPANTOID] FOREIGN KEY([PARTICIPANTOID])
REFERENCES [SHKProcLevelParticipant] ([oid])
;
ALTER TABLE [SHKUserProcLevelParticipant] CHECK CONSTRAINT [SHKUserProcLevelParticipant_PARTICIPANTOID]
;
ALTER TABLE [SHKUserProcLevelParticipant]  WITH CHECK ADD  CONSTRAINT [SHKUserProcLevelParticipant_USEROID] FOREIGN KEY([USEROID])
REFERENCES [SHKNormalUser] ([oid])
;
ALTER TABLE [SHKUserProcLevelParticipant] CHECK CONSTRAINT [SHKUserProcLevelParticipant_USEROID]
;
ALTER TABLE [SHKOldEventAuditDataBLOBs]  WITH CHECK ADD  CONSTRAINT [SHKOldEventAuditDataBLOBs_OldEventAuditDataWOB] FOREIGN KEY([OldEventAuditDataWOB])
REFERENCES [SHKOldEventAuditDataWOB] ([oid])
;
ALTER TABLE [SHKOldEventAuditDataBLOBs] CHECK CONSTRAINT [SHKOldEventAuditDataBLOBs_OldEventAuditDataWOB]
;
ALTER TABLE [app_plugin_default]  WITH CHECK ADD  CONSTRAINT [FK7A835713462EF4C7] FOREIGN KEY([appId], [appVersion])
REFERENCES [app_app] ([appId], [appVersion])
;
ALTER TABLE [app_plugin_default] CHECK CONSTRAINT [FK7A835713462EF4C7]
;
ALTER TABLE [app_userview]  WITH CHECK ADD  CONSTRAINT [FKE411D54E462EF4C7] FOREIGN KEY([appId], [appVersion])
REFERENCES [app_app] ([appId], [appVersion])
;
ALTER TABLE [app_userview] CHECK CONSTRAINT [FKE411D54E462EF4C7]
;
ALTER TABLE [app_datalist]  WITH CHECK ADD  CONSTRAINT [FK5E9247A6462EF4C7] FOREIGN KEY([appId], [appVersion])
REFERENCES [app_app] ([appId], [appVersion])
;
ALTER TABLE [app_datalist] CHECK CONSTRAINT [FK5E9247A6462EF4C7]
;
ALTER TABLE [app_env_variable]  WITH CHECK ADD  CONSTRAINT [FK740A62EC462EF4C7] FOREIGN KEY([appId], [appVersion])
REFERENCES [app_app] ([appId], [appVersion])
;
ALTER TABLE [app_env_variable] CHECK CONSTRAINT [FK740A62EC462EF4C7]
;
ALTER TABLE [app_form]  WITH CHECK ADD  CONSTRAINT [FK45957822462EF4C7] FOREIGN KEY([appId], [appVersion])
REFERENCES [app_app] ([appId], [appVersion])
;
ALTER TABLE [app_form] CHECK CONSTRAINT [FK45957822462EF4C7]
;
ALTER TABLE [app_message]  WITH CHECK ADD  CONSTRAINT [FKEE346FE9462EF4C7] FOREIGN KEY([appId], [appVersion])
REFERENCES [app_app] ([appId], [appVersion])
;
ALTER TABLE [app_message] CHECK CONSTRAINT [FKEE346FE9462EF4C7]
;
ALTER TABLE [app_package]  WITH CHECK ADD  CONSTRAINT [FK852EA428462EF4C7] FOREIGN KEY([appId], [appVersion])
REFERENCES [app_app] ([appId], [appVersion])
;
ALTER TABLE [app_package] CHECK CONSTRAINT [FK852EA428462EF4C7]
;
ALTER TABLE [SHKPackLevelXPDLAppToolAgntApp]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLAppToolAgntApp_TOOLAGENTOID] FOREIGN KEY([TOOLAGENTOID])
REFERENCES [SHKToolAgentApp] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLAppToolAgntApp] CHECK CONSTRAINT [SHKPackLevelXPDLAppToolAgntApp_TOOLAGENTOID]
;
ALTER TABLE [SHKPackLevelXPDLAppToolAgntApp]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLAppToolAgntApp_XPDL_APPOID] FOREIGN KEY([XPDL_APPOID])
REFERENCES [SHKPackLevelXPDLApp] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLAppToolAgntApp] CHECK CONSTRAINT [SHKPackLevelXPDLAppToolAgntApp_XPDL_APPOID]
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppUser]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLAppTAAppUser_TOOLAGENTOID] FOREIGN KEY([TOOLAGENTOID])
REFERENCES [SHKToolAgentAppUser] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppUser] CHECK CONSTRAINT [SHKPackLevelXPDLAppTAAppUser_TOOLAGENTOID]
;

ALTER TABLE [SHKPackLevelXPDLAppTAAppUser]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLAppTAAppUser_XPDL_APPOID] FOREIGN KEY([XPDL_APPOID])
REFERENCES [SHKPackLevelXPDLApp] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppUser] CHECK CONSTRAINT [SHKPackLevelXPDLAppTAAppUser_XPDL_APPOID]
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppDetail]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLAppTAAppDetail_TOOLAGENTOID] FOREIGN KEY([TOOLAGENTOID])
REFERENCES [SHKToolAgentAppDetail] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppDetail] CHECK CONSTRAINT [SHKPackLevelXPDLAppTAAppDetail_TOOLAGENTOID]
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppDetail]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLAppTAAppDetail_XPDL_APPOID] FOREIGN KEY([XPDL_APPOID])
REFERENCES [SHKPackLevelXPDLApp] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppDetail] CHECK CONSTRAINT [SHKPackLevelXPDLAppTAAppDetail_XPDL_APPOID]
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppDetUsr]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLAppTAAppDetUsr_TOOLAGENTOID] FOREIGN KEY([TOOLAGENTOID])
REFERENCES [SHKToolAgentAppDetailUser] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppDetUsr] CHECK CONSTRAINT [SHKPackLevelXPDLAppTAAppDetUsr_TOOLAGENTOID]
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppDetUsr]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLAppTAAppDetUsr_XPDL_APPOID] FOREIGN KEY([XPDL_APPOID])
REFERENCES [SHKPackLevelXPDLApp] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLAppTAAppDetUsr] CHECK CONSTRAINT [SHKPackLevelXPDLAppTAAppDetUsr_XPDL_APPOID]
;
ALTER TABLE [app_package_activity_form]  WITH CHECK ADD  CONSTRAINT [FKA8D741D5F255BCC] FOREIGN KEY([packageId], [packageVersion])
REFERENCES [app_package] ([packageId], [packageVersion])
;
ALTER TABLE [app_package_activity_form] CHECK CONSTRAINT [FKA8D741D5F255BCC]
;
ALTER TABLE [app_package_activity_plugin]  WITH CHECK ADD  CONSTRAINT [FKADE8644C5F255BCC] FOREIGN KEY([packageId], [packageVersion])
REFERENCES [app_package] ([packageId], [packageVersion])
;
ALTER TABLE [app_package_activity_plugin] CHECK CONSTRAINT [FKADE8644C5F255BCC]
;
ALTER TABLE [app_package_participant]  WITH CHECK ADD  CONSTRAINT [FK6D7BF59C5F255BCC] FOREIGN KEY([packageId], [packageVersion])
REFERENCES [app_package] ([packageId], [packageVersion])
;
ALTER TABLE [app_package_participant] CHECK CONSTRAINT [FK6D7BF59C5F255BCC]
;
ALTER TABLE [SHKProcLevelXPDLAppToolAgntApp]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLAppToolAgntApp_TOOLAGENTOID] FOREIGN KEY([TOOLAGENTOID])
REFERENCES [SHKToolAgentApp] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLAppToolAgntApp] CHECK CONSTRAINT [SHKProcLevelXPDLAppToolAgntApp_TOOLAGENTOID]
;
ALTER TABLE [SHKProcLevelXPDLAppToolAgntApp]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLAppToolAgntApp_XPDL_APPOID] FOREIGN KEY([XPDL_APPOID])
REFERENCES [SHKProcLevelXPDLApp] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLAppToolAgntApp] CHECK CONSTRAINT [SHKProcLevelXPDLAppToolAgntApp_XPDL_APPOID]
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppUser]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLAppTAAppUser_TOOLAGENTOID] FOREIGN KEY([TOOLAGENTOID])
REFERENCES [SHKToolAgentAppUser] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppUser] CHECK CONSTRAINT [SHKProcLevelXPDLAppTAAppUser_TOOLAGENTOID]
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppUser]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLAppTAAppUser_XPDL_APPOID] FOREIGN KEY([XPDL_APPOID])
REFERENCES [SHKProcLevelXPDLApp] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppUser] CHECK CONSTRAINT [SHKProcLevelXPDLAppTAAppUser_XPDL_APPOID]
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppDetail]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLAppTAAppDetail_TOOLAGENTOID] FOREIGN KEY([TOOLAGENTOID])
REFERENCES [SHKToolAgentAppDetail] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppDetail] CHECK CONSTRAINT [SHKProcLevelXPDLAppTAAppDetail_TOOLAGENTOID]
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppDetail]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLAppTAAppDetail_XPDL_APPOID] FOREIGN KEY([XPDL_APPOID])
REFERENCES [SHKProcLevelXPDLApp] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppDetail] CHECK CONSTRAINT [SHKProcLevelXPDLAppTAAppDetail_XPDL_APPOID]
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppDetUsr]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLAppTAAppDetUsr_TOOLAGENTOID] FOREIGN KEY([TOOLAGENTOID])
REFERENCES [SHKToolAgentAppDetailUser] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppDetUsr] CHECK CONSTRAINT [SHKProcLevelXPDLAppTAAppDetUsr_TOOLAGENTOID]
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppDetUsr]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLAppTAAppDetUsr_XPDL_APPOID] FOREIGN KEY([XPDL_APPOID])
REFERENCES [SHKProcLevelXPDLApp] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLAppTAAppDetUsr] CHECK CONSTRAINT [SHKProcLevelXPDLAppTAAppDetUsr_XPDL_APPOID]
;
ALTER TABLE [SHKProcessDataBLOBs]  WITH CHECK ADD  CONSTRAINT [SHKProcessDataBLOBs_ProcessDataWOB] FOREIGN KEY([ProcessDataWOB])
REFERENCES [SHKProcessDataWOB] ([oid])
;
ALTER TABLE [SHKProcessDataBLOBs] CHECK CONSTRAINT [SHKProcessDataBLOBs_ProcessDataWOB]
;
ALTER TABLE [SHKProcesses]  WITH CHECK ADD  CONSTRAINT [SHKProcesses_ProcessDefinition] FOREIGN KEY([ProcessDefinition])
REFERENCES [SHKProcessDefinitions] ([oid])
;
ALTER TABLE [SHKProcesses] CHECK CONSTRAINT [SHKProcesses_ProcessDefinition]
;
ALTER TABLE [SHKProcesses]  WITH CHECK ADD  CONSTRAINT [SHKProcesses_State] FOREIGN KEY([State])
REFERENCES [SHKProcessStates] ([oid])
;
ALTER TABLE [SHKProcesses] CHECK CONSTRAINT [SHKProcesses_State]
;
ALTER TABLE [app_report_activity_instance]  WITH CHECK ADD  CONSTRAINT [FK9C6ABDD8B06E2043] FOREIGN KEY([activityUid])
REFERENCES [app_report_activity] ([uuid])
;
ALTER TABLE [app_report_activity_instance] CHECK CONSTRAINT [FK9C6ABDD8B06E2043]
;
ALTER TABLE [app_report_activity_instance]  WITH CHECK ADD  CONSTRAINT [FK9C6ABDD8D4610A90] FOREIGN KEY([processInstanceId])
REFERENCES [app_report_process_instance] ([instanceId])
;
ALTER TABLE [app_report_activity_instance] CHECK CONSTRAINT [FK9C6ABDD8D4610A90]
;
ALTER TABLE [SHKProcessDataWOB]  WITH CHECK ADD  CONSTRAINT [SHKProcessDataWOB_Process] FOREIGN KEY([Process])
REFERENCES [SHKProcesses] ([oid])
;
ALTER TABLE [SHKProcessDataWOB] CHECK CONSTRAINT [SHKProcessDataWOB_Process]
;
ALTER TABLE [SHKProcessData]  WITH CHECK ADD  CONSTRAINT [SHKProcessData_Process] FOREIGN KEY([Process])
REFERENCES [SHKProcesses] ([oid])
;
ALTER TABLE [SHKProcessData] CHECK CONSTRAINT [SHKProcessData_Process]
;
ALTER TABLE [SHKDeadlines]  WITH CHECK ADD  CONSTRAINT [SHKDeadlines_Activity] FOREIGN KEY([Activity])
REFERENCES [SHKActivities] ([oid])
;
ALTER TABLE [SHKDeadlines] CHECK CONSTRAINT [SHKDeadlines_Activity]
;
ALTER TABLE [SHKDeadlines]  WITH CHECK ADD  CONSTRAINT [SHKDeadlines_Process] FOREIGN KEY([Process])
REFERENCES [SHKProcesses] ([oid])
;
ALTER TABLE [SHKDeadlines] CHECK CONSTRAINT [SHKDeadlines_Process]
;
ALTER TABLE [SHKAndJoinTable]  WITH CHECK ADD  CONSTRAINT [SHKAndJoinTable_Activity] FOREIGN KEY([Activity])
REFERENCES [SHKActivities] ([oid])
;
ALTER TABLE [SHKAndJoinTable] CHECK CONSTRAINT [SHKAndJoinTable_Activity]
;
ALTER TABLE [SHKAndJoinTable]  WITH CHECK ADD  CONSTRAINT [SHKAndJoinTable_BlockActivity] FOREIGN KEY([BlockActivity])
REFERENCES [SHKActivities] ([oid])
;
ALTER TABLE [SHKAndJoinTable] CHECK CONSTRAINT [SHKAndJoinTable_BlockActivity]
;
ALTER TABLE [SHKAndJoinTable]  WITH CHECK ADD  CONSTRAINT [SHKAndJoinTable_Process] FOREIGN KEY([Process])
REFERENCES [SHKProcesses] ([oid])
;
ALTER TABLE [SHKAndJoinTable] CHECK CONSTRAINT [SHKAndJoinTable_Process]
;
ALTER TABLE [app_report_package]  WITH CHECK ADD  CONSTRAINT [FKBD580A19E475ABC] FOREIGN KEY([appUid])
REFERENCES [app_report_app] ([uuid])
;
ALTER TABLE [app_report_package] CHECK CONSTRAINT [FKBD580A19E475ABC]
;
ALTER TABLE [SHKProcessRequesters]  WITH CHECK ADD  CONSTRAINT [SHKProcessRequesters_ActivityRequester] FOREIGN KEY([ActivityRequester])
REFERENCES [SHKActivities] ([oid])
;
ALTER TABLE [SHKProcessRequesters] CHECK CONSTRAINT [SHKProcessRequesters_ActivityRequester]
;
ALTER TABLE [SHKProcessRequesters]  WITH CHECK ADD  CONSTRAINT [SHKProcessRequesters_ResourceRequester] FOREIGN KEY([ResourceRequester])
REFERENCES [SHKResourcesTable] ([oid])
;
ALTER TABLE [SHKProcessRequesters] CHECK CONSTRAINT [SHKProcessRequesters_ResourceRequester]
;
ALTER TABLE [SHKAssignmentsTable]  WITH CHECK ADD  CONSTRAINT [SHKAssignmentsTable_Activity] FOREIGN KEY([Activity])
REFERENCES [SHKActivities] ([oid])
;
ALTER TABLE [SHKAssignmentsTable] CHECK CONSTRAINT [SHKAssignmentsTable_Activity]
;
ALTER TABLE [SHKAssignmentsTable]  WITH CHECK ADD  CONSTRAINT [SHKAssignmentsTable_TheResource] FOREIGN KEY([TheResource])
REFERENCES [SHKResourcesTable] ([oid])
;
ALTER TABLE [SHKAssignmentsTable] CHECK CONSTRAINT [SHKAssignmentsTable_TheResource]
;
ALTER TABLE [app_report_process]  WITH CHECK ADD  CONSTRAINT [FKDAFFF442D40695DD] FOREIGN KEY([packageUid])
REFERENCES [app_report_package] ([uuid])
;
ALTER TABLE [app_report_process] CHECK CONSTRAINT [FKDAFFF442D40695DD]
;
ALTER TABLE [app_report_process_instance]  WITH CHECK ADD  CONSTRAINT [FK351D7BF2918F93D] FOREIGN KEY([processUid])
REFERENCES [app_report_process] ([uuid])
;
ALTER TABLE [app_report_process_instance] CHECK CONSTRAINT [FK351D7BF2918F93D]
;
ALTER TABLE [app_report_activity]  WITH CHECK ADD  CONSTRAINT [FK5E33D79C918F93D] FOREIGN KEY([processUid])
REFERENCES [app_report_process] ([uuid])
;
ALTER TABLE [app_report_activity] CHECK CONSTRAINT [FK5E33D79C918F93D]
;
ALTER TABLE [SHKToolAgentAppDetail]  WITH CHECK ADD  CONSTRAINT [SHKToolAgentAppDetail_TOOLAGENT_APPOID] FOREIGN KEY([TOOLAGENT_APPOID])
REFERENCES [SHKToolAgentApp] ([oid])
;
ALTER TABLE [SHKToolAgentAppDetail] CHECK CONSTRAINT [SHKToolAgentAppDetail_TOOLAGENT_APPOID]
;
ALTER TABLE [SHKToolAgentAppUser]  WITH CHECK ADD  CONSTRAINT [SHKToolAgentAppUser_TOOLAGENT_APPOID] FOREIGN KEY([TOOLAGENT_APPOID])
REFERENCES [SHKToolAgentApp] ([oid])
;
ALTER TABLE [SHKToolAgentAppUser] CHECK CONSTRAINT [SHKToolAgentAppUser_TOOLAGENT_APPOID]
;
ALTER TABLE [SHKToolAgentAppUser]  WITH CHECK ADD  CONSTRAINT [SHKToolAgentAppUser_USEROID] FOREIGN KEY([USEROID])
REFERENCES [SHKToolAgentUser] ([oid])
;
ALTER TABLE [SHKToolAgentAppUser] CHECK CONSTRAINT [SHKToolAgentAppUser_USEROID]
;
ALTER TABLE [SHKToolAgentAppDetailUser]  WITH CHECK ADD  CONSTRAINT [SHKToolAgentAppDetailUser_TOOLAGENT_APPOID] FOREIGN KEY([TOOLAGENT_APPOID])
REFERENCES [SHKToolAgentAppDetail] ([oid])
;
ALTER TABLE [SHKToolAgentAppDetailUser] CHECK CONSTRAINT [SHKToolAgentAppDetailUser_TOOLAGENT_APPOID]
;
ALTER TABLE [SHKToolAgentAppDetailUser]  WITH CHECK ADD  CONSTRAINT [SHKToolAgentAppDetailUser_USEROID] FOREIGN KEY([USEROID])
REFERENCES [SHKToolAgentUser] ([oid])
;
ALTER TABLE [SHKToolAgentAppDetailUser] CHECK CONSTRAINT [SHKToolAgentAppDetailUser_USEROID]
;
ALTER TABLE [SHKXPDLApplicationProcess]  WITH CHECK ADD  CONSTRAINT [SHKXPDLApplicationProcess_PACKAGEOID] FOREIGN KEY([PACKAGEOID])
REFERENCES [SHKXPDLApplicationPackage] ([oid])
;
ALTER TABLE [SHKXPDLApplicationProcess] CHECK CONSTRAINT [SHKXPDLApplicationProcess_PACKAGEOID]
;
ALTER TABLE [SHKPackLevelXPDLApp]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelXPDLApp_PACKAGEOID] FOREIGN KEY([PACKAGEOID])
REFERENCES [SHKXPDLApplicationPackage] ([oid])
;
ALTER TABLE [SHKPackLevelXPDLApp] CHECK CONSTRAINT [SHKPackLevelXPDLApp_PACKAGEOID]
;
ALTER TABLE [SHKProcLevelXPDLApp]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelXPDLApp_PROCESSOID] FOREIGN KEY([PROCESSOID])
REFERENCES [SHKXPDLApplicationProcess] ([oid])
;
ALTER TABLE [SHKProcLevelXPDLApp] CHECK CONSTRAINT [SHKProcLevelXPDLApp_PROCESSOID]
;
ALTER TABLE [SHKXPDLHistoryData]  WITH CHECK ADD  CONSTRAINT [SHKXPDLHistoryData_XPDLHistory] FOREIGN KEY([XPDLHistory])
REFERENCES [SHKXPDLHistory] ([oid])
;
ALTER TABLE [SHKXPDLHistoryData] CHECK CONSTRAINT [SHKXPDLHistoryData_XPDLHistory]
;
ALTER TABLE [SHKXPDLParticipantProcess]  WITH CHECK ADD  CONSTRAINT [SHKXPDLParticipantProcess_PACKAGEOID] FOREIGN KEY([PACKAGEOID])
REFERENCES [SHKXPDLParticipantPackage] ([oid])
;
ALTER TABLE [SHKXPDLParticipantProcess] CHECK CONSTRAINT [SHKXPDLParticipantProcess_PACKAGEOID]
;
ALTER TABLE [SHKPackLevelParticipant]  WITH CHECK ADD  CONSTRAINT [SHKPackLevelParticipant_PACKAGEOID] FOREIGN KEY([PACKAGEOID])
REFERENCES [SHKXPDLParticipantPackage] ([oid])
;
ALTER TABLE [SHKPackLevelParticipant] CHECK CONSTRAINT [SHKPackLevelParticipant_PACKAGEOID]
;
ALTER TABLE [SHKProcLevelParticipant]  WITH CHECK ADD  CONSTRAINT [SHKProcLevelParticipant_PROCESSOID] FOREIGN KEY([PROCESSOID])
REFERENCES [SHKXPDLParticipantProcess] ([oid])
;
ALTER TABLE [SHKProcLevelParticipant] CHECK CONSTRAINT [SHKProcLevelParticipant_PROCESSOID]
;
ALTER TABLE [SHKXPDLReferences]  WITH CHECK ADD  CONSTRAINT [SHKXPDLReferences_ReferringXPDL] FOREIGN KEY([ReferringXPDL])
REFERENCES [SHKXPDLS] ([oid])
;
ALTER TABLE [SHKXPDLReferences] CHECK CONSTRAINT [SHKXPDLReferences_ReferringXPDL]
;
ALTER TABLE [SHKXPDLData]  WITH CHECK ADD  CONSTRAINT [SHKXPDLData_XPDL] FOREIGN KEY([XPDL])
REFERENCES [SHKXPDLS] ([oid])
;
ALTER TABLE [SHKXPDLData] CHECK CONSTRAINT [SHKXPDLData_XPDL]
;
ALTER TABLE [SHKActivityData]  WITH CHECK ADD  CONSTRAINT [SHKActivityData_Activity] FOREIGN KEY([Activity])
REFERENCES [SHKActivities] ([oid])
;
ALTER TABLE [SHKActivityData] CHECK CONSTRAINT [SHKActivityData_Activity]
;
ALTER TABLE [SHKActivityDataWOB]  WITH CHECK ADD  CONSTRAINT [SHKActivityDataWOB_Activity] FOREIGN KEY([Activity])
REFERENCES [SHKActivities] ([oid])
;
ALTER TABLE [SHKActivityDataWOB] CHECK CONSTRAINT [SHKActivityDataWOB_Activity]

-- INSERT DATA --

INSERT INTO dir_role (id,name,description) VALUES ('ROLE_ADMIN','Admin','Administrator');
INSERT INTO dir_role (id,name,description) VALUES ('ROLE_USER','User','Normal User');

INSERT INTO dir_user (id,username,password,firstName,lastName,email,active,timeZone) VALUES ('admin','admin','admin','Admin','admin','admin@email.domain',1,'0');

INSERT INTO dir_user_role (roleId,userId) VALUES ('ROLE_ADMIN','admin');

-- END INSERT DATA --

