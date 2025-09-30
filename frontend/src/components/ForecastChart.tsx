interface ForecastPoint {
  label: string;
  value: number;
  band: string;
  color: string;
}

interface ForecastChartProps {
  forecast: ForecastPoint[];
}

const ForecastChart = ({ forecast }: ForecastChartProps) => {
  if (!forecast.length) {
    return <p className="forecast-empty">Forecast data unavailable.</p>;
  }

  const maxValue = Math.max(...forecast.map((point) => point.value), 1);

  return (
    <div className="forecast-chart">
      {forecast.map((point) => (
        <div className="forecast-bar" key={point.label}>
          <div className="forecast-bar-visual" aria-hidden>
            <div
              className="forecast-bar-fill"
              style={{
                height: `${Math.max(20, (point.value / maxValue) * 100)}%`,
                background: `linear-gradient(180deg, ${point.color}E6, ${point.color}99)`
              }}
            />
          </div>
          <span className="forecast-value">{point.value}</span>
          <span className="forecast-label">{point.label}</span>
          <span className="forecast-band">{point.band}</span>
        </div>
      ))}
    </div>
  );
};

export type { ForecastPoint };
export default ForecastChart;
