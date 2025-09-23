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
    <form onSubmit={handleSubmit} className="search-form">
      <label>
        <span>City</span>
        <input
          type="text"
          value={localCity}
          onChange={(event) => setLocalCity(event.target.value)}
          placeholder="Enter a city"
        />
      </label>
      <label className="checkbox">
        <input
          type="checkbox"
          checked={localAsthma}
          onChange={(event) => setLocalAsthma(event.target.checked)}
        />
        <span>Asthma or respiratory sensitivity</span>
      </label>
      <button type="submit" disabled={loading}>
        {loading ? "Loading..." : "Check AQI"}
      </button>
    </form>
  );
};

export default SearchBar;
