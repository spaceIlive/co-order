package gwan.co_order.controller;

import gwan.co_order.domain.*;
import gwan.co_order.service.MemberService;
import gwan.co_order.service.ParticipationService;
import gwan.co_order.service.PostService;
import gwan.co_order.service.ProductService;
import gwan.co_order.service.StoreService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberService memberService;
    private final StoreService storeService;
    private final ParticipationService participationService;
    private final ProductService productService;

    @GetMapping("/posts")
    public String postList(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }
        
        // 현재 로그인한 회원의 위치 기준으로 주변 모집글 조회
        Member member = memberService.findMemberById(memberId);
        double latitude = member.getAddress().getLatitude();
        double longitude = member.getAddress().getLongitude();
        
        List<Post> posts = postService.findNearOpenPosts(latitude, longitude);
        model.addAttribute("posts", posts);
        
        return "post-list";
    }

    @GetMapping("/posts/{postId}")
    public String postDetail(@PathVariable Long postId, HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        Post post = postService.findPostById(postId);
        model.addAttribute("post", post);
        
        return "post-detail";
    }

    @GetMapping("/posts/new")
    public String createForm(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }
        
        List<Store> stores = storeService.findAllStores();
        model.addAttribute("stores", stores);
        model.addAttribute("postForm", new PostForm());
        
        return "post-register";
    }
    
    // Ajax로 가게 선택 시 상품 목록 반환
    @GetMapping("/api/stores/{storeId}/products")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<ProductDto> getStoreProducts(@PathVariable Long storeId) {
        List<Product> products = storeService.getStoreProducts(storeId);
        return products.stream()
                .map(ProductDto::from)
                .toList();
    }
    
    //포스트 올리기
    @PostMapping("/posts")
    public String create(@Valid PostForm form, BindingResult result, HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            List<Store> stores = storeService.findAllStores();
            model.addAttribute("stores", stores);
            model.addAttribute("postForm", form);
            return "post-register";
        }
        
        // 상품 선택 검증
        if (form.getProductQuantities() == null || form.getProductQuantities().isEmpty()) {
            result.rejectValue("productQuantities", "required", "최소 1개 이상의 상품을 선택해주세요.");
            List<Store> stores = storeService.findAllStores();
            model.addAttribute("stores", stores);
            model.addAttribute("postForm", form);
            return "post-register";
        }
        
        try {
            // 현재 로그인한 회원의 주소 가져오기
            Member member = memberService.findMemberById(memberId);
            Address hostAddress = member.getAddress();
            
            // 1. 모집글 생성 (Post만 생성)
            Post createdPost = postService.createPost(memberId, form.getStoreId(), hostAddress, form.getDeadline());
            
            // 2. 호스트 참여 생성 (Participation 생성)
            Long hostParticipationId = participationService.joinPost(memberId, createdPost.getId(), hostAddress);
            
            // 3. 선택한 상품들 추가
            for (Long productId : form.getProductQuantities().keySet()) {
                Integer quantity = form.getProductQuantities().get(productId);
                if (quantity != null && quantity > 0) {
                    Product product = productService.findProductById(productId);
                    int totalPrice = product.getPrice() * quantity;
                    participationService.addProduct(hostParticipationId, product, quantity, totalPrice);
                }
            }
            
            return "redirect:/posts";
        } catch (Exception e) {
            // 에러 발생 시 폼 페이지로 돌아가서 에러 메시지 표시
            List<Store> stores = storeService.findAllStores();
            model.addAttribute("stores", stores);
            model.addAttribute("postForm", form);
            model.addAttribute("errorMessage", "모집글 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "post-register";
        }
    }
}

