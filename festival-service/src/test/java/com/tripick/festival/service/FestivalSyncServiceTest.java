package com.tripick.festival.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.festival.client.CultureFestivalClient;
import com.tripick.festival.client.dto.CultureFestivalItem;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.repository.FestivalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FestivalSyncServiceTest {

    @Mock
    private CultureFestivalClient cultureFestivalClient;

    @Mock
    private FestivalRepository festivalRepository;

    @InjectMocks
    private FestivalSyncService festivalSyncService;

    @Test
    void syncAll_신규축제는_저장된다() {
        when(cultureFestivalClient.fetchAll()).thenReturn(List.of(item("서울 봄꽃 축제", "2026-04-10", "2026-04-20", "서울특별시 영등포구")));
        when(festivalRepository.findByApiId(any())).thenReturn(Optional.empty());

        int synced = festivalSyncService.syncAll();

        assertThat(synced).isEqualTo(1);
        verify(festivalRepository).save(any(Festival.class));
    }

    @Test
    void syncAll_이미존재하는축제는_갱신만하고_새로저장하지않는다() {
        Festival existing = Festival.builder()
                .apiId("existing")
                .name("부산 불꽃축제")
                .startDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 10, 1))
                .build();
        when(cultureFestivalClient.fetchAll()).thenReturn(List.of(item("부산 불꽃축제", "2026-10-04", "2026-10-04", "부산광역시 해운대구")));
        when(festivalRepository.findByApiId(any())).thenReturn(Optional.of(existing));

        int synced = festivalSyncService.syncAll();

        assertThat(synced).isEqualTo(1);
        assertThat(existing.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 4));
        verify(festivalRepository, never()).save(any());
    }

    @Test
    void syncAll_필수항목누락시_스킵한다() {
        when(cultureFestivalClient.fetchAll()).thenReturn(List.of(item(null, "2026-04-10", "2026-04-20", "서울")));

        int synced = festivalSyncService.syncAll();

        assertThat(synced).isEqualTo(0);
        verify(festivalRepository, never()).save(any());
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CultureFestivalItem item(String name, String startDate, String endDate, String roadAddress) {
        Map<String, String> raw = new HashMap<>();
        raw.put("축제명", name);
        raw.put("축제시작일자", startDate);
        raw.put("축제종료일자", endDate);
        raw.put("소재지도로명주소", roadAddress);
        return OBJECT_MAPPER.convertValue(raw, CultureFestivalItem.class);
    }
}
