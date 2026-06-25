package com.tripick.festival.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.festival.dto.request.TravelSpotCreateRequest;
import com.tripick.festival.entity.TravelSpot;
import com.tripick.festival.repository.TravelSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelSpotService {

    private final TravelSpotRepository travelSpotRepository;

    public Page<TravelSpot> getTravelSpots(String region, String category, Pageable pageable) {
        return travelSpotRepository.search(region, category, pageable);
    }

    public TravelSpot getTravelSpot(Long id) {
        return travelSpotRepository.findById(id)
                .orElseThrow(() -> new TripickException(ErrorCode.TRAVEL_SPOT_NOT_FOUND));
    }

    @Transactional
    public TravelSpot create(TravelSpotCreateRequest request) {
        return travelSpotRepository.save(TravelSpot.builder()
                .name(request.getName())
                .region(request.getRegion())
                .category(request.getCategory())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build());
    }
}
