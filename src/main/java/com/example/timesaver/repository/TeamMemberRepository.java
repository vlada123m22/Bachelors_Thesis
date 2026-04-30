package com.example.timesaver.repository;

import com.example.timesaver.model.Team;
import com.example.timesaver.model.TeamMember;
import com.example.timesaver.model.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
//For backgrounds/roles in each team
public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {
    long countByTeam(Team team);
    Optional<TeamMember> findByTeamAndApplicant(Team team, Applicant applicant);
    List<TeamMember> findByTeam(Team team);

    @Query("select count(m) from TeamMember m where m.team = ?1")
    long countMembers(Team team);
}