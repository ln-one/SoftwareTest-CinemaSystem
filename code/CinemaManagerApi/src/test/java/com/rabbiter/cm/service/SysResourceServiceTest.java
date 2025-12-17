package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysResource;
import com.rabbiter.cm.mapper.SysResourceMapper;
import com.rabbiter.cm.service.impl.SysResourceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 资源权限模块单元测试
 * 测试负责人：成员E
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("资源权限服务测试")
class SysResourceServiceTest {

    @Mock
    private SysResourceMapper sysResourceMapper;

    @InjectMocks
    private SysResourceServiceImpl sysResourceService;

    private SysResource testResource;
    private SysResource parentResource;

    @BeforeEach
    void setUp() {
        parentResource = new SysResource();
        parentResource.setId(1L);
        parentResource.setName("系统管理");
        parentResource.setLevel(1);
        parentResource.setParentId(0L);

        testResource = new SysResource();
        testResource.setId(2L);
        testResource.setName("用户管理");
        testResource.setLevel(2);
        testResource.setParentId(1L);
    }

    @Test
    @DisplayName("查询所有资源")
    void findAllResources_ReturnsResourceList() {
        List<SysResource> resourceList = Arrays.asList(parentResource, testResource);
        when(sysResourceMapper.findAllResources()).thenReturn(resourceList);

        List<SysResource> result = sysResourceService.findAllResources();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("查询所有资源 - 无资源")
    void findAllResources_NoResources_ReturnsEmptyList() {
        when(sysResourceMapper.findAllResources()).thenReturn(Collections.emptyList());

        List<SysResource> result = sysResourceService.findAllResources();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询带子资源的资源列表")
    void findWithChildren_ReturnsResourceList() {
        List<SysResource> resourceList = Arrays.asList(parentResource);
        when(sysResourceMapper.findWithChildren()).thenReturn(resourceList);

        List<SysResource> result = sysResourceService.findWithChildren();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("查询所有资源及其子资源")
    void findAllWithAllChildren_ReturnsResourceList() {
        List<SysResource> resourceList = Arrays.asList(parentResource, testResource);
        when(sysResourceMapper.findAllWithAllChildren()).thenReturn(resourceList);

        List<SysResource> result = sysResourceService.findAllWithAllChildren();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("根据ID查询资源 - 资源存在")
    void findResourceById_ResourceExists_ReturnsResource() {
        when(sysResourceMapper.findResourceById(2L)).thenReturn(testResource);

        SysResource result = sysResourceService.findResourceById(2L);

        assertNotNull(result);
        assertEquals("用户管理", result.getName());
    }

    @Test
    @DisplayName("根据ID查询资源 - 资源不存在")
    void findResourceById_ResourceNotExists_ReturnsNull() {
        when(sysResourceMapper.findResourceById(999L)).thenReturn(null);

        SysResource result = sysResourceService.findResourceById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("添加资源 - 顶级资源")
    void addResource_TopLevel_SetsLevelToOne() {
        SysResource newResource = new SysResource();
        newResource.setName("新模块");
        newResource.setParentId(0L);
        when(sysResourceMapper.addResource(any())).thenReturn(1);

        int result = sysResourceService.addResource(newResource);

        assertEquals(1, result);
        assertEquals(1, newResource.getLevel());
    }

    @Test
    @DisplayName("添加资源 - 子资源")
    void addResource_ChildLevel_SetsLevelBasedOnParent() {
        SysResource newResource = new SysResource();
        newResource.setName("子模块");
        newResource.setParentId(1L);
        when(sysResourceMapper.findResourceById(1L)).thenReturn(parentResource);
        when(sysResourceMapper.addResource(any())).thenReturn(1);

        int result = sysResourceService.addResource(newResource);

        assertEquals(1, result);
        assertEquals(2, newResource.getLevel());
    }

    @Test
    @DisplayName("更新资源 - 顶级资源")
    void updateResource_TopLevel_SetsLevelToOne() {
        SysResource updateResource = new SysResource();
        updateResource.setId(1L);
        updateResource.setName("更新模块");
        updateResource.setParentId(0L);
        when(sysResourceMapper.updateResource(any())).thenReturn(1);

        int result = sysResourceService.updateResource(updateResource);

        assertEquals(1, result);
        assertEquals(1, updateResource.getLevel());
    }

    @Test
    @DisplayName("更新资源 - 子资源")
    void updateResource_ChildLevel_SetsLevelBasedOnParent() {
        SysResource updateResource = new SysResource();
        updateResource.setId(2L);
        updateResource.setName("更新子模块");
        updateResource.setParentId(1L);
        when(sysResourceMapper.findResourceById(1L)).thenReturn(parentResource);
        when(sysResourceMapper.updateResource(any())).thenReturn(1);

        int result = sysResourceService.updateResource(updateResource);

        assertEquals(1, result);
        assertEquals(2, updateResource.getLevel());
    }

    @Test
    @DisplayName("删除资源 - 单个删除")
    void deleteResource_SingleResource_ReturnsDeletedCount() {
        when(sysResourceMapper.deleteResource(1L)).thenReturn(1);

        int result = sysResourceService.deleteResource(new Long[]{1L});

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除资源 - 批量删除")
    void deleteResource_MultipleResources_ReturnsDeletedCount() {
        when(sysResourceMapper.deleteResource(anyLong())).thenReturn(1);

        int result = sysResourceService.deleteResource(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("验证资源层级关系")
    void findResourceById_VerifyLevelHierarchy() {
        when(sysResourceMapper.findResourceById(2L)).thenReturn(testResource);

        SysResource result = sysResourceService.findResourceById(2L);

        assertEquals(2, result.getLevel());
        assertEquals(1L, result.getParentId());
    }
}
