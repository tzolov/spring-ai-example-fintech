package com.example.spring.ai.fintech;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.client.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
	private FinancialQAService financialQAService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	List<String> questions = List.of(
			"What customer segments grew the fastest for {company} in 2022?");
	// List<String> questions = List.of(
	// 		"What is the revenue of {company} in 2022? Answer in millions, with page reference",
	// 		"What customer segments grew the fastest for {company} in 2022?",
	// 		"What geographies grew the fastest for {company} in 2022?",
	// 		"How {company} performed in the different geographies in 2022?",
	// 		"What was the COVID-19 impact on {company}'s business between 2021 and 2022?",
	// 		"What is the revenue growth of {company} from 2021 to 2022?");

	@Override
	public void run(String... args) throws Exception {

		List<String> results = new ArrayList<>();

		for (String question : questions) {
			for (String company : List.of("Uber")) {
			// for (String company : List.of("Uber", "Lyft")) {
				Generation response = this.financialQAService.query(question, company);
				System.out.println(">>> " + response.getText());
				results.add("> Q: " + question + "\n  A: " + response.getText());
			}
		}

		results.forEach(System.out::println);
	}

}
