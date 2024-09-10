package com.bulkSms.Model;

import lombok.Data;

import java.util.Objects;

@Data
public class RegistrationDetails {

    private String firstName;

    private String lastName;

    private String emailId;

    private String password;

    private String mobileNo;

    private String role;

    public boolean hasNullFields(){
        return Objects.isNull(firstName) || Objects.isNull(lastName) || Objects.isNull(emailId) || Objects.isNull(password) || Objects.isNull(mobileNo);
    }

    public boolean blankFields(){
        return firstName.isBlank() || lastName.isBlank() || emailId.isBlank() || password.isBlank() || mobileNo.isBlank();
    }

    public void validate(){
        if (hasNullFields() || blankFields()) {
            throw new IllegalArgumentException("All fields are required");
        }
    }
}