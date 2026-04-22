package com.mediatheque.mediatheque.repository;

import com.mediatheque.mediatheque.model.CollectionEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionEntryRepository extends JpaRepository<CollectionEntry, Long> {
}
