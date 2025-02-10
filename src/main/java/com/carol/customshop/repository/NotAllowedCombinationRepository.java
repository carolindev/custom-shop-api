
package com.carol.customshop.repository;

import com.carol.customshop.entity.NotAllowedCombination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotAllowedCombinationRepository extends JpaRepository<NotAllowedCombination, Long> {}