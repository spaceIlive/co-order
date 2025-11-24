package gwan.co_order.repository;

import gwan.co_order.domain.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoreRepository {

    private final EntityManager em;

    public void saveStore(Store store) {
        em.persist(store);
    }

    public Store findStoreById(Long storeId) {
        return em.find(Store.class, storeId);
    }
    public void deleteStore(Store store) {
        em.remove(store);
    }

    public List<Store> findAllStores() {
        return em.createQuery("select s from Store s", Store.class)
                .getResultList();
    }

    public Store findStoreByName(String storeName) {
        return em.createQuery("select s from Store s where s.name = :name", Store.class)
                .setParameter("name", storeName)
                .getSingleResult();
    }
}
