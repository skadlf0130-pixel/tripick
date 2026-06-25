package com.tripick.festival.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public Page<Festival> getFestivals(String region, Integer month, String category, Pageable pageable) {
        LocalDate monthStart = null;
        LocalDate monthEnd = null;
        if (month != null) {
            monthStart = LocalDate.of(LocalDate.now().getYear(), month, 1);
            monthEnd = monthStart.plusMonths(1).minusDays(1);
        }
        return festivalRepository.search(region, category, monthStart, monthEnd, pageable);
    }

    public Festival getFestival(Long id) {
        return festivalRepository.findById(id)
                .orElseThrow(() -> new TripickException(ErrorCode.FESTIVAL_NOT_FOUND));
    }
}
