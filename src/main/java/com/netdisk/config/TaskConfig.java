package com.netdisk.config;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.netdisk.task.DeleteTmp;

/**
 * @author riemann
 * @date 2019/06/23 18:21
 */
@Configuration
public class TaskConfig {

    // 定义要执行的EmailTask任务类
    @Bean
    public JobDetail delete() {
        return JobBuilder.newJob(DeleteTmp.class).withIdentity("delete").storeDurably().build();
    }

    // Simple触发器定义与设置
    @Bean
    public SimpleTrigger deleteTmpTrigger() {
        // Simple类型：可设置时间间隔、是否重复、触发频率（misfire机制）等
        // 这里我设置的10s的定时任务
        SimpleScheduleBuilder ssb = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(12).repeatForever();

        // 一个Trigger只对应一个Job，Schedule调度器调度Trigger执行对应的Job
        SimpleTrigger sTrigger = TriggerBuilder.newTrigger().forJob(delete()).
                withIdentity("delete").withDescription("simple类型的触发器").withSchedule(ssb).build();
        return sTrigger;

    }
}
