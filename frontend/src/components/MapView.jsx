import { useEffect } from "react";
import { MapContainer, TileLayer, CircleMarker, Popup, Polyline, useMap, useMapEvents } from "react-leaflet";
import { NER_REGION_CENTER, DEFAULT_ZOOM } from "../utils/constants.js";
import { normalizeRiskKey, RISK_STYLES } from "../utils/riskUtils.js";

const RISK_HEX = {
  low: "#22A567",
  medium: "#E0B324",
  high: "#2563EB",
  extreme: "#7C3AED",
};

function ClickCapture({ onMapClick }) {
  useMapEvents({
    click(e) {
      onMapClick?.(e.latlng);
    },
  });
  return null;
}

function LocationMarker({ position }) {
  const map = useMap();

  useEffect(() => {
    if (position) map.flyTo([position.lat, position.lng], Math.max(map.getZoom(), 12), { duration: 1 });
  }, [map, position]);

  if (!position) return null;
  return (
    <CircleMarker
      center={[position.lat, position.lng]}
      radius={8}
      pathOptions={{ color: "#14532d", fillColor: "#4ade80", fillOpacity: 0.9, weight: 3 }}
    >
      <Popup>You are here</Popup>
    </CircleMarker>
  );
}

function RouteOverlay({ points }) {
  const map = useMap();
  const coordinates = points.map((point) => [point.lat, point.lng]);

  useEffect(() => {
    if (coordinates.length > 1) {
      map.fitBounds(coordinates, { padding: [28, 28] });
    }
  }, [map, points]);

  if (coordinates.length < 2) return null;
  return <Polyline positions={coordinates} pathOptions={{ color: "#f59e0b", weight: 4, opacity: 0.9 }} />;
}

export default function MapView({ districts = [], routePoints = [], onSelectDistrict, onMapClick, userPosition, height = "480px" }) {
  return (
    <div style={{ height }} className="rounded overflow-hidden border border-slate-700">
      <MapContainer
        center={[NER_REGION_CENTER.lat, NER_REGION_CENTER.lng]}
        zoom={DEFAULT_ZOOM}
        style={{ height: "100%", width: "100%" }}
        scrollWheelZoom
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ClickCapture onMapClick={onMapClick} />
        <LocationMarker position={userPosition} />
        <RouteOverlay points={routePoints} />
        {routePoints.map((point) => (
          <CircleMarker
            key={point.id}
            center={[point.lat, point.lng]}
            radius={8}
            pathOptions={{
              color: RISK_HEX[normalizeRiskKey(point.riskLevel)],
              fillColor: RISK_HEX[normalizeRiskKey(point.riskLevel)],
              fillOpacity: 0.9,
              weight: 2,
            }}
          >
            <Popup>
              <div className="text-sm">
                <p className="font-semibold">{point.location ?? `Point ${point.index}`}</p>
                <p>{point.districtName}</p>
                <p>Risk: {RISK_STYLES[normalizeRiskKey(point.riskLevel)].label}</p>
                <p>Confidence: {point.confidence}%</p>
              </div>
            </Popup>
          </CircleMarker>
        ))}
        {districts.map((d) => (
          <CircleMarker
            key={d.id}
            center={[d.lat, d.lng]}
            radius={10}
            pathOptions={{
              color: RISK_HEX[normalizeRiskKey(d.risk)],
              fillColor: RISK_HEX[normalizeRiskKey(d.risk)],
              fillOpacity: 0.82,
              weight: 2,
            }}
            eventHandlers={{
              click: () => onSelectDistrict?.(d),
            }}
          >
            <Popup>
              <div className="text-sm">
                <p className="font-semibold">{d.name}</p>
                <p>Risk: {RISK_STYLES[normalizeRiskKey(d.risk)].label}</p>
                <p>Incidents (YTD): {d.incidents}</p>
              </div>
            </Popup>
          </CircleMarker>
        ))}
      </MapContainer>
    </div>
  );
}
