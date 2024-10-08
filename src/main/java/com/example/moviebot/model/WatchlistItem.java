package com.example.moviebot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "watchlist")
public class WatchlistItem {
    @Id
    private String id;
    private String userId;
    private String movieId;
    private boolean watched;
}