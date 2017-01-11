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
import {CodenvyTeamRoles} from '../../../components/api/codenvy-team-roles';
import {CodenvyTeam} from '../../../components/api/codenvy-team.factory';

/**
 * @ngdoc controller
 * @name teams.invite.members:ListMembersController
 * @description This class is handling the controller for the list of invited members.
 * @author Ann Shumilova
 */
export class ListMembersController {

  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * No members selected.
   */
  private isNoSelected: boolean;
  /**
   * Bulk operation checked state.
   */
  private isBulkChecked: boolean;
  /**
   * Status of selected members.
   */
  private membersSelectedStatus: any;
  /**
   * Number of selected members.
   */
  private membersSelectedNumber: number;
  /**
   * Members order by value.
   */
  private membersOrderBy: string;
  /**
   * Member roles by email.
   */
  private memberRoles: any;
  /**
   * List of members to be invited.
   */
  private members: Array<any>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: angular.material.IDialogService, lodash: any, codenvyTeam: CodenvyTeam) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.codenvyTeam = codenvyTeam;

    this.isNoSelected = true;
    this.isBulkChecked = false;
    this.membersSelectedStatus = {};
    this.membersSelectedNumber = 0;
    this.membersOrderBy = 'email';
    this.memberRoles = {};
  }

  /**
   * Forms the list of members.
   */
  buildMembersList(): void {
    this.memberRoles = {};
    this.members.forEach((member: any) => {
      let roles = {
        isTeamMember: member.roles.indexOf(CodenvyTeamRoles.TEAM_MEMBER) >= 0,
        isTeamAdmin: member.roles.indexOf(CodenvyTeamRoles.TEAM_ADMIN) >= 0
      };
      this.memberRoles[member.email] = roles;
    });
  }

  /**
   * Handler the roles changes and updates member's roles.
   *
   * @param member member
   */
  onRolesChanged(member: any): void {
    let roles = [];
    let rolesInfo = this.memberRoles[member.email];
    if (rolesInfo.isTeamMember) {
      roles.push(CodenvyTeamRoles.TEAM_MEMBER);
    }

    if (rolesInfo.isTeamAdmin) {
      roles.push(CodenvyTeamRoles.TEAM_ADMIN);
    }

    member.roles = roles;
  }

  /**
   * Update members selected status
   */
  updateSelectedStatus(): void {
    this.membersSelectedNumber = 0;
    this.isBulkChecked = !!this.members.length;
    this.members.forEach((member: any) => {
      if (this.membersSelectedStatus[member.email]) {
        this.membersSelectedNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllMembers();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllMembers();
    this.isBulkChecked = true;
  }

  /**
   * Check all members in list.
   */
  selectAllMembers(): void {
    this.membersSelectedNumber = this.members.length;
    this.members.forEach((member: any) => {
      this.membersSelectedStatus[member.email] = true;
    });
  }

  /**
   * Uncheck all members in list
   */
  deselectAllMembers(): void {
    this.membersSelectedStatus = {};
    this.membersSelectedNumber = 0;
  }

  /**
   * Adds member to the list.
   *
   * @param user
   * @param roles
   */
  addMembers(users: Array<any>, roles: Array<any>): void {
    users.forEach((user: any) => {
      user.roles = roles;
      this.members.push(user);
    });
    this.buildMembersList();
  }

  /**
   * Shows dialog to add new member.
   *
   * @param $event
   */
  showAddDialog($event: MouseEvent): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'MemberDialogController',
      controllerAs: 'memberDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        members: this.members,
        member: null,
        callbackController: this
      },
      templateUrl: 'app/teams/member-dialog/member-dialog.html'
    });
  }

  /**
   * Removes selected members.
   */
  removeSelectedMembers(): void {
    this.lodash.remove(this.members, (member: any) => {
      return this.membersSelectedStatus[member.email];
    });
    this.buildMembersList();
    this.deselectAllMembers();
    this.isBulkChecked = false;
  }
}
