import { useState } from 'react'
import './App.css'
import TaskCreationForm from './components/TaskCreationForm'
import TechnicianAssignmentModal from './components/TechnicianAssignmentModal'
import TaskReassignmentModal from './components/TaskReassignmentModal'

function App() {
  const [showAssignmentModal, setShowAssignmentModal] = useState(false);
  const [showReassignmentModal, setShowReassignmentModal] = useState(false);
  const [selectedTask, setSelectedTask] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);

  // Demo tasks for showcase
  const demoTasks = [
    { id: 1, title: 'HVAC Repair - Commercial Building', status: 'UNASSIGNED', priority: 'HIGH' },
    { id: 2, title: 'Electrical Inspection', status: 'ASSIGNED', priority: 'MEDIUM', assignedTechnicianId: 1, assignedTechnician: 'John Doe' },
    { id: 3, title: 'Plumbing Service Call', status: 'IN_PROGRESS', priority: 'URGENT', assignedTechnicianId: 2, assignedTechnician: 'Jane Smith' },
  ];

  const handleAssignClick = (task) => {
    setSelectedTask(task);
    setShowAssignmentModal(true);
  };

  const handleReassignClick = (task) => {
    setSelectedTask(task);
    setShowReassignmentModal(true);
  };

  const handleAssignmentComplete = (result) => {
    console.log('Task assigned successfully:', result);
    // In a real app, this would refresh the task list
  };

  const handleReassignmentComplete = (result) => {
    console.log('Task reassigned successfully:', result);
    // In a real app, this would refresh the task list
  };

  const handleCreateSuccess = () => {
    setShowCreateForm(false);
    console.log('Task created successfully, would refresh task list');
  };

  const handleCreateCancel = () => {
    setShowCreateForm(false);
    console.log('Task creation cancelled');
  };

  const canReassign = (task) => {
    return task.status === 'ASSIGNED' || task.status === 'IN_PROGRESS';
  };

  if (showCreateForm) {
    return <TaskCreationForm onSuccess={handleCreateSuccess} onCancel={handleCreateCancel} />;
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Task Management</h1>
        <button className="create-task-button" onClick={() => setShowCreateForm(true)}>
          + Create Task
        </button>
      </header>

      <main className="task-list-container">
        <h2>Tasks</h2>
        <div className="task-list">
          {demoTasks.map((task) => (
            <div key={task.id} className={`task-item priority-${task.priority.toLowerCase()}`}>
              <div className="task-info">
                <span className="task-id">#{task.id}</span>
                <h3 className="task-title">{task.title}</h3>
                <span className={`task-status ${task.status.toLowerCase().replace('_', '-')}`}>
                  {task.status.replace('_', ' ')}
                </span>
                <span className={`task-priority ${task.priority.toLowerCase()}`}>
                  {task.priority}
                </span>
                {task.assignedTechnician && (
                  <span className="task-technician">Assigned to: {task.assignedTechnician}</span>
                )}
              </div>
              <div className="task-actions">
                {task.status === 'UNASSIGNED' && (
                  <button
                    className="assign-button"
                    onClick={() => handleAssignClick(task)}
                  >
                    Assign Task
                  </button>
                )}
                {canReassign(task) && (
                  <button
                    className="reassign-button"
                    onClick={() => handleReassignClick(task)}
                  >
                    Reassign
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      </main>

      <TechnicianAssignmentModal
        task={selectedTask}
        isOpen={showAssignmentModal}
        onClose={() => setShowAssignmentModal(false)}
        onAssignmentComplete={handleAssignmentComplete}
      />

      <TaskReassignmentModal
        task={selectedTask}
        isOpen={showReassignmentModal}
        onClose={() => setShowReassignmentModal(false)}
        onReassignmentComplete={handleReassignmentComplete}
      />
    </div>
  )
}

export default App
