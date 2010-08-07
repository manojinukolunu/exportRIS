/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.sakaiproject.nakamura.exportRIS;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Node;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.sakaiproject.nakamura.api.exportRIS.CitationsService;

@SlingServlet(methods = "GET", resourceTypes = "sakai/citation", extensions = "ris")
public class CitationServlet extends SlingSafeMethodsServlet {
  /**
   *
   */
  private static final long serialVersionUID = -2002186252317448037L;

  @Reference
  private CitationsService citationsService;

  @Override
  protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp)
      throws ServletException, IOException {
    /*
     * Since Sling will give you the node that the user is requesting, you can work with
     * it directly.
     */
    try {
      resp.setContentType("application/octet-stream");
      resp.setCharacterEncoding("UTF-8");

//      Session session = req.getResourceResolver().adaptTo(Session.class);
//      UserManager um = AccessControlUtil.getUserManager(session);
//      Authorizable au = um.getAuthorizable(session.getUserID());
//      String citationDataPath = PersonalUtils.getPublicPath(au) + "/citationdata";

      // PrintWriter w = response.getWriter();
//      Node citationData = (Node) session.getItem(citationDataPath);
      // Node citation = session.getNode(absPath);//absPath will be like
      // /_user/a/admin/public/citataiondata not required same as above
		//resp.getWriter().write(citationData.getPath()+"\n");
      //int i;

      Node citationData = req.getResource().adaptTo(Node.class);
      String ris = citationsService.exportAsRis(citationData.getNodes());
      PrintWriter writer = resp.getWriter();
      writer.write(ris);
      writer.flush();

    } catch (Exception e) {
      resp.getWriter().write("asd");
    }

  }
}

