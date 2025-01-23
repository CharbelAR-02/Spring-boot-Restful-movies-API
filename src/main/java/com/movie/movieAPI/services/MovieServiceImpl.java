package com.movie.movieAPI.services;

import com.movie.movieAPI.dto.MovieDto;
import com.movie.movieAPI.dto.MoviePageResponse;
import com.movie.movieAPI.entities.Movie;
import com.movie.movieAPI.exceptions.FileExistsException;
import com.movie.movieAPI.exceptions.MovieNotFoundException;
import com.movie.movieAPI.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MovieServiceImpl implements MovieService{

    private final MovieRepository movieRepository;
    private final FileService fileService;
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
            throw new FileExistsException("File name already exists! Please enter another file name.");
        }
        String uploadedFileName =  fileService.uploadFile(path,file);

        movieDto.setPoster(uploadedFileName);

        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        Movie savedMovie = movieRepository.save(movie);

        String posterUrl = baseUrl + "/file/" + uploadedFileName;

        MovieDto response = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
        return response;
    }



    @Override
    public MovieDto getMovie(Integer movieId) {
      Movie movie = movieRepository.findById(movieId).orElseThrow(() ->new MovieNotFoundException("movie not found with id : "+movieId));

        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public List<MovieDto> getAllMovies() {

      List<Movie> movies =  movieRepository.findAll();

      List<MovieDto> movieDtos = new ArrayList<>();

      for (Movie movie : movies){
          String posterUrl = baseUrl + "/file/" + movie.getPoster();

          MovieDto movieDto = new MovieDto(
                  movie.getMovieId(),
                  movie.getTitle(),
                  movie.getDirector(),
                  movie.getStudio(),
                  movie.getMovieCast(),
                  movie.getReleaseYear(),
                  movie.getPoster(),
                  posterUrl
          );
          movieDtos.add(movieDto);
      }
        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {

        Movie mv = movieRepository.findById(movieId).orElseThrow(() ->new MovieNotFoundException("movie not found with id : "+movieId));

        String filename = mv.getPoster();
        if (file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + filename));
            filename = fileService.uploadFile(path,file);
        }

        movieDto.setPoster(filename);

        Movie movie = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

       Movie updatedMovie = movieRepository.save(movie);

        String posterUrl = baseUrl + "/file/" + filename;

        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        logger.info("Attempting to delete movie with ID: {}", movieId);

        // Adding a small delay before deleting the file to avoid locking issues
        try {
            Thread.sleep(1000); // Wait for 500 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread was interrupted during sleep", e);
        }

        Movie mv = movieRepository.findById(movieId).orElseThrow(() ->new MovieNotFoundException("movie not found with id : "+movieId));
        Integer id = mv.getMovieId();

        String posterPath = path + File.separator + mv.getPoster();
        logger.info("Deleting poster at path: {}", posterPath);
        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));
        logger.info("Deleting movie record from the database");
        movieRepository.delete(mv);
        return "Movie deleted with id = " + id;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

        for (Movie movie : movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();

            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return new MoviePageResponse(movieDtos,pageNumber,pageSize,
                                     moviePages.getTotalElements(),
                                     moviePages.getTotalPages(),
                                     moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {

        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                                               : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

        for (Movie movie : movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();

            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return new MoviePageResponse(movieDtos,pageNumber,pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }
}
