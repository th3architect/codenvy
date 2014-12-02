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
package com.codenvy.im.artifacts;

import com.codenvy.im.agent.Agent;
import com.codenvy.im.agent.LocalAgent;
import com.codenvy.im.command.Command;
import com.codenvy.im.command.MacroCommand;
import com.codenvy.im.command.SimpleCommand;
import com.codenvy.im.command.UnpackCommand;
import com.codenvy.im.install.InstallOptions;
import com.codenvy.im.utils.Commons;
import com.codenvy.im.utils.Version;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class InstallManagerArtifact extends AbstractArtifact {
    private static final Logger LOG = LoggerFactory.getLogger(InstallManagerArtifact.class);
    public static final String NAME = "installation-manager";

    private static final String CODENVY_CLI_DIR_NAME = "codenvy-cli";
    private static final Path PATH_TO_UPDATE_CLI_SCRIPT = Paths.get("/home/codenvy-shared/codenvy-cli-update-script.sh");

    @Inject
    public InstallManagerArtifact() {
        super(NAME);
    }

    /** {@inheritDoc} */
    @Override
    public Version getInstalledVersion(String authToken) throws IOException {
        try (InputStream in = Artifact.class.getClassLoader().getResourceAsStream("codenvy/BuildInfo.properties")) {
            Properties props = new Properties();
            props.load(in);

            if (props.containsKey("version")) {
                return Version.valueOf((String)props.get("version"));
            } else {
                throw new IOException(this.getName());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getPriority() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getInstallInfo(InstallOptions installOptions) throws IOException {
        return new ArrayList<String>() {{
            add("Unpack update of installation manager");
            add("Update installation manager");
        }};
    }

    /** {@inheritDoc} */
    @Override
    public Command getInstallCommand(Version version, final Path pathToBinaries, InstallOptions installOptions) {
        try {
            int step = installOptions.getStep();

            final Path dirToUnpack = pathToBinaries.getParent().resolve("unpack");
            final Agent syncAgent = new LocalAgent();
            final Agent asyncAgent = new LocalAgent(true);

            final Path pathToNewVersionOfDaemon = dirToUnpack.resolve("daemon");
            final Path pathToNewVersionOfCliClient = dirToUnpack.resolve("cli");

            switch (step) {
                case 0:
                    final String imDaemonPackName = Commons.getBinaryFileName(this, version, null);
                    final String cliClientPackName = Commons.getBinaryFileName(this, version, "cli");

                    return new MacroCommand(new ArrayList<Command>() {{
                        add(new SimpleCommand(format("rm -rf %s", dirToUnpack.toAbsolutePath()), syncAgent, "Delete directory to unpack, if exist."));
                        add(new UnpackCommand(pathToBinaries, dirToUnpack, "Unpack downloaded installation manager"));
                        add(new UnpackCommand(dirToUnpack.resolve(imDaemonPackName), pathToNewVersionOfDaemon,
                                              "Unpack installation manager daemon"));
                        add(new UnpackCommand(dirToUnpack.resolve(cliClientPackName), pathToNewVersionOfCliClient,
                                              "Unpack installation manager cli client"));
                    }}, "Unpack downloaded installation manager");

                case 1:

                    final Path cliClientDir = Paths.get(format("%s/%s", installOptions.getCliUserHomeDir(), CODENVY_CLI_DIR_NAME));

                    final String contentOfUpdateCliScript = format("#!/bin/bash \n" +
                                                                   "rm -rf %1$s/* \n" +     // remove content of cli client dir
                                                                   "cp -r %2$s/* %1$s \n" + // copy update into the user's home dir
                                                                   "chmod +x %3$s \n" +     // set permissions to execute CLI client scripts
                                                                   "rm -f %4$s \n" +        // remove update script after all
                                                                   "",
                                                                   cliClientDir.toAbsolutePath(),
                                                                   pathToNewVersionOfCliClient.toAbsolutePath(),
                                                                   cliClientDir.resolve("bin/*").toAbsolutePath(),
                                                                   PATH_TO_UPDATE_CLI_SCRIPT.toAbsolutePath());

                    final Command updateCliClientCommand = new MacroCommand(new ArrayList<Command>() {{
                        add(new SimpleCommand(format("echo '%s' > %s ; ", contentOfUpdateCliScript, PATH_TO_UPDATE_CLI_SCRIPT.toAbsolutePath()),
                                              syncAgent, "Create script to update cli client"));

                        add(new SimpleCommand(format("chmod 775 %s ; ", PATH_TO_UPDATE_CLI_SCRIPT.toAbsolutePath()),
                                              syncAgent, "Set permissions to execute update script"));
                    }}, "Update installation manager CLI client");


                    final Command updateDaemonCommand =
                        new SimpleCommand(format("sleep 2 ; " +   // time to send response to CLI client before updating daemon
                                                 "sudo %1$s/installation-manager stop ; " +     // start stopping daemon
                                                 "sleep 10 ; " +                                // wait for finish stopping
                                                 "sudo kill -9 $(ps aux | grep [i]nstallation-manager | cut -d\" \" -f3) &>/dev/null ; " + // kill manager if stop doesn't work
                                                 "sudo rm -rf %1$s ; " +                         // remove directory with daemon
                                                 "sudo mkdir %1$s ; " +                          // create directory with daemon
                                                 "sudo cp -r %2$s/* %1$s ; " +                   // copy update into the directory with daemon
                                                 "sudo chmod 757 %1$s ; " +                      // make it possible to write files into the directory with daemon
                                                 "sudo chown -R codenvy:codenvy %1$s ; " +       // make the user 'codenvy' group 'codenvy' as an owner files within the directory
                                                 "sudo chmod +x %1$s/installation-manager ; " + // set permission to execute daemon script
                                                 "sudo %1$s/installation-manager start ; " +    // start daemon
                                                 "",
                                                 getInstalledPath().toAbsolutePath(),
                                                 pathToNewVersionOfDaemon.toAbsolutePath(),
                                                 dirToUnpack.toAbsolutePath()),
                                          asyncAgent, "Update installation manager daemon");

                    return new MacroCommand(new ArrayList<Command>() {{
                        add(updateCliClientCommand);
                        add(updateDaemonCommand);
                    }}, "Update installation manager");

                default:
                    throw new IllegalArgumentException(format("Step number %d is out of range", step));
            }
        } catch (URISyntaxException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected Path getInstalledPath() throws URISyntaxException {
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        return Paths.get(location.toURI()).getParent();
    }
}