package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.feign.EventFeign;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {EventFeign.class})
public class CompilationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompilationApplication.class, args);
    }
}
