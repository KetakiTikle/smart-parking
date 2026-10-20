package com.spms.model;

public class Slot {
    private final int slotId;
    private final int lotId;
    private final String slotNumber;
    private final String status;        // ACTIVE or MAINTENANCE
    private final boolean occupiedNow;  // has a confirmed booking covering this moment

    public Slot(int slotId, int lotId, String slotNumber, String status, boolean occupiedNow) {
        this.slotId = slotId;
        this.lotId = lotId;
        this.slotNumber = slotNumber;
        this.status = status;
        this.occupiedNow = occupiedNow;
    }

    public int getSlotId() { return slotId; }
    public int getLotId() { return lotId; }
    public String getSlotNumber() { return slotNumber; }
    public String getStatus() { return status; }
    public boolean isOccupiedNow() { return occupiedNow; }

    /** FREE, BOOKED or MAINTENANCE - what the driver sees on the slot grid. */
    public String getDisplayStatus() {
        if ("MAINTENANCE".equals(status)) return "MAINTENANCE";
        return occupiedNow ? "BOOKED" : "FREE";
    }
}
