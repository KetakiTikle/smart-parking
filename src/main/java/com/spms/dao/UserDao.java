package com.spms.dao;

import com.spms.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<User> MAPPER = (rs, i) -> new User(
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("password_hash"),
            rs.getString("role"));

    public Optional<User> findByEmail(String email) {
        // "?" placeholders = prepared statement, so SQL injection is not possible
        List<User> list = jdbc.query("SELECT * FROM users WHERE email = ?", MAPPER, email);
        return list.stream().findFirst();
    }

    public boolean emailExists(String email) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public void save(String name, String email, String phone, String passwordHash, String role) {
        jdbc.update("INSERT INTO users (name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, ?)",
                name, email, phone, passwordHash, role);
    }
}
