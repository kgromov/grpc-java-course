package com.vinsguru.sec03;

public record JsonPersonRecord(String lastName,
                               int age,
                               String email,
                               boolean employed,
                               double salary,
                               long bankAccountNumber,
                               int balance) {
}
