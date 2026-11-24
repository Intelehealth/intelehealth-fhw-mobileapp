import React from 'react';

import QueueListItem from '../../src/components/QueueListItem';
import type {QueueListItemProps} from '../../src/components/QueueListItem';
import {hasText, hasTextContaining, render, texts, act, pressableWithText} from '../../testUtils/queue';

const NOW = Date.parse('2026-11-23T10:00:00.000Z');
const at = (seconds: number) => new Date(NOW + seconds * 1000).toISOString();

function props(overrides: Partial<QueueListItemProps> = {}): QueueListItemProps {
  return {
    queueNumber: 'OMRS-1',
    patientName: 'Asha Patil',
    gender: 'F',
    age: 34,
    patientId: 'OMRS-1',
    symptoms: [],
    position: 1,
    status: 'nextInQueue',
    time: '07:00',
    now: NOW,
    ...overrides,
  };
}

async function rootOf(p: QueueListItemProps) {
  return (await render(<QueueListItem {...p} />)).root;
}

describe('QueueListItem live time', () => {
  it('counts down to etaAt for the next patient', async () => {
    const root = await rootOf(props({etaAt: at(5 * 60)}));
    expect(hasText(root, 'Wait time 05:00')).toBe(true);
  });

  it('counts down for waiting patients too', async () => {
    const root = await rootOf(props({status: 'waiting', etaAt: at(65)}));
    expect(hasText(root, 'Wait time 01:05')).toBe(true);
  });

  it('switches to HH:MM:SS from one hour', async () => {
    const root = await rootOf(props({etaAt: at(2 * 3600 + 3 * 60 + 4)}));
    expect(hasText(root, 'Wait time 02:03:04')).toBe(true);
  });

  it('shows Overdue and the time past the ETA once it has passed', async () => {
    const root = await rootOf(props({etaAt: at(-90)}));
    expect(hasText(root, 'Overdue 01:30')).toBe(true);
    expect(hasTextContaining(root, 'Wait time')).toBe(false);
  });

  it('is overdue exactly at the ETA', async () => {
    const root = await rootOf(props({etaAt: at(0)}));
    expect(hasText(root, 'Overdue 00:00')).toBe(true);
  });

  it('uses the localized overdue label', async () => {
    const root = await rootOf(props({etaAt: at(-10), overdueLabel: 'Late'}));
    expect(hasText(root, 'Late 00:10')).toBe(true);
  });

  it('counts call duration up from connectedAt when on call', async () => {
    const root = await rootOf(props({status: 'onCall', connectedAt: at(-125), etaAt: at(600)}));
    expect(hasText(root, 'Duration 02:05')).toBe(true);
  });

  it('falls back to the native time without an instant', async () => {
    const root = await rootOf(props({time: '07:00'}));
    expect(hasText(root, 'Wait time 07:00')).toBe(true);
  });

  it('falls back to the native time when the instant cannot be parsed', async () => {
    const root = await rootOf(props({etaAt: 'not-a-date', time: '04:00'}));
    expect(hasText(root, 'Wait time 04:00')).toBe(true);
  });

  it.each(['doctorAssigned', 'connecting', 'awaitingPrescription'] as const)(
    'shows no time for %s',
    async status => {
      const root = await rootOf(props({status, etaAt: at(300), time: '05:00'}));
      expect(hasTextContaining(root, '05:00')).toBe(false);
      expect(hasTextContaining(root, 'Wait time')).toBe(false);
    },
  );

  it('re-computes the time when the shared clock ticks', async () => {
    const etaAt = at(60);
    const renderer = await render(<QueueListItem {...props({etaAt})} />);
    expect(hasText(renderer.root, 'Wait time 01:00')).toBe(true);

    await act(() => renderer.update(<QueueListItem {...props({etaAt, now: NOW + 61_000})} />));

    expect(hasText(renderer.root, 'Overdue 00:01')).toBe(true);
  });

  it('uses the localized time label', async () => {
    const root = await rootOf(props({etaAt: at(60), timeLabel: 'Espera'}));
    expect(hasText(root, 'Espera 01:00')).toBe(true);
  });
});

describe('QueueListItem badge and patient', () => {
  it('shows the English status label by default', async () => {
    expect(hasText(await rootOf(props({status: 'waiting'})), 'Waiting')).toBe(true);
    expect(hasText(await rootOf(props({status: 'onCall'})), 'On Call')).toBe(true);
    expect(hasText(await rootOf(props({status: 'doctorAssigned'})), 'Doctor Assigned')).toBe(true);
  });

  it('prefers the localized status label from native', async () => {
    const root = await rootOf(props({statusLabel: 'अगला'}));
    expect(hasText(root, 'अगला')).toBe(true);
    expect(hasText(root, 'Next in Queue')).toBe(false);
  });

  it('shows no badge for an unmapped status', async () => {
    const root = await rootOf(props({status: '' as any, statusLabel: 'Should not show'}));
    expect(hasText(root, 'Should not show')).toBe(false);
  });

  it('shows name, gender and age', async () => {
    const root = await rootOf(props());
    expect(hasText(root, 'Asha Patil F 34')).toBe(true);
  });

  it('prefixes the patient id with ID- once', async () => {
    expect(hasText(await rootOf(props({patientId: 'OMRS-1'})), 'ID-OMRS-1')).toBe(true);
    expect(hasText(await rootOf(props({patientId: 'ID-9'})), 'ID-9')).toBe(true);
  });
});

describe('QueueListItem symptoms', () => {
  it('shows up to three symptoms and collapses the rest', async () => {
    const root = await rootOf(props({symptoms: ['Fever', 'Cough', 'Cold', 'Rash', 'Pain']}));
    const all = texts(root);
    expect(all).toEqual(expect.arrayContaining(['Fever', 'Cough', 'Cold', '+2 More']));
    expect(all).not.toContain('Rash');
  });

  it('uses the localized "+N More" template', async () => {
    const root = await rootOf(props({symptoms: ['a', 'b', 'c', 'd'], moreLabel: '+%1$d और'}));
    expect(hasText(root, '+1 और')).toBe(true);
  });

  it('has no more-chip with three or fewer symptoms', async () => {
    const root = await rootOf(props({symptoms: ['a', 'b', 'c']}));
    expect(hasTextContaining(root, 'More')).toBe(false);
  });
});

describe('QueueListItem press', () => {
  it('calls onPress when tapped', async () => {
    const onPress = jest.fn();
    const root = await rootOf(props({onPress}));
    await act(() => pressableWithText(root, 'Asha Patil').props.onPress());
    expect(onPress).toHaveBeenCalledTimes(1);
  });
});
