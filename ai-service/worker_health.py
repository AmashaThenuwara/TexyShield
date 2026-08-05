"""
File: worker_health.py
Includes functionality for the Smart Factory backend.
"""
##########################################################################
# worker_health.py
# Ergonomics & Worker Health Detection
# Uses: 
#   1. melihuzunoglu/human-fall-detection (YOLOv11) -> Posture & Fatigue
#   2. GiteshUjgaonkar/yolov8-emotion-model (YOLOv8) -> Facial Stress Detection
#   3. MediaPipe -> Robust face cropping & fallback skeleton tracking
# Detects: Standing ✅ | Sitting ✅ | Fallen ✅ | Stress (Low/Moderate/High) ✅
##########################################################################

import os
import cv2
import shutil
from ultralytics import YOLO
from huggingface_hub import hf_hub_download

# ── Paths ─────────────────────────────────────────────────────────────
FALL_MODEL_PATH = os.path.join(os.path.dirname(__file__), "models", "fall_detection_yolo11.pt")
EMOTION_MODEL_PATH = os.path.join(os.path.dirname(__file__), "models", "emotion_model.onnx")

# ── Load Fall Detection Model ─────────────────────────────────────────
def _load_fall_model():
    if os.path.exists(FALL_MODEL_PATH):
        print("[INFO] Loading cached fall detection model from:", FALL_MODEL_PATH)
    else:
        print("[INFO] Downloading fall detection model from HuggingFace...")
        os.makedirs(os.path.dirname(FALL_MODEL_PATH), exist_ok=True)
        downloaded = hf_hub_download(
            repo_id="melihuzunoglu/human-fall-detection",
            filename="best.pt"
        )
        shutil.copy(downloaded, FALL_MODEL_PATH)
        print("[INFO] Model saved to:", FALL_MODEL_PATH)
    return YOLO(FALL_MODEL_PATH)

# ── Load Emotion Detection Model ──────────────────────────────────────
def _load_emotion_model():
    import onnxruntime as ort
    if os.path.exists(EMOTION_MODEL_PATH):
        print("[INFO] Loading custom ONNX emotion model from:", EMOTION_MODEL_PATH)
        return ort.InferenceSession(EMOTION_MODEL_PATH)
    else:
        raise FileNotFoundError(f"Emotion model not found at {EMOTION_MODEL_PATH}")

try:
    fall_model = _load_fall_model()
    FALL_MODEL_LOADED = True
    print("[INFO] Fall detection model ready ✅")
except Exception as e:
    fall_model = None
    FALL_MODEL_LOADED = False
    print(f"[WARNING] Could not load fall detection model: {e}")

try:
    emotion_model = _load_emotion_model()
    EMOTION_MODEL_LOADED = True
    print("[INFO] Emotion detection model ready ✅")
except Exception as e:
    emotion_model = None
    EMOTION_MODEL_LOADED = False
    print(f"[WARNING] Could not load emotion detection model: {e}")


def detect_health(image_path: str) -> dict:
    """
    Detect worker health/ergonomics and stress level from an image.

    Uses a pipeline approach:
      1. YOLOv11 + MediaPipe Pose fallback for highly robust human presence & posture detection.
      2. MediaPipe Face Detection to locate and crop the face.
      3. YOLOv8 Emotion Classifier to detect stress on the cropped face.
    """
    posture = "Unknown"
    fatigue = "NORMAL"
    stress = "LOW (Relaxed)"
    status = "HEALTHY"

    # Read image
    image = cv2.imread(image_path)
    if image is None:
        return {
            "posture": "ERROR - Could not load image",
            "fatigue": "Unknown",
            "stress": "Unknown",
            "status": "ERROR"
        }
    h, w, _ = image.shape

    # ── 1. Posture & Person Detection ──
    yolo_person_found = False
    if FALL_MODEL_LOADED and fall_model is not None:
        try:
            results = fall_model(image_path, conf=0.25, verbose=False)
            detections = []
            for result in results:
                for box in result.boxes:
                    class_id = int(box.cls[0])
                    confidence = float(box.conf[0])
                    class_name = fall_model.names[class_id].lower()
                    detections.append({"class": class_name, "conf": confidence})
            
            if detections:
                yolo_person_found = True
                top = max(detections, key=lambda d: d["conf"])
                label = top["class"]

                if "fall" in label or "fallen" in label:
                    posture = "FALLEN ⚠️"
                    fatigue = "HIGH (Fall Detected)"
                    status = "CRITICAL"
                elif "sit" in label or "sitting" in label:
                    posture = "SITTING"
                    fatigue = "NORMAL"
                elif "stand" in label or "standing" in label or "walk" in label:
                    posture = "STANDING"
                    fatigue = "NORMAL"
                else:
                    posture = label.upper()
        except Exception as e:
            print(f"[ERROR] YOLO Posture inference failed: {e}")

    # Fallback to MediaPipe skeleton tracking if YOLO missed the worker
    if not yolo_person_found or posture == "Unknown":
        try:
            import mediapipe as mp
            mp_pose = mp.solutions.pose
            pose = mp_pose.Pose(static_image_mode=True, min_detection_confidence=0.4)
            rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            pose_results = pose.process(rgb)

            if pose_results.pose_landmarks:
                landmarks = pose_results.pose_landmarks.landmark
                
                # Retrieve Y-coordinates for head, hips, and ankles to determine posture
                nose_y = landmarks[0].y
                hip_y = (landmarks[11].y + landmarks[12].y) / 2
                knee_y = (landmarks[13].y + landmarks[14].y) / 2
                ankle_y = (landmarks[15].y + landmarks[16].y) / 2

                height_diff = ankle_y - nose_y

                # Classification heuristics
                if height_diff < 0.35:  # Laying down / horizontal
                    posture = "FALLEN ⚠️"
                    fatigue = "HIGH (Lying Down)"
                    status = "CRITICAL"
                elif abs(hip_y - knee_y) < 0.12:  # Hips and knees at horizontal level -> Sitting
                    posture = "SITTING"
                    fatigue = "NORMAL"
                else:
                    posture = "STANDING"
                    fatigue = "NORMAL"
            else:
                posture = "No person detected"
                status = "WARNING"
        except Exception as e:
            print(f"[ERROR] MediaPipe posture fallback failed: {e}")
            if posture == "Unknown":
                posture = "No person detected"

    # ── 2. Face Cropping & Facial Expression (Stress) Analysis ──
    face_crop_path = "temp_face_crop.jpg"
    face_cropped = False
    try:
        cascade_path = os.path.join(os.path.dirname(__file__), 'models', 'haarcascade_frontalface_default.xml')
        face_cascade = cv2.CascadeClassifier(cascade_path)
        
        gray_img = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        # Detect faces
        faces = face_cascade.detectMultiScale(gray_img, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))
        
        if len(faces) > 0:
            # Get the largest face
            faces = sorted(faces, key=lambda f: f[2]*f[3], reverse=True)
            x, y, w_box, h_box = faces[0]
            
            pad_w = int(w_box * 0.15)
            pad_h = int(h_box * 0.15)
            
            x1 = max(0, x - pad_w)
            y1 = max(0, y - pad_h)
            x2 = min(w, x + w_box + pad_w)
            y2 = min(h, y + h_box + pad_h)
            
            if (x2 - x1) > 10 and (y2 - y1) > 10:
                face_crop = image[y1:y2, x1:x2]
                cv2.imwrite(face_crop_path, face_crop)
                face_cropped = True
                print("[AI] Face successfully detected and cropped using OpenCV.")
    except Exception as e:
        print(f"[ERROR] Face detection/cropping failed: {e}")

    # Run emotion classifier on the cropped face
    if EMOTION_MODEL_LOADED and emotion_model is not None:
        if face_cropped:
            try:
                import numpy as np
                emotion_classes = ['anger', 'disgust', 'fear', 'happy', 'neutral', 'sad', 'surprise']
                
                img = cv2.imread(face_crop_path)
                if img is not None:
                    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
                    resized = cv2.resize(gray, (48, 48))
                    normalized = resized / 255.0
                    input_tensor = np.expand_dims(np.expand_dims(normalized, -1), 0).astype(np.float32)
                    
                    input_name = emotion_model.get_inputs()[0].name
                    predictions = emotion_model.run(None, {input_name: input_tensor})[0]
                    
                    class_id = np.argmax(predictions[0])
                    confidence = float(predictions[0][class_id])
                    emotion_label = emotion_classes[class_id]
                    
                    print(f"[AI] Detected expression: {emotion_label} ({confidence:.2f})")
                    
                    # Map detected expression to stress levels
                    if emotion_label in ["anger", "fear", "sad", "disgust"]:
                        stress = "HIGH (Stressed) ⚠️"
                        status = "WARNING" if status != "CRITICAL" else "CRITICAL"
                    elif emotion_label in ["surprise"]:
                        stress = "MODERATE (Anxious)"
                        status = "WARNING" if status != "CRITICAL" else "CRITICAL"
                    else:
                        stress = "LOW (Relaxed)"
                else:
                    stress = "Unknown"
            except Exception as e:
                print(f"[ERROR] Emotion model inference failed: {e}")
                stress = "Unknown"
        else:
            print("[AI] No face detected, skipping emotion analysis.")
            stress = "Unknown"
            
    # Run facial health classification
    facial_state = "Unknown"
    if face_cropped:
        try:
            from facial_health_detection import load_model, analyze_facial_health
            fh_res = analyze_facial_health(face_crop_path)
            if fh_res.get("status") == "SUCCESS":
                facial_state = fh_res.get("state", "Unknown")
                print(f"[AI] Detected facial health state: {facial_state} ({fh_res.get('confidence')})")
                
                # Map detected state to fatigue levels & status
                if facial_state == "eyes_closed":
                    fatigue = "HIGH (Micro-sleep) ⚠️"
                    status = "REST_RECOMMENDED"
                elif facial_state == "yawning":
                    fatigue = "MODERATE (Yawning)"
                    if status != "CRITICAL" and status != "REST_RECOMMENDED":
                        status = "REST_RECOMMENDED"
                elif facial_state == "drowsy_appearance":
                    fatigue = "MODERATE (Sleepy)"
                    if status != "CRITICAL" and status != "REST_RECOMMENDED":
                        status = "REST_RECOMMENDED"
        except Exception as e:
            print(f"[ERROR] Facial health model inference failed: {e}")
    
    # Cleanup temp face crop file
    if os.path.exists(face_crop_path):
        try:
            os.remove(face_crop_path)
        except:
            pass

    if posture == "FALLEN ⚠️":
        status = "CRITICAL"

    return {
        "posture": posture,
        "fatigue": fatigue,
        "stress": stress,
        "status": status,
        "facial_state": facial_state
    }
