"""
File: overheating.py
Includes functionality for the Smart Factory backend.
"""
import joblib
import numpy as np
import os

model_path = os.path.join(
    os.path.dirname(__file__),
    "models",
    "overheating_model.pkl"
)

model = joblib.load(model_path)

def predict_overheating(temps):

    data = np.array([temps])  # [t1, t2, t3, t4]
    prediction = model.predict(data)

    return round(prediction[0], 2)