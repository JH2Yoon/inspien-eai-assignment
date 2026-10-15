package com.jhy.inspien.scenario1.dto;

import java.math.BigDecimal;

public class OrderItem {
    private String userId;
    private String itemId;
    private String itemName;
    private BigDecimal price;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}