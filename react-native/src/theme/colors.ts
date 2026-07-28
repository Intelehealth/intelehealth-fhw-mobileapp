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
} as const;

export type ColorName = keyof typeof Colors;
