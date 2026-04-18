package com.payment.product.service;

import com.payment.common.exception.BusinessException;
import com.payment.common.exception.ResourceNotFoundException;
import com.payment.product.dto.CategoryDTO;
import com.payment.product.entity.Category;
import com.payment.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Category Service — Business logic for category management.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BusinessException("Category '" + dto.getName() + "' already exists", "ERR_CATEGORY_EXISTS");
        }

        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created: {}", saved.getName());
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        Category updated = categoryRepository.save(category);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        categoryRepository.deleteById(id);
        log.info("Category {} deleted", id);
    }

    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
