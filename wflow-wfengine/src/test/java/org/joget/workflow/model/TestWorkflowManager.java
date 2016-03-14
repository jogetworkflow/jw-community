package org.joget.workflow.model;

import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.service.WorkflowManager;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:wfengineApplicationContext.xml"})
public class TestWorkflowManager {

    public TestWorkflowManager() {
    }
    @Autowired
    WorkflowManager workflowManager;

    String packageId = "workflow_patterns";
    String processId = "WfBCP1_Sequence";
    String xpdl = "/workflow_patterns.xpdl";

    @Test
    @Transactional
    @Rollback(false)
    public void suite() throws FileNotFoundException, IOException, Exception{
        try {
            testCloseAndRemovePackage();
            testUploadProcess();
            testStartProcess();
            testPendingA();
            testAssignment();
            testAcceptedA();
            testStartActivityC();
            testStartProcessWithLinking();
            testCopyProcess();
        }
        finally {
            testCloseAndRemovePackage();
        }
    }
    
    public void testUploadProcess() throws FileNotFoundException, IOException, Exception {
        LogUtil.info(getClass().getName(), ">>> testUploadProcess");

        BufferedReader reader = null;
        String fileContents = "";
        String line;

        try {
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(xpdl)));
            while ((line = reader.readLine()) != null) {
                fileContents += line + "\n";
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        byte[] processDefinitionData = fileContents.getBytes();
        workflowManager.processUpload(null, processDefinitionData);
    }

    public void testStartProcess(){
        LogUtil.info(getClass().getName(), ">>> testStartProcess");
        String packageVersion = workflowManager.getCurrentPackageVersion(packageId);
        workflowManager.processStart(packageId+"#"+packageVersion+"#"+processId);
        }

    public void testPendingA(){
        LogUtil.info(getClass().getName(), ">>> testPendingA");
        Map activityInstance = workflowManager.getActivityInstanceByProcessIdAndStatus(processId, false);
        workflowManager.assignmentAccept(String.valueOf(activityInstance.get("A")));
    }

    public void testAssignment(){
        LogUtil.info(getClass().getName(), ">>> testAssignment");
        Map activityInstance = workflowManager.getActivityInstanceByProcessIdAndStatus(processId, true);
        String activityId = String.valueOf(activityInstance.get("A"));
        WorkflowActivity wa = workflowManager.getActivityById(activityId);
        String processInstanceId = wa.getProcessId();
        WorkflowAssignment ass = workflowManager.getAssignmentByProcess(processInstanceId);
        WorkflowAssignment ass2 = workflowManager.getAssignment(activityId);
        Assert.assertTrue(ass != null && ass2 != null && ass.getActivityId().equals(ass2.getActivityId()));
    }

    public void testAcceptedA(){
        LogUtil.info(getClass().getName(), ">>> testAcceptedA");
        Map activityInstance = workflowManager.getActivityInstanceByProcessIdAndStatus(processId, true);
        workflowManager.assignmentComplete(String.valueOf(activityInstance.get("A")));
    }

    public void testStartActivityC() {
        LogUtil.info(getClass().getName(), ">>> testStartActivityC");

        String currentActivityDef = "B";
        String desiredActivityDef = "C";

        // get process instance
        Map runningActivities = workflowManager.getActivityInstanceByProcessIdAndStatus(processId, null);
        String activityId = String.valueOf(runningActivities.get(currentActivityDef));
        WorkflowActivity wa = workflowManager.getActivityById(activityId);
        String processInstanceId = wa.getProcessId();

        // abort running activities and start activity C
        boolean started = workflowManager.activityStart(processInstanceId, desiredActivityDef, true);

        // check running activities
        runningActivities = workflowManager.getActivityInstanceByProcessIdAndStatus(processId, null);
        String abortedActivity = (String)runningActivities.get(currentActivityDef);
        String runningActivity = (String)runningActivities.get(desiredActivityDef);
        LogUtil.info(getClass().getName(), "Running activities: " + runningActivities + "; Result: " + started);

        Assert.assertTrue(abortedActivity == null && runningActivity != null);
    }

    public void testStartProcessWithLinking(){
        LogUtil.info(getClass().getName(), ">>> testStartProcessWithLinking");

        //start and get instant id of 1st process
        String packageVersion = workflowManager.getCurrentPackageVersion(packageId);
        WorkflowProcessResult result = workflowManager.processStart(packageId+"#"+packageVersion+"#"+processId);
        String process1Id = result.getProcess().getInstanceId();
        LogUtil.info(getClass().getName(), "-------------  process one id : " + process1Id + "  -------------");

        //start 2nd process with 1st process instant id and get 2nd process instant id
        WorkflowProcessResult nextResult = workflowManager.processStartWithLinking(packageId+"#"+packageVersion+"#"+processId, null, null, process1Id);
        String process2Id = nextResult.getProcess().getInstanceId();
        LogUtil.info(getClass().getName(), "-------------  process two id : " + process2Id + "  -------------");

        //check process linking data is correct or not
        WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(process2Id);
        LogUtil.info(getClass().getName(), "-------------  origin process id : " + link.getOriginProcessId() + "  -------------");
        workflowManager.internalDeleteWorkflowProcessLink(link);
        Assert.assertNotNull(link);
        Assert.assertTrue(process1Id.equals(link.getOriginProcessId()) && process1Id.equals(link.getParentProcessId()));
    }

    public void testCopyProcess() {
        LogUtil.info(getClass().getName(), ">>> testCopyProcess");

        boolean valid = false;

        // start and get instance id of the 1st process
        String packageVersion = workflowManager.getCurrentPackageVersion(packageId);
        WorkflowProcessResult result = workflowManager.processStart(packageId+"#"+packageVersion+"#"+processId);
        String processInstanceId = result.getProcess().getInstanceId();
        LogUtil.info(getClass().getName(), "-------------  process one id : " + processInstanceId + "  -------------");

        // abort running activities and start activity B
        String firstActivityDef = "A";
        String desiredActivityDef = "B";
        boolean started = workflowManager.activityStart(processInstanceId, desiredActivityDef, true);

        if (started) {
            // start 2nd process from the 1st process instance id
            WorkflowProcessResult nextResult = workflowManager.processCopyFromInstanceId(processInstanceId, packageId+"#"+packageVersion+"#"+processId, true);
            WorkflowProcess processStarted = nextResult.getProcess();

            if (processStarted != null) {
                // check for the aborted and running activities
                String newProcessId = processStarted.getInstanceId();
                Collection<WorkflowActivity> activityList = workflowManager.getActivityList(newProcessId, 0, 1000, null, null);
                for (WorkflowActivity act: activityList) {
                    if (act.getState().startsWith("open")) {
                        if (firstActivityDef.equals(act.getActivityDefId())) {
                            valid = false;
                            break;
                        }
                        if (desiredActivityDef.equals(act.getActivityDefId())) {
                            valid = true;
                        }
                    }
                }
                LogUtil.info(getClass().getName(), "-------------  new process id : " + newProcessId + "  ------------- " + valid);

                // cleanup
                WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(newProcessId);
                workflowManager.internalDeleteWorkflowProcessLink(link);
            }
        }

        Assert.assertTrue(valid);
    }

    public void testCloseAndRemovePackage(){
        LogUtil.info(getClass().getName(), ">>> testCloseAndRemovePackage");
        Collection<WorkflowProcess> processList = workflowManager.getRunningProcessList(packageId, null, null, null, null, null, 0, 100);

        for(WorkflowProcess process : processList) workflowManager.removeProcessInstance(process.getInstanceId());

        workflowManager.processDeleteAndUnload(packageId);
    }
}
