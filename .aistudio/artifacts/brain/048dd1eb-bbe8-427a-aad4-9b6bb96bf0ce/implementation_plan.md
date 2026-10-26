# Everyday Life – All-in-One Smart Calculator

An offline-first, privacy-conscious suite of daily, shopping, finance, travel, and wellness calculators for Android, engineered with Jetpack Compose, Material 3, and zero forced cloud dependencies.

## User Review & Critical Decisions

> [!IMPORTANT]
> The recommended defaults have been selected to ensure instant offline utility, strict privacy compliance, and smooth Google Play publication:
> - **Technology Stack**: Kotlin + Jetpack Compose on modern Android (the native foundation pre-configured in this environment), adhering strictly to Material Design 3 and offline Room / DataStore persistence.
> - **Visual Identity**: Modern Indigo theme featuring deep slate/charcoal dark mode, high-contrast crisp typography, responsive layout containers, and distinct category tinting.
> - **Health Calculators (BMI & Pregnancy Due Date)**: Included in MVP with explicit medical disclaimers, clear assumption disclosures (e.g., standard 28-day cycle), zero ad-targeting data leakage, and a one-tap local data deletion control.
> - **AdMob Integration**: Non-intrusive bottom banner slot with Google official test ad units (`ca-app-pub-3940256099942544/6300978111`), graceful offline fallback, and strict zero-ad placement on health/sensitive screens.

---

## 1. Overview & Core Concept

- **What It Does**: Provides a unified, ultra-fast hub for daily mathematics, shopping comparisons, commercial calculations (markup, margins, tax/VAT, tip split), date & age timing, fuel economy/cost, multi-unit conversions, and safe personal wellness estimates.
- **Target Audience / Persona**: Everyday consumers, shoppers, small business owners, travelers, commuters, and parents seeking reliable, instant calculations without intrusive ads, sign-in walls, or internet requirements.
- **Key Value**: 100% offline functionality, immediate keyboard focus with instant live calculation, comprehensive history with copy/share, and high-accuracy decimal arithmetic avoiding floating-point rounding errors.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Search & Quick Launch**: User opens the app; home screen displays a search bar with quick filters (All, Shopping, Finance, Date/Time, Travel, Wellness), a "Recent" carousel, and pinned "Favorites".
2. **Instant Calculation**: Tapping any calculator (e.g. Discount Calculator) opens a dedicated, distraction-free screen with large numeric inputs, automatic live computation as digits are typed, breakdown details (e.g. Original Price, Discount %, Saved Amount, Final Price), and quick actions (Copy Result, Share, Add to Favorites).
3. **History & Recall**: Non-sensitive calculations are logged to a local searchable History tab, allowing users to review past calculations or clear all logs at any time.

```
┌────────────────────────────────────────────────────────┐
│                   Top App Bar                          │
│     Everyday Calculator         [Search]  [Settings]   │
├────────────────────────────────────────────────────────┤
│  [ Filter Chips: All | Shopping | Finance | Travel ]   │
├────────────────────────────────────────────────────────┤
│  ★ Favorites & Recents Row (Quick horizontal scroll)   │
├────────────────────────────────────────────────────────┤
│  Category Grid / List:                                 │
│  ┌─────────────────────────┐ ┌──────────────────────┐  │
│  │ % Percentage & Discount │ │ 💰 Margin & Tax / VAT│  │
│  └─────────────────────────┘ └──────────────────────┘  │
│  ┌─────────────────────────┐ ┌──────────────────────┐  │
│  │ 📅 Age & Date Diff      │ │ ⛽ Fuel Cost & Trip   │  │
│  └─────────────────────────┘ └──────────────────────┘  │
│  ┌─────────────────────────┐ ┌──────────────────────┐  │
│  │ ⚖️ Unit Converter        │ │ 🩺 BMI & Health Est. │  │
│  └─────────────────────────┘ └──────────────────────┘  │
├────────────────────────────────────────────────────────┤
│  [ Bottom Navigation: Calculators | History | Settings]│
│  [ AdMob Test Banner Container (Non-blocking)         ]│
└────────────────────────────────────────────────────────┘
```

### Visual Identity & Theme
- **Color Palette**:
  - `Primary`: Indigo (`#4F46E5`) & Electric Blue (`#3B82F6`) for high-contrast clarity.
  - `Surface Dark`: Slate Charcoal (`#0F172A`), Card Slate (`#1E293B`), Border (`#334155`).
  - `Surface Light`: Crisp Off-White (`#F8FAFC`), Card White (`#FFFFFF`), Border (`#E2E8F0`).
  - `Accents`: Emerald Green (`#10B981`) for savings/discounts, Amber (`#F59E0B`) for fuel & warnings, Violet (`#8B5CF6`) for dates/age.
- **Typography**: Clean tabular numerals and legible headings using Android M3 typography with responsive `sp` scaling.
- **Touch Targets**: Minimum 48dp height/width for all input fields, buttons, and segmented pickers.

---

## 3. Key Product Decisions & Trade-Offs

- **MVP Calculator Priority List**:
  1. **Standard Calculator**: Arithmetic with live expression parsing and error recovery.
  2. **Percentage Calculator**: 3 modes (What is X% of Y, X is what % of Y, % increase/decrease).
  3. **Discount & Sale Price**: Original price, discount %, additional extra discount %, tax rate, and total savings.
  4. **Tax & VAT Calculator**: Exclusive vs. Inclusive tax toggles, custom tax rates, and base/net calculations.
  5. **Tip & Bill Splitter**: Bill amount, tip % presets (10%, 15%, 18%, 20%, custom), number of people, and per-person split breakdown.
  6. **Profit Margin & Markup**: Cost price, selling price, profit amount, margin percentage, and markup percentage with bidirectional input solving.
  7. **Age Calculator**: Precise years, months, days, total days lived, and next birthday countdown.
  8. **Date Difference Calculator**: Days, weeks, months between two chosen dates, with leap-year precision.
  9. **Fuel Cost & Trip Economy**: Distance, fuel efficiency (L/100km or MPG), fuel price per unit, total fuel needed, and total trip cost.
  10. **Unit Converter**: Multi-category conversion (Length, Weight, Volume, Temperature, Speed) with instant bi-directional conversion.
  11. **BMI & Wellness Estimate**: Metric/Imperial BMI, WHO category classification, healthy weight range estimate, and prominent medical disclaimer banner.
  12. **Pregnancy Due Date & Gestational Age**: Naegele's rule based on Last Menstrual Period (LMP) with 28-day cycle assumption, current gestational week/trimester, and explicit clinical disclaimer that estimates do not replace medical diagnosis.

- **Data Privacy & Storage**:
  - Room SQLite Database for calculation history (storing tool ID, inputs, results, and timestamp).
  - Explicit filter: Health & pregnancy estimates are strictly excluded from automated history logging unless the user toggles an explicit "Save Note" on the tool.
  - "Clear All History" and "Delete Health Data" one-tap buttons in Settings.

- **Monetization & AdMob Policy**:
  - Non-intrusive bottom adaptive banner container using standard Google AdMob test ad IDs.
  - Ads are hidden on health estimate screens to comply with Google Play health and sensitive content policies.
  - Zero disruptive interstitials on app open or immediate navigation.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                      │
│   HomeScreen  │  CalculatorDetailScreen  │  HistoryScreen       │
└───────────────────────────────┬────────────────────────────────┘
                                │ State / Events
┌───────────────────────────────▼────────────────────────────────┐
│                    ViewModel & StateFlow                       │
│   MainViewModel: Search query, favorites, active route         │
│   CalcViewModel: Real-time calculation logic & input validation│
└───────────────┬────────────────────────────────┬───────────────┘
                │                                │
┌───────────────▼──────────────┐  ┌──────────────▼───────────────┐
│     Calculation Engines      │  │      Local Data Store        │
│  - FinancialMath (BigDecimal)│  │  - AppDatabase (Room)        │
│  - DateMath (java.time)      │  │  - HistoryDao (Records)      │
│  - UnitConverterEngine       │  │  - UserPreferences (DataStore)│
│  - HealthMath (Estimates)    │  │  - Clear Data Controls       │
└──────────────────────────────┘  └──────────────────────────────┘
```

- **Numeric Accuracy**: Uses `BigDecimal` with `RoundingMode.HALF_UP` for all monetary, tax, tip, and discount math to eliminate IEEE-754 floating-point inaccuracies.
- **Date Handling**: Uses `java.time.LocalDate` and `java.time.Period` for exact calendar math including leap years and month boundaries.
- **Testing Strategy**: Robolectric unit tests covering all 12 core engines, edge cases (zero inputs, negative values, leap days, extreme numbers), and history persistence.
