package org.bito.liquor.common.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "liquor_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquorPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liquor_id", nullable = false)
    private Liquor liquor;

    @Column(nullable = false)
    private String source;

    @Column(name = "current_price")
    private Integer currentPrice;

    @Column(name = "original_price")
    private Integer originalPrice;

    @Column(name = "crawled_at")
    private LocalDateTime crawledAt;

    @PrePersist
    protected void onCreate() {
        if (crawledAt == null) {
            crawledAt = LocalDateTime.now();
        }
    }

    public Integer getDiscountPercent() {
        if (originalPrice != null && originalPrice > 0 && currentPrice != null) {
            return (int) ((1 - (double) currentPrice / originalPrice) * 100);
        }
        return 0;
    }
}
