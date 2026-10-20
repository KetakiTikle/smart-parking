package com.spms.service;

import com.spms.dao.ParkingLotDao;
import com.spms.dao.ReservationDao;
import com.spms.dao.SlotDao;
import com.spms.model.ParkingLot;
import com.spms.model.Reservation;
import com.spms.model.Slot;
import com.spms.util.ReservationRules;
import com.spms.util.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** MODULE 2: reservation and cancellation logic (FR-4, NFR-2, NFR-5). */
@Service
public class ReservationService {

    private final SlotDao slotDao;
    private final ParkingLotDao lotDao;
    private final ReservationDao reservationDao;

    public ReservationService(SlotDao slotDao, ParkingLotDao lotDao, ReservationDao reservationDao) {
        this.slotDao = slotDao;
        this.lotDao = lotDao;
        this.reservationDao = reservationDao;
    }

    /**
     * Validates, checks for double booking, and saves the reservation.
     * Runs in ONE transaction: the slot row is locked, the overlap check runs, then the insert runs.
     * @return the new reservation id
     * @throws BookingException with a user-friendly message if any rule fails
     */
    @Transactional
    public int reserve(int userId, int slotId, String vehicleNoRaw, String startRaw, String hoursRaw) {
        LocalDateTime now = LocalDateTime.now();

        // ---- 1. input validation ----
        fail(ValidationUtil.validateVehicleNo(vehicleNoRaw));
        LocalDateTime start = parseDateTime(startRaw);
        fail(ValidationUtil.validateStartTime(start, now));
        Integer hours = parseHours(hoursRaw);
        fail(ValidationUtil.validateDuration(hours));

        String vehicleNo = ValidationUtil.normalizeVehicleNo(vehicleNoRaw);
        LocalDateTime end = start.plusHours(hours);

        // ---- 2. lock the slot, then check the business rules ----
        Slot slot = slotDao.lockById(slotId)
                .orElseThrow(() -> new BookingException("Parking slot not found."));
        if (!"ACTIVE".equals(slot.getStatus())) {
            throw new BookingException("This slot is under maintenance and cannot be booked.");
        }
        if (reservationDao.countOverlapping(slotId, start, end) > 0) {
            throw new BookingException(
                    "This slot is already booked for the selected time. Please choose another slot or time.");
        }

        // ---- 3. price and save ----
        ParkingLot lot = lotDao.findById(slot.getLotId(), now)
                .orElseThrow(() -> new BookingException("Parking lot not found."));
        BigDecimal total = ReservationRules.calculateCost(lot.getPricePerHour(), hours);
        return reservationDao.insert(userId, slotId, vehicleNo, start, end, total);
    }

    /** Cancels a booking if it is yours, confirmed, and has not started (NFR-5). */
    @Transactional
    public void cancel(int userId, int reservationId) {
        int rows = reservationDao.cancel(reservationId, userId, LocalDateTime.now());
        if (rows == 1) {
            return;
        }
        // nothing changed - work out why, so the user gets a clear message
        Reservation r = reservationDao.findByIdForUser(reservationId, userId)
                .orElseThrow(() -> new BookingException("Booking not found."));
        if ("CANCELLED".equals(r.getStatus())) {
            throw new BookingException("This booking is already cancelled.");
        }
        throw new BookingException("Cannot cancel a booking that has already started.");
    }

    // ---------------- helpers ----------------
    private static void fail(String message) {
        if (message != null) {
            throw new BookingException(message);
        }
    }

    /** Accepts the browser's datetime-local format, e.g. 2026-10-22T14:30. Returns null if invalid. */
    private static LocalDateTime parseDateTime(String s) {
        try {
            return (s == null || s.trim().isEmpty()) ? null : LocalDateTime.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer parseHours(String s) {
        try {
            return (s == null) ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
