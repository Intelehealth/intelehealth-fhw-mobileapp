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
};

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
    avatarUrl,
    onPress,
    style,
  } = props;

  const config = STATUS_CONFIG[status];

  // Collapse overflow symptoms into a "+N More" chip, matching the design.
  const visibleSymptoms = symptoms.slice(0, MAX_VISIBLE_TAGS);
  const extraSymptomCount = symptoms.length - visibleSymptoms.length;

  return (
    <TouchableOpacity
      style={[styles.card, config.card, style]}
      activeOpacity={onPress ? 0.7 : 1}
      onPress={onPress}
      disabled={!onPress}>
      {/* Header: queue number + status badge */}
      <View style={styles.headerRow}>
        <Text style={styles.queueNumber}>{queueNumber}</Text>
        <View style={[styles.badge, config.badge]}>
          <Text style={[styles.badgeText, config.badgeText]}>{config.label}</Text>
        </View>
      </View>

      {/* Profile: avatar + name / meta / id */}
      <View style={styles.profileContainer}>
        <Image source={{ uri: avatarUrl }} style={styles.avatar} />
        <View style={styles.profileDetails}>
          <Text style={styles.profileName}>
            {patientName}{' '}
            <Text style={styles.profileMeta}>
              {gender} {age}
            </Text>
          </Text>
          <Text style={styles.profileId}>{patientId}</Text>
        </View>
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
            <Text style={styles.tagText}>+{extraSymptomCount} More</Text>
          </View>
        )}
      </View>

      {/* Footer: position + status-specific time metric */}
      <View style={styles.footerRow}>
        <Text style={styles.footerMeta}>
          Position <Text style={styles.boldText}>#{position}</Text>
        </Text>
        <Text style={styles.footerMeta}>
          {config.timeLabel} <Text style={styles.boldText}>{time}</Text>
        </Text>
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
    borderTopWidth: 1,
    borderTopColor: Colors.divider,
    paddingTop: 12,
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
