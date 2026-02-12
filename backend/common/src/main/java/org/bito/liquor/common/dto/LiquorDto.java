package org.bito.liquor.common.dto;

import lombok.Builder;
import lombok.Data;
import org.bito.liquor.common.model.Liquor;
import org.bito.liquor.common.model.LiquorPrice;

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
    private String fullname;
    private String clazz;
    private LocalDateTime updatedAt;

    public static LiquorDto from(LiquorPrice price) {
        Liquor liquor = price.getLiquor();
        return LiquorDto.builder()
                .id(price.getId())
                .productCode(liquor.getProductCode())
                .name(liquor.getName())
                .brand(liquor.getBrand())
                .category(liquor.getCategory())
                .volume(liquor.getVolume())
                .alcoholPercent(liquor.getAlcoholPercent())
                .country(liquor.getCountry())
                .currentPrice(price.getCurrentPrice())
                .originalPrice(price.getOriginalPrice())
                .discountPercent(price.getDiscountPercent())
                .imageUrl(liquor.getImageUrl())
                .productUrl(liquor.getProductUrl())
                .source(price.getSource())
                .fullname(liquor.getProductName())
                .clazz(liquor.getClazz())
                .updatedAt(price.getCrawledAt())
                .build();
    }
}
