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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.sakaiproject.nakamura.api.exportRIS.CitationsService;
import org.sakaiproject.nakamura.api.personal.PersonalUtils;
import org.sakaiproject.nakamura.search.SearchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SlingServlet(methods = "GET", paths = "/citations")
@Properties(value = {
    @Property(name = "service.description", value = "Perfoms searchs for citations."),
    @Property(name = "service.vendor", value = "The Sakai Foundation") })
public class CitationsServlet extends SearchServlet {
  private static final long serialVersionUID = -4952853469676386408L;

  private static final Logger LOGGER = LoggerFactory.getLogger(CitationsServlet.class);

  @Reference
  private CitationsService risService;

  /**
   * {@inheritDoc}
   *
   * @see org.apache.sling.api.servlets.SlingSafeMethodsServlet#doGet(org.apache.sling.api.SlingHttpServletRequest,
   *      org.apache.sling.api.SlingHttpServletResponse)
   */
  @Override
  protected void doGet(SlingHttpServletRequest request,
      SlingHttpServletResponse response) throws ServletException,
      IOException {

    // make sure the caller knows we only handler .ris format.
    if ("ris".equalsIgnoreCase(request.getRequestPathInfo().getExtension())) {
      String user = request.getParameter("user");
      Session session = request.getResourceResolver().adaptTo(Session.class);

      try {
        UserManager um = AccessControlUtil.getUserManager(session);
        Authorizable au = um.getAuthorizable(user);

        // should be able to retrieve this without appending  "/citationdata" since we use
        // resourceType
        String userPublicPath = PersonalUtils.getPublicPath(au);
        String queryString = "SELECT * FROM [nt:base] WHERE ISDESCENDANTNODE([/{"
            + userPublicPath
            + "}]) AND [sling:resourceType] = 'sakai/citation'";
        String queryLanguage = "JCR-SQL2";

        // Create the query.
        LOGGER.debug("Posting Query {} ", queryString);
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(queryString, queryLanguage);

        QueryResult result = query.execute();
        String output = risService.exportAsRis(result.getNodes());

        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(output);
        writer.flush();
      } catch (RepositoryException re) {
        throw new ServletException(re.getMessage(), re);
      }
    }
  }
}
