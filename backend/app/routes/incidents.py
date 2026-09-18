import os
import uuid

from flask import Blueprint, request, jsonify, current_app, Response, send_from_directory
from flask_jwt_extended import get_jwt_identity, verify_jwt_in_request
from werkzeug.utils import secure_filename

from app.extensions import db
from app.models.incident import Incident
from app.models.notification import Notification
from app.models.user import User
from app.services.sms_service import send_sms
from app.utils.decorators import role_required

incidents_bp = Blueprint("incidents", __name__, url_prefix="/api/incidents")

ALLOWED_EXTENSIONS = {"png", "jpg", "jpeg", "webp"}


def _allowed(filename):
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


@incidents_bp.get("")
def list_incidents():
    status = request.args.get("status")
    if status and status != "approved":
        return jsonify({"error": "Only approved reports are public."}), 403
    status = "approved"

    query = Incident.query
    if status:
        query = query.filter_by(status=status)
    incidents = query.order_by(Incident.created_at.desc()).all()
    return jsonify([i.to_dict() for i in incidents]), 200


@incidents_bp.post("")
def submit_incident():
    title = request.form.get("title")
    description = request.form.get("description")
    lat = request.form.get("lat", type=float)
    lng = request.form.get("lng", type=float)
    district = request.form.get("district")

    if not title or lat is None or lng is None:
        return jsonify({"error": "title, lat, and lng are required."}), 400

    image_path = None
    image_data = None
    image_mimetype = None
    file = request.files.get("image")
    if file and file.filename:
        if not _allowed(file.filename):
            return jsonify({"error": "Unsupported image type."}), 400
        ext = file.filename.rsplit(".", 1)[1].lower()
        image_path = f"{uuid.uuid4()}.{ext}"
        image_data = file.read()
        image_mimetype = file.mimetype or f"image/{ext}"

    reporter_id = None
    try:
        verify_jwt_in_request(optional=True)
        reporter_id = get_jwt_identity()
    except Exception:
        pass

    incident = Incident(
        title=title,
        description=description,
        district=district,
        lat=lat,
        lng=lng,
        image_path=image_path,
        image_data=image_data,
        image_mimetype=image_mimetype,
        reporter_id=reporter_id,
    )
    db.session.add(incident)
    db.session.commit()

    return jsonify(incident.to_dict()), 201


@incidents_bp.get("/uploads/<path:filename>")
def serve_upload(filename):
    incident = Incident.query.filter_by(image_path=filename).first()
    if incident and incident.image_data:
        return Response(incident.image_data, mimetype=incident.image_mimetype or "application/octet-stream")
    return send_from_directory(current_app.config["UPLOAD_FOLDER"], filename)


@incidents_bp.patch("/<incident_id>/approve")
@role_required("district_officer")
def approve(incident_id):
    incident = Incident.query.get_or_404(incident_id)
    body = request.get_json(silent=True) or {}
    was_already_approved = incident.status == "approved"
    incident.status = "approved"
    incident.review_comment = (body.get("comment") or "").strip()[:2000] or None
    db.session.commit()

    if not was_already_approved:
        location = incident.district or f"{incident.lat:.4f}, {incident.lng:.4f}"
        message = f"Landslide alert: {incident.title} reported near {location}. Please be careful."
        users = User.query.filter(User.phone.isnot(None), User.phone != "").all()
        for user in users:
            db.session.add(Notification(user_id=user.id, text=message))
            send_sms(user.phone, message)
        db.session.commit()

    return jsonify(incident.to_dict()), 200


@incidents_bp.patch("/<incident_id>/reject")
@role_required("district_officer")
def reject(incident_id):
    incident = Incident.query.get_or_404(incident_id)
    body = request.get_json(silent=True) or {}
    incident.status = "rejected"
    incident.review_comment = (body.get("comment") or "").strip()[:2000] or None
    db.session.commit()
    return jsonify(incident.to_dict()), 200


@incidents_bp.patch("/<incident_id>")
@role_required("district_officer")
def update_incident(incident_id):
    incident = Incident.query.get_or_404(incident_id)
    body = request.get_json(silent=True) or {}

    for field in ("title", "description", "district"):
        if field in body:
            value = body[field]
            if field == "title" and not isinstance(value, str):
                return jsonify({"error": "Title must be text."}), 400
            setattr(incident, field, value.strip() if isinstance(value, str) else value)

    for field in ("lat", "lng"):
        if field in body:
            try:
                setattr(incident, field, float(body[field]))
            except (TypeError, ValueError):
                return jsonify({"error": f"{field} must be a number."}), 400

    if not incident.title:
        return jsonify({"error": "Title is required."}), 400

    # Any content correction requires a fresh review.
    if incident.status != "pending":
        incident.status = "pending"
        incident.review_comment = None
    db.session.commit()
    return jsonify(incident.to_dict()), 200


@incidents_bp.delete("/<incident_id>")
@role_required("district_officer")
def delete_incident(incident_id):
    incident = Incident.query.get_or_404(incident_id)

    if incident.image_path and not incident.image_data:
        image_file = os.path.join(current_app.config["UPLOAD_FOLDER"], secure_filename(incident.image_path))
        if os.path.isfile(image_file):
            os.remove(image_file)

    db.session.delete(incident)
    db.session.commit()
    return jsonify({"id": incident_id}), 200
