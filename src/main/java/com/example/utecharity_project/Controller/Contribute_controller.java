package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.Payment_model;
import com.example.utecharity_project.Repository.Payment_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class Contribute_controller {
    @Autowired
    Payment_Repo paymentRepo;

    @GetMapping("/huong-dan-dong-gop")
    public String contribute() {
        return "page_user/Contribute";
    }

    @GetMapping("/cap-nhat-dong-gop")
    public String contributionupdate(Model model) {
        List<Payment_model> paymentModels = paymentRepo.findByDisplay(1); // Láº¥y cÃ¡c payment cÃ³ display = 1
        model.addAttribute("paymentModelUpdate", paymentModels);
        return "page_user/ContributionUpdate";
    }
}

