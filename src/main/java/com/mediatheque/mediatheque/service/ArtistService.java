package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.model.Artist;
import com.mediatheque.mediatheque.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    public Artist findOrCreateArtist(String name) {
        return artistRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Artist newArtist = new Artist();
                    newArtist.setName(name);
                    return artistRepository.save(newArtist);
                });
    }
}
