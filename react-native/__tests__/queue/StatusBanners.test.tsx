import React from 'react';
import {DeviceEventEmitter} from 'react-native';

import HomeStatusBanner from '../../src/screens/HomeStatusBanner';
import VisitSummaryStatusBanner from '../../src/screens/VisitSummaryStatusBanner';
import {QueueNavigator} from '../../src/native/QueueNavigator';
import {act, hasText, inSeconds, pressableWithText, render, texts} from '../../testUtils/queue';

jest.mock('../../src/native/QueueNavigator', () => ({
  QueueNavigator: {
    openQueueDetails: jest.fn(),
    openPatientQueue: jest.fn(),
    dismissStatusBanner: jest.fn(),
    close: jest.fn(),
  },
}));

beforeEach(() => {
  jest.clearAllMocks();
  DeviceEventEmitter.removeAllListeners();
});

describe('HomeStatusBanner', () => {
  it('uses the "Doctor is on Break" defaults', async () => {
    const {root} = await render(<HomeStatusBanner />);
    expect(texts(root)).toEqual(expect.arrayContaining(['Doctor is on Break', 'Queue Paused', 'View Queue', '×']));
  });

  it('shows the payload title and labels', async () => {
    const {root} = await render(
      <HomeStatusBanner title="A doctor is away — ENT" subtitle="कतार रुकी" actionLabel="कतार देखें" />,
    );
    expect(texts(root)).toEqual(expect.arrayContaining(['A doctor is away — ENT', 'कतार रुकी', 'कतार देखें']));
  });

  it('View Queue opens the patient queue', async () => {
    const {root} = await render(<HomeStatusBanner />);
    await act(() => pressableWithText(root, 'View Queue').props.onPress());
    expect(QueueNavigator.openPatientQueue).toHaveBeenCalledTimes(1);
  });

  it('× hides the banner and dismisses it natively', async () => {
    const renderer = await render(<HomeStatusBanner />);
    await act(() => pressableWithText(renderer.root, '×').props.onPress());
    expect(renderer.toJSON()).toBeNull();
    expect(QueueNavigator.dismissStatusBanner).toHaveBeenCalledTimes(1);
  });

  it('hides on StatusBannerDismiss and re-shows on the next update', async () => {
    const renderer = await render(<HomeStatusBanner />);

    await act(() => DeviceEventEmitter.emit('StatusBannerDismiss'));
    expect(renderer.toJSON()).toBeNull();

    await act(() => DeviceEventEmitter.emit('StatusBannerUpdate', {title: 'A doctor is away — Skin'}));
    expect(hasText(renderer.root, 'A doctor is away — Skin')).toBe(true);
  });
});

describe('VisitSummaryStatusBanner', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-11-23T10:00:00.000Z'));
  });
  afterEach(() => jest.useRealTimers());

  it('shows title, status and native time without an instant', async () => {
    const {root} = await render(
      <VisitSummaryStatusBanner title="Position #2" subtitle="Waiting" status="waiting" time="8 Mins" />,
    );
    expect(texts(root)).toEqual(expect.arrayContaining(['Position #2', 'Waiting', '8 Mins']));
  });

  it('is not dismissible', async () => {
    const {root} = await render(<VisitSummaryStatusBanner title="Position #1" />);
    expect(texts(root)).not.toContain('×');
  });

  it('counts the wait down live from etaAt in minutes', async () => {
    const {root} = await render(
      <VisitSummaryStatusBanner status="nextInQueue" etaAt={inSeconds(10 * 60)} time="99 Mins" />,
    );
    expect(hasText(root, '10 Mins')).toBe(true);

    await act(() => jest.advanceTimersByTime(9 * 60 * 1000));
    expect(hasText(root, '1 Min')).toBe(true);

    await act(() => jest.advanceTimersByTime(5 * 60 * 1000));
    expect(hasText(root, '0 Mins')).toBe(true);
  });

  it('counts the call duration up from connectedAt', async () => {
    const {root} = await render(
      <VisitSummaryStatusBanner status="onCall" connectedAt={inSeconds(-3 * 60)} etaAt={inSeconds(600)} />,
    );
    expect(hasText(root, '3 Mins')).toBe(true);
  });

  it('uses localized minute units', async () => {
    const {root} = await render(
      <VisitSummaryStatusBanner status="waiting" etaAt={inSeconds(60)} minuteLabel="मिनट" minutesLabel="मिनट्स" />,
    );
    expect(hasText(root, '1 मिनट')).toBe(true);
  });

  it('keeps the native time for statuses that have no live time', async () => {
    const {root} = await render(
      <VisitSummaryStatusBanner status="doctorAssigned" etaAt={inSeconds(600)} time="" title="In Queue" />,
    );
    expect(texts(root)).not.toContain('10 Mins');
  });

  it('applies a VisitSummaryQueueBannerUpdate event', async () => {
    const {root} = await render(<VisitSummaryStatusBanner title="Position #3" subtitle="Waiting" time="9 Mins" />);

    await act(() =>
      DeviceEventEmitter.emit('VisitSummaryQueueBannerUpdate', {
        title: 'Position #1', subtitle: 'Next in Queue', status: 'nextInQueue', time: '2 Mins',
      }),
    );

    expect(texts(root)).toEqual(expect.arrayContaining(['Position #1', 'Next in Queue', '2 Mins']));
    expect(texts(root)).not.toContain('Position #3');
  });
});
