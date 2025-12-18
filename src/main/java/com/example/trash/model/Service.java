package com.example.trash.model;

import javafx.beans.property.*;

public class Service {
    private int id;
    private StringProperty name = new SimpleStringProperty();
    private DoubleProperty cost = new SimpleDoubleProperty();
    private String code;
    private int durationDays;
    private double deviationAvg;
    private boolean archived;

    // JavaFX properties getters
    public StringProperty nameProperty() { return name; }
    public DoubleProperty costProperty() { return cost; }

    // Regular getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public double getCost() { return cost.get(); }
    public void setCost(double cost) { this.cost.set(cost); }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public double getDeviationAvg() { return deviationAvg; }
    public void setDeviationAvg(double deviationAvg) { this.deviationAvg = deviationAvg; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    @Override
    public String toString() {
        return code + " - " + getName() + " (" + getCost() + " руб.)";
    }
}