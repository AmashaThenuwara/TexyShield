"""
File: machine_type_detection.py
Includes functionality for the Smart Factory backend.
"""
##########################################################################
# machine_type_detection.py
# Machine Type Visual Detection using trained YOLOv8 model
# Model: SmartFactory_best.pt  (trained on 14 garment machine classes)
# Detects: Machine category from camera image / base64 photo
##########################################################################

import os
import base64
from ultralytics import YOLO

# ── Model path ────────────────────────────────────────────────────────────
MODELS_DIR = os.path.join(os.path.dirname(__file__), "models")
MODEL_PATH = os.path.join(MODELS_DIR, "machine_type_detection.pt")

# ── Class names (must match training order) ───────────────────────────────
CLASS_NAMES = [
    "2-needle lockstitch",
    "Automatic CNC pattern machines",
    "Bartack machine",
    "Blind stitch",
    "Button-sewing machine",
    "Buttonhole machine",
    "Cutter",
    "Feed-off-the-arm",
    "Flatlock Coverstitch",
    "Multi-needle chain stitch",
    "Overlock Serger",
    "Single-needle lockstitch",
    "Waistband PMD machines",
    "Zigzag stitching"
]

# ── Load model ────────────────────────────────────────────────────────────
try:
    machine_type_model = YOLO(MODEL_PATH)
    MODEL_LOADED = True
    print(f"[INFO] Machine type detection model loaded ✅ → {MODEL_PATH}")
except Exception as e:
    machine_type_model = None
    MODEL_LOADED = False
    print(f"[WARNING] Could not load machine type detection model: {e}")


def detect_machine_type(image_path: str) -> dict:
    """
    Detect the type of sewing machine in the given image.

    Args:
        image_path: Absolute file path OR base64-encoded image string.

    Returns:
        dict:
          - machine_type    : Detected class name (e.g. "Overlock Serger")
          - confidence      : Detection confidence 0–100 (%)
          - all_detections  : List of all detected objects with confidence
          - status          : "DETECTED" | "NOT_DETECTED" | "MODEL_UNAVAILABLE"
    """

    if not MODEL_LOADED or machine_type_model is None:
        return {
            "machine_type": "Unknown",
            "confidence": 0,
            "all_detections": [],
            "status": "MODEL_UNAVAILABLE"
        }

    # ── Decode base64 if needed ───────────────────────────────────────────
    temp_path = None
    if len(image_path) > 200 and not os.path.exists(image_path):
        try:
            if "," in image_path:
                image_path = image_path.split(",")[1]
            img_data = base64.b64decode(image_path)
            temp_path = os.path.join(os.path.dirname(__file__), "temp_machine_type.jpg")
            with open(temp_path, "wb") as f:
                f.write(img_data)
            image_path = temp_path
        except Exception as e:
            return {
                "machine_type": "Unknown",
                "confidence": 0,
                "all_detections": [],
                "status": f"BASE64_ERROR: {str(e)}"
            }

    # ── Run inference ─────────────────────────────────────────────────────
    try:
        results = machine_type_model.predict(
            source=image_path,
            conf=0.25,         # Minimum confidence threshold
            verbose=False
        )

        detections = []
        for result in results:
            for box in result.boxes:
                class_id   = int(box.cls[0])
                confidence = float(box.conf[0])
                label      = CLASS_NAMES[class_id] if class_id < len(CLASS_NAMES) else f"class_{class_id}"
                detections.append({
                    "machine_type": label,
                    "confidence":   round(confidence * 100, 1)
                })

        # Cleanup temp file
        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)

        if not detections:
            return {
                "machine_type": "Unknown",
                "confidence": 0,
                "all_detections": [],
                "status": "NOT_DETECTED"
            }

        # Return the highest-confidence detection as the primary result
        best = max(detections, key=lambda d: d["confidence"])
        return {
            "machine_type":   best["machine_type"],
            "confidence":     best["confidence"],
            "all_detections": detections,
            "status":         "DETECTED"
        }

    except Exception as e:
        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)
        return {
            "machine_type": "Unknown",
            "confidence": 0,
            "all_detections": [],
            "status": f"ERROR: {str(e)}"
        }
