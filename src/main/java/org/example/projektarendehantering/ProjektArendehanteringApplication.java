package org.example.projektarendehantering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration.class,
        io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration.class,
        io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration.class,
        io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration.class
})
public class ProjektArendehanteringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjektArendehanteringApplication.class, args);
    }

}
