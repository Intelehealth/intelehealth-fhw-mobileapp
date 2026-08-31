import React from 'react';
import { Image, StyleSheet, TextInput, View } from 'react-native';

import { SearchBarProps } from './types';
import { Colors, FontFamily } from '../../theme';

// Android drawable resource (app/src/main/res/drawable/search_icon.xml),
// referenced by name the way RN resolves native drawables.
const SEARCH_ICON = { uri: 'search_icon' };

/**
 * Rounded search input used at the top of the Patient's Queue screen.
 */
export default function SearchBar({
  value,
  onChangeText,
  placeholder = 'Search patient',
  style,
}: SearchBarProps) {
  return (
    <View style={[styles.container, style]}>
      <Image source={SEARCH_ICON} style={styles.icon} resizeMode="contain" />
      <TextInput
        style={styles.input}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={Colors.textPlaceholder}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.inputBorder,
    borderRadius: 8,
    paddingHorizontal: 12,
    height: 44,
    backgroundColor: Colors.white,
  },
  icon: {
    width: 18,
    height: 18,
    marginRight: 8,
  },
  input: {
    flex: 1,
    fontFamily: FontFamily.lato,
    fontSize: 16,
    color: Colors.textPrimary,
    padding: 0, // strip Android's default vertical padding so it aligns with the icon
    textAlignVertical: 'center', // center the text within the input's box on Android
    includeFontPadding: false, // remove asymmetric font padding that shifts text off-center
  },
});
