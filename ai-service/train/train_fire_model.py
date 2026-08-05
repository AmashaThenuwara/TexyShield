"""
File: train_fire_model.py
Includes functionality for the Smart Factory backend.
"""
from sklearn.ensemble import RandomForestClassifier
import pandas as pd
import joblib
import os

# Sample training data
data = {
    "temperature": [25, 30, 35, 38, 42, 45, 28, 33, 40, 50],
    "gas":         [500, 1000, 2000, 3000, 3800, 4200, 800, 1500, 3600, 4500],
    "risk": [
        "NORMAL",
        "NORMAL",
        "WARNING",
        "WARNING",
        "HIGH FIRE RISK",
        "HIGH FIRE RISK",
        "NORMAL",
        "NORMAL",
        "WARNING",
        "HIGH FIRE RISK"
    ]
}

df = pd.DataFrame(data)

X = df[["temperature", "gas"]]
y = df["risk"]

model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X, y)

os.makedirs("../models", exist_ok=True)

joblib.dump(model, "../models/fire_risk_model.pkl")

print("Fire Risk Model Trained and Saved Successfully!")