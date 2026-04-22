package com.mediatheque.mediatheque.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {
    private Long id;
    private String title;
    private Integer year;
    private String genre;
    private String label;
    private String discogsId;
    private String discogsFormat;
    private String coverUrl;
    private String artistName;
    private Long artistId;
}
