package org.example.projektarendehantering.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UiController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/ui/cases")
    public String listCases() {
        return "cases/list";
    }

    @GetMapping("/ui/cases/new")
    public String newCase() {
        return "cases/new";
    }

    @GetMapping("/ui/cases/{caseId}")
    public String caseDetail(@PathVariable String caseId, Model model) {
        model.addAttribute("caseId", caseId);
        return "cases/detail";
    }
}

