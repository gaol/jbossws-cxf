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
package org.jboss.test.ws.jaxws.samples.httpbinding;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.http.HTTPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Element;

/**
 * Test HTTP Binding
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Apr-2007
 */
@ExtendWith(ArquillianExtension.class)
public class HttpJAXBTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-httpbinding-jaxb.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.httpbinding.ProviderBeanJAXB.class)
               .addClass(org.jboss.test.ws.jaxws.samples.httpbinding.UserType.class)
               .addClass(org.jboss.test.ws.jaxws.samples.httpbinding.WebServiceEndpoint.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/httpbinding/jaxb/META-INF/permissions.xml"), "permissions.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/httpbinding/shared/wsdl/HttpBinding.wsdl"), "wsdl/HttpBinding.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/httpbinding/jaxb/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/ProviderEndpoint?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
   }

   @Test
   @RunAsClient
   public void testProviderDispatch() throws Exception
   {
      Dispatch<Object> dispatch = createDispatch("ProviderEndpoint");

      UserType user = new UserType();
      user.setString("Kermit");
      user.setQname(new QName("TheFrog"));
      UserType userRes = (UserType)dispatch.invoke(user);
      assertEquals(user.getString(), userRes.getString());
      assertEquals(user.getQname(), userRes.getQname());
   }

   private Dispatch<Object> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/httpbinding";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL endpointAddress = new URL(baseURL + "/" + target);

      Service service = Service.create(serviceName);
      service.addPort(portName, HTTPBinding.HTTP_BINDING, endpointAddress.toExternalForm());

      JAXBContext jbc = JAXBContext.newInstance(new Class[] { UserType.class });
      Dispatch<Object> dispatch = service.createDispatch(portName, jbc, Mode.PAYLOAD);
      return dispatch;
   }
}
