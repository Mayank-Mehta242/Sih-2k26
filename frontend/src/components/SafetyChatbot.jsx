import { useEffect, useRef, useState } from "react";
import { Bot, MessageCircle, Send, X } from "lucide-react";
import { incidentService } from "../services/incidentService.js";
import { weatherService } from "../services/weatherService.js";
import { NER_REGION_CENTER } from "../utils/constants.js";

const WELCOME_MESSAGE = {
  from: "bot",
  text: "Hello. Ask me about current weather or approved landslide reports in the North Eastern Region.",
};

const NER_LOCATIONS = [
  "arunachal pradesh",
  "assam",
  "manipur",
  "meghalaya",
  "mizoram",
  "nagaland",
  "sikkim",
  "tripura",
  "north east",
  "northeast",
  "north eastern",
  "north-eastern",
  "ner",
];

const NER_STATE_COORDINATES = [
  { label: "Arunachal Pradesh", key: "arunachal pradesh", lat: 27.0844, lng: 93.6053 },
  { label: "Assam", key: "assam", lat: 26.1445, lng: 91.7362 },
  { label: "Manipur", key: "manipur", lat: 24.817, lng: 93.9368 },
  { label: "Meghalaya", key: "meghalaya", lat: 25.5788, lng: 91.8933 },
  { label: "Mizoram", key: "mizoram", lat: 23.7271, lng: 92.7176 },
  { label: "Nagaland", key: "nagaland", lat: 25.6751, lng: 94.1086 },
  { label: "Sikkim", key: "sikkim", lat: 27.3389, lng: 88.6065 },
  { label: "Tripura", key: "tripura", lat: 23.8315, lng: 91.2868 },
];

const NON_NER_LOCATIONS = [
  "andhra pradesh",
  "bihar",
  "chhattisgarh",
  "goa",
  "gujarat",
  "haryana",
  "himachal pradesh",
  "jharkhand",
  "karnataka",
  "kerala",
  "madhya pradesh",
  "maharashtra",
  "odisha",
  "punjab",
  "rajasthan",
  "tamil nadu",
  "telangana",
  "uttar pradesh",
  "uttarakhand",
  "west bengal",
  "andaman",
  "chandigarh",
  "delhi",
  "jammu",
  "ladakh",
  "lakshadweep",
  "puducherry",
  "tehri",
  "garhwal",
];

function getRequestedState(question) {
  const normalizedQuestion = question.toLowerCase();
  return NER_STATE_COORDINATES.find((state) => normalizedQuestion.includes(state.key));
}

function createReply(question, weather, incidents, locationLabel = "regional") {
  const normalizedQuestion = question.toLowerCase();
  const mentionsNER = NER_LOCATIONS.some((location) => normalizedQuestion.includes(location));
  const mentionsNonNER = NON_NER_LOCATIONS.some((location) => normalizedQuestion.includes(location));
  const asksAboutSpecificLocation = /\b(?:in|near|at|around|from)\s+[a-z]/.test(normalizedQuestion);
  const asksWeather = /weather|rain|rainfall|temperature|temp|humidity|wind|condition|forecast/.test(normalizedQuestion);
  const asksLandslide = /landslide|slide|road block|debris|incident|blocked road|risk/.test(normalizedQuestion);

  if (mentionsNonNER || (asksAboutSpecificLocation && !mentionsNER)) {
    return "I can only answer questions about Arunachal Pradesh, Assam, Manipur, Meghalaya, Mizoram, Nagaland, Sikkim, and Tripura.";
  }

  if (asksWeather && weather) {
    if (weather.condition?.toLowerCase().includes("data unavailable")) {
      return "Live regional weather is temporarily unavailable. Please try again shortly.";
    }
    return `Current ${locationLabel} weather: ${weather.condition}, ${weather.temperatureC}°C, ${weather.humidityPct}% humidity, ${weather.rainfallMm} mm rainfall, and wind at ${weather.windKmh} km/h.`;
  }

  if (asksLandslide) {
    if (!incidents.length) return "There are no approved landslide or incident reports available right now.";
    const reportSummary = incidents
      .slice(0, 3)
      .map((incident) => `${incident.title} near ${incident.district || "the reported area"}`)
      .join("; ");
    const remaining = incidents.length > 3 ? ` There are ${incidents.length - 3} more approved reports.` : "";
    return `I found ${incidents.length} approved incident report${incidents.length === 1 ? "" : "s"}: ${reportSummary}.${remaining} Check the map and local authorities before travelling.`;
  }

  if (asksWeather || asksLandslide) return "I could not load the latest information. Please try again shortly.";
  return "I can help with regional weather and approved landslide or incident reports. Try asking, ‘What is the weather?’ or ‘Are there any landslide reports?’";
}

export default function SafetyChatbot() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([WELCOME_MESSAGE]);
  const [question, setQuestion] = useState("");
  const [weather, setWeather] = useState(null);
  const [incidents, setIncidents] = useState([]);
  const [stateWeather, setStateWeather] = useState({});
  const [loading, setLoading] = useState(false);
  const endRef = useRef(null);

  useEffect(() => {
    if (!open || weather || loading) return;
    setLoading(true);
    Promise.allSettled([
      weatherService.getWeather(NER_REGION_CENTER.lat, NER_REGION_CENTER.lng),
      incidentService.list("approved"),
    ])
      .then(([weatherResult, incidentResult]) => {
        if (weatherResult.status === "fulfilled") setWeather(weatherResult.value);
        if (incidentResult.status === "fulfilled") setIncidents(incidentResult.value);
      })
      .finally(() => setLoading(false));
  }, [open, weather, loading]);

  useEffect(() => {
    if (open) endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, open]);

  async function submitQuestion(event) {
    event.preventDefault();
    const trimmedQuestion = question.trim();
    if (!trimmedQuestion) return;
    const requestedState = getRequestedState(trimmedQuestion);
    const responseId = `bot-${Date.now()}`;
    const selectedWeather = requestedState ? stateWeather[requestedState.key] : weather;
    const selectedIncidents = requestedState
      ? incidents.filter((incident) => `${incident.title} ${incident.district || ""}`.toLowerCase().includes(requestedState.key))
      : incidents;
    setMessages((current) => [
      ...current,
      { from: "user", text: trimmedQuestion },
      { from: "bot", text: requestedState && !selectedWeather ? `Loading ${requestedState.label} weather...` : createReply(trimmedQuestion, selectedWeather, selectedIncidents, requestedState?.label) , id: responseId },
    ]);
    setQuestion("");

    if (requestedState && !selectedWeather) {
      try {
        const latestWeather = await weatherService.getWeather(requestedState.lat, requestedState.lng);
        setStateWeather((current) => ({ ...current, [requestedState.key]: latestWeather }));
        setMessages((current) => current.map((message) => (
          message.id === responseId
            ? { ...message, text: createReply(trimmedQuestion, latestWeather, selectedIncidents, requestedState.label) }
            : message
        )));
      } catch {
        setMessages((current) => current.map((message) => (
          message.id === responseId
            ? { ...message, text: "Live state weather is temporarily unavailable. Please try again shortly." }
            : message
        )));
      }
    }
  }

  return (
    <div className="fixed bottom-5 right-5 z-40 flex flex-col items-end gap-3 sm:bottom-6 sm:right-6">
      {open && (
        <section className="flex h-[min(30rem,calc(100vh-7rem))] max-h-[30rem] w-[calc(100vw-2rem)] max-w-sm flex-col overflow-hidden rounded-xl border border-[#b8dfe1] bg-white shadow-2xl shadow-[#102a43]/20" aria-label="PahadSuraksha assistant">
          <header className="flex items-center justify-between bg-[#102a43] px-4 py-3 text-white">
            <div className="flex items-center gap-2">
              <span className="flex h-8 w-8 items-center justify-center rounded-full bg-[#087f8c]">
                <Bot className="h-4 w-4" />
              </span>
              <div>
                <p className="text-sm font-bold">Safety assistant</p>
                <p className="text-xs text-[#b8eef0]">Weather and incident updates</p>
              </div>
            </div>
            <button type="button" onClick={() => setOpen(false)} className="rounded p-1 text-white hover:bg-white/15" aria-label="Close assistant" title="Close assistant">
              <X className="h-5 w-5" />
            </button>
          </header>

          <div className="flex-1 space-y-3 overflow-y-auto bg-[#f5f8fa] p-3" aria-live="polite">
            {messages.map((message, index) => (
              <div key={`${message.from}-${index}`} className={`flex ${message.from === "user" ? "justify-end" : "justify-start"}`}>
                <p className={`max-w-[88%] rounded-lg px-3 py-2 text-sm leading-5 ${message.from === "user" ? "bg-[#0b5266] text-white" : "border border-[#d5e3e7] bg-white text-[#243447]"}`}>
                  {message.text}
                </p>
              </div>
            ))}
            {loading && <p className="text-xs text-[#526579]">Loading regional updates…</p>}
            <div ref={endRef} />
          </div>

          <form onSubmit={submitQuestion} className="flex gap-2 border-t border-slate-200 bg-white p-3">
            <input
              className="input-field min-w-0 py-2 text-sm"
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="Ask about weather or landslides"
              aria-label="Ask the safety assistant"
            />
            <button type="submit" className="btn-primary shrink-0 px-3" aria-label="Send question" title="Send question">
              <Send className="h-4 w-4" />
            </button>
          </form>
        </section>
      )}
      <button type="button" onClick={() => setOpen((current) => !current)} className="flex h-14 w-14 items-center justify-center rounded-full bg-[#0b5266] text-white shadow-lg shadow-[#102a43]/25 transition-transform hover:scale-105 hover:bg-[#083f50]" aria-label={open ? "Close safety assistant" : "Open safety assistant"} title="Safety assistant">
        {open ? <X className="h-6 w-6" /> : <MessageCircle className="h-6 w-6" />}
      </button>
    </div>
  );
}
