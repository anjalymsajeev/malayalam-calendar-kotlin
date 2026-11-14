// Sprint 0 accuracy spike. Throwaway measurement code, not part of the SDK.
// Usage: TZ=Asia/Kolkata node compare.js <node_modules dir> <sankranti fixture csv> <out dir>
const path = require('path');
const fs = require('fs');
const [nm, fixtureCsv, outDir] = process.argv.slice(2);
const A = require(path.join(nm, 'astronomy-engine'));
const { Kollavarsham } = require(path.join(nm, 'kollavarsham'));

const TVM = { lat: 8.5241, lon: 76.9366 }; // Thiruvananthapuram
const observer = new A.Observer(TVM.lat, TVM.lon, 0);
const IST = 5.5 / 24;
const MONTHS = ['Chingam', 'Kanni', 'Thulam', 'Vrischikam', 'Dhanu', 'Makaram',
  'Kumbham', 'Meenam', 'Medam', 'Idavam', 'Mithunam', 'Karkidakam'];

// Lahiri: 23°15'00.658" at 1956-03-21 00:00 TT, carried by IAU 2006 general precession, plus nutation.
const T = (jd) => (jd - 2451545.0) / 36525;
const pA = (t) => 5028.796195 * t + 1.1054348 * t * t; // arcsec
const T0 = T(2435553.5);
const CONVENTION = process.env.AYANAMSA || 'A';
function lahiriTrue(time) {
  if (CONVENTION === 'B') { // convention documented by the Dart package (drikpanchang-calibrated)
    return 23.863092 + pA(time.tt / 36525) / 3600;
  }
  const mean = 23.250182778 + (pA(time.tt / 36525) - pA(T0)) / 3600; // 1956 Lahiri definition + nutation
  return mean + A.e_tilt(time).dpsi / 3600;
}
function siderealSun(time) {
  const lon = A.SunPosition(time).elon - lahiriTrue(time);
  return ((lon % 360) + 360) % 360;
}
// Find the moment the sidereal Sun crosses sign*30° in [t1, t2] (Date objects).
function sankranti(sign, t1, t2) {
  const target = sign * 30;
  const f = (t) => ((siderealSun(A.MakeTime(t)) - target + 540) % 360) - 180;
  let a = t1.getTime(), b = t2.getTime();
  for (let i = 0; i < 60; i++) {
    const m = (a + b) / 2;
    if (f(new Date(m)) < 0) a = m; else b = m;
  }
  return new Date((a + b) / 2);
}
const istDate = (d) => new Date(d.getTime() + 5.5 * 3600e3).toISOString().slice(0, 10);
const addDays = (s, n) => { const d = new Date(s + 'T00:00:00Z'); d.setUTCDate(d.getUTCDate() + n); return d.toISOString().slice(0, 10); };

// Kerala rule: day 1 is the civil (IST) day of the sankranti if it falls before
// sunrise + 3/5 of daytime on that day, otherwise the next day.
function keralaMonthStart(s) {
  const day = istDate(s);
  const midnightUtc = new Date(new Date(day + 'T00:00:00Z').getTime() - 5.5 * 3600e3);
  const rise = A.SearchRiseSet(A.Body.Sun, observer, +1, midnightUtc, 1).date;
  const set = A.SearchRiseSet(A.Body.Sun, observer, -1, rise, 1).date;
  const cutoff = new Date(rise.getTime() + 0.6 * (set - rise));
  return { start: s < cutoff ? day : addDays(day, 1), marginMin: (cutoff - s) / 60000 };
}

// 1. Validate the spike's sankranti moments against the Swiss Ephemeris fixture.
const rows = fs.readFileSync(fixtureCsv, 'utf8').trim().split('\n').slice(1);
const diffs = rows.map((r) => {
  const [sign, utc] = r.split(',');
  const ref = new Date(utc);
  const mine = sankranti(+sign, new Date(ref - 2 * 864e5), new Date(+ref + 2 * 864e5));
  return (mine - ref) / 60000;
});
diffs.sort((a, b) => a - b);
const q = (p) => diffs[Math.floor(p * (diffs.length - 1))].toFixed(2);
const fixtureSummary = `spike vs Swiss Ephemeris Lahiri fixture (${diffs.length} sankrantis 1900-2100), minutes: ` +
  `min ${q(0)}, median ${q(0.5)}, max ${q(1)}`;

// 2. Month starts 1990-2030: spike (modern, Kerala rule) vs kollavarsham-js (Surya Siddhanta).
const spike = new Map(); // ISO date -> {month, margin}
for (let y = 1989; y <= 2031; y++) {
  for (let sign = 0; sign < 12; sign++) {
    // Mesha (sign 0) sankranti is around April 14; sign k is ~k months later.
    const approx = new Date(Date.UTC(y, 3, 14) + sign * 30.4375 * 864e5);
    const s = sankranti(sign, new Date(approx - 6 * 864e5), new Date(+approx + 6 * 864e5));
    const { start, marginMin } = keralaMonthStart(s);
    spike.set(start, { month: MONTHS[(sign + 8) % 12], marginMin, sankrantiIst: new Date(+s + 5.5 * 3600e3).toISOString().slice(0, 16) });
  }
}
const kv = new Kollavarsham({ system: 'SuryaSiddhanta', latitude: TVM.lat, longitude: TVM.lon });
const js = new Map();
for (let d = new Date(1990, 0, 1); d <= new Date(2030, 11, 31); d.setDate(d.getDate() + 1)) {
  const k = kv.fromGregorianDate(new Date(d));
  if (k.date === 1) {
    const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    js.set(iso, { year: k.year, month: k.masaName.trim().replace('Karkitakam', 'Karkidakam') });
  }
}
const spikeIn = [...spike.keys()].filter((k) => k >= '1990-01-01' && k <= '2030-12-31');
const agree = spikeIn.filter((k) => js.has(k) && js.get(k).month === spike.get(k).month);
const disagreements = [];
for (const k of spikeIn) {
  if (agree.includes(k)) continue;
  const m = spike.get(k).month;
  const jsDate = [...js.entries()].find(([d, v]) => v.month === m && Math.abs(new Date(d) - new Date(k)) < 5 * 864e5);
  disagreements.push({ month: m, spike: k, kollavarshamJs: jsDate ? jsDate[0] : 'none', sankrantiIst: spike.get(k).sankrantiIst, marginMin: +spike.get(k).marginMin.toFixed(1) });
}

// 3. Both engines vs observed Kerala dates (as listed, with sources, in VALIDATION_STRATEGY.md).
const observed = [
  ['2021-11-16', 'Vrischikam'], ['2022-08-17', 'Chingam'], ['2023-08-17', 'Chingam'],
  ['2024-08-17', 'Chingam'], ['2025-08-17', 'Chingam'], ['2023-04-15', 'Medam'],
  ['2024-04-14', 'Medam'], ['2025-04-14', 'Medam'], ['2024-01-15', 'Makaram'],
  ['2025-01-14', 'Makaram'], ['2024-11-16', 'Vrischikam'], ['2024-07-16', 'Karkidakam'],
  ['2025-07-17', 'Karkidakam'], ['2025-09-17', 'Kanni'],
];
const obs = observed.map(([d, m]) => ({
  observed: d, month: m,
  spike: spike.has(d) && spike.get(d).month === m ? 'match' : 'MISS',
  kollavarshamJs: js.has(d) && js.get(d).month === m ? 'match' : 'MISS',
}));

// 4. Upcoming month starts (current Malayalam year) from the spike, for the report.
const upcoming = [...spike.entries()].filter(([d]) => d >= '2026-04-01' && d <= '2027-04-30')
  .map(([d, v]) => ({ start: d, month: v.month, sankrantiIst: v.sankrantiIst, marginMin: +v.marginMin.toFixed(1) }));

const out = { fixtureSummary, monthStarts: spikeIn.length, agree: agree.length, disagreements, observed: obs, upcoming };
fs.writeFileSync(path.join(outDir, `spike-results-${CONVENTION}.json`), JSON.stringify(out, null, 2));
console.log(fixtureSummary);
console.log(`month starts 1990-2030: ${spikeIn.length}, kollavarsham-js agrees on ${agree.length}`);
console.table(obs);
console.table(upcoming);
console.log('first 25 disagreements:'); console.table(disagreements.slice(0, 25));
