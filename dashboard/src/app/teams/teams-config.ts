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

import {NavbarTeamsController} from './navbar-teams/navbar-teams.controller';
import {NavbarTeams} from './navbar-teams/navbar-teams.directive';

import {CreateTeamController} from './create-team/create-team.controller';
import {AddMemberController} from './invite-members/add-member/add-member.controller';
import {ListMembersController} from './invite-members/list-members.controller';
import {ListMembers} from './invite-members/list-members.directive';


import {ListTeamWorkspaces} from './team-details/team-workspaces/list-team-workspaces.directive';
import {ListTeamWorkspacesController} from './team-details/team-workspaces/list-team-workspaces.controller';

import {ListTeamMembers} from './team-details/team-members/list-team-members.directive';
import {ListTeamMembersController} from './team-details/team-members/list-team-members.controller';
import {MemberItem} from './team-details/team-members/member-item/member-item.directive';
import {MemberItemController} from './team-details/team-members/member-item/member-item.controller';

import {TeamDetailsController} from './team-details/team-details.controller';

export class TeamsConfig {

  constructor(register: any) {
    register.controller('NavbarTeamsController', NavbarTeamsController);
    register.directive('navbarTeams', NavbarTeams);
    register.controller('CreateTeamController', CreateTeamController);

    register.controller('AddMemberController', AddMemberController);
    register.controller('ListMembersController', ListMembersController);
    register.directive('listMembers', ListMembers);

    register.controller('ListTeamWorkspacesController', ListTeamWorkspacesController);
    register.directive('listTeamWorkspaces', ListTeamWorkspaces);

    register.controller('ListTeamMembersController', ListTeamMembersController);
    register.directive('listTeamMembers', ListTeamMembers);

    register.controller('MemberItemController', MemberItemController);
    register.directive('memberItem', MemberItem);

    register.controller('TeamDetailsController', TeamDetailsController);

    let locationProvider = {
      title: (params: any) => {
        return params.teamName;
      },
      templateUrl: 'app/teams/team-details/team-details.html',
      controller: 'TeamDetailsController',
      controllerAs: 'teamDetailsController'
    };

    // config routes
    register.app.config(function ($routeProvider: ng.route.IRouteProvider) {
      $routeProvider.accessWhen('/team/create', {
        title: 'New Team',
        templateUrl: 'app/teams/create-team/create-team.html',
        controller: 'CreateTeamController',
        controllerAs: 'createTeamController'
      })
      .accessWhen('/team/:teamName', locationProvider)
      .accessWhen('/team/:teamName/:page', locationProvider);
    });
  }
}
