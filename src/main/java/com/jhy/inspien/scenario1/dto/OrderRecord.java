package com.jhy.inspien.scenario1.dto;

public class OrderRecord {
    private final String orderId;
    private final ParsedOrder parsedOrder;

    public OrderRecord(String orderId, ParsedOrder parsedOrder) { this.orderId = orderId; this.parsedOrder = parsedOrder; }
    public String getOrderId() { return orderId; }
    public ParsedOrder getParsedOrder() { return parsedOrder; }
}