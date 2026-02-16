package org.bito.liquor.common.repository;

import org.bito.liquor.common.model.Whisky;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WhiskyRepository extends JpaRepository<Whisky, Long> {
    Optional<Whisky> findBySeedKey(String seedKey);

    List<Whisky> findAllByOrderByUpdatedAtDesc();

    @Query("""
            SELECT w
            FROM Whisky w
            WHERE LOWER(COALESCE(w.productName, w.normalizedName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(w.brand, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY w.updatedAt DESC
            """)
    List<Whisky> searchByKeyword(@Param("keyword") String keyword);
}
