import { NativeModules } from 'react-native';

import type { QueueListItemProps } from '../components/QueueListItem';

/**
 * JS wrapper over the native `QueueNavigator` module (Android).
 *
 * Queue Details is hosted as a standalone Activity (not a fragment), so opening
 * it means launching that Activity natively and forwarding the tapped row as
 * initial properties. `close()` finishes the Activity for back navigation.
 */
const { QueueNavigator: Native } = NativeModules;

export const QueueNavigator = {
  /** Launch the Queue Details Activity for the tapped queue row. */
  openQueueDetails(item: QueueListItemProps): void {
    Native?.openQueueDetails(item);
  },

  /**
   * Open the Patient's Queue screen from the home screen (used by the home
   * status banner's "View Queue" and the queue card's "Open Queue" buttons).
   */
  openPatientQueue(): void {
    Native?.openPatientQueue();
  },

  /**
   * Collapse the home status-banner container after its banner is dismissed, so
   * the freed space doesn't leave a gap above the Add Patient card.
   */
  dismissStatusBanner(): void {
    Native?.dismissStatusBanner();
  },

  /** Finish the Queue Details Activity (back navigation). */
  close(): void {
    Native?.close();
  },
};
