package com.tripick.festival.service;

import com.tripick.festival.client.CultureFestivalClient;
import com.tripick.festival.client.dto.CultureFestivalItem;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.List;

/**
 * 전국문화축제표준데이터에는 고유 식별자가 없어 (축제명+시작일+주소) 합성키 해시를 apiId로 사용해 upsert한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FestivalSyncService {

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyyMMdd")
    };

    private final CultureFestivalClient cultureFestivalClient;
    private final FestivalRepository festivalRepository;

    public int syncAll() {
        List<CultureFestivalItem> items = cultureFestivalClient.fetchAll();
        int synced = 0;
        for (CultureFestivalItem item : items) {
            if (upsert(item)) {
                synced++;
            }
        }
        log.info("전국문화축제표준데이터 동기화 완료 - 조회 {}건, 반영 {}건", items.size(), synced);
        return synced;
    }

    private boolean upsert(CultureFestivalItem item) {
        LocalDate startDate = parseDate(item.getStartDate());
        LocalDate endDate = parseDate(item.getEndDate());
        if (item.getFestivalName() == null || startDate == null || endDate == null) {
            log.warn("필수 항목 누락으로 스킵: {}", item.getFestivalName());
            return false;
        }

        String region = item.getAddress();
        String apiId = buildApiId(item.getFestivalName(), startDate, region);
        BigDecimal latitude = parseDecimal(item.getLatitude());
        BigDecimal longitude = parseDecimal(item.getLongitude());

        Festival festival = festivalRepository.findByApiId(apiId).orElse(null);
        if (festival == null) {
            festival = Festival.builder()
                    .apiId(apiId)
                    .name(item.getFestivalName())
                    .startDate(startDate)
                    .endDate(endDate)
                    .region(region)
                    .description(item.getContent())
                    .officialUrl(item.getHomepageUrl())
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
            festival.updateCacheTime();
            festivalRepository.save(festival);
        } else {
            festival.updateFromSync(item.getFestivalName(), startDate, endDate, region,
                    item.getContent(), item.getHomepageUrl(), latitude, longitude);
        }
        return true;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(raw.trim(), format);
            } catch (DateTimeParseException ignored) {
                // 다음 포맷 시도
            }
        }
        log.warn("날짜 파싱 실패: {}", raw);
        return null;
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildApiId(String name, LocalDate startDate, String region) {
        String raw = name + "|" + startDate + "|" + (region != null ? region : "");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 40);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
