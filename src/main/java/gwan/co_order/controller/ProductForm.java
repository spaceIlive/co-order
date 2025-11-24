package gwan.co_order.controller;

import gwan.co_order.domain.Product;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductForm {
    
    @NotNull(message = "가게를 선택해주세요")
    private Long storeId;
    
    @NotEmpty(message = "메뉴명은 필수입니다")
    private String name;
    
    @NotNull(message = "가격은 필수입니다")
    private Integer price;
    
    // Product 엔티티로부터 Form 생성
    public static ProductForm from(Product product) {
        ProductForm form = new ProductForm();
        form.setStoreId(product.getStore().getId());
        form.setName(product.getName());
        form.setPrice(product.getPrice());
        return form;
    }
}

