package com.example.controller;

import com.example.dto.request.EmailScheduleRequest;
import com.example.dto.response.EmailScheduleResponse;
import com.example.job.EmailJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class EmailScheduleController {
    private final Scheduler scheduler;

    @PostMapping("/schedule-email")
    public ResponseEntity<EmailScheduleResponse> scheduleEmail(
            @Valid @RequestBody final EmailScheduleRequest emailScheduleRequest
    ) {
        try {
            ZonedDateTime triggerAt = ZonedDateTime.of(
                emailScheduleRequest.getDateTime(), emailScheduleRequest.getTimeZone()
            );
            if (isPastDateProvided(triggerAt)) {
                return scheduleFailedDueToPastDateResponse();
            }
            JobDetail jobDetail = buildJobDetail(emailScheduleRequest);
            Trigger trigger = buildTrigger(jobDetail, triggerAt);
            scheduler.scheduleJob(jobDetail, trigger);

            return scheduleSuccessResponse(jobDetail);
        } catch (Exception exception) {
            log.error("Exception In Scheduling Email:: ", exception);
            return scheduleFailedResponse();
        }
    }

    private static ResponseEntity<EmailScheduleResponse> scheduleSuccessResponse(final JobDetail jobDetail) {
        EmailScheduleResponse emailScheduleResponse = EmailScheduleResponse.builder()
                .success(true)
                .jobId(jobDetail.getKey().getName())
                .jobGroup(jobDetail.getKey().getGroup())
                .message("Email Scheduled Successfully.!")
                .build();
        return ResponseEntity.ok(emailScheduleResponse);
    }

    private static ResponseEntity<EmailScheduleResponse> scheduleFailedDueToPastDateResponse() {
        EmailScheduleResponse emailSchedulePastDateProvidedResponse = EmailScheduleResponse.builder()
                .success(false)
                .message("Email Can't Schedule In Past")
                .build();
        return ResponseEntity.badRequest().body(emailSchedulePastDateProvidedResponse);
    }

    private static ResponseEntity<EmailScheduleResponse> scheduleFailedResponse() {
        EmailScheduleResponse emailScheduleFailedResponse = EmailScheduleResponse.builder()
                .success(false)
                .message("Not Able To Schedule Email")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(emailScheduleFailedResponse);
    }

    private static boolean isPastDateProvided(final ZonedDateTime triggerAt) {
        return triggerAt.isBefore(ZonedDateTime.now());
    }

    private JobDetail buildJobDetail(final EmailScheduleRequest emailScheduleRequest) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", emailScheduleRequest.getEmail());
        jobDataMap.put("body", emailScheduleRequest.getBody());
        jobDataMap.put("subject", emailScheduleRequest.getSubject());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Email Sending Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(final JobDetail jobDetail, final ZonedDateTime triggerAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Email Sending Trigger")
                .startAt(Date.from(triggerAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
