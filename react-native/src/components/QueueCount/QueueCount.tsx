import React from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { QueueCountProps } from './types';
import { Colors, FontFamily, FontWeight } from '../../theme';

/**
 * Shows the current queue size, e.g. "Patients in Queue : 7".
 */
export default function QueueCount({
  count,
  label = 'Patients in Queue :',
  style,
}: QueueCountProps) {
  return (
    <View style={[styles.container, style]}>
      <Text style={styles.label}>
        {label} <Text style={styles.count}>{count}</Text>
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    color: Colors.textMuted,
  },
  count: {
    fontFamily: FontFamily.lato,
    fontWeight: FontWeight.bold,
    color: Colors.textDark,
  },
});
