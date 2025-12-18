package com.example.trash.model;

import java.util.Date;

public class UtilizerProcess {
    private int id;
    private String processUuid;
    private int orderId;
    private String orderNumber;
    private int serviceId;
    private String serviceCode;
    private String serviceName;
    private int utilizerId;
    private String utilizerName;
    private int researcherId;
    private String researcherName;
    private int processType;
    private String status;
    private int progress;
    private Date startTime;
    private Date estimatedCompletionTime;
    private Date actualCompletionTime;

    // Конструкторы
    public UtilizerProcess() {}

    public UtilizerProcess(int orderId, String orderNumber, int serviceId,
                           String serviceCode, String serviceName, int utilizerId,
                           String utilizerName, int researcherId, String researcherName) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.serviceId = serviceId;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.utilizerId = utilizerId;
        this.utilizerName = utilizerName;
        this.researcherId = researcherId;
        this.researcherName = researcherName;
        this.status = "pending";
        this.progress = 0;
        this.startTime = new Date();
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProcessUuid() { return processUuid; }
    public void setProcessUuid(String processUuid) { this.processUuid = processUuid; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public int getUtilizerId() { return utilizerId; }
    public void setUtilizerId(int utilizerId) { this.utilizerId = utilizerId; }

    public String getUtilizerName() { return utilizerName; }
    public void setUtilizerName(String utilizerName) { this.utilizerName = utilizerName; }

    public int getResearcherId() { return researcherId; }
    public void setResearcherId(int researcherId) { this.researcherId = researcherId; }

    public String getResearcherName() { return researcherName; }
    public void setResearcherName(String researcherName) { this.researcherName = researcherName; }

    public int getProcessType() { return processType; }
    public void setProcessType(int processType) { this.processType = processType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(Date estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    public Date getActualCompletionTime() { return actualCompletionTime; }
    public void setActualCompletionTime(Date actualCompletionTime) {
        this.actualCompletionTime = actualCompletionTime;
    }

    // Вспомогательные методы
    public String getProgressFormatted() {
        return progress + "%";
    }

    public String getStatusColor() {
        switch (status) {
            case "pending": return "#FFC107"; // желтый
            case "running": return "#2196F3"; // синий
            case "completed": return "#4CAF50"; // зеленый
            case "failed": return "#F44336"; // красный
            default: return "#9E9E9E"; // серый
        }
    }

    public boolean isRunning() {
        return "running".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }
}