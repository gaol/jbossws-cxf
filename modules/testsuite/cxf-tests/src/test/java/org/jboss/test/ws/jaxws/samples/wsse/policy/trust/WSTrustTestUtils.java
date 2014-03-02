/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.ClientCallbackHandler;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.UsernameTokenCallbackHandler;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface;

/**
 * Some client util methods for WS-Trust testcases 
 *
 * @author alessio.soldano@jboss.com
 * @since 08-May-2012
 */
public class WSTrustTestUtils
{
   public static JBossWSCXFTestSetup getTestSetup(Class<?> testClass, String archives) {
      JBossWSCXFTestSetup testSetup = new JBossWSCXFTestSetup(testClass, archives);
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties",
            JBossWSTestHelper.getResourceFile("jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties",
            JBossWSTestHelper.getResourceFile("jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties").getAbsolutePath());
      authenticationOptions.put("unauthenticatedIdentity", "anonymous");
      testSetup.addSecurityDomainRequirement("JBossWS-trust-sts", authenticationOptions);
      return testSetup;
   }
   
   public static void setupWsseAndSTSClient(ServiceIface proxy, Bus bus, String stsWsdlLocation, QName stsService, QName stsPort)
   {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      setServiceContextAttributes(ctx);
      ctx.put(SecurityConstants.STS_CLIENT, createSTSClient(bus, stsWsdlLocation, stsService, stsPort));
   }

   public static void setupWsse(ServiceIface proxy, Bus bus)
   {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      setServiceContextAttributes(ctx);
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.USERNAME), "alice");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.CALLBACK_HANDLER), new ClientCallbackHandler());
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_PROPERTIES), Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_USERNAME), "mystskey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USERNAME), "myclientkey");
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_PROPERTIES), Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO), "true");
   }


   /**
    * A PASSWORD is provided in place of the ClientCallbackHandler in the
    * STSClient.  A USERNAME and PASSWORD is required by CXF in the msg.
    *
    * @param proxy
    * @param bus
    * @param stsWsdlLocation
    * @param stsService
    * @param stsPort
    * @see org.apache.cxf.ws.security.SecurityConstants#PASSWORD
    */
   public static void setupWsseAndSTSClientNoCallbackHandler(ServiceIface proxy, Bus bus, String stsWsdlLocation, QName stsService, QName stsPort) {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      setServiceContextAttributes(ctx);

      STSClient stsClient = new STSClient(bus);
      if (stsWsdlLocation != null) {
         stsClient.setWsdlLocation(stsWsdlLocation);
         stsClient.setServiceQName(stsService);
         stsClient.setEndpointQName(stsPort);
      }
      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.USERNAME, "alice");
      props.put(SecurityConstants.PASSWORD, "clarinet");
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");
      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   /**
    * Uses the SIGNATURE_PROPERTIES keystore's  "alias name" as the SIGNATURE_USERNAME when
    * USERNAME and SIGNATURE_USERNAME is not provided.
    *
    * @param proxy
    * @param bus
    * @param stsWsdlLocation
    * @param stsService
    * @param stsPort
    * @see org.apache.cxf.ws.security.SecurityConstants#SIGNATURE_PROPERTIES
    */
   public static void setupWsseAndSTSClientNoSignatureUsername(ServiceIface proxy, Bus bus, String stsWsdlLocation, QName stsService, QName stsPort) {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");

      ctx.put(SecurityConstants.STS_CLIENT, createSTSClient(bus, stsWsdlLocation, stsService, stsPort));
   }

   /**
    * Request a security token that allows it to act as if it were somebody else.
    *
    * @param proxy
    * @param bus
    */
   public static void setupWsseAndSTSClientActAs(BindingProvider proxy, Bus bus) {

      Map<String, Object> ctx = proxy.getRequestContext();

      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myactaskey");
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");


      UsernameTokenCallbackHandler ch = new UsernameTokenCallbackHandler();
      String str = ch.getUsernameTokenString("myactaskey", null);

      ctx.put(SecurityConstants.STS_TOKEN_ACT_AS, str);


      STSClient stsClient = new STSClient(bus);
      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.USERNAME, "bob");
      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");

      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   /**
    * Request a security token that allows it to act on the behalf of somebody else.
    *
    * @param proxy
    * @param bus
    */
   public static void setupWsseAndSTSClientOnBehalfOf(BindingProvider proxy, Bus bus) {

      Map<String, Object> ctx = proxy.getRequestContext();

      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myactaskey");
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.USERNAME,"alice");
      ctx.put(SecurityConstants.PASSWORD, "clarinet");

      STSClient stsClient = new STSClient(bus);
      stsClient.setOnBehalfOf(new UsernameTokenCallbackHandler());

      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");

      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   public static void setupWsseAndSTSClientBearer(BindingProvider proxy, Bus bus) {

      Map<String, Object> ctx = proxy.getRequestContext();
      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");

      STSClient stsClient = new STSClient(bus);
      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.USERNAME, "alice");
      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      props.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      props.put(SecurityConstants.SIGNATURE_PROPERTIES,
         Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");
      //todo: rls explain required use
      stsClient.setTokenType("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
      stsClient.setKeyType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer");

      // CXF is ignoring this because of the proxy WSDL policy type X509Token
      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }

   private static String appendIssuedTokenSuffix(String prop)
   {
      return prop + ".it";
   }

    /**
     * Create and configure an STSClient for use by service ServiceImpl.
     *
     * Whenever an "<sp:IssuedToken>" policy is configured on a WSDL port, as is the
     * case for ServiceImpl, a STSClient must be created and configured in
     * order for the service to connect to the STS-server to obtain a token.
     *
     * @param bus
     * @param stsWsdlLocation
     * @param stsService
     * @param stsPort
     * @return
     */
   private static STSClient createSTSClient(Bus bus, String stsWsdlLocation, QName stsService, QName stsPort){
      STSClient stsClient = new STSClient(bus);
      if (stsWsdlLocation != null) {
         stsClient.setWsdlLocation(stsWsdlLocation);
         stsClient.setServiceQName(stsService);
         stsClient.setEndpointQName(stsPort);
      }
      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.USERNAME, "alice");
      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");
      return stsClient;
   }

   private static void setServiceContextAttributes(Map<String, Object> ctx){
      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
   }
}
