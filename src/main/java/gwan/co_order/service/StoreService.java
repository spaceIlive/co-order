package gwan.co_order.service;

import gwan.co_order.domain.Product;
import gwan.co_order.domain.Store;
import gwan.co_order.repository.ProductRepository;
import gwan.co_order.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    public void saveStore(Store store) {
        storeRepository.saveStore(store);
    }

    public void deleteStore(Long storeId) {
        Store store = storeRepository.findStoreById(storeId);
        storeRepository.deleteStore(store);
    }
    
    @Transactional(readOnly = true)
    public Store findStoreById(Long storeId) {
        return storeRepository.findStoreById(storeId);
    }

    @Transactional(readOnly = true)
    public List<Store> findAllStores() {
        return storeRepository.findAllStores();
    }

    @Transactional(readOnly = true)
    public List<Product> getStoreProducts(Long storeId) {
        return productRepository.findProductsByStoreId(storeId);
    }
}
