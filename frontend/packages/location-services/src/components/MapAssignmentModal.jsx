import { useState, useEffect, useCallback, useMemo } from 'react';
import { TechnicianStatus, getStatusLabel, getStatusColor } from '../services/technicianService';
import { getTechniciansWithDistanceFromTask, formatDistance } from '../utils/distanceUtils';
import './MapAssignmentModal.css';

const WORKLOAD_THRESHOLD = 10;

/**
 * MapAssignmentModal - Modal dialog for assigning tasks to technicians from the map
 * Displays nearby technicians with distance information
 * 
 * Note: This component uses task?.id as a key from the parent to reset state
 * when a new task is selected.
 * 
 * @param {Object} props
 * @param {Object} props.task - Task object to assign (requires id, title, coordinates)
 * @param {Array} props.technicians - Array of technician objects with location data
 * @param {boolean} props.isOpen - Whether modal is open
 * @param {Function} props.onClose - Callback when modal closes
 * @param {Function} props.onAssign - Callback when assignment is confirmed (taskId, technicianId)
 * @param {Function} props.onHighlightTechnicians - Callback to highlight technicians on map
 * @param {boolean} props.isAssigning - Whether assignment is in progress
 * @param {string} props.error - Error message to display
 * @param {string} props.successMessage - Success message to display
 */
const MapAssignmentModal = ({ 
  task, 
  technicians = [],
  isOpen, 
  onClose, 
  onAssign,
  onHighlightTechnicians,
  isAssigning = false,
  error = '',
  successMessage = '',
}) => {
  // State for technician selection
  const [selectedTechnician, setSelectedTechnician] = useState(null);
  const [showConfirmation, setShowConfirmation] = useState(false);

  // Calculate distances and sort technicians using useMemo
  const techniciansWithDistance = useMemo(() => {
    if (!isOpen || !task || technicians.length === 0) {
      return [];
    }
    return getTechniciansWithDistanceFromTask(task, technicians, 'km');
  }, [isOpen, task, technicians]);

  // Highlight nearby technicians on map when modal opens
  useEffect(() => {
    if (isOpen && techniciansWithDistance.length > 0 && onHighlightTechnicians) {
      const nearbyIds = techniciansWithDistance.map(t => t.technicianId);
      onHighlightTechnicians(nearbyIds);
    }
  }, [isOpen, techniciansWithDistance, onHighlightTechnicians]);

  // Clear highlights when modal closes
  useEffect(() => {
    if (!isOpen && onHighlightTechnicians) {
      onHighlightTechnicians([]);
    }
  }, [isOpen, onHighlightTechnicians]);

  // Handle clicking outside modal to close
  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget && !isAssigning) {
      onClose();
    }
  };

  // Handle escape key to close modal
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen && !isAssigning) {
        onClose();
      }
    };
    
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, isAssigning, onClose]);

  // Handle technician selection
  const handleSelectTechnician = useCallback((technician) => {
    setSelectedTechnician(technician);
  }, []);

  // Handle proceeding to confirmation
  const handleProceedToConfirm = () => {
    if (selectedTechnician) {
      setShowConfirmation(true);
    }
  };

  // Handle going back from confirmation
  const handleBackToSelection = () => {
    setShowConfirmation(false);
  };

  // Handle assignment confirmation
  const handleConfirmAssignment = () => {
    if (!selectedTechnician || !task || isAssigning) return;
    onAssign(task.id, selectedTechnician.technicianId);
  };

  if (!isOpen) return null;

  const isAtCapacity = (technician) => (technician.workload || 0) >= WORKLOAD_THRESHOLD;
  const isAvailable = (technician) => technician.status === TechnicianStatus.AVAILABLE;

  return (
    <div 
      className="map-assignment-modal-overlay" 
      onClick={handleOverlayClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby="map-assignment-modal-title"
    >
      <div className="map-assignment-modal-container">
        <div className="map-assignment-modal-header">
          <h2 id="map-assignment-modal-title" className="map-assignment-modal-title">
            {showConfirmation ? 'Confirm Assignment' : 'Assign Task from Map'}
          </h2>
          <button 
            className="map-assignment-modal-close-button" 
            onClick={onClose}
            disabled={isAssigning}
            aria-label="Close modal"
          >
            ×
          </button>
        </div>

        <div className="map-assignment-modal-content">
          {/* Task Info */}
          <div className="map-assignment-task-info">
            <h3 className="map-assignment-task-title">{task?.title}</h3>
            <p className="map-assignment-task-address">{task?.clientAddress}</p>
          </div>

          {error && (
            <div className="map-assignment-modal-error-banner" role="alert">
              {error}
            </div>
          )}

          {successMessage && (
            <div className="map-assignment-modal-success-banner" role="status">
              {successMessage}
            </div>
          )}

          {showConfirmation && selectedTechnician ? (
            <div className="map-assignment-confirmation-view">
              <p className="map-assignment-confirmation-title">
                Are you sure you want to assign this task?
              </p>
              <div className="map-assignment-confirmation-details">
                <p className="map-assignment-confirmation-label">Task</p>
                <p className="map-assignment-confirmation-value">{task?.title}</p>
                <p className="map-assignment-confirmation-label">Technician</p>
                <p className="map-assignment-confirmation-value">{selectedTechnician.name}</p>
                <p className="map-assignment-confirmation-label">Distance</p>
                <p className="map-assignment-confirmation-value">
                  {formatDistance(selectedTechnician.distance, 'km')}
                </p>
                <p className="map-assignment-confirmation-label">Current Workload</p>
                <p className="map-assignment-confirmation-value">
                  {selectedTechnician.workload || 0} active task{(selectedTechnician.workload || 0) !== 1 ? 's' : ''}
                </p>
                {isAtCapacity(selectedTechnician) && (
                  <div className="map-assignment-confirmation-warning">
                    <span role="img" aria-label="Warning">⚠️</span>
                    This technician is at or exceeds capacity ({WORKLOAD_THRESHOLD}+ tasks)
                  </div>
                )}
              </div>
            </div>
          ) : techniciansWithDistance.length === 0 ? (
            <div className="map-assignment-empty-state">
              <div className="map-assignment-empty-state-icon" aria-hidden="true">📍</div>
              <p className="map-assignment-empty-state-text">No technicians with valid locations found</p>
            </div>
          ) : (
            <ul className="map-assignment-technician-list" role="listbox" aria-label="Available technicians sorted by distance">
              {techniciansWithDistance.map((technician) => (
                <li
                  key={technician.technicianId}
                  className={`map-assignment-technician-item ${selectedTechnician?.technicianId === technician.technicianId ? 'selected' : ''} ${isAtCapacity(technician) ? 'at-capacity' : ''} ${!isAvailable(technician) ? 'unavailable' : ''}`}
                  onClick={() => handleSelectTechnician(technician)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      handleSelectTechnician(technician);
                    }
                  }}
                  role="option"
                  aria-selected={selectedTechnician?.technicianId === technician.technicianId}
                  tabIndex={0}
                >
                  <div className="map-assignment-technician-info">
                    <div className="map-assignment-technician-header">
                      <p className="map-assignment-technician-name">
                        {technician.name}
                        {isAtCapacity(technician) && (
                          <span className="map-assignment-warning-icon" role="img" aria-label="At capacity warning">⚠️</span>
                        )}
                      </p>
                      <span 
                        className="map-assignment-technician-status"
                        style={{ 
                          backgroundColor: getStatusColor(technician.status),
                          color: '#ffffff',
                        }}
                      >
                        {getStatusLabel(technician.status)}
                      </span>
                    </div>
                    <div className="map-assignment-technician-details">
                      <span className="map-assignment-technician-distance">
                        📍 {formatDistance(technician.distance, 'km')}
                      </span>
                      <span className="map-assignment-technician-workload">
                        {technician.workload || 0} task{(technician.workload || 0) !== 1 ? 's' : ''}
                      </span>
                    </div>
                    {isAtCapacity(technician) && (
                      <div className="map-assignment-capacity-warning">
                        <span role="img" aria-hidden="true">⚠️</span>
                        At capacity ({technician.workload} tasks)
                      </div>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="map-assignment-modal-actions">
          {showConfirmation ? (
            <>
              <button
                className="map-assignment-modal-button map-assignment-modal-button-secondary"
                onClick={handleBackToSelection}
                disabled={isAssigning}
              >
                Back
              </button>
              <button
                className="map-assignment-modal-button map-assignment-modal-button-primary"
                onClick={handleConfirmAssignment}
                disabled={isAssigning}
              >
                {isAssigning ? 'Assigning...' : 'Confirm Assignment'}
              </button>
            </>
          ) : (
            <>
              <button
                className="map-assignment-modal-button map-assignment-modal-button-secondary"
                onClick={onClose}
                disabled={isAssigning}
              >
                Cancel
              </button>
              <button
                className="map-assignment-modal-button map-assignment-modal-button-primary"
                onClick={handleProceedToConfirm}
                disabled={!selectedTechnician}
              >
                Continue
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default MapAssignmentModal;
