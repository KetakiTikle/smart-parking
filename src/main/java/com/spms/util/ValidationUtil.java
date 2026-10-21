package com.spms.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * All input-validation rules in one place so they can be unit-tested
 * without a database or web server.
 * Every method returns an error message, or null when the input is valid.
 */
public final class ValidationUtil {

    private ValidationUtil() { }

    private static final Pattern NAME     = Pattern.compile("^[A-Za-z ]{2,50}$");
    private static final Pattern EMAIL    = Pattern.compile("^[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*@[A-Za-z0-9]+([.-][A-Za-z0-9]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE    = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern VEHICLE  = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$");
    private static final Pattern LOCATION = Pattern.compile("^[A-Za-z0-9 ,.\\-]+$");

    public static final int MIN_HOURS = 1;
    public static final int MAX_HOURS = 12;
    public static final int MAX_ADVANCE_DAYS = 30;

    // ------------------------------------------------------------------
    // MODULE 1: registration and login
    // ------------------------------------------------------------------
    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) return "Name is required.";
        if (!NAME.matcher(name.trim()).matches()) return "Name must be 2-50 letters and spaces only.";
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "Email is required.";
        String e = email.trim();
        if (e.length() > 100 || !EMAIL.matcher(e).matches()) return "Enter a valid email address.";
        return null;
    }

    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "Phone number is required.";
        if (!PHONE.matcher(phone.trim()).matches()) return "Phone must be 10 digits and start with 6, 7, 8 or 9.";
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) return "Password is required.";
        if (password.length() < 8 || password.length() > 20) return "Password must be 8-20 characters long.";
        if (password.contains(" ")) return "Password must not contain spaces.";
        if (!password.matches(".*[A-Z].*")) return "Password needs at least one uppercase letter.";
        if (!password.matches(".*[a-z].*")) return "Password needs at least one lowercase letter.";
        if (!password.matches(".*[0-9].*")) return "Password needs at least one digit.";
        if (!password.matches(".*[^A-Za-z0-9].*")) return "Password needs at least one special character.";
        return null;
    }

    public static String validateConfirmPassword(String password, String confirm) {
        if (confirm == null || !confirm.equals(password)) return "Passwords do not match.";
        return null;
    }

    // ------------------------------------------------------------------
    // MODULE 2: search and reservation
    // ------------------------------------------------------------------
    public static String validateLocation(String q) {
        if (q == null || q.trim().isEmpty()) return "Please enter a location to search.";
        if (q.trim().length() > 50) return "Location must be at most 50 characters.";
        if (!LOCATION.matcher(q.trim()).matches())
            return "Location may contain only letters, numbers, spaces, commas, dots and hyphens.";
        return null;
    }

    /** "mh 31-ab 1234" becomes "MH31AB1234". */
    public static String normalizeVehicleNo(String v) {
        return v == null ? "" : v.replaceAll("[\\s-]", "").toUpperCase();
    }

    public static String validateVehicleNo(String v) {
        String n = normalizeVehicleNo(v);
        if (n.isEmpty()) return "Vehicle number is required.";
        if (!VEHICLE.matcher(n).matches()) return "Enter a valid vehicle number (e.g. MH31AB1234).";
        return null;
    }

    public static String validateDuration(Integer hours) {
        if (hours == null || hours < MIN_HOURS || hours > MAX_HOURS)
            return "Duration must be a whole number of hours between " + MIN_HOURS + " and " + MAX_HOURS + ".";
        return null;
    }

    /** start == null means the text could not be parsed as a date and time. */
    public static String validateStartTime(LocalDateTime start, LocalDateTime now) {
        if (start == null) return "Enter a valid start date and time.";
        if (start.isBefore(now.truncatedTo(ChronoUnit.MINUTES))) return "Start time cannot be in the past.";
        if (start.isAfter(now.plusDays(MAX_ADVANCE_DAYS)))
            return "Bookings can be made up to " + MAX_ADVANCE_DAYS + " days in advance.";
        return null;
    }
}
