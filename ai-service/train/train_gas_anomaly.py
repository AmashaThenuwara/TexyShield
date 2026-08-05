"""
File: train_gas_anomaly.py
Includes functionality for the Smart Factory backend.
"""
import numpy as np
import joblib
import os
from sklearn.ensemble import IsolationForest

# ------------------------
# Sample gas data
# Normal values around 3000–3500
# ------------------------
gas_data = np.array([
    [3000],
    [3100],
    [3200],
    [3300],
    [3400],
    [3500],
    [3050],
    [3150],
    [3250],
    [3350],
    [3450],
    [3550],

    # anomalies (important)
    [4200],
    [4500],
    [4800]
])

# ------------------------
# Train Isolation Forest
# ------------------------
model = IsolationForest(
    contamination=0.2,  # 20% anomalies
    random_state=42
)

model.fit(gas_data)

# ------------------------
# Save model
# ------------------------
os.makedirs("../models", exist_ok=True)

joblib.dump(model, "../models/gas_anomaly_model.pkl")

print("Gas Anomaly Model Trained Successfully!")