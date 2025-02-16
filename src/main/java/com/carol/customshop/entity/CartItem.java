package com.carol.customshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ElementCollection
    @CollectionTable(name = "cart_item_options", joinColumns = @JoinColumn(name = "cart_item_id"))
    @Column(name = "selected_options")
    private List<ProductTypeAttributeOption> selectedOptions = new ArrayList<>();

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String label;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
