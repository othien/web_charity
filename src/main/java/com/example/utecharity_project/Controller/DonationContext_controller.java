package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.Artical_model;
import com.example.utecharity_project.Repository.Charitycontent_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class DonationContext_controller {
    @Autowired
    private Charitycontent_Repo charitycontentRepo;

    @GetMapping("/chuong-trinh")
    public String donationContext(Model model) {
        List<Artical_model> charitycontent = charitycontentRepo.findAll();

        // Format endDate for each article
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Artical_model article : charitycontent) {
            article.setFormattedEndDate(article.getEndDate().format(formatter));
        }

        model.addAttribute("charitycontent", charitycontent);
        return "page_user/DonationContext";
    }
}

