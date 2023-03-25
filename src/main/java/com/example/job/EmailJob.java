package com.example.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String email = jobDataMap.getString("email");
        String body = jobDataMap.getString("body");
        String subject = jobDataMap.getString("subject");

        log.info("Sending Email To:: {}, \nWith Body:: \n{} \n Subject Is:: {}", email, body, subject);
    }

}
