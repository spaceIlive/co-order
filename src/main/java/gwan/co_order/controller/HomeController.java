package gwan.co_order.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }
        
        String memberName = (String) session.getAttribute("memberName");
        model.addAttribute("memberName", memberName);
        model.addAttribute("openPostCount", 0);
        model.addAttribute("myParticipationCount", 0);
        
        return "index";
    }
}


