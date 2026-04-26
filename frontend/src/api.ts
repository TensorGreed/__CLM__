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
}

const apiBase = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')

export function createHttpApiClient(): ApiClient {
  return {
    listTenants: (session, signal) =>
      requestJson<TenantResponse[]>('/api/v1/tenants', session, { signal }),
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
