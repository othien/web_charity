package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.Authorization_model;
import com.example.utecharity_project.Repository.Authorization_Repo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private Authorization_Repo authorizationRepo;

    @GetMapping("/dang-nhap")
    public String loginPage() {
        return "page_user/auth/login";
    }

    @PostMapping("/dang-nhap")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session,
            Model model) {
        Authorization_model user = authorizationRepo.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            // Check if it's a normal user role if needed, but for now allow all logins
            // here?
            // Or restrict to 'user' role? User didn't specify strict role separation for
            // login pages,
            // but usually admin login is separate.
            // Let's assume this is for regular users.
            session.setAttribute("user", user); // Store entire user object for easy access
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRoles());
            return "redirect:/trang-chu";
        } else {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
            return "page_user/auth/login";
        }
    }

    @GetMapping("/dang-ky")
    public String registerPage() {
        return "page_user/auth/register";
    }

    @PostMapping("/dang-ky")
    public String register(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullname,
            @RequestParam String email,
            @RequestParam String confirm_password,
            Model model) {

        if (!password.equals(confirm_password)) {
            model.addAttribute("error", "Mật khẩu nhập lại không khớp!");
            return "page_user/auth/register";
        }

        if (authorizationRepo.existsByUsername(username)) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "page_user/auth/register";
        }

        if (authorizationRepo.existsByEmail(email)) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return "page_user/auth/register";
        }

        Authorization_model newUser = new Authorization_model();
        newUser.setUsername(username);
        newUser.setPassword(password); // In production, use hashing!
        newUser.setFullname(fullname);
        newUser.setEmail(email);
        newUser.setRoles("user"); // Default role

        authorizationRepo.save(newUser);

        return "redirect:/dang-nhap?registerSuccess=true";
    }

    @Autowired
    private com.example.utecharity_project.Service.EmailService emailService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "page_user/auth/forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Authorization_model user = authorizationRepo.findByEmail(email).orElse(null);

        if (user == null) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống.");
            return "page_user/auth/forgot_password";
        }

        // Generate simple random password
        String newPassword = "UTE" + (int) (Math.random() * 100000);
        user.setPassword(newPassword);
        authorizationRepo.save(user);

        // Send Email
        String subject = "Khôi phục mật khẩu - UTE Charity";
        String message = "Mật khẩu mới của bạn là: " + newPassword + "\nVui lòng đăng nhập và đổi mật khẩu ngay.";
        emailService.sendMail(email, subject, message);

        model.addAttribute("success", "Mật khẩu mới đã được gửi đến email của bạn.");
        return "page_user/auth/forgot_password";
    }

    @GetMapping("/dang-xuat")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/trang-chu";
    }
}
