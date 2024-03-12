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
package org.jboss.test.ws.jaxws.jbws2701;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * [JBWS-2701] @XmlSeeAlso and generated wsdl
 *
 * @author alessio.soldano@jboss.com
 * @since 30-Sep-2009
 */
@ExtendWith(ArquillianExtension.class)
public class JBWS2701TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2701.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2701.ClassA.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2701.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2701.EndpointImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDL() throws Exception
   {
      URL url = new URL(baseURL + "/jaxws-jbws2701/EndpointService/EndpointImpl?wsdl");
      assertTrue(IOUtils.readAndCloseStream(url.openStream()).contains("classA"));
   }

   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      URL url = new URL(baseURL + "/jaxws-jbws2701/EndpointService/EndpointImpl?wsdl");
      QName serviceName = new QName("http://org.jboss/test/ws/jbws2701", "EndpointService");
      Service service = Service.create(url, serviceName);
      Endpoint port = service.getPort(Endpoint.class);
      String s = "Hi";
      assertEquals(s, port.echo(s));
   }
}
