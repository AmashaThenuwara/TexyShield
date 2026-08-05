"""
File: train_overcurrent.py
Includes functionality for the Smart Factory backend.
"""
import numpy as np
import joblib
import os
from sklearn.ensemble import IsolationForest

# -------------------------
# Normal current values
# -------------------------
current_data = np.array([
    [200],
    [220],
    [210],
    [230],
    [240],
    [250],
    [260],
    [270],
    [280],
    [300],
    [320],
    [340],
    [360],

    # anomalies (overcurrent)
    [600],
    [750],
    [900],
    [1200]
])

# -------------------------
# Train model
# -------------------------
model = IsolationForest(
    contamination=0.2,
    random_state=42
)

model.fit(current_data)

# -------------------------
# Save model
# -------------------------
os.makedirs("../models", exist_ok=True)

joblib.dump(model, "../models/overcurrent_model.pkl")

print("Overcurrent Model Trained Successfully!")