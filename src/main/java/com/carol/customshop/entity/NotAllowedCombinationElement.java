package com.carol.customshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "not_allowed_combination_options")
@Getter
@Setter
@NoArgsConstructor
public class NotAllowedCombinationElement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "not_allowed_combination_id", nullable = false)
    private NotAllowedCombination notAllowedCombination;

    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private ProductTypeAttribute attribute;

    @ManyToOne
    @JoinColumn(name = "attribute_option_id", nullable = false)
    private ProductTypeAttributeOption attributeOption;
}
