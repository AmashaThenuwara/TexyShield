from fastapi import FastAPI
from pydantic import BaseModel

from fire_risk import fire_risk
from overheating import predict_overheating
from gas_anomaly import detect_gas_anomaly
from overcurrent import detect_overcurrent 
from ppe_detection import detect_ppe

app = FastAPI()

# -------------------------
# PPE Request Model
# -------------------------
class PPERequest(BaseModel):
    image_path: str


# -------------------------
# Fire Risk Request Model
# -------------------------
class FireRequest(BaseModel):
    temperature: float
    gas: float


# -------------------------
# Overheating Request Model
# -------------------------
class OverheatRequest(BaseModel):
    temperatures: list[float]


# -------------------------
# Gas Anomaly Request Model
# -------------------------
class GasRequest(BaseModel):
    gas: float


# -------------------------
# Overcurrent Request Model
# -------------------------
class CurrentRequest(BaseModel):
    current: float


# -------------------------
# Home Route
# -------------------------
@app.get("/")
def home():
    return {"message": "AI Service Running 🚀"}


# -------------------------
# Fire Risk API
# -------------------------
@app.post("/fire-risk")
def predict_fire(data: FireRequest):

    result = fire_risk(data.temperature, data.gas)

    return {
        "type": "FIRE",
        "risk_level": result,
        "temperature": data.temperature,
        "gas": data.gas
    }


# -------------------------
# Overheating API
# -------------------------
@app.post("/overheating")
def overheating(data: OverheatRequest):

    result = predict_overheating(data.temperatures)

    return {
        "predicted_temperature": result
    }


# -------------------------
# Gas Anomaly API
# -------------------------
@app.post("/gas-anomaly")
def gas_anomaly(data: GasRequest):

    result = detect_gas_anomaly(data.gas)

    return {
        "status": result
    }


# -------------------------
# Predictive Maintenance Request Model
# -------------------------
class MaintenanceRequest(BaseModel):
    temperature: float
    current: float
    vibration: float
    working_hours: float

# -------------------------
# Overcurrent API (NEW)
# -------------------------
@app.post("/overcurrent")
def overcurrent(data: CurrentRequest):

    result = detect_overcurrent(data.current)

    return {
        "status": result
    }

# -------------------------
# Predictive Maintenance API (NEW)
# -------------------------
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

# -------------------------
# PPE Detection API (NEW)
# -------------------------
@app.post("/ppe-detection")
def ppe_detection_api(data: PPERequest):
    img_path = data.image_path
    
    # If the image_path is actually a base64 encoded string from Android
    if len(img_path) > 200:
        try:
            # Remove header if present e.g., "data:image/jpeg;base64,"
            if "," in img_path:
                img_path = img_path.split(",")[1]
            
            img_data = base64.b64decode(img_path)
            temp_path = "temp_ppe_image.jpg"
            with open(temp_path, "wb") as f:
                f.write(img_data)
            img_path = temp_path
        except Exception as e:
            pass

    result = detect_ppe(img_path)
    
    missing = []
    required = [
        "helmet",
        "safety vest",
        "gloves",
        "safety shoes"
    ]
    
    for item in required:
        if item not in result:
            missing.append(item)

    return {
        "detected": result,
        "missing": missing,
        "status": "SAFE" if len(missing) == 0 else "WARNING"
    }

from worker_health import detect_health

# -------------------------
# Worker Health Request Model
# -------------------------
class HealthRequest(BaseModel):
    image_path: str

# -------------------------
# Worker Health API (NEW)
# -------------------------
@app.post("/worker-health")
def worker_health(data: HealthRequest):
    img_path = data.image_path
    
    # If the image_path is actually a base64 encoded string from Android
    if len(img_path) > 200:
        try:
            if "," in img_path:
                img_path = img_path.split(",")[1]
            import base64
            img_data = base64.b64decode(img_path)
            temp_path = "temp_health_image.jpg"
            with open(temp_path, "wb") as f:
                f.write(img_data)
            img_path = temp_path
        except Exception as e:
            pass

    result = detect_health(img_path)
    return result
