package com.github.motoryang.system.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestJob {

    @XxlJob("testJobHandler")
    public void demoJobHandler() throws Exception {
        log.info("testJobHandler start");
        XxlJobHelper.log("XXL-JOB, Hello World.");
    }

}
