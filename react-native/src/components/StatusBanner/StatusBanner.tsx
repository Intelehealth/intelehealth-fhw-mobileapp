import React from 'react';
import { Image, StyleSheet, Text, View, TouchableOpacity } from 'react-native';
import type { ImageSourcePropType, TextStyle, ViewStyle } from 'react-native';

import { StatusBannerProps, StatusBannerVariant } from './types';
import { Colors, FontFamily, FontWeight } from '../../theme';

/**
 * Per-variant visual configuration. Keeping every colour/icon in one map means
 * the JSX stays variant-agnostic — add or tweak a variant here and every banner
 * updates. Mirrors the STATUS_CONFIG pattern used by QueueListItem.
 *
 * `iconSource` points at an Android vector drawable in
 * app/src/main/res/drawable/, referenced by name the way RN resolves native
 * drawables (same pattern as SearchBar's search_icon). Each drawable's stroke
 * colour already matches its variant accent, so the image is drawn untinted.
 */
type VariantConfig = {
  container: ViewStyle;
  iconChip: ViewStyle;
  iconSource: ImageSourcePropType; // default leading icon (native drawable)
  accent: TextStyle; // colour applied to the title text
  button: ViewStyle;
  buttonLabel: TextStyle;
};

const VARIANT_CONFIG: Record<StatusBannerVariant, VariantConfig> = {
  warning: {
    container: {
      backgroundColor: Colors.bannerWarningBg,
      borderColor: Colors.bannerWarningBorder,
    },
    iconChip: { backgroundColor: Colors.bannerWarningIconBg },
    iconSource: { uri: 'wait_status_icon' },
    accent: { color: Colors.bannerWarningAccent },
    button: { backgroundColor: Colors.bannerWarningButtonBg },
    buttonLabel: { color: Colors.bannerWarningButtonText },
  },
  success: {
    container: {
      backgroundColor: Colors.bannerSuccessBg,
      borderColor: Colors.bannerSuccessBorder,
    },
    iconChip: { backgroundColor: Colors.bannerSuccessIconBg },
    iconSource: { uri: 'next_in_queue_status_icon' },
    accent: { color: Colors.bannerSuccessAccent },
    button: { backgroundColor: Colors.bannerSuccessButtonBg },
    buttonLabel: { color: Colors.bannerButtonTextLight },
  },
  priority: {
    container: {
      backgroundColor: Colors.bannerPriorityBg,
      borderColor: Colors.bannerPriorityBorder,
    },
    iconChip: { backgroundColor: Colors.bannerPriorityIconBg },
    iconSource: { uri: 'priority_status_icon' },
    accent: { color: Colors.bannerPriorityAccent },
    button: { backgroundColor: Colors.bannerPriorityButtonBg },
    buttonLabel: { color: Colors.bannerButtonTextLight },
  },
  alert: {
    container: {
      backgroundColor: Colors.bannerAlertBg,
      borderColor: Colors.bannerAlertBorder,
    },
    iconChip: { backgroundColor: Colors.bannerAlertIconBg },
    iconSource: { uri: 'doctor_break_status' },
    accent: { color: Colors.bannerAlertAccent },
    button: { backgroundColor: Colors.bannerAlertButtonBg },
    buttonLabel: { color: Colors.bannerButtonTextLight },
  },
};

/**
 * A reusable status banner: [icon] [title + sub-lines] [action pill] [×].
 *
 * The surface, border, icon and action button are all driven by `variant`, so
 * this single component renders every banner in the design. Examples:
 *
 * ```tsx
 * // Queue position changed
 * <StatusBanner
 *   variant="warning"
 *   title="Sarrah Paul · Q-104"
 *   subtitle={['Position #2 → #3', '5 mins wait']}
 *   actionLabel="View Queue"
 *   onActionPress={openQueue}
 *   onDismiss={dismiss}
 * />
 *
 * // Next in line
 * <StatusBanner
 *   variant="success"
 *   title="Sarrah Paul is next in line"
 *   subtitle="Dr. will call you shortly"
 *   actionLabel="View Queue"
 * />
 *
 * // Priority added
 * <StatusBanner variant="priority" title="Priority Added"
 *   subtitle="Sarrah Paul moved to #4" actionLabel="View Queue" />
 *
 * // Doctor on break
 * <StatusBanner variant="alert" title="Doctor is on Break"
 *   subtitle="Queue Paused" actionLabel="View Queue" />
 * ```
 */
export default function StatusBanner(props: StatusBannerProps) {
  const {
    variant = 'warning',
    title,
    subtitle,
    icon,
    actionLabel,
    onActionPress,
    dismissible = true,
    onDismiss,
    style,
  } = props;

  const config = VARIANT_CONFIG[variant];

  // Normalise subtitle into an array of lines so callers can pass one string
  // or several.
  const subtitleLines =
    subtitle == null ? [] : Array.isArray(subtitle) ? subtitle : [subtitle];

  return (
    <View style={[styles.container, config.container, style]}>
      {/* Leading icon: the variant's native drawable, or a caller override */}
      <View style={[styles.iconChip, config.iconChip]}>
        {icon ?? (
          <Image
            source={config.iconSource}
            style={styles.glyph}
            resizeMode="contain"
          />
        )}
      </View>

      {/* Accent-coloured title + sub-lines */}
      <View style={styles.content}>
        <Text style={[styles.title, config.accent]} numberOfLines={2}>
          {title}
        </Text>
        {subtitleLines.map((line, index) => (
          <Text key={index} style={styles.subtitle} numberOfLines={1}>
            {line}
          </Text>
        ))}
      </View>

      {/* Trailing action pill */}
      {actionLabel ? (
        <TouchableOpacity
          style={[styles.button, config.button]}
          activeOpacity={0.8}
          onPress={onActionPress}
          disabled={!onActionPress}>
          <Text style={[styles.buttonLabel, config.buttonLabel]}>
            {actionLabel}
          </Text>
        </TouchableOpacity>
      ) : null}

      {/* Dismiss (×) */}
      {dismissible ? (
        <TouchableOpacity
          style={styles.close}
          activeOpacity={0.6}
          onPress={onDismiss}
          hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
          <Text style={styles.closeGlyph}>×</Text>
        </TouchableOpacity>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 12,
    borderWidth: 1,
    paddingVertical: 12,
    paddingHorizontal: 12,
    width: '100%',
  },
  iconChip: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  glyph: {
    width: 18,
    height: 18,
  },
  content: {
    flex: 1,
    marginRight: 8,
  },
  title: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    fontWeight: FontWeight.semibold,
    color: Colors.textDark,
  },
  subtitle: {
    fontFamily: FontFamily.lato,
    fontSize: 12,
    fontWeight: FontWeight.regular,
    color: Colors.textMuted,
    marginTop: 2,
  },
  button: {
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 7,
    marginRight: 4,
  },
  buttonLabel: {
    fontFamily: FontFamily.lato,
    fontSize: 13,
    fontWeight: FontWeight.semibold,
  },
  close: {
    paddingHorizontal: 4,
    paddingVertical: 2,
  },
  closeGlyph: {
    fontFamily: FontFamily.lato,
    fontSize: 18,
    lineHeight: 20,
    color: Colors.bannerCloseIcon,
  },
});
