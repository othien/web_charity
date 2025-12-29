package com.example.utecharity_project.Controller;
import com.example.utecharity_project.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class Email_controller {
    @Autowired
    private EmailService emailService;

//    @GetMapping("/email")
//    public String showEmailForm(Model model) {
//        return "email-form";
//    }
//
//    @PostMapping("/send-email")
//    public String sendEmail(String to, String subject, String message, Model model) {
//        emailService.sendMail(to, subject, message);
//        model.addAttribute("success", "Email sent successfully!");
//        return "email-form";
//    }
}

