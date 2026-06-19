package com.jiangmai.platform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseDTO {
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private BigDecimal price;
    private String status;
}
