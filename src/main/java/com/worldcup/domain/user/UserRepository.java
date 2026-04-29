package com.worldcup.domain.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"favoriteTeams", "favoriteTeams.country", "favoriteTeams.country.confederation"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithFavoriteTeams(Long id);

    @EntityGraph(attributePaths = {"favoritePlayers", "favoritePlayers.team", "favoritePlayers.team.country"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithFavoritePlayers(Long id);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateLastLogin(Long id, OffsetDateTime lastLoginAt);

    @Modifying
    @Query("UPDATE User u SET u.displayName = :displayName, u.avatarUrl = :avatarUrl, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateProfile(Long id, String displayName, String avatarUrl);

    @Modifying
    @Query("UPDATE User u SET u.isVerified = true, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void markVerified(Long id);

    @Modifying
    @Query("UPDATE User u SET u.role = :role, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateRole(Long id, String role);

    @Modifying
    @Query("UPDATE User u SET u.isActive = :active, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateActive(Long id, Boolean active);
}
