import logging

import requests
from flask import current_app


logger = logging.getLogger(__name__)


def send_sms(to_number, message):
    """Send one SMS through TextFlow on RapidAPI when delivery is configured."""
    if not current_app.config["SMS_ENABLED"]:
        return False

    rapidapi_key = current_app.config["SMS_RAPIDAPI_KEY"]
    textflow_api_key = current_app.config["TEXTFLOW_API_KEY"]
    if not rapidapi_key or not textflow_api_key:
        logger.warning("SMS is enabled but TextFlow configuration is incomplete")
        return False

    headers = {
        "Content-Type": "application/json",
        "x-rapidapi-host": current_app.config["TEXTFLOW_API_HOST"],
        "x-rapidapi-key": rapidapi_key,
    }
    payload = {
        "data": {
            "phone_number": to_number,
            "text": message,
            "api_key": textflow_api_key,
        }
    }
    try:
        response = requests.post(
            current_app.config["TEXTFLOW_API_URL"],
            json=payload,
            headers=headers,
            timeout=10,
        )
        response.raise_for_status()
        return True
    except requests.RequestException:
        logger.exception("Unable to send SMS to %s", to_number)
        return False