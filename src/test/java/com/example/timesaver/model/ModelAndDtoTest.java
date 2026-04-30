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
        applicant.setApplicantId(1L);
        applicant.setFirstName("First");
        applicant.setLastName("Last");
        applicant.setIsSelected(true);
        applicant.setHasApplied(true);
        applicant.setRoles("R1|R2");
        applicant.setBackground("B1");
        applicant.setTimezone("UTC");
        applicant.setRegistrationTimestamp(ZonedDateTime.now());
        
        Project project = new Project();
        project.setProjectId(10L);
        applicant.setProject(project);
        
        Team team = new Team();
        team.setTeamId(20L);
        applicant.setTeam(team);
        
        User user = new User();
        user.setId(30L);
        applicant.setUser(user);

        assertEquals(1L, applicant.getApplicantId());
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
        fq.setQuestionId(1L);
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
        fq2.setQuestionId(1L);
        assertEquals(fq, fq2);
        assertEquals(fq.hashCode(), fq2.hashCode());
        assertNotEquals(fq, new Object());

        // QuestionAnswer
        QuestionAnswer qa = new QuestionAnswer();
        qa.setQuestionAnswerId(1L);
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
        ass.setId(1L);
        ass.setProject(project);
        ass.setTitle("Title");
        ass.setDescription("Desc");
        ass.setDueDate(ZonedDateTime.now());
        
        assertEquals("Title", ass.getTitle());

        // Submission
        Submission sub = new Submission();
        sub.setId(1L);
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
        sch.setScheduleId(1L);
        sch.setProject(project);
        sch.setDayNumber(1);
        sch.setStartTime(ZonedDateTime.now());
        sch.setEndTime(ZonedDateTime.now());
        sch.setActivityTitle("Act");
        sch.setActivityDescription("ActDesc");
        
        assertEquals("Act", sch.getActivityTitle());

        // TeamMember, TeamRoleRequirement, etc.
        TeamMember tm = new TeamMember();
        tm.setTeamMemberId(1L);
        tm.setTeam(team);
        tm.setApplicant(applicant);
        tm.onCreate();
        assertNotNull(tm.getJoinedAt());

        TeamRoleRequirement trr = new TeamRoleRequirement();
        trr.setId(1L);
        trr.setRoleCode("R1");
        trr.setMinNeeded(1);
        trr.setMaxNeeded(2);
        assertEquals("R1", trr.getRoleCode());

        TeamBackgroundRequirement tbr = new TeamBackgroundRequirement();
        tbr.setId(1L);
        tbr.setBackgroundCode("B1");
        assertEquals("B1", tbr.getBackgroundCode());

        TeamMemberRole tmr = new TeamMemberRole();
        tmr.setRoleCode("R1");
        assertEquals("R1", tmr.getRoleCode());

        TeamMemberBackground tmb = new TeamMemberBackground();
        tmb.setBackgroundCode("B1");
        assertEquals("B1", tmb.getBackgroundCode());

        TeamApplication ta = new TeamApplication();
        ta.setId(1L);
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
        busr.setApplicantIds(List.of(1L));
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
        epr.setProjectId(1L);
        assertEquals(1L, epr.getProjectId());

        FormQuestionDTO fqd = new FormQuestionDTO();
        fqd.setQuestion("Q");
        assertEquals("Q", fqd.getQuestion());

        GetProjectResponse gpr = new GetProjectResponse(1L, "N", "D", ZonedDateTime.now(), ZonedDateTime.now(), 10, 1, Collections.emptyList());
        assertEquals("N", gpr.getProjectName());

        ProjectResponse pr = new ProjectResponse("S", "M");
        assertEquals("S", pr.getStatus());
        ProjectResponse pr2 = new ProjectResponse("S", "M", 1L);
        assertEquals(1L, pr2.getProjectId());

        ScheduleDTO sd = new ScheduleDTO();
        sd.setActivityTitle("A");
        assertEquals("A", sd.getActivityTitle());

        // team
        TeamNrMembers tnm = new TeamNrMembers(new Team(), 5L);
        assertEquals(5L, tnm.getNrOfMembers());
        tnm.setTeam(null);
        assertNull(tnm.getTeam());

        // teamflow
        ApplyToTeamRequest attr = new ApplyToTeamRequest("M");
        assertEquals("M", attr.message());

        CreateTeamRequest ctr = new CreateTeamRequest(1L, "T", "D", Collections.emptyList(), Collections.emptyList());
        assertEquals("T", ctr.ideaTitle());
        new CreateTeamRequest.RoleReq("C", 1, 2);
        new CreateTeamRequest.BackgroundReq("C", 1, 2);

        DecisionRequest dr = new DecisionRequest("A", List.of("R"), List.of("B"));
        assertEquals("A", dr.decision());

        TeamApplicationDTO tad = new TeamApplicationDTO(1L, 2L, 3L, "F", "L", TeamApplication.Status.PENDING, ZonedDateTime.now(), null);
        assertEquals("F", tad.firstName());

        TeamListingDTO tld = new TeamListingDTO(1L, "T", "IT", 100L, 1, 5, 4, Collections.emptyList(), Collections.emptyList());
        assertEquals("T", tld.teamName());
    }
    @Test
    public void testEqualsAndHashCode() {
        Applicant a1 = new Applicant(); a1.setApplicantId(1L);
        Applicant a2 = new Applicant(); a2.setApplicantId(1L);
        Applicant a3 = new Applicant(); a3.setApplicantId(2L);
        assertEquals(a1, a2);
        assertNotEquals(a1, a3);
        assertNotEquals(a1, null);
        assertNotEquals(a1, new Object());
        assertEquals(a1.hashCode(), a2.hashCode());

        Team t1 = new Team(); t1.setTeamId(1L);
        Team t2 = new Team(); t2.setTeamId(1L);
        Team t3 = new Team(); t3.setTeamId(2L);
        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertNotEquals(t1, null);
        assertNotEquals(t1, new Object());
        assertEquals(t1.hashCode(), t2.hashCode());

        Project p1 = new Project(); p1.setProjectId(1L);
        Project p2 = new Project(); p2.setProjectId(1L);
        Project p3 = new Project(); p3.setProjectId(2L);
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, new Object());
        assertEquals(p1.hashCode(), p2.hashCode());

        User u1 = new User(); u1.setId(1L);
        User u2 = new User(); u2.setId(1L);
        User u3 = new User(); u3.setId(2L);
        assertEquals(u1, u2);
        assertNotEquals(u1, u3);
        assertNotEquals(u1, null);
        assertNotEquals(u1, new Object());
        assertEquals(u1.hashCode(), u2.hashCode());

        FormQuestion f1 = new FormQuestion(); f1.setQuestionId(1L);
        FormQuestion f2 = new FormQuestion(); f2.setQuestionId(1L);
        FormQuestion f3 = new FormQuestion(); f3.setQuestionId(2L);
        assertEquals(f1, f2);
        assertNotEquals(f1, f3);
        assertNotEquals(f1, null);
        assertNotEquals(f1, new Object());
        assertEquals(f1.hashCode(), f2.hashCode());
        
        QuestionAnswer qa = new QuestionAnswer();
        qa.setQuestionAnswer("O1|O2");
        assertArrayEquals(new String[]{"O1", "O2"}, qa.getCheckboxAnswers());
        qa.setCheckboxAnswers(new String[]{"O3"});
        assertEquals("O3", qa.getQuestionAnswer());
        qa.setCheckboxAnswers(null);
        assertEquals("", qa.getQuestionAnswer());
        qa.setQuestionAnswer(null);
        assertArrayEquals(new String[0], qa.getCheckboxAnswers());

        Assignment ass1 = new Assignment(); ass1.setId(1L);
        Assignment ass2 = new Assignment(); ass2.setId(1L);
        assertEquals(ass1, ass2);
        assertEquals(ass1.hashCode(), ass2.hashCode());

        Submission sub1 = new Submission(); sub1.setId(1L);
        Submission sub2 = new Submission(); sub2.setId(1L);
        assertEquals(sub1, sub2);
        assertEquals(sub1.hashCode(), sub2.hashCode());

        TeamMember tm1 = new TeamMember(); tm1.setTeamMemberId(1L);
        TeamMember tm2 = new TeamMember(); tm2.setTeamMemberId(1L);
        assertEquals(tm1, tm2);
        assertEquals(tm1.hashCode(), tm2.hashCode());

        TeamRoleRequirement trr1 = new TeamRoleRequirement(); trr1.setId(1L);
        TeamRoleRequirement trr2 = new TeamRoleRequirement(); trr2.setId(1L);
        assertEquals(trr1, trr2);

        TeamBackgroundRequirement tbr1 = new TeamBackgroundRequirement(); tbr1.setId(1L);
        TeamBackgroundRequirement tbr2 = new TeamBackgroundRequirement(); tbr2.setId(1L);
        assertEquals(tbr1, tbr2);

        TeamMemberRole tmr1 = new TeamMemberRole(); tmr1.setId(1L);
        TeamMemberRole tmr2 = new TeamMemberRole(); tmr2.setId(1L);
        assertEquals(tmr1, tmr2);

        TeamMemberBackground tmb1 = new TeamMemberBackground(); tmb1.setId(1L);
        TeamMemberBackground tmb2 = new TeamMemberBackground(); tmb2.setId(1L);
        assertEquals(tmb1, tmb2);

        TeamApplication ta1 = new TeamApplication(); ta1.setId(1L);
        TeamApplication ta2 = new TeamApplication(); ta2.setId(1L);
        assertEquals(ta1, ta2);
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

        GetProjectResponse gpr = new GetProjectResponse(1L, "P", "D", null, null, 10, 1, Collections.emptyList());
        assertEquals(1L, gpr.getProjectId());

        ApplyToTeamRequest attr = new ApplyToTeamRequest("msg");
        assertEquals("msg", attr.message());
        
        DecisionRequest dr = new DecisionRequest("ACCEPT", List.of("R"), List.of("B"));
        assertEquals("ACCEPT", dr.decision());
        
        CreateTeamRequest ctr = new CreateTeamRequest(1L, "T", "D", null, null);
        assertEquals("T", ctr.ideaTitle());
        
        TeamListingDTO.ReqWithCounts rwc = new TeamListingDTO.ReqWithCounts("C", 1, 2, 1, 0, 1);
        assertEquals("C", rwc.code());
    }
}
