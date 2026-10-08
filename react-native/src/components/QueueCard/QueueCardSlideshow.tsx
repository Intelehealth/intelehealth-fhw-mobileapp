import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  DeviceEventEmitter,
  FlatList,
  LayoutChangeEvent,
  NativeScrollEvent,
  NativeSyntheticEvent,
  StyleSheet,
  View,
} from 'react-native';

import QueueCard from './QueueCard';
import { QueueCardProps, QueueCardSlideshowProps } from './types';
import { Colors } from '../../theme';

// Native event emitted by QueueCardUpdater when a "Next In Queue" FCM
// notification arrives while this card is mounted. Keep in sync with
// QueueCardUpdater.EVENT_QUEUE_CARD_UPDATE on the Android side.
const QUEUE_CARD_UPDATE_EVENT = 'QueueCardUpdate';

// Space between adjacent slides, so a card's border doesn't touch the next one
// mid-swipe.
const SLIDE_GAP = 8;

/**
 * Accepts the event payload ({ patients: [...] }) and, for payloads from
 * builds before the slideshow, a bare single-patient object.
 */
function toPatientList(update: QueueCardSlideshowProps & QueueCardProps) {
  if (Array.isArray(update?.patients)) {
    return update.patients;
  }
  return update ? [update as QueueCardProps] : [];
}

/**
 * "Next In Queue" slideshow: one QueueCard per patient, swiped manually, with
 * page dots. Seeded from the patient list in the host fragment's
 * initial props and refreshed live from QueueCardUpdate events.
 */
export default function QueueCardSlideshow(props: QueueCardSlideshowProps) {
  const [patients, setPatients] = useState<QueueCardProps[]>(
    props.patients ?? [],
  );
  const [index, setIndex] = useState(0);
  const [width, setWidth] = useState(0);
  const listRef = useRef<FlatList<QueueCardProps>>(null);

  // Re-seed if the host remounts us with fresh initial props.
  useEffect(() => {
    setPatients(props.patients ?? []);
  }, [props.patients]);

  // Each FCM carries the full current list, so it replaces what is shown.
  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      QUEUE_CARD_UPDATE_EVENT,
      update => setPatients(toPatientList(update)),
    );
    return () => subscription.remove();
  }, []);

  // A shorter list may leave the current page out of range; go back to the
  // first card.
  useEffect(() => {
    if (index >= patients.length) {
      setIndex(0);
      listRef.current?.scrollToOffset({ offset: 0, animated: false });
    }
  }, [patients.length, index]);

  const onLayout = useCallback((e: LayoutChangeEvent) => {
    setWidth(e.nativeEvent.layout.width);
  }, []);

  const onMomentumScrollEnd = useCallback(
    (e: NativeSyntheticEvent<NativeScrollEvent>) => {
      if (width > 0) {
        setIndex(
          Math.round(e.nativeEvent.contentOffset.x / (width + SLIDE_GAP)),
        );
      }
    },
    [width],
  );

  if (patients.length === 0) {
    return null;
  }

  return (
    <View onLayout={onLayout}>
      {width === 0 ? (
        // Until the width is known, show the first card so the host's
        // wrap_content container gets a height straight away.
        <QueueCard {...patients[0]} />
      ) : (
        <FlatList
          ref={listRef}
          data={patients}
          horizontal
          showsHorizontalScrollIndicator={false}
          decelerationRate="fast"
          snapToInterval={width + SLIDE_GAP}
          disableIntervalMomentum
          keyExtractor={(item, i) => `${item.queueNumber ?? ''}-${i}`}
          getItemLayout={(_, i) => ({
            length: width + SLIDE_GAP,
            offset: (width + SLIDE_GAP) * i,
            index: i,
          })}
          onMomentumScrollEnd={onMomentumScrollEnd}
          renderItem={({ item, index: i }) => (
            <View
              style={[
                styles.slide,
                { width, marginRight: i < patients.length - 1 ? SLIDE_GAP : 0 },
              ]}>
              <QueueCard {...item} style={styles.slideCard} />
            </View>
          )}
        />
      )}

      {patients.length > 1 && (
        <View style={styles.dots}>
          {patients.map((_, i) => (
            <View
              key={i}
              style={[styles.dot, i === index && styles.dotActive]}
            />
          ))}
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  slide: {
    // Slides stretch to the tallest card; let each card fill its slide so the
    // row has a uniform height.
    alignSelf: 'stretch',
  },
  slideCard: {
    flex: 1,
  },
  dots: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 8,
    gap: 6,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: Colors.avatarPlaceholder,
  },
  dotActive: {
    width: 16,
    backgroundColor: Colors.primary,
  },
});
