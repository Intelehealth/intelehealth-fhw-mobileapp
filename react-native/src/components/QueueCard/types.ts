/**
 * Types for the QueueCard component.
 */

// Full data structure matching the Android data pipeline.
export interface QueuePatientData {
  queueNumber: string;
  patientName: string;
  gender: string;
  age: number;
  patientId: string;
  symptoms: string[];
  position: number;
  waitTimeMinutes: number;
  avatarUrl?: string;
}

// Props received when mounted from Android. When passed via a ReactFragment's
// initialProperties bundle, React Native injects the keys directly into the
// root props object, so every field is optional here.
export interface QueueCardProps {
  queueNumber?: string;
  patientName?: string;
  gender?: string;
  age?: number;
  patientId?: string;
  symptoms?: string[];
  position?: number;
  waitTimeMinutes?: number;
  avatarUrl?: string;
}
