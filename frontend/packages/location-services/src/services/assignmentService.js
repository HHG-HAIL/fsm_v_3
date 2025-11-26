/**
 * Assignment service for handling task assignment operations
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * Gets auth headers with token
 * @returns {Object} Headers object
 */
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return headers;
};

/**
 * Assigns a task to a technician
 * @param {number} taskId - ID of the task to assign
 * @param {number} technicianId - ID of the technician to assign to
 * @returns {Promise<Object>} Assignment response
 */
export async function assignTaskToTechnician(taskId, technicianId) {
  // Construct URL properly to handle trailing slashes
  const baseUrl = API_BASE_URL.endsWith('/') ? API_BASE_URL.slice(0, -1) : API_BASE_URL;
  const url = `${baseUrl}/tasks/${taskId}/assign`;

  const response = await fetch(url, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ technicianId }),
  });

  if (!response.ok) {
    let errorMessage = 'Failed to assign task';
    try {
      const error = await response.json();
      errorMessage = error.message || errorMessage;
    } catch {
      errorMessage = `${response.status} ${response.statusText}`;
    }
    throw new Error(errorMessage);
  }

  return response.json();
}
