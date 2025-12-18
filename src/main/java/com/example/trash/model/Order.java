package com.example.trash.model;

import java.util.Date;
import java.util.List;

public class Order {
    private int id;
    private int clientId;
    private String orderNumber; // Changed from int to String
    private String caseCode;
    private String status;
    private Date createdAt;
    private int durationDays;
    private List<Integer> services;
    private double totalCost; // Add this field

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; } // Changed parameter type

    public String getCaseCode() { return caseCode; }
    public void setCaseCode(String caseCode) { this.caseCode = caseCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public List<Integer> getServices() { return services; }
    public void setServices(List<Integer> services) { this.services = services; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getFormattedDate() {
        if (createdAt != null) {
            return new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(createdAt);
        }
        return "";
    }

    @Override
    public String toString() {
        return "Заказ №" + orderNumber + " (" + caseCode + ") - " + status;
    }
}