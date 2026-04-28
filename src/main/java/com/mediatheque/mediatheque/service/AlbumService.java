package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.dto.AlbumDTO;
import com.mediatheque.mediatheque.model.Album;
import com.mediatheque.mediatheque.model.Artist;
import com.mediatheque.mediatheque.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistService artistService;

    public AlbumDTO getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        return toDTO(album);
    }

    public AlbumDTO createAlbum(AlbumDTO dto) {
        if (dto.getDiscogsId() != null && !dto.getDiscogsId().isEmpty()) {
            var existing = albumRepository.findFirstByDiscogsId(dto.getDiscogsId());
            if (existing.isPresent()) {
                return toDTO(existing.get());
            }
        }

        Artist artist = artistService.findOrCreateArtist(dto.getArtistName());
        Album album = new Album();
        album.setTitle(dto.getTitle());
        album.setYear(dto.getYear());
        album.setGenre(dto.getGenre());
        album.setLabel(dto.getLabel());
        album.setDiscogsId(dto.getDiscogsId());
        album.setCoverUrl(dto.getCoverUrl());
        album.setArtist(artist);
        Album saved = albumRepository.save(album);
        return toDTO(saved);
    }

    private AlbumDTO toDTO(Album album) {
        AlbumDTO dto = new AlbumDTO();
        dto.setId(album.getId());
        dto.setTitle(album.getTitle());
        dto.setYear(album.getYear());
        dto.setGenre(album.getGenre());
        dto.setLabel(album.getLabel());
        dto.setDiscogsId(album.getDiscogsId());
        dto.setCoverUrl(album.getCoverUrl());
        dto.setArtistName(album.getArtist().getName());
        return dto;
    }
}
