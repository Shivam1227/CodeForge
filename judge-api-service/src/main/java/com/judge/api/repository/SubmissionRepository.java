package com.judge.api.repository;

import com.judge.api.entity.Submission;
import com.judge.api.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // Capped at 100 — a profile page should never risk loading a user's entire
    // submission history if they've made thousands over time.
    List<Submission> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(DISTINCT s.problemId) FROM Submission s WHERE s.userId = :userId AND s.status = :status")
    long countDistinctSolvedProblems(@Param("userId") Long userId, @Param("status") SubmissionStatus status);

    // Native query — DATE(created_at) grouping is MySQL-specific, not portable JPQL
    @Query(value = "SELECT DATE(created_at) AS day, COUNT(*) AS cnt " +
            "FROM submissions WHERE user_id = :userId GROUP BY DATE(created_at) ORDER BY day",
            nativeQuery = true)
    List<Object[]> heatmapRawData(@Param("userId") Long userId);

    long countByUserIdAndStatus(Long userId, SubmissionStatus status);

    @Query("SELECT DISTINCT s.problemId FROM Submission s WHERE s.userId = :userId AND s.status = :status")
    List<Long> findDistinctSolvedProblemIds(@Param("userId") Long userId, @Param("status") SubmissionStatus status);
}