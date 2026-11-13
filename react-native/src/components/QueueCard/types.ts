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
  speciality?: string;
  // ISO-8601 instant the patient is expected to be seen (server "etaAt"). The
  // same absolute instant the queue list uses, so the home card counts the wait
  // time down live and stays in sync with the listing instead of showing a
  // static minutes snapshot.
  etaAt?: string;
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
  speciality?: string;
  etaAt?: string;
  // Shared clock tick (ms) pushed by the slideshow so every card recomputes its
  // live wait time from the same instant with a single timer. Absent when a card
  // is rendered standalone, in which case it falls back to Date.now().
  now?: number;
}

// Root props of the QueueCardModule slideshow: the patient list delivered by
// QueueCardUpdater.toBundle(List) on the Android side.
export interface QueueCardSlideshowProps {
  patients?: QueueCardProps[];
}
