/**
 * Distance utility functions for calculating distances between coordinates
 */

/**
 * Earth's radius in kilometers
 */
const EARTH_RADIUS_KM = 6371;

/**
 * Earth's radius in miles
 */
const EARTH_RADIUS_MILES = 3959;

/**
 * Converts degrees to radians
 * @param {number} degrees - Angle in degrees
 * @returns {number} Angle in radians
 */
export function degreesToRadians(degrees) {
  return degrees * (Math.PI / 180);
}

/**
 * Calculates the Haversine distance between two geographic coordinates
 * Using the Haversine formula to calculate the great-circle distance
 * 
 * @param {number} lat1 - Latitude of first point
 * @param {number} lng1 - Longitude of first point
 * @param {number} lat2 - Latitude of second point
 * @param {number} lng2 - Longitude of second point
 * @param {string} unit - Unit of measurement ('km' or 'miles'), defaults to 'km'
 * @returns {number} Distance between the two points in the specified unit
 */
export function calculateHaversineDistance(lat1, lng1, lat2, lng2, unit = 'km') {
  const earthRadius = unit === 'miles' ? EARTH_RADIUS_MILES : EARTH_RADIUS_KM;
  
  const dLat = degreesToRadians(lat2 - lat1);
  const dLng = degreesToRadians(lng2 - lng1);
  
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(degreesToRadians(lat1)) * Math.cos(degreesToRadians(lat2)) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  
  return earthRadius * c;
}

/**
 * Calculates distance from a task to a technician
 * @param {Object} task - Task object with coordinates
 * @param {Object} task.coordinates - Task coordinates
 * @param {number} task.coordinates.lat - Task latitude
 * @param {number} task.coordinates.lng - Task longitude
 * @param {Object} technician - Technician object with location
 * @param {number} technician.latitude - Technician latitude
 * @param {number} technician.longitude - Technician longitude
 * @param {string} unit - Unit of measurement ('km' or 'miles'), defaults to 'km'
 * @returns {number|null} Distance in the specified unit, or null if coordinates are invalid
 */
export function calculateTaskToTechnicianDistance(task, technician, unit = 'km') {
  if (!task?.coordinates?.lat || !task?.coordinates?.lng) {
    return null;
  }
  
  if (technician?.latitude == null || technician?.longitude == null) {
    return null;
  }
  
  return calculateHaversineDistance(
    task.coordinates.lat,
    task.coordinates.lng,
    technician.latitude,
    technician.longitude,
    unit
  );
}

/**
 * Calculates distances from a task to all technicians and sorts by distance
 * @param {Object} task - Task object with coordinates
 * @param {Array} technicians - Array of technician objects with location data
 * @param {string} unit - Unit of measurement ('km' or 'miles'), defaults to 'km'
 * @returns {Array} Array of technicians with distance property, sorted by distance (nearest first)
 */
export function getTechniciansWithDistanceFromTask(task, technicians, unit = 'km') {
  if (!task || !Array.isArray(technicians)) {
    return [];
  }
  
  return technicians
    .map(technician => {
      const distance = calculateTaskToTechnicianDistance(task, technician, unit);
      return {
        ...technician,
        distance,
        distanceUnit: unit,
      };
    })
    .filter(technician => technician.distance !== null)
    .sort((a, b) => a.distance - b.distance);
}

/**
 * Formats distance for display
 * @param {number} distance - Distance value
 * @param {string} unit - Unit of measurement ('km' or 'miles')
 * @param {number} decimals - Number of decimal places (default: 1)
 * @returns {string} Formatted distance string
 */
export function formatDistance(distance, unit = 'km', decimals = 1) {
  if (distance === null || distance === undefined) {
    return 'N/A';
  }
  
  const formatted = distance.toFixed(decimals);
  return `${formatted} ${unit}`;
}

/**
 * Default radius for "nearby" technicians in kilometers
 */
export const DEFAULT_NEARBY_RADIUS_KM = 10;

/**
 * Filters technicians to only include those within a certain radius of a task
 * @param {Object} task - Task object with coordinates
 * @param {Array} technicians - Array of technician objects with location data
 * @param {number} radiusKm - Radius in kilometers (default: 10)
 * @returns {Array} Array of nearby technicians with distance property, sorted by distance
 */
export function getNearbyTechnicians(task, technicians, radiusKm = DEFAULT_NEARBY_RADIUS_KM) {
  const techniciansWithDistance = getTechniciansWithDistanceFromTask(task, technicians, 'km');
  return techniciansWithDistance.filter(technician => technician.distance <= radiusKm);
}
