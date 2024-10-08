package com.example.moviebot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
public class Preference {
    private String genre;
    private String actor;
    private Integer releaseYear;
}
