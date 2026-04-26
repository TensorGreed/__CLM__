import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { App } from './App'

describe('App', () => {
  it('renders the repository foundation overview', () => {
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>,
    )

    expect(
      screen.getByRole('heading', { name: /repository foundation/i }),
    ).toBeInTheDocument()
    expect(screen.getByText(/Spring Boot API skeleton/i)).toBeInTheDocument()
  })

  it('routes to the system service view', () => {
    render(
      <MemoryRouter initialEntries={['/system']}>
        <App />
      </MemoryRouter>,
    )

    expect(
      screen.getByRole('heading', { name: /local services/i }),
    ).toBeInTheDocument()
    expect(screen.getByText(/localhost:5432/i)).toBeInTheDocument()
  })
})
