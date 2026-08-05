"""
File: machine_maintenance.py
Includes functionality for the Smart Factory backend.
"""
##########################################################################
# machine_maintenance.py
# Machine Health Prediction using pre-trained AI4I model from HuggingFace
# Uses: SharleyK/engine-predictive-maintenance-model
# Predicts: Machine Health | Failure Risk | Status
# Features: air_temp, process_temp, rpm, torque, tool_wear, product_type
##########################################################################

import os
import joblib
import numpy as np
from huggingface_hub import hf_hub_download

# ── Model paths ───────────────────────────────────────────────────────
MODELS_DIR    = os.path.join(os.path.dirname(__file__), "models")
MODEL_FILE    = os.path.join(MODELS_DIR, "machine_maintenance_model.pkl")
ENCODER_FILE  = os.path.join(MODELS_DIR, "machine_label_encoder.pkl")

# ── Download & cache model from HuggingFace ───────────────────────────
def _download_file(repo_id: str, filename: str, dest: str):
    if not os.path.exists(dest):
        print(f"[INFO] Downloading {filename} from HuggingFace ({repo_id})...")
        os.makedirs(MODELS_DIR, exist_ok=True)
        path = hf_hub_download(repo_id=repo_id, filename=filename)
        import shutil
        shutil.copy(path, dest)
        print(f"[INFO] Saved to: {dest}")
    else:
        print(f"[INFO] Using cached: {dest}")

# ── Product type encoding (L=0, M=1, H=2) ─────────────────────────────
PRODUCT_TYPE_MAP = {"L": 0, "M": 1, "H": 2, "low": 0, "medium": 1, "high": 2}

try:
    _download_file(
        repo_id="SharleyK/engine-predictive-maintenance-model",
        filename="random_forest_model.pkl",
        dest=MODEL_FILE
    )
    maintenance_model = joblib.load(MODEL_FILE)

    # Try to load label encoder (optional)
    try:
        _download_file(
            repo_id="SharleyK/engine-predictive-maintenance-model",
            filename="label_encoder.pkl",
            dest=ENCODER_FILE
        )
        label_encoder = joblib.load(ENCODER_FILE)
    except Exception:
        label_encoder = None

    MODEL_LOADED = True
    print("[INFO] Machine maintenance model ready ✅")

except Exception as e:
    maintenance_model = None
    label_encoder     = None
    MODEL_LOADED      = False
    print(f"[WARNING] Could not load machine maintenance model: {e}")


def predict_machine_health(
    air_temperature: float,
    process_temperature: float,
    rotational_speed: float,
    torque: float,
    tool_wear: float,
    product_type: str = "M"
) -> dict:
    """
    Predict machine health status from sensor readings.

    Args:
        air_temperature    : Air temperature in Kelvin (e.g. 298.1)
        process_temperature: Process temperature in Kelvin (e.g. 308.6)
        rotational_speed   : Rotational speed in RPM (e.g. 1551)
        torque             : Torque in Nm (e.g. 42.8)
        tool_wear          : Tool wear in minutes (e.g. 0)
        product_type       : "L" (Low), "M" (Medium), "H" (High)

    Returns:
        dict:
          - health_score  : 0–100 (higher = healthier)
          - failure_risk  : "LOW" | "MEDIUM" | "HIGH"
          - status        : "GOOD" | "WARNING" | "CRITICAL"
          - recommendation: Human-readable action text
    """

    if not MODEL_LOADED or maintenance_model is None:
        # Fallback: rule-based estimation
        return _rule_based_prediction(tool_wear, torque, rotational_speed)

    try:
        # Encode product type
        type_encoded = PRODUCT_TYPE_MAP.get(str(product_type).strip().upper(), 1)

        # Build feature array matching AI4I schema:
        # [Type, Air temperature [K], Process temperature [K],
        #  Rotational speed [rpm], Torque [Nm], Tool wear [min]]
        features = np.array([[
            type_encoded,
            air_temperature,
            process_temperature,
            rotational_speed,
            torque,
            tool_wear
        ]])

        prediction = maintenance_model.predict(features)[0]

        # Get probability if available
        try:
            proba = maintenance_model.predict_proba(features)[0]
            failure_prob = float(max(proba[1:]))  # probability of any failure
        except Exception:
            failure_prob = 1.0 if prediction != 0 else 0.05

        # Decode label
        if label_encoder:
            try:
                label = str(label_encoder.inverse_transform([prediction])[0])
            except Exception:
                label = str(prediction)
        else:
            label = str(prediction)

        # Map to health score and status
        health_score = max(0, int(100 - failure_prob * 100))

        if failure_prob < 0.2:
            failure_risk   = "LOW"
            status         = "GOOD"
            recommendation = "Machine is operating normally. No action required."
        elif failure_prob < 0.5:
            failure_risk   = "MEDIUM"
            status         = "WARNING"
            recommendation = "Schedule maintenance soon. Monitor sensor readings closely."
        else:
            failure_risk   = "HIGH"
            status         = "CRITICAL"
            recommendation = "STOP MACHINE. Immediate maintenance required!"

        return {
            "health_score":   health_score,
            "failure_risk":   failure_risk,
            "status":         status,
            "prediction":     label,
            "recommendation": recommendation
        }

    except Exception as e:
        print(f"[ERROR] Machine maintenance prediction failed: {e}")
        return _rule_based_prediction(tool_wear, torque, rotational_speed)


def _rule_based_prediction(tool_wear: float, torque: float, rpm: float) -> dict:
    """Simple fallback rule-based prediction when model unavailable."""
    risk_score = 0
    if tool_wear   > 200: risk_score += 40
    elif tool_wear > 100: risk_score += 20
    if torque      > 60:  risk_score += 30
    elif torque    > 40:  risk_score += 10
    if rpm         < 1200: risk_score += 30
    elif rpm       < 1400: risk_score += 10

    health_score = max(0, 100 - risk_score)

    if risk_score < 20:
        return {"health_score": health_score, "failure_risk": "LOW",    "status": "GOOD",     "recommendation": "Normal operation."}
    elif risk_score < 50:
        return {"health_score": health_score, "failure_risk": "MEDIUM", "status": "WARNING",  "recommendation": "Schedule maintenance soon."}
    else:
        return {"health_score": health_score, "failure_risk": "HIGH",   "status": "CRITICAL", "recommendation": "Immediate maintenance required!"}
