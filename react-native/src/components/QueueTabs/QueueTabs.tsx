import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';

import { QueueTab, QueueTabsProps } from './types';
import { Colors, FontFamily, FontWeight } from '../../theme';

// Default filter strip matching the design: All / Next / Waiting / On Call.
const DEFAULT_TABS: QueueTab[] = [
  { key: 'all', label: 'All' },
  { key: 'nextInQueue', label: 'Next' },
  { key: 'waiting', label: 'Waiting' },
  { key: 'onCall', label: 'On Call' },
];

/**
 * Horizontal filter tabs for the Patient Queue. Controlled: the parent owns the
 * selected key and re-renders the list when `onChange` fires. The active tab
 * gets a crimson underline sized to its label.
 */
export default function QueueTabs({
  activeKey,
  onChange,
  tabs = DEFAULT_TABS,
  style,
}: QueueTabsProps) {
  return (
    <View style={[styles.container, style]}>
      {tabs.map(tab => {
        const active = tab.key === activeKey;
        return (
          <TouchableOpacity
            key={tab.key}
            style={styles.tab}
            activeOpacity={0.7}
            onPress={() => onChange(tab.key)}>
            <Text style={[styles.tabText, active && styles.tabTextActive]}>
              {tab.label}
            </Text>
            <View style={[styles.indicator, active && styles.indicatorActive]} />
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderBottomColor: Colors.tabDivider,
  },
  tab: {
    flex: 1,
    alignItems: 'center',
  },
  tabText: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    color: Colors.textMuted,
    textAlign: 'center',
    paddingBottom: 8,
    paddingTop: 12,
  },
  tabTextActive: {
    color: Colors.textDark,
    fontWeight: FontWeight.bold,
  },
  // Spans the full tab width so the underline sits under the whole tab, not just the label.
  indicator: {
    alignSelf: 'stretch',
    height: 2,
    borderRadius: 1,
    backgroundColor: 'transparent',
  },
  indicatorActive: {
    backgroundColor: Colors.tabIndicator,
  },
});
