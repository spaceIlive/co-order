package gwan.co_order.controller;

import gwan.co_order.service.DeliveryService;
import gwan.co_order.service.dto.DeliveryGroupView;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/deliveries/manage")
    public String manageDeliveries(HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        List<DeliveryGroupView> deliveryGroups = deliveryService.getOrderedDeliveryGroups();
        model.addAttribute("deliveryGroups", deliveryGroups);
        model.addAttribute("assignmentForm", new DriverAssignmentForm());
        return "delivery-manage";
    }

    @PostMapping("/deliveries/{postId}/assign")
    public String assignDriver(@PathVariable Long postId,
                               @Valid DriverAssignmentForm form,
                               BindingResult bindingResult,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }

        if (form.getDriverName() == null || form.getDriverName().isBlank()) {
            bindingResult.rejectValue("driverName", "required", "배달기사 이름을 입력해주세요.");
        }
        if (form.getDriverContact() == null || form.getDriverContact().isBlank()) {
            bindingResult.rejectValue("driverContact", "required", "연락처를 입력해주세요.");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "배달기사 정보가 올바르지 않습니다.");
            return "redirect:/deliveries/manage";
        }

        try {
            deliveryService.assignDriverToGroup(postId, form.getDriverName(), form.getDriverContact());
            redirectAttributes.addFlashAttribute("message", "배달 기사를 배정했습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/deliveries/manage";
    }

    @PostMapping("/deliveries/{postId}/complete")
    public String completeDelivery(@PathVariable Long postId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }

        try {
            deliveryService.completeGroupDelivery(postId);
            redirectAttributes.addFlashAttribute("message", "배송을 완료 처리했습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/deliveries/manage";
    }
}

