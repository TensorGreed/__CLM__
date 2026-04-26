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

The app contains only the routed operational shell needed for R1-E01:

- Sidebar navigation.
- Overview route.
- System route.
- Responsive layout.
- Component tests.

Certificate inventory, authentication, RBAC, dashboards, and lifecycle workflows start in later roadmap stories.

## Testing

Vitest and Testing Library are configured with jsdom.

Tests live next to the components they verify.
