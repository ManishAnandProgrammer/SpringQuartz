package com.example.dto.request;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
public class EmailScheduleRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;

    @NotNull
    private LocalDateTime dateTime;

    @NotNull
    private ZoneId timeZone;
}
