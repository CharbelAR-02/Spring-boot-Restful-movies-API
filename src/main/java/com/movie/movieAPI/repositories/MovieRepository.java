package com.movie.movieAPI.repositories;

import com.movie.movieAPI.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie,Integer> {
}
