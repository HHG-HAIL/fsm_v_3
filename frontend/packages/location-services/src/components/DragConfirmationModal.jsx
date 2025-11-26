import { useEffect, useCallback } from 'react';
import { TechnicianStatus, getStatusLabel, getStatusColor } from '../services/technicianService';
import { calculateTaskToTechnicianDistance, formatDistance } from '../utils/distanceUtils';
import './DragConfirmationModal.css';

const WORKLOAD_THRESHOLD = 10;

/**
 * DragConfirmationModal - Modal dialog for confirming drag-and-drop task assignments
 * 
 * @param {Object} props
 * @param {Object} props.task - Task being assigned
 * @param {Object} props.technician - Technician being assigned to
 * @param {boolean} props.isOpen - Whether modal is open
 * @param {Function} props.onClose - Callback when modal closes
 * @param {Function} props.onConfirm - Callback when assignment is confirmed
 * @param {boolean} props.isAssigning - Whether assignment is in progress
 * @param {string} props.error - Error message to display
 * @param {string} props.successMessage - Success message to display
 */
const DragConfirmationModal = ({ 
  task, 
  technician,
  isOpen, 
  onClose, 
  onConfirm,
  isAssigning = false,
  error = '',
  successMessage = '',
}) => {
  // Calculate distance between task and technician
  const distance = task && technician 
    ? calculateTaskToTechnicianDistance(task, technician, 'km') 
    : null;

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

  // Handle confirm assignment
  const handleConfirm = useCallback(() => {
    if (!task || !technician || isAssigning) return;
    onConfirm(task.id, technician.technicianId);
  }, [task, technician, isAssigning, onConfirm]);

  // Handle cancel
  const handleCancel = useCallback(() => {
    if (!isAssigning) {
      onClose();
    }
  }, [isAssigning, onClose]);

  if (!isOpen || !task || !technician) return null;

  const isAtCapacity = (technician.workload || 0) >= WORKLOAD_THRESHOLD;
  const isAvailable = technician.status === TechnicianStatus.AVAILABLE;

  return (
    <div 
      className="drag-confirmation-modal-overlay" 
      onClick={handleOverlayClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby="drag-confirmation-modal-title"
    >
      <div className="drag-confirmation-modal-container">
        <div className="drag-confirmation-modal-header">
          <h2 id="drag-confirmation-modal-title" className="drag-confirmation-modal-title">
            Confirm Drag-and-Drop Assignment
          </h2>
          <button 
            className="drag-confirmation-modal-close-button" 
            onClick={handleCancel}
            disabled={isAssigning}
            aria-label="Close modal"
          >
            ×
          </button>
        </div>

        <div className="drag-confirmation-modal-content">
          {/* Success/Error banners */}
          {error && (
            <div className="drag-confirmation-modal-error-banner" role="alert">
              {error}
            </div>
          )}

          {successMessage && (
            <div className="drag-confirmation-modal-success-banner" role="status">
              {successMessage}
            </div>
          )}

          {/* Assignment Preview */}
          <div className="drag-confirmation-preview">
            <div className="drag-confirmation-icon" aria-hidden="true">
              <span className="drag-icon-task">📋</span>
              <span className="drag-icon-arrow">→</span>
              <span className="drag-icon-technician">👤</span>
            </div>
            
            <p className="drag-confirmation-question">
              Assign this task to the selected technician?
            </p>
          </div>

          {/* Task Info */}
          <div className="drag-confirmation-section">
            <h3 className="drag-confirmation-section-title">Task</h3>
            <div className="drag-confirmation-section-content">
              <p className="drag-confirmation-primary">{task.title}</p>
              <p className="drag-confirmation-secondary">{task.clientAddress}</p>
            </div>
          </div>

          {/* Technician Info */}
          <div className="drag-confirmation-section">
            <h3 className="drag-confirmation-section-title">Technician</h3>
            <div className="drag-confirmation-section-content">
              <div className="drag-confirmation-technician-header">
                <p className="drag-confirmation-primary">{technician.name}</p>
                <span 
                  className="drag-confirmation-status"
                  style={{ 
                    backgroundColor: getStatusColor(technician.status),
                    color: '#ffffff',
                  }}
                >
                  {getStatusLabel(technician.status)}
                </span>
              </div>
              <div className="drag-confirmation-details">
                <span className="drag-confirmation-detail">
                  📍 {distance !== null ? formatDistance(distance, 'km') : 'N/A'}
                </span>
                <span className="drag-confirmation-detail">
                  📋 {technician.workload || 0} task{(technician.workload || 0) !== 1 ? 's' : ''}
                </span>
              </div>
            </div>
          </div>

          {/* Warnings */}
          {(!isAvailable || isAtCapacity) && (
            <div className="drag-confirmation-warnings">
              {!isAvailable && (
                <div className="drag-confirmation-warning">
                  <span role="img" aria-label="Warning">⚠️</span>
                  This technician is currently {getStatusLabel(technician.status).toLowerCase()}
                </div>
              )}
              {isAtCapacity && (
                <div className="drag-confirmation-warning">
                  <span role="img" aria-label="Warning">⚠️</span>
                  This technician is at or exceeds capacity ({WORKLOAD_THRESHOLD}+ tasks)
                </div>
              )}
            </div>
          )}
        </div>

        <div className="drag-confirmation-modal-actions">
          <button
            className="drag-confirmation-modal-button drag-confirmation-modal-button-secondary"
            onClick={handleCancel}
            disabled={isAssigning}
          >
            Cancel
          </button>
          <button
            className="drag-confirmation-modal-button drag-confirmation-modal-button-primary"
            onClick={handleConfirm}
            disabled={isAssigning}
          >
            {isAssigning ? 'Assigning...' : 'Confirm Assignment'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default DragConfirmationModal;
