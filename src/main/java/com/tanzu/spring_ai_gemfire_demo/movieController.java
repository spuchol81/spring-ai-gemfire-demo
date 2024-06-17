package com.tanzu.spring_ai_gemfire_demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.tanzu.spring_ai_gemfire_demo.Movie;
import org.springframework.ai.openai.OpenAiChatModel;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class movieController {

  public static void main(String[] args) {
    SpringApplication.run(movieController.class, args);
  }

  @RestController
  @RequestMapping("/api")
  public static class ApiController {
    private final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private List<Movie> movies;
    private final VectorStore movieVectorStore;
    private final ChatClient chatClient;

    @Value("classpath:static/imdb.json")
    private Resource movieResource;

    @Value("classpath:prompts/moviesearchprompt.st")
	private Resource systemMoviePrompt;

    public ApiController(
        ResourceLoader resourceLoader,
        ObjectMapper objectMapper ,
        VectorStore movieVectorStore,
        ChatClient.Builder chatClientBuilder) {
      this.resourceLoader = resourceLoader;
      this.objectMapper = objectMapper;
      this.movies = loadMoviesFromJson(); // Load movies from JSON file
      this.movieVectorStore = movieVectorStore;
      this.chatClient = chatClientBuilder.build();

    }

    // Load movies from JSON file
    private List<Movie> loadMoviesFromJson() {
      try {
        Resource resource = resourceLoader.getResource("classpath:static/imdb.json");
        Movie[] movies = objectMapper.readValue(resource.getInputStream(), Movie[].class);
        return List.of(movies);
      } catch (IOException e) {
        e.printStackTrace();
        return new ArrayList<>();
      }
    }

    @GetMapping("/movies")
    public ResponseEntity<List<Movie>> getAllMovies() {
      return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    @GetMapping("/movies/search")
    public ResponseEntity<List<Movie>> getmatchingMovies(
        @RequestParam(value = "request", defaultValue = "All the movies") String request){
      //Ask vector db to retieve movie that are close to the user request 
      logger.info("Looking for vectors similar to your request");
      //SearchRequest query = SearchRequest.query(request);
      SearchRequest query = SearchRequest.query(request).withTopK(20);
      List<Document> similarMovies = movieVectorStore.similaritySearch(query);

      //parse the vector list to rebuild movie object on your own
      List<Movie> movList = new ArrayList<Movie>();
      List<String> matchingmovies =
          similarMovies.stream().map(Document::getContent).collect(Collectors.toList());
      for (String currentmovie : matchingmovies) {
        //logger.info(currentmovie);
        Movie curMovie = mapToMovie(currentmovie);
        movList.add(curMovie);
      }

      //use this list as context for a chat model and let him build the movie dataset
      /*String documents = similarMovies.stream().map(Document::getContent).collect(Collectors.toList()).toString();
      logger.info("Building dataset matching you request");
      List<Movie> movList = chatClient.prompt()
                 .system(p -> p.text(systemMoviePrompt).param("context", documents))
                 .user(request)
                 .call()
                 .entity(new ParameterizedTypeReference<List<Movie>>() {
              });*/
      return new ResponseEntity<>(movList, HttpStatus.OK);
    }

    @GetMapping("/movies/{imdbID}")
    public Optional<Movie> getMovieByImdbId(@PathVariable String imdbID) {
      return movies.stream().filter(movie -> movie.getImdbID().equals(imdbID)).findFirst();
    }

    @GetMapping("/movies/chat")
    public String chatAboutMovies(@RequestParam(value = "question", defaultValue = "what is the first Ranked movie in our collection? ") String question) {
      SearchRequest query = SearchRequest.query(question).withTopK(20);
      List<Document> similarMovies = movieVectorStore.similaritySearch(query);
      String documents = similarMovies.stream().map(Document::getContent).collect(Collectors.toList()).toString();
      return chatClient.prompt()
      .system(p -> p.text(systemMoviePrompt).param("context",documents))
      .user(question)
      .call()
      .content();
    }


    @GetMapping("/movies/{imdbID}/title")
    public String getMovieTitleImdbId(@PathVariable String imdbID) {
      return movies.stream()
          .filter(movie -> movie.getImdbID().equals(imdbID))
          .findFirst()
          .get()
          .getTitle();
    }

    @GetMapping("/movies/{imdbID}/plot")
    public String getMoviePlotImdbId(@PathVariable String imdbID) {
      return movies.stream()
          .filter(movie -> movie.getImdbID().equals(imdbID))
          .findFirst()
          .get()
          .getPlot();
    }
  }


  public static Movie mapToMovie(String movieString) {
    Map<String, String> movieMap = new HashMap<>();
    String[] pairs = movieString.substring(1, movieString.length() - 1).split(", ");

    for (String pair : pairs) {
        String[] keyValue = pair.split("=", 2); // Specify limit to avoid exception
        movieMap.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : ""); // Handle empty value if there's no "=" in the pair
    }

    Movie movie = new Movie();
    movie.setImdbID(movieMap.get("imdbID"));
    movie.setTitle(movieMap.get("title"));
    movie.setPlot(movieMap.get("plot"));
    movie.setPoster(movieMap.get("poster"));
    // Set other attributes as needed

    return movie;
}

  
}

