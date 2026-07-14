import joblib
import numpy as np
import os

model_path = os.path.join(
    os.path.dirname(__file__),
    "models",
    "overcurrent_model.pkl"
)

model = joblib.load(model_path)

def detect_overcurrent(current):

    data = np.array([[current]])
    prediction = model.predict(data)

    # 1 = normal, -1 = anomaly
    if prediction[0] == -1:
        return "OVERCURRENT 🚨"
    else:
        return "NORMAL"