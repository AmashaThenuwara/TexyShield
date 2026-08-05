"""
File: extract_dataset.py
Includes functionality for the Smart Factory backend.
"""
import requests
import zipfile
import os

def main():
    zip_url = "https://raw.githubusercontent.com/vaishali-2003kamble/yawning-dataset/main/archive%20(5).zip"
    dest_dir = "scratch/temp_zip"
    os.makedirs(dest_dir, exist_ok=True)
    zip_path = os.path.join(dest_dir, "dataset.zip")
    
    print(f"Downloading dataset from {zip_url}...")
    r = # CAMERA INTEGRATION: Connecting to ESP32 stream
requests.get(zip_url, stream=True, timeout=30)
    if r.status_code == 200:
        with open(zip_path, "wb") as f:
            for chunk in r.iter_content(chunk_size=8192):
                f.write(chunk)
        print("Download complete. Extracting...")
        
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(dest_dir)
        print("Extraction complete!")
        
        # List top level contents of extracted zip
        print("Extracted directories:")
        for root, dirs, files in os.walk(dest_dir):
            if root == dest_dir:
                print("Root Dirs:", dirs)
                for d in dirs:
                    sub_dirs = os.listdir(os.path.join(root, d))
                    print(f"  {d} contains: {sub_dirs[:10]}")
    else:
        print("Failed to download, status:", r.status_code)

if __name__ == "__main__":
    main()
