--
-- PostgreSQL database dump
--

-- Dumped from database version 14.5 (Debian 14.5-1.pgdg110+1)
-- Dumped by pg_dump version 14.5 (Debian 14.5-1.pgdg110+1)

-- Started on 2022-11-24 03:57:58 UTC

-- SET statement_timeout = 0;
-- SET lock_timeout = 0;
-- SET idle_in_transaction_session_timeout = 0;
-- SET client_encoding = 'UTF8';
-- SET standard_conforming_strings = on;
-- SELECT pg_catalog.set_config('search_path', '', false);
-- SET check_function_bodies = false;
-- SET xmloption = content;
-- SET client_min_messages = warning;
-- SET row_security = off;

--
-- TOC entry 3 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- CREATE SCHEMA public;


--
-- TOC entry 4668 (class 0 OID 0)
-- Dependencies: 3
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

-- COMMENT ON SCHEMA public IS 'standard public schema';


-- SET default_table_access_method = heap;

--
-- TOC entry 209 (class 1259 OID 16385)
-- Name: app_app; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_app (
    appid character varying(510) NOT NULL,
    appversion bigint NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    published boolean,
    datecreated timestamp with time zone,
    datemodified timestamp with time zone,
    license text,
    description text,
    meta text
);


--
-- TOC entry 210 (class 1259 OID 16391)
-- Name: app_builder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_builder (
    appid character varying(255) NOT NULL,
    appversion bigint NOT NULL,
    id character varying(255) NOT NULL,
    name character varying(255),
    type character varying(255),
    datecreated timestamp without time zone,
    datemodified timestamp without time zone,
    json text,
    description text
);


--
-- TOC entry 211 (class 1259 OID 16396)
-- Name: app_datalist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_datalist (
    appid character varying(510) NOT NULL,
    appversion bigint NOT NULL,
    id character varying(510) NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    description text,
    json text,
    datecreated timestamp with time zone,
    datemodified timestamp with time zone
);


--
-- TOC entry 212 (class 1259 OID 16402)
-- Name: app_env_variable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_env_variable (
    appid character varying(510) NOT NULL,
    appversion bigint NOT NULL,
    id character varying(510) NOT NULL,
    value text,
    remarks text
);


--
-- TOC entry 213 (class 1259 OID 16407)
-- Name: app_fd; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_fd (
    id character varying(510) NOT NULL,
    datecreated timestamp with time zone,
    datemodified timestamp with time zone
);


--
-- TOC entry 214 (class 1259 OID 16412)
-- Name: app_fd_appcenter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_fd_appcenter (
    id character varying(255) NOT NULL,
    datecreated timestamp without time zone,
    datemodified timestamp without time zone,
    createdby character varying(255),
    createdbyname character varying(255),
    modifiedby character varying(255),
    modifiedbyname character varying(255)
);


--
-- TOC entry 215 (class 1259 OID 16417)
-- Name: app_form; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_form (
    appid character varying(510) NOT NULL,
    appversion bigint NOT NULL,
    formid character varying(510) NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    datecreated timestamp with time zone,
    datemodified timestamp with time zone,
    tablename character varying(510) DEFAULT NULL::character varying,
    json text,
    description text
);


--
-- TOC entry 216 (class 1259 OID 16424)
-- Name: app_form_data_audit_trail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_form_data_audit_trail (
    id character varying(255) NOT NULL,
    appid character varying(255),
    appversion character varying(255),
    formid character varying(255),
    tablename character varying(255),
    username character varying(255),
    action character varying(255),
    data text,
    datetime timestamp without time zone
);


--
-- TOC entry 217 (class 1259 OID 16429)
-- Name: app_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_message (
    appid character varying(510) NOT NULL,
    appversion bigint NOT NULL,
    ouid character varying(510) NOT NULL,
    messagekey character varying(510) DEFAULT NULL::character varying,
    locale character varying(510) DEFAULT NULL::character varying,
    message text
);


--
-- TOC entry 218 (class 1259 OID 16436)
-- Name: app_package; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_package (
    packageid character varying(510) NOT NULL,
    packageversion bigint NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    datecreated timestamp with time zone,
    datemodified timestamp with time zone,
    appid character varying(510) DEFAULT NULL::character varying,
    appversion bigint
);


--
-- TOC entry 219 (class 1259 OID 16443)
-- Name: app_package_activity_form; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_package_activity_form (
    processdefid character varying(510) NOT NULL,
    activitydefid character varying(510) NOT NULL,
    packageid character varying(510) NOT NULL,
    packageversion bigint NOT NULL,
    ouid character varying(510) DEFAULT NULL::character varying,
    type character varying(510) DEFAULT NULL::character varying,
    formid character varying(510) DEFAULT NULL::character varying,
    formurl character varying(510) DEFAULT NULL::character varying,
    formiframestyle character varying(510) DEFAULT NULL::character varying,
    autocontinue boolean,
    disablesaveasdraft boolean
);


--
-- TOC entry 220 (class 1259 OID 16453)
-- Name: app_package_activity_plugin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_package_activity_plugin (
    processdefid character varying(510) NOT NULL,
    activitydefid character varying(510) NOT NULL,
    packageid character varying(510) NOT NULL,
    packageversion bigint NOT NULL,
    ouid character varying(510) DEFAULT NULL::character varying,
    pluginname character varying(510) DEFAULT NULL::character varying,
    pluginproperties text
);


--
-- TOC entry 221 (class 1259 OID 16460)
-- Name: app_package_participant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_package_participant (
    processdefid character varying(510) NOT NULL,
    participantid character varying(510) NOT NULL,
    packageid character varying(510) NOT NULL,
    packageversion bigint NOT NULL,
    ouid character varying(510) DEFAULT NULL::character varying,
    type character varying(510) DEFAULT NULL::character varying,
    value text,
    pluginproperties text
);


--
-- TOC entry 222 (class 1259 OID 16467)
-- Name: app_plugin_default; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_plugin_default (
    appid character varying(510) NOT NULL,
    appversion bigint NOT NULL,
    id character varying(510) NOT NULL,
    pluginname character varying(510) DEFAULT NULL::character varying,
    plugindescription text,
    pluginproperties text
);


--
-- TOC entry 223 (class 1259 OID 16473)
-- Name: app_report_activity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_report_activity (
    uuid character varying(510) NOT NULL,
    activitydefid character varying(510) DEFAULT NULL::character varying,
    activityname character varying(510) DEFAULT NULL::character varying,
    processuid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 224 (class 1259 OID 16481)
-- Name: app_report_activity_instance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_report_activity_instance (
    instanceid character varying(510) NOT NULL,
    performer character varying(510) DEFAULT NULL::character varying,
    state character varying(510) DEFAULT NULL::character varying,
    status character varying(510) DEFAULT NULL::character varying,
    nameofaccepteduser character varying(510) DEFAULT NULL::character varying,
    assignmentusers text,
    due timestamp with time zone,
    createdtime timestamp with time zone,
    startedtime timestamp with time zone,
    finishtime timestamp with time zone,
    delay bigint,
    timeconsumingfromcreatedtime bigint,
    timeconsumingfromstartedtime bigint,
    activityuid character varying(510) DEFAULT NULL::character varying,
    processinstanceid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 225 (class 1259 OID 16492)
-- Name: app_report_app; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_report_app (
    uuid character varying(510) NOT NULL,
    appid character varying(510) DEFAULT NULL::character varying,
    appversion character varying(510) DEFAULT NULL::character varying,
    appname character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 226 (class 1259 OID 16500)
-- Name: app_report_package; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_report_package (
    uuid character varying(510) NOT NULL,
    packageid character varying(510) DEFAULT NULL::character varying,
    packagename character varying(510) DEFAULT NULL::character varying,
    packageversion character varying(510) DEFAULT NULL::character varying,
    appuid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 227 (class 1259 OID 16509)
-- Name: app_report_process; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_report_process (
    uuid character varying(510) NOT NULL,
    processdefid character varying(510) DEFAULT NULL::character varying,
    processname character varying(510) DEFAULT NULL::character varying,
    packageuid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 228 (class 1259 OID 16517)
-- Name: app_report_process_instance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_report_process_instance (
    instanceid character varying(510) NOT NULL,
    requester character varying(510) DEFAULT NULL::character varying,
    state character varying(510) DEFAULT NULL::character varying,
    due timestamp with time zone,
    startedtime timestamp with time zone,
    finishtime timestamp with time zone,
    delay bigint,
    timeconsumingfromstartedtime bigint,
    processuid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 229 (class 1259 OID 16525)
-- Name: app_resource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_resource (
    appid character varying(255) NOT NULL,
    appversion bigint NOT NULL,
    id character varying(255) NOT NULL,
    filesize bigint,
    permissionclass character varying(255),
    permissionproperties text
);


--
-- TOC entry 230 (class 1259 OID 16530)
-- Name: app_userview; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE app_userview (
    appid character varying(510) NOT NULL,
    appversion bigint NOT NULL,
    id character varying(510) NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    description text,
    json text,
    datecreated timestamp with time zone,
    datemodified timestamp with time zone
);


--
-- TOC entry 231 (class 1259 OID 16536)
-- Name: dir_department; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_department (
    id character varying(510) NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    description character varying(510) DEFAULT NULL::character varying,
    organizationid character varying(510) DEFAULT NULL::character varying,
    hod character varying(510) DEFAULT NULL::character varying,
    parentid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 232 (class 1259 OID 16546)
-- Name: dir_employment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_employment (
    id character varying(510) NOT NULL,
    userid character varying(510) DEFAULT NULL::character varying,
    startdate date,
    enddate date,
    employeecode character varying(510) DEFAULT NULL::character varying,
    role character varying(510) DEFAULT NULL::character varying,
    gradeid character varying(510) DEFAULT NULL::character varying,
    departmentid character varying(510) DEFAULT NULL::character varying,
    organizationid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 233 (class 1259 OID 16557)
-- Name: dir_employment_report_to; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_employment_report_to (
    employmentid character varying(510) NOT NULL,
    reporttoid character varying(510) NOT NULL,
    id character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 234 (class 1259 OID 16563)
-- Name: dir_grade; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_grade (
    id character varying(510) NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    description character varying(510) DEFAULT NULL::character varying,
    organizationid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 235 (class 1259 OID 16571)
-- Name: dir_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_group (
    id character varying(510) NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    description character varying(510) DEFAULT NULL::character varying,
    organizationid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 236 (class 1259 OID 16579)
-- Name: dir_organization; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_organization (
    id character varying(510) NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    description character varying(510) DEFAULT NULL::character varying,
    parentid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 237 (class 1259 OID 16587)
-- Name: dir_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_role (
    id character varying(510) NOT NULL,
    name character varying(510) DEFAULT NULL::character varying,
    description character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 238 (class 1259 OID 16594)
-- Name: dir_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_user (
    id character varying(510) NOT NULL,
    username character varying(510) DEFAULT NULL::character varying,
    password character varying(510) DEFAULT NULL::character varying,
    firstname character varying(510) DEFAULT NULL::character varying,
    lastname character varying(510) DEFAULT NULL::character varying,
    email character varying(510) DEFAULT NULL::character varying,
    active integer,
    timezone character varying(510) DEFAULT NULL::character varying,
    locale character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 239 (class 1259 OID 16606)
-- Name: dir_user_extra; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_user_extra (
    username character varying(510) NOT NULL,
    algorithm character varying(510) DEFAULT NULL::character varying,
    loginattempt integer,
    failedloginattempt integer,
    lastlogedindate timestamp with time zone,
    lockoutdate timestamp with time zone,
    lastpasswordchangedate timestamp with time zone,
    requiredpasswordchange boolean,
    nopasswordexpiration boolean
);


--
-- TOC entry 240 (class 1259 OID 16612)
-- Name: dir_user_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_user_group (
    groupid character varying(510) NOT NULL,
    userid character varying(510) NOT NULL
);


--
-- TOC entry 241 (class 1259 OID 16617)
-- Name: dir_user_meta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_user_meta (
    username character varying(255) NOT NULL,
    meta_key character varying(255) NOT NULL,
    meta_value text
);


--
-- TOC entry 242 (class 1259 OID 16622)
-- Name: dir_user_password_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_user_password_history (
    id character varying(510) NOT NULL,
    username character varying(510) DEFAULT NULL::character varying,
    salt character varying(510) DEFAULT NULL::character varying,
    password character varying(510) DEFAULT NULL::character varying,
    updateddate timestamp with time zone
);


--
-- TOC entry 243 (class 1259 OID 16630)
-- Name: dir_user_replacement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_user_replacement (
    id character varying(255) NOT NULL,
    username character varying(255),
    replacementuser character varying(255),
    appid text,
    processids text,
    startdate timestamp without time zone,
    enddate timestamp without time zone
);


--
-- TOC entry 244 (class 1259 OID 16635)
-- Name: dir_user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE dir_user_role (
    roleid character varying(510) NOT NULL,
    userid character varying(510) NOT NULL
);


--
-- TOC entry 245 (class 1259 OID 16640)
-- Name: objectid; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE objectid (
    nextoid numeric(19,0) NOT NULL
);


--
-- TOC entry 246 (class 1259 OID 16643)
-- Name: shkactivities; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkactivities (
    id character varying(200) NOT NULL,
    activitysetdefinitionid character varying(180) DEFAULT NULL::character varying,
    activitydefinitionid character varying(180) NOT NULL,
    process numeric(19,0) NOT NULL,
    theresource numeric(19,0) DEFAULT NULL::numeric,
    pdefname character varying(400) NOT NULL,
    processid character varying(400) NOT NULL,
    resourceid character varying(200) DEFAULT NULL::character varying,
    state numeric(19,0) NOT NULL,
    blockactivityid character varying(200) DEFAULT NULL::character varying,
    performer character varying(200) DEFAULT NULL::character varying,
    isperformerasynchronous boolean,
    priority integer,
    name character varying(508) DEFAULT NULL::character varying,
    activated bigint NOT NULL,
    activatedtzo bigint NOT NULL,
    accepted bigint,
    acceptedtzo bigint,
    laststatetime bigint NOT NULL,
    laststatetimetzo bigint NOT NULL,
    limittime bigint NOT NULL,
    limittimetzo bigint NOT NULL,
    description character varying(508) DEFAULT NULL::character varying,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 247 (class 1259 OID 16655)
-- Name: shkactivitydata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkactivitydata (
    activity numeric(19,0) NOT NULL,
    variabledefinitionid character varying(200) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(8000) DEFAULT NULL::character varying,
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp with time zone,
    variablevaluebool boolean,
    isresult boolean NOT NULL,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 248 (class 1259 OID 16661)
-- Name: shkactivitydatablobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkactivitydatablobs (
    activitydatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 249 (class 1259 OID 16666)
-- Name: shkactivitydatawob; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkactivitydatawob (
    activity numeric(19,0) NOT NULL,
    variabledefinitionid character varying(200) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(8000) DEFAULT NULL::character varying,
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp with time zone,
    variablevaluebool boolean,
    isresult boolean NOT NULL,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 250 (class 1259 OID 16672)
-- Name: shkactivitystateeventaudits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkactivitystateeventaudits (
    keyvalue character varying(60) NOT NULL,
    name character varying(100) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 251 (class 1259 OID 16675)
-- Name: shkactivitystates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkactivitystates (
    keyvalue character varying(60) NOT NULL,
    name character varying(100) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 252 (class 1259 OID 16678)
-- Name: shkandjointable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkandjointable (
    process numeric(19,0) NOT NULL,
    blockactivity numeric(19,0) DEFAULT NULL::numeric,
    activitydefinitionid character varying(180) NOT NULL,
    activity numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 253 (class 1259 OID 16682)
-- Name: shkassignmenteventaudits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkassignmenteventaudits (
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(200) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(200) NOT NULL,
    activityname character varying(508) DEFAULT NULL::character varying,
    processid character varying(200) NOT NULL,
    processname character varying(508) DEFAULT NULL::character varying,
    processfactoryname character varying(400) NOT NULL,
    processfactoryversion character varying(40) NOT NULL,
    activitydefinitionid character varying(180) NOT NULL,
    activitydefinitionname character varying(180) DEFAULT NULL::character varying,
    activitydefinitiontype integer NOT NULL,
    processdefinitionid character varying(180) NOT NULL,
    processdefinitionname character varying(180) DEFAULT NULL::character varying,
    packageid character varying(180) NOT NULL,
    oldresourceusername character varying(200) DEFAULT NULL::character varying,
    oldresourcename character varying(200) DEFAULT NULL::character varying,
    newresourceusername character varying(200) NOT NULL,
    newresourcename character varying(200) DEFAULT NULL::character varying,
    isaccepted boolean NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 254 (class 1259 OID 16694)
-- Name: shkassignmentstable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkassignmentstable (
    activity numeric(19,0) NOT NULL,
    theresource numeric(19,0) NOT NULL,
    activityid character varying(200) NOT NULL,
    activityprocessid character varying(200) NOT NULL,
    activityprocessdefname character varying(400) NOT NULL,
    resourceid character varying(200) NOT NULL,
    isaccepted boolean NOT NULL,
    isvalid boolean NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 255 (class 1259 OID 16699)
-- Name: shkcounters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkcounters (
    name character varying(200) NOT NULL,
    the_number numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 256 (class 1259 OID 16702)
-- Name: shkcreateprocesseventaudits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkcreateprocesseventaudits (
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(200) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    processid character varying(200) NOT NULL,
    processname character varying(508) DEFAULT NULL::character varying,
    processfactoryname character varying(400) NOT NULL,
    processfactoryversion character varying(40) NOT NULL,
    processdefinitionid character varying(180) NOT NULL,
    processdefinitionname character varying(180) DEFAULT NULL::character varying,
    packageid character varying(180) NOT NULL,
    pactivityid character varying(200) DEFAULT NULL::character varying,
    pprocessid character varying(200) DEFAULT NULL::character varying,
    pprocessname character varying(508) DEFAULT NULL::character varying,
    pprocessfactoryname character varying(400) DEFAULT NULL::character varying,
    pprocessfactoryversion character varying(40) DEFAULT NULL::character varying,
    pactivitydefinitionid character varying(180) DEFAULT NULL::character varying,
    pactivitydefinitionname character varying(180) DEFAULT NULL::character varying,
    pprocessdefinitionid character varying(180) DEFAULT NULL::character varying,
    pprocessdefinitionname character varying(180) DEFAULT NULL::character varying,
    ppackageid character varying(180) DEFAULT NULL::character varying,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 257 (class 1259 OID 16719)
-- Name: shkdataeventaudits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkdataeventaudits (
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(200) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(200) DEFAULT NULL::character varying,
    activityname character varying(508) DEFAULT NULL::character varying,
    processid character varying(200) NOT NULL,
    processname character varying(508) DEFAULT NULL::character varying,
    processfactoryname character varying(400) NOT NULL,
    processfactoryversion character varying(40) NOT NULL,
    activitydefinitionid character varying(180) DEFAULT NULL::character varying,
    activitydefinitionname character varying(180) DEFAULT NULL::character varying,
    activitydefinitiontype integer,
    processdefinitionid character varying(180) NOT NULL,
    processdefinitionname character varying(180) DEFAULT NULL::character varying,
    packageid character varying(180) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 258 (class 1259 OID 16730)
-- Name: shkdeadlines; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkdeadlines (
    process numeric(19,0) NOT NULL,
    activity numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    timelimit bigint NOT NULL,
    timelimittzo bigint NOT NULL,
    exceptionname character varying(200) NOT NULL,
    issynchronous boolean NOT NULL,
    isexecuted boolean NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 259 (class 1259 OID 16733)
-- Name: shkeventtypes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkeventtypes (
    keyvalue character varying(60) NOT NULL,
    name character varying(100) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 260 (class 1259 OID 16736)
-- Name: shkgroupgrouptable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkgroupgrouptable (
    sub_gid numeric(19,0) NOT NULL,
    groupid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 261 (class 1259 OID 16739)
-- Name: shkgrouptable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkgrouptable (
    groupid character varying(200) NOT NULL,
    description character varying(508) DEFAULT NULL::character varying,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 262 (class 1259 OID 16745)
-- Name: shkgroupuser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkgroupuser (
    username character varying(200) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 263 (class 1259 OID 16748)
-- Name: shkgroupuserpacklevelpart; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkgroupuserpacklevelpart (
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 264 (class 1259 OID 16751)
-- Name: shkgroupuserproclevelpart; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkgroupuserproclevelpart (
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 265 (class 1259 OID 16754)
-- Name: shkneweventauditdata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkneweventauditdata (
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(200) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(8000) DEFAULT NULL::character varying,
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp with time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 266 (class 1259 OID 16760)
-- Name: shkneweventauditdatablobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkneweventauditdatablobs (
    neweventauditdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 267 (class 1259 OID 16765)
-- Name: shkneweventauditdatawob; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkneweventauditdatawob (
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(200) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(8000) DEFAULT NULL::character varying,
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp with time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 268 (class 1259 OID 16771)
-- Name: shknextxpdlversions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shknextxpdlversions (
    xpdlid character varying(180) NOT NULL,
    nextversion character varying(40) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 269 (class 1259 OID 16774)
-- Name: shknormaluser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shknormaluser (
    username character varying(200) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 270 (class 1259 OID 16777)
-- Name: shkoldeventauditdata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkoldeventauditdata (
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(200) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(8000) DEFAULT NULL::character varying,
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp with time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 271 (class 1259 OID 16783)
-- Name: shkoldeventauditdatablobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkoldeventauditdatablobs (
    oldeventauditdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 272 (class 1259 OID 16788)
-- Name: shkoldeventauditdatawob; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkoldeventauditdatawob (
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(200) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(8000) DEFAULT NULL::character varying,
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp with time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 273 (class 1259 OID 16794)
-- Name: shkpacklevelparticipant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkpacklevelparticipant (
    participant_id character varying(180) NOT NULL,
    packageoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 274 (class 1259 OID 16797)
-- Name: shkpacklevelxpdlapp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkpacklevelxpdlapp (
    application_id character varying(180) NOT NULL,
    packageoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 275 (class 1259 OID 16800)
-- Name: shkpacklevelxpdlapptaappdetail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkpacklevelxpdlapptaappdetail (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 276 (class 1259 OID 16803)
-- Name: shkpacklevelxpdlapptaappdetusr; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkpacklevelxpdlapptaappdetusr (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 277 (class 1259 OID 16806)
-- Name: shkpacklevelxpdlapptaappuser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkpacklevelxpdlapptaappuser (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 278 (class 1259 OID 16809)
-- Name: shkpacklevelxpdlapptoolagntapp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkpacklevelxpdlapptoolagntapp (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 279 (class 1259 OID 16812)
-- Name: shkprocessdata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkprocessdata (
    process numeric(19,0) NOT NULL,
    variabledefinitionid character varying(200) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(8000) DEFAULT NULL::character varying,
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp with time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 280 (class 1259 OID 16818)
-- Name: shkprocessdatablobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkprocessdatablobs (
    processdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 281 (class 1259 OID 16823)
-- Name: shkprocessdatawob; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkprocessdatawob (
    process numeric(19,0) NOT NULL,
    variabledefinitionid character varying(200) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(8000) DEFAULT NULL::character varying,
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp with time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 282 (class 1259 OID 16829)
-- Name: shkprocessdefinitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkprocessdefinitions (
    name character varying(400) NOT NULL,
    packageid character varying(180) NOT NULL,
    processdefinitionid character varying(180) NOT NULL,
    processdefinitioncreated bigint NOT NULL,
    processdefinitionversion character varying(40) NOT NULL,
    state integer NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 283 (class 1259 OID 16834)
-- Name: shkprocesses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkprocesses (
    syncversion bigint NOT NULL,
    id character varying(200) NOT NULL,
    processdefinition numeric(19,0) NOT NULL,
    pdefname character varying(400) NOT NULL,
    activityrequesterid character varying(200) DEFAULT NULL::character varying,
    activityrequesterprocessid character varying(200) DEFAULT NULL::character varying,
    resourcerequesterid character varying(200) NOT NULL,
    externalrequesterclassname character varying(508) DEFAULT NULL::character varying,
    state numeric(19,0) NOT NULL,
    priority integer,
    name character varying(508) DEFAULT NULL::character varying,
    created bigint NOT NULL,
    createdtzo bigint NOT NULL,
    started bigint,
    startedtzo bigint,
    laststatetime bigint NOT NULL,
    laststatetimetzo bigint NOT NULL,
    limittime bigint NOT NULL,
    limittimetzo bigint NOT NULL,
    description character varying(508) DEFAULT NULL::character varying,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 284 (class 1259 OID 16844)
-- Name: shkprocessrequesters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkprocessrequesters (
    id character varying(200) NOT NULL,
    activityrequester numeric(19,0) DEFAULT NULL::numeric,
    resourcerequester numeric(19,0) DEFAULT NULL::numeric,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 285 (class 1259 OID 16849)
-- Name: shkprocessstateeventaudits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkprocessstateeventaudits (
    keyvalue character varying(60) NOT NULL,
    name character varying(100) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 286 (class 1259 OID 16852)
-- Name: shkprocessstates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkprocessstates (
    keyvalue character varying(60) NOT NULL,
    name character varying(100) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 287 (class 1259 OID 16855)
-- Name: shkproclevelparticipant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkproclevelparticipant (
    participant_id character varying(180) NOT NULL,
    processoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 288 (class 1259 OID 16858)
-- Name: shkproclevelxpdlapp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkproclevelxpdlapp (
    application_id character varying(180) NOT NULL,
    processoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 289 (class 1259 OID 16861)
-- Name: shkproclevelxpdlapptaappdetail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkproclevelxpdlapptaappdetail (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 290 (class 1259 OID 16864)
-- Name: shkproclevelxpdlapptaappdetusr; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkproclevelxpdlapptaappdetusr (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 291 (class 1259 OID 16867)
-- Name: shkproclevelxpdlapptaappuser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkproclevelxpdlapptaappuser (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 292 (class 1259 OID 16870)
-- Name: shkproclevelxpdlapptoolagntapp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkproclevelxpdlapptoolagntapp (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 293 (class 1259 OID 16873)
-- Name: shkresourcestable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkresourcestable (
    username character varying(200) NOT NULL,
    name character varying(200) DEFAULT NULL::character varying,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 294 (class 1259 OID 16877)
-- Name: shkstateeventaudits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkstateeventaudits (
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(200) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(200) DEFAULT NULL::character varying,
    activityname character varying(508) DEFAULT NULL::character varying,
    processid character varying(200) NOT NULL,
    processname character varying(508) DEFAULT NULL::character varying,
    processfactoryname character varying(400) NOT NULL,
    processfactoryversion character varying(40) NOT NULL,
    activitydefinitionid character varying(180) DEFAULT NULL::character varying,
    activitydefinitionname character varying(180) DEFAULT NULL::character varying,
    activitydefinitiontype integer,
    processdefinitionid character varying(180) NOT NULL,
    processdefinitionname character varying(180) DEFAULT NULL::character varying,
    packageid character varying(180) NOT NULL,
    oldprocessstate numeric(19,0) DEFAULT NULL::numeric,
    newprocessstate numeric(19,0) DEFAULT NULL::numeric,
    oldactivitystate numeric(19,0) DEFAULT NULL::numeric,
    newactivitystate numeric(19,0) DEFAULT NULL::numeric,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 295 (class 1259 OID 16892)
-- Name: shktoolagentapp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shktoolagentapp (
    tool_agent_name character varying(500) NOT NULL,
    app_name character varying(180) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 296 (class 1259 OID 16897)
-- Name: shktoolagentappdetail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shktoolagentappdetail (
    app_mode numeric(10,0) NOT NULL,
    toolagent_appoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 297 (class 1259 OID 16900)
-- Name: shktoolagentappdetailuser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shktoolagentappdetailuser (
    toolagent_appoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 298 (class 1259 OID 16903)
-- Name: shktoolagentappuser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shktoolagentappuser (
    toolagent_appoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 299 (class 1259 OID 16906)
-- Name: shktoolagentuser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shktoolagentuser (
    username character varying(200) NOT NULL,
    pwd character varying(200) DEFAULT NULL::character varying,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 300 (class 1259 OID 16910)
-- Name: shkusergrouptable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkusergrouptable (
    userid numeric(19,0) NOT NULL,
    groupid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 301 (class 1259 OID 16913)
-- Name: shkuserpacklevelpart; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkuserpacklevelpart (
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 302 (class 1259 OID 16916)
-- Name: shkuserproclevelparticipant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkuserproclevelparticipant (
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 303 (class 1259 OID 16919)
-- Name: shkusertable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkusertable (
    userid character varying(200) NOT NULL,
    firstname character varying(100) DEFAULT NULL::character varying,
    lastname character varying(100) DEFAULT NULL::character varying,
    passwd character varying(100) NOT NULL,
    email character varying(508) DEFAULT NULL::character varying,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 304 (class 1259 OID 16927)
-- Name: shkxpdlapplicationpackage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdlapplicationpackage (
    package_id character varying(180) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 305 (class 1259 OID 16930)
-- Name: shkxpdlapplicationprocess; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdlapplicationprocess (
    process_id character varying(180) NOT NULL,
    packageoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 306 (class 1259 OID 16933)
-- Name: shkxpdldata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdldata (
    xpdlcontent bytea,
    xpdlclasscontent bytea,
    xpdl numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 307 (class 1259 OID 16938)
-- Name: shkxpdlhistory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdlhistory (
    xpdlid character varying(180) NOT NULL,
    xpdlversion character varying(40) NOT NULL,
    xpdlclassversion bigint NOT NULL,
    xpdluploadtime timestamp with time zone NOT NULL,
    xpdlhistoryuploadtime timestamp with time zone NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 308 (class 1259 OID 16941)
-- Name: shkxpdlhistorydata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdlhistorydata (
    xpdlcontent bytea NOT NULL,
    xpdlclasscontent bytea NOT NULL,
    xpdlhistory numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 309 (class 1259 OID 16946)
-- Name: shkxpdlparticipantpackage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdlparticipantpackage (
    package_id character varying(180) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 310 (class 1259 OID 16949)
-- Name: shkxpdlparticipantprocess; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdlparticipantprocess (
    process_id character varying(180) NOT NULL,
    packageoid numeric(19,0) NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 311 (class 1259 OID 16952)
-- Name: shkxpdlreferences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdlreferences (
    referredxpdlid character varying(180) NOT NULL,
    referringxpdl numeric(19,0) NOT NULL,
    referredxpdlnumber integer NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 312 (class 1259 OID 16955)
-- Name: shkxpdls; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE shkxpdls (
    xpdlid character varying(180) NOT NULL,
    xpdlversion character varying(40) NOT NULL,
    xpdlclassversion bigint NOT NULL,
    xpdluploadtime timestamp with time zone NOT NULL,
    oid numeric(19,0) NOT NULL,
    version integer NOT NULL
);


--
-- TOC entry 313 (class 1259 OID 16958)
-- Name: wf_audit_trail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_audit_trail (
    id character varying(510) NOT NULL,
    username character varying(510) DEFAULT NULL::character varying,
    clazz character varying(510) DEFAULT NULL::character varying,
    method character varying(510) DEFAULT NULL::character varying,
    message text,
    "timestamp" timestamp with time zone,
    appid character varying(510) DEFAULT NULL::character varying,
    appversion character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 321 (class 1259 OID 18097)
-- Name: wf_history_activity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_history_activity (
    activityid character varying(255) NOT NULL,
    activityname character varying(255),
    activitydefid character varying(255),
    activated bigint,
    accepted bigint,
    laststatetime bigint,
    limitduration character varying(255),
    participantid character varying(255),
    assignmentusers character varying(255),
    performer character varying(255),
    state character varying(255),
    type character varying(255),
    due timestamp without time zone,
    variables character varying(255),
    processid character varying(255)
);


--
-- TOC entry 322 (class 1259 OID 18104)
-- Name: wf_history_process; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_history_process (
    processid character varying(255) NOT NULL,
    processname character varying(255),
    processrequesterid character varying(255),
    resourcerequesterid character varying(255),
    version character varying(255),
    processdefid character varying(255),
    started bigint,
    created bigint,
    laststatetime bigint,
    limitduration character varying(255),
    due timestamp without time zone,
    state character varying(255),
    variables character varying(255)
);


--
-- TOC entry 314 (class 1259 OID 16968)
-- Name: wf_process_link; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_process_link (
    processid character varying(510) NOT NULL,
    parentprocessid character varying(510) DEFAULT NULL::character varying,
    originprocessid character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 323 (class 1259 OID 18111)
-- Name: wf_process_link_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_process_link_history (
    processid character varying(255) NOT NULL,
    parentprocessid character varying(255),
    originprocessid character varying(255)
);


--
-- TOC entry 315 (class 1259 OID 16975)
-- Name: wf_report; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_report (
    activityinstanceid character varying(510) NOT NULL,
    processinstanceid character varying(510) DEFAULT NULL::character varying,
    priority character varying(510) DEFAULT NULL::character varying,
    createdtime timestamp with time zone,
    startedtime timestamp with time zone,
    datelimit bigint,
    due timestamp with time zone,
    delay bigint,
    finishtime timestamp with time zone,
    timeconsumingfromdatecreated bigint,
    timeconsumingfromdatestarted bigint,
    performer character varying(510) DEFAULT NULL::character varying,
    nameofaccepteduser character varying(510) DEFAULT NULL::character varying,
    status character varying(510) DEFAULT NULL::character varying,
    state character varying(510) DEFAULT NULL::character varying,
    packageid character varying(510) DEFAULT NULL::character varying,
    processdefid character varying(510) DEFAULT NULL::character varying,
    activitydefid character varying(510) DEFAULT NULL::character varying,
    assignmentusers text,
    appid character varying(510) DEFAULT NULL::character varying,
    appversion bigint
);


--
-- TOC entry 316 (class 1259 OID 16990)
-- Name: wf_report_activity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_report_activity (
    activitydefid character varying(510) NOT NULL,
    activityname character varying(510) DEFAULT NULL::character varying,
    description character varying(510) DEFAULT NULL::character varying,
    priority character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 317 (class 1259 OID 16998)
-- Name: wf_report_package; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_report_package (
    packageid character varying(510) NOT NULL,
    packagename character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 318 (class 1259 OID 17004)
-- Name: wf_report_process; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_report_process (
    processdefid character varying(510) NOT NULL,
    processname character varying(510) DEFAULT NULL::character varying,
    version character varying(510) DEFAULT NULL::character varying
);


--
-- TOC entry 319 (class 1259 OID 17011)
-- Name: wf_resource_bundle_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_resource_bundle_message (
    id character varying(510) NOT NULL,
    messagekey character varying(510) DEFAULT NULL::character varying,
    locale character varying(510) DEFAULT NULL::character varying,
    message text
);


--
-- TOC entry 320 (class 1259 OID 17018)
-- Name: wf_setup; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE wf_setup (
    id character varying(510) NOT NULL,
    property character varying(510) DEFAULT NULL::character varying,
    value text,
    ordering integer
);


--
-- TOC entry 4548 (class 0 OID 16385)
-- Dependencies: 209
-- Data for Name: app_app; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4549 (class 0 OID 16391)
-- Dependencies: 210
-- Data for Name: app_builder; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4550 (class 0 OID 16396)
-- Dependencies: 211
-- Data for Name: app_datalist; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4551 (class 0 OID 16402)
-- Dependencies: 212
-- Data for Name: app_env_variable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4552 (class 0 OID 16407)
-- Dependencies: 213
-- Data for Name: app_fd; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4553 (class 0 OID 16412)
-- Dependencies: 214
-- Data for Name: app_fd_appcenter; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4554 (class 0 OID 16417)
-- Dependencies: 215
-- Data for Name: app_form; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4555 (class 0 OID 16424)
-- Dependencies: 216
-- Data for Name: app_form_data_audit_trail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4556 (class 0 OID 16429)
-- Dependencies: 217
-- Data for Name: app_message; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4557 (class 0 OID 16436)
-- Dependencies: 218
-- Data for Name: app_package; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4558 (class 0 OID 16443)
-- Dependencies: 219
-- Data for Name: app_package_activity_form; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4559 (class 0 OID 16453)
-- Dependencies: 220
-- Data for Name: app_package_activity_plugin; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4560 (class 0 OID 16460)
-- Dependencies: 221
-- Data for Name: app_package_participant; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4561 (class 0 OID 16467)
-- Dependencies: 222
-- Data for Name: app_plugin_default; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4562 (class 0 OID 16473)
-- Dependencies: 223
-- Data for Name: app_report_activity; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4563 (class 0 OID 16481)
-- Dependencies: 224
-- Data for Name: app_report_activity_instance; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4564 (class 0 OID 16492)
-- Dependencies: 225
-- Data for Name: app_report_app; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4565 (class 0 OID 16500)
-- Dependencies: 226
-- Data for Name: app_report_package; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4566 (class 0 OID 16509)
-- Dependencies: 227
-- Data for Name: app_report_process; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4567 (class 0 OID 16517)
-- Dependencies: 228
-- Data for Name: app_report_process_instance; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4568 (class 0 OID 16525)
-- Dependencies: 229
-- Data for Name: app_resource; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4569 (class 0 OID 16530)
-- Dependencies: 230
-- Data for Name: app_userview; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4570 (class 0 OID 16536)
-- Dependencies: 231
-- Data for Name: dir_department; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4571 (class 0 OID 16546)
-- Dependencies: 232
-- Data for Name: dir_employment; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4572 (class 0 OID 16557)
-- Dependencies: 233
-- Data for Name: dir_employment_report_to; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4573 (class 0 OID 16563)
-- Dependencies: 234
-- Data for Name: dir_grade; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4574 (class 0 OID 16571)
-- Dependencies: 235
-- Data for Name: dir_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4575 (class 0 OID 16579)
-- Dependencies: 236
-- Data for Name: dir_organization; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4576 (class 0 OID 16587)
-- Dependencies: 237
-- Data for Name: dir_role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO dir_role VALUES ('ROLE_ADMIN', 'Admin', 'Administrator');
INSERT INTO dir_role VALUES ('ROLE_USER', 'User', 'Normal User');


--
-- TOC entry 4577 (class 0 OID 16594)
-- Dependencies: 238
-- Data for Name: dir_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO dir_user VALUES ('admin', 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Admin', 'Admin', NULL, 1, '0', NULL);


--
-- TOC entry 4578 (class 0 OID 16606)
-- Dependencies: 239
-- Data for Name: dir_user_extra; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4579 (class 0 OID 16612)
-- Dependencies: 240
-- Data for Name: dir_user_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4580 (class 0 OID 16617)
-- Dependencies: 241
-- Data for Name: dir_user_meta; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4581 (class 0 OID 16622)
-- Dependencies: 242
-- Data for Name: dir_user_password_history; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4582 (class 0 OID 16630)
-- Dependencies: 243
-- Data for Name: dir_user_replacement; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4583 (class 0 OID 16635)
-- Dependencies: 244
-- Data for Name: dir_user_role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO dir_user_role VALUES ('ROLE_ADMIN', 'admin');


--
-- TOC entry 4584 (class 0 OID 16640)
-- Dependencies: 245
-- Data for Name: objectid; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO objectid VALUES (1000200);


--
-- TOC entry 4585 (class 0 OID 16643)
-- Dependencies: 246
-- Data for Name: shkactivities; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4586 (class 0 OID 16655)
-- Dependencies: 247
-- Data for Name: shkactivitydata; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4587 (class 0 OID 16661)
-- Dependencies: 248
-- Data for Name: shkactivitydatablobs; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4588 (class 0 OID 16666)
-- Dependencies: 249
-- Data for Name: shkactivitydatawob; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4589 (class 0 OID 16672)
-- Dependencies: 250
-- Data for Name: shkactivitystateeventaudits; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4590 (class 0 OID 16675)
-- Dependencies: 251
-- Data for Name: shkactivitystates; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO shkactivitystates VALUES ('open.running', 'open.running', 1000001, 0);
INSERT INTO shkactivitystates VALUES ('open.not_running.not_started', 'open.not_running.not_started', 1000003, 0);
INSERT INTO shkactivitystates VALUES ('open.not_running.suspended', 'open.not_running.suspended', 1000005, 0);
INSERT INTO shkactivitystates VALUES ('closed.completed', 'closed.completed', 1000007, 0);
INSERT INTO shkactivitystates VALUES ('closed.terminated', 'closed.terminated', 1000009, 0);
INSERT INTO shkactivitystates VALUES ('closed.aborted', 'closed.aborted', 1000011, 0);


--
-- TOC entry 4591 (class 0 OID 16678)
-- Dependencies: 252
-- Data for Name: shkandjointable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4592 (class 0 OID 16682)
-- Dependencies: 253
-- Data for Name: shkassignmenteventaudits; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4593 (class 0 OID 16694)
-- Dependencies: 254
-- Data for Name: shkassignmentstable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4594 (class 0 OID 16699)
-- Dependencies: 255
-- Data for Name: shkcounters; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4595 (class 0 OID 16702)
-- Dependencies: 256
-- Data for Name: shkcreateprocesseventaudits; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4596 (class 0 OID 16719)
-- Dependencies: 257
-- Data for Name: shkdataeventaudits; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4597 (class 0 OID 16730)
-- Dependencies: 258
-- Data for Name: shkdeadlines; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4598 (class 0 OID 16733)
-- Dependencies: 259
-- Data for Name: shkeventtypes; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4599 (class 0 OID 16736)
-- Dependencies: 260
-- Data for Name: shkgroupgrouptable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4600 (class 0 OID 16739)
-- Dependencies: 261
-- Data for Name: shkgrouptable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4601 (class 0 OID 16745)
-- Dependencies: 262
-- Data for Name: shkgroupuser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4602 (class 0 OID 16748)
-- Dependencies: 263
-- Data for Name: shkgroupuserpacklevelpart; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4603 (class 0 OID 16751)
-- Dependencies: 264
-- Data for Name: shkgroupuserproclevelpart; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4604 (class 0 OID 16754)
-- Dependencies: 265
-- Data for Name: shkneweventauditdata; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4605 (class 0 OID 16760)
-- Dependencies: 266
-- Data for Name: shkneweventauditdatablobs; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4606 (class 0 OID 16765)
-- Dependencies: 267
-- Data for Name: shkneweventauditdatawob; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4607 (class 0 OID 16771)
-- Dependencies: 268
-- Data for Name: shknextxpdlversions; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4608 (class 0 OID 16774)
-- Dependencies: 269
-- Data for Name: shknormaluser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4609 (class 0 OID 16777)
-- Dependencies: 270
-- Data for Name: shkoldeventauditdata; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4610 (class 0 OID 16783)
-- Dependencies: 271
-- Data for Name: shkoldeventauditdatablobs; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4611 (class 0 OID 16788)
-- Dependencies: 272
-- Data for Name: shkoldeventauditdatawob; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4612 (class 0 OID 16794)
-- Dependencies: 273
-- Data for Name: shkpacklevelparticipant; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4613 (class 0 OID 16797)
-- Dependencies: 274
-- Data for Name: shkpacklevelxpdlapp; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4614 (class 0 OID 16800)
-- Dependencies: 275
-- Data for Name: shkpacklevelxpdlapptaappdetail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4615 (class 0 OID 16803)
-- Dependencies: 276
-- Data for Name: shkpacklevelxpdlapptaappdetusr; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4616 (class 0 OID 16806)
-- Dependencies: 277
-- Data for Name: shkpacklevelxpdlapptaappuser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4617 (class 0 OID 16809)
-- Dependencies: 278
-- Data for Name: shkpacklevelxpdlapptoolagntapp; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4618 (class 0 OID 16812)
-- Dependencies: 279
-- Data for Name: shkprocessdata; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4619 (class 0 OID 16818)
-- Dependencies: 280
-- Data for Name: shkprocessdatablobs; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4620 (class 0 OID 16823)
-- Dependencies: 281
-- Data for Name: shkprocessdatawob; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4621 (class 0 OID 16829)
-- Dependencies: 282
-- Data for Name: shkprocessdefinitions; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4622 (class 0 OID 16834)
-- Dependencies: 283
-- Data for Name: shkprocesses; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4623 (class 0 OID 16844)
-- Dependencies: 284
-- Data for Name: shkprocessrequesters; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4624 (class 0 OID 16849)
-- Dependencies: 285
-- Data for Name: shkprocessstateeventaudits; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4625 (class 0 OID 16852)
-- Dependencies: 286
-- Data for Name: shkprocessstates; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO shkprocessstates VALUES ('open.running', 'open.running', 1000000, 0);
INSERT INTO shkprocessstates VALUES ('open.not_running.not_started', 'open.not_running.not_started', 1000002, 0);
INSERT INTO shkprocessstates VALUES ('open.not_running.suspended', 'open.not_running.suspended', 1000004, 0);
INSERT INTO shkprocessstates VALUES ('closed.completed', 'closed.completed', 1000006, 0);
INSERT INTO shkprocessstates VALUES ('closed.terminated', 'closed.terminated', 1000008, 0);
INSERT INTO shkprocessstates VALUES ('closed.aborted', 'closed.aborted', 1000010, 0);


--
-- TOC entry 4626 (class 0 OID 16855)
-- Dependencies: 287
-- Data for Name: shkproclevelparticipant; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4627 (class 0 OID 16858)
-- Dependencies: 288
-- Data for Name: shkproclevelxpdlapp; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4628 (class 0 OID 16861)
-- Dependencies: 289
-- Data for Name: shkproclevelxpdlapptaappdetail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4629 (class 0 OID 16864)
-- Dependencies: 290
-- Data for Name: shkproclevelxpdlapptaappdetusr; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4630 (class 0 OID 16867)
-- Dependencies: 291
-- Data for Name: shkproclevelxpdlapptaappuser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4631 (class 0 OID 16870)
-- Dependencies: 292
-- Data for Name: shkproclevelxpdlapptoolagntapp; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4632 (class 0 OID 16873)
-- Dependencies: 293
-- Data for Name: shkresourcestable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4633 (class 0 OID 16877)
-- Dependencies: 294
-- Data for Name: shkstateeventaudits; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4634 (class 0 OID 16892)
-- Dependencies: 295
-- Data for Name: shktoolagentapp; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4635 (class 0 OID 16897)
-- Dependencies: 296
-- Data for Name: shktoolagentappdetail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4636 (class 0 OID 16900)
-- Dependencies: 297
-- Data for Name: shktoolagentappdetailuser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4637 (class 0 OID 16903)
-- Dependencies: 298
-- Data for Name: shktoolagentappuser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4638 (class 0 OID 16906)
-- Dependencies: 299
-- Data for Name: shktoolagentuser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4639 (class 0 OID 16910)
-- Dependencies: 300
-- Data for Name: shkusergrouptable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4640 (class 0 OID 16913)
-- Dependencies: 301
-- Data for Name: shkuserpacklevelpart; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4641 (class 0 OID 16916)
-- Dependencies: 302
-- Data for Name: shkuserproclevelparticipant; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4642 (class 0 OID 16919)
-- Dependencies: 303
-- Data for Name: shkusertable; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4643 (class 0 OID 16927)
-- Dependencies: 304
-- Data for Name: shkxpdlapplicationpackage; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4644 (class 0 OID 16930)
-- Dependencies: 305
-- Data for Name: shkxpdlapplicationprocess; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4645 (class 0 OID 16933)
-- Dependencies: 306
-- Data for Name: shkxpdldata; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4646 (class 0 OID 16938)
-- Dependencies: 307
-- Data for Name: shkxpdlhistory; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4647 (class 0 OID 16941)
-- Dependencies: 308
-- Data for Name: shkxpdlhistorydata; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4648 (class 0 OID 16946)
-- Dependencies: 309
-- Data for Name: shkxpdlparticipantpackage; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4649 (class 0 OID 16949)
-- Dependencies: 310
-- Data for Name: shkxpdlparticipantprocess; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4650 (class 0 OID 16952)
-- Dependencies: 311
-- Data for Name: shkxpdlreferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4651 (class 0 OID 16955)
-- Dependencies: 312
-- Data for Name: shkxpdls; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4652 (class 0 OID 16958)
-- Dependencies: 313
-- Data for Name: wf_audit_trail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4660 (class 0 OID 18097)
-- Dependencies: 321
-- Data for Name: wf_history_activity; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4661 (class 0 OID 18104)
-- Dependencies: 322
-- Data for Name: wf_history_process; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4653 (class 0 OID 16968)
-- Dependencies: 314
-- Data for Name: wf_process_link; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4662 (class 0 OID 18111)
-- Dependencies: 323
-- Data for Name: wf_process_link_history; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4654 (class 0 OID 16975)
-- Dependencies: 315
-- Data for Name: wf_report; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4655 (class 0 OID 16990)
-- Dependencies: 316
-- Data for Name: wf_report_activity; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4656 (class 0 OID 16998)
-- Dependencies: 317
-- Data for Name: wf_report_package; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4657 (class 0 OID 17004)
-- Dependencies: 318
-- Data for Name: wf_report_process; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4658 (class 0 OID 17011)
-- Dependencies: 319
-- Data for Name: wf_resource_bundle_message; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4659 (class 0 OID 17018)
-- Dependencies: 320
-- Data for Name: wf_setup; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3788 (class 2606 OID 17025)
-- Name: app_app app_app_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_app
    ADD CONSTRAINT app_app_pkey PRIMARY KEY (appid, appversion);


--
-- TOC entry 3791 (class 2606 OID 17027)
-- Name: app_builder app_builder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_builder
    ADD CONSTRAINT app_builder_pkey PRIMARY KEY (appid, appversion, id);


--
-- TOC entry 3795 (class 2606 OID 17029)
-- Name: app_datalist app_datalist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_datalist
    ADD CONSTRAINT app_datalist_pkey PRIMARY KEY (appid, appversion, id);


--
-- TOC entry 3798 (class 2606 OID 17031)
-- Name: app_env_variable app_env_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_env_variable
    ADD CONSTRAINT app_env_variable_pkey PRIMARY KEY (appid, appversion, id);


--
-- TOC entry 3802 (class 2606 OID 17033)
-- Name: app_fd_appcenter app_fd_appcenter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_fd_appcenter
    ADD CONSTRAINT app_fd_appcenter_pkey PRIMARY KEY (id);


--
-- TOC entry 3800 (class 2606 OID 17035)
-- Name: app_fd app_fd_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_fd
    ADD CONSTRAINT app_fd_pkey PRIMARY KEY (id);


--
-- TOC entry 3807 (class 2606 OID 17037)
-- Name: app_form_data_audit_trail app_form_data_audit_trail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_form_data_audit_trail
    ADD CONSTRAINT app_form_data_audit_trail_pkey PRIMARY KEY (id);


--
-- TOC entry 3805 (class 2606 OID 17039)
-- Name: app_form app_form_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_form
    ADD CONSTRAINT app_form_pkey PRIMARY KEY (appid, appversion, formid);


--
-- TOC entry 3810 (class 2606 OID 17041)
-- Name: app_message app_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_message
    ADD CONSTRAINT app_message_pkey PRIMARY KEY (appid, appversion, ouid);


--
-- TOC entry 3816 (class 2606 OID 17043)
-- Name: app_package_activity_form app_package_activity_form_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_package_activity_form
    ADD CONSTRAINT app_package_activity_form_pkey PRIMARY KEY (processdefid, activitydefid, packageid, packageversion);


--
-- TOC entry 3819 (class 2606 OID 17045)
-- Name: app_package_activity_plugin app_package_activity_plugin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_package_activity_plugin
    ADD CONSTRAINT app_package_activity_plugin_pkey PRIMARY KEY (processdefid, activitydefid, packageid, packageversion);


--
-- TOC entry 3822 (class 2606 OID 17047)
-- Name: app_package_participant app_package_participant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_package_participant
    ADD CONSTRAINT app_package_participant_pkey PRIMARY KEY (processdefid, participantid, packageid, packageversion);


--
-- TOC entry 3813 (class 2606 OID 17049)
-- Name: app_package app_package_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_package
    ADD CONSTRAINT app_package_pkey PRIMARY KEY (packageid, packageversion);


--
-- TOC entry 3825 (class 2606 OID 17051)
-- Name: app_plugin_default app_plugin_default_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_plugin_default
    ADD CONSTRAINT app_plugin_default_pkey PRIMARY KEY (appid, appversion, id);


--
-- TOC entry 3831 (class 2606 OID 17053)
-- Name: app_report_activity_instance app_report_activity_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_activity_instance
    ADD CONSTRAINT app_report_activity_instance_pkey PRIMARY KEY (instanceid);


--
-- TOC entry 3827 (class 2606 OID 17055)
-- Name: app_report_activity app_report_activity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_activity
    ADD CONSTRAINT app_report_activity_pkey PRIMARY KEY (uuid);


--
-- TOC entry 3834 (class 2606 OID 17057)
-- Name: app_report_app app_report_app_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_app
    ADD CONSTRAINT app_report_app_pkey PRIMARY KEY (uuid);


--
-- TOC entry 3837 (class 2606 OID 17059)
-- Name: app_report_package app_report_package_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_package
    ADD CONSTRAINT app_report_package_pkey PRIMARY KEY (uuid);


--
-- TOC entry 3842 (class 2606 OID 17061)
-- Name: app_report_process_instance app_report_process_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_process_instance
    ADD CONSTRAINT app_report_process_instance_pkey PRIMARY KEY (instanceid);


--
-- TOC entry 3840 (class 2606 OID 17063)
-- Name: app_report_process app_report_process_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_process
    ADD CONSTRAINT app_report_process_pkey PRIMARY KEY (uuid);


--
-- TOC entry 3845 (class 2606 OID 17065)
-- Name: app_resource app_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_resource
    ADD CONSTRAINT app_resource_pkey PRIMARY KEY (appid, appversion, id);


--
-- TOC entry 3848 (class 2606 OID 17067)
-- Name: app_userview app_userview_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_userview
    ADD CONSTRAINT app_userview_pkey PRIMARY KEY (appid, appversion, id);


--
-- TOC entry 3853 (class 2606 OID 17069)
-- Name: dir_department dir_department_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_department
    ADD CONSTRAINT dir_department_pkey PRIMARY KEY (id);


--
-- TOC entry 3858 (class 2606 OID 17071)
-- Name: dir_employment dir_employment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_employment
    ADD CONSTRAINT dir_employment_pkey PRIMARY KEY (id);


--
-- TOC entry 3862 (class 2606 OID 17073)
-- Name: dir_employment_report_to dir_employment_report_to_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_employment_report_to
    ADD CONSTRAINT dir_employment_report_to_pkey PRIMARY KEY (employmentid, reporttoid);


--
-- TOC entry 3866 (class 2606 OID 17075)
-- Name: dir_grade dir_grade_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_grade
    ADD CONSTRAINT dir_grade_pkey PRIMARY KEY (id);


--
-- TOC entry 3869 (class 2606 OID 17077)
-- Name: dir_group dir_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_group
    ADD CONSTRAINT dir_group_pkey PRIMARY KEY (id);


--
-- TOC entry 3872 (class 2606 OID 17079)
-- Name: dir_organization dir_organization_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_organization
    ADD CONSTRAINT dir_organization_pkey PRIMARY KEY (id);


--
-- TOC entry 3874 (class 2606 OID 17081)
-- Name: dir_role dir_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_role
    ADD CONSTRAINT dir_role_pkey PRIMARY KEY (id);


--
-- TOC entry 3878 (class 2606 OID 17083)
-- Name: dir_user_extra dir_user_extra_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_extra
    ADD CONSTRAINT dir_user_extra_pkey PRIMARY KEY (username);


--
-- TOC entry 3881 (class 2606 OID 17085)
-- Name: dir_user_group dir_user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_group
    ADD CONSTRAINT dir_user_group_pkey PRIMARY KEY (userid, groupid);


--
-- TOC entry 3884 (class 2606 OID 17087)
-- Name: dir_user_meta dir_user_meta_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_meta
    ADD CONSTRAINT dir_user_meta_pkey PRIMARY KEY (username, meta_key);


--
-- TOC entry 3886 (class 2606 OID 17089)
-- Name: dir_user_password_history dir_user_password_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_password_history
    ADD CONSTRAINT dir_user_password_history_pkey PRIMARY KEY (id);


--
-- TOC entry 3876 (class 2606 OID 17091)
-- Name: dir_user dir_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user
    ADD CONSTRAINT dir_user_pkey PRIMARY KEY (id);


--
-- TOC entry 3888 (class 2606 OID 17093)
-- Name: dir_user_replacement dir_user_replacement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_replacement
    ADD CONSTRAINT dir_user_replacement_pkey PRIMARY KEY (id);


--
-- TOC entry 3890 (class 2606 OID 17095)
-- Name: dir_user_role dir_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_role
    ADD CONSTRAINT dir_user_role_pkey PRIMARY KEY (userid, roleid);


--
-- TOC entry 3894 (class 2606 OID 17097)
-- Name: objectid objectid_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY objectid
    ADD CONSTRAINT objectid_pkey PRIMARY KEY (nextoid);


--
-- TOC entry 3896 (class 2606 OID 17099)
-- Name: shkactivities shkactivities_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_id_key UNIQUE (id);


--
-- TOC entry 3898 (class 2606 OID 17101)
-- Name: shkactivities shkactivities_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_pkey PRIMARY KEY (oid);


--
-- TOC entry 3904 (class 2606 OID 17103)
-- Name: shkactivitydata shkactivitydata_activity_variabledefinitionid_ordno_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydata
    ADD CONSTRAINT shkactivitydata_activity_variabledefinitionid_ordno_key UNIQUE (activity, variabledefinitionid, ordno);


--
-- TOC entry 3906 (class 2606 OID 17105)
-- Name: shkactivitydata shkactivitydata_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydata
    ADD CONSTRAINT shkactivitydata_cnt_key UNIQUE (cnt);


--
-- TOC entry 3908 (class 2606 OID 17107)
-- Name: shkactivitydata shkactivitydata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydata
    ADD CONSTRAINT shkactivitydata_pkey PRIMARY KEY (oid);


--
-- TOC entry 3911 (class 2606 OID 17109)
-- Name: shkactivitydatablobs shkactivitydatablobs_activitydatawob_ordno_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydatablobs
    ADD CONSTRAINT shkactivitydatablobs_activitydatawob_ordno_key UNIQUE (activitydatawob, ordno);


--
-- TOC entry 3913 (class 2606 OID 17111)
-- Name: shkactivitydatablobs shkactivitydatablobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydatablobs
    ADD CONSTRAINT shkactivitydatablobs_pkey PRIMARY KEY (oid);


--
-- TOC entry 3916 (class 2606 OID 17113)
-- Name: shkactivitydatawob shkactivitydatawob_activity_variabledefinitionid_ordno_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydatawob
    ADD CONSTRAINT shkactivitydatawob_activity_variabledefinitionid_ordno_key UNIQUE (activity, variabledefinitionid, ordno);


--
-- TOC entry 3918 (class 2606 OID 17115)
-- Name: shkactivitydatawob shkactivitydatawob_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydatawob
    ADD CONSTRAINT shkactivitydatawob_cnt_key UNIQUE (cnt);


--
-- TOC entry 3920 (class 2606 OID 17117)
-- Name: shkactivitydatawob shkactivitydatawob_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydatawob
    ADD CONSTRAINT shkactivitydatawob_pkey PRIMARY KEY (oid);


--
-- TOC entry 3922 (class 2606 OID 17119)
-- Name: shkactivitystateeventaudits shkactivitystateeventaudits_keyvalue_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitystateeventaudits
    ADD CONSTRAINT shkactivitystateeventaudits_keyvalue_key UNIQUE (keyvalue);


--
-- TOC entry 3924 (class 2606 OID 17121)
-- Name: shkactivitystateeventaudits shkactivitystateeventaudits_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitystateeventaudits
    ADD CONSTRAINT shkactivitystateeventaudits_name_key UNIQUE (name);


--
-- TOC entry 3926 (class 2606 OID 17123)
-- Name: shkactivitystateeventaudits shkactivitystateeventaudits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitystateeventaudits
    ADD CONSTRAINT shkactivitystateeventaudits_pkey PRIMARY KEY (oid);


--
-- TOC entry 3928 (class 2606 OID 17125)
-- Name: shkactivitystates shkactivitystates_keyvalue_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitystates
    ADD CONSTRAINT shkactivitystates_keyvalue_key UNIQUE (keyvalue);


--
-- TOC entry 3930 (class 2606 OID 17127)
-- Name: shkactivitystates shkactivitystates_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitystates
    ADD CONSTRAINT shkactivitystates_name_key UNIQUE (name);


--
-- TOC entry 3932 (class 2606 OID 17129)
-- Name: shkactivitystates shkactivitystates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitystates
    ADD CONSTRAINT shkactivitystates_pkey PRIMARY KEY (oid);


--
-- TOC entry 3936 (class 2606 OID 17131)
-- Name: shkandjointable shkandjointable_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_cnt_key UNIQUE (cnt);


--
-- TOC entry 3938 (class 2606 OID 17133)
-- Name: shkandjointable shkandjointable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_pkey PRIMARY KEY (oid);


--
-- TOC entry 3941 (class 2606 OID 17135)
-- Name: shkassignmenteventaudits shkassignmenteventaudits_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkassignmenteventaudits
    ADD CONSTRAINT shkassignmenteventaudits_cnt_key UNIQUE (cnt);


--
-- TOC entry 3943 (class 2606 OID 17137)
-- Name: shkassignmenteventaudits shkassignmenteventaudits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkassignmenteventaudits
    ADD CONSTRAINT shkassignmenteventaudits_pkey PRIMARY KEY (oid);


--
-- TOC entry 3947 (class 2606 OID 17139)
-- Name: shkassignmentstable shkassignmentstable_activity_theresource_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_activity_theresource_key UNIQUE (activity, theresource);


--
-- TOC entry 3949 (class 2606 OID 17141)
-- Name: shkassignmentstable shkassignmentstable_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_cnt_key UNIQUE (cnt);


--
-- TOC entry 3951 (class 2606 OID 17143)
-- Name: shkassignmentstable shkassignmentstable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_pkey PRIMARY KEY (oid);


--
-- TOC entry 3954 (class 2606 OID 17145)
-- Name: shkcounters shkcounters_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkcounters
    ADD CONSTRAINT shkcounters_name_key UNIQUE (name);


--
-- TOC entry 3956 (class 2606 OID 17147)
-- Name: shkcounters shkcounters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkcounters
    ADD CONSTRAINT shkcounters_pkey PRIMARY KEY (oid);


--
-- TOC entry 3958 (class 2606 OID 17149)
-- Name: shkcreateprocesseventaudits shkcreateprocesseventaudits_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkcreateprocesseventaudits
    ADD CONSTRAINT shkcreateprocesseventaudits_cnt_key UNIQUE (cnt);


--
-- TOC entry 3960 (class 2606 OID 17151)
-- Name: shkcreateprocesseventaudits shkcreateprocesseventaudits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkcreateprocesseventaudits
    ADD CONSTRAINT shkcreateprocesseventaudits_pkey PRIMARY KEY (oid);


--
-- TOC entry 3963 (class 2606 OID 17153)
-- Name: shkdataeventaudits shkdataeventaudits_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkdataeventaudits
    ADD CONSTRAINT shkdataeventaudits_cnt_key UNIQUE (cnt);


--
-- TOC entry 3965 (class 2606 OID 17155)
-- Name: shkdataeventaudits shkdataeventaudits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkdataeventaudits
    ADD CONSTRAINT shkdataeventaudits_pkey PRIMARY KEY (oid);


--
-- TOC entry 3969 (class 2606 OID 17157)
-- Name: shkdeadlines shkdeadlines_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_cnt_key UNIQUE (cnt);


--
-- TOC entry 3971 (class 2606 OID 17159)
-- Name: shkdeadlines shkdeadlines_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_pkey PRIMARY KEY (oid);


--
-- TOC entry 3974 (class 2606 OID 17161)
-- Name: shkeventtypes shkeventtypes_keyvalue_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkeventtypes
    ADD CONSTRAINT shkeventtypes_keyvalue_key UNIQUE (keyvalue);


--
-- TOC entry 3976 (class 2606 OID 17163)
-- Name: shkeventtypes shkeventtypes_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkeventtypes
    ADD CONSTRAINT shkeventtypes_name_key UNIQUE (name);


--
-- TOC entry 3978 (class 2606 OID 17165)
-- Name: shkeventtypes shkeventtypes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkeventtypes
    ADD CONSTRAINT shkeventtypes_pkey PRIMARY KEY (oid);


--
-- TOC entry 3981 (class 2606 OID 17167)
-- Name: shkgroupgrouptable shkgroupgrouptable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_pkey PRIMARY KEY (oid);


--
-- TOC entry 3983 (class 2606 OID 17169)
-- Name: shkgroupgrouptable shkgroupgrouptable_sub_gid_groupid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_sub_gid_groupid_key UNIQUE (sub_gid, groupid);


--
-- TOC entry 3986 (class 2606 OID 17171)
-- Name: shkgrouptable shkgrouptable_groupid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgrouptable
    ADD CONSTRAINT shkgrouptable_groupid_key UNIQUE (groupid);


--
-- TOC entry 3988 (class 2606 OID 17173)
-- Name: shkgrouptable shkgrouptable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgrouptable
    ADD CONSTRAINT shkgrouptable_pkey PRIMARY KEY (oid);


--
-- TOC entry 3990 (class 2606 OID 17175)
-- Name: shkgroupuser shkgroupuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuser
    ADD CONSTRAINT shkgroupuser_pkey PRIMARY KEY (oid);


--
-- TOC entry 3992 (class 2606 OID 17177)
-- Name: shkgroupuser shkgroupuser_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuser
    ADD CONSTRAINT shkgroupuser_username_key UNIQUE (username);


--
-- TOC entry 3995 (class 2606 OID 17179)
-- Name: shkgroupuserpacklevelpart shkgroupuserpacklevelpart_participantoid_useroid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_participantoid_useroid_key UNIQUE (participantoid, useroid);


--
-- TOC entry 3997 (class 2606 OID 17181)
-- Name: shkgroupuserpacklevelpart shkgroupuserpacklevelpart_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_pkey PRIMARY KEY (oid);


--
-- TOC entry 4001 (class 2606 OID 17183)
-- Name: shkgroupuserproclevelpart shkgroupuserproclevelpart_participantoid_useroid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_participantoid_useroid_key UNIQUE (participantoid, useroid);


--
-- TOC entry 4003 (class 2606 OID 17185)
-- Name: shkgroupuserproclevelpart shkgroupuserproclevelpart_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_pkey PRIMARY KEY (oid);


--
-- TOC entry 4006 (class 2606 OID 17187)
-- Name: shkneweventauditdata shkneweventauditdata_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdata
    ADD CONSTRAINT shkneweventauditdata_cnt_key UNIQUE (cnt);


--
-- TOC entry 4009 (class 2606 OID 17189)
-- Name: shkneweventauditdata shkneweventauditdata_dataeventaudit_variabledefinitionid_or_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdata
    ADD CONSTRAINT shkneweventauditdata_dataeventaudit_variabledefinitionid_or_key UNIQUE (dataeventaudit, variabledefinitionid, ordno);


--
-- TOC entry 4011 (class 2606 OID 17191)
-- Name: shkneweventauditdata shkneweventauditdata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdata
    ADD CONSTRAINT shkneweventauditdata_pkey PRIMARY KEY (oid);


--
-- TOC entry 4014 (class 2606 OID 17193)
-- Name: shkneweventauditdatablobs shkneweventauditdatablobs_neweventauditdatawob_ordno_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdatablobs
    ADD CONSTRAINT shkneweventauditdatablobs_neweventauditdatawob_ordno_key UNIQUE (neweventauditdatawob, ordno);


--
-- TOC entry 4016 (class 2606 OID 17195)
-- Name: shkneweventauditdatablobs shkneweventauditdatablobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdatablobs
    ADD CONSTRAINT shkneweventauditdatablobs_pkey PRIMARY KEY (oid);


--
-- TOC entry 4018 (class 2606 OID 17197)
-- Name: shkneweventauditdatawob shkneweventauditdatawob_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdatawob
    ADD CONSTRAINT shkneweventauditdatawob_cnt_key UNIQUE (cnt);


--
-- TOC entry 4021 (class 2606 OID 17199)
-- Name: shkneweventauditdatawob shkneweventauditdatawob_dataeventaudit_variabledefinitionid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdatawob
    ADD CONSTRAINT shkneweventauditdatawob_dataeventaudit_variabledefinitionid_key UNIQUE (dataeventaudit, variabledefinitionid, ordno);


--
-- TOC entry 4023 (class 2606 OID 17201)
-- Name: shkneweventauditdatawob shkneweventauditdatawob_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdatawob
    ADD CONSTRAINT shkneweventauditdatawob_pkey PRIMARY KEY (oid);


--
-- TOC entry 4025 (class 2606 OID 17203)
-- Name: shknextxpdlversions shknextxpdlversions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shknextxpdlversions
    ADD CONSTRAINT shknextxpdlversions_pkey PRIMARY KEY (oid);


--
-- TOC entry 4027 (class 2606 OID 17205)
-- Name: shknextxpdlversions shknextxpdlversions_xpdlid_nextversion_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shknextxpdlversions
    ADD CONSTRAINT shknextxpdlversions_xpdlid_nextversion_key UNIQUE (xpdlid, nextversion);


--
-- TOC entry 4029 (class 2606 OID 17207)
-- Name: shknormaluser shknormaluser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shknormaluser
    ADD CONSTRAINT shknormaluser_pkey PRIMARY KEY (oid);


--
-- TOC entry 4031 (class 2606 OID 17209)
-- Name: shknormaluser shknormaluser_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shknormaluser
    ADD CONSTRAINT shknormaluser_username_key UNIQUE (username);


--
-- TOC entry 4033 (class 2606 OID 17211)
-- Name: shkoldeventauditdata shkoldeventauditdata_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdata
    ADD CONSTRAINT shkoldeventauditdata_cnt_key UNIQUE (cnt);


--
-- TOC entry 4036 (class 2606 OID 17213)
-- Name: shkoldeventauditdata shkoldeventauditdata_dataeventaudit_variabledefinitionid_or_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdata
    ADD CONSTRAINT shkoldeventauditdata_dataeventaudit_variabledefinitionid_or_key UNIQUE (dataeventaudit, variabledefinitionid, ordno);


--
-- TOC entry 4038 (class 2606 OID 17215)
-- Name: shkoldeventauditdata shkoldeventauditdata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdata
    ADD CONSTRAINT shkoldeventauditdata_pkey PRIMARY KEY (oid);


--
-- TOC entry 4041 (class 2606 OID 17217)
-- Name: shkoldeventauditdatablobs shkoldeventauditdatablobs_oldeventauditdatawob_ordno_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdatablobs
    ADD CONSTRAINT shkoldeventauditdatablobs_oldeventauditdatawob_ordno_key UNIQUE (oldeventauditdatawob, ordno);


--
-- TOC entry 4043 (class 2606 OID 17219)
-- Name: shkoldeventauditdatablobs shkoldeventauditdatablobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdatablobs
    ADD CONSTRAINT shkoldeventauditdatablobs_pkey PRIMARY KEY (oid);


--
-- TOC entry 4045 (class 2606 OID 17221)
-- Name: shkoldeventauditdatawob shkoldeventauditdatawob_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdatawob
    ADD CONSTRAINT shkoldeventauditdatawob_cnt_key UNIQUE (cnt);


--
-- TOC entry 4048 (class 2606 OID 17223)
-- Name: shkoldeventauditdatawob shkoldeventauditdatawob_dataeventaudit_variabledefinitionid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdatawob
    ADD CONSTRAINT shkoldeventauditdatawob_dataeventaudit_variabledefinitionid_key UNIQUE (dataeventaudit, variabledefinitionid, ordno);


--
-- TOC entry 4050 (class 2606 OID 17225)
-- Name: shkoldeventauditdatawob shkoldeventauditdatawob_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdatawob
    ADD CONSTRAINT shkoldeventauditdatawob_pkey PRIMARY KEY (oid);


--
-- TOC entry 4053 (class 2606 OID 17227)
-- Name: shkpacklevelparticipant shkpacklevelparticipant_participant_id_packageoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelparticipant
    ADD CONSTRAINT shkpacklevelparticipant_participant_id_packageoid_key UNIQUE (participant_id, packageoid);


--
-- TOC entry 4055 (class 2606 OID 17229)
-- Name: shkpacklevelparticipant shkpacklevelparticipant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelparticipant
    ADD CONSTRAINT shkpacklevelparticipant_pkey PRIMARY KEY (oid);


--
-- TOC entry 4057 (class 2606 OID 17231)
-- Name: shkpacklevelxpdlapp shkpacklevelxpdlapp_application_id_packageoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapp
    ADD CONSTRAINT shkpacklevelxpdlapp_application_id_packageoid_key UNIQUE (application_id, packageoid);


--
-- TOC entry 4060 (class 2606 OID 17233)
-- Name: shkpacklevelxpdlapp shkpacklevelxpdlapp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapp
    ADD CONSTRAINT shkpacklevelxpdlapp_pkey PRIMARY KEY (oid);


--
-- TOC entry 4062 (class 2606 OID 17235)
-- Name: shkpacklevelxpdlapptaappdetail shkpacklevelxpdlapptaappdetail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_pkey PRIMARY KEY (oid);


--
-- TOC entry 4066 (class 2606 OID 17237)
-- Name: shkpacklevelxpdlapptaappdetail shkpacklevelxpdlapptaappdetail_xpdl_appoid_toolagentoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_xpdl_appoid_toolagentoid_key UNIQUE (xpdl_appoid, toolagentoid);


--
-- TOC entry 4068 (class 2606 OID 17239)
-- Name: shkpacklevelxpdlapptaappdetusr shkpacklevelxpdlapptaappdetusr_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_pkey PRIMARY KEY (oid);


--
-- TOC entry 4072 (class 2606 OID 17241)
-- Name: shkpacklevelxpdlapptaappdetusr shkpacklevelxpdlapptaappdetusr_xpdl_appoid_toolagentoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_xpdl_appoid_toolagentoid_key UNIQUE (xpdl_appoid, toolagentoid);


--
-- TOC entry 4074 (class 2606 OID 17243)
-- Name: shkpacklevelxpdlapptaappuser shkpacklevelxpdlapptaappuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_pkey PRIMARY KEY (oid);


--
-- TOC entry 4078 (class 2606 OID 17245)
-- Name: shkpacklevelxpdlapptaappuser shkpacklevelxpdlapptaappuser_xpdl_appoid_toolagentoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_xpdl_appoid_toolagentoid_key UNIQUE (xpdl_appoid, toolagentoid);


--
-- TOC entry 4080 (class 2606 OID 17247)
-- Name: shkpacklevelxpdlapptoolagntapp shkpacklevelxpdlapptoolagntapp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_pkey PRIMARY KEY (oid);


--
-- TOC entry 4084 (class 2606 OID 17249)
-- Name: shkpacklevelxpdlapptoolagntapp shkpacklevelxpdlapptoolagntapp_xpdl_appoid_toolagentoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_xpdl_appoid_toolagentoid_key UNIQUE (xpdl_appoid, toolagentoid);


--
-- TOC entry 4086 (class 2606 OID 17251)
-- Name: shkprocessdata shkprocessdata_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdata
    ADD CONSTRAINT shkprocessdata_cnt_key UNIQUE (cnt);


--
-- TOC entry 4088 (class 2606 OID 17253)
-- Name: shkprocessdata shkprocessdata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdata
    ADD CONSTRAINT shkprocessdata_pkey PRIMARY KEY (oid);


--
-- TOC entry 4091 (class 2606 OID 17255)
-- Name: shkprocessdata shkprocessdata_process_variabledefinitionid_ordno_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdata
    ADD CONSTRAINT shkprocessdata_process_variabledefinitionid_ordno_key UNIQUE (process, variabledefinitionid, ordno);


--
-- TOC entry 4093 (class 2606 OID 17257)
-- Name: shkprocessdatablobs shkprocessdatablobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdatablobs
    ADD CONSTRAINT shkprocessdatablobs_pkey PRIMARY KEY (oid);


--
-- TOC entry 4096 (class 2606 OID 17259)
-- Name: shkprocessdatablobs shkprocessdatablobs_processdatawob_ordno_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdatablobs
    ADD CONSTRAINT shkprocessdatablobs_processdatawob_ordno_key UNIQUE (processdatawob, ordno);


--
-- TOC entry 4098 (class 2606 OID 17261)
-- Name: shkprocessdatawob shkprocessdatawob_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdatawob
    ADD CONSTRAINT shkprocessdatawob_cnt_key UNIQUE (cnt);


--
-- TOC entry 4100 (class 2606 OID 17263)
-- Name: shkprocessdatawob shkprocessdatawob_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdatawob
    ADD CONSTRAINT shkprocessdatawob_pkey PRIMARY KEY (oid);


--
-- TOC entry 4103 (class 2606 OID 17265)
-- Name: shkprocessdatawob shkprocessdatawob_process_variabledefinitionid_ordno_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdatawob
    ADD CONSTRAINT shkprocessdatawob_process_variabledefinitionid_ordno_key UNIQUE (process, variabledefinitionid, ordno);


--
-- TOC entry 4105 (class 2606 OID 17267)
-- Name: shkprocessdefinitions shkprocessdefinitions_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdefinitions
    ADD CONSTRAINT shkprocessdefinitions_name_key UNIQUE (name);


--
-- TOC entry 4107 (class 2606 OID 17269)
-- Name: shkprocessdefinitions shkprocessdefinitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdefinitions
    ADD CONSTRAINT shkprocessdefinitions_pkey PRIMARY KEY (oid);


--
-- TOC entry 4109 (class 2606 OID 17271)
-- Name: shkprocesses shkprocesses_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_id_key UNIQUE (id);


--
-- TOC entry 4111 (class 2606 OID 17273)
-- Name: shkprocesses shkprocesses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_pkey PRIMARY KEY (oid);


--
-- TOC entry 4116 (class 2606 OID 17275)
-- Name: shkprocessrequesters shkprocessrequesters_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_id_key UNIQUE (id);


--
-- TOC entry 4118 (class 2606 OID 17277)
-- Name: shkprocessrequesters shkprocessrequesters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_pkey PRIMARY KEY (oid);


--
-- TOC entry 4121 (class 2606 OID 17279)
-- Name: shkprocessstateeventaudits shkprocessstateeventaudits_keyvalue_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessstateeventaudits
    ADD CONSTRAINT shkprocessstateeventaudits_keyvalue_key UNIQUE (keyvalue);


--
-- TOC entry 4123 (class 2606 OID 17281)
-- Name: shkprocessstateeventaudits shkprocessstateeventaudits_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessstateeventaudits
    ADD CONSTRAINT shkprocessstateeventaudits_name_key UNIQUE (name);


--
-- TOC entry 4125 (class 2606 OID 17283)
-- Name: shkprocessstateeventaudits shkprocessstateeventaudits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessstateeventaudits
    ADD CONSTRAINT shkprocessstateeventaudits_pkey PRIMARY KEY (oid);


--
-- TOC entry 4127 (class 2606 OID 17285)
-- Name: shkprocessstates shkprocessstates_keyvalue_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessstates
    ADD CONSTRAINT shkprocessstates_keyvalue_key UNIQUE (keyvalue);


--
-- TOC entry 4129 (class 2606 OID 17287)
-- Name: shkprocessstates shkprocessstates_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessstates
    ADD CONSTRAINT shkprocessstates_name_key UNIQUE (name);


--
-- TOC entry 4131 (class 2606 OID 17289)
-- Name: shkprocessstates shkprocessstates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessstates
    ADD CONSTRAINT shkprocessstates_pkey PRIMARY KEY (oid);


--
-- TOC entry 4133 (class 2606 OID 17291)
-- Name: shkproclevelparticipant shkproclevelparticipant_participant_id_processoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelparticipant
    ADD CONSTRAINT shkproclevelparticipant_participant_id_processoid_key UNIQUE (participant_id, processoid);


--
-- TOC entry 4135 (class 2606 OID 17293)
-- Name: shkproclevelparticipant shkproclevelparticipant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelparticipant
    ADD CONSTRAINT shkproclevelparticipant_pkey PRIMARY KEY (oid);


--
-- TOC entry 4138 (class 2606 OID 17295)
-- Name: shkproclevelxpdlapp shkproclevelxpdlapp_application_id_processoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapp
    ADD CONSTRAINT shkproclevelxpdlapp_application_id_processoid_key UNIQUE (application_id, processoid);


--
-- TOC entry 4140 (class 2606 OID 17297)
-- Name: shkproclevelxpdlapp shkproclevelxpdlapp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapp
    ADD CONSTRAINT shkproclevelxpdlapp_pkey PRIMARY KEY (oid);


--
-- TOC entry 4143 (class 2606 OID 17299)
-- Name: shkproclevelxpdlapptaappdetail shkproclevelxpdlapptaappdetail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_pkey PRIMARY KEY (oid);


--
-- TOC entry 4147 (class 2606 OID 17301)
-- Name: shkproclevelxpdlapptaappdetail shkproclevelxpdlapptaappdetail_xpdl_appoid_toolagentoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_xpdl_appoid_toolagentoid_key UNIQUE (xpdl_appoid, toolagentoid);


--
-- TOC entry 4149 (class 2606 OID 17303)
-- Name: shkproclevelxpdlapptaappdetusr shkproclevelxpdlapptaappdetusr_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_pkey PRIMARY KEY (oid);


--
-- TOC entry 4153 (class 2606 OID 17305)
-- Name: shkproclevelxpdlapptaappdetusr shkproclevelxpdlapptaappdetusr_xpdl_appoid_toolagentoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_xpdl_appoid_toolagentoid_key UNIQUE (xpdl_appoid, toolagentoid);


--
-- TOC entry 4155 (class 2606 OID 17307)
-- Name: shkproclevelxpdlapptaappuser shkproclevelxpdlapptaappuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_pkey PRIMARY KEY (oid);


--
-- TOC entry 4159 (class 2606 OID 17309)
-- Name: shkproclevelxpdlapptaappuser shkproclevelxpdlapptaappuser_xpdl_appoid_toolagentoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_xpdl_appoid_toolagentoid_key UNIQUE (xpdl_appoid, toolagentoid);


--
-- TOC entry 4161 (class 2606 OID 17311)
-- Name: shkproclevelxpdlapptoolagntapp shkproclevelxpdlapptoolagntapp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_pkey PRIMARY KEY (oid);


--
-- TOC entry 4165 (class 2606 OID 17313)
-- Name: shkproclevelxpdlapptoolagntapp shkproclevelxpdlapptoolagntapp_xpdl_appoid_toolagentoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_xpdl_appoid_toolagentoid_key UNIQUE (xpdl_appoid, toolagentoid);


--
-- TOC entry 4167 (class 2606 OID 17315)
-- Name: shkresourcestable shkresourcestable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkresourcestable
    ADD CONSTRAINT shkresourcestable_pkey PRIMARY KEY (oid);


--
-- TOC entry 4169 (class 2606 OID 17317)
-- Name: shkresourcestable shkresourcestable_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkresourcestable
    ADD CONSTRAINT shkresourcestable_username_key UNIQUE (username);


--
-- TOC entry 4171 (class 2606 OID 17319)
-- Name: shkstateeventaudits shkstateeventaudits_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_cnt_key UNIQUE (cnt);


--
-- TOC entry 4177 (class 2606 OID 17321)
-- Name: shkstateeventaudits shkstateeventaudits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_pkey PRIMARY KEY (oid);


--
-- TOC entry 4180 (class 2606 OID 17323)
-- Name: shktoolagentapp shktoolagentapp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentapp
    ADD CONSTRAINT shktoolagentapp_pkey PRIMARY KEY (oid);


--
-- TOC entry 4182 (class 2606 OID 17325)
-- Name: shktoolagentapp shktoolagentapp_tool_agent_name_app_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentapp
    ADD CONSTRAINT shktoolagentapp_tool_agent_name_app_name_key UNIQUE (tool_agent_name, app_name);


--
-- TOC entry 4184 (class 2606 OID 17327)
-- Name: shktoolagentappdetail shktoolagentappdetail_app_mode_toolagent_appoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappdetail
    ADD CONSTRAINT shktoolagentappdetail_app_mode_toolagent_appoid_key UNIQUE (app_mode, toolagent_appoid);


--
-- TOC entry 4186 (class 2606 OID 17329)
-- Name: shktoolagentappdetail shktoolagentappdetail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappdetail
    ADD CONSTRAINT shktoolagentappdetail_pkey PRIMARY KEY (oid);


--
-- TOC entry 4189 (class 2606 OID 17331)
-- Name: shktoolagentappdetailuser shktoolagentappdetailuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_pkey PRIMARY KEY (oid);


--
-- TOC entry 4192 (class 2606 OID 17333)
-- Name: shktoolagentappdetailuser shktoolagentappdetailuser_toolagent_appoid_useroid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_toolagent_appoid_useroid_key UNIQUE (toolagent_appoid, useroid);


--
-- TOC entry 4195 (class 2606 OID 17335)
-- Name: shktoolagentappuser shktoolagentappuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_pkey PRIMARY KEY (oid);


--
-- TOC entry 4198 (class 2606 OID 17337)
-- Name: shktoolagentappuser shktoolagentappuser_toolagent_appoid_useroid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_toolagent_appoid_useroid_key UNIQUE (toolagent_appoid, useroid);


--
-- TOC entry 4201 (class 2606 OID 17339)
-- Name: shktoolagentuser shktoolagentuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentuser
    ADD CONSTRAINT shktoolagentuser_pkey PRIMARY KEY (oid);


--
-- TOC entry 4203 (class 2606 OID 17341)
-- Name: shktoolagentuser shktoolagentuser_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentuser
    ADD CONSTRAINT shktoolagentuser_username_key UNIQUE (username);


--
-- TOC entry 4206 (class 2606 OID 17343)
-- Name: shkusergrouptable shkusergrouptable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_pkey PRIMARY KEY (oid);


--
-- TOC entry 4208 (class 2606 OID 17345)
-- Name: shkusergrouptable shkusergrouptable_userid_groupid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_userid_groupid_key UNIQUE (userid, groupid);


--
-- TOC entry 4212 (class 2606 OID 17347)
-- Name: shkuserpacklevelpart shkuserpacklevelpart_participantoid_useroid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_participantoid_useroid_key UNIQUE (participantoid, useroid);


--
-- TOC entry 4214 (class 2606 OID 17349)
-- Name: shkuserpacklevelpart shkuserpacklevelpart_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_pkey PRIMARY KEY (oid);


--
-- TOC entry 4218 (class 2606 OID 17351)
-- Name: shkuserproclevelparticipant shkuserproclevelparticipant_participantoid_useroid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_participantoid_useroid_key UNIQUE (participantoid, useroid);


--
-- TOC entry 4220 (class 2606 OID 17353)
-- Name: shkuserproclevelparticipant shkuserproclevelparticipant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_pkey PRIMARY KEY (oid);


--
-- TOC entry 4223 (class 2606 OID 17355)
-- Name: shkusertable shkusertable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkusertable
    ADD CONSTRAINT shkusertable_pkey PRIMARY KEY (oid);


--
-- TOC entry 4225 (class 2606 OID 17357)
-- Name: shkusertable shkusertable_userid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkusertable
    ADD CONSTRAINT shkusertable_userid_key UNIQUE (userid);


--
-- TOC entry 4227 (class 2606 OID 17359)
-- Name: shkxpdlapplicationpackage shkxpdlapplicationpackage_package_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlapplicationpackage
    ADD CONSTRAINT shkxpdlapplicationpackage_package_id_key UNIQUE (package_id);


--
-- TOC entry 4229 (class 2606 OID 17361)
-- Name: shkxpdlapplicationpackage shkxpdlapplicationpackage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlapplicationpackage
    ADD CONSTRAINT shkxpdlapplicationpackage_pkey PRIMARY KEY (oid);


--
-- TOC entry 4232 (class 2606 OID 17363)
-- Name: shkxpdlapplicationprocess shkxpdlapplicationprocess_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlapplicationprocess
    ADD CONSTRAINT shkxpdlapplicationprocess_pkey PRIMARY KEY (oid);


--
-- TOC entry 4234 (class 2606 OID 17365)
-- Name: shkxpdlapplicationprocess shkxpdlapplicationprocess_process_id_packageoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlapplicationprocess
    ADD CONSTRAINT shkxpdlapplicationprocess_process_id_packageoid_key UNIQUE (process_id, packageoid);


--
-- TOC entry 4236 (class 2606 OID 17367)
-- Name: shkxpdldata shkxpdldata_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdldata
    ADD CONSTRAINT shkxpdldata_cnt_key UNIQUE (cnt);


--
-- TOC entry 4238 (class 2606 OID 17369)
-- Name: shkxpdldata shkxpdldata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdldata
    ADD CONSTRAINT shkxpdldata_pkey PRIMARY KEY (oid);


--
-- TOC entry 4241 (class 2606 OID 17371)
-- Name: shkxpdldata shkxpdldata_xpdl_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdldata
    ADD CONSTRAINT shkxpdldata_xpdl_key UNIQUE (xpdl);


--
-- TOC entry 4243 (class 2606 OID 17373)
-- Name: shkxpdlhistory shkxpdlhistory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlhistory
    ADD CONSTRAINT shkxpdlhistory_pkey PRIMARY KEY (oid);


--
-- TOC entry 4245 (class 2606 OID 17375)
-- Name: shkxpdlhistory shkxpdlhistory_xpdlid_xpdlversion_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlhistory
    ADD CONSTRAINT shkxpdlhistory_xpdlid_xpdlversion_key UNIQUE (xpdlid, xpdlversion);


--
-- TOC entry 4247 (class 2606 OID 17377)
-- Name: shkxpdlhistorydata shkxpdlhistorydata_cnt_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlhistorydata
    ADD CONSTRAINT shkxpdlhistorydata_cnt_key UNIQUE (cnt);


--
-- TOC entry 4249 (class 2606 OID 17379)
-- Name: shkxpdlhistorydata shkxpdlhistorydata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlhistorydata
    ADD CONSTRAINT shkxpdlhistorydata_pkey PRIMARY KEY (oid);


--
-- TOC entry 4252 (class 2606 OID 17381)
-- Name: shkxpdlparticipantpackage shkxpdlparticipantpackage_package_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlparticipantpackage
    ADD CONSTRAINT shkxpdlparticipantpackage_package_id_key UNIQUE (package_id);


--
-- TOC entry 4254 (class 2606 OID 17383)
-- Name: shkxpdlparticipantpackage shkxpdlparticipantpackage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlparticipantpackage
    ADD CONSTRAINT shkxpdlparticipantpackage_pkey PRIMARY KEY (oid);


--
-- TOC entry 4257 (class 2606 OID 17385)
-- Name: shkxpdlparticipantprocess shkxpdlparticipantprocess_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlparticipantprocess
    ADD CONSTRAINT shkxpdlparticipantprocess_pkey PRIMARY KEY (oid);


--
-- TOC entry 4259 (class 2606 OID 17387)
-- Name: shkxpdlparticipantprocess shkxpdlparticipantprocess_process_id_packageoid_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlparticipantprocess
    ADD CONSTRAINT shkxpdlparticipantprocess_process_id_packageoid_key UNIQUE (process_id, packageoid);


--
-- TOC entry 4261 (class 2606 OID 17389)
-- Name: shkxpdlreferences shkxpdlreferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlreferences
    ADD CONSTRAINT shkxpdlreferences_pkey PRIMARY KEY (oid);


--
-- TOC entry 4263 (class 2606 OID 17391)
-- Name: shkxpdlreferences shkxpdlreferences_referredxpdlid_referringxpdl_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlreferences
    ADD CONSTRAINT shkxpdlreferences_referredxpdlid_referringxpdl_key UNIQUE (referredxpdlid, referringxpdl);


--
-- TOC entry 4266 (class 2606 OID 17393)
-- Name: shkxpdls shkxpdls_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdls
    ADD CONSTRAINT shkxpdls_pkey PRIMARY KEY (oid);


--
-- TOC entry 4268 (class 2606 OID 17395)
-- Name: shkxpdls shkxpdls_xpdlid_xpdlversion_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdls
    ADD CONSTRAINT shkxpdls_xpdlid_xpdlversion_key UNIQUE (xpdlid, xpdlversion);


--
-- TOC entry 4270 (class 2606 OID 17397)
-- Name: wf_audit_trail wf_audit_trail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_audit_trail
    ADD CONSTRAINT wf_audit_trail_pkey PRIMARY KEY (id);


--
-- TOC entry 4289 (class 2606 OID 18103)
-- Name: wf_history_activity wf_history_activity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_history_activity
    ADD CONSTRAINT wf_history_activity_pkey PRIMARY KEY (activityid);


--
-- TOC entry 4291 (class 2606 OID 18110)
-- Name: wf_history_process wf_history_process_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_history_process
    ADD CONSTRAINT wf_history_process_pkey PRIMARY KEY (processid);


--
-- TOC entry 4293 (class 2606 OID 18117)
-- Name: wf_process_link_history wf_process_link_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_process_link_history
    ADD CONSTRAINT wf_process_link_history_pkey PRIMARY KEY (processid);


--
-- TOC entry 4272 (class 2606 OID 17399)
-- Name: wf_process_link wf_process_link_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_process_link
    ADD CONSTRAINT wf_process_link_pkey PRIMARY KEY (processid);


--
-- TOC entry 4279 (class 2606 OID 17401)
-- Name: wf_report_activity wf_report_activity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_report_activity
    ADD CONSTRAINT wf_report_activity_pkey PRIMARY KEY (activitydefid);


--
-- TOC entry 4281 (class 2606 OID 17403)
-- Name: wf_report_package wf_report_package_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_report_package
    ADD CONSTRAINT wf_report_package_pkey PRIMARY KEY (packageid);


--
-- TOC entry 4276 (class 2606 OID 17405)
-- Name: wf_report wf_report_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_report
    ADD CONSTRAINT wf_report_pkey PRIMARY KEY (activityinstanceid);


--
-- TOC entry 4283 (class 2606 OID 17407)
-- Name: wf_report_process wf_report_process_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_report_process
    ADD CONSTRAINT wf_report_process_pkey PRIMARY KEY (processdefid);


--
-- TOC entry 4285 (class 2606 OID 17409)
-- Name: wf_resource_bundle_message wf_resource_bundle_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_resource_bundle_message
    ADD CONSTRAINT wf_resource_bundle_message_pkey PRIMARY KEY (id);


--
-- TOC entry 4287 (class 2606 OID 17411)
-- Name: wf_setup wf_setup_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_setup
    ADD CONSTRAINT wf_setup_pkey PRIMARY KEY (id);


--
-- TOC entry 3793 (class 1259 OID 17412)
-- Name: app_datalist_appid_appversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_datalist_appid_appversion_idx ON app_datalist USING btree (appid, appversion);


--
-- TOC entry 3796 (class 1259 OID 17413)
-- Name: app_env_variable_appid_appversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_env_variable_appid_appversion_idx ON app_env_variable USING btree (appid, appversion);


--
-- TOC entry 3803 (class 1259 OID 17414)
-- Name: app_form_appid_appversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_form_appid_appversion_idx ON app_form USING btree (appid, appversion);


--
-- TOC entry 3808 (class 1259 OID 17415)
-- Name: app_message_appid_appversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_message_appid_appversion_idx ON app_message USING btree (appid, appversion);


--
-- TOC entry 3814 (class 1259 OID 17416)
-- Name: app_package_activity_form_packageid_packageversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_package_activity_form_packageid_packageversion_idx ON app_package_activity_form USING btree (packageid, packageversion);


--
-- TOC entry 3817 (class 1259 OID 17417)
-- Name: app_package_activity_plugin_packageid_packageversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_package_activity_plugin_packageid_packageversion_idx ON app_package_activity_plugin USING btree (packageid, packageversion);


--
-- TOC entry 3811 (class 1259 OID 17418)
-- Name: app_package_appid_appversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_package_appid_appversion_idx ON app_package USING btree (appid, appversion);


--
-- TOC entry 3820 (class 1259 OID 17419)
-- Name: app_package_participant_packageid_packageversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_package_participant_packageid_packageversion_idx ON app_package_participant USING btree (packageid, packageversion);


--
-- TOC entry 3823 (class 1259 OID 17420)
-- Name: app_plugin_default_appid_appversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_plugin_default_appid_appversion_idx ON app_plugin_default USING btree (appid, appversion);


--
-- TOC entry 3829 (class 1259 OID 17421)
-- Name: app_report_activity_instance_activityuid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_report_activity_instance_activityuid_idx ON app_report_activity_instance USING btree (activityuid);


--
-- TOC entry 3832 (class 1259 OID 17422)
-- Name: app_report_activity_instance_processinstanceid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_report_activity_instance_processinstanceid_idx ON app_report_activity_instance USING btree (processinstanceid);


--
-- TOC entry 3828 (class 1259 OID 17423)
-- Name: app_report_activity_processuid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_report_activity_processuid_idx ON app_report_activity USING btree (processuid);


--
-- TOC entry 3835 (class 1259 OID 17424)
-- Name: app_report_package_appuid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_report_package_appuid_idx ON app_report_package USING btree (appuid);


--
-- TOC entry 3843 (class 1259 OID 17425)
-- Name: app_report_process_instance_processuid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_report_process_instance_processuid_idx ON app_report_process_instance USING btree (processuid);


--
-- TOC entry 3838 (class 1259 OID 17426)
-- Name: app_report_process_packageuid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_report_process_packageuid_idx ON app_report_process USING btree (packageuid);


--
-- TOC entry 3846 (class 1259 OID 17427)
-- Name: app_userview_appid_appversion_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX app_userview_appid_appversion_idx ON app_userview USING btree (appid, appversion);


--
-- TOC entry 3849 (class 1259 OID 17428)
-- Name: dir_department_hod_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_department_hod_idx ON dir_department USING btree (hod);


--
-- TOC entry 3850 (class 1259 OID 17429)
-- Name: dir_department_organizationid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_department_organizationid_idx ON dir_department USING btree (organizationid);


--
-- TOC entry 3851 (class 1259 OID 17430)
-- Name: dir_department_parentid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_department_parentid_idx ON dir_department USING btree (parentid);


--
-- TOC entry 3854 (class 1259 OID 17431)
-- Name: dir_employment_departmentid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_employment_departmentid_idx ON dir_employment USING btree (departmentid);


--
-- TOC entry 3855 (class 1259 OID 17432)
-- Name: dir_employment_gradeid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_employment_gradeid_idx ON dir_employment USING btree (gradeid);


--
-- TOC entry 3856 (class 1259 OID 17433)
-- Name: dir_employment_organizationid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_employment_organizationid_idx ON dir_employment USING btree (organizationid);


--
-- TOC entry 3860 (class 1259 OID 17434)
-- Name: dir_employment_report_to_employmentid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_employment_report_to_employmentid_idx ON dir_employment_report_to USING btree (employmentid);


--
-- TOC entry 3863 (class 1259 OID 17435)
-- Name: dir_employment_report_to_reporttoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_employment_report_to_reporttoid_idx ON dir_employment_report_to USING btree (reporttoid);


--
-- TOC entry 3859 (class 1259 OID 17436)
-- Name: dir_employment_userid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_employment_userid_idx ON dir_employment USING btree (userid);


--
-- TOC entry 3864 (class 1259 OID 17437)
-- Name: dir_grade_organizationid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_grade_organizationid_idx ON dir_grade USING btree (organizationid);


--
-- TOC entry 3867 (class 1259 OID 17438)
-- Name: dir_group_organizationid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_group_organizationid_idx ON dir_group USING btree (organizationid);


--
-- TOC entry 3870 (class 1259 OID 17439)
-- Name: dir_organization_parentid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_organization_parentid_idx ON dir_organization USING btree (parentid);


--
-- TOC entry 3879 (class 1259 OID 17440)
-- Name: dir_user_group_groupid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_user_group_groupid_idx ON dir_user_group USING btree (groupid);


--
-- TOC entry 3882 (class 1259 OID 17441)
-- Name: dir_user_group_userid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_user_group_userid_idx ON dir_user_group USING btree (userid);


--
-- TOC entry 3891 (class 1259 OID 17442)
-- Name: dir_user_role_roleid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_user_role_roleid_idx ON dir_user_role USING btree (roleid);


--
-- TOC entry 3892 (class 1259 OID 17443)
-- Name: dir_user_role_userid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dir_user_role_userid_idx ON dir_user_role USING btree (userid);


--
-- TOC entry 3789 (class 1259 OID 17444)
-- Name: idx_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_name ON app_app USING btree (name);


--
-- TOC entry 3792 (class 1259 OID 17445)
-- Name: idx_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_type ON app_builder USING btree (type);


--
-- TOC entry 3899 (class 1259 OID 17446)
-- Name: shkactivities_process_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkactivities_process_idx ON shkactivities USING btree (process);


--
-- TOC entry 3900 (class 1259 OID 17447)
-- Name: shkactivities_state_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkactivities_state_idx ON shkactivities USING btree (state);


--
-- TOC entry 3901 (class 1259 OID 17448)
-- Name: shkactivities_theresource_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkactivities_theresource_idx ON shkactivities USING btree (theresource);


--
-- TOC entry 3902 (class 1259 OID 17449)
-- Name: shkactivitydata_activity_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkactivitydata_activity_idx ON shkactivitydata USING btree (activity);


--
-- TOC entry 3909 (class 1259 OID 17450)
-- Name: shkactivitydatablobs_activitydatawob_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkactivitydatablobs_activitydatawob_idx ON shkactivitydatablobs USING btree (activitydatawob);


--
-- TOC entry 3914 (class 1259 OID 17451)
-- Name: shkactivitydatawob_activity_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkactivitydatawob_activity_idx ON shkactivitydatawob USING btree (activity);


--
-- TOC entry 3933 (class 1259 OID 17452)
-- Name: shkandjointable_activity_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkandjointable_activity_idx ON shkandjointable USING btree (activity);


--
-- TOC entry 3934 (class 1259 OID 17453)
-- Name: shkandjointable_blockactivity_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkandjointable_blockactivity_idx ON shkandjointable USING btree (blockactivity);


--
-- TOC entry 3939 (class 1259 OID 17454)
-- Name: shkandjointable_process_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkandjointable_process_idx ON shkandjointable USING btree (process);


--
-- TOC entry 3944 (class 1259 OID 17455)
-- Name: shkassignmenteventaudits_thetype_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkassignmenteventaudits_thetype_idx ON shkassignmenteventaudits USING btree (thetype);


--
-- TOC entry 3945 (class 1259 OID 17456)
-- Name: shkassignmentstable_activity_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkassignmentstable_activity_idx ON shkassignmentstable USING btree (activity);


--
-- TOC entry 3952 (class 1259 OID 17457)
-- Name: shkassignmentstable_theresource_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkassignmentstable_theresource_idx ON shkassignmentstable USING btree (theresource);


--
-- TOC entry 3961 (class 1259 OID 17458)
-- Name: shkcreateprocesseventaudits_thetype_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkcreateprocesseventaudits_thetype_idx ON shkcreateprocesseventaudits USING btree (thetype);


--
-- TOC entry 3966 (class 1259 OID 17459)
-- Name: shkdataeventaudits_thetype_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkdataeventaudits_thetype_idx ON shkdataeventaudits USING btree (thetype);


--
-- TOC entry 3967 (class 1259 OID 17460)
-- Name: shkdeadlines_activity_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkdeadlines_activity_idx ON shkdeadlines USING btree (activity);


--
-- TOC entry 3972 (class 1259 OID 17461)
-- Name: shkdeadlines_process_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkdeadlines_process_idx ON shkdeadlines USING btree (process);


--
-- TOC entry 3979 (class 1259 OID 17462)
-- Name: shkgroupgrouptable_groupid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkgroupgrouptable_groupid_idx ON shkgroupgrouptable USING btree (groupid);


--
-- TOC entry 3984 (class 1259 OID 17463)
-- Name: shkgroupgrouptable_sub_gid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkgroupgrouptable_sub_gid_idx ON shkgroupgrouptable USING btree (sub_gid);


--
-- TOC entry 3993 (class 1259 OID 17464)
-- Name: shkgroupuserpacklevelpart_participantoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkgroupuserpacklevelpart_participantoid_idx ON shkgroupuserpacklevelpart USING btree (participantoid);


--
-- TOC entry 3998 (class 1259 OID 17465)
-- Name: shkgroupuserpacklevelpart_useroid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkgroupuserpacklevelpart_useroid_idx ON shkgroupuserpacklevelpart USING btree (useroid);


--
-- TOC entry 3999 (class 1259 OID 17466)
-- Name: shkgroupuserproclevelpart_participantoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkgroupuserproclevelpart_participantoid_idx ON shkgroupuserproclevelpart USING btree (participantoid);


--
-- TOC entry 4004 (class 1259 OID 17467)
-- Name: shkgroupuserproclevelpart_useroid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkgroupuserproclevelpart_useroid_idx ON shkgroupuserproclevelpart USING btree (useroid);


--
-- TOC entry 4007 (class 1259 OID 17468)
-- Name: shkneweventauditdata_dataeventaudit_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkneweventauditdata_dataeventaudit_idx ON shkneweventauditdata USING btree (dataeventaudit);


--
-- TOC entry 4012 (class 1259 OID 17469)
-- Name: shkneweventauditdatablobs_neweventauditdatawob_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkneweventauditdatablobs_neweventauditdatawob_idx ON shkneweventauditdatablobs USING btree (neweventauditdatawob);


--
-- TOC entry 4019 (class 1259 OID 17470)
-- Name: shkneweventauditdatawob_dataeventaudit_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkneweventauditdatawob_dataeventaudit_idx ON shkneweventauditdatawob USING btree (dataeventaudit);


--
-- TOC entry 4034 (class 1259 OID 17471)
-- Name: shkoldeventauditdata_dataeventaudit_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkoldeventauditdata_dataeventaudit_idx ON shkoldeventauditdata USING btree (dataeventaudit);


--
-- TOC entry 4039 (class 1259 OID 17472)
-- Name: shkoldeventauditdatablobs_oldeventauditdatawob_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkoldeventauditdatablobs_oldeventauditdatawob_idx ON shkoldeventauditdatablobs USING btree (oldeventauditdatawob);


--
-- TOC entry 4046 (class 1259 OID 17473)
-- Name: shkoldeventauditdatawob_dataeventaudit_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkoldeventauditdatawob_dataeventaudit_idx ON shkoldeventauditdatawob USING btree (dataeventaudit);


--
-- TOC entry 4051 (class 1259 OID 17474)
-- Name: shkpacklevelparticipant_packageoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelparticipant_packageoid_idx ON shkpacklevelparticipant USING btree (packageoid);


--
-- TOC entry 4058 (class 1259 OID 17475)
-- Name: shkpacklevelxpdlapp_packageoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapp_packageoid_idx ON shkpacklevelxpdlapp USING btree (packageoid);


--
-- TOC entry 4063 (class 1259 OID 17476)
-- Name: shkpacklevelxpdlapptaappdetail_toolagentoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapptaappdetail_toolagentoid_idx ON shkpacklevelxpdlapptaappdetail USING btree (toolagentoid);


--
-- TOC entry 4064 (class 1259 OID 17477)
-- Name: shkpacklevelxpdlapptaappdetail_xpdl_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapptaappdetail_xpdl_appoid_idx ON shkpacklevelxpdlapptaappdetail USING btree (xpdl_appoid);


--
-- TOC entry 4069 (class 1259 OID 17478)
-- Name: shkpacklevelxpdlapptaappdetusr_toolagentoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapptaappdetusr_toolagentoid_idx ON shkpacklevelxpdlapptaappdetusr USING btree (toolagentoid);


--
-- TOC entry 4070 (class 1259 OID 17479)
-- Name: shkpacklevelxpdlapptaappdetusr_xpdl_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapptaappdetusr_xpdl_appoid_idx ON shkpacklevelxpdlapptaappdetusr USING btree (xpdl_appoid);


--
-- TOC entry 4075 (class 1259 OID 17480)
-- Name: shkpacklevelxpdlapptaappuser_toolagentoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapptaappuser_toolagentoid_idx ON shkpacklevelxpdlapptaappuser USING btree (toolagentoid);


--
-- TOC entry 4076 (class 1259 OID 17481)
-- Name: shkpacklevelxpdlapptaappuser_xpdl_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapptaappuser_xpdl_appoid_idx ON shkpacklevelxpdlapptaappuser USING btree (xpdl_appoid);


--
-- TOC entry 4081 (class 1259 OID 17482)
-- Name: shkpacklevelxpdlapptoolagntapp_toolagentoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapptoolagntapp_toolagentoid_idx ON shkpacklevelxpdlapptoolagntapp USING btree (toolagentoid);


--
-- TOC entry 4082 (class 1259 OID 17483)
-- Name: shkpacklevelxpdlapptoolagntapp_xpdl_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkpacklevelxpdlapptoolagntapp_xpdl_appoid_idx ON shkpacklevelxpdlapptoolagntapp USING btree (xpdl_appoid);


--
-- TOC entry 4089 (class 1259 OID 17484)
-- Name: shkprocessdata_process_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkprocessdata_process_idx ON shkprocessdata USING btree (process);


--
-- TOC entry 4094 (class 1259 OID 17485)
-- Name: shkprocessdatablobs_processdatawob_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkprocessdatablobs_processdatawob_idx ON shkprocessdatablobs USING btree (processdatawob);


--
-- TOC entry 4101 (class 1259 OID 17486)
-- Name: shkprocessdatawob_process_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkprocessdatawob_process_idx ON shkprocessdatawob USING btree (process);


--
-- TOC entry 4112 (class 1259 OID 17487)
-- Name: shkprocesses_processdefinition_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkprocesses_processdefinition_idx ON shkprocesses USING btree (processdefinition);


--
-- TOC entry 4113 (class 1259 OID 17488)
-- Name: shkprocesses_state_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkprocesses_state_idx ON shkprocesses USING btree (state);


--
-- TOC entry 4114 (class 1259 OID 17489)
-- Name: shkprocessrequesters_activityrequester_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkprocessrequesters_activityrequester_idx ON shkprocessrequesters USING btree (activityrequester);


--
-- TOC entry 4119 (class 1259 OID 17490)
-- Name: shkprocessrequesters_resourcerequester_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkprocessrequesters_resourcerequester_idx ON shkprocessrequesters USING btree (resourcerequester);


--
-- TOC entry 4136 (class 1259 OID 17491)
-- Name: shkproclevelparticipant_processoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelparticipant_processoid_idx ON shkproclevelparticipant USING btree (processoid);


--
-- TOC entry 4141 (class 1259 OID 17492)
-- Name: shkproclevelxpdlapp_processoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapp_processoid_idx ON shkproclevelxpdlapp USING btree (processoid);


--
-- TOC entry 4144 (class 1259 OID 17493)
-- Name: shkproclevelxpdlapptaappdetail_toolagentoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapptaappdetail_toolagentoid_idx ON shkproclevelxpdlapptaappdetail USING btree (toolagentoid);


--
-- TOC entry 4145 (class 1259 OID 17494)
-- Name: shkproclevelxpdlapptaappdetail_xpdl_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapptaappdetail_xpdl_appoid_idx ON shkproclevelxpdlapptaappdetail USING btree (xpdl_appoid);


--
-- TOC entry 4150 (class 1259 OID 17495)
-- Name: shkproclevelxpdlapptaappdetusr_toolagentoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapptaappdetusr_toolagentoid_idx ON shkproclevelxpdlapptaappdetusr USING btree (toolagentoid);


--
-- TOC entry 4151 (class 1259 OID 17496)
-- Name: shkproclevelxpdlapptaappdetusr_xpdl_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapptaappdetusr_xpdl_appoid_idx ON shkproclevelxpdlapptaappdetusr USING btree (xpdl_appoid);


--
-- TOC entry 4156 (class 1259 OID 17497)
-- Name: shkproclevelxpdlapptaappuser_toolagentoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapptaappuser_toolagentoid_idx ON shkproclevelxpdlapptaappuser USING btree (toolagentoid);


--
-- TOC entry 4157 (class 1259 OID 17498)
-- Name: shkproclevelxpdlapptaappuser_xpdl_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapptaappuser_xpdl_appoid_idx ON shkproclevelxpdlapptaappuser USING btree (xpdl_appoid);


--
-- TOC entry 4162 (class 1259 OID 17499)
-- Name: shkproclevelxpdlapptoolagntapp_toolagentoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapptoolagntapp_toolagentoid_idx ON shkproclevelxpdlapptoolagntapp USING btree (toolagentoid);


--
-- TOC entry 4163 (class 1259 OID 17500)
-- Name: shkproclevelxpdlapptoolagntapp_xpdl_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkproclevelxpdlapptoolagntapp_xpdl_appoid_idx ON shkproclevelxpdlapptoolagntapp USING btree (xpdl_appoid);


--
-- TOC entry 4172 (class 1259 OID 17501)
-- Name: shkstateeventaudits_newactivitystate_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkstateeventaudits_newactivitystate_idx ON shkstateeventaudits USING btree (newactivitystate);


--
-- TOC entry 4173 (class 1259 OID 17502)
-- Name: shkstateeventaudits_newprocessstate_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkstateeventaudits_newprocessstate_idx ON shkstateeventaudits USING btree (newprocessstate);


--
-- TOC entry 4174 (class 1259 OID 17503)
-- Name: shkstateeventaudits_oldactivitystate_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkstateeventaudits_oldactivitystate_idx ON shkstateeventaudits USING btree (oldactivitystate);


--
-- TOC entry 4175 (class 1259 OID 17504)
-- Name: shkstateeventaudits_oldprocessstate_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkstateeventaudits_oldprocessstate_idx ON shkstateeventaudits USING btree (oldprocessstate);


--
-- TOC entry 4178 (class 1259 OID 17505)
-- Name: shkstateeventaudits_thetype_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkstateeventaudits_thetype_idx ON shkstateeventaudits USING btree (thetype);


--
-- TOC entry 4187 (class 1259 OID 17506)
-- Name: shktoolagentappdetail_toolagent_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shktoolagentappdetail_toolagent_appoid_idx ON shktoolagentappdetail USING btree (toolagent_appoid);


--
-- TOC entry 4190 (class 1259 OID 17507)
-- Name: shktoolagentappdetailuser_toolagent_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shktoolagentappdetailuser_toolagent_appoid_idx ON shktoolagentappdetailuser USING btree (toolagent_appoid);


--
-- TOC entry 4193 (class 1259 OID 17508)
-- Name: shktoolagentappdetailuser_useroid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shktoolagentappdetailuser_useroid_idx ON shktoolagentappdetailuser USING btree (useroid);


--
-- TOC entry 4196 (class 1259 OID 17509)
-- Name: shktoolagentappuser_toolagent_appoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shktoolagentappuser_toolagent_appoid_idx ON shktoolagentappuser USING btree (toolagent_appoid);


--
-- TOC entry 4199 (class 1259 OID 17510)
-- Name: shktoolagentappuser_useroid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shktoolagentappuser_useroid_idx ON shktoolagentappuser USING btree (useroid);


--
-- TOC entry 4204 (class 1259 OID 17511)
-- Name: shkusergrouptable_groupid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkusergrouptable_groupid_idx ON shkusergrouptable USING btree (groupid);


--
-- TOC entry 4209 (class 1259 OID 17512)
-- Name: shkusergrouptable_userid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkusergrouptable_userid_idx ON shkusergrouptable USING btree (userid);


--
-- TOC entry 4210 (class 1259 OID 17513)
-- Name: shkuserpacklevelpart_participantoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkuserpacklevelpart_participantoid_idx ON shkuserpacklevelpart USING btree (participantoid);


--
-- TOC entry 4215 (class 1259 OID 17514)
-- Name: shkuserpacklevelpart_useroid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkuserpacklevelpart_useroid_idx ON shkuserpacklevelpart USING btree (useroid);


--
-- TOC entry 4216 (class 1259 OID 17515)
-- Name: shkuserproclevelparticipant_participantoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkuserproclevelparticipant_participantoid_idx ON shkuserproclevelparticipant USING btree (participantoid);


--
-- TOC entry 4221 (class 1259 OID 17516)
-- Name: shkuserproclevelparticipant_useroid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkuserproclevelparticipant_useroid_idx ON shkuserproclevelparticipant USING btree (useroid);


--
-- TOC entry 4230 (class 1259 OID 17517)
-- Name: shkxpdlapplicationprocess_packageoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkxpdlapplicationprocess_packageoid_idx ON shkxpdlapplicationprocess USING btree (packageoid);


--
-- TOC entry 4239 (class 1259 OID 17518)
-- Name: shkxpdldata_xpdl_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkxpdldata_xpdl_idx ON shkxpdldata USING btree (xpdl);


--
-- TOC entry 4250 (class 1259 OID 17519)
-- Name: shkxpdlhistorydata_xpdlhistory_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkxpdlhistorydata_xpdlhistory_idx ON shkxpdlhistorydata USING btree (xpdlhistory);


--
-- TOC entry 4255 (class 1259 OID 17520)
-- Name: shkxpdlparticipantprocess_packageoid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkxpdlparticipantprocess_packageoid_idx ON shkxpdlparticipantprocess USING btree (packageoid);


--
-- TOC entry 4264 (class 1259 OID 17521)
-- Name: shkxpdlreferences_referringxpdl_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX shkxpdlreferences_referringxpdl_idx ON shkxpdlreferences USING btree (referringxpdl);


--
-- TOC entry 4273 (class 1259 OID 17522)
-- Name: wf_report_activitydefid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX wf_report_activitydefid_idx ON wf_report USING btree (activitydefid);


--
-- TOC entry 4274 (class 1259 OID 17523)
-- Name: wf_report_packageid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX wf_report_packageid_idx ON wf_report USING btree (packageid);


--
-- TOC entry 4277 (class 1259 OID 17524)
-- Name: wf_report_processdefid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX wf_report_processdefid_idx ON wf_report USING btree (processdefid);


--
-- TOC entry 4324 (class 2606 OID 17525)
-- Name: dir_user_group fk2f0367fd159b6639; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_group
    ADD CONSTRAINT fk2f0367fd159b6639 FOREIGN KEY (groupid) REFERENCES dir_group(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4325 (class 2606 OID 17530)
-- Name: dir_user_group fk2f0367fdce539211; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_group
    ADD CONSTRAINT fk2f0367fdce539211 FOREIGN KEY (userid) REFERENCES dir_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4309 (class 2606 OID 17535)
-- Name: app_report_process_instance fk351d7bf2918f93d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_process_instance
    ADD CONSTRAINT fk351d7bf2918f93d FOREIGN KEY (processuid) REFERENCES app_report_process(uuid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4297 (class 2606 OID 17540)
-- Name: app_form fk45957822462ef4c7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_form
    ADD CONSTRAINT fk45957822462ef4c7 FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4319 (class 2606 OID 17545)
-- Name: dir_employment_report_to fk536229452787e613; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_employment_report_to
    ADD CONSTRAINT fk536229452787e613 FOREIGN KEY (employmentid) REFERENCES dir_employment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4320 (class 2606 OID 17550)
-- Name: dir_employment_report_to fk53622945f4068416; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_employment_report_to
    ADD CONSTRAINT fk53622945f4068416 FOREIGN KEY (reporttoid) REFERENCES dir_employment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4323 (class 2606 OID 17555)
-- Name: dir_organization fk55a15fa5961bd498; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_organization
    ADD CONSTRAINT fk55a15fa5961bd498 FOREIGN KEY (parentid) REFERENCES dir_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4326 (class 2606 OID 17560)
-- Name: dir_user_role fk5c5fe738c8fe3ca7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_role
    ADD CONSTRAINT fk5c5fe738c8fe3ca7 FOREIGN KEY (roleid) REFERENCES dir_role(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4327 (class 2606 OID 17565)
-- Name: dir_user_role fk5c5fe738ce539211; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_user_role
    ADD CONSTRAINT fk5c5fe738ce539211 FOREIGN KEY (userid) REFERENCES dir_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4304 (class 2606 OID 17570)
-- Name: app_report_activity fk5e33d79c918f93d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_activity
    ADD CONSTRAINT fk5e33d79c918f93d FOREIGN KEY (processuid) REFERENCES app_report_process(uuid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4295 (class 2606 OID 17575)
-- Name: app_datalist fk5e9247a6462ef4c7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_datalist
    ADD CONSTRAINT fk5e9247a6462ef4c7 FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4302 (class 2606 OID 17580)
-- Name: app_package_participant fk6d7bf59c5f255bcc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_package_participant
    ADD CONSTRAINT fk6d7bf59c5f255bcc FOREIGN KEY (packageid, packageversion) REFERENCES app_package(packageid, packageversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4296 (class 2606 OID 17585)
-- Name: app_env_variable fk740a62ec462ef4c7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_env_variable
    ADD CONSTRAINT fk740a62ec462ef4c7 FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4303 (class 2606 OID 17590)
-- Name: app_plugin_default fk7a835713462ef4c7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_plugin_default
    ADD CONSTRAINT fk7a835713462ef4c7 FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4299 (class 2606 OID 17595)
-- Name: app_package fk852ea428462ef4c7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_package
    ADD CONSTRAINT fk852ea428462ef4c7 FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4305 (class 2606 OID 17600)
-- Name: app_report_activity_instance fk9c6abdd8b06e2043; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_activity_instance
    ADD CONSTRAINT fk9c6abdd8b06e2043 FOREIGN KEY (activityuid) REFERENCES app_report_activity(uuid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4306 (class 2606 OID 17605)
-- Name: app_report_activity_instance fk9c6abdd8d4610a90; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_activity_instance
    ADD CONSTRAINT fk9c6abdd8d4610a90 FOREIGN KEY (processinstanceid) REFERENCES app_report_process_instance(instanceid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4342 (class 2606 OID 17610)
-- Name: shkdeadlines fk_6vyqeugwr76mqc6o26ednljwq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT fk_6vyqeugwr76mqc6o26ednljwq FOREIGN KEY (activity) REFERENCES shkactivities(oid);


--
-- TOC entry 4294 (class 2606 OID 17615)
-- Name: app_builder fk_idup4nrrc79iy4kc46wf5919j; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_builder
    ADD CONSTRAINT fk_idup4nrrc79iy4kc46wf5919j FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion);


--
-- TOC entry 4310 (class 2606 OID 17620)
-- Name: app_resource fk_nnvkg0h6yy8o3f4yjhd20ury0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_resource
    ADD CONSTRAINT fk_nnvkg0h6yy8o3f4yjhd20ury0 FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion);


--
-- TOC entry 4300 (class 2606 OID 17625)
-- Name: app_package_activity_form fka8d741d5f255bcc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_package_activity_form
    ADD CONSTRAINT fka8d741d5f255bcc FOREIGN KEY (packageid, packageversion) REFERENCES app_package(packageid, packageversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4301 (class 2606 OID 17630)
-- Name: app_package_activity_plugin fkade8644c5f255bcc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_package_activity_plugin
    ADD CONSTRAINT fkade8644c5f255bcc FOREIGN KEY (packageid, packageversion) REFERENCES app_package(packageid, packageversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4405 (class 2606 OID 17635)
-- Name: wf_report fkb943cca47a4e8f48; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_report
    ADD CONSTRAINT fkb943cca47a4e8f48 FOREIGN KEY (packageid) REFERENCES wf_report_package(packageid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4406 (class 2606 OID 17640)
-- Name: wf_report fkb943cca4a39d6461; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_report
    ADD CONSTRAINT fkb943cca4a39d6461 FOREIGN KEY (processdefid) REFERENCES wf_report_process(processdefid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4407 (class 2606 OID 17645)
-- Name: wf_report fkb943cca4cb863f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_report
    ADD CONSTRAINT fkb943cca4cb863f FOREIGN KEY (activitydefid) REFERENCES wf_report_activity(activitydefid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4321 (class 2606 OID 17650)
-- Name: dir_grade fkbc9a49a518cebae1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_grade
    ADD CONSTRAINT fkbc9a49a518cebae1 FOREIGN KEY (organizationid) REFERENCES dir_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4322 (class 2606 OID 17655)
-- Name: dir_group fkbc9a804d18cebae1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_group
    ADD CONSTRAINT fkbc9a804d18cebae1 FOREIGN KEY (organizationid) REFERENCES dir_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4307 (class 2606 OID 17660)
-- Name: app_report_package fkbd580a19e475abc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_package
    ADD CONSTRAINT fkbd580a19e475abc FOREIGN KEY (appuid) REFERENCES app_report_app(uuid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4315 (class 2606 OID 17665)
-- Name: dir_employment fkc6620ade14ce02e9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_employment
    ADD CONSTRAINT fkc6620ade14ce02e9 FOREIGN KEY (gradeid) REFERENCES dir_grade(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4316 (class 2606 OID 17670)
-- Name: dir_employment fkc6620ade18cebae1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_employment
    ADD CONSTRAINT fkc6620ade18cebae1 FOREIGN KEY (organizationid) REFERENCES dir_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4317 (class 2606 OID 17675)
-- Name: dir_employment fkc6620ade716ae35f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_employment
    ADD CONSTRAINT fkc6620ade716ae35f FOREIGN KEY (departmentid) REFERENCES dir_department(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4318 (class 2606 OID 17680)
-- Name: dir_employment fkc6620adece539211; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_employment
    ADD CONSTRAINT fkc6620adece539211 FOREIGN KEY (userid) REFERENCES dir_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4308 (class 2606 OID 17685)
-- Name: app_report_process fkdafff442d40695dd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_report_process
    ADD CONSTRAINT fkdafff442d40695dd FOREIGN KEY (packageuid) REFERENCES app_report_package(uuid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4311 (class 2606 OID 17690)
-- Name: app_userview fke411d54e462ef4c7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_userview
    ADD CONSTRAINT fke411d54e462ef4c7 FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4298 (class 2606 OID 17695)
-- Name: app_message fkee346fe9462ef4c7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY app_message
    ADD CONSTRAINT fkee346fe9462ef4c7 FOREIGN KEY (appid, appversion) REFERENCES app_app(appid, appversion) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4312 (class 2606 OID 17700)
-- Name: dir_department fkeee8aa4418cebae1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_department
    ADD CONSTRAINT fkeee8aa4418cebae1 FOREIGN KEY (organizationid) REFERENCES dir_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4313 (class 2606 OID 17705)
-- Name: dir_department fkeee8aa4480db1449; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_department
    ADD CONSTRAINT fkeee8aa4480db1449 FOREIGN KEY (hod) REFERENCES dir_employment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4314 (class 2606 OID 17710)
-- Name: dir_department fkeee8aa44ef6bb2b7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY dir_department
    ADD CONSTRAINT fkeee8aa44ef6bb2b7 FOREIGN KEY (parentid) REFERENCES dir_department(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4408 (class 2606 OID 18118)
-- Name: wf_history_activity fkinow7ia90gehddswt6gtj82xs; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY wf_history_activity
    ADD CONSTRAINT fkinow7ia90gehddswt6gtj82xs FOREIGN KEY (processid) REFERENCES wf_history_process(processid);


--
-- TOC entry 4328 (class 2606 OID 17715)
-- Name: shkactivities shkactivities_process; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_process FOREIGN KEY (process) REFERENCES shkprocesses(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4329 (class 2606 OID 17720)
-- Name: shkactivities shkactivities_state; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_state FOREIGN KEY (state) REFERENCES shkactivitystates(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4330 (class 2606 OID 17725)
-- Name: shkactivities shkactivities_theresource; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_theresource FOREIGN KEY (theresource) REFERENCES shkresourcestable(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4331 (class 2606 OID 17730)
-- Name: shkactivitydata shkactivitydata_activity; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydata
    ADD CONSTRAINT shkactivitydata_activity FOREIGN KEY (activity) REFERENCES shkactivities(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4332 (class 2606 OID 17735)
-- Name: shkactivitydatablobs shkactivitydatablobs_activitydatawob; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydatablobs
    ADD CONSTRAINT shkactivitydatablobs_activitydatawob FOREIGN KEY (activitydatawob) REFERENCES shkactivitydatawob(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4333 (class 2606 OID 17740)
-- Name: shkactivitydatawob shkactivitydatawob_activity; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkactivitydatawob
    ADD CONSTRAINT shkactivitydatawob_activity FOREIGN KEY (activity) REFERENCES shkactivities(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4334 (class 2606 OID 17745)
-- Name: shkandjointable shkandjointable_activity; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_activity FOREIGN KEY (activity) REFERENCES shkactivities(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4335 (class 2606 OID 17750)
-- Name: shkandjointable shkandjointable_blockactivity; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_blockactivity FOREIGN KEY (blockactivity) REFERENCES shkactivities(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4336 (class 2606 OID 17755)
-- Name: shkandjointable shkandjointable_process; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_process FOREIGN KEY (process) REFERENCES shkprocesses(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4337 (class 2606 OID 17760)
-- Name: shkassignmenteventaudits shkassignmenteventaudits_thetype; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkassignmenteventaudits
    ADD CONSTRAINT shkassignmenteventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4338 (class 2606 OID 17765)
-- Name: shkassignmentstable shkassignmentstable_activity; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_activity FOREIGN KEY (activity) REFERENCES shkactivities(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4339 (class 2606 OID 17770)
-- Name: shkassignmentstable shkassignmentstable_theresource; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_theresource FOREIGN KEY (theresource) REFERENCES shkresourcestable(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4340 (class 2606 OID 17775)
-- Name: shkcreateprocesseventaudits shkcreateprocesseventaudits_thetype; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkcreateprocesseventaudits
    ADD CONSTRAINT shkcreateprocesseventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4341 (class 2606 OID 17780)
-- Name: shkdataeventaudits shkdataeventaudits_thetype; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkdataeventaudits
    ADD CONSTRAINT shkdataeventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4343 (class 2606 OID 17785)
-- Name: shkdeadlines shkdeadlines_activity; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_activity FOREIGN KEY (activity) REFERENCES shkactivities(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4344 (class 2606 OID 17790)
-- Name: shkdeadlines shkdeadlines_process; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_process FOREIGN KEY (process) REFERENCES shkprocesses(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4345 (class 2606 OID 17795)
-- Name: shkgroupgrouptable shkgroupgrouptable_groupid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_groupid FOREIGN KEY (groupid) REFERENCES shkgrouptable(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4346 (class 2606 OID 17800)
-- Name: shkgroupgrouptable shkgroupgrouptable_sub_gid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_sub_gid FOREIGN KEY (sub_gid) REFERENCES shkgrouptable(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4347 (class 2606 OID 17805)
-- Name: shkgroupuserpacklevelpart shkgroupuserpacklevelpart_participantoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkpacklevelparticipant(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4348 (class 2606 OID 17810)
-- Name: shkgroupuserpacklevelpart shkgroupuserpacklevelpart_useroid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_useroid FOREIGN KEY (useroid) REFERENCES shkgroupuser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4349 (class 2606 OID 17815)
-- Name: shkgroupuserproclevelpart shkgroupuserproclevelpart_participantoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkproclevelparticipant(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4350 (class 2606 OID 17820)
-- Name: shkgroupuserproclevelpart shkgroupuserproclevelpart_useroid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_useroid FOREIGN KEY (useroid) REFERENCES shkgroupuser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4351 (class 2606 OID 17825)
-- Name: shkneweventauditdata shkneweventauditdata_dataeventaudit; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdata
    ADD CONSTRAINT shkneweventauditdata_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4352 (class 2606 OID 17830)
-- Name: shkneweventauditdatablobs shkneweventauditdatablobs_neweventauditdatawob; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdatablobs
    ADD CONSTRAINT shkneweventauditdatablobs_neweventauditdatawob FOREIGN KEY (neweventauditdatawob) REFERENCES shkneweventauditdatawob(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4353 (class 2606 OID 17835)
-- Name: shkneweventauditdatawob shkneweventauditdatawob_dataeventaudit; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkneweventauditdatawob
    ADD CONSTRAINT shkneweventauditdatawob_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4354 (class 2606 OID 17840)
-- Name: shkoldeventauditdata shkoldeventauditdata_dataeventaudit; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdata
    ADD CONSTRAINT shkoldeventauditdata_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4355 (class 2606 OID 17845)
-- Name: shkoldeventauditdatablobs shkoldeventauditdatablobs_oldeventauditdatawob; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdatablobs
    ADD CONSTRAINT shkoldeventauditdatablobs_oldeventauditdatawob FOREIGN KEY (oldeventauditdatawob) REFERENCES shkoldeventauditdatawob(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4356 (class 2606 OID 17850)
-- Name: shkoldeventauditdatawob shkoldeventauditdatawob_dataeventaudit; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkoldeventauditdatawob
    ADD CONSTRAINT shkoldeventauditdatawob_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4357 (class 2606 OID 17855)
-- Name: shkpacklevelparticipant shkpacklevelparticipant_packageoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelparticipant
    ADD CONSTRAINT shkpacklevelparticipant_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlparticipantpackage(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4358 (class 2606 OID 17860)
-- Name: shkpacklevelxpdlapp shkpacklevelxpdlapp_packageoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapp
    ADD CONSTRAINT shkpacklevelxpdlapp_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlapplicationpackage(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4359 (class 2606 OID 17865)
-- Name: shkpacklevelxpdlapptaappdetail shkpacklevelxpdlapptaappdetail_toolagentoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetail(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4360 (class 2606 OID 17870)
-- Name: shkpacklevelxpdlapptaappdetail shkpacklevelxpdlapptaappdetail_xpdl_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4361 (class 2606 OID 17875)
-- Name: shkpacklevelxpdlapptaappdetusr shkpacklevelxpdlapptaappdetusr_toolagentoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetailuser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4362 (class 2606 OID 17880)
-- Name: shkpacklevelxpdlapptaappdetusr shkpacklevelxpdlapptaappdetusr_xpdl_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4363 (class 2606 OID 17885)
-- Name: shkpacklevelxpdlapptaappuser shkpacklevelxpdlapptaappuser_toolagentoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappuser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4364 (class 2606 OID 17890)
-- Name: shkpacklevelxpdlapptaappuser shkpacklevelxpdlapptaappuser_xpdl_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4365 (class 2606 OID 17895)
-- Name: shkpacklevelxpdlapptoolagntapp shkpacklevelxpdlapptoolagntapp_toolagentoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4366 (class 2606 OID 17900)
-- Name: shkpacklevelxpdlapptoolagntapp shkpacklevelxpdlapptoolagntapp_xpdl_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4367 (class 2606 OID 17905)
-- Name: shkprocessdata shkprocessdata_process; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdata
    ADD CONSTRAINT shkprocessdata_process FOREIGN KEY (process) REFERENCES shkprocesses(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4368 (class 2606 OID 17910)
-- Name: shkprocessdatablobs shkprocessdatablobs_processdatawob; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdatablobs
    ADD CONSTRAINT shkprocessdatablobs_processdatawob FOREIGN KEY (processdatawob) REFERENCES shkprocessdatawob(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4369 (class 2606 OID 17915)
-- Name: shkprocessdatawob shkprocessdatawob_process; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessdatawob
    ADD CONSTRAINT shkprocessdatawob_process FOREIGN KEY (process) REFERENCES shkprocesses(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4370 (class 2606 OID 17920)
-- Name: shkprocesses shkprocesses_processdefinition; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_processdefinition FOREIGN KEY (processdefinition) REFERENCES shkprocessdefinitions(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4371 (class 2606 OID 17925)
-- Name: shkprocesses shkprocesses_state; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_state FOREIGN KEY (state) REFERENCES shkprocessstates(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4372 (class 2606 OID 17930)
-- Name: shkprocessrequesters shkprocessrequesters_activityrequester; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_activityrequester FOREIGN KEY (activityrequester) REFERENCES shkactivities(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4373 (class 2606 OID 17935)
-- Name: shkprocessrequesters shkprocessrequesters_resourcerequester; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_resourcerequester FOREIGN KEY (resourcerequester) REFERENCES shkresourcestable(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4374 (class 2606 OID 17940)
-- Name: shkproclevelparticipant shkproclevelparticipant_processoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelparticipant
    ADD CONSTRAINT shkproclevelparticipant_processoid FOREIGN KEY (processoid) REFERENCES shkxpdlparticipantprocess(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4375 (class 2606 OID 17945)
-- Name: shkproclevelxpdlapp shkproclevelxpdlapp_processoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapp
    ADD CONSTRAINT shkproclevelxpdlapp_processoid FOREIGN KEY (processoid) REFERENCES shkxpdlapplicationprocess(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4376 (class 2606 OID 17950)
-- Name: shkproclevelxpdlapptaappdetail shkproclevelxpdlapptaappdetail_toolagentoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetail(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4377 (class 2606 OID 17955)
-- Name: shkproclevelxpdlapptaappdetail shkproclevelxpdlapptaappdetail_xpdl_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4378 (class 2606 OID 17960)
-- Name: shkproclevelxpdlapptaappdetusr shkproclevelxpdlapptaappdetusr_toolagentoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetailuser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4379 (class 2606 OID 17965)
-- Name: shkproclevelxpdlapptaappdetusr shkproclevelxpdlapptaappdetusr_xpdl_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4380 (class 2606 OID 17970)
-- Name: shkproclevelxpdlapptaappuser shkproclevelxpdlapptaappuser_toolagentoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappuser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4381 (class 2606 OID 17975)
-- Name: shkproclevelxpdlapptaappuser shkproclevelxpdlapptaappuser_xpdl_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4382 (class 2606 OID 17980)
-- Name: shkproclevelxpdlapptoolagntapp shkproclevelxpdlapptoolagntapp_toolagentoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4383 (class 2606 OID 17985)
-- Name: shkproclevelxpdlapptoolagntapp shkproclevelxpdlapptoolagntapp_xpdl_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4384 (class 2606 OID 17990)
-- Name: shkstateeventaudits shkstateeventaudits_newactivitystate; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_newactivitystate FOREIGN KEY (newactivitystate) REFERENCES shkactivitystateeventaudits(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4385 (class 2606 OID 17995)
-- Name: shkstateeventaudits shkstateeventaudits_newprocessstate; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_newprocessstate FOREIGN KEY (newprocessstate) REFERENCES shkprocessstateeventaudits(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4386 (class 2606 OID 18000)
-- Name: shkstateeventaudits shkstateeventaudits_oldactivitystate; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_oldactivitystate FOREIGN KEY (oldactivitystate) REFERENCES shkactivitystateeventaudits(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4387 (class 2606 OID 18005)
-- Name: shkstateeventaudits shkstateeventaudits_oldprocessstate; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_oldprocessstate FOREIGN KEY (oldprocessstate) REFERENCES shkprocessstateeventaudits(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4388 (class 2606 OID 18010)
-- Name: shkstateeventaudits shkstateeventaudits_thetype; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4389 (class 2606 OID 18015)
-- Name: shktoolagentappdetail shktoolagentappdetail_toolagent_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappdetail
    ADD CONSTRAINT shktoolagentappdetail_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4390 (class 2606 OID 18020)
-- Name: shktoolagentappdetailuser shktoolagentappdetailuser_toolagent_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentappdetail(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4391 (class 2606 OID 18025)
-- Name: shktoolagentappdetailuser shktoolagentappdetailuser_useroid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_useroid FOREIGN KEY (useroid) REFERENCES shktoolagentuser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4392 (class 2606 OID 18030)
-- Name: shktoolagentappuser shktoolagentappuser_toolagent_appoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentapp(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4393 (class 2606 OID 18035)
-- Name: shktoolagentappuser shktoolagentappuser_useroid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_useroid FOREIGN KEY (useroid) REFERENCES shktoolagentuser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4394 (class 2606 OID 18040)
-- Name: shkusergrouptable shkusergrouptable_groupid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_groupid FOREIGN KEY (groupid) REFERENCES shkgrouptable(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4395 (class 2606 OID 18045)
-- Name: shkusergrouptable shkusergrouptable_userid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_userid FOREIGN KEY (userid) REFERENCES shkusertable(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4396 (class 2606 OID 18050)
-- Name: shkuserpacklevelpart shkuserpacklevelpart_participantoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkpacklevelparticipant(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4397 (class 2606 OID 18055)
-- Name: shkuserpacklevelpart shkuserpacklevelpart_useroid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_useroid FOREIGN KEY (useroid) REFERENCES shknormaluser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4398 (class 2606 OID 18060)
-- Name: shkuserproclevelparticipant shkuserproclevelparticipant_participantoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_participantoid FOREIGN KEY (participantoid) REFERENCES shkproclevelparticipant(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4399 (class 2606 OID 18065)
-- Name: shkuserproclevelparticipant shkuserproclevelparticipant_useroid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_useroid FOREIGN KEY (useroid) REFERENCES shknormaluser(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4400 (class 2606 OID 18070)
-- Name: shkxpdlapplicationprocess shkxpdlapplicationprocess_packageoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlapplicationprocess
    ADD CONSTRAINT shkxpdlapplicationprocess_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlapplicationpackage(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4401 (class 2606 OID 18075)
-- Name: shkxpdldata shkxpdldata_xpdl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdldata
    ADD CONSTRAINT shkxpdldata_xpdl FOREIGN KEY (xpdl) REFERENCES shkxpdls(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4402 (class 2606 OID 18080)
-- Name: shkxpdlhistorydata shkxpdlhistorydata_xpdlhistory; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlhistorydata
    ADD CONSTRAINT shkxpdlhistorydata_xpdlhistory FOREIGN KEY (xpdlhistory) REFERENCES shkxpdlhistory(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4403 (class 2606 OID 18085)
-- Name: shkxpdlparticipantprocess shkxpdlparticipantprocess_packageoid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlparticipantprocess
    ADD CONSTRAINT shkxpdlparticipantprocess_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlparticipantpackage(oid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 4404 (class 2606 OID 18090)
-- Name: shkxpdlreferences shkxpdlreferences_referringxpdl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY shkxpdlreferences
    ADD CONSTRAINT shkxpdlreferences_referringxpdl FOREIGN KEY (referringxpdl) REFERENCES shkxpdls(oid) DEFERRABLE INITIALLY DEFERRED;


-- Completed on 2022-11-24 03:57:59 UTC

--
-- PostgreSQL database dump complete
--

