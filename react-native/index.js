/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import QueueCard from './src/components/QueueCard';
import PatientQueue from './src/screens/PatientQueue';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerComponent('QueueCardModule', () => QueueCard);
AppRegistry.registerComponent('PatientQueueModule', () => PatientQueue);
