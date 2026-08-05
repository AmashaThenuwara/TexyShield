"""
File: facial_health_detection.py
Includes functionality for the Smart Factory backend.
"""
import os
import cv2
from ultralytics import YOLO

MODEL_PATH = os.path.join(os.path.dirname(__file__), "models", "facial_health_model.pt")
model = None

def load_model():
    global model
    if model is None:
        if os.path.exists(MODEL_PATH):
            print(f"[INFO] Loading Facial Health Model from {MODEL_PATH}...")
            model = YOLO(MODEL_PATH)
        else:
            print(f"[WARNING] Facial Health Model not found at '{MODEL_PATH}'.")
    return model

def analyze_facial_health(image_source) -> dict:
    """
    Run inference using the facial health indicator model.
    
    Args:
        image_source: path to the face crop image (str) or a numpy BGR frame.
        
    Returns:
        dict containing predicted state, confidence, and all class probabilities.
    """
    cls_model = load_model()
    if cls_model is None:
        return {
            "state": "Unknown",
            "confidence": 0.0,
            "probabilities": {},
            "status": "MODEL_UNAVAILABLE"
        }
        
    try:
        results = cls_model.predict(source=image_source, verbose=False)
        if not results:
            return {
                "state": "Unknown",
                "confidence": 0.0,
                "probabilities": {},
                "status": "NO_PREDICTION"
            }
            
        probs = results[0].probs
        predicted_index = int(probs.top1)
        confidence = float(probs.top1conf)
        label = results[0].names[predicted_index]
        
        prob_dict = {}
        for idx, val in enumerate(probs.data.tolist()):
            cls_name = results[0].names[idx]
            prob_dict[cls_name] = round(val, 4)
            
        return {
            "state": label,
            "confidence": round(confidence, 3),
            "probabilities": prob_dict,
            "status": "SUCCESS"
        }
    except Exception as e:
        print(f"[ERROR] Facial health model inference error: {e}")
        return {
            "state": "Unknown",
            "confidence": 0.0,
            "probabilities": {},
            "status": f"ERROR: {str(e)}"
        }
