package com.pet_project.watchstore.entity;

/** Khớp với cột users.role ENUM('CUSTOMER','ADMIN'). */
public enum Role {
    CUSTOMER,
    ADMIN;

    /** Tên authority theo quy ước Spring Security: hasRole("ADMIN") <=> "ROLE_ADMIN". */
    public String authority() {
        return "ROLE_" + name();
    }
}
