"""
File: main.py
Includes functionality for the Smart Factory backend.
"""
#######################################################################
#   INDUSTRY 4.0 SMART GARMENT FACTORY - MAIN AI & BLOCKCHAIN SERVER   #
#---------------------------------------------------------------------#
# This file orchestrates the entire backend, merging FastAPI routes   #
# for YOLOv8 PPE detection, sensor anomaly detection, worker health   #
# analysis, and cryptographic Blockchain hashing for safety ledgers.  #
#######################################################################

from fastapi import FastAPI
from pydantic import BaseModel

from fire_risk import fire_risk
from overheating import predict_overheating
from gas_anomaly import detect_gas_anomaly
from overcurrent import detect_overcurrent
from ppe_detection import detect_ppe
from machine_maintenance import predict_machine_health
from machine_type_detection import detect_machine_type

app = FastAPI()


# PPE Request Model

class PPERequest(BaseModel):
    image_path: str



# Fire Risk Request Model

class FireRequest(BaseModel):
    temperature: float
    gas: float



# Overheating Request Model

class OverheatRequest(BaseModel):
    temperatures: list[float]



# Gas Anomaly Request Model

class GasRequest(BaseModel):
    gas: float



# Overcurrent Request Model

class CurrentRequest(BaseModel):
    current: float



# Home Route

@app.get("/")
def home():
    return {"message": "AI Service Running 🚀"}



# Fire Risk API

@app.post("/fire-risk")
def predict_fire(data: FireRequest):

    result = fire_risk(data.temperature, data.gas)

    return {
        "type": "FIRE",
        "risk_level": result,
        "temperature": data.temperature,
        "gas": data.gas
    }



# Overheating API

@app.post("/overheating")
def overheating(data: OverheatRequest):

    result = predict_overheating(data.temperatures)

    return {
        "predicted_temperature": result
    }



# Gas Anomaly API

@app.post("/gas-anomaly")
def gas_anomaly(data: GasRequest):

    result = detect_gas_anomaly(data.gas)

    return {
        "status": result
    }



# Predictive Maintenance Request Model

class MaintenanceRequest(BaseModel):
    temperature: float
    current: float
    vibration: float
    working_hours: float


# Overcurrent API (NEW)

@app.post("/overcurrent")
def overcurrent(data: CurrentRequest):

    result = detect_overcurrent(data.current)

    return {
        "status": result
    }


# Predictive Maintenance API (NEW)

@app.post("/predictive-maintenance")
def predictive_maintenance(data: MaintenanceRequest):

    # We use overheating model to predict base score, or you can adjust logic
    prediction = predict_overheating([data.temperature, data.current, data.vibration, data.working_hours])
    
    # Calculate health score based on prediction (example logic)
    health_score = max(0, min(100, int(100 - (prediction - 40))))
    status = "GOOD" if health_score > 70 else ("WARNING" if health_score > 40 else "CRITICAL")

    return {
        "health_score": health_score,
        "status": status
    }
import base64
import os


# PPE Detection API

@app.post("/ppe-detection")
def ppe_detection_api(data: PPERequest):
    img_path = data.image_path
    
    # If the image_path is actually a base64 encoded string from Android
    if len(img_path) > 200:
        try:
            # Remove data URI header if present e.g. "data:image/jpeg;base64,"
            if "," in img_path:
                img_path = img_path.split(",")[1]
            
            img_data = base64.b64decode(img_path)
            temp_path = "temp_ppe_image.jpg"
            with open(temp_path, "wb") as f:
                f.write(img_data)
            img_path = temp_path
        except Exception as e:
            return {"detected": [], "missing": ["all"], "status": "ERROR", "error": str(e)}

    # detect_ppe now returns a dict: {detected, violations, compliant, detections}
    result = detect_ppe(img_path)
    detected_names = result.get("detected", [])
    violations     = result.get("violations", [])

    # Map dataset class names → garment factory PPE items
    def has(keyword):
        return any(keyword.lower() in d.lower() for d in detected_names)

    missing = []
    
    # The user's specific dataset classes: 'with_mask', 'without_mask', 'mask_weared_incorrect'
    if has("without_mask") or has("mask_weared_incorrect"):
        missing.append("face-mask")
    elif not has("with_mask"):
        # If it detected absolutely nothing, assume missing
        missing.append("face-mask")

    status = "SAFE" if len(missing) == 0 else "WARNING"
    return {
        "detected": detected_names,
        "missing": missing,
        "status": status
    }

from worker_health import detect_health


# Worker Health Request Model

class HealthRequest(BaseModel):
    image_path: str


# Worker Health / Ergonomics API

@app.post("/worker-health")
def worker_health(data: HealthRequest):
    img_path = data.image_path
    
    # If the image_path is a base64 encoded string from Android
    if len(img_path) > 200:
        try:
            if "," in img_path:
                img_path = img_path.split(",")[1]
            img_data = base64.b64decode(img_path)
            temp_path = "temp_health_image.jpg"
            with open(temp_path, "wb") as f:
                f.write(img_data)
            img_path = temp_path
        except Exception as e:
            return {
                "posture": "ERROR",
                "fatigue": "Unknown",
                "status": "ERROR",
                "message": f"Base64 decode failed: {str(e)}"
            }

    result = detect_health(img_path)
    # Ensure all required keys exist for Android HealthPrediction model
    return {
        "posture": result.get("posture", "Unknown"),
        "fatigue": result.get("fatigue", "Unknown"),
        "status":  result.get("status", "UNKNOWN"),
        "stress":  result.get("stress", "Unknown"),
        "facial_state": result.get("facial_state", "Unknown")
    }


# ── Machine Type Detection API ───────────────────────────────────────────

class MachineTypeRequest(BaseModel):
    image_path: str  # File path or base64-encoded image

@app.post("/machine-type-detection")
def machine_type_detection(data: MachineTypeRequest):
    """
    Detect the sewing machine type from a camera image.
    Returns machine category, confidence score, and all detections.
    """
    result = detect_machine_type(data.image_path)
    return result


# ── Machine Health API (for AR Machine Assistant) ──────────────────────

class MachineHealthRequest(BaseModel):
    air_temperature:     float = 298.1   # Kelvin
    process_temperature: float = 308.6   # Kelvin
    rotational_speed:    float = 1551.0  # RPM
    torque:              float = 42.8    # Nm
    tool_wear:           float = 0.0     # minutes
    product_type:        str   = "M"     # L / M / H

@app.post("/machine-health")
def machine_health(data: MachineHealthRequest):
    result = predict_machine_health(
        air_temperature    = data.air_temperature,
        process_temperature= data.process_temperature,
        rotational_speed   = data.rotational_speed,
        torque             = data.torque,
        tool_wear          = data.tool_wear,
        product_type       = data.product_type
    )
    return result


import hashlib
import json
from time import time


# Blockchain Implementation

class Blockchain:
    def __init__(self):
        self.chain = []
        self.create_block(proof=1, previous_hash='0', report_data="Genesis Block")

    def create_block(self, proof, previous_hash, report_data):
        block = {
            'index': len(self.chain) + 1,
            'timestamp': time(),
            'report_data': report_data,
            'proof': proof,
            'previous_hash': previous_hash
        }
        self.chain.append(block)
        return block

    def get_previous_block(self):
        return self.chain[-1]

    def proof_of_work(self, previous_proof):
        new_proof = 1
        check_proof = False
        while not check_proof:
            hash_operation = hashlib.sha256(str(new_proof**2 - previous_proof**2).encode()).hexdigest()
            if hash_operation[:4] == '0000':
                check_proof = True
            else:
                new_proof += 1
        return new_proof

    def hash(self, block):
        encoded_block = json.dumps(block, sort_keys=True).encode()
        return hashlib.sha256(encoded_block).hexdigest()

    def is_chain_valid(self, chain):
        previous_block = chain[0]
        block_index = 1
        while block_index < len(chain):
            block = chain[block_index]
            if block['previous_hash'] != self.hash(previous_block):
                return False
            previous_proof = previous_block['proof']
            proof = block['proof']
            hash_operation = hashlib.sha256(str(proof**2 - previous_proof**2).encode()).hexdigest()
            if hash_operation[:4] != '0000':
                return False
            previous_block = block
            block_index += 1
        return True

blockchain = Blockchain()
attendance_blockchain = Blockchain() # Independent ledger for ESP32 QR scans

class ReportData(BaseModel):
    worker_id: str
    issue: str
    location: str
    timestamp: str

@app.post("/mine_block")
def mine_block(data: ReportData):
    previous_block = blockchain.get_previous_block()
    previous_proof = previous_block['proof']
    proof = blockchain.proof_of_work(previous_proof)
    previous_hash = blockchain.hash(previous_block)
    
    report_string = f"[{data.timestamp}] Worker: {data.worker_id} | Issue: {data.issue} | Location: {data.location}"
    
    block = blockchain.create_block(proof, previous_hash, report_string)
    
    return {
        'message': 'Congratulations, you just mined an emergency block!',
        'index': block['index'],
        'timestamp': block['timestamp'],
        'report_data': block['report_data'],
        'proof': block['proof'],
        'previous_hash': block['previous_hash']
    }

@app.get("/get_chain")
def get_chain():
    response = {
        'chain': blockchain.chain,
        'length': len(blockchain.chain),
        'is_valid': blockchain.is_chain_valid(blockchain.chain)
    }
    return response

# ── Attendance Blockchain API ──────────────────────────────────────────

class AttendanceData(BaseModel):
    worker_uid: str
    worker_name: str
    timestamp: str
    shift: str = "Morning"

@app.post("/mine_attendance_block")
def mine_attendance_block(data: AttendanceData):
    previous_block = attendance_blockchain.get_previous_block()
    previous_proof = previous_block['proof']
    proof = attendance_blockchain.proof_of_work(previous_proof)
    previous_hash = attendance_blockchain.hash(previous_block)
    
    report_string = f"[{data.timestamp}] Worker: {data.worker_name} ({data.worker_uid}) | Shift: {data.shift} | Status: SCANNED_IN"
    
    block = attendance_blockchain.create_block(proof, previous_hash, report_string)
    
    return {
        'message': 'Attendance securely logged on blockchain!',
        'index': block['index'],
        'timestamp': block['timestamp'],
        'report_data': block['report_data'],
        'proof': block['proof'],
        'previous_hash': block['previous_hash']
    }

@app.get("/get_attendance_chain")
def get_attendance_chain():
    response = {
        'chain': attendance_blockchain.chain,
        'length': len(attendance_blockchain.chain),
        'is_valid': attendance_blockchain.is_chain_valid(attendance_blockchain.chain)
    }
    return response


# ── ESP32-CAM QR Scanner & Stream Reader Integration ───────────────────
import threading
import time as time_mod
import cv2
import requests

# Store camera threads: {ip: stop_event}
active_camera_threads = {}
# Prevent double scanning within 10 seconds: {uid: timestamp}
last_scanned_timestamps = {}
# Global container for the latest frame read by the scanner thread
latest_frame = None

@app.on_event("startup")
def startup_event():
    """
    On backend startup, automatically try to connect and scan the camera
    at IP 192.168.43.144.
    """
    default_ip = "192.168.43.144"
    print(f"[Backend] Starting default camera scanner thread for IP: {default_ip}")
    
    stop_event = threading.Event()
    active_camera_threads[default_ip] = stop_event
    
    thread = threading.Thread(target=qr_scanner_thread, args=(default_ip, stop_event), daemon=True)
    thread.start()

import numpy as np

def qr_scanner_thread(cam_ip, stop_event):
    global latest_frame
    print(f"[QR Scanner] Thread started for camera: {cam_ip}")
    stream_url = f"http://{cam_ip}/stream"
    qr_detector = cv2.QRCodeDetector()
    
    while not stop_event.is_set():
        try:
            print(f"[QR Scanner] Connecting to stream at {stream_url}...")
            # Use requests to fetch the stream as a raw byte stream
            # CAMERA INTEGRATION: Connecting to ESP32 stream
            r = requests.get(stream_url, stream=True, timeout=8)
            if r.status_code != 200:
                print(f"[QR Scanner] Connection failed (HTTP {r.status_code}). Retrying in 3 seconds...")
                time_mod.sleep(3)
                continue
                
            print(f"[QR Scanner] Connected to stream successfully!")
            bytes_data = b""
            
            # Read streaming chunks continuously
            for chunk in r.iter_content(chunk_size=2048):
                if stop_event.is_set():
                    break
                bytes_data += chunk
                
                # Search for JPEG Start of Frame (0xFF, 0xD8) and End of Frame (0xFF, 0xD9)
                a = bytes_data.find(b'\xff\xd8')
                b = bytes_data.find(b'\xff\xd9')
                
                if a != -1 and b != -1 and b > a:
                    jpg = bytes_data[a:b+2]
                    bytes_data = bytes_data[b+2:]
                    
                    # Convert raw JPEG byte buffer back into an image matrix
                    frame = cv2.imdecode(np.frombuffer(jpg, dtype=np.uint8), cv2.IMREAD_COLOR)
                    if frame is not None:
                        latest_frame = frame.copy()
                        
                        # Run QR detection on the frame
                        data, bbox, _ = qr_detector.detectAndDecode(frame)
                        if data and data.startswith("WORKER_QR|"):
                            process_decoded_qr(data)
                            
        except Exception as e:
            print(f"[QR Scanner] Connection error/timeout: {e}. Reconnecting in 3 seconds...")
            time_mod.sleep(3)
            continue

def process_decoded_qr(raw_data):
    try:
        parts = raw_data.split("|")
        if len(parts) < 3:
            return
            
        uid = parts[1]
        name = parts[2]
        
        # Debounce: Prevent scanning the same card multiple times within 15 seconds
        current_time = time_mod.time()
        if current_time - last_scanned_timestamps.get(uid, 0) < 15:
            return
            
        last_scanned_timestamps[uid] = current_time
        print(f"[QR Scanner] Scanned worker: {name} (UID: {uid})")
        
        # Push scan to Firebase queue. The Android app will listen, write permanent log,
        # play a beep sound, mine it via our API endpoint, and clear this queue.
        firebase_host = "smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
        firebase_auth = "AIzaSyAMqFfoTPH_dOYCkkF_wy1qg5BWnEUFWGg"
        firebase_url = f"https://{firebase_host}/AttendanceScans.json?auth={firebase_auth}"
        
        payload = {
            "uid": uid,
            "name": name,
            "timestamp": int(current_time * 1000)
        }
        
        res = requests.post(firebase_url, json=payload)
        if res.status_code == 200:
            print("[QR Scanner] Logged successfully to Firebase.")
        else:
            print(f"[QR Scanner] Firebase error: {res.status_code} - {res.text}")
            
    except Exception as e:
        print(f"[QR Scanner] Error decoding payload: {e}")

@app.get("/register-cam")
def register_cam(ip: str):
    """
    Called by the ESP32-CAM to register its IP address.
    Launches a dedicated scanning thread.
    """
    print(f"[Backend] Registering camera at IP: {ip}")
    
    # Close any running thread for this IP
    if ip in active_camera_threads:
        print(f"[Backend] Stopping old scanning thread for {ip}")
        active_camera_threads[ip].set()
        time_mod.sleep(0.5)
        
    stop_event = threading.Event()
    active_camera_threads[ip] = stop_event
    
    thread = threading.Thread(target=qr_scanner_thread, args=(ip, stop_event), daemon=True)
    thread.start()
    
    return {"status": "SUCCESS", "message": f"Camera streaming active for IP: {ip}"}

from fastapi.responses import StreamingResponse

def generate_proxy_frames():
    global latest_frame
    while True:
        if latest_frame is not None:
            try:
                ret, jpeg = cv2.imencode('.jpg', latest_frame)
                if ret:
                    frame_bytes = jpeg.tobytes()
                    yield (b'--frame\r\n'
                           b'Content-Type: image/jpeg\r\n'
                           b'Content-Length: ' + str(len(frame_bytes)).encode() + b'\r\n\r\n' +
                           frame_bytes + b'\r\n')
            except Exception as e:
                print(f"[Stream Proxy] Error encoding frame: {e}")
        time_mod.sleep(0.07) # ~14 FPS matches the camera nicely and keeps CPU low

@app.get("/stream")
def stream_proxy():
    """
    Proxies the ESP32-CAM stream so multiple devices (like the Android App)
    can view it simultaneously without overloading the ESP32-CAM.
    """
    return StreamingResponse(generate_proxy_frames(), media_type="multipart/x-mixed-replace; boundary=frame")
