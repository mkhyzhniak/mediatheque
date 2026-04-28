package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.dto.AlbumDTO;
import com.mediatheque.mediatheque.model.Format;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiscogsService {

    private static final Logger log = LoggerFactory.getLogger(DiscogsService.class);

    @Value("${discogs.token}")
    private String token;

    @Value("${discogs.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<AlbumDTO> searchAlbums(String query) {

        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/database/search")
                .queryParam("q", query)
                .queryParam("type", "release")
                .queryParam("per_page", 20)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Discogs token=" + token);
        headers.set("User-Agent", "Mediatheque/1.0");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<AlbumDTO> results = new ArrayList<>();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode resultsNode = root.get("results");

            if (resultsNode != null && resultsNode.isArray()) {
                for (JsonNode item : resultsNode) {
                    Format detectedFormat = detectFormat(item);
                    if (detectedFormat == null) continue;

                    AlbumDTO dto = new AlbumDTO();

                    String title = item.has("title") ? item.get("title").asString() : "";
                    if (title != null && title.contains(" - ")) {
                        String[] parts = title.split(" - ", 2);
                        dto.setArtistName(parts[0].trim());
                        dto.setTitle(parts[1].trim());
                    } else {
                        dto.setArtistName("Unknown");
                        dto.setTitle(title != null ? title : "");
                    }

                    dto.setYear(item.has("year") ? parseYear(item.get("year").asString()) : 0);

                    dto.setGenre(item.has("genre") && item.get("genre").isArray()
                            && !item.get("genre").isEmpty()
                            ? item.get("genre").get(0).asString() : "");

                    dto.setLabel(item.has("label") && item.get("label").isArray()
                            && !item.get("label").isEmpty()
                            ? item.get("label").get(0).asString() : "");

                    dto.setDiscogsId(item.has("id") ? item.get("id").asString() : "");

                    dto.setCoverUrl(item.has("cover_image")
                            ? item.get("cover_image").asString() : "");

                    dto.setDiscogsFormat(detectedFormat);
                    results.add(dto);
                }
            }

        } catch (HttpClientErrorException e) {
            log.error("Discogs API error: {} {}", e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error parsing Discogs response", e);
        }

        return results;
    }

    private int parseYear(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Format detectFormat(JsonNode item) {
        if (!item.has("format") || !item.get("format").isArray()) return null;

        for (JsonNode f : item.get("format")) {
            String format = f.asString();
            if (format == null) continue;
            format = format.toUpperCase();

            if (format.contains("CASS") || format.contains("TAPE")) return Format.CASSETTE;
            if (format.contains("VINYL") || format.equals("LP") || format.equals("12\"") || format.equals("7\"")) return Format.VINYL;
            if (format.equals("CD") || format.contains("COMPACT DISC")) return Format.CD;
        }
        return null;
    }
}