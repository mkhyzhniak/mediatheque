package com.mediatheque.mediatheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "collection_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Format format;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition")
    private Condition condition;

    private String notes;

    @Column(name = "added_date")
    private LocalDate addedDate;

    @Column(name = "in_wishlist")
    private Boolean inWishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
