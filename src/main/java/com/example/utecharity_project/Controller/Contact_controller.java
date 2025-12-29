package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.Contact_model;
import com.example.utecharity_project.Repository.Contact_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Contact_controller {
    @Autowired
    Contact_Repo contactRepo;

    @GetMapping("lien-he")
    public String contact(Model model) {
        return "page_user/Contact";
    }

    @PostMapping("/insert/contact")
    public String insertcontact(@RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("message") String message) {

        Contact_model contactModel = new Contact_model();
        contactModel.setUser_fullname(name);
        contactModel.setUser_email(email);
        contactModel.setUser_phone(phone);
        contactModel.setUser_comment(message);
        contactModel.setType("Liên hệ chung");

        contactRepo.save(contactModel);

        return "redirect:/lien-he?contactSuccess=true";
    }

    @Autowired
    com.example.utecharity_project.Repository.HelpRequest_Repo helpRequestRepo;

    @PostMapping("/insert/help-request")
    public String insertHelpRequest(@RequestParam("beneficiaryName") String beneficiaryName,
            @RequestParam("beneficiaryPhone") String beneficiaryPhone,
            @RequestParam("reporterName") String reporterName,
            @RequestParam("reporterPhone") String reporterPhone,
            @RequestParam(value = "reporterEmail", required = false) String reporterEmail,
            @RequestParam("relationship") String relationship,
            @RequestParam("description") String description,
            @RequestParam(value = "verificationImage", required = false) String verificationImage) {

        Contact_model contactModel = new Contact_model();
        contactModel.setBeneficiaryName(beneficiaryName);
        contactModel.setBeneficiaryPhone(beneficiaryPhone);
        contactModel.setReporterName(reporterName);
        contactModel.setReporterPhone(reporterPhone);
        contactModel.setUser_email(reporterEmail);
        contactModel.setRelationship(relationship);
        contactModel.setUser_comment(description);
        contactModel.setVerificationImage(verificationImage);
        contactModel.setType("Yêu cầu hỗ trợ");
        contactModel.setStatus(0); // Pending
        contactModel.setUser_fullname(reporterName);

        contactRepo.save(contactModel);

        return "redirect:/lien-he?helpRequestSuccess=true";
    }
}
