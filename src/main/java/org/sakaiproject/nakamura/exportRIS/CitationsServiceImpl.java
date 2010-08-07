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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.exportRIS.CitationsService;

/**
 * @author chall
 *
 */
@Component
@Service
public class CitationsServiceImpl implements CitationsService {

	public String exportAsRis(NodeIterator iter) throws RepositoryException {

		StringBuilder output = new StringBuilder();

		while (iter.hasNext()) {
			output.append(exportAsRis(iter.nextNode()));
		}

		return output.toString();
	}

	public String exportAsRis(Node node) throws RepositoryException {

		String output = node.getName()
				+ "\nUR " + node.getProperty("UR").getString()
				+ "\nTL " + node.getProperty("TL").getString()
				+ "\nTY " + node.getProperty("TY").getString()
				+ "\nER \n";
		return output;
	}
}
