import { CSSProperties, useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import { api, AqiResponse, StationCoords } from "../api";

interface InsightsPageProps {
  city: string;
  hasSearched: boolean;
  onBack: () => void;
}

interface CategoryInfo {
  image: "green" | "yellow" | "orange" | "red";
  label: string;
  accent: string;
  range: string;
  summary: string;
  guidance: { general: string; sensitive: string };
}

const statusVariants = {
  initial: { opacity: 0, y: 10 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -10 }
};
const CATEGORY_STEPS: Array<{ threshold: number; info: CategoryInfo }> = [
  {
    threshold: 50,
    info: {
      image: "green",
      label: "Good",
      accent: "#4ade80",
      range: "AQI 0-50 | Minimal risk",
      summary: "Air quality is clean. Outdoor activities are encouraged for everyone.",
      guidance: {
        general: "Enjoy the outdoors with no restrictions.",
        sensitive: "No extra precautions required today."
      }
    }
  },
  {
    threshold: 100,
    info: {
      image: "yellow",
      label: "Moderate",
      accent: "#facc15",
      range: "AQI 51-100 | Acceptable",
      summary: "Air quality is acceptable; some sensitive people might notice irritation.",
      guidance: {
        general: "Keep outdoor plans but stay aware of any unusual symptoms.",
        sensitive: "Consider shorter outdoor sessions or lighter exertion if you are pollution sensitive."
      }
    }
  },
  {
    threshold: 150,
    info: {
      image: "orange",
      label: "Unhealthy for Sensitive Groups",
      accent: "#fb923c",
      range: "AQI 101-150 | Elevated",
      summary: "Sensitive groups may experience health effects. The general public is less likely to be affected.",
      guidance: {
        general: "Most people can continue as usual but consider breaks during heavy activity.",
        sensitive: "Limit prolonged or intense outdoor exertion and aim for mornings or indoor spaces."
      }
    }
  },
  {
    threshold: 200,
    info: {
      image: "red",
      label: "Unhealthy",
      accent: "#f87171",
      range: "AQI 151-200 | High",
      summary: "Everyone may begin to experience effects; sensitive groups could feel more serious symptoms.",
      guidance: {
        general: "Cut back strenuous outdoor exercise and take frequent breaks indoors.",
        sensitive: "Avoid outdoor exertion; use a high-quality mask or stay in filtered spaces when outside."
      }
    }
  },
  {
    threshold: 300,
    info: {
      image: "red",
      label: "Very Unhealthy",
      accent: "#c084fc",
      range: "AQI 201-300 | Very high",
      summary: "Health warnings of emergency conditions. The entire population is likely to be affected.",
      guidance: {
        general: "Stay indoors with clean air if possible. Postpone outdoor plans and close windows.",
        sensitive: "Remain indoors, use purified air, and consult care providers if symptoms appear."
      }
    }
  },
  {
    threshold: 500,
    info: {
      image: "red",
      label: "Hazardous",
      accent: "#f472b6",
      range: "AQI 301-500 | Dangerous",
      summary: "Serious health effects expected for everyone. This is an emergency condition.",
      guidance: {
        general: "Avoid all outdoor activity. Seal indoor spaces and use filtration if available.",
        sensitive: "Stay sheltered, follow medical plans, and seek guidance if breathing becomes difficult."
      }
    }
  }
];
const categoryForAqi = (aqi: number): CategoryInfo => {
  for (const step of CATEGORY_STEPS) {
    if (aqi <= step.threshold) return step.info;
  }
  return CATEGORY_STEPS[CATEGORY_STEPS.length - 1].info;
};
const formatObservationDetails = (iso?: string) => {
  if (!iso) return { local: "Unknown", relative: "" };
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return { local: iso, relative: "" };
  const local = date.toLocaleString(undefined, {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
  });
  const diffMs = date.getTime() - Date.now();
  const absMs = Math.abs(diffMs);
  const fmt = new Intl.RelativeTimeFormat(undefined, { numeric: "auto" });
  if (absMs < 60_000) return { local, relative: fmt.format(Math.round(diffMs / 1000), "second") };
  if (absMs < 3_600_000) return { local, relative: fmt.format(Math.round(diffMs / 60_000), "minute") };
  if (absMs < 86_400_000) return { local, relative: fmt.format(Math.round(diffMs / 3_600_000), "hour") };
  return { local, relative: fmt.format(Math.round(diffMs / 86_400_000), "day") };
};
const toTitleCase = (value: string) =>
  value
    .split(/\s+/)
    .filter(Boolean)
    .map((word) => {
      if (word.length <= 3 && word === word.toUpperCase()) return word;
      const lower = word.toLowerCase();
      return lower.charAt(0).toUpperCase() + lower.slice(1);
    })
    .join(" ");
const isAscii = (value: string) => /^[\x00-\x7F]+$/.test(value);
const formatDistance = (meters?: number | null) => {
  if (meters == null || Number.isNaN(meters)) return null;
  if (meters >= 1000) {
    const km = meters / 1000;
    return `${km >= 10 ? Math.round(km) : km.toFixed(1)} km away`;
  }
  return `${Math.round(meters)} m away`;
};
const InsightsPage = ({ city, hasSearched, onBack }: InsightsPageProps) => {
  const [data, setData] = useState<AqiResponse | null>(null);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  useEffect(() => {
    if (!hasSearched || !city) return;
    const controller = new AbortController();
    const load = async () => {
      setLoading(true);
      setFetchError(null);
      setData(null);
      try {
        // InsightsPage.tsx (inside load())
        const response = await api.get<AqiResponse>("/api/aqi", {
          params: { city, _ts: Date.now() },   // cache buster
          signal: controller.signal
        });
        setData(response.data);
      } catch (error) {
        if (!controller.signal.aborted) {
          console.error(error);
          setFetchError("Unable to fetch air quality right now. Try again in a moment.");
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };
    void load();
    return () => controller.abort();
  }, [city, hasSearched]);
  const category = useMemo(() => {
    if (!data || data.aqi == null) return null;
    return categoryForAqi(data.aqi);
  }, [data]);
  const accent = category?.accent ?? "#4ade80";
  const cardStyle = useMemo<CSSProperties | undefined>(() => {
    if (!category) return undefined;
    return {
      borderColor: `${accent}55`,
      boxShadow: `0 20px 60px ${accent}22`,
      background: `linear-gradient(135deg, ${accent}1f 0%, rgba(7, 18, 32, 0.88) 60%, rgba(4, 12, 24, 0.95) 100%)`
    };
  }, [category, accent]);
  const displayCity = useMemo(() => {
    const primary = (data?.resolved ?? "").split(",")[0]?.trim() ?? "";
    if (primary && isAscii(primary)) return toTitleCase(primary);
    const fallback = data?.query ?? city;
    return fallback ? toTitleCase(fallback) : "";
  }, [data?.query, data?.resolved, city]);
  const pm25Unit = data?.unit ?? "ug/m3";
  const observationDetails = useMemo(
    () => formatObservationDetails(data?.observedUtc),
    [data?.observedUtc]
  );
  // Prevent rendering an object as a React child
  const stationLabel = useMemo(() => {
    if (!data) return null;
    const stationRaw = data.station;
    let label: string | null = null;
    if (typeof stationRaw === "string" && stationRaw.trim()) {
      label = stationRaw.trim();
    } else if (stationRaw && typeof stationRaw === "object") {
      const coords = stationRaw as StationCoords;
      if (typeof coords.latitude === "number" && typeof coords.longitude === "number") {
        label = `${coords.latitude.toFixed(4)}, ${coords.longitude.toFixed(4)}`;
      }
    }
    const locality = data.locality?.trim();
    const country = data.country?.trim();
    const parts = [label, locality, country].filter(Boolean) as string[];
    const distance = formatDistance(data.stationDistanceMeters);
    if (distance) parts.push(distance);
    return parts.length ? parts.join(" | ") : null;
  }, [data]);
  const content = () => {
  if (!hasSearched) {
    return (
      <motion.div className="insights-status" variants={statusVariants} initial="initial" animate="animate">
        Start with a city search to see live readings.
      </motion.div>
    );
  }
  if (loading) {
    return (
      <motion.div className="insights-status" variants={statusVariants} initial="initial" animate="animate">
        Checking the air around you...
      </motion.div>
    );
  }
  if (fetchError) {
    return (
      <motion.div className="insights-status insights-status-error" variants={statusVariants} initial="initial" animate="animate">
        {fetchError}
      </motion.div>
    );
  }
  if (!data) return null;
  if (data.pm25 == null || data.aqi == null || !category) {
    const message = data.message ?? "No air quality data available.";
    return (
      <motion.div className="insights-status" variants={statusVariants} initial="initial" animate="animate">
        {message}
      </motion.div>
    );
  }
  return (
    <div className="insights-output-shell">
      <motion.div
        className="insights-spotlight"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <div className="insights-photo" style={{ borderColor: `${accent}33` }}>
          <img src={`/images/${category.image}.jpg`} alt={`${category.label} air quality visual`} />
        </div>
        <div className="typing-card" style={cardStyle}>
          <span className="typing-kicker" style={{ color: category.accent }}>
            {category.label} air quality
          </span>
          <div className="city-heading">
            <h2>{displayCity || city}</h2>
            {data.countryCode && (
              <span className="country-chip" style={{ borderColor: `${accent}66`, color: category.accent }}>
                {data.countryCode}
              </span>
            )}
          </div>
          <p className="typing-description">{category.range}</p>
          <div className="insights-metrics">
            <div className="metric-card metric-card--accent" style={{ borderColor: `${accent}66`, background: `linear-gradient(160deg, ${accent}3d 0%, rgba(6,16,32,0.85) 90%)` }}>
              <span className="metric-label">AQI</span>
              <span className="metric-value">{data.aqi}</span>
              <span className="metric-sub">{category.summary}</span>
            </div>
            <div className="metric-card">
              <span className="metric-label">PM2.5</span>
              <span className="metric-value">{data.pm25.toFixed(1)}</span>
              <span className="metric-sub">{pm25Unit}</span>
            </div>
            <div className="metric-card">
              <span className="metric-label">Observed</span>
              <span className="metric-value">{observationDetails.local}</span>
              {observationDetails.relative && <span className="metric-sub">{observationDetails.relative}</span>}
            </div>
          </div>
          <div className="insights-guidance-grid">
            <div className="guidance-card">
              <h3>General population</h3>
              <p>{category.guidance.general}</p>
            </div>
            <div className="guidance-card">
              <h3>Sensitive groups</h3>
              <p>{category.guidance.sensitive}</p>
            </div>
          </div>
          {stationLabel && (
            <p className="insights-provenance">
              Data source: <strong>{stationLabel}</strong>
            </p>
          )}
          {data.health_advice && <p className="additional-health-note">{data.health_advice}</p>}
        </div>
      </motion.div>
    </div>
  );
};
  return (
    <div className="insights-page">
      <div className="insights-bar">
        <button className="back-button" onClick={onBack}>
          <span className="back-icon" aria-hidden="true">←</span>
          <span>Back to search</span>
        </button>
        {category && (
          <div
            className="insights-meta-chip"
            style={{ borderColor: category.accent, color: category.accent, background: `${category.accent}1a` }}
          >
            {category.label.toUpperCase()}
          </div>
        )}
      </div>
      {content()}
    </div>
  );
};
export default InsightsPage;
