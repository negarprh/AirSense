import { useState } from "react";
import { api, AqiResponse, AdviceResponse } from "./api";
import SearchBar from "./components/SearchBar";
import AqiResult from "./components/AqiResult";
import backgroundVisual from "/images/background.jpg";

function App() {
  const [city, setCity] = useState("Los Angeles");
  const [asthma, setAsthma] = useState(false);
  const [aqiData, setAqiData] = useState<AqiResponse | null>(null);
  const [adviceData, setAdviceData] = useState<AdviceResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasSearched, setHasSearched] = useState(false);

  const fetchData = async (searchCity: string, asthmaFlag: boolean) => {
    const trimmed = searchCity.trim();
    if (!trimmed) {
      setError("Please enter a city name.");
      setHasSearched(false);
      setAqiData(null);
      setAdviceData(null);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const aqiResponse = await api.get<AqiResponse>("/api/aqi", {
        params: { city: trimmed }
      });
      setAqiData(aqiResponse.data);

      const adviceResponse = await api.get<AdviceResponse>("/api/advice", {
        params: { city: trimmed, asthma: asthmaFlag }
      });
      setAdviceData(adviceResponse.data);
      setHasSearched(true);
    } catch (err) {
      console.error(err);
      setError("Unable to fetch air quality right now. Try again in a moment.");
      setHasSearched(true);
      setAqiData(null);
      setAdviceData(null);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (searchCity: string, asthmaFlag: boolean) => {
    setCity(searchCity);
    setAsthma(asthmaFlag);
    void fetchData(searchCity, asthmaFlag);
  };

  const resultActive = hasSearched || loading || !!error;

  return (
    <div className="app-background" style={{ backgroundImage: `url(${backgroundVisual})` }}>
      <div className="app-overlay">
        <header className="hero">
          <h1 className="hero-title">Welcome to AirSense</h1>
          <p>Where you can sense the clear air</p>
        </header>

        <SearchBar
          city={city}
          asthma={asthma}
          onSearch={handleSearch}
          loading={loading}
        />

        {resultActive && (
          <div className={`result-wrapper ${resultActive ? "visible" : ""}`}>
            <AqiResult
              aqi={aqiData}
              advice={adviceData}
              asthma={asthma}
              error={error}
              loading={loading}
            />
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
