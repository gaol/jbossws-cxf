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
package org.jboss.test.ws.jaxws.cxf.jbws3060;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Jun-2010
 */
@ExtendWith(ArquillianExtension.class)
public class JBWS3060ServletTestCase extends JBWS3060Tests
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeploymentJse() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3060-jse.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.logging\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3060.EndpointOneImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3060.EndpointTwoImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3060/WEB-INF/web.xml"));
      return archive;
   }

   @Override
   protected String getEndpointOneURL()
   {
      return baseURL + "/ServiceOne/EndpointOne";
   }


   @Override
   protected String getEndpointTwoURL()
   {
      return baseURL + "/ServiceTwo/EndpointTwo";
   }
}
