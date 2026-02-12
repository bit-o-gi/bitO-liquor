package org.bito.liquor.common.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "liquors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Liquor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String productCode;

    private String name;

    private String brand;

    private String category;

    private Integer volume;

    private Double alcoholPercent;

    private String country;

    private Integer currentPrice;

    private Integer originalPrice;

    @Column(length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String productUrl;

    private String source;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getDiscountPercent() {
        if (originalPrice != null && originalPrice > 0 && currentPrice != null) {
            return (int) ((1 - (double) currentPrice / originalPrice) * 100);
        }
        return 0;
    }
}
