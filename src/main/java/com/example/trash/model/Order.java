package com.example.trash.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private IntegerProperty id = new SimpleIntegerProperty();
    private IntegerProperty orderNumber = new SimpleIntegerProperty();
    private StringProperty caseCode = new SimpleStringProperty();
    private IntegerProperty clientId = new SimpleIntegerProperty();
    private ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private StringProperty status = new SimpleStringProperty();
    private List<Integer> services;
    private DoubleProperty totalCost = new SimpleDoubleProperty();

    public Order() {
        this.createdAt.set(LocalDateTime.now());
        this.status.set("Создан");
    }

    // Геттеры и сеттеры
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getOrderNumber() { return orderNumber.get(); }
    public void setOrderNumber(int orderNumber) { this.orderNumber.set(orderNumber); }
    public IntegerProperty orderNumberProperty() { return orderNumber; }

    public String getCaseCode() { return caseCode.get(); }
    public void setCaseCode(String caseCode) { this.caseCode.set(caseCode); }
    public StringProperty caseCodeProperty() { return caseCode; }

    public int getClientId() { return clientId.get(); }
    public void setClientId(int clientId) { this.clientId.set(clientId); }
    public IntegerProperty clientIdProperty() { return clientId; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public List<Integer> getServices() { return services; }
    public void setServices(List<Integer> services) { this.services = services; }

    public double getTotalCost() { return totalCost.get(); }
    public void setTotalCost(double totalCost) { this.totalCost.set(totalCost); }
    public DoubleProperty totalCostProperty() { return totalCost; }

    public String getFormattedDate() {
        if (createdAt.get() == null) return "";
        return createdAt.get().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}