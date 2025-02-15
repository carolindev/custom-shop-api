package com.carol.customshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_option_overrides",
        indexes = { @Index(name = "idx_product_option", columnList = "product_id, option_id") },
        uniqueConstraints = { @UniqueConstraint(columnNames = {"product_id", "option_id"}) }
)
@Getter
@Setter
@NoArgsConstructor
public class ProductOptionOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    private ProductTypeAttributeOption option;

    private boolean active; // Allows the product to override activation per option

    private boolean outOfStock; // Tracks stock availability at product level
}
