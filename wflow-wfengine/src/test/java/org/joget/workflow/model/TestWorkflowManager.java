package org.joget.workflow.model;

import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.service.WorkflowManager;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import org.joget.workflow.shark.model.dao.WorkflowAssignmentDao;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testWfengineApplicationContext.xml"})
public class TestWorkflowManager {

    public TestWorkflowManager() {
    }
    @Autowired
    WorkflowManager workflowManager;
    
    @Autowired
    WorkflowAssignmentDao workflowAssignmentDao;

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
            testGetAssignments();
        }
        finally {
            testCloseAndRemovePackage();
        }
    }
    
    public void testUploadProcess() throws FileNotFoundException, IOException, Exception {
        System.out.println(">>> testUploadProcess");

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
        System.out.println(">>> testStartProcess");
        String packageVersion = workflowManager.getCurrentPackageVersion(packageId);
        workflowManager.processStart(packageId+"#"+packageVersion+"#"+processId);
    }

    public void testPendingA(){
        System.out.println(">>> testPendingA");
        Map activityInstance = workflowManager.getActivityInstanceByProcessIdAndStatus(processId, false);
        workflowManager.assignmentAccept(String.valueOf(activityInstance.get("A")));
    }

    public void testAssignment(){
        System.out.println(">>> testAssignment");
        Map activityInstance = workflowManager.getActivityInstanceByProcessIdAndStatus(processId, true);
        String activityId = String.valueOf(activityInstance.get("A"));
        WorkflowActivity wa = workflowManager.getActivityById(activityId);
        String processInstanceId = wa.getProcessId();
        WorkflowAssignment ass = workflowManager.getAssignmentByProcess(processInstanceId);
        WorkflowAssignment ass2 = workflowManager.getAssignment(activityId);
        Assert.assertTrue(ass != null && ass2 != null && ass.getActivityId().equals(ass2.getActivityId()));
    }

    public void testAcceptedA(){
        System.out.println(">>> testAcceptedA");
        Map activityInstance = workflowManager.getActivityInstanceByProcessIdAndStatus(processId, true);
        workflowManager.assignmentComplete(String.valueOf(activityInstance.get("A")));
    }

    public void testStartActivityC() {
        System.out.println(">>> testStartActivityC");

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
        System.out.println("Running activities: " + runningActivities + "; Result: " + started);

        Assert.assertTrue(abortedActivity == null && runningActivity != null);
    }

    public void testStartProcessWithLinking(){
        System.out.println(">>> testStartProcessWithLinking");

        //start and get instant id of 1st process
        String packageVersion = workflowManager.getCurrentPackageVersion(packageId);
        WorkflowProcessResult result = workflowManager.processStart(packageId+"#"+packageVersion+"#"+processId);
        String process1Id = result.getProcess().getInstanceId();
        String parentProcess1Id = result.getParentProcessId();
        System.out.println("-------------  process one id : " + process1Id + "  -------------");

        //start 2nd process with 1st process instant id and get 2nd process instant id
        WorkflowProcessResult nextResult = workflowManager.processStartWithLinking(packageId+"#"+packageVersion+"#"+processId, null, null, parentProcess1Id);
        String process2Id = nextResult.getProcess().getInstanceId();
        System.out.println("-------------  process two id : " + process2Id + "  -------------");

        //check process linking data is correct or not
        WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(process2Id);
        System.out.println("-------------  origin process id : " + link.getOriginProcessId() + "  -------------");
        workflowManager.internalDeleteWorkflowProcessLink(link);
        Assert.assertNotNull(link);
        Assert.assertTrue(parentProcess1Id.equals(link.getOriginProcessId()) && parentProcess1Id.equals(link.getParentProcessId()));
    }

    public void testCopyProcess() {
        System.out.println(">>> testCopyProcess");

        boolean valid = false;

        // start and get instance id of the 1st process
        String packageVersion = workflowManager.getCurrentPackageVersion(packageId);
        WorkflowProcessResult result = workflowManager.processStart(packageId+"#"+packageVersion+"#"+processId);
        String processInstanceId = result.getProcess().getInstanceId();
        System.out.println("-------------  process one id : " + processInstanceId + "  -------------");

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
                System.out.println("-------------  new process id : " + newProcessId + "  ------------- " + valid);

                // cleanup
                WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(newProcessId);
                workflowManager.internalDeleteWorkflowProcessLink(link);
            }
        }

        Assert.assertTrue(valid);
    }
    
    public void testGetAssignments() {
        System.out.println(">>> testGetAssignments");
        
        String packageVersion = workflowManager.getCurrentPackageVersion(packageId);
        
        System.out.println(">>> getAssignmentListLite by packageId");
        Collection<WorkflowAssignment> assignments = workflowManager.getAssignmentListLite(packageId, null, null, null, null, null, null, null);
        Assert.assertEquals(4, assignments.size());
        
        System.out.println(">>> getAssignmentListLite by processDefId");
        assignments = workflowManager.getAssignmentListLite(null, packageId+"#"+packageVersion+"#"+processId, null, null, null, null, null, null);
        Assert.assertEquals(4, assignments.size());
        
        System.out.println(">>> getAssignmentListLite by processId");
        assignments = workflowManager.getAssignmentListLite(null, null, assignments.iterator().next().getProcessId(), null, null, null, null, null);
        Assert.assertEquals(1, assignments.size());
        
        System.out.println(">>> getAssignmentListLite by activityDefId");
        assignments = workflowManager.getAssignmentListLite(null, null, null, "A", null, null, null, null);
        Assert.assertEquals(2, assignments.size());
        
        System.out.println(">>> getAssignmentList by packageId");
        assignments = workflowManager.getAssignmentList(packageId, null, null, null, null, null, null, null);
        Assert.assertEquals(4, assignments.size());
        
        System.out.println(">>> getAssignmentList by processDefId");
        assignments = workflowManager.getAssignmentList(null, packageId+"#"+packageVersion+"#"+processId, null, null, null, null, null, null);
        Assert.assertEquals(4, assignments.size());
        
        System.out.println(">>> getAssignmentList by processId");
        assignments = workflowManager.getAssignmentList(null, null, assignments.iterator().next().getProcessId(), null, null, null, null, null);
        Assert.assertEquals(1, assignments.size());
        
        System.out.println(">>> getAssignmentList by activityDefId");
        assignments = workflowManager.getAssignmentList(null, null, null, "A", null, null, null, null);
        Assert.assertEquals(2, assignments.size());
        
        System.out.println(">>> getAssignmentSize by packageId");
        int count = workflowManager.getAssignmentSize(packageId, null, null, null);
        Assert.assertEquals(4, count);
        
        System.out.println(">>> getAssignmentSize by processDefId");
        count = workflowManager.getAssignmentSize(null, packageId+"#"+packageVersion+"#"+processId, null, null);
        Assert.assertEquals(4, count);
        
        System.out.println(">>> getAssignmentSize by processId");
        count = workflowManager.getAssignmentSize(null, null, assignments.iterator().next().getProcessId(), null);
        Assert.assertEquals(1, count);
        
        System.out.println(">>> getAssignmentSize by activityDefId");
        count = workflowManager.getAssignmentSize(null, null, null, "A");
        Assert.assertEquals(2, count);
        
        System.out.println(">>> getClosedActivitiesList by packageId");
        Collection<WorkflowActivity> activities = workflowManager.getClosedActivitiesList(packageId, null, null, null, null, null, null, null, null, null);
        Assert.assertEquals(4, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by packageId & aborted");
        activities = workflowManager.getClosedActivitiesList(packageId, null, null, null, null, "aborted", null, null, null, null);
        Assert.assertEquals(3, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by packageId & completed");
        activities = workflowManager.getClosedActivitiesList(packageId, null, null, null, null, "completed", null, null, null, null);
        Assert.assertEquals(1, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by processDefId");
        activities = workflowManager.getClosedActivitiesList(null, packageId+"#"+packageVersion+"#"+processId, null, null, null, null, null, null, null, null);
        Assert.assertEquals(4, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by processDefId & aborted");
        activities = workflowManager.getClosedActivitiesList(null, packageId+"#"+packageVersion+"#"+processId, null, null, null, "aborted", null, null, null, null);
        Assert.assertEquals(3, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by processDefId & completed");
        activities = workflowManager.getClosedActivitiesList(null, packageId+"#"+packageVersion+"#"+processId, null, null, null, "completed", null, null, null, null);
        Assert.assertEquals(1, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by processId");
        String processInsId = activities.iterator().next().getProcessId();
        activities = workflowManager.getClosedActivitiesList(null, null, processInsId, null, null, null, null, null, null, null);
        Assert.assertEquals(2, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by processId & aborted");
        activities = workflowManager.getClosedActivitiesList(null, null, processInsId, null, null, "aborted", null, null, null, null);
        Assert.assertEquals(1, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by processId & completed");
        activities = workflowManager.getClosedActivitiesList(null, null, processInsId, null, null, "completed", null, null, null, null);
        Assert.assertEquals(1, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by username");
        activities = workflowManager.getClosedActivitiesList(null, null, null, null, "roleAnonymous", null, null, null, null, null);
        Assert.assertEquals(1, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by username & aborted");
        activities = workflowManager.getClosedActivitiesList(null, null, null, null, "roleAnonymous", "aborted", null, null, null, null);
        Assert.assertEquals(0, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by username & completed");
        activities = workflowManager.getClosedActivitiesList(null, null, null, null, "roleAnonymous", "completed", null, null, null, null);
        Assert.assertEquals(1, activities.size());
        
        System.out.println(">>> getClosedActivitiesList by admin username");
        activities = workflowManager.getClosedActivitiesList(null, null, null, null, "admin", null, null, null, null, null);
        Assert.assertEquals(0, activities.size());
        
        System.out.println(">>> getClosedActivitiesListSize by packageId");
        count = workflowManager.getClosedActivitiesListSize(packageId, null, null, null, null, null);
        Assert.assertEquals(4, count);
        
        System.out.println(">>> getClosedActivitiesListSize by packageId & aborted");
        count = workflowManager.getClosedActivitiesListSize(packageId, null, null, null, null, "aborted");
        Assert.assertEquals(3, count);
        
        System.out.println(">>> getClosedActivitiesListSize by packageId & completed");
        count = workflowManager.getClosedActivitiesListSize(packageId, null, null, null, null, "completed");
        Assert.assertEquals(1, count);
        
        System.out.println(">>> getClosedActivitiesListSize by processDefId");
        count = workflowManager.getClosedActivitiesListSize(null, packageId+"#"+packageVersion+"#"+processId, null, null, null, null);
        Assert.assertEquals(4, count);
        
        System.out.println(">>> getClosedActivitiesListSize by processDefId & aborted");
        count = workflowManager.getClosedActivitiesListSize(null, packageId+"#"+packageVersion+"#"+processId, null, null, null, "aborted");
        Assert.assertEquals(3, count);
        
        System.out.println(">>> getClosedActivitiesListSize by processDefId & completed");
        count = workflowManager.getClosedActivitiesListSize(null, packageId+"#"+packageVersion+"#"+processId, null, null, null, "completed");
        Assert.assertEquals(1, count);
        
        System.out.println(">>> getClosedActivitiesListSize by processId");
        count = workflowManager.getClosedActivitiesListSize(null, null, processInsId, null, null, null);
        Assert.assertEquals(2, count);
        
        System.out.println(">>> getClosedActivitiesListSize by processId & aborted");
        count = workflowManager.getClosedActivitiesListSize(null, null, processInsId, null, null, "aborted");
        Assert.assertEquals(1, count);
        
        System.out.println(">>> getClosedActivitiesListSize by processId & completed");
        count = workflowManager.getClosedActivitiesListSize(null, null, processInsId, null, null, "completed");
        Assert.assertEquals(1, count);
        
        System.out.println(">>> getClosedActivitiesListSize by username");
        count = workflowManager.getClosedActivitiesListSize(null, null, null, null, "roleAnonymous", null);
        Assert.assertEquals(1, count);
        
        System.out.println(">>> getClosedActivitiesListSize by username & aborted");
        count = workflowManager.getClosedActivitiesListSize(null, null, null, null, "roleAnonymous", "aborted");
        Assert.assertEquals(0, count);
        
        System.out.println(">>> getClosedActivitiesListSize by username & completed");
        count = workflowManager.getClosedActivitiesListSize(null, null, null, null, "roleAnonymous", "completed");
        Assert.assertEquals(1, count);
        
        System.out.println(">>> getClosedActivitiesListSize by admin username");
        count = workflowManager.getClosedActivitiesListSize(null, null, null, null, "admin", null);
        Assert.assertEquals(0, count);
    }

    public void testCloseAndRemovePackage(){
        System.out.println(">>> testCloseAndRemovePackage");
        Collection<WorkflowProcess> processList = workflowManager.getRunningProcessList(packageId, null, null, null, null, null, 0, 100);

        for(WorkflowProcess process : processList) workflowManager.removeProcessInstance(process.getInstanceId());
        
        //remove all history data
        Collection<WorkflowProcess> hisProcessList = workflowAssignmentDao.getProcessHistories(null, null, null, null, null, null, null, null, null, null, null);
        for(WorkflowProcess process : hisProcessList) workflowAssignmentDao.deleteProcessHistory(process.getInstanceId());

        workflowManager.processDeleteAndUnload(packageId);
    }
}
