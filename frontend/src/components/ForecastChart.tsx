import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";
import dayjs from "dayjs";

type P = { data: { t: string; aqi: number; pm25: number }[] };
export default function ForecastChart({ data }: P) {
  const fmt = (iso: string) => dayjs(iso).format("MMM D HH:mm");
  return (
    <div style={{ width: "100%", height: 320 }}>
      <ResponsiveContainer>
        <LineChart data={data}>
          <XAxis dataKey="t" tickFormatter={fmt} minTickGap={24} />
          <YAxis yAxisId="left" domain={[0, 500]} />
          <YAxis yAxisId="right" orientation="right" />
          <Tooltip labelFormatter={(v) => fmt(String(v))} />
          <Line type="monotone" dataKey="aqi" yAxisId="left" dot={false}/>
          <Line type="monotone" dataKey="pm25" yAxisId="right" dot={false}/>
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
