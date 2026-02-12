package org.bito.liquor.common.repository;

import org.bito.liquor.common.model.LiquorPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiquorPriceRepository extends JpaRepository<LiquorPrice, Long> {

    Optional<LiquorPrice> findByLiquorIdAndSource(Long liquorId, String source);

    List<LiquorPrice> findByLiquorIdOrderByCrawledAtDesc(Long liquorId);

    List<LiquorPrice> findByLiquorIdAndCrawledAtBetweenOrderByCrawledAtAsc(
            Long liquorId, LocalDateTime start, LocalDateTime end);

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

    @Query("SELECT lp FROM LiquorPrice lp JOIN FETCH lp.liquor l WHERE l.brand = :brand ORDER BY lp.crawledAt DESC")
    List<LiquorPrice> findByBrand(String brand);

    @Query("SELECT lp FROM LiquorPrice lp JOIN FETCH lp.liquor l WHERE l.category = :category ORDER BY lp.crawledAt DESC")
    List<LiquorPrice> findByCategory(String category);

    @Query("SELECT lp FROM LiquorPrice lp JOIN FETCH lp.liquor l WHERE lp.currentPrice IS NOT NULL AND lp.currentPrice <= :price ORDER BY lp.currentPrice ASC")
    List<LiquorPrice> findCheapLiquors(Integer price);

    @Query(value = """
            SELECT lp.*
            FROM liquor_price lp
            JOIN (
              SELECT liquor_id, MIN(current_price) AS min_price
              FROM liquor_price lp2
              JOIN liquor l2 ON l2.id = lp2.liquor_id
              WHERE l2.category ILIKE '%whisk%' AND lp2.current_price IS NOT NULL
              GROUP BY liquor_id
            ) x ON x.liquor_id = lp.liquor_id AND x.min_price = lp.current_price
            ORDER BY lp.current_price ASC
            """, nativeQuery = true)
    List<LiquorPrice> findLowestPriceWhiskies();
}
