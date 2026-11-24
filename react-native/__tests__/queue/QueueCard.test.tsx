import React from 'react';
import {DeviceEventEmitter, View} from 'react-native';

import QueueCard, {QueueCardSlideshow} from '../../src/components/QueueCard';
import type {QueueCardProps} from '../../src/components/QueueCard';
import {QueueNavigator} from '../../src/native/QueueNavigator';
import {act, hasText, hasTextContaining, pressableWithText, render, texts} from '../../testUtils/queue';

jest.mock('../../src/native/QueueNavigator', () => ({
  QueueNavigator: {
    openQueueDetails: jest.fn(),
    openPatientQueue: jest.fn(),
    dismissStatusBanner: jest.fn(),
    close: jest.fn(),
  },
}));

const NOW = Date.parse('2026-11-23T10:00:00.000Z');
const at = (seconds: number) => new Date(NOW + seconds * 1000).toISOString();

const patient = (name: string, extra: Partial<QueueCardProps> = {}): QueueCardProps => ({
  queueNumber: `Q-${name}`,
  patientName: name,
  gender: 'F',
  age: 30,
  patientId: 'OMRS-1',
  symptoms: ['Fever'],
  position: 1,
  waitTimeMinutes: 7,
  ...extra,
});

beforeEach(() => {
  jest.clearAllMocks();
  DeviceEventEmitter.removeAllListeners();
});

describe('QueueCard', () => {
  it('counts the wait down from etaAt', async () => {
    const {root} = await render(<QueueCard {...patient('Asha', {etaAt: at(125)})} now={NOW} />);
    expect(hasText(root, 'Wait Time 02:05')).toBe(true);
  });

  it('shows Overdue once the ETA has passed', async () => {
    const {root} = await render(<QueueCard {...patient('Asha', {etaAt: at(-30)})} now={NOW} />);
    expect(hasText(root, 'Overdue 00:30')).toBe(true);
  });

  it('falls back to waitTimeMinutes without (or with a bad) etaAt', async () => {
    expect(hasText((await render(<QueueCard {...patient('Asha')} now={NOW} />)).root, 'Wait Time 07:00')).toBe(true);
    expect(
      hasText((await render(<QueueCard {...patient('Asha', {etaAt: 'bad', waitTimeMinutes: 61})} now={NOW} />)).root,
        'Wait Time 01:01:00'),
    ).toBe(true);
  });

  it('shows patient details with the ID- prefix', async () => {
    const {root} = await render(<QueueCard {...patient('Asha')} now={NOW} />);
    expect(hasText(root, 'Asha F 30')).toBe(true);
    expect(hasText(root, 'ID-OMRS-1')).toBe(true);
    expect(hasText(root, 'Next In Queue')).toBe(true);
  });

  it('never invents data for empty fields', async () => {
    const {root} = await render(<QueueCard now={NOW} />);
    expect(hasText(root, ' 0')).toBe(true); // empty name/gender, age 0
    expect(hasText(root, 'Wait Time 00:00')).toBe(true);
    expect(hasTextContaining(root, 'ID-')).toBe(false);
  });

  it('shows two symptoms then "+N More"', async () => {
    const {root} = await render(<QueueCard {...patient('Asha', {symptoms: ['Fever', 'Cough', 'Cold', 'Rash']})} now={NOW} />);
    expect(texts(root)).toEqual(expect.arrayContaining(['Fever', 'Cough', '+2 More']));
    expect(texts(root)).not.toContain('Cold');
  });

  it('opens the patient queue from the button and from the card', async () => {
    const {root} = await render(<QueueCard {...patient('Asha')} now={NOW} />);

    await act(() => pressableWithText(root, 'Open Queue').props.onPress());
    await act(() => pressableWithText(root, 'Asha F 30').props.onPress());

    expect(QueueNavigator.openPatientQueue).toHaveBeenCalledTimes(2);
  });
});

describe('QueueCardSlideshow', () => {
  const layout = async (root: any, width = 320) => {
    const host = root.findAll((n: any) => typeof n.props.onLayout === 'function')[0];
    await act(() => host.props.onLayout({nativeEvent: {layout: {width, height: 200, x: 0, y: 0}}}));
  };

  const names = (root: any) => texts(root).filter(t => /^(Asha|Ravi|Meena) /.test(t)).map(t => t.split(' ')[0]);
  const dots = (root: any) =>
    root.findAllByType(View).filter((v: any) => {
      const style = [v.props.style].flat(3).filter(Boolean);
      // Page dots: 6px tall pills. The card's green "Updated now" dot has a right margin.
      return style.some((s: any) => s.height === 6 && s.borderRadius === 3)
        && !style.some((s: any) => s.marginRight === 6);
    });

  it('renders nothing without patients', async () => {
    const renderer = await render(<QueueCardSlideshow patients={[]} />);
    expect(renderer.toJSON()).toBeNull();
  });

  it('shows the first card until the width is known', async () => {
    const {root} = await render(<QueueCardSlideshow patients={[patient('Asha'), patient('Ravi')]} />);
    expect(names(root)).toEqual(['Asha']);
  });

  it('shows every card and one dot per card after layout', async () => {
    const {root} = await render(<QueueCardSlideshow patients={[patient('Asha'), patient('Ravi'), patient('Meena')]} />);
    await layout(root);
    expect(names(root)).toEqual(['Asha', 'Ravi', 'Meena']);
    expect(dots(root)).toHaveLength(3);
  });

  it('has no dots for a single card', async () => {
    const {root} = await render(<QueueCardSlideshow patients={[patient('Asha')]} />);
    await layout(root);
    expect(dots(root)).toHaveLength(0);
  });

  it('replaces the cards on a QueueCardUpdate event', async () => {
    const {root} = await render(<QueueCardSlideshow patients={[patient('Asha')]} />);
    await layout(root);

    await act(() => DeviceEventEmitter.emit('QueueCardUpdate', {patients: [patient('Ravi'), patient('Meena')]}));

    expect(names(root)).toEqual(['Ravi', 'Meena']);
  });

  it('accepts a legacy single-patient event payload', async () => {
    const {root} = await render(<QueueCardSlideshow patients={[patient('Asha')]} />);
    await act(() => DeviceEventEmitter.emit('QueueCardUpdate', patient('Meena')));
    expect(names(root)).toEqual(['Meena']);
  });

  it('disappears when an update empties the list', async () => {
    const renderer = await render(<QueueCardSlideshow patients={[patient('Asha')]} />);
    await act(() => DeviceEventEmitter.emit('QueueCardUpdate', {patients: []}));
    expect(renderer.toJSON()).toBeNull();
  });

  describe('live wait time', () => {
    beforeEach(() => {
      jest.useFakeTimers();
      jest.setSystemTime(new Date(NOW));
    });
    afterEach(() => jest.useRealTimers());

    it('ticks every card from one shared clock', async () => {
      const {root} = await render(
        <QueueCardSlideshow patients={[patient('Asha', {etaAt: at(3)}), patient('Ravi', {etaAt: at(61)})]} />,
      );
      await layout(root);
      expect(texts(root)).toEqual(expect.arrayContaining(['Wait Time 00:03', 'Wait Time 01:01']));

      await act(() => jest.advanceTimersByTime(4000));

      expect(texts(root)).toEqual(expect.arrayContaining(['Overdue 00:01', 'Wait Time 00:57']));
    });
  });
});
