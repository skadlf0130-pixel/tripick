package com.tripick.festival.service;

import com.tripick.festival.client.RegionCodeMapper;
import com.tripick.festival.client.WeatherForecastClient;
import com.tripick.festival.dto.response.DailyWeatherResponse;
import com.tripick.festival.entity.Festival;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RegionCodeMapper regionCodeMapper;

    @Mock
    private WeatherForecastClient weatherForecastClient;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void 기간이_예보범위_밖이면_빈리스트를_반환한다() {
        Festival farFutureFestival = festival("서울특별시", LocalDate.now().plusMonths(6), LocalDate.now().plusMonths(6).plusDays(2));

        List<DailyWeatherResponse> forecast = weatherService.getForecast(farFutureFestival);

        assertThat(forecast).isEmpty();
        verifyNoInteractions(weatherForecastClient);
    }

    @Test
    void 지역코드매칭에실패하면_빈리스트를_반환한다() {
        Festival festival = festival("알수없는지역", LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
        when(regionCodeMapper.resolveRegId("알수없는지역")).thenReturn(null);

        List<DailyWeatherResponse> forecast = weatherService.getForecast(festival);

        assertThat(forecast).isEmpty();
    }

    @Test
    void 기간이_예보범위와_겹치면_날짜별_예보를_반환한다() {
        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = LocalDate.now().plusDays(4);
        Festival festival = festival("서울특별시", start, end);
        when(regionCodeMapper.resolveRegId("서울특별시")).thenReturn("11B10101");
        when(weatherForecastClient.fetchLandForecast("11B10101"))
                .thenReturn(List.of(Map.of("wf3Pm", "맑음", "wf3Am", "맑음", "rnSt3Pm", "20", "wf4Pm", "흐림", "rnSt4Pm", "30")));
        when(weatherForecastClient.fetchTemperatureForecast("11B10101"))
                .thenReturn(List.of(Map.of("taMin3", "10", "taMax3", "20", "taMin4", "11", "taMax4", "21")));

        List<DailyWeatherResponse> forecast = weatherService.getForecast(festival);

        assertThat(forecast).hasSize(2);
        assertThat(forecast.get(0).date()).isEqualTo(start);
        assertThat(forecast.get(0).weather()).isEqualTo("맑음");
        assertThat(forecast.get(0).minTemp()).isEqualTo(10);
        assertThat(forecast.get(0).maxTemp()).isEqualTo(20);
        assertThat(forecast.get(0).rainProbability()).isEqualTo(20);
    }

    @Test
    void 날씨API호출이_실패해도_예외를_던지지않고_빈리스트를_반환한다() {
        Festival festival = festival("서울특별시", LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
        when(regionCodeMapper.resolveRegId("서울특별시")).thenThrow(new RuntimeException("외부 API 오류"));

        List<DailyWeatherResponse> forecast = weatherService.getForecast(festival);

        assertThat(forecast).isEmpty();
    }

    private Festival festival(String region, LocalDate startDate, LocalDate endDate) {
        return Festival.builder()
                .apiId("test-" + region + startDate)
                .name("테스트 축제")
                .region(region)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
