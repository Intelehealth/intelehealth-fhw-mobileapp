import type { StyleProp, ViewStyle } from 'react-native';

/**
 * Types for the QueueListItem component.
 *
 * A single row rendered inside the Patient Queue list. Its surface, border and
 * status badge change based on `status`, and the footer metric switches between
 * "Duration" (a patient already On Call) and "Wait time" (everyone else).
 */

// Drives the whole visual variant of the card.
export type QueueStatus = 'onCall' | 'nextInQueue' | 'waiting';

export interface QueueListItemProps {
  // Queue token, e.g. "Q-104".
  queueNumber: string;
  // Patient display name, e.g. "Anthony G".
  patientName: string;
  // Short gender marker, e.g. "M" / "F".
  gender: string;
  // Patient age in years.
  age: number;
  // Patient identifier, e.g. "ID-987654jK".
  patientId: string;
  // Presenting symptoms; extras collapse into a "+N More" pill.
  symptoms: string[];
  // Position in the queue (shown as "#2").
  position: number;
  // Visual + semantic state of the row.
  status: QueueStatus;
  // Pre-formatted time string, e.g. "04:32". Rendered as the elapsed call
  // "Duration" for onCall, otherwise as the queue "Wait time".
  time: string;
  // Optional avatar image URL.
  avatarUrl?: string;
  // Fired when the row is tapped.
  onPress?: () => void;
  // Optional style override for the outer card container.
  style?: StyleProp<ViewStyle>;
}
