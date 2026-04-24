# Run Load Test NOW - Your Backend is on Port 8081

## Your backend IS running in Docker on port 8081 ✅

## Run the Load Test

```bash
locust -f load_test.py --host http://localhost:8081
```

Then open: **http://localhost:8089**

## Configure in Web UI

- **Number of users**: 10
- **Spawn rate**: 2
- **Host**: `http://localhost:8081` (should be auto-filled)

Click **"Start swarming"**

## That's It!

The load test will now:
1. Create organizer users and log them in
2. Create projects
3. Create participant users
4. Apply to projects
5. Create and manage teams
6. Test all endpoints

## Watch the Results

In the Locust UI you'll see:
- Request statistics
- Success/failure rates
- Response times
- Charts

All your endpoints will be tested with proper authentication!

## Files Already Updated for Port 8081

✅ `load_test.py` - Updated
✅ `run_load_test.bat` - Updated
✅ All documentation - Updated

Just run: `locust -f load_test.py --host http://localhost:8081`
