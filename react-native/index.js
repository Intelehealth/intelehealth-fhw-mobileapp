/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import {QueueCardSlideshow} from './src/components/QueueCard';
import PatientQueue from './src/screens/PatientQueue';
import QueueDetails from './src/screens/QueueDetails';
import HomeStatusBanner from './src/screens/HomeStatusBanner';
import VisitSummaryStatusBanner from './src/screens/VisitSummaryStatusBanner';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerComponent('QueueCardModule', () => QueueCardSlideshow);
AppRegistry.registerComponent('PatientQueueModule', () => PatientQueue);
AppRegistry.registerComponent('QueueDetailsModule', () => QueueDetails);
AppRegistry.registerComponent('StatusBannerModule', () => HomeStatusBanner);
AppRegistry.registerComponent(
  'VisitSummaryStatusBannerModule',
  () => VisitSummaryStatusBanner,
);
