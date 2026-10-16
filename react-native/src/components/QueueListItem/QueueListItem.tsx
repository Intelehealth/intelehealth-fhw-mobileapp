import React from 'react';
import { StyleSheet, Text, View, Image, TouchableOpacity } from 'react-native';
import type { TextStyle, ViewStyle } from 'react-native';

import { QueueListItemProps, QueueStatus } from './types';
import { Colors, FontFamily, FontWeight } from '../../theme';

// Maximum number of symptom pills shown before the rest collapse into "+N More".
const MAX_VISIBLE_TAGS = 3;

/**
 * Per-status visual configuration. Keeping this in one map means the JSX stays
 * status-agnostic — add or tweak a variant here and every row updates.
 */
type StatusConfig = {
  card: ViewStyle;
  badge: ViewStyle;
  badgeText: TextStyle;
  label: string;
  // Footer metric label shown on the right ("Duration" vs "Wait time").
  timeLabel: string;
};

const STATUS_CONFIG: Record<QueueStatus, StatusConfig> = {
  onCall: {
    card: {
      backgroundColor: Colors.cardGreen,
      borderColor: Colors.cardGreenBorder,
    },
    badge: { backgroundColor: Colors.badgeOnCallBg },
    badgeText: { color: Colors.badgeOnCallText },
    label: 'On Call',
    timeLabel: 'Duration',
  },
  nextInQueue: {
    card: {
      backgroundColor: Colors.cardCream,
      borderColor: Colors.cardCreamBorder,
    },
    badge: { backgroundColor: Colors.badgeNextBg },
    badgeText: { color: Colors.badgeNextText },
    label: 'Next in Queue',
    timeLabel: 'Wait time',
  },
  waiting: {
    card: {
      backgroundColor: Colors.white,
      borderColor: Colors.cardNeutralBorder,
    },
    badge: {
      backgroundColor: Colors.white,
      borderWidth: 1,
      borderColor: Colors.badgeWaitingBorder,
    },
    badgeText: { color: Colors.badgeWaitingText },
    label: 'Waiting',
    timeLabel: 'Wait time',
  },
  // No position or time for the statuses below, so no footer metric.
  doctorAssigned: {
    card: {
      backgroundColor: Colors.white,
      borderColor: Colors.cardNeutralBorder,
    },
    badge: { backgroundColor: Colors.badgeNextBg },
    badgeText: { color: Colors.badgeNextText },
    label: 'Doctor Assigned',
    timeLabel: '',
  },
  connecting: {
    card: {
      backgroundColor: Colors.cardGreen,
      borderColor: Colors.cardGreenBorder,
    },
    badge: {
      backgroundColor: Colors.white,
      borderWidth: 1,
      borderColor: Colors.badgeOnCallText,
    },
    badgeText: { color: Colors.badgeOnCallText },
    label: 'Connecting',
    timeLabel: '',
  },
  awaitingPrescription: {
    card: {
      backgroundColor: Colors.white,
      borderColor: Colors.cardNeutralBorder,
    },
    badge: { backgroundColor: Colors.badgeOnCallBg },
    badgeText: { color: Colors.badgeOnCallText },
    label: 'Awaiting Prescription',
    timeLabel: '',
  },
};

// Statuses that show a live time in the footer (count-down to the ETA, or the
// call duration). The others have no position or time.
const TIMED_STATUSES: ReadonlyArray<QueueStatus> = ['onCall', 'nextInQueue', 'waiting'];

// Used when native sends a status outside the union (it maps unhandled DB
// statuses such as SUBMITTED to ""): neutral card, no badge.
const FALLBACK_CONFIG: StatusConfig = { ...STATUS_CONFIG.waiting, label: '', timeLabel: '' };

// Milliseconds -> "MM:SS", or "HH:MM:SS" once it reaches an hour (so long
// waits read as 05:18:28 rather than 318:28). Clamped at zero.
function formatMmSs(ms: number): string {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000));
  const hh = Math.floor(totalSeconds / 3600);
  const mm = Math.floor((totalSeconds % 3600) / 60);
  const ss = totalSeconds % 60;
  const pad = (n: number) => String(n).padStart(2, '0');
  return hh > 0 ? `${pad(hh)}:${pad(mm)}:${pad(ss)}` : `${pad(mm)}:${pad(ss)}`;
}

// Footer label shown instead of "Wait time" once the ETA has passed.
const OVERDUE_LABEL = 'Overdue';

/**
 * Resolves the footer time string. When a live instant is available it is
 * recomputed from `now` each tick — onCall counts up from `connectedAt`,
 * next/waiting counts down to `etaAt`. Once a next/waiting countdown reaches
 * zero the row is `overdue` and the value counts up from `etaAt` instead
 * (time past the ETA). Otherwise the pre-formatted `fallback` (from native) is
 * used. Parsing failures also fall back.
 */
function computeDisplayTime(
  status: QueueStatus,
  etaAt: string | undefined,
  connectedAt: string | undefined,
  now: number | undefined,
  fallback: string,
): { text: string; overdue: boolean } {
  if (!TIMED_STATUSES.includes(status)) {
    // Assigned / connecting / awaiting prescription (or unmapped): no time.
    return { text: '', overdue: false };
  }
  const nowMs = now ?? Date.now();
  const instant = status === 'onCall' ? connectedAt : etaAt;
  if (!instant) {
    return { text: fallback, overdue: false };
  }
  const targetMs = Date.parse(instant);
  if (Number.isNaN(targetMs)) {
    return { text: fallback, overdue: false };
  }
  if (status === 'onCall') {
    // Elapsed = now - connectedAt.
    return { text: formatMmSs(nowMs - targetMs), overdue: false };
  }
  // Remaining = etaAt - now; at/after zero, show how far past the ETA we are.
  const remainingMs = targetMs - nowMs;
  if (remainingMs <= 0) {
    return { text: formatMmSs(-remainingMs), overdue: true };
  }
  return { text: formatMmSs(remainingMs), overdue: false };
}

/**
 * A single patient row for the Patient Queue list. The surface, border and
 * status badge are driven by `status`; drop it straight into a FlatList as the
 * `renderItem` output.
 */
export default function QueueListItem(props: QueueListItemProps) {
  const {
    queueNumber,
    patientName,
    gender,
    age,
    patientId,
    symptoms,
    position,
    status,
    time,
    etaAt,
    connectedAt,
    now,
    avatarUrl,
    onPress,
    style,
    statusLabel,
    timeLabel: timeLabelProp,
    overdueLabel,
    moreLabel,
  } = props;

  const config = STATUS_CONFIG[status] ?? FALLBACK_CONFIG;
  // Localized badge text from native; STATUS_CONFIG's English label is only the
  // fallback. An unmapped status keeps no badge either way.
  const badgeLabel = config.label === '' ? '' : statusLabel || config.label;

  // Live time value. onCall counts UP from connectedAt (elapsed), everyone else
  // counts DOWN to etaAt (remaining). Recomputed from the absolute instant on
  // every `now` tick (self-correcting after background), and falls back to the
  // native-formatted `time` when no timestamp is present. Once the wait
  // countdown hits zero the label switches to "Overdue" and the value shows the
  // time elapsed past the ETA.
  const { text: displayTime, overdue } = computeDisplayTime(
    status,
    etaAt,
    connectedAt,
    now,
    time,
  );
  const timeLabel = overdue
    ? overdueLabel || OVERDUE_LABEL
    : timeLabelProp ?? config.timeLabel;

  // Fall back to the native `avatar1` drawable when the patient has no synced
  // photo. On Android, RN's <Image> resolves a bare `{uri}` (no path/scheme) to
  // a drawable of that name — reusing the same placeholder the native patient
  // lists use, so nothing extra is bundled here.
  const avatarSource = avatarUrl ? { uri: avatarUrl } : { uri: 'avatar1' };

  // Collapse overflow symptoms into a "+N More" chip, matching the design.
  const visibleSymptoms = symptoms.slice(0, MAX_VISIBLE_TAGS);
  const extraSymptomCount = symptoms.length - visibleSymptoms.length;

  return (
    <TouchableOpacity
      style={[styles.card, config.card, style]}
      activeOpacity={onPress ? 0.7 : 1}
      onPress={onPress}
      disabled={!onPress}>
      {/* Header (queue number + status badge) hidden for now; the status badge
          now lives at the end of the profile row instead. */}
      {/* <View style={styles.headerRow}>
        <Text style={styles.queueNumber}>{queueNumber}</Text>
        <View style={[styles.badge, config.badge]}>
          <Text style={[styles.badgeText, config.badgeText]}>{config.label}</Text>
        </View>
      </View> */}

      {/* Profile: avatar + name / meta / id + status badge at the end */}
      <View style={styles.profileContainer}>
        <Image source={avatarSource} style={styles.avatar} />
        <View style={styles.profileDetails}>
          <Text style={styles.profileName}>
            {patientName}{' '}
            <Text style={styles.profileMeta}>
              {gender} {age}
            </Text>
          </Text>
          <Text style={styles.profileId}>
            {patientId && !patientId.startsWith('ID-') ? `ID-${patientId}` : patientId}
          </Text>
        </View>
        {badgeLabel !== '' && (
          <View style={[styles.badge, config.badge, styles.profileBadge]}>
            <Text style={[styles.badgeText, config.badgeText]}>{badgeLabel}</Text>
          </View>
        )}
      </View>

      {/* Symptom tags */}
      <View style={styles.tagsContainer}>
        {visibleSymptoms.map((symptom, index) => (
          <View key={index} style={styles.tag}>
            <Text style={styles.tagText}>{symptom}</Text>
          </View>
        ))}
        {extraSymptomCount > 0 && (
          <View style={styles.tag}>
            <Text style={styles.tagText}>
              {moreLabel
                ? moreLabel.replace('%1$d', String(extraSymptomCount))
                : `+${extraSymptomCount} More`}
            </Text>
          </View>
        )}
      </View>

      {/* Footer: position + status-specific time metric */}
      <View style={styles.footerRow}>
        {/* Position hidden for now. */}
        {/* <Text style={styles.footerMeta}>
          Position <Text style={styles.boldText}>#{position}</Text>
        </Text> */}
        {/* Clock icon + time metric, pinned to the right end. Statuses without
            a time (assigned / connecting / awaiting prescription) show none. */}
        {displayTime !== '' && (
          <View style={styles.timeMetric}>
            <Image source={{ uri: 'ic_queue_clock' }} style={styles.clockIcon} />
            <Text style={styles.footerMeta}>
              {timeLabel} <Text style={styles.boldText}>{displayTime}</Text>
            </Text>
          </View>
        )}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    width: '100%',
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  queueNumber: {
    fontFamily: FontFamily.lato,
    fontSize: 16,
    fontWeight: FontWeight.extraBold,
    color: Colors.primary,
  },
  badge: {
    borderRadius: 20,
    paddingHorizontal: 12,
    paddingVertical: 5,
  },
  badgeText: {
    fontFamily: FontFamily.lato,
    fontSize: 12,
    fontWeight: FontWeight.semibold,
  },
  profileContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 14,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: Colors.avatarPlaceholder,
  },
  profileDetails: {
    flex: 1,
    marginLeft: 12,
  },
  // Keep the status badge pinned to the top of the profile row so it stays put
  // even when the patient name wraps to a second line.
  profileBadge: {
    alignSelf: 'flex-start',
  },
  profileName: {
    fontFamily: FontFamily.lato,
    fontSize: 16,
    fontWeight: FontWeight.semibold,
    color: Colors.textDark,
  },
  profileMeta: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    fontWeight: FontWeight.regular,
    color: Colors.textPlaceholder,
  },
  profileId: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    color: Colors.textPlaceholder,
    marginTop: 2,
  },
  tagsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 14,
  },
  tag: {
    backgroundColor: Colors.tagBackground,
    borderWidth: 1,
    borderColor: Colors.tagBorder,
    borderRadius: 20,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  tagText: {
    fontFamily: FontFamily.lato,
    color: Colors.tagText,
    fontSize: 13,
    fontWeight: FontWeight.medium,
  },
  footerRow: {
    flexDirection: 'row',
    // Only the time metric remains (position is hidden), so keep it at the end.
    justifyContent: 'flex-end',
    alignItems: 'center',
    // Divider line above the footer hidden for now (position is hidden too).
    // borderTopWidth: 1,
    // borderTopColor: Colors.divider,
    // paddingTop: 12,
  },
  // Clock icon + time text, laid out inline with the icon leading.
  // Gap between icon and text is 11px per the Figma footer-row spec.
  timeMetric: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  clockIcon: {
    width: 15,
    height: 15,
  },
  footerMeta: {
    fontFamily: FontFamily.lato,
    fontSize: 13,
    color: Colors.textPlaceholder,
  },
  boldText: {
    fontFamily: FontFamily.lato,
    fontWeight: FontWeight.bold,
    color: Colors.textDark,
  },
});
