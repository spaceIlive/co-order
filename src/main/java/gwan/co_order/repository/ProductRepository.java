package gwan.co_order.repository;

import gwan.co_order.domain.Product;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final EntityManager em;

    public void saveProduct(Product product) {
        em.persist(product);
    }

    public Product findProductById(Long productId) {
        return em.find(Product.class, productId);
    }

    public List<Product> findProductsByStoreId(Long storeId) {
        return em.createQuery("select p from Product p where p.store.id = :store_id", Product.class)
                .setParameter("store_id", storeId)
                .getResultList();
    }

    public void delete(Product product) {
        em.remove(product);
    }
    
    // 같은 가게에서 제품 이름 중복 체크
    public boolean existsByStoreIdAndName(Long storeId, String name) {
        Long count = em.createQuery(
            "select count(p) from Product p " +
            "where p.store.id = :storeId and p.name = :name", 
            Long.class)
            .setParameter("storeId", storeId)
            .setParameter("name", name)
            .getSingleResult();
        
        return count > 0;
    }
}
