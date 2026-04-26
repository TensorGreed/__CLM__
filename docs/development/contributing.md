# Contributing

Use roadmap story IDs in branches, commits, and pull requests.

Example:

```text
feature/R1-E02-S01-api-conventions
```

## Before Opening A Pull Request

Run the relevant checks:

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm run lint
npm test
npm run build
```

```bash
npm run docs:lint
```

## Pull Request Notes

Include:

- Story ID.
- User-visible behavior.
- Security considerations.
- Tests run.
- Documentation updated.
- Migration or deployment notes.
