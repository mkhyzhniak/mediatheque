package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.dto.AlbumDTO;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiscogsService {

    @Value("${discogs.token}")
    private String token;

    @Value("${discogs.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<AlbumDTO> searchAlbums(String query) {
        String url = baseUrl + "/database/search?q=" + query + "&type=release&per_page=20";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Discogs token=" + token);
        headers.set("User-Agent", "Mediatheque/1.0");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        List<AlbumDTO> results = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode resultsNode = root.get("results");

            if (resultsNode != null && resultsNode.isArray()) {
                for (JsonNode item : resultsNode) {
                    String detectedFormat = detectFormat(item);
                    if (detectedFormat == null) continue;

                    AlbumDTO dto = new AlbumDTO();

                    String title = item.has("title") ? item.get("title").asText() : "";
                    if (title.contains(" - ")) {
                        String[] parts = title.split(" - ", 2);
                        dto.setArtistName(parts[0].trim());
                        dto.setTitle(parts[1].trim());
                    } else {
                        dto.setArtistName("Unknown");
                        dto.setTitle(title);
                    }

                    dto.setYear(item.has("year") ? item.get("year").asInt(0) : 0);
                    dto.setGenre(item.has("genre") && item.get("genre").isArray() && !item.get("genre").isEmpty()
                            ? item.get("genre").get(0).asText() : "");
                    dto.setLabel(item.has("label") && item.get("label").isArray() && !item.get("label").isEmpty()
                            ? item.get("label").get(0).asText() : "");
                    dto.setDiscogsId(item.has("id") ? String.valueOf(item.get("id").asInt()) : "");
                    dto.setCoverUrl(item.has("cover_image") ? item.get("cover_image").asText() : "");
                    dto.setDiscogsFormat(detectedFormat);

                    results.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private String detectFormat(JsonNode item) {
        if (!item.has("format") || !item.get("format").isArray()) return null;

        for (JsonNode f : item.get("format")) {
            String format = f.asText().toUpperCase();
            if (format.contains("CASS") || format.contains("TAPE")) return "CASSETTE";
            if (format.contains("VINYL") || format.equals("LP") || format.equals("12\"") || format.equals("7\"")) return "VINYL";
            if (format.equals("CD") || format.contains("COMPACT DISC")) return "CD";
        }
        return null;
    }
}