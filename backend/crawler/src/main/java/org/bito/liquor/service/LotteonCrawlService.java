package org.bito.liquor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bito.liquor.common.model.Liquor;
import org.bito.liquor.common.model.PriceHistory;
import org.bito.liquor.common.repository.LiquorRepository;
import org.bito.liquor.common.repository.PriceHistoryRepository;
import org.bito.liquor.scraper.LotteonScraper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotteonCrawlService {

    private final LiquorRepository liquorRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final LotteonScraper lotteonScraper;

    @Transactional
    public List<Liquor> scrapeLiquors() {
        log.info("롯데온 양주 크롤링 시작");

        List<Liquor> scrapedLiquors = lotteonScraper.scrapeLiquors();

        int newCount = 0;
        int updateCount = 0;

        for (Liquor scraped : scrapedLiquors) {
            Optional<Liquor> existing = liquorRepository.findByProductCode(scraped.getProductCode());

            if (existing.isPresent()) {
                Liquor liquor = existing.get();

                if (liquor.getCurrentPrice() != null &&
                        !liquor.getCurrentPrice().equals(scraped.getCurrentPrice())) {
                    savePriceHistory(liquor);
                }

                liquor.setName(scraped.getName());
                liquor.setCurrentPrice(scraped.getCurrentPrice());
                liquor.setOriginalPrice(scraped.getOriginalPrice());
                liquor.setImageUrl(scraped.getImageUrl());
                liquor.setProductUrl(scraped.getProductUrl());
                liquor.setBrand(scraped.getBrand());
                liquor.setCategory(scraped.getCategory());
                liquor.setVolume(scraped.getVolume());
                liquor.setAlcoholPercent(scraped.getAlcoholPercent());
                liquor.setCountry(scraped.getCountry());

                liquorRepository.save(liquor);
                updateCount++;
            } else {
                liquorRepository.save(scraped);
                newCount++;
            }
        }

        log.info("롯데온 크롤링 완료 - 신규: {}개, 업데이트: {}개", newCount, updateCount);

        return liquorRepository.findAllByOrderByUpdatedAtDesc();
    }

    @Transactional
    public void savePriceHistory(Liquor liquor) {
        if (liquor.getCurrentPrice() != null) {
            PriceHistory history = PriceHistory.builder()
                    .liquor(liquor)
                    .price(liquor.getCurrentPrice())
                    .recordedAt(LocalDateTime.now())
                    .build();
            priceHistoryRepository.save(history);
            log.debug("가격 이력 저장: {} - {}원", liquor.getName(), liquor.getCurrentPrice());
        }
    }
}
