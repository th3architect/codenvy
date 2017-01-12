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
import {IInvoice, CodenvyInvoices} from '../../../components/api/codenvy-invoices.factory';

export class ListInvoicesController {
  codenvyInvoices: CodenvyInvoices;
  cheNotification: any
  $filter: any;

  invoices: Array<IInvoice>;
  accountId: string;
  isLoading: boolean;
  filter: any;

  /**
   * @ngInject for Dependency injection
   */
  constructor (codenvyInvoices: CodenvyInvoices, cheNotification: any, $filter: any) {
    this.codenvyInvoices = codenvyInvoices;
    this.cheNotification = cheNotification;
    this.filter = {creationDate: ''};
    this.$filter = $filter;

    this.isLoading = true;
    this.codenvyInvoices.fetchInvoices(this.accountId).then(() => {
      this.invoices = this.codenvyInvoices.getInvoices(this.accountId);
      this.formatInvoicesDate();
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.invoices = this.codenvyInvoices.getInvoices(this.accountId);
        this.formatInvoicesDate();
      } else {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to load invoices.');
      }
    });
  }

  /**
   * Formats the invoices creation date.
   */
  formatInvoicesDate(): void {
    this.invoices.forEach((invoice: any) => {
      invoice.creationDate = this.$filter('date')(new Date(invoice.creationDate), 'dd-MMM-yyyy');
    });
  }
}
