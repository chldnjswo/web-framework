package kr.ac.hansung.cse.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.ac.hansung.cse.model.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepository {

    @PersistenceContext
    private EntityManager em;

    public Category save(Category category) {
        em.persist(category);
        return category;
    }

    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(em.find(Category.class, id));
    }

    public List<Category> findAll() {
        return em.createQuery("SELECT c FROM Category c ORDER BY c.id", Category.class)
                .getResultList();
    }

    public Optional<Category> findByName(String name) {
        List<Category> result = em.createQuery(
                        "SELECT c FROM Category c " +
                                "WHERE LOWER(REPLACE(TRIM(c.name), ' ', '')) = LOWER(:name)", Category.class)
                .setParameter("name", normalizeName(name))
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    public long countProductsByCategoryId(Long categoryId){
        return em.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.category.id = :id",
                Long.class)
                .setParameter("id", categoryId).getSingleResult();

    }
    public void delete(Long id){
        Category c = em.find(Category.class, id); if(c!=null) em.remove(c);
    }


    public Optional<Category> findByIdWithProducts(Long id) {
        List<Category> result = em.createQuery(
                        "SELECT DISTINCT c FROM Category c JOIN FETCH c.products WHERE c.id = :id",
                        Category.class)
                .setParameter("id", id)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    private String normalizeName(String name) {
        return name == null ? null : name.replaceAll("\\s+", "");
    }
}
