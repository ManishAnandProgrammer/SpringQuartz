package com.example.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class EmailScheduleResponse {
    private boolean success;
    private String jobId;
    private String jobGroup;
    private String message;
}
