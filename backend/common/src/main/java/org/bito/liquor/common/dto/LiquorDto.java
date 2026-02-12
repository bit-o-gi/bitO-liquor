package org.bito.liquor.common.dto;

import lombok.Builder;
import lombok.Data;
import org.bito.liquor.common.model.Liquor;

import java.time.LocalDateTime;

@Data
@Builder
public class LiquorDto {

    private Long id;
    private String productCode;
    private String name;
    private String brand;
    private String category;
    private Integer volume;
    private Double alcoholPercent;
    private String country;
    private Integer currentPrice;
    private Integer originalPrice;
    private Integer discountPercent;
    private String imageUrl;
    private String productUrl;
    private String source;
    private LocalDateTime updatedAt;

    public static LiquorDto from(Liquor liquor) {
        return LiquorDto.builder()
                .id(liquor.getId())
                .productCode(liquor.getProductCode())
                .name(liquor.getName())
                .brand(liquor.getBrand())
                .category(liquor.getCategory())
                .volume(liquor.getVolume())
                .alcoholPercent(liquor.getAlcoholPercent())
                .country(liquor.getCountry())
                .currentPrice(liquor.getCurrentPrice())
                .originalPrice(liquor.getOriginalPrice())
                .discountPercent(liquor.getDiscountPercent())
                .imageUrl(liquor.getImageUrl())
                .productUrl(liquor.getProductUrl())
                .source(liquor.getSource())
                .updatedAt(liquor.getUpdatedAt())
                .build();
    }
}
