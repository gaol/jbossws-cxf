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
package org.jboss.test.ws.jaxws.jbws3556;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="ropalka@redhat.com">Richard Opalka</a>
 */
@ExtendWith(ArquillianExtension.class)
public class JBWS3556TestCase extends JBossWSTest {

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3556.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3556.EndpointIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3556.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3556.MyException.class);
      return archive;
   }

    private EndpointIface getProxy() throws Exception {
        final URL wsdlURL = new URL(baseURL + "/EndpointImpl?wsdl");
        final QName serviceName = new QName("http://jbws3556.jaxws.ws.test.jboss.org/", "EndpointImplService");
        final Service service = Service.create(wsdlURL, serviceName);
        return service.getPort(EndpointIface.class);
    }

   @Test
   @RunAsClient
    public void testException() throws Exception {
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwException();
            fail("Expected exception not thrown");
        } catch (MyException e) {
            assertEquals("from 1,1,message 1,summary 1", e.toString());
        }
    }
}
