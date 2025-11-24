package gwan.co_order.controller;

import gwan.co_order.domain.Address;
import gwan.co_order.domain.Member;
import gwan.co_order.domain.Post;
import gwan.co_order.domain.Product;
import gwan.co_order.service.MemberService;
import gwan.co_order.service.ParticipationService;
import gwan.co_order.service.PostService;
import gwan.co_order.service.ProductService;
import gwan.co_order.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ParticipationController {

    private final PostService postService;
    private final StoreService storeService;
    private final ParticipationService participationService;
    private final MemberService memberService;
    private final ProductService productService;

    @GetMapping("/posts/{postId}/participations/new")
    public String createForm(@PathVariable Long postId, HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        Post post = postService.findPostById(postId);
        List<Product> products = storeService.getStoreProducts(post.getStore().getId());
        
        // Product 엔티티를 ProductDto로 변환 (JavaScript에서 사용하기 위해)
        List<ProductDto> productDtos = products.stream()
                .map(ProductDto::from)
                .toList();
        
        // 예상 배달비 계산 (현재 인원 + 1)
        int expectedDeliveryFee = calculateExpectedDeliveryFee(post);
        
        model.addAttribute("post", post);
        model.addAttribute("products", productDtos);
        model.addAttribute("expectedDeliveryFee", expectedDeliveryFee);
        
        return "participation-register";
    }
    
    @PostMapping("/posts/{postId}/participations")
    public String create(@PathVariable Long postId, ParticipationForm form, HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }
        
        // 상품 선택 검증
        if (form.getProductQuantities() == null || form.getProductQuantities().isEmpty()) {
            Post post = postService.findPostById(postId);
            List<Product> products = storeService.getStoreProducts(post.getStore().getId());
            
            List<ProductDto> productDtos = products.stream()
                    .map(ProductDto::from)
                    .toList();
            
            int expectedDeliveryFee = calculateExpectedDeliveryFee(post);
            
            model.addAttribute("post", post);
            model.addAttribute("products", productDtos);
            model.addAttribute("expectedDeliveryFee", expectedDeliveryFee);
            model.addAttribute("error", "최소 1개 이상의 상품을 선택해주세요.");
            
            return "participation-register";
        }
        
        try {
            // 현재 로그인한 회원의 주소 가져오기
            Member member = memberService.findMemberById(memberId);
            Address deliveryAddress = member.getAddress();
            
            // 1. 참여 생성 (중복 참여 검증 포함)
            Long participationId = participationService.joinPost(memberId, postId, deliveryAddress);
            
            // 2. 선택한 상품들 추가
            for (Long productId : form.getProductQuantities().keySet()) {
                Integer quantity = form.getProductQuantities().get(productId);
                if (quantity != null && quantity > 0) {
                    Product product = productService.findProductById(productId);
                    int totalPrice = product.getPrice() * quantity;
                    participationService.addProduct(participationId, product, quantity, totalPrice);
                }
            }
            
            return "redirect:/posts";
            
        } catch (IllegalStateException e) {
            // 중복 참여 또는 기타 검증 실패 시
            Post post = postService.findPostById(postId);
            List<Product> products = storeService.getStoreProducts(post.getStore().getId());
            
            List<ProductDto> productDtos = products.stream()
                    .map(ProductDto::from)
                    .toList();
            
            int expectedDeliveryFee = calculateExpectedDeliveryFee(post);
            
            model.addAttribute("post", post);
            model.addAttribute("products", productDtos);
            model.addAttribute("expectedDeliveryFee", expectedDeliveryFee);
            model.addAttribute("error", e.getMessage());
            
            return "participation-register";
        }
    }
    
    @PostMapping("/participations/{participationId}/cancel")
    public String cancelParticipation(@PathVariable Long participationId, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }
        
        try {
            // 참여 취소
            participationService.cancelParticipation(participationId);
            return "redirect:/orders";
        } catch (Exception e) {
            // 에러 발생 시 세션에 에러 메시지 저장
            String errorMessage = e.getMessage() != null ? e.getMessage() : "참여 취소 중 오류가 발생했습니다.";
            session.setAttribute("errorMessage", errorMessage);
            return "redirect:/orders";
        }
    }
    
    private int calculateExpectedDeliveryFee(Post post) {
        // 간단한 배달비 계산 (실제로는 Policy 사용)
        // 총 배달비 / (현재 인원 + 1)
        int totalDeliveryFee = 3000; // 기본값
        int expectedParticipants = post.getCurrentParticipants() + 1;
        return totalDeliveryFee / expectedParticipants;
    }
}
