package com.mediatheque.mediatheque.dto;

import com.mediatheque.mediatheque.model.Format;
import com.mediatheque.mediatheque.model.Condition;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEntryDTO {
    private Long id;
    private Format format;
    private Condition condition;
    private String notes;
    private LocalDate addedDate;
    private Boolean inWishlist;
    private Long albumId;
    private String albumTitle;
    private String artistName;
    private String coverUrl;
}