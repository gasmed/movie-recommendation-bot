package com.example.moviebot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
@Document(collection = "movies")
public class Movie {
    @Id
    private String id;
    private String title;
    private String genre;
    private List<String> actors;
    private Integer releaseYear;
    private String description;
}