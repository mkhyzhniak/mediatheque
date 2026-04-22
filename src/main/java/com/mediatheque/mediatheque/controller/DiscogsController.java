package com.mediatheque.mediatheque.controller;

import com.mediatheque.mediatheque.dto.AlbumDTO;
import com.mediatheque.mediatheque.model.Format;
import com.mediatheque.mediatheque.service.AlbumService;
import com.mediatheque.mediatheque.service.CollectionEntryService;
import com.mediatheque.mediatheque.service.DiscogsService;
import com.mediatheque.mediatheque.dto.CollectionEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class DiscogsController {

    private final DiscogsService discogsService;
    private final AlbumService albumService;
    private final CollectionEntryService collectionEntryService;

    @GetMapping
    public String searchPage() {
        return "search";
    }

    @GetMapping("/results")
    public String search(@RequestParam String query, Model model) {
        if (query == null || query.trim().isEmpty()) {
            return "search";
        }

        List<AlbumDTO> results = discogsService.searchAlbums(query);

        List<AlbumDTO> vinyl = results.stream()
                .filter(a -> "VINYL".equals(a.getDiscogsFormat()))
                .collect(java.util.stream.Collectors.toList());
        List<AlbumDTO> cassette = results.stream()
                .filter(a -> "CASSETTE".equals(a.getDiscogsFormat()))
                .collect(java.util.stream.Collectors.toList());
        List<AlbumDTO> cd = results.stream()
                .filter(a -> "CD".equals(a.getDiscogsFormat()))
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("vinyl", vinyl);
        model.addAttribute("cassette", cassette);
        model.addAttribute("cd", cd);
        model.addAttribute("query", query);
        model.addAttribute("hasResults", !results.isEmpty());
        return "search";
    }

    @PostMapping("/add")
    public String addToCollection(@ModelAttribute AlbumDTO albumDTO,
                                  @RequestParam String format,
                                  @RequestParam(defaultValue = "false") Boolean wishlist) {
        AlbumDTO savedAlbum = albumService.createAlbum(albumDTO);

        if (wishlist) {
            CollectionEntryDTO entryDTO = new CollectionEntryDTO();
            entryDTO.setAlbumId(savedAlbum.getId());
            entryDTO.setFormat(Format.valueOf(format));
            entryDTO.setInWishlist(true);
            collectionEntryService.createEntry(entryDTO);
            return "redirect:/collection/wishlist";
        }

        return "redirect:/collection/add/" + savedAlbum.getId() + "?format=" + format;
    }
}
