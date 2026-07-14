import os
from ultralytics import YOLO

# Load YOLO model (fallback to general model if custom PPE model is not yet trained)
model_path = "models/ppe_yolov8.pt"
if not os.path.exists(model_path):
    model_path = "yolov8n.pt" # Ultralytics will auto-download this

model = YOLO(model_path)

def detect_ppe(image_path):
    results = model(image_path)
    detected = []
    
    for result in results:
        for box in result.boxes:
            class_id = int(box.cls[0])
            name = model.names[class_id]
            detected.append(name)
            
    return detected
