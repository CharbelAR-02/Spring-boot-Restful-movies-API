package com.movie.movieAPI.controllers;

import com.movie.movieAPI.auth.entities.User;
import com.movie.movieAPI.auth.repositories.UserRepository;
import com.movie.movieAPI.auth.services.JwtService;
import com.movie.movieAPI.services.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping("/file/")
public class FileController {

    private final FileService fileService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public FileController(FileService fileService, JwtService jwtService, UserRepository userRepository){
        this.fileService = fileService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Value("${project.poster}")
    private String path;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileHandler(@RequestPart MultipartFile file) throws IOException {
       String uploadedFileName = fileService.uploadFile(path,file);
       return ResponseEntity.ok("File uploaded : " + uploadedFileName);
    }

    @GetMapping("{fileName}")
    public void serviceFileHandler(@PathVariable String fileName, HttpServletResponse response) throws IOException {
       InputStream resourceFile = fileService.getResourceFile(path,fileName);
       response.setContentType(MediaType.IMAGE_PNG_VALUE);
        StreamUtils.copy(resourceFile,response.getOutputStream());
    }



}
