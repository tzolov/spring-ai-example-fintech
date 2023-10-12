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

package com.example.spring.ai.fintech.config;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import com.example.spring.ai.fintech.DataLoadingService;
import com.example.spring.ai.fintech.FinancialQAService;
import com.theokanning.openai.OpenAiHttpException;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.LoggerFactory;

import org.springframework.ai.client.AiClient;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.retriever.VectorStoreRetriever;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.support.Args;

import org.slf4j.Logger;

/**
 *
 * @author Christian Tzolov
 */
@Configuration
@EnableRetry
public class Config {

	private static final Logger logger = LoggerFactory.getLogger(Config.class);

	@Value("file:src/main/resources/data/uber-10-k-2022.pdf")
	// @Value("file:src/main/resources/data/lyft-10-k.pdf")
	private Resource resource;

	@Bean
	public RetryListener retryListener() {
		return new RetryListener() {
			@Override
			public <T, E extends Throwable> void onError(RetryContext context,
					RetryCallback<T, E> callback,
					Throwable throwable) {

				var name = context.getAttribute(RetryContext.NAME);
				var args = context.getAttribute("ARGS");
				BackOffContext backOffContext = (BackOffContext) context.getAttribute("backOffContext");

				if (throwable instanceof OpenAiHttpException oahex) {
 					if ("context_length_exceeded".equals(oahex.code)) {
						logger.error(throwable.getMessage() +
								"\n - abort retries!" +
								"\n - name: " + name +
								"\n - arguments: " + toString(args));
						context.setExhaustedOnly();
						return;
					}
				}
				logger.warn(throwable.getMessage() +
						"\n - name: " + name +
						"\n - arguments: " + toString(args) +
						"\n - Retry#: " + context.getRetryCount());
			}

			private String toString(Object args) {
				if (args == null)
					return "";
				if (args instanceof Args) {
					return Stream.of(((Args) args).getArgs())
							.map(a -> a.toString())
							.collect(Collectors.joining(", "));
				}
				return args.toString();
			}
		};
	}

	@Bean
	public FinancialQAService financialQAService(AiClient aiClient, VectorStoreRetriever vectorStoreRetriever) {
		return new FinancialQAService(aiClient, vectorStoreRetriever);
	}

	@Bean
	public DataLoadingService DataLoadingService(VectorStore vectorStore) {
		return new DataLoadingService(this.resource, vectorStore);
	}

	@Bean
	public VectorStore vectorStore(EmbeddingClient embeddingClient, JdbcTemplate jdbcTemplate) {
		return new PgVectorStore(jdbcTemplate, embeddingClient);
	}

	@Bean
	public VectorStoreRetriever vectorStoreRetriever(VectorStore vectorStore) {
		return new VectorStoreRetriever(vectorStore, 5, 0.75);
	}

	@Bean
	public JdbcTemplate myJdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	@Primary
	@ConfigurationProperties("app.datasource")
	public DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	public HikariDataSource dataSource(DataSourceProperties dataSourceProperties) {
		return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

}
