package com.farmmarket.repository;

import com.farmmarket.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Page<CommunityPost> findAllByOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);

    Page<CommunityPost> findByCategory(CommunityPost.PostCategory category, Pageable pageable);

    List<CommunityPost> findByAuthorId(Long authorId);

    List<CommunityPost> findByIsPinnedTrue();
}
