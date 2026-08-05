"""
File: check_dataset_counts.py
Includes functionality for the Smart Factory backend.
"""
import os

def main():
    dataset_dir = "facial_health_dataset"
    if not os.path.exists(dataset_dir):
        print("Dataset directory not found!")
        return
        
    for split in ["train", "val", "test"]:
        print(f"Split: {split}")
        split_dir = os.path.join(dataset_dir, split)
        if os.path.exists(split_dir):
            for cls in sorted(os.listdir(split_dir)):
                cls_dir = os.path.join(split_dir, cls)
                if os.path.isdir(cls_dir):
                    files = os.listdir(cls_dir)
                    print(f"  {cls}: {len(files)} files")
        else:
            print("  Split dir not found!")

if __name__ == "__main__":
    main()
