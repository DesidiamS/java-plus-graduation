package ru.practictum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.feign.EventFeign;
import ru.practicum.feign.UserFeign;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {EventFeign.class, UserFeign.class})
public class RequestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestApplication.class, args);
    }
}
