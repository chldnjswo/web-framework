package kr.ac.hansung.cse.controller;

import jakarta.validation.Valid;
import kr.ac.hansung.cse.exception.DuplicateCategoryException;
import kr.ac.hansung.cse.model.CategoryForm;
import kr.ac.hansung.cse.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService){
        this.categoryService=categoryService;
    }

    @GetMapping
    public String listCategories(Model model){
        model.addAttribute("categories", categoryService.getAllCategories());

        return "categoryList";
    }
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return "categoryForm"; }

    @PostMapping("/create")
    public String createCategory(
            @Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model){
        if (bindingResult.hasErrors()) return "categoryForm";
        try {
            categoryService.createCategory(categoryForm.getName());
            redirectAttributes.addFlashAttribute("successMessage", "등록 완료");
        } catch (DuplicateCategoryException e) {

            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            return "categoryForm"; }
        return "redirect:/categories"; }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "삭제 완료");
        } catch (IllegalStateException e) {
            // 연결된 상품 있을 때 → Flash로 오류 메시지 전달
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }  return "redirect:/categories"; }


}
