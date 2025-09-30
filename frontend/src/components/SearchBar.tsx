import { FormEvent, useEffect, useState } from "react";

interface SearchBarProps {
  city: string;
  asthma: boolean;
  loading?: boolean;
  onSearch: (city: string, asthma: boolean) => void;
}

const SearchBar = ({ city, asthma, loading = false, onSearch }: SearchBarProps) => {
  const [localCity, setLocalCity] = useState(city);
  const [localAsthma, setLocalAsthma] = useState(asthma);

  useEffect(() => {
    setLocalCity(city);
  }, [city]);

  useEffect(() => {
    setLocalAsthma(asthma);
  }, [asthma]);

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    onSearch(localCity, localAsthma);
  };

  return (
    <form className="search-card" onSubmit={handleSubmit}>
      <div className="search-row">
        <input
          type="text"
          value={localCity}
          onChange={(event) => setLocalCity(event.target.value)}
          placeholder="Search for a city..."
          aria-label="City name"
          className="search-input"
        />
        <button type="submit" disabled={loading} className="search-button">
          {loading ? "Searching..." : "Search"}
        </button>
      </div>
      <label className="search-toggle">
        <input
          type="checkbox"
          checked={localAsthma}
          onChange={(event) => setLocalAsthma(event.target.checked)}
        />
        <span>Include guidance for asthma or sensitive airways</span>
      </label>
    </form>
  );
};

export default SearchBar;
