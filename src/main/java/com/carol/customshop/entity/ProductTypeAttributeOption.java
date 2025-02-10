package com.carol.customshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_type_attribute_options")
@Getter
@Setter
@NoArgsConstructor
public class ProductTypeAttributeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /**
     * The parent attribute to which this option belongs.
     */
    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private ProductTypeAttribute attribute;
}
