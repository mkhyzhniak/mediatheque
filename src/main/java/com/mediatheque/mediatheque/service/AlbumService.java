package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.dto.AlbumDTO;
import com.mediatheque.mediatheque.model.Album;
import com.mediatheque.mediatheque.model.Artist;
import com.mediatheque.mediatheque.repository.AlbumRepository;
import com.mediatheque.mediatheque.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    public List<AlbumDTO> getAllAlbums() {
        return albumRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AlbumDTO getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        return toDTO(album);
    }

    public AlbumDTO createAlbum(AlbumDTO dto) {
        Artist artist = findOrCreateArtist(dto.getArtistName());
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

    public void deleteAlbum(Long id) {
        albumRepository.deleteById(id);
    }

    private Artist findOrCreateArtist(String name) {
        return artistRepository.findAll()
                .stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Artist newArtist = new Artist();
                    newArtist.setName(name);
                    return artistRepository.save(newArtist);
                });
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
        dto.setArtistId(album.getArtist().getId());
        return dto;
    }
}
