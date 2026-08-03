import React, {useCallback, useState} from 'react';
import {FlatList, SafeAreaView, StyleSheet, View} from 'react-native';

import {Colors} from '../../theme';
import QueueCount from '../../components/QueueCount';
import SearchBar from '../../components/SearchBar';
import QueueListItem from '../../components/QueueListItem';
import type {QueueListItemProps} from '../../components/QueueListItem';
import QueueTabs from '../../components/QueueTabs';
import type {QueueFilter} from '../../components/QueueTabs';
import {QueueNavigator} from '../../native/QueueNavigator';

// Fixed gap between queue rows. Defined outside the screen so React keeps a
// stable component type across renders (avoids remounting the list).
const ItemSeparator = () => <View style={styles.separator} />;

// Props delivered from the native host (PatientQueueFragment) as initialProperties.
// `queue` is a plain array of row objects mapped from QueueModel on the native side.
interface PatientQueueProps {
  queue?: QueueListItemProps[];
}

/**
 * Patient's Queue screen.
 *
 * Hosted natively via a ReactFragment (component name "PatientQueueModule")
 * inside the bottom-nav host. The `queue` prop is supplied by the native
 * fragment from VisitsDAO; when absent (e.g. standalone dev) it falls back to
 * a small mock set.
 */
function PatientQueue({queue: queueProp}: PatientQueueProps): React.JSX.Element {
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState<QueueFilter>('all');

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

  const source = queueProp && queueProp.length > 0 ? queueProp : mockQueue;
  // Attach a stable list key per row (native rows have no `key`).
  const queue: (QueueListItemProps & {key: string})[] = source.map(
    (item, index) => ({...item, key: `${item.patientId ?? item.queueNumber}-${index}`}),
  );

  // Tabs narrow the list by status; 'all' shows everything.
  const visibleQueue =
    filter === 'all' ? queue : queue.filter(item => item.status === filter);

  // Row tap → open the native Queue Details Activity for that row.
  const handleOpenDetails = useCallback((item: QueueListItemProps) => {
    QueueNavigator.openQueueDetails(item);
  }, []);

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
        renderItem={({item}) => (
          <QueueListItem {...item} onPress={() => handleOpenDetails(item)} />
        )}
        contentContainerStyle={styles.listContent}
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
  separator: {
    height: 12,
  },
});

export default PatientQueue;
