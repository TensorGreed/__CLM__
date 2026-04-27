export type Permission =
  | 'TENANT_READ'
  | 'TENANT_MANAGE'
  | 'ORGANIZATION_READ'
  | 'ORGANIZATION_MANAGE'
  | 'ROLE_READ'
  | 'ROLE_MANAGE'
  | 'CERTIFICATE_READ'
  | 'CERTIFICATE_IMPORT'
  | 'CERTIFICATE_MANAGE'
  | 'SERVICE_ACCOUNT_READ'
  | 'SERVICE_ACCOUNT_MANAGE'
  | 'AUDIT_READ'
  | 'SENSITIVE_ACTION_EXECUTE'
  | 'TASK_READ'
  | 'TASK_MANAGE'

export type RoleKey =
  | 'NO_ACCESS'
  | 'ADMIN'
  | 'OPERATOR'
  | 'REQUESTER'
  | 'APPROVER'
  | 'AUDITOR'
  | 'READ_ONLY'
  | 'SERVICE_ACCOUNT'

export type AuthMode = 'none' | 'basic' | 'bearer'

export type CertificateStatus = 'ACTIVE' | 'EXPIRED' | 'REVOKED'

export interface ApiSession {
  role: RoleKey
  authMode: AuthMode
  email: string
  password: string
  token: string
  tenantId: string
}

export interface ApiErrorBody {
  code?: string
  message?: string
  remediation?: string
  correlationId?: string
}

export class ApiError extends Error {
  readonly status: number
  readonly body?: ApiErrorBody

  constructor(status: number, message: string, body?: ApiErrorBody) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface TenantResponse {
  id: string
  slug: string
  name: string
  status: string
  defaultTenant: boolean
  createdAt?: string
  updatedAt?: string
}

export interface TenantCreateRequest {
  slug: string
  name: string
}

export interface TenantUpdateRequest {
  name: string
  status: string
}

export interface OrganizationResponse {
  id: string
  tenantId: string
  slug: string
  name: string
  status: string
  createdAt?: string
  updatedAt?: string
}

export interface OrganizationCreateRequest {
  slug: string
  name: string
}

export interface OrganizationUpdateRequest {
  name: string
  status: string
}

export interface RoleResponse {
  key: RoleKey
  permissions: Permission[]
}

export interface PermissionResponse {
  key: Permission
}

export interface ServiceAccountResponse {
  id: string
  tenantId: string
  name: string
  status: string
}

export interface ServiceAccountCreateRequest {
  tenantId: string
  name: string
}

export interface ApiTokenResponse {
  id: string
  serviceAccountId: string
  tokenPrefix: string
  status: string
  scopes: Permission[]
  expiresAt: string | null
}

export interface ApiTokenCreateRequest {
  scopes: Permission[]
  expiresAt: string | null
}

export interface ApiTokenSecretResponse {
  id: string
  serviceAccountId: string
  tokenPrefix: string
  token: string
  scopes: Permission[]
  expiresAt: string | null
}

export interface SearchResultResponse {
  type: 'certificate' | string
  id: string
  tenantId: string | null
  title: string
  subtitle: string
  status: string
  href: string
  matchedFields: string[]
}

export interface SearchResponse {
  query: string
  limit: number
  results: SearchResultResponse[]
}

export interface CertificateSummaryResponse {
  id: string
  tenantId: string
  owner: string | null
  orphaned: boolean
  status: CertificateStatus
  commonName: string | null
  subject: string
  issuer: string
  serialNumber: string
  validFrom: string
  validTo: string
  sha256Fingerprint: string
  subjectAlternativeNames: string[]
  tags: string[]
}

export interface CertificateVersionResponse {
  id: string
  versionNumber: number
  source: string
  subject: string
  issuer: string
  serialNumber: string
  validFrom: string
  validTo: string
  sha256Fingerprint: string
  sha1Fingerprint: string
  publicKeyAlgorithm: string
  signatureAlgorithm: string
  subjectAlternativeNames: string[]
  chainLength: number
  selfSigned: boolean
  createdAt: string
}

export interface CertificateChainEntryResponse {
  id: string
  position: number
  subject: string
  issuer: string
  serialNumber: string
  validFrom: string
  validTo: string
  sha256Fingerprint: string
  sha1Fingerprint: string
  selfSigned: boolean
}

export interface CertificateSourceObservationResponse {
  id: string
  certificateVersionId: string
  sourceType: string
  sourceKey: string
  observedResourceKey: string
  sourceName: string | null
  sha256Fingerprint: string
  metadata: Record<string, string>
  firstSeenAt: string
  lastSeenAt: string
  observationCount: number
}

export interface CertificateMetadataValueResponse {
  type: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'INSTANT'
  value: string
}

export interface CertificateStatusHistoryResponse {
  id: string
  fromStatus: CertificateStatus | null
  toStatus: CertificateStatus
  reason: string | null
  changedBy: string | null
  changedAt: string
}

export interface CertificateAuditEventResponse {
  id: string
  occurredAt: string
  actorType: string
  actorId: string | null
  action: string
  decision: string
  status: string
  reason: string | null
  correlationId: string | null
}

export interface CertificateDetailResponse
  extends CertificateSummaryResponse {
  sha1Fingerprint: string
  currentVersion: CertificateVersionResponse
  versions: CertificateVersionResponse[]
  chain: CertificateChainEntryResponse[]
  sourceObservations: CertificateSourceObservationResponse[]
  metadata: Record<string, CertificateMetadataValueResponse>
  statusHistory: CertificateStatusHistoryResponse[]
  auditTimeline: CertificateAuditEventResponse[]
}

export interface CertificateListQuery {
  tenantId?: string
  page: number
  size: number
  filters: string[]
  sort: string
}

export interface ApiClient {
  listTenants(session: ApiSession, signal?: AbortSignal): Promise<TenantResponse[]>
  createTenant(
    request: TenantCreateRequest,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<TenantResponse>
  updateTenant(
    tenantId: string,
    request: TenantUpdateRequest,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<TenantResponse>
  listOrganizations(
    tenantId: string,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<OrganizationResponse[]>
  createOrganization(
    tenantId: string,
    request: OrganizationCreateRequest,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<OrganizationResponse>
  updateOrganization(
    tenantId: string,
    organizationId: string,
    request: OrganizationUpdateRequest,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<OrganizationResponse>
  listRoles(session: ApiSession, signal?: AbortSignal): Promise<RoleResponse[]>
  listPermissions(
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<PermissionResponse[]>
  listServiceAccounts(
    tenantId: string | undefined,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<ServiceAccountResponse[]>
  createServiceAccount(
    request: ServiceAccountCreateRequest,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<ServiceAccountResponse>
  listApiTokens(
    serviceAccountId: string,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<ApiTokenResponse[]>
  createApiToken(
    serviceAccountId: string,
    request: ApiTokenCreateRequest,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<ApiTokenSecretResponse>
  rotateApiToken(
    tokenId: string,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<ApiTokenSecretResponse>
  revokeApiToken(
    tokenId: string,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<ApiTokenResponse>
  listCertificates(
    query: CertificateListQuery,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<PageResponse<CertificateSummaryResponse>>
  getCertificate(
    certificateId: string,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<CertificateDetailResponse>
  globalSearch(
    query: string,
    tenantId: string | undefined,
    session: ApiSession,
    signal?: AbortSignal,
  ): Promise<SearchResponse>
}

const apiBase = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')

export function createHttpApiClient(): ApiClient {
  return {
    listTenants: (session, signal) =>
      requestJson<TenantResponse[]>('/api/v1/tenants', session, { signal }),
    createTenant: (request, session, signal) =>
      requestJson<TenantResponse>('/api/v1/tenants', session, jsonInit('POST', request, signal)),
    updateTenant: (tenantId, request, session, signal) =>
      requestJson<TenantResponse>(
        `/api/v1/tenants/${tenantId}`,
        session,
        jsonInit('PUT', request, signal),
      ),
    listOrganizations: (tenantId, session, signal) =>
      requestJson<OrganizationResponse[]>(
        `/api/v1/tenants/${tenantId}/organizations`,
        session,
        { signal },
      ),
    createOrganization: (tenantId, request, session, signal) =>
      requestJson<OrganizationResponse>(
        `/api/v1/tenants/${tenantId}/organizations`,
        session,
        jsonInit('POST', request, signal),
      ),
    updateOrganization: (tenantId, organizationId, request, session, signal) =>
      requestJson<OrganizationResponse>(
        `/api/v1/tenants/${tenantId}/organizations/${organizationId}`,
        session,
        jsonInit('PUT', request, signal),
      ),
    listRoles: (session, signal) =>
      requestJson<RoleResponse[]>('/api/v1/roles', session, { signal }),
    listPermissions: (session, signal) =>
      requestJson<PermissionResponse[]>('/api/v1/permissions', session, { signal }),
    listServiceAccounts: (tenantId, session, signal) =>
      requestJson<ServiceAccountResponse[]>(
        serviceAccountListPath(tenantId),
        session,
        { signal },
      ),
    createServiceAccount: (request, session, signal) =>
      requestJson<ServiceAccountResponse>(
        '/api/v1/service-accounts',
        session,
        jsonInit('POST', request, signal),
      ),
    listApiTokens: (serviceAccountId, session, signal) =>
      requestJson<ApiTokenResponse[]>(
        `/api/v1/service-accounts/${serviceAccountId}/tokens`,
        session,
        { signal },
      ),
    createApiToken: (serviceAccountId, request, session, signal) =>
      requestJson<ApiTokenSecretResponse>(
        `/api/v1/service-accounts/${serviceAccountId}/tokens`,
        session,
        jsonInit('POST', request, signal),
      ),
    rotateApiToken: (tokenId, session, signal) =>
      requestJson<ApiTokenSecretResponse>(
        `/api/v1/api-tokens/${tokenId}/rotate`,
        session,
        jsonInit('POST', undefined, signal),
      ),
    revokeApiToken: (tokenId, session, signal) =>
      requestJson<ApiTokenResponse>(
        `/api/v1/api-tokens/${tokenId}/revoke`,
        session,
        jsonInit('POST', undefined, signal),
      ),
    listCertificates: (query, session, signal) =>
      requestJson<PageResponse<CertificateSummaryResponse>>(
        certificateListPath(query),
        session,
        { signal },
      ),
    getCertificate: (certificateId, session, signal) =>
      requestJson<CertificateDetailResponse>(
        `/api/v1/certificates/${certificateId}`,
        session,
        { signal },
      ),
    globalSearch: (query, tenantId, session, signal) =>
      requestJson<SearchResponse>(globalSearchPath(query, tenantId), session, {
        signal,
      }),
  }
}

function certificateListPath(query: CertificateListQuery) {
  const params = new URLSearchParams()
  params.set('page', String(query.page))
  params.set('size', String(query.size))
  if (query.tenantId) {
    params.set('tenantId', query.tenantId)
  }
  for (const filter of query.filters) {
    params.append('filter', filter)
  }
  if (query.sort) {
    params.set('sort', query.sort)
  }
  return `/api/v1/certificates?${params.toString()}`
}

function serviceAccountListPath(tenantId?: string) {
  if (!tenantId) {
    return '/api/v1/service-accounts'
  }
  const params = new URLSearchParams()
  params.set('tenantId', tenantId)
  return `/api/v1/service-accounts?${params.toString()}`
}

function globalSearchPath(query: string, tenantId?: string) {
  const params = new URLSearchParams()
  params.set('q', query)
  params.set('limit', '10')
  if (tenantId) {
    params.set('tenantId', tenantId)
  }
  return `/api/v1/search?${params.toString()}`
}

function jsonInit(
  method: 'POST' | 'PUT',
  body: unknown,
  signal?: AbortSignal,
): RequestInit {
  return {
    method,
    signal,
    headers: {
      'Content-Type': 'application/json',
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  }
}

async function requestJson<T>(
  path: string,
  session: ApiSession,
  init: RequestInit = {},
): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')
  const authorization = authorizationHeader(session)
  if (authorization) {
    headers.set('Authorization', authorization)
  }

  const response = await fetch(`${apiBase}${path}`, {
    ...init,
    headers,
    credentials: 'include',
  })

  if (!response.ok) {
    const body = await safeJson<ApiErrorBody>(response)
    throw new ApiError(
      response.status,
      body?.message ?? `API request failed with HTTP ${response.status}.`,
      body,
    )
  }

  return response.json() as Promise<T>
}

function authorizationHeader(session: ApiSession) {
  if (session.authMode === 'basic' && session.email && session.password) {
    return `Basic ${btoa(`${session.email}:${session.password}`)}`
  }
  if (session.authMode === 'bearer' && session.token) {
    return `Bearer ${session.token}`
  }
  return null
}

async function safeJson<T>(response: Response) {
  try {
    return (await response.json()) as T
  } catch {
    return undefined
  }
}
