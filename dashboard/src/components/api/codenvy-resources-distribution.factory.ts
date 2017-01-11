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

import {CodenvyResourceLimits} from './codenvy-resource-limits';

interface ICodenvyResourcesResource<T> extends ng.resource.IResourceClass<T> {
  distribute: any;
  getResources: any;
}

const RAM_RESOURCE_TYPE: string = 'RAM';

/**
 * This class is handling the team's resources management API.
 *
 * @author Ann Shumilova
 */
export class CodenvyResourcesDistribution {
  /**
   * Angular promise service.
   */
  private $q: ng.IQService;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Angular Resource service.
   */
  private $resource: ng.resource.IResourceService;
  /**
   * Client to make remote distribution resources API calls.
   */
  private remoteResourcesAPI: ICodenvyResourcesResource<any>;
  /**
   * Team resources with team's id as a key.
   */
  private teamResources: Map<string, any>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $resource: ng.resource.IResourceService, lodash: any) {
    this.$q = $q;
    this.$resource = $resource;
    this.lodash = lodash;

    this.teamResources = new Map();

    this.remoteResourcesAPI = <ICodenvyResourcesResource<any>>this.$resource('/api/organization/resource', {}, {
      distribute: {method: 'POST', url: '/api/organization/resource/:teamId'},
      getResources: {method: 'GET', url: '/api/resource/:teamId', isArray: true},
    });
  }

  /**
   * Distributes resources for pointed team.
   *
   * @param teamId id of team to distribute resources
   * @param resources resources to distribute
   * @returns {ng.IPromise<T>}
   */
  distributeResources(teamId: string, resources: Array<any>): ng.IPromise<any> {
     return this.remoteResourcesAPI.distribute({'teamId': teamId}, resources).$promise;
  }

  /**
   * Fetch distributed resources by team's id.
   *
   * @param teamId team id
   * @returns {ng.IPromise<any>}
   */
  fetchTeamResources(teamId: string): ng.IPromise<any> {
    let promise = this.remoteResourcesAPI.getResources({'teamId': teamId}).$promise;
    let resultPromise = promise.then((resources) => {
      this.teamResources.set(teamId, resources);
    });

    return resultPromise;
  }

  /**
   * Returns the list of team's permissions by team's id
   *
   * @param teamId team id
   * @returns {*} list of team permissions
   */
  getTeamResources(teamId: string): any {
    return this.teamResources.get(teamId);
  }

  /**
   * Returns team's resource limits by resource type.
   *
   * @param teamId id of team
   * @param type type of resource
   * @returns {any} resource limit
   */
  getTeamResourceByType(teamId: string, type: CodenvyResourceLimits): any {
    let resources = this.teamResources.get(teamId);
    if (!resources) {
      return null;
    }

    return this.lodash.find(resources, (resource: any) => {
      return resource.type === type.valueOf();
    });
  }

  /**
   * Returns the modified team's resources with pointed type and value.
   *
   * @param resources resources
   * @param type type of resource
   * @param value value to be set
   * @returns {any} modified
   */
  setTeamResourceLimitByType(resources: any, type: CodenvyResourceLimits, value: string): any {
    let resource = this.lodash.find(resources, (resource: any) => {
      return resource.type === type.valueOf();
    });

    if (!resource) {
      resource = {};
      resource.type = type.valueOf();
      resources.push(resource);
    }

    resource.amount = value;
    return resources;
  }
}
