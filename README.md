# Timesaver API Documentation

## Overview

**Base URL:** `http://<host>:<port>`  
**Authentication:** JWT Bearer Token (except public endpoints)  
**Content-Type:** `application/json` (unless otherwise stated)

All protected endpoints require the following header:
```
Authorization: Bearer <jwt_token>
```

---

## Roles & Access Control

| Role | Description |
|------|-------------|
| `ORGANIZER` | Create/manage projects; select applicants |
| `PARTICIPANT` | Apply to projects; manage team memberships |

---

## Error Responses

All endpoints may return the following standard error shapes:

**Validation Error (400)**
```json
{
  "status": "Failure",
  "message": "Validation failed",
  "errors": {
    "fieldName": "error message"
  }
}
```

**Access Denied (403)**
```json
{
  "status": "Failure",
  "message": "Access denied: ..."
}
```

**Not Found (404)**
```json
{
  "status": "Failure",
  "message": "..."
}
```

**Generic Error (400 / 500)**
```json
{
  "status": "Failure",
  "message": "..."
}
```

---

## Authentication — `/auth`

### POST `/auth/signup/organizer`
Register a new organizer account.

**Access:** Public  
**Request Body:**
```json
{
  "UserName": "string",
  "Password": "string"
}
```

**Responses:**

| Status | Description |
|--------|-------------|
| 201 | Account created successfully |
| 409 | Username already exists |
| 500 | Internal server error |

**201 Response Body:**
```json
{
  "created": true,
  "errorMessage": null
}
```

---

### POST `/auth/signup`
Register a new organizer account (alias of `/auth/signup/organizer`).

**Access:** Public  
**Request/Response:** Same as `POST /auth/signup/organizer`

---

### POST `/auth/signup/participant`
Register a new participant account.

**Access:** Public  
**Request Body:**
```json
{
  "UserName": "string",
  "Password": "string"
}
```

**Responses:**

| Status | Description |
|--------|-------------|
| 201 | Account created successfully |
| 409 | Username already exists |

**201 Response Body:**
```json
{
  "created": true,
  "errorMessage": null
}
```

---

### POST `/auth/login`
Authenticate a user and obtain a JWT token.

**Access:** Public  
**Request Body:**
```json
{
  "UserName": "string",
  "Password": "string"
}
```

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Login successful |
| 401 | Invalid credentials |

**200 Response Body:**
```json
{
  "State": "Success",
  "ErrorMessage": null,
  "Token": "<jwt_token>",
  "roles": ["ORGANIZER"]
}
```

**401 Response Body:**
```json
{
  "State": "Failure",
  "ErrorMessage": "User not found",
  "Token": null
}
```

---

### POST `/auth/change-password/{userName}`
Change the password for the authenticated user.

**Access:** Any authenticated user  
**Path Parameter:** `userName` — the username of the account  
**Request Body:**
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | `"Password changed successfully"` |
| 401 | Incorrect old password |
| 404 | User not found |

---

### DELETE `/auth/delete-profile/{userName}`
Delete the profile of a participant or organizer (and all associated data).

**Access:** `PARTICIPANT`, `ORGANIZER`  
**Path Parameter:** `userName` — the username of the account to delete

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | `"Profile deleted successfully"` |
| 403 | Role not permitted to delete |
| 500 | Unexpected error |

> **Note:** This is a destructive operation. For participants, it removes all their applicant records, question answers, team memberships, and team applications. For organizers, it removes all their projects and all associated data (applicants, teams, form questions, etc.).


---

## Projects — `/projects`

### POST `/projects`
Create a new project.

**Access:** `ORGANIZER`  
**Request Body:**
```json
{
  "projectName": "string (required, 1–200 chars)",
  "projectDescription": "string (max 2000 chars)",
  "startDate": "2025-06-01T08:00:00+03:00",
  "endDate": "2025-06-05T18:00:00+03:00",
  "teamsPreformed": true,
  "scheduleVisibility": "EVERYBODY | APPLICANTS | ACCEPTED_PARTICIPANTS",
  "maxNrParticipants": 30,
  "minNrParticipants": 3,
  "roleOptions": ["Developer", "Designer"],
  "backgroundOptions": ["Computer Science", "Marketing"],
  "rolesQuestionText": "What are your preferred roles in the team?",
  "backgroundQuestionText": "What is your background?",
  "formQuestions": [
    {
      "questionNumber": 1,
      "questionType": "TEXT | CHECKBOX | FILE",
      "question": "string (1–1000 chars)",
      "checkboxOptions": "Option A|Option B|Option C"
    }
  ],
  "schedules": [
    {
      "dayNumber": 1,
      "startTime": "2025-06-01T09:00:00+03:00",
      "endTime": "2025-06-01T10:00:00+03:00",
      "activityTitle": "Opening Ceremony",
      "activityDescription": "Welcome and introductions"
    }
  ]
}
```

> **Notes:**
> - `questionNumber` values must be unique, start from 1, and be sequential.
> - `checkboxOptions` is a pipe (`|`) separated string. Required only when `questionType` is `CHECKBOX`.
> - `teamsPreformed: true` applicants register in teams, like at a hackathon (team-based); `false` means participants are registering individually and forming the teams at the event (idea-based)

**Responses:**

| Status | Description |
|--------|-------------|
| 201 | Project created successfully |
| 400 | Validation error |

**201 Response Body:**
```json
{
  "status": "Success",
  "message": null,
  "projectId": 42
}
```

---

### GET `/projects/{projectId}`
Get project details including form questions.

**Access:** Public  
**Path Parameter:** `projectId` — integer project ID  
**Required Header:** `X-Timezone: America/New_York` (IANA timezone string)

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Project found |
| 404 | Project not found |

**200 Response Body:**
```json
{
  "projectId": 42,
  "projectName": "Hackathon 2025",
  "projectDescription": "Annual coding hackathon",
  "startDate": "2025-06-01T09:00:00-04:00",
  "endDate": "2025-06-03T18:00:00-04:00",
  "maxNrParticipants": 30,
  "minNrParticipants": 3,
  "formQuestions": [
    {
      "questionNumber": 1,
      "questionType": "TEXT",
      "question": "Why do you want to join?",
      "checkboxOptions": null
    }
  ]
}
```

> Dates are returned converted to the timezone specified in the `X-Timezone` header.

---

### PUT `/projects`
Update an existing project. Only the project's organizer can edit it.

**Access:** `ORGANIZER`  
**Request Body:**
```json
{
  "projectId": 42,
  "projectName": "string (required, 3–200 chars)",
  "projectDescription": "string",
  "startDate": "2025-06-01T08:00:00+03:00",
  "endDate": "2025-06-05T18:00:00+03:00",
  "maxNrParticipants": 30,
  "minNrParticipants": 3,
  "teamsPreformed": true,
  "scheduleVisibility": "EVERYBODY | APPLICANTS | ACCEPTED_PARTICIPANTS",
  "roleOptions": ["Developer", "Designer"],
  "backgroundOptions": ["Computer Science", "Marketing"],
  "rolesQuestionText": "string",
  "backgroundQuestionText": "string",
  "formQuestions": [
    {
      "questionNumber": 1,
      "questionType": "TEXT",
      "question": "string (required, 1–1000 chars)",
      "checkboxOptions": null
    }
  ],
  "schedules": []
}
```

> **Warning:** Updating a project replaces all form questions. This will fail if applicants have already submitted answers for the existing questions (foreign key constraint).

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Project updated |
| 400 | Validation error |
| 401 | Not authenticated |
| 403 | Not the project organizer |
| 404 | Project not found |

**200 Response Body:**
```json
{
  "status": "Success",
  "message": null,
  "projectId": 42
}
```

---

### DELETE `/projects/{projectId}`
Delete a project. Only the project's organizer can delete it.

**Access:** `ORGANIZER`  
**Path Parameter:** `projectId` — integer project ID

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Project deleted |
| 401 | Not authenticated |
| 404 | Project not found or permission denied |
| 500 | Unexpected error |

**200 Response Body:**
```json
{
  "status": "Success",
  "message": "Project deleted successfully"
}
```

---

### GET `/projects`
Get all projects created by the currently authenticated organizer.

**Access:** `ORGANIZER`

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | List of projects |
| 401 | Not authenticated |

**200 Response Body:**
```json
[
  {
    "projectId": 42,
    "projectName": "Hackathon 2025",
    "projectDescription": "...",
    "startDate": "2025-06-01T09:00:00Z",
    "endDate": "2025-06-03T18:00:00Z",
    "maxNrParticipants": 30,
    "minNrParticipants": 3
  }
]
```

---

### GET `/projects/{projectId}/schedule/{dayNumber}`
Get the schedule for a specific day of a project. Visibility is controlled by the project's `scheduleVisibility` setting.

**Access:** Depends on `scheduleVisibility`:
- `EVERYBODY` — public (no authentication required)
- `APPLICANTS` — any user who has applied
- `ACCEPTED_PARTICIPANTS` — only accepted applicants (or the organizer)

**Path Parameters:**
- `projectId` — integer project ID
- `dayNumber` — integer day number (1-based)

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Schedule items for the day |
| 403 | Access denied by visibility setting |

**200 Response Body:**
```json
[
  {
    "dayNumber": 1,
    "startTime": "2025-06-01T09:00:00Z",
    "endTime": "2025-06-01T10:00:00Z",
    "activityTitle": "Opening Ceremony",
    "activityDescription": "Welcome and introductions"
  }
]
```

---

### GET `/projects/dashboard/future`
Get all upcoming projects (start date in the future), ordered by start date ascending.

**Access:** `PARTICIPANT`

**200 Response Body:**
```json
[
  {
    "projectId": 42,
    "projectName": "Hackathon 2025",
    "startDate": "2025-06-01T09:00:00Z",
    "endDate": "2025-06-03T18:00:00Z",
    "projectDescription": "...",
    "maxTeamSize": 5,
    "minTeamSize": 2
  }
]
```

---

## Applications — `/projects/apply`

### GET `/projects/apply/{projectId}`
Get the application form for a project, including form questions, role options, background options, and whether teams are pre-formed.

**Access:** Public  
**Path Parameter:** `projectId` — integer project ID

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Form retrieved |
| 404 | Project not found |

**200 Response Body:**
```json
{
  "formQuestions": [
    {
      "questionNumber": 1,
      "questionType": "TEXT",
      "question": "Tell us about yourself",
      "checkboxOptions": null
    },
    {
      "questionNumber": 2,
      "questionType": "CHECKBOX",
      "question": "Which tools do you know?",
      "checkboxOptions": "React|Vue|Angular"
    },
    {
      "questionNumber": 3,
      "questionType": "FILE",
      "question": "Upload your CV",
      "checkboxOptions": null
    }
  ],
  "roleOptions": ["Developer", "Designer"],
  "backgroundOptions": ["Computer Science", "Marketing"],
  "teamsPreformed": true
}
```

> `teamsPreformed: `true` applicants  register in teams, like at a hackathon (team-based); `false` means participants are registering individually and forming the teams at the event (idea-based)


---

### POST `/projects/apply`
Submit an application to a project.

**Access:** Public  
**Content-Type:** `multipart/form-data`

**Form Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `applicationData` | String (JSON) | Serialized `SubmitApplicationRequest` object |
| `file_{N}` | File | File upload for FILE-type question number N (e.g., `file_3` for question 3) |

**`applicationData` JSON Schema:**
```json
{
  "projectId": 42,
  "firstName": "string (required, 2–100 chars)",
  "lastName": "string (required, 2–100 chars)",
  "timezone": "America/New_York",
  "roles": ["Developer"],
  "background": ["Computer Science"],
  "teamName": "Team Alpha",
  "joinExistentTeam": false,
  "teammates": [
    {
      "firstName": "string (required, 2–100 chars)",
      "lastName": "string (required, 2–100 chars)"
    }
  ],
  "questionsAnswers": [
    {
      "questionNumber": 1,
      "questionType": "TEXT",
      "question": "Tell us about yourself",
      "answer": "I am a developer..."
    },
    {
      "questionNumber": 2,
      "questionType": "CHECKBOX",
      "question": "Which tools do you know?",
      "answer": "React|Angular"
    },
    {
      "questionNumber": 3,
      "questionType": "FILE",
      "question": "Upload your CV",
      "answer": ""
    }
  ]
}
```

> **File Naming Convention:** For each `FILE` type question (e.g., question number 3), upload the file using the parameter name `file_3`. The number must exactly match the `questionNumber` of the FILE question.

> **Team Logic:**
> - `joinExistentTeam: false` + `teamName` → Creates a new team with that name.
> - `joinExistentTeam: true` + `teamName` → Joins an existing team with that name.
> - `teammates` → Lists pre-formed teammates who will be added as applicants without accounts.

**Responses:**

| Status | Description |
|--------|-------------|
| 201 | Application submitted successfully |
| 400 | Bad request or validation error |
| 409 | Team name already exists (conflict) |

**201 Response Body:**
```json
{
  "status": "Success",
  "message": "Application submitted successfully",
  "teamExists": null
}
```

**409 Response Body:**
```json
{
  "status": "409: Conflict",
  "message": "The team already exists. Please join the team or choose a different name.",
  "teamExists": true
}
```

**Error codes returned in status field:**
- `"404: Not Found"` — Project not found
- `"401: Unauthorized"` — User must be logged in to apply
- `"409: Conflict"` — Team name already taken
- `"400: Bad Request"` — Invalid roles or backgrounds

---

## Applicants — `/{projectId}`

These endpoints are used by organizers to view and manage applicants for a specific project.

### GET `/{projectId}/teams`
Get all teams and their members (first and last name) for a project.

**Access:** Authenticated  
**Path Parameter:** `projectId`

**200 Response Body:**
```json
{
  "teams": [
    {
      "teamName": "Team Alpha",
      "teamMembers": [
        { "firstName": "John", "lastName": "Doe" }
      ]
    }
  ],
  "singleParticipants": [
    { "firstName": "Jane", "lastName": "Smith" }
  ]
}
```

---

### GET `/{projectId}/teams/{teamName}/members`
Get the first and last names of all members belonging to a specific team within a project.

**Access:** Public  
**Path Parameters:**
- `projectId` — integer project ID
- `teamName` — the team name (case-insensitive)

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Members retrieved |
| 500 | Unexpected error |

**200 Response Body:**
```json
[
  { "firstName": "John", "lastName": "Doe" },
  { "firstName": "Jane", "lastName": "Smith" }
]
```

---

### GET `/{projectId}/participants`
Get all applicants for a project, including their form answers and team assignment.

**Access:** Authenticated  
**Path Parameter:** `projectId`

**200 Response Body:**
```json
{
  "questionDTOs": [
    { "questionNumber": 1, "question": "Tell us about yourself" }
  ],
  "participantsWithTeams": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "teamName": "Team Alpha",
      "isSelected": true,
      "questionAnswerDTOs": [
        { "questionNumber": 1, "questionAnswer": "I am a developer" }
      ]
    }
  ],
  "participantsWithoutTeams": [
    {
      "firstName": "Jane",
      "lastName": "Smith",
      "isSelected": false,
      "questionAnswerDTOs": []
    }
  ]
}
```

---

### PATCH `/{projectId}/applicants/{applicantId}/selection`
Accept or reject a single applicant.

**Access:** Organizer of the project  
**Path Parameters:**
- `projectId`
- `applicantId`

**Request Body:**
```json
{
  "selected": true
}
```

> `true` = accepted, `false` = rejected

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Selection updated |
| 400 | Applicant not found or not in project |
| 403 | Not the project organizer |
| 500 | Unexpected error |

**200 Response Body:**
```json
{
  "status": "OK",
  "message": "Applicant marked as accepted"
}
```

---

### PATCH `/{projectId}/applicants/selection`
Accept or reject multiple applicants in bulk.

**Access:** Organizer of the project  
**Path Parameter:** `projectId`

**Request Body:**
```json
{
  "applicantIds": [1, 2, 3],
  "selected": true
}
```

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Bulk selection updated |
| 400 | One or more applicants not in project |
| 403 | Not the project organizer |

**200 Response Body:**
```json
{
  "status": "OK",
  "message": "Updated 3 applicants as accepted"
}
```

---

### POST `/{projectId}/create-teams`
Trigger the automatic team-formation algorithm. Combines incomplete teams, assigns single applicants to existing teams, and creates new teams as needed, respecting the project's min/max participant constraints.

**Access:** `ORGANIZER`  
**Path Parameter:** `projectId`

**Responses:**

| Status | Description |
|--------|-------------|
| 201 | Teams created successfully |
| 500 | Error during team creation |

---

## Participant — `/participant`

### GET `/participant/my-applications`
Get all projects the authenticated participant has applied for, along with their acceptance status.

**Access:** `PARTICIPANT`  
**Note:** The `userName` is resolved from the `userName` request attribute (must be set by filter/interceptor).

**200 Response Body:**
```json
[
  {
    "projectId": 42,
    "projectName": "Hackathon 2025",
    "startDate": "2025-06-01T09:00:00Z",
    "endDate": "2025-06-03T18:00:00Z",
    "projectDescription": "...",
    "hasApplied": true,
    "isAccepted": true
  }
]
```

---

### GET `/participant/team-applications/{projectId}/{userName}`
Get all team applications submitted by a participant for a specific project. Only available for projects where `teamsPreformed = false`.

**Access:** `PARTICIPANT`  
**Path Parameters:**
- `projectId`
- `userName`

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Team applications retrieved |
| 400 | Project uses pre-formed teams |
| 404 | User or project not found, or user has not applied |
| 500 | Unexpected error |

**200 Response Body:**
```json
[
  {
    "teamId": 5,
    "teamName": "Team Beta",
    "status": "PENDING",
    "appliedAt": "2025-05-10T14:30:00Z",
    "decisionAt": null
  }
]
```

> Possible status values: `PENDING`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`

---

### GET `/participant/projects/{projectId}/applicants`
Get the first and last names of all applicants for a given project. Intended for participants to see who else has applied.

**Access:** `PARTICIPANT`  
**Path Parameter:** `projectId` — integer project ID

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Applicant names retrieved |
| 500 | Unexpected error |

**200 Response Body:**
```json
[
  { "firstName": "John", "lastName": "Doe" },
  { "firstName": "Jane", "lastName": "Smith" }
]
```

---

## Teams Flow — `/api/teams-flow`

These endpoints support idea-driven team formation (when `teamsPreformed = false`). All endpoints resolve the acting user from the JWT token and look up their applicant record for the given project.

### POST `/api/teams-flow/projects/{projectId}/teams`
Create a new team within a project. The authenticated user becomes the team lead.

**Access:** Authenticated (must be an applicant of the project)  
**Path Parameter:** `projectId`

**Request Body:**
```json
{
  "projectId": 42,
  "ideaTitle": "Smart City App",
  "ideaDescription": "An app to manage city resources",
  "roles": [
    { "code": "Developer", "min": 1, "max": 3 },
    { "code": "Designer", "min": 0, "max": 1 }
  ],
  "backgrounds": [
    { "code": "Computer Science", "min": 1, "max": 3 }
  ]
}
```

> Role and background `code` values must match the options defined in the project's `roleOptions` / `backgroundOptions`.

**Responses:**

| Status | Description |
|--------|-------------|
| 201 | Team created |
| 400 | Applicant already a lead or member of a team; or invalid role/background codes |
| 500 | Unexpected error |

---

### GET `/api/teams-flow/projects/{projectId}/teams`
List all teams in a project with their current size, spots available, and role/background requirements.

**Access:** Authenticated  
**Path Parameter:** `projectId`

**200 Response Body:**
```json
[
  {
    "teamId": 5,
    "teamName": "a3f1b2c4-...",
    "ideaTitle": "Smart City App",
    "leadApplicantId": 12,
    "size": 2,
    "maxSize": 5,
    "spotsLeft": 3,
    "roles": [
      {
        "code": "Developer",
        "min": 1,
        "max": 3,
        "assigned": 1,
        "remainingMin": 0,
        "remainingMax": 2
      }
    ],
    "backgrounds": []
  }
]
```

---

### POST `/api/teams-flow/teams/{teamId}/applications`
Apply to join a team. The authenticated user must be an applicant of the same project and must not already be a lead or member of another team.

**Access:** Authenticated (must be an applicant of the project)  
**Path Parameter:** `teamId`

**Responses:**

| Status | Description |
|--------|-------------|
| 201 | Application submitted |
| 400 | Already a member/lead; or application already exists |

---

### GET `/api/teams-flow/teams/{teamId}/applications`
Get all applications to a team. Only the team lead can view this.

**Access:** Team lead (authenticated applicant)  
**Path Parameter:** `teamId`

**200 Response Body:**
```json
[
  {
    "id": 10,
    "teamId": 5,
    "applicantId": 7,
    "firstName": "Jane",
    "lastName": "Smith",
    "status": "PENDING",
    "appliedAt": "2025-05-15T10:00:00Z",
    "decisionAt": null
  }
]
```

---

### POST `/api/teams-flow/teams/{teamId}/applications/{appId}/decision`
Accept or reject a team application. Only the team lead can decide.

**Access:** Team lead  
**Path Parameters:**
- `teamId`
- `appId` — team application ID

**Request Body:**
```json
{
  "decision": "ACCEPT",
  "assignRoles": ["Developer"],
  "assignBackgrounds": ["Computer Science"]
}
```

> `decision` must be `"ACCEPT"` or `"REJECT"`. When accepting, `assignRoles` and `assignBackgrounds` are validated against the team's requirements and capacity limits.

**Responses:**

| Status | Description |
|--------|-------------|
| 201 | Decision recorded |
| 400 | Team full; role/background capacity exceeded; application already decided |
| 500 | Unexpected error |

---

### DELETE `/api/teams-flow/teams/{teamId}/members/{memberId}`
Remove a member from a team. Only the team lead can do this.

**Access:** Team lead  
**Path Parameters:**
- `teamId`
- `memberId` — team member ID (not applicant ID)

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Member removed |
| 400 | Member not in this team |

---

### POST `/api/teams-flow/teams/{teamId}/members/{memberId}/leave`
Leave a team voluntarily. Only the member themselves can do this.

**Access:** The member (authenticated applicant)  
**Path Parameters:**
- `teamId`
- `memberId` — team member ID (not applicant ID)

**Responses:**

| Status | Description |
|--------|-------------|
| 200 | Successfully left team |
| 400 | Member not in this team or not the acting user |

---

## Data Models

### QuestionType
```
TEXT      — Free-text answer
CHECKBOX  — Multiple-choice selection (options stored pipe-separated)
FILE      — File upload
```

### ScheduleVisibility
```
EVERYBODY             — Anyone can view the schedule
APPLICANTS            — Only people who have applied can view
ACCEPTED_PARTICIPANTS — Only accepted participants (and the organizer) can view
```

### TeamApplication.Status
```
PENDING    — Application submitted, awaiting decision
ACCEPTED   — Application accepted by team lead
REJECTED   — Application rejected by team lead
WITHDRAWN  — Applicant withdrew
```

---

## Security Notes

- JWT tokens are validated on every request via `JwtAuthenticationFilter`.
- Tokens are signed with HMAC-SHA256 using a secret configured in `jwt.secret`.
- Token expiry is controlled by `jwt.expiration` (milliseconds).
- Sessions are **stateless** — no server-side session state is maintained.
- CSRF protection is **disabled** (appropriate for stateless JWT APIs).
- XSS protection headers are applied (`X-XSS-Protection: 1; mode=block`).
- Frame embedding is denied (`X-Frame-Options: DENY`).
- Swagger UI is accessible at `/swagger-ui/**` and `/v3/api-docs/**` without authentication.