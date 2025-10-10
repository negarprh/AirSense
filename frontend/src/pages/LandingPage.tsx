import SearchBar from "../components/SearchBar";

interface LandingPageProps {
  city: string;
  error: string | null;
  onSearch: (city: string) => void;
}

const POPULAR_CITIES = ["Los Angeles", "New York", "Tokyo", "London"];

const LandingPage = ({ city, error, onSearch }: LandingPageProps) => {
  return (
    <main className="landing-page">
      <div className="landing-content">
        <header className="hero">
          <h1 className="hero-title">Air Sense</h1>
          <p className="hero-subtitle">
            Sense the Clear Air
          </p>
        </header>

        <SearchBar city={city} onSearch={onSearch} />

        {error && <p className="form-error" role="alert">{error}</p>}

        <section className="landing-hints" aria-label="Popular searches">
          <span className="hint-label">Try one of these cities</span>
          <div className="hint-chip-row">
            {POPULAR_CITIES.map((name) => (
              <button
                key={name}
                type="button"
                className="hint-chip"
                onClick={() => onSearch(name)}
              >
                {name}
              </button>
            ))}
          </div>
        </section>
      </div>
    </main>
  );
};

export default LandingPage;
