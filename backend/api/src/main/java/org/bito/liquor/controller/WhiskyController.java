package org.bito.liquor.controller;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.model.Whisky;
import org.bito.liquor.service.WhiskyQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/whisky")
@RequiredArgsConstructor
public class WhiskyController {

    private final WhiskyQueryService whiskyQueryService;

    @GetMapping("/lowest")
    public List<Whisky> getLowestPriceWhiskies() {
        return whiskyQueryService.getLowestPriceWhiskies();
    }
}
