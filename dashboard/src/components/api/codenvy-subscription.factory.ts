/*
 *  [2015] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
'use strict';
import IResource = angular.resource.IResource;

/**
 * This class is handling the credit card API.
 * @author Ann Shumilova
 */
export class CodenvySubscription {
  $resource: ng.resource.IResourceService
  $q: ng.IQService;
  $log: ng.ILogService;

  subscriptionsPerAccount: Map<string, any>;
  licensesPerAccount: Map<string, any>;
  packages: Array<any>;

  remoteSubscriptionAPI: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($resource: ng.resource.IResourceService, $q: ng.IQService, $log: ng.ILogService) {
    // keep resource
    this.$resource = $resource;
    this.$q = $q;
    this.$log = $log;

    this.subscriptionsPerAccount = new Map();
    this.licensesPerAccount = new Map();
    this.packages = [];

    // remote call
    this.remoteSubscriptionAPI = this.$resource('/api/subscription/:accountId', {}, {
      getActiveSubscription: {method: 'GET', url: '/api/subscription/:accountId'},
      createSubscription: {method: 'POST', url: '/api/subscription/:accountId'},
      updateSubscription: {method: 'PUT', url: '/api/subscription/:accountId'},
      getLicense: {method: 'GET', url: '/api/license/account/:accountId'},
      getPackages: {method: 'GET', url: '/api/package', isArray: true}
    });

  }

  /**
   * Fetch active subscription by account id.
   *
   * @param accountId {string}
   * @return {ng.IPromise<any>}
   */
  fetchActiveSubscription(accountId: string): ng.IPromise<any> {
    let promise = this.remoteSubscriptionAPI.getActiveSubscription({accountId: accountId}).$promise;

    let resultPromise = promise.then((data: any) => {
      this.subscriptionsPerAccount.set(accountId, data);
    });
    return resultPromise;
  }

  /**
   * Returns active subscription for pointed account if exists.
   *
   * @param accountId account's id
   * @returns {any}
   */
  getActiveSubscription(accountId: string): any {
    return this.subscriptionsPerAccount.get(accountId);
  }

  /**
   * Updates active subscription.
   *
   * @param accountId {string} account's id
   * @param subscription subscription to be updated
   * @return {IPromise<{method, url}|any|void|PromiseLike<void>|IDBRequest>}
   */
  updateSubscription(accountId: string, subscription: any): ng.IPromise<any> {
    return this.remoteSubscriptionAPI.updateSubscription({accountId: accountId}, subscription).$promise;
  }

  /**
   * Creates new subscription.
   *
   * @param accountId {string} account's id
   * @param subscription subscription to be created
   * @return {IPromise<{method, url}|any|void|PromiseLike<void>|IDBRequest>}
   */
  createSubscription(accountId: string, subscription: any): ng.IPromise<any> {
    return this.remoteSubscriptionAPI.createSubscription({accountId: accountId}, subscription).$promise;
  }

  /**
   * Fetch license by account id.
   *
   * @param accountId account's id
   * @returns {PromiseLike<TResult>|Promise<TResult>}
   */
  fetchLicense(accountId: string): ng.IPromise<any> {
    let promise = this.remoteSubscriptionAPI.getLicense({accountId: accountId}).$promise;

    let resultPromise = promise.then((data: any) => {
      this.licensesPerAccount.set(accountId, data);
    });

    return resultPromise;
  }

  /**
   * Returns license for pointed account.
   *
   * @param accountId account's id
   * @returns {any}
   */
  getLicense(accountId: string): any {
    return this.licensesPerAccount.get(accountId);
  }

  /**
   * Fetches defined subscription packages.
   *
   * @returns {PromiseLike<TResult>|Promise<TResult>}
   */
  fetchPackages(): ng.IPromise<any> {
    let promise = this.remoteSubscriptionAPI.getPackages().$promise;

    let resultPromise = promise.then((data: any) => {
      this.packages = data;
    });

    return resultPromise;
  }

  /**
   * Returns the list of defined packages.
   *
   * @returns {Array<any>}
   */
  getPackages(): Array<any> {
    return this.packages;
  }
}
