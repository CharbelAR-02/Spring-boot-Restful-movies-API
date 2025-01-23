package com.movie.movieAPI.auth.repositories;

import com.movie.movieAPI.auth.entities.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Integer> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.refreshToken = :refreshToken")
    void deleteByRefreshToken(@Param("refreshToken") String refreshToken);
}
