# JavaFX Dashboard with Role Login and Sign Up

This project is a JavaFX app with a polished centred login/signup card and a role-based dashboard navigation theme.

It also includes a floating **AI chatbot**. If you set an OpenAI-compatible API key, the chatbot can answer open-ended questions in a real conversational way.

## Features

- Login page with username/email-style authentication
- Sign up page for creating new users with a role
- Role detection after login (`ADMIN`, `TEACHER`, `STUDENT`)
- Sidebar buttons for `Users`, `Courses`, `Evaluation`, `Presence`
- Role-based section visibility and role-specific content
- Logout button to return to login page
- Dashboard pages use the same modern card/grid theme as the login and sign-up screens
- Floating draggable AI chat button on the dashboard
- OpenAI-compatible chatbot mode with local fallback when no API key is configured
- SQLite persistence for users and courses (JDBC)
- Layered architecture foundation (service/use-case + repository + infrastructure)

## Architecture (current)

- `Main.java`: JavaFX composition root and view rendering
- `AuthService.java`: facade used by UI
- `AuthUseCase.java`: authentication/register business rules
- `UserRepository.java` + `SQLiteUserRepository.java`: user data access
- `DatabaseManager.java`: SQLite connection + schema initialization
- `Cours.java` + `CoursRepository.java` + `SQLiteCoursRepository.java`: course CRUD layer
- `AiChatService.java`: chatbot provider (online + local fallback)

This keeps UI code usable while progressively moving business and persistence logic out of `Main.java`.

## Login design

The login page now uses a centred white card, branding header, email/password fields, remember-me and forgot-password actions, social buttons, and a gradient sign-in button.

## Sign up design

The sign up page matches the same theme and lets a new user choose a username, email, password, confirmation password, and role before registering.

## Dashboard pages

- `Users`: user management cards and table-style rows
- `Courses`: class cards and recent activity panels
- `Evaluation`: grade cards and progress bars
- `Presence`: attendance cards and schedule panels

## Demo credentials

- `admin` / `admin123`
- `admin@educore.com` / `admin123`
- `teacher` / `teacher123`
- `teacher@educore.com` / `teacher123`
- `student` / `student123`
- `student@educore.com` / `student123`

You can also create your own account from the sign up page and then sign in with that username or email.

## Project files

- `src/Main.java`: Login, sign up, dashboard scene flow, and view switching
- `src/AuthService.java`: Authentication facade used by UI
- `src/AuthUseCase.java`: Authentication/register business logic
- `src/UserRepository.java`: User repository contract
- `src/SQLiteUserRepository.java`: SQLite implementation for users
- `src/DatabaseManager.java`: SQLite schema and connection manager
- `src/Cours.java`: Course model
- `src/CoursRepository.java`: Course repository contract
- `src/SQLiteCoursRepository.java`: SQLite CRUD implementation for courses
- `src/AiChatService.java`: OpenAI-compatible AI chat client with fallback logic
- `src/ChatMessage.java`: Chat message record used by the AI service
- `src/User.java`: User model
- `src/Role.java`: Role enum
- `src/ArchitectureSmokeTest.java`: tiny runner to verify architecture wiring
- `pom.xml`: Maven JavaFX configuration

## Configure real AI chat

Set these environment variables before launching the app:

```powershell
$env:OPENAI_API_KEY = "your-api-key"
$env:OPENAI_MODEL = "gpt-4o-mini"
# Optional if you are using an OpenAI-compatible proxy or custom endpoint:
# $env:OPENAI_BASE_URL = "https://api.openai.com"
```

If `OPENAI_API_KEY` is not set, the chatbot still works in local fallback mode.

## Run

```powershell
Set-Location "C:\Users\chahi\IdeaProjects\test"
mvn -q clean compile
mvn -q javafx:run
```

## Run architecture smoke test

This verifies SQLite schema + auth + course repository wiring:

```powershell
Set-Location "C:\Users\chahi\IdeaProjects\test"
mvn -q -DskipTests compile
java -cp "target\classes;target\dependency\*" ArchitectureSmokeTest
```

If `target\dependency` is empty, copy dependencies first:

```powershell
Set-Location "C:\Users\chahi\IdeaProjects\test"
mvn -q dependency:copy-dependencies -DincludeScope=runtime
java -cp "target\classes;target\dependency\*" ArchitectureSmokeTest
```

Or run with AI enabled:

```powershell
$env:OPENAI_API_KEY = "your-api-key"
Set-Location "C:\Users\chahi\IdeaProjects\test"
mvn -q javafx:run
```

