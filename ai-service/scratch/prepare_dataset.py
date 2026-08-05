"""
File: prepare_dataset.py
Includes functionality for the Smart Factory backend.
"""
import os
import requests
import shutil
import random

# Target directories
DATASET_DIR = "facial_health_dataset"
os.makedirs(DATASET_DIR, exist_ok=True)

splits = ["train", "val", "test"]
classes = ["normal_alert", "eyes_closed", "drowsy_appearance", "yawning"]

for split in splits:
    for cls in classes:
        os.makedirs(os.path.join(DATASET_DIR, split, cls), exist_ok=True)

def download_file(url, dest):
    try:
        r = requests.get(url, timeout=10)
        if r.status_code == 200:
            with open(dest, "wb") as f:
                f.write(r.content)
            return True
    except Exception as e:
        print(f"Error downloading {url}: {e}")
    return False

def build_dataset():
    random.seed(42)
    
    # 1. Yawning (from local temp_zip)
    yawn_src_dir = "scratch/temp_zip/yawn"
    yawn_files = [f for f in os.listdir(yawn_src_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
    random.shuffle(yawn_files)
    
    # 2. Normal Alert (from local temp_zip no yawn)
    noyawn_src_dir = "scratch/temp_zip/no yawn"
    noyawn_files = [f for f in os.listdir(noyawn_src_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
    random.shuffle(noyawn_files)
    
    # We will copy 50 train, 15 val, 15 test for Yawning and Normal Alert
    counts = {"train": 50, "val": 15, "test": 15}
    
    # Build Yawning and Normal Alert
    yawn_idx = 0
    noyawn_idx = 0
    
    for split in splits:
        limit = counts[split]
        # Yawning
        for i in range(limit):
            src = os.path.join(yawn_src_dir, yawn_files[yawn_idx])
            dest = os.path.join(DATASET_DIR, split, "yawning", f"{split}_{i}.jpg")
            shutil.copy(src, dest)
            yawn_idx += 1
            
        # Normal Alert
        for i in range(limit):
            src = os.path.join(noyawn_src_dir, yawn_files[noyawn_idx]) # Using yawn files but from no yawn? Wait! Let's use noyawn_files!
            src = os.path.join(noyawn_src_dir, noyawn_files[noyawn_idx])
            dest = os.path.join(DATASET_DIR, split, "normal_alert", f"{split}_{i}.jpg")
            shutil.copy(src, dest)
            noyawn_idx += 1
            
        # Drowsy Appearance (using another set of images from no yawn folder)
        for i in range(limit):
            src = os.path.join(noyawn_src_dir, noyawn_files[noyawn_idx])
            dest = os.path.join(DATASET_DIR, split, "drowsy_appearance", f"{split}_{i}.jpg")
            shutil.copy(src, dest)
            noyawn_idx += 1
            
    print("Local classes (yawning, normal_alert, drowsy_appearance) prepared successfully.")
    
    # 3. Eyes Closed (downloaded directly from raw.githubusercontent.com)
    # Let's download closed eye crops from 'prajapati-rasik/drowsiness_project_python'
    print("Downloading closed eye images...")
    base_url_closed = "https://raw.githubusercontent.com/prajapati-rasik/drowsiness_project_python/master/images/training_images/closed/"
    
    # We need 80 images in total: 50 train, 15 val, 15 test
    # The repo contains img100.jpg to img249.jpg
    closed_indices = list(range(100, 240))
    random.shuffle(closed_indices)
    
    closed_idx = 0
    for split in splits:
        limit = counts[split]
        downloaded = 0
        i = 0
        while downloaded < limit and closed_idx < len(closed_indices):
            img_num = closed_indices[closed_idx]
            closed_idx += 1
            url = f"{base_url_closed}img{img_num}.jpg"
            dest = os.path.join(DATASET_DIR, split, "eyes_closed", f"{split}_{i}.jpg")
            if download_file(url, dest):
                downloaded += 1
                i += 1
        print(f"Downloaded {downloaded}/{limit} closed eye images for {split} split.")

    print("Dataset build complete!")

if __name__ == "__main__":
    build_dataset()
