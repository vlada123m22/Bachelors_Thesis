#!/usr/bin/env python3
"""
Quick script to check if the backend is running and accessible
"""

import sys
import requests

def check_backend(host="http://localhost:8080"):
    print("=" * 60)
    print("Backend Connectivity Check")
    print("=" * 60)
    print(f"\nChecking: {host}")
    print("-" * 60)

    try:
        # Try to connect to a known endpoint (use POST to login as a connectivity check)
        response = requests.post(
            f"{host}/auth/login",
            json={"UserName": "connectivity_test", "Password": "test"},
            timeout=10
        )

        print("OK Backend is REACHABLE!")
        print(f"  Status Code: {response.status_code}")
        print(f"  Response: {response.text[:100] if response.text else 'No content'}")

        if response.status_code in [200, 401, 403]:
            print("\nOK Perfect! Backend is responding correctly.")
            print(f"  Status {response.status_code} = Backend is alive and processing requests")

        print("\n" + "=" * 60)
        print("OK BACKEND IS READY FOR LOAD TESTING!")
        print("=" * 60)
        print("\nYou can now run:")
        print(f"  locust -f load_test.py --host {host}")
        print("\nThen open: http://localhost:8089")
        return True

    except requests.exceptions.ConnectionError:
        print("X Backend is NOT REACHABLE!")
        print("\nConnection refused. This means:")
        print("  1. Backend Docker container is not running, OR")
        print("  2. Backend is running on a different port")
        print("\n" + "-" * 60)
        print("To start your backend:")
        print("  Option 1: docker-compose up")
        print("  Option 2: docker start platform_backend")
        print("\nTo check backend container:")
        print("  docker ps | findstr platform_backend")
        print("=" * 60)
        return False

    except requests.exceptions.Timeout:
        print("X Backend connection TIMEOUT!")
        print("  Backend might be starting up or experiencing issues.")
        print("  Wait a moment and try again.")
        return False

    except Exception as e:
        print(f"X Unexpected error: {e}")
        return False

if __name__ == "__main__":
    host = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

    success = check_backend(host)
    sys.exit(0 if success else 1)
