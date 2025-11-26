import { useState } from 'react';
import PropTypes from 'prop-types';
import './TaskCompletionModal.css';

const TaskCompletionModal = ({ isOpen, onClose, onSubmit, isSubmitting }) => {
  const [workSummary, setWorkSummary] = useState('');
  const [validationError, setValidationError] = useState('');

  const handleWorkSummaryChange = (e) => {
    const value = e.target.value;
    setWorkSummary(value);
    
    // Clear validation error when user starts typing
    if (validationError && value.length >= 10) {
      setValidationError('');
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // Validate work summary
    if (workSummary.trim().length < 10) {
      setValidationError('Work summary must be at least 10 characters long');
      return;
    }
    
    onSubmit(workSummary.trim());
  };

  const handleCancel = () => {
    setWorkSummary('');
    setValidationError('');
    onClose();
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal-overlay" onClick={handleCancel}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Complete Task</h2>
          <button
            className="close-button"
            onClick={handleCancel}
            disabled={isSubmitting}
            aria-label="Close modal"
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <label htmlFor="work-summary" className="form-label">
              Work Summary *
            </label>
            <textarea
              id="work-summary"
              className={`work-summary-textarea ${validationError ? 'error' : ''}`}
              value={workSummary}
              onChange={handleWorkSummaryChange}
              placeholder="Describe the work completed (minimum 10 characters)..."
              rows="6"
              disabled={isSubmitting}
              required
              aria-required="true"
              aria-invalid={!!validationError}
              aria-describedby={validationError ? 'summary-error' : undefined}
            />
            {validationError && (
              <div id="summary-error" className="validation-error" role="alert">
                {validationError}
              </div>
            )}
            <div className="character-count">
              {workSummary.length} characters
            </div>
          </div>

          <div className="modal-footer">
            <button
              type="button"
              className="cancel-button"
              onClick={handleCancel}
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="submit-button"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Completing...' : 'Complete Task'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

TaskCompletionModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  isSubmitting: PropTypes.bool,
};

export default TaskCompletionModal;
