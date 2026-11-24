import React from 'react';
import {DeviceEventEmitter, TextInput} from 'react-native';

import PatientQueue from '../../src/screens/PatientQueue';
import type {QueueListItemProps} from '../../src/components/QueueListItem';
import {QueueNavigator} from '../../src/native/QueueNavigator';
import {act, hasText, hasTextContaining, inSeconds, pressableWithText, render, texts} from '../../testUtils/queue';

jest.mock('../../src/native/QueueNavigator', () => ({
  QueueNavigator: {
    openQueueDetails: jest.fn(),
    openPatientQueue: jest.fn(),
    dismissStatusBanner: jest.fn(),
    close: jest.fn(),
  },
}));

function row(name: string, status: QueueListItemProps['status'], extra: Partial<QueueListItemProps> = {}): QueueListItemProps {
  return {
    queueNumber: `Q-${name}`,
    patientName: name,
    gender: 'F',
    age: 30,
    patientId: `ID-${name.toUpperCase()}`,
    symptoms: [],
    position: 1,
    status,
    time: '05:00',
    ...extra,
  };
}

const QUEUE = [
  row('Asha', 'nextInQueue'),
  row('Ravi', 'waiting'),
  row('Meena', 'waiting'),
  row('Kiran', 'onCall'),
  row('Dev', 'doctorAssigned'),
];

const shownNames = (root: any) =>
  QUEUE.map(r => r.patientName).filter(n => texts(root).some(t => t.startsWith(`${n} `)));

async function pressTab(root: any, label: string) {
  await act(() => pressableWithText(root, label).props.onPress());
}

async function search(root: any, term: string) {
  await act(() => root.findByType(TextInput).props.onChangeText(term));
}

beforeEach(() => {
  jest.clearAllMocks();
  DeviceEventEmitter.removeAllListeners();
});

describe('PatientQueue list', () => {
  it('shows every row and the queue size', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} />);
    expect(shownNames(root)).toEqual(['Asha', 'Ravi', 'Meena', 'Kiran', 'Dev']);
    expect(hasText(root, 'Patients in Queue : 5')).toBe(true);
  });

  it('renders an empty list without a queue prop', async () => {
    const {root} = await render(<PatientQueue />);
    expect(shownNames(root)).toEqual([]);
    expect(hasText(root, 'Patients in Queue : 0')).toBe(true);
  });

  it.each([
    ['Next', ['Asha']],
    ['Waiting', ['Ravi', 'Meena']],
    ['On Call', ['Kiran']],
    ['All', ['Asha', 'Ravi', 'Meena', 'Kiran', 'Dev']],
  ])('filters by the %s tab', async (tab, expected) => {
    const {root} = await render(<PatientQueue queue={QUEUE} />);
    await pressTab(root, tab === 'All' ? 'All' : tab);
    expect(shownNames(root)).toEqual(expected);
  });

  it('keeps the total count while a tab filters the list', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} />);
    await pressTab(root, 'On Call');
    expect(hasText(root, 'Patients in Queue : 5')).toBe(true);
  });

  it('searches by name, id or queue number, ignoring case and spaces', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} />);

    await search(root, '  rAvI ');
    expect(shownNames(root)).toEqual(['Ravi']);

    await search(root, 'id-meena');
    expect(shownNames(root)).toEqual(['Meena']);

    await search(root, 'q-kiran');
    expect(shownNames(root)).toEqual(['Kiran']);

    await search(root, 'nobody');
    expect(shownNames(root)).toEqual([]);
  });

  it('combines search with the selected tab', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} />);
    await pressTab(root, 'Waiting');
    await search(root, 'a'); // Ravi, Meena (and Asha/Kiran, filtered out by the tab)
    expect(shownNames(root)).toEqual(['Ravi', 'Meena']);
  });

  it('replaces the list on a PatientQueueUpdate event', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} />);

    await act(() => DeviceEventEmitter.emit('PatientQueueUpdate', {queue: [row('Ravi', 'nextInQueue')]}));
    expect(shownNames(root)).toEqual(['Ravi']);
    expect(hasText(root, 'Patients in Queue : 1')).toBe(true);

    await act(() => DeviceEventEmitter.emit('PatientQueueUpdate', {}));
    expect(shownNames(root)).toEqual([]);
  });

  it('uses localized labels from native', async () => {
    const {root} = await render(
      <PatientQueue
        queue={QUEUE}
        labels={{tabAll: 'सभी', tabNext: 'अगला', tabWaiting: 'प्रतीक्षा', tabOnCall: 'कॉल पर', countLabel: 'कतार :'}}
      />,
    );
    expect(texts(root)).toEqual(expect.arrayContaining(['सभी', 'अगला', 'प्रतीक्षा', 'कॉल पर', 'कतार : 5']));
    expect(root.findByType(TextInput).props.placeholder).toBe('Search patient');
  });
});

describe('PatientQueue live time', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-11-23T10:00:00.000Z'));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('ticks every second and turns Overdue at the ETA', async () => {
    const {root} = await render(<PatientQueue queue={[row('Asha', 'nextInQueue', {etaAt: inSeconds(2)})]} />);
    expect(hasText(root, 'Wait time 00:02')).toBe(true);

    await act(() => jest.advanceTimersByTime(1000));
    expect(hasText(root, 'Wait time 00:01')).toBe(true);

    await act(() => jest.advanceTimersByTime(2000));
    expect(hasText(root, 'Overdue 00:01')).toBe(true);
  });

  it('counts call duration up while on call', async () => {
    const {root} = await render(<PatientQueue queue={[row('Kiran', 'onCall', {connectedAt: inSeconds(-59)})]} />);
    expect(hasText(root, 'Duration 00:59')).toBe(true);

    await act(() => jest.advanceTimersByTime(1000));
    expect(hasText(root, 'Duration 01:00')).toBe(true);
  });
});

describe('PatientQueue status banner', () => {
  const banner = {variant: 'alert' as const, title: 'A doctor is away — ENT', subtitle: 'Queue Paused'};

  it('shows no banner when none is persisted', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} />);
    expect(hasTextContaining(root, 'away')).toBe(false);
  });

  it('shows the persisted banner without a View Queue action', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} banner={banner} />);
    expect(hasText(root, 'A doctor is away — ENT')).toBe(true);
    expect(hasText(root, 'Queue Paused')).toBe(true);
    expect(hasText(root, 'View Queue')).toBe(false);
  });

  it('dismissing hides it and tells native to dismiss it everywhere', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} banner={banner} />);
    await act(() => pressableWithText(root, '×').props.onPress());

    expect(hasText(root, 'A doctor is away — ENT')).toBe(false);
    expect(QueueNavigator.dismissStatusBanner).toHaveBeenCalledTimes(1);
  });

  it('hides on a StatusBannerDismiss event', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} banner={banner} />);
    await act(() => DeviceEventEmitter.emit('StatusBannerDismiss'));
    expect(hasText(root, 'A doctor is away — ENT')).toBe(false);
  });

  it('shows a live StatusBannerUpdate even after a dismiss', async () => {
    const {root} = await render(<PatientQueue queue={QUEUE} />);
    await act(() => DeviceEventEmitter.emit('StatusBannerUpdate', {title: 'A doctor is away — Skin'}));
    expect(hasText(root, 'A doctor is away — Skin')).toBe(true);
    expect(hasText(root, 'Queue Paused')).toBe(true); // default subtitle

    await act(() => DeviceEventEmitter.emit('StatusBannerDismiss'));
    await act(() => DeviceEventEmitter.emit('StatusBannerUpdate', {title: 'Back away'}));
    expect(hasText(root, 'Back away')).toBe(true);
  });
});
