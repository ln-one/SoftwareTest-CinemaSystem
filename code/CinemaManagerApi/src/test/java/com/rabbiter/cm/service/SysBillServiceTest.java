package com.rabbiter.cm.service;

import com.rabbiter.cm.domain.SysBill;
import com.rabbiter.cm.mapper.SysBillMapper;
import com.rabbiter.cm.service.impl.SysBillServiceImpl;
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
 * 订单模块单元测试
 * 测试负责人：成员B
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务测试")
class SysBillServiceTest {

    @Mock
    private SysBillMapper sysBillMapper;

    @InjectMocks
    private SysBillServiceImpl sysBillService;

    private SysBill testBill;

    @BeforeEach
    void setUp() {
        testBill = new SysBill();
        testBill.setBillId(1L);
        testBill.setUserId(1L);
        testBill.setSessionId(1L);
        testBill.setSeats("[\"1排1座\"]");
    }

    @Test
    @DisplayName("查询所有订单")
    void findAllBills_ReturnsBillList() {
        List<SysBill> billList = Arrays.asList(testBill, new SysBill());
        when(sysBillMapper.findAllBills(any())).thenReturn(billList);

        List<SysBill> result = sysBillService.findAllBills(new SysBill());

        assertEquals(2, result.size());
        verify(sysBillMapper, times(1)).findAllBills(any());
    }

    @Test
    @DisplayName("根据ID查询订单 - 订单存在")
    void findBillById_BillExists_ReturnsBill() {
        when(sysBillMapper.findBillById(1L)).thenReturn(testBill);

        SysBill result = sysBillService.findBillById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getBillId());
    }

    @Test
    @DisplayName("根据ID查询订单 - 订单不存在")
    void findBillById_BillNotExists_ReturnsNull() {
        when(sysBillMapper.findBillById(999L)).thenReturn(null);

        SysBill result = sysBillService.findBillById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("添加订单 - 成功")
    void addBill_Success_ReturnsBill() {
        when(sysBillMapper.addBill(any())).thenReturn(1);

        Object result = sysBillService.addBill(testBill);

        assertEquals(testBill, result);
    }

    @Test
    @DisplayName("添加订单 - 失败")
    void addBill_Failure_ReturnsZero() {
        when(sysBillMapper.addBill(any())).thenReturn(0);

        Object result = sysBillService.addBill(testBill);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("更新订单")
    void updateBill_ReturnsUpdatedCount() {
        when(sysBillMapper.updateBill(any())).thenReturn(1);

        int result = sysBillService.updateBill(testBill);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除订单 - 单个删除")
    void deleteBill_SingleBill_ReturnsDeletedCount() {
        when(sysBillMapper.deleteBill(1L)).thenReturn(1);

        int result = sysBillService.deleteBill(new Long[]{1L});

        assertEquals(1, result);
    }

    @Test
    @DisplayName("删除订单 - 批量删除")
    void deleteBill_MultipleBills_ReturnsDeletedCount() {
        when(sysBillMapper.deleteBill(anyLong())).thenReturn(1);

        int result = sysBillService.deleteBill(new Long[]{1L, 2L, 3L});

        assertEquals(3, result);
    }

    @Test
    @DisplayName("查询超时订单")
    void findTimeoutBill_ReturnsTimeoutBills() {
        List<SysBill> timeoutBills = Arrays.asList(testBill);
        when(sysBillMapper.findTimeoutBill()).thenReturn(timeoutBills);

        List<SysBill> result = sysBillService.findTimeoutBill();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("查询超时订单 - 无超时订单")
    void findTimeoutBill_NoTimeoutBills_ReturnsEmptyList() {
        when(sysBillMapper.findTimeoutBill()).thenReturn(Collections.emptyList());

        List<SysBill> result = sysBillService.findTimeoutBill();

        assertTrue(result.isEmpty());
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("删除订单 - 空数组返回0")
    void deleteBill_EmptyArray_ReturnsZero() {
        int result = sysBillService.deleteBill(new Long[]{});
        assertEquals(0, result);
        verify(sysBillMapper, never()).deleteBill(anyLong());
    }

    @Test
    @DisplayName("删除订单 - 部分删除失败")
    void deleteBill_PartialFailure_ReturnsPartialCount() {
        // 模拟第二个删除失败
        when(sysBillMapper.deleteBill(1L)).thenReturn(1);
        when(sysBillMapper.deleteBill(2L)).thenReturn(0); // 失败
        when(sysBillMapper.deleteBill(3L)).thenReturn(1);

        int result = sysBillService.deleteBill(new Long[]{1L, 2L, 3L});

        assertEquals(2, result); // 只成功删除2个
    }

    @Test
    @DisplayName("添加订单 - 验证返回对象包含正确信息")
    void addBill_Success_ReturnsBillWithCorrectInfo() {
        SysBill newBill = new SysBill();
        newBill.setUserId(100L);
        newBill.setSessionId(200L);
        newBill.setSeats("[\"5排10座\", \"5排11座\"]");
        
        when(sysBillMapper.addBill(any())).thenReturn(1);

        Object result = sysBillService.addBill(newBill);

        assertTrue(result instanceof SysBill);
        SysBill returnedBill = (SysBill) result;
        assertEquals(100L, returnedBill.getUserId());
        assertEquals(200L, returnedBill.getSessionId());
    }

    // ==================== 潜在Bug测试 ====================

    @Test
    @DisplayName("【Bug测试】删除订单 - null数组应抛出异常")
    void deleteBill_NullArray_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            sysBillService.deleteBill(null);
        });
    }

    @Test
    @DisplayName("【Bug测试】查询订单 - null参数处理")
    void findAllBills_NullParam_ShouldHandle() {
        when(sysBillMapper.findAllBills(null)).thenReturn(Collections.emptyList());
        
        List<SysBill> result = sysBillService.findAllBills(null);
        
        assertNotNull(result);
    }
}
