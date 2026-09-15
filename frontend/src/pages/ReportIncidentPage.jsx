import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { ImagePlus, LocateFixed, Send } from "lucide-react";
import Card from "../components/Card.jsx";
import MapView from "../components/MapView.jsx";
import { useGeolocation } from "../hooks/useGeolocation.js";
import { incidentService } from "../services/incidentService.js";

export default function ReportIncidentPage() {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [image, setImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [coords, setCoords] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [incidents, setIncidents] = useState([]);
  const { position, locate, locating } = useGeolocation();

  useEffect(() => {
    if (position) setCoords(position);
  }, [position]);

  useEffect(() => {
    incidentService.list("approved").then(setIncidents).catch(() => setIncidents([]));
  }, []);

  function handleImage(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setImage(file);
    setImagePreview(URL.createObjectURL(file));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const latitude = Number(coords?.lat);
    const longitude = Number(coords?.lng);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude) || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
      toast.error("Enter a valid latitude and longitude, or use My Location.");
      return;
    }
    setSubmitting(true);
    try {
      const formData = new FormData();
      formData.append("title", title);
      formData.append("description", description);
      formData.append("lat", latitude);
      formData.append("lng", longitude);
      if (image) formData.append("image", image);

      await incidentService.submit(formData);
      toast.success("Report submitted for review.");
      setTitle("");
      setDescription("");
      setImage(null);
      setImagePreview(null);
    } catch (err) {
      toast.error("Could not submit report.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="max-w-4xl">
      <h1 className="text-3xl font-bold text-white mb-8">Report an Incident</h1>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card title="Incident Details">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-sm text-slate-200 mb-1.5 block">Title</label>
              <input
                required
                className="input-field"
                placeholder="e.g. Debris on NH-58 near Devprayag"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>

            <div>
              <label className="text-sm text-slate-200 mb-1.5 block">Description</label>
              <textarea
                required
                rows={3}
                className="input-field resize-none"
                placeholder="What did you see? How large is the affected area?"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <div>
              <label className="text-sm text-slate-200 mb-1.5 block">Photo (optional)</label>
              <label className="flex items-center gap-3 border border-dashed border-slate-600 rounded px-4 py-3 cursor-pointer hover:border-forest-500 transition-colors">
                <ImagePlus className="h-5 w-5 text-slate-500" />
                <span className="text-sm text-slate-200">
                  {image ? image.name : "Click to upload"}
                </span>
                <input type="file" accept="image/*" className="hidden" onChange={handleImage} />
              </label>
              {imagePreview && (
                <img src={imagePreview} alt="Preview" className="mt-3 rounded max-h-40 object-cover w-full" />
              )}
            </div>

            <div>
              <div className="flex items-center justify-between gap-3 mb-2">
                <p className="text-sm text-slate-200">Location</p>
                <button type="button" onClick={locate} className="btn-secondary text-sm">
                <LocateFixed className="h-4 w-4" />
                {locating ? "Locating…" : coords ? "Set" : "My Location"}
                </button>
              </div>
              <p className="text-xs text-slate-400 mb-2">Click the map, type coordinates manually, or use your current location.</p>
              <div className="grid grid-cols-2 gap-3">
                {[{ label: "Latitude", key: "lat", placeholder: "26.1445" }, { label: "Longitude", key: "lng", placeholder: "91.7362" }].map((coordinate) => (
                  <label key={coordinate.key} className="text-xs text-slate-300">
                    {coordinate.label}
                    <input
                      required
                      type="number"
                      step="any"
                      min={coordinate.key === "lat" ? -90 : -180}
                      max={coordinate.key === "lat" ? 90 : 180}
                      className="input-field mt-1"
                      value={coords?.[coordinate.key] ?? ""}
                      onChange={(event) => setCoords((current) => ({
                        ...(current ?? { lat: "", lng: "" }),
                        [coordinate.key]: event.target.value,
                      }))}
                      placeholder={coordinate.placeholder}
                    />
                  </label>
                ))}
              </div>
            </div>

            <button type="submit" disabled={submitting} className="btn-primary w-full">
              {submitting ? "Submitting…" : "Submit Report"}
            </button>
          </form>
        </Card>

        <div className="space-y-4">
          <Card title="Reported Incidents" className="!p-0 overflow-hidden">
            <MapView
              districts={incidents.map((incident) => ({
                ...incident,
                name: incident.title,
                risk: "medium",
              }))}
              onMapClick={setCoords}
              userPosition={coords}
              height="280px"
            />
          </Card>
          <Card>
            <p className="text-sm text-slate-200">
              Reports are reviewed before appearing on the public map.
            </p>
          </Card>
        </div>
      </div>
    </div>
  );
}
