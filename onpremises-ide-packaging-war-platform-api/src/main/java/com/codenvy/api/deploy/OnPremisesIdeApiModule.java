/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.deploy;
/*
import com.codenvy.api.analytics.AnalyticsModule;
*/

import com.codenvy.api.dao.authentication.PasswordEncryptor;
import com.codenvy.api.dao.authentication.SSHAPasswordEncryptor;
import com.codenvy.api.dao.ldap.UserDaoImpl;
import com.codenvy.api.dao.mongo.AccountDaoImpl;
import com.codenvy.api.dao.mongo.MachineMongoDatabaseProvider;
import com.codenvy.api.dao.mongo.OrganizationMongoDatabaseProvider;
import com.codenvy.api.dao.mongo.RecipeDaoImpl;
import com.codenvy.api.dao.mongo.WorkspaceDaoImpl;
import com.codenvy.api.dao.util.ProfileMigrator;
import com.codenvy.api.factory.FactoryMongoDatabaseProvider;
import com.codenvy.auth.sso.client.EnvironmentContextResolver;
import com.codenvy.auth.sso.client.SSOContextResolver;
import com.codenvy.auth.sso.client.filter.ConjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.DisjunctionRequestFilter;
import com.codenvy.auth.sso.client.filter.NegationRequestFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentNumberFilter;
import com.codenvy.auth.sso.client.filter.PathSegmentValueFilter;
import com.codenvy.auth.sso.client.filter.RegexpRequestFilter;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.filter.RequestMethodFilter;
import com.codenvy.auth.sso.client.filter.UriStartFromRequestFilter;
import com.codenvy.auth.sso.server.RolesExtractor;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.auth.sso.server.organization.WorkspaceCreationValidator;
import com.codenvy.workspace.CreateWsRootDirInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.mongodb.client.MongoDatabase;
import com.palominolabs.metrics.guice.InstrumentationModule;

import org.eclipse.che.api.account.server.AccountService;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.auth.oauth.OAuthAuthorizationHeaderProvider;
import org.eclipse.che.api.core.notification.WSocketEventBusServer;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.core.rest.permission.PermissionManager;
import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.machine.server.recipe.PermissionsChecker;
import org.eclipse.che.api.machine.server.recipe.PermissionsCheckerImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeLoader;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.machine.server.recipe.providers.RecipeProvider;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.schedule.executor.ScheduleModule;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.docker.machine.ext.DockerMachineExtServerLauncher;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl;
import org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider;
import org.eclipse.che.security.oauth1.OAuthAuthenticatorAuthorizationHeaderProvider;
import org.eclipse.che.vfs.impl.fs.CleanableSearcherProvider;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.eclipse.che.vfs.impl.fs.MountPointCacheCleaner;
import org.eclipse.che.vfs.impl.fs.WorkspaceHashLocalFSMountStrategy;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.ServiceBindingHelper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.inject.Matchers.names;


/*
import com.codenvy.api.metrics.server.MetricModule;
import com.codenvy.api.resources.server.ResourcesManager;
import com.codenvy.api.resources.server.ResourcesService;
*/
/*
import com.codenvy.api.subscription.saas.server.ResourcesManagerImpl;
import com.codenvy.api.subscription.saas.server.SaasSubscriptionModule;
import com.codenvy.api.subscription.saas.server.billing.BillingModule;
*/
/*
import com.codenvy.sql.SQLModule;
<<<<<<< HEAD
*/
/*
import org.eclipse.che.api.builder.BuilderAdminService;
import org.eclipse.che.api.builder.BuilderSelectionStrategy;
import org.eclipse.che.api.builder.BuilderService;
import org.eclipse.che.api.builder.LastInUseBuilderSelectionStrategy;
*/
/*
import org.eclipse.che.api.factory.FactoryAcceptValidator;
import org.eclipse.che.api.factory.FactoryAcceptValidatorImpl;
import org.eclipse.che.api.factory.FactoryCreateValidator;
import org.eclipse.che.api.factory.FactoryCreateValidatorImpl;
import org.eclipse.che.api.factory.FactoryService;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.runner.RandomRunnerSelectionStrategy;
import org.eclipse.che.api.runner.RunnerAdminService;
import org.eclipse.che.api.runner.RunnerSelectionStrategy;
import org.eclipse.che.api.runner.RunnerService;
*/
/*
import org.eclipse.che.ide.ext.java.jdi.server.DebuggerService;
import org.eclipse.che.ide.ext.java.server.format.FormatService;
*/

/**
 * Guice container configuration file. Replaces old REST application composers and servlet context listeners.
 *
 * @author Max Shaposhnik
 */
@DynaModule
public class OnPremisesIdeApiModule extends AbstractModule {

    @Override
    protected void configure() {
        // < Copied from IDE3 api war

        bind(ApiInfoService.class);
//        bind(GitConnectionFactory.class).to(NativeGitConnectionFactory.class);
//        Multibinder.newSetBinder(binder(), SshKeyUploader.class).addBinding().to(GitHubKeyUploader.class);
//        bind(SshKeyProvider.class).to(SshKeyProviderImpl.class);
        bind(AuthenticationService.class);
        bind(WorkspaceService.class);
        bind(UserService.class);
        bind(UserProfileService.class);
        bind(AccountService.class);

        //recipe service
        bind(RecipeService.class);
        bind(PermissionsChecker.class).to(PermissionsCheckerImpl.class);

        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(ETagResponseFilter.class);
        bind(WSocketEventBusServer.class);

        install(new org.eclipse.che.api.core.rest.CoreRestModule());
        install(new org.eclipse.che.api.analytics.AnalyticsModule());
        //install(new org.eclipse.che.api.project.server.BaseProjectModule());
        install(new org.eclipse.che.api.vfs.server.VirtualFileSystemModule());
        /*
        install(new org.eclipse.che.api.factory.FactoryModule());
        */
        install(new com.codenvy.che.docs.DocsModule());
        install(new org.eclipse.che.api.machine.server.MachineModule());

        // Copied from IDE3 api war >

        //Temporary FS change
        final Multibinder<VirtualFileFilter> multibinder = Multibinder.newSetBinder(binder(),
                                                                                    VirtualFileFilter.class,
                                                                                    Names.named("vfs.index_filter"));
        multibinder.addBinding().toInstance(virtualFile -> !virtualFile.getPath().endsWith("/.codenvy/misc.xml"));
        bind(SearcherProvider.class).to(CleanableSearcherProvider.class);
        bind(MountPointCacheCleaner.Finalizer.class).asEagerSingleton();

        bind(LocalFSMountStrategy.class).to(WorkspaceHashLocalFSMountStrategy.class); // (RemoteDockerNode class want it)

        //oauth 1
        bind(org.eclipse.che.security.oauth1.OAuthAuthenticatorProvider.class);
        bind(OAuthAuthorizationHeaderProvider.class).to(OAuthAuthenticatorAuthorizationHeaderProvider.class);

        //oauth 2
        bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);
        bind(org.eclipse.che.api.auth.oauth.OAuthTokenProvider.class).to(OAuthAuthenticatorTokenProvider.class);

        //factory
        bind(com.mongodb.DB.class).annotatedWith(Names.named("mongo.db.organization"))
                                  .toProvider(com.codenvy.api.dao.mongo.OrganizationMongoDBProvider.class);

        bind(MongoDatabase.class).annotatedWith(Names.named("mongo.db.organization"))
                                 .toProvider(OrganizationMongoDatabaseProvider.class);

        bind(MongoDatabase.class).annotatedWith(Names.named("mongo.db.factory"))
                                 .toProvider(FactoryMongoDatabaseProvider.class);



        bind(org.eclipse.che.api.factory.server.FactoryStore.class).to(com.codenvy.factory.storage.mongo.MongoDBFactoryStore.class);
        bind(FactoryAcceptValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl.class);
        bind(FactoryCreateValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryCreateValidatorImpl.class);
        bind(FactoryEditValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryEditValidatorImpl.class);
        bind(FactoryService.class);
        //bind(com.codenvy.ide.factory.server.MessageService.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder =
                Multibinder.newSetBinder(binder(), org.eclipse.che.api.project.server.handlers.ProjectHandler.class);


        //user-workspace-account
        bind(PasswordEncryptor.class).toInstance(new SSHAPasswordEncryptor());
        /*
        bind(DB.class).toProvider(com.codenvy.api.dao.mongo.MongoDatabaseProvider.class);
        */

        bind(WorkspaceDao.class).to(WorkspaceDaoImpl.class);
        bind(UserDao.class).to(UserDaoImpl.class);
        bind(UserProfileDao.class).to(com.codenvy.api.dao.ldap.UserProfileDaoImpl.class);
        bind(PreferenceDao.class).to(com.codenvy.api.dao.mongo.PreferenceDaoImpl.class);
        //bind(MemberDao.class).to(LocalMemberDaoImpl.class);
        bind(AccountDao.class).to(AccountDaoImpl.class);
        bind(org.eclipse.che.api.auth.AuthenticationDao.class).to(com.codenvy.api.dao.authentication.AuthenticationDaoImpl.class);
//        bind(FactoryStore.class).to(InMemoryFactoryStore.class);
        bind(RecipeDao.class).to(RecipeDaoImpl.class);
        bind(RecipeLoader.class);
        Multibinder<String> recipeBinder = Multibinder.newSetBinder(binder(), String.class, Names.named("predefined.recipe.path"));
        recipeBinder.addBinding().toProvider(RecipeProvider.class);
        recipeBinder.addBinding().toInstance("predefined-recipes.json");

        bind(WorkspaceManager.class);
        /*
        bind(ResourcesManager.class).to(ResourcesManagerImpl.class);
        */

        bind(com.codenvy.service.http.WorkspaceInfoCache.class);
        bind(com.codenvy.workspace.listener.WsCacheCleanupSubscriber.class);

        bind(com.codenvy.service.password.PasswordService.class);

        //authentication

        bind(TokenValidator.class).to(com.codenvy.auth.sso.server.BearerTokenValidator.class);
        bind(com.codenvy.auth.sso.oauth.SsoOAuthAuthenticationService.class);
        bind(org.eclipse.che.security.oauth1.OAuthAuthenticationService.class);


        //SSO
        Multibinder<com.codenvy.api.dao.authentication.AuthenticationHandler> handlerBinder =
                Multibinder.newSetBinder(binder(), com.codenvy.api.dao.authentication.AuthenticationHandler.class);
        handlerBinder.addBinding().to(com.codenvy.auth.sso.server.ldap.LdapAuthenticationHandler.class);
        handlerBinder.addBinding().to(com.codenvy.auth.sso.server.OrgServiceAuthenticationHandler.class);


        Multibinder<RolesExtractor> rolesExtractorBinder = Multibinder.newSetBinder(binder(), RolesExtractor.class);
        rolesExtractorBinder.addBinding().to(com.codenvy.auth.sso.server.ldap.LdapRolesExtractor.class);

        rolesExtractorBinder.addBinding().to(com.codenvy.auth.sso.server.OrgServiceRolesExtractor.class);

        bind(UserCreator.class).to(com.codenvy.auth.sso.server.OrgServiceUserCreator.class);

        bind(WorkspaceCreationValidator.class).to(com.codenvy.auth.sso.server.OrgServiceWorkspaceValidator.class);


        bind(SSOContextResolver.class).to(EnvironmentContextResolver.class);

        bind(com.codenvy.auth.sso.client.TokenHandler.class)
                .to(com.codenvy.auth.sso.client.NoUserInteractionTokenHandler.class);

        bindConstant().annotatedWith(Names.named("auth.jaas.realm")).to("default_realm");
        bindConstant().annotatedWith(Names.named("auth.handler.default")).to("org");
        bindConstant().annotatedWith(Names.named("auth.sso.access_cookie_path")).to("/api/internal/sso/server");
        bindConstant().annotatedWith(Names.named("auth.sso.access_ticket_lifetime_seconds")).to(259200);
        bindConstant().annotatedWith(Names.named("auth.sso.bearer_ticket_lifetime_seconds")).to(3600);
        bindConstant().annotatedWith(Names.named("auth.sso.create_workspace_page_url")).to("/site/auth/create");
        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");
        bindConstant().annotatedWith(Names.named("auth.oauth.access_denied_error_page")).to("/site/login");
        bindConstant().annotatedWith(Names.named("error.page.workspace_not_found_redirect_url")).to("/site/error/error-tenant-name");
        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
                      .to("/site/error/error-cookies-disabled");

        bind(RequestFilter.class).toInstance(
                new DisjunctionRequestFilter(
                        new ConjunctionRequestFilter(
                                new UriStartFromRequestFilter("/api/factory"),
                                new RequestMethodFilter("GET"),
                                new DisjunctionRequestFilter(
                                        new UriStartFromRequestFilter("/api/factory/nonencoded"),
                                        new PathSegmentValueFilter(4, "image"),
                                        new PathSegmentValueFilter(4, "snippet"),
                                        new ConjunctionRequestFilter(
                                                //api/factory/{}
                                                new PathSegmentNumberFilter(3),
                                                new NegationRequestFilter(new UriStartFromRequestFilter("/api/factory/find"))
                                        ))
                        ),
                        new UriStartFromRequestFilter("/api/analytics/public-metric"),
                        new UriStartFromRequestFilter("/api/docs"),
                        new RegexpRequestFilter("^/api/builder/(\\w+)/download/(.+)$"),
                        new ConjunctionRequestFilter(
                                new UriStartFromRequestFilter("/api/oauth/authenticate"),
                                r -> isNullOrEmpty(r.getParameter("userId"))
                        )
                )
                                            );


        bindConstant().annotatedWith(Names.named("notification.server.propagate_events")).to("vfs,workspace");

        bind(com.codenvy.service.http.WorkspaceInfoCache.WorkspaceCacheLoader.class)
                .to(com.codenvy.service.http.WorkspaceInfoCache.ManagerCacheLoader.class);

        bind(com.codenvy.workspace.listener.VfsCleanupPerformer.class).to(com.codenvy.workspace.IdexVfsHelper.class);
        bind(com.codenvy.workspace.activity.websocket.WebsocketListenerInitializer.class);
        bind(com.codenvy.workspace.activity.RunActivityChecker.class).asEagerSingleton();
        /*
        bind(com.codenvy.workspace.activity.WsActivityListener.class).asEagerSingleton(); // temporarrily disabled, needs MemberhsipDao
        */
        bind(com.codenvy.workspace.listener.VfsStopSubscriber.class).asEagerSingleton();

        /*
        bind(com.codenvy.workspace.listener.FactoryWorkspaceResourceProvider.class).asEagerSingleton();
        */
        bind(ProfileMigrator.class).asEagerSingleton();

        /*
        install(new com.codenvy.workspace.interceptor.InterceptorModule()); // temporarrily disabled, needs MemberhsipDao
        */
        install(new com.codenvy.auth.sso.server.deploy.SsoServerInterceptorModule());
        install(new com.codenvy.auth.sso.server.deploy.SsoServerModule());

        install(new InstrumentationModule());
/*
        install(new SQLModule());

        install(new MetricModule());
        bind(ResourcesService.class);

        install(new SubscriptionModule());
        install(new OnPremisesSubscriptionModule());

        install(new SaasSubscriptionModule());
        install(new BillingModule());

        install(new SubscriptionModule());
        install(new AnalyticsModule());
        install(new ScheduleModule());
        */
        bind(org.eclipse.che.api.machine.server.MachineService.class);
        bind(org.eclipse.che.api.workspace.server.MachineClient.class).to(org.eclipse.che.api.machine.server.MachineClientImpl.class).in(
                Singleton.class);
        bind(org.eclipse.che.api.machine.server.dao.SnapshotDao.class).to(com.codenvy.api.dao.mongo.MongoSnapshotDaoImpl.class);
        bind(com.mongodb.DB.class).annotatedWith(Names.named("mongo.db.machine"))
                                  .toProvider(com.codenvy.api.dao.mongo.MachineMongoDBProvider.class);

        bind(MongoDatabase.class).annotatedWith(Names.named("mongo.db.machine"))
                                 .toProvider(MachineMongoDatabaseProvider.class);

        install(new ScheduleModule());
        //install(new CreditCardModule());
        //install(new GithubModule());

        /*
        install(new AnalyticsModule());
        */


        CreateWsRootDirInterceptor createWsRootDirInterceptor = new CreateWsRootDirInterceptor();
        requestInjection(createWsRootDirInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceManager.class),
                        names("createWorkspace"),
                        createWsRootDirInterceptor);

        bind(org.eclipse.che.plugin.docker.client.DockerConnector.class).to(com.codenvy.swarm.client.SwarmDockerConnector.class);

        install(new org.eclipse.che.plugin.docker.machine.ext.DockerTerminalModule());

        install(new FactoryModuleBuilder()
                        .implement(org.eclipse.che.api.machine.server.spi.Instance.class,
//                                   com.codenvy.router.PredictableMachineServerUrlInstance.class)
                                   com.codenvy.swarm.machine.SwarmInstance.class)
                        .implement(org.eclipse.che.api.machine.server.spi.InstanceProcess.class,
                                   org.eclipse.che.plugin.docker.machine.DockerProcess.class)
                        .implement(org.eclipse.che.plugin.docker.machine.DockerNode.class, com.codenvy.machine.RemoteDockerNode.class)
                        .build(org.eclipse.che.plugin.docker.machine.DockerMachineFactory.class));

        Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider> machineImageProviderMultibinder =
                Multibinder.newSetBinder(binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
        machineImageProviderMultibinder.addBinding()
                                       .to(org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.class);

        install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());

        bind(com.codenvy.machine.backup.WorkspaceFsBackupScheduler.class).asEagerSingleton();

        bindConstant().annotatedWith(Names.named(DockerMachineExtServerLauncher.START_EXT_SERVER_COMMAND))
                      .to("mkdir -p ~/che && unzip /mnt/che/ext-server.zip -d ~/che/ext-server && ~/che/ext-server/bin/catalina.sh start");

        bind(String.class).annotatedWith(Names.named("machine.docker.che_api.endpoint"))
                          .to(Key.get(String.class, Names.named("api.endpoint")));

//        install(new com.codenvy.router.MachineRouterModule());

        // TODO rebind to WorkspacePermissionManager after account is established
        bind(PermissionManager.class).annotatedWith(Names.named("service.workspace.permission_manager"))
                                     .to(DummyPermissionManager.class);
    }
}