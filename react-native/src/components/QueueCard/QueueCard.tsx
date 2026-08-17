import React, { useEffect, useState } from 'react';
import {
  DeviceEventEmitter,
  StyleSheet,
  Text,
  View,
  Image,
  TouchableOpacity,
} from 'react-native';

import { QueueCardProps } from './types';
import { Colors, FontFamily, FontWeight } from '../../theme';
import { QueueNavigator } from '../../native/QueueNavigator';

// Maximum number of symptom pills shown before the rest collapse into "+N More".
const MAX_VISIBLE_TAGS = 2;

// Native event emitted by QueueCardUpdater when a "Next In Queue" FCM
// notification arrives while this card is mounted. Keep in sync with
// QueueCardUpdater.EVENT_QUEUE_CARD_UPDATE on the Android side.
const QUEUE_CARD_UPDATE_EVENT = 'QueueCardUpdate';

export default function QueueCard(props: QueueCardProps) {
  // Card content lives in state so a live FCM update can refresh it without a
  // native remount. Seeded from the initial props delivered by the host
  // fragment (which itself may be the last persisted queue payload).
  const [data, setData] = useState<QueueCardProps>(props);

  // Re-seed if the host remounts us with fresh initial props.
  useEffect(() => {
    setData(props);
  }, [props]);

  // Subscribe to live queue updates pushed from native on FCM receipt. Only
  // the fields present in the payload override the current card; anything
  // omitted keeps its existing value.
  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      QUEUE_CARD_UPDATE_EVENT,
      (update: QueueCardProps) => {
        setData(prev => ({ ...prev, ...update }));
      },
    );
    return () => subscription.remove();
  }, []);

  // Fallback to default mock data if properties aren't completely ready.
  const queueNumber = data.queueNumber || "Q-104";
  const patientName = data.patientName || "Anthony G";
  const gender = data.gender || "M";
  const age = data.age || 50;
  const patientId = data.patientId || "ID-987654jK";
  const symptoms = data.symptoms || ["Abdominal Pain", "Nausea"];
  const position = data.position ?? 2;
  const waitTimeMinutes = data.waitTimeMinutes ?? 8;
  const avatarUrl = data.avatarUrl || 'https://unsplash.com';

  // Collapse overflow symptoms into a "+N More" chip, matching the Figma design.
  const visibleSymptoms = symptoms.slice(0, MAX_VISIBLE_TAGS);
  const extraSymptomCount = symptoms.length - visibleSymptoms.length;

  return (
    <View style={styles.cardContainer}>
      {/* Header Row */}
      <View style={styles.headerRow}>
        <Text style={styles.headerTitle}>Next In Queue</Text>
        <View style={styles.statusContainer}>
          <View style={styles.greenDot} />
          <Text style={styles.statusText}>Updated now</Text>
        </View>
      </View>

      {/* Queue Number */}
      <Text style={styles.queueNumber}>{queueNumber}</Text>

      {/* Profile Section */}
      <View style={styles.profileContainer}>
        <Image
          source={{ uri: avatarUrl }}
          style={styles.avatar}
        />
        <View style={styles.profileDetails}>
          <Text style={styles.profileName}>
            {patientName} <Text style={styles.profileMeta}>{gender} {age}</Text>
          </Text>
          <Text style={styles.profileId}>{patientId}</Text>
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
          <Text style={styles.footerMeta}>
            Position <Text style={styles.boldText}>#{position}</Text>
          </Text>
          <Text style={styles.footerMeta}>
            Wait Time <Text style={styles.boldText}>{waitTimeMinutes} mins</Text>
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
    </View>
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
    borderTopWidth: 1,
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
