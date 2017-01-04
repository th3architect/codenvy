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
import {CodenvyTeam} from '../../../../components/api/codenvy-team.factory';

/**
 * Controller for a team item.
 *
 * @author Ann Shumilova
 */
export class TeamItemController {
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Team details (the value is set in directive attributes).
   */
  private team: any;
  /**
   * Callback needed to react on teams updation (the value is set in directive attributes).
   */
  private onUpdate: Function;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, codenvyTeam: CodenvyTeam, $mdDialog: angular.material.IDialogService, cheNotification: any) {
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.codenvyTeam = codenvyTeam;
    this.cheNotification = cheNotification;
  }

  /**
   * Redirect to factory details.
   */
  redirectToTeamDetails(page: string) {
    this.$location.path('/team/' + this.team.name + (page ? '/' + page : ''));
  }

  /**
   * Removes team after confirmation.
   */
  removeTeam(): void {
    this.confirmRemoval().then(() => {
      this.codenvyTeam.deleteTeam(this.team.id).then(() => {
        this.onUpdate();
      }, (error: any) => {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete team ' + this.team.name);
      });
    });
  }

  /**
   * Shows dialog to confirm the current team removal.
   *
   * @returns {angular.IPromise<any>}
   */
  confirmRemoval(): ng.IPromise<any> {
    let confirm = this.$mdDialog.confirm()
      .title('Do you want to delete team ' + this.team.name + ' ?')
      .ariaLabel('Remove teams')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }
}

