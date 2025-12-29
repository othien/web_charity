package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.Artical_model;
import com.example.utecharity_project.Model.Service_model;
import com.example.utecharity_project.Repository.Charitycontent_Repo;
import com.example.utecharity_project.Repository.CommunityNews_Repo;
import com.example.utecharity_project.Repository.Payment_Repo;
import com.example.utecharity_project.Repository.ServiceOperations_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class Home_controller {
        @Autowired
        private Charitycontent_Repo charitycontentRepo;

        @Autowired
        private ServiceOperations_Repo serviceOperationsRepo;

        @Autowired
        private Payment_Repo paymentRepo;

        @Autowired
        private CommunityNews_Repo communityNewsRepo;

        // Render ra trang product
        @GetMapping("/trang-chu")
        public String list_content(Model model) {
                List<Artical_model> charitycontent = charitycontentRepo.findAll();
                List<Service_model> serviceModels = serviceOperationsRepo.findAll();

                // Format endDate for each article
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Artical_model article : charitycontent) {
                        if (article.getEndDate() != null) {
                                article.setFormattedEndDate(article.getEndDate().format(formatter));
                        }
                }

                // Get latest projects (sorted by ID descending, limit 6)
                List<Artical_model> latestProjects = charitycontent.stream()
                                .sorted(Comparator.comparing(Artical_model::getId).reversed())
                                .limit(6)
                                .collect(Collectors.toList());
                model.addAttribute("latestProjects", latestProjects);

                // Get ongoing projects - Dự án đang gây quỹ (chưa đạt mục tiêu và chưa hết hạn)
                List<Artical_model> ongoingProjects = charitycontent.stream()
                                .filter(p -> !p.isExpired() && !p.isGoalReached())
                                .sorted(Comparator.comparing(Artical_model::getId).reversed())
                                .limit(6)
                                .collect(Collectors.toList());

                // Nếu không có dự án nào đang gây quỹ, hiển thị tất cả dự án chưa hết hạn
                if (ongoingProjects.isEmpty()) {
                        ongoingProjects = charitycontent.stream()
                                        .filter(p -> !p.isExpired())
                                        .sorted(Comparator.comparing(Artical_model::getId).reversed())
                                        .limit(6)
                                        .collect(Collectors.toList());
                }

                // Nếu vẫn không có, hiển thị tất cả dự án
                if (ongoingProjects.isEmpty()) {
                        ongoingProjects = charitycontent.stream()
                                        .sorted(Comparator.comparing(Artical_model::getId).reversed())
                                        .limit(6)
                                        .collect(Collectors.toList());
                }
                model.addAttribute("ongoingProjects", ongoingProjects);

                // Get successful projects - Dự án đã đạt mục tiêu
                List<Artical_model> successfulProjectsList = charitycontent.stream()
                                .filter(p -> p.isGoalReached())
                                .sorted(Comparator.comparing(Artical_model::getId).reversed())
                                .limit(6)
                                .collect(Collectors.toList());
                model.addAttribute("successfulProjectsList", successfulProjectsList);

                model.addAttribute("title_home", "title_home");
                model.addAttribute("title_introduce", "title_introduce");
                model.addAttribute("title_donationcontext", "title_donationcontext");
                model.addAttribute("title_fundraisingcampaign", "title_fundraisingcampaign");
                model.addAttribute("title_criticalillness", "title_criticalillness");
                model.addAttribute("title_empoweringstudentstoattendschool", "title_empoweringstudentstoattendschool");
                model.addAttribute("title_studentmeals", "title_studentmeals");
                model.addAttribute("title_excellentstudentsfacinghardships", "title_excellentstudentsfacinghardships");
                model.addAttribute("title_contribute", "title_contribute");
                model.addAttribute("title_contributionupdates", "title_contributionupdates");
                model.addAttribute("title_donationguidelines", "title_donationguidelines");
                model.addAttribute("title_communitynews", "title_communitynews");
                model.addAttribute("title_contact", "title_contact");
                model.addAttribute("title_language", "title_language");
                model.addAttribute("Kindnessalwaysbringsaboutmiracles", "Kindnessalwaysbringsaboutmiracles");
                model.addAttribute("Donation", "Donation");
                model.addAttribute("KeyProjects", "KeyProjects");
                model.addAttribute("NewlyAnnouncedProjects", "NewlyAnnouncedProjects");
                model.addAttribute("OngoingProjects", "OngoingProjects");
                model.addAttribute("SuccessfulProjects", "SuccessfulProjects");
                model.addAttribute("SeeMoreDetails", "SeeMoreDetails");
                model.addAttribute("Statistics", "Statistics");
                model.addAttribute("NumberofActiveUsers", "NumberofActiveUsers");
                model.addAttribute("AmountDonated", "AmountDonated");
                model.addAttribute("EventsOrganized", "EventsOrganized");
                model.addAttribute("ProgramServiceActivities", "ProgramServiceActivities");
                model.addAttribute("Connectwithusonsocialmedia", "Connectwithusonsocialmedia");
                model.addAttribute("AboutUs", "AboutUs");
                model.addAttribute("Webelievethat", "Webelievethat");
                model.addAttribute("Guide", "Guide");
                model.addAttribute("DonationGuide", "DonationGuide");
                model.addAttribute("Introduction", "Introduction");
                model.addAttribute("Organization", "Organization");
                model.addAttribute("History", "History");
                model.addAttribute("Program", "Program");
                model.addAttribute("EmergencyRelief", "EmergencyRelief");
                model.addAttribute("MedicalAssistance", "MedicalAssistance");
                model.addAttribute("LowcostMeals", "LowcostMeals");
                model.addAttribute("LivelihoodSupport", "LivelihoodSupport");
                model.addAttribute("Contact", "Contact");
                model.addAttribute("SaigonUniversityCharityFund", "SaigonUniversityCharityFund");
                model.addAttribute("ViewMore", "ViewMore");

                model.addAttribute("charitycontent", charitycontent);
                model.addAttribute("serviceModels", serviceModels);

                // === STATISTICS - Những con số biết nói ===

                // 1. Total Projects (Dự án)
                long totalProjects = charitycontentRepo.count();
                model.addAttribute("totalProjects", totalProjects);

                // 2. Successful Projects (Dự án thành công)
                long successfulProjects = charitycontent.stream()
                                .filter(p -> "1".equals(p.getStatus())
                                                || (p.getAmountRaised() >= p.getGoalAmount() && p.getGoalAmount() > 0))
                                .count();
                model.addAttribute("successfulProjects", successfulProjects);

                // 3. Total Revenue / Donations (Tiền ủng hộ)
                Double totalRevenue = paymentRepo.sumTotalRevenue();
                model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

                // 4. Total Donation Count (Số lượt ủng hộ)
                long totalDonationCount = paymentRepo.findAll().stream()
                                .filter(p -> p.getPaymentStatus() == 1)
                                .count();
                model.addAttribute("totalDonationCount", totalDonationCount);

                return "page_user/Home";
        }

        // Redirect old route to new unified project detail page
        @GetMapping("/chuong-trinh/{id}")
        public String showCharityContentDetail(@PathVariable Long id) {
                return "redirect:/du-an/" + id;
        }

}
