"""
File: test_facial_health_model.py
Includes functionality for the Smart Factory backend.
"""
import sys
import os
import cv2
from ultralytics import YOLO

def main():
    if len(sys.argv) < 2:
        print("Usage: python test_facial_health_model.py <path_to_image>")
        sys.exit(1)
        
    image_path = sys.argv[1]
    if not os.path.exists(image_path):
        print(f"Error: File '{image_path}' not found.")
        sys.exit(1)
        
    model_path = os.path.join(os.path.dirname(__file__), "models", "facial_health_model.pt")
    if not os.path.exists(model_path):
        print(f"Error: Model '{model_path}' not found. Please train or copy the model first.")
        sys.exit(1)
        
    print(f"Loading model from {model_path}...")
    model = YOLO(model_path)
    
    print(f"Running inference on {image_path}...")
    results = model.predict(source=image_path, verbose=False)
    
    if len(results) == 0:
        print("No predictions generated.")
        sys.exit(1)
        
    probs = results[0].probs
    predicted_index = int(probs.top1)
    confidence = float(probs.top1conf)
    label = results[0].names[predicted_index]
    
    print("\n========================================")
    print("INFERENCE RESULTS:")
    print("========================================")
    print(f"Predicted Class : {label}")
    print(f"Confidence      : {confidence:.3f}")
    print("----------------------------------------")
    print("All Class Probabilities:")
    for idx, prob_val in enumerate(probs.data.tolist()):
        cls_name = results[0].names[idx]
        print(f"  {cls_name}: {prob_val:.4f}")
    print("========================================\n")

if __name__ == "__main__":
    main()
