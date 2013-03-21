/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.dashboard.pig.scripts;

import com.codenvy.dashboard.pig.scripts.util.Event;
import com.codenvy.dashboard.pig.scripts.util.LogGenerator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptEventCount extends BasePigTest
{
   @Test
   public void testEventCountTenantCreated() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws3", "user2").withDate("2010-10-02").build());
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.EVENT_COUNT_TENANT_CREATED, log, new String[][]{{Constants.DATE, "20101001"}});

      FileObject fileObject = ScriptType.EVENT_COUNT_TENANT_CREATED.createFileObject(BASE_DIR, 20101001);

      Assert.assertEquals(fileObject.getValue(), 2L);
   }

   @Test
   public void testEventCountTenantDestroyed() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantDestroyedEvent("ws1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantDestroyedEvent("ws2").withDate("2010-10-01").build());
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.EVENT_COUNT_TENANT_DESTROYED, log, new String[][]{{Constants.DATE, "20101001"}});

      FileObject fileObject = ScriptType.EVENT_COUNT_TENANT_DESTROYED.createFileObject(BASE_DIR, 20101001);

      Assert.assertEquals(fileObject.getValue(), 2L);
   }

   @Test
   public void testEventCountUserCreated() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserCreatedEvent("user1", "user@user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserCreatedEvent("user2", "user@user2").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserCreatedEvent("user3", "user@user3").withDate("2010-10-01").build());
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.EVENT_COUNT_USER_CREATED, log, new String[][]{{Constants.DATE, "20101001"}});

      FileObject fileObject = ScriptType.EVENT_COUNT_USER_CREATED.createFileObject(BASE_DIR, 20101001);

      Assert.assertEquals(fileObject.getValue(), 3L);
   }

   @Test
   public void testEventCountUserRemoved() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserRemovedEvent("user1").withDate("2010-10-01").build());
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.EVENT_COUNT_USER_REMOVED, log, new String[][]{{Constants.DATE, "20101001"}});

      FileObject fileObject = ScriptType.EVENT_COUNT_USER_REMOVED.createFileObject(BASE_DIR, 20101001);

      Assert.assertEquals(fileObject.getValue(), 1L);
   }

   @Test
   public void testEventCountProjectCreated() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectCreatedEvent("user", "ws", "session", "project").withDate("2010-10-01")
         .build());
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.EVENT_COUNT_PROJECT_CREATED, log, new String[][]{{Constants.DATE, "20101001"}});

      FileObject fileObject = ScriptType.EVENT_COUNT_PROJECT_CREATED.createFileObject(BASE_DIR, 20101001);

      Assert.assertEquals(fileObject.getValue(), 1L);
   }

   @Test
   public void testEventCountProjectDestroyed() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectDestroyedEvent("user", "ws", "session", "project").withDate("2010-10-01")
         .build());
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.EVENT_COUNT_PROJECT_DESTROYED, log, new String[][]{{Constants.DATE, "20101001"}});

      FileObject fileObject = ScriptType.EVENT_COUNT_PROJECT_DESTROYED.createFileObject(BASE_DIR, 20101001);

      Assert.assertEquals(fileObject.getValue(), 1L);
   }
}
