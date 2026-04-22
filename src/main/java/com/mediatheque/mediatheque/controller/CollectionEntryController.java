package com.mediatheque.mediatheque.controller;

import com.mediatheque.mediatheque.dto.CollectionEntryDTO;
import com.mediatheque.mediatheque.model.Format;
import com.mediatheque.mediatheque.model.Condition;
import com.mediatheque.mediatheque.service.CollectionEntryService;
import com.mediatheque.mediatheque.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/collection")
public class CollectionEntryController {

    private final CollectionEntryService collectionEntryService;
    private final AlbumService albumService;

    @GetMapping
    public String getCollection(Model model) {
        model.addAttribute("entries", collectionEntryService.getCollection());
        return "collection";
    }

    @GetMapping("/wishlist")
    public String getWishlist(Model model) {
        model.addAttribute("entries", collectionEntryService.getWishlist());
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
    public String addEntry(@PathVariable Long albumId, @ModelAttribute CollectionEntryDTO entryDTO) {
        entryDTO.setAlbumId(albumId);
        if (entryDTO.getAddedDate() == null) {
            entryDTO.setAddedDate(java.time.LocalDate.now());
        }
        collectionEntryService.createEntry(entryDTO);
        return "redirect:/collection";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("entry", collectionEntryService.getEntryById(id));
        model.addAttribute("conditions", Condition.values());
        model.addAttribute("isEdit", true);
        return "entry-form";
    }

    @PostMapping("/edit/{id}")
    public String updateEntry(@PathVariable Long id, @ModelAttribute CollectionEntryDTO dto) {
        collectionEntryService.updateEntry(id, dto);
        return "redirect:/collection";
    }

    @PostMapping("/{id}/delete")
    public String deleteEntry(@PathVariable Long id) {
        collectionEntryService.deleteEntry(id);
        return "redirect:/collection";
    }

    @PostMapping("/{id}/move")
    public String moveToCollection(@PathVariable Long id) {
        collectionEntryService.moveToCollection(id);
        return "redirect:/collection/edit/" + id;
    }
}
