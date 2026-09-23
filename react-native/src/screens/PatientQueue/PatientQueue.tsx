import React, {useCallback, useEffect, useState} from 'react';
import {
  AppState,
  DeviceEventEmitter,
  FlatList,
  SafeAreaView,
  StyleSheet,
  View,
} from 'react-native';

import {Colors} from '../../theme';
import QueueCount from '../../components/QueueCount';
import SearchBar from '../../components/SearchBar';
import QueueListItem from '../../components/QueueListItem';
import type {QueueListItemProps} from '../../components/QueueListItem';
import QueueTabs from '../../components/QueueTabs';
import type {QueueFilter} from '../../components/QueueTabs';
import StatusBanner from '../../components/StatusBanner';
import type {StatusBannerVariant} from '../../components/StatusBanner';
// Disabled for now: queue details navigation is turned off.
// import {QueueNavigator} from '../../native/QueueNavigator';

// Fixed gap between queue rows. Defined outside the screen so React keeps a
// stable component type across renders (avoids remounting the list).
const ItemSeparator = () => <View style={styles.separator} />;

// Native event emitted by StatusBannerUpdater when a "queue_status" FCM
// notification arrives while this screen is mounted. Same event the home banner
// (HomeStatusBanner) listens to, so both banners update together. Keep in sync
// with StatusBannerUpdater.EVENT_STATUS_BANNER_UPDATE on the Android side.
const STATUS_BANNER_UPDATE_EVENT = 'StatusBannerUpdate';

// Shape of the status banner delivered from native — the persisted "queue_status"
// FCM payload (initial prop) and each live update carry the same fields.
interface StatusBannerData {
  variant?: StatusBannerVariant;
  title?: string;
  subtitle?: string;
  actionLabel?: string;
}

// Props delivered from the native host (PatientQueueFragment) as initialProperties.
// `queue` is a plain array of row objects mapped from QueueModel on the native side.
// `banner` is the persisted FCM banner payload, absent until one has been received.
interface PatientQueueProps {
  queue?: QueueListItemProps[];
  banner?: StatusBannerData;
}

/**
 * Patient's Queue screen.
 *
 * Hosted natively via a ReactFragment (component name "PatientQueueModule")
 * inside the bottom-nav host. The `queue` prop is supplied by the native
 * fragment from VisitsDAO; when absent (e.g. standalone dev) it falls back to
 * a small mock set.
 */
function PatientQueue({
  queue: queueProp,
  banner: bannerProp,
}: PatientQueueProps): React.JSX.Element {
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState<QueueFilter>('all');
  // Banner content lives in state so a live FCM update can refresh it without a
  // native remount. Seeded from the initial prop delivered by the host fragment
  // (the last persisted "queue_status" payload), mirroring HomeStatusBanner.
  const [banner, setBanner] = useState<StatusBannerData | undefined>(bannerProp);
  // Whether the banner is shown; dismissing (×) hides it, a new FCM update
  // re-shows it.
  const [bannerVisible, setBannerVisible] = useState(true);

  // Re-seed if the host remounts us with fresh initial props.
  useEffect(() => {
    setBanner(bannerProp);
  }, [bannerProp]);

  // Subscribe to live banner updates pushed from native on FCM receipt. Each
  // event is a complete banner, so it replaces the current content and re-shows
  // the banner if the user had dismissed the previous one.
  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      STATUS_BANNER_UPDATE_EVENT,
      (update: StatusBannerData) => {
        setBanner(update);
        setBannerVisible(true);
      },
    );
    return () => subscription.remove();
  }, []);

  // Fallback mock used only when the native host doesn't supply data.
  const mockQueue: QueueListItemProps[] = [
    {
      queueNumber: 'Q-104',
      patientName: 'Anthony G',
      gender: 'M',
      age: 50,
      patientId: 'ID-987654jK',
      symptoms: ['Abdominal Pain', 'Nausea', 'Fever'],
      position: 1,
      status: 'onCall',
      time: '04:32',
    },
    {
      queueNumber: 'Q-105',
      patientName: 'Anthony G',
      gender: 'M',
      age: 50,
      patientId: 'ID-987654jK',
      symptoms: ['Abdominal Pain', 'Nausea', 'Fever'],
      position: 2,
      status: 'nextInQueue',
      time: '02:59',
    },
    {
      queueNumber: 'Q-106',
      patientName: 'Anthony G',
      gender: 'M',
      age: 50,
      patientId: 'ID-987654jK',
      symptoms: ['Abdominal Pain', 'Nausea', 'Fever'],
      position: 3,
      status: 'waiting',
      time: '08:59',
    },
  ];

  // Show mock data for now; actual data disabled temporarily.
//   const source = queueProp && queueProp.length > 0 ? queueProp : mockQueue;
//   const source = mockQueue;
    const source = queueProp;
  // Attach a stable list key per row (native rows have no `key`).
  const queue: (QueueListItemProps & {key: string})[] = source.map(
    (item, index) => ({...item, key: `${item.patientId ?? item.queueNumber}-${index}`}),
  );

  // Tabs narrow the list by status; 'all' shows everything.
  const byStatus =
    filter === 'all' ? queue : queue.filter(item => item.status === filter);

  // Search narrows further by patient name / id / queue number (case-insensitive).
  const searchTerm = search.trim().toLowerCase();
  const visibleQueue = searchTerm
    ? byStatus.filter(item =>
        [item.patientName, item.patientId, item.queueNumber]
          .filter(Boolean)
          .some(field => field.toLowerCase().includes(searchTerm)),
      )
    : byStatus;

  // Shared 1s clock that drives the live wait time / duration on the rows. Each
  // row recomputes its own MM:SS from this `now` against its etaAt/connectedAt,
  // so there is a single timer for the whole list (not one per row) and no DB
  // re-read or native bridge traffic per tick.
  const [now, setNow] = useState(() => Date.now());
  // Only run the timer when at least one visible row actually has an instant to
  // count against (skips mock/timeless data and empty lists).
  const hasLiveTime = visibleQueue.some(item => item.etaAt || item.connectedAt);

  useEffect(() => {
    if (!hasLiveTime) {
      return;
    }
    let interval: ReturnType<typeof setInterval> | undefined;
    const start = () => {
      if (interval == null) {
        setNow(Date.now());
        interval = setInterval(() => setNow(Date.now()), 1000);
      }
    };
    const stop = () => {
      if (interval != null) {
        clearInterval(interval);
        interval = undefined;
      }
    };
    // Pause the ticker in the background so it doesn't burn battery unseen; on
    // return to foreground it restarts and recomputes from the absolute instant.
    const sub = AppState.addEventListener('change', state => {
      if (state === 'active') {
        start();
      } else {
        stop();
      }
    });
    start();
    return () => {
      stop();
      sub.remove();
    };
  }, [hasLiveTime]);

  // Row tap → open the native Queue Details Activity for that row.
  // Disabled for now: we don't want to show the queue details screen yet.
  // const handleOpenDetails = useCallback((item: QueueListItemProps) => {
  //   QueueNavigator.openQueueDetails(item);
  // }, []);

  // The banner sits above the list (as the FlatList header) so it shares the
  // list's horizontal insets and scrolls with the content. Its fields fall back
  // to the "Doctor is on Break" defaults when a payload is absent (dev, or before
  // any notification), matching the home banner. Dismissing hides it until the
  // next FCM update. No action pill here — we are already on the queue screen.
  const listHeader = bannerVisible ? (
    <View>
      <StatusBanner
        variant={banner?.variant ?? 'alert'}
        title={banner?.title ?? 'Doctor is on Break'}
        subtitle={banner?.subtitle ?? 'Queue Paused'}
        onDismiss={() => setBannerVisible(false)}
        style={styles.banner}
      />
    </View>
  ) : null;

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <QueueCount count={queue.length} style={styles.count} />
        <SearchBar value={search} onChangeText={setSearch} />
        <QueueTabs activeKey={filter} onChange={setFilter} style={styles.tabs} />
      </View>
      <FlatList
        data={visibleQueue}
        keyExtractor={item => item.key}
        // Re-render visible rows when the shared clock ticks.
        extraData={now}
        renderItem={({item}) => (
          // onPress disabled for now: we don't want to show queue details yet.
          // <QueueListItem {...item} onPress={() => handleOpenDetails(item)} />
          <QueueListItem {...item} now={now} />
        )}
        contentContainerStyle={styles.listContent}
        ListHeaderComponent={listHeader}
        ItemSeparatorComponent={ItemSeparator}
        showsVerticalScrollIndicator={false}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.white,
  },
  header: {
    paddingHorizontal: 16,
    paddingTop: 12,
  },
  count: {
    marginBottom: 12,
  },
  tabs: {
    marginTop: 4,
  },
  listContent: {
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 24,
  },
  banner: {
    // Space between the banner and the first queue row (rows are spaced by
    // ItemSeparator, which doesn't apply to the list header).
    marginBottom: 12,
  },
  separator: {
    height: 12,
  },
});

export default PatientQueue;
