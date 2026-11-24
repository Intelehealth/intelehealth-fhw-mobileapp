import {NativeModules} from 'react-native';

/** The JS wrapper reads NativeModules.QueueNavigator once, at import time. */
function loadWith(nativeModule: object | undefined) {
  (NativeModules as any).QueueNavigator = nativeModule;
  let wrapper: typeof import('../../src/native/QueueNavigator').QueueNavigator;
  jest.isolateModules(() => {
    wrapper = require('../../src/native/QueueNavigator').QueueNavigator;
  });
  return wrapper!;
}

afterEach(() => {
  delete (NativeModules as any).QueueNavigator;
});

describe('QueueNavigator JS bridge', () => {
  it('forwards every call to the native module', () => {
    const native = {
      openQueueDetails: jest.fn(),
      openPatientQueue: jest.fn(),
      dismissStatusBanner: jest.fn(),
      close: jest.fn(),
    };
    const navigator = loadWith(native);
    const row = {patientName: 'Asha'} as any;

    navigator.openQueueDetails(row);
    navigator.openPatientQueue();
    navigator.dismissStatusBanner();
    navigator.close();

    expect(native.openQueueDetails).toHaveBeenCalledWith(row);
    expect(native.openPatientQueue).toHaveBeenCalledTimes(1);
    expect(native.dismissStatusBanner).toHaveBeenCalledTimes(1);
    expect(native.close).toHaveBeenCalledTimes(1);
  });

  it('is a safe no-op when the native module is missing (standalone/dev)', () => {
    const navigator = loadWith(undefined);
    expect(() => {
      navigator.openQueueDetails({} as any);
      navigator.openPatientQueue();
      navigator.dismissStatusBanner();
      navigator.close();
    }).not.toThrow();
  });
});
