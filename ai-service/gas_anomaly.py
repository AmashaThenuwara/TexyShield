import joblib
import numpy as np
import os

model_path = os.path.join(
    os.path.dirname(__file__),
    "models",
    "gas_anomaly_model.pkl"
)

model = joblib.load(model_path)

def detect_gas_anomaly(gas):

    data = np.array([[gas]])
    prediction = model.predict(data)

    # Isolation Forest output:
    # 1 = normal, -1 = anomaly
    if prediction[0] == -1:
        return "ANOMALY 🚨"
    else:
        return "NORMAL"