package com.pluralsight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// 6 private fields
public class Transaction  {
    private LocalDateTime dateTime;
    private LocalDate date;
    private LocalTime time;
    private String description;
    private String vendor;
    private double amount;

    // constructor with 4 of the 6 fields
    // date and time are taken from entered dateTime, separated to print in proper format
    public Transaction(LocalDateTime dateTime, String description, String vendor, double amount) {
        this.dateTime = dateTime;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
        date = dateTime.toLocalDate();
        time = dateTime.toLocalTime();
    }

    // getter methods
    public LocalDateTime getDateTime() {return dateTime;}

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getVendor() {
        return vendor;
    }

    public double getAmount() {
        return amount;
    }

    // toString matching the format of entries in transactions.csv
    @Override
    public String toString(){
        return String.format("%tF|%tT|%s|%s|%.2f",date, time, description, vendor, amount);
    }

}
