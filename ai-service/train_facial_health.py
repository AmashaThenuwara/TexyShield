"""
File: train_facial_health.py
Includes functionality for the Smart Factory backend.
"""
import os
from ultralytics import YOLO

def main():
    dataset_dir = "facial_health_dataset"
    print("========================================")
    print("STARTING FACIAL HEALTH MODEL TRAINING")
    print("========================================")
    
    # 1. Load pretrained YOLOv8 classification model
    print("Loading pretrained weights (yolov8n-cls.pt)...")
    model = YOLO("yolov8n-cls.pt")
    
    # 2. Train model on the dataset
    # We run 30 epochs on CPU
    epochs = 30
    imgsz = 224
    batch = 8
    
    print(f"Training on dataset '{dataset_dir}' for {epochs} epochs, imgsz={imgsz}, batch={batch}...")
    
    results = model.train(
        data=dataset_dir,
        epochs=epochs,
        imgsz=imgsz,
        batch=batch,
        device="cpu", # Force CPU as CUDA is not available
        workers=2,
        name="facial_health_model"
    )
    
    print("\nTraining complete! 🚀")
    
    # Locate best.pt weight file
    # Default runs path: runs/classify/facial_health_model/weights/best.pt
    best_weights_path = "runs/classify/facial_health_model/weights/best.pt"
    
    if os.path.exists(best_weights_path):
        # 3. Export/copy the best model to models/facial_health_model.pt
        os.makedirs("models", exist_ok=True)
        dest_path = "models/facial_health_model.pt"
        shutil_copy(best_weights_path, dest_path)
        print(f"Saved the best model to {dest_path} ✅")
    else:
        print(f"[WARNING] Could not locate best weights at '{best_weights_path}'")

def shutil_copy(src, dest):
    import shutil
    try:
        shutil.copy(src, dest)
        print(f"Copied {src} to {dest}")
    except Exception as e:
        print(f"Error copying weights: {e}")

if __name__ == "__main__":
    main()
