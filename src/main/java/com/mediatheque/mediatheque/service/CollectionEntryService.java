package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.dto.CollectionEntryDTO;
import com.mediatheque.mediatheque.model.Album;
import com.mediatheque.mediatheque.model.CollectionEntry;
import com.mediatheque.mediatheque.repository.AlbumRepository;
import com.mediatheque.mediatheque.repository.CollectionEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionEntryService {

    private final CollectionEntryRepository collectionEntryRepository;
    private final AlbumRepository albumRepository;

    public List<CollectionEntryDTO> getCollection() {
        return collectionEntryRepository.findAll()
                .stream()
                .filter(e -> e.getInWishlist() == null || !e.getInWishlist())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CollectionEntryDTO> getWishlist() {
        return collectionEntryRepository.findAll()
                .stream()
                .filter(e -> e.getInWishlist() != null && e.getInWishlist())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CollectionEntryDTO getEntryById(Long id) {
        CollectionEntry entry = collectionEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        return toDTO(entry);
    }

    public CollectionEntryDTO createEntry(CollectionEntryDTO dto) {
        Album album = albumRepository.findById(dto.getAlbumId())
                .orElseThrow(() -> new RuntimeException("Album not found"));
        CollectionEntry entry = new CollectionEntry();
        entry.setFormat(dto.getFormat());
        entry.setCondition(dto.getCondition());
        entry.setNotes(dto.getNotes());
        entry.setAddedDate(dto.getAddedDate() != null ? dto.getAddedDate() : LocalDate.now());
        entry.setInWishlist(dto.getInWishlist());
        entry.setAlbum(album);
        CollectionEntry saved = collectionEntryRepository.save(entry);
        return toDTO(saved);
    }

    public CollectionEntryDTO updateEntry(Long id, CollectionEntryDTO dto) {
        CollectionEntry entry = collectionEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        entry.setFormat(dto.getFormat());
        entry.setCondition(dto.getCondition());
        entry.setNotes(dto.getNotes());
        entry.setInWishlist(dto.getInWishlist());
        CollectionEntry saved = collectionEntryRepository.save(entry);
        return toDTO(saved);
    }

    public void deleteEntry(Long id) {
        CollectionEntry entry = collectionEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        Long albumId = entry.getAlbum().getId();
        collectionEntryRepository.deleteById(id);

        long remaining = collectionEntryRepository.findAll()
                .stream()
                .filter(e -> e.getAlbum().getId().equals(albumId))
                .count();

        if (remaining == 0) {
            albumRepository.deleteById(albumId);
        }
    }

    public CollectionEntryDTO moveToCollection(Long id) {
        CollectionEntry entry = collectionEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        entry.setInWishlist(false);
        CollectionEntry saved = collectionEntryRepository.save(entry);
        return toDTO(saved);
    }

    private CollectionEntryDTO toDTO(CollectionEntry entry) {
        CollectionEntryDTO dto = new CollectionEntryDTO();
        dto.setId(entry.getId());
        dto.setFormat(entry.getFormat());
        dto.setCondition(entry.getCondition());
        dto.setNotes(entry.getNotes());
        dto.setAddedDate(entry.getAddedDate());
        dto.setInWishlist(entry.getInWishlist());
        dto.setAlbumId(entry.getAlbum().getId());
        dto.setAlbumTitle(entry.getAlbum().getTitle());
        dto.setArtistName(entry.getAlbum().getArtist().getName());
        dto.setCoverUrl(entry.getAlbum().getCoverUrl());
        return dto;
    }
}
