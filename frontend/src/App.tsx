import {
  Link,
  Navigate,
  NavLink,
  Route,
  Routes,
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
} from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import {
  ApiError,
  createHttpApiClient,
  type ApiClient,
  type ApiSession,
  type CertificateAuditEventResponse,
  type CertificateChainEntryResponse,
  type CertificateDetailResponse,
  type CertificateListQuery,
  type CertificateMetadataValueResponse,
  type CertificateSourceObservationResponse,
  type CertificateStatus,
  type CertificateStatusHistoryResponse,
  type CertificateSummaryResponse,
  type CertificateVersionResponse,
  type PageResponse,
  type Permission,
  type RoleKey,
  type TenantResponse,
} from './api'

const defaultApiClient = createHttpApiClient()

const rolePermissions: Record<RoleKey, Permission[]> = {
  NO_ACCESS: [],
  ADMIN: [
    'TENANT_READ',
    'TENANT_MANAGE',
    'ORGANIZATION_READ',
    'ORGANIZATION_MANAGE',
    'ROLE_READ',
    'ROLE_MANAGE',
    'CERTIFICATE_READ',
    'CERTIFICATE_IMPORT',
    'CERTIFICATE_MANAGE',
    'SERVICE_ACCOUNT_READ',
    'SERVICE_ACCOUNT_MANAGE',
    'AUDIT_READ',
    'SENSITIVE_ACTION_EXECUTE',
    'TASK_READ',
    'TASK_MANAGE',
  ],
  OPERATOR: [
    'TENANT_READ',
    'ORGANIZATION_READ',
    'ROLE_READ',
    'CERTIFICATE_READ',
    'CERTIFICATE_IMPORT',
    'CERTIFICATE_MANAGE',
    'TASK_READ',
    'TASK_MANAGE',
    'SENSITIVE_ACTION_EXECUTE',
  ],
  REQUESTER: [
    'TENANT_READ',
    'ORGANIZATION_READ',
    'CERTIFICATE_READ',
    'TASK_READ',
  ],
  APPROVER: [
    'TENANT_READ',
    'ORGANIZATION_READ',
    'ROLE_READ',
    'CERTIFICATE_READ',
    'TASK_READ',
    'AUDIT_READ',
    'SENSITIVE_ACTION_EXECUTE',
  ],
  AUDITOR: [
    'TENANT_READ',
    'ORGANIZATION_READ',
    'ROLE_READ',
    'CERTIFICATE_READ',
    'TASK_READ',
    'AUDIT_READ',
  ],
  READ_ONLY: [
    'TENANT_READ',
    'ORGANIZATION_READ',
    'ROLE_READ',
    'CERTIFICATE_READ',
    'TASK_READ',
  ],
  SERVICE_ACCOUNT: [
    'TENANT_READ',
    'ORGANIZATION_READ',
    'CERTIFICATE_READ',
    'TASK_READ',
  ],
}

const defaultSession: ApiSession = {
  role: 'OPERATOR',
  authMode: 'basic',
  email: '',
  password: '',
  token: '',
  tenantId: '',
}

const inventoryColumns = [
  { key: 'status', label: 'Status' },
  { key: 'subject', label: 'Subject' },
  { key: 'owner', label: 'Owner' },
  { key: 'issuer', label: 'Issuer' },
  { key: 'validTo', label: 'Expires' },
  { key: 'sans', label: 'SANs' },
  { key: 'tags', label: 'Tags' },
  { key: 'fingerprint', label: 'Fingerprint' },
]

const defaultColumns = [
  'status',
  'subject',
  'owner',
  'issuer',
  'validTo',
  'tags',
]

interface AppProps {
  apiClient?: ApiClient
  initialSession?: Partial<ApiSession>
  storageKey?: string
}

interface InventoryQueryState {
  subject: string
  owner: string
  status: string
  tag: string
  metadataKey: string
  metadataValue: string
  sort: string
  page: number
  size: number
}

type LoadState<T> =
  | { status: 'idle' | 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; error: unknown }

export function App({
  apiClient = defaultApiClient,
  initialSession,
  storageKey = 'clm.ui.profile',
}: AppProps) {
  const location = useLocation()
  const [session, setSession] = useState<ApiSession>(() =>
    loadSession(storageKey, initialSession),
  )

  function updateSession(patch: Partial<ApiSession>) {
    setSession((current) => {
      const next = { ...current, ...patch }
      persistSession(storageKey, next)
      return next
    })
  }

  return (
    <AppShell session={session} updateSession={updateSession} apiClient={apiClient}>
      <Routes>
        <Route path="/" element={<Navigate to="/certificates" replace />} />
        <Route
          path="/certificates"
          element={
            <RequirePermission session={session} permission="CERTIFICATE_READ">
              <InventoryPage
                key={`inventory-${location.search}`}
                apiClient={apiClient}
                session={session}
                storageKey={storageKey}
              />
            </RequirePermission>
          }
        />
        <Route
          path="/certificates/:certificateId"
          element={
            <RequirePermission session={session} permission="CERTIFICATE_READ">
              <CertificateDetailPage
                key={`detail-${location.pathname}`}
                apiClient={apiClient}
                session={session}
              />
            </RequirePermission>
          }
        />
        <Route path="/system" element={<SystemPage session={session} />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </AppShell>
  )
}

interface ShellProps {
  session: ApiSession
  updateSession: (patch: Partial<ApiSession>) => void
  apiClient: ApiClient
  children: React.ReactNode
}

function AppShell({ session, updateSession, apiClient, children }: ShellProps) {
  const navigate = useNavigate()
  const [search, setSearch] = useState('')

  function submitSearch(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const value = search.trim()
    navigate(value ? `/certificates?search=${encodeURIComponent(value)}` : '/certificates')
  }

  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">
            CLM
          </span>
          <div>
            <strong>CLM Platform</strong>
            <span>Operations</span>
          </div>
        </div>

        <nav className="nav-list">
          {hasPermission(session.role, 'CERTIFICATE_READ') ? (
            <NavLink
              to="/certificates"
              className={({ isActive }) =>
                isActive ? 'nav-link active' : 'nav-link'
              }
            >
              <span aria-hidden="true">CI</span>
              Certificates
            </NavLink>
          ) : null}
          <NavLink
            to="/system"
            className={({ isActive }) =>
              isActive ? 'nav-link active' : 'nav-link'
            }
          >
            <span aria-hidden="true">SY</span>
            System
          </NavLink>
        </nav>

        <AccessPanel session={session} updateSession={updateSession} />
      </aside>

      <main className="main-panel">
        <header className="topbar">
          <form className="global-search" role="search" onSubmit={submitSearch}>
            <label className="visually-hidden" htmlFor="global-search">
              Search certificates
            </label>
            <input
              id="global-search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search certificates"
            />
            <button type="submit">Search</button>
          </form>

          <TenantSelector
            session={session}
            updateSession={updateSession}
            apiClient={apiClient}
          />

          <div className="user-menu" aria-label="User menu">
            <span>{roleLabel(session.role)}</span>
            <strong>{principalLabel(session)}</strong>
          </div>
        </header>

        {children}
      </main>
    </div>
  )
}

function AccessPanel({
  session,
  updateSession,
}: {
  session: ApiSession
  updateSession: (patch: Partial<ApiSession>) => void
}) {
  return (
    <section className="access-panel" aria-labelledby="access-heading">
      <h2 id="access-heading">Access</h2>
      <label>
        Role
        <select
          value={session.role}
          onChange={(event) =>
            updateSession({ role: event.target.value as RoleKey })
          }
        >
          {Object.keys(rolePermissions).map((role) => (
            <option value={role} key={role}>
              {roleLabel(role as RoleKey)}
            </option>
          ))}
        </select>
      </label>

      <label>
        API auth
        <select
          value={session.authMode}
          onChange={(event) =>
            updateSession({
              authMode: event.target.value as ApiSession['authMode'],
              password: '',
              token: '',
            })
          }
        >
          <option value="none">None</option>
          <option value="basic">Basic</option>
          <option value="bearer">Bearer token</option>
        </select>
      </label>

      {session.authMode === 'basic' ? (
        <>
          <label>
            Email
            <input
              value={session.email}
              autoComplete="username"
              onChange={(event) => updateSession({ email: event.target.value })}
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={session.password}
              autoComplete="current-password"
              onChange={(event) =>
                updateSession({ password: event.target.value })
              }
            />
          </label>
        </>
      ) : null}

      {session.authMode === 'bearer' ? (
        <label>
          Token
          <input
            type="password"
            value={session.token}
            autoComplete="off"
            onChange={(event) => updateSession({ token: event.target.value })}
          />
        </label>
      ) : null}
    </section>
  )
}

function TenantSelector({
  session,
  updateSession,
  apiClient,
}: {
  session: ApiSession
  updateSession: (patch: Partial<ApiSession>) => void
  apiClient: ApiClient
}) {
  const [state, setState] = useState<LoadState<TenantResponse[]>>({
    status: 'loading',
  })

  useEffect(() => {
    const controller = new AbortController()
    apiClient
      .listTenants(session, controller.signal)
      .then((data) => setState({ status: 'success', data }))
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, session])

  if (state.status === 'success') {
    return (
      <label className="tenant-select">
        Tenant
        <select
          value={session.tenantId}
          onChange={(event) => updateSession({ tenantId: event.target.value })}
        >
          <option value="">All accessible</option>
          {state.data.map((tenant) => (
            <option value={tenant.id} key={tenant.id}>
              {tenant.name}
            </option>
          ))}
        </select>
      </label>
    )
  }

  return (
    <label className="tenant-select">
      Tenant
      <select disabled value="">
        <option>
          {state.status === 'error' ? 'Unavailable' : 'Loading tenants'}
        </option>
      </select>
    </label>
  )
}

function InventoryPage({
  apiClient,
  session,
  storageKey,
}: {
  apiClient: ApiClient
  session: ApiSession
  storageKey: string
}) {
  const [searchParams] = useSearchParams()
  const searchSubject = searchParams.get('search') ?? ''
  const [draft, setDraft] = useState<InventoryQueryState>(() =>
    initialInventoryQuery(searchSubject),
  )
  const [query, setQuery] = useState<InventoryQueryState>(() =>
    initialInventoryQuery(searchSubject),
  )
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set())
  const [visibleColumns, setVisibleColumns] = useState<string[]>(() =>
    loadColumns(`${storageKey}.inventoryColumns`),
  )
  const [state, setState] = useState<LoadState<PageResponse<CertificateSummaryResponse>>>({
    status: 'loading',
  })

  useEffect(() => {
    persistColumns(`${storageKey}.inventoryColumns`, visibleColumns)
  }, [storageKey, visibleColumns])

  const listQuery = useMemo<CertificateListQuery>(
    () => ({
      tenantId: session.tenantId || undefined,
      page: query.page,
      size: query.size,
      filters: inventoryFilters(query),
      sort: query.sort,
    }),
    [query, session.tenantId],
  )

  useEffect(() => {
    const controller = new AbortController()
    apiClient
      .listCertificates(listQuery, session, controller.signal)
      .then((data) => {
        setState({ status: 'success', data })
        setSelectedIds(
          (current) =>
            new Set(
              [...current].filter((id) =>
                data.content.some((certificate) => certificate.id === id),
              ),
            ),
        )
      })
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, listQuery, session])

  function applyFilters(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setQuery({ ...draft, page: 0 })
    setSelectedIds(new Set())
  }

  function resetFilters() {
    const next = initialInventoryQuery('')
    setDraft(next)
    setQuery(next)
    setSelectedIds(new Set())
  }

  function toggleColumn(column: string) {
    setVisibleColumns((current) => {
      if (current.includes(column)) {
        const next = current.filter((item) => item !== column)
        return next.length === 0 ? current : next
      }
      return [...current, column]
    })
  }

  const page = state.status === 'success' ? state.data : null
  const allPageSelected =
    page !== null &&
    page.content.length > 0 &&
    page.content.every((certificate) => selectedIds.has(certificate.id))

  return (
    <section className="workspace" aria-labelledby="inventory-heading">
      <div className="workspace-header">
        <div>
          <p className="eyebrow">Inventory</p>
          <h1 id="inventory-heading">Certificate Inventory</h1>
        </div>
        <div className="toolbar">
          <span className="selection-count">{selectedIds.size} selected</span>
          <details className="column-menu">
            <summary>Columns</summary>
            <div>
              {inventoryColumns.map((column) => (
                <label key={column.key}>
                  <input
                    type="checkbox"
                    checked={visibleColumns.includes(column.key)}
                    onChange={() => toggleColumn(column.key)}
                  />
                  {column.label}
                </label>
              ))}
            </div>
          </details>
        </div>
      </div>

      <form className="filter-bar" onSubmit={applyFilters}>
        <label>
          Subject
          <input
            value={draft.subject}
            onChange={(event) =>
              setDraft((current) => ({
                ...current,
                subject: event.target.value,
              }))
            }
          />
        </label>
        <label>
          Status
          <select
            value={draft.status}
            onChange={(event) =>
              setDraft((current) => ({ ...current, status: event.target.value }))
            }
          >
            <option value="">Any</option>
            <option value="ACTIVE">Active</option>
            <option value="EXPIRED">Expired</option>
            <option value="REVOKED">Revoked</option>
          </select>
        </label>
        <label>
          Owner
          <input
            value={draft.owner}
            onChange={(event) =>
              setDraft((current) => ({ ...current, owner: event.target.value }))
            }
          />
        </label>
        <label>
          Tag
          <input
            value={draft.tag}
            onChange={(event) =>
              setDraft((current) => ({ ...current, tag: event.target.value }))
            }
          />
        </label>
        <label>
          Metadata key
          <input
            value={draft.metadataKey}
            onChange={(event) =>
              setDraft((current) => ({
                ...current,
                metadataKey: event.target.value,
              }))
            }
          />
        </label>
        <label>
          Metadata value
          <input
            value={draft.metadataValue}
            onChange={(event) =>
              setDraft((current) => ({
                ...current,
                metadataValue: event.target.value,
              }))
            }
          />
        </label>
        <label>
          Sort
          <select
            value={draft.sort}
            onChange={(event) =>
              setDraft((current) => ({ ...current, sort: event.target.value }))
            }
          >
            <option value="expiresAt,asc">Expiry ascending</option>
            <option value="expiresAt,desc">Expiry descending</option>
            <option value="owner,asc">Owner ascending</option>
            <option value="issuer,asc">Issuer ascending</option>
            <option value="status,asc">Status ascending</option>
          </select>
        </label>
        <div className="filter-actions">
          <button type="submit">Apply</button>
          <button type="button" className="secondary-button" onClick={resetFilters}>
            Reset
          </button>
        </div>
      </form>

      {state.status === 'loading' || state.status === 'idle' ? (
        <LoadingState label="Loading certificates" />
      ) : null}
      {state.status === 'error' ? <ErrorState error={state.error} /> : null}
      {state.status === 'success' && state.data.content.length === 0 ? (
        <EmptyState title="No certificates match the current view" />
      ) : null}
      {state.status === 'success' && state.data.content.length > 0 ? (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th scope="col" className="select-column">
                    <input
                      type="checkbox"
                      aria-label="Select all certificates on this page"
                      checked={allPageSelected}
                      onChange={(event) => {
                        const next = new Set(selectedIds)
                        for (const certificate of state.data.content) {
                          if (event.target.checked) {
                            next.add(certificate.id)
                          } else {
                            next.delete(certificate.id)
                          }
                        }
                        setSelectedIds(next)
                      }}
                    />
                  </th>
                  {inventoryColumns
                    .filter((column) => visibleColumns.includes(column.key))
                    .map((column) => (
                      <th scope="col" key={column.key}>
                        {column.label}
                      </th>
                    ))}
                </tr>
              </thead>
              <tbody>
                {state.data.content.map((certificate) => (
                  <CertificateRow
                    key={certificate.id}
                    certificate={certificate}
                    visibleColumns={visibleColumns}
                    selected={selectedIds.has(certificate.id)}
                    onSelectedChange={(selected) => {
                      setSelectedIds((current) => {
                        const next = new Set(current)
                        if (selected) {
                          next.add(certificate.id)
                        } else {
                          next.delete(certificate.id)
                        }
                        return next
                      })
                    }}
                  />
                ))}
              </tbody>
            </table>
          </div>

          <div className="pagination-bar">
            <span>
              Page {state.data.page + 1} of {Math.max(state.data.totalPages, 1)}
            </span>
            <div>
              <button
                type="button"
                className="secondary-button"
                disabled={query.page === 0}
                onClick={() =>
                  setQuery((current) => ({
                    ...current,
                    page: Math.max(current.page - 1, 0),
                  }))
                }
              >
                Previous
              </button>
              <button
                type="button"
                className="secondary-button"
                disabled={query.page + 1 >= state.data.totalPages}
                onClick={() =>
                  setQuery((current) => ({ ...current, page: current.page + 1 }))
                }
              >
                Next
              </button>
            </div>
          </div>
        </>
      ) : null}
    </section>
  )
}

function CertificateRow({
  certificate,
  visibleColumns,
  selected,
  onSelectedChange,
}: {
  certificate: CertificateSummaryResponse
  visibleColumns: string[]
  selected: boolean
  onSelectedChange: (selected: boolean) => void
}) {
  return (
    <tr>
      <td className="select-column">
        <input
          type="checkbox"
          aria-label={`Select ${certificateLabel(certificate)}`}
          checked={selected}
          onChange={(event) => onSelectedChange(event.target.checked)}
        />
      </td>
      {visibleColumns.includes('status') ? (
        <td>
          <StatusBadge status={certificate.status} />
        </td>
      ) : null}
      {visibleColumns.includes('subject') ? (
        <td className="primary-cell">
          <Link to={`/certificates/${certificate.id}`}>
            {certificateLabel(certificate)}
          </Link>
          <span>{truncateMiddle(certificate.subject, 88)}</span>
        </td>
      ) : null}
      {visibleColumns.includes('owner') ? (
        <td>{certificate.owner ?? (certificate.orphaned ? 'Orphaned' : 'Unassigned')}</td>
      ) : null}
      {visibleColumns.includes('issuer') ? (
        <td>{truncateMiddle(certificate.issuer, 72)}</td>
      ) : null}
      {visibleColumns.includes('validTo') ? (
        <td>{formatDate(certificate.validTo)}</td>
      ) : null}
      {visibleColumns.includes('sans') ? (
        <td>{certificate.subjectAlternativeNames.slice(0, 3).join(', ')}</td>
      ) : null}
      {visibleColumns.includes('tags') ? <td>{tagList(certificate.tags)}</td> : null}
      {visibleColumns.includes('fingerprint') ? (
        <td className="mono">{truncateMiddle(certificate.sha256Fingerprint, 34)}</td>
      ) : null}
    </tr>
  )
}

function CertificateDetailPage({
  apiClient,
  session,
}: {
  apiClient: ApiClient
  session: ApiSession
}) {
  const { certificateId } = useParams()
  const [activeTab, setActiveTab] = useState('summary')
  const [state, setState] = useState<LoadState<CertificateDetailResponse>>({
    status: 'loading',
  })

  useEffect(() => {
    if (!certificateId) {
      return
    }
    const controller = new AbortController()
    apiClient
      .getCertificate(certificateId, session, controller.signal)
      .then((data) => setState({ status: 'success', data }))
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, certificateId, session])

  if (!certificateId) {
    return <NotFoundPage />
  }

  if (state.status === 'loading' || state.status === 'idle') {
    return <LoadingState label="Loading certificate detail" />
  }

  if (state.status === 'error') {
    return <ErrorState error={state.error} />
  }

  if (state.status !== 'success') {
    return <LoadingState label="Loading certificate detail" />
  }

  const certificate = state.data
  const tabs = [
    ['summary', 'Summary'],
    ['versions', 'Versions'],
    ['chain', 'Chain'],
    ['sources', 'Sources'],
    ['metadata', 'Metadata'],
    ['status', 'Status'],
    ['audit', 'Audit'],
    ['automation', 'Automation'],
  ]

  return (
    <section className="workspace" aria-labelledby="detail-heading">
      <div className="workspace-header">
        <div>
          <Link className="back-link" to="/certificates">
            Back to inventory
          </Link>
          <h1 id="detail-heading">{certificateLabel(certificate)}</h1>
          <p className="detail-subtitle">{truncateMiddle(certificate.subject, 120)}</p>
        </div>
        <StatusBadge status={certificate.status} />
      </div>

      <div className="summary-strip">
        <SummaryItem label="Owner" value={certificate.owner ?? 'Orphaned'} />
        <SummaryItem label="Expires" value={formatDate(certificate.validTo)} />
        <SummaryItem label="Issuer" value={truncateMiddle(certificate.issuer, 48)} />
        <SummaryItem
          label="Current version"
          value={`v${certificate.currentVersion.versionNumber}`}
        />
      </div>

      <div className="tabs" role="tablist" aria-label="Certificate detail sections">
        {tabs.map(([key, label]) => (
          <button
            key={key}
            type="button"
            role="tab"
            aria-selected={activeTab === key}
            className={activeTab === key ? 'tab active' : 'tab'}
            onClick={() => setActiveTab(key)}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="tab-panel">
        {activeTab === 'summary' ? <SummarySection certificate={certificate} /> : null}
        {activeTab === 'versions' ? (
          <VersionsSection versions={certificate.versions} />
        ) : null}
        {activeTab === 'chain' ? <ChainSection chain={certificate.chain} /> : null}
        {activeTab === 'sources' ? (
          <SourcesSection observations={certificate.sourceObservations} />
        ) : null}
        {activeTab === 'metadata' ? (
          <MetadataSection metadata={certificate.metadata} />
        ) : null}
        {activeTab === 'status' ? (
          <StatusHistorySection history={certificate.statusHistory} />
        ) : null}
        {activeTab === 'audit' ? (
          <AuditSection events={certificate.auditTimeline} />
        ) : null}
        {activeTab === 'automation' ? <AutomationPlaceholders /> : null}
      </div>
    </section>
  )
}

function SummarySection({
  certificate,
}: {
  certificate: CertificateDetailResponse
}) {
  return (
    <div className="detail-grid">
      <DetailItem label="Common name" value={certificate.commonName ?? 'None'} />
      <DetailItem label="Serial number" value={certificate.serialNumber} />
      <DetailItem label="Valid from" value={formatDate(certificate.validFrom)} />
      <DetailItem label="Valid to" value={formatDate(certificate.validTo)} />
      <DetailItem
        label="SHA-256"
        value={certificate.sha256Fingerprint}
        monospace
      />
      <DetailItem label="SHA-1" value={certificate.sha1Fingerprint} monospace />
      <DetailItem
        label="Subject alternative names"
        value={certificate.subjectAlternativeNames.join(', ') || 'None'}
      />
      <DetailItem label="Tags" value={tagList(certificate.tags)} />
    </div>
  )
}

function VersionsSection({
  versions,
}: {
  versions: CertificateVersionResponse[]
}) {
  if (versions.length === 0) {
    return <EmptyState title="No certificate versions are recorded" />
  }
  return (
    <div className="table-wrap">
      <table className="data-table compact">
        <thead>
          <tr>
            <th scope="col">Version</th>
            <th scope="col">Source</th>
            <th scope="col">Validity</th>
            <th scope="col">Key</th>
            <th scope="col">Signature</th>
            <th scope="col">Fingerprint</th>
          </tr>
        </thead>
        <tbody>
          {versions.map((version) => (
            <tr key={version.id}>
              <td>v{version.versionNumber}</td>
              <td>{version.source}</td>
              <td>
                {formatDate(version.validFrom)} to {formatDate(version.validTo)}
              </td>
              <td>{version.publicKeyAlgorithm}</td>
              <td>{version.signatureAlgorithm}</td>
              <td className="mono">{truncateMiddle(version.sha256Fingerprint, 34)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function ChainSection({ chain }: { chain: CertificateChainEntryResponse[] }) {
  if (chain.length === 0) {
    return <EmptyState title="No imported chain entries are recorded" />
  }
  return (
    <Timeline>
      {chain.map((entry) => (
        <TimelineItem
          key={entry.id}
          title={`Position ${entry.position}`}
          meta={`${formatDate(entry.validFrom)} to ${formatDate(entry.validTo)}`}
        >
          <p>{entry.subject}</p>
          <p>{entry.selfSigned ? 'Self-signed' : entry.issuer}</p>
        </TimelineItem>
      ))}
    </Timeline>
  )
}

function SourcesSection({
  observations,
}: {
  observations: CertificateSourceObservationResponse[]
}) {
  if (observations.length === 0) {
    return <EmptyState title="No source observations are recorded" />
  }
  return (
    <div className="table-wrap">
      <table className="data-table compact">
        <thead>
          <tr>
            <th scope="col">Source</th>
            <th scope="col">Observed resource</th>
            <th scope="col">Last seen</th>
            <th scope="col">Count</th>
            <th scope="col">Metadata</th>
          </tr>
        </thead>
        <tbody>
          {observations.map((observation) => (
            <tr key={observation.id}>
              <td>
                <strong>{observation.sourceType}</strong>
                <span>{observation.sourceName ?? observation.sourceKey}</span>
              </td>
              <td>{truncateMiddle(observation.observedResourceKey, 74)}</td>
              <td>{formatDate(observation.lastSeenAt)}</td>
              <td>{observation.observationCount}</td>
              <td>{metadataPairs(observation.metadata)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function MetadataSection({
  metadata,
}: {
  metadata: Record<string, CertificateMetadataValueResponse>
}) {
  const entries = Object.entries(metadata)
  if (entries.length === 0) {
    return <EmptyState title="No custom metadata is recorded" />
  }
  return (
    <dl className="metadata-list">
      {entries.map(([key, value]) => (
        <div key={key}>
          <dt>{key}</dt>
          <dd>
            <span>{value.type}</span>
            {value.value}
          </dd>
        </div>
      ))}
    </dl>
  )
}

function StatusHistorySection({
  history,
}: {
  history: CertificateStatusHistoryResponse[]
}) {
  if (history.length === 0) {
    return <EmptyState title="No status transitions are recorded" />
  }
  return (
    <Timeline>
      {history.map((transition) => (
        <TimelineItem
          key={transition.id}
          title={`${transition.fromStatus ?? 'Created'} to ${transition.toStatus}`}
          meta={formatDate(transition.changedAt)}
        >
          <p>{transition.reason ?? 'No reason recorded'}</p>
          <p>{transition.changedBy ?? 'system'}</p>
        </TimelineItem>
      ))}
    </Timeline>
  )
}

function AuditSection({ events }: { events: CertificateAuditEventResponse[] }) {
  if (events.length === 0) {
    return <EmptyState title="No audit events are recorded" />
  }
  return (
    <Timeline>
      {events.map((event) => (
        <TimelineItem
          key={event.id}
          title={event.action}
          meta={`${formatDate(event.occurredAt)} | ${event.actorType}`}
        >
          <p>
            {event.decision} / {event.status}
          </p>
          <p>{event.reason ?? 'No reason recorded'}</p>
        </TimelineItem>
      ))}
    </Timeline>
  )
}

function AutomationPlaceholders() {
  const rows = [
    ['Endpoints', 'Not linked'],
    ['Destinations', 'Not configured'],
    ['Notifications', 'Not scheduled'],
    ['Policy', 'Not evaluated'],
    ['Jobs', 'No active tasks'],
  ]
  return (
    <div className="table-wrap">
      <table className="data-table compact">
        <thead>
          <tr>
            <th scope="col">Area</th>
            <th scope="col">State</th>
          </tr>
        </thead>
        <tbody>
          {rows.map(([area, state]) => (
            <tr key={area}>
              <td>{area}</td>
              <td>{state}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function SystemPage({ session }: { session: ApiSession }) {
  return (
    <section className="workspace" aria-labelledby="system-heading">
      <div className="workspace-header">
        <div>
          <p className="eyebrow">Runtime</p>
          <h1 id="system-heading">System</h1>
        </div>
        <span className="status-pill">Local</span>
      </div>

      <dl className="system-list">
        <div>
          <dt>API</dt>
          <dd>/actuator/health</dd>
        </div>
        <div>
          <dt>OpenAPI</dt>
          <dd>/v3/api-docs</dd>
        </div>
        <div>
          <dt>Role</dt>
          <dd>{roleLabel(session.role)}</dd>
        </div>
      </dl>
    </section>
  )
}

function RequirePermission({
  session,
  permission,
  children,
}: {
  session: ApiSession
  permission: Permission
  children: React.ReactNode
}) {
  if (!hasPermission(session.role, permission)) {
    return <PermissionDenied requiredPermission={permission} />
  }
  return children
}

function PermissionDenied({
  requiredPermission,
}: {
  requiredPermission: Permission
}) {
  return (
    <section className="state-panel" aria-labelledby="permission-heading">
      <h1 id="permission-heading">Permission denied</h1>
      <p>{requiredPermission} is required for this area.</p>
    </section>
  )
}

function LoadingState({ label }: { label: string }) {
  return (
    <div className="state-panel" role="status">
      <div className="loading-bar" aria-hidden="true" />
      <p>{label}</p>
    </div>
  )
}

function ErrorState({ error }: { error: unknown }) {
  if (error instanceof ApiError) {
    const title =
      error.status === 401
        ? 'Authentication required'
        : error.status === 403
          ? 'Permission denied'
          : 'Request failed'
    return (
      <section className="state-panel error-state" aria-labelledby="error-heading">
        <h2 id="error-heading">{title}</h2>
        <p>{error.message}</p>
        {error.body?.correlationId ? (
          <p className="mono">Correlation {error.body.correlationId}</p>
        ) : null}
      </section>
    )
  }

  return (
    <section className="state-panel error-state" aria-labelledby="error-heading">
      <h2 id="error-heading">Request failed</h2>
      <p>Unexpected frontend error.</p>
    </section>
  )
}

function EmptyState({ title }: { title: string }) {
  return (
    <section className="state-panel empty-state" aria-label={title}>
      <h2>{title}</h2>
    </section>
  )
}

function NotFoundPage() {
  return (
    <section className="state-panel" aria-labelledby="not-found-heading">
      <h1 id="not-found-heading">Not found</h1>
    </section>
  )
}

function SummaryItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  )
}

function DetailItem({
  label,
  value,
  monospace,
}: {
  label: string
  value: string
  monospace?: boolean
}) {
  return (
    <div>
      <span>{label}</span>
      <strong className={monospace ? 'mono' : undefined}>{value}</strong>
    </div>
  )
}

function Timeline({ children }: { children: React.ReactNode }) {
  return <ol className="timeline">{children}</ol>
}

function TimelineItem({
  title,
  meta,
  children,
}: {
  title: string
  meta: string
  children: React.ReactNode
}) {
  return (
    <li>
      <div>
        <strong>{title}</strong>
        <span>{meta}</span>
      </div>
      {children}
    </li>
  )
}

function StatusBadge({ status }: { status: CertificateStatus }) {
  return <span className={`status-badge ${status.toLowerCase()}`}>{status}</span>
}

function initialInventoryQuery(subject: string): InventoryQueryState {
  return {
    subject,
    owner: '',
    status: '',
    tag: '',
    metadataKey: '',
    metadataValue: '',
    sort: 'expiresAt,asc',
    page: 0,
    size: 25,
  }
}

function inventoryFilters(query: InventoryQueryState) {
  const filters: string[] = []
  if (query.subject.trim()) {
    filters.push(`subject:${query.subject.trim()}`)
  }
  if (query.status) {
    filters.push(`status:${query.status}`)
  }
  if (query.owner.trim()) {
    filters.push(`owner:${query.owner.trim()}`)
  }
  if (query.tag.trim()) {
    filters.push(`tag:${query.tag.trim()}`)
  }
  if (query.metadataKey.trim() && query.metadataValue.trim()) {
    filters.push(`metadata.${query.metadataKey.trim()}:${query.metadataValue.trim()}`)
  }
  return filters
}

function hasPermission(role: RoleKey, permission: Permission) {
  return rolePermissions[role].includes(permission)
}

function roleLabel(role: RoleKey) {
  return role
    .toLowerCase()
    .split('_')
    .map((part) => part[0].toUpperCase() + part.slice(1))
    .join(' ')
}

function principalLabel(session: ApiSession) {
  if (session.authMode === 'basic' && session.email) {
    return session.email
  }
  if (session.authMode === 'bearer' && session.token) {
    return 'Service token'
  }
  return 'Unauthenticated'
}

function certificateLabel(certificate: CertificateSummaryResponse) {
  return (
    certificate.commonName ??
    certificate.subjectAlternativeNames[0] ??
    certificate.subject
  )
}

function formatDate(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function truncateMiddle(value: string, maxLength: number) {
  if (value.length <= maxLength) {
    return value
  }
  const keep = Math.max(Math.floor((maxLength - 3) / 2), 4)
  return `${value.slice(0, keep)}...${value.slice(value.length - keep)}`
}

function tagList(tags: string[]) {
  return tags.length === 0 ? 'None' : tags.join(', ')
}

function metadataPairs(metadata: Record<string, string>) {
  const pairs = Object.entries(metadata)
  return pairs.length === 0
    ? 'None'
    : pairs.map(([key, value]) => `${key}=${value}`).join(', ')
}

function loadColumns(key: string) {
  try {
    const raw = window.localStorage.getItem(key)
    if (!raw) {
      return defaultColumns
    }
    const parsed = JSON.parse(raw) as string[]
    const valid = parsed.filter((column) =>
      inventoryColumns.some((item) => item.key === column),
    )
    return valid.length > 0 ? valid : defaultColumns
  } catch {
    return defaultColumns
  }
}

function persistColumns(key: string, columns: string[]) {
  try {
    window.localStorage.setItem(key, JSON.stringify(columns))
  } catch {
    return
  }
}

function loadSession(key: string, initialSession?: Partial<ApiSession>) {
  const stored = readStoredSession(key)
  return {
    ...defaultSession,
    ...stored,
    password: '',
    token: '',
    ...initialSession,
  }
}

function readStoredSession(key: string) {
  try {
    const raw = window.localStorage.getItem(key)
    return raw ? (JSON.parse(raw) as Partial<ApiSession>) : {}
  } catch {
    return {}
  }
}

function persistSession(key: string, session: ApiSession) {
  const safeProfile = {
    role: session.role,
    authMode: session.authMode,
    email: session.email,
    tenantId: session.tenantId,
  }
  try {
    window.localStorage.setItem(key, JSON.stringify(safeProfile))
  } catch {
    return
  }
}

function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}
