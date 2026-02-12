package org.bito.liquor.common.repository;

import org.bito.liquor.common.model.LiquorPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiquorPriceRepository extends JpaRepository<LiquorPrice, Long> {

    Optional<LiquorPrice> findByLiquorIdAndSource(Long liquorId, String source);

    @Query("SELECT lp FROM LiquorPrice lp JOIN FETCH lp.liquor l ORDER BY lp.crawledAt DESC")
    List<LiquorPrice> findAllOrderByUpdatedAtDesc();

    @Query("""
            SELECT lp
            FROM LiquorPrice lp
            JOIN FETCH lp.liquor l
            WHERE LOWER(COALESCE(l.productName, l.normalizedName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY lp.crawledAt DESC
            """)
    List<LiquorPrice> searchByKeyword(String keyword);
}
