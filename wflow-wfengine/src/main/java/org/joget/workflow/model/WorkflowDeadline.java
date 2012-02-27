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

    private void setLimitFromExpression() {
        if (deadlineExpression != null) {
            Pattern pattern = Pattern.compile("\\+(.+)\\);");
            Matcher matcher = pattern.matcher(deadlineExpression);

            while (matcher.find()) {
                try {
                    String value = matcher.group(1);
                    value = value.replace("(", "");
                    value = value.replace(")", "");
                    
                    String number[] = value.split("\\*");
                    deadlineLimit = Integer.parseInt(number[0]);
                    if (number.length > 1) {
                        deadlineLimit *= Integer.parseInt(number[1]);
                    }
                } catch (NumberFormatException nfe) {
                    deadlineLimit = 0;
                }
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
}
