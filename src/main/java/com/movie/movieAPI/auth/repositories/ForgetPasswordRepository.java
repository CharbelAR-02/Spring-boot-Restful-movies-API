package com.movie.movieAPI.auth.repositories;

import com.movie.movieAPI.auth.entities.ForgotPassword;
import com.movie.movieAPI.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface ForgetPasswordRepository extends JpaRepository<ForgotPassword,Integer> {

    @Query("select fp from ForgotPassword fp where fp.otp = ?1 and fp.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, User user);
}
