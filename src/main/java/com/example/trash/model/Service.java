package com.example.trash.model;

import javafx.beans.property.*;

public class Service {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty name = new SimpleStringProperty();
    private DoubleProperty cost = new SimpleDoubleProperty();
    private StringProperty code = new SimpleStringProperty();
    private IntegerProperty durationDays = new SimpleIntegerProperty();
    private DoubleProperty deviationAvg = new SimpleDoubleProperty();
    private BooleanProperty archived = new SimpleBooleanProperty();

    // Конструкторы
    public Service() {}

    public Service(int id, String name, double cost, String code,
                   Integer durationDays, Double deviationAvg, boolean archived) {
        setId(id);
        setName(name);
        setCost(cost);
        setCode(code);
        setDurationDays(durationDays);
        setDeviationAvg(deviationAvg);
        setArchived(archived);
    }

    // Property методы
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty costProperty() { return cost; }
    public StringProperty codeProperty() { return code; }
    public IntegerProperty durationDaysProperty() { return durationDays; }
    public DoubleProperty deviationAvgProperty() { return deviationAvg; }
    public BooleanProperty archivedProperty() { return archived; }

    // Геттеры
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getCost() { return cost.get(); }
    public String getCode() { return code.get(); }
    public Integer getDurationDays() { return durationDays.get(); }
    public Double getDeviationAvg() { return deviationAvg.get(); }
    public boolean isArchived() { return archived.get(); }

    // Сеттеры
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setCost(double cost) { this.cost.set(cost); }
    public void setCode(String code) { this.code.set(code); }
    public void setDurationDays(Integer durationDays) { this.durationDays.set(durationDays); }
    public void setDeviationAvg(Double deviationAvg) { this.deviationAvg.set(deviationAvg); }
    public void setArchived(boolean archived) { this.archived.set(archived); }

    @Override
    public String toString() {
        return getName() + " (" + getCost() + " руб.)";
    }
}