/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jsfunit.spy.test;

import java.io.IOException;
import java.util.TimeZone;
import org.apache.cactus.ServletTestCase;
import org.jboss.jsfunit.jsfsession.JSFClientSession;
import org.jboss.jsfunit.jsfsession.JSFServerSession;
import org.jboss.jsfunit.jsfsession.JSFSession;
import org.jboss.jsfunit.spy.ui.SpyUIBackingBean;

/**
 * Tests for the classes in the UI package.
 *
 * @author Stan Silvert
 * @since 1.1
 */
public class UITest extends ServletTestCase
{
   private JSFServerSession server;
   private JSFClientSession client;
   
   @Override
   public void setUp() throws IOException
   {
      JSFSession jsfSession = new JSFSession("/jsfunit-spy-ui/index.jsf");
      this.server = jsfSession.getJSFServerSession();
      this.client = jsfSession.getJSFClientSession();
   }
   
   public void testGetTimeZone() throws IOException
   {
      SpyUIBackingBean backingBean = (SpyUIBackingBean)server.getManagedBeanValue("#{spybackingbean}");
      TimeZone timeZone = backingBean.getTimeZone();
      assertNotNull(timeZone);
      assertEquals(TimeZone.getDefault(), timeZone);
   }

}
