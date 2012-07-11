<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Viewer test</title>
    </head>
    <body>
        <h1>Viewer Test</h1>

        <form method="POST" action="viewer.jsp">
            <dl>
                <dt>
                    xpdl
                </dt>
                <dd>
                    <textarea name="xpdl" rows="20" cols="80">
<?xml version="1.0" encoding="UTF-8"?>
<xpdl:Package xmlns="http://www.wfmc.org/2002/XPDL1.0" xmlns:xpdl="http://www.wfmc.org/2002/XPDL1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" Id="sample_processes" Name="Sample Processes" xsi:schemaLocation="http://www.wfmc.org/2002/XPDL1.0 http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd">
    <xpdl:PackageHeader>
        <xpdl:XPDLVersion>1.0</xpdl:XPDLVersion>
        <xpdl:Vendor>Together</xpdl:Vendor>
        <xpdl:Created>2008-04-30 18:10:46</xpdl:Created>
    </xpdl:PackageHeader>
    <xpdl:Script Type="text/javascript"/>
    <xpdl:Participants>
        <xpdl:Participant Id="requester" Name="Requester">
            <xpdl:ParticipantType Type="ROLE"/>
        </xpdl:Participant>
        <xpdl:Participant Id="approver" Name="Approver">
            <xpdl:ParticipantType Type="ROLE"/>
        </xpdl:Participant>
        <xpdl:Participant Id="escalation_manager" Name="Escalation Manager">
            <xpdl:ParticipantType Type="ROLE"/>
        </xpdl:Participant>
        <xpdl:Participant Id="escalation_assignee" Name="Escalation Assignee">
            <xpdl:ParticipantType Type="ROLE"/>
        </xpdl:Participant>
        <xpdl:Participant Id="system" Name="System">
            <xpdl:ParticipantType Type="SYSTEM"/>
        </xpdl:Participant>
    </xpdl:Participants>
    <xpdl:Applications>
        <xpdl:Application Id="default_application"/>
    </xpdl:Applications>
    <xpdl:DataFields>
        <xpdl:DataField Id="status" IsArray="FALSE" Name="Status">
            <xpdl:DataType>
                <xpdl:BasicType Type="STRING"/>
            </xpdl:DataType>
        </xpdl:DataField>
        <xpdl:DataField Id="id" IsArray="FALSE" Name="ID">
            <xpdl:DataType>
                <xpdl:BasicType Type="STRING"/>
            </xpdl:DataType>
        </xpdl:DataField>
        <xpdl:DataField Id="escalation_assignee" IsArray="FALSE" Name="Escalation Assignee">
            <xpdl:DataType>
                <xpdl:BasicType Type="STRING"/>
            </xpdl:DataType>
        </xpdl:DataField>
        <xpdl:DataField Id="escalation_manager" IsArray="FALSE" Name="Escalation Manager">
            <xpdl:DataType>
                <xpdl:BasicType Type="STRING"/>
            </xpdl:DataType>
        </xpdl:DataField>
        <xpdl:DataField Id="approver" IsArray="FALSE" Name="Approver">
            <xpdl:DataType>
                <xpdl:BasicType Type="STRING"/>
            </xpdl:DataType>
        </xpdl:DataField>
    </xpdl:DataFields>
    <xpdl:WorkflowProcesses>
        <xpdl:WorkflowProcess Id="sample_approval" Name="Sample Approval Process">
            <xpdl:ProcessHeader>
                <xpdl:Created>2009-04-07 11:45:43</xpdl:Created>
            </xpdl:ProcessHeader>
            <xpdl:FormalParameters>
                <xpdl:FormalParameter Id="escalate_status" Mode="INOUT">
                    <xpdl:DataType>
                        <xpdl:BasicType Type="STRING"/>
                    </xpdl:DataType>
                </xpdl:FormalParameter>
            </xpdl:FormalParameters>
            <xpdl:Activities>
                <xpdl:Activity Id="submit_request" Name="Submit Request">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>requester</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="requester"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="301,21"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="approve_request" Name="Approve Request">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>approver</xpdl:Performer>
                    <xpdl:Deadline Execution="ASYNCHR">
                        <xpdl:DeadlineCondition>var d=new java.util.Date(); d.setTime(ACTIVITY_ACTIVATED_TIME.getTime()+600000); d;</xpdl:DeadlineCondition>
                        <xpdl:ExceptionName>TIMEOUT</xpdl:ExceptionName>
                    </xpdl:Deadline>
                    <xpdl:TransitionRestrictions>
                        <xpdl:TransitionRestriction>
                            <xpdl:Split Type="XOR">
                                <xpdl:TransitionRefs>
                                    <xpdl:TransitionRef Id="sample_approval_tra4"/>
                                    <xpdl:TransitionRef Id="sample_approval_tra14"/>
                                </xpdl:TransitionRefs>
                            </xpdl:Split>
                        </xpdl:TransitionRestriction>
                    </xpdl:TransitionRestrictions>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="441,21"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="route_approval_decision">
                    <xpdl:Route/>
                    <xpdl:TransitionRestrictions>
                        <xpdl:TransitionRestriction>
                            <xpdl:Split Type="XOR">
                                <xpdl:TransitionRefs>
                                    <xpdl:TransitionRef Id="sample_approval_tra2"/>
                                    <xpdl:TransitionRef Id="sample_approval_tra6"/>
                                    <xpdl:TransitionRef Id="sample_approval_tra7"/>
                                </xpdl:TransitionRefs>
                            </xpdl:Split>
                        </xpdl:TransitionRestriction>
                    </xpdl:TransitionRestrictions>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="800,61"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="incomplete_request" Name="Update Incomplete Request">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>requester</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="requester"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="799,21"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="subflow_escalate" Name="Escalate Subflow">
                    <xpdl:Implementation>
                        <xpdl:SubFlow Execution="SYNCHR" Id="sample_escalation">
                            <xpdl:ActualParameters>
                                <xpdl:ActualParameter>status</xpdl:ActualParameter>
                                <xpdl:ActualParameter>escalation_manager</xpdl:ActualParameter>
                                <xpdl:ActualParameter>id</xpdl:ActualParameter>
                            </xpdl:ActualParameters>
                        </xpdl:SubFlow>
                    </xpdl:Implementation>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="716,50"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="close_request" Name="Close Request">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>approver</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="995,29"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="insert_db" Name="Insert DB (draft)">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="173,157"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="generate_id" Name="Generate ID">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="173,77"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="route_start">
                    <xpdl:Route/>
                    <xpdl:TransitionRestrictions>
                        <xpdl:TransitionRestriction>
                            <xpdl:Split Type="AND">
                                <xpdl:TransitionRefs>
                                    <xpdl:TransitionRef Id="sample_approval_tra8"/>
                                    <xpdl:TransitionRef Id="sample_approval_tra5"/>
                                </xpdl:TransitionRefs>
                            </xpdl:Split>
                        </xpdl:TransitionRestriction>
                    </xpdl:TransitionRestrictions>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="requester"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="177,15"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="update_db_new" Name="Update DB (new)">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:TransitionRestrictions>
                        <xpdl:TransitionRestriction>
                            <xpdl:Join Type="XOR"/>
                        </xpdl:TransitionRestriction>
                    </xpdl:TransitionRestrictions>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="352,53"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="update_db" Name="Update DB">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:TransitionRestrictions>
                        <xpdl:TransitionRestriction>
                            <xpdl:Join Type="XOR"/>
                        </xpdl:TransitionRestriction>
                    </xpdl:TransitionRestrictions>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="670,63"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="complete_db" Name="Complete DB">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="1022,204"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="send_reminder" Name="Send Reminder">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="558,234"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="sample_approval_act1" Name="Update Escalate Manager">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="799,242"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
            </xpdl:Activities>
            <xpdl:Transitions>
                <xpdl:Transition From="update_db_new" Id="sample_approval_tra1" To="approve_request">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="route_approval_decision" Id="sample_approval_tra2" To="incomplete_request">
                    <xpdl:Condition>status=='incomplete'</xpdl:Condition>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="incomplete_request" Id="sample_approval_tra3" To="update_db_new">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="approve_request" Id="sample_approval_tra4" To="update_db">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_BREAK_POINTS" Value="509,240"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="route_approval_decision" Id="sample_approval_tra6" To="sample_approval_act1">
                    <xpdl:Condition Type="CONDITION">status=='escalate'</xpdl:Condition>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="route_approval_decision" Id="sample_approval_tra7" Name="resolved" To="close_request">
                    <xpdl:Condition Type="OTHERWISE"/>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="route_start" Id="sample_approval_tra5" To="submit_request">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="route_start" Id="sample_approval_tra8" To="generate_id">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_BREAK_POINTS" Value="214,189"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="generate_id" Id="sample_approval_tra9" To="insert_db">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="submit_request" Id="sample_approval_tra10" To="update_db_new">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="update_db" Id="sample_approval_tra11" To="route_approval_decision">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="subflow_escalate" Id="sample_approval_tra12" To="update_db">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="close_request" Id="sample_approval_tra13" To="complete_db">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="approve_request" Id="sample_approval_tra14" To="send_reminder">
                    <xpdl:Condition Type="EXCEPTION">TIMEOUT</xpdl:Condition>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_BREAK_POINTS" Value="598,502"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="sample_approval_act1" Id="sample_approval_tra15" To="subflow_escalate">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_BREAK_POINTS" Value="840,674"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
            </xpdl:Transitions>
            <xpdl:ExtendedAttributes>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER" Value="requester;system;approver;escalation_manager"/>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_START_OF_WORKFLOW" Value="JaWE_GRAPH_PARTICIPANT_ID=requester,CONNECTING_ACTIVITY_ID=route_start,X_OFFSET=75,Y_OFFSET=26,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULT"/>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_END_OF_WORKFLOW" Value="JaWE_GRAPH_PARTICIPANT_ID=system,CONNECTING_ACTIVITY_ID=send_reminder,X_OFFSET=669,Y_OFFSET=247,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT"/>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_END_OF_WORKFLOW" Value="JaWE_GRAPH_PARTICIPANT_ID=system,CONNECTING_ACTIVITY_ID=complete_db,X_OFFSET=1160,Y_OFFSET=217,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT"/>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_END_OF_WORKFLOW" Value="JaWE_GRAPH_PARTICIPANT_ID=system,CONNECTING_ACTIVITY_ID=insert_db,X_OFFSET=201,Y_OFFSET=237,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT"/>
            </xpdl:ExtendedAttributes>
        </xpdl:WorkflowProcess>
        <xpdl:WorkflowProcess Id="sample_escalation" Name="Sample Escalation Process">
            <xpdl:ProcessHeader>
                <xpdl:Created>2009-04-07 11:52:56</xpdl:Created>
            </xpdl:ProcessHeader>
            <xpdl:FormalParameters>
                <xpdl:FormalParameter Id="escalate_status" Mode="INOUT">
                    <xpdl:DataType>
                        <xpdl:BasicType Type="STRING"/>
                    </xpdl:DataType>
                </xpdl:FormalParameter>
                <xpdl:FormalParameter Id="escalation_manager" Mode="INOUT">
                    <xpdl:DataType>
                        <xpdl:BasicType Type="STRING"/>
                    </xpdl:DataType>
                </xpdl:FormalParameter>
                <xpdl:FormalParameter Id="id" Mode="IN">
                    <xpdl:DataType>
                        <xpdl:BasicType Type="STRING"/>
                    </xpdl:DataType>
                </xpdl:FormalParameter>
            </xpdl:FormalParameters>
            <xpdl:Activities>
                <xpdl:Activity Id="resolve_escalation_request" Name="Resolve Escalation Request">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>escalation_manager</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="257,16"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalate_status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="route_escalation_decision">
                    <xpdl:Route/>
                    <xpdl:TransitionRestrictions>
                        <xpdl:TransitionRestriction>
                            <xpdl:Join Type="XOR"/>
                            <xpdl:Split Type="XOR">
                                <xpdl:TransitionRefs>
                                    <xpdl:TransitionRef Id="sample_escalation_tra3"/>
                                    <xpdl:TransitionRef Id="sample_escalation_tra2"/>
                                    <xpdl:TransitionRef Id="sample_escalation_tra5"/>
                                </xpdl:TransitionRefs>
                            </xpdl:Split>
                        </xpdl:TransitionRestriction>
                    </xpdl:TransitionRestrictions>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="389,17"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="verify_escalation_request" Name="Verify Escalation Request">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>escalation_manager</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="809,188"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalate_status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="update_assigned_escalation_request" Name="Update Assigned Escalation Request">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>escalation_assignee</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="676,52"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalate_status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="route_resolved">
                    <xpdl:Route/>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="633,13"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="assign_escalation_request" Name="Assign Escalation Request">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>escalation_manager</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="513,198"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="sample_escalation_act1" Name="Update Escalate Assignee">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="587,55"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="sample_escalation_act2" Name="Update SubFlow Process ID">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="149,48"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="sample_escalation_act3" Name="Update DB (verify)">
                    <xpdl:Implementation>
                        <xpdl:Tool Id="default_application"/>
                    </xpdl:Implementation>
                    <xpdl:Performer>system</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="system"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="748,52"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalate_status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
                <xpdl:Activity Id="sample_escalation_act4" Name="Perform Escalation">
                    <xpdl:Implementation>
                        <xpdl:No/>
                    </xpdl:Implementation>
                    <xpdl:Performer>escalation_manager</xpdl:Performer>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="282,196"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_assignee"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="approver"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalate_status"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_UPDATE" Value="escalation_manager"/>
                        <xpdl:ExtendedAttribute Name="VariableToProcess_VIEW" Value="id"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Activity>
            </xpdl:Activities>
            <xpdl:Transitions>
                <xpdl:Transition From="resolve_escalation_request" Id="sample_escalation_tra1" To="route_escalation_decision">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="route_escalation_decision" Id="sample_escalation_tra2" Name="resolved" To="route_resolved">
                    <xpdl:Condition Type="OTHERWISE"/>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="route_escalation_decision" Id="sample_escalation_tra3" To="assign_escalation_request">
                    <xpdl:Condition>escalate_status=='assign'</xpdl:Condition>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="route_escalation_decision" Id="sample_escalation_tra5" To="sample_escalation_act4">
                    <xpdl:Condition>escalate_status=='escalate'</xpdl:Condition>
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="update_assigned_escalation_request" Id="sample_escalation_tra4" To="sample_escalation_act3">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="verify_escalation_request" Id="sample_escalation_tra6" To="route_escalation_decision">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="assign_escalation_request" Id="sample_escalation_tra7" To="sample_escalation_act1">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="sample_escalation_act1" Id="sample_escalation_tra8" To="update_assigned_escalation_request">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="sample_escalation_act2" Id="sample_escalation_tra9" To="resolve_escalation_request">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_BREAK_POINTS" Value="188,44"/>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
                <xpdl:Transition From="sample_escalation_act3" Id="sample_escalation_tra10" To="verify_escalation_request">
                    <xpdl:ExtendedAttributes>
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_SPLINE"/>
                    </xpdl:ExtendedAttributes>
                </xpdl:Transition>
            </xpdl:Transitions>
            <xpdl:ExtendedAttributes>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER" Value="escalation_manager;system;escalation_assignee"/>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_END_OF_WORKFLOW" Value="JaWE_GRAPH_PARTICIPANT_ID=escalation_manager,CONNECTING_ACTIVITY_ID=route_resolved,X_OFFSET=768,Y_OFFSET=26,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT"/>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_START_OF_WORKFLOW" Value="JaWE_GRAPH_PARTICIPANT_ID=system,CONNECTING_ACTIVITY_ID=sample_escalation_act2,X_OFFSET=73,Y_OFFSET=57,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULT"/>
                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_END_OF_WORKFLOW" Value="JaWE_GRAPH_PARTICIPANT_ID=escalation_manager,CONNECTING_ACTIVITY_ID=sample_escalation_act4,X_OFFSET=435,Y_OFFSET=208,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=END_DEFAULT"/>
            </xpdl:ExtendedAttributes>
        </xpdl:WorkflowProcess>
    </xpdl:WorkflowProcesses>
    <xpdl:ExtendedAttributes>
        <xpdl:ExtendedAttribute Name="JaWE_CONFIGURATION" Value="default"/>
        <xpdl:ExtendedAttribute Name="EDITING_TOOL" Value="Workflow Designer"/>
        <xpdl:ExtendedAttribute Name="EDITING_TOOL_VERSION" Value="2.0-2(4?)-C-20080226-2126"/>
    </xpdl:ExtendedAttributes>
</xpdl:Package>
                    </textarea>
                </dd>
                <dt>
                    packageId
                </dt>
                <dd>
                    <input name="packageId" value="sample_processes" />
                </dd>
                <dt>
                    processId
                </dt>
                <dd>
                    <input name="processId" value="sample_approval" />
                </dd>
                <dt>
                    activityId 1
                </dt>
                <dd>
                    <input name="activityId" value="approve_request" />
                </dd>
                <dt>
                    activityId 2
                </dt>
                <dd>
                    <input name="activityId" value="send_reminder" />
                </dd>
                <dt>
                    &nbsp;
                </dt>
                <dd>
                    <input type="submit" value="Generate JPEG" />
                </dd>
            </dl>


        </form>


    </body>
</html>
