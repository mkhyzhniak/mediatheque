package com.mediatheque.mediatheque.controller;

import com.mediatheque.mediatheque.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAllAttributes(statsService.getStats());
        return "stats";
    }
}
