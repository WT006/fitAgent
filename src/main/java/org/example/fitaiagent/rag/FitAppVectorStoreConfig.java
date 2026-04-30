package org.example.fitaiagent.rag;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class FitAppVectorStoreConfig {
    @Resource
    private MyInternationalTextSplitter textSplitter;

//    @Resource
//    private MyKeywordMetadataEnricher keywordMetadataEnricher;

    @Bean
    public VectorStore fitAppVectorStore(DashScopeEmbeddingModel dashScopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
        List<Document> documents = textSplitter.splitChineseText();
        //List<Document> enrichedDocuments = keywordMetadataEnricher.enrichDocuments(documents);
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }

}