package org.bito.liquor.common.repository;

import org.bito.liquor.common.model.Liquor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiquorRepository extends JpaRepository<Liquor, Long> {

    Optional<Liquor> findByNormalizedNameAndClazzAndVolume(String normalizedName, String clazz, Integer volume);

    List<Liquor> findByBrand(String brand);

    List<Liquor> findByCategory(String category);

    List<Liquor> findByProductNameContainingIgnoreCase(String keyword);

    List<Liquor> findAllByOrderByUpdatedAtDesc();
}
