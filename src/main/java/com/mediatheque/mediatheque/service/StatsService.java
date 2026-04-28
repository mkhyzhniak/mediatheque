package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.dto.CollectionEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final CollectionEntryService collectionEntryService;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        List<CollectionEntryDTO> collection = collectionEntryService.getCollection("DATE_NEW", null, null);
        List<CollectionEntryDTO> wishlist = collectionEntryService.getWishlist("DATE_NEW", null, null);

        stats.put("totalItems", collection.size());
        stats.put("wishlistItems", wishlist.size());

        long artists = collection.stream().map(CollectionEntryDTO::getArtistName).distinct().count();
        long albums = collection.stream().map(CollectionEntryDTO::getAlbumTitle).distinct().count();
        stats.put("totalArtists", artists);
        stats.put("totalAlbums", albums);

        Map<String, Long> byFormat = collection.stream()
                .collect(Collectors.groupingBy(e -> e.getFormat() != null ? e.getFormat().name() : "UNKNOWN", Collectors.counting()));
        stats.put("vinylCount", byFormat.getOrDefault("VINYL", 0L));
        stats.put("cassetteCount", byFormat.getOrDefault("CASSETTE", 0L));
        stats.put("cdCount", byFormat.getOrDefault("CD", 0L));

        Map<String, Long> byGenre = collection.stream()
                .filter(e -> e.getGenre() != null && !e.getGenre().isEmpty())
                .collect(Collectors.groupingBy(CollectionEntryDTO::getGenre, Collectors.counting()));
        List<Map.Entry<String, Long>> topGenres = byGenre.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(4)
                .collect(Collectors.toList());
        stats.put("topGenres", topGenres);

        Map<String, Long> byArtist = collection.stream()
                .collect(Collectors.groupingBy(CollectionEntryDTO::getArtistName, Collectors.counting()));
        Map.Entry<String, Long> topArtist = byArtist.entrySet().stream()
                .max(Map.Entry.comparingByValue()).orElse(null);
        stats.put("topArtist", topArtist != null ? topArtist.getKey() : "—");
        stats.put("topArtistCount", topArtist != null ? topArtist.getValue() : 0);

        String topFormat = byFormat.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("—");
        long topFormatCount = byFormat.values().stream().max(Long::compare).orElse(0L);
        stats.put("topFormat", topFormat);
        stats.put("topFormatCount", topFormatCount);

        String topGenre = topGenres.isEmpty() ? "—" : topGenres.getFirst().getKey();
        long topGenreCount = topGenres.isEmpty() ? 0 : topGenres.getFirst().getValue();
        stats.put("topGenre", topGenre);
        stats.put("topGenreCount", topGenreCount);

        Map<String, Long> wishByArtist = wishlist.stream()
                .collect(Collectors.groupingBy(CollectionEntryDTO::getArtistName, Collectors.counting()));
        Map.Entry<String, Long> wishTopArtist = wishByArtist.entrySet().stream()
                .max(Map.Entry.comparingByValue()).orElse(null);
        stats.put("wishTopArtist", wishTopArtist != null ? wishTopArtist.getKey() : "—");
        stats.put("wishTopArtistCount", wishTopArtist != null ? wishTopArtist.getValue() : 0);

        Map<String, Long> wishByGenre = wishlist.stream()
                .filter(e -> e.getGenre() != null && !e.getGenre().isEmpty())
                .collect(Collectors.groupingBy(CollectionEntryDTO::getGenre, Collectors.counting()));
        Map.Entry<String, Long> wishTopGenre = wishByGenre.entrySet().stream()
                .max(Map.Entry.comparingByValue()).orElse(null);
        List<Map.Entry<String, Long>> wishTopGenres = wishByGenre.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(4)
                .collect(Collectors.toList());
        stats.put("wishTopGenres", wishTopGenres);
        stats.put("wishTopGenre", wishTopGenre != null ? wishTopGenre.getKey() : "—");
        stats.put("wishTopGenreCount", wishTopGenre != null ? wishTopGenre.getValue() : 0);

        return stats;
    }
}
