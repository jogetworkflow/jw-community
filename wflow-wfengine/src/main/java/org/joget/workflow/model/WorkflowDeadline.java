package org.joget.workflow.model;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkflowDeadline {

    private long deadlineLimit;
    private String deadlineExpression;
    private Map context;

    public long getDeadlineLimit() {
        return deadlineLimit;
    }

    public void setDeadlineLimit(long deadlineLimit) {
        this.deadlineLimit = deadlineLimit;
        setExpressionFromLimit();
    }

    public String getDeadlineExpression() {
        return deadlineExpression;
    }

    public void setDeadlineExpression(String deadlineExpression) throws Exception {
        this.deadlineExpression = deadlineExpression;
        setLimitFromExpression();
    }

    private void setLimitFromExpression() throws Exception {
        if (deadlineExpression != null) {
            Pattern pattern = Pattern.compile("\\+(.+)\\);");
            Matcher matcher = pattern.matcher(deadlineExpression);

            if (matcher.find()) {
                try {
                    String matchedValue = matcher.group(1);
                    matchedValue = matchedValue.replace("(", "");
                    matchedValue = matchedValue.replace(")", "");
                    
                    String number[] = matchedValue.split("\\*");
                    String value = getWorkflowVariable(number[0]);
                    deadlineLimit = Integer.parseInt(value);
                    if (number.length > 1) {
                        deadlineLimit *= Integer.parseInt(number[1]);
                    }
                } catch (NumberFormatException nfe) {
                    //limit is incorrect
                    //throw exception to stop deadline plugin
                    throw new Exception();
                }
            } else {
                //no limit found
                //throw exception to stop deadline plugin
                throw new Exception();
            }
        }
    }

    private void setExpressionFromLimit() {
        if (deadlineExpression != null) {
            Pattern pattern = Pattern.compile("\\+(.+)\\);");
            Matcher matcher = pattern.matcher(deadlineExpression);

            while (matcher.find()) {
                deadlineExpression = matcher.replaceFirst("+" + deadlineLimit + ");");
            }
        }
    }

    public Map getContext() {
        return context;
    }

    public void setContext(Map context) {
        this.context = context;
    }
    
    private String getWorkflowVariable(String key) {
        if (context != null && context.containsKey(key)) {
            return context.get(key).toString();
        }
        return key;
    }
}
