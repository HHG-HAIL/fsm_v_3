import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskReassignmentModal from './TaskReassignmentModal';
import * as taskApi from '../services/taskApi';

// Mock the taskApi module
vi.mock('../services/taskApi', () => ({
  getTechnicians: vi.fn(),
  reassignTask: vi.fn(),
}));

describe('TaskReassignmentModal', () => {
  const mockAssignedTask = {
    id: 1,
    title: 'Test Task',
    status: 'ASSIGNED',
    assignedTechnicianId: 1,
    assignedTechnician: 'John Doe',
  };

  const mockInProgressTask = {
    id: 2,
    title: 'In Progress Task',
    status: 'IN_PROGRESS',
    assignedTechnicianId: 2,
    assignedTechnician: 'Jane Smith',
  };

  const mockTechnicians = [
    { id: 1, name: 'John Doe', email: 'john@example.com', workload: 3, status: 'ACTIVE', role: 'TECHNICIAN' },
    { id: 2, name: 'Jane Smith', email: 'jane@example.com', workload: 7, status: 'ACTIVE', role: 'TECHNICIAN' },
    { id: 3, name: 'Bob Wilson', email: 'bob@example.com', workload: 12, status: 'ACTIVE', role: 'TECHNICIAN' },
    { id: 4, name: 'Alice Brown', email: 'alice@example.com', workload: 2, status: 'ACTIVE', role: 'TECHNICIAN' },
  ];

  let mockOnClose;
  let mockOnReassignmentComplete;

  beforeEach(() => {
    mockOnClose = vi.fn();
    mockOnReassignmentComplete = vi.fn();
    vi.clearAllMocks();
    taskApi.getTechnicians.mockResolvedValue(mockTechnicians);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Component Rendering', () => {
    it('does not render when isOpen is false', () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={false}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('renders when isOpen is true', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      expect(screen.getByRole('dialog')).toBeInTheDocument();
      expect(screen.getByText('Reassign Task')).toBeInTheDocument();
    });

    it('displays loading state initially', async () => {
      taskApi.getTechnicians.mockImplementation(() => new Promise(() => {}));

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      expect(screen.getByText('Loading technicians...')).toBeInTheDocument();
    });

    it('displays task information', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Test Task')).toBeInTheDocument();
        expect(screen.getByText('ASSIGNED')).toBeInTheDocument();
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });
    });

    it('displays technician list after loading, excluding current technician', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        // John Doe (current technician) should be excluded
        const technicianOptions = screen.getAllByRole('option');
        expect(technicianOptions).toHaveLength(3);
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
        expect(screen.getByText('Bob Wilson')).toBeInTheDocument();
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });
    });

    it('sorts technicians by workload (least loaded first)', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        const listItems = screen.getAllByRole('option');
        expect(listItems[0]).toHaveTextContent('Alice Brown');
        expect(listItems[1]).toHaveTextContent('Jane Smith');
        expect(listItems[2]).toHaveTextContent('Bob Wilson');
      });
    });

    it('displays warning icon for technicians at capacity (>=10 tasks)', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        const listItems = screen.getAllByRole('option');
        // Bob Wilson (12 tasks) should have warning
        expect(listItems[2]).toHaveTextContent('At capacity');
      });
    });

    it('displays empty state when no other technicians available', async () => {
      // Return only the current technician
      taskApi.getTechnicians.mockResolvedValue([mockTechnicians[0]]);

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('No other technicians available for reassignment')).toBeInTheDocument();
      });
    });

    it('displays error message when loading fails', async () => {
      taskApi.getTechnicians.mockRejectedValue(new Error('Network error'));

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Network error')).toBeInTheDocument();
      });
    });
  });

  describe('IN_PROGRESS Task Handling', () => {
    it('displays reason input field for IN_PROGRESS tasks', async () => {
      render(
        <TaskReassignmentModal
          task={mockInProgressTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByLabelText(/Reason for Reassignment/i)).toBeInTheDocument();
      });
    });

    it('does not display reason input field for ASSIGNED tasks', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      expect(screen.queryByLabelText(/Reason for Reassignment/i)).not.toBeInTheDocument();
    });

    it('shows error when trying to continue without reason for IN_PROGRESS task', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockInProgressTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      // Select a technician
      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      // Try to continue without entering a reason
      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      // Should show error
      await waitFor(() => {
        expect(screen.getByText('Reason is required for reassigning tasks that are in progress')).toBeInTheDocument();
      });
    });

    it('shows warning dialog when continuing with reason for IN_PROGRESS task', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockInProgressTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      // Select a technician
      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      // Enter reason
      const reasonInput = screen.getByLabelText(/Reason for Reassignment/i);
      await user.type(reasonInput, 'Technician is on leave');

      // Continue
      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      // Should show warning dialog
      await waitFor(() => {
        expect(screen.getByText('Warning: In Progress Task')).toBeInTheDocument();
        expect(screen.getByText(/This task is currently/i)).toBeInTheDocument();
      });
    });

    it('allows proceeding from warning to confirmation', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockInProgressTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      // Select a technician
      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      // Enter reason
      const reasonInput = screen.getByLabelText(/Reason for Reassignment/i);
      await user.type(reasonInput, 'Technician is on leave');

      // Continue to warning
      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      // Proceed from warning
      const proceedButton = screen.getByRole('button', { name: /Proceed Anyway/i });
      await user.click(proceedButton);

      // Should show confirmation
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: 'Confirm Reassignment' })).toBeInTheDocument();
      });
    });
  });

  describe('Technician Selection', () => {
    it('highlights selected technician', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      expect(aliceItem).toHaveClass('selected');
    });

    it('enables Continue button when technician is selected', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      expect(continueButton).toBeDisabled();

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      expect(continueButton).not.toBeDisabled();
    });

    it('allows selecting technician using keyboard', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      aliceItem.focus();
      fireEvent.keyDown(aliceItem, { key: 'Enter' });

      expect(aliceItem).toHaveClass('selected');
    });
  });

  describe('Confirmation View', () => {
    it('shows confirmation view when Continue is clicked (ASSIGNED task)', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      expect(screen.getByRole('heading', { name: 'Confirm Reassignment' })).toBeInTheDocument();
      expect(screen.getByText('Test Task')).toBeInTheDocument();
    });

    it('displays workload warning in confirmation for at-capacity technicians', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Bob Wilson')).toBeInTheDocument();
      });

      const bobItem = screen.getByText('Bob Wilson').closest('[role="option"]');
      await user.click(bobItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      expect(screen.getByText(/This technician is at or exceeds capacity/)).toBeInTheDocument();
    });

    it('allows going back from confirmation', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const backButton = screen.getByRole('button', { name: /Back/i });
      await user.click(backButton);

      expect(screen.getByText('Reassign Task')).toBeInTheDocument();
      expect(screen.getByRole('listbox')).toBeInTheDocument();
    });
  });

  describe('Reassignment Process', () => {
    it('calls reassignTask with correct parameters (ASSIGNED task)', async () => {
      const user = userEvent.setup();
      taskApi.reassignTask.mockResolvedValue({ 
        assignmentId: 1,
        assignmentHistory: [] 
      });

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Reassignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(taskApi.reassignTask).toHaveBeenCalledWith(1, 4, null);
      });
    });

    it('calls reassignTask with reason for IN_PROGRESS task', async () => {
      const user = userEvent.setup();
      taskApi.reassignTask.mockResolvedValue({ 
        assignmentId: 1,
        assignmentHistory: [] 
      });

      render(
        <TaskReassignmentModal
          task={mockInProgressTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      // Select a technician
      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      // Enter reason
      const reasonInput = screen.getByLabelText(/Reason for Reassignment/i);
      await user.type(reasonInput, 'Technician is unavailable');

      // Continue to warning
      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      // Proceed from warning
      const proceedButton = screen.getByRole('button', { name: /Proceed Anyway/i });
      await user.click(proceedButton);

      // Confirm
      const confirmButton = screen.getByRole('button', { name: /Confirm Reassignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(taskApi.reassignTask).toHaveBeenCalledWith(2, 4, 'Technician is unavailable');
      });
    });

    it('shows loading state during reassignment', async () => {
      const user = userEvent.setup();
      let resolveReassign;
      taskApi.reassignTask.mockImplementation(() => new Promise((resolve) => {
        resolveReassign = resolve;
      }));

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Reassignment/i });
      await user.click(confirmButton);

      expect(screen.getByRole('button', { name: /Reassigning.../i })).toBeDisabled();

      resolveReassign({ assignmentId: 1, assignmentHistory: [] });
    });

    it('displays success message after reassignment', async () => {
      const user = userEvent.setup();
      taskApi.reassignTask.mockResolvedValue({ 
        assignmentId: 1,
        assignmentHistory: [] 
      });

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Reassignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(screen.getByText('Task successfully reassigned to Alice Brown')).toBeInTheDocument();
      });
    });

    it('displays error message when reassignment fails', async () => {
      const user = userEvent.setup();
      taskApi.reassignTask.mockRejectedValue(new Error('Reassignment failed'));

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Reassignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(screen.getByText('Reassignment failed')).toBeInTheDocument();
      });
    });

    it('goes back to selection view when reassignment fails', async () => {
      const user = userEvent.setup();
      taskApi.reassignTask.mockRejectedValue(new Error('Reassignment failed'));

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Reassignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(screen.getByText('Reassign Task')).toBeInTheDocument();
        expect(screen.getByRole('listbox')).toBeInTheDocument();
      });
    });
  });

  describe('Modal Interactions', () => {
    it('calls onClose when Cancel button is clicked', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const cancelButton = screen.getByRole('button', { name: /Cancel/i });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('calls onClose when close button is clicked', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const closeButton = screen.getByLabelText('Close modal');
      await user.click(closeButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('calls onClose when clicking overlay', async () => {
      const user = userEvent.setup();

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const overlay = screen.getByRole('dialog');
      await user.click(overlay);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('calls onClose when Escape key is pressed', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('does not close modal while reassignment is in progress', async () => {
      const user = userEvent.setup();
      taskApi.reassignTask.mockImplementation(() => new Promise(() => {}));

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Reassignment/i });
      await user.click(confirmButton);

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('has proper dialog role and aria attributes', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-modal', 'true');
      expect(dialog).toHaveAttribute('aria-labelledby', 'modal-title');
    });

    it('has proper listbox role for technician list', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        const listbox = screen.getByRole('listbox');
        expect(listbox).toHaveAttribute('aria-label', 'Available technicians');
      });
    });

    it('has proper option role for technician items', async () => {
      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        const options = screen.getAllByRole('option');
        expect(options).toHaveLength(3);
      });
    });

    it('uses role="alert" for error messages', async () => {
      taskApi.getTechnicians.mockRejectedValue(new Error('Test error'));

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        const error = screen.getByRole('alert');
        expect(error).toHaveTextContent('Test error');
      });
    });

    it('uses role="status" for success messages', async () => {
      const user = userEvent.setup();
      taskApi.reassignTask.mockResolvedValue({ 
        assignmentId: 1,
        assignmentHistory: [] 
      });

      render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Reassignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        const success = screen.getByRole('status');
        expect(success).toHaveTextContent('Task successfully reassigned to Alice Brown');
      });
    });
  });

  describe('State Reset', () => {
    it('resets state when modal is reopened', async () => {
      const user = userEvent.setup();
      const { rerender } = render(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Alice Brown')).toBeInTheDocument();
      });

      // Select a technician
      const aliceItem = screen.getByText('Alice Brown').closest('[role="option"]');
      await user.click(aliceItem);

      // Go to confirmation
      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      // Close modal
      rerender(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={false}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      // Reopen modal
      rerender(
        <TaskReassignmentModal
          task={mockAssignedTask}
          isOpen={true}
          onClose={mockOnClose}
          onReassignmentComplete={mockOnReassignmentComplete}
        />
      );

      // Should be back to selection view, not confirmation
      await waitFor(() => {
        expect(screen.getByText('Reassign Task')).toBeInTheDocument();
        expect(screen.getByRole('listbox')).toBeInTheDocument();
      });
    });
  });
});
