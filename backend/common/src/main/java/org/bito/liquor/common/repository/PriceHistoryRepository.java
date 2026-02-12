package org.bito.liquor.common.repository;

import org.bito.liquor.common.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByLiquorIdOrderByRecordedAtDesc(Long liquorId);

    List<PriceHistory> findByLiquorIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long liquorId, LocalDateTime start, LocalDateTime end);
}
