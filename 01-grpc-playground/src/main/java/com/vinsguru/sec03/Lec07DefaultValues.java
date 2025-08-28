package com.vinsguru.sec03;

import com.google.protobuf.Descriptors;
import com.vinsguru.models.sec03.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lec07DefaultValues {

    private static final Logger log = LoggerFactory.getLogger(Lec07DefaultValues.class);

    public static void main(String[] args) {

        var school = School.newBuilder().build();

        log.info("{}", school.getId());
        log.info("{}", school.getName());
        log.info("{}", school.getAddress().getCity());


        log.info("is default? : {}", school.getAddress().equals(Address.getDefaultInstance()));

        // has
        log.info("has address? {}", school.hasAddress());
        // for not optional scalar fields corresponding has${field_name} methods are not generated
        Descriptors.FieldDescriptor nameField = School.getDescriptor().findFieldByName("name");
        log.info("has name? {}", school.hasField(nameField));

        // collection
        var lib = Library.newBuilder().build();
        log.info("{}", lib.getBooksList());

        // map
        var dealer = Dealer.newBuilder().build();
        log.info("{}", dealer.getInventoryMap());

        // enum
        var car = Car.newBuilder().build();
        log.info("{}", car.getBodyStyle());

    }

}
