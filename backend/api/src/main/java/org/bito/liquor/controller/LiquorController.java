package org.bito.liquor.controller;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.common.dto.FlavorVectorRequestDto;
import org.bito.liquor.common.dto.LiquorDto;
import org.bito.liquor.common.dto.WhiskyRecommendationResponseDto;
import org.bito.liquor.common.model.LiquorPrice;
import org.bito.liquor.service.LiquorQueryService;
import org.bito.liquor.service.WhiskyRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/liquors")
@RequiredArgsConstructor
public class LiquorController {

    private final LiquorQueryService liquorQueryService;
    private final WhiskyRecommendationService whiskyRecommendationService;

    @GetMapping
    public ResponseEntity<List<LiquorDto>> getAllLiquors() {
        List<LiquorPrice> liquors = liquorQueryService.getAllLiquors();
        return ResponseEntity.ok(liquors.stream().map(LiquorDto::from).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<LiquorDto>> searchLiquors(@RequestParam("q") String keyword) {
        List<LiquorPrice> liquors = liquorQueryService.searchLiquors(keyword);
        return ResponseEntity.ok(liquors.stream().map(LiquorDto::from).toList());
    }

    @PostMapping("/recommendations")
    public ResponseEntity<WhiskyRecommendationResponseDto> recommendLiquors(
            @RequestBody FlavorVectorRequestDto request
    ) {
        return ResponseEntity.ok(whiskyRecommendationService.recommend(request));
    }
}
