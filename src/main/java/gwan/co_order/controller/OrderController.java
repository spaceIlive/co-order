package gwan.co_order.controller;

import gwan.co_order.domain.Participation;
import gwan.co_order.service.ParticipationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final ParticipationService participationService;

    @GetMapping("/orders")
    public String myOrders(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }

        // 세션에서 에러 메시지 가져오기
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }

        Map<String, List<Participation>> grouped = 
            participationService.findParticipationsGrouped(memberId);

        model.addAttribute("openList", grouped.get("open"));
        model.addAttribute("completedList", grouped.get("completed"));
        model.addAttribute("cancelledList", grouped.get("cancelled"));

        return "my-orders";
    }
}

