package com.example.utecharity_project.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IntroducePage_controller {

    @GetMapping("/gioi-thieu")
    public String introduce() {
        return "page_user/IntroducePage";
    }
}

