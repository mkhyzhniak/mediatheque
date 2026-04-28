package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.dto.CollectionEntryDTO;
import com.mediatheque.mediatheque.model.Album;
import com.mediatheque.mediatheque.model.CollectionEntry;
import com.mediatheque.mediatheque.model.User;
import com.mediatheque.mediatheque.repository.AlbumRepository;
import com.mediatheque.mediatheque.repository.CollectionEntryRepository;
import com.mediatheque.mediatheque.repository.UserRepository;
import com.mediatheque.mediatheque.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CollectionEntryService {

    private final CollectionEntryRepository collectionEntryRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new RuntimeException("Not authenticated");
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<CollectionEntryDTO> getCollection(String sort, List<String> formats, List<String> artists) {
        User user = getCurrentUser();
        List<CollectionEntryDTO> list = collectionEntryRepository.findByUserAndInWishlistFalse(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        list = applyFilters(list, formats, artists);
        return sortEntries(list, sort);
    }

    public List<String> getCollectionArtists() {
        User user = getCurrentUser();
        return collectionEntryRepository.findByUserAndInWishlistFalse(user).stream()
                .filter(e -> e.getAlbum() != null && e.getAlbum().getArtist() != null)
                .map(e -> e.getAlbum().getArtist().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<CollectionEntryDTO> getWishlist(String sort, List<String> formats, List<String> artists) {
        User user = getCurrentUser();
        List<CollectionEntryDTO> list = collectionEntryRepository.findByUserAndInWishlistTrue(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        list = applyFilters(list, formats, artists);
        return sortEntries(list, sort);
    }

    public List<String> getWishlistArtists() {
        User user = getCurrentUser();
        return collectionEntryRepository.findByUserAndInWishlistTrue(user).stream()
                .map(e -> e.getAlbum().getArtist().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public CollectionEntryDTO getEntryById(Long id) {
        CollectionEntry entry = collectionEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        return toDTO(entry);
    }

    public void createEntry(CollectionEntryDTO dto) {
        User user = getCurrentUser();
        Album album = albumRepository.findById(dto.getAlbumId())
                .orElseThrow(() -> new RuntimeException("Album not found"));
        CollectionEntry entry = new CollectionEntry();
        entry.setFormat(dto.getFormat());
        entry.setCondition(dto.getCondition());
        entry.setNotes(dto.getNotes());
        entry.setAddedDate(dto.getAddedDate() != null ? dto.getAddedDate() : LocalDate.now());
        entry.setInWishlist(dto.getInWishlist());
        entry.setAlbum(album);
        entry.setUser(user);
        collectionEntryRepository.save(entry);
    }

    public void updateEntry(Long id, CollectionEntryDTO dto) {
        CollectionEntry entry = collectionEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        entry.setFormat(dto.getFormat());
        entry.setCondition(dto.getCondition());
        entry.setNotes(dto.getNotes());
        if (dto.getAddedDate() != null) {
            entry.setAddedDate(dto.getAddedDate());
        }
        entry.setInWishlist(dto.getInWishlist());
        collectionEntryRepository.save(entry);
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
            Album album = albumRepository.findById(albumId).orElse(null);
            Long artistId = album != null ? album.getArtist().getId() : null;
            albumRepository.deleteById(albumId);

            if (artistId != null) {
                long artistAlbums = albumRepository.findAll().stream()
                        .filter(a -> a.getArtist().getId().equals(artistId))
                        .count();
                if (artistAlbums == 0) {
                    artistRepository.deleteById(artistId);
                }
            }
        }
    }

    public void moveToCollection(Long id) {
        CollectionEntry entry = collectionEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        entry.setInWishlist(false);
        collectionEntryRepository.save(entry);
    }

    private List<CollectionEntryDTO> sortEntries(List<CollectionEntryDTO> list, String sort) {
        if (sort == null) return list;
        return switch (sort) {
            case "DATE_NEW" -> list.stream().sorted(Comparator.comparing(CollectionEntryDTO::getAddedDate, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
            case "DATE_OLD" -> list.stream().sorted(Comparator.comparing(CollectionEntryDTO::getAddedDate, Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList());
            case "ARTIST_AZ" -> list.stream().sorted(Comparator.comparing(CollectionEntryDTO::getArtistName, Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList());
            case "ARTIST_ZA" -> list.stream().sorted(Comparator.comparing(CollectionEntryDTO::getArtistName, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
            case "ALBUM_AZ" -> list.stream().sorted(Comparator.comparing(CollectionEntryDTO::getAlbumTitle, Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList());
            case "ALBUM_ZA" -> list.stream().sorted(Comparator.comparing(CollectionEntryDTO::getAlbumTitle, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
            default -> list;
        };
    }

    private List<CollectionEntryDTO> applyFilters(List<CollectionEntryDTO> list, List<String> formats, List<String> artists) {
        if (formats != null && !formats.isEmpty()) {
            list = list.stream().filter(e -> e.getFormat() != null && formats.contains(e.getFormat().name())).collect(Collectors.toList());
        }
        if (artists != null && !artists.isEmpty()) {
            list = list.stream().filter(e -> e.getArtistName() != null && artists.contains(e.getArtistName())).collect(Collectors.toList());
        }
        return list;
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
        dto.setGenre(entry.getAlbum().getGenre());
        dto.setLabel(entry.getAlbum().getLabel());
        dto.setYear(entry.getAlbum().getYear());
        return dto;
    }
}
