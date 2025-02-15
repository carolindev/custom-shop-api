package com.carol.customshop.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_not_allowed_combinations_overrides")
@Getter
@Setter
@NoArgsConstructor
public class ProductNACombinationOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "not_allowed_combination_id", nullable = false)
    private NotAllowedCombination notAllowedCombination;

    private boolean active; // Allows the product to override restriction
}
