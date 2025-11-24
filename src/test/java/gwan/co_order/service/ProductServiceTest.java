package gwan.co_order.service;

import gwan.co_order.domain.Address;
import gwan.co_order.domain.Product;
import gwan.co_order.domain.Store;
import gwan.co_order.repository.ProductRepository;
import gwan.co_order.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired ProductService productService;
    @Autowired ProductRepository productRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired EntityManager em;

    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setName("테스트가게");
        store.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        storeRepository.saveStore(store);
    }

    @Test
    @DisplayName("상품 등록")
    void saveProduct() {
        //given
        Product product = new Product();
        product.setName("양념치킨");
        product.setPrice(18000);
        product.setStore(store);

        //when
        productService.saveProduct(product);
        em.flush();
        em.clear();

        //then
        Product foundProduct = productRepository.findProductById(product.getId());
        assertNotNull(foundProduct);
        assertEquals("양념치킨", foundProduct.getName());
        assertEquals(18000, foundProduct.getPrice());
    }

    @Test
    @DisplayName("상품 조회")
    void findOne() {
        //given
        Product product = new Product();
        product.setName("후라이드치킨");
        product.setPrice(17000);
        product.setStore(store);
        productService.saveProduct(product);
        Long productId = product.getId();
        em.flush();
        em.clear();

        //when
        Product foundProduct = productService.findProductById(productId);

        //then
        assertNotNull(foundProduct);
        assertEquals(productId, foundProduct.getId());
        assertEquals("후라이드치킨", foundProduct.getName());
        assertEquals(17000, foundProduct.getPrice());
    }

    @Test
    @DisplayName("가게별 상품 목록 조회")
    void findProducts() {
        //given
        Product product1 = new Product();
        product1.setName("양념치킨");
        product1.setPrice(18000);
        product1.setStore(store);
        productService.saveProduct(product1);
        
        Product product2 = new Product();
        product2.setName("후라이드치킨");
        product2.setPrice(17000);
        product2.setStore(store);
        productService.saveProduct(product2);
        em.flush();
        em.clear();

        //when
        List<Product> products = productRepository.findProductsByStoreId(store.getId());

        //then
        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(p -> p.getName().equals("양념치킨")));
        assertTrue(products.stream().anyMatch(p -> p.getName().equals("후라이드치킨")));
    }

    @Test
    @DisplayName("상품 삭제")
    void deleteProduct() {
        //given
        Product product = new Product();
        product.setName("테스트상품");
        product.setPrice(10000);
        product.setStore(store);
        productService.saveProduct(product);
        Long productId = product.getId();
        em.flush();
        em.clear();

        //when
        productService.deleteProduct(productId);
        em.flush();
        em.clear();

        //then
        Product deletedProduct = productRepository.findProductById(productId);
        assertNull(deletedProduct);
    }

    @Test
    @DisplayName("상품 수정")
    void updateProduct() {
        //given
        Product product = new Product();
        product.setName("원래이름");
        product.setPrice(10000);
        product.setStore(store);
        productService.saveProduct(product);
        Long productId = product.getId();
        em.flush();
        em.clear();

        //when
        productService.updateProduct(productId, "새이름", 15000);
        em.flush();
        em.clear();

        //then
        Product updatedProduct = productService.findProductById(productId);
        assertEquals("새이름", updatedProduct.getName());
        assertEquals(15000, updatedProduct.getPrice());
    }

    @Test
    @DisplayName("상품 수정 - 같은 이름으로 변경")
    void updateProduct_SameName() {
        //given
        Product product = new Product();
        product.setName("원래이름");
        product.setPrice(10000);
        product.setStore(store);
        productService.saveProduct(product);
        Long productId = product.getId();
        em.flush();
        em.clear();

        //when
        productService.updateProduct(productId, "원래이름", 15000);
        em.flush();
        em.clear();

        //then
        Product updatedProduct = productService.findProductById(productId);
        assertEquals("원래이름", updatedProduct.getName());
        assertEquals(15000, updatedProduct.getPrice());
    }

    @Test
    @DisplayName("상품 수정 - 중복 상품명 예외")
    void updateProduct_DuplicateName() {
        //given
        Product product1 = new Product();
        product1.setName("상품1");
        product1.setPrice(10000);
        product1.setStore(store);
        productService.saveProduct(product1);

        Product product2 = new Product();
        product2.setName("상품2");
        product2.setPrice(20000);
        product2.setStore(store);
        productService.saveProduct(product2);
        Long product2Id = product2.getId();
        em.flush();
        em.clear();

        //when & then
        assertThrows(IllegalStateException.class, () -> {
            productService.updateProduct(product2Id, "상품1", 20000);
        });
    }

    @Test
    @DisplayName("상품 등록 - 중복 상품명 예외")
    void saveProduct_DuplicateName() {
        //given
        Product product1 = new Product();
        product1.setName("중복상품");
        product1.setPrice(10000);
        product1.setStore(store);
        productService.saveProduct(product1);

        Product product2 = new Product();
        product2.setName("중복상품");
        product2.setPrice(20000);
        product2.setStore(store);

        //when & then
        assertThrows(IllegalStateException.class, () -> {
            productService.saveProduct(product2);
        });
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회")
    void findProductById_NotFound() {
        //when
        Product foundProduct = productService.findProductById(999L);

        //then
        assertNull(foundProduct);
    }

}