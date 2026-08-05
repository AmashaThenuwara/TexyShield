"""
File: train_overheating_model.py
Includes functionality for the Smart Factory backend.
"""
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
import joblib
import os

# Sample dataset (you will improve later if needed)
data = {
    "t1": [30, 31, 32, 33, 34, 35, 36, 37, 38, 39],
    "t2": [31, 32, 33, 34, 35, 36, 37, 38, 39, 40],
    "t3": [32, 33, 34, 35, 36, 37, 38, 39, 40, 41],
    "t4": [33, 34, 35, 36, 37, 38, 39, 40, 41, 42],
    "future_temp": [34, 35, 36, 37, 38, 39, 40, 41, 42, 43]
}

df = pd.DataFrame(data)

X = df[["t1", "t2", "t3", "t4"]]
y = df["future_temp"]

model = RandomForestRegressor(n_estimators=100, random_state=42)
model.fit(X, y)

# Save model
os.makedirs("../models", exist_ok=True)

joblib.dump(model, "../models/overheating_model.pkl")

print("Overheating Model Trained Successfully!")