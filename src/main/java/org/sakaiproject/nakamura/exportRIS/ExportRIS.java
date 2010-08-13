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

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.sakaiproject.nakamura.api.doc.BindingType;
import org.sakaiproject.nakamura.api.doc.ServiceBinding;
import org.sakaiproject.nakamura.api.doc.ServiceDocumentation;
import org.sakaiproject.nakamura.api.doc.ServiceMethod;
import org.sakaiproject.nakamura.api.doc.ServiceResponse;
import org.sakaiproject.nakamura.api.personal.PersonalUtils;
import org.sakaiproject.nakamura.api.user.UserConstants;
import org.sakaiproject.nakamura.util.ExtendedJSONWriter;
import org.sakaiproject.nakamura.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.*;
import java.util.*;


@SlingServlet(methods = { "GET","POST" }, paths = { "/citations" }, extensions = "ris")
public class ExportRIS extends SlingAllMethodsServlet {
  /**
   *
   */
  private static final long serialVersionUID = -2002186252317448037L;

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
     
      Session session = req.getResourceResolver().adaptTo(Session.class);
      UserManager um = AccessControlUtil.getUserManager(session);
      Authorizable au = um.getAuthorizable(session.getUserID());
      String citationDataPath = PersonalUtils.getPublicPath(au) + "/citationdata";

      // PrintWriter w = response.getWriter();
      Node citationData = (Node) session.getItem(citationDataPath);
      // Node citation = session.getNode(absPath);//absPath will be like
      // /_user/a/admin/public/citataiondata not required same as above
        //resp.getWriter().write(citationData.getPath()+"\n");
      //int i;
     
      for (NodeIterator entries = citationData.getNodes(); entries.hasNext();) {
        Node entry = entries.nextNode();
        //resp.getWriter().write(entry.getName()+"\n");
        for(PropertyIterator propIterator = entry.getProperties(); propIterator.hasNext(); ){
        	Property prop = propIterator.nextProperty();
			if((prop.getString()).equals("sakai:citation")||(prop.getString()).equals("nt:unstructured"))
			{
				continue;
			}
        	resp.getWriter().write(prop.getName()+" - "+prop.getString()+"\n");
        }
		resp.getWriter().write("ER - ");
		resp.getWriter().write("\n");
		resp.getWriter().write("\n");
        //resp.getWriter().write(entry.getPath()+"\n");
        resp.getWriter().flush();
      }

    } catch (Exception e) {
      resp.getWriter().write("asd");
    }

  }
 
  //This will get the citations from the imported citations file and add to the public path of the logged in user
  protected void doPost(SlingHttpServletRequest req, SlingHttpServletResponse resp)
      throws ServletException, IOException {
	 // String nameOfFinalNode = null;
        try{
            int countIndex=0;
             Session session = req.getResourceResolver().adaptTo(Session.class);
              UserManager um = AccessControlUtil.getUserManager(session);
              Authorizable au = um.getAuthorizable(session.getUserID());
              String citationDataPath = PersonalUtils.getPublicPath(au) + "/citationdata";
			  Node citationData=null;
				if(session.nodeExists(citationDataPath))
				{
					citationData = (Node) session.getItem(citationDataPath);
				}
				else{
					Node publicNode=session.getNode(PersonalUtils.getPublicPath(au));
					citationData=publicNode.addNode(citationDataPath);
				}
              // PrintWriter w = response.getWriter();
              //Node citationData = (Node) session.getItem(citationDataPath);//get the public path node
              for(NodeIterator iter=citationData.getNodes();iter.hasNext();){
            	  iter.next();
            	  countIndex++;//holds the number of nodes in the citation data
              }
            InputStream in = req.getRequestParameter("myfile").getInputStream();
            
            
            Scanner scanner=new Scanner(in);//scan the inputstream
            
            OUT:
             while (scanner.hasNextLine()){
            	String nextLine=scanner.nextLine();
            	String nextLine1=null;
            	
            	
            		nextLine1=nextLine.trim();
            		if(nextLine1.equals("")&&scanner.hasNextLine()){
                		nextLine=scanner.nextLine();
                		
                	}
                	else if(!scanner.hasNextLine()){
                		break OUT;
                	}
            	
            	
            	//get the nextline of from the uploaded file
            	try {
            		ArrayList<String> lineal= processLine(nextLine);//get the two tokens from the scanner and store in an arraylist
                	ArrayList<String> citation=new ArrayList<String>();
                	String name1=" ";
                	//resp.getWriter().write(name1);
                	//name1.trim();
                	
                	while(!(name1.equals("ER"))){
                		citation.add(lineal.get(0));
                		if(name1.equals("ER")){
                			break;
                		}
                    	citation.add(lineal.get(1));
                    	lineal = processLine(scanner.nextLine());
                    	name1=lineal.get(0).trim();
                   	
                    	
                	}
                	
                    if(countIndex!= 1){
                    	String s = "" + countIndex;
            			Node addedNode=citationData.addNode(s);
            			addedNode.setProperty("sling:resourceType","sakai:citation");
            			for(int i=0;i<citation.size();i++)
            			{
            				if(i==citation.size()){
            					break;
            				}
            				String name=citation.get(i).trim();
            				if(i==citation.size()){
            					break;
            				}
            				String value=citation.get(i+1).trim();
            				addedNode.setProperty(name,value);
            				
            				i++;
            				
            			}
            			countIndex++;
            			session.save();
                    	}
                    else if(countIndex==1){
                    	String s = "" + countIndex;
            			Node addedNode=citationData.addNode(s);
            			addedNode.setProperty("sling:resourceType","sakai:citation");
            			for(int i=0;i<citation.size();i++)
            			{
            				if(i==citation.size()){
            					break;
            				}
            				String name=citation.get(i).trim();
            				if(i==citation.size()){
            					break;
            				}
            				String value=citation.get(i+1).trim();
            				addedNode.setProperty(name,value);
            				i++;
            				
            			}
            			countIndex++;
            			session.save();
                    	}
                   } 
            	catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
             resp.getWriter().write("Citations Added");
            }
            catch(RepositoryException re){
            	throw new ServletException(re.getMessage(), re);
            }
           

}

    public ArrayList<String> processLine(String line){
    	try{
    		Scanner scanner=new Scanner(line);//process each line
    		String name = null;
    		String value = null;
    		ArrayList<String> al=new ArrayList<String>();
    		if(scanner.hasNext()){
    			scanner.useDelimiter(" - ");//extract the contents of the file before and after  - 
            	name=scanner.next();//name has the UR ,TL etc
            	String nameER=name.trim();
            	if(nameER.equals("ER")){
            		al.add(name);
            	}
            	else{
            		value=scanner.next();
            		al.add(name);
            		al.add(value);
            	}
            	 //values of UR ,TL etc
            	
            
           
    		}
    		
    		
    		return al;
    	}
      catch(Exception e)
      {
    	e.printStackTrace();
      }
		return null;
      }
    
   
}