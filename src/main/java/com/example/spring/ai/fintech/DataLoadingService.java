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

import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.pdf.layout.PageExtractedTextFormatter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 *
 * @author Christian Tzolov
 */
public class DataLoadingService implements InitializingBean {

	private Resource pdfResource;

	private VectorStore vectorStore;

	@Value("${spring.ai.openai.test.skip-loading}")
	private Boolean skipLoading = false;

	public DataLoadingService(Resource pdfResource, VectorStore vectorStore) {
		Assert.notNull(pdfResource, "PDF Resource must not be null.");
		Assert.notNull(vectorStore, "VectorStore must not be null.");

		this.pdfResource = pdfResource;
		this.vectorStore = vectorStore;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (this.skipLoading) {
			return;
		}

		PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
				this.pdfResource,
				PdfDocumentReaderConfig.builder()
						.withPageExtractedTextFormatter(PageExtractedTextFormatter.builder()
								.withNumberOfBottomTextLinesToDelete(3)
								.withNumberOfTopPagesToSkipBeforeDelete(1)
								// .withLeftAlignment(true)
								.build())
						.withPagesPerDocument(1)
						.build());

		var textSplitter = new TokenTextSplitter();

		this.vectorStore.accept(
				textSplitter.apply(
						pdfReader.get()));

		System.out.println("Exit loader");
	}

}
