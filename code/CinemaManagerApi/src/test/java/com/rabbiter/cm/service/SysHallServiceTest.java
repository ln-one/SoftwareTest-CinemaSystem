package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysHall;
import com.rabbiter.cm.mapper.SysHallMapper;
import com.rabbiter.cm.service.impl.SysHallServiceImpl;
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
import static org.mockito.Mockito.*;

/**
 * 影厅模块单元测试
 * 测试负责人：成员D
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("影厅服务测试")
class SysHallServiceTest {

    @Mock
    private SysHallMapper sysHallMapper;

    @InjectMocks
    private SysHallServiceImpl sysHallService;

    private SysHall testHall;

    @BeforeEach
    void setUp() {
        testHall = new SysHall();
        testHall.setHallId(1L);
        testHall.setCinemaId(1L);
        testHall.setHallName("1号厅");
        testHall.setHallCategory("IMAX");
        testHall.setRowNums(10);
        testHall.setSeatNumsRow(20);
        testHall.setSeatNums(200);
    }

    @Test
    @DisplayName("查询所有影厅")
    void findAllHalls_ReturnsHallList() {
        List<SysHall> hallList = Arrays.asList(testHall, new SysHall());
        when(sysHallMapper.findAllHalls(any())).thenReturn(hallList);

        List<SysHall> result = sysHallService.findAllHalls(new SysHall());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("查询所有影厅 - 无影厅")
    void findAllHalls_NoHalls_ReturnsEmptyList() {
        when(sysHallMapper.findAllHalls(any())).thenReturn(Collections.emptyList());

        List<SysHall> result = sysHallService.findAllHalls(new SysHall());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("根据ID查询影厅 - 影厅存在")
    void findHallById_HallExists_ReturnsHall() {
        when(sysHallMapper.findHallById(any())).thenReturn(testHall);

        SysHall result = sysHallService.findHallById(testHall);

        assertNotNull(result);
        assertEquals("1号厅", result.getHallName());
    }

    @Test
    @DisplayName("根据ID查询影厅 - 影厅不存在")
    void findHallById_HallNotExists_ReturnsNull() {
        when(sysHallMapper.findHallById(any())).thenReturn(null);

        SysHall result = sysHallService.findHallById(new SysHall());

        assertNull(result);
    }

    @Test
    @DisplayName("添加影厅")
    void addHall_ReturnsInsertedCount() {
        when(sysHallMapper.addHall(any())).thenReturn(1);

        int result = sysHallService.addHall(testHall);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("更新影厅")
    void updateHall_ReturnsUpdatedCount() {
        when(sysHallMapper.updateHall(any())).thenReturn(1);

        int result = sysHallService.updateHall(testHall);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除影厅 - 单个删除")
    void deleteHall_SingleHall_ReturnsDeletedCount() {
        when(sysHallMapper.deleteHall(any())).thenReturn(1);

        int result = sysHallService.deleteHall(new SysHall[]{testHall});

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除影厅 - 批量删除")
    void deleteHall_MultipleHalls_ReturnsDeletedCount() {
        when(sysHallMapper.deleteHall(any())).thenReturn(1);

        SysHall hall2 = new SysHall();
        hall2.setHallId(2L);
        int result = sysHallService.deleteHall(new SysHall[]{testHall, hall2});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("验证影厅座位数计算")
    void findHallById_VerifySeatCalculation() {
        when(sysHallMapper.findHallById(any())).thenReturn(testHall);

        SysHall result = sysHallService.findHallById(testHall);

        assertEquals(200, result.getSeatNums());
        assertEquals(10, result.getRowNums());
        assertEquals(20, result.getSeatNumsRow());
    }

    @Test
    @DisplayName("验证影厅类别")
    void findHallById_VerifyHallCategory() {
        when(sysHallMapper.findHallById(any())).thenReturn(testHall);

        SysHall result = sysHallService.findHallById(testHall);

        assertEquals("IMAX", result.getHallCategory());
    }
}
