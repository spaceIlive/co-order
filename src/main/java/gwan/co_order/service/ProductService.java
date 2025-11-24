package gwan.co_order.service;

import gwan.co_order.domain.Product;
import gwan.co_order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    public void saveProduct(Product product) {
        // 같은 가게에서 제품 이름 중복 체크
        if (productRepository.existsByStoreIdAndName(
                product.getStore().getId(), 
                product.getName())) {
            throw new IllegalStateException("이미 존재하는 상품명입니다.");
        }
        
        productRepository.saveProduct(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findProductById(productId);
        productRepository.delete(product);
    }

    public Product findProductById(Long productId) {
        return productRepository.findProductById(productId);
    }
    
    @Transactional
    public void updateProduct(Long productId, String name, int price) {
        Product product = productRepository.findProductById(productId);
        
        // 이름 변경 시 중복 체크 (기존 이름과 다를 때만)
        if (!product.getName().equals(name)) {
            if (productRepository.existsByStoreIdAndName(
                    product.getStore().getId(), name)) {
                throw new IllegalStateException("이미 존재하는 상품명입니다.");
            }
        }
        
        product.setName(name);
        product.setPrice(price);
        // @Transactional이라 자동으로 변경 감지됨
    }
}