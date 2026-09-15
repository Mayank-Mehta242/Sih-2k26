"""Shared feature and artifact contract for landslide models."""
import os

import joblib
import numpy as np


TABULAR_FEATURES = [
    "rainfall",
    "humidity",
    "temperature",
    "elevation",
    "slope",
    "historicalIncidents",
]
FEATURE_ORDER = TABULAR_FEATURES
LABELS = ["LOW", "MEDIUM", "HIGH", "VERY HIGH"]


def load_artifact(path):
    if not os.path.exists(path):
        raise FileNotFoundError(
            f"No trained model found at {path}. Run `python machine_learning/train_model.py` first."
        )
    return joblib.load(path)


def ordered_features(inputs):
    return np.array([[float(inputs[feature]) for feature in FEATURE_ORDER]], dtype=float)