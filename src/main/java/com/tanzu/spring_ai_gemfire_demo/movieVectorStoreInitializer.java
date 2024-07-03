package com.tanzu.spring_ai_gemfire_demo;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.PgVectorStore;
//import org.springframework.ai.vectorstore.GemFireVectorStore;
//import org.springframework.ai.vectorstore.GemFireVectorStore.GemFireVectorStoreConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.core.env.Environment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

//@Profile("init")
@Configuration
class InitConfig {

    /*@Bean
    public GemFireVectorStoreConfig gemFireVectorStoreConfig() {
      return GemFireVectorStoreConfig.builder()
                                     .withHost("localhost")
                                     .withPort(7071)
                                     .withIndex("movies")//bug to be reported, builder not taking index into account
                                     .build();
    }
    @Bean
    public VectorStore vectorStore(GemFireVectorStoreConfig config, EmbeddingModel embeddingModel) {
    var  mystore = new GemFireVectorStore(config, embeddingModel); //index to be set after instantiation
    mystore.setIndexName("movies");
    return mystore;
}*/
}

@Component
@Profile("init")
public class movieVectorStoreInitializer {
    private final Logger logger = LoggerFactory.getLogger(movieVectorStoreInitializer.class);
    private final VectorStore movieVectorStore;
    private final Resource resource;

    @Autowired
    public movieVectorStoreInitializer(VectorStore movieVectorStore,
                                       @Value("classpath:static/imdb.json") Resource resource) {
        this.movieVectorStore = movieVectorStore;
        this.resource = resource;
    }

    @PostConstruct
    public void initializeVectors() throws IOException {
        
        logger.info("Building embeddings and pushing data into Gemfire ");
        DocumentReader reader = new JsonReader(resource);
        List<Document> documents = reader.get();
        movieVectorStore.add(documents);
    }
}