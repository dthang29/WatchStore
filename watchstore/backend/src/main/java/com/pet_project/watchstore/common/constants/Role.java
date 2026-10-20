package com.pet_project.watchstore.common.constants;

public enum Role {
    CUSTOMER,
    ADMIN;
    public String authority() {
        return "ROLE_" + name();
    }
}