"""
File: search_github_images.py
Includes functionality for the Smart Factory backend.
"""
import requests

def main():
    url = "https://huggingface.co/api/datasets/ckcl/driver-safety-dataset"
    r = requests.get(url, timeout=10)
    if r.status_code == 200:
        siblings = r.json().get("siblings", [])
        for sib in siblings[:15]:
            print(sib['rfilename'])
    else:
        print("Failed to fetch, status:", r.status_code)

if __name__ == "__main__":
    main()
