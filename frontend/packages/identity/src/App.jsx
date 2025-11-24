import { useState, useEffect } from 'react'
import LoginPage from './components/LoginPage'
import UserManagement from './components/UserManagement'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userRole, setUserRole] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('userRole');
    setIsAuthenticated(!!token);
    setUserRole(role);
  }, []);

  const handleLogout = () => {
    localStorage.clear();
    setIsAuthenticated(false);
    setUserRole(null);
  };

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  if (userRole === 'ADMIN') {
    return (
      <div>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          padding: '10px 20px', 
          background: '#f8f9fa',
          borderBottom: '1px solid #dee2e6'
        }}>
          <span>Welcome, {localStorage.getItem('userName')}</span>
          <button 
            onClick={handleLogout}
            style={{
              padding: '6px 12px',
              background: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Logout
          </button>
        </div>
        <UserManagement />
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>Welcome, {localStorage.getItem('userName')}</h1>
      <p>Your role: {userRole}</p>
      <button 
        onClick={handleLogout}
        style={{
          padding: '10px 20px',
          background: '#dc3545',
          color: 'white',
          border: 'none',
          borderRadius: '6px',
          cursor: 'pointer',
          marginTop: '20px'
        }}
      >
        Logout
      </button>
    </div>
  );
}

export default App

