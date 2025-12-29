package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.Artical_model;
import com.example.utecharity_project.Model.Articaldetail_model;
import com.example.utecharity_project.Model.Payment_model;
import com.example.utecharity_project.Repository.ArticalDetail_Repo;
import com.example.utecharity_project.Repository.Charitycontent_Repo;
import com.example.utecharity_project.Repository.Payment_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class FundraisingCampaign_controller {

    @Autowired
    private Charitycontent_Repo charitycontentRepo;

    @Autowired
    private ArticalDetail_Repo articalDetailRepo;

    @Autowired
    private com.example.utecharity_project.Repository.Follow_Repo followRepo;

    @Autowired
    private com.example.utecharity_project.Repository.Authorization_Repo authorizationRepo;

    @Autowired
    private Payment_Repo paymentRepo;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Helper to get followed IDs
    private java.util.Set<Long> getFollowedProjectIds(jakarta.servlet.http.HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            com.example.utecharity_project.Model.Authorization_model user = authorizationRepo.findByUsername(username)
                    .orElse(null);
            if (user != null) {
                java.util.List<com.example.utecharity_project.Model.Follow_model> follows = followRepo.findByUser(user);
                return follows.stream().map(f -> f.getProject().getId()).collect(java.util.stream.Collectors.toSet());
            }
        }
        return new java.util.HashSet<>();
    }

    @GetMapping("/chien-dich-gay-quy")
    public String fundraisingCampaign(Model model, jakarta.servlet.http.HttpSession session) {
        List<Artical_model> campaigns = charitycontentRepo.findAll();
        campaigns.forEach(campaign -> {
            if (campaign.getEndDate() != null) {
                campaign.setFormattedEndDate(campaign.getEndDate().format(formatter));
            }
        });
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("followedProjectIds", getFollowedProjectIds(session));
        return "page_user/FundraisingCampaign";
    }

    @GetMapping("/benh-hiem-ngheo")
    public String diseaseCampaign(Model model, jakarta.servlet.http.HttpSession session) {
        List<Artical_model> campaigns = charitycontentRepo
                .filterArticals(null, "Bá»‡nh hiá»ƒm nghÃ¨o", null, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        campaigns.forEach(campaign -> {
            if (campaign.getEndDate() != null) {
                campaign.setFormattedEndDate(campaign.getEndDate().format(formatter));
            }
        });
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("followedProjectIds", getFollowedProjectIds(session));
        return "page_user/diseaseCampaign";
    }

    @GetMapping("/chap-canh-sinh-vien")
    public String studentSupportCampaign(Model model, jakarta.servlet.http.HttpSession session) {
        List<Artical_model> campaigns = charitycontentRepo.filterArticals(null, "Cháº¥p cÃ¡nh sinh viÃªn", null,
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        campaigns.forEach(campaign -> {
            if (campaign.getEndDate() != null) {
                campaign.setFormattedEndDate(campaign.getEndDate().format(formatter));
            }
        });
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("followedProjectIds", getFollowedProjectIds(session));
        return "page_user/studentSupportCampaign";
    }

    @GetMapping("/bua-an-sinh-vien")
    public String mealSupportCampaign(Model model, jakarta.servlet.http.HttpSession session) {
        List<Artical_model> campaigns = charitycontentRepo
                .filterArticals(null, "Bá»¯a Äƒn sinh viÃªn", null, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        campaigns.forEach(campaign -> {
            if (campaign.getEndDate() != null) {
                campaign.setFormattedEndDate(campaign.getEndDate().format(formatter));
            }
        });
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("followedProjectIds", getFollowedProjectIds(session));
        return "page_user/mealSupportCampaign";
    }

    @GetMapping("/sinh-vien-gioi-co-hoan-canh")
    public String talentedStudentsCampaign(Model model, jakarta.servlet.http.HttpSession session) {
        List<Artical_model> campaigns = charitycontentRepo.filterArticals(null, "Sinh viÃªn giá» i cÃ³ hoÃ n cáº£nh",
                null, org.springframework.data.domain.Pageable.unpaged()).getContent();
        campaigns.forEach(campaign -> {
            if (campaign.getEndDate() != null) {
                campaign.setFormattedEndDate(campaign.getEndDate().format(formatter));
            }
        });
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("followedProjectIds", getFollowedProjectIds(session));
        return "page_user/talentedStudentsCampaign";
    }

    @GetMapping("/du-an")
    public String projects(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Model model, jakarta.servlet.http.HttpSession session) {

        List<Artical_model> allCampaigns = charitycontentRepo
                .filterArticals(keyword, category, null, org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        // Format dates
        allCampaigns.forEach(campaign -> {
            if (campaign.getEndDate() != null) {
                campaign.setFormattedEndDate(campaign.getEndDate().format(formatter));
            }
        });

        List<Artical_model> activeCampaigns = allCampaigns.stream()
                .filter(c -> c.isActive()) // Assuming isActive() checks date
                .toList();

        List<Artical_model> endedCampaigns = allCampaigns.stream()
                .filter(c -> !c.isActive())
                .toList();

        model.addAttribute("activeCampaigns", activeCampaigns);
        model.addAttribute("endedCampaigns", endedCampaigns);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("followedProjectIds", getFollowedProjectIds(session));
        return "page_user/projects";
    }

    @GetMapping("/du-an/{id}")
    public String projectDetail(@PathVariable("id") Long id, Model model, jakarta.servlet.http.HttpSession session) {
        Artical_model campaign = charitycontentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y dá»± Ã¡n ID: " + id));
        Articaldetail_model detail = articalDetailRepo.findFirstByArtical_Id(id);
        List<Payment_model> donors = paymentRepo.findByArtical_IdAndPaymentStatus(id, 1);

        model.addAttribute("campaign", campaign);
        model.addAttribute("detail", detail);
        model.addAttribute("donors", donors);
        model.addAttribute("followedProjectIds", getFollowedProjectIds(session));

        return "page_user/campaign_detail";
    }
}
