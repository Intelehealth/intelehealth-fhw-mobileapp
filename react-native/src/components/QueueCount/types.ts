import type { StyleProp, ViewStyle } from 'react-native';

/**
 * Props for the QueueCount component ("Patients in Queue : 7").
 */
export interface QueueCountProps {
  // Number of patients currently in the queue.
  count: number;
  // Optional style override for the outer container.
  style?: StyleProp<ViewStyle>;
}
