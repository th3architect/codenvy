/*
 *  [2015] - [2016] Codenvy, S.A.
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
import {CodenvyResourcesDistribution} from './../../../components/api/codenvy-resources-distribution.factory';
import {CodenvyResourceLimits} from './../../../components/api/codenvy-resource-limits';

export class RamInfoController {
  $mdDialog: ng.material.IDialogService;
  codenvyResourcesDistribution: CodenvyResourcesDistribution;
  lodash: any;
  /**
   * Current account id, comes from external component.
   */
  accountId: string;
  totalRAM: number;
  usedRAM: number;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($mdDialog: ng.material.IDialogService, codenvyResourcesDistribution: CodenvyResourcesDistribution, lodash: any) {
    this.$mdDialog = $mdDialog;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;
    this.lodash = lodash;

    this.getRamInfo();
  }

  getRamInfo() {
    this.codenvyResourcesDistribution.fetchTeamResources(this.accountId).then(() => {
      let resources = this.codenvyResourcesDistribution.getTeamResources(this.accountId);
      this.totalRAM = this.getRamValue(resources);
    }, (error: any) => {
      if (error.status === 304) {
        let resources = this.codenvyResourcesDistribution.getTeamResources(this.accountId);
        this.totalRAM = this.getRamValue(resources);
      }
    });

    this.codenvyResourcesDistribution.fetchUsedTeamResources(this.accountId).then(() => {
      let resources = this.codenvyResourcesDistribution.getUsedTeamResources(this.accountId);
      this.usedRAM = this.getRamValue(resources);
    }, (error: any) => {
      if (error.status === 304) {
        let resources = this.codenvyResourcesDistribution.getUsedTeamResources(this.accountId);
        this.usedRAM = this.getRamValue(resources);
      }
    });
  }

  /**
   *
   * @param resources
   */
  getRamValue(resources: Array<any>): number {
    if (!resources || resources.length === 0) {
      return 0;
    }

    let ram = this.lodash.find(resources, (resource: any) => {
      return resource.type === CodenvyResourceLimits.RAM;
    });
    return ram ? (ram.amount / 1024) : 0;
  }

  /**
   * Shows popup
   */
  getMoreRAM(): void {
    this.$mdDialog.show({
      controller: 'MoreRamController',
      controllerAs: 'moreRamController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        accountId: this.accountId,
        totalRAM: this.totalRAM,
        usedRAM: this.usedRAM,
        callbackController: this
      },
      templateUrl: 'app/billing/ram-info/more-ram-dialog.html'
    });
  }

  onRAMChanged(): void {
    this.getRamInfo();
  }
}
