import os
from datetime import timedelta

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def _frontend_origins():
    """Return the browser origins permitted to call the public API.

    ``FRONTEND_ORIGINS`` supports a comma-separated list so local development
    and one or more deployed frontends can use the same Render service.
    ``FRONTEND_ORIGIN`` remains supported for existing deployments.
    """
    configured = os.environ.get("FRONTEND_ORIGINS") or os.environ.get("FRONTEND_ORIGIN")
    if configured:
        return tuple(origin.strip().rstrip("/") for origin in configured.split(",") if origin.strip())
    return (
        "http://localhost:5173",
        "https://sih-2k26-five.vercel.app",
    )


class Config:
    SECRET_KEY = os.environ.get("SECRET_KEY", "dev-secret")
    JWT_SECRET_KEY = os.environ.get("JWT_SECRET_KEY", "dev-jwt-secret")
    JWT_ACCESS_TOKEN_EXPIRES = timedelta(days=7)

    SQLALCHEMY_DATABASE_URI = os.environ.get(
        "DATABASE_URL", f"sqlite:///{os.path.join(BASE_DIR, 'instance', 'pahadsuraksha.db')}"
    )
    SQLALCHEMY_TRACK_MODIFICATIONS = False

    FRONTEND_ORIGINS = _frontend_origins()

    SMS_ENABLED = os.environ.get("SMS_ENABLED", "false").lower() == "true"
    SMS_RAPIDAPI_KEY = os.environ.get("SMS_RAPIDAPI_KEY", "")
    TEXTFLOW_API_KEY = os.environ.get("TEXTFLOW_API_KEY", "")
    TEXTFLOW_API_URL = os.environ.get(
        "TEXTFLOW_API_URL", "https://textflow-sms-api.p.rapidapi.com/send-sms"
    )
    TEXTFLOW_API_HOST = os.environ.get("TEXTFLOW_API_HOST", "textflow-sms-api.p.rapidapi.com")

    RAPIDAPI_KEY = os.environ.get("RAPIDAPI_KEY", "")
    RAPIDAPI_HOST = os.environ.get("RAPIDAPI_HOST", "open-weather13.p.rapidapi.com")
    RAPIDAPI_UA = os.environ.get("RAPIDAPI_UA", "RapidAPI-Playground")
    ELEVATION_API_URL = os.environ.get("ELEVATION_API_URL", "https://api.open-elevation.com/api/v1/lookup")
    WEATHER_CACHE_MINUTES = int(os.environ.get("WEATHER_CACHE_MINUTES", "30"))

    UPLOAD_FOLDER = os.path.join(BASE_DIR, "uploads", "incidents")
    MAX_CONTENT_LENGTH = 8 * 1024 * 1024  # 8 MB uploads

    ML_MODEL_PATH = os.path.join(BASE_DIR, "machine_learning", "landslide_xgboost_model.pkl")
