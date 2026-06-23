package group1.com.MangaSystemAndManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MangaSystemAndManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(MangaSystemAndManagementApplication.class, args);
	}

}
