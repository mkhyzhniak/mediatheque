package com.mediatheque.mediatheque.repository;

import com.mediatheque.mediatheque.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
}
