package com.tripick.festival.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.festival.dto.request.TravelSpotCreateRequest;
import com.tripick.festival.entity.TravelSpot;
import com.tripick.festival.repository.TravelSpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelSpotServiceTest {

    @Mock
    private TravelSpotRepository travelSpotRepository;

    @InjectMocks
    private TravelSpotService travelSpotService;

    @Test
    void 여행지등록_성공() {
        TravelSpotCreateRequest request = new TravelSpotCreateRequest(
                "해운대 해수욕장", "부산광역시", "자연", "부산의 대표 해수욕장", null, null, null);
        when(travelSpotRepository.save(any(TravelSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TravelSpot spot = travelSpotService.create(request);

        assertThat(spot.getName()).isEqualTo("해운대 해수욕장");
        assertThat(spot.getRegion()).isEqualTo("부산광역시");
    }

    @Test
    void 존재하는여행지_조회_성공() {
        TravelSpot spot = TravelSpot.builder().name("경복궁").region("서울특별시").build();
        when(travelSpotRepository.findById(1L)).thenReturn(Optional.of(spot));

        TravelSpot result = travelSpotService.getTravelSpot(1L);

        assertThat(result.getName()).isEqualTo("경복궁");
    }

    @Test
    void 존재하지않는여행지_조회시_예외발생() {
        when(travelSpotRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelSpotService.getTravelSpot(999L))
                .isInstanceOf(TripickException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRAVEL_SPOT_NOT_FOUND);
    }
}
