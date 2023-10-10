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

import javax.sql.DataSource;

import com.example.spring.ai.fintech.DataLoadingService;
import com.example.spring.ai.fintech.FinancialQAService;
import com.zaxxer.hikari.HikariDataSource;

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
import org.springframework.retry.annotation.EnableRetry;

/**
 *
 * @author Christian Tzolov
 */
@Configuration
@EnableRetry
public class Config {

	@Value("file:src/main/resources/data/uber-10-k.pdf")
	// @Value("file:src/main/resources/data/lyft-10-k.pdf")
	private Resource resource;

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
