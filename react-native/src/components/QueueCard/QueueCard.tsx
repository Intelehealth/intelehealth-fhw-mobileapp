import React from 'react';
import {
  StyleProp,
  StyleSheet,
  Text,
  View,
  ViewStyle,
  Image,
  TouchableOpacity,
} from 'react-native';

import { QueueCardProps } from './types';
import { Colors, FontFamily, FontWeight } from '../../theme';
import { QueueNavigator } from '../../native/QueueNavigator';

// Maximum number of symptom pills shown before the rest collapse into "+N More".
const MAX_VISIBLE_TAGS = 2;
// Queue number and position are hidden for now; flip to true to show them again.
const SHOW_QUEUE_POSITION = false;

// Footer label shown instead of "Wait Time" once the ETA has passed.
const OVERDUE_LABEL = 'Overdue';

// Milliseconds -> "MM:SS", or "HH:MM:SS" once it reaches an hour. Clamped at
// zero. Identical to the queue list row (QueueListItem.formatMmSs) so the two
// screens render the same string.
function formatMmSs(ms: number): string {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000));
  const hh = Math.floor(totalSeconds / 3600);
  const mm = Math.floor((totalSeconds % 3600) / 60);
  const ss = totalSeconds % 60;
  const pad = (n: number) => String(n).padStart(2, '0');
  return hh > 0 ? `${pad(hh)}:${pad(mm)}:${pad(ss)}` : `${pad(mm)}:${pad(ss)}`;
}

/**
 * Live wait time for the card, computed exactly like the queue list row: count
 * down from the absolute `etaAt` instant against `now`, and once it reaches zero
 * show how far past the ETA we are (overdue). When `etaAt` is absent/unparseable
 * it falls back to the `waitTimeMinutes` snapshot, rendered in the same MM:SS
 * shape so the format never changes between states.
 */
function computeWaitTime(
  etaAt: string | undefined,
  now: number,
  fallbackMinutes: number,
): { text: string; overdue: boolean } {
  if (etaAt) {
    const targetMs = Date.parse(etaAt);
    if (!Number.isNaN(targetMs)) {
      const remainingMs = targetMs - now;
      if (remainingMs <= 0) {
        return { text: formatMmSs(-remainingMs), overdue: true };
      }
      return { text: formatMmSs(remainingMs), overdue: false };
    }
  }
  return { text: formatMmSs(fallbackMinutes * 60000), overdue: false };
}

/**
 * A single "Next In Queue" card. Rendered per patient by QueueCardSlideshow,
 * which owns the patient list and the live FCM updates.
 */
export default function QueueCard({
  style,
  ...data
}: QueueCardProps & { style?: StyleProp<ViewStyle> }) {
  // Neutral fallbacks for fields missing from the payload. No mock patient here:
  // a real notification with an empty field must never show fake data.
  const queueNumber = data.queueNumber || '';
  const patientName = data.patientName || '';
  const gender = data.gender || '';
  const age = data.age || 0;
  const patientId = data.patientId || '';
  const symptoms = data.symptoms || [];
  const position = data.position ?? 0;
  const waitTimeMinutes = data.waitTimeMinutes ?? 0;
  // Live wait time from the shared clock (slideshow) or the current instant when
  // rendered standalone. Recomputed every tick from the absolute `etaAt`, so it
  // stays in sync with the queue listing instead of showing a static minutes
  // value.
  const { text: waitTime, overdue } = computeWaitTime(
    data.etaAt,
    data.now ?? Date.now(),
    waitTimeMinutes,
  );
  // Patient photo, same as the queue list rows; falls back to the native
  // `avatar1` drawable (a bare `{uri}` resolves to a drawable of that name on
  // Android) when the patient has no synced photo.
  const avatarSource = data.avatarUrl
    ? { uri: data.avatarUrl }
    : { uri: 'avatar1' };

  // Collapse overflow symptoms into a "+N More" chip, matching the Figma design.
  const visibleSymptoms = symptoms.slice(0, MAX_VISIBLE_TAGS);
  const extraSymptomCount = symptoms.length - visibleSymptoms.length;

  return (
    // Whole card is tappable (same as the native Prescriptions / Open visits
    // cards), not just the "Open Queue" button. A tap opens the queue; inside
    // the horizontal slideshow a swipe still scrolls, since RN distinguishes a
    // tap from a scroll gesture.
    <TouchableOpacity
      style={[styles.cardContainer, style]}
      activeOpacity={0.8}
      onPress={() => QueueNavigator.openPatientQueue()}>
      {/* Header Row */}
      <View style={styles.headerRow}>
        <Text style={styles.headerTitle}>Next In Queue</Text>
        <View style={styles.statusContainer}>
          <View style={styles.greenDot} />
          <Text style={styles.statusText}>Updated now</Text>
        </View>
      </View>

      {/* Queue Number */}
      {SHOW_QUEUE_POSITION && (
        <Text style={styles.queueNumber}>{queueNumber}</Text>
      )}

      {/* Profile Section */}
      <View style={styles.profileContainer}>
        <Image
          source={avatarSource}
          // Patient photos are full-size camera files; decode them at avatar
          // size so remounting the card (every return to Home) stays quick.
          resizeMethod="resize"
          style={styles.avatar}
        />
        <View style={styles.profileDetails}>
          <Text style={styles.profileName}>
            {patientName} <Text style={styles.profileMeta}>{gender} {age}</Text>
          </Text>
          <Text style={styles.profileId}>
            {patientId && !patientId.startsWith('ID-') ? `ID-${patientId}` : patientId}
          </Text>
        </View>
      </View>

      {/* Dynamic Tags Row */}
      <View style={styles.tagsContainer}>
        {visibleSymptoms.map((symptom, index) => (
          <View key={index} style={styles.tag}>
            <Text style={styles.tagText}>{symptom}</Text>
          </View>
        ))}
        {extraSymptomCount > 0 && (
          <View style={styles.tag}>
            <Text style={styles.tagText}>+{extraSymptomCount} More</Text>
          </View>
        )}
      </View>

      {/* Footer Row */}
      <View style={styles.footerRow}>
        <View style={styles.footerMetaGroup}>
          {SHOW_QUEUE_POSITION && (
            <Text style={styles.footerMeta}>
              Position <Text style={styles.boldText}>#{position}</Text>
            </Text>
          )}
          <Text style={styles.footerMeta}>
            {overdue ? OVERDUE_LABEL : 'Wait Time'}{' '}
            <Text style={styles.boldText}>{waitTime}</Text>
          </Text>
        </View>

        <TouchableOpacity
          style={styles.actionButton}
          activeOpacity={0.7}
          onPress={() => QueueNavigator.openPatientQueue()}>
          <Text style={styles.actionText}>Open Queue</Text>
          <Text style={styles.chevron}>{'›'}</Text>
        </TouchableOpacity>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  cardContainer: {
    backgroundColor: Colors.cardCream,
    borderRadius: 8,
    padding: 12,
    borderWidth: 1,
    borderColor: Colors.cardCreamBorder,
    width: '100%',
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  headerTitle: {
    fontFamily: FontFamily.lato,
    fontSize: 18,
    fontWeight: FontWeight.bold,
    color: Colors.textPrimary,
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  greenDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: Colors.success,
    marginRight: 6,
  },
  statusText: {
    fontFamily: FontFamily.lato,
    fontSize: 13,
    color: Colors.textMuted,
  },
  queueNumber: {
    fontFamily: FontFamily.lato,
    fontSize: 20,
    fontWeight: FontWeight.extraBold,
    color: Colors.primary,
    marginBottom: 12,
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
    marginLeft: 12,
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
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 0,
    borderTopColor: Colors.divider,
    paddingTop: 12,
  },
  footerMetaGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
    flexShrink: 1,
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
  actionButton: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  actionText: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    fontWeight: FontWeight.bold,
    color: Colors.primary,
    marginRight: 2,
  },
  chevron: {
    fontFamily: FontFamily.lato,
    fontSize: 18,
    fontWeight: FontWeight.bold,
    color: Colors.primary,
  },
});
