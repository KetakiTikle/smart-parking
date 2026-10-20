package com.spms.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pure business rules for reservations (unit-tested, no database needed). */
public final class ReservationRules {

    private ReservationRules() { }

    /**
     * Two time ranges overlap when each one starts before the other ends.
     * Back-to-back bookings (end1 == start2) do NOT overlap.
     * This is the same condition the SQL query uses:
     *   existing.start_time < newEnd AND existing.end_time > newStart
     */
    public static boolean overlaps(LocalDateTime start1, LocalDateTime end1,
                                   LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /** Total cost = hours x price per hour. */
    public static BigDecimal calculateCost(BigDecimal pricePerHour, int hours) {
        return pricePerHour.multiply(BigDecimal.valueOf(hours));
    }

    /** NFR-5: only a CONFIRMED booking that has not started yet can be cancelled. */
    public static boolean canCancel(String status, LocalDateTime start, LocalDateTime now) {
        return "CONFIRMED".equals(status) && start.isAfter(now);
    }
}
