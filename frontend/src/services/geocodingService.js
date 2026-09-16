const NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

export async function searchLocation(query, signal) {
  const params = new URLSearchParams({
    q: query,
    format: "jsonv2",
    addressdetails: "1",
    countrycodes: "in",
    limit: "1",
  });
  const response = await fetch(`${NOMINATIM_URL}?${params}`, {
    signal,
    headers: { Accept: "application/json" },
  });
  if (!response.ok) throw new Error("Location search failed.");

  const results = await response.json();
  const match = results[0];
  if (!match) return null;

  const latitude = Number(match.lat);
  const longitude = Number(match.lon);
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return null;
  return { latitude, longitude, displayName: match.display_name };
}
