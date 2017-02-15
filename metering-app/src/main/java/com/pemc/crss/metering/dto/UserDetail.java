package com.pemc.crss.metering.dto;

import lombok.Data;

@Data
public class UserDetail {

    private Long id;
    private String username;
    private String lastName;
    private String firstName;
    private String middleName;
    private String fullName;
    private String department;
    private boolean pemcUser;
    private boolean nonPemcUser;

}
