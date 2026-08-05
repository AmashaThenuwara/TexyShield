"""
File: fire_risk.py
Includes functionality for the Smart Factory backend.
"""
import joblib
import os

# Load trained model
model_path = os.path.join(
    os.path.dirname(__file__),
    "models",
    "fire_risk_model.pkl"
)

model = joblib.load(model_path)

def fire_risk(temperature, gas):

    prediction = model.predict([[temperature, gas]])

    return prediction[0]