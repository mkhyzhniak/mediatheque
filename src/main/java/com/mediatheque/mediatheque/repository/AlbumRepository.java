package com.mediatheque.mediatheque.repository;

import com.mediatheque.mediatheque.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
}