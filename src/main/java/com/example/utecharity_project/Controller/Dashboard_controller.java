package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Model.*;
import com.example.utecharity_project.Repository.*;

import com.example.utecharity_project.Service.EmailService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class Dashboard_controller {
    @Autowired
    Charitycontent_Repo charitycontentRepo;

    @Autowired
    ArticalDetail_Repo articalDetailRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    Payment_Repo paymentRepo;

    @Autowired
    CommunityNews_Repo communityNewsRepo;

    @Autowired
    Contact_Repo contactRepo;

    @Autowired
    Note_Repo noteRepo;

    @Autowired
    ServiceOperations_Repo serviceOperationsRepo;

    @Autowired
    Authorization_Repo authorizationRepo;

    @Autowired
    Activity_Repo activityRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    Follow_Repo followRepo;

    // --- Admin Dashboard Routes ---
    @GetMapping("/admin")
    public String adminHome(@RequestParam(value = "filterType", defaultValue = "YEAR") String filterType,
            @RequestParam(value = "filterDate", required = false) String filterDate,
            @RequestParam(value = "filterMonth", required = false) Integer filterMonth,
            @RequestParam(value = "filterYear", required = false) Integer filterYear,
            @RequestParam(value = "filterQuarter", required = false) Integer filterQuarter,
            HttpSession session, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login-siteadmin";
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // Defaults
        if (filterYear == null)
            filterYear = currentYear;
        if (filterMonth == null)
            filterMonth = currentMonth;
        if (filterQuarter == null)
            filterQuarter = (currentMonth - 1) / 3 + 1;

        // --- Filter Logic ---
        Double total = 0.0;
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Double> data = new java.util.ArrayList<>();
        String filterTitle = "";

        // Logic Switch
        if ("DAY".equals(filterType)) {
            java.time.LocalDate date;
            try {
                date = (filterDate != null && !filterDate.isEmpty()) ? java.time.LocalDate.parse(filterDate) : now;
            } catch (Exception e) {
                date = now;
            }

            filterTitle = "Hôm nay (" + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
            Double dayTotal = paymentRepo.sumRevenueByDay(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
            total = dayTotal != null ? dayTotal : 0.0;

            for (int h = 0; h < 24; h++) {
                labels.add(h + "h");
                Double hourly = paymentRepo.sumRevenueByHour(h, date.getDayOfMonth(), date.getMonthValue(),
                        date.getYear());
                data.add(hourly != null ? hourly : 0.0);
            }
            model.addAttribute("selectedFilterDate", date.toString());

        } else if ("MONTH".equals(filterType)) {
            filterTitle = "Tháng " + filterMonth + "/" + filterYear;
            Double monthTotal = paymentRepo.sumRevenueByMonthYear(filterMonth, filterYear);
            total = monthTotal != null ? monthTotal : 0.0;

            java.time.YearMonth yearMonth = java.time.YearMonth.of(filterYear, filterMonth);
            int daysInMonth = yearMonth.lengthOfMonth();
            for (int d = 1; d <= daysInMonth; d++) {
                labels.add(d + "/" + filterMonth);
                Double daily = paymentRepo.sumRevenueByDay(d, filterMonth, filterYear);
                data.add(daily != null ? daily : 0.0);
            }

        } else if ("QUARTER".equals(filterType)) {
            filterTitle = "Quý " + filterQuarter + "/" + filterYear;
            int startM = (filterQuarter - 1) * 3 + 1;
            int endM = filterQuarter * 3;

            Double quarterTotal = paymentRepo.sumRevenueByQuarter(startM, endM, filterYear);
            total = quarterTotal != null ? quarterTotal : 0.0;

            for (int m = startM; m <= endM; m++) {
                labels.add("Th." + m);
                Double monthly = paymentRepo.sumRevenueByMonthYear(m, filterYear);
                data.add(monthly != null ? monthly : 0.0);
            }

        } else { // YEAR or Default
            filterTitle = "Năm " + filterYear;
            filterType = "YEAR";

            Double yearTotal = paymentRepo.sumRevenueByYear(filterYear);
            total = yearTotal != null ? yearTotal : 0.0;

            for (int m = 1; m <= 12; m++) {
                labels.add("Th." + m);
                Double monthly = paymentRepo.sumRevenueByMonthYear(m, filterYear);
                data.add(monthly != null ? monthly : 0.0);
            }
        }

        model.addAttribute("totalDonated", total);
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", data);
        model.addAttribute("filterType", filterType);
        model.addAttribute("filterTitle", filterTitle);
        model.addAttribute("selectedFilterYear", filterYear);
        model.addAttribute("selectedFilterMonth", filterMonth);
        model.addAttribute("selectedFilterQuarter", filterQuarter);

        // --- Project Statistics (Global) ---
        List<Artical_model> projects = charitycontentRepo.findAll();
        double disbursed = 0;
        int running = 0;
        int completed = 0;
        int ended = 0;

        for (Artical_model p : projects) {
            if (p.getDisbursedAmount() != null && p.getDisbursedAmount() > 0)
                disbursed += p.getDisbursedAmount();
            String s = p.getStatus();
            if ("Đang gây quỹ".equals(s) || "Đã đủ quỹ".equals(s) || "Đang giải ngân".equals(s))
                running++;
            if ("Đã hoàn thành".equals(s))
                completed++;
            if ("Đã kết thúc".equals(s))
                ended++;
        }

        model.addAttribute("disbursedAmount", disbursed);
        model.addAttribute("countRunning", running);
        model.addAttribute("countCompleted", completed);
        model.addAttribute("countEnded", ended);
        model.addAttribute("totalProjects", projects.size());

        // Recent Donations (last 10, sorted by paymentTime DESC)
        Pageable pageable10 = PageRequest.of(0, 10, org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "paymentTime"));
        Page<Payment_model> recentDonations = paymentRepo.filterPayments(null, null, null, null, null, pageable10);
        model.addAttribute("recentDonations", recentDonations.getContent());

        // Recent Support Requests (last 5, sorted by ID DESC as proxy for time)
        Pageable pageable5 = PageRequest.of(0, 5,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        Page<Contact_model> recentRequests = contactRepo.findByType("Yêu cầu hỗ trợ", pageable5);
        model.addAttribute("recentRequests", recentRequests.getContent());

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("user", session.getAttribute("username"));

        String role = (String) session.getAttribute("role");
        model.addAttribute("role", role);

        return "page_admin/admin_dashboard";
    }

    @GetMapping("/admin/users")
    public String adminUsers(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        Pageable pageable = PageRequest.of(page, size);
        Page<Authorization_model> pageResult = authorizationRepo.filterUsers(searchTerm,
                "ALL".equals(role) ? null : role, pageable);

        model.addAttribute("users", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("selectedRole", role);
        model.addAttribute("activePage", "users");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/user_management";
    }

    @GetMapping("/admin/projects")
    public String adminProjects(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        Pageable pageable = PageRequest.of(page, size);
        Page<Artical_model> pageResult = charitycontentRepo.filterArticals(searchTerm,
                "ALL".equals(category) ? null : category,
                "ALL".equals(status) ? null : status, pageable);

        model.addAttribute("projects", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("activePage", "projects");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/project_management";
    }

    // Project CRUD
    @GetMapping("/admin/projects/create")
    public String createProjectForm(HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        model.addAttribute("project", new Artical_model());
        model.addAttribute("projectDetail", new Articaldetail_model());
        model.addAttribute("activePage", "projects");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/project_form";
    }

    @GetMapping("/admin/projects/edit/{id}")
    public String editProjectForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        Artical_model project = charitycontentRepo.findById(id).orElse(null);
        if (project == null)
            return "redirect:/admin/projects";

        // Get project details
        Articaldetail_model detail = null;
        if (project.getArticalDetails() != null && !project.getArticalDetails().isEmpty()) {
            detail = project.getArticalDetails().get(0);
        } else {
            detail = new Articaldetail_model();
        }

        model.addAttribute("project", project);
        model.addAttribute("projectDetail", detail);
        model.addAttribute("activePage", "projects");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/project_form";
    }

    @PostMapping("/admin/projects/save")
    public String saveProject(
            @ModelAttribute Artical_model project,
            @RequestParam(required = false) String content_1,
            @RequestParam(required = false) String img_content,
            @RequestParam(required = false) String content_2,
            @RequestParam(required = false) String img_content2,
            @RequestParam(required = false) String content_3,
            @RequestParam(required = false) String dynamicContent,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        if (project.getDisbursedAmount() == null) {
            project.setDisbursedAmount(0.0);
        }

        // Set default status for new project
        if (project.getStatus() == null || project.getStatus().isEmpty()) {
            project.setStatus("Đang gây quỹ");
        }

        // Auto-update status if goal reached
        if (project.getAmountRaised() >= project.getGoalAmount() && "Đang gây quỹ".equals(project.getStatus())) {
            project.setStatus("Đã đủ quỹ");
        }

        // Save project first
        Artical_model savedProject = charitycontentRepo.save(project);

        // Save or update project details
        Articaldetail_model detail;
        if (savedProject.getArticalDetails() != null && !savedProject.getArticalDetails().isEmpty()) {
            detail = savedProject.getArticalDetails().get(0);
        } else {
            detail = new Articaldetail_model();
            detail.setArtical(savedProject);
        }

        // Legacy fields (kept for backward compatibility)
        detail.setContent_1(content_1);
        detail.setImg_content(img_content);
        detail.setContent_2(content_2);
        detail.setImg_content2(img_content2);
        detail.setContent_3(content_3);

        // New dynamic content field
        detail.setDynamicContent(dynamicContent);

        articalDetailRepo.save(detail);

        redirectAttributes.addFlashAttribute("successMessage", "Lưu dự án thành công!");

        return "redirect:/admin/projects";
    }

    @PostMapping("/admin/projects/status/{id}")
    public String updateProjectStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        Artical_model project = charitycontentRepo.findById(id).orElse(null);
        if (project != null) {
            project.setStatus(status);
            charitycontentRepo.save(project);
        }
        return "redirect:/admin/projects";
    }

    // Confirm project completion (for projects that reached goal)
    @PostMapping("/admin/projects/complete/{id}")
    public String confirmProjectCompletion(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        Artical_model project = charitycontentRepo.findById(id).orElse(null);
        if (project != null && project.isGoalReached()) {
            project.setStatus("Đã hoàn thành");
            charitycontentRepo.save(project);
        }
        return "redirect:/admin/projects";
    }

    // Report form for completed projects
    @GetMapping("/admin/projects/report/{id}")
    public String reportForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        Artical_model project = charitycontentRepo.findById(id).orElse(null);
        if (project == null)
            return "redirect:/admin/projects";

        model.addAttribute("project", project);
        model.addAttribute("activePage", "projects");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/project_report_form";
    }

    // Save project report
    @PostMapping("/admin/projects/report/save")
    public String saveProjectReport(
            @RequestParam Long projectId,
            @RequestParam String reportImage,
            @RequestParam String reportContent,
            HttpSession session) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        Artical_model project = charitycontentRepo.findById(projectId).orElse(null);
        if (project != null) {
            project.setReportImage(reportImage);
            project.setReportContent(reportContent);
            charitycontentRepo.save(project);
        }
        return "redirect:/admin/projects";
    }

    @GetMapping("/admin/projects/delete/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String deleteProject(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        try {
            // Find project first
            Artical_model project = charitycontentRepo.findById(id).orElse(null);
            if (project != null) {
                // Delete associated payments first (foreign key constraint)
                paymentRepo.deleteByArtical(project);

                // Delete associated follows (foreign key constraint)
                followRepo.deleteByProject(project);

                // Delete associated article details
                if (project.getArticalDetails() != null && !project.getArticalDetails().isEmpty()) {
                    articalDetailRepo.deleteAll(project.getArticalDetails());
                }
                // Delete the project
                charitycontentRepo.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Xóa dự án thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy dự án!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa dự án: " + e.getMessage());
        }
        return "redirect:/admin/projects";
    }

    @GetMapping("/admin/donations")
    public String adminDonations(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "quarter", required = false) Integer quarter,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        Pageable pageable = PageRequest.of(page, size);
        Page<Payment_model> pageResult;

        // Convert quarter to month range if specified
        Integer startMonth = month;
        Integer endMonth = month;
        if (quarter != null && year != null) {
            startMonth = (quarter - 1) * 3 + 1;
            endMonth = quarter * 3;
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            try {
                Long id = Long.valueOf(searchTerm);
                pageResult = paymentRepo.searchById(id, pageable);
            } catch (NumberFormatException e) {
                pageResult = paymentRepo.searchByOrderId(searchTerm, pageable);
            }
        } else {
            pageResult = paymentRepo.filterPayments(projectId, "ALL".equals(category) ? null : category, startMonth,
                    endMonth, year, pageable);
        }

        // Calculate Totals
        Double totalRevenue = 0.0;
        if (projectId != null) {
            totalRevenue = paymentRepo.sumRevenueByCampaign(projectId);
        } else if (category != null && !"ALL".equals(category)) {
            totalRevenue = paymentRepo.sumRevenueByCategory(category);
        } else if (month != null && year != null) {
            totalRevenue = paymentRepo.sumRevenueByMonthYear(month, year);
        } else if (quarter != null && year != null) {
            totalRevenue = paymentRepo.sumRevenueByQuarter(startMonth, endMonth, year);
        } else {
            totalRevenue = paymentRepo.sumTotalRevenue();
        }

        Long totalDonations = pageResult.getTotalElements();

        model.addAttribute("donations", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("searchTerm", searchTerm);

        // Filter attributes
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedQuarter", quarter);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);

        model.addAttribute("projects", charitycontentRepo.findAll());
        model.addAttribute("activePage", "donations");
        model.addAttribute("user", session.getAttribute("username"));

        // Totals
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("totalDonations", totalDonations);

        return "page_admin/donation_management";
    }

    // Export donations to CSV
    @GetMapping("/admin/donations/export")
    public void exportDonationsCSV(HttpServletResponse response, HttpSession session) throws java.io.IOException {
        if (session.getAttribute("username") == null) {
            response.sendRedirect("/login-siteadmin");
            return;
        }
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=donations_export.csv");

        java.io.PrintWriter writer = response.getWriter();
        writer.write('\uFEFF'); // BOM for Excel UTF-8
        writer.println("Mã GD,Người đóng góp,Email,Dự án,Số tiền,Thời gian,Trạng thái");

        List<Payment_model> donations = paymentRepo.findAll();
        for (Payment_model p : donations) {
            String username = p.getUser() != null ? p.getUser().getFullname() : "Ẩn danh";
            String email = p.getUser() != null ? p.getUser().getEmail() : "-";
            String projectName = p.getArtical() != null ? p.getArtical().getTitle() : "-";
            String priceStr = p.getTotalPrice() != null ? p.getTotalPrice() : "0";
            String status = p.getPaymentStatus() == 1 ? "Thành công" : "Chờ xử lý";
            String time = p.getPaymentTime() != null ? p.getPaymentTime().toString() : "-";

            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    p.getOrderId(), username, email, projectName, priceStr, time, status);
        }
        writer.flush();
    }

    @GetMapping("/admin/news")
    public String adminNews(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        Pageable pageable = PageRequest.of(page, size);

        // Parse dates
        java.time.LocalDate fromLocalDate = null;
        java.time.LocalDate toLocalDate = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) {
                fromLocalDate = java.time.LocalDate.parse(fromDate);
            }
            if (toDate != null && !toDate.isEmpty()) {
                toLocalDate = java.time.LocalDate.parse(toDate);
            }
        } catch (Exception e) {
            // Ignore date parsing errors
        }

        // Use comprehensive filter with sort options
        String searchValue = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;
        Page<Communitynews_model> pageResult;

        if ("title".equals(sortBy)) {
            pageResult = communityNewsRepo.filterNewsSortByTitle(searchValue, fromLocalDate, toLocalDate, pageable);
        } else if ("oldest".equals(sortBy)) {
            pageResult = communityNewsRepo.filterNewsOldestFirst(searchValue, fromLocalDate, toLocalDate, pageable);
        } else {
            // Default: newest first
            pageResult = communityNewsRepo.filterNews(searchValue, fromLocalDate, toLocalDate, pageable);
        }

        model.addAttribute("newsList", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalElements", pageResult.getTotalElements());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("activePage", "news");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/news_management";
    }

    // News CRUD
    @GetMapping("/admin/news/create")
    public String createNewsForm(HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        model.addAttribute("news", new Communitynews_model());
        model.addAttribute("activePage", "news");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/news_form";
    }

    @GetMapping("/admin/news/edit/{id}")
    public String editNewsForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        Communitynews_model news = communityNewsRepo.findById(id).orElse(null);
        if (news == null)
            return "redirect:/admin/news";
        model.addAttribute("news", news);
        model.addAttribute("activePage", "news");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/news_form";
    }

    @PostMapping("/admin/news/save")
    public String saveNews(@ModelAttribute Communitynews_model news, HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        // Set date_update if null
        if (news.getDate_update() == null) {
            news.setDate_update(LocalDate.now());
        }

        communityNewsRepo.save(news);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu tin tức thành công!");
        return "redirect:/admin/news";
    }

    @GetMapping("/admin/news/delete/{id}")
    public String deleteNews(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        communityNewsRepo.deleteById(id);
        return "redirect:/admin/news";
    }

    @GetMapping("/admin/contacts")
    public String adminContacts(
            @RequestParam(value = "type", defaultValue = "Liên hệ chung") String type,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "relationship", required = false) String relationship,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";

        Pageable pageable = PageRequest.of(page, size);

        // Use comprehensive filter
        String searchValue = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;
        String relationshipValue = (relationship != null && !relationship.trim().isEmpty()) ? relationship.trim()
                : null;

        Page<Contact_model> pageResult = contactRepo.filterContacts(type, searchValue, status, relationshipValue,
                pageable);

        model.addAttribute("contacts", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalElements", pageResult.getTotalElements());
        model.addAttribute("selectedType", type);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("status", status);
        model.addAttribute("relationship", relationship);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("activePage", "contacts");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/contact_management";
    }

    // Approve support request
    @PostMapping("/admin/contacts/approve/{id}")
    public String approveRequest(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        Contact_model contact = contactRepo.findById(id).orElse(null);
        if (contact != null) {
            contact.setStatus(1); // 1 = Đã phê duyệt
            contactRepo.save(contact);
        }
        return "redirect:/admin/contacts?type=Y%C3%AAu+c%E1%BA%A7u+h%E1%BB%97+tr%E1%BB%A3&approveSuccess=true";
    }

    // Create project from support request
    @GetMapping("/admin/contacts/create-project/{id}")
    public String createProjectFromRequest(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        Contact_model contact = contactRepo.findById(id).orElse(null);
        if (contact == null)
            return "redirect:/admin/contacts?type=Yêu cầu hỗ trợ";

        Artical_model project = new Artical_model();
        project.setTitle("Hỗ trợ: "
                + (contact.getBeneficiaryName() != null ? contact.getBeneficiaryName() : "Trường hợp khó khăn"));
        project.setStatus("Đang gây quỹ");
        project.setStartDate(java.time.LocalDate.now());
        project.setEndDate(java.time.LocalDate.now().plusMonths(1));

        model.addAttribute("project", project);
        model.addAttribute("prefillDescription", contact.getUser_comment());
        model.addAttribute("fromRequestId", id);
        model.addAttribute("activePage", "projects");
        model.addAttribute("user", session.getAttribute("username"));
        return "page_admin/project_form";
    }

    // After saving project from request, update request status
    @PostMapping("/admin/projects/save-from-request")
    public String saveProjectFromRequest(@ModelAttribute Artical_model project, @RequestParam Long fromRequestId,
            @RequestParam(required = false) String content_1,
            @RequestParam(required = false) String img_content,
            @RequestParam(required = false) String content_2,
            @RequestParam(required = false) String img_content2,
            @RequestParam(required = false) String content_3,
            HttpSession session) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        if (project.getStatus() == null || project.getStatus().isEmpty()) {
            project.setStatus("Đang gây quỹ");
        }
        project.setDisbursedAmount(0.0);
        project.setFromRequestId(fromRequestId); // Link project to help request
        Artical_model savedProject = charitycontentRepo.save(project);

        // Save Details
        if (content_1 != null || content_2 != null || content_3 != null) {
            Articaldetail_model detail = new Articaldetail_model();
            detail.setArtical(savedProject);
            detail.setContent_1(content_1);
            detail.setImg_content(img_content);
            detail.setContent_2(content_2);
            detail.setImg_content2(img_content2);
            detail.setContent_3(content_3);
            articalDetailRepo.save(detail); // Assuming articalDetailRepo is injected
        }

        // Update request status to "Hoàn tất"
        Contact_model contact = contactRepo.findById(fromRequestId).orElse(null);
        if (contact != null) {
            contact.setStatus(2); // 2 = Hoàn tất
            contactRepo.save(contact);
        }
        return "redirect:/admin/projects";
    }
    // --- End Admin Routes ---

    // Render ra trang quáº£n lÃ½ ná»™i dung bÃ i viáº¿t
    @GetMapping("/dashboard_articlemanagement")
    public String articlemanagement(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size); // XÃ¡c Ä‘á»‹nh sá»‘ trang vÃ  sá»‘ má»¥c trÃªn má»—i trang
        Page<Articaldetail_model> pageResult = articalDetailRepo.findAll(pageable);

        model.addAttribute("articaldetailModelLists", pageResult.getContent()); // Danh sÃ¡ch cÃ¡c má»¥c cá»§a trang
                                                                                // hiá»‡n táº¡i
        model.addAttribute("currentPage", page); // Trang hiá»‡n táº¡i
        model.addAttribute("totalPages", pageResult.getTotalPages()); // Tá»•ng sá»‘ trang
        model.addAttribute("totalItems", pageResult.getTotalElements()); // Tá»•ng sá»‘ má»¥c

        return "page_admin/ArticleManagement_admin";
    }

    @GetMapping("/dashboard_revenuemanagement")
    public String revenue(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "campaignId", required = false) Long campaignId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "quarter", required = false) Integer quarter,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment_model> resultPage;

        // Convert quarter to month range if specified
        Integer startMonth = month;
        Integer endMonth = month;
        if (quarter != null && year != null) {
            startMonth = (quarter - 1) * 3 + 1;
            endMonth = quarter * 3;
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            try {
                Long id = Long.valueOf(searchTerm);
                resultPage = paymentRepo.searchById(id, pageable);
            } catch (NumberFormatException e) {
                resultPage = paymentRepo.searchByOrderId(searchTerm, pageable);
            }
        } else {
            // Use the new filter method
            resultPage = paymentRepo.filterPayments(campaignId, "ALL".equals(category) ? null : category, startMonth,
                    endMonth, year, pageable);
        }

        // Calculate Total Revenue based on filters (or global if no filter)
        Double totalRevenue = 0.0;
        if (campaignId != null) {
            totalRevenue = paymentRepo.sumRevenueByCampaign(campaignId);
        } else if (category != null && !"ALL".equals(category)) {
            totalRevenue = paymentRepo.sumRevenueByCategory(category);
        } else if (month != null && year != null) {
            totalRevenue = paymentRepo.sumRevenueByMonthYear(month, year);
        } else if (quarter != null && year != null) {
            totalRevenue = paymentRepo.sumRevenueByQuarter(startMonth, endMonth, year);
        } else {
            totalRevenue = paymentRepo.sumTotalRevenue();
        }

        // Calculate total donation count
        Long totalDonations = resultPage.getTotalElements();

        // Add Artical List for Dropdown
        List<Artical_model> campaigns = charitycontentRepo.findAll();
        model.addAttribute("campaigns", campaigns);

        model.addAttribute("paymentModel", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("searchTerm", searchTerm);

        // Add Filter Attributes back to model
        model.addAttribute("selectedCampaignId", campaignId);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedQuarter", quarter);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("totalDonations", totalDonations);

        return "page_admin/RevenueManagement_admin";
    }

    @PostMapping("/displayrevenue")
    public String displayrevenue(@RequestParam("id") Long id, @RequestParam("display") int display, Model model) {
        // TÃ¬m Ä‘á»‘i tÆ°á»£ng Payment_model theo ID
        Optional<Payment_model> paymentModelOp = paymentRepo.findById(id);

        if (paymentModelOp.isPresent()) {
            Payment_model paymentModel = paymentModelOp.get();
            paymentModel.setDisplay(display);
            paymentRepo.save(paymentModel);
        }
        return "redirect:/dashboard_revenuemanagement";
    }

    // Handle chuc nang delete revenue
    @PostMapping("revenue/delete/{id_delete}")
    public String revenue_delete(@PathVariable("id_delete") Long id) {
        paymentRepo.deleteById(id);
        return "redirect:/dashboard_revenuemanagement";
    }

    // Redirect old route to new admin route
    @GetMapping("/dashboard_newsmanagement")
    public String newsManagementOld() {
        return "redirect:/admin/news";
    }

    // Old routes - redirect to new admin routes
    @GetMapping("newsmanagement/{id_update}")
    public String newsmanagement_update(@PathVariable("id_update") Long id) {
        return "redirect:/admin/news/edit/" + id;
    }

    @PostMapping("newsmanagement/{id_update}")
    public String handle_newsmanagement_update(@PathVariable("id_update") Long id) {
        return "redirect:/admin/news/edit/" + id;
    }

    @PostMapping("newsmanagement/delete/{id_delete}")
    public String newsmanagement_delete(@PathVariable("id_delete") Long id) {
        return "redirect:/admin/news";
    }

    @GetMapping("/insert/news")
    public String insertnews() {
        return "redirect:/admin/news/create";
    }

    @PostMapping("/insert/news")
    public String insertNewsPost() {
        return "redirect:/admin/news/create";
    }

    @GetMapping("dashboard_contact")
    public String contact(@RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Contact_model> resultPage;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            resultPage = contactRepo.search(searchTerm, pageable);
        } else {
            resultPage = contactRepo.findByType("ALL".equals(type) ? null : type, pageable);
        }

        model.addAttribute("contactModels", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("selectedType", type);
        model.addAttribute("searchTerm", searchTerm);
        return "page_admin/ContactManagement_admin";
    }

    @PostMapping("contact/delete/{id_delete}")
    public String contact_delete(@PathVariable("id_delete") Long id) {
        contactRepo.deleteById(id);
        return "redirect:/dashboard_contact";
    }

    // Delete help request from admin contacts page
    @GetMapping("/admin/contacts/delete/{id}")
    public String deleteHelpRequest(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("username") == null)
            return "redirect:/login-siteadmin";
        contactRepo.deleteById(id);
        return "redirect:/admin/contacts?type=Y%C3%AAu+c%E1%BA%A7u+h%E1%BB%97+tr%E1%BB%A3&deleteSuccess=true";
    }

    @GetMapping("dashboard_note")
    public String note(Model model) {
        List<Note_model> notes = noteRepo.findAll();
        model.addAttribute("notes", notes);
        return "page_admin/NoteManagement_admin";
    }

    @PostMapping("/addNote")
    public String addNote(@RequestParam String date, @RequestParam String content) {
        Note_model note = new Note_model();
        note.setDate(date);
        note.setContent(content);
        noteRepo.save(note);
        return "redirect:/dashboard_note";
    }

    @GetMapping("/note")
    public ResponseEntity<List<Note_model>> getNotesByDate(@RequestParam String date) {
        List<Note_model> notes = noteRepo.findByDate(date);
        return ResponseEntity.ok(notes);
    }

    @DeleteMapping("/deleteNote/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteRepo.deleteById(id);
        return ResponseEntity.noContent().build(); // Tráº£ vá» mÃ£ 204 No Content
    }

    @GetMapping("dashboard_statistical")
    public String statistical(Model model) {
        Double totalRevenue = paymentRepo.sumTotalRevenue();
        long totalProjects = charitycontentRepo.count();
        long totalNews = communityNewsRepo.count();

        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalNews", totalNews);

        return "page_admin/StatisticalManagement_admin";
    }

    // render ra trang chiáº¿n dá»‹ch dashboard (Now using Artical)
    @GetMapping("/dashboard_campaignmanagement")
    public String campaignmanagement(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Artical_model> pageResult;

        // Use the Artical filter method
        pageResult = charitycontentRepo.filterArticals(searchTerm, "ALL".equals(category) ? null : category,
                "ALL".equals(status) ? null : status, pageable);

        // ThÃªm cÃ¡c thuá»™c tÃ­nh vÃ o model
        model.addAttribute("fundraisingCampaignModel", pageResult.getContent()); // Using same attribute name to
                                                                                 // minimize view changes slightly, or
                                                                                 // better yet, rename it.
        // Let's keep "fundraisingCampaignModel" name in View for less friction, or
        // rename.
        // Actually, let's rename it to 'campaigns' in the view later, but for now
        // passing the list of Articals.
        // Wait, the View expects properties like 'goalAmount', 'amountRaised'. Artical
        // has them.

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);

        return "page_admin/CampaignManagement_admin";
    }

    // UPDATE STATUS for Artical
    @PostMapping("/updateStatusCampaign")
    public String updateStatusCampaign(@RequestParam("id") Long id, @RequestParam("status") String status) {
        try {
            Artical_model artical = charitycontentRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Artical not found"));
            artical.setStatus(status);
            charitycontentRepo.save(artical);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/dashboard_campaignmanagement";
    }

    // DELETE for Artical
    @PostMapping("/campaign/delete/{id}")
    public String deleteCampaign(@PathVariable("id") Long id) {
        try {
            // Delete associated details first if not cascade
            // CascadeType.ALL is set in Artical_model, so deleting Artical should delete
            // Details.
            charitycontentRepo.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/dashboard_campaignmanagement";
    }

    // Artical implementation for updateCategory
    @PostMapping("/updateCategoryCampaign")
    public String updateCategoryCampaign(@RequestParam("id") Long id, @RequestParam("display") String display) {
        Optional<Artical_model> articalOptional = charitycontentRepo.findById(id);
        if (articalOptional.isPresent()) {
            Artical_model artical = articalOptional.get();
            artical.setDisplaycategory(display);
            charitycontentRepo.save(artical);
        }
        return "redirect:/dashboard_campaignmanagement";
    }

    @GetMapping("/insert/campaign")
    public String insertcampaign() {
        return "page_admin/CRUD_CampaignManagement/insertCampaign";
    }

    @PostMapping("/insert/campaign")
    public String insertCampaign(
            @RequestParam("title") String title,
            @RequestParam("imgUrl") String imgUrl,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("goalAmount") String goalAmount,
            @RequestParam("code") String code,
            @RequestParam("displaycategory") String displaycategory,
            @RequestParam(value = "content1", required = false) String content1,
            @RequestParam(value = "imgContent1", required = false) String imgContent1,
            @RequestParam(value = "content2", required = false) String content2,
            @RequestParam(value = "imgContent2", required = false) String imgContent2,
            @RequestParam(value = "content3", required = false) String content3,
            HttpSession session,
            Model model) {

        // Validate basic fields
        if (title.isBlank() || imgUrl.isBlank() || startDate.isBlank() || endDate.isBlank() ||
                goalAmount.isBlank() || code.isBlank()) {
            model.addAttribute("error", "Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ thÃ´ng tin.");
            return "page_admin/CRUD_CampaignManagement/insertCampaign";
        }

        // Validate Date
        LocalDate start, end;
        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
            if (start.isAfter(end)) {
                model.addAttribute("error", "NgÃ y báº¯t Ä‘áº§u khÃ´ng thá»ƒ sau ngÃ y káº¿t thÃºc.");
                return "page_admin/CRUD_CampaignManagement/insertCampaign";
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "NgÃ y khÃ´ng há»£p lá»‡.");
            return "page_admin/CRUD_CampaignManagement/insertCampaign";
        }

        // Validate Goal
        double goal;
        try {
            goal = Double.parseDouble(goalAmount);
            if (goal <= 0) {
                model.addAttribute("error", "Sá»‘ tiá» n pháº£i lá»›n hÆ¡n 0.");
                return "page_admin/CRUD_CampaignManagement/insertCampaign";
            }
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Sá»‘ tiá» n khÃ´ng há»£p lá»‡.");
            return "page_admin/CRUD_CampaignManagement/insertCampaign";
        }

        // Check Duplicate Code
        if (!charitycontentRepo.findByCode(code).isEmpty()) {
            model.addAttribute("error", "MÃ£ chiáº¿n dá»‹ch Ä‘Ã£ tá»“n táº¡i.");
            return "page_admin/CRUD_CampaignManagement/insertCampaign";
        }

        // Save Artical
        Artical_model artical = new Artical_model();
        artical.setTitle(title);
        artical.setImg(imgUrl);
        artical.setStartDate(start);
        artical.setEndDate(end);
        artical.setAmountRaised(0);
        artical.setGoalAmount(goal);
        artical.setCode(code);
        artical.setStatus("Đang vận động");
        artical.setDisplaycategory(displaycategory);

        Artical_model savedArtical = charitycontentRepo.save(artical);

        // Save ArticalDetail
        Articaldetail_model detail = new Articaldetail_model();
        detail.setArtical(savedArtical);
        detail.setContent_1(content1);
        detail.setImg_content(imgContent1);
        detail.setContent_2(content2);
        detail.setImg_content2(imgContent2);
        detail.setContent_3(content3);

        articalDetailRepo.save(detail);

        // Log Activity
        String username = (String) session.getAttribute("username");
        Activity_model activityModel = new Activity_model();
        activityModel.setUsername(username != null ? username : "Unknown");
        activityModel.setActivity("ThÃªm");
        activityModel.setDetail_activity("ThÃªm dá»± Ã¡n: " + title);
        activityModel.setDatetime(LocalDateTime.now());
        activityRepo.save(activityModel);

        return "redirect:/dashboard_campaignmanagement";
    }

    @GetMapping("campaignmanagement/{id_update}")
    public String campaignmanagement_update(@PathVariable("id_update") Long id, Model model) {
        Artical_model artical = charitycontentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID " + id));
        Articaldetail_model detail = articalDetailRepo.findFirstByArtical_Id(id);

        model.addAttribute("fundraisingCampaignModel", artical);
        model.addAttribute("detail", detail);

        return "page_admin/CRUD_CampaignManagement/updateCampaign";
    }

    @PostMapping("campaignmanagement/{id_update}")
    public String handleCampaignManagementUpdate(
            @RequestParam("title") String title,
            @RequestParam("imgUrl") String imgUrl,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("goalAmount") String goalAmount,
            @RequestParam("code") String code,
            @RequestParam("displaycategory") String displaycategory,
            @RequestParam(value = "content1", required = false) String content1,
            @RequestParam(value = "imgContent1", required = false) String imgContent1,
            @RequestParam(value = "content2", required = false) String content2,
            @RequestParam(value = "imgContent2", required = false) String imgContent2,
            @RequestParam(value = "content3", required = false) String content3,
            @PathVariable("id_update") Long id,
            HttpSession session,
            Model model) {

        Artical_model artical = charitycontentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y dá»± Ã¡n: " + id));

        LocalDate start, end;
        double goal;
        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
            goal = Double.parseDouble(goalAmount);
        } catch (Exception e) {
            model.addAttribute("error", "Lá»—i Ä‘á»‹nh dáº¡ng dá»¯ liá»‡u.");
            return "page_admin/CRUD_CampaignManagement/updateCampaign";
        }

        artical.setTitle(title);
        artical.setImg(imgUrl);
        artical.setStartDate(start);
        artical.setEndDate(end);
        artical.setGoalAmount(goal);
        artical.setCode(code);
        artical.setDisplaycategory(displaycategory);
        charitycontentRepo.save(artical);

        Articaldetail_model detail = articalDetailRepo.findFirstByArtical_Id(id);
        if (detail == null) {
            detail = new Articaldetail_model();
            detail.setArtical(artical);
        }
        detail.setContent_1(content1);
        detail.setImg_content(imgContent1);
        detail.setContent_2(content2);
        detail.setImg_content2(imgContent2);
        detail.setContent_3(content3);
        articalDetailRepo.save(detail);

        String username = (String) session.getAttribute("username");
        Activity_model activityModel = new Activity_model();
        activityModel.setUsername(username != null ? username : "Unknown");
        activityModel.setActivity("Sá»a");
        activityModel.setDetail_activity("Cáº­p nháº­t dá»± Ã¡n ID: " + id);
        activityModel.setDatetime(LocalDateTime.now());
        activityRepo.save(activityModel);

        return "redirect:/dashboard_campaignmanagement";
    }

    @GetMapping("/dashboard_authorization")
    public String render_authorization(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Authorization_model> pageResult = authorizationRepo.filterUsers(searchTerm,
                "ALL".equals(role) ? null : role, pageable);

        model.addAttribute("authorizationModels", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("selectedRole", role);

        return "page_admin/AuthorizationManagement_admin";
    }

    @GetMapping("/insert/authorizaton")
    public String insertauthorization() {
        return "page_admin/CRUD_AuthorizationManagement/insertAuthorization";
    }

    @PostMapping("/insert/authorization")
    public String insertauthorization(@RequestParam("inputusername") String inputusername,
            @RequestParam("inputpassword") String inputpassword,
            @RequestParam("confirmpassword") String confirmpassword,
            @RequestParam("email") String email,
            @RequestParam("inputroles") String inputroles, Model model) {

        // Kiá»ƒm tra xem tÃªn tÃ i khoáº£n Ä‘Ã£ tá»“n táº¡i
        if (authorizationRepo.existsByUsername(inputusername)) {
            model.addAttribute("error", "TÃªn tÃ i khoáº£n Ä‘Ã£ tá»“n táº¡i. Vui lÃ²ng chá»n tÃªn khÃ¡c.");
            return "page_admin/CRUD_AuthorizationManagement/insertAuthorization";
        }

        // Kiá»ƒm tra máº­t kháº©u cÃ³ khá»›p khÃ´ng
        if (!inputpassword.equals(confirmpassword)) {
            model.addAttribute("error",
                    "Máº­t kháº©u vÃ  máº­t kháº©u nháº­p láº¡i khÃ´ng khá»›p. Vui lÃ²ng thá»­ láº¡i.");
            return "page_admin/CRUD_AuthorizationManagement/insertAuthorization";
        }

        // Táº¡o vÃ  gÃ¡n dá»¯ liá»‡u vÃ o Authorization_model
        Authorization_model authorizationModel = new Authorization_model();
        authorizationModel.setUsername(inputusername);
        authorizationModel.setPassword(inputpassword); // CÃ³ thá»ƒ mÃ£ hÃ³a máº­t kháº©u náº¿u cáº§n
        authorizationModel.setEmail(email);
        authorizationModel.setRoles(inputroles);

        // LÆ°u tÃ i khoáº£n má»›i vÃ o database
        authorizationRepo.save(authorizationModel);

        // Redirect Ä‘áº¿n dashboard authorization sau khi thÃ nh cÃ´ng
        return "redirect:/dashboard_authorization";
    }

    @GetMapping("/authorizaton/{id_update}")
    public String authorizaton_update(@PathVariable("id_update") Long id, Model model) {
        Authorization_model authorizationModel = authorizationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service ID: " + id));
        model.addAttribute("authorizationModel", authorizationModel);
        return "page_admin/CRUD_AuthorizationManagement/updateAuthorization";
    }

    @PostMapping("/authorizaton/{id_update}")
    public String handle_authorization_update(@RequestParam("inputusername") String inputusername,
            @RequestParam("inputpassword") String inputpassword, @RequestParam("email") String email,
            @RequestParam("inputroles") String inputroles, @PathVariable("id_update") Long id) {
        Authorization_model authorizationModel = authorizationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID" + id));
        authorizationModel.setUsername(inputusername);
        authorizationModel.setPassword(inputpassword);
        authorizationModel.setEmail(email);
        authorizationModel.setRoles(inputroles);

        authorizationRepo.save(authorizationModel);

        return "redirect:/dashboard_authorization";
    }

    @PostMapping("authorizaton/delete/{id_delete}")
    public String authorizaton_delete(@PathVariable("id_delete") Long id) {
        authorizationRepo.deleteById(id);

        return "redirect:/dashboard_authorization";
    }

    @GetMapping("dashboard_activity")
    public String render_activity(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        LocalDate startLocalDate = null;
        LocalDate endLocalDate = null;

        if (startDate != null && !startDate.isEmpty()) {
            startLocalDate = LocalDate.parse(startDate, dateFormatter);
        }
        if (endDate != null && !endDate.isEmpty()) {
            endLocalDate = LocalDate.parse(endDate, dateFormatter);
        }

        LocalDateTime startDateTime = (startLocalDate != null) ? startLocalDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endLocalDate != null) ? endLocalDate.atTime(23, 59, 59) : null;

        Page<Activity_model> pageResult = activityRepo.filterActivities(username, startDateTime, endDateTime, pageable);
        List<Activity_model> activityModels = pageResult.getContent();

        // Format datetime
        activityModels.forEach(activity -> {
            if (activity.getDatetime() != null) {
                activity.setFormattedDatetime(activity.getDatetime().format(formatter));
            }
        });

        model.addAttribute("activityModels", activityModels);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("username", username);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "page_admin/Activity_admin";
    }

    @GetMapping("/email")
    public String showEmailForm(Model model) {
        return "page_admin/Handle/FormEmail";
    }

    @PostMapping("/send-email")
    public String sendEmail(String to, String subject, String message, Model model) {
        emailService.sendMail(to, subject, message);
        model.addAttribute("success", "Email Ä‘Ã£ Ä‘Æ°á»£c gá»­i thÃ nh cÃ´ng!");
        return "page_admin/Handle/ResponeEmail";
    }

    @Autowired
    com.example.utecharity_project.Repository.HelpRequest_Repo helpRequestRepo;

    @GetMapping("/dashboard_helprequest")
    public String helpRequestManagement(Model model) {
        List<com.example.utecharity_project.Model.HelpRequest_model> helpRequests = helpRequestRepo.findAll();
        model.addAttribute("helpRequests", helpRequests);
        return "page_admin/HelpRequestManagement_admin";
    }

    @PostMapping("/dashboard_helprequest/updateStatus")
    public String updateHelpRequestStatus(@RequestParam("id") Long id, @RequestParam("status") int status) {
        com.example.utecharity_project.Model.HelpRequest_model request = helpRequestRepo.findById(id).orElse(null);
        if (request != null) {
            request.setStatus(status);
            helpRequestRepo.save(request);
        }
        return "redirect:/dashboard_helprequest";
    }
}
