package com.example.moviebot.service;

import com.example.moviebot.model.WatchlistItem;
import com.example.moviebot.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WatchlistService {
    @Autowired
    private WatchlistRepository watchlistRepository;

    public Mono<WatchlistItem> addToWatchlist(String userId, String movieId) {
        WatchlistItem item = new WatchlistItem();
        item.setUserId(userId);
        item.setMovieId(movieId);
        item.setWatched(false);
        return watchlistRepository.save(item);
    }

    public Flux<WatchlistItem> getWatchlist(String userId) {
        return watchlistRepository.findByUserId(userId);
    }

    public Mono<WatchlistItem> markAsWatched(String itemId) {
        return watchlistRepository.findById(itemId)
                .flatMap(item -> {
                    item.setWatched(true);
                    return watchlistRepository.save(item);
                });
    }

    public Mono<Void> removeFromWatchlist(String itemId) {
        return watchlistRepository.deleteById(itemId);
    }
}