import React from 'react';
import {Image, StyleSheet, View} from 'react-native';

import StatusBanner from '../../components/StatusBanner';
import type {StatusBannerVariant} from '../../components/StatusBanner';

// Props delivered from the native host (VisitSummaryActivity_New) as
// initialProperties. All optional so the banner also renders standalone in dev;
// defaults match the "Next in Queue" design at the top of the Visit Summary.
interface VisitSummaryStatusBannerProps {
  variant?: StatusBannerVariant;
  title?: string;
  subtitle?: string;
  // Trailing pill on the right (e.g. the estimated wait, "8 Mins").
  time?: string;
}

/**
 * Visit Summary top queue banner.
 *
 * Hosted natively via a ReactFragment (component name
 * "VisitSummaryStatusBannerModule") inside R.id.vs_queue_banner_container,
 * mirroring how the home "Doctor is on Break" banner (StatusBannerModule) is
 * embedded. Reuses the shared StatusBanner component so this banner stays
 * visually identical to the ones on the Home and Patient's Queue screens.
 *
 * Unlike the home banner, this one is not dismissible and shows the estimated
 * wait as a static pill instead of a "View Queue" action.
 */
export default function VisitSummaryStatusBanner({
  variant = 'warning',
  title = 'Queue 104 · Position #2',
  subtitle = 'Next in Queue',
  time = '8 Mins',
}: VisitSummaryStatusBannerProps): React.JSX.Element {
  return (
    <View style={styles.wrapper}>
      <StatusBanner
        variant={variant}
        title={title}
        subtitle={subtitle}
        actionLabel={time}
        dismissible={false}
        // Override the variant's default arrow glyph with the amber dot
        // (ic_circle, a native vector drawable) and clear the chip background so
        // the dot shows on its own, hugging the title.
        icon={
          <Image
            source={{uri: 'ic_circle'}}
            style={styles.dot}
            resizeMode="contain"
          />
        }
        iconChipStyle={styles.dotChip}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    // Small inset so the 1px border isn't clipped by the native container edge
    // (matches the home status_banner / queue_card containers' 3dp padding).
    paddingHorizontal: 3,
  },
  dot: {
    width: 8,
    height: 8,
  },
  // Transparent, shrink-wrapped chip so the dot has no background circle and
  // sits right before the title.
  dotChip: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: 'transparent',
    marginRight: 12,
  },
});
