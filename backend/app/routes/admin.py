import io
import csv
import os
import sqlite3
import tempfile

from flask import Blueprint, current_app, jsonify, request, Response, send_file
from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError
from app.extensions import db
from app.models.user import User
from app.models.district import District
from app.models.incident import Incident
from app.utils.decorators import role_required

admin_bp = Blueprint("admin", __name__, url_prefix="/api/admin")


SQLITE_IMPORT_TABLES = ("users", "districts", "incidents", "predictions", "notifications", "weather_cache")


@admin_bp.post("/migrate-sqlite")
def migrate_sqlite():
    """One-time, token-protected import of the project's local SQLite data.

    This is deliberately disabled until DATABASE_MIGRATION_TOKEN is configured.
    It is designed for an empty Render PostgreSQL database and replaces its
    contents only when the caller explicitly supplies replace=true.
    """
    expected_token = current_app.config["DATABASE_MIGRATION_TOKEN"]
    if not expected_token or request.headers.get("X-Migration-Token") != expected_token:
        return jsonify({"error": "Migration is disabled or unauthorized."}), 403
    if not current_app.config["SQLALCHEMY_DATABASE_URI"].startswith("postgresql"):
        return jsonify({"error": "DATABASE_URL must point to PostgreSQL before importing."}), 400
    if request.form.get("replace") != "true":
        return jsonify({"error": "Set replace=true to confirm replacement of the target database."}), 400

    uploaded = request.files.get("database")
    if not uploaded or not uploaded.filename.lower().endswith(".db"):
        return jsonify({"error": "Upload the pahadsuraksha.db file in the database field."}), 400

    temp_path = None
    try:
        with tempfile.NamedTemporaryFile(suffix=".db", delete=False) as temporary_file:
            temp_path = temporary_file.name
            uploaded.save(temp_path)

        source = sqlite3.connect(temp_path)
        source.row_factory = sqlite3.Row
        source_tables = {
            row[0]
            for row in source.execute("SELECT name FROM sqlite_master WHERE type='table'")
        }
        missing = set(SQLITE_IMPORT_TABLES) - source_tables
        if missing:
            return jsonify({"error": f"Invalid SQLite database; missing: {', '.join(sorted(missing))}."}), 400

        db.create_all()
        # Delete child rows before their parents so PostgreSQL foreign keys remain valid.
        for table_name in reversed(SQLITE_IMPORT_TABLES):
            db.session.execute(db.metadata.tables[table_name].delete())

        imported = {}
        for table_name in SQLITE_IMPORT_TABLES:
            table = db.metadata.tables[table_name]
            allowed_columns = {column.name for column in table.columns}
            rows = [
                {key: row[key] for key in row.keys() if key in allowed_columns}
                for row in source.execute(f"SELECT * FROM {table_name}")
            ]
            if rows:
                db.session.execute(table.insert(), rows)
            imported[table_name] = len(rows)

        # SQLite stores weather-cache IDs directly; advance PostgreSQL's
        # auto-increment sequence so the next cached weather row does not
        # reuse an imported ID.
        db.session.execute(
            text(
                "SELECT setval(pg_get_serial_sequence('weather_cache', 'id'), "
                "COALESCE((SELECT MAX(id) FROM weather_cache), 1), true)"
            )
        )

        db.session.commit()
        source.close()
        return jsonify({"message": "SQLite data imported into PostgreSQL.", "imported": imported}), 200
    except (sqlite3.Error, SQLAlchemyError) as error:
        db.session.rollback()
        return jsonify({"error": f"Import failed: {error}"}), 500
    finally:
        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)


@admin_bp.get("/incidents")
@role_required("district_officer")
def list_incidents():
    incidents = Incident.query.order_by(Incident.created_at.desc()).all()
    return jsonify([incident.to_dict() for incident in incidents]), 200


@admin_bp.get("/users")
@role_required("district_officer")
def list_users():
    users = User.query.order_by(User.created_at.desc()).all()
    return jsonify([u.to_dict() for u in users]), 200


@admin_bp.get("/analytics/districts")
@role_required("district_officer")
def district_analytics():
    districts = District.query.all()
    return (
        jsonify(
            [
                {
                    "district": d.name,
                    "risk": d.current_risk,
                    "incidents": d.incidents_ytd,
                }
                for d in districts
            ]
        ),
        200,
    )


@admin_bp.get("/export.csv")
@role_required("district_officer")
def export_csv():
    incidents = Incident.query.order_by(Incident.created_at.desc()).all()

    buffer = io.StringIO()
    writer = csv.writer(buffer)
    writer.writerow(["ID", "Title", "District", "Latitude", "Longitude", "Status", "Reported On"])
    for i in incidents:
        writer.writerow([i.id, i.title, i.district, i.lat, i.lng, i.status, i.created_at.strftime("%Y-%m-%d")])

    return Response(
        buffer.getvalue(),
        mimetype="text/csv",
        headers={"Content-Disposition": "attachment; filename=pahadsuraksha_incidents.csv"},
    )


@admin_bp.get("/export.pdf")
@role_required("district_officer")
def export_pdf():
    from fpdf import FPDF  # imported lazily so the app still boots without fpdf2 installed for CSV-only use

    incidents = Incident.query.order_by(Incident.created_at.desc()).all()

    pdf = FPDF()
    pdf.add_page()
    pdf.set_font("Helvetica", "B", 14)
    pdf.cell(0, 10, "PahadSuraksha — Incident Report Export", ln=True)
    pdf.set_font("Helvetica", "", 10)
    pdf.ln(4)

    for i in incidents:
        pdf.set_font("Helvetica", "B", 11)
        pdf.multi_cell(0, 6, f"{i.title} ({i.status})")
        pdf.set_font("Helvetica", "", 9)
        pdf.multi_cell(0, 5, f"District: {i.district or 'Unknown'}  |  Reported: {i.created_at.strftime('%Y-%m-%d')}")
        pdf.ln(3)

    output = io.BytesIO(pdf.output(dest="S"))
    output.seek(0)
    return send_file(
        output, mimetype="application/pdf", as_attachment=True, download_name="ner_suraksha_incidents.pdf"
    )
