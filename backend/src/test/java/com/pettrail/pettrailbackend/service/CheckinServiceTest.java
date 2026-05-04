package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.CheckinItem;
import com.pettrail.pettrailbackend.entity.CheckinRecord;
import com.pettrail.pettrailbackend.entity.CheckinStats;
import com.pettrail.pettrailbackend.entity.UserHiddenItem;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.CheckinItemMapper;
import com.pettrail.pettrailbackend.mapper.CheckinRecordMapper;
import com.pettrail.pettrailbackend.mapper.CheckinStatsMapper;
import com.pettrail.pettrailbackend.mapper.UserHiddenItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckinServiceTest {

    @Mock
    private CheckinItemMapper checkinItemMapper;

    @Mock
    private CheckinRecordMapper checkinRecordMapper;

    @Mock
    private CheckinStatsMapper checkinStatsMapper;

    @Mock
    private UserHiddenItemMapper userHiddenItemMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private CheckinService checkinService;

    private CheckinItem defaultItem;
    private CheckinItem customItem;
    private CheckinStats existingStats;

    @BeforeEach
    void setUp() {
        defaultItem = new CheckinItem();
        defaultItem.setId(1L);
        defaultItem.setUserId(0L);
        defaultItem.setName("喂食");
        defaultItem.setIcon("🍽️");
        defaultItem.setIsDefault(1);
        defaultItem.setIsEnabled(1);

        customItem = new CheckinItem();
        customItem.setId(10L);
        customItem.setUserId(100L);
        customItem.setName("遛狗");
        customItem.setIcon("🐕");
        customItem.setIsDefault(0);
        customItem.setIsEnabled(1);

        existingStats = new CheckinStats();
        existingStats.setId(1L);
        existingStats.setUserId(100L);
        existingStats.setItemId(1L);
        existingStats.setTotalCount(5);
        existingStats.setCurrentStreak(3);
        existingStats.setMaxStreak(5);
        existingStats.setLastCheckinDate(LocalDate.now().minusDays(1));
    }

    @Test
    void getUserCheckinItems_shouldIncludeCustomAndHidden() {
        when(checkinItemMapper.selectDefaultItems()).thenReturn(new ArrayList<>(Arrays.asList(defaultItem)));
        when(checkinItemMapper.selectByUserId(100L)).thenReturn(Arrays.asList(customItem));
        when(userHiddenItemMapper.selectHiddenItemIdsByUserId(100L)).thenReturn(Arrays.asList(1L));

        List<CheckinItem> items = checkinService.getUserCheckinItems(100L);

        assertEquals(2, items.size());
        assertTrue(items.get(0).getHidden());
        assertFalse(items.get(1).getHidden());
    }

    @Test
    void getUserCheckinItems_noUserId_shouldShowAllDefault() {
        when(checkinItemMapper.selectDefaultItems()).thenReturn(new ArrayList<>(Arrays.asList(defaultItem)));

        List<CheckinItem> items = checkinService.getUserCheckinItems(null);

        assertEquals(1, items.size());
        assertFalse(items.get(0).getHidden());
    }

    @Test
    void hideItem_shouldInsertWhenNotExists() {
        when(userHiddenItemMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userHiddenItemMapper.insert(any(UserHiddenItem.class))).thenReturn(1);

        checkinService.hideItem(100L, 1L);

        verify(userHiddenItemMapper).insert(any(UserHiddenItem.class));
    }

    @Test
    void hideItem_shouldNotInsertWhenAlreadyHidden() {
        UserHiddenItem existing = new UserHiddenItem();
        existing.setUserId(100L);
        existing.setItemId(1L);
        when(userHiddenItemMapper.selectList(any())).thenReturn(Arrays.asList(existing));

        checkinService.hideItem(100L, 1L);

        verify(userHiddenItemMapper, never()).insert(any(UserHiddenItem.class));
    }

    @Test
    void showItem_shouldDeleteHiddenRecord() {
        when(userHiddenItemMapper.delete(any())).thenReturn(1);

        checkinService.showItem(100L, 1L);

        verify(userHiddenItemMapper).delete(any());
    }

    @Test
    void createCustomItem_shouldCreateWithCorrectFields() {
        when(checkinItemMapper.insert(any(CheckinItem.class))).thenReturn(1);

        CheckinItem result = checkinService.createCustomItem(100L, "自定义项", "⭐", 1, "描述");

        assertNotNull(result);
        assertEquals(100L, result.getUserId());
        assertEquals("自定义项", result.getName());
        assertEquals(0, result.getIsDefault());
        assertEquals(1, result.getIsEnabled());
    }

    @Test
    void updateCustomItem_shouldUpdateWhenOwner() {
        when(checkinItemMapper.selectById(10L)).thenReturn(customItem);
        when(checkinItemMapper.updateById(any(CheckinItem.class))).thenReturn(1);

        CheckinItem result = checkinService.updateCustomItem(100L, 10L, "新名称", null, null, null);

        assertEquals("新名称", result.getName());
        verify(checkinItemMapper).updateById(any(CheckinItem.class));
    }

    @Test
    void updateCustomItem_shouldRejectNonOwner() {
        when(checkinItemMapper.selectById(10L)).thenReturn(customItem);

        assertThrows(BusinessException.class,
                () -> checkinService.updateCustomItem(999L, 10L, "新名称", null, null, null));
    }

    @Test
    void deleteCustomItem_shouldSoftDeleteWhenOwner() {
        when(checkinItemMapper.selectById(10L)).thenReturn(customItem);
        when(checkinItemMapper.updateById(any(CheckinItem.class))).thenReturn(1);

        checkinService.deleteCustomItem(100L, 10L);

        ArgumentCaptor<CheckinItem> captor = ArgumentCaptor.forClass(CheckinItem.class);
        verify(checkinItemMapper).updateById(captor.capture());
        assertEquals(0, captor.getValue().getIsEnabled());
    }

    @Test
    void deleteCustomItem_shouldRejectDefaultItem() {
        when(checkinItemMapper.selectById(1L)).thenReturn(defaultItem);

        assertThrows(BusinessException.class,
                () -> checkinService.deleteCustomItem(100L, 1L));
    }

    @Test
    void checkin_shouldCreateRecordWhenNotExists() {
        when(checkinRecordMapper.selectByUserIdItemIdAndDate(eq(100L), eq(10L), eq(1L), any(LocalDate.class)))
                .thenReturn(null);
        when(checkinRecordMapper.insert(any(CheckinRecord.class))).thenReturn(1);
        when(checkinStatsMapper.selectByUserIdAndItemId(100L, 1L)).thenReturn(null);
        when(checkinStatsMapper.insert(any(CheckinStats.class))).thenReturn(1);

        CheckinRecord result = checkinService.checkin(100L, 10L, 1L, "备注", null, null);

        assertNotNull(result);
        assertEquals(100L, result.getUserId());
        assertEquals(1, result.getStatus());
        verify(checkinRecordMapper).insert(any(CheckinRecord.class));
    }

    @Test
    void checkin_shouldRejectDuplicate() {
        CheckinRecord existing = new CheckinRecord();
        existing.setId(1L);
        existing.setUserId(100L);
        when(checkinRecordMapper.selectByUserIdItemIdAndDate(eq(100L), eq(10L), eq(1L), any(LocalDate.class)))
                .thenReturn(existing);

        assertThrows(BusinessException.class,
                () -> checkinService.checkin(100L, 10L, 1L, null, null, null));
    }

    @Test
    void checkin_shouldUpdateStreakWhenConsecutive() {
        when(checkinRecordMapper.selectByUserIdItemIdAndDate(eq(100L), eq(10L), eq(1L), any(LocalDate.class)))
                .thenReturn(null);
        when(checkinRecordMapper.insert(any(CheckinRecord.class))).thenReturn(1);
        when(checkinStatsMapper.selectByUserIdAndItemId(100L, 1L)).thenReturn(existingStats);
        when(checkinStatsMapper.updateById(any(CheckinStats.class))).thenReturn(1);

        checkinService.checkin(100L, 10L, 1L, null, null, null);

        ArgumentCaptor<CheckinStats> captor = ArgumentCaptor.forClass(CheckinStats.class);
        verify(checkinStatsMapper).updateById(captor.capture());
        assertEquals(4, captor.getValue().getCurrentStreak());
        assertEquals(6, captor.getValue().getTotalCount());
    }

    @Test
    void checkin_shouldResetStreakWhenNotConsecutive() {
        existingStats.setLastCheckinDate(LocalDate.now().minusDays(3));

        when(checkinRecordMapper.selectByUserIdItemIdAndDate(eq(100L), eq(10L), eq(1L), any(LocalDate.class)))
                .thenReturn(null);
        when(checkinRecordMapper.insert(any(CheckinRecord.class))).thenReturn(1);
        when(checkinStatsMapper.selectByUserIdAndItemId(100L, 1L)).thenReturn(existingStats);
        when(checkinStatsMapper.updateById(any(CheckinStats.class))).thenReturn(1);

        checkinService.checkin(100L, 10L, 1L, null, null, null);

        ArgumentCaptor<CheckinStats> captor = ArgumentCaptor.forClass(CheckinStats.class);
        verify(checkinStatsMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getCurrentStreak());
    }

    @Test
    void cancelCheckin_shouldUpdateStatusAndStats() {
        CheckinRecord record = new CheckinRecord();
        record.setId(1L);
        record.setUserId(100L);
        record.setItemId(1L);
        record.setStatus(1);

        when(checkinRecordMapper.selectById(1L)).thenReturn(record);
        when(checkinRecordMapper.updateById(any(CheckinRecord.class))).thenReturn(1);
        when(checkinStatsMapper.selectByUserIdAndItemId(100L, 1L)).thenReturn(existingStats);
        when(checkinStatsMapper.updateById(any(CheckinStats.class))).thenReturn(1);

        checkinService.cancelCheckin(100L, 1L);

        ArgumentCaptor<CheckinRecord> recordCaptor = ArgumentCaptor.forClass(CheckinRecord.class);
        verify(checkinRecordMapper).updateById(recordCaptor.capture());
        assertEquals(2, recordCaptor.getValue().getStatus());
    }

    @Test
    void cancelCheckin_shouldRejectOtherUser() {
        CheckinRecord record = new CheckinRecord();
        record.setId(1L);
        record.setUserId(999L);
        when(checkinRecordMapper.selectById(1L)).thenReturn(record);

        assertThrows(BusinessException.class, () -> checkinService.cancelCheckin(100L, 1L));
    }

    @Test
    void getCalendar_shouldReturnRecords() {
        CheckinRecord r1 = new CheckinRecord();
        r1.setId(1L);
        r1.setRecordDate(LocalDate.of(2026, 4, 15));
        when(checkinRecordMapper.selectByUserIdAndDateRange(eq(100L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(r1));

        List<CheckinRecord> result = checkinService.getCalendar(100L, 2026, 4);
        assertEquals(1, result.size());
    }

    @Test
    void getUserStats_shouldReturnAggregatedStats() {
        when(checkinStatsMapper.selectList(any())).thenReturn(Arrays.asList(existingStats));
        when(checkinRecordMapper.selectByUserIdAndDateRange(eq(100L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(checkinItemMapper.selectById(1L)).thenReturn(defaultItem);

        Map<String, Object> stats = checkinService.getUserStats(100L);

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalCount"));
        assertTrue(stats.containsKey("currentStreak"));
        assertTrue(stats.containsKey("maxStreak"));
        assertTrue(stats.containsKey("itemStats"));
    }
}
