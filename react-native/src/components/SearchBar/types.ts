import type { StyleProp, ViewStyle } from 'react-native';

/**
 * Props for the SearchBar component.
 */
export interface SearchBarProps {
  // Current text value (controlled input).
  value?: string;
  // Called with the new text on every keystroke.
  onChangeText?: (text: string) => void;
  // Placeholder shown when empty. Defaults to "Search patient".
  placeholder?: string;
  // Optional style override for the outer container.
  style?: StyleProp<ViewStyle>;
}
