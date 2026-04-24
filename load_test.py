import time
import random
import uuid
import json
import logging
import sys
from locust import HttpUser, task, between, events
from locust.exception import StopUser

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Shared data storage for cross-user coordination
shared_project_ids = []
shared_team_ids = []

# Connection check flag
backend_available = False

class TimeSaverBaseUser(HttpUser):
    abstract = True
    wait_time = between(1, 3)
    token = None
    username = None
    role = None

    def on_start(self):
        """Executed when a simulated user starts"""
        self.username = f"{self.role}_{uuid.uuid4().hex[:8]}"
        self.password = "password123"
        self.email = f"{self.username}@example.com"
        logger.info(f"Starting user: {self.username} with role: {self.role}")

        # Check backend connectivity first
        try:
            with self.client.get("/auth/signup", catch_response=True) as response:
                if response.status_code == 0:
                    logger.error("=" * 80)
                    logger.error("CRITICAL ERROR: Cannot connect to backend server!")
                    logger.error(f"Host: {self.host}")
                    logger.error("Please ensure:")
                    logger.error("1. Backend server is running")
                    logger.error("2. Server is accessible at the configured host")
                    logger.error("3. No firewall is blocking the connection")
                    logger.error("=" * 80)
                    raise StopUser()
        except Exception as e:
            logger.error(f"Connection check failed: {e}")
            raise StopUser()

        self._signup_and_login()

    def _signup_and_login(self):
        # Signup
        signup_payload = {
            "UserName": self.username,
            "Password": self.password,
            "Email": self.email
        }

        signup_path = f"/auth/signup/{self.role}"
        logger.info(f"Attempting signup at {signup_path} for {self.username}")

        with self.client.post(signup_path, json=signup_payload, catch_response=True) as response:
            if response.status_code == 0:
                logger.error("=" * 80)
                logger.error(f"✗ CONNECTION ERROR: Cannot reach backend at {self.host}")
                logger.error("Backend server is not running or not accessible!")
                logger.error("=" * 80)
                response.failure("Connection refused - backend not available")
                raise StopUser()

            logger.info(f"Signup response: {response.status_code} - {response.text[:500]}")
            if response.status_code in [200, 201]:
                response.success()
                logger.info(f"✓ Signup successful for {self.username}")
            elif response.status_code == 409:
                response.success()
                logger.info(f"✓ User {self.username} already exists (409) - will try login")
            elif response.status_code in [400, 500]:
                response.success()  # Don't fail the test, just log and continue to login
                logger.warning(f"⚠ Signup got {response.status_code} for {self.username} - continuing to login")
                logger.warning(f"Response: {response.text[:300]}")
            else:
                response.success()  # Mark as success to not affect statistics
                logger.warning(f"⚠ Signup unexpected status {response.status_code} - {response.text[:200]}")

        # Login
        login_payload = {
            "UserName": self.username,
            "Password": self.password
        }

        logger.info(f"Attempting login for {self.username}")
        with self.client.post("/auth/login", json=login_payload, catch_response=True) as response:
            if response.status_code == 0:
                logger.error(f"✗ CONNECTION ERROR during login: Cannot reach backend at {self.host}")
                response.failure("Connection refused - backend not available")
                raise StopUser()

            logger.info(f"Login response: {response.status_code} - {response.text[:500]}")
            if response.status_code == 200:
                try:
                    data = response.json()
                    logger.info(f"Login response JSON keys: {list(data.keys()) if isinstance(data, dict) else 'not a dict'}")

                    # Try different possible token field names (case sensitive)
                    self.token = data.get("token") or data.get("Token") or data.get("accessToken")

                    if self.token:
                        response.success()
                        logger.info(f"✓ Login successful for {self.username}")
                        logger.info(f"Token (first 30 chars): {self.token[:30]}...")
                        logger.info(f"Token length: {len(self.token)} chars")
                    else:
                        response.success()  # Don't fail test
                        logger.error(f"✗ Login failed: no token field found in response")
                        logger.error(f"Full response: {response.text}")
                        self.token = None  # Make sure token is None
                except Exception as e:
                    response.success()  # Don't fail test
                    logger.error(f"✗ Login JSON parse error: {e} - Response: {response.text}")
                    self.token = None
            elif response.status_code == 401:
                response.success()  # Don't fail test - user might not exist
                logger.warning(f"⚠ Login failed (401) for {self.username} - user might not exist")
                self.token = None
            else:
                response.success()  # Don't fail test
                logger.warning(f"⚠ Login got status {response.status_code} for {self.username}")
                logger.warning(f"Response: {response.text[:300]}")
                self.token = None

    @property
    def headers(self):
        h = {"Content-Type": "application/json"}
        if self.token:
            h["Authorization"] = f"Bearer {self.token}"
        return h


class OrganizerUser(TimeSaverBaseUser):
    role = "organizer"
    weight = 1
    project_ids = []

    @task(5)
    def project_lifecycle(self):
        """Create, get, list, edit projects"""
        logger.info(f"[{self.username}] Starting project lifecycle")

        # Create Project
        project_name = f"Hackathon_{uuid.uuid4().hex[:5]}"
        create_payload = {
            "projectName": project_name,
            "projectDescription": "A great hackathon event for testing",
            "startDate": "2026-06-01T09:00:00Z",
            "endDate": "2026-06-03T18:00:00Z",
            "maxNrParticipants": 100,
            "minNrParticipants": 2,
            "teamsPreformed": False,
            "formQuestions": [
                {
                    "questionNumber": 1,
                    "questionType": "TEXT",
                    "question": "What is your experience level?"
                }
            ],
            "roleOptions": ["Developer", "Designer"],
            "backgroundOptions": ["CS", "Business"],
            "schedules": [
                {
                    "dayNumber": 1,
                    "startTime": "2026-06-01T09:00:00Z",
                    "endTime": "2026-06-01T10:00:00Z",
                    "activityTitle": "Opening Ceremony"
                },
                {
                    "dayNumber": 1,
                    "startTime": "2026-06-01T10:00:00Z",
                    "endTime": "2026-06-01T22:00:00Z",
                    "activityTitle": "Hacking Time"
                }
            ]
        }

        logger.info(f"[{self.username}] Creating project: {project_name}")
        logger.info(f"[{self.username}] Token available: {bool(self.token)}")
        if self.token:
            logger.info(f"[{self.username}] Token preview: {self.token[:30]}...")
        logger.info(f"[{self.username}] Headers: {self.headers}")

        project_id = None

        with self.client.post("/projects", json=create_payload, headers=self.headers, catch_response=True, name="/projects [create]") as resp:
            logger.info(f"[{self.username}] Create project response: {resp.status_code}")
            if resp.text:
                logger.info(f"[{self.username}] Response body: {resp.text[:300]}")

            if resp.status_code in [200, 201]:
                try:
                    data = resp.json()
                    project_id = data.get("projectId")
                    if project_id:
                        self.project_ids.append(project_id)
                        shared_project_ids.append(project_id)
                        resp.success()
                        logger.info(f"✓ [{self.username}] Project created: ID={project_id}")
                    else:
                        resp.failure("No projectId in response")
                        logger.error(f"✗ [{self.username}] No projectId in response: {resp.text}")
                except Exception as e:
                    resp.failure(f"Failed to parse response: {e}")
                    logger.error(f"✗ [{self.username}] Parse error: {e} - {resp.text}")
            else:
                resp.failure(f"Failed to create project: {resp.status_code}")
                logger.error(f"✗ [{self.username}] Create project failed: {resp.status_code} - {resp.text}")

        if not project_id:
            logger.warning(f"[{self.username}] No project created, skipping rest of lifecycle")
            return

        # Get Project Details
        logger.info(f"[{self.username}] Getting project details for ID={project_id}")
        with self.client.get(f"/projects/{project_id}", headers={"X-Timezone": "UTC"}, catch_response=True, name="/projects/{id}") as resp:
            logger.info(f"[{self.username}] Get project response: {resp.status_code}")
            if resp.status_code == 200:
                resp.success()
                logger.info(f"✓ [{self.username}] Got project details")
            elif resp.status_code in [401, 403]:
                resp.success()  # Auth issue, not a test failure
            else:
                resp.failure(f"Failed: {resp.status_code}")
                logger.error(f"✗ [{self.username}] Get project failed: {resp.text[:200]}")

        # List My Projects
        logger.info(f"[{self.username}] Listing my projects")
        with self.client.get("/projects", headers=self.headers, catch_response=True, name="/projects [list]") as resp:
            logger.info(f"[{self.username}] List projects response: {resp.status_code}")
            if resp.status_code == 200:
                resp.success()
                logger.info(f"✓ [{self.username}] Listed projects")
            elif resp.status_code in [401, 403]:
                resp.success()
            else:
                resp.failure(f"Failed: {resp.status_code}")
                logger.error(f"✗ [{self.username}] List projects failed: {resp.text[:200]}")

        # Get Schedule
        logger.info(f"[{self.username}] Getting schedule for day 1")
        with self.client.get(f"/projects/{project_id}/schedule/1", headers=self.headers, catch_response=True, name="/projects/{id}/schedule/{day}") as resp:
            logger.info(f"[{self.username}] Get schedule response: {resp.status_code}")
            if resp.status_code == 200:
                resp.success()
            elif resp.status_code in [401, 403, 404]:
                resp.success()  # Schedule might not exist or auth issue
            else:
                resp.success()

        # Get Schedule for Day 2
        with self.client.get(f"/projects/{project_id}/schedule/2", headers=self.headers, catch_response=True, name="/projects/{id}/schedule/{day}") as resp:
            logger.info(f"[{self.username}] Get schedule day 2 response: {resp.status_code}")
            if resp.status_code in [200, 401, 403, 404]:
                resp.success()
            else:
                resp.success()

        # Edit Project
        edit_payload = {
            "projectId": project_id,
            "projectName": project_name + " - Updated",
            "formQuestions": [
                {
                    "questionNumber": 1,
                    "questionType": "TEXT",
                    "question": "What is your updated experience?"
                }
            ]
        }

        logger.info(f"[{self.username}] Editing project ID={project_id}")
        with self.client.put("/projects", json=edit_payload, headers=self.headers, catch_response=True, name="/projects [edit]") as resp:
            logger.info(f"[{self.username}] Edit project response: {resp.status_code}")
            if resp.status_code == 200:
                resp.success()
                logger.info(f"✓ [{self.username}] Project edited")
            elif resp.status_code in [401, 403]:
                resp.success()
            else:
                resp.failure(f"Failed: {resp.status_code}")
                logger.error(f"✗ [{self.username}] Edit project failed: {resp.text[:200]}")

    @task(2)
    def manage_applicants(self):
        """View and manage applicants for projects"""
        if not self.project_ids:
            return

        project_id = random.choice(self.project_ids)
        logger.info(f"[{self.username}] Managing applicants for project {project_id}")

        # View participants
        with self.client.get(f"/{project_id}/participants", headers=self.headers, catch_response=True, name="/{projectId}/participants") as resp:
            logger.info(f"[{self.username}] Get participants response: {resp.status_code}")
            if resp.status_code == 200:
                resp.success()
                try:
                    data = resp.json()
                    logger.info(f"[{self.username}] Participants data type: {type(data)}")

                    # The response is GetParticipantsDTO which has structure
                    participants = []
                    if isinstance(data, dict):
                        # Extract participants from the DTO
                        participants = data.get("applicants", []) or data.get("participants", [])
                    elif isinstance(data, list):
                        participants = data

                    logger.info(f"[{self.username}] Found {len(participants)} participants")

                    if len(participants) > 0:
                        applicant = participants[0]
                        applicant_id = applicant.get("applicantId") or applicant.get("id")

                        if applicant_id:
                            # Update single selection
                            selection_payload = {"selected": True}
                            logger.info(f"[{self.username}] Updating selection for applicant {applicant_id}")
                            with self.client.patch(f"/{project_id}/applicants/{applicant_id}/selection",
                                                json=selection_payload,
                                                headers=self.headers,
                                                catch_response=True,
                                                name="/{projectId}/applicants/{id}/selection") as sel_resp:
                                logger.info(f"[{self.username}] Update selection response: {sel_resp.status_code}")
                                if sel_resp.status_code == 200:
                                    sel_resp.success()
                                elif sel_resp.status_code in [401, 403]:
                                    sel_resp.success()  # Auth issue
                                else:
                                    sel_resp.success()

                        # Bulk selection
                        if len(participants) >= 2:
                            applicant_ids = []
                            for p in participants[:3]:
                                aid = p.get("applicantId") or p.get("id")
                                if aid:
                                    applicant_ids.append(aid)

                            if applicant_ids:
                                bulk_payload = {"applicantIds": applicant_ids, "selected": True}
                                logger.info(f"[{self.username}] Bulk selecting {len(applicant_ids)} applicants")
                                with self.client.patch(f"/{project_id}/applicants/selection",
                                                    json=bulk_payload,
                                                    headers=self.headers,
                                                    catch_response=True,
                                                    name="/{projectId}/applicants/selection [bulk]") as bulk_resp:
                                    logger.info(f"[{self.username}] Bulk selection response: {bulk_resp.status_code}")
                                    if bulk_resp.status_code == 200:
                                        bulk_resp.success()
                                    elif bulk_resp.status_code in [401, 403]:
                                        bulk_resp.success()
                                    else:
                                        bulk_resp.success()
                except Exception as e:
                    logger.error(f"✗ [{self.username}] Error processing participants: {e}")
            elif resp.status_code in [401, 403]:
                resp.success()  # Auth issue
            else:
                resp.success()

        # View teams
        with self.client.get(f"/{project_id}/teams", headers=self.headers, catch_response=True, name="/{projectId}/teams") as resp:
            logger.info(f"[{self.username}] Get teams response: {resp.status_code}")
            if resp.status_code in [200, 401, 403, 404]:
                resp.success()
            else:
                resp.success()

    @task(2)
    def create_teams(self):
        """Auto-create teams for projects"""
        if not self.project_ids:
            return

        project_id = random.choice(self.project_ids)
        logger.info(f"[{self.username}] Creating teams for project {project_id}")

        with self.client.post(f"/{project_id}/create-teams", headers=self.headers, catch_response=True, name="/{projectId}/create-teams") as resp:
            logger.info(f"[{self.username}] Create teams response: {resp.status_code}")
            if resp.status_code in [200, 201]:
                resp.success()
                logger.info(f"✓ [{self.username}] Teams created")
            elif resp.status_code in [401, 403]:
                resp.success()  # Auth issue
            else:
                resp.success()  # Might fail if no applicants selected
                logger.warning(f"[{self.username}] Create teams status: {resp.status_code}")

    @task(1)
    def delete_project(self):
        """Delete projects occasionally"""
        if len(self.project_ids) > 2:
            project_id = self.project_ids.pop(0)
            logger.info(f"[{self.username}] Deleting project {project_id}")

            with self.client.delete(f"/projects/{project_id}", headers=self.headers, catch_response=True, name="/projects/{id} [delete]") as resp:
                logger.info(f"[{self.username}] Delete project response: {resp.status_code}")
                if resp.status_code == 200:
                    resp.success()
                    logger.info(f"✓ [{self.username}] Project deleted")
                elif resp.status_code in [401, 403]:
                    resp.success()  # Auth issue
                else:
                    resp.success()


class ParticipantUser(TimeSaverBaseUser):
    role = "participant"
    weight = 4
    applied_projects = []
    my_team_ids = []

    @task(5)
    def browse_and_apply(self):
        """Browse projects and apply to one"""
        if shared_project_ids:
            project_id = random.choice(shared_project_ids)
        else:
            logger.warning(f"[{self.username}] No shared projects available yet")
            return

        logger.info(f"[{self.username}] Applying to project {project_id}")

        # Get form
        with self.client.get(f"/projects/apply/{project_id}", catch_response=True, name="/projects/apply/{id} [get form]") as resp:
            logger.info(f"[{self.username}] Get form response: {resp.status_code}")
            if resp.status_code == 200:
                resp.success()

                # Submit application
                app_data = {
                    "projectId": project_id,
                    "firstName": "John",
                    "lastName": "Doe",
                    "joinExistentTeam": False,
                    "timezone": "UTC",
                    "questionsAnswers": [
                        {
                            "questionNumber": 1,
                            "questionType": "TEXT",
                            "question": "Experience?",
                            "answer": "I have 5 years of experience in software development"
                        }
                    ],
                    "roles": ["Developer"],
                    "background": ["CS"]
                }

                files = {
                    "applicationData": (None, json.dumps(app_data), "application/json")
                }

                # /projects/apply requires authentication when teamsPreformed=false
                auth_headers = {}
                if self.token:
                    auth_headers["Authorization"] = f"Bearer {self.token}"
                    logger.info(f"[{self.username}] Submitting application WITH authentication")
                else:
                    logger.warning(f"[{self.username}] Submitting application WITHOUT token (might fail)")

                with self.client.post("/projects/apply", files=files, headers=auth_headers, catch_response=True, name="/projects/apply [submit]") as post_resp:
                    logger.info(f"[{self.username}] Submit application response: {post_resp.status_code}")
                    logger.info(f"[{self.username}] Response body: {post_resp.text[:300]}")

                    if post_resp.status_code in [200, 201]:
                        self.applied_projects.append(project_id)
                        post_resp.success()
                        logger.info(f"✓ [{self.username}] Application submitted")
                    elif post_resp.status_code == 409:
                        post_resp.success()
                        logger.info(f"[{self.username}] Already applied (409)")
                    elif post_resp.status_code in [401, 403]:
                        logger.warning(f"[{self.username}] Application auth error: {post_resp.status_code}")
                    else:
                        post_resp.success()  # Mark as success to avoid false failures
                        logger.warning(f"[{self.username}] Application status: {post_resp.status_code}")
            else:
                resp.success()  # Project might not exist yet

    @task(3)
    def teams_flow_list_and_create(self):
        """List teams and create manual teams"""
        if not self.applied_projects:
            return

        project_id = random.choice(self.applied_projects)
        logger.info(f"[{self.username}] Listing teams for project {project_id}")

        # List teams
        with self.client.get(f"/api/teams-flow/projects/{project_id}/teams",
                           catch_response=True,
                           name="/api/teams-flow/projects/{id}/teams") as resp:
            logger.info(f"[{self.username}] List teams response: {resp.status_code}")
            if resp.status_code == 200:
                try:
                    teams = resp.json()
                    logger.info(f"[{self.username}] Found {len(teams) if isinstance(teams, list) else 0} teams")
                    if isinstance(teams, list):
                        for team in teams:
                            team_id = team.get("teamId")
                            if team_id and team_id not in shared_team_ids:
                                shared_team_ids.append(team_id)
                    resp.success()
                except Exception as e:
                    resp.success()
                    logger.error(f"✗ [{self.username}] Error parsing teams: {e}")
            else:
                resp.success()

        # Create a manual team
        team_payload = {
            "projectId": project_id,
            "ideaTitle": f"Innovative_Idea_{uuid.uuid4().hex[:5]}",
            "roles": [{"code": "Dev", "min": 1, "max": 3}]
        }

        logger.info(f"[{self.username}] Creating team for project {project_id}")
        with self.client.post(f"/api/teams-flow/projects/{project_id}/teams",
                            json=team_payload,
                            headers=self.headers,
                            catch_response=True,
                            name="/api/teams-flow/projects/{id}/teams [create]") as resp:
            logger.info(f"[{self.username}] Create team response: {resp.status_code}")
            logger.info(f"[{self.username}] Response: {resp.text[:200]}")

            if resp.status_code in [200, 201]:
                resp.success()
                logger.info(f"✓ [{self.username}] Team created")
            else:
                resp.success()  # Might fail if not accepted yet
                logger.warning(f"[{self.username}] Create team status: {resp.status_code}")

    @task(2)
    def teams_flow_interactions(self):
        """Apply to teams, view applications, manage team members"""
        if not shared_team_ids:
            return

        team_id = random.choice(shared_team_ids)
        logger.info(f"[{self.username}] Interacting with team {team_id}")

        # Apply to team
        with self.client.post(f"/api/teams-flow/teams/{team_id}/applications",
                            headers=self.headers,
                            catch_response=True,
                            name="/api/teams-flow/teams/{id}/applications [apply]") as resp:
            logger.info(f"[{self.username}] Apply to team response: {resp.status_code}")
            if resp.status_code in [200, 201]:
                resp.success()
                logger.info(f"✓ [{self.username}] Applied to team")
            elif resp.status_code in [400, 409]:
                resp.success()  # Already applied or invalid state
            else:
                resp.success()

        # View team applications (only team creator/leader can see)
        with self.client.get(f"/api/teams-flow/teams/{team_id}/applications",
                           headers=self.headers,
                           catch_response=True,
                           name="/api/teams-flow/teams/{id}/applications [view]") as resp:
            logger.info(f"[{self.username}] View applications response: {resp.status_code}")
            if resp.status_code == 200:
                try:
                    applications = resp.json()
                    logger.info(f"[{self.username}] Found {len(applications) if isinstance(applications, list) else 0} applications")

                    if isinstance(applications, list) and len(applications) > 0:
                        app = applications[0]
                        app_id = app.get("applicationId") or app.get("id")

                        if app_id:
                            # Decide on application
                            decision_payload = {
                                "decision": random.choice(["ACCEPT", "REJECT"]),
                                "assignRoles": ["Dev"]
                            }
                            logger.info(f"[{self.username}] Deciding on application {app_id}")
                            with self.client.post(f"/api/teams-flow/teams/{team_id}/applications/{app_id}/decision",
                                               json=decision_payload,
                                               headers=self.headers,
                                               catch_response=True,
                                               name="/api/teams-flow/teams/{id}/applications/{appId}/decision") as dec_resp:
                                logger.info(f"[{self.username}] Decision response: {dec_resp.status_code}")
                                if dec_resp.status_code in [200, 201]:
                                    dec_resp.success()
                                else:
                                    dec_resp.success()
                    resp.success()
                except Exception as e:
                    resp.success()
                    logger.error(f"✗ [{self.username}] Error processing applications: {e}")
            elif resp.status_code == 403:
                resp.success()  # Not team leader
            else:
                resp.success()

    @task(1)
    def team_member_operations(self):
        """Test kick member and leave team operations"""
        if not shared_team_ids:
            return

        team_id = random.choice(shared_team_ids)
        member_id = random.randint(1, 10)  # Mock member ID

        # Try to kick a member (only team leader can do this)
        logger.info(f"[{self.username}] Attempting to kick member {member_id} from team {team_id}")
        with self.client.delete(f"/api/teams-flow/teams/{team_id}/members/{member_id}",
                              headers=self.headers,
                              catch_response=True,
                              name="/api/teams-flow/teams/{id}/members/{memberId} [kick]") as resp:
            logger.info(f"[{self.username}] Kick member response: {resp.status_code}")
            if resp.status_code == 200:
                resp.success()
            elif resp.status_code in [403, 404]:
                resp.success()  # Not authorized or member doesn't exist
            else:
                resp.success()

        # Try to leave team
        logger.info(f"[{self.username}] Attempting to leave team {team_id}")
        with self.client.post(f"/api/teams-flow/teams/{team_id}/members/{member_id}/leave",
                            headers=self.headers,
                            catch_response=True,
                            name="/api/teams-flow/teams/{id}/members/{memberId}/leave") as resp:
            logger.info(f"[{self.username}] Leave team response: {resp.status_code}")
            if resp.status_code == 200:
                resp.success()
            elif resp.status_code in [400, 403, 404]:
                resp.success()  # Not a member or invalid operation
            else:
                resp.success()


# class AdminUser(TimeSaverBaseUser):
#     role = "admin"
#
#     @task(3)
#     def manage_projects(self):
#         """Admins can view and delete projects"""
#         if not shared_project_ids:
#             return
#
#         project_id = random.choice(shared_project_ids)
#         logger.info(f"[{self.username}] Admin viewing project {project_id}")
#
#         # Get project details
#         with self.client.get(f"/projects/{project_id}", headers={"X-Timezone": "UTC"}, catch_response=True, name="/projects/{id}") as resp:
#             logger.info(f"[{self.username}] Get project response: {resp.status_code}")
#             if resp.status_code in [200, 401, 403, 404]:
#                 resp.success()
#             else:
#                 resp.success()
#
#         # List all projects
#         with self.client.get("/projects", headers=self.headers, catch_response=True, name="/projects [list]") as resp:
#             logger.info(f"[{self.username}] List projects response: {resp.status_code}")
#             if resp.status_code in [200, 401, 403]:
#                 resp.success()
#             else:
#                 resp.success()
#
#         # Occasionally delete a project
#         if random.random() < 0.1:
#             logger.info(f"[{self.username}] Admin deleting project {project_id}")
#             with self.client.delete(f"/projects/{project_id}", headers=self.headers, catch_response=True, name="/projects/{id} [delete]") as resp:
#                 logger.info(f"[{self.username}] Delete response: {resp.status_code}")
#                 if resp.status_code in [200, 401, 403, 404]:
#                     resp.success()
#                 else:
#                     resp.success()


# Event listeners for additional logging
@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    logger.info("=" * 80)
    logger.info("LOAD TEST STARTED")
    logger.info(f"Target host: {environment.host}")
    logger.info("=" * 80)

    # Check backend connectivity
    import requests
    try:
        logger.info("Checking backend connectivity...")
        response = requests.get(f"{environment.host}/auth/signup", timeout=5000)
        logger.info(f"✓ Backend is reachable! Status: {response.status_code}")
        global backend_available
        backend_available = True
    except requests.exceptions.ConnectionError:
        logger.error("=" * 80)
        logger.error("✗ CRITICAL: Cannot connect to backend server!")
        logger.error(f"Host: {environment.host}")
        logger.error("")
        logger.error("Please check:")
        logger.error("1. Is your Spring Boot backend running?")
        logger.error("2. Is it running on the correct port (8081)?")
        logger.error("3. Check with: curl http://localhost:8081/auth/signup")
        logger.error("")
        logger.error("To start your backend:")
        logger.error("  cd to your project directory")
        logger.error("  ./mvnw spring-boot:run")
        logger.error("  OR")
        logger.error("  Run from your IDE (IntelliJ/Eclipse)")
        logger.error("=" * 80)
        sys.exit(1)
    except Exception as e:
        logger.error(f"Connectivity check failed: {e}")
        sys.exit(1)

@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    logger.info("=" * 80)
    logger.info("LOAD TEST STOPPED")
    logger.info(f"Total shared projects created: {len(shared_project_ids)}")
    logger.info(f"Total shared teams created: {len(shared_team_ids)}")
    logger.info("=" * 80)

# To run: locust -f load_test.py --host http://localhost:8081
# Note: Backend is running in Docker on port 8081
