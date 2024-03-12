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
package org.jboss.test.ws.jaxws.jbws2074.usecase6.client;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.jbws2074.usecase6.service.POJOIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * [JBWS-2074] Resource injection in jaxws endpoints and handlers
 * [JBWS-3846] Refactor creation process of jaxws handlers from predefined configurations
 *
 * @author ropalka@redhat.com
 * @author alessio.soldano@jboss.com
 * 
 * @since 03-Mar-2015
 */
@ExtendWith(ArquillianExtension.class)
public final class JBWS2074TestCase extends JBossWSTest
{
   private static final String WAR_DEPLOYMENT = "jaxws-jbws2074-usecase6";
   
   @ArquillianResource
   Deployer deployer;
   
   private static WebArchive getWarArchive() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, WAR_DEPLOYMENT + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.logging\n"))
         .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.DescriptorResourcesHandler.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.JavaResourcesHandler.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.ManualResourcesHandler.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase6.service.POJOIface.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase6.service.POJOImpl.class)
         .addAsWebInfResource("org/jboss/test/ws/jaxws/jbws2074/usecase6/service/endpoint-config.xml", "endpoint-config.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase6/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = WAR_DEPLOYMENT, testable = false, managed = false, order = 1)
   public static WebArchive createClientDeployment1() {
      return getWarArchive();
   }

   @Deployment(name = "jaxws-jbws2074-ear-usecase6", testable = false, managed = false, order = 2)
   public static EnterpriseArchive createClientDeployment() {
      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "jaxws-jbws2074-usecase6.ear");
      archive
         .addManifest()
         .addAsModule(getWarArchive())
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase6-ear/META-INF/application.xml"), "application.xml");
      return archive;
   }

   public void executeTest() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws2074-usecase6/Service";
      QName serviceName = new QName("http://ws.jboss.org/jbws2074", "POJOService");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      POJOIface port = (POJOIface)service.getPort(POJOIface.class);

      String retStr = port.echo("hello");

      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":Inbound:ManualResourcesHandler");
      expStr.append(":Inbound:JavaResourcesHandler");
      expStr.append(":Inbound:DescriptorResourcesHandler");
      expStr.append(":POJOImpl");
      expStr.append(":Outbound:DescriptorResourcesHandler");
      expStr.append(":Outbound:JavaResourcesHandler");
      expStr.append(":Outbound:ManualResourcesHandler");
      assertEquals(expStr.toString(), retStr);
   }

   @Test
   @RunAsClient
   public void testusecase6WithoutEar() throws Exception
   {
      try
      {
         deployer.deploy(WAR_DEPLOYMENT);
         executeTest();
      }
      finally
      {
         deployer.undeploy(WAR_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   public void testusecase6WithEar() throws Exception
   {
      try
      {
         deployer.deploy("jaxws-jbws2074-ear-usecase6");
         executeTest();
      }
      finally
      {
         deployer.undeploy("jaxws-jbws2074-ear-usecase6");
      }
   }

}
