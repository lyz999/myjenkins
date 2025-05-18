package cn.lyz.myjenkins;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyjenkinsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyjenkinsApplication.class, args);
    }

}
