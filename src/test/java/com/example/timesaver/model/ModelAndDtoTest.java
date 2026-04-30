package com.example.timesaver.model;

import com.example.timesaver.model.dto.applicants.display.*;
import com.example.timesaver.model.dto.applicants.selection.*;
import com.example.timesaver.model.dto.application.*;
import com.example.timesaver.model.dto.auth.*;
import com.example.timesaver.model.dto.project.*;
import com.example.timesaver.model.dto.team.*;
import com.example.timesaver.model.dto.teamflow.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAndDtoTest {

    @Test
    public void testEntities() {
        // Applicant
        Applicant applicant = new Applicant();
        applicant.setApplicantId(1);
        applicant.setFirstName("First");
        applicant.setLastName("Last");
        applicant.setIsSelected(true);
        applicant.setHasApplied(true);
        applicant.setRoles("R1|R2");
        applicant.setBackground("B1");
        applicant.setTimezone("UTC");
        applicant.setRegistrationTimestamp(ZonedDateTime.now());

        Project project = new Project();
        project.setProjectId(10);
        applicant.setProject(project);

        Team team = new Team();
        team.setTeamId(20);
        applicant.setTeam(team);

        User user = new User();
        user.setId(30);
        applicant.setUser(user);

        assertEquals(1, applicant.getApplicantId());
        assertEquals("First", applicant.getFirstName());
        assertEquals(project, applicant.getProject());
        assertEquals(team, applicant.getTeam());
        assertEquals(user, applicant.getUser());
        assertTrue(applicant.getIsSelected());
        assertTrue(applicant.getHasApplied());
        assertEquals("R1|R2", applicant.getRoles());
        assertEquals("B1", applicant.getBackground());
        assertEquals("UTC", applicant.getTimezone());
        assertNotNull(applicant.getRegistrationTimestamp());

        // Project
        project.setProjectName("Name");
        project.setProjectDescription("Desc");
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now());
        project.setMinNrParticipants(1);
        project.setMaxNrParticipants(10);
        project.setTeamsPreformed(true);
        project.setOrganizer(user);
        project.setRolesOptions("R1");
        project.setBackgroundOptions("B1");
        project.setRolesQuestionText("RQ");
        project.setBackgroundQuestionText("BQ");

        assertEquals("Name", project.getProjectName());
        assertEquals(1, project.getMinNrParticipants());
        assertEquals(user, project.getOrganizer());
        assertTrue(project.getTeamsPreformed());

        // Team
        team.setTeamName("Team");
        team.setProject(project);
        team.setLead(applicant);
        team.setDateCreated(ZonedDateTime.now());
        team.setDateUpdated(ZonedDateTime.now());
        team.setIdeaTitle("Idea");
        team.setIdeaDescription("IdeaDesc");

        assertEquals("Team", team.getTeamName());
        assertEquals(applicant, team.getLead());
        assertEquals("Idea", team.getIdeaTitle());

        // User
        user.setUserName("user");
        user.setPassword("pass");
        user.setEmail("email");
        user.setCreationDateTime(LocalDateTime.now());
        user.setRoles(Collections.singleton(Role.ORGANIZER));

        assertEquals("user", user.getUserName());
        assertEquals(Role.ORGANIZER, user.getRoles().iterator().next());

        // FormQuestion
        FormQuestion fq = new FormQuestion();
        fq.setQuestionId(1);
        fq.setProject(project);
        fq.setQuestionNumber(1);
        fq.setQuestionType(QuestionType.TEXT);
        fq.setQuestion("Q?");
        fq.setCheckboxOptions("O1|O2");
        fq.onCreate();

        assertEquals(1, fq.getQuestionNumber());
        assertEquals(QuestionType.TEXT, fq.getQuestionType());
        assertNotNull(fq.getCreatedAt());

        FormQuestion fq2 = new FormQuestion();
        fq2.setQuestionId(1);
        assertEquals(fq, fq2);
        assertEquals(fq.hashCode(), fq2.hashCode());
        assertNotEquals(fq, new Object());

        // QuestionAnswer
        QuestionAnswer qa = new QuestionAnswer();
        qa.setQuestionAnswerId(1);
        qa.setQuestion(fq);
        qa.setApplicant(applicant);
        qa.setQuestionAnswer("Ans");

        assertEquals("Ans", qa.getQuestionAnswer());
        qa.setCheckboxAnswers(new String[]{"A", "B"});
        assertEquals("A|B", qa.getQuestionAnswer());
        assertArrayEquals(new String[]{"A", "B"}, qa.getCheckboxAnswers());
        qa.setCheckboxAnswers(null);
        assertEquals("", qa.getQuestionAnswer());
        qa.setQuestionAnswer(null);
        assertEquals(0, qa.getCheckboxAnswers().length);

        // Assignment
        Assignment ass = new Assignment();
        ass.setId(1);
        ass.setProject(project);
        ass.setTitle("Title");
        ass.setDescription("Desc");
        ass.setDueDate(ZonedDateTime.now());

        assertEquals("Title", ass.getTitle());

        // Submission
        Submission sub = new Submission();
        sub.setId(1);
        sub.setAssignment(ass);
        sub.setTeam(team);
        sub.setUploadedBy(user);
        sub.setTextContent("Text");
        sub.setFilePath("Path");
        sub.onCreate();
        assertNotNull(sub.getUploadTimestamp());

        assertEquals("Text", sub.getTextContent());

        // ProjectSchedule
        ProjectSchedule sch = new ProjectSchedule();
        sch.setScheduleId(1);
        sch.setProject(project);
        sch.setDayNumber(1);
        sch.setStartTime(ZonedDateTime.now());
        sch.setEndTime(ZonedDateTime.now());
        sch.setActivityTitle("Act");
        sch.setActivityDescription("ActDesc");

        assertEquals("Act", sch.getActivityTitle());

        // TeamMember
        TeamMember tm = new TeamMember();
        tm.setTeamMemberId(1);
        tm.setTeam(team);
        tm.setApplicant(applicant);
        tm.onCreate();
        assertNotNull(tm.getJoinedAt());

        // TeamRoleRequirement
        TeamRoleRequirement trr = new TeamRoleRequirement();
        trr.setId(1);
        trr.setRoleCode("R1");
        trr.setMinNeeded(1);
        trr.setMaxNeeded(2);
        assertEquals("R1", trr.getRoleCode());

        // TeamBackgroundRequirement
        TeamBackgroundRequirement tbr = new TeamBackgroundRequirement();
        tbr.setId(1);
        tbr.setBackgroundCode("B1");
        assertEquals("B1", tbr.getBackgroundCode());

        // TeamMemberRole
        TeamMemberRole tmr = new TeamMemberRole();
        tmr.setRoleCode("R1");
        assertEquals("R1", tmr.getRoleCode());

        // TeamMemberBackground
        TeamMemberBackground tmb = new TeamMemberBackground();
        tmb.setBackgroundCode("B1");
        assertEquals("B1", tmb.getBackgroundCode());

        // TeamApplication
        TeamApplication ta = new TeamApplication();
        ta.setTeamApplicationId(1);
        ta.setStatus(TeamApplication.Status.PENDING);
        assertEquals(TeamApplication.Status.PENDING, ta.getStatus());
    }

    @Test
    public void testDTOs() {
        // applicants.display
        GetParticipantsDTO gp = new GetParticipantsDTO(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        gp.setQuestionDTOs(null);
        assertNull(gp.getQuestionDTOs());

        GetTeamsDTO gt = new GetTeamsDTO(Collections.emptyList(), Collections.emptyList());
        gt.setTeams(null);
        assertNull(gt.getTeams());

        ParticipantWithNoTeamDTO pnt = new ParticipantWithNoTeamDTO("F", "L", true, Collections.emptyList());
        assertEquals("F", pnt.getFirstName());

        ParticipantWithTeamDTO pwt = new ParticipantWithTeamDTO("F", "L", "T", true, Collections.emptyList());
        assertEquals("T", pwt.getTeamName());

        com.example.timesaver.model.dto.application.QuestionAnswerDTO qad = new com.example.timesaver.model.dto.application.QuestionAnswerDTO();
        qad.setQuestionNumber(1);
        qad.setQuestion("Q");
        qad.setAnswer("A");
        qad.setQuestionType(QuestionType.TEXT);
        assertEquals("A", qad.getAnswer());

        com.example.timesaver.model.dto.applicants.display.QuestionAnswerDTO qad2 = new com.example.timesaver.model.dto.applicants.display.QuestionAnswerDTO(1, "A");
        assertEquals("A", qad2.getQuestionAnswer());

        QuestionDTO qd = new QuestionDTO(1, "Q");
        assertEquals("Q", qd.getQuestion());
        qd.setQuestionNumber(2);
        assertEquals(2, qd.getQuestionNumber());
        new QuestionDTO();

        TeamDTO td = new TeamDTO("T", Collections.emptyList());
        assertEquals("T", td.getTeamName());

        // applicants.selection
        BulkUpdateSelectionRequest busr = new BulkUpdateSelectionRequest();
        busr.setApplicantIds(List.of(1));
        busr.setSelected(true);
        assertTrue(busr.getSelected());

        UpdateSelectionRequest usr = new UpdateSelectionRequest();
        usr.setSelected(true);
        assertTrue(usr.getSelected());

        UpdateSelectionResponse usres = new UpdateSelectionResponse("S", "M");
        assertEquals("S", usres.getStatus());

        // application
        ApplicationResponse ar = new ApplicationResponse("S", "M");
        assertEquals("S", ar.getStatus());
        ApplicationResponse ar2 = new ApplicationResponse("S", "M", true);
        assertTrue(ar2.getTeamExists());

        // GetFormResponse(List<FormQuestionDTO>, List<String>, List<String>)
        GetFormResponse gfr = new GetFormResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertNotNull(gfr.getFormQuestions());

        SubmitApplicationRequest sar = new SubmitApplicationRequest();
        sar.setFirstName("F");
        assertEquals("F", sar.getFirstName());

        TeammateDTO tmd = new TeammateDTO("F", "L");
        assertEquals("F", tmd.getFirstName());

        // auth
        LoginRequest lr = new LoginRequest();
        lr.setUserName("U");
        lr.setPassword("P");
        assertEquals("U", lr.getUserName());

        LoginResponse lres = new LoginResponse("S", "E", "T");
        assertEquals("T", lres.getToken());

        SignUpRequest sur = new SignUpRequest();
        sur.setUserName("U");
        assertEquals("U", sur.getUserName());

        SignUpResponse sures = new SignUpResponse(true, "E");
        assertTrue(sures.isCreated());
        assertEquals("E", sures.getErrorMessage());

        // project
        CreateProjectRequest cpr = new CreateProjectRequest();
        cpr.setProjectName("P");
        cpr.setRoleOptions(List.of("R"));
        cpr.setBackgroundOptions(List.of("B"));
        assertEquals("P", cpr.getProjectName());

        EditProjectRequest epr = new EditProjectRequest();
        epr.setProjectId(1);
        assertEquals(1, epr.getProjectId());

        FormQuestionDTO fqd = new FormQuestionDTO();
        fqd.setQuestion("Q");
        assertEquals("Q", fqd.getQuestion());

        GetProjectResponse gpr = new GetProjectResponse(1, "N", "D", ZonedDateTime.now(), ZonedDateTime.now(), 10, 1, Collections.emptyList());
        assertEquals("N", gpr.getProjectName());

        ProjectResponse pr = new ProjectResponse("S", "M");
        assertEquals("S", pr.getStatus());
        ProjectResponse pr2 = new ProjectResponse("S", "M", 1);
        assertEquals(1, pr2.getProjectId());

        ScheduleDTO sd = new ScheduleDTO();
        sd.setActivityTitle("A");
        assertEquals("A", sd.getActivityTitle());

        // team
        TeamNrMembers tnm = new TeamNrMembers(new Team(), 5);
        assertEquals(5, tnm.getNrOfMembers());
        tnm.setTeam(null);
        assertNull(tnm.getTeam());

        // teamflow
        ApplyToTeamRequest attr = new ApplyToTeamRequest("M");
        assertEquals("M", attr.message());

        CreateTeamRequest ctr = new CreateTeamRequest(1, "T", "D", Collections.emptyList(), Collections.emptyList());
        assertEquals("T", ctr.ideaTitle());
        new CreateTeamRequest.RoleReq("C", 1, 2);
        new CreateTeamRequest.BackgroundReq("C", 1, 2);

        DecisionRequest dr = new DecisionRequest("A", List.of("R"), List.of("B"));
        assertEquals("A", dr.decision());

        TeamApplicationDTO tad = new TeamApplicationDTO(1, 2, 3, "F", "L", TeamApplication.Status.PENDING, ZonedDateTime.now(), null);
        assertEquals("F", tad.firstName());

        TeamListingDTO tld = new TeamListingDTO(1, "T", "IT", 100, 1, 5, 4, Collections.emptyList(), Collections.emptyList());
        assertEquals("T", tld.teamName());
    }

    @Test
    public void testPrePersistMethods() {
        Applicant a = new Applicant();
        a.onCreate();
        assertNotNull(a.getRegistrationTimestamp());
        assertNotNull(a.getTimezone());

        Team t = new Team();
        t.onCreate();
        assertNotNull(t.getDateCreated());
        assertNotNull(t.getDateUpdated());
        t.onUpdate();

        Project p = new Project();
        p.onCreate();
        assertNotNull(p.getCreatedAt());
        assertNotNull(p.getUpdatedAt());
        p.onUpdate();

        FormQuestion f = new FormQuestion();
        f.onCreate();
        assertNotNull(f.getCreatedAt());
        assertNotNull(f.getUpdatedAt());

        Submission s = new Submission();
        s.onCreate();
        assertNotNull(s.getUploadTimestamp());

        TeamMember tm = new TeamMember();
        tm.onCreate();
        assertNotNull(tm.getJoinedAt());
    }

    @Test
    public void testMoreDTOs() {
        ApplicationResponse ar1 = new ApplicationResponse("S", "M");
        assertEquals("S", ar1.getStatus());
        ApplicationResponse ar2 = new ApplicationResponse("S", "M", true);
        assertTrue(ar2.getTeamExists());

        GetFormResponse gfr = new GetFormResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertEquals(0, gfr.getFormQuestions().size());

        GetProjectResponse gpr = new GetProjectResponse(1, "P", "D", null, null, 10, 1, Collections.emptyList());
        assertEquals(1, gpr.getProjectId());

        ApplyToTeamRequest attr = new ApplyToTeamRequest("msg");
        assertEquals("msg", attr.message());

        DecisionRequest dr = new DecisionRequest("ACCEPT", List.of("R"), List.of("B"));
        assertEquals("ACCEPT", dr.decision());

        CreateTeamRequest ctr = new CreateTeamRequest(1, "T", "D", null, null);
        assertEquals("T", ctr.ideaTitle());

        TeamListingDTO.ReqWithCounts rwc = new TeamListingDTO.ReqWithCounts("C", 1, 2, 1, 0, 1);
        assertEquals("C", rwc.code());
    }
}
