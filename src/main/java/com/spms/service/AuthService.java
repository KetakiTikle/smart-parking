package com.spms.service;

import com.spms.dao.UserDao;
import com.spms.model.User;
import com.spms.util.ValidationUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** MODULE 1: registration and login logic (FR-1, FR-2). */
@Service
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder encoder;

    public AuthService(UserDao userDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.encoder = encoder;
    }

    /**
     * Validates on the server and creates the account.
     * @return a map of field name -> error message. Empty map means success.
     */
    public Map<String, String> register(String name, String email, String phone,
                                        String password, String confirmPassword) {
        Map<String, String> errors = new LinkedHashMap<>();
        addIfError(errors, "name", ValidationUtil.validateName(name));
        addIfError(errors, "email", ValidationUtil.validateEmail(email));
        addIfError(errors, "phone", ValidationUtil.validatePhone(phone));
        addIfError(errors, "password", ValidationUtil.validatePassword(password));
        addIfError(errors, "confirmPassword", ValidationUtil.validateConfirmPassword(password, confirmPassword));

        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        if (!errors.containsKey("email") && userDao.emailExists(cleanEmail)) {
            errors.put("email", "This email is already registered.");
        }
        if (!errors.isEmpty()) {
            return errors;
        }

        try {
            userDao.save(name.trim().replaceAll("\\s+", " "), cleanEmail, phone.trim(),
                    encoder.encode(password), "DRIVER");
        } catch (DuplicateKeyException e) {
            // two people registering the same email at the same instant
            errors.put("email", "This email is already registered.");
        }
        return errors;
    }

    /** Returns the user only if the email exists and the password matches the stored BCrypt hash. */
    public Optional<User> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return userDao.findByEmail(email.trim().toLowerCase())
                .filter(u -> encoder.matches(password, u.getPasswordHash()));
    }

    private static void addIfError(Map<String, String> errors, String field, String message) {
        if (message != null) {
            errors.put(field, message);
        }
    }
}
