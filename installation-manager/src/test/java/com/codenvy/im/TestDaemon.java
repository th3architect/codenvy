/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.im;

import com.codenvy.im.restlet.InstallationManagerService;
import com.codenvy.im.restlet.RestletClientFactory;

import org.testng.annotations.Test;

/**
 * @author Dmytro Nochevnov
 */
public class TestDaemon {
    @Test
    public void testConnection() throws Exception {
        Daemon.start();
        InstallationManagerService installationManagerServiceProxy = RestletClientFactory.createServiceProxy(InstallationManagerService.class);

        installationManagerServiceProxy.getUpdateServerEndpoint();

        Daemon.stop();
    }
}