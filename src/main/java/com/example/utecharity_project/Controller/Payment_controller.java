package com.example.utecharity_project.Controller;

import com.example.utecharity_project.Config.VNPayService;
import com.example.utecharity_project.Model.Artical_model;
import com.example.utecharity_project.Model.Authorization_model;
import com.example.utecharity_project.Model.Payment_model;
import com.example.utecharity_project.Repository.Authorization_Repo;
import com.example.utecharity_project.Repository.Charitycontent_Repo;
import com.example.utecharity_project.Repository.Payment_Repo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@org.springframework.stereotype.Controller
public class Payment_controller {
    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private Payment_Repo paymentRepo;

    @Autowired
    private Charitycontent_Repo articalRepo;

    @Autowired
    private Authorization_Repo authorizationRepo;

    @GetMapping("/thanh-toan")
    public String home(Model model) {
        model.addAttribute("project", null);
        return "payment/payment_user";
    }

    @GetMapping("/donate")
    public String donateToProject(@RequestParam(value = "projectId", required = false) Long projectId, Model model) {
        if (projectId != null) {
            Artical_model project = articalRepo.findById(projectId).orElse(null);
            model.addAttribute("project", project);
        } else {
            model.addAttribute("project", null);
        }
        return "payment/payment_user";
    }

    @PostMapping("/submitOrder")
    public String submidOrder(@RequestParam("amount") int orderTotal,
            @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);
        return "redirect:" + vnpayUrl;
    }

    @GetMapping("/vnpay-payment")
    public String GetMapping(HttpServletRequest request, Model model, HttpSession session) {
        int paymentStatus = vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime parsedPaymentTime = LocalDateTime.parse(paymentTime, inputFormatter);
            String formattedPaymentTime = parsedPaymentTime.format(outputFormatter);

            String formattedTotalPrice = totalPrice.substring(0, totalPrice.length() - 2);
            double totalAmount = Double.parseDouble(formattedTotalPrice);

            Payment_model payment = new Payment_model();
            payment.setOrderId(orderInfo);
            payment.setTotalPrice(String.valueOf(totalAmount));
            payment.setTransactionId(transactionId);
            payment.setPaymentTime(parsedPaymentTime);
            payment.setPaymentStatus(paymentStatus);
            payment.setDisplay(1); // Display = 1 to show in lists

            // Get logged-in user from session and link to payment
            String username = (String) session.getAttribute("username");
            if (username != null) {
                Authorization_model user = authorizationRepo.findByUsername(username).orElse(null);
                if (user != null) {
                    payment.setUser(user);
                }
            }

            if (paymentStatus == 1 && orderInfo != null && !orderInfo.trim().isEmpty()) {
                // Extract project code from orderInfo - try different approaches
                String cleanOrderInfo = orderInfo.trim();
                Artical_model artical = null;

                // Strategy 1: Try the last word as project code (e.g., "Ung ho du an YT300319"
                // -> "YT300319")
                String[] words = cleanOrderInfo.split("\\s+");
                if (words.length > 0) {
                    String lastWord = words[words.length - 1];
                    artical = articalRepo.findByCode(lastWord).stream().findFirst().orElse(null);
                }

                // Strategy 2: If not found, try last 5-8 characters
                if (artical == null) {
                    for (int len = 8; len >= 5; len--) {
                        if (cleanOrderInfo.length() >= len) {
                            String code = cleanOrderInfo.replaceAll("\\s", "")
                                    .substring(cleanOrderInfo.replaceAll("\\s", "").length() - len);
                            artical = articalRepo.findByCode(code).stream().findFirst().orElse(null);
                            if (artical != null)
                                break;
                        }
                    }
                }

                if (artical != null) {
                    if ("Đã kết thúc".equals(artical.getStatus()) || "Đã hoàn thành".equals(artical.getStatus())) {
                        updateDefaultArticle(totalAmount);
                        model.addAttribute("message",
                                "Du an da ket thuc. So tien da duoc chuyen vao quy chung cua quy tu thien UTE.");
                    } else {
                        // Update project's raised amount
                        double newAmountRaised = artical.getAmountRaised() + totalAmount;
                        artical.setAmountRaised(newAmountRaised);

                        // Check if goal is reached and update status
                        if (newAmountRaised >= artical.getGoalAmount()) {
                            artical.setStatus("Đã đủ quỹ");
                        }

                        articalRepo.save(artical);
                        payment.setArtical(artical);
                        model.addAttribute("message", "Quyen gop thanh cong cho du an: " + artical.getTitle());
                        model.addAttribute("projectTitle", artical.getTitle());
                    }
                } else {
                    updateDefaultArticle(totalAmount);
                    model.addAttribute("message",
                            "Khong tim thay du an phu hop. So tien da duoc chuyen vao quy chung.");
                }
            }

            paymentRepo.save(payment);

            model.addAttribute("orderId", orderInfo);
            model.addAttribute("totalPrice", totalAmount);
            model.addAttribute("paymentTime", formattedPaymentTime);
            model.addAttribute("transactionId", transactionId);

            return paymentStatus == 1 ? "payment/ordersuccess" : "payment/orderfail";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Da xay ra loi khi xu ly thanh toan: " + e.getMessage());
            return "payment/orderfail";
        }
    }

    private void updateDefaultArticle(double amount) {
        Artical_model defaultArtical = articalRepo.findByCode("QC000").stream()
                .findFirst()
                .orElse(null);
        if (defaultArtical != null) {
            defaultArtical.setAmountRaised(defaultArtical.getAmountRaised() + amount);
            articalRepo.save(defaultArtical);
        }
    }

}
