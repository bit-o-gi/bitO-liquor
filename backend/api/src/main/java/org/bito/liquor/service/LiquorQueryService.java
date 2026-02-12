package org.bito.liquor.service;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.common.model.LiquorPrice;
import org.bito.liquor.common.repository.LiquorPriceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquorQueryService {

    private final LiquorPriceRepository liquorPriceRepository;

    public List<LiquorPrice> getAllLiquors() {
        return liquorPriceRepository.findAllOrderByUpdatedAtDesc();
    }

    public List<LiquorPrice> searchLiquors(String keyword) {
        return liquorPriceRepository.searchByKeyword(keyword);
    }
}
