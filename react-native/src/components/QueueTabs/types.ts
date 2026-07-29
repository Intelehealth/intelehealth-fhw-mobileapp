import type { StyleProp, ViewStyle } from 'react-native';

import type { QueueStatus } from '../QueueListItem';

/**
 * Types for the QueueTabs component — the All / Next / Waiting / On Call filter
 * strip above the queue list.
 */

// The active filter. 'all' shows everything; the rest map 1:1 onto a
// QueueListItem status, so filtering is a plain `item.status === filter` check.
export type QueueFilter = 'all' | QueueStatus;

// A single tab definition.
export interface QueueTab {
  key: QueueFilter;
  label: string;
}

export interface QueueTabsProps {
  // Currently selected filter.
  activeKey: QueueFilter;
  // Fired with the tapped tab's key.
  onChange: (key: QueueFilter) => void;
  // Optional override of the tab set / labels. Defaults to
  // All / Next / Waiting / On Call.
  tabs?: QueueTab[];
  // Optional style override for the outer container.
  style?: StyleProp<ViewStyle>;
}
