package com.mediatheque.mediatheque.controller;

import com.mediatheque.mediatheque.dto.CollectionEntryDTO;
import com.mediatheque.mediatheque.model.Condition;
import com.mediatheque.mediatheque.model.Format;
import com.mediatheque.mediatheque.service.AlbumService;
import com.mediatheque.mediatheque.service.CollectionEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CollectionEntryController {

    private final CollectionEntryService collectionEntryService;
    private final AlbumService albumService;

    @GetMapping("/")
    public String getCollection(
            @RequestParam(required = false, defaultValue = "DATE_NEW") String sort,
            @RequestParam(required = false) List<String> format,
            @RequestParam(required = false) List<String> artist,
            Model model) {
        model.addAttribute("entries", collectionEntryService.getCollection(sort, format, artist));
        model.addAttribute("sort", sort);
        model.addAttribute("selectedFormats", format != null ? format : Collections.emptyList());
        model.addAttribute("selectedArtists", artist != null ? artist : Collections.emptyList());
        model.addAttribute("artists", collectionEntryService.getCollectionArtists());
        return "collection";
    }

    @GetMapping("/wishlist")
    public String getWishlist(
            @RequestParam(required = false, defaultValue = "DATE_NEW") String sort,
            @RequestParam(required = false) List<String> format,
            @RequestParam(required = false) List<String> artist,
            Model model) {
        model.addAttribute("entries", collectionEntryService.getWishlist(sort, format, artist));
        model.addAttribute("sort", sort);
        model.addAttribute("selectedFormats", format != null ? format : Collections.emptyList());
        model.addAttribute("selectedArtists", artist != null ? artist : Collections.emptyList());
        model.addAttribute("artists", collectionEntryService.getWishlistArtists());
        return "wishlist";
    }

    @GetMapping("/add/{albumId}")
    public String showAddForm(@PathVariable Long albumId,
                              @RequestParam(required = false) String format,
                              Model model) {
        model.addAttribute("album", albumService.getAlbumById(albumId));
        CollectionEntryDTO entry = new CollectionEntryDTO();
        if (format != null) {
            entry.setFormat(Format.valueOf(format));
        }
        entry.setAddedDate(java.time.LocalDate.now());
        model.addAttribute("entry", entry);
        model.addAttribute("conditions", Condition.values());
        model.addAttribute("isEdit", false);
        return "entry-form";
    }

    @PostMapping("/add/{albumId}")
    public String addEntry(@PathVariable Long albumId, @ModelAttribute CollectionEntryDTO entryDTO,
                           Model model) {
        if (entryDTO.getCondition() == null) {
            model.addAttribute("error", "Please select a condition");
            model.addAttribute("album", albumService.getAlbumById(albumId));
            model.addAttribute("entry", entryDTO);
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("isEdit", false);
            return "entry-form";
        }
        if (entryDTO.getNotes() != null && entryDTO.getNotes().length() > 255) {
            model.addAttribute("error", "Notes must be less than 255 characters");
            model.addAttribute("album", albumService.getAlbumById(albumId));
            model.addAttribute("entry", entryDTO);
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("isEdit", false);
            return "entry-form";
        }

        entryDTO.setAlbumId(albumId);
        if (entryDTO.getAddedDate() == null) {
            entryDTO.setAddedDate(java.time.LocalDate.now());
        }
        collectionEntryService.createEntry(entryDTO);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("entry", collectionEntryService.getEntryById(id));
        model.addAttribute("conditions", Condition.values());
        model.addAttribute("isEdit", true);
        return "entry-form";
    }

    @PostMapping("/edit/{id}")
    public String updateEntry(@PathVariable Long id, @ModelAttribute CollectionEntryDTO dto,
                              Model model) {
        if (dto.getCondition() == null) {
            model.addAttribute("error", "Please select a condition");
            model.addAttribute("entry", collectionEntryService.getEntryById(id));
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("isEdit", true);
            return "entry-form";
        }
        if (dto.getNotes() != null && dto.getNotes().length() > 255) {
            model.addAttribute("error", "Notes must be less than 255 characters");
            model.addAttribute("entry", collectionEntryService.getEntryById(id));
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("isEdit", true);
            return "entry-form";
        }
        collectionEntryService.updateEntry(id, dto);
        return "redirect:/";
    }

    @PostMapping("/{id}/delete")
    public String deleteEntry(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer) {
        collectionEntryService.deleteEntry(id);
        if (referer != null && referer.contains("/wishlist")) {
            return "redirect:/wishlist";
        }
        return "redirect:/";
    }

    @PostMapping("/{id}/move")
    public String moveToCollection(@PathVariable Long id) {
        collectionEntryService.moveToCollection(id);
        CollectionEntryDTO entry = collectionEntryService.getEntryById(id);
        if (entry.getAddedDate() == null) {
            entry.setAddedDate(java.time.LocalDate.now());
            collectionEntryService.updateEntry(id, entry);
        }
        return "redirect:/edit/" + id;
    }
}

