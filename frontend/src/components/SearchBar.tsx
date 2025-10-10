import { FormEvent, useEffect, useState } from "react";

interface SearchBarProps {
  city: string;
  onSearch: (city: string) => void;
}

const SearchBar = ({ city, onSearch }: SearchBarProps) => {
  const [localCity, setLocalCity] = useState(city);

  useEffect(() => setLocalCity(city), [city]);

  const disabled = !localCity.trim();

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (disabled) return;
    onSearch(localCity.trim());
  };

  return (
    <form className="search-card" onSubmit={handleSubmit} role="search">
      <label htmlFor="city-search" className="visually-hidden">Search for a city</label>
      <div className="search-row">
        <input
          id="city-search"
          type="text"
          value={localCity}
          onChange={(e) => setLocalCity(e.target.value)}
          placeholder="Search for a city..."
          aria-label="City name"
          className="search-input"
          autoComplete="off"
          autoFocus
        />
        <button
          type="submit"
          className="search-button"
          disabled={disabled}
          aria-disabled={disabled}
        >
          <span className="search-button-label">Show air quality</span>
          <svg viewBox="0 0 20 20" aria-hidden="true" className="search-button-icon">
            <path d="M4 10h10m0 0l-3.5-4M14 10l-3.5 4" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
      </div>
    </form>
  );
};

export default SearchBar;
