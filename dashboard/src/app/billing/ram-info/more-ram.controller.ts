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
import {CodenvySubscription} from '../../../components/api/codenvy-subscription.factory';
import {CodenvyResourceLimits} from '../../../components/api/codenvy-resource-limits';

export class MoreRamController {
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Subscription API service.
   */
  private codenvySubscription: CodenvySubscription;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Notification service.
   */
  private cheNotification: any;

  /**
   * New RAM value.
   */
  value: number;
  /**
   * Current account's id (set from outside).
   */
  accountId: string;
  /**
   * Callback controller (set from outside).
   */
  callbackController: any;
  /**
   * Provided free RAM. Is retrieved from license details.
   */
  freeRAM: number;
  /**
   * Price of the resources. Is retrieved from package details.
   */
  price: number;
  /**
   * Amount of resources, that are paid for. Is retrieved from package details.
   */
  amount: string;
  /**
   * Minimum amount of resources, that can be bought. Is retrieved from package details.
   */
  minValue: number;
  /**
   * Maximum amount of resources, that can be bought. Is retrieved from package details.
   */
  maxValue: number;
  /**
   * Package with RAM type.
   */
  ramPackage: any;
  /**
   * Loading state of the dialog.
   */
  isLoading: boolean;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($mdDialog: angular.material.IDialogService, codenvySubscription: CodenvySubscription, lodash: any, cheNotification: any) {
    this.$mdDialog = $mdDialog;
    this.codenvySubscription = codenvySubscription;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.isLoading = true;

    this.getLicense();
    this.getPackages();
  }

  /**
   * Fetches the license details.
   */
  getLicense(): void {
    this.codenvySubscription.fetchLicense(this.accountId).then(() => {
      this.processLicense(this.codenvySubscription.getLicense(this.accountId));
    }, (error: any) => {
      if (error.status === 304) {
        this.processLicense(this.codenvySubscription.getLicense(this.accountId));
      }
    });
  }

  /**
   * Processes license, retrieves free resources info.
   *
   * @param license
   */
  processLicense(license: any): void {
    let details = license.resourcesDetails;
    let freeResources = this.lodash.find(details, (resource: any) => {
      return resource.providerId === 'free';
    });

    if (!freeResources) {
      this.freeRAM = 0;
      return;
    }

    let ram = this.lodash.find(freeResources.resources, (resource: any) => {
      return resource.type === CodenvyResourceLimits.RAM;
    });
    this.freeRAM = ram ? (ram.amount / 1024) : 0;
  }

  /**
   * Fetches the list of packages.
   */
  getPackages(): void {
    this.isLoading = true;
    this.codenvySubscription.fetchPackages().then(() => {
      this.isLoading = false;
      this.processPackages(this.codenvySubscription.getPackages());
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.processPackages(this.codenvySubscription.getPackages());
      }
    });
  }

  /**
   * Processes packages to get RAM resources details.
   *
   * @param packages list of packages
   */
  processPackages(packages: Array<any>): void {
    this.ramPackage = this.lodash.find(packages, (pack: any) => {
      return pack.type === CodenvyResourceLimits.RAM;
    });

    if (!this.ramPackage) {
      return;
    }

    let ramResource = this.lodash.find(this.ramPackage.resources, (resource: any) => {
      return resource.type === CodenvyResourceLimits.RAM;
    })

    if (!ramResource) {
      return;
    }

    this.price = ramResource.monthlyPrice;
    this.amount = ramResource.amount + ' ' + ramResource.unit;
    this.minValue = ramResource.minAmount / 1000; //TODO
    this.maxValue = ramResource.maxAmount / 1000; //TODO
    this.value = angular.copy(this.minValue);
  }

  /**
   * Hides the dialog.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Requests more RAM based on subscription state.
   */
  getMoreRAM(): void {
    this.isLoading = true;
    this.codenvySubscription.fetchActiveSubscription(this.accountId).then(() => {
      this.processSubscription(this.codenvySubscription.getActiveSubscription(this.accountId));
    }, (error:  any) => {
      this.processSubscription(this.codenvySubscription.getActiveSubscription(this.accountId));
    });
  }

  /**
   * Process active subscription if exists or creates new one,
   *
   * @param subscription
   */
  processSubscription(subscription: any): void {
    let ramValue = this.value * 1024; //TODO

    let promise;
    // check subscription exists:
    if (subscription) {
      let packages = angular.copy(subscription.packages);

      // try to update RAM package:
      let ramPackage = this.lodash.find(packages, (pckg: any) => {
        return pckg.templateId === this.ramPackage.id;
      });

      if (ramPackage) {
        let ramResource = this.lodash.find(ramPackage.resources, (resource: any) => {
          return resource.type === CodenvyResourceLimits.RAM;
        });
        // check RAM resource was defined:
        if (ramResource) {
          ramResource.amount += ramValue;
        } else { // process no RAM resource:
          ramPackage.resources.push(this.prepareRAMResource(ramValue));
        }
      } else { // process no RAM package:
        let resources = [this.prepareRAMResource(ramValue)];
        packages.push({resources: resources});
      }
      promise = this.codenvySubscription.updateSubscription(this.accountId, packages);
    } else { // process no active subscription:
      let packages = [];
      let resources = [this.prepareRAMResource(ramValue)];
      packages.push({resources: resources});
      promise = this.codenvySubscription.createSubscription(this.accountId, packages);
    }

    promise.then(() => {
      this.isLoading = false;
      this.callbackController.onRAMChanged();
      this.hide();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to add more RAM to account.');
    });
  }

  /**
   * Returns RAM resource based on provided RAM amount.
   *
   * @param value RAM amount
   * @returns any ram resource
   */
  prepareRAMResource(value: number): any {
    return {amount: value, unit: 'mb', type: CodenvyResourceLimits.RAM};
  }
}
