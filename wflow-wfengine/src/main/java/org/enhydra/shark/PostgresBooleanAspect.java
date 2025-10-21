package org.enhydra.shark;

import java.sql.DatabaseMetaData;
import java.util.Locale;
import javax.sql.DataSource;
import java.sql.Connection;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

/**
 * Aspect to fix PostgreSQL boolean type incompatibility in Shark DODS queries.
 * Converts integer boolean comparisons (IsValid = 1) to boolean literals (IsValid = TRUE).
 */
@Aspect
public class PostgresBooleanAspect {

    private static volatile Boolean IS_PG = null;

    /**
     * Rewrite numeric boolean predicates to PostgreSQL boolean literals.
     */
    private static String normalizeBooleanPredicates(String sql) {
        if (sql == null) return null;
        return sql
            .replaceAll("(?i)\\bIsValid\\s*=\\s*1\\b",    "IsValid = TRUE")
            .replaceAll("(?i)\\bIsValid\\s*=\\s*0\\b",    "IsValid = FALSE")
            .replaceAll("(?i)\\bIsAccepted\\s*=\\s*1\\b", "IsAccepted = TRUE")
            .replaceAll("(?i)\\bIsAccepted\\s*=\\s*0\\b", "IsAccepted = FALSE");
    }

    /**
     * Check if SQL contains boolean assignment patterns that need fixing
     */
    private static boolean containsComparison(String s) {
        return s != null && s.matches("(?s).*\\b(IsValid|IsAccepted)\\b\\s*=\\s*[01].*");
    }

    /**
     * Detect if the current datasource is PostgreSQL
     */
    private static boolean isPostgreSQL() {
        Boolean cached = IS_PG;
        if (cached != null) return cached;

        synchronized (PostgresBooleanAspect.class) {
            if (IS_PG != null) return IS_PG;

            boolean detected = false;
            try {
                ApplicationContext ctx = WorkflowUtil.getApplicationContext();
                if (ctx != null) {
                    DataSource ds = ctx.containsBean("dataSource") ? (DataSource) ctx.getBean("dataSource") : ctx.containsBean("setupDataSource") ? (DataSource) ctx.getBean("setupDataSource") : null;

                    if (ds != null) {
                        try (Connection c = ds.getConnection()) {
                            DatabaseMetaData md = c.getMetaData();
                            String product = md.getDatabaseProductName();
                            String url = md.getURL();
                            detected = (product != null && product.toLowerCase(Locale.ROOT).contains("postgresql")) || (url != null && url.toLowerCase(Locale.ROOT).startsWith("jdbc:postgresql:"));
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.warn(PostgresBooleanAspect.class.getName(), e.getMessage());
            }

            IS_PG = detected;
            return detected;
        }
    }
    
    /**
     * Intercept DODS WHERE-clause construction and replace numeric boolean
     * comparisons with boolean literals for PostgreSQL 
     */
    @Around("execution(* com.lutris.dods.builder.generator.query.QueryBuilder.addWhere(..))")
    public Object rewriteClause(ProceedingJoinPoint pjp) throws Throwable {
        if (!isPostgreSQL()) {
            return pjp.proceed();
        }
        Object[] args = pjp.getArgs();
        if (args.length >= 1 && args[0] instanceof String) {
            String whereClause = (String) args[0];
            if (containsComparison(whereClause)) {
                String fixed = normalizeBooleanPredicates(whereClause);
                if (!fixed.equals(whereClause)) {
                    args[0] = fixed;
                    return pjp.proceed(args);
                }
            }
        }
        return pjp.proceed();
    }
}