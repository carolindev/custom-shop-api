package com.carol.customshop.repository;

import com.carol.customshop.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<CartItem, UUID> {

    Page<CartItem> findAll(Pageable pageable);
}