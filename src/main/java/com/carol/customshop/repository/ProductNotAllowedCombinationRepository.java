
package com.carol.customshop.repository;

import com.carol.customshop.entity.Product;
import com.carol.customshop.entity.ProductNotAllowedCombination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductNotAllowedCombinationRepository extends JpaRepository<ProductNotAllowedCombination, Long> {
    List<ProductNotAllowedCombination> findByProduct(Product product);
}