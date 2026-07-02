package dev.springai.simple_cli;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class SimpleCliApplication {
	static void main(String[] args) {
		SpringApplication.run(SimpleCliApplication.class, args);
	}

	@Bean
	public ApplicationRunner run(ChatClient.Builder builder) {
		return args -> {
			try(Scanner sc = new Scanner(System.in)) {
				System.out.println("Welcome to the Spring AI CLI! Type your question and press Enter.");

				ChatClient client = builder.build();

				String question = sc.nextLine();

				String response = client
						.prompt(question)
						.call()
						.content();

				System.out.println(response);
			}
		};
	}
}
