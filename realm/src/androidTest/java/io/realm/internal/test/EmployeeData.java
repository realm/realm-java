package io.realm.internal.test;

import java.nio.ByteBuffer;
import java.util.Date;

public class EmployeeData {

    public String firstName;
    public String lastName;
    public int salary;
    public boolean driver;
    public byte[] photo;
    public Date birthdate;
    public PhoneData[] phones;

    public EmployeeData(String firstName, String lastName, int salary, boolean driver, byte[] photo, Date birthdate, PhoneData[] phones) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.driver = driver;
        this.photo = photo;
        this.birthdate = birthdate;
        this.phones = phones;
    }

    public Object get(int index) {
        switch (index) {
        case 0:
            return firstName;
        case 1:
            return lastName;
        case 2:
            return new Long(salary);
        case 3:
            return new Boolean(driver);
        case 4:
            return ByteBuffer.wrap(photo);
        case 5:
            return birthdate;
        case 6:
            return phones;
        default:
            throw new IllegalArgumentException("Incorrect index: " + index);
        }
    }

}
