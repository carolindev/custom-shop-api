package com.carol.customshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "product_not_allowed_combination_options")
@Getter
@Setter
@NoArgsConstructor
public class ProductNotAllowedCombinationElement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "combination_id", nullable = false)
    private ProductNotAllowedCombination combination;

    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private ProductTypeAttribute attribute;

    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    private ProductTypeAttributeOption option;

    public ProductNotAllowedCombinationElement(ProductTypeAttribute attribute, ProductTypeAttributeOption option) {
        this.attribute = attribute;
        this.option = option;
    }
}
