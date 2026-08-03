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

  /** Finish the Queue Details Activity (back navigation). */
  close(): void {
    Native?.close();
  },
};
