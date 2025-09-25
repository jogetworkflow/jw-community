package org.joget.workflow.shark.migrate.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Component
public class MigrateRoles {

    private static final Log log = LogFactory.getLog(MigrateRoles.class);

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private static final String TABLE_NAME = "dir_role";
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String DESC_COLUMN = "description";

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate = new JdbcTemplate(dataSource);
            ensureSystemRoles();
        } catch (Exception e) {
            log.error("Failed to initialize roles: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void ensureSystemRoles() {
        createRoleIfMissing("ROLE_SYSTEM_MANAGER", "System Manager", "System Manager");
        createRoleIfMissing("ROLE_APP_CREATOR", "App Creator", "App Creator");
    }

    private void createRoleIfMissing(String id, String name, String desc) {
        String checkSql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", TABLE_NAME, ID_COLUMN);
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);

        if (count != null && count == 0) {
            String insertSql = String.format(
                "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                TABLE_NAME, ID_COLUMN, NAME_COLUMN, DESC_COLUMN
            );
            int rowsAffected = jdbcTemplate.update(insertSql, id, name, desc);
            if (rowsAffected > 0) {
                log.info("Created system role: " + id);
            }
        }
    }
}
