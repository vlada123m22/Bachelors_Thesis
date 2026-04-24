# Quick Start - Backend on Port 8081 (Docker)

## Your Setup

✅ Backend running in Docker on port **8081**

## Run Load Test (3 Commands)

### 1. Check Backend
```bash
python check_backend.py http://localhost:8081
```
Should see: ✓ Backend is REACHABLE!

### 2. Run Load Test
```bash
locust -f load_test.py --host http://localhost:8081
```

### 3. Open Web UI
Open browser: **http://localhost:8089**

Configure:
- Number of users: **10-20**
- Spawn rate: **1-2**
- Host: **http://localhost:8081**

Click **"Start swarming"**

## OR Use the Automated Script

```bash
run_load_test.bat
```

This will:
1. Check if backend is running on 8081
2. Start the load test automatically
3. Point to the correct port

## Quick Verification

### Check if backend is on 8081:
```bash
curl http://localhost:8081/auth/signup/participant
```

Expected: Status 405 (Method Not Allowed) - this is good!

### Or use PowerShell:
```powershell
Invoke-WebRequest -Uri http://localhost:8081/auth/signup/participant
```

### Check Docker status:
```bash
docker ps | findstr timesaver
```

Should show your container running with `0.0.0.0:8081->8080/tcp`

## If Backend is Not Running

```bash
# Start with Docker Compose
docker-compose up

# Or start container directly
docker start <container-name>

# Check logs
docker logs <container-name>
```

## Expected Results

When working:
- ✓ All auth requests succeed (signup, login)
- ✓ Projects are created
- ✓ Participants apply successfully
- ✓ Teams are formed
- ✓ ~100% success rate in Locust UI

## Common Issues

### "Connection refused"
→ Docker container not running
→ Run: `docker-compose up` or `docker start <container>`

### "Wrong port"
→ Already fixed! Using 8081 everywhere now

### Locust shows 0% success
→ Backend probably on different port
→ Check: `docker ps` to see port mapping

## Files Updated for Port 8081

- ✅ `load_test.py` - Comments updated
- ✅ `run_load_test.bat` - Uses 8081
- ✅ This guide created

## Summary

Everything is now configured for **port 8081**. Just run:

```bash
run_load_test.bat
```

Or manually:
```bash
locust -f load_test.py --host http://localhost:8081
```

Then open http://localhost:8089 and start testing! 🚀
