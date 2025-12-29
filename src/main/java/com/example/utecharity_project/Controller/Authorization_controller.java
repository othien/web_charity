package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.Authorization_model;
import com.example.utecharity_project.Repository.Authorization_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class Authorization_controller {
    @Autowired
    private Authorization_Repo authorizationRepo;

    @GetMapping("/login-siteadmin")
    public String loginPage() {
        return "page_admin/Login_View_Admin"; // Hiá»ƒn thá»‹ trang Ä‘Äƒng nháº­p
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session,
            Model model) {
        // TÃ¬m user trong CSDL
        Authorization_model user = authorizationRepo.findByUsername(username).orElse(null);

        if (user != null && user.getPassword().equals(password)) {
            // LÆ°u thÃ´ng tin ngÆ°á»i dÃ¹ng vÃ  quyá»n vÃ o session
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRoles());
            session.setAttribute("role", user.getRoles());
            return "redirect:/admin"; // Chuyển hướng đến trang dashboard admin
        } else {
            model.addAttribute("error", "Sai tÃ i khoáº£n hoáº·c máº­t kháº©u");
            return "page_admin/Login_View_Admin"; // Náº¿u sai, quay láº¡i trang Ä‘Äƒng nháº­p
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // XÃ³a session khi Ä‘Äƒng xuáº¥t
        return "redirect:/login-siteadmin"; // Chuyển hướng về trang đăng nhập
    }

}
