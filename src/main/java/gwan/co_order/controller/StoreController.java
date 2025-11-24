package gwan.co_order.controller;

import gwan.co_order.domain.Address;
import gwan.co_order.domain.Store;
import gwan.co_order.service.StoreService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/stores")
    public String storeList(HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        List<Store> stores = storeService.findAllStores();
        model.addAttribute("stores", stores);
        return "store-list";
    }

    @GetMapping("/stores/new")
    public String createForm(HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("storeForm", new StoreForm());
        return "store-register";
    }

    @PostMapping("/stores")
    public String create(@Valid StoreForm form, BindingResult result, HttpSession session) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            return "store-register";
        }
        
        Store store = new Store();
        store.setName(form.getName());
        
        Address address = new Address(form.getAddress(), form.getLatitude(), form.getLongitude());
        store.setAddress(address);
        
        storeService.saveStore(store);
        return "redirect:/stores";
    }
}


