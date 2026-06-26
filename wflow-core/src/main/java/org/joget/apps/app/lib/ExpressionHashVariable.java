package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.springframework.expression.AccessException;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorExecutor;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
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
            // Secure configuration
            context.setTypeLocator(new NoTypeLocator());
            context.setBeanResolver(new NoBeanResolver());
            context.setConstructorResolvers(List.of((new NoConstructorResolver())));
            context.setMethodResolvers(List.of((new SecureMethodResolver())));
            context.setPropertyAccessors(List.of((new SecurePropertyAccessor())));
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

    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/expressionHashVariable.json", null, true, null);
    }

    // Security Classes
    public static class NoTypeLocator implements TypeLocator {
        @Override
        public Class<?> findType(String typeName) throws EvaluationException {
            throw new EvaluationException("Access to types is forbidden");
        }
    }

    public static class SecureMethodResolver extends ReflectiveMethodResolver {
        @Override
        public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name,
                List<org.springframework.core.convert.TypeDescriptor> argumentTypes) throws AccessException {
            if ("getClass".equalsIgnoreCase(name) || "getClassLoader".equalsIgnoreCase(name)) {
                throw new AccessException("Access to getClass() or getClassLoader() is forbidden");
            }
            return super.resolve(context, targetObject, name, argumentTypes);
        }
    }

    public static class SecurePropertyAccessor extends ReflectivePropertyAccessor {
        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
            if ("class".equalsIgnoreCase(name) || "classLoader".equalsIgnoreCase(name)) { // also block classLoader just in case
                return false;
            }
            return super.canRead(context, target, name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            if ("class".equalsIgnoreCase(name) || "classLoader".equalsIgnoreCase(name)) {
                throw new AccessException("Access to class/classLoader is forbidden");
            }
            return super.read(context, target, name);
        }
    }

    public static class NoBeanResolver implements BeanResolver {
        @Override
        public Object resolve(EvaluationContext context, String beanName) throws AccessException {
            throw new AccessException("Access to beans is forbidden");
        }
    }

    public static class NoConstructorResolver implements ConstructorResolver {
        @Override
        public ConstructorExecutor resolve(EvaluationContext context, String typeName,
                List<org.springframework.core.convert.TypeDescriptor> argumentTypes) throws AccessException {
            throw new AccessException("Access to constructors is forbidden");
        }
    }
}
