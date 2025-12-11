package com.example.trash.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Client {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty fio = new SimpleStringProperty();
    private ObjectProperty<LocalDate> birthDate = new SimpleObjectProperty<>();
    private StringProperty passportSeries = new SimpleStringProperty();
    private StringProperty passportNumber = new SimpleStringProperty();
    private StringProperty phone = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();
    private IntegerProperty companyId = new SimpleIntegerProperty();
    private IntegerProperty typeClientId = new SimpleIntegerProperty();
    private BooleanProperty archived = new SimpleBooleanProperty();
    private StringProperty companyName = new SimpleStringProperty();

    // Конструкторы
    public Client() {}

    public Client(int id, String fio, LocalDate birthDate, String passportSeries,
                  String passportNumber, String phone, String email,
                  int companyId, int typeClientId, boolean archived) {
        setId(id);
        setFio(fio);
        setBirthDate(birthDate);
        setPassportSeries(passportSeries);
        setPassportNumber(passportNumber);
        setPhone(phone);
        setEmail(email);
        setCompanyId(companyId);
        setTypeClientId(typeClientId);
        setArchived(archived);
    }

    // Property методы
    public IntegerProperty idProperty() { return id; }
    public StringProperty fioProperty() { return fio; }
    public ObjectProperty<LocalDate> birthDateProperty() { return birthDate; }
    public StringProperty passportSeriesProperty() { return passportSeries; }
    public StringProperty passportNumberProperty() { return passportNumber; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty emailProperty() { return email; }
    public IntegerProperty companyIdProperty() { return companyId; }
    public IntegerProperty typeClientIdProperty() { return typeClientId; }
    public BooleanProperty archivedProperty() { return archived; }
    public StringProperty companyNameProperty() { return companyName; }

    // Геттеры
    public int getId() { return id.get(); }
    public String getFio() { return fio.get(); }
    public LocalDate getBirthDate() { return birthDate.get(); }
    public String getPassportSeries() { return passportSeries.get(); }
    public String getPassportNumber() { return passportNumber.get(); }
    public String getPhone() { return phone.get(); }
    public String getEmail() { return email.get(); }
    public int getCompanyId() { return companyId.get(); }
    public int getTypeClientId() { return typeClientId.get(); }
    public boolean isArchived() { return archived.get(); }
    public String getCompanyName() { return companyName.get(); }

    // Сеттеры
    public void setId(int id) { this.id.set(id); }
    public void setFio(String fio) { this.fio.set(fio); }
    public void setBirthDate(LocalDate birthDate) { this.birthDate.set(birthDate); }
    public void setPassportSeries(String passportSeries) { this.passportSeries.set(passportSeries); }
    public void setPassportNumber(String passportNumber) { this.passportNumber.set(passportNumber); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setEmail(String email) { this.email.set(email); }
    public void setCompanyId(int companyId) { this.companyId.set(companyId); }
    public void setTypeClientId(int typeClientId) { this.typeClientId.set(typeClientId); }
    public void setArchived(boolean archived) { this.archived.set(archived); }
    public void setCompanyName(String companyName) { this.companyName.set(companyName); }

    // Метод для отображения
    public String getName() {
        return getFio();
    }
}