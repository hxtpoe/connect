/*jshint unused: vars */
require.config({
    paths: {
        angular: '../bower_components/angular/angular',
        angularRoute: '../bower_components/angular-route/angular-route',
        angularCookies: '../bower_components/angular-cookies/angular-cookies',
        angularSanitize: '../bower_components/angular-sanitize/angular-sanitize',
        angularResource: '../bower_components/angular-resource/angular-resource',
        sassbootstrap: '../bower_components/sass-bootstrap/dist/js/bootstrap',
        'sass-bootstrap': '../bower_components/sass-bootstrap/dist/js/bootstrap',
        jquery: '../bower_components/jquery/dist/jquery',
        'angular-scenario': '../bower_components/angular-scenario/angular-scenario',
        'angular-sanitize': '../bower_components/angular-sanitize/angular-sanitize',
        'angular-route': '../bower_components/angular-route/angular-route',
        'angular-resource': '../bower_components/angular-resource/angular-resource',
        'angular-mocks': '../bower_components/angular-mocks/angular-mocks',
        'angular-cookies': '../bower_components/angular-cookies/angular-cookies'
    },
    shim: {
        angular: {
            exports: 'angular'
        },
        angularRoute: [
            'angular'
        ],
        angularCookies: [
            'angular'
        ],
        angularSanitize: [
            'angular'
        ],
        angularResource: [
            'angular'
        ],
        angularMocks: {
            deps: [
                'angular'
            ],
            exports: 'angular.mock'
        }
    },
    priority: [
        'angular'
    ]
});

//http://code.angularjs.org/1.2.1/docs/guide/bootstrap#overview_deferred-bootstrap
window.name = 'NG_DEFER_BOOTSTRAP!';

require([
    'angular',
    'app',
    'angularRoute',
    'angularCookies',
    'angularSanitize',
    'angularResource'
  ], function (angular, app, ngRoutes, ngCookies, ngSanitize, ngResource) {
    'use strict';
    /* jshint ignore:start */
    var $html = angular.element(document.getElementsByTagName('html')[0]);
    /* jshint ignore:end */
    angular.element().ready(function () {
        angular.resumeBootstrap([app.name]);
      });
  });