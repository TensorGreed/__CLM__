import { NavLink, Route, Routes } from 'react-router-dom'

const navigation = [
  { to: '/', label: 'Overview' },
  { to: '/system', label: 'System' },
]

const readinessItems = [
  ['Backend', 'Spring Boot API skeleton with Actuator health'],
  ['Frontend', 'React TypeScript app shell with routed views'],
  ['Data', 'PostgreSQL local development service'],
  ['Delivery', 'CI jobs for backend, frontend, docs, and dependency review'],
]

export function App() {
  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">
            CLM
          </span>
          <div>
            <strong>CLM Platform</strong>
            <span>Foundation</span>
          </div>
        </div>

        <nav className="nav-list">
          {navigation.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                isActive ? 'nav-link active' : 'nav-link'
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="main-panel">
        <header className="topbar">
          <div>
            <p className="eyebrow">R1-E01</p>
            <h1>Repository Foundation</h1>
          </div>
          <span className="status-pill">Local</span>
        </header>

        <Routes>
          <Route path="/" element={<OverviewPage />} />
          <Route path="/system" element={<SystemPage />} />
        </Routes>
      </main>
    </div>
  )
}

function OverviewPage() {
  return (
    <section className="content-band" aria-labelledby="overview-heading">
      <div className="section-heading">
        <p className="eyebrow">Build slice</p>
        <h2 id="overview-heading">Application skeleton</h2>
      </div>

      <div className="readiness-grid">
        {readinessItems.map(([label, description]) => (
          <article className="readiness-item" key={label}>
            <span>{label}</span>
            <p>{description}</p>
          </article>
        ))}
      </div>
    </section>
  )
}

function SystemPage() {
  return (
    <section className="content-band" aria-labelledby="system-heading">
      <div className="section-heading">
        <p className="eyebrow">Runtime</p>
        <h2 id="system-heading">Local services</h2>
      </div>

      <dl className="system-list">
        <div>
          <dt>API</dt>
          <dd>http://localhost:8080/actuator/health</dd>
        </div>
        <div>
          <dt>UI</dt>
          <dd>http://localhost:5173</dd>
        </div>
        <div>
          <dt>PostgreSQL</dt>
          <dd>localhost:5432, database clm</dd>
        </div>
      </dl>
    </section>
  )
}
