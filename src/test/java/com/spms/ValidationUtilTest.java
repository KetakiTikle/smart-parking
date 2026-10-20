package com.spms;

import com.spms.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for all validation rules (Unit Testing, BVA, Equivalence Partitioning). */
class ValidationUtilTest {

    // ---------------- Name (BVA: 1 / 2 / 50 / 51 characters) ----------------
    @Test void name_valid() { assertNull(ValidationUtil.validateName("Ketaki Tikle")); }
    @Test void name_twoChars_valid() { assertNull(ValidationUtil.validateName("Ab")); }
    @Test void name_fiftyChars_valid() { assertNull(ValidationUtil.validateName("A".repeat(50))); }
    @Test void name_oneChar_invalid() { assertNotNull(ValidationUtil.validateName("A")); }
    @Test void name_fiftyOneChars_invalid() { assertNotNull(ValidationUtil.validateName("A".repeat(51))); }
    @Test void name_withDigits_invalid() { assertNotNull(ValidationUtil.validateName("Ketaki123")); }
    @Test void name_empty_invalid() { assertNotNull(ValidationUtil.validateName("   ")); }

    // ---------------- Email (Equivalence Partitioning) ----------------
    @Test void email_valid() { assertNull(ValidationUtil.validateEmail("ketaki@example.com")); }
    @ParameterizedTest
    @ValueSource(strings = {"", "ketaki.example.com", "ketaki@", "@example.com", "ketaki@example", "a b@example.com"})
    void email_invalid(String email) { assertNotNull(ValidationUtil.validateEmail(email)); }

    // ---------------- Phone (BVA: 9 / 10 / 11 digits, first digit 6-9) ----------------
    @Test void phone_valid() { assertNull(ValidationUtil.validatePhone("9876543210")); }
    @Test void phone_nineDigits_invalid() { assertNotNull(ValidationUtil.validatePhone("987654321")); }
    @Test void phone_elevenDigits_invalid() { assertNotNull(ValidationUtil.validatePhone("98765432101")); }
    @Test void phone_startsWithFive_invalid() { assertNotNull(ValidationUtil.validatePhone("5876543210")); }
    @Test void phone_letters_invalid() { assertNotNull(ValidationUtil.validatePhone("98765abcde")); }

    // ---------------- Password (BVA: 7 / 8 / 20 / 21 characters) ----------------
    @Test void password_eightChars_valid() { assertNull(ValidationUtil.validatePassword("Abcd@123")); }
    @Test void password_twentyChars_valid() { assertNull(ValidationUtil.validatePassword("Aa1@" + "x".repeat(16))); }
    @Test void password_sevenChars_invalid() { assertNotNull(ValidationUtil.validatePassword("Abc@123")); }
    @Test void password_twentyOneChars_invalid() { assertNotNull(ValidationUtil.validatePassword("Aa1@" + "x".repeat(17))); }
    @Test void password_noUpper_invalid() { assertNotNull(ValidationUtil.validatePassword("abcd@1234")); }
    @Test void password_noLower_invalid() { assertNotNull(ValidationUtil.validatePassword("ABCD@1234")); }
    @Test void password_noDigit_invalid() { assertNotNull(ValidationUtil.validatePassword("Abcdef@gh")); }
    @Test void password_noSpecial_invalid() { assertNotNull(ValidationUtil.validatePassword("Abcdefg12")); }
    @Test void password_withSpace_invalid() { assertNotNull(ValidationUtil.validatePassword("Abc d@123")); }

    @Test void confirmPassword_match() { assertNull(ValidationUtil.validateConfirmPassword("Abcd@123", "Abcd@123")); }
    @Test void confirmPassword_mismatch() { assertNotNull(ValidationUtil.validateConfirmPassword("Abcd@123", "Abcd@124")); }

    // ---------------- Location search ----------------
    @Test void location_valid() { assertNull(ValidationUtil.validateLocation("Civil Lines")); }
    @Test void location_empty_invalid() { assertNotNull(ValidationUtil.validateLocation("  ")); }
    @Test void location_sqlInjection_invalid() { assertNotNull(ValidationUtil.validateLocation("' OR '1'='1")); }
    @Test void location_tooLong_invalid() { assertNotNull(ValidationUtil.validateLocation("a".repeat(51))); }

    // ---------------- Vehicle number (Equivalence Partitioning) ----------------
    @Test void vehicle_valid() { assertNull(ValidationUtil.validateVehicleNo("MH31AB1234")); }
    @Test void vehicle_oneLetterSeries_valid() { assertNull(ValidationUtil.validateVehicleNo("MH31A1234")); }
    @Test void vehicle_lowercaseWithSpaces_valid() { assertNull(ValidationUtil.validateVehicleNo("mh 31 ab 1234")); }
    @Test void vehicle_normalised() { assertEquals("MH31AB1234", ValidationUtil.normalizeVehicleNo("mh-31 ab 1234")); }
    @ParameterizedTest
    @ValueSource(strings = {"", "1234MH", "MH31AB12", "MH3AB1234", "MHAB311234"})
    void vehicle_invalid(String v) { assertNotNull(ValidationUtil.validateVehicleNo(v)); }

    // ---------------- Duration (BVA: 0 / 1 / 12 / 13) ----------------
    @Test void duration_zero_invalid() { assertNotNull(ValidationUtil.validateDuration(0)); }
    @Test void duration_one_valid() { assertNull(ValidationUtil.validateDuration(1)); }
    @Test void duration_twelve_valid() { assertNull(ValidationUtil.validateDuration(12)); }
    @Test void duration_thirteen_invalid() { assertNotNull(ValidationUtil.validateDuration(13)); }
    @Test void duration_null_invalid() { assertNotNull(ValidationUtil.validateDuration(null)); }

    // ---------------- Start time (BVA around now and +30 days) ----------------
    private final LocalDateTime now = LocalDateTime.of(2026, 10, 20, 10, 30, 45);

    @Test void start_null_invalid() { assertNotNull(ValidationUtil.validateStartTime(null, now)); }
    @Test void start_sameMinuteAsNow_valid() { assertNull(ValidationUtil.validateStartTime(now.withSecond(0), now)); }
    @Test void start_oneMinuteInPast_invalid() { assertNotNull(ValidationUtil.validateStartTime(now.minusMinutes(1).withSecond(0), now)); }
    @Test void start_exactly30Days_valid() { assertNull(ValidationUtil.validateStartTime(now.plusDays(30), now)); }
    @Test void start_31Days_invalid() { assertNotNull(ValidationUtil.validateStartTime(now.plusDays(31), now)); }
}
