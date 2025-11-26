import { useState, useEffect, useCallback } from 'react';
import './Notification.css';

/**
 * Notification component for displaying toast-style messages
 * 
 * @param {Object} props
 * @param {string} props.message - Message to display
 * @param {string} props.type - Notification type (success, error, info, warning)
 * @param {boolean} props.isVisible - Whether notification is visible
 * @param {number} props.duration - Auto-hide duration in milliseconds (0 to disable)
 * @param {Function} props.onClose - Callback when notification closes
 */
const Notification = ({ 
  message, 
  type = 'info', 
  isVisible = false,
  duration = 3000,
  onClose,
}) => {
  const [isExiting, setIsExiting] = useState(false);

  const handleClose = useCallback(() => {
    setIsExiting(true);
    setTimeout(() => {
      setIsExiting(false);
      if (onClose) {
        onClose();
      }
    }, 300); // Match animation duration
  }, [onClose]);

  useEffect(() => {
    if (isVisible && duration > 0) {
      const timer = setTimeout(() => {
        handleClose();
      }, duration);
      return () => clearTimeout(timer);
    }
  }, [isVisible, duration, handleClose]);

  if (!isVisible) return null;

  const getIcon = () => {
    switch (type) {
      case 'success':
        return '✓';
      case 'error':
        return '✕';
      case 'warning':
        return '⚠';
      case 'info':
      default:
        return 'ℹ';
    }
  };

  return (
    <div 
      className={`notification notification-${type} ${isExiting ? 'notification-exiting' : ''}`}
      role="alert"
      aria-live="polite"
    >
      <span className="notification-icon" aria-hidden="true">{getIcon()}</span>
      <span className="notification-message">{message}</span>
      <button 
        className="notification-close" 
        onClick={handleClose}
        aria-label="Close notification"
      >
        ×
      </button>
    </div>
  );
};

export default Notification;
