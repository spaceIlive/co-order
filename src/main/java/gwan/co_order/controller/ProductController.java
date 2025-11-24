package gwan.co_order.controller;

import gwan.co_order.domain.Product;
import gwan.co_order.domain.Store;
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
public class ProductController {

    private final ProductService productService;
    private final StoreService storeService;

    @GetMapping("/stores/{storeId}/products")
    public String productList(@PathVariable Long storeId, HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        Store store = storeService.findStoreById(storeId);
        List<Product> products = storeService.getStoreProducts(storeId);
        
        model.addAttribute("store", store);
        model.addAttribute("products", products);
        return "product-list";
    }

    @GetMapping("/stores/{storeId}/products/new")
    public String createForm(@PathVariable Long storeId, HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        Store store = storeService.findStoreById(storeId);
        model.addAttribute("store", store);
        model.addAttribute("productForm", new ProductForm());
        return "product-register";
    }

    @PostMapping("/products")
    public String create(@Valid ProductForm form, BindingResult result, HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            Store store = storeService.findStoreById(form.getStoreId());
            model.addAttribute("store", store);
            return "product-register";
        }
        
        try {
            Store store = storeService.findStoreById(form.getStoreId());
            Product product = Product.createProduct(store, form.getName(), form.getPrice());
            productService.saveProduct(product);
            return "redirect:/stores/" + form.getStoreId() + "/products";
            
        } catch (IllegalStateException e) {
            Store store = storeService.findStoreById(form.getStoreId());
            model.addAttribute("store", store);
            result.rejectValue("name", "duplicate", e.getMessage());
            return "product-register";
        }
    }
    
    @GetMapping("/products/{productId}/edit")
    public String editForm(@PathVariable Long productId, HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        Product product = productService.findProductById(productId);
        ProductForm form = ProductForm.from(product);
        
        model.addAttribute("product", product);
        model.addAttribute("productForm", form);
        return "product-edit";
    }
    
    @PostMapping("/products/{productId}/edit")
    public String update(@PathVariable Long productId, 
                        @Valid ProductForm form, 
                        BindingResult result,
                        HttpSession session,
                        Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            Product product = productService.findProductById(productId);
            model.addAttribute("product", product);
            return "product-edit";
        }
        
        try {
            productService.updateProduct(productId, form.getName(), form.getPrice());
            return "redirect:/stores/" + form.getStoreId() + "/products";
            
        } catch (IllegalStateException e) {
            Product product = productService.findProductById(productId);
            model.addAttribute("product", product);
            result.rejectValue("name", "duplicate", e.getMessage());
            return "product-edit";
        }
    }
}

