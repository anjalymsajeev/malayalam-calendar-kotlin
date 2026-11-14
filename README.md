# Kollavarsham Kotlin SDK

A Kotlin SDK for the Malayalam (Kollavarsham) calendar. It converts between Gregorian and Malayalam dates following the Kerala solar-month rule. It works offline, is thread-safe, and has no Android dependency in the core.

> **Status: Sprint 0 (discovery and architecture), awaiting approval.** There is no SDK code yet.

## Sprint 0 deliverables

| Document | Contents |
|---|---|
| [docs/sprint-0/existing-library-research.md](docs/sprint-0/existing-library-research.md) | Evaluation of existing libraries, accuracy spike, reuse recommendation, license review |
| [CALENDAR_ALGORITHM.md](CALENDAR_ALGORITHM.md) | Calendar rules and the computation strategy |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Modules and principles |
| [docs/sprint-0/api-proposal.md](docs/sprint-0/api-proposal.md) | Domain model and public API proposal |
| [VALIDATION_STRATEGY.md](VALIDATION_STRATEGY.md) | Evidence tiers, reference dataset, test suites |
| [SPRINT_PLAN.md](SPRINT_PLAN.md) | User stories, risks, open questions, Sprint 1 backlog |
| [docs/adr/](docs/adr/) | Architecture decision records 0001–0006 |
| [tools/accuracy-spike/](tools/accuracy-spike/) | Reproducible measurement script and results (not shipped) |

## Target API (illustrative; implemented in Sprints 1–3)

```kotlin
val calendar = KollavarshamCalendar.create()
calendar.fromGregorian(LocalDate.of(2026, 11, 14))           // 1202 Thulam 28
calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))  // 2026-11-17
```
