package com.luggage.luggagesystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class LuggageSystemApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }

    @Test
    void databaseConnectionWorks() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertFalse(connection.isClosed());
            assertEquals("luggage_system", connection.getCatalog());
        }
    }
}