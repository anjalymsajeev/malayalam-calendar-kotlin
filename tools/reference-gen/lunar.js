// Generates T2 lunar reference fixtures with astronomy-engine (MIT, independent implementation):
//   tithi-boundaries-2020-2027.csv      every 12° step of the Moon-Sun elongation (SearchMoonPhase)
//   nakshatra-boundaries-2020-2027.csv  every 13°20' step of the Moon's Lahiri sidereal longitude
// Usage: node lunar.js <node_modules dir> <output dir>
const path = require('path');
const fs = require('fs');
const [nm, outDir] = process.argv.slice(2);
const A = require(path.join(nm, 'astronomy-engine'));

const start = new Date(Date.UTC(2020, 0, 1));
const end = new Date(Date.UTC(2028, 0, 1));

// Tithi boundaries: elongation = k * 12°.
const tithi = ['elongation_deg,utc'];
for (let t = A.MakeTime(start); t.date < end;) {
  let next = null;
  for (let k = 0; k < 30; k++) {
    const hit = A.SearchMoonPhase(k * 12, t, 3);
    if (hit && (!next || hit.ut < next.time.ut)) next = { k, time: hit };
  }
  tithi.push(`${next.k * 12},${next.time.date.toISOString()}`);
  t = next.time.AddDays(1 / 1440);
}
fs.writeFileSync(path.join(outDir, 'tithi-boundaries-2020-2027.csv'), tithi.join('\n') + '\n');

// Nakshatra boundaries from sidereal lunar longitude (Lahiri: 1956 definition + IAU 2006 precession + nutation).
const pA = (t) => 5028.796195 * t + 1.1054348 * t * t;
const T0 = (2435553.5 - 2451545.0) / 36525;
const sidereal = (ms) => {
  const time = A.MakeTime(new Date(ms));
  const ayan = 23.250182778 + (pA(time.tt / 36525) - pA(T0)) / 3600 + A.e_tilt(time).dpsi / 3600;
  return (((A.EclipticGeoMoon(time).lon - ayan) % 360) + 360) % 360;
};
const arc = 360 / 27;
const nak = ['boundary_deg,utc'];
let ms = start.getTime();
let idx = Math.floor(sidereal(ms) / arc);
while (ms < end.getTime()) {
  const target = ((idx + 1) % 27) * arc;
  const f = (x) => ((sidereal(x) - target + 540) % 360) - 180;
  let a = ms, b = ms + 3 * 864e5;
  for (let i = 0; i < 50; i++) { const m = (a + b) / 2; if (f(m) < 0) a = m; else b = m; }
  ms = Math.round((a + b) / 2);
  nak.push(`${target.toFixed(6)},${new Date(ms).toISOString()}`);
  idx = (idx + 1) % 27;
  ms += 60e3;
}
fs.writeFileSync(path.join(outDir, 'nakshatra-boundaries-2020-2027.csv'), nak.join('\n') + '\n');
console.log(`wrote ${tithi.length - 1} tithi and ${nak.length - 1} nakshatra boundaries`);
