package kr.ac.hansung.cse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.ac.hansung.cse.config.DbConfig;
import kr.ac.hansung.cse.exception.DuplicateCategoryException;
import kr.ac.hansung.cse.model.Category;
import kr.ac.hansung.cse.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DbConfig.class)
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("카테고리 저장 시 모든 공백을 제거한다")
    void createCategory_removesAllWhitespaceBeforeSaving() {
        Category saved = categoryService.createCategory("  전자   제품  ");

        assertEquals("전자제품", saved.getName());
    }

    @Test
    @DisplayName("중간 공백이 달라도 같은 카테고리로 중복 처리한다")
    void createCategory_rejectsDuplicateIgnoringInnerSpaces() {
        em.persist(new Category("전 자 제 품"));
        em.flush();

        assertThrows(DuplicateCategoryException.class,
                () -> categoryService.createCategory("전자제품"));
    }
}
