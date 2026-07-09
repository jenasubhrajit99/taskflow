package com.taskflow.comment.repository;

import com.taskflow.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.task.id = :taskId AND c.parentComment IS NULL")
    Page<Comment> findTopLevelByTaskId(@Param("taskId") UUID taskId, Pageable pageable);

    Page<Comment> findByParentCommentId(UUID parentCommentId, Pageable pageable);

    long countByTaskId(UUID taskId);
}
