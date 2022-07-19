// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  SOCKET_ENDPOINT: 'https://hireplusplus-socketio-vljhcfbbca-uc.a.run.app',
  PEERJS_HOST: 'hireplusplus-peerjs-vljhcfbbca-uc.a.run.app',
  PEERJS_PORT: 443,
  PEERJS_PATH: '/peerjs/myapp',
  // SOCKET_ENDPOINT: 'http://localhost:3000',
  // PEERJS_HOST: '/',
  // PEERJS_PORT: 3001,
  // PEERJS_PATH: '/peerjs/myapp',
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
