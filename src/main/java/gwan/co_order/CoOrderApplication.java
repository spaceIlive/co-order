package gwan.co_order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoOrderApplication.class, args);
	}

}