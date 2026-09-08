# Microfinance Loan Tracker, SDG-1 (No Poverty)

**PRG2104 Final Project, Tier C (AI-Integrated)**

## What this is?

A standalone ScalaFX desktop app for a community lender. It supports SDG 1
("No Poverty") by helping small lenders run a microfinance programme without
a spreadsheet: register borrowers, disburse Micro or Group loans, track
repayment status, and see which loans carry the highest default risk.

## Features (4, each end-to-end)

1. **Register Borrower**, add a borrower (name, income, dependents, village); list persists to `data/borrowers.csv`.
2. **Disburse Loan**, pick a borrower, choose Micro or Group loan, set principal/rate/term (and group size for Group loans); persists to `data/loans.csv`.
3. **Repayments**, table of every loan with its computed monthly repayment; mark a loan Repaid or Defaulted.
4. **Dashboard**, totals (borrowers, disbursed amount, active/repaid/defaulted counts) and a default-risk ranking of all loans.

## Setup & run

Requirements: **JDK 21**, **sbt 1.9+** (project pins 1.10.1 in `project/build.properties`).

```bash
cd ".\Microfinance Loan Tracker_23052301"
sbt run
```

The first run creates a `data/` folder next to the project for CSV persistence
(borrowers/loans survive an app restart). To start with a few sample
borrowers already loaded, copy the sample file in before running:

```bash
mkdir -p data
cp src/main/resources/sample_borrowers.csv data/borrowers.csv
```

To run the unit tests:

```bash
sbt test
```

## Design summary (see `docs/UML.png` for the full diagram)

- `model.Loan`, abstract class, extended by `MicroLoan` and `GroupLoan`
  (inheritance + subtype polymorphism: each overrides `monthlyRepayment`
  differently).
- `repo.Repository[T]`, one generic, immutable repository class reused for
  both `Borrower` and `Loan` (parametric polymorphism).
- `persistence.CsvStore[T]`, abstract base with the shared load/save/`Try`
  logic; `BorrowerStore` and `LoanStore` only supply the CSV encode/decode
  (keeps persistence DRY).
- `model.RiskAssessable`, trait mixed into the dashboard for the default-risk
  formula, kept separate from the loan data model.
- `validation.Validation`, turns raw text-field input into `Either[String, A]`
  so the UI shows a friendly message instead of a crash on bad input.

## AI use summary

Built under the PRG2104 Tier C policy, AI (Claude) was used throughout for
scaffolding, debugging, and UI styling work. See `ai/interaction_log.md`
for the full 15-entry log and `docs/ai_reflection.md` for the reflection.

## Third-party citations

This project depends on ScalaFX (BSD 3-Clause, https://www.scalafx.org/),
JavaFX/OpenJFX (GPLv2 with Classpath Exception, https://openjfx.io/), and
ScalaTest (Apache License 2.0, https://www.scalatest.org/, test-scope only).
Exact pinned versions and per-library usage are in `docs/citations.md`. No
other third-party code, assets, fonts, or icons are used anywhere in this
project.

## Compile status

Verified clean with `sbt clean compile` under JDK 21: 14 sources, 0 warnings
(scalacOptions already includes `-deprecation -feature -Wunused:all` in
`build.sbt`, so a plain `sbt clean compile` is sufficient).
