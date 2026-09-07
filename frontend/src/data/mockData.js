// Mock data for development
// In production, this is replaced by API calls when VITE_USE_MOCK_DATA=false

export const mockDistricts = [
  { id: "tawang", name: "Tawang, Arunachal Pradesh", lat: 27.586, lng: 91.859, risk: "high", incidents: 18 },
  { id: "dima-hasao", name: "Dima Hasao, Assam", lat: 25.5, lng: 93.0, risk: "medium", incidents: 14, elevation: 850, slope: 28 },
  { id: "chirang", name: "Chirang, Assam", lat: 26.58, lng: 90.62, risk: "high", incidents: 16, elevation: 180, slope: 22 },
  { id: "baksa", name: "Baksa, Assam", lat: 26.7, lng: 91.23, risk: "high", incidents: 20, elevation: 240, slope: 26 },
  { id: "imphal-west", name: "Imphal West, Manipur", lat: 24.78, lng: 93.94, risk: "medium", incidents: 11 },
  { id: "east-khasi-hills", name: "East Khasi Hills, Meghalaya", lat: 25.58, lng: 91.89, risk: "high", incidents: 21 },
  { id: "aizawl", name: "Aizawl, Mizoram", lat: 23.73, lng: 92.72, risk: "high", incidents: 17 },
  { id: "kohima", name: "Kohima, Nagaland", lat: 25.67, lng: 94.11, risk: "medium", incidents: 13 },
  { id: "gangtok", name: "Gangtok, Sikkim", lat: 27.33, lng: 88.61, risk: "high", incidents: 19 },
  { id: "west-tripura", name: "West Tripura, Tripura", lat: 23.83, lng: 91.28, risk: "low", incidents: 7 },
];

export const mockStats = {
  monitoredDistricts: 8,
  monitoredDistrictName: "NER region",
  reportedIncidents: 56,
  activeUsers: 234,
};

export const mockHistorical = {
  monthly: [
    { month: "Apr", count: 8 },
    { month: "May", count: 11 },
    { month: "Jun", count: 15 },
    { month: "Jul", count: 22 },
  ],
  yearly: [
    { year: 2024, count: 42 },
    { year: 2025, count: 51 },
    { year: 2026, count: 56 },
  ],
  topVulnerable: ["East Khasi Hills", "Tawang", "Gangtok", "Aizawl"],
};

export const mockWeather = {
  location: "North Eastern Region",
  temperatureC: 14,
  humidityPct: 75,
  rainfallMm: 35,
  windKmh: 16,
  elevationM: 1950,
  condition: "Moderate Rain",
  forecast: [
    { day: "Mon", tempC: 14, rainMm: 35, humidityPct: 75 },
    { day: "Tue", tempC: 13, rainMm: 45, humidityPct: 80 },
    { day: "Wed", tempC: 15, rainMm: 25, humidityPct: 70 },
  ],
};

export const mockPredictionResult = {
  riskLevel: "HIGH",
  confidence: 87,
  reasons: [
    "Heavy rainfall over past 48 hours",
    "Steep terrain in this area",
    "Historical incidents nearby",
  ],
  factorWeights: [
    { factor: "Rainfall", weight: 0.35 },
    { factor: "Slope", weight: 0.30 },
    { factor: "Historical incidents", weight: 0.25 },
    { factor: "Humidity", weight: 0.10 },
  ],
};

export const mockReports = [
  {
    id: "r1",
    title: "Debris on Badrinath highway near Pipalkoti",
    district: "Tawang, Arunachal Pradesh",
    lat: 30.28,
    lng: 79.35,
    status: "approved",
    createdAt: "2026-07-21",
  },
  {
    id: "r2",
    title: "Road blockage after intense rainfall near Tawang",
    district: "Tawang, Arunachal Pradesh",
    lat: 27.59,
    lng: 91.86,
    status: "pending",
    createdAt: "2026-07-25",
  },
];

export const emergencyContacts = [
  { label: "MDoNER / NER coordination", number: "011-2345-1234" },
  { label: "National Emergency", number: "112" },
  { label: "Ambulance", number: "108" },
];
