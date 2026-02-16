package org.bito.liquor.controller;

import lombok.RequiredArgsConstructor;
import org.bito.liquor.common.dto.WhiskyImageUpdateResponseDto;
import org.bito.liquor.common.dto.WhiskyImageUpsertRequestDto;
import org.bito.liquor.common.model.Whisky;
import org.bito.liquor.service.WhiskyImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/whiskies")
@RequiredArgsConstructor
public class WhiskyController {

    private final WhiskyImageService whiskyImageService;

    @PatchMapping("/image")
    public ResponseEntity<WhiskyImageUpdateResponseDto> upsertImage(
            @RequestBody WhiskyImageUpsertRequestDto request
    ) {
        try {
            Whisky updated = whiskyImageService.upsertImage(request);
            return ResponseEntity.ok(WhiskyImageUpdateResponseDto.from(updated));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
