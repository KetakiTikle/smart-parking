package com.spms;

import com.spms.util.ReservationRules;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the double-booking rule, cost calculation and cancellation rule. */
class ReservationRulesTest {

    private static LocalDateTime t(int hour, int minute) {
        return LocalDateTime.of(2026, 10, 22, hour, minute);
    }

    // ---------------- Overlap (the double-booking rule) ----------------
    @Test void overlap_exactSameTime() { assertTrue(ReservationRules.overlaps(t(10, 0), t(12, 0), t(10, 0), t(12, 0))); }
    @Test void overlap_partialAtStart() { assertTrue(ReservationRules.overlaps(t(10, 0), t(12, 0), t(9, 0), t(11, 0))); }
    @Test void overlap_partialAtEnd() { assertTrue(ReservationRules.overlaps(t(10, 0), t(12, 0), t(11, 0), t(13, 0))); }
    @Test void overlap_newInsideExisting() { assertTrue(ReservationRules.overlaps(t(10, 0), t(14, 0), t(11, 0), t(12, 0))); }
    @Test void overlap_newContainsExisting() { assertTrue(ReservationRules.overlaps(t(11, 0), t(12, 0), t(10, 0), t(14, 0))); }
    @Test void noOverlap_backToBack_newAfter() { assertFalse(ReservationRules.overlaps(t(10, 0), t(12, 0), t(12, 0), t(13, 0))); }
    @Test void noOverlap_backToBack_newBefore() { assertFalse(ReservationRules.overlaps(t(10, 0), t(12, 0), t(9, 0), t(10, 0))); }
    @Test void noOverlap_farApart() { assertFalse(ReservationRules.overlaps(t(10, 0), t(12, 0), t(15, 0), t(16, 0))); }

    // ---------------- Cost = hours x price per hour ----------------
    @Test void cost_twoHoursAt30() {
        assertEquals(0, new BigDecimal("60.00").compareTo(ReservationRules.calculateCost(new BigDecimal("30.00"), 2)));
    }
    @Test void cost_twelveHoursAt40() {
        assertEquals(0, new BigDecimal("480.00").compareTo(ReservationRules.calculateCost(new BigDecimal("40.00"), 12)));
    }
    @Test void cost_oneHourAt20() {
        assertEquals(0, new BigDecimal("20.00").compareTo(ReservationRules.calculateCost(new BigDecimal("20.00"), 1)));
    }

    // ---------------- Cancellation rule ----------------
    @Test void cancel_confirmedFuture_allowed() { assertTrue(ReservationRules.canCancel("CONFIRMED", t(12, 0), t(10, 0))); }
    @Test void cancel_alreadyStarted_refused() { assertFalse(ReservationRules.canCancel("CONFIRMED", t(9, 0), t(10, 0))); }
    @Test void cancel_startsExactlyNow_refused() { assertFalse(ReservationRules.canCancel("CONFIRMED", t(10, 0), t(10, 0))); }
    @Test void cancel_alreadyCancelled_refused() { assertFalse(ReservationRules.canCancel("CANCELLED", t(12, 0), t(10, 0))); }
}
