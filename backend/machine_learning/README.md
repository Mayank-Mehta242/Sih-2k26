# Landslide model pipeline

## Current baseline

The first implemented stage is a tabular XGBoost multiclass classifier. It
uses the six columns currently present in `dataset.csv` and supported by the
prediction API:

- `rainfall`, `humidity`, and `temperature` from the existing RapidAPI weather
  integration
- `elevation` and `slope` from seeded district data
- `historicalIncidents` from seeded district counts

`soilMoisture`, `soilType`, `ndvi`, `landCover`, satellite imagery, and
historical weather sequences are not currently available as training or API
inputs. The trainer does not fabricate these values. They require timestamped,
geospatially aligned sources and validated landslide labels before use.

## Training and comparison

From `backend/`, run:

```text
python machine_learning/train_model.py
```

The script uses one stratified 80/20 holdout shared by Random Forest and
XGBoost. It reports accuracy plus macro precision, recall, F1, one-vs-rest
ROC-AUC, and macro PR-AUC. The comparison is saved to
`machine_learning/model_metrics.json`; the serving artifact is saved as
`machine_learning/landslide_xgboost_model.pkl`.

The checked-in CSV is synthetic development data. Its metrics are pipeline
checks only, not evidence of real-world landslide prediction accuracy. Do not
claim XGBoost is better unless the reported holdout metrics prove it on a real
validation dataset.

## Extension stages

The shared `model_pipeline.py` feature contract is the serving boundary. A
future satellite stage can add pretrained CNN embeddings, and a later fusion
stage can add GRU weather-sequence embeddings plus a tabular encoder. Those
encoders should produce features consumed by a new fusion artifact without
changing the weather service or the `/api/predict` response contract. A
Transformer is intentionally out of scope for the current implementation.