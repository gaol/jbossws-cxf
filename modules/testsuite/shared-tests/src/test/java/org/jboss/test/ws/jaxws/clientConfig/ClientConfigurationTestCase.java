/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.clientConfig;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Verifies client configuration setup
 *
 * @author alessio.soldano@jboss.com
 * @since 31-May-2012
 */
@ExtendWith(ArquillianExtension.class)
public class ClientConfigurationTestCase extends JBossWSTest
{
   private static final String DEFAULT_CONFIG_TESTS_SERVER = "default-config-tests";
   private static final String SERVER_DEPLOYMENT = "jaxws-clientConfig";
   private static final String IN_CONTAINER_CLIENT_DEPLOYMENT = "jaxws-clientConfig-inContainer-client";
   
   private final String baseURL = "http://" + getServerHost() + ":" + getServerPort(SHARED_TESTS_GROUP_QUALIFIER, DEFAULT_CONFIG_TESTS_SERVER);
   
   @ArquillianResource
   private Deployer deployer;
   
   @ArquillianResource
   private ContainerController containerController;
   
   @Deployment(name = IN_CONTAINER_CLIENT_DEPLOYMENT, order = 1, testable = false, managed = false)
   @TargetsContainer(DEFAULT_CONFIG_TESTS_SERVER)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, IN_CONTAINER_CLIENT_DEPLOYMENT + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.ws.common,org.jboss.as.server \n"))
         .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/clientConfig/META-INF/jaxws-client-config.xml"), "META-INF/jaxws-client-config.xml")
         .addClass(org.jboss.test.helper.ClientHelper.class)
         .addClass(org.jboss.test.helper.TestServlet.class)
         .addClass(org.jboss.test.ws.jaxws.clientConfig.CustomHandler.class)
         .addClass(org.jboss.test.ws.jaxws.clientConfig.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.clientConfig.Endpoint2.class)
         .addAsResource("org/jboss/test/ws/jaxws/clientConfig/jaxws-client-config.xml", "jaxws-client-config.xml")
         .addClass(org.jboss.test.ws.jaxws.clientConfig.Helper.class)
         .addClass(org.jboss.test.ws.jaxws.clientConfig.LogHandler.class)
         .addClass(org.jboss.test.ws.jaxws.clientConfig.RoutingHandler.class)
         .addClass(org.jboss.test.ws.jaxws.clientConfig.TestUtils.class)
         .addClass(org.jboss.test.ws.jaxws.clientConfig.UserHandler.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/clientConfig/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Deployment(name = SERVER_DEPLOYMENT, order = 2, testable = false, managed = false)
   @TargetsContainer(DEFAULT_CONFIG_TESTS_SERVER)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, SERVER_DEPLOYMENT + ".war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.clientConfig.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.EndpointImpl.class);
      return archive;
   }

   //TODO: this method should be annotated with @BeforeEach, and should not be called again in each test method
   // after https://github.com/arquillian/arquillian-core/issues/543 gets fixed
   public void startContainerAndDeploy() {
      if (!containerController.isStarted(DEFAULT_CONFIG_TESTS_SERVER)) {
         containerController.start(DEFAULT_CONFIG_TESTS_SERVER);
         deployer.deploy(SERVER_DEPLOYMENT);
         deployer.deploy(IN_CONTAINER_CLIENT_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig")
   public void testClientConfigurer() {
      startContainerAndDeploy();
      if (isIntegrationCXF()) {
         assertTrue(getHelper().testClientConfigurer());
      }
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(value = "jaxws-clientConfig-inContainer-client")
   public void testClientConfigurerInContainer() throws Exception {
      startContainerAndDeploy();
      if (isIntegrationCXF()) {
         assertEquals("1", runTestInContainer("testClientConfigurer"));
      }
   }
   
   /**
    * Verifies a custom client configuration can be read from conf file and set
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig")
   @WrapThreadContextClassLoader
   public void testCustomClientConfigurationFromFile() throws Exception {
      startContainerAndDeploy();
      assertTrue(getHelper().testCustomClientConfigurationFromFile());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig")
   @WrapThreadContextClassLoader
   public void testCustomClientConfigurationOnDispatchFromFile() throws Exception {
      startContainerAndDeploy();
      assertTrue(getHelper().testCustomClientConfigurationOnDispatchFromFile());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(value = "jaxws-clientConfig-inContainer-client")
   public void testCustomClientConfigurationFromFileInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testCustomClientConfigurationFromFile"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(value = "jaxws-clientConfig-inContainer-client")
   public void testCustomClientConfigurationOnDispatchFromFileInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatchFromFile"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig")
   @WrapThreadContextClassLoader
   public void testCustomClientConfigurationFromFileUsingFeature() throws Exception {
      startContainerAndDeploy();
      assertTrue(getHelper().testCustomClientConfigurationFromFileUsingFeature());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig")
   @WrapThreadContextClassLoader
   public void testCustomClientConfigurationFromFileUsingFeatureOnDispatch() throws Exception {
      startContainerAndDeploy();
      assertTrue(getHelper().testCustomClientConfigurationFromFileUsingFeatureOnDispatch());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(value = "jaxws-clientConfig-inContainer-client")
   public void testCustomClientConfigurationFromFileUsingFeatureInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testCustomClientConfigurationFromFileUsingFeature"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(value = "jaxws-clientConfig-inContainer-client")
   public void testCustomClientConfigurationFromFileUsingFeatureOnDispatchInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testCustomClientConfigurationFromFileUsingFeatureOnDispatch"));
   }
   
   /**
    * Verifies a client configuration can be changed after another one has been set
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig")
   @WrapThreadContextClassLoader
   public void testConfigurationChange() throws Exception {
      startContainerAndDeploy();
      assertTrue(getHelper().testConfigurationChange());
   }


   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig")
   @WrapThreadContextClassLoader
   public void testConfigurationChangeOnDispatch() throws Exception {
      startContainerAndDeploy();
      assertTrue(getHelper().testConfigurationChangeOnDispatch());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testConfigurationChangeInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testConfigurationChange"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testConfigurationChangeOnDispatchInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testConfigurationChangeOnDispatch"));
   }
   
   // -------------- default conf tests -------------------
   
   /**
    * Verifies the default client configuration from AS model is used
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testDefaultClientConfigurationInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testDefaultClientConfiguration"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testDefaultClientConfigurationOnDispatchInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testDefaultClientConfigurationOnDispatch"));
   }
   
   /**
    * Verifies the SEI class FQN default client configuration from AS model is used
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testSEIClassDefaultClientConfigurationInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testSEIClassDefaultClientConfiguration"));
   }
   //no corresponding test on Dispatch, as that has no SEI
   
   /**
    * Verifies the SEI class FQN client configuration from default conf file
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testSEIClassDefaultFileClientConfigurationInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testSEIClassDefaultFileClientConfiguration"));
   }
   //no corresponding test on Dispatch, as that has no SEI
   
   /**
    * Verifies a client configuration from AS model can be set
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testCustomClientConfigurationInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testCustomClientConfiguration"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testCustomClientConfigurationOnDispatchInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatch"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testCustomClientConfigurationUsingFeatureInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testCustomClientConfigurationUsingFeature"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-clientConfig-inContainer-client")
   public void testCustomClientConfigurationOnDispatchUsingFeatureInContainer() throws Exception {
      startContainerAndDeploy();
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatchUsingFeature"));
   }
   
   // -----------------------------------

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-clientConfig-client.jar") { {
         archive
               .addManifest()
               .addAsResource("org/jboss/test/ws/jaxws/clientConfig/jaxws-client-config.xml", "jaxws-client-config.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/clientConfig/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
   }
   
   private Helper getHelper() {
      Helper helper = new Helper();
      helper.setTargetEndpoint(baseURL + "/jaxws-clientConfig/EndpointImpl");
      return helper;
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL(baseURL
         + "/jaxws-clientConfig-inContainer-client?path=/jaxws-clientConfig/EndpointImpl&method=" + test
         + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
