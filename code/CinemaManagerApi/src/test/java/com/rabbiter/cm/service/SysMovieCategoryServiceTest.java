package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysMovieCategory;
import com.rabbiter.cm.domain.SysMovieToCategory;
import com.rabbiter.cm.mapper.SysMovieCategoryMapper;
import com.rabbiter.cm.service.impl.SysMovieCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 电影类别模块单元测试
 * 测试负责人：成员C
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("电影类别服务测试")
class SysMovieCategoryServiceTest {

    @Mock
    private SysMovieCategoryMapper sysMovieCategoryMapper;

    @InjectMocks
    private SysMovieCategoryServiceImpl sysMovieCategoryService;

    private SysMovieCategory testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new SysMovieCategory();
        testCategory.setMovieCategoryId(1L);
        testCategory.setMovieCategoryName("动作");
    }

    @Test
    @DisplayName("查询所有电影类别")
    void findAllCategorys_ReturnsCategoryList() {
        List<SysMovieCategory> categoryList = Arrays.asList(testCategory, new SysMovieCategory());
        when(sysMovieCategoryMapper.findAllCategorys()).thenReturn(categoryList);

        List<SysMovieCategory> result = sysMovieCategoryService.findAllCategorys();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("根据ID查询类别 - 类别存在")
    void findCategoryById_CategoryExists_ReturnsCategory() {
        when(sysMovieCategoryMapper.findCategoryById(1L)).thenReturn(testCategory);

        SysMovieCategory result = sysMovieCategoryService.findCategoryById(1L);

        assertNotNull(result);
        assertEquals("动作", result.getMovieCategoryName());
    }

    @Test
    @DisplayName("根据ID查询类别 - 类别不存在")
    void findCategoryById_CategoryNotExists_ReturnsNull() {
        when(sysMovieCategoryMapper.findCategoryById(999L)).thenReturn(null);

        SysMovieCategory result = sysMovieCategoryService.findCategoryById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("添加电影类别")
    void addCategory_ReturnsInsertedCount() {
        when(sysMovieCategoryMapper.addCategory(any())).thenReturn(1);

        int result = sysMovieCategoryService.addCategory(testCategory);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("更新电影类别")
    void updateCategory_ReturnsUpdatedCount() {
        when(sysMovieCategoryMapper.updateCategory(any())).thenReturn(1);

        int result = sysMovieCategoryService.updateCategory(testCategory);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除电影类别 - 单个删除")
    void deleteCategory_SingleCategory_ReturnsDeletedCount() {
        when(sysMovieCategoryMapper.deleteCategory(1L)).thenReturn(1);

        int result = sysMovieCategoryService.deleteCategory(new Long[]{1L});

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除电影类别 - 批量删除")
    void deleteCategory_MultipleCategories_ReturnsDeletedCount() {
        when(sysMovieCategoryMapper.deleteCategory(anyLong())).thenReturn(1);

        int result = sysMovieCategoryService.deleteCategory(new Long[]{1L, 2L});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("添加电影与类别关联")
    void addMovieToCategory_ReturnsInsertedCount() {
        SysMovieToCategory movieToCategory = new SysMovieToCategory();
        movieToCategory.setMovieId(1L);
        movieToCategory.setMovieCategoryId(1L);
        when(sysMovieCategoryMapper.addMovieToCategory(any())).thenReturn(1);

        int result = sysMovieCategoryService.addMovieToCategory(movieToCategory);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除电影与类别关联")
    void deleteMovieToCategory_ReturnsDeletedCount() {
        SysMovieToCategory movieToCategory = new SysMovieToCategory();
        movieToCategory.setMovieId(1L);
        movieToCategory.setMovieCategoryId(1L);
        when(sysMovieCategoryMapper.deleteMovieToCategory(any())).thenReturn(1);

        int result = sysMovieCategoryService.deleteMovieToCategory(movieToCategory);

        assertEquals(1, result);
    }
}
