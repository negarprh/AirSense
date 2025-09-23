import { useEffect, useState } from "react";
import { api, AqiResponse, AdviceResponse } from "./api";
import SearchBar from "./components/SearchBar";
import AqiResult from "./components/AqiResult";

function App() {
  const [city, setCity] = useState("Los Angeles");
  const [asthma, setAsthma] = useState(false);
  const [aqiData, setAqiData] = useState<AqiResponse | null>(null);
  const [adviceData, setAdviceData] = useState<AdviceResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async (searchCity: string, asthmaFlag: boolean) => {
    if (!searchCity.trim()) {
      setError("Please enter a city name.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const aqiResponse = await api.get<AqiResponse>("/api/aqi", {
        params: { city: searchCity }
      });
      setAqiData(aqiResponse.data);

      const adviceResponse = await api.get<AdviceResponse>("/api/advice", {
        params: { city: searchCity, asthma: asthmaFlag }
      });
      setAdviceData(adviceResponse.data);
    } catch (err) {
      console.error(err);
      setError("Unable to fetch AQI data right now. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchData(city, asthma);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearch = (searchCity: string, asthmaFlag: boolean) => {
    setCity(searchCity);
    setAsthma(asthmaFlag);
    void fetchData(searchCity, asthmaFlag);
  };

  return (
    <div className="main" style={{ maxWidth: 600, padding: 16 }}>
      <SearchBar
        city={city}
        asthma={asthma}
        onSearch={handleSearch}
        loading={loading}
      />
      <AqiResult
        aqi={aqiData}
        advice={adviceData}
        asthma={asthma}
        error={error}
      />
    </div>
  );
}

export default App;
