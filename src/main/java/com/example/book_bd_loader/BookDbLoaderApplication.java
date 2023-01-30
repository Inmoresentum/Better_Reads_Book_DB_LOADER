package com.example.book_bd_loader;

import com.example.book_bd_loader.DataStackAstraProperties.DataStaxAstraProperties;
import com.example.book_bd_loader.Entity.Author;
import com.example.book_bd_loader.Entity.AuthorRepository;
import com.example.book_bd_loader.Entity.Book;
import com.example.book_bd_loader.Entity.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BookDbLoaderApplication {

    final AuthorRepository authorRepository;
    final BookRepository bookRepository;

    public BookDbLoaderApplication(@Lazy AuthorRepository authorRepository, @Lazy BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(BookDbLoaderApplication.class, args);
    }

    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
        Path bundle = astraProperties.getSecureConnectBundle().toPath();
        return builder -> builder.withCloudSecureConnectBundle(bundle);
    }

    @Value("${datadump.location.authors}")
    private String authorDataDumpLocation;

    @Value("${datadump.location.works}")
    private String worksDataDumpLocation;


    private void initializeAuthors() throws IOException, JSONException {
        BufferedReader br = new BufferedReader(new InputStreamReader
                // Change the according to your system.
                (new FileInputStream("/home/denuvo-drm/Downloads/BOOKS_DB/minimal_authors.txt")));
        ArrayList<Author> aThousandAuthors = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (aThousandAuthors.size() < 1001) {
                JSONObject jsonObject = new JSONObject(line.substring(line.indexOf("{")));
                Author author = new Author();
                author.setId(jsonObject.optString("key").replace("/authors/", ""));
                author.setPersonalName(jsonObject.optString("personal_name"));
                author.setName(jsonObject.optString("name"));
                aThousandAuthors.add(author);
            } else {
                authorRepository.saveAll(aThousandAuthors);
                aThousandAuthors.clear();
            }
        }

        if (!aThousandAuthors.isEmpty()) {
            authorRepository.saveAll(aThousandAuthors);
        }
    }

    private void initializeWorks() throws IOException, JSONException {
        var dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        BufferedReader br = new BufferedReader(new InputStreamReader
                (new FileInputStream("/home/denuvo-drm/Downloads/BOOKS_DB/minimal_works.txt")));
        String line;
        while ((line = br.readLine()) != null) {
            JSONObject jsonObject = new JSONObject(line.substring(line.indexOf("{")));
            var book = new Book();
            book.setName(jsonObject.optString("title"));
            var descriptionObject = jsonObject.optJSONObject("description");
            if (descriptionObject != null) {
                book.setDescription(descriptionObject.optString("value"));
            }

            var publishedObj = jsonObject.optJSONObject("created");
            if (publishedObj != null) {
                String dateString = publishedObj.getString("value");
                book.setPublishedDate(LocalDate.parse(dateString, dateFormat));
            }
            var coversJSONArray = jsonObject.optJSONArray("covers");
            if (coversJSONArray != null) {
                List<String> coverIds = new ArrayList<>();
                for (int i = 0; i < coversJSONArray.length(); i++) {
                    coverIds.add(coversJSONArray.getString(i));
                }
                book.setCoverIds(coverIds);
            }
            var authorsJSONArray = jsonObject.optJSONArray("authors");
            if (authorsJSONArray != null) {
                List<String> authorIds = new ArrayList<>();
                for (int i = 0; i < authorsJSONArray.length(); i++) {
                    String authorId = authorsJSONArray.getJSONObject(i).getJSONObject("author")
                            .getString("key").replace("/authors/", "");
                    authorIds.add(authorId);
                }
                book.setAuthorId(authorIds);
                List<String> authorNames = authorIds.stream().map(authorRepository::findById)
                        .map(optionalAuthor -> {
                            if (optionalAuthor.isEmpty()) return "Unknown Author";
                            return optionalAuthor.get().getName();
                        }).collect(Collectors.toList());
                book.setAuthorNames(authorNames);
            }
            book.setId(jsonObject.getString("key").replace("/works/", ""));
            System.out.println(book);
            bookRepository.save(book);
        }
    }


    @PostConstruct
    public void start() {
        System.out.println(worksDataDumpLocation);
        try {
//            initializeAuthors();
            initializeWorks();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
        System.out.println(authorDataDumpLocation);
    }
}
