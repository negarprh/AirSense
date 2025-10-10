import { FormEvent, useEffect, useState } from "react";

interface SearchBarProps {
  city: string;
  onSearch: (city: string) => void;
}

const SearchBar = ({ city, onSearch }: SearchBarProps) => {
  const [localCity, setLocalCity] = useState(city);

  useEffect(() => {
    setLocalCity(city);
  }, [city]);

  const isDisabled = !localCity.trim();

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    if (isDisabled) return;
    onSearch(localCity.trim());
  };

  return (
    <form className="search-card" onSubmit={handleSubmit}>
      <label htmlFor="city-search" className="visually-hidden">Search for a city</label>
      <div className="search-row">
        <span className="search-icon" aria-hidden="true">
          <svg viewBox="0 0 20 20" focusable="false">
            <path
              d="M12.5 12.5L17 17m-2.5-6.5a5.5 5.5 0 11-11 0 5.5 5.5 0 0111 0z"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </span>
        <input
          id="city-search"
          type="text"
          value={localCity}
          onChange={(event) => setLocalCity(event.target.value)}
          placeholder="Search for a city..."
          aria-label="City name"
          className="search-input"
          autoComplete="off"
        />
        <button type="submit" className="search-button" disabled={isDisabled}>
          <span>Show air quality</span>
          <svg viewBox="0 0 20 20" aria-hidden="true">
            <path d="M4 10h10m0 0l-3.5-4M14 10l-3.5 4" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
      </div>
    </form>
  );
};

export default SearchBar;
