package com.carol.customshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "not_allowed_combinations")
@Getter
@Setter
@NoArgsConstructor
public class NotAllowedCombination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each NotAllowedCombination belongs to one ProductType
    @ManyToOne
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    // Each NotAllowedCombination consists of multiple attribute-option pairs
    @OneToMany(
            mappedBy = "notAllowedCombination",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<NotAllowedCombinationOption> options;

}
