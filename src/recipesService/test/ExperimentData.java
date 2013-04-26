/*
* Copyright (c) Joan-Manuel Marques 2013. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This file is part of the practical assignment of Distributed Systems course.
*
* This code is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package recipesService.test;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/*
* @author Joan-Manuel Marques
* December 2012
*
*/

public class ExperimentData implements Serializable{

	private static final long serialVersionUID = 6374596151531473932L;

	// groupId
	String groupId;

	// num nodes
	int numNodes;

	// percentage of (required) received results prior to perform evaluation 
	int percentageRequiredResults;
	
	private List<String> params = new Vector<String>();

	public ExperimentData(){		
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getNumNodes() {
		return numNodes;
	}

	public void setNumNodes(int numNodes) {
		this.numNodes = numNodes;
	}

	public int getPercentageRequiredResults() {
		return percentageRequiredResults;
	}

	public void setPercentageRequiredResults(int percentageRequiredResults) {
		this.percentageRequiredResults = percentageRequiredResults;
	}

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "ExperimentData [groupId=" + groupId + ", numNodes=" + numNodes
				+ ", percentageRequiredResults=" + percentageRequiredResults
				+ ", params=" + params + "]";
	}
}
