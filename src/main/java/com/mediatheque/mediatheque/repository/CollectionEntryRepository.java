package com.mediatheque.mediatheque.repository;

import com.mediatheque.mediatheque.model.CollectionEntry;
import com.mediatheque.mediatheque.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionEntryRepository extends JpaRepository<CollectionEntry, Long> {
    List<CollectionEntry> findByUserAndInWishlistFalse(User user);
    List<CollectionEntry> findByUserAndInWishlistTrue(User user);
}
