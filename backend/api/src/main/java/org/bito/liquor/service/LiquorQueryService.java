package org.bito.liquor.service;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.common.model.LiquorPrice;
import org.bito.liquor.common.repository.LiquorPriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LiquorQueryService {

    private final LiquorPriceRepository liquorPriceRepository;

    public List<LiquorPrice> getAllLiquors() {
        return liquorPriceRepository.findAllOrderByUpdatedAtDesc();
    }

    public Optional<LiquorPrice> getLiquorById(Long id) {
        return liquorPriceRepository.findById(id);
    }

    public List<LiquorPrice> searchLiquors(String keyword) {
        return liquorPriceRepository.searchByKeyword(keyword);
    }

    public List<LiquorPrice> getLiquorsByBrand(String brand) {
        return liquorPriceRepository.findByBrand(brand);
    }

    public List<LiquorPrice> getLiquorsByCategory(String category) {
        return liquorPriceRepository.findByCategory(category);
    }

    public List<LiquorPrice> getCheapLiquors(Integer maxPrice) {
        return liquorPriceRepository.findCheapLiquors(maxPrice);
    }

    public List<LiquorPrice> getLowestPriceWhiskies() {
        return liquorPriceRepository.findLowestPriceWhiskies();
    }

    public List<LiquorPrice> getPriceHistory(Long liquorId) {
        return liquorPriceRepository.findByLiquorIdOrderByCrawledAtDesc(liquorId);
    }

    public List<LiquorPrice> getPriceHistoryBetween(Long liquorId, LocalDateTime start, LocalDateTime end) {
        return liquorPriceRepository.findByLiquorIdAndCrawledAtBetweenOrderByCrawledAtAsc(
                liquorId, start, end);
    }
}
