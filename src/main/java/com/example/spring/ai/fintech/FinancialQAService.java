/*
 * Copyright 2023-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spring.ai.fintech;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.client.AiClient;
import org.springframework.ai.client.AiResponse;
import org.springframework.ai.client.Generation;
import org.springframework.ai.document.Document;
import org.springframework.ai.prompt.Prompt;
import org.springframework.ai.prompt.PromptTemplate;
import org.springframework.ai.prompt.SystemPromptTemplate;
import org.springframework.ai.prompt.messages.Message;
import org.springframework.ai.prompt.messages.UserMessage;
import org.springframework.ai.retriever.VectorStoreRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Assert;

/**
 * What is the revenue of Uber in 2022? Answer in millions, with page reference
 *
 * @author Christian Tzolov
 */
public class FinancialQAService {

	private final VectorStoreRetriever vectorStoreRetriever;

	@Value("file:src/main/resources/prompts/system-qa.st")
	private Resource systemBikePrompt;

	private AiClient aiClient;

	public FinancialQAService(AiClient aiClient, VectorStoreRetriever vectorStoreRetriever) {
		Assert.notNull(aiClient, "AiClient must not be null.");
		Assert.notNull(vectorStoreRetriever, "VectorStoreRetriever must not be null.");

		this.aiClient = aiClient;
		this.vectorStoreRetriever = vectorStoreRetriever;
	}

	@Retryable
	public Generation query(String question, String companyName) {

		// 1. Retrieve related (to the message) documents form the vector store.
		List<Document> similarDocuments = this.vectorStoreRetriever.retrieve(question);

		similarDocuments.stream().map(d -> d.getContent()).forEach(System.out::println);

		// 2. Embed documents into SystemMessage with the `system-qa.st` prompt template
		Message systemMessage = getSystemMessage(similarDocuments);

		PromptTemplate promptTemplate = new PromptTemplate(question);
		var materializedQuestion = promptTemplate.render(Map.of("company", companyName));

		// 3. Create an user message with the input question.
		UserMessage userMessage = new UserMessage(materializedQuestion);

		// 4. Ask the AI model
		Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
		AiResponse response = this.aiClient.generate(prompt);

		return response.getGeneration();
	}

	private Message getSystemMessage(List<Document> similarDocuments) {

		String documents = similarDocuments.stream().map(entry -> entry.getContent()).collect(Collectors.joining("\n"));
		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemBikePrompt);
		Message systemMessage = systemPromptTemplate.createMessage(Map.of("documents", documents));
		return systemMessage;

	}

}
