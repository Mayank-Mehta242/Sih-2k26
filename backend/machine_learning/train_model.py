"""Train and compare the tabular landslide-risk models.

The checked-in CSV is a synthetic development dataset. Its metrics are useful
for checking the pipeline, but must not be treated as field accuracy.
"""
import os
import json
import numpy as np
import pandas as pd
from xgboost import XGBClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, average_precision_score, classification_report, f1_score, precision_score, recall_score, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, label_binarize
import joblib

from model_pipeline import FEATURE_ORDER

RANDOM_STATE = 42
MODEL_PATH = os.path.join(os.path.dirname(__file__), "landslide_xgboost_model.pkl")
DATASET_PATH = os.path.join(os.path.dirname(__file__), "dataset.csv")
METRICS_PATH = os.path.join(os.path.dirname(__file__), "model_metrics.json")


def _risk_label(rainfall, humidity, slope, historical_incidents, elevation):
    """Ground-truth rule used only to LABEL the synthetic training data —
    the model itself learns a Random Forest boundary from these examples
    rather than reapplying this rule at inference time."""
    score = (
        0.34 * min(rainfall / 80, 1)
        + 0.16 * min(humidity / 100, 1)
        + 0.27 * min(slope / 60, 1)
        + 0.18 * min(historical_incidents / 8, 1)
        + 0.05 * min(elevation / 3000, 1)
    )
    noise = np.random.normal(0, 0.05)
    score += noise

    if score < 0.3:
        return "LOW"
    if score < 0.5:
        return "MEDIUM"
    if score < 0.7:
        return "HIGH"
    return "VERY HIGH"


def generate_synthetic_dataset(n=6000):
    rng = np.random.default_rng(RANDOM_STATE)
    rainfall = rng.gamma(shape=2.0, scale=15, size=n)  # skewed toward lower rainfall, occasional heavy events
    humidity = rng.uniform(30, 100, size=n)
    temperature = rng.uniform(2, 30, size=n)
    elevation = rng.uniform(300, 3800, size=n)
    slope = rng.uniform(0, 60, size=n)
    historical_incidents = rng.poisson(lam=1.5, size=n)

    labels = [
        _risk_label(r, h, s, hi, e)
        for r, h, s, hi, e in zip(rainfall, humidity, slope, historical_incidents, elevation)
    ]

    df = pd.DataFrame(
        {
            "rainfall": rainfall.round(1),
            "humidity": humidity.round(1),
            "temperature": temperature.round(1),
            "elevation": elevation.round(0),
            "slope": slope.round(1),
            "historicalIncidents": historical_incidents,
            "riskLevel": labels,
        }
    )
    return df


def _metrics(model, X_test, y_test):
    predictions = model.predict(X_test)
    probabilities = model.predict_proba(X_test)
    classes = list(model.classes_)
    y_test_binary = label_binarize(y_test, classes=classes)
    return {
        "accuracy": round(float(accuracy_score(y_test, predictions)), 4),
        "precision_macro": round(float(precision_score(y_test, predictions, average="macro", zero_division=0)), 4),
        "recall_macro": round(float(recall_score(y_test, predictions, average="macro", zero_division=0)), 4),
        "f1_macro": round(float(f1_score(y_test, predictions, average="macro", zero_division=0)), 4),
        "roc_auc_ovr_macro": round(float(roc_auc_score(y_test_binary, probabilities, multi_class="ovr", average="macro")), 4),
        "pr_auc_macro": round(float(average_precision_score(y_test_binary, probabilities, average="macro")), 4),
        "classification_report": classification_report(y_test, predictions, zero_division=0, output_dict=True),
    }


def _print_metrics(name, metrics):
    print(name)
    for metric in ("accuracy", "precision_macro", "recall_macro", "f1_macro", "roc_auc_ovr_macro", "pr_auc_macro"):
        print(f"  {metric}: {metrics[metric]:.4f}")


def train():
    if os.path.exists(DATASET_PATH):
        print(f"Loading existing dataset from {DATASET_PATH}")
        df = pd.read_csv(DATASET_PATH)
    else:
        print("No dataset.csv found — generating a synthetic dataset (replace with real records for production).")
        df = generate_synthetic_dataset()
        df.to_csv(DATASET_PATH, index=False)
        print(f"Synthetic dataset saved to {DATASET_PATH}")

    missing = [feature for feature in FEATURE_ORDER + ["riskLevel"] if feature not in df.columns]
    if missing:
        raise ValueError(f"Dataset is missing required columns: {', '.join(missing)}")

    X = df[FEATURE_ORDER]
    label_encoder = LabelEncoder()
    y = label_encoder.fit_transform(df["riskLevel"])

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=RANDOM_STATE, stratify=y
    )

    random_forest = RandomForestClassifier(
        n_estimators=500,
        max_depth=None,
        min_samples_leaf=1,
        random_state=RANDOM_STATE,
        n_jobs=-1,
    )
    xgboost = XGBClassifier(
        n_estimators=350,
        max_depth=6,
        learning_rate=0.05,
        subsample=0.9,
        colsample_bytree=0.9,
        objective="multi:softprob",
        eval_metric="mlogloss",
        tree_method="hist",
        random_state=RANDOM_STATE,
        n_jobs=-1,
    )

    random_forest.fit(X_train, y_train)
    xgboost.fit(X_train, y_train)
    random_forest_metrics = _metrics(random_forest, X_test, y_test)
    xgboost_metrics = _metrics(xgboost, X_test, y_test)
    _print_metrics("Random Forest", random_forest_metrics)
    _print_metrics("XGBoost", xgboost_metrics)

    xgboost.metadata = {
        "model": "xgboost",
        "feature_order": FEATURE_ORDER,
        "class_names": list(label_encoder.classes_),
        "training_rows": len(df),
        "test_rows": len(X_test),
        "random_state": RANDOM_STATE,
    }
    joblib.dump(xgboost, MODEL_PATH)
    with open(METRICS_PATH, "w", encoding="utf-8") as metrics_file:
        json.dump(
            {
                "dataset": os.path.basename(DATASET_PATH),
                "dataset_is_synthetic": True,
                "features": FEATURE_ORDER,
                "test_size": 0.2,
                "random_state": RANDOM_STATE,
                "models": {"random_forest": random_forest_metrics, "xgboost": xgboost_metrics},
            },
            metrics_file,
            indent=2,
        )
    print(f"Model saved to {MODEL_PATH}")
    print(f"Metrics saved to {METRICS_PATH}")
    print(f"Model saved to {MODEL_PATH}")


if __name__ == "__main__":
    train()
