import { useState, useCallback } from 'react'
import './App.css'
import Map from './components/Map'
import MapAssignmentModal from './components/MapAssignmentModal'
import Notification from './components/Notification'
import { useUnassignedTasks } from './hooks/useUnassignedTasks'
import { useTechnicianLocations } from './hooks/useTechnicianLocations'
import { assignTaskToTechnician } from './services/assignmentService'

// Refresh interval: 30 seconds
const REFRESH_INTERVAL = 30000;

function App() {
  const { tasks, loading: tasksLoading, error: tasksError, refresh: refreshTasks } = useUnassignedTasks({ 
    refreshInterval: REFRESH_INTERVAL 
  });

  const { technicians, loading: techniciansLoading, error: techniciansError, refresh: refreshTechnicians } = useTechnicianLocations({
    refreshInterval: REFRESH_INTERVAL
  });

  // State for assignment modal
  const [selectedTask, setSelectedTask] = useState(null);
  const [isAssignmentModalOpen, setIsAssignmentModalOpen] = useState(false);
  const [isAssigning, setIsAssigning] = useState(false);
  const [assignmentError, setAssignmentError] = useState('');
  const [assignmentSuccess, setAssignmentSuccess] = useState('');

  // State for highlighted technicians
  const [highlightedTechnicianIds, setHighlightedTechnicianIds] = useState([]);

  // State for notification
  const [notification, setNotification] = useState({ 
    isVisible: false, 
    message: '', 
    type: 'info' 
  });

  // Handler for task marker clicks
  const handleTaskClick = (task) => {
    // Task selection functionality - popup displays automatically
    void task; // Acknowledge parameter to avoid unused warning
  };

  // Handler for assigning tasks - opens the map assignment modal
  const handleAssignTask = useCallback((task) => {
    setSelectedTask(task);
    setIsAssignmentModalOpen(true);
    setAssignmentError('');
    setAssignmentSuccess('');
  }, []);

  // Handler for viewing task details
  const handleViewDetails = (task) => {
    // Navigate to task detail view
    // For now, just log the action
    console.log('View task details:', task.id);
    // TODO: Integrate with task detail view or navigation when available
  };

  // Handler for technician marker clicks
  const handleTechnicianClick = (technician) => {
    // Technician selection functionality - popup displays automatically
    console.log('Technician clicked:', technician.technicianId);
  };

  // Handler for closing assignment modal
  const handleCloseAssignmentModal = useCallback(() => {
    if (!isAssigning) {
      setIsAssignmentModalOpen(false);
      setSelectedTask(null);
      setAssignmentError('');
      setAssignmentSuccess('');
      setHighlightedTechnicianIds([]);
    }
  }, [isAssigning]);

  // Handler for highlighting technicians on map
  const handleHighlightTechnicians = useCallback((technicianIds) => {
    setHighlightedTechnicianIds(technicianIds);
  }, []);

  // Handler for confirming assignment
  const handleConfirmAssignment = useCallback(async (taskId, technicianId) => {
    setIsAssigning(true);
    setAssignmentError('');
    setAssignmentSuccess('');

    try {
      await assignTaskToTechnician(taskId, technicianId);
      
      // Find technician name for success message
      const assignedTechnician = technicians.find(t => t.technicianId === technicianId);
      const technicianName = assignedTechnician?.name || `Technician #${technicianId}`;
      
      setAssignmentSuccess(`Task successfully assigned to ${technicianName}`);
      
      // Show notification
      setNotification({
        isVisible: true,
        message: `Task successfully assigned to ${technicianName}`,
        type: 'success',
      });

      // Close modal after short delay to show success message
      setTimeout(() => {
        setIsAssignmentModalOpen(false);
        setSelectedTask(null);
        setHighlightedTechnicianIds([]);
        setAssignmentSuccess('');
        
        // Refresh data to update map markers
        refreshTasks();
        refreshTechnicians();
      }, 1500);
    } catch (err) {
      setAssignmentError(err.message || 'Failed to assign task');
      setNotification({
        isVisible: true,
        message: err.message || 'Failed to assign task',
        type: 'error',
      });
    } finally {
      setIsAssigning(false);
    }
  }, [technicians, refreshTasks, refreshTechnicians]);

  // Handler for closing notification
  const handleCloseNotification = useCallback(() => {
    setNotification(prev => ({ ...prev, isVisible: false }));
  }, []);

  // Handler for manual refresh
  const handleRefresh = () => {
    refreshTasks();
    refreshTechnicians();
  };

  // Add highlight status to technicians
  const techniciansWithHighlight = technicians.map(technician => ({
    ...technician,
    isHighlighted: highlightedTechnicianIds.includes(technician.technicianId),
  }));

  const loading = tasksLoading || techniciansLoading;
  const error = tasksError || techniciansError;

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Location Services</h1>
        <div className="app-status">
          {loading && <span className="status-loading">Loading...</span>}
          {error && <span className="status-error">{error}</span>}
          {!loading && !error && (
            <span className="status-info">
              {tasks.length} unassigned task{tasks.length !== 1 ? 's' : ''} • {' '}
              {technicians.length} technician{technicians.length !== 1 ? 's' : ''}
            </span>
          )}
          <button 
            className="refresh-button" 
            onClick={handleRefresh} 
            disabled={loading}
            title="Refresh data"
          >
            ↻
          </button>
        </div>
      </header>
      <main className="map-container">
        <Map 
          tasks={tasks} 
          technicians={techniciansWithHighlight}
          onTaskClick={handleTaskClick}
          onAssignTask={handleAssignTask}
          onViewDetails={handleViewDetails}
          onTechnicianClick={handleTechnicianClick}
        />
      </main>

      {/* Map Assignment Modal */}
      <MapAssignmentModal
        task={selectedTask}
        technicians={technicians}
        isOpen={isAssignmentModalOpen}
        onClose={handleCloseAssignmentModal}
        onAssign={handleConfirmAssignment}
        onHighlightTechnicians={handleHighlightTechnicians}
        isAssigning={isAssigning}
        error={assignmentError}
        successMessage={assignmentSuccess}
      />

      {/* Success/Error Notification */}
      <Notification
        message={notification.message}
        type={notification.type}
        isVisible={notification.isVisible}
        onClose={handleCloseNotification}
        duration={4000}
      />
    </div>
  )
}

export default App
