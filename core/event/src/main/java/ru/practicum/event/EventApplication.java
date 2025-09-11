package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.feign.RequestFeign;
import ru.practicum.feign.StatsFeign;
import ru.practicum.feign.UserFeign;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {StatsFeign.class, RequestFeign.class, UserFeign.class})
public class EventApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventApplication.class, args);
    }

}
