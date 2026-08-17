import React, {useEffect, useState} from 'react';
import {DeviceEventEmitter, StyleSheet, View} from 'react-native';

import StatusBanner from '../../components/StatusBanner';
import type {StatusBannerVariant} from '../../components/StatusBanner';
import {QueueNavigator} from '../../native/QueueNavigator';

// Props delivered from the native host (HomeFragment_New) as initialProperties.
// All optional so the banner also renders standalone in dev; defaults match the
// "Doctor is on Break" design.
interface HomeStatusBannerProps {
  variant?: StatusBannerVariant;
  title?: string;
  subtitle?: string;
  actionLabel?: string;
}

// Native event emitted by StatusBannerUpdater when a "queue_status" FCM
// notification arrives while this banner is mounted. Keep in sync with
// StatusBannerUpdater.EVENT_STATUS_BANNER_UPDATE on the Android side.
const STATUS_BANNER_UPDATE_EVENT = 'StatusBannerUpdate';

/**
 * Home-screen status banner.
 *
 * Hosted natively via a ReactFragment (component name "StatusBannerModule")
 * inside R.id.status_banner_container, mirroring how the "Next In Queue" card
 * (QueueCardModule) is embedded. Reuses the shared StatusBanner component so the
 * home banner stays visually identical to the one on the Patient's Queue screen.
 */
export default function HomeStatusBanner(
  props: HomeStatusBannerProps,
): React.JSX.Element | null {
  // Banner content lives in state so a live FCM update can refresh it without a
  // native remount. Seeded from the initial props delivered by the host
  // fragment (which itself may be the last persisted banner payload).
  const [banner, setBanner] = useState<HomeStatusBannerProps>(props);
  const [visible, setVisible] = useState(true);

  // Re-seed if the host remounts us with fresh initial props.
  useEffect(() => {
    setBanner(props);
  }, [props]);

  // Subscribe to live banner updates pushed from native on FCM receipt. Each
  // event is a complete banner, so it replaces the current content; a new
  // update also re-shows the banner if the user had dismissed the previous one.
  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      STATUS_BANNER_UPDATE_EVENT,
      (update: HomeStatusBannerProps) => {
        setBanner(update);
        setVisible(true);
      },
    );
    return () => subscription.remove();
  }, []);

  if (!visible) {
    return null;
  }

  // Fall back to the "Doctor is on Break" defaults when a field is absent
  // (standalone/dev, or a payload that omits an optional field).
  const variant = banner.variant ?? 'alert';
  const title = banner.title ?? 'Doctor is on Break';
  const subtitle = banner.subtitle ?? 'Queue Paused';
  const actionLabel = banner.actionLabel ?? 'View Queue';

  // Hide the RN view immediately, then collapse the native container so no gap
  // is left above the Add Patient card.
  const handleDismiss = () => {
    setVisible(false);
    QueueNavigator.dismissStatusBanner();
  };

  return (
    <View style={styles.wrapper}>
      <StatusBanner
        variant={variant}
        title={title}
        subtitle={subtitle}
        actionLabel={actionLabel}
        onActionPress={() => QueueNavigator.openPatientQueue()}
        onDismiss={handleDismiss}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    // Small inset so the 1px border isn't clipped by the native container edge
    // (matches the queue_card_container's 3dp horizontal padding).
    paddingHorizontal: 3,
  },
});
