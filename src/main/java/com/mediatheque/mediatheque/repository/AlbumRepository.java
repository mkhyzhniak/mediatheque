package com.mediatheque.mediatheque.repository;

import com.mediatheque.mediatheque.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findFirstByDiscogsId(String discogsId);
}