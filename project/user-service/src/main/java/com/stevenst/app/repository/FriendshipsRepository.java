package com.stevenst.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stevenst.app.model.Friendships;
import com.stevenst.lib.model.User;

import jakarta.transaction.Transactional;

@Repository
public interface FriendshipsRepository extends JpaRepository<Friendships, Long> {
        @Query("SELECT COUNT(f) > 0 FROM Friendships f WHERE " +
                        "(f.user1 = :user1 AND f.user2 = :user2) OR " +
                        "(f.user1 = :user2 AND f.user2 = :user1)")
        boolean existsByUsers(@Param("user1") User user1, @Param("user2") User user2);

        @Modifying
        @Transactional
        @Query("DELETE FROM Friendships f WHERE " +
                        "(f.user1 = :user1 AND f.user2 = :user2) OR " +
                        "(f.user1 = :user2 AND f.user2 = :user1)")
        void deleteByUsers(User user1, User user2);

        @Query("SELECT f FROM Friendships f WHERE " +
                        "f.user1 = :user OR f.user2 = :user")
        List<Friendships> findAllByUser(@Param("user") User user);
}
