package com.spms.model;

import java.math.BigDecimal;

public class ParkingLot {
    private final int lotId;
    private final String name;
    private final String location;
    private final String address;
    private final int totalSlots;
    private final BigDecimal pricePerHour;
    private final int availableNow;   // free, active slots at this moment

    public ParkingLot(int lotId, String name, String location, String address,
                      int totalSlots, BigDecimal pricePerHour, int availableNow) {
        this.lotId = lotId;
        this.name = name;
        this.location = location;
        this.address = address;
        this.totalSlots = totalSlots;
        this.pricePerHour = pricePerHour;
        this.availableNow = availableNow;
    }

    public int getLotId() { return lotId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getAddress() { return address; }
    public int getTotalSlots() { return totalSlots; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public int getAvailableNow() { return availableNow; }
}
