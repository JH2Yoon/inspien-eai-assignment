package com.jhy.inspien.scenario2.dto;

public class ShipmentRecord {
    private String orderId;
    private String shipmentId;
    private String applicantKey;
    private String itemId;
    private String address;

    public ShipmentRecord(String orderId, String shipmentId, String applicantKey, String itemId, String address) {
        this.orderId = orderId;
        this.shipmentId = shipmentId;
        this.applicantKey = applicantKey;
        this.itemId = itemId;
        this.address = address;
    }

    public String getOrderId() { return orderId; }
    public String getShipmentId() { return shipmentId; }
    public String getApplicantKey() { return applicantKey; }
    public String getItemId() { return itemId; }
    public String getAddress() { return address; }
}