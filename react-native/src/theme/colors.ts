/**
 * Central color palette. Add colors here rather than hardcoding hex values
 * inside component StyleSheets, so the whole app stays visually consistent.
 */
export const Colors = {
  // Brand
  primary: '#2E1A8B', // deep purple — queue number, action links

  // Surfaces
  white: '#FFFFFF',
  cardCream: '#FFFBEB', // queue card background
  cardCreamBorder: '#FDE68A',
  avatarPlaceholder: '#E5E7EB',
  divider: '#F3F4F6',
  inputBorder: '#E5E7EB', // search / text input outline

  // Text
  textPrimary: '#111827',
  textDark: '#1F2937',
  textMuted: '#6B7280',
  textPlaceholder: '#9CA3AF',

  // Status / accent (green)
  success: '#10B981',
  tagBackground: '#E6FDF4',
  tagBorder: '#34D399',
  tagText: '#059669',

  // Queue status: On Call (green surface + badge)
  cardGreen: '#F0FDF4',
  cardGreenBorder: '#BBF7D0',
  badgeOnCallBg: '#D1FAE5',
  badgeOnCallText: '#047857',

  // Queue status: Next in Queue (amber badge — reuses cardCream surface)
  badgeNextBg: '#FEF3C7',
  badgeNextText: '#D97706',

  // Queue status: Waiting (neutral surface + outlined badge)
  cardNeutralBorder: '#E5E7EB',
  badgeWaitingBorder: '#D1D5DB',
  badgeWaitingText: '#6B7280',

  // Queue filter tabs
  tabIndicator: '#EA315B', // active-tab underline (crimson)
  tabDivider: '#FFE8E8', // full-width hairline under the tab row

  // Buttons
  buttonSecondaryBg: '#EDE9FE', // light lavender — secondary/"Back to Queue"

  // Status banners (see StatusBanner component). Each variant = surface + border
  // + accent (icon/highlight) + soft icon chip + action-button bg/text.
  // -- warning (amber): position changes, wait reminders
  bannerWarningBg: '#FEFCE8',
  bannerWarningBorder: '#FFC581',
  bannerWarningAccent: '#D97706',
  bannerWarningIconBg: '#FEF3C7',
  bannerWarningButtonBg: '#FEF3C7',
  bannerWarningButtonText: '#D97706',
  // -- success (green): "next in line"
  bannerSuccessBg: '#F0FDF4',
  bannerSuccessBorder: '#1BA54E',
  bannerSuccessAccent: '#16A34A',
  bannerSuccessIconBg: '#DCFCE7',
  bannerSuccessButtonBg: '#16A34A',
  // -- priority (neutral surface, orange accent): "priority added"
  bannerPriorityBg: '#FFF7ED',
  bannerPriorityBorder: '#EA580C',
  bannerPriorityAccent: '#EA580C',
  bannerPriorityIconBg: '#FFEDD5',
  bannerPriorityButtonBg: '#F97316',
  // -- alert (orange): "doctor on break / queue paused"
  bannerAlertBg: '#FFF7ED',
  bannerAlertBorder: '#EA580C',
  bannerAlertAccent: '#EA580C',
  bannerAlertIconBg: '#FFEDD5',
  bannerAlertButtonBg: '#F97316',
  // -- shared
  bannerButtonTextLight: '#FFFFFF', // solid-button label
  bannerCloseIcon: '#9CA3AF', // dismiss (×) glyph

} as const;

export type ColorName = keyof typeof Colors;
