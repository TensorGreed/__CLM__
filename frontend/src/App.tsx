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
  type ApiTokenResponse,
  type ApiTokenSecretResponse,
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
  type OrganizationResponse,
  type PageResponse,
  type Permission,
  type PermissionResponse,
  type RoleResponse,
  type RoleKey,
  type SearchResponse,
  type ServiceAccountResponse,
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
          path="/search"
          element={
            <RequirePermission session={session} permission="CERTIFICATE_READ">
              <GlobalSearchPage
                key={`search-${location.search}`}
                apiClient={apiClient}
                session={session}
              />
            </RequirePermission>
          }
        />
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
        <Route
          path="/settings"
          element={
            <RequireAnyPermission
              session={session}
              permissions={[
                'TENANT_READ',
                'ORGANIZATION_READ',
                'ROLE_READ',
                'SERVICE_ACCOUNT_READ',
              ]}
            >
              <SettingsPage apiClient={apiClient} session={session} />
            </RequireAnyPermission>
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
    navigate(value ? `/search?q=${encodeURIComponent(value)}` : '/search')
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
          {hasAnyPermission(session.role, [
            'TENANT_READ',
            'ORGANIZATION_READ',
            'ROLE_READ',
            'SERVICE_ACCOUNT_READ',
          ]) ? (
            <NavLink
              to="/settings"
              className={({ isActive }) =>
                isActive ? 'nav-link active' : 'nav-link'
              }
            >
              <span aria-hidden="true">SE</span>
              Settings
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
              Global search
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

function GlobalSearchPage({
  apiClient,
  session,
}: {
  apiClient: ApiClient
  session: ApiSession
}) {
  const [searchParams, setSearchParams] = useSearchParams()
  const query = searchParams.get('q') ?? ''
  const [draft, setDraft] = useState(query)
  const [state, setState] = useState<LoadState<SearchResponse>>(() =>
    query.trim() ? { status: 'loading' } : { status: 'idle' },
  )

  useEffect(() => {
    const normalizedQuery = query.trim()
    if (!normalizedQuery) {
      return
    }
    const controller = new AbortController()
    apiClient
      .globalSearch(
        normalizedQuery,
        session.tenantId || undefined,
        session,
        controller.signal,
      )
      .then((data) => setState({ status: 'success', data }))
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, query, session])

  function submitSearch(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const normalizedQuery = draft.trim()
    setSearchParams(normalizedQuery ? { q: normalizedQuery } : {})
  }

  return (
    <section className="workspace" aria-labelledby="search-heading">
      <div className="workspace-header">
        <div>
          <p className="eyebrow">Search</p>
          <h1 id="search-heading">Global Search</h1>
        </div>
      </div>

      <form className="search-panel" role="search" onSubmit={submitSearch}>
        <label>
          Search term
          <input
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            autoFocus
          />
        </label>
        <button type="submit">Search</button>
      </form>

      {state.status === 'idle' ? <EmptyState title="Enter a search term" /> : null}
      {state.status === 'loading' ? <LoadingState label="Searching" /> : null}
      {state.status === 'error' ? <ErrorState error={state.error} /> : null}
      {state.status === 'success' && state.data.results.length === 0 ? (
        <EmptyState title="No results found" />
      ) : null}
      {state.status === 'success' && state.data.results.length > 0 ? (
        <div className="result-list">
          {state.data.results.map((result) => (
            <Link className="result-row" to={result.href} key={`${result.type}-${result.id}`}>
              <span className="type-pill">{result.type}</span>
              <strong>{result.title}</strong>
              <span>{truncateMiddle(result.subtitle, 110)}</span>
              <small>
                {result.status} | {result.matchedFields.join(', ')}
              </small>
            </Link>
          ))}
        </div>
      ) : null}
    </section>
  )
}

const settingsTabs = [
  { key: 'tenants', label: 'Tenants', permission: 'TENANT_READ' },
  { key: 'organizations', label: 'Organizations', permission: 'ORGANIZATION_READ' },
  { key: 'users-groups', label: 'Users & Groups', permission: 'ROLE_READ' },
  { key: 'roles', label: 'Roles', permission: 'ROLE_READ' },
  { key: 'service-accounts', label: 'Service Accounts', permission: 'SERVICE_ACCOUNT_READ' },
] as const

type SettingsTabKey = (typeof settingsTabs)[number]['key']

function SettingsPage({
  apiClient,
  session,
}: {
  apiClient: ApiClient
  session: ApiSession
}) {
  const visibleTabs = settingsTabs.filter((tab) =>
    hasPermission(session.role, tab.permission),
  )
  const [activeTab, setActiveTab] = useState<SettingsTabKey>(
    visibleTabs[0]?.key ?? 'tenants',
  )
  const effectiveTab = visibleTabs.some((tab) => tab.key === activeTab)
    ? activeTab
    : visibleTabs[0]?.key

  if (!effectiveTab) {
    return <PermissionDenied requiredPermission="ROLE_READ" />
  }

  return (
    <section className="workspace" aria-labelledby="settings-heading">
      <div className="workspace-header">
        <div>
          <p className="eyebrow">Administration</p>
          <h1 id="settings-heading">Settings</h1>
        </div>
      </div>

      <div className="tabs" role="tablist" aria-label="Settings sections">
        {visibleTabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            role="tab"
            aria-selected={effectiveTab === tab.key}
            className={effectiveTab === tab.key ? 'tab active' : 'tab'}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="tab-panel">
        {effectiveTab === 'tenants' ? (
          <TenantSettings apiClient={apiClient} session={session} />
        ) : null}
        {effectiveTab === 'organizations' ? (
          <OrganizationSettings apiClient={apiClient} session={session} />
        ) : null}
        {effectiveTab === 'users-groups' ? (
          <UsersGroupsSettings />
        ) : null}
        {effectiveTab === 'roles' ? (
          <RolesSettings apiClient={apiClient} session={session} />
        ) : null}
        {effectiveTab === 'service-accounts' ? (
          <ServiceAccountSettings apiClient={apiClient} session={session} />
        ) : null}
      </div>
    </section>
  )
}

function TenantSettings({
  apiClient,
  session,
}: {
  apiClient: ApiClient
  session: ApiSession
}) {
  const canManage = hasPermission(session.role, 'TENANT_MANAGE')
  const [refreshKey, setRefreshKey] = useState(0)
  const [state, setState] = useState<LoadState<TenantResponse[]>>({
    status: 'loading',
  })
  const [slug, setSlug] = useState('')
  const [name, setName] = useState('')
  const [message, setMessage] = useState('')

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
  }, [apiClient, session, refreshKey])

  async function createTenant(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')
    try {
      await apiClient.createTenant({ slug, name }, session)
      setSlug('')
      setName('')
      setMessage('Tenant created')
      setRefreshKey((current) => current + 1)
    } catch (error) {
      setState({ status: 'error', error })
    }
  }

  if (state.status === 'loading' || state.status === 'idle') {
    return <LoadingState label="Loading tenants" />
  }
  if (state.status === 'error') {
    return <ErrorState error={state.error} />
  }
  if (state.status !== 'success') {
    return <LoadingState label="Loading tenants" />
  }
  const tenants = state.data

  return (
    <div className="settings-grid">
      {canManage ? (
        <form className="settings-form" onSubmit={createTenant}>
          <h2>Create tenant</h2>
          <label>
            Slug
            <input value={slug} onChange={(event) => setSlug(event.target.value)} />
          </label>
          <label>
            Name
            <input value={name} onChange={(event) => setName(event.target.value)} />
          </label>
          <button type="submit">Create</button>
          {message ? <p className="form-message">{message}</p> : null}
        </form>
      ) : null}

      <div className="table-wrap">
        <table className="data-table compact">
          <thead>
            <tr>
              <th scope="col">Tenant</th>
              <th scope="col">Slug</th>
              <th scope="col">Status</th>
              <th scope="col">Default</th>
              {canManage ? <th scope="col">Update</th> : null}
            </tr>
          </thead>
          <tbody>
            {tenants.map((tenant) => (
              <TenantRow
                key={tenant.id}
                tenant={tenant}
                canManage={canManage}
                session={session}
                apiClient={apiClient}
                onUpdated={() => setRefreshKey((current) => current + 1)}
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function TenantRow({
  tenant,
  canManage,
  session,
  apiClient,
  onUpdated,
}: {
  tenant: TenantResponse
  canManage: boolean
  session: ApiSession
  apiClient: ApiClient
  onUpdated: () => void
}) {
  const [name, setName] = useState(tenant.name)
  const [status, setStatus] = useState(tenant.status)

  async function updateTenant() {
    await apiClient.updateTenant(tenant.id, { name, status }, session)
    onUpdated()
  }

  return (
    <tr>
      <td className="primary-cell">
        <strong>{tenant.name}</strong>
        <span>{tenant.id}</span>
      </td>
      <td>{tenant.slug}</td>
      <td>{tenant.status}</td>
      <td>{tenant.defaultTenant ? 'Yes' : 'No'}</td>
      {canManage ? (
        <td>
          <div className="inline-edit">
            <input
              aria-label={`Name for ${tenant.name}`}
              value={name}
              onChange={(event) => setName(event.target.value)}
            />
            <select
              aria-label={`Status for ${tenant.name}`}
              value={status}
              onChange={(event) => setStatus(event.target.value)}
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="DISABLED">DISABLED</option>
            </select>
            <button type="button" className="secondary-button" onClick={updateTenant}>
              Save
            </button>
          </div>
        </td>
      ) : null}
    </tr>
  )
}

function OrganizationSettings({
  apiClient,
  session,
}: {
  apiClient: ApiClient
  session: ApiSession
}) {
  const canManage = hasPermission(session.role, 'ORGANIZATION_MANAGE')
  const [selectedTenantId, setSelectedTenantId] = useState(session.tenantId)
  const [refreshKey, setRefreshKey] = useState(0)
  const [tenantState, setTenantState] = useState<LoadState<TenantResponse[]>>({
    status: 'loading',
  })
  const [organizationState, setOrganizationState] = useState<LoadState<OrganizationResponse[]>>({
    status: 'loading',
  })
  const [slug, setSlug] = useState('')
  const [name, setName] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    apiClient
      .listTenants(session, controller.signal)
      .then((data) => setTenantState({ status: 'success', data }))
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setTenantState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, session])

  const tenants = tenantState.status === 'success' ? tenantState.data : []
  const effectiveTenantId = selectedTenantId || tenants[0]?.id || ''

  useEffect(() => {
    if (!effectiveTenantId) {
      return
    }
    const controller = new AbortController()
    apiClient
      .listOrganizations(effectiveTenantId, session, controller.signal)
      .then((data) => setOrganizationState({ status: 'success', data }))
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setOrganizationState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, effectiveTenantId, session, refreshKey])

  async function createOrganization(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!effectiveTenantId) {
      return
    }
    try {
      await apiClient.createOrganization(effectiveTenantId, { slug, name }, session)
      setSlug('')
      setName('')
      setRefreshKey((current) => current + 1)
    } catch (error) {
      setOrganizationState({ status: 'error', error })
    }
  }

  if (tenantState.status === 'loading' || tenantState.status === 'idle') {
    return <LoadingState label="Loading tenants" />
  }
  if (tenantState.status === 'error') {
    return <ErrorState error={tenantState.error} />
  }

  return (
    <div className="settings-grid">
      <form className="settings-form" onSubmit={createOrganization}>
        <h2>Organizations</h2>
        <label>
          Tenant
          <select
            value={effectiveTenantId}
            onChange={(event) => setSelectedTenantId(event.target.value)}
          >
            {tenants.map((tenant) => (
              <option value={tenant.id} key={tenant.id}>
                {tenant.name}
              </option>
            ))}
          </select>
        </label>
        {canManage ? (
          <>
            <label>
              Slug
              <input value={slug} onChange={(event) => setSlug(event.target.value)} />
            </label>
            <label>
              Name
              <input value={name} onChange={(event) => setName(event.target.value)} />
            </label>
            <button type="submit">Create</button>
          </>
        ) : null}
      </form>

      {organizationState.status === 'loading' || organizationState.status === 'idle' ? (
        <LoadingState label="Loading organizations" />
      ) : null}
      {organizationState.status === 'error' ? (
        <ErrorState error={organizationState.error} />
      ) : null}
      {organizationState.status === 'success' ? (
        <div className="table-wrap">
          <table className="data-table compact">
            <thead>
              <tr>
                <th scope="col">Organization</th>
                <th scope="col">Slug</th>
                <th scope="col">Status</th>
                {canManage ? <th scope="col">Update</th> : null}
              </tr>
            </thead>
            <tbody>
              {organizationState.data.map((organization) => (
                <OrganizationRow
                  key={organization.id}
                  organization={organization}
                  canManage={canManage}
                  session={session}
                  apiClient={apiClient}
                  onUpdated={() => setRefreshKey((current) => current + 1)}
                />
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  )
}

function OrganizationRow({
  organization,
  canManage,
  session,
  apiClient,
  onUpdated,
}: {
  organization: OrganizationResponse
  canManage: boolean
  session: ApiSession
  apiClient: ApiClient
  onUpdated: () => void
}) {
  const [name, setName] = useState(organization.name)
  const [status, setStatus] = useState(organization.status)

  async function updateOrganization() {
    await apiClient.updateOrganization(
      organization.tenantId,
      organization.id,
      { name, status },
      session,
    )
    onUpdated()
  }

  return (
    <tr>
      <td className="primary-cell">
        <strong>{organization.name}</strong>
        <span>{organization.id}</span>
      </td>
      <td>{organization.slug}</td>
      <td>{organization.status}</td>
      {canManage ? (
        <td>
          <div className="inline-edit">
            <input
              aria-label={`Name for ${organization.name}`}
              value={name}
              onChange={(event) => setName(event.target.value)}
            />
            <select
              aria-label={`Status for ${organization.name}`}
              value={status}
              onChange={(event) => setStatus(event.target.value)}
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="DISABLED">DISABLED</option>
            </select>
            <button
              type="button"
              className="secondary-button"
              onClick={updateOrganization}
            >
              Save
            </button>
          </div>
        </td>
      ) : null}
    </tr>
  )
}

function UsersGroupsSettings() {
  const mappings = [
    ['clm-admins', 'ADMIN'],
    ['clm-operators', 'OPERATOR'],
    ['clm-auditors', 'AUDITOR'],
  ]
  return (
    <div className="settings-grid">
      <div className="table-wrap">
        <table className="data-table compact">
          <thead>
            <tr>
              <th scope="col">Identity source</th>
              <th scope="col">Group</th>
              <th scope="col">Mapped role</th>
            </tr>
          </thead>
          <tbody>
            {mappings.map(([group, role]) => (
              <tr key={group}>
                <td>OIDC</td>
                <td>{group}</td>
                <td>{role}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <dl className="system-list">
        <div>
          <dt>Local users</dt>
          <dd>Bootstrap admin</dd>
        </div>
        <div>
          <dt>Group claim</dt>
          <dd>groups</dd>
        </div>
        <div>
          <dt>Provisioning</dt>
          <dd>OIDC group mapping</dd>
        </div>
      </dl>
    </div>
  )
}

function RolesSettings({
  apiClient,
  session,
}: {
  apiClient: ApiClient
  session: ApiSession
}) {
  const [state, setState] = useState<LoadState<{
    roles: RoleResponse[]
    permissions: PermissionResponse[]
  }>>({ status: 'loading' })

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      apiClient.listRoles(session, controller.signal),
      apiClient.listPermissions(session, controller.signal),
    ])
      .then(([roles, permissions]) => setState({ status: 'success', data: { roles, permissions } }))
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, session])

  if (state.status === 'loading' || state.status === 'idle') {
    return <LoadingState label="Loading roles" />
  }
  if (state.status === 'error') {
    return <ErrorState error={state.error} />
  }
  if (state.status !== 'success') {
    return <LoadingState label="Loading roles" />
  }
  const { roles, permissions } = state.data

  return (
    <div className="settings-grid">
      <div className="table-wrap">
        <table className="data-table compact permission-matrix">
          <thead>
            <tr>
              <th scope="col">Role</th>
              <th scope="col">Permissions</th>
            </tr>
          </thead>
          <tbody>
            {roles.map((role) => (
              <tr key={role.key}>
                <td>{roleLabel(role.key)}</td>
                <td>{role.permissions.join(', ')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <dl className="system-list">
        <div>
          <dt>Total permissions</dt>
          <dd>{permissions.length}</dd>
        </div>
        <div>
          <dt>Role source</dt>
          <dd>Built-in matrix</dd>
        </div>
      </dl>
    </div>
  )
}

function ServiceAccountSettings({
  apiClient,
  session,
}: {
  apiClient: ApiClient
  session: ApiSession
}) {
  const canManage = hasPermission(session.role, 'SERVICE_ACCOUNT_MANAGE')
  const [refreshKey, setRefreshKey] = useState(0)
  const [state, setState] = useState<LoadState<ServiceAccountResponse[]>>({
    status: 'loading',
  })
  const [selectedAccountId, setSelectedAccountId] = useState('')
  const [name, setName] = useState('')
  const [lastSecret, setLastSecret] = useState<ApiTokenSecretResponse | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    apiClient
      .listServiceAccounts(session.tenantId || undefined, session, controller.signal)
      .then((data) => setState({ status: 'success', data }))
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, session, refreshKey])

  const accounts = state.status === 'success' ? state.data : []
  const effectiveAccountId = selectedAccountId || accounts[0]?.id || ''

  async function createServiceAccount(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!session.tenantId) {
      return
    }
    try {
      await apiClient.createServiceAccount({ tenantId: session.tenantId, name }, session)
      setName('')
      setRefreshKey((current) => current + 1)
    } catch (error) {
      setState({ status: 'error', error })
    }
  }

  if (state.status === 'loading' || state.status === 'idle') {
    return <LoadingState label="Loading service accounts" />
  }
  if (state.status === 'error') {
    return <ErrorState error={state.error} />
  }
  if (state.status !== 'success') {
    return <LoadingState label="Loading service accounts" />
  }

  return (
    <div className="settings-grid">
      <form className="settings-form" onSubmit={createServiceAccount}>
        <h2>Service accounts</h2>
        <label>
          Account
          <select
            value={effectiveAccountId}
            onChange={(event) => setSelectedAccountId(event.target.value)}
          >
            {accounts.map((account) => (
              <option value={account.id} key={account.id}>
                {account.name}
              </option>
            ))}
          </select>
        </label>
        {canManage ? (
          <>
            <label>
              New account name
              <input value={name} onChange={(event) => setName(event.target.value)} />
            </label>
            <button type="submit" disabled={!session.tenantId}>
              Create
            </button>
          </>
        ) : null}
      </form>

      {accounts.length === 0 ? <EmptyState title="No service accounts found" /> : null}
      {accounts.length > 0 ? (
        <div className="table-wrap">
          <table className="data-table compact">
            <thead>
              <tr>
                <th scope="col">Name</th>
                <th scope="col">Tenant</th>
                <th scope="col">Status</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((account) => (
                <tr key={account.id}>
                  <td className="primary-cell">
                    <strong>{account.name}</strong>
                    <span>{account.id}</span>
                  </td>
                  <td>{account.tenantId}</td>
                  <td>{account.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      {effectiveAccountId ? (
        <TokenSettings
          apiClient={apiClient}
          session={session}
          serviceAccountId={effectiveAccountId}
          canManage={canManage}
          lastSecret={lastSecret}
          onSecret={setLastSecret}
        />
      ) : null}
    </div>
  )
}

function TokenSettings({
  apiClient,
  session,
  serviceAccountId,
  canManage,
  lastSecret,
  onSecret,
}: {
  apiClient: ApiClient
  session: ApiSession
  serviceAccountId: string
  canManage: boolean
  lastSecret: ApiTokenSecretResponse | null
  onSecret: (secret: ApiTokenSecretResponse | null) => void
}) {
  const [refreshKey, setRefreshKey] = useState(0)
  const [state, setState] = useState<LoadState<ApiTokenResponse[]>>({
    status: 'loading',
  })
  const [scope, setScope] = useState<Permission>('CERTIFICATE_READ')

  useEffect(() => {
    const controller = new AbortController()
    apiClient
      .listApiTokens(serviceAccountId, session, controller.signal)
      .then((data) => setState({ status: 'success', data }))
      .catch((error: unknown) => {
        if (!isAbortError(error)) {
          setState({ status: 'error', error })
        }
      })
    return () => controller.abort()
  }, [apiClient, serviceAccountId, session, refreshKey])

  async function createToken() {
    const secret = await apiClient.createApiToken(
      serviceAccountId,
      { scopes: [scope], expiresAt: null },
      session,
    )
    onSecret(secret)
    setRefreshKey((current) => current + 1)
  }

  async function rotateToken(tokenId: string) {
    const secret = await apiClient.rotateApiToken(tokenId, session)
    onSecret(secret)
    setRefreshKey((current) => current + 1)
  }

  async function revokeToken(tokenId: string) {
    await apiClient.revokeApiToken(tokenId, session)
    onSecret(null)
    setRefreshKey((current) => current + 1)
  }

  if (state.status === 'loading' || state.status === 'idle') {
    return <LoadingState label="Loading API tokens" />
  }
  if (state.status === 'error') {
    return <ErrorState error={state.error} />
  }
  if (state.status !== 'success') {
    return <LoadingState label="Loading API tokens" />
  }
  const tokens = state.data

  return (
    <div className="token-panel">
      {lastSecret ? (
        <div className="secret-panel">
          <strong>Token secret</strong>
          <code>{lastSecret.token}</code>
        </div>
      ) : null}
      {canManage ? (
        <div className="inline-edit">
          <label>
            Scope
            <select
              value={scope}
              onChange={(event) => setScope(event.target.value as Permission)}
            >
              {rolePermissions.ADMIN.filter((permission) => permission !== 'SENSITIVE_ACTION_EXECUTE').map((permission) => (
                <option value={permission} key={permission}>
                  {permission}
                </option>
              ))}
            </select>
          </label>
          <button type="button" onClick={createToken}>
            Create token
          </button>
        </div>
      ) : null}
      {tokens.length === 0 ? <EmptyState title="No API tokens found" /> : null}
      {tokens.length > 0 ? (
        <div className="table-wrap">
          <table className="data-table compact">
            <thead>
              <tr>
                <th scope="col">Prefix</th>
                <th scope="col">Status</th>
                <th scope="col">Scopes</th>
                <th scope="col">Expires</th>
                {canManage ? <th scope="col">Actions</th> : null}
              </tr>
            </thead>
            <tbody>
              {tokens.map((token) => (
                <tr key={token.id}>
                  <td>{token.tokenPrefix}</td>
                  <td>{token.status}</td>
                  <td>{token.scopes.join(', ')}</td>
                  <td>{token.expiresAt ? formatDate(token.expiresAt) : 'Never'}</td>
                  {canManage ? (
                    <td>
                      <div className="inline-edit">
                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() => rotateToken(token.id)}
                        >
                          Rotate
                        </button>
                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() => revokeToken(token.id)}
                        >
                          Revoke
                        </button>
                      </div>
                    </td>
                  ) : null}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
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

function RequireAnyPermission({
  session,
  permissions,
  children,
}: {
  session: ApiSession
  permissions: Permission[]
  children: React.ReactNode
}) {
  if (!hasAnyPermission(session.role, permissions)) {
    return <PermissionDenied requiredPermission={permissions[0] ?? 'ROLE_READ'} />
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

function hasAnyPermission(role: RoleKey, permissions: Permission[]) {
  return permissions.some((permission) => hasPermission(role, permission))
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
