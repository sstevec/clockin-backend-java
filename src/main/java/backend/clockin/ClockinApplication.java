package backend.clockin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {"backend.clockin.*","com.xunsiya.*"})
@MapperScan(basePackages = "backend.clockin.mapper")
public class ClockinApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClockinApplication.class, args);
    }

}
