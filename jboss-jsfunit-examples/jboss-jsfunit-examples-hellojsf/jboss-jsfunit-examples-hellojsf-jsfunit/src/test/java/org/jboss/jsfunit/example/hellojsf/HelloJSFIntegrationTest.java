/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jsfunit.example.hellojsf;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import java.io.IOException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import org.jboss.jsfunit.framework.FacesContextBridge;
import org.jboss.jsfunit.framework.WebConversationFactory;
import org.xml.sax.SAXException;

/**
 * This class does tests using only the JSFUnit framework package.  See 
 * the FacadeAPITest for examples using a simpler API.
 *
 * @author Stan Silvert
 */
public class HelloJSFIntegrationTest extends ServletTestCase
{
   private WebResponse webResponse;
   
   /**
    * Start a JSFUnit session by getting the /index.faces page.
    */
   public void setUp() throws IOException, SAXException
   {
      // Dispenses an ordinary httpunit WebConversation with special cookies
      // for JSFUnit
      WebConversation webConversation = WebConversationFactory.makeWebConversation();
      
      // Initial JSF request
      WebRequest req = new GetMethodWebRequest(WebConversationFactory.getWARURL() + "/index.faces");
      this.webResponse = webConversation.getResponse(req);
   }
   
   /**
    * @return the suite of tests being tested
    */
   public static Test suite()
   {
      return new TestSuite( HelloJSFIntegrationTest.class );
   }
   
   /**
    * The initial page was called up in the setUp() method.  This shows
    * some simple JSFUnit tests you can do on that page.
    */
   public void testInitialPage() throws IOException, SAXException
   {
      // JSF normally destroys the FacesContext, but JSFUnit keeps it and hands it
      // to your test thread via the bridge.  You should call this after each request
      // to get the latest FacesContext.
      FacesContext facesContext = FacesContextBridge.getCurrentInstance();

      // Now you have the key to all state as of the last request
      UIViewRoot root = facesContext.getViewRoot();

      // Test navigation to initial viewID
      assertEquals("/index.jsp", root.getViewId());

      // Assert that the prompt component is in the component tree and rendered
      UIComponent prompt = root.findComponent("form1:prompt");
      assertTrue(prompt.isRendered());

      // Assert that the greeting component is in the component tree but not rendered
      UIComponent greeting = root.findComponent("form1:greeting");
      assertFalse(greeting.isRendered());
   }
   
   
   public void testInputValidation() throws IOException, SAXException
   {
      // --------- User interaction ------------------------
      // Submit invalid data
      WebForm form = webResponse.getFormWithID("form1");
      form.setParameter("form1:input_foo_text", "A"); // input too short - validation error
      SubmitButton submitButton = form.getSubmitButtonWithID("form1:submit_button");
      form.submit(submitButton);
      // ---------------------------------------------------

      FacesContext facesContext = FacesContextBridge.getCurrentInstance();
      UIViewRoot root = facesContext.getViewRoot();

      // Test that I was returned to the initial view because of input error
      assertEquals("/index.jsp", root.getViewId());
      
      // Should be only one FacesMessge generated for this test.
      // It is for the component input_foo_text.
      FacesMessage message = (FacesMessage)facesContext.getMessages().next();
      assertTrue(message.getDetail().contains("input_foo_text"));
   }
   
   /**
    * This demonstrates how to test managed beans.
    */
   public void testValidInput() throws IOException, SAXException
   {
      // --------- User interaction ------------------------
      // Submit good data
      WebForm form = webResponse.getFormWithID("form1");
      form.setParameter("form1:input_foo_text", "Stan");
      SubmitButton submitButton = form.getSubmitButtonWithID("form1:submit_button");
      form.submit(submitButton);
      // ---------------------------------------------------

      FacesContext facesContext = FacesContextBridge.getCurrentInstance();
      UIViewRoot root = facesContext.getViewRoot();

      // test the greeting component
      UIComponent greeting = root.findComponent("form1:greeting");
      assertTrue(greeting.isRendered());
      assertEquals("Hello Stan", ((ValueHolder)greeting).getValue());

      // Test the backing bean.  Since I am in-container, I can test any
      // managed bean in any scope - even Seam scopes such as Conversation.
      assertEquals("Stan", (String)facesContext.getApplication()
                                               .createValueBinding("#{foo.text}")
                                               .getValue(facesContext));
   }
   
   public void testGoodbyeButton() throws IOException, SAXException
   {
      testValidInput(); // put "Stan" into the input field
      
      // --------- User interaction ------------------------
      // User presses "Goodbye" button.
      WebForm form = webResponse.getFormWithID("form1");
      SubmitButton goodbyeButton = form.getSubmitButtonWithID("form1:goodbye_button");
      form.submit(goodbyeButton);
      // ---------------------------------------------------

      FacesContext facesContext = FacesContextBridge.getCurrentInstance();
      UIViewRoot root = facesContext.getViewRoot();

      // Test navigation to a new view
      assertEquals("/finalgreeting.jsp", root.getViewId());

      // Test the greeting
      UIComponent finalGreeting = root.findComponent("finalgreeting");
      assertEquals("Bye Stan. I enjoyed our chat.", ((ValueHolder)finalGreeting).getValue());
   }
   
}
