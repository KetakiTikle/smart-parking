package com.spms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reservation {
    private final int reservationId;
    private final int userId;
    private final int slotId;
    private final String vehicleNo;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final BigDecimal totalAmount;
    private final String status;        // CONFIRMED, CANCELLED, COMPLETED (stored)
    private String lotName;             // filled by join queries
    private String slotNumber;

    public Reservation(int reservationId, int userId, int slotId, String vehicleNo,
                       LocalDateTime startTime, LocalDateTime endTime,
                       BigDecimal totalAmount, String status) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.slotId = slotId;
        this.vehicleNo = vehicleNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getReservationId() { return reservationId; }
    public int getUserId() { return userId; }
    public int getSlotId() { return slotId; }
    public String getVehicleNo() { return vehicleNo; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getLotName() { return lotName; }
    public void setLotName(String lotName) { this.lotName = lotName; }
    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    /** UPCOMING, ACTIVE, COMPLETED or CANCELLED - shown on "My Bookings". */
    public String getDisplayStatus() {
        if ("CANCELLED".equals(status)) return "CANCELLED";
        LocalDateTime now = LocalDateTime.now();
        if (!endTime.isAfter(now)) return "COMPLETED";
        if (!startTime.isAfter(now)) return "ACTIVE";
        return "UPCOMING";
    }

    /** Business rule NFR-5: cancel only before the scheduled start time. */
    public boolean isCancellable() {
        return "CONFIRMED".equals(status) && startTime.isAfter(LocalDateTime.now());
    }
}
