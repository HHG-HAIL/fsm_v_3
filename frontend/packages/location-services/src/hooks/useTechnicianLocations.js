import { useState, useEffect, useCallback, useRef } from 'react';
import { fetchTechnicianLocations } from '../services/technicianService';

/**
 * Custom hook for fetching and managing technician locations
 * 
 * @param {Object} options - Hook options
 * @param {number} options.refreshInterval - Auto-refresh interval in milliseconds (0 to disable)
 * @returns {Object} Hook state and methods
 */
export function useTechnicianLocations({ refreshInterval = 0 } = {}) {
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);
  const abortControllerRef = useRef(null);

  const loadTechnicians = useCallback(async () => {
    // Cancel any in-flight request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    // Create new abort controller for this request
    abortControllerRef.current = new AbortController();
    const currentController = abortControllerRef.current;

    try {
      setLoading(true);
      setError(null);
      
      const data = await fetchTechnicianLocations();
      
      // Only update state if this request wasn't aborted
      if (!currentController.signal.aborted) {
        setTechnicians(data);
        setLastUpdated(new Date());
      }
    } catch (err) {
      // Don't update state if request was aborted
      if (!currentController.signal.aborted) {
        setError(err.message || 'Failed to load technician locations');
        setTechnicians([]);
      }
    } finally {
      // Only update loading state if this request wasn't aborted
      if (!currentController.signal.aborted) {
        setLoading(false);
      }
    }
  }, []);

  // Initial load
  useEffect(() => {
    loadTechnicians();
    
    // Cleanup function
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [loadTechnicians]);

  // Auto-refresh
  useEffect(() => {
    if (refreshInterval > 0) {
      const intervalId = setInterval(loadTechnicians, refreshInterval);
      return () => clearInterval(intervalId);
    }
  }, [refreshInterval, loadTechnicians]);

  return {
    technicians,
    loading,
    error,
    lastUpdated,
    refresh: loadTechnicians,
  };
}
