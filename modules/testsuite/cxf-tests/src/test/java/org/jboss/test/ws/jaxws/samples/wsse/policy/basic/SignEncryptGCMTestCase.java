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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.IgnoreJdk;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security Policy sign & encrypt test case
 * using GCM algorithm suite
 *
 * @author alessio.soldano@jboss.com
 * @since 27-Feb-2012
 */
@RunWith(Arquillian.class)
public final class SignEncryptGCMTestCase extends JBossWSTest
{
   @Rule
   public IgnoreJdk ignoreOnIbm8 = IgnoreJdk.IBM8; //https://issues.jboss.org/browse/JBEAP-5200

   private static final String WS_DEPLOYMENT = "jaxws-samples-wsse-policy-sign-encrypt-gcm";
   private static final String SERVLET_DEPLOYMENT = "jaxws-samples-wsse-policy-sign-encrypt-gcm-client";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = WS_DEPLOYMENT, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, WS_DEPLOYMENT + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/gcm/WEB-INF/bob.jks"), "classes/bob.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/gcm/WEB-INF/bob.properties"), "classes/bob.properties")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/gcm/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/gcm/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/gcm/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/gcm/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = SERVLET_DEPLOYMENT, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, SERVLET_DEPLOYMENT + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.SignEncryptHelper.class)
         .addClass(org.jboss.wsf.test.ClientHelper.class)
         .addClass(org.jboss.wsf.test.CryptoCheckHelper.class)
         .addClass(org.jboss.wsf.test.TestServlet.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/alice.properties"), "classes/META-INF/alice.properties")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/alice.jks"), "classes/META-INF/alice.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/jaxws-client-config.xml"), "classes/META-INF/jaxws-client-config.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("SignEncryptGCMTestCase-client.jar") { {
      archive
         .addManifest()
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/alice.jks"), "alice.jks")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/alice.properties"), "alice.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   @OperateOnDeployment(WS_DEPLOYMENT)
   public void testClientSide() throws Exception
   {
      SignEncryptHelper helper = new SignEncryptHelper();
      helper.setTargetEndpoint(baseURL + "/jaxws-samples-wsse-policy-sign-encrypt-gcm");
      assertTrue(helper.testSignEncrypt());
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(WS_DEPLOYMENT)
   @WrapThreadContextClassLoader
   public void testClientSideUsingConfigProperties() throws Exception
   {
      SignEncryptHelper helper = new SignEncryptHelper();
      helper.setTargetEndpoint(baseURL + "/jaxws-samples-wsse-policy-sign-encrypt-gcm");
      assertTrue(helper.testSignEncryptUsingConfigProperties());
   }
   
//   public void testServerSide() throws Exception
//   {
//      URL url = new URL(baseURL +
//            "?path=/jaxws-samples-wsse-policy-sign-encrypt-gcm&method=testSignEncrypt&helper=" + SignEncryptHelper.class.getName());
//      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
//      assertEquals("1", br.readLine());
//   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVLET_DEPLOYMENT)
   public void testServerSideUsingConfigProperties() throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-samples-wsse-policy-sign-encrypt-gcm&method=testSignEncryptUsingConfigProperties&helper=" + SignEncryptHelper.class.getName());
      assertEquals("1", IOUtils.readAndCloseStream(url.openStream()));
   }
}
