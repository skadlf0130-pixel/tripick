package com.tripick.festival.scheduler;

import com.tripick.festival.service.FestivalSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FestivalSyncScheduler {

    private final FestivalSyncService festivalSyncService;

    @Scheduled(cron = "0 0 3 * * *")
    public void syncFestivals() {
        log.info("축제 데이터 정기 동기화 시작");
        festivalSyncService.syncAll();
    }
}
