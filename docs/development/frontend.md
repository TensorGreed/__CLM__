# Frontend Foundation

The frontend is a React TypeScript app built with Vite.

## Commands

```bash
npm ci
npm run dev
npm run lint
npm test
npm run build
```

## Current Scope

The app contains the routed operational shell and first certificate inventory workflow:

- Sidebar navigation.
- Role-aware navigation.
- Top certificate search.
- Tenant selector.
- User/access menu with Basic and bearer API credential modes.
- Certificate inventory route.
- Certificate detail route.
- System route.
- Route guards and permission-denied state.
- Loading, empty, error, and permission-denied states.
- Responsive layout.
- Component tests.

The inventory table supports filters, sorting, pagination, saved columns, and bulk selection. Certificate detail pages show summary, versions, chain entries, source observations, metadata, status history, audit events, and placeholders for later endpoint, destination, notification, policy, and job sections.

The Vite dev server proxies `/api`, `/actuator`, and `/v3` to the Spring Boot API. Local `npm run dev` defaults to `http://localhost:8080`; Docker Compose sets `VITE_DEV_PROXY_TARGET=http://api:8080` for container-to-container traffic.

Private key handling, issuance, renewal, destinations, plugins, MCP, settings pages, and global cross-resource search are later roadmap stories.

## Testing

Vitest and Testing Library are configured with jsdom.

Tests live next to the components they verify.
