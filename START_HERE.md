# START HERE - Load Test Quick Start

## The Problem

Your load test is showing **Status Code 0**, which means:

**🔴 YOUR BACKEND IS NOT RUNNING 🔴**

Status code 0 = Connection refused = Backend server is not accessible

## The Solution (3 Steps)

### Step 1: Start Your Backend ✅

Choose ONE method:

#### Method A: Command Line (Recommended)
```bash
cd C:\Univer\Teza_de_licenta\backend\timesaver
mvnw.cmd spring-boot:run
```

#### Method B: IntelliJ IDEA
1. Open project in IntelliJ
2. Find `TimesaverApplication.java` (main class)
3. Right-click → Run 'TimesaverApplication'

#### Method C: JAR file
```bash
cd C:\Univer\Teza_de_licenta\backend\timesaver
java -jar target\timesaver-0.0.1-SNAPSHOT.jar
```

**Wait for this message in console:**
```
Tomcat started on port(s): 8080 (http)
Started TimesaverApplication
```

### Step 2: Verify Backend is Running ✅

```bash
# Run the checker script
python check_backend.py

# OR manually test
curl http://localhost:8080/auth/signup/participant
```

You should see:
- ✓ "Backend is REACHABLE!"
- Status 405 (Method Not Allowed) - this is GOOD!

### Step 3: Run Load Test ✅

```bash
locust -f load_test.py --host http://localhost:8080
```

Then open: **http://localhost:8089**

Configure:
- Users: 10
- Spawn rate: 2
- Host: http://localhost:8080

Click **"Start swarming"**

## Quick Verification Commands

```bash
# 1. Check if backend is running
netstat -ano | findstr :8080

# 2. Test connectivity
python check_backend.py

# 3. Run load test
locust -f load_test.py --host http://localhost:8080
```

## What You'll See When It Works

### Backend Console:
```
2026-04-24 13:00:00.000  INFO ... Tomcat started on port(s): 8080 (http)
2026-04-24 13:00:00.000  INFO ... Started TimesaverApplication in 5.123 seconds
```

### Load Test Console:
```
✓ Backend is reachable! Status: 405
LOAD TEST STARTED
✓ Signup successful for organizer_abc123
✓ Login successful for organizer_abc123
✓ Project created: ID=1
```

### Locust Web UI:
- Success rate: ~100%
- All requests showing 200/201 status codes
- Green charts showing activity

## Troubleshooting

### "Connection refused" or Status 0
→ **Backend is not running!** Go to Step 1

### "Port already in use"
```bash
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace 1234 with actual PID)
taskkill /PID 1234 /F
```

### "Locust not found"
```bash
pip install locust requests
```

### Backend won't start
- Check `application.properties` for database configuration
- Ensure database is running (PostgreSQL/MySQL)
- Check for error messages in console

## File Reference

- `load_test.py` - Main load test script with comprehensive logging
- `check_backend.py` - Quick backend connectivity checker
- `LOAD_TEST_README.md` - Detailed documentation
- `LOAD_TEST_FIXES.md` - Technical details about fixes

## Expected Results

When everything works:
- ✅ All users sign up successfully
- ✅ All users login and receive JWT tokens
- ✅ Organizers create projects
- ✅ Participants apply to projects
- ✅ Teams are created and managed
- ✅ ~100% success rate (some 404s are normal)

## Still Having Issues?

1. **Read backend logs** - Most issues are backend-related
2. **Check database** - Ensure it's running and accessible
3. **Test manually** - Use curl/Postman to verify endpoints work
4. **Start with 1 user** - Easier to debug than 10-20 users

## Summary

**The load test script is working perfectly!** It just can't connect to a server that isn't running.

1. ✅ Start your backend
2. ✅ Verify it's accessible (check_backend.py)
3. ✅ Run the load test
4. 🎉 Watch the requests succeed!
