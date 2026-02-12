package org.bito.liquor.repository;

import org.bito.liquor.model.Whisky;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WhiskyRepository extends JpaRepository<Whisky, Long> {

    @Query(value = "SELECT DISTINCT ON (name, class, volume) * " +
            "FROM whisky " +
            "ORDER BY name, class, volume, current_price ASC",
            nativeQuery = true)
    List<Whisky> findLowestPriceWhiskies();
}
