import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, CalendarDays, MapPin, MessageSquare, ShieldCheck, ChevronLeft, ChevronRight } from "lucide-react";
import Card from "../components/Card.jsx";
import LoadingSpinner from "../components/LoadingSpinner.jsx";
import { incidentService } from "../services/incidentService.js";
import arunachalImage from "../../../images/arunachal.jpg";
import assamImage from "../../../images/assam_0.jpg";
import manipurImage from "../../../images/manipur_0 (1).jpg";
import meghalayaImage from "../../../images/meghalaya_0.jpg";
import mizoramImage from "../../../images/mizoram_0.jpg";
import nagalandImage from "../../../images/nagaland_0.jpg.jpeg";

export default function LandingPage() {
  const [incidents, setIncidents] = useState(null);
  const [activeSlide, setActiveSlide] = useState(0);

  const regionalSlides = [
    {
      name: "Arunachal Pradesh",
      image: arunachalImage,
      caption: "Mountain corridors and high-altitude communities",
    },
    {
      name: "Assam",
      image: assamImage,
      caption: "River plains, rainfall, and connected districts",
    },
    {
      name: "Manipur",
      image: manipurImage,
      caption: "Hill communities, roads, and seasonal rainfall",
    },
    {
      name: "Meghalaya",
      image: meghalayaImage,
      caption: "Waterfalls, forests, and vulnerable hill roads",
    },
    {
      name: "Mizoram",
      image: mizoramImage,
      caption: "Hilly corridors, bridges, and active monsoon terrain",
    },
    {
      name: "Nagaland",
      image: nagalandImage,
      caption: "Living landscapes supported by local reporting",
    },
  ];

  useEffect(() => {
    incidentService.list("approved").then(setIncidents);
  }, []);

  useEffect(() => {
    const timer = window.setInterval(() => {
      setActiveSlide((current) => (current + 1) % regionalSlides.length);
    }, 6500);
    return () => window.clearInterval(timer);
  }, [regionalSlides.length]);

  const slide = regionalSlides[activeSlide];

  return (
    <div>
      <div className="border-b border-[#d5e3e7] bg-[#fffdf4] px-4 py-2.5 text-sm text-[#526579]">
        <div className="mx-auto flex max-w-7xl items-center gap-3 sm:px-6 lg:px-8">
          <span className="bg-[#e59b2f] px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-white">Notice</span>
          <span>Regional risk information is updated from weather, terrain, and verified community reports.</span>
        </div>
      </div>

      <section className="relative isolate overflow-hidden border-b border-[#dbe5ea] bg-[#102a43] px-4 py-14 sm:py-20">
        {regionalSlides.map((item, index) => (
          <div
            key={item.name}
            className={`absolute inset-0 -z-10 bg-cover bg-center transition-opacity duration-1000 ${index === activeSlide ? "opacity-55" : "opacity-0"}`}
            style={{ backgroundImage: `url(${item.image})` }}
            aria-hidden="true"
          />
        ))}
        <div className="absolute inset-0 -z-10 bg-gradient-to-r from-[#102a43]/80 via-[#102a43]/50 to-[#102a43]/20" />
        <div className="mx-auto grid max-w-7xl items-center gap-12 lg:grid-cols-[1.15fr_0.85fr]">
          <div>
            <div className="mb-5 inline-flex items-center gap-2 text-xs font-bold uppercase tracking-[0.14em] text-[#b8eef0]">
              <ShieldCheck className="h-4 w-4" /> Official regional monitoring platform
            </div>
            <h1 className="max-w-3xl text-4xl font-bold leading-tight text-white sm:text-5xl lg:text-6xl">
              Safer journeys through earlier warnings
            </h1>
            <p className="mt-6 max-w-2xl text-base leading-8 text-slate-200 sm:text-lg">
            Monitor landslides, flash floods, road blockages, and slope failures across the North Eastern Region.
            Combine live weather, terrain, and community reports to support preventive action.
            </p>
            <div className="mt-8 flex flex-col items-start gap-4 sm:flex-row">
              <Link to="/dashboard/risk" className="btn-primary">
                Check Travel Risk <ArrowRight className="h-4 w-4" />
              </Link>
              <Link to="/report-incident" className="btn-secondary">
                Report an Incident
              </Link>
            </div>
          </div>
          <div className="relative overflow-hidden rounded-lg border border-white/30 bg-white/10 p-7 text-white shadow-xl shadow-black/20 backdrop-blur-sm">
            <div className="absolute right-0 top-0 h-28 w-28 border-l border-b border-[#3d637a]" />
            <p className="text-xs font-bold uppercase tracking-[0.18em] text-[#9fd9dc]">Regional situation desk</p>
            <p className="mt-8 text-5xl font-bold">08</p>
            <p className="mt-1 text-sm text-slate-300">states monitored across the NER</p>
            <div className="mt-8 grid grid-cols-2 gap-4 border-t border-[#3d637a] pt-5 text-sm">
              <div><strong className="block text-xl text-white">24/7</strong><span className="text-slate-300">risk observation</span></div>
              <div><strong className="block text-xl text-white">Live</strong><span className="text-slate-300">community reports</span></div>
            </div>
            <div className="mt-8 flex items-center justify-between border-t border-white/25 pt-4">
              <div>
                <p className="text-xs font-bold uppercase tracking-[0.14em] text-[#b8eef0]">Featured region</p>
                <p className="mt-1 text-lg font-semibold">{slide.name}</p>
                <p className="text-xs text-slate-300">{slide.caption}</p>
              </div>
              <div className="flex gap-1.5">
                {regionalSlides.map((item, index) => (
                  <button
                    key={item.name}
                    type="button"
                    onClick={() => setActiveSlide(index)}
                    className={`h-1.5 transition-all ${index === activeSlide ? "w-8 bg-white" : "w-3 bg-white/40"}`}
                    aria-label={`Show ${item.name}`}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
        <div className="mx-auto mt-8 flex max-w-7xl items-center justify-between border-t border-white/20 pt-4 text-xs text-slate-300">
          <span>Regional visual brief | {slide.name}</span>
          <div className="flex gap-2">
            <button type="button" onClick={() => setActiveSlide((activeSlide - 1 + regionalSlides.length) % regionalSlides.length)} className="border border-white/30 p-1.5 transition-colors hover:bg-white/15" aria-label="Previous region"><ChevronLeft className="h-4 w-4" /></button>
            <button type="button" onClick={() => setActiveSlide((activeSlide + 1) % regionalSlides.length)} className="border border-white/30 p-1.5 transition-colors hover:bg-white/15" aria-label="Next region"><ChevronRight className="h-4 w-4" /></button>
          </div>
        </div>
      </section>

      <section className="border-b border-[#dbe5ea] bg-[#f5f8fa] px-4 py-12 sm:py-16">
        <div className="mx-auto grid max-w-7xl gap-10 lg:grid-cols-[0.9fr_1.1fr]">
          <div>
            <p className="mb-3 text-xs font-bold uppercase tracking-[0.16em] text-[#087f8c]">About the platform</p>
            <h2 className="mb-5 text-3xl font-bold text-[#102a43]">Coordinated intelligence for the North Eastern Region</h2>
            <p className="max-w-xl text-sm leading-7 text-[#526579]">
              PahadSuraksha AI supports citizens, field officials, and district authorities with timely information for safer movement and faster preventive action across all eight North Eastern states.
            </p>
            <Link to="/dashboard" className="mt-6 inline-flex items-center gap-2 text-sm font-bold text-[#0b5266] hover:text-[#083f50]">
              Open the regional map <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
          <div className="grid gap-4 sm:grid-cols-3">
            {["Weather and rainfall", "Terrain and slope", "Citizen reports"].map((label, index) => (
              <div key={label} className="border-t-4 border-[#0b5266] bg-white p-5 shadow-sm">
                <p className="text-3xl font-bold text-[#102a43]">0{index + 1}</p>
                <p className="mt-8 text-sm font-semibold text-[#102a43]">{label}</p>
                <p className="mt-2 text-xs leading-5 text-[#526579]">Signals combined for practical local risk awareness.</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 sm:py-16 lg:px-8">
        <div className="mb-8 flex items-end justify-between gap-4 border-b border-[#dbe5ea] pb-4">
          <div>
            <p className="mb-2 text-xs font-bold uppercase tracking-[0.16em] text-[#087f8c]">Public information</p>
            <h2 className="text-2xl font-bold text-[#102a43]">What’s new</h2>
          </div>
          <Link to="/dashboard" className="hidden text-sm font-semibold text-[#0b5266] sm:block">View monitoring map</Link>
        </div>
        {!incidents ? (
          <LoadingSpinner label="Loading approved incidents" />
        ) : incidents.length === 0 ? (
          <div className="card text-center text-[#526579]">No approved incident reports yet.</div>
        ) : (
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {incidents.map((incident) => (
              <Card key={incident.id} className="border-t-4 border-t-[#0b5266] p-5">
                {incident.imageUrl && (
                  <img
                    src={incident.imageUrl}
                    alt={`Evidence for ${incident.title}`}
                    className="mb-4 h-40 w-full rounded object-cover border border-slate-600"
                  />
                )}
                <h3 className="font-semibold text-[#102a43]">{incident.title}</h3>
                <div className="mt-3 flex flex-wrap gap-3 text-xs text-[#526579]">
                  <span className="inline-flex items-center gap-1"><MapPin className="h-3.5 w-3.5" />{incident.district}</span>
                  <span className="inline-flex items-center gap-1"><CalendarDays className="h-3.5 w-3.5" />{incident.createdAt}</span>
                </div>
                {incident.reviewComment && (
                  <p className="mt-3 inline-flex gap-2 text-sm text-[#526579]">
                    <MessageSquare className="h-4 w-4 shrink-0 text-[#087f8c]" />
                    {incident.reviewComment}
                  </p>
                )}
              </Card>
            ))}
          </div>
        )}
      </section>

    </div>
  );
}
