package org.joget.workflow.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkflowDeadline {

    private int deadlineLimit;
    private String deadlineExpression;

    public int getDeadlineLimit() {
        return deadlineLimit;
    }

    public void setDeadlineLimit(int deadlineLimit) {
        this.deadlineLimit = deadlineLimit;
        setExpressionFromLimit();
    }

    public String getDeadlineExpression() {
        return deadlineExpression;
    }

    public void setDeadlineExpression(String deadlineExpression) {
        this.deadlineExpression = deadlineExpression;
        setLimitFromExpression();
    }

    private void setLimitFromExpression(){
        Pattern pattern = Pattern.compile("(.+\\+)([0-9]+)(\\);.+)");
        Matcher matcher = pattern.matcher(deadlineExpression);

        while (matcher.find()) {
            try{
                deadlineLimit = Integer.parseInt(matcher.group(2));
            }catch(NumberFormatException nfe){
                deadlineLimit = 0;
            }
        }
    }

    private void setExpressionFromLimit(){
        Pattern pattern = Pattern.compile("(.+\\+)([0-9]+)(\\);.+)");
        Matcher matcher = pattern.matcher(deadlineExpression);

        while (matcher.find()) {
            deadlineExpression = matcher.replaceFirst("$1" + deadlineLimit + "$3");
        }
    }
}
