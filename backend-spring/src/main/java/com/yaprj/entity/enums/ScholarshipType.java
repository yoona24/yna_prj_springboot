package com.yaprj.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScholarshipType {
    NATIONAL("national"),
    WORK_STUDY("work_study"),
    TUITION_LOAN("tuition_loan"),
    LIVING_LOAN("living_loan"),
    LOCAL("local"),
    PRIVATE("private"),
    UNIVERSITY("university"),
    OTHER("other");
    
    private final String value;
    
    ScholarshipType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public static ScholarshipType fromValue(String value) {
        for (ScholarshipType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return OTHER;
    }
}
