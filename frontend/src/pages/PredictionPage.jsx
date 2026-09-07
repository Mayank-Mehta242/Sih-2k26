import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { Loader2 } from "lucide-react";
import Card from "../components/Card.jsx";
import RiskBadge from "../components/RiskBadge.jsx";
import { predictionService } from "../services/predictionService.js";
import { districtService } from "../services/districtService.js";
import { weatherService } from "../services/weatherService.js";

const FIELDS = [
  { key: "rainfall", label: "Rainfall (mm, last 48h)", placeholder: "e.g. 45" },
  { key: "humidity", label: "Humidity (%)", placeholder: "e.g. 78" },
  { key: "temperature", label: "Temperature (°C)", placeholder: "e.g. 16" },
  { key: "elevation", label: "Elevation (m)", placeholder: "e.g. 1800" },
  { key: "slope", label: "Slope (°)", placeholder: "e.g. 38" },
  { key: "historicalIncidents", label: "Historical incidents nearby", placeholder: "e.g. 4" },
];

const INPUT_RANGES = {
  rainfall: 80,
  humidity: 100,
  temperature: 40,
  elevation: 3800,
  slope: 60,
  historicalIncidents: 8,
};

const INPUT_UNITS = {
  rainfall: "mm",
  humidity: "%",
  temperature: "°C",
  elevation: "m",
  slope: "°",
  historicalIncidents: "events",
};

export default function PredictionPage() {
  const [form, setForm] = useState({});
  const [districts, setDistricts] = useState([]);
  const [selectedDistrictId, setSelectedDistrictId] = useState("");
  const [locationLoading, setLocationLoading] = useState(true);
  const [locationError, setLocationError] = useState("");
  const [weatherLoading, setWeatherLoading] = useState(false);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [submittedInputs, setSubmittedInputs] = useState(null);

  const selectedDistrict = districts.find((district) => district.id === selectedDistrictId);

  useEffect(() => {
    districtService
      .list()
      .then((list) => {
        setDistricts(list);
        const defaultDistrict = list.find((district) => district.id === "chirang") ?? list[0];
        setSelectedDistrictId(defaultDistrict?.id ?? "");
      })
      .catch(() => setLocationError("Could not load monitored locations."))
      .finally(() => setLocationLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedDistrict) return;
    let active = true;
    setWeatherLoading(true);
    setLocationError("");
    weatherService
      .getWeather(selectedDistrict.lat, selectedDistrict.lng)
      .then((weather) => {
        if (!active) return;
        setForm((current) => ({
          ...current,
          rainfall: weather.rainfallMm,
          humidity: weather.humidityPct,
          temperature: weather.temperatureC,
          elevation: selectedDistrict.elevation ?? 500,
          slope: selectedDistrict.slope ?? 25,
          historicalIncidents: selectedDistrict.incidents ?? 0,
        }));
      })
      .catch(() => {
        if (active) setLocationError("Weather data is unavailable for this location.");
      })
      .finally(() => {
        if (active) setWeatherLoading(false);
      });
    return () => {
      active = false;
    };
  }, [selectedDistrict]);

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setResult(null);
    try {
      const inputs = Object.fromEntries(
        FIELDS.map(({ key }) => [key, Number(form[key])])
      );
      const output = await predictionService.predict(inputs);
      setSubmittedInputs(inputs);
      setResult(output);
    } catch (err) {
      toast.error("Could not get risk estimate. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-2">Check Travel Risk</h1>
      <p className="text-slate-200 mb-6">
        Select a monitored location to automatically load rainfall, humidity, temperature, terrain, and historical events.
      </p>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card title="Location Details">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-sm text-slate-200 mb-1.5 block" htmlFor="risk-location">
                Monitored location
              </label>
              <select
                id="risk-location"
                required
                className="input-field"
                value={selectedDistrictId}
                disabled={locationLoading || districts.length === 0}
                onChange={(event) => setSelectedDistrictId(event.target.value)}
              >
                <option value="">Select a district</option>
                {districts.map((district) => (
                  <option key={district.id} value={district.id}>{district.name}</option>
                ))}
              </select>
            </div>
            {locationError && <p className="text-sm text-risk-high">{locationError}</p>}
            <div className="grid sm:grid-cols-2 gap-4">
              {FIELDS.map((f) => (
                <div key={f.key}>
                  <label className="text-sm text-slate-200 mb-1.5 block">{f.label}</label>
                  <input
                    type="number"
                    step="any"
                    required
                    className="input-field"
                    placeholder={f.placeholder}
                    value={form[f.key] ?? ""}
                    readOnly
                    disabled={weatherLoading || !selectedDistrict}
                    onChange={(e) => setForm({ ...form, [f.key]: e.target.value })}
                  />
                </div>
              ))}
            </div>
            <button type="submit" disabled={loading || weatherLoading || !selectedDistrict} className="btn-primary w-full">
              {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              {loading ? "Analyzing…" : weatherLoading ? "Loading conditions…" : "Estimate Risk"}
            </button>
            <p className="text-xs text-slate-300">
              Historical events are taken from the selected district's monitored incident record.
            </p>
          </form>
        </Card>

        <Card title="Risk Estimate">
          {!result && !loading && (
            <p className="text-sm text-slate-200 py-8 text-center">
              Enter the conditions above to see your risk estimate.
            </p>
          )}
          {loading && <p className="text-sm text-slate-200 py-8 text-center">Calculating…</p>}
          {result && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <RiskBadge level={result.riskLevel} size="lg" />
                <div className="text-right">
                  <p className="text-3xl font-bold text-white">{result.confidence}%</p>
                  <p className="text-xs text-slate-300">confidence</p>
                </div>
              </div>

              <div>
                <p className="text-xs text-slate-300 uppercase tracking-wide mb-2">Why this risk level</p>
                <ul className="space-y-2">
                  {result.reasons.map((r) => (
                    <li key={r} className="flex items-start gap-2 text-sm text-slate-200">
                      <span className="h-1.5 w-1.5 rounded-full bg-forest-500 mt-1.5 shrink-0" />
                      {r}
                    </li>
                  ))}
                </ul>
              </div>

              <div>
                <p className="text-xs text-slate-300 uppercase tracking-wide mb-2">Entered conditions</p>
                <div className="space-y-2">
                  {FIELDS.map((field) => {
                    const value = submittedInputs?.[field.key] ?? 0;
                    const percentage = Math.min(
                      100,
                      Math.max(0, (value / INPUT_RANGES[field.key]) * 100)
                    );
                    return (
                      <div key={field.key}>
                        <div className="flex justify-between text-xs text-slate-300 mb-1">
                          <span>{field.label.replace(/ \(.+\)/, "")}</span>
                          <span>{value} {INPUT_UNITS[field.key]}</span>
                        </div>
                        <div className="h-1.5 rounded bg-slate-700 overflow-hidden">
                          <div
                            className="h-full bg-forest-500 transition-all duration-500"
                            style={{ width: `${percentage}%` }}
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              <div>
                <p className="text-xs text-slate-300 uppercase tracking-wide mb-2">Model importance</p>
                <div className="space-y-2">
                  {result.factorWeights.map((f) => (
                    <div key={f.factor}>
                      <div className="flex justify-between text-xs text-slate-300 mb-1">
                        <span>{f.factor}</span>
                        <span>{Math.round(f.weight * 100)}%</span>
                      </div>
                      <div className="h-1.5 rounded bg-slate-700 overflow-hidden">
                        <div
                          className="h-full bg-forest-500"
                          style={{ width: `${f.weight * 100}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
