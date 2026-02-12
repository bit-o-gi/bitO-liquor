package org.bito.liquor.controller;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.common.dto.LiquorDto;
import org.bito.liquor.common.model.Liquor;
import org.bito.liquor.service.LiquorQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/whisky")
@RequiredArgsConstructor
public class WhiskyController {

    private final LiquorQueryService liquorQueryService;

    @GetMapping("/lowest")
    public List<LiquorDto> getLowestPriceWhiskies() {
        List<Liquor> liquors = liquorQueryService.getLowestPriceWhiskies();
        return liquors.stream().map(LiquorDto::from).toList();
    }
}
