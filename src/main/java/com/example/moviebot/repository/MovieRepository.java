package com.example.moviebot.repository;

import com.example.moviebot.model.Movie;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;


public interface MovieRepository extends ReactiveMongoRepository<Movie, String> {
}