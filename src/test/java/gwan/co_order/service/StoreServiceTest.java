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
class StoreServiceTest {

    @Autowired StoreService storeService;
    @Autowired StoreRepository storeRepository;
    @Autowired ProductRepository productRepository;
    @Autowired EntityManager em;

    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setName("테스트가게");
        store.setAddress(new Address("서울시 강남구", 1.0, 1.0));
    }

    @Test
    @DisplayName("가게 등록")
    void saveStore() {
        //when
        storeService.saveStore(store);
        em.flush();
        em.clear();

        //then
        Store foundStore = storeRepository.findStoreById(store.getId());
        assertNotNull(foundStore);
        assertEquals("테스트가게", foundStore.getName());
    }

    @Test
    @DisplayName("가게 조회")
    void findStoreById() {
        //given
        storeService.saveStore(store);
        Long storeId = store.getId();
        em.flush();
        em.clear();

        //when
        Store foundStore = storeService.findStoreById(storeId);

        //then
        assertNotNull(foundStore);
        assertEquals(storeId, foundStore.getId());
        assertEquals("테스트가게", foundStore.getName());
    }

    @Test
    @DisplayName("모든 가게 조회")
    void findAllStores() {
        //given
        storeService.saveStore(store);
        
        Store store2 = new Store();
        store2.setName("테스트가게2");
        store2.setAddress(new Address("서울시 서초구", 2.0, 2.0));
        storeService.saveStore(store2);
        em.flush();
        em.clear();

        //when
        List<Store> stores = storeService.findAllStores();

        //then
        assertTrue(stores.size() >= 2);
        assertTrue(stores.stream().anyMatch(s -> s.getName().equals("테스트가게")));
        assertTrue(stores.stream().anyMatch(s -> s.getName().equals("테스트가게2")));
    }

    @Test
    @DisplayName("가게의 상품 목록 조회")
    void getStoreProducts() {
        //given
        storeService.saveStore(store);
        Long storeId = store.getId();

        Product product1 = new Product();
        product1.setName("상품1");
        product1.setPrice(10000);
        product1.setStore(store);
        productRepository.saveProduct(product1);

        Product product2 = new Product();
        product2.setName("상품2");
        product2.setPrice(20000);
        product2.setStore(store);
        productRepository.saveProduct(product2);
        em.flush();
        em.clear();

        //when
        List<Product> products = storeService.getStoreProducts(storeId);

        //then
        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(p -> p.getName().equals("상품1")));
        assertTrue(products.stream().anyMatch(p -> p.getName().equals("상품2")));
    }

    @Test
    @DisplayName("가게 삭제")
    void deleteStore() {
        //given
        storeService.saveStore(store);
        Long storeId = store.getId();
        em.flush();
        em.clear();

        //when
        storeService.deleteStore(storeId);
        em.flush();
        em.clear();

        //then
        Store deletedStore = storeRepository.findStoreById(storeId);
        assertNull(deletedStore);
    }

    @Test
    @DisplayName("존재하지 않는 가게 ID로 조회")
    void findStoreById_NotFound() {
        //when
        Store foundStore = storeService.findStoreById(999L);

        //then
        assertNull(foundStore);
    }


    @Test
    @DisplayName("존재하지 않는 가게의 상품 목록 조회")
    void getStoreProducts_NotFound() {
        //when
        List<Product> products = storeService.getStoreProducts(999L);

        //then
        assertTrue(products.isEmpty());
    }
}

