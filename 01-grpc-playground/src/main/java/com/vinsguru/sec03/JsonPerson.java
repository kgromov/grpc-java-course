package com.vinsguru.sec03;

import java.util.Objects;

public class JsonPerson {
    private String lastName;
    private int age;
    private String email;
    private boolean employed;
    private double salary;
    private long bankAccountNumber;
    private int balance;

    public JsonPerson() {

    }

    public JsonPerson(String lastName, int age, String email, boolean employed,
                      double salary, long bankAccountNumber, int balance) {
        this.lastName = lastName;
        this.age = age;
        this.email = email;
        this.employed = employed;
        this.salary = salary;
        this.bankAccountNumber = bankAccountNumber;
        this.balance = balance;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public long getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(long bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmployed() {
        return employed;
    }

    public void setEmployed(boolean employed) {
        this.employed = employed;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        JsonPerson that = (JsonPerson) obj;
        return age == that.age &&
                employed == that.employed &&
                Double.compare(that.salary, salary) == 0 &&
                bankAccountNumber == that.bankAccountNumber &&
                balance == that.balance &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastName, age, email, employed, salary, bankAccountNumber, balance);
    }

    @Override
    public String toString() {
        return "JsonPerson[" +
                "lastName='" + lastName + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", employed=" + employed +
                ", salary=" + salary +
                ", bankAccountNumber=" + bankAccountNumber +
                ", balance=" + balance +
                ']';
    }
}
