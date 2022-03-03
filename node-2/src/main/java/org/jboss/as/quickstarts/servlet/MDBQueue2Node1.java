/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.servlet;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Definition of the two JMS destinations used
 */
@JMSDestinationDefinitions(
    value = {
        @JMSDestinationDefinition(
            name = "java:/queue/MDBQueue2Node1",
            interfaceName = "javax.jms.Queue",
            destinationName = "MDBQueue2Node1"
        )
    }
)

/**
 * <p>
 * A simple servlet 3 as client that is capable of sending messages to a remote queue or counting messages in that queue.
 * </p>
 */
@WebServlet("/MDBQueue2Node1")
public class MDBQueue2Node1 extends HttpServlet {

    private static final long serialVersionUID = -8314035702649252239L;

    @Inject
    @JMSConnectionFactory("java:/jms/remoteCF")
    private JMSContext context;

    @Resource(lookup = "java:/queue/MDBQueue2Node1")
    private Queue queue;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        try {
            String operation = req.getParameter("op");
            final Destination destination = queue;

            if ("send".equalsIgnoreCase(operation)) {
                out.write("<p>Sending messages to <em>" + destination + "</em></p>");
                out.write("<h2>The following messages will be sent to the destination:</h2>");
                for (int i = 0; i < 10; i++) {
                    String text = "This is message " + (i + 1);
                    context.createProducer().send(destination, text);
                    out.write("Message (" + i + "): " + text + "</br>");
                }
                out.write("<p><i>Go to your JBoss EAP server console or server log to see the result of messages processing.</i></p>");
            } else if ("count".equalsIgnoreCase(operation)) {
                QueueBrowser browser = context.createBrowser(queue);
                Enumeration messages = browser.getEnumeration();
                int MSG_COUNT = 0;
                while(messages.hasMoreElements()){
                    messages.nextElement();
                    MSG_COUNT++;
                }
                out.write("<count>" + MSG_COUNT + "</count>");
            } else {
                out.write("<p>Operation unknown: please specify parameter \"op\", e.g. '?op=send', '?op=count' </p>");
            }
        } catch (JMSException e) {
            out.write(e.getMessage());
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
