package com.example.moviebot.service;

import com.example.moviebot.model.Movie;
import com.example.moviebot.model.Preference;
import com.example.moviebot.model.User;
import com.example.moviebot.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserService userService;

    public Flux<Movie> findMovies(long chatId, String genre, String actor, Integer releaseYear, String keyword) {
        return userService.findOrCreateUser(chatId, null)
                .flatMapMany(user -> {
                    Preference userPreference = user.getPreferences().isEmpty() ? null : user.getPreferences().getFirst();

                    // First attempt: search with user preferences
                    Flux<Movie> result = searchMovies(
                            (genre == null || genre.isEmpty()) && userPreference != null ? userPreference.getGenre() : genre,
                            (actor == null || actor.isEmpty()) && userPreference != null ? userPreference.getActor() : actor,
                            (releaseYear == null) && userPreference != null ? userPreference.getReleaseYear() : releaseYear,
                            keyword
                    );

                    // If no results, search without user preferences
                    return result.hasElements().flatMapMany(hasElements -> {
                        if (hasElements) {
                            return result;
                        } else {
                            return searchMovies(genre, actor, releaseYear, keyword);
                        }
                    });
                });
    }

    private Flux<Movie> searchMovies(String genre, String actor, Integer releaseYear, String keyword) {
        return movieRepository.findAll()
                .filter(movie -> {
                    boolean matchesGenre = genre == null || genre.equalsIgnoreCase("skip") || movie.getGenre().equalsIgnoreCase(genre);
                    boolean matchesActor = actor == null || actor.equalsIgnoreCase("skip") || movie.getActors().stream().anyMatch(a -> a.equalsIgnoreCase(actor));
                    boolean matchesYear = releaseYear == null || "skip".equalsIgnoreCase(String.valueOf(releaseYear)) || movie.getReleaseYear().equals(releaseYear);
                    boolean matchesKeyword = keyword == null || keyword.isEmpty() || keyword.equalsIgnoreCase("skip") ||
                            movie.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                            movie.getDescription().toLowerCase().contains(keyword.toLowerCase());
                    return matchesGenre && matchesActor && matchesYear && matchesKeyword;
                });
    }

    public Mono<Movie> getMovieById(String id) {
        return movieRepository.findById(id);
    }

    public Mono<Movie> getRandomMovie() {
        return movieRepository.count()
                .flatMap(count -> {
                    long randomIndex = (long) (Math.random() * count);
                    return movieRepository.findAll().skip(randomIndex).next();
                });
    }
}