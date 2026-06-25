package com.tripick.festival.client;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 축제 주소(시/도)를 기상청 중기예보 예보구역코드(regId)로 매핑.
 * 코드값은 기상청 "중기예보구역코드" 공식 자료 기준 - 실제 키로 호출해볼 때 한 번 더 검증 필요.
 * 시/도보다 더 세분화하지 않음(중기예보 자체가 광역 단위 예보라 더 쪼개는 게 의미 없음).
 */
@Component
public class RegionCodeMapper {

    private static final Map<String, String> REGION_TO_REG_ID = new LinkedHashMap<>();

    static {
        REGION_TO_REG_ID.put("서울", "11B10101");
        REGION_TO_REG_ID.put("인천", "11B10101");
        REGION_TO_REG_ID.put("경기", "11B10101");
        REGION_TO_REG_ID.put("강원도 영서", "11D10301");
        REGION_TO_REG_ID.put("강원도 영동", "11D20201");
        REGION_TO_REG_ID.put("강원", "11D10301");
        REGION_TO_REG_ID.put("충북", "11C10301");
        REGION_TO_REG_ID.put("충청북도", "11C10301");
        REGION_TO_REG_ID.put("충남", "11C20401");
        REGION_TO_REG_ID.put("충청남도", "11C20401");
        REGION_TO_REG_ID.put("대전", "11C20401");
        REGION_TO_REG_ID.put("세종", "11C20401");
        REGION_TO_REG_ID.put("전북", "11F10202");
        REGION_TO_REG_ID.put("전라북도", "11F10202");
        REGION_TO_REG_ID.put("전남", "11F20501");
        REGION_TO_REG_ID.put("전라남도", "11F20501");
        REGION_TO_REG_ID.put("광주", "11F20501");
        REGION_TO_REG_ID.put("경북", "11H10701");
        REGION_TO_REG_ID.put("경상북도", "11H10701");
        REGION_TO_REG_ID.put("대구", "11H10701");
        REGION_TO_REG_ID.put("경남", "11H20201");
        REGION_TO_REG_ID.put("경상남도", "11H20201");
        REGION_TO_REG_ID.put("부산", "11H20201");
        REGION_TO_REG_ID.put("울산", "11H20201");
        REGION_TO_REG_ID.put("제주", "11G00201");
    }

    /** 주소 문자열에 포함된 시/도 키워드를 찾아 regId를 반환, 매칭 실패 시 null. */
    public String resolveRegId(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        for (Map.Entry<String, String> entry : REGION_TO_REG_ID.entrySet()) {
            if (address.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
