"""
File: ppe_detection.py
Includes functionality for the Smart Factory backend.
"""
import os
from ultralytics import YOLO

# Load the custom Garment PPE model (after you train it or download weights from Roboflow)
model_path = "models/garment_ppe.pt"

if not os.path.exists(model_path):
    print(f"[WARNING] Garment PPE model not found at '{model_path}'. Waiting for training...")
    # Fallback to generic yolov8n just so the server doesn't crash before training
    model_path = "yolov8n.pt"

model = YOLO(model_path)

# Update to your Roboflow Garment PPE Classes (Adjust these to match your exact dataset classes)
PPE_CLASSES = [
    'face-mask', 'no-face-mask', 
    'hair-net', 'no-hair-net', 
    'head-cover', 'no-head-cover',
    'person'
]

# Classes that indicate a PPE violation in a garment factory
VIOLATION_CLASSES = {'no-face-mask', 'no-hair-net', 'no-head-cover'}

def detect_ppe(image_path, confidence_threshold=0.3):
    """
    Detect Garment PPE compliance in an image.
    """
    results = model(image_path, conf=confidence_threshold)

    detected = []
    detections = []

    for result in results:
        for box in result.boxes:
            class_id = int(box.cls[0])
            confidence = float(box.conf[0])
            name = model.names[class_id]
            bbox = box.xyxy[0].tolist()

            detected.append(name)
            detections.append({
                "class": name,
                "confidence": round(confidence, 3),
                "bbox": [round(v, 1) for v in bbox]
            })

    violations = [cls for cls in detected if cls in VIOLATION_CLASSES]
    compliant = len(violations) == 0

    return {
        "detected": detected,
        "violations": violations,
        "compliant": compliant,
        "detections": detections
    }
