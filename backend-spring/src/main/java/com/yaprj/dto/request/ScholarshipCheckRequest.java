package com.yaprj.dto.request;

import com.yaprj.entity.enums.AcademicStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScholarshipCheckRequest {
    @NotNull(message = "Academic status is required")
    private AcademicStatus academicStatus;
    
    @NotNull(message = "Grade is required")
    @Min(value = 1, message = "Grade must be between 1 and 6")
    @Max(value = 6, message = "Grade must be between 1 and 6")
    private Integer grade;
    
    @NotNull(message = "Birth year is required")
    @Min(value = 1980, message = "Birth year must be between 1980 and 2010")
    @Max(value = 2010, message = "Birth year must be between 1980 and 2010")
    private Integer birthYear;
    
    @NotNull(message = "GPA is required")
    @DecimalMin(value = "0.0", message = "GPA must be between 0 and 4.5")
    @DecimalMax(value = "4.5", message = "GPA must be between 0 and 4.5")
    private BigDecimal gpa;
    
    @NotNull(message = "Income level is required")
    @Min(value = 1, message = "Income level must be between 1 and 10")
    @Max(value = 10, message = "Income level must be between 1 and 10")
    private Integer incomeLevel;
}
