import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DragConfirmationModal from './DragConfirmationModal';

// Mock the services
vi.mock('../services/technicianService', () => ({
  TechnicianStatus: {
    AVAILABLE: 'available',
    BUSY: 'busy',
    OFFLINE: 'offline',
  },
  getStatusLabel: (status) => {
    switch (status) {
      case 'available': return 'Available';
      case 'busy': return 'Busy';
      case 'offline': return 'Offline';
      default: return 'Unknown';
    }
  },
  getStatusColor: (status) => {
    switch (status) {
      case 'available': return '#22c55e';
      case 'busy': return '#f59e0b';
      case 'offline': return '#6b7280';
      default: return '#6b7280';
    }
  },
}));

// Mock distance utils
vi.mock('../utils/distanceUtils', () => ({
  calculateTaskToTechnicianDistance: () => 2.5,
  formatDistance: (distance, unit) => `${distance.toFixed(1)} ${unit}`,
}));

describe('DragConfirmationModal', () => {
  const mockTask = {
    id: 1,
    title: 'Fix Water Leak',
    clientAddress: '123 Main St',
    priority: 'HIGH',
    coordinates: { lat: 37.7749, lng: -122.4194 },
  };

  const mockTechnician = {
    technicianId: 101,
    name: 'John Smith',
    status: 'available',
    latitude: 37.7850,
    longitude: -122.4090,
    workload: 3,
  };

  const defaultProps = {
    task: mockTask,
    technician: mockTechnician,
    isOpen: true,
    onClose: vi.fn(),
    onConfirm: vi.fn(),
    isAssigning: false,
    error: '',
    successMessage: '',
  };

  describe('Rendering', () => {
    it('renders nothing when isOpen is false', () => {
      render(<DragConfirmationModal {...defaultProps} isOpen={false} />);
      
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('renders nothing when task is null', () => {
      render(<DragConfirmationModal {...defaultProps} task={null} />);
      
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('renders nothing when technician is null', () => {
      render(<DragConfirmationModal {...defaultProps} technician={null} />);
      
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('renders the modal when open with valid data', () => {
      render(<DragConfirmationModal {...defaultProps} />);
      
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    it('displays the modal title', () => {
      render(<DragConfirmationModal {...defaultProps} />);
      
      expect(screen.getByText('Confirm Drag-and-Drop Assignment')).toBeInTheDocument();
    });

    it('displays the task title', () => {
      render(<DragConfirmationModal {...defaultProps} />);
      
      expect(screen.getByText(mockTask.title)).toBeInTheDocument();
    });

    it('displays the task address', () => {
      render(<DragConfirmationModal {...defaultProps} />);
      
      expect(screen.getByText(mockTask.clientAddress)).toBeInTheDocument();
    });

    it('displays the technician name', () => {
      render(<DragConfirmationModal {...defaultProps} />);
      
      expect(screen.getByText(mockTechnician.name)).toBeInTheDocument();
    });

    it('displays the technician status', () => {
      render(<DragConfirmationModal {...defaultProps} />);
      
      expect(screen.getByText('Available')).toBeInTheDocument();
    });

    it('displays confirm and cancel buttons', () => {
      render(<DragConfirmationModal {...defaultProps} />);
      
      expect(screen.getByRole('button', { name: /confirm assignment/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
    });
  });

  describe('Error and Success Messages', () => {
    it('displays error message when provided', () => {
      render(<DragConfirmationModal {...defaultProps} error="Assignment failed" />);
      
      expect(screen.getByRole('alert')).toHaveTextContent('Assignment failed');
    });

    it('displays success message when provided', () => {
      render(<DragConfirmationModal {...defaultProps} successMessage="Task assigned successfully" />);
      
      expect(screen.getByRole('status')).toHaveTextContent('Task assigned successfully');
    });
  });

  describe('Warnings', () => {
    it('shows warning when technician is busy', () => {
      const busyTechnician = { ...mockTechnician, status: 'busy' };
      render(<DragConfirmationModal {...defaultProps} technician={busyTechnician} />);
      
      expect(screen.getByText(/currently busy/i)).toBeInTheDocument();
    });

    it('shows warning when technician is at capacity', () => {
      const atCapacityTechnician = { ...mockTechnician, workload: 10 };
      render(<DragConfirmationModal {...defaultProps} technician={atCapacityTechnician} />);
      
      expect(screen.getByText(/at or exceeds capacity/i)).toBeInTheDocument();
    });
  });

  describe('User Interactions', () => {
    it('calls onConfirm when confirm button is clicked', async () => {
      const user = userEvent.setup();
      const onConfirm = vi.fn();
      
      render(<DragConfirmationModal {...defaultProps} onConfirm={onConfirm} />);
      
      await user.click(screen.getByRole('button', { name: /confirm assignment/i }));
      
      expect(onConfirm).toHaveBeenCalledWith(mockTask.id, mockTechnician.technicianId);
    });

    it('calls onClose when cancel button is clicked', async () => {
      const user = userEvent.setup();
      const onClose = vi.fn();
      
      render(<DragConfirmationModal {...defaultProps} onClose={onClose} />);
      
      await user.click(screen.getByRole('button', { name: /cancel/i }));
      
      expect(onClose).toHaveBeenCalled();
    });

    it('calls onClose when close button is clicked', async () => {
      const user = userEvent.setup();
      const onClose = vi.fn();
      
      render(<DragConfirmationModal {...defaultProps} onClose={onClose} />);
      
      await user.click(screen.getByLabelText('Close modal'));
      
      expect(onClose).toHaveBeenCalled();
    });

    it('does not call onClose when inner container is clicked', async () => {
      const user = userEvent.setup();
      const onClose = vi.fn();
      
      render(<DragConfirmationModal {...defaultProps} onClose={onClose} />);
      
      // Click on the container (not the overlay background)
      const container = document.querySelector('.drag-confirmation-modal-container');
      await user.click(container);
      
      // onClose should NOT be called when clicking inside the modal
      expect(onClose).not.toHaveBeenCalled();
    });
  });

  describe('Loading State', () => {
    it('shows loading text when isAssigning is true', () => {
      render(<DragConfirmationModal {...defaultProps} isAssigning={true} />);
      
      expect(screen.getByRole('button', { name: /assigning/i })).toBeInTheDocument();
    });

    it('disables confirm button when isAssigning is true', () => {
      render(<DragConfirmationModal {...defaultProps} isAssigning={true} />);
      
      expect(screen.getByRole('button', { name: /assigning/i })).toBeDisabled();
    });

    it('disables cancel button when isAssigning is true', () => {
      render(<DragConfirmationModal {...defaultProps} isAssigning={true} />);
      
      expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
    });

    it('disables close button when isAssigning is true', () => {
      render(<DragConfirmationModal {...defaultProps} isAssigning={true} />);
      
      expect(screen.getByLabelText('Close modal')).toBeDisabled();
    });

    it('does not call onClose when cancel clicked during assignment', () => {
      render(<DragConfirmationModal {...defaultProps} isAssigning={true} />);
      
      // Button is disabled so we can't click it normally, but let's verify the handler
      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      expect(cancelButton).toBeDisabled();
    });
  });
});
