package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.Communitynews_model;
import com.example.utecharity_project.Repository.CommunityNews_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CommunityNews_controller {

    @Autowired
    private CommunityNews_Repo communityNewsRepo;

    @GetMapping("/tin-tuc-cong-dong")
    public String communityNews(Model model) {
        List<Communitynews_model> newsList = communityNewsRepo.findAll();
        model.addAttribute("communityNewsModels", newsList);
        return "page_user/CommunityNews";
    }
}
