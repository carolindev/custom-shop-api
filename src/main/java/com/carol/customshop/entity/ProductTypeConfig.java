package com.carol.customshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeConfig {
    private String customisation;  // e.g., "fully_customisable"
   // private String paymentType;    // e.g., "one_time_payment", "subscription"
}
