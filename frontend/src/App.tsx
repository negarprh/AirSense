import { useState } from "react";
import { Route, Routes, useNavigate } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import InsightsPage from "./pages/InsightsPage";
import backgroundVisual from "/images/background.jpg";

function App() {
  const [city, setCity] = useState("Los Angeles");
  const [hasSearched, setHasSearched] = useState(false);
  const [landingError, setLandingError] = useState<string | null>(null);

  const navigate = useNavigate();

  const beginSearchFromLanding = (nextCity: string) => {
    if (!nextCity) {
      setLandingError("Please enter a city name.");
      setHasSearched(false);
      return;
    }

    setLandingError(null);
    setCity(nextCity);
    setHasSearched(true);
    navigate("/insights");
  };

  const handleBackToSearch = () => {
    setHasSearched(false);
    setLandingError(null);
    setCity("");
    navigate("/");
  };

  return (
    <div className="app-background" style={{ backgroundImage: `url(${backgroundVisual})` }}>
      <div className="app-overlay">
        <Routes>
          <Route
            path="/"
            element={
              <LandingPage
                city={city}
                error={landingError}
                onSearch={beginSearchFromLanding}
              />
            }
          />
       
          <Route
            path="/insights"
            element={
              <InsightsPage
                key={city}              // <— forces a fresh mount on city change
                city={city}
                hasSearched={hasSearched}
                onBack={handleBackToSearch}
              />
            }
          />

        </Routes>
      </div>
    </div>
  );
}

export default App;
