package com.tripick.community.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportDecisionRequest {

    @NotNull(message = "처리 결과(accept/reject)를 선택해주세요")
    private Boolean accept;
}
