package com.example.easyvote;

public class EmployeeListModel {
    private String employeeName;
    private String employeePhone;
    private String employeeAddress;

    // Default constructor (required for Firebase)
    public EmployeeListModel() {}

    public EmployeeListModel(String employeeName, String employeePhone, String employeeAddress) {
        this.employeeName = employeeName;
        this.employeePhone = employeePhone;
        this.employeeAddress = employeeAddress;
    }

    // Getters and Setters
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeePhone() {
        return employeePhone;
    }

    public void setEmployeePhone(String employeePhone) {
        this.employeePhone = employeePhone;
    }

    public String getEmployeeAddress() {
        return employeeAddress;
    }

    public void setEmployeeAddress(String employeeAddress) {
        this.employeeAddress = employeeAddress;
    }
}
