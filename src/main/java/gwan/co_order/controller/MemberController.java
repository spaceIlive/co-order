package gwan.co_order.controller;

import gwan.co_order.domain.Address;
import gwan.co_order.domain.Member;
import gwan.co_order.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    //로그인 관련
    
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String name,
                       @RequestParam String password,
                       HttpSession session) {
        try {
            Member member = memberService.login(name, password);
            session.setAttribute("memberId", member.getId());
            session.setAttribute("memberName", member.getName());
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            return "redirect:/login?error=notfound";
        } catch (IllegalStateException e) {
            return "redirect:/login?error=wrongpassword";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    //회원가입 관련

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "member-register";
    }

    @PostMapping("/members")
    public String create(@Valid MemberForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "member-register";
        }
        
        try {
            Member member = new Member();
            member.setName(form.getName());
            member.setPassword(form.getPassword());
            
            Address address = new Address(form.getAddress(), form.getLatitude(), form.getLongitude());
            member.setAddress(address);
            
            memberService.join(member);
            return "redirect:/login";
        } catch (IllegalStateException e) {
            result.rejectValue("name", "duplicate", "이미 존재하는 이름입니다.");
            return "member-register";
        }
    }
}


