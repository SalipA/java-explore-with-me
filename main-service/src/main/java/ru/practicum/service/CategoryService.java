package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.dto.reversible.CategoryDto;
import ru.practicum.entity.Category;
import ru.practicum.mapper.CategoryMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto addCategory(CategoryDto categoryDto) {
        Category newCategory = CategoryMapper.toCategory(categoryDto);
        Category saved = categoryRepository.save(newCategory);
        log.info("Category value = {} has been saved, id = {}", categoryDto, saved.getId());
        return CategoryMapper.toCategoryDto(saved);
    }

    public void deleteCategory(Long catId) {
        getCategoryIfExists(catId);
        categoryRepository.deleteById(catId);
        log.info("Category with id={} has been deleted", catId);
    }

    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        getCategoryIfExists(catId);
        Category updatedCategory = CategoryMapper.toCategory(categoryDto);
        updatedCategory.setId(catId);
        Category saved = categoryRepository.save(updatedCategory);
        log.info("Category value = {} has been updated, id = {}", updatedCategory, saved.getId());
        return CategoryMapper.toCategoryDto(saved);
    }

    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(from, size);
        List<Category> categories = categoryRepository.findAll(pageRequest).getContent();
        log.info("Get request for Categories list processed successfully");
        return CategoryMapper.toCategoryDtoList(categories);
    }

    public CategoryDto getCategory(Long catId) {
        Category categoryFromDb = getCategoryIfExists(catId);
        log.info("Get request for Category by id = {} processed successfully", catId);
        return CategoryMapper.toCategoryDto(categoryFromDb);
    }

    public Category getCategoryIfExists(Long catId) {
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isPresent()) {
            log.info("Get request for Category by id = {} processed successfully", catId);
            return category.get();
        } else {
            String message = "Category with id=" + catId + " was not found";
            log.error(message);
            throw new NotFoundException(message);
        }
    }

    public List<Category> getCategoriesById(Long[] ids) {
        if (ids == null) {
            log.info("Get request for Categories list by ids = null processed successfully");
            return List.of();
        } else {
            log.info("Get request for Categories list by ids = {} processed successfully", Arrays.toString(ids));
            return categoryRepository.getCategoriesByIdIn(Arrays.asList(ids));
        }
    }
}