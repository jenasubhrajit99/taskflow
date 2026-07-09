package com.taskflow.project.repository;

import com.taskflow.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Page<Project> findByWorkspaceId(UUID workspaceId, Pageable pageable);

    boolean existsByWorkspaceIdAndKey(UUID workspaceId, String key);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.user.email = :email")
    List<Project> findByMemberEmail(@Param("email") String email);
}
