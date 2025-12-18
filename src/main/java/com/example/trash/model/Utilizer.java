package com.example.trash.model;

import java.util.Date;

public class Utilizer {
    private int id;
    private String name;
    private String description;
    private int capacity;
    private int currentLoad;
    private String status;
    private String ipAddress;
    private int port;
    private String model;
    private String manufacturer;
    private Date lastMaintenance;
    private Date nextMaintenance;
    private int totalProcesses;
    private int successfulProcesses;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(int currentLoad) { this.currentLoad = currentLoad; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public Date getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(Date lastMaintenance) { this.lastMaintenance = lastMaintenance; }

    public Date getNextMaintenance() { return nextMaintenance; }
    public void setNextMaintenance(Date nextMaintenance) { this.nextMaintenance = nextMaintenance; }

    public int getTotalProcesses() { return totalProcesses; }
    public void setTotalProcesses(int totalProcesses) { this.totalProcesses = totalProcesses; }

    public int getSuccessfulProcesses() { return successfulProcesses; }
    public void setSuccessfulProcesses(int successfulProcesses) { this.successfulProcesses = successfulProcesses; }

    // Методы бизнес-логики
    public boolean isAvailable() {
        return "available".equals(status) && currentLoad < capacity;
    }

    public int getAvailableSlots() {
        return Math.max(0, capacity - currentLoad);
    }

    public double getLoadPercentage() {
        return capacity > 0 ? (double) currentLoad / capacity * 100 : 0;
    }

    public double getSuccessRate() {
        return totalProcesses > 0 ? (double) successfulProcesses / totalProcesses * 100 : 0;
    }

    public String getStatusColor() {
        if ("maintenance".equals(status)) return "gray";
        if ("full".equals(status) || currentLoad >= capacity) return "red";
        if ("busy".equals(status)) return "yellow";
        if ("available".equals(status)) return "green";
        return "gray";
    }

    @Override
    public String toString() {
        return name + " (" + currentLoad + "/" + capacity + ") - " + status;
    }
}