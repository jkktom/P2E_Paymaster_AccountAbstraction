## Blooming Blockchain Service — Frontend (Next.js App Router)

This is the Next.js frontend for the Blooming Blockchain Service. The UI is fully in English and integrates with a Spring Boot backend and zkSync (Account Abstraction with a paymaster).

### Tech Stack
- Next.js (App Router, TypeScript, Tailwind CSS)
- Google OAuth (backend-driven)
- Spring Boot REST API integration
- zkSync AA concepts surfaced in UI (gasless actions)

---

## Prerequisites
- Node.js 18+
- Backend running (default: `http://localhost:8080`)

---

## Setup
1. Install dependencies:
```bash
npm install
# or
yarn install
```

2. Start the dev server:
```bash
npm run dev
# or
yarn dev
```
Open http://localhost:3000

---

## Available Scripts
- `npm run dev` — Start development server
- `npm run build` — Build for production
- `npm run start` — Start production build
- `npm run lint` — Lint code

---

## Features
- Google Sign-In (redirects to backend OAuth)
- Points and token management (earn, convert, exchange)
- Governance proposals (create, list, vote)
- AA wallet address display (gasless flow)

All user-facing text has been translated to English. Date formatting uses the `en-US` locale.

---

## Key Files
- Main page: `src/app/page.tsx`
- Components: `src/components/*`
- Proposals: `src/components/proposals/*`
- Utilities: `src/utils/*`

---

## Troubleshooting
- API calls failing: check `NEXT_PUBLIC_API_BASE_URL` and backend availability.
- Google login not completing: ensure backend OAuth callback is configured and reachable.
- Date display issues: UI uses English locale; confirm backend date formats.

---

## Deployment
Build and run:
```bash
npm run build
npm run start
```
Ensure environment variables are configured on your hosting platform.
