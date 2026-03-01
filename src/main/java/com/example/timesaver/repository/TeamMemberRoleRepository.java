package com.example.timesaver.repository;

import com.example.timesaver.model.TeamMemberRole;
import com.example.timesaver.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface TeamMemberRoleRepository extends JpaRepository<TeamMemberRole, Long> {
    void deleteByMemberId(Long memberId);

    @Query("select r.roleCode as code, count(r) as cnt from TeamMemberRole r where r.team = ?1 group by r.roleCode")
    List<Map<String, Object>> countByRole(Team team);
}