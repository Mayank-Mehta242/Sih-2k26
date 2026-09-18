"""
Creates all tables and seeds representative districts across the North Eastern Region plus one demo admin account.
Safe to re-run — it reconciles the monitored district and skips existing users.

Usage:
    python seed_db.py
"""
from dotenv import load_dotenv
from sqlalchemy import inspect, text

load_dotenv()

from app import create_app  # noqa: E402
from app.extensions import db  # noqa: E402
from app.models.district import District  # noqa: E402
from app.models.user import User  # noqa: E402

DISTRICTS = [
    ("tawang", "Tawang, Arunachal Pradesh", 27.586, 91.859, "high", 18, 2085, 38),
    ("dima-hasao", "Dima Hasao, Assam", 25.5, 93.0, "medium", 14, 850, 28),
    ("chirang", "Chirang, Assam", 26.58, 90.62, "high", 16, 180, 22),
    ("baksa", "Baksa, Assam", 26.7, 91.23, "high", 20, 240, 26),
    ("imphal-west", "Imphal West, Manipur", 24.78, 93.94, "medium", 11, 790, 20),
    ("east-khasi-hills", "East Khasi Hills, Meghalaya", 25.58, 91.89, "high", 21, 1496, 35),
    ("aizawl", "Aizawl, Mizoram", 23.73, 92.72, "high", 17, 1132, 32),
    ("kohima", "Kohima, Nagaland", 25.67, 94.11, "medium", 13, 1444, 30),
    ("gangtok", "Gangtok, Sikkim", 27.33, 88.61, "high", 19, 1650, 36),
    ("west-tripura", "West Tripura, Tripura", 23.83, 91.28, "low", 7, 35, 12),
]


def seed():
    app = create_app()
    with app.app_context():
        db.create_all()

        if "review_comment" not in {column["name"] for column in inspect(db.engine).get_columns("incidents")}:
            db.session.execute(text("ALTER TABLE incidents ADD COLUMN review_comment TEXT"))
        incident_columns = {column["name"] for column in inspect(db.engine).get_columns("incidents")}
        if "image_data" not in incident_columns:
            db.session.execute(text("ALTER TABLE incidents ADD COLUMN image_data BYTEA"))
        if "image_mimetype" not in incident_columns:
            db.session.execute(text("ALTER TABLE incidents ADD COLUMN image_mimetype VARCHAR(100)"))
        district_columns = {column["name"] for column in inspect(db.engine).get_columns("districts")}
        if "elevation_m" not in district_columns:
            db.session.execute(text("ALTER TABLE districts ADD COLUMN elevation_m FLOAT NOT NULL DEFAULT 500"))
        if "slope_deg" not in district_columns:
            db.session.execute(text("ALTER TABLE districts ADD COLUMN slope_deg FLOAT NOT NULL DEFAULT 25"))

        District.query.filter(~District.id.in_([district[0] for district in DISTRICTS])).delete(synchronize_session=False)
        User.query.filter(User.role.in_(["admin", "official"])).update(
            {"role": "district_officer"}, synchronize_session=False
        )
        User.query.filter(User.role.in_(["citizen", "tourist"])).update(
            {"role": "driver"}, synchronize_session=False
        )

        for slug, name, lat, lng, risk, incidents, elevation, slope in DISTRICTS:
            district = db.session.get(District, slug)
            if district is None:
                district = District(id=slug)
                db.session.add(district)
            district.name = name
            district.lat = lat
            district.lng = lng
            district.current_risk = risk
            district.incidents_ytd = incidents
            district.elevation_m = elevation
            district.slope_deg = slope

        admin_user = User.query.filter_by(email="admin@pahadsuraksha.gov.in").first()
        if not admin_user:
            admin = User(name="Regional Officer", email="admin@pahadsuraksha.gov.in", role="district_officer", district="NER region")
            admin.set_password("ChangeMe123!")
            db.session.add(admin)
            print("Created demo admin — admin@pahadsuraksha.gov.in / ChangeMe123! (change this password)")
        else:
            admin_user.name = "Regional Officer"
            admin_user.role = "district_officer"
            admin_user.district = "NER region"

        db.session.commit()
        print(f"Seeded {District.query.count()} districts.")


if __name__ == "__main__":
    seed()
