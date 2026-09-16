import { PhoneCall } from "lucide-react";
import { emergencyContacts } from "../data/mockData.js";
import logo from "../ChatGPT.png";

export default function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-[#102a43] text-white">
      <div className="mx-auto grid max-w-7xl gap-10 px-4 py-12 sm:px-6 md:grid-cols-[1.3fr_1fr_1fr] lg:px-8">
        <div>
          <div className="flex items-center gap-2 mb-3">
            <img src={logo} alt="PahadSuraksha logo" className="h-7 w-7 rounded object-cover" />
            <span className="font-bold text-white">PahadSuraksha</span>
          </div>
          <p className="text-sm text-slate-300">
            AI-enabled monitoring and early risk intelligence for the North Eastern Region, supporting MDoNER and
            state disaster management authorities.
          </p>
        </div>

        <div>
          <p className="mb-3 font-semibold text-white">Important links</p>
          <ul className="space-y-2 text-sm text-slate-300">
            <li><a href="https://mdoner.gov.in/" target="_blank" rel="noreferrer" className="hover:text-white">Ministry of DoNER</a></li>
            <li><a href="https://india.gov.in/" target="_blank" rel="noreferrer" className="hover:text-white">National Portal of India</a></li>
            <li><a href="/" className="hover:text-white">Citizen services</a></li>
            <li><a href="/report-incident" className="hover:text-white">Report an incident</a></li>
          </ul>
        </div>

        <div>
          <p className="font-semibold text-white mb-3">Emergency contacts</p>
          <ul className="space-y-2">
            {emergencyContacts.map((c) => (
              <li key={c.number} className="flex items-start gap-2 text-sm p-2 rounded-md hover:bg-white/10 transition-colors">
                <PhoneCall className="h-4 w-4 text-risk-high shrink-0 mt-0.5" />
                <div>
                  <span className="text-slate-300">{c.label}</span>
                  <span className="font-mono text-white ml-2">{c.number}</span>
                </div>
              </li>
            ))}
          </ul>
        </div>

      </div>
      <div className="border-t border-white/15 py-4 text-center text-xs text-slate-300">
        © {new Date().getFullYear()} PahadSuraksha AI | Government of India style regional monitoring service
      </div>
    </footer>
  );
}
