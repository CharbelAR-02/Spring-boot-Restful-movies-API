package com.movie.movieAPI.exceptions;

public class FileExistsException extends RuntimeException{

    public FileExistsException(String message){
        super(message);
    }
}
