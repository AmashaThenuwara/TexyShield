"""
File: cctv_monitor.py
Includes functionality for the Smart Factory backend.
"""
import cv2
import os
from ultralytics import YOLO

# ── CAMERA CONFIGURATION ─────────────────────────────────────────────
# To use your phone camera, install the "IP Webcam" app (Android).
# Open the app, click "Start server", and put the IPv4 address below.
# Example: CAMERA_SOURCE = "http://192.168.1.100:8080/video"
# For default laptop webcam, leave it as 0.
CAMERA_SOURCE = 0
# ─────────────────────────────────────────────────────────────────────

def main():
    print("Loading AI Model...")
    # Load YOLO model (same logic as ppe_detection.py)
    model_path = "models/ppe_yolov8.pt"
    if not os.path.exists(model_path):
        model_path = "yolov8n.pt" # Ultralytics will auto-download this if missing
    
    model = YOLO(model_path)
    
    print(f"Initializing Camera (Source: {CAMERA_SOURCE})...")
    cap = # CAMERA INTEGRATION: Capturing video stream from device
cv2.VideoCapture(CAMERA_SOURCE)
    
    if not cap.isOpened():
        print("Error: Could not open webcam.")
        return

    print("====================================")
    print("CCTV Feed Running. Press 'q' to quit.")
    print("====================================")

    while True:
        ret, frame = cap.read()
        if not ret:
            print("Failed to grab frame.")
            break
            
        # Run YOLO inference on the frame
        # stream=True is faster for video feeds
        results = model(frame, verbose=False)
        
        # Plot the bounding boxes on the frame
        annotated_frame = results[0].plot()
        
        # Display the live video feed
        cv2.imshow("Smart Factory CCTV Monitor", annotated_frame)
        
        # Wait for 1 ms and check if the user pressed 'q'
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
            
    # Cleanup
    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
