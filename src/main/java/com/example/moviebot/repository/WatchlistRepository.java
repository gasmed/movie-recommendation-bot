package com.example.moviebot.repository;

import com.example.moviebot.model.WatchlistItem;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface WatchlistRepository extends ReactiveMongoRepository<WatchlistItem, String> {
    Flux<WatchlistItem> findByUserId(String userId);
}