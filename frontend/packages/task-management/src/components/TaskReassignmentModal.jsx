import { useState, useEffect, useCallback } from 'react';
import { getTechnicians, reassignTask } from '../services/taskApi';
import './TaskReassignmentModal.css';

const WORKLOAD_THRESHOLD = 10;

/**
 * TaskReassignmentModal - Modal dialog for reassigning tasks to different technicians
 * 
 * @param {Object} props
 * @param {Object} props.task - Task object to reassign (requires id, title, status, assignedTechnicianId)
 * @param {boolean} props.isOpen - Whether modal is open
 * @param {Function} props.onClose - Callback when modal closes
 * @param {Function} props.onReassignmentComplete - Callback when reassignment succeeds
 */
const TaskReassignmentModal = ({ task, isOpen, onClose, onReassignmentComplete }) => {
  const [technicians, setTechnicians] = useState([]);
  const [selectedTechnician, setSelectedTechnician] = useState(null);
  const [reason, setReason] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isReassigning, setIsReassigning] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [showWarning, setShowWarning] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [assignmentHistory, setAssignmentHistory] = useState([]);

  const isInProgress = task?.status === 'IN_PROGRESS';

  // Load technicians when modal opens
  const loadTechnicians = useCallback(async () => {
    if (!isOpen) return;
    
    setIsLoading(true);
    setError('');
    
    try {
      const technicianList = await getTechnicians();
      // Sort by workload (least loaded first), exclude current technician
      const sortedTechnicians = technicianList
        .filter(tech => tech.id !== task?.assignedTechnicianId)
        .map(tech => ({
          ...tech,
          workload: tech.workload || 0,
        }))
        .sort((a, b) => a.workload - b.workload);
      
      setTechnicians(sortedTechnicians);
    } catch (err) {
      setError(err.message || 'Failed to load technicians');
    } finally {
      setIsLoading(false);
    }
  }, [isOpen, task?.assignedTechnicianId]);

  useEffect(() => {
    if (isOpen) {
      loadTechnicians();
      setSelectedTechnician(null);
      setReason('');
      setShowConfirmation(false);
      setShowWarning(false);
      setShowHistory(false);
      setError('');
      setSuccessMessage('');
      setAssignmentHistory([]);
    }
  }, [isOpen, loadTechnicians]);

  // Handle clicking outside modal to close
  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget && !isReassigning) {
      onClose();
    }
  };

  // Handle escape key to close modal
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen && !isReassigning) {
        onClose();
      }
    };
    
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, isReassigning, onClose]);

  // Handle technician selection
  const handleSelectTechnician = (technician) => {
    setSelectedTechnician(technician);
    setError('');
  };

  // Handle proceeding to next step
  const handleProceedToConfirm = () => {
    if (!selectedTechnician) return;
    
    // Validate reason for IN_PROGRESS tasks
    if (isInProgress && !reason.trim()) {
      setError('Reason is required for reassigning tasks that are in progress');
      return;
    }
    
    if (isInProgress && !showWarning) {
      // Show warning first for IN_PROGRESS tasks
      setShowWarning(true);
    } else {
      setShowConfirmation(true);
    }
  };

  // Handle going back from confirmation
  const handleBackToSelection = () => {
    setShowConfirmation(false);
    setShowWarning(false);
  };

  // Handle reassignment confirmation
  const handleConfirmReassignment = async () => {
    if (!selectedTechnician || !task) return;

    setIsReassigning(true);
    setError('');

    try {
      const result = await reassignTask(
        task.id,
        selectedTechnician.id,
        isInProgress ? reason : null
      );
      
      setAssignmentHistory(result.assignmentHistory || []);
      setSuccessMessage(`Task successfully reassigned to ${selectedTechnician.name}`);
      
      // Show success message briefly, then close
      setTimeout(() => {
        if (onReassignmentComplete) {
          onReassignmentComplete(result);
        }
        onClose();
      }, 1500);
    } catch (err) {
      setError(err.message || 'Failed to reassign task');
      setShowConfirmation(false);
      setShowWarning(false);
    } finally {
      setIsReassigning(false);
    }
  };

  // Toggle history view
  const handleToggleHistory = () => {
    setShowHistory(!showHistory);
  };

  if (!isOpen) return null;

  const isAtCapacity = (technician) => technician.workload >= WORKLOAD_THRESHOLD;

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getActionLabel = (action) => {
    switch (action) {
      case 'ASSIGNED': return 'Assigned';
      case 'REASSIGNED': return 'Reassigned';
      default: return action;
    }
  };

  const renderTechnicianSelection = () => (
    <>
      <div className="reassign-task-info">
        <div className="task-info-row">
          <span className="task-info-label">Task:</span>
          <span className="task-info-value">{task?.title}</span>
        </div>
        <div className="task-info-row">
          <span className="task-info-label">Status:</span>
          <span className={`status-badge status-${task?.status?.toLowerCase().replace('_', '-')}`}>
            {task?.status?.replace('_', ' ')}
          </span>
        </div>
        {task?.assignedTechnician && (
          <div className="task-info-row">
            <span className="task-info-label">Current Technician:</span>
            <span className="task-info-value">{task.assignedTechnician}</span>
          </div>
        )}
      </div>

      {isInProgress && (
        <div className="reason-input-container">
          <label htmlFor="reassign-reason" className="reason-label">
            Reason for Reassignment <span className="required">*</span>
          </label>
          <textarea
            id="reassign-reason"
            className="reason-textarea"
            placeholder="Please provide a reason for reassigning this in-progress task..."
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={3}
          />
        </div>
      )}

      <div className="technician-section-header">
        <h3>Select New Technician</h3>
      </div>

      {technicians.length === 0 && !error ? (
        <div className="empty-state">
          <div className="empty-state-icon" aria-hidden="true">👥</div>
          <p className="empty-state-text">No other technicians available for reassignment</p>
        </div>
      ) : (
        <ul className="technician-list" role="listbox" aria-label="Available technicians">
          {technicians.map((technician) => (
            <li
              key={technician.id}
              className={`technician-item ${selectedTechnician?.id === technician.id ? 'selected' : ''} ${isAtCapacity(technician) ? 'at-capacity' : ''}`}
              onClick={() => handleSelectTechnician(technician)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleSelectTechnician(technician);
                }
              }}
              role="option"
              aria-selected={selectedTechnician?.id === technician.id}
              tabIndex={0}
            >
              <div className="technician-info">
                <p className="technician-name">
                  {technician.name}
                  {isAtCapacity(technician) && (
                    <span className="warning-icon" role="img" aria-label="At capacity warning">⚠️</span>
                  )}
                </p>
                <p className="technician-email">{technician.email}</p>
                {isAtCapacity(technician) && (
                  <div className="capacity-warning">
                    <span role="img" aria-hidden="true">⚠️</span>
                    At capacity ({technician.workload} tasks)
                  </div>
                )}
              </div>
              <div className={`workload-badge ${isAtCapacity(technician) ? 'high-workload' : ''}`}>
                <span className="workload-count">{technician.workload}</span>
                <span className="workload-label">Tasks</span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </>
  );

  const renderWarningView = () => (
    <div className="warning-view">
      <div className="warning-icon-large" role="img" aria-label="Warning">⚠️</div>
      <p className="warning-title">Warning: Task In Progress</p>
      <p className="warning-message">
        This task is currently <strong>IN PROGRESS</strong>. Reassigning it may disrupt ongoing work.
      </p>
      <div className="warning-reason-display">
        <p className="warning-reason-label">Reassignment Reason:</p>
        <p className="warning-reason-value">{reason}</p>
      </div>
      <p className="warning-question">
        Are you sure you want to proceed with reassigning this task to <strong>{selectedTechnician?.name}</strong>?
      </p>
    </div>
  );

  const renderConfirmationView = () => (
    <div className="confirmation-view">
      <p className="confirmation-title">
        Confirm Reassignment
      </p>
      <div className="confirmation-details">
        <p className="confirmation-label">Task</p>
        <p className="confirmation-value">{task?.title}</p>
        {task?.assignedTechnician && (
          <>
            <p className="confirmation-label">Previous Technician</p>
            <p className="confirmation-value">{task.assignedTechnician}</p>
          </>
        )}
        <p className="confirmation-label">New Technician</p>
        <p className="confirmation-value">{selectedTechnician?.name}</p>
        <p className="confirmation-label">Current Workload</p>
        <p className="confirmation-value">
          {selectedTechnician?.workload} active task{selectedTechnician?.workload !== 1 ? 's' : ''}
        </p>
        {reason && (
          <>
            <p className="confirmation-label">Reason</p>
            <p className="confirmation-value">{reason}</p>
          </>
        )}
        {isAtCapacity(selectedTechnician) && (
          <div className="confirmation-warning">
            <span role="img" aria-label="Warning">⚠️</span>
            This technician is at or exceeds capacity ({WORKLOAD_THRESHOLD}+ tasks)
          </div>
        )}
      </div>
    </div>
  );

  const renderHistoryView = () => (
    <div className="history-view">
      <h3 className="history-title">Assignment History</h3>
      {assignmentHistory.length === 0 ? (
        <p className="history-empty">No assignment history available</p>
      ) : (
        <ul className="history-list">
          {assignmentHistory.map((entry, index) => (
            <li key={entry.id || index} className="history-item">
              <div className="history-item-header">
                <span className={`history-action action-${entry.action?.toLowerCase()}`}>
                  {getActionLabel(entry.action)}
                </span>
                <span className="history-date">{formatDate(entry.actionAt)}</span>
              </div>
              <div className="history-item-details">
                <p>Technician ID: {entry.technicianId}</p>
                {entry.previousTechnicianId && (
                  <p>Previous Technician ID: {entry.previousTechnicianId}</p>
                )}
                {entry.actionBy && <p>By: {entry.actionBy}</p>}
                {entry.reason && <p>Reason: {entry.reason}</p>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );

  const getModalTitle = () => {
    if (showHistory) return 'Assignment History';
    if (showConfirmation) return 'Confirm Reassignment';
    if (showWarning) return 'Warning: In Progress Task';
    return 'Reassign Task';
  };

  return (
    <div 
      className="modal-overlay" 
      onClick={handleOverlayClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <div className="modal-container reassignment-modal">
        <div className="modal-header">
          <h2 id="modal-title" className="modal-title">
            {getModalTitle()}
          </h2>
          <button 
            className="modal-close-button" 
            onClick={onClose}
            disabled={isReassigning}
            aria-label="Close modal"
          >
            ×
          </button>
        </div>

        <div className="modal-content">
          {error && (
            <div className="modal-error-banner" role="alert">
              {error}
            </div>
          )}

          {successMessage && (
            <div className="modal-success-banner" role="status">
              {successMessage}
            </div>
          )}

          {isLoading ? (
            <div className="modal-loading" aria-live="polite">
              <div className="spinner" aria-hidden="true"></div>
              <span>Loading technicians...</span>
            </div>
          ) : showHistory ? (
            renderHistoryView()
          ) : showConfirmation && selectedTechnician ? (
            renderConfirmationView()
          ) : showWarning && selectedTechnician ? (
            renderWarningView()
          ) : (
            renderTechnicianSelection()
          )}
        </div>

        <div className="modal-actions">
          {showHistory ? (
            <button
              className="modal-button modal-button-secondary"
              onClick={handleToggleHistory}
            >
              Back to Reassignment
            </button>
          ) : showConfirmation ? (
            <>
              <button
                className="modal-button modal-button-secondary"
                onClick={handleBackToSelection}
                disabled={isReassigning}
              >
                Back
              </button>
              <button
                className="modal-button modal-button-primary"
                onClick={handleConfirmReassignment}
                disabled={isReassigning}
              >
                {isReassigning ? 'Reassigning...' : 'Confirm Reassignment'}
              </button>
            </>
          ) : showWarning ? (
            <>
              <button
                className="modal-button modal-button-secondary"
                onClick={handleBackToSelection}
                disabled={isReassigning}
              >
                Cancel
              </button>
              <button
                className="modal-button modal-button-warning"
                onClick={handleProceedToConfirm}
                disabled={isReassigning}
              >
                Proceed Anyway
              </button>
            </>
          ) : (
            <>
              <button
                className="modal-button modal-button-secondary"
                onClick={onClose}
                disabled={isReassigning}
              >
                Cancel
              </button>
              <button
                className="modal-button modal-button-primary"
                onClick={handleProceedToConfirm}
                disabled={!selectedTechnician || isLoading}
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

export default TaskReassignmentModal;
