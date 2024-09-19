package com.mindex.challenge.data;

import java.time.LocalDate;

public class Compensation {
    private String employee;
    private int salary;
    private LocalDate effectiveDate;

    public Compensation() {
    }

    public Compensation(String employee, int salary, LocalDate effectiveDate) {
        this.employee = employee;
        this.salary = salary;
        this.effectiveDate = effectiveDate;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
