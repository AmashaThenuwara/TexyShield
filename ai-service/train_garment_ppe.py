"""
File: train_garment_ppe.py
Includes functionality for the Smart Factory backend.
"""
from ultralytics import YOLO

# 1. Load a pre-trained base YOLOv8 model
model = YOLO('yolov8n.pt') 

# 2. Train the model on your new Roboflow Garment PPE Dataset
# Make sure your dataset is in "ai-service/datasets/garment_ppe"
# and contains a data.yaml file!
results = model.train(
    data='datasets/garment_ppe/data.yaml',
    epochs=50,       # Number of training epochs
    imgsz=640,       # Image size
    batch=16,        # Batch size
    name='garment_ppe_model' # Name of the saved model
)

print("Training complete! Your new model is saved in: runs/detect/garment_ppe_model/weights/best.pt")
