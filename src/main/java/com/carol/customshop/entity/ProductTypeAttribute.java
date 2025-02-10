package com.carol.customshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_type_attributes")
@Getter
@Setter
@NoArgsConstructor
public class ProductTypeAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    private String attributeName;

    @OneToMany(mappedBy = "attribute",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProductTypeAttributeOption> options = new ArrayList<>();
}
