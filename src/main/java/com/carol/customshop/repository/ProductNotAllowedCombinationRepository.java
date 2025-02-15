
package com.carol.customshop.repository;

import com.carol.customshop.entity.ProductNotAllowedCombination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductNotAllowedCombinationRepository extends JpaRepository<ProductNotAllowedCombination, Long> {}