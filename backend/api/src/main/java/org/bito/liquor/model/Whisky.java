package org.bito.liquor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "whisky", schema = "public")
public class Whisky {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(name = "class")
    @JsonProperty("class")
    private String clazz;
    private Integer volume;
    private String brand;
    private String category;
    private String country;

    private Double alcoholPercent;
    private Integer currentPrice;
    private Integer originalPrice;

    private String imageUrl;
    private String fullname;
    private String productCode;
    private String productUrl;

    @Column(name = "source")
    private String source;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
