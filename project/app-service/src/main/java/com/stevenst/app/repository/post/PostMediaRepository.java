package com.stevenst.app.repository.post;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stevenst.app.model.PostMedia;

import jakarta.transaction.Transactional;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {
	@Query("SELECT pm.mediaName FROM PostMedia pm WHERE pm.post.id = :postId")
	List<String> findMediaNamesByPostId(@Param("postId") Long postId);

	@Transactional
	@Modifying
	void deleteAllByPostId(Long postId);
}
