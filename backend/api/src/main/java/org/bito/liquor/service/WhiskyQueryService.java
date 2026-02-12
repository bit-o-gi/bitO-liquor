package org.bito.liquor.service;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.model.Whisky;
import org.bito.liquor.repository.WhiskyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WhiskyQueryService {

    private final WhiskyRepository whiskyRepository;

    public List<Whisky> getLowestPriceWhiskies() {
        return whiskyRepository.findLowestPriceWhiskies();
    }
}
