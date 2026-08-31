import React from 'react';
import {
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';

import {Colors, FontFamily, FontWeight} from '../../theme';
import QueueListItem from '../../components/QueueListItem';
import type {QueueListItemProps} from '../../components/QueueListItem';
import {QueueNavigator} from '../../native/QueueNavigator';

/**
 * A single label/value line in the "Queue Information" block. Hoisted out of the
 * screen so React keeps a stable component type across renders.
 */
type DetailRowProps = {
  label: string;
  value: string;
  // Renders the value in the brand purple + bold (e.g. Doctor, Visit Sent).
  highlight?: boolean;
  // Hides the bottom hairline on the last row.
  last?: boolean;
};

const DetailRow = ({label, value, highlight, last}: DetailRowProps) => (
  <View style={[styles.detailRow, last && styles.detailRowLast]}>
    <Text style={styles.detailLabel}>{label}</Text>
    <Text style={[styles.detailValue, highlight && styles.detailValueHighlight]}>
      {value}
    </Text>
  </View>
);

// Fallback used only when neither an `item` prop nor native initialProperties
// are supplied (e.g. previewing the screen standalone).
const MOCK_PATIENT: QueueListItemProps = {
  queueNumber: 'Q-105',
  patientName: 'Anthony G',
  gender: 'M',
  age: 50,
  patientId: 'ID-987654jK',
  symptoms: ['Abdominal Pain', 'Nausea', 'Fever'],
  position: 2,
  status: 'nextInQueue',
  time: '02:59',
};

// Accepts the selected row either as a single `item` object (JS navigation from
// PatientQueue) or as the fields spread at the root (native initialProperties).
interface QueueDetailsProps extends Partial<QueueListItemProps> {
  item?: QueueListItemProps;
  // Called by the back arrow / "Back to Queue". No-op when launched standalone.
  onBack?: () => void;
}

/**
 * Queue Details screen.
 *
 * Registered as a native module ("QueueDetailsModule") and also rendered inline
 * by PatientQueue when a row is tapped. Reuses <QueueListItem /> for the patient
 * summary card at the top, then shows the queue metadata and primary actions.
 */
function QueueDetails(props: QueueDetailsProps): React.JSX.Element {
  const {item, onBack} = props;

  // Priority: explicit `item` prop → root-spread fields → mock.
  const patient: QueueListItemProps =
    item ?? (props.queueNumber ? (props as QueueListItemProps) : MOCK_PATIENT);

  // Back defaults to finishing the native Activity; `onBack` overrides it (e.g.
  // when previewing the screen inside another JS view).
  const handleBack = onBack ?? (() => QueueNavigator.close());

  const details: DetailRowProps[] = [
    {label: 'Queue Position', value: `#${patient.position}`},
    {label: 'Visit Sent', value: '10:45 AM', highlight: true},
    {label: 'Doctor', value: 'General Physician', highlight: true},
    {label: 'Place in Queue', value: '2 of 5'},
  ];

  return (
    <SafeAreaView style={styles.container}>
      {/* Toolbar is provided natively by QueueDetailsActivity. */}
      <ScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}>
        {/* Reused patient summary card */}
        <QueueListItem {...patient} />

        {/* Queue metadata */}
        <Text style={styles.sectionTitle}>Queue Information</Text>
        <View style={styles.detailCard}>
          {details.map((row, index) => (
            <DetailRow
              key={row.label}
              {...row}
              last={index === details.length - 1}
            />
          ))}
        </View>

        {/* Actions */}
        <TouchableOpacity style={styles.primaryButton} activeOpacity={0.8}>
          <Text style={styles.primaryButtonText}>View Visit Summary</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.secondaryButton}
          activeOpacity={0.8}
          onPress={handleBack}>
          <Text style={styles.secondaryButtonText}>Back to Queue</Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.white,
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 24,
  },
  sectionTitle: {
    fontFamily: FontFamily.lato,
    fontSize: 16,
    fontWeight: FontWeight.bold,
    color: Colors.textPrimary,
    marginTop: 24,
    marginBottom: 8,
  },
  detailCard: {
    marginBottom: 24,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: Colors.divider,
  },
  detailRowLast: {
    borderBottomWidth: 0,
  },
  detailLabel: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    color: Colors.textMuted,
  },
  detailValue: {
    fontFamily: FontFamily.lato,
    fontSize: 14,
    fontWeight: FontWeight.bold,
    color: Colors.textDark,
  },
  detailValueHighlight: {
    color: Colors.primary,
  },
  primaryButton: {
    backgroundColor: Colors.primary,
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
    marginBottom: 12,
  },
  primaryButtonText: {
    fontFamily: FontFamily.lato,
    fontSize: 15,
    fontWeight: FontWeight.bold,
    color: Colors.white,
  },
  secondaryButton: {
    backgroundColor: Colors.buttonSecondaryBg,
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
  },
  secondaryButtonText: {
    fontFamily: FontFamily.lato,
    fontSize: 15,
    fontWeight: FontWeight.bold,
    color: Colors.primary,
  },
});

export default QueueDetails;
