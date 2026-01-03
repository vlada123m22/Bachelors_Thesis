package com.example.timesaver.service;

import com.example.timesaver.model.Applicant;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.Team;
import com.example.timesaver.model.dto.team.IncompleteTeam;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeamService {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProjectRepository projectRepository;

    //incomplete Teams: Map<teamId, nrOfMembers>
    //singleApplicants: applicantId
    //Map<Long,Long> incompleteTeams, List<Long> singleApplicants)

    public void createTeams(Long projectId){
        //TODO: Ensure that the last team does not have less than min nr of members
        Optional<Project> projectOpt = projectRepository.findByIdWithQuestions(projectId);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Project not found");
        }

        Project project = projectOpt.get();

        Integer minNr = project.getMinNrParticipants();
        Integer maxNr = project.getMaxNrParticipants();

        List<IncompleteTeam> incompleteTeams = teamRepository.incompleteTeamsByProject(projectId, minNr);
        List<Applicant> singleApplicants = applicantRepository.getSingleApplicants(projectId);

        //join teams and remove the second joined team
        Collection<IncompleteTeam> teamsToRemove = this.joinTeams(incompleteTeams, maxNr);
        incompleteTeams.removeAll(teamsToRemove);


        Queue<IncompleteTeam> incompleteTeamsQueue = new LinkedList<>(incompleteTeams);
        Queue<Applicant> singleApplicQueue = new LinkedList<>(singleApplicants);
        Map<Applicant,Team> singleApplicantTeam = new HashMap<>();

        while (!singleApplicQueue.isEmpty()){
            Applicant a = singleApplicQueue.remove();
            if (!incompleteTeamsQueue.isEmpty()) {
                IncompleteTeam currentIncTeam = incompleteTeamsQueue.remove();
                if(currentIncTeam.getNrOfMembers()<maxNr) {
                    a.setTeam(currentIncTeam.getTeam());
                    currentIncTeam.setNrOfMembers(currentIncTeam.getNrOfMembers() + 1);
                    incompleteTeamsQueue.offer(currentIncTeam);
                }

                continue;
            }

            //create new team and add it to the queue
            Team team = new Team();
            team.setProject(project);
            a.setTeam(team);
            teamRepository.save(team);
            applicantRepository.save(a);

            IncompleteTeam newIncompleteTeam = new IncompleteTeam(team, 1);
            incompleteTeamsQueue.offer(newIncompleteTeam);

        }


    }


    //After team creation, checks if there are any teams with fewer members than the minimum of this project, and
    Collection<IncompleteTeam> joinTeams(List<IncompleteTeam> incompleteTeams, Integer maxNr){
        //If a team has one member less than the maximum, it cannot be joined with another team, so remove
        incompleteTeams.removeIf(t -> t.getNrOfMembers()==(maxNr-1));
        HashMap<IncompleteTeam, IncompleteTeam> joinedTeams = new HashMap<>();

        for (int i = 0; i < incompleteTeams.size(); i++) {
            for (int j = i+1; j < incompleteTeams.size(); j++) {
                IncompleteTeam team1 = incompleteTeams.get(i);
                IncompleteTeam team2 = incompleteTeams.get(j);
                if((team1.getNrOfMembers()+ team2.getNrOfMembers())>maxNr){
                    continue;
                }
                joinedTeams.put(team1,team2);
                teamRepository.joinTeams(incompleteTeams.get(i).getTeam(), incompleteTeams.get(j).getTeam());
                teamRepository.delete(team2.getTeam());
            }
        }

        return joinedTeams.values();
    }


}
