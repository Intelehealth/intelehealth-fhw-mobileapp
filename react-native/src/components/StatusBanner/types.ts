import type { ReactNode } from 'react';
import type { StyleProp, ViewStyle } from 'react-native';

/**
 * Types for the StatusBanner component.
 *
 * A single, reusable notification strip: [icon] [title + sub-lines] [action] [×].
 * The colour scheme (surface, border, icon, action button) is driven entirely by
 * `variant`, so the same component covers every banner in the design — queue
 * position updates, "next in line", priority changes, doctor-on-break, etc.
 */

// Selects the whole visual + semantic style of the banner.
export type StatusBannerVariant = 'warning' | 'success' | 'priority' | 'alert';

export interface StatusBannerProps {
  // Colour/emphasis scheme. Defaults to 'warning'.
  variant?: StatusBannerVariant;

  // Main heading, rendered in the variant's accent colour. Pass a string for
  // the default style, or a composed <Text> for mixed styling inside the title.
  title: ReactNode;

  // Secondary line(s) under the title. A string[] renders each on its own line
  // (e.g. "Position #2 → #3" then "5 mins wait").
  subtitle?: string | string[];

  // Overrides the default per-variant leading glyph (e.g. a native drawable
  // <Image />). When omitted, the variant's built-in glyph is shown.
  icon?: ReactNode;

  // Label for the trailing action pill (e.g. "View Queue"). Omit to hide it.
  actionLabel?: string;
  // Fired when the action pill is tapped.
  onActionPress?: () => void;

  // Whether to show the trailing dismiss (×). Defaults to true.
  dismissible?: boolean;
  // Fired when the dismiss (×) is tapped.
  onDismiss?: () => void;

  // Optional style override for the outer banner container.
  style?: StyleProp<ViewStyle>;
}
