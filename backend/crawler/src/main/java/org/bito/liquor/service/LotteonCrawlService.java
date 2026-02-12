package org.bito.liquor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bito.liquor.common.model.Liquor;
import org.bito.liquor.common.model.LiquorPrice;
import org.bito.liquor.common.repository.LiquorRepository;
import org.bito.liquor.common.repository.LiquorPriceRepository;
import org.bito.liquor.scraper.LotteonScraper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotteonCrawlService {
    private static final String NORMALIZED_SOURCE = "LOTTEON";

    private final LiquorRepository liquorRepository;
    private final LiquorPriceRepository liquorPriceRepository;
    private final LotteonScraper lotteonScraper;

    @Transactional
    public List<LiquorPrice> scrapeLiquors() {
        log.info("롯데온 양주 크롤링 시작");

        List<Liquor> scrapedLiquors = lotteonScraper.scrapeLiquors();

        int newCount = 0;
        int updateCount = 0;

        for (Liquor scraped : scrapedLiquors) {
            normalizeLiquor(scraped, NORMALIZED_SOURCE);

            Liquor liquor = upsertLiquor(scraped);
            boolean existed = liquorPriceRepository.findByLiquorIdAndSource(liquor.getId(), NORMALIZED_SOURCE).isPresent();
            upsertLiquorPrice(liquor, scraped);

            if (existed) {
                updateCount++;
            } else {
                newCount++;
            }
        }

        log.info("롯데온 크롤링 완료 - 신규: {}개, 업데이트: {}개", newCount, updateCount);

        return liquorPriceRepository.findAllOrderByUpdatedAtDesc();
    }

    private void normalizeLiquor(Liquor liquor, String source) {
        liquor.setSource(source);
        liquor.setNormalizedName(liquor.getName() == null ? null : liquor.getName().trim().toLowerCase());
        if (liquor.getCurrentPrice() != null && liquor.getCurrentPrice() <= 0) {
            liquor.setCurrentPrice(null);
        }
        if (liquor.getOriginalPrice() != null && liquor.getOriginalPrice() <= 0) {
            liquor.setOriginalPrice(null);
        }
        if (liquor.getCurrentPrice() == null && liquor.getOriginalPrice() != null) {
            liquor.setCurrentPrice(liquor.getOriginalPrice());
        }
        if (liquor.getOriginalPrice() == null && liquor.getCurrentPrice() != null) {
            liquor.setOriginalPrice(liquor.getCurrentPrice());
        }
        if (liquor.getCurrentPrice() != null && liquor.getOriginalPrice() != null
                && liquor.getOriginalPrice() < liquor.getCurrentPrice()) {
            liquor.setOriginalPrice(liquor.getCurrentPrice());
        }
    }

    private Liquor upsertLiquor(Liquor scraped) {
        String normalizedName = scraped.getNormalizedName();
        String clazz = scraped.getClazz() == null ? "" : scraped.getClazz();
        Integer volume = scraped.getVolume() == null ? 0 : scraped.getVolume();

        Liquor liquor = liquorRepository
                .findByNormalizedNameAndClazzAndVolume(normalizedName, clazz, volume)
                .orElseGet(Liquor::new);

        liquor.setNormalizedName(normalizedName);
        liquor.setName(scraped.getName());
        liquor.setBrand(scraped.getBrand());
        liquor.setCategory(scraped.getCategory());
        liquor.setCountry(scraped.getCountry());
        liquor.setAlcoholPercent(scraped.getAlcoholPercent());
        liquor.setVolume(volume);
        liquor.setClazz(clazz);
        liquor.setProductCode(scraped.getProductCode());
        liquor.setProductName(scraped.getFullname() == null ? scraped.getName() : scraped.getFullname());
        liquor.setProductUrl(scraped.getProductUrl());
        liquor.setImageUrl(scraped.getImageUrl());

        return liquorRepository.save(liquor);
    }

    private LiquorPrice upsertLiquorPrice(Liquor liquor, Liquor scraped) {
        LiquorPrice price = liquorPriceRepository
                .findByLiquorIdAndSource(liquor.getId(), NORMALIZED_SOURCE)
                .orElseGet(() -> LiquorPrice.builder()
                        .liquor(liquor)
                        .source(NORMALIZED_SOURCE)
                        .build());

        price.setLiquor(liquor);
        price.setSource(NORMALIZED_SOURCE);
        price.setCurrentPrice(scraped.getCurrentPrice());
        price.setOriginalPrice(scraped.getOriginalPrice());
        return liquorPriceRepository.save(price);
    }
}
