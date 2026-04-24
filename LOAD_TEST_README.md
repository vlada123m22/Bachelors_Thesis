# Load Test Guide

## Current Issue: Connection Error (Status Code 0)

**Status code 0** means the load test **cannot connect to your backend server**. This is not an authentication issue - it's a connectivity issue.

## Quick Fix Checklist

### 1. Is Your Backend Running?

Check if your Spring Boot application is running:

```bash
# Check if something is listening on port 8080
netstat -ano | findstr :8080

# Or on Linux/Mac
lsof -i :8080
```

If nothing is returned, **your backend is NOT running**.

### 2. Start Your Backend

**Option A: From Command Line**
```bash
cd C:\Univer\Teza_de_licenta\backend\timesaver
mvnw spring-boot:run
```

**Option B: From IntelliJ IDEA**
1. Open the project in IntelliJ
2. Find the main application class (usually `TimesaverApplication.java`)
3. Right-click → Run
4. Wait for "Started TimesaverApplication" in the console

**Option C: From JAR file**
```bash
java -jar target/timesaver-0.0.1-SNAPSHOT.jar
```

### 3. Verify Backend is Accessible

Test with curl or browser:

```bash
# This should return a 405 Method Not Allowed (which is good - it means the endpoint exists)
curl http://localhost:8080/auth/signup/participant

# Or test in browser:
http://localhost:8080/auth/signup/participant
```

If you get a connection error, your backend is not running.

### 4. Check Application Logs

When you start your Spring Boot app, you should see:

```
Tomcat started on port(s): 8080 (http)
Started TimesaverApplication in X.XXX seconds
```

If you see a different port (like 8081), update the load test command:

```bash
locust -f load_test.py --host http://localhost:8081
```

## Running the Load Test (After Backend is Running)

### Step 1: Ensure Backend is Running

You should see something like this in your backend console:
```
2026-04-24 13:00:00.000  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
```

### Step 2: Run the Load Test

```bash
# Make sure you're in the correct directory
cd C:\Univer\Teza_de_licenta\backend\timesaver

# Run locust
locust -f load_test.py --host http://localhost:8080
```

### Step 3: Open the Web UI

1. Open browser to: http://localhost:8089
2. Set parameters:
   - Number of users: 10-20 (start small)
   - Spawn rate: 1-2 users per second
   - Host: http://localhost:8080 (should be pre-filled)
3. Click "Start swarming"

## Common Issues and Solutions

### Issue 1: "Connection refused" or Status 0

**Cause**: Backend is not running

**Solution**: Start your Spring Boot backend (see step 2 above)

### Issue 2: "Port already in use"

**Cause**: Another process is using port 8080

**Solution**:
```bash
# Find the process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

Or configure Spring Boot to use a different port in `application.properties`:
```properties
server.port=8081
```

### Issue 3: Backend starts but immediately stops

**Cause**: Database connection issue or configuration error

**Solution**: Check your `application.properties` file and database settings

### Issue 4: "Locust not found"

**Cause**: Locust is not installed

**Solution**:
```bash
pip install locust requests
```

## Verifying Everything Works

### 1. Backend Health Check

```bash
# Test signup endpoint (should return 405 - Method Not Allowed)
curl -X GET http://localhost:8080/auth/signup/participant

# This confirms the server is running and routing works
```

### 2. Test Actual Signup (manual test)

```bash
curl -X POST http://localhost:8080/auth/signup/participant \
  -H "Content-Type: application/json" \
  -d '{"UserName":"testuser123","Password":"password123","Email":"test@example.com"}'
```

Expected response: HTTP 201 with user created message

### 3. Test Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"UserName":"testuser123","Password":"password123"}'
```

Expected response: HTTP 200 with JWT token

## Load Test Features

The load test now includes:

✅ **Automatic connectivity check** - Fails fast if backend is not reachable
✅ **Comprehensive logging** - Every request is logged with details
✅ **User roles**: Organizer, Participant, Admin
✅ **Full workflow**: Signup → Login → Create Projects → Apply → Manage Teams
✅ **No assignment endpoints** (as requested)

## Monitoring During Load Test

Watch your backend logs for:
- Request handling
- Database queries
- Any errors or exceptions

Watch Locust UI for:
- Request success rate (should be ~100%)
- Response times
- Requests per second

## Need Help?

1. **Check backend logs first** - most issues are backend-related
2. **Check load test logs** - shows exactly what's happening
3. **Start with 1 user** - easier to debug
4. **Increase gradually** - 1 → 5 → 10 → 20 users

## Expected Behavior

When working correctly:
- All users sign up successfully
- All users login and get tokens
- Organizers create projects
- Participants apply to projects
- Teams are created and managed
- ~100% success rate (some 404s are OK for missing data)

## Summary

The main issue is that **your backend is not running**. Start it, verify it's accessible, then run the load test. The load test itself is working correctly - it just can't connect to a server that isn't running!
