# Load Test Fixes - Summary

## Issues Fixed

### 1. Authentication Issues
- **Fixed**: Proper capitalization of field names (`UserName`, `Password`, `Email`) to match backend DTOs
- **Fixed**: Added try-catch for JSON parsing in login response
- **Fixed**: Proper Bearer token authentication in all authenticated endpoints

### 2. Request Body Format Issues

#### Authentication Endpoints
- ✅ Signup: Uses correct field names (UserName, Password, Email)
- ✅ Login: Uses correct field names (UserName, Password)

#### Project Endpoints
- ✅ Create Project: Matches `CreateProjectRequest` DTO exactly
- ✅ Edit Project: Includes required `projectId` field
- ✅ Get Project: Uses `X-Timezone` header (not in auth headers)
- ✅ Get Schedule: Correct path format

#### Application Endpoints
- ✅ Get Form: Public endpoint, no auth
- ✅ Submit Application: Uses multipart/form-data with `applicationData` parameter
- ✅ Application data matches `SubmitApplicationRequest` DTO (includes all required fields)

#### Organizer Dashboard Endpoints
- ✅ Display Participants: Correct path `/{projectId}/participants`
- ✅ Display Teams: Correct path `/{projectId}/teams`
- ✅ Update Selection: Correct body format `{selected: true/false}`
- ✅ Bulk Selection: Correct body format `{applicantIds: [...], selected: true/false}`
- ✅ Create Teams: Correct path `/{projectId}/create-teams`, requires auth

#### Teams Flow Endpoints
- ✅ List Teams: Public endpoint at `/api/teams-flow/projects/{projectId}/teams`
- ✅ Create Team: Requires auth, needs applicant to exist for project
- ✅ Apply to Team: POST with auth, no body needed
- ✅ View Applications: GET with auth, only team creator can view
- ✅ Decide on Application: Requires auth and decision payload

### 3. Error Handling
- All requests now use `catch_response=True` for proper error handling
- Success is marked for expected failures (404, 409, etc.) to avoid skewing load test results
- Graceful handling of missing data (no projects, no teams, etc.)

## Key Changes

1. **Shared Data Pools**: Projects and teams are shared across users for realistic interactions
2. **Proper Authentication Flow**: All authenticated endpoints now use proper headers
3. **Correct Request Bodies**: All request bodies match backend DTOs exactly
4. **Better Error Handling**: Expected failures don't count as errors in load test results
5. **Removed Assignment Tests**: As requested, no assignment-related endpoints are tested

## Endpoints Tested

### Authentication (All Roles)
- POST `/auth/signup/{role}` - Sign up new users
- POST `/auth/login` - Login and get token
- GET `/auth/test` - Test authenticated access
- GET `/auth/{role}/test` - Test role-specific access

### Projects (Organizer)
- POST `/projects` - Create project
- GET `/projects` - List my projects
- GET `/projects/{id}` - Get project details
- PUT `/projects` - Edit project
- DELETE `/projects/{id}` - Delete project
- GET `/projects/{id}/schedule/{day}` - Get schedule for day

### Applications (Participant)
- GET `/projects/apply/{id}` - Get application form (public)
- POST `/projects/apply` - Submit application (multipart/form-data)

### Organizer Dashboard (Organizer)
- GET `/{projectId}/participants` - View participants
- GET `/{projectId}/teams` - View teams
- PATCH `/{projectId}/applicants/{id}/selection` - Update single selection
- PATCH `/{projectId}/applicants/selection` - Bulk update selection
- POST `/{projectId}/create-teams` - Auto-create teams

### Teams Flow (Participant)
- GET `/api/teams-flow/projects/{id}/teams` - List teams (public)
- POST `/api/teams-flow/projects/{id}/teams` - Create team
- POST `/api/teams-flow/teams/{id}/applications` - Apply to team
- GET `/api/teams-flow/teams/{id}/applications` - View applications
- POST `/api/teams-flow/teams/{id}/applications/{appId}/decision` - Decide on application

### Admin
- All auth test endpoints
- Project viewing and deletion

## How to Run

1. Make sure your backend is running on `http://localhost:8080`

2. Install Locust if not already installed:
```bash
pip install locust
```

3. Run the load test:
```bash
locust -f load_test.py --host http://localhost:8080
```

4. Open your browser to `http://localhost:8089`

5. Configure the test:
   - Number of users: Start with 10-20
   - Spawn rate: 1-2 users per second
   - Host: http://localhost:8080 (should be pre-filled)

6. Click "Start swarming"

## Expected Behavior

- **All requests should now succeed** (status 2xx) or be gracefully handled
- Authentication flow: signup → login → get token → use token
- Organizers create projects → Participants apply → Organizers manage → Participants interact with teams
- Shared data allows realistic cross-user interactions

## Notes

- Some requests may legitimately fail (404, 409) due to race conditions or missing data - these are marked as success
- The test uses catch_response for intelligent error handling
- All field names match backend DTOs exactly (case-sensitive)
- Multipart/form-data is used for application submission as required by the backend
