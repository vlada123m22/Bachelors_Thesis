package com.example.timesaver.service;

import com.example.timesaver.model.*;
import com.example.timesaver.model.TeamApplication.Status;
import com.example.timesaver.model.dto.teamflow.CreateTeamRequest;
import com.example.timesaver.model.dto.teamflow.DecisionRequest;
import com.example.timesaver.model.dto.teamflow.TeamApplicationDTO;
import com.example.timesaver.model.dto.teamflow.TeamListingDTO;
import com.example.timesaver.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamApplicationService {
    private final ProjectRepository projectRepository;
    private final ApplicantRepository applicantRepository;
    private final TeamRepository teamRepository;
    private final TeamApplicationRepository teamApplicationRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRoleRequirementRepository roleReqRepo;
    private final TeamBackgroundRequirementRepository bgReqRepo;
    private final TeamMemberRoleRepository memberRoleRepo;
    private final TeamMemberBackgroundRepository memberBgRepo;

    public TeamApplicationService(ProjectRepository projectRepository,
                                  ApplicantRepository applicantRepository,
                                  TeamRepository teamRepository,
                                  TeamApplicationRepository teamApplicationRepository,
                                  TeamMemberRepository teamMemberRepository,
                                  TeamRoleRequirementRepository roleReqRepo,
                                  TeamBackgroundRequirementRepository bgReqRepo,
                                  TeamMemberRoleRepository memberRoleRepo,
                                  TeamMemberBackgroundRepository memberBgRepo) {
        this.projectRepository = projectRepository;
        this.applicantRepository = applicantRepository;
        this.teamRepository = teamRepository;
        this.teamApplicationRepository = teamApplicationRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.roleReqRepo = roleReqRepo;
        this.bgReqRepo = bgReqRepo;
        this.memberRoleRepo = memberRoleRepo;
        this.memberBgRepo = memberBgRepo;
    }

    private int currentTeamSizeIncludingLead(Team team) {
        long members = teamMemberRepository.countByTeam(team);
        // include lead if set
        return (team.getLead() != null ? 1 : 0) + (int) members;
    }

    private int maxTeamSize(Project p) {
        return Optional.ofNullable(p.getMaxNrParticipants()).orElse(0);
    }

    private void require(boolean cond, String msg) {
        if (!cond) throw new IllegalStateException(msg);
    }

    private void validateCodesSubset(List<String> requested, List<String> allowed, String label) {
        if (requested == null) return;
        Set<String> allowedSet = new HashSet<>(allowed.stream().map(String::toLowerCase).toList());
        for (String r : requested) {
            if (!allowedSet.contains(r.toLowerCase())) {
                throw new IllegalArgumentException(label + " contains value not allowed by project catalog: " + r);
            }
        }
    }

    @Transactional
    public Team createTeam(Long projectId, Long leadApplicantId, CreateTeamRequest req) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));
        require(Boolean.FALSE.equals(p.getTeamsPreformed()), "Teams are preformed for this project");

        Applicant lead = applicantRepository.findById(leadApplicantId)
                .orElseThrow(() -> new NoSuchElementException("Applicant not found"));
        require(Objects.equals(lead.getProject().getProjectId(), projectId), "Applicant not in this project");
        // Optional: check applicant is accepted in project (depends on your Applicant status model)

        // Ensure lead is not already a lead or member of any team in the same project
        List<Team> projectTeams = teamRepository.findAllTeamsByProject(projectId);
        for (Team t : projectTeams) {
            if (t.getLead() != null && Objects.equals(t.getLead().getApplicantId(), leadApplicantId)) {
                throw new IllegalStateException("Already a team lead in this project");
            }
            if (teamMemberRepository.findByTeamAndApplicant(t, lead).isPresent()) {
                throw new IllegalStateException("Already a member of a team in this project");
            }
        }

        // Validate roles/backgrounds against project catalogs (pipe-lists)
        List<String> roleCatalog = PipeList.split(p.getRolesOptions());
        List<String> bgCatalog = PipeList.split(p.getBackgroundOptions());
        if (req.roles() != null) {
            validateCodesSubset(req.roles().stream().map(CreateTeamRequest.RoleReq::code).toList(), roleCatalog, "roles");
        }
        if (req.backgrounds() != null) {
            validateCodesSubset(req.backgrounds().stream().map(CreateTeamRequest.BackgroundReq::code).toList(), bgCatalog, "backgrounds");
        }

        Team team = new Team();
        team.setProject(p);
        team.setTeamName(UUID.randomUUID().toString()); // or supply a name input if you want
        team.setLead(lead);
        team.setIdeaTitle(req.ideaTitle());
        team.setIdeaDescription(req.ideaDescription());
        Team saved = teamRepository.save(team);

        if (req.roles() != null) {
            for (CreateTeamRequest.RoleReq r : req.roles()) {
                TeamRoleRequirement tr = new TeamRoleRequirement();
                tr.setTeam(saved);
                tr.setRoleCode(r.code());
                tr.setMinNeeded(Optional.ofNullable(r.min()).orElse(0));
                tr.setMaxNeeded(Optional.ofNullable(r.max()).orElse(0));
                roleReqRepo.save(tr);
            }
        }

        if (req.backgrounds() != null) {
            for (CreateTeamRequest.BackgroundReq b : req.backgrounds()) {
                TeamBackgroundRequirement tb = new TeamBackgroundRequirement();
                tb.setTeam(saved);
                tb.setBackgroundCode(b.code());
                tb.setMinNeeded(Optional.ofNullable(b.min()).orElse(0));
                tb.setMaxNeeded(Optional.ofNullable(b.max()).orElse(0));
                bgReqRepo.save(tb);
            }
        }

        return saved;
    }

    @Transactional
    public void applyToTeam(Long teamId, Long applicantId) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        Project p = team.getProject();
        require(Boolean.FALSE.equals(p.getTeamsPreformed()), "Teams are preformed for this project");

        Applicant applicant = applicantRepository.findById(applicantId).orElseThrow();
        require(Objects.equals(applicant.getProject().getProjectId(), p.getProjectId()), "Not in this project");
        require(team.getLead() == null || !Objects.equals(team.getLead().getApplicantId(), applicantId), "Lead cannot apply to team");

        // Ensure not a lead or already a member of any team in this project
        for (Team t : teamRepository.findAllTeamsByProject(p.getProjectId())) {
            if (t.getLead() != null && Objects.equals(t.getLead().getApplicantId(), applicantId)) {
                throw new IllegalStateException("Team leads cannot apply to join other teams in this project");
            }
            if (teamMemberRepository.findByTeamAndApplicant(t, applicant).isPresent()) {
                throw new IllegalStateException("Already a member of a team in this project");
            }
        }

        teamApplicationRepository.findByTeamAndApplicant(team, applicant)
                .ifPresent(a -> {
                    throw new IllegalStateException("Application already exists");
                });

        TeamApplication app = new TeamApplication();
        app.setTeam(team);
        app.setApplicant(applicant);
        app.setStatus(Status.PENDING);
        teamApplicationRepository.save(app);
    }

    @Transactional
    public void decideApplication(Long teamId, Long appId, Long actingLeadApplicantId, DecisionRequest req) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        require(team.getLead() != null && Objects.equals(team.getLead().getApplicantId(), actingLeadApplicantId),
                "Only the team lead can decide applications");

        TeamApplication app = teamApplicationRepository.findById(appId)
                .orElseThrow(() -> new NoSuchElementException("Application not found with id: " + appId));
        require(Objects.equals(app.getTeam().getTeamId(), teamId), "Application does not belong to team");
        require(app.getStatus() == Status.PENDING, "Application already decided");

        if ("REJECT".equalsIgnoreCase(req.decision())) {
            app.setStatus(Status.REJECTED);
            app.setDecisionAt(java.time.ZonedDateTime.now());
            teamApplicationRepository.save(app);
            return;
        }

        // Accept path
        Project p = team.getProject();
        int curSize = currentTeamSizeIncludingLead(team);
        int max = maxTeamSize(p);
        require(curSize + 1 <= max, "Team is full");

        // Validate assignments against requirements (max caps)
        List<TeamRoleRequirement> reqRoles = roleReqRepo.findByTeam(team);
        List<TeamBackgroundRequirement> reqBgs = bgReqRepo.findByTeam(team);

        Map<String, Integer> assignedRoleCounts = memberRoleRepo.countByRole(team).stream()
                .collect(Collectors.toMap(m -> (String) m.get("code"), m -> ((Number) m.get("cnt")).intValue()));
        Map<String, Integer> assignedBgCounts = memberBgRepo.countByBackground(team).stream()
                .collect(Collectors.toMap(m -> (String) m.get("code"), m -> ((Number) m.get("cnt")).intValue()));

        Set<String> assignRoles = new HashSet<>(Optional.ofNullable(req.assignRoles()).orElse(List.of()));
        Set<String> assignBgs = new HashSet<>(Optional.ofNullable(req.assignBackgrounds()).orElse(List.of()));

        // Check roles
        for (String r : assignRoles) {
            TeamRoleRequirement rr = reqRoles.stream().filter(x -> x.getRoleCode().equalsIgnoreCase(r)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Role not allowed for this team: " + r));
            int used = assignedRoleCounts.getOrDefault(rr.getRoleCode(), 0);
            require(used + 1 <= rr.getMaxNeeded(), "Role capacity exceeded: " + rr.getRoleCode());
        }
        // Check backgrounds
        for (String b : assignBgs) {
            TeamBackgroundRequirement rb = reqBgs.stream().filter(x -> x.getBackgroundCode().equalsIgnoreCase(b)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Background not allowed for this team: " + b));
            int used = assignedBgCounts.getOrDefault(rb.getBackgroundCode(), 0);
            require(used + 1 <= rb.getMaxNeeded(), "Background capacity exceeded: " + rb.getBackgroundCode());
        }

        // Insert member and assignments
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setApplicant(app.getApplicant());
        member = teamMemberRepository.save(member);

        for (String r : assignRoles) {
            TeamMemberRole mr = new TeamMemberRole();
            mr.setTeam(team);
            mr.setMember(member);
            mr.setRoleCode(r);
            memberRoleRepo.save(mr);
        }
        for (String b : assignBgs) {
            TeamMemberBackground mb = new TeamMemberBackground();
            mb.setTeam(team);
            mb.setMember(member);
            mb.setBackgroundCode(b);
            memberBgRepo.save(mb);
        }

        app.setStatus(Status.ACCEPTED);
        app.setDecisionAt(java.time.ZonedDateTime.now());
        teamApplicationRepository.save(app);
    }

    @Transactional
    public void leaveTeam(Long teamId, Long memberId, Long actingApplicantId) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        TeamMember member = teamMemberRepository.findById(memberId).orElseThrow();
        require(Objects.equals(member.getTeam().getTeamId(), teamId), "Member not in this team");
        require(Objects.equals(member.getApplicant().getApplicantId(), actingApplicantId), "Only the member can leave");

        memberRoleRepo.deleteByMemberId(memberId);
        memberBgRepo.deleteByMemberId(memberId);
        teamMemberRepository.delete(member);
    }


    @Transactional
    public void removeMember(Long teamId, Long memberId, Long actingLeadApplicantId) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        require(team.getLead() != null && Objects.equals(team.getLead().getApplicantId(), actingLeadApplicantId),
                "Only lead can remove members");
        TeamMember member = teamMemberRepository.findById(memberId).orElseThrow();
        require(Objects.equals(member.getTeam().getTeamId(), teamId), "Member not in this team");

        memberRoleRepo.deleteByMemberId(memberId);
        memberBgRepo.deleteByMemberId(memberId);
        teamMemberRepository.delete(member);
    }

    @Transactional
    public List<TeamApplicationDTO> getTeamApplications(Long teamId, Long actingLeadApplicantId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NoSuchElementException("Team not found"));
        require(team.getLead() != null && Objects.equals(team.getLead().getApplicantId(), actingLeadApplicantId),
                "Only lead can view applications");

        return teamApplicationRepository.findByTeam(team).stream()
                .map(app -> new TeamApplicationDTO(
                        app.getId(),
                        app.getTeam().getTeamId(),
                        app.getApplicant().getApplicantId(),
                        app.getApplicant().getFirstName(),
                        app.getApplicant().getLastName(),
                        app.getStatus(),
                        app.getAppliedAt(),
                        app.getDecisionAt()
                )).collect(Collectors.toList());
    }

    @Transactional
    public List<TeamListingDTO> listTeams(Long projectId) {
        Project p = projectRepository.findById(projectId).orElseThrow();
        List<Team> teams = teamRepository.findAllTeamsByProject(projectId);
        int max = maxTeamSize(p);

        return teams.stream().map(t -> {
            int size = currentTeamSizeIncludingLead(t);
            int spots = Math.max(0, max - size);

            Map<String,Integer> assignedRoleCounts = memberRoleRepo.countByRole(t).stream()
                    .collect(Collectors.toMap(m -> (String) m.get("code"), m -> ((Number)m.get("cnt")).intValue()));
            Map<String,Integer> assignedBgCounts = memberBgRepo.countByBackground(t).stream()
                    .collect(Collectors.toMap(m -> (String) m.get("code"), m -> ((Number)m.get("cnt")).intValue()));

            var roleReqs = roleReqRepo.findByTeam(t).stream().map(r -> {
                int assigned = assignedRoleCounts.getOrDefault(r.getRoleCode(), 0);
                int remMax = Math.max(0, r.getMaxNeeded() - assigned);
                int remMin = Math.max(0, r.getMinNeeded() - assigned);
                return new TeamListingDTO.ReqWithCounts(r.getRoleCode(), r.getMinNeeded(), r.getMaxNeeded(), assigned, remMin, remMax);
            }).toList();

            var bgReqs = bgReqRepo.findByTeam(t).stream().map(b -> {
                int assigned = assignedBgCounts.getOrDefault(b.getBackgroundCode(), 0);
                int remMax = Math.max(0, b.getMaxNeeded() - assigned);
                int remMin = Math.max(0, b.getMinNeeded() - assigned);
                return new TeamListingDTO.ReqWithCounts(b.getBackgroundCode(), b.getMinNeeded(), b.getMaxNeeded(), assigned, remMin, remMax);
            }).toList();

            return new TeamListingDTO(
                    t.getTeamId(),
                    t.getTeamName(),
                    t.getIdeaTitle(),
                    t.getLead() != null ? t.getLead().getApplicantId() : null,
                    size,
                    max,
                    spots,
                    roleReqs,
                    bgReqs
            );
        }).toList();
        }

}
