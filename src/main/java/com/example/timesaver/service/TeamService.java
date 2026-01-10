package com.example.timesaver.service;

import com.example.timesaver.model.Applicant;
import com.example.timesaver.model.Project;
import com.example.timesaver.model.Team;
import com.example.timesaver.model.dto.team.TeamNrMembers;
import com.example.timesaver.repository.ApplicantRepository;
import com.example.timesaver.repository.ProjectRepository;
import com.example.timesaver.repository.TeamRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeamService {

    private final ApplicantRepository applicantRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;

    public TeamService(ApplicantRepository applicantRepository, TeamRepository teamRepository, ProjectRepository projectRepository) {
        this.applicantRepository = applicantRepository;
        this.teamRepository = teamRepository;
        this.projectRepository = projectRepository;
    }


    @Transactional
    public void createTeams(Long projectId){

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Project not found");
        }

        Project project = projectOpt.get();

        Integer minNr = project.getMinNrParticipants();
        Integer maxNr = project.getMaxNrParticipants();

        List<TeamNrMembers> incompleteTeams = teamRepository.incompleteTeamsByProject(projectId, minNr);
        System.out.println("incomplete teams: " + incompleteTeams);
        List<Applicant> singleApplicants = applicantRepository.getSingleApplicants(projectId);

        //join teams and remove the second joined team
        Collection<TeamNrMembers> teamsToRemove = this.joinTeams(incompleteTeams, maxNr);
        System.out.println("removed teams because joined: " + teamsToRemove);
        incompleteTeams.removeAll(teamsToRemove);


        Queue<TeamNrMembers> incompleteTeamsQueue = new LinkedList<>(incompleteTeams);
        Queue<Applicant> singleApplicQueue = new LinkedList<>(singleApplicants);

        //Queue for storing the single applicants whose cardinal number in the new team is higher that the minimum number of members in a team set by the organizer
        Queue<Applicant> applicantsOverMinNr = new LinkedList<>();

        Integer tempIterationNumber = 0;
        System.out.println("after while iteration: " + tempIterationNumber);
        System.out.println("incompleteTeamsQueue: " + incompleteTeamsQueue);
        while (!singleApplicQueue.isEmpty()){
            Applicant a = singleApplicQueue.remove();
            if (!incompleteTeamsQueue.isEmpty()) {
                TeamNrMembers currentIncTeam = incompleteTeamsQueue.remove();
                if(currentIncTeam.getNrOfMembers()<maxNr) {
                    a.setTeam(currentIncTeam.getTeam());
                    applicantRepository.save(a);
                    currentIncTeam.setNrOfMembers(currentIncTeam.getNrOfMembers() + 1);
                    if (currentIncTeam.getNrOfMembers()<maxNr) incompleteTeamsQueue.offer(currentIncTeam);
                }


                if (currentIncTeam.getNrOfMembers()>minNr) applicantsOverMinNr.add(a);
                tempIterationNumber++;
                System.out.println("after while iteration: " + tempIterationNumber);
                System.out.println("applicant" + a);
                System.out.println("incompleteTeamsQueue: " + incompleteTeamsQueue);
                System.out.println("applicantsOverMinNr: " + applicantsOverMinNr);
                System.out.println("singleApplicantsQueue: " + singleApplicQueue);

                continue;
            }



            //create new team and add it to the queue
            Team newTeam = new Team();

            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString();

            newTeam.setProject(project);
            newTeam.setTeamName(uuidAsString);
            teamRepository.save(newTeam);
            a.setTeam(newTeam);
            applicantRepository.save(a);

            TeamNrMembers newIncompleteTeam = new TeamNrMembers(newTeam, 1L);
            incompleteTeamsQueue.offer(newIncompleteTeam);

            System.out.println("After new team was created: Team: " + newTeam + "Applicant: " + a);
            tempIterationNumber++;
            System.out.println("after while iteration: " + tempIterationNumber);
            System.out.println("applicant" + a);
            System.out.println("incompleteTeamsQueue: " + incompleteTeamsQueue);
            System.out.println("applicantsOverMinNr: " + applicantsOverMinNr);
            System.out.println("singleApplicantsQueue: " + singleApplicQueue);
        }

        System.out.println("==================================================");
        System.out.println("Second while");
        System.out.println("====================================================");
        System.out.println("icompleteTeamsQueue: "+ incompleteTeamsQueue);
        System.out.println("applicantsOverMinNr: "+ applicantsOverMinNr);
        //add members to the remaining incomplete teams if there are single applicants in any teams whose cardinal number is bigger than the minimum number of required members in a team
        while(!incompleteTeamsQueue.isEmpty() && !applicantsOverMinNr.isEmpty()){
            TeamNrMembers incompleteTeam = incompleteTeamsQueue.remove();
            Applicant movableApplicant = applicantsOverMinNr.remove();
            movableApplicant.setTeam(incompleteTeam.getTeam());
            incompleteTeam.setNrOfMembers(incompleteTeam.getNrOfMembers()+1);
            applicantRepository.save(movableApplicant);

            if (incompleteTeam.getNrOfMembers()<minNr)incompleteTeamsQueue.add(incompleteTeam);
        }

    }


    @Transactional
    //checks if there are any teams with fewer members than the minimum for this project, and joins them
    Collection<TeamNrMembers> joinTeams(List<TeamNrMembers> incompleteTeams, Integer maxNr){
        //If a team has one member less than the maximum, it cannot be joined with another team, so remove
        incompleteTeams.removeIf(t -> t.getNrOfMembers()==(maxNr-1));
        HashMap<TeamNrMembers, TeamNrMembers> joinedTeams = new HashMap<>();

        for (int i = 0; i < incompleteTeams.size(); i++) {
            for (int j = i+1; j < incompleteTeams.size(); j++) {
                TeamNrMembers team1 = incompleteTeams.get(i);
                TeamNrMembers team2 = incompleteTeams.get(j);
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
