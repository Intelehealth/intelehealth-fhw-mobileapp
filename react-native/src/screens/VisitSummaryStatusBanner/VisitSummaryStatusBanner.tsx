import React, {useEffect, useState} from 'react';
import {AppState, Image, StyleSheet, View} from 'react-native';

import StatusBanner from '../../components/StatusBanner';
import type {StatusBannerVariant} from '../../components/StatusBanner';
import type {QueueStatus} from '../../components/QueueListItem';
import {Colors, FontFamily, FontWeight} from '../../theme';

// Milliseconds -> "N Mins" (design format), clamped at zero. "1 Min" singular.
function formatMins(ms: number): string {
  const minutes = Math.max(0, Math.round(ms / 60000));
  return `${minutes} ${minutes === 1 ? 'Min' : 'Mins'}`;
}

/**
 * Same source instants as QueueListItem: onCall counts UP from `connectedAt`,
 * next/waiting counts DOWN to `etaAt`. Shown in minutes ("8 Mins") per the
 * Visit Summary design. Falls back to the native `fallback` when there is no
 * instant (or it can't be parsed).
 */
function computeBannerTime(
  status: QueueStatus | '',
  etaAt: string | undefined,
  connectedAt: string | undefined,
  now: number,
  fallback: string,
): string {
  const instant = status === 'onCall' ? connectedAt : etaAt;
  if (!status || !instant) {
    return fallback;
  }
  const targetMs = Date.parse(instant);
  if (Number.isNaN(targetMs)) {
    return fallback;
  }
  return formatMins(status === 'onCall' ? now - targetMs : targetMs - now);
}

// Props delivered from the native host (VisitSummaryActivity_New) as
// initialProperties. All optional so the banner also renders standalone in dev;
// defaults match the "Next in Queue" design at the top of the Visit Summary.
interface VisitSummaryStatusBannerProps {
  variant?: StatusBannerVariant;
  title?: string;
  subtitle?: string;
  // Trailing pill on the right (e.g. the estimated wait, "8 Mins"). Used as the
  // fallback when there is no live instant to tick from.
  time?: string;
  // Same fields as the Patient's Queue list item, so the pill shows the same
  // live wait time / call duration as this visit's row in the list.
  status?: QueueStatus | '';
  etaAt?: string;
  connectedAt?: string;
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
 * Unlike the home banner, this one is not dismissible and shows the wait time
 * as a pill instead of a "View Queue" action. When etaAt/connectedAt are given
 * the pill ticks every second, exactly like QueueListItem.
 */
export default function VisitSummaryStatusBanner({
  variant = 'warning',
  title = 'Queue 104 · Position #2',
  subtitle = 'Next in Queue',
  time = '8 Mins',
  status = '',
  etaAt,
  connectedAt,
}: VisitSummaryStatusBannerProps): React.JSX.Element {
  const [now, setNow] = useState(() => Date.now());
  const hasLiveTime = Boolean(etaAt || connectedAt);

  // 1s clock, paused in the background (same approach as PatientQueue).
  useEffect(() => {
    if (!hasLiveTime) {
      return;
    }
    let interval: ReturnType<typeof setInterval> | undefined;
    const start = () => {
      if (interval == null) {
        setNow(Date.now());
        interval = setInterval(() => setNow(Date.now()), 1000);
      }
    };
    const stop = () => {
      if (interval != null) {
        clearInterval(interval);
        interval = undefined;
      }
    };
    const sub = AppState.addEventListener('change', state => {
      if (state === 'active') {
        start();
      } else {
        stop();
      }
    });
    start();
    return () => {
      stop();
      sub.remove();
    };
  }, [hasLiveTime]);

  const displayTime = computeBannerTime(status, etaAt, connectedAt, now, time);

  return (
    <View style={styles.wrapper}>
      <StatusBanner
        variant={variant}
        title={title}
        subtitle={subtitle}
        // Design: muted regular title ("Queue 104 · Position #2") over a bold
        // amber status line ("Next in Queue") — the reverse of StatusBanner's
        // default emphasis.
        titleStyle={styles.title}
        subtitleStyle={styles.subtitle}
        actionLabel={displayTime}
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
  title: {
    fontFamily: FontFamily.lato,
    fontSize: 12,
    fontWeight: FontWeight.regular,
    color: Colors.textMuted,
  },
  subtitle: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    // bold (>= 700) so Android picks Lato_bold.ttf; semibold falls back to regular.
    fontWeight: FontWeight.bold,
    color: Colors.bannerWarningAccent,
    marginTop: 2,
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
