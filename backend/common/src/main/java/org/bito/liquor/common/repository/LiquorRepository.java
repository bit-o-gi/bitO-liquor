package org.bito.liquor.common.repository;

import org.bito.liquor.common.model.Liquor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiquorRepository extends JpaRepository<Liquor, Long> {

    Optional<Liquor> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    List<Liquor> findByBrand(String brand);

    List<Liquor> findByCategory(String category);

    List<Liquor> findByCurrentPriceLessThanOrderByCurrentPriceAsc(Integer price);

    List<Liquor> findByNameContainingIgnoreCase(String keyword);

    List<Liquor> findAllByOrderByUpdatedAtDesc();

    @Query(value = "SELECT DISTINCT ON (name, class, volume) * " +
            "FROM liquors " +
            "WHERE category ILIKE '%whisk%' " +
            "ORDER BY name, class, volume, current_price ASC",
            nativeQuery = true)
    List<Liquor> findLowestPriceWhiskies();
}
