import type { TextStyle } from 'react-native';

/**
 * Font families available to React Native.
 *
 * The Lato .ttf files live in `app/src/main/assets/fonts/` as:
 *   - Lato.ttf        (regular — used when fontWeight < 700)
 *   - Lato_bold.ttf   (used when fontWeight >= 700)
 *
 * React Native's Android ReactFontManager resolves `fontFamily: 'Lato'` to the
 * correct file automatically based on the fontWeight, so a single family name
 * covers both the regular and bold TTFs. To add more weights (e.g. medium),
 * drop `Lato_medium.ttf` in the assets folder and expose it here.
 */
export const FontFamily = {
  lato: 'Lato',
} as const;

/**
 * Named font weights. Typed as TextStyle['fontWeight'] so they drop straight
 * into a StyleSheet without casting. Note: on Android RN only maps weights
 * >= 700 to the "_bold" TTF; lighter weights fall back to the regular file.
 */
export const FontWeight: Record<
  'regular' | 'medium' | 'semibold' | 'bold' | 'extraBold',
  TextStyle['fontWeight']
> = {
  regular: '400',
  medium: '500',
  semibold: '600',
  bold: '700',
  extraBold: '800',
};
