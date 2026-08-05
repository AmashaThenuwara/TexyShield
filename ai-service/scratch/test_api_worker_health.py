"""
File: test_api_worker_health.py
Includes functionality for the Smart Factory backend.
"""
import requests
import base64
import sys
import os

def test_api(image_path):
    url = "http://127.0.0.1:8000/worker-health"
    
    if not os.path.exists(image_path):
        print(f"Error: Image '{image_path}' not found.")
        return
        
    print(f"Reading image '{image_path}' and converting to Base64...")
    with open(image_path, "rb") as f:
        img_bytes = f.read()
        base64_str = base64.b64encode(img_bytes).decode("utf-8")
        
    payload = {
        "image_path": base64_str
    }
    
    print(f"Sending POST request to {url}...")
    try:
        r = requests.post(url, json=payload, timeout=15)
        print(f"Status Code: {r.status_code}")
        print("Response JSON:")
        print(r.json())
    except Exception as e:
        print(f"Error sending request: {e}")

if __name__ == "__main__":
    img = "facial_health_dataset/test/yawning/test_0.jpg"
    if len(sys.argv) > 1:
        img = sys.argv[1]
    test_api(img)
