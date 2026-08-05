"""
File: test_local.py
Includes functionality for the Smart Factory backend.
"""
import cv2
import json
from worker_health import detect_health

if __name__ == "__main__":
    result = detect_health("temp_health_image.jpg")
    print("\nFINAL RESULT:", json.dumps(result, indent=2))
