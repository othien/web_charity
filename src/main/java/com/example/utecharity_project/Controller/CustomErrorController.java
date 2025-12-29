package com.example.utecharity_project.Controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public String handleError() {
        // Tráº£ vá» tÃªn cá»§a view tÆ°Æ¡ng á»©ng vá»›i trang lá»—i tÃ¹y chá»‰nh
        return "common_view/Error_view";
    }
}

