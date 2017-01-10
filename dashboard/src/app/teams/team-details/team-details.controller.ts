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
import {CodenvyTeam} from '../../../components/api/codenvy-team.factory';
import {CodenvyResourcesDistribution} from '../../../components/api/codenvy-resources-distribution.factory';
import {CodenvyResourceLimits} from '../../../components/api/codenvy-resource-limits';

/**
 * Controller for a managing team details.
 *
 * @author Ann Shumilova
 */
export class TeamDetailsController {
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Team resources API interaction.
   */
  private codenvyResourcesDistribution: CodenvyResourcesDistribution;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Current team's name. Comes from route path params.
   */
  private teamName: string;
  /**
   * Current team's data.
   */
  private team: any;
  /**
   * New team's name (for renaming widget).
   */
  private newName: string;
  /**
   * If <code>true</code> - team with pointed name doesn't exist or cannot be accessed.
   */
  private invalidTeam: boolean;
  /**
   * Index of the selected tab.
   */
  private selectedTabIndex: number;
  /**
   * Team limits.
   */
  private limits: any;
  /**
   * Copy of limits before letting to modify, to be able to compare.
   */
  private limitsCopy: any;
  /**
   * Page loading state.
   */
  private isLoading: boolean;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, codenvyResourcesDistribution: CodenvyResourcesDistribution, $route: ng.route.IRouteService, $location: ng.ILocationService,
              $mdDialog: angular.material.IDialogService, cheNotification: any) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;
    this.teamName = $route.current.params.teamName;
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;

    let page = $route.current.params.page;
    if (!page) {
      $location.path('/team/' + this.teamName);
    } else {
      this.selectedTabIndex = 0;
      switch (page) {
        case 'settings':
          this.selectedTabIndex = 0;
          break;
        case 'developers':
          this.selectedTabIndex = 1;
          break;
        case 'workspaces':
          this.selectedTabIndex = 2;
          break;
        default:
          $location.path('/team/' + this.teamName);
          break;
      }
    }

    this.fetchTeamDetails();
  }

  /**
   * Fetches the team's details by it's name.
   */
  fetchTeamDetails(): void {
    this.team  = this.codenvyTeam.getTeamByName(this.teamName);
    this.newName = angular.copy(this.teamName);
    if (!this.team) {
      this.codenvyTeam.fetchTeamByName(this.teamName).then((team) => {
        this.team = team;
        this.fetchLimits();
      }, (error: any) => {
        this.invalidTeam = true;
      });
    } else {
      this.fetchLimits();
    }
  }

  fetchLimits(): void {
    this.isLoading = true;
    this.codenvyResourcesDistribution.fetchTeamResources(this.team.id).then(() => {
      this.isLoading = false;
      this.processResources(this.codenvyResourcesDistribution.getTeamResources(this.team.id));
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.processResources(this.codenvyResourcesDistribution.getTeamResources(this.team.id));
      }
    });
  }

  processResources(resources): void {
    let ramLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RAM);
    let workspaceLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.WORKSPACE);
    let runtimeLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RUNTIME);

    this.limits = {};
    this.limits.workspaceCap = workspaceLimit ? workspaceLimit.amount : undefined;
    this.limits.runtimeCap = runtimeLimit ? runtimeLimit.amount : undefined;
    this.limits.ramCap = ramLimit ? ramLimit.amount : undefined;
    this.limitsCopy = angular.copy(this.limits);
  }

  /**
   * Confirms and performs team's deletion.
   *
   * @param event
   */
  deleteTeam(event: MouseEvent): void {
    let confirm = this.$mdDialog.confirm()
      .title('Would you like to delete team \'' + this.team.name + '\'?')
      .ariaLabel('Delete team')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      let promise = this.codenvyTeam.deleteTeam(this.team.id);
      promise.then(() => {
        this.$location.path('/workspaces');
        this.codenvyTeam.fetchTeams();
      }, (error: any) => {
        this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Team deletion failed.');
      });

      return promise;
    });
  }

  /**
   * Update team's details.
   *
   * @param invalid <true> if invalid data entered
   */
  updateTeamName(invalid: boolean): void {
    if (!invalid && this.newName && this.team && this.newName !== this.team.name) {
      this.team.name = this.newName;
      this.codenvyTeam.updateTeam(this.team).then(() => {
        this.codenvyTeam.fetchTeams().then(() => {
          this.$location.path('/team/' + this.newName);
        });
      }, (error: any) => {
        this.cheNotification.showError((error.data && error.data.message !== null) ? error.data.message : 'Rename team failed.');
      });
    }
  }

  updateLimits(invalid: boolean): void {
    if (invalid || !this.team || !this.limits || angular.equals(this.limits, this.limitsCopy)) {
      return;
    }

    let ramLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RAM);
    let workspaceLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.WORKSPACE);
    let runtimeLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RUNTIME);

    let resources = this.codenvyResourcesDistribution.getTeamResources(this.team.id);
    resources = angular.copy(resources);
    resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.RAM, this.limits.ramCap);
    resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.WORKSPACE, this.limits.workspaceCap);
    resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.RUNTIME, this.limits.runtimeCap);

    this.isLoading = true;
    this.codenvyResourcesDistribution.distributeResources(this.team.id, resources).then(() => {
      this.fetchLimits();
    }, (error: any) => {
      this.fetchLimits();
      this.cheNotification.showError((error.data && error.data.message !== null) ? error.data.message : 'Failed to update team resource limits.');
    });
  }
}
