/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardFactories.AddAction.RUN_COMMAND;

import com.google.inject.Inject;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.LoadingBehaviorPage;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactories;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckRunCommandFeatureTest {
  protected static final String PROJECT_NAME = generate("project", 4);
  private static final String FACTORY_NAME = generate("factory", 4);

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Dashboard dashboard;
  @Inject protected DashboardFactories dashboardFactories;
  @Inject private Ide ide;
  @Inject private LoadingBehaviorPage loadingBehaviorPage;
  @Inject private Loader loader;
  @Inject protected Wizard wizard;
  @Inject private Menu menu;
  @Inject private TestWorkspace testWorkspace;
  @Inject private DefaultTestUser user;
  @Inject private Consoles consoles;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestFactoryServiceClient factoryServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.deleteFactoryWorkspaces(testWorkspace.getName(), user.getName());
    factoryServiceClient.deleteFactory(FACTORY_NAME);
  }

  @Test
  public void checkRunCommandFeatureTest() throws ExecutionException, InterruptedException {
    // wait the jdt.ls server is started
    consoles.waitExpectedTextIntoConsole("Initialized language server");
    createProject(PROJECT_NAME);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    dashboard.open();
    dashboard.selectFactoriesOnDashbord();
    dashboardFactories.clickOnAddFactoryBtn();
    dashboardFactories.selectWorkspaceForCreation(testWorkspace.getName());
    dashboardFactories.setFactoryName(FACTORY_NAME);
    dashboardFactories.clickOnCreateFactoryBtn();
    dashboardFactories.selectAction(RUN_COMMAND);
    enterParamValueOnDashboardFactories();
    dashboardFactories.clickAddOnAddAction();
    dashboardFactories.clickOnOpenFactory();

    String currentWin = seleniumWebDriver.getWindowHandle();
    seleniumWebDriverHelper.switchToNextWindow(currentWin);

    loadingBehaviorPage.waitWhileLoadPageIsClosed();
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    projectExplorer.waitItem(PROJECT_NAME);
    consoles.waitExpectedTextIntoConsole(BUILD_SUCCESS);
  }

  private void createProject(String projectName) {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.waitCreateProjectWizardForm();
    wizard.typeProjectNameOnWizard(projectName);
    selectSample();
    wizard.clickCreateButton();
    loader.waitOnClosed();
    wizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
    loader.waitOnClosed();
  }

  protected void selectSample() {
    wizard.selectSample(WEB_JAVA_SPRING);
  }

  protected void enterParamValueOnDashboardFactories() {
    String nameBuildCommand = PROJECT_NAME + ": build and run";
    dashboardFactories.enterParamValue(nameBuildCommand);
  }
}
