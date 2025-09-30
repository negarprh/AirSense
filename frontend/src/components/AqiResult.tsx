import { AdviceResponse, AqiResponse, HistoryPoint } from "../api";
import ForecastChart, { ForecastPoint } from "./ForecastChart";

interface AqiResultProps {
  aqi: AqiResponse | null;
  advice: AdviceResponse | null;
  asthma: boolean;
  error: string | null;
  loading: boolean;
}

interface BandInfo {
  label: string;
  color: string;
  tagline: string;
}

const AqiResult = ({ aqi, advice, asthma, error, loading }: AqiResultProps) => {
  if (loading) {
    return (
      <div className="result-card glassy-card">
        <p className="status-message">Checking air quality...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="result-card glassy-card error">
        <p className="status-message">{error}</p>
      </div>
    );
  }

  if (!aqi || !advice) {
    return (
      <div className="result-card glassy-card">
        <p className="status-message">Search for a city to view live air quality insights.</p>
      </div>
    );
  }

  const bandInfo = getBandInfo(aqi.aqi);
  const updatedAt = new Date(aqi.timestamp);
  const forecast = buildForecast(aqi.aqi, aqi.history);

  return (
    <div className="result-card glassy-card">
      <div
        className="result-header"
        style={{ background: `linear-gradient(135deg, ${bandInfo.color}E6, ${bandInfo.color}88)` }}
      >
        <div className="aqi-pill">
          <span className="aqi-value">{aqi.aqi}</span>
          <div className="aqi-band-details">
            <span className="aqi-band">{aqi.band}</span>
            <span className="aqi-band-sub">{bandInfo.tagline}</span>
          </div>
        </div>
        <div className="aqi-meta">
          <span className="aqi-city">{aqi.city}</span>
          <span>Primary pollutant: {aqi.mainPollutant}</span>
          <span>Updated: {updatedAt.toLocaleString()}</span>
        </div>
      </div>

      <div className="result-body">
        <section className="result-section">
          <h3>Health Guidance</h3>
          <p className="advice-main">{advice.publicAdvice}</p>
          <p className="advice-sensitive">
            {asthma ? "For asthma or sensitive airways:" : "Sensitive groups:"} {advice.sensitiveAdvice}
          </p>
          <small className="advice-note">{advice.pollutantNote}</small>
        </section>

        <section className="result-section">
          <h3>3-Day Outlook</h3>
          <ForecastChart forecast={forecast} />
        </section>
      </div>
    </div>
  );
};

const buildForecast = (currentAqi: number, history: HistoryPoint[] = []): ForecastPoint[] => {
  const effectiveHistory = history.filter((point) => !Number.isNaN(point.aqi));
  const recentSlice = effectiveHistory.slice(-6);
  const reference = recentSlice.length > 0 ? recentSlice[0].aqi : currentAqi;
  const slope = (currentAqi - reference) / Math.max(1, recentSlice.length - 1);

  return [1, 2, 3].map((day) => {
    const projected = clamp(Math.round(currentAqi + slope * day * 2 + day * 5), 5, 350);
    const info = getBandInfo(projected);
    const label = new Intl.DateTimeFormat(undefined, { weekday: "short" }).format(addDays(new Date(), day));
    return {
      label,
      value: projected,
      band: info.label,
      color: info.color
    };
  });
};

const getBandInfo = (aqi: number): BandInfo => {
  if (aqi <= 50) {
    return { label: "Good", color: "#00B050", tagline: "Air quality is clean." };
  }
  if (aqi <= 100) {
    return { label: "Moderate", color: "#92D050", tagline: "Fair with minor caution." };
  }
  if (aqi <= 150) {
    return {
      label: "Unhealthy for Sensitive Groups",
      color: "#FFC000",
      tagline: "Sensitive lungs should take it easy."
    };
  }
  if (aqi <= 200) {
    return { label: "Unhealthy", color: "#FF0000", tagline: "Limit outdoor time." };
  }
  if (aqi <= 300) {
    return { label: "Very Unhealthy", color: "#7030A0", tagline: "Stay indoors when possible." };
  }
  return { label: "Hazardous", color: "#7F0000", tagline: "Emergency conditions: avoid exposure." };
};

const clamp = (value: number, min: number, max: number) => Math.min(Math.max(value, min), max);

const addDays = (date: Date, days: number) => {
  const copy = new Date(date);
  copy.setDate(copy.getDate() + days);
  return copy;
};

export default AqiResult;


