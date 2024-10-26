package com.example.springchilli;

import com.example.Controlers.ChiliPeperApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringChilliApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringChilliApplication.class, args);
        ChiliPeperApplication.run();
    }

}
