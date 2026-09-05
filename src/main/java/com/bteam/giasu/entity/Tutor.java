package com.bteam.giasu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tutors")
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1-1 Relation: Mỗi Tutor tương ứng với 1 User
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "subjects", columnDefinition = "text[]")
    private List<String> subjects;

    @Column(name = "price_per_session", precision = 10, scale = 2)
    private BigDecimal pricePerSession;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating = new BigDecimal("5.0");

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "experience_years")
    private Integer experienceYears = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private   ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
