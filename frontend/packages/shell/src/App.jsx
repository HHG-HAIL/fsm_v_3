import './App.css'

const IDENTITY_SERVICE_URL = import.meta.env.VITE_IDENTITY_SERVICE_URL || 'http://localhost:5174';

function App() {
  return (
    <div className="shell-container">
      <header className="shell-header">
        <h1>Field Service Management System</h1>
      </header>
      <main className="shell-content">
        <iframe
          src={IDENTITY_SERVICE_URL}
          title="Identity Service"
          className="micro-frontend-iframe"
        />
      </main>
    </div>
  )
}

export default App

