### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.3.0/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.3.0/maven-plugin/reference/html/#build-image)
* [Thymeleaf](https://docs.spring.io/spring-boot/docs/3.3.0/reference/htmlsingle/index.html#web.servlet.spring-mvc.template-engines)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.3.0/reference/htmlsingle/index.html#web)
* [OpenAI](https://docs.spring.io/spring-ai/reference/api/clients/openai-chat.html)
* [Docker Compose Support](https://docs.spring.io/spring-boot/docs/3.3.0/reference/htmlsingle/index.html#features.docker-compose)

### Guides
The following guides illustrate how to use some features concretely:

* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)



1. Start the gemfire db:

    ```bash
    docker compose up
    ```

2. Create the movies index:

    ```bash
    curl -X POST http://localhost:7071/gemfire-vectordb/v1/indexes -H "Content-Type: application/json" -d
    '{
      "name": "movies"
    }'
    ```

3. Check the creation of the index (notice number of embeddings):

    ```bash
    curl http://localhost:7071/gemfire-vectordb/v1/indexes/movies
    {
       "name" : "movies",
       "fields" : [ ],
       "beam-width" : 100,
       "max-connections" : 16,
       "vector-similarity-function" : "COSINE",
       "buckets" : 1,
       "number-of-embeddings" : 0
    }% 
    ```
4. Start the app in init mode:

    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=init  
    ```

5. Check the index again (notice number of embeddings):
   
   ```bash
   curl http://localhost:7071/gemfire-vectordb/v1/indexes/movies
        {
        "name" : "movies",
        "fields" : [ ],
        "beam-width" : 100,
        "max-connections" : 16,
        "vector-similarity-function" : "COSINE",
        "buckets" : 1,
        "number-of-embeddings" : 250
        }%
    ```

