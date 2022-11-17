package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.commons.util.LogUtil;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ExpressionHashVariable extends DefaultHashVariablePlugin {
    protected static StandardEvaluationContext context = null;
    
    protected static StandardEvaluationContext getContext() {
        if (context == null) {
            context = new StandardEvaluationContext();
            try {
                context.registerFunction("isParsed", ExpressionHashVariable.class.getDeclaredMethod("isParsed", new Class[] { String.class }));
                
                Method[] methods = Math.class.getMethods();
                for (Method m : methods) {
                    if (Modifier.isStatic(m.getModifiers())) {
                        context.registerFunction(m.getName(), m);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(ExpressionHashVariable.class.getName(), e, "");
            }
        }
        return context;
    }
    
    @Override
    public String processHashVariable(String variableKey) {
        try {
            variableKey = variableKey.replaceAll("\\$(\\w+\\()", "#$1");
            
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(variableKey);
            Object result = exp.getValue(getContext());
            if (result != null) {
                return result.toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            LogUtil.error(ExpressionHashVariable.class.getName(), e, "Invalid expression: " + variableKey);
        }
        return null;
    }
    
    @Override
    public String getName() {
        return "Expression Hash Variable";
    }

    @Override
    public String getPrefix() {
        return "exp";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLabel() {
        return "Expression Hash Variable";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public Collection<String> availableSyntax() {
        Collection <String> list = new ArrayList<String>();
        
        list.add(getPrefix() + ".EXPRESSION");
        
        return list;
    }
    
    public static Boolean isParsed(String input) {
        return !((input.startsWith("#") && input.endsWith("#")) || (input.startsWith("{") && input.endsWith("}")));
    }
}
