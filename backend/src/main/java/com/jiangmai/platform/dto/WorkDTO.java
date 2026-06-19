package com.jiangmai.platform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WorkDTO {
    private String title;
    private String description;
    private String imageUrl;
    private Integer isForSale;
    private BigDecimal price;
}
