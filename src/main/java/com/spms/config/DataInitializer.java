package com.spms.config;

import com.spms.dao.UserDao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the demo admin and driver accounts on first start (BCrypt hashes are generated here). */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserDao userDao;
    private final PasswordEncoder encoder;

    public DataInitializer(UserDao userDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("System Admin", "admin@parking.com", "9876543210", "Admin@123", "ADMIN");
        createIfMissing("Demo Driver", "driver@test.com", "9123456789", "Driver@123", "DRIVER");
    }

    private void createIfMissing(String name, String email, String phone, String password, String role) {
        if (!userDao.emailExists(email)) {
            userDao.save(name, email, phone, encoder.encode(password), role);
            System.out.println("Created demo account: " + email + " (" + role + ")");
        }
    }
}
