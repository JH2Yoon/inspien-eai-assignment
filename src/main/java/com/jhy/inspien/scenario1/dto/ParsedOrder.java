package com.jhy.inspien.scenario1.dto;

public class ParsedOrder {
    private final OrderHeader header;
    private final OrderItem item;

    public ParsedOrder(OrderHeader header, OrderItem item) {
        this.header = header;
        this.item = item;
    }

    public OrderHeader getHeader() { return header; }
    public OrderItem getItem() { return item; }
}