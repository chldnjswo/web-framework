package kr.ac.hansung.cse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.ac.hansung.cse.config.DbConfig;
import kr.ac.hansung.cse.model.Category;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional                              // 각 테스트는 독립 트랜잭션 (테스트 후 롤백)
@ExtendWith(SpringExtension.class)          // JUnit 5에서 Spring 빈(Bean) 주입 및 컨테이너 기능을 쓸 수 있게 연결
@ContextConfiguration(classes = DbConfig.class)   // 테스트를 실행할 때 사용할 설정(Configuration)지정
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    private Product testProduct;

    @BeforeEach
    public void setUp() {
        testProduct = new Product("Test Laptop",null,
                new BigDecimal("999.99"),"Test description");
        productRepository.save(testProduct);  // persist in test tx (rolled back after each test)
    }

    @Test
    @DisplayName("Test1: findById")
    public void testFindById() {
        Optional<Product> found = productRepository.findById(testProduct.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Laptop", found.get().getName());
    }

    @Test @DisplayName("Test2: findAll")
    public void testFindAll() {
        List<Product> products = productRepository.findAll();
        assertFalse(products.isEmpty());
    }

    @Test @DisplayName("Test3: update via merge()")
    public void testUpdate() {
        Long id = testProduct.getId();

        // ① flush: setUp()의 INSERT를 DB에 반영
        // ② clear: 1차 캐시 초기화 → testProduct는 Detached 상태
        em.flush();
        em.clear();

        // ③ find: DB에서 다시 로드 (Hibernate가 tags 컬렉션 스냅샷을 세션에 등록)
        //    persist() 직후 detach()하면 컬렉션 스냅샷이 없어 merge() 시 NPE 발생
        testProduct = em.find(Product.class, id);

        // ④ detach: 다른 트랜잭션에서 받은 Detached 엔티티 상황 시뮬레이션
        em.detach(testProduct);
        testProduct.setName("Updated Laptop");  // Detached 변경 (DB 반영 안 됨)

        // ⑤ merge: Detached → Managed로 복귀, UPDATE SQL 실행
        Product updated = productRepository.update(testProduct);
        assertEquals("Updated Laptop", updated.getName());
    }

    @Test @DisplayName("Test4: delete")
    public void testDelete() {
        Long id = testProduct.getId();
        productRepository.delete(id);
        Optional<Product> deleted = productRepository.findById(id);
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("Test5: findByNameContaining")
    public void testFindByNameContaining() {
        List<Product> products = productRepository.findByNameContaining("Laptop");

        assertFalse(products.isEmpty());
        assertTrue(products.stream().anyMatch(product -> "Test Laptop".equals(product.getName())));
    }

    @Test
    @DisplayName("Test6: findByNameContainingAndCategoryId")
    public void testFindByNameContainingAndCategoryId() {
        Category electronics = new Category("테스트전자카테고리");
        Category books = new Category("테스트도서카테고리");
        em.persist(electronics);
        em.persist(books);

        Product gamingLaptop = new Product("Gaming Laptop", electronics,
                new BigDecimal("1999.99"), "Electronics category");
        Product laptopGuide = new Product("Laptop Guide", books,
                new BigDecimal("29.99"), "Books category");
        em.persist(gamingLaptop);
        em.persist(laptopGuide);
        em.flush();
        em.clear();

        List<Product> products = productRepository.findByNameContainingAndCategoryId("Laptop", electronics.getId());

        assertEquals(1, products.size());
        assertEquals("Gaming Laptop", products.get(0).getName());
        assertNotNull(products.get(0).getCategory());
        assertEquals("테스트전자카테고리", products.get(0).getCategory().getName());
    }
}
