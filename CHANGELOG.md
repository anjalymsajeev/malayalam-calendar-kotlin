# Changelog

All notable changes are documented here. The project follows [Semantic Versioning](https://semver.org/);
0.x releases may contain breaking changes.

## [Unreleased]

### Added (Sprint 2: conversion engine)
- `kollavarsham-astronomy`: VSOP87D solar position (426-term truncation), FK5 correction, nutation, aberration;
  Espenak–Meeus ΔT; Lahiri ayanamsa (two conventions); sankranti search; sunrise and sunset.
- Kerala 3/5-of-daytime month-start rule (`AstronomicalBoundarySource`).
- `KollavarshamCalendar.create()`, `create(configuration)`, `LocalDate.toMalayalamDate()`, `MalayalamDate.toLocalDate()`.
- First verified override: Thulam 1197 = 2021-10-17 (cited).
- Reference dataset of 21 cited month starts; borderline-month list (30 months, 1900–2100); invariant tests for every day 1900–2100.

### Decided
- Lahiri convention A (`LahiriTrue`), on evidence (ADR-0002 accepted).

### Added (Sprint 1: core SDK foundation)
- Gradle multi-module build: `kollavarsham-astronomy` (skeleton), `kollavarsham-core`, `kollavarsham-locale`.
- Domain model: `MalayalamMonth`, `MalayalamWeekday`, `MalayalamDate`, `MonthKey`, `MonthBoundary`,
  `BoundarySource`, `GeoLocation`, `CalendarConfiguration`.
- Validation: `DateValidation`, `KollavarshamException`, `InvalidMalayalamDateException`,
  `DateOutOfSupportedRangeException`.
- `KollavarshamCalendar` conversion, validation, month-structure and arithmetic APIs, running on an
  internal month-boundary source. The astronomical source and the public `create()` arrive in Sprint 2.
- Override layer for evidence-backed almanac corrections (ADR-0005).
- Malayalam and English month and weekday names (`displayName`).
- Public API tracking with the binary-compatibility-validator, and CI.

### Sprint 0
- Research, architecture, algorithm and validation documents; ADRs 0001–0006; accuracy spike.
