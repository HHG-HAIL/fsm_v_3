import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import DraggableTaskMarker from './DraggableTaskMarker';

// Mock react-leaflet
vi.mock('react-leaflet', () => ({
  Marker: ({ children, position, draggable, eventHandlers }) => (
    <div 
      data-testid="marker"
      data-position={JSON.stringify(position)}
      data-draggable={draggable}
      onClick={eventHandlers?.click}
      onDragStart={eventHandlers?.dragstart}
      onDrag={eventHandlers?.drag}
      onDragEnd={eventHandlers?.dragend}
    >
      {children}
    </div>
  ),
  Popup: ({ children }) => (
    <div data-testid="popup">
      {children}
    </div>
  ),
  useMap: () => ({
    latLngToContainerPoint: () => ({ x: 100, y: 100 }),
    containerPointToLatLng: () => ({ lat: 37.7749, lng: -122.4194 }),
    distance: () => 100,
  }),
}));

// Mock services
vi.mock('../services/taskService', () => ({
  getPriorityColor: (priority) => {
    switch (priority) {
      case 'HIGH': return '#ef4444';
      case 'MEDIUM': return '#f59e0b';
      case 'LOW': return '#22c55e';
      default: return '#6b7280';
    }
  },
  getPriorityLabel: (priority) => {
    switch (priority) {
      case 'HIGH': return 'High';
      case 'MEDIUM': return 'Medium';
      case 'LOW': return 'Low';
      default: return 'Unknown';
    }
  },
  getPriorityTextColor: () => '#ffffff',
}));

// Mock markerUtils
vi.mock('../utils/markerUtils', () => ({
  createTaskMarkerIcon: () => ({}),
}));

describe('DraggableTaskMarker', () => {
  const mockTask = {
    id: 1,
    title: 'Fix Water Leak',
    description: 'Water leak in the kitchen sink needs repair',
    clientAddress: '123 Main St, San Francisco, CA',
    priority: 'HIGH',
    estimatedDuration: 60,
    coordinates: { lat: 37.7749, lng: -122.4194 },
  };

  const defaultProps = {
    task: mockTask,
    onClick: vi.fn(),
    onAssignTask: vi.fn(),
    onViewDetails: vi.fn(),
    onDragStart: vi.fn(),
    onDrag: vi.fn(),
    onDragEnd: vi.fn(),
    technicians: [],
    dropRadius: 50,
  };

  describe('Rendering', () => {
    it('renders nothing when task is null', () => {
      const { container } = render(<DraggableTaskMarker {...defaultProps} task={null} />);
      
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when task has no coordinates', () => {
      const taskWithoutCoords = { ...mockTask, coordinates: null };
      const { container } = render(<DraggableTaskMarker {...defaultProps} task={taskWithoutCoords} />);
      
      expect(container.firstChild).toBeNull();
    });

    it('renders marker with correct position', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      const marker = screen.getByTestId('marker');
      const position = JSON.parse(marker.getAttribute('data-position'));
      expect(position).toEqual([mockTask.coordinates.lat, mockTask.coordinates.lng]);
    });

    it('renders marker as draggable', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      const marker = screen.getByTestId('marker');
      expect(marker.getAttribute('data-draggable')).toBe('true');
    });

    it('renders popup with task title', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      expect(screen.getByText(mockTask.title)).toBeInTheDocument();
    });

    it('renders popup with task address', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      expect(screen.getByText(mockTask.clientAddress)).toBeInTheDocument();
    });

    it('renders popup with task priority', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      expect(screen.getByText('High Priority')).toBeInTheDocument();
    });

    it('renders popup with task duration', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      expect(screen.getByText(`${mockTask.estimatedDuration} min`)).toBeInTheDocument();
    });

    it('renders popup with description', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      expect(screen.getByText(mockTask.description)).toBeInTheDocument();
    });

    it('renders assign button in popup', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      expect(screen.getByRole('button', { name: /assign task/i })).toBeInTheDocument();
    });

    it('renders view details button in popup', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      expect(screen.getByRole('button', { name: /view details/i })).toBeInTheDocument();
    });

    it('renders drag hint in popup', () => {
      render(<DraggableTaskMarker {...defaultProps} />);
      
      expect(screen.getByText(/drag this marker/i)).toBeInTheDocument();
    });
  });

  describe('Description truncation', () => {
    it('truncates long descriptions', () => {
      const longDescription = 'A'.repeat(150);
      const taskWithLongDesc = { ...mockTask, description: longDescription };
      render(<DraggableTaskMarker {...defaultProps} task={taskWithLongDesc} />);
      
      // Should be truncated to 100 chars + '...'
      const descriptionElement = screen.getByText(/^A+\.\.\.$/);
      expect(descriptionElement).toBeInTheDocument();
    });

    it('does not truncate short descriptions', () => {
      const shortDescription = 'Short description';
      const taskWithShortDesc = { ...mockTask, description: shortDescription };
      render(<DraggableTaskMarker {...defaultProps} task={taskWithShortDesc} />);
      
      expect(screen.getByText(shortDescription)).toBeInTheDocument();
    });
  });

  describe('Optional fields', () => {
    it('does not render duration when not provided', () => {
      const taskWithoutDuration = { ...mockTask, estimatedDuration: null };
      render(<DraggableTaskMarker {...defaultProps} task={taskWithoutDuration} />);
      
      expect(screen.queryByText(/min$/)).not.toBeInTheDocument();
    });

    it('does not render description when not provided', () => {
      const taskWithoutDesc = { ...mockTask, description: null };
      render(<DraggableTaskMarker {...defaultProps} task={taskWithoutDesc} />);
      
      // The popup should still render, just without description
      expect(screen.getByText(mockTask.title)).toBeInTheDocument();
    });
  });
});
