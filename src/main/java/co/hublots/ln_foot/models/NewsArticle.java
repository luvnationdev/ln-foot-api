package co.hublots.ln_foot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "news_articles", schema = "lnfoot_web")
public class NewsArticle {

    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "summary", nullable = false)
    private String summary;

    @Builder.Default
    @Column(name = "is_major_update", nullable = false)
    private Boolean isMajorUpdate = false; // Indicates if this is a significant news update

    @Lob
    @Column(nullable = false)
    private String content; // Can be HTML or Markdown

    @Column(name = "author_name") // Used if not linking directly to a User entity, or as a fallback
    private String authorName;

    @Column(name = "publication_date")
    private LocalDateTime publicationDate;

    @Column(name = "source_url", length = 2048) // URLs can be long
    private String sourceUrl;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private NewsCategory category;

    @Enumerated(EnumType.STRING)
    private NewsStatus status;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "news_article_tags", schema = "lnfoot_web", joinColumns = @JoinColumn(name = "news_article_id"))
    @Column(name = "tag")
    private List<String> tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum NewsCategory {
        GENERAL, TRANSFERS, MATCH_PREVIEW, MATCH_REVIEW, INJURY_UPDATE, OTHER
    }

    public enum NewsStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
