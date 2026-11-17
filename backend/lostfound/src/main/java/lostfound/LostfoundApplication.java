package lostfound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LostfoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(LostfoundApplication.class, args);
    }
}