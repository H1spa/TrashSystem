package com.example.trash.model;

import java.time.LocalDate;

public class Experiment {
    private int id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // "Активен", "Завершен", "Неудачен"
    private String result;
    private String notes;
    private String researcherName;
    private String conclusions;

    // Конструкторы
    public Experiment() {}

    public Experiment(int id, String name, LocalDate startDate, String status) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.status = status;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getResearcherName() { return researcherName; }
    public void setResearcherName(String researcherName) { this.researcherName = researcherName; }

    public String getConclusions() { return conclusions; }
    public void setConclusions(String conclusions) { this.conclusions = conclusions; }
}