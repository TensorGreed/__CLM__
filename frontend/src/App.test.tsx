import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'
import { App } from './App'
import { ApiError, type ApiClient, type CertificateDetailResponse } from './api'

const certificateSummary = {
  id: 'cert-1',
  tenantId: 'tenant-1',
  owner: 'platform-team',
  orphaned: false,
  status: 'ACTIVE' as const,
  commonName: 'inventory.example.test',
  subject: 'CN=inventory.example.test,O=Example',
  issuer: 'CN=Example Root,O=Example',
  serialNumber: '01A2',
  validFrom: '2026-01-01T00:00:00Z',
  validTo: '2026-12-31T00:00:00Z',
  sha256Fingerprint: 'AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99',
  subjectAlternativeNames: ['DNS:inventory.example.test', 'DNS:www.example.test'],
  tags: ['prod', 'web'],
}

const certificateDetail: CertificateDetailResponse = {
  ...certificateSummary,
  sha1Fingerprint: '11:22:33:44:55',
  currentVersion: {
    id: 'version-1',
    versionNumber: 1,
    source: 'IMPORT',
    subject: certificateSummary.subject,
    issuer: certificateSummary.issuer,
    serialNumber: certificateSummary.serialNumber,
    validFrom: certificateSummary.validFrom,
    validTo: certificateSummary.validTo,
    sha256Fingerprint: certificateSummary.sha256Fingerprint,
    sha1Fingerprint: '11:22:33:44:55',
    publicKeyAlgorithm: 'RSA',
    signatureAlgorithm: 'SHA256withRSA',
    subjectAlternativeNames: certificateSummary.subjectAlternativeNames,
    chainLength: 2,
    selfSigned: false,
    createdAt: '2026-01-01T00:00:00Z',
  },
  versions: [
    {
      id: 'version-1',
      versionNumber: 1,
      source: 'IMPORT',
      subject: certificateSummary.subject,
      issuer: certificateSummary.issuer,
      serialNumber: certificateSummary.serialNumber,
      validFrom: certificateSummary.validFrom,
      validTo: certificateSummary.validTo,
      sha256Fingerprint: certificateSummary.sha256Fingerprint,
      sha1Fingerprint: '11:22:33:44:55',
      publicKeyAlgorithm: 'RSA',
      signatureAlgorithm: 'SHA256withRSA',
      subjectAlternativeNames: certificateSummary.subjectAlternativeNames,
      chainLength: 2,
      selfSigned: false,
      createdAt: '2026-01-01T00:00:00Z',
    },
  ],
  chain: [
    {
      id: 'chain-1',
      position: 1,
      subject: 'CN=Example Root,O=Example',
      issuer: 'CN=Example Root,O=Example',
      serialNumber: '10',
      validFrom: '2025-01-01T00:00:00Z',
      validTo: '2030-01-01T00:00:00Z',
      sha256Fingerprint: '44:55:66',
      sha1Fingerprint: '77:88:99',
      selfSigned: true,
    },
  ],
  sourceObservations: [
    {
      id: 'source-1',
      certificateVersionId: 'version-1',
      sourceType: 'aws-acm',
      sourceKey: 'account-123',
      observedResourceKey: 'arn:aws:acm:us-east-1:123:certificate/web',
      sourceName: 'AWS ACM',
      sha256Fingerprint: certificateSummary.sha256Fingerprint,
      metadata: { region: 'us-east-1' },
      firstSeenAt: '2026-04-01T00:00:00Z',
      lastSeenAt: '2026-04-26T00:00:00Z',
      observationCount: 2,
    },
  ],
  metadata: {
    environment: { type: 'STRING', value: 'prod' },
    criticality: { type: 'NUMBER', value: '1' },
  },
  statusHistory: [
    {
      id: 'status-1',
      fromStatus: null,
      toStatus: 'ACTIVE',
      reason: 'Certificate imported.',
      changedBy: 'user-1',
      changedAt: '2026-01-01T00:00:00Z',
    },
  ],
  auditTimeline: [
    {
      id: 'audit-1',
      occurredAt: '2026-01-01T00:00:00Z',
      actorType: 'USER',
      actorId: 'user-1',
      action: 'certificate.imported',
      decision: 'ALLOW',
      status: 'SUCCESS',
      reason: 'Certificate imported.',
      correlationId: 'corr-1',
    },
  ],
}

describe('App', () => {
  beforeEach(() => {
    window.localStorage.clear()
  })

  it('renders the role-aware shell and inventory table', async () => {
    const apiClient = createApiClient()

    renderApp('/certificates', apiClient)

    expect(
      await screen.findByRole('heading', { name: /certificate inventory/i }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('link', { name: /inventory.example.test/i }),
    ).toBeInTheDocument()
    expect(screen.getByLabelText(/tenant/i)).toHaveTextContent('Default Tenant')
    expect(screen.getByLabelText(/user menu/i)).toHaveTextContent('Operator')
  })

  it('applies inventory filters, sorting, pagination, and bulk selection', async () => {
    const user = userEvent.setup()
    const apiClient = createApiClient()

    renderApp('/certificates', apiClient)
    await screen.findByRole('link', { name: /inventory.example.test/i })

    const subjectInputs = screen.getAllByLabelText(/^Subject$/i)
    const statusControls = screen.getAllByLabelText(/^Status$/i)
    await user.type(subjectInputs[subjectInputs.length - 1]!, 'inventory')
    await user.selectOptions(statusControls[statusControls.length - 1]!, 'ACTIVE')
    await user.type(screen.getByLabelText(/^Tag$/i), 'prod')
    await user.selectOptions(screen.getByLabelText(/^Sort$/i), 'owner,asc')
    await user.click(screen.getByRole('button', { name: /apply/i }))

    await waitFor(() =>
      expect(apiClient.listCertificates).toHaveBeenLastCalledWith(
        expect.objectContaining({
          filters: expect.arrayContaining([
            'subject:inventory',
            'status:ACTIVE',
            'tag:prod',
          ]),
          sort: 'owner,asc',
        }),
        expect.anything(),
        expect.anything(),
      ),
    )

    await user.click(
      screen.getByLabelText(/select all certificates on this page/i),
    )
    expect(screen.getByText(/1 selected/i)).toBeInTheDocument()
  })

  it('renders certificate detail sections', async () => {
    const user = userEvent.setup()
    const apiClient = createApiClient()

    renderApp('/certificates/cert-1', apiClient)

    expect(
      await screen.findByRole('heading', { name: /inventory.example.test/i }),
    ).toBeInTheDocument()
    expect(screen.getByText(/SHA-256/i)).toBeInTheDocument()

    await user.click(screen.getByRole('tab', { name: /sources/i }))
    expect(screen.getByText(/aws-acm/i)).toBeInTheDocument()
    expect(screen.getByText(/region=us-east-1/i)).toBeInTheDocument()

    await user.click(screen.getByRole('tab', { name: /metadata/i }))
    expect(screen.getByText(/environment/i)).toBeInTheDocument()
    expect(screen.getByText(/prod/i)).toBeInTheDocument()

    await user.click(screen.getByRole('tab', { name: /status/i }))
    expect(screen.getByText(/Created to ACTIVE/i)).toBeInTheDocument()

    await user.click(screen.getByRole('tab', { name: /audit/i }))
    expect(screen.getAllByText(/certificate.imported/i)[0]).toBeInTheDocument()
  })

  it('shows route guard permission denied state before calling inventory APIs', () => {
    const apiClient = createApiClient()

    renderApp('/certificates', apiClient, { role: 'NO_ACCESS' })

    expect(
      screen.getByRole('heading', { name: /permission denied/i }),
    ).toBeInTheDocument()
    expect(apiClient.listCertificates).not.toHaveBeenCalled()
  })

  it('renders empty and API permission states', async () => {
    const emptyClient = createApiClient({
      content: [],
      totalElements: 0,
      totalPages: 0,
    })

    renderApp('/certificates', emptyClient, undefined, 'empty-state')

    expect(
      await screen.findByRole('heading', {
        name: /no certificates match the current view/i,
      }),
    ).toBeInTheDocument()

    const forbiddenClient = createApiClient()
    forbiddenClient.listCertificates.mockRejectedValue(
      new ApiError(403, 'Use an account with the required permission.'),
    )

    renderApp('/certificates', forbiddenClient, undefined, 'forbidden-state')

    expect(
      await screen.findByRole('heading', { name: /^permission denied$/i }),
    ).toBeInTheDocument()
  })

  it('runs global search and links to typed results', async () => {
    const user = userEvent.setup()
    const apiClient = createApiClient()

    renderApp('/search', apiClient)

    await user.type(screen.getByRole('textbox', { name: /^search term$/i }), 'inventory')
    const searchButtons = screen.getAllByRole('button', { name: /^search$/i })
    await user.click(searchButtons[searchButtons.length - 1]!)

    expect(
      await screen.findByRole('link', { name: /inventory.example.test/i }),
    ).toHaveAttribute('href', '/certificates/cert-1')
    expect(apiClient.globalSearch).toHaveBeenCalledWith(
      'inventory',
      undefined,
      expect.anything(),
      expect.anything(),
    )
  })

  it('renders settings pages and enforces management controls by role', async () => {
    const user = userEvent.setup()
    const apiClient = createApiClient()

    renderApp('/settings', apiClient, { role: 'ADMIN', tenantId: 'tenant-1' })

    expect(await screen.findByRole('heading', { name: /settings/i })).toBeInTheDocument()
    expect((await screen.findAllByText(/Default Tenant/i))[0]).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /create/i })).toBeInTheDocument()

    await user.click(screen.getByRole('tab', { name: /organizations/i }))
    expect(await screen.findByText(/Platform Operations/i)).toBeInTheDocument()

    await user.click(screen.getByRole('tab', { name: /roles/i }))
    expect(await screen.findByText(/Built-in matrix/i)).toBeInTheDocument()

    await user.click(screen.getByRole('tab', { name: /service accounts/i }))
    expect((await screen.findAllByText(/inventory-importer/i))[0]).toBeInTheDocument()
    expect(await screen.findByText(/tok_123/i)).toBeInTheDocument()

    const readOnlyClient = createApiClient()
    renderApp(
      '/settings',
      readOnlyClient,
      { role: 'READ_ONLY', tenantId: 'tenant-1' },
      'settings-readonly',
    )
    expect((await screen.findAllByText(/Default Tenant/i))[0]).toBeInTheDocument()
    expect(readOnlyClient.createTenant).not.toHaveBeenCalled()
  })
})

function renderApp(
  route: string,
  apiClient: ReturnType<typeof createApiClient>,
  initialSession?: Parameters<typeof App>[0]['initialSession'],
  storageKey = 'test-profile',
) {
  return render(
    <MemoryRouter initialEntries={[route]}>
      <App
        apiClient={apiClient}
        initialSession={initialSession}
        storageKey={storageKey}
      />
    </MemoryRouter>,
  )
}

function createApiClient(
  pageOverrides: Partial<Awaited<ReturnType<ApiClient['listCertificates']>>> = {},
) {
  const listTenants = vi.fn<ApiClient['listTenants']>().mockResolvedValue([
    {
      id: 'tenant-1',
      slug: 'default',
      name: 'Default Tenant',
      status: 'ACTIVE',
      defaultTenant: true,
    },
  ])
  const listCertificates = vi
    .fn<ApiClient['listCertificates']>()
    .mockResolvedValue({
      content: [certificateSummary],
      page: 0,
      size: 25,
      totalElements: 1,
      totalPages: 1,
      ...pageOverrides,
    })
  const getCertificate = vi
    .fn<ApiClient['getCertificate']>()
    .mockResolvedValue(certificateDetail)
  const createTenant = vi.fn<ApiClient['createTenant']>().mockResolvedValue({
    id: 'tenant-2',
    slug: 'other',
    name: 'Other Tenant',
    status: 'ACTIVE',
    defaultTenant: false,
  })
  const updateTenant = vi.fn<ApiClient['updateTenant']>().mockResolvedValue({
    id: 'tenant-1',
    slug: 'default',
    name: 'Default Tenant',
    status: 'ACTIVE',
    defaultTenant: true,
  })
  const listOrganizations = vi.fn<ApiClient['listOrganizations']>().mockResolvedValue([
    {
      id: 'org-1',
      tenantId: 'tenant-1',
      slug: 'platform',
      name: 'Platform Operations',
      status: 'ACTIVE',
    },
  ])
  const createOrganization = vi
    .fn<ApiClient['createOrganization']>()
    .mockResolvedValue({
      id: 'org-2',
      tenantId: 'tenant-1',
      slug: 'security',
      name: 'Security',
      status: 'ACTIVE',
    })
  const updateOrganization = vi
    .fn<ApiClient['updateOrganization']>()
    .mockResolvedValue({
      id: 'org-1',
      tenantId: 'tenant-1',
      slug: 'platform',
      name: 'Platform Operations',
      status: 'ACTIVE',
    })
  const listRoles = vi.fn<ApiClient['listRoles']>().mockResolvedValue([
    { key: 'ADMIN', permissions: ['TENANT_READ', 'TENANT_MANAGE'] },
    { key: 'READ_ONLY', permissions: ['TENANT_READ'] },
  ])
  const listPermissions = vi.fn<ApiClient['listPermissions']>().mockResolvedValue([
    { key: 'TENANT_READ' },
    { key: 'TENANT_MANAGE' },
  ])
  const listServiceAccounts = vi
    .fn<ApiClient['listServiceAccounts']>()
    .mockResolvedValue([
      {
        id: 'service-account-1',
        tenantId: 'tenant-1',
        name: 'inventory-importer',
        status: 'ACTIVE',
      },
    ])
  const createServiceAccount = vi
    .fn<ApiClient['createServiceAccount']>()
    .mockResolvedValue({
      id: 'service-account-2',
      tenantId: 'tenant-1',
      name: 'discovery',
      status: 'ACTIVE',
    })
  const listApiTokens = vi.fn<ApiClient['listApiTokens']>().mockResolvedValue([
    {
      id: 'token-1',
      serviceAccountId: 'service-account-1',
      tokenPrefix: 'tok_123',
      status: 'ACTIVE',
      scopes: ['CERTIFICATE_READ'],
      expiresAt: null,
    },
  ])
  const createApiToken = vi.fn<ApiClient['createApiToken']>().mockResolvedValue({
    id: 'token-2',
    serviceAccountId: 'service-account-1',
    tokenPrefix: 'tok_456',
    token: 'clm_secret',
    scopes: ['CERTIFICATE_READ'],
    expiresAt: null,
  })
  const rotateApiToken = vi.fn<ApiClient['rotateApiToken']>().mockResolvedValue({
    id: 'token-1',
    serviceAccountId: 'service-account-1',
    tokenPrefix: 'tok_789',
    token: 'clm_rotated',
    scopes: ['CERTIFICATE_READ'],
    expiresAt: null,
  })
  const revokeApiToken = vi.fn<ApiClient['revokeApiToken']>().mockResolvedValue({
    id: 'token-1',
    serviceAccountId: 'service-account-1',
    tokenPrefix: 'tok_123',
    status: 'REVOKED',
    scopes: ['CERTIFICATE_READ'],
    expiresAt: null,
  })
  const globalSearch = vi.fn<ApiClient['globalSearch']>().mockResolvedValue({
    query: 'inventory',
    limit: 10,
    results: [
      {
        type: 'certificate',
        id: 'cert-1',
        tenantId: 'tenant-1',
        title: 'inventory.example.test',
        subtitle: 'platform-team | CN=Example Root,O=Example',
        status: 'ACTIVE',
        href: '/certificates/cert-1',
        matchedFields: ['subject'],
      },
    ],
  })

  return {
    listTenants,
    createTenant,
    updateTenant,
    listOrganizations,
    createOrganization,
    updateOrganization,
    listRoles,
    listPermissions,
    listServiceAccounts,
    createServiceAccount,
    listApiTokens,
    createApiToken,
    rotateApiToken,
    revokeApiToken,
    listCertificates,
    getCertificate,
    globalSearch,
  }
}
