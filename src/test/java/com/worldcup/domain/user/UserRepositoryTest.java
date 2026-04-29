package com.worldcup.domain.user;

import com.worldcup.AbstractRepositoryTest;
import com.worldcup.TestDataFactory;
import com.worldcup.domain.player.Player;
import com.worldcup.domain.player.PlayerRepository;
import com.worldcup.domain.team.Team;
import com.worldcup.domain.team.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired UserRepository userRepo;
    @Autowired TeamRepository teamRepo;
    @Autowired PlayerRepository playerRepo;
    @PersistenceContext EntityManager em;

    private User savedUser(String suffix) {
        return userRepo.save(TestDataFactory.user(suffix).build());
    }

    // ---- findByEmail ----

    @Test
    void findByEmail_existingEmail_returnsUser() {
        User user = savedUser("email1");
        assertThat(userRepo.findByEmail(user.getEmail()))
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo(user.getUsername());
    }

    @Test
    void findByEmail_unknownEmail_returnsEmpty() {
        assertThat(userRepo.findByEmail("nobody@nowhere.com")).isEmpty();
    }

    // ---- existsBy ----

    @Test
    void existsByEmail_trueForSaved_falseForUnknown() {
        User user = savedUser("exists1");
        assertThat(userRepo.existsByEmail(user.getEmail())).isTrue();
        assertThat(userRepo.existsByEmail("ghost@test.com")).isFalse();
    }

    @Test
    void existsByUsername_trueForSaved_falseForUnknown() {
        User user = savedUser("exists2");
        assertThat(userRepo.existsByUsername(user.getUsername())).isTrue();
        assertThat(userRepo.existsByUsername("nobody")).isFalse();
    }

    // ---- updateLastLogin ----

    @Test
    void updateLastLogin_setsTimestamp() {
        User user = savedUser("login1");
        OffsetDateTime loginTime = OffsetDateTime.now().withNano(0);

        userRepo.updateLastLogin(user.getId(), loginTime);
        em.flush();
        em.clear();

        User updated = userRepo.findById(user.getId()).orElseThrow();
        assertThat(updated.getLastLoginAt()).isNotNull();
        assertThat(updated.getLastLoginAt().toEpochSecond())
                .isEqualTo(loginTime.toEpochSecond());
    }

    // ---- updateProfile ----

    @Test
    void updateProfile_changesDisplayNameAndAvatarUrl() {
        User user = savedUser("profile1");

        userRepo.updateProfile(user.getId(), "New Name", "https://cdn.example.com/avatar.png");
        em.flush();
        em.clear();

        User updated = userRepo.findById(user.getId()).orElseThrow();
        assertThat(updated.getDisplayName()).isEqualTo("New Name");
        assertThat(updated.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar.png");
    }

    @Test
    void updateProfile_nullAvatar_doesNotThrow() {
        User user = savedUser("profile2");
        userRepo.updateProfile(user.getId(), "Updated Name", null);
        em.flush();
        em.clear();

        User updated = userRepo.findById(user.getId()).orElseThrow();
        assertThat(updated.getDisplayName()).isEqualTo("Updated Name");
        assertThat(updated.getAvatarUrl()).isNull();
    }

    // ---- findByIdWithFavoriteTeams ----

    @Test
    void findByIdWithFavoriteTeams_loadsCollectionInOneQuery() {
        User user = savedUser("favteam1");
        Team team = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc().get(0);

        User loaded = userRepo.findByIdWithFavoriteTeams(user.getId()).orElseThrow();
        loaded.getFavoriteTeams().add(team);
        userRepo.save(loaded);
        em.flush();
        em.clear();

        User withFavs = userRepo.findByIdWithFavoriteTeams(user.getId()).orElseThrow();
        // If @EntityGraph is missing, this would throw LazyInitializationException
        assertThat(withFavs.getFavoriteTeams()).hasSize(1);
        assertThat(withFavs.getFavoriteTeams().iterator().next().getId()).isEqualTo(team.getId());
    }

    // ---- findByIdWithFavoritePlayers ----

    @Test
    void findByIdWithFavoritePlayers_loadsCollectionInOneQuery() {
        User user = savedUser("favplayer1");
        Team team = teamRepo.findAllByOrderByGroupIdAscFifaRankingAsc().get(0);
        Player player = playerRepo.findByTeamIdOrderByPositionAscShirtNumberAsc(
                team.getId(), PageRequest.of(0, 1)).getContent().get(0);

        User loaded = userRepo.findByIdWithFavoritePlayers(user.getId()).orElseThrow();
        loaded.getFavoritePlayers().add(player);
        userRepo.save(loaded);
        em.flush();
        em.clear();

        User withFavs = userRepo.findByIdWithFavoritePlayers(user.getId()).orElseThrow();
        assertThat(withFavs.getFavoritePlayers()).hasSize(1);
        assertThat(withFavs.getFavoritePlayers().iterator().next().getId()).isEqualTo(player.getId());
    }
}
