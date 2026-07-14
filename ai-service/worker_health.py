import cv2
import mediapipe as mp

pose = mp.solutions.pose.Pose()

def detect_health(image_path):
    image = cv2.imread(image_path)
    if image is None:
        return {"status": "ERROR", "message": "Failed to load image"}

    rgb = cv2.cvtColor(
        image,
        cv2.COLOR_BGR2RGB
    )

    result = pose.process(rgb)

    if result.pose_landmarks:
        return {
            "posture": "SAFE",
            "fatigue": "NORMAL",
            "status": "HEALTHY"
        }
    else:
        return {
            "posture": "FALL DETECTED",
            "fatigue": "HIGH",
            "status": "WARNING"
        }
