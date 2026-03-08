package com.example.timesaver.repository;

import com.example.timesaver.model.TeamMemberBackground;
import com.example.timesaver.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface TeamMemberBackgroundRepository extends JpaRepository<TeamMemberBackground, Long> {
    void deleteByMemberId(Long memberId);

    @Query("select b.backgroundCode as code, count(b) as cnt from TeamMemberBackground b where b.team = ?1 group by b.backgroundCode")
    List<Map<String, Object>> countByBackground(Team team);
}