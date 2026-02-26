package com.yaprj.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AcademicStatus {
    ENROLLED("enrolled"),
    EXPECTED("expected"),
    LEAVE("leave");
    
    private final String value;
    
    AcademicStatus(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public static AcademicStatus fromValue(String value) {
        for (AcademicStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
