package org.bito.liquor.common.dto;

import lombok.Builder;
import lombok.Data;
import org.bito.liquor.common.model.LiquorPrice;

import java.time.LocalDateTime;

@Data
@Builder
public class PriceHistoryDto {

    private Long id;
    private Integer price;
    private LocalDateTime recordedAt;

    public static PriceHistoryDto from(LiquorPrice history) {
        return PriceHistoryDto.builder()
                .id(history.getId())
                .price(history.getCurrentPrice())
                .recordedAt(history.getCrawledAt())
                .build();
    }
}
