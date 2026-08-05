"""
File: run_backend.py
Includes functionality for the Smart Factory backend.
"""
import os
import socket
import subprocess
import sys

def get_local_ip():
    """
    Finds the active local IPv4 address of the computer.
    """
    try:
        # Create a dummy socket connection to find the active network interface IP
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        # Doesn't need to be reachable, just triggers routing table query
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except Exception:
        # Fallback if offline
        try:
            return socket.gethostbyname(socket.gethostname())
        except Exception:
            return "127.0.0.1"

def update_android_base_url(ip_address):
    """
    Locates RetrofitClient.kt and auto-updates BASE_URL with the new IP address.
    """
    # Relative path from 'ai-service/' to 'SmartFactory/app/src/main/java/.../RetrofitClient.kt'
    current_dir = os.path.dirname(os.path.abspath(__file__))
    retrofit_file = os.path.abspath(os.path.join(
        current_dir, 
        "..", 
        "SmartFactory", 
        "app", 
        "src", 
        "main", 
        "java", 
        "com", 
        "example", 
        "smartfactory", 
        "api", 
        "RetrofitClient.kt"
    ))
    
    if not os.path.exists(retrofit_file):
        print(f"[WARNING] RetrofitClient.kt not found at expected path: {retrofit_file}")
        return False
        
    print(f"[INFO] Found RetrofitClient.kt at: {retrofit_file}")
    
    try:
        with open(retrofit_file, "r", encoding="utf-8") as f:
            lines = f.readlines()
            
        updated = False
        for i, line in enumerate(lines):
            if "const val BASE_URL =" in line:
                new_line = f'    const val BASE_URL = "http://{ip_address}:8000/"  // ← Auto-updated by run_backend.py\n'
                if line != new_line:
                    lines[i] = new_line
                    updated = True
                break
                
        if updated:
            with open(retrofit_file, "w", encoding="utf-8") as f:
                f.writelines(lines)
            print(f"[SUCCESS] Auto-updated Android Retrofit BASE_URL to: http://{ip_address}:8000/")
        else:
            print(f"[INFO] Android Retrofit BASE_URL is already up to date: http://{ip_address}:8000/")
        return True
    except Exception as e:
        print(f"[ERROR] Failed to update RetrofitClient.kt: {e}")
        return False

def main():
    print("========================================")
    print("SMART FACTORY BACKEND AUTOMATION LAUNCHER")
    print("========================================")
    
    # 1. Detect current local IP
    ip = get_local_ip()
    print(f"[INFO] Detected local IP address: {ip}")
    
    # 2. Update Android configuration automatically
    update_android_base_url(ip)
    
    # 3. Inform user about ADB reverse utility option
    print("\n[TIP] Testing via USB? You can run: 'adb reverse tcp:8000 tcp:8000'")
    print("      This redirects phone's localhost:8000 traffic directly to this PC.\n")
    
    # 4. Launch FastAPI server binding to all interfaces
    print(f"Launching Uvicorn server on http://0.0.0.0:8000 (accessible locally at http://{ip}:8000)...")
    
    # Run Uvicorn command
    cmd = [
        sys.executable, "-m", "uvicorn", "main:app", 
        "--host", "0.0.0.0", 
        "--port", "8000", 
        "--reload"
    ]
    
    try:
        subprocess.run(cmd, check=True)
    except KeyboardInterrupt:
        print("\n[INFO] Backend server stopped by user.")
    except Exception as e:
        print(f"\n[ERROR] Failed to start Uvicorn: {e}")

if __name__ == "__main__":
    main()
