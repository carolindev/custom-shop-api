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
public class NotAllowedCombinationOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "not_allowed_combination_id", nullable = false)
    private NotAllowedCombination notAllowedCombination;

    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;

    @Column(name = "attribute_option_id", nullable = false)
    private Long attributeOptionId;
}
