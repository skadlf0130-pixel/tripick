package com.tripick.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {

    @NotBlank(message = "신고 이유를 입력해주세요")
    @Size(max = 500, message = "신고 이유는 최대 500자까지 입력 가능합니다")
    private String reason;
}
