package org.bito.liquor.controller;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.common.dto.LiquorDto;
import org.bito.liquor.common.dto.PriceHistoryDto;
import org.bito.liquor.common.model.LiquorPrice;
import org.bito.liquor.service.LiquorQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/liquors")
@RequiredArgsConstructor
public class LiquorController {

    private final LiquorQueryService liquorQueryService;

    @GetMapping
    public ResponseEntity<List<LiquorDto>> getAllLiquors() {
        List<LiquorPrice> liquors = liquorQueryService.getAllLiquors();
        return ResponseEntity.ok(liquors.stream().map(LiquorDto::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiquorDto> getLiquorById(@PathVariable Long id) {
        return liquorQueryService.getLiquorById(id)
                .map(liquor -> ResponseEntity.ok(LiquorDto.from(liquor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<LiquorDto>> searchLiquors(@RequestParam("q") String keyword) {
        List<LiquorPrice> liquors = liquorQueryService.searchLiquors(keyword);
        return ResponseEntity.ok(liquors.stream().map(LiquorDto::from).toList());
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<LiquorDto>> getLiquorsByBrand(@PathVariable String brand) {
        List<LiquorPrice> liquors = liquorQueryService.getLiquorsByBrand(brand);
        return ResponseEntity.ok(liquors.stream().map(LiquorDto::from).toList());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<LiquorDto>> getLiquorsByCategory(@PathVariable String category) {
        List<LiquorPrice> liquors = liquorQueryService.getLiquorsByCategory(category);
        return ResponseEntity.ok(liquors.stream().map(LiquorDto::from).toList());
    }

    @GetMapping("/cheap")
    public ResponseEntity<List<LiquorDto>> getCheapLiquors(
            @RequestParam(value = "max", defaultValue = "50000") Integer maxPrice) {
        List<LiquorPrice> liquors = liquorQueryService.getCheapLiquors(maxPrice);
        return ResponseEntity.ok(liquors.stream().map(LiquorDto::from).toList());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<PriceHistoryDto>> getPriceHistory(@PathVariable Long id) {
        List<PriceHistoryDto> history = liquorQueryService.getPriceHistory(id)
                .stream().map(PriceHistoryDto::from).toList();
        return ResponseEntity.ok(history);
    }
}
