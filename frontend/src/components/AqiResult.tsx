import { AdviceResponse, AqiResponse } from "../api";

interface AqiResultProps {
  aqi: AqiResponse | null;
  advice: AdviceResponse | null;
  asthma: boolean;
  error: string | null;
}

const AqiResult = ({ aqi, advice, asthma, error }: AqiResultProps) => {
  if (error) {
    return <div className="result error">{error}</div>;
  }

  if (!aqi || !advice) {
    return <div className="result">Enter a city to see air quality information.</div>;
  }

  return (
    <div className="result">
      <h2>{aqi.city}</h2>
      <p>AQI: {aqi.aqi}</p>
      <p>Band: {aqi.band}</p>
      <p>Main pollutant: {aqi.mainPollutant}</p>
      <p>{advice.publicAdvice}</p>
      <p>{asthma ? advice.sensitiveAdvice : advice.pollutantNote}</p>
    </div>
  );
};

export default AqiResult;
