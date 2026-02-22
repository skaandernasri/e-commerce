package tn.temporise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableAsync
public class AsyncConfig {
  @Bean(name = "mailExecutor")
  public Executor mailExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // I/O-bound → more threads than CPU cores
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(15);

    // Limit queue to avoid memory explosion
    executor.setQueueCapacity(100);

    // Helpful for logs & debugging
    executor.setThreadNamePrefix("mail-async-");

    // Backpressure: caller runs task when pool is full
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    executor.initialize();
    return executor;
  }

  @Bean(name = "metaPixelEventExecutor")
  public Executor metaPixelEventExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // I/O-bound → more threads than CPU cores
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(15);

    // Limit queue to avoid memory explosion
    executor.setQueueCapacity(100);

    // Helpful for logs & debugging
    executor.setThreadNamePrefix("mail-async-");

    // Backpressure: caller runs task when pool is full
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    executor.initialize();
    return executor;
  }
}
