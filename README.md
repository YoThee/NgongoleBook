# Ngongole Book

BSE 3340 Mobile Application Development, Zambia University College of Technology.

## The problem

Small traders in Zambia (shops, market stalls, tuck shops, salons) often sell on credit (*ngongole*). They usually track it in an exercise book. Pages get lost, part-payments are forgotten, and nobody notices who is late.

**Ngongole Book lets a small trader record who owes them what, log part-payments, and get a reminder when a customer passes their promised date.** It works fully offline.

## Team

| Name                 | Student number | GitHub account | Owns research topic |
|------                |----------------|----------------|---------------------|
| Kunda Lwando         |  2300785       |      YoThee    | Room database       |
| Daliso Chanda        |  2200811       |                | Room database       |
| Sean Anderson        |  2300811       |                | WorkManager + notifications |
| Vigo Nyamabo         |  2300754       |     vigo4vrlit | WorkManager + notifications |
| Tamba Martin Syatyoka|  2300892       |                | Navigation + form (explains both topics at a basic level) |

## How to build and run

Requirements: **Android Studio** (Koala or newer), which includes JDK 17. An emulator (API 24+) or a phone with USB debugging.

1. `git clone <repository-url>`
2. In Android Studio choose **File > Open** and select the `NgongoleBook` folder (the folder that contains `settings.gradle.kts`, not the parent `web` folder).
3. If Android Studio asks to install the Android SDK / API 34, accept it. Wait for Gradle sync (first time it downloads Gradle 8.7 and libraries, so you need internet once).
4. Press **Run** (green triangle) with an emulator or phone selected.
5. Optional: run the unit tests with **Run > Run Tests in 'ValidatorsTest'**.

To see the reminder notification quickly: open **Settings** (gear icon) > turn on *Daily overdue reminder* > allow notifications > *Send a test notification now*.

## Screens

| Screen | Purpose |
|--------|---------|
| **Debt list** (home) | Total owed, overdue count, list of debts, search, filters, empty state |
| **New debt** | Validated form: name, phone, item, amount, promised date |
| **Debt details** | Balance, payment history, record a payment, call / SMS reminder, delete |
| **Settings** | Turn the daily reminder on/off, notification permission, test notification |

## How the brief's requirements are met

| Requirement | Where |
|-------------|-------|
| 3+ screens, back button works | `ui/navigation/AppNavHost.kt` (4 screens, `popBackStack()` and the system back button) |
| Form with validation | `ui/add/AddDebtScreen.kt` + `domain/Validators.kt` (errors shown under each field) |
| Scrollable list | `ui/list/DebtListScreen.kt` (`LazyColumn`) |
| Lifecycle handled | State lives in ViewModels (`*ViewModel.kt`), so rotation does not lose the form, search text or filter. The dialogs use `rememberSaveable`. |
| Saving data | Room database `ngongole.db` (`data/`) |
| Clean structure | `data/` (database), `domain/` (rules, formatting), `notifications/` (background work), `ui/` (screens and ViewModels) |
| Does not crash | Empty states, validation, notification permission denied, no dialer/SMS app (`try/catch`), deleted debt screen |
| Runs from a fresh clone | No keys, no secrets, no local files needed |
| Two researched concepts | See the Research Note below |

## Project structure

```
app/src/main/java/com/zuct/ngongolebook/
  NgongoleApp.kt          creates the repository and reminder manager once
  MainActivity.kt         hosts the Compose UI
  data/                   Entities, DebtDao, AppDatabase, DebtRepository
  domain/                 Validators, Formatters, DebtStatus (no UI code)
  notifications/          NotificationHelper, OverdueReminderWorker, ReminderManager
  ui/
    navigation/           AppNavHost
    list/ add/ detail/ settings/    Screen + ViewModel for each screen
    components/           Reusable pieces (summary card, debt row, empty state)
    theme/                Colours, automatic dark mode
```

## Research Note

> **Team: edit this section before submitting.** The technical explanation below describes what the code does. The "What went wrong" paragraphs must be replaced with the real problems *your* group hit, because you will be asked about them in the interview.

### Concept 1: Room database (owner: ______)

**What it is.** Room is Android's library on top of SQLite. You describe tables as Kotlin classes (`@Entity`), queries as functions (`@Dao`), and Room generates the code. Queries that return `Flow` re-run automatically when the table changes.

**Why the app needs it.** A debt ledger is relational: one debt has many payments. We have two tables (`debts`, `payments`) linked by a foreign key with `ON DELETE CASCADE`, so deleting a debt removes its payments. One query (`DebtDao.observeAll`) adds up the payments for each debt using a sub-select, so the balance is always correct. Because the list screen collects a `Flow`, recording a payment updates the list without any refresh code.

**Where we learned it.**
- Android Developers: *Save data in a local database using Room* https://developer.android.com/training/data-storage/room
- Android Developers: *Define relationships between objects* https://developer.android.com/training/data-storage/room/relationships
- Android Basics with Compose codelab: *Persist data with Room*

**What went wrong.** *(Replace with your own experience. Typical problems: KSP and Kotlin version mismatch breaking the build; forgetting the foreign-key index; "Cannot access database on the main thread" until the DAO functions were made `suspend`.)*

### Concept 2: WorkManager and notifications with runtime permission (owner: ______)

**What it is.** WorkManager schedules background work that still runs when the app is closed or the phone restarts. Notifications need a *channel* (Android 8+) and, on Android 13+, the user's permission (`POST_NOTIFICATIONS`).

**Why the app needs it.** The trader should be told that customers are late without opening the app. A `PeriodicWorkRequest` runs every 24 hours (first run delayed until 08:00). `OverdueReminderWorker` reads the overdue debts from Room and posts one notification. Settings asks for the permission at the moment the user turns reminders on; if they refuse, reminders stay off and the app explains why instead of crashing. `NotificationHelper.show` also re-checks permission before posting.

**Where we learned it.**
- Android Developers: *Schedule tasks with WorkManager* https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started
- Android Developers: *Notification runtime permission* https://developer.android.com/develop/ui/views/notifications/notification-permission
- Android Developers: *Create a notification* https://developer.android.com/develop/ui/views/notifications/build-notification

**What went wrong.** *(Replace with your own experience. Typical problems: no notification appeared because the permission was denied; periodic work can be delayed by the system so we added a "send a test notification" button using a one-time request; forgetting to create the channel.)*

### Tools and AI use

Android Studio, Kotlin, Jetpack Compose. AI tools were used as a tutor and to help draft code. Every member must be able to explain the code they submit.

## Commit rule

Every member commits from their own GitHub account. Do not commit everything from one laptop.
