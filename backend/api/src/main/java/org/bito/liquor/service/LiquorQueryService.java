package org.bito.liquor.service;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.common.model.Liquor;
import org.bito.liquor.common.model.PriceHistory;
import org.bito.liquor.common.repository.LiquorRepository;
import org.bito.liquor.common.repository.PriceHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LiquorQueryService {

    private final LiquorRepository liquorRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public List<Liquor> getAllLiquors() {
        return liquorRepository.findAllByOrderByUpdatedAtDesc();
    }

    public Optional<Liquor> getLiquorById(Long id) {
        return liquorRepository.findById(id);
    }

    public List<Liquor> searchLiquors(String keyword) {
        return liquorRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Liquor> getLiquorsByBrand(String brand) {
        return liquorRepository.findByBrand(brand);
    }

    public List<Liquor> getLiquorsByCategory(String category) {
        return liquorRepository.findByCategory(category);
    }

    public List<Liquor> getCheapLiquors(Integer maxPrice) {
        return liquorRepository.findByCurrentPriceLessThanOrderByCurrentPriceAsc(maxPrice);
    }

    public List<PriceHistory> getPriceHistory(Long liquorId) {
        return priceHistoryRepository.findByLiquorIdOrderByRecordedAtDesc(liquorId);
    }

    public List<PriceHistory> getPriceHistoryBetween(Long liquorId, LocalDateTime start, LocalDateTime end) {
        return priceHistoryRepository.findByLiquorIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                liquorId, start, end);
    }
}
