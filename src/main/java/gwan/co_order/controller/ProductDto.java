package gwan.co_order.controller;

import gwan.co_order.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private int price;
    
    // Product 엔티티를 DTO로 변환
    public static ProductDto from(Product product) {
        return new ProductDto(
            product.getId(),
            product.getName(),
            product.getPrice()
        );
    }
}