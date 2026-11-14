// Generates T2 reference fixtures with astronomy-engine (MIT, independent VSOP87-based code):
//   sankranti-1900-2101.csv  every nirayana sankranti, both Lahiri conventions
//   sunrise-sunset.csv       sunrise/sunset at Thiruvananthapuram on the 1st and 15th of each month 2000-2030
// Usage: node generate.js <node_modules dir> <output dir>
const path = require('path');
const fs = require('fs');
const [nm, outDir] = process.argv.slice(2);
const A = require(path.join(nm, 'astronomy-engine'));

const pA = (t) => 5028.796195 * t + 1.1054348 * t * t;
const T0 = (2435553.5 - 2451545.0) / 36525;
const ayanamsa = {
  LahiriTrue: (time) => 23.250182778 + (pA(time.tt / 36525) - pA(T0)) / 3600 + A.e_tilt(time).dpsi / 3600,
  LahiriDrikCalibrated: (time) => 23.863092 + pA(time.tt / 36525) / 3600,
};
const sidereal = (date, conv) => {
  const time = A.MakeTime(date);
  return (((A.SunPosition(time).elon - ayanamsa[conv](time)) % 360) + 360) % 360;
};
function sankranti(sign, approx, conv) {
  const f = (t) => ((sidereal(new Date(t), conv) - sign * 30 + 540) % 360) - 180;
  let a = approx - 8 * 864e5, b = approx + 8 * 864e5;
  for (let i = 0; i < 60; i++) { const m = (a + b) / 2; if (f(m) < 0) a = m; else b = m; }
  return new Date(Math.round((a + b) / 2));
}
const rows = ['rashi,lahiri_true_utc,lahiri_drik_calibrated_utc'];
for (let y = 1899; y <= 2101; y++) {
  for (let sign = 0; sign < 12; sign++) {
    const approx = Date.UTC(y, 3, 14) + sign * 30.4375 * 864e5;
    rows.push([sign, sankranti(sign, approx, 'LahiriTrue').toISOString(), sankranti(sign, approx, 'LahiriDrikCalibrated').toISOString()].join(','));
  }
}
fs.writeFileSync(path.join(outDir, 'sankranti-1899-2101.csv'), rows.join('\n') + '\n');

const obs = new A.Observer(8.5241, 76.9366, 0);
const sun = ['date,sunrise_utc,sunset_utc'];
for (let y = 2000; y <= 2030; y++) for (let m = 0; m < 12; m++) for (const d of [1, 15]) {
  const midnightIst = new Date(Date.UTC(y, m, d) - 5.5 * 3600e3);
  const rise = A.SearchRiseSet(A.Body.Sun, obs, +1, midnightIst, 1).date;
  const set = A.SearchRiseSet(A.Body.Sun, obs, -1, rise, 1).date;
  sun.push(`${new Date(Date.UTC(y, m, d)).toISOString().slice(0, 10)},${rise.toISOString()},${set.toISOString()}`);
}
fs.writeFileSync(path.join(outDir, 'sunrise-sunset-tvm.csv'), sun.join('\n') + '\n');
console.log(`wrote ${rows.length - 1} sankrantis and ${sun.length - 1} sunrise/sunset rows`);
