package com.example.booksystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class CheckInTableInitializer {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS t_check_in (" +
                    "id BIGINT AUTO_INCREMENT, " +
                    "user_id BIGINT NOT NULL, " +
                    "check_in_date DATE NOT NULL, " +
                    "create_time DATETIME, " +
                    "PRIMARY KEY (id), " +
                    "UNIQUE (user_id, check_in_date)" +
                    ")");
        } catch (Exception e) {
            // Table may already exist, ignore
        }
    }
}
