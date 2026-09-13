import { useState } from "react";
import toast from "react-hot-toast";
import { AlertTriangle, Loader2, MapPin, Route } from "lucide-react";
import Card from "../components/Card.jsx";
import MapView from "../components/MapView.jsx";
import RiskBadge from "../components/RiskBadge.jsx";
import { districtService } from "../services/districtService.js";
import { incidentService } from "../services/incidentService.js";
import { predictionService } from "../services/predictionService.js";
import { weatherService } from "../services/weatherService.js";
import { normalizeRiskKey } from "../utils/riskUtils.js";

const CHECKPOINT_SPACING_KM = 5;
const DEFAULT_START = { lat: 26.58, lng: 90.62 };
const DEFAULT_END = { lat: 26.7, lng: 91.23 };
const DEFAULT_START_NAME = "Chirang";
const DEFAULT_END_NAME = "Baksa";
const PROTOTYPE_ROUTE = [
  ["Chirang", 26.58, 90.62],
  ["Bijni", 26.49, 90.70],
  ["Patiladaha", 26.43, 90.77],
  ["Barpeta Road", 26.50, 90.97],
  ["Howly", 26.42, 90.98],
  ["Sarthebari", 26.47, 91.04],
  ["Pathsala", 26.49, 91.17],
  ["Salbari", 26.43, 91.18],
  ["Mushalpur", 26.78, 91.28],
  ["Baksa", 26.70, 91.23],
];

function interpolateRoute(start, end, startName, endName) {
  const isPrototypeRoute = Math.abs(start.lat - DEFAULT_START.lat) < 0.001
    && Math.abs(start.lng - DEFAULT_START.lng) < 0.001
    && Math.abs(end.lat - DEFAULT_END.lat) < 0.001
    && Math.abs(end.lng - DEFAULT_END.lng) < 0.001;
  if (isPrototypeRoute) {
    const checkpoints = [];
    for (let segmentIndex = 0; segmentIndex < PROTOTYPE_ROUTE.length - 1; segmentIndex += 1) {
      const [fromName, fromLat, fromLng] = PROTOTYPE_ROUTE[segmentIndex];
      const [toName, toLat, toLng] = PROTOTYPE_ROUTE[segmentIndex + 1];
      const segmentDistance = distanceKm({ lat: fromLat, lng: fromLng }, { lat: toLat, lng: toLng });
      const segmentCount = Math.max(1, Math.ceil(segmentDistance / CHECKPOINT_SPACING_KM));

      for (let step = 0; step < segmentCount; step += 1) {
        const progress = step / segmentCount;
        checkpoints.push({
          lat: fromLat + (toLat - fromLat) * progress,
          lng: fromLng + (toLng - fromLng) * progress,
          location: step === 0 ? fromName : `${fromName} - ${toName}`,
        });
      }
    }
    const [lastName, lastLat, lastLng] = PROTOTYPE_ROUTE[PROTOTYPE_ROUTE.length - 1];
    checkpoints.push({ lat: lastLat, lng: lastLng, location: lastName });
    checkpoints[0].location = startName;
    checkpoints[checkpoints.length - 1].location = endName;
    return checkpoints.map((checkpoint, index) => ({ ...checkpoint, id: `route-point-${index + 1}`, index: index + 1 }));
  }

  const routeDistance = distanceKm(start, end);
  const checkpointCount = Math.max(2, Math.ceil(routeDistance / CHECKPOINT_SPACING_KM) + 1);
  return Array.from({ length: checkpointCount }, (_, index) => {
    const progress = index / (checkpointCount - 1);
    return {
      id: `route-point-${index + 1}`,
      index: index + 1,
      location: index === 0 ? startName : index === checkpointCount - 1 ? endName : `Route checkpoint ${index + 1}`,
      lat: start.lat + (end.lat - start.lat) * progress,
      lng: start.lng + (end.lng - start.lng) * progress,
    };
  });
}

function nearestDistrict(point, districts) {
  return districts.reduce((nearest, district) => {
    const distance = Math.hypot(point.lat - district.lat, point.lng - district.lng);
    return !nearest || distance < nearest.distance ? { district, distance } : nearest;
  }, null)?.district;
}

function riskRank(level) {
  return { low: 1, medium: 2, high: 3, extreme: 4 }[normalizeRiskKey(level)] ?? 1;
}

function highestRiskLevel(first, second) {
  return riskRank(first) >= riskRank(second) ? first : second;
}

function distanceKm(first, second) {
  const latitudeDistance = (first.lat - second.lat) * 111;
  const longitudeDistance = (first.lng - second.lng) * 111 * Math.cos((first.lat * Math.PI) / 180);
  return Math.hypot(latitudeDistance, longitudeDistance);
}

export default function PredictionPage() {
  const [start, setStart] = useState(DEFAULT_START);
  const [end, setEnd] = useState(DEFAULT_END);
  const [startName, setStartName] = useState(DEFAULT_START_NAME);
  const [endName, setEndName] = useState(DEFAULT_END_NAME);
  const [routePoints, setRoutePoints] = useState([]);
  const [incidents, setIncidents] = useState([]);
  const [districts, setDistricts] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    const parsedStart = { lat: Number(start.lat), lng: Number(start.lng) };
    const parsedEnd = { lat: Number(end.lat), lng: Number(end.lng) };
    if ([start.lat, start.lng, end.lat, end.lng].some((value) => value === "" || value == null || !Number.isFinite(Number(value)))) {
      setError("Choose a location from the prototype place suggestions.");
      return;
    }

    setLoading(true);
    setError("");
    setRoutePoints([]);
    try {
      const monitoredDistricts = districts ?? await districtService.list();
      setDistricts(monitoredDistricts);
      const points = interpolateRoute(parsedStart, parsedEnd, startName, endName);
      const results = [];
      let fallbackUsed = false;
      for (const point of points) {
        const district = nearestDistrict(point, monitoredDistricts);
        let weather;
        try {
          weather = await weatherService.getWeather(point.lat, point.lng);
        } catch (weatherError) {
          fallbackUsed = true;
          weather = { rainfallMm: 10, humidityPct: 70, temperatureC: 16 };
        }

        let prediction;
        try {
          prediction = await predictionService.predict({
            rainfall: weather.rainfallMm,
            humidity: weather.humidityPct,
            temperature: weather.temperatureC,
            elevation: district?.elevation ?? 500 + point.index * 25,
            slope: district?.slope ?? 25 + (point.index % 3) * 4,
            historicalIncidents: district?.incidents ?? 0,
          });
        } catch (predictionError) {
          fallbackUsed = true;
          prediction = {
            riskLevel: district?.risk?.toUpperCase() ?? "MEDIUM",
            confidence: 50,
            reasons: ["Using the monitored district baseline while live prediction is unavailable"],
            factorWeights: [],
          };
        }
        const effectiveRisk = highestRiskLevel(prediction.riskLevel, district?.risk ?? "LOW");
        results.push({
          ...point,
          ...prediction,
          riskLevel: effectiveRisk,
          districtName: district?.name ?? point.location,
        });
      }
      if (fallbackUsed) toast("Some route points used monitored district baseline data.");
      setRoutePoints(results);
      try {
        setIncidents(await incidentService.list("approved"));
      } catch (incidentError) {
        setIncidents([]);
      }
    } catch (err) {
      toast.error("Could not analyze this route. Please try again.");
      setError("Weather or prediction data is unavailable for this route.");
    } finally {
      setLoading(false);
    }
  }

  const highestRisk = routePoints.reduce(
    (highest, point) => riskRank(point.riskLevel) > riskRank(highest) ? point.riskLevel : highest,
    "LOW"
  );
  const pronePoints = routePoints.filter((point) => riskRank(point.riskLevel) >= riskRank("HIGH"));
  const nearbyReports = incidents
    .map((report) => {
      const nearestPoint = routePoints.reduce((nearest, point) => {
        const distance = distanceKm(report, point);
        return !nearest || distance < nearest.distance ? { point, distance } : nearest;
      }, null);
      return nearestPoint ? { report, ...nearestPoint } : null;
    })
    .filter((match) => match && match.distance <= 15);
  const mapPoints = routePoints.length > 0
    ? routePoints
    : [
        { id: "manual-start", index: "S", location: startName, lat: Number(start.lat), lng: Number(start.lng), riskLevel: "LOW", districtName: startName, confidence: "-" },
        { id: "manual-end", index: "E", location: endName, lat: Number(end.lat), lng: Number(end.lng), riskLevel: "LOW", districtName: endName, confidence: "-" },
      ];

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-2">Route Landslide Risk</h1>
      <p className="text-slate-200 mb-6">
        Enter two locations to identify landslide-prone sections along your route before you travel.
      </p>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card title="Route Details">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid sm:grid-cols-2 gap-4">
              {[{ label: "Start point", value: start, setter: setStart, name: startName, setName: setStartName }, { label: "End point", value: end, setter: setEnd, name: endName, setName: setEndName }].map((point) => (
                <fieldset key={point.label} className="space-y-3">
                  <legend className="text-sm font-semibold text-white flex items-center gap-2">
                    <MapPin className="h-4 w-4 text-forest-400" />{point.label}
                  </legend>
                  <label className="block text-xs text-slate-300">
                    Location name
                    <input
                      list="prototype-locations"
                      className="input-field mt-1"
                      value={point.name}
                      onChange={(input) => {
                        const name = input.target.value;
                        point.setName(name);
                        const match = PROTOTYPE_ROUTE.find(([location]) => location.toLowerCase() === name.trim().toLowerCase());
                        if (match) point.setter({ lat: match[1], lng: match[2] });
                        else point.setter({ lat: "", lng: "" });
                      }}
                      placeholder="e.g. Chirang"
                    />
                  </label>
                </fieldset>
              ))}
            </div>
            <datalist id="prototype-locations">
              {PROTOTYPE_ROUTE.map(([location]) => <option key={location} value={location} />)}
            </datalist>
            <div className="flex items-center gap-2 text-xs text-slate-300 bg-slate-900/30 rounded-lg p-3">
              <Route className="h-4 w-4 shrink-0 text-risk-medium" /> Route conditions are checked across the journey between both locations.
            </div>
            {error && <p className="text-sm text-risk-high">{error}</p>}
            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Route className="h-4 w-4" />}
              {loading ? "Analyzing route…" : "Analyze Route"}
            </button>
          </form>
        </Card>

        <Card title="Route Overview">
          {!routePoints.length && !loading && <p className="text-sm text-slate-200 py-8 text-center">Analyze a route to see risk at each sampled location.</p>}
          {loading && <p className="text-sm text-slate-200 py-8 text-center">Calculating…</p>}
          {routePoints.length > 0 && (
            <div className="space-y-3">
              <div className="flex items-center justify-between text-sm text-slate-300">
                <span>{pronePoints.length} possible landslide-prone areas</span><RiskBadge level={highestRisk} />
              </div>
              {pronePoints.length === 0 && <p className="text-sm text-slate-200 py-6 text-center">No landslide-prone area. Happy journey.</p>}
              <div className="max-h-80 overflow-y-auto space-y-2 pr-1">
                {pronePoints.map((point) => (
                  <div key={point.id} className="flex items-center justify-between gap-3 border border-slate-700 rounded-lg p-3">
                    <div className="min-w-0"><p className="text-sm font-semibold text-white">{point.location} <span className="text-slate-400 font-normal">({point.lat.toFixed(3)}, {point.lng.toFixed(3)})</span></p><p className="text-xs text-slate-300 truncate">Sample {point.index} · {point.districtName}</p></div>
                    <div className="text-right shrink-0"><RiskBadge level={point.riskLevel} /><p className="text-xs text-slate-400 mt-1">{point.confidence}% confidence</p></div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </Card>
      </div>
      {routePoints.length > 0 && (
        <Card title="Incident Reports Along Route">
          {nearbyReports.length === 0 ? (
            <p className="text-sm text-slate-200 py-4 text-center">No approved incident reports near this route.</p>
          ) : (
            <div className="space-y-3">
              {nearbyReports.map(({ report, point, distance }) => (
                <div key={report.id} className="flex items-start justify-between gap-4 border border-slate-700 rounded-lg p-3">
                  <div className="flex gap-3 min-w-0">
                    <AlertTriangle className="h-5 w-5 text-risk-high shrink-0 mt-0.5" />
                    <div className="min-w-0">
                      <p className="text-sm font-semibold text-white">{report.title}</p>
                      <p className="text-xs text-slate-300 mt-1">Near {point.location} · {report.district || "Route area"} · {distance.toFixed(1)} km away</p>
                      {report.createdAt && <p className="text-xs text-slate-400 mt-1">Reported {report.createdAt}</p>}
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <RiskBadge level={point.riskLevel} />
                    <p className="text-xs text-slate-400 mt-1">route risk</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      )}
      <div className="mt-6">
        <p className="text-sm text-slate-300 mb-2">Route preview</p>
        <MapView routePoints={mapPoints} districts={routePoints.length > 0 ? districts ?? [] : []} height="520px" />
      </div>
    </div>
  );
}
