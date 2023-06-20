package com.ankit.teaboard.service;

import com.ankit.teaboard.entity.AuctionScheduler;
import com.ankit.teaboard.repository.AuctionSchedulerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Configuration
@EnableScheduling
@Service
public class AuctionSchedulerService implements SchedulingConfigurer {
    TaskScheduler taskScheduler;
    private ScheduledFuture<?> job1;
    private ScheduledFuture<?> job2;

    private ScheduledFuture<?> auctionBidJob;

    private Map<String,ScheduledFuture> scheduledTasksMap = new HashMap<>();

    @Autowired
    private AuctionService auctionService;
    @Autowired
    private AuctionSchedulerRepository auctionSchedulerRepository;

    @Value("scheduler_delay_time")
    private String parseschedulerDelayTimeStr;

    //public static ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());

    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("ThreadScheduler-");
        scheduler.initialize();
        return scheduler;
    }

    /*private void job1(TaskScheduler scheduler) {
        job1 = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " The Task1 executed at " + new Date());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                String cronExp = "0/5 * * * * ?";// Can be pulled from a db .
                return new CronTrigger(cronExp).nextExecutionTime(triggerContext);
            }
        });
    }

    private void job2(TaskScheduler scheduler){
        job2=scheduler.schedule(new Runnable(){
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+" The Task2 executed at "+ new Date());
            }
        }, new Trigger(){
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                String cronExp="0/1 * * * * ?";//Can be pulled from a db . This will run every minute
                return new CronTrigger(cronExp).nextExecutionTime(triggerContext);
            }
        });
    }

    public void auctionBidJob(TaskScheduler scheduler) {
        auctionBidJob = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " The Auction Bid Task executed at " + new Date());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                String cronExp = "0/5 * * * * ?";// Can be pulled from a db .
                return new CronTrigger(cronExp).nextExecutionTime(triggerContext);
            }
        });
    }*/

    public void createAuctionScheduler(AuctionScheduler auctionScheduler){
        Long auctionDetailId = auctionScheduler.getAuctionDetail().getAuctionDetailId();
        ScheduledFuture auctionBidScheduler = this.taskScheduler().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("Auction Bid Scheduler executed : "+auctionScheduler.getSchedulerName() +" AT "+ new Date());
                auctionService.saveBidPeriodically(auctionDetailId);
            }
        }, auctionScheduler.getScheduledTime(),5000l);
        ScheduledFuture auctionBidCancelScheduler = this.taskScheduler().schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("Cancel Scheduler executed : "+auctionScheduler.getSchedulerName() +" AT "+ new Date());
                cancelBidScheduler(auctionDetailId);
            }
        }, auctionScheduler.getEndTime());

        this.scheduledTasksMap.put("AUCTION_ID_"+auctionDetailId,auctionBidScheduler);
        this.scheduledTasksMap.put("AUCTION_ID_CANCEL"+auctionDetailId,auctionBidCancelScheduler);
    }

    public void cancelBidScheduler(Long auctionDetailId){
        ScheduledFuture taskToCancel = this.scheduledTasksMap.get("AUCTION_ID_"+auctionDetailId);
        taskToCancel.cancel(true);
        System.out.println("AUCTION_ID_"+auctionDetailId+" scheduler cancelled AT : " + new Date());
        List<AuctionScheduler> auctionSchedulers = auctionSchedulerRepository.getAuctionSchedulerByAuctionDetailId(auctionDetailId);
        AuctionScheduler auctionScheduler = auctionSchedulers.get(0);
        auctionScheduler.setIsActive(0);
        auctionScheduler.setCstatus(1);
        auctionSchedulerRepository.save(auctionScheduler);
    }
}
