import './App.css'

function App() {
  return (
    <div className="shell-container">
      <header className="shell-header">
        <h1>Field Service Management System</h1>
      </header>
      <main className="shell-content">
        <iframe
          src="http://localhost:5174"
          title="Identity Service"
          className="micro-frontend-iframe"
        />
      </main>
    </div>
  )
}

export default App

