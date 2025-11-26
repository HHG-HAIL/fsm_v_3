import DraggableTaskMarker from './DraggableTaskMarker';
import './TaskMarker.css';
import './DragOverlay.css';

/**
 * TaskMarkersLayer component that renders multiple draggable task markers on the map
 * @param {Object} props - Component props
 * @param {Array} props.tasks - Array of task objects with coordinates
 * @param {function} props.onTaskClick - Optional handler called when a task marker is clicked
 * @param {function} props.onAssignTask - Optional handler for assigning a task
 * @param {function} props.onViewDetails - Optional handler for viewing task details
 * @param {function} props.onDragStart - Optional handler when drag starts
 * @param {function} props.onDrag - Optional handler during drag
 * @param {function} props.onDragEnd - Optional handler when drag ends
 * @param {Array} props.technicians - Array of technician objects for drop zone detection
 * @param {number} props.dropRadius - Radius in pixels for drop zone detection (default: 50)
 */
const TaskMarkersLayer = ({ 
  tasks = [], 
  onTaskClick, 
  onAssignTask, 
  onViewDetails,
  onDragStart,
  onDrag,
  onDragEnd,
  technicians = [],
  dropRadius = 50,
}) => {
  if (!Array.isArray(tasks) || tasks.length === 0) {
    return null;
  }

  return (
    <>
      {tasks.map((task) => (
        <DraggableTaskMarker
          key={task.id}
          task={task}
          onClick={onTaskClick}
          onAssignTask={onAssignTask}
          onViewDetails={onViewDetails}
          onDragStart={onDragStart}
          onDrag={onDrag}
          onDragEnd={onDragEnd}
          technicians={technicians}
          dropRadius={dropRadius}
        />
      ))}
    </>
  );
};

export default TaskMarkersLayer;
