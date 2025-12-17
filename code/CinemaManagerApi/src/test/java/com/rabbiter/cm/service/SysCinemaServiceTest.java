package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysCinema;
import com.rabbiter.cm.mapper.SysCinemaMapper;
import com.rabbiter.cm.service.impl.SysCinemaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 影院模块单元测试
 * 测试负责人：成员D
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("影院服务测试")
class SysCinemaServiceTest {

    @Mock
    private SysCinemaMapper sysCinemaMapper;

    @InjectMocks
    private SysCinemaServiceImpl sysCinemaService;

    private SysCinema testCinema;

    @BeforeEach
    void setUp() {
        testCinema = new SysCinema();
        testCinema.setCinemaId(1L);
        testCinema.setCinemaName("测试影院");
        testCinema.setCinemaPhone("010-12345678");
        testCinema.setCinemaAddress("北京市海淀区");
    }

    @Test
    @DisplayName("查询影院信息")
    void findCinema_ReturnsCinema() {
        when(sysCinemaMapper.findCinema()).thenReturn(testCinema);

        SysCinema result = sysCinemaService.findCinema();

        assertNotNull(result);
        assertEquals("测试影院", result.getCinemaName());
    }

    @Test
    @DisplayName("查询影院信息 - 无影院")
    void findCinema_NoCinema_ReturnsNull() {
        when(sysCinemaMapper.findCinema()).thenReturn(null);

        SysCinema result = sysCinemaService.findCinema();

        assertNull(result);
    }

    @Test
    @DisplayName("更新影院信息 - 成功")
    void updateCinema_Success_ReturnsUpdatedCount() {
        when(sysCinemaMapper.updateCinema(any())).thenReturn(1);

        int result = sysCinemaService.updateCinema(testCinema);

        assertEquals(1, result);
        verify(sysCinemaMapper, times(1)).updateCinema(any());
    }

    @Test
    @DisplayName("更新影院信息 - 失败")
    void updateCinema_Failure_ReturnsZero() {
        when(sysCinemaMapper.updateCinema(any())).thenReturn(0);

        int result = sysCinemaService.updateCinema(testCinema);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("根据ID查询影院 - 影院存在")
    void findCinemaById_CinemaExists_ReturnsCinema() {
        when(sysCinemaMapper.findCinemaById(1L)).thenReturn(testCinema);

        SysCinema result = sysCinemaService.findCinemaById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCinemaId());
    }

    @Test
    @DisplayName("根据ID查询影院 - 影院不存在")
    void findCinemaById_CinemaNotExists_ReturnsNull() {
        when(sysCinemaMapper.findCinemaById(999L)).thenReturn(null);

        SysCinema result = sysCinemaService.findCinemaById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("验证影院电话格式")
    void findCinema_VerifyPhoneFormat() {
        when(sysCinemaMapper.findCinema()).thenReturn(testCinema);

        SysCinema result = sysCinemaService.findCinema();

        assertNotNull(result.getCinemaPhone());
        assertTrue(result.getCinemaPhone().contains("-"));
    }

    @Test
    @DisplayName("验证影院地址不为空")
    void findCinema_VerifyAddressNotEmpty() {
        when(sysCinemaMapper.findCinema()).thenReturn(testCinema);

        SysCinema result = sysCinemaService.findCinema();

        assertNotNull(result.getCinemaAddress());
        assertFalse(result.getCinemaAddress().isEmpty());
    }
}
