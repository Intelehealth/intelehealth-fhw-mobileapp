import React, {useState} from 'react';
import {StyleSheet, View} from 'react-native';

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

/**
 * Home-screen status banner.
 *
 * Hosted natively via a ReactFragment (component name "StatusBannerModule")
 * inside R.id.status_banner_container, mirroring how the "Next In Queue" card
 * (QueueCardModule) is embedded. Reuses the shared StatusBanner component so the
 * home banner stays visually identical to the one on the Patient's Queue screen.
 */
export default function HomeStatusBanner({
  variant = 'alert',
  title = 'Doctor is on Break',
  subtitle = 'Queue Paused',
  actionLabel = 'View Queue',
}: HomeStatusBannerProps): React.JSX.Element | null {
  const [visible, setVisible] = useState(true);
  if (!visible) {
    return null;
  }

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
