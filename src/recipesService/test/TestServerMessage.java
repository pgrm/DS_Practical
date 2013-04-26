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

/**
 * @author Joan-Manuel Marques
 * February 2012
 *
 */
public class TestServerMessage implements Serializable{

	private static final long serialVersionUID = 9158194712771111054L;
	
	private String groupId;
	private ExperimentData experimentData;
	private TestServerMsgType testServerMsgType;
	
	public TestServerMessage(TestServerMsgType testServerMsgType, String groupId, ExperimentData experimentData){
		this.testServerMsgType = testServerMsgType;
		this.groupId = groupId;
		this.experimentData = experimentData;
	}
	
	public String getGroupId() {
		return groupId;
	}

	public ExperimentData getExperimentData() {
		return experimentData;
	}

	public TestServerMsgType type(){
		return this.testServerMsgType;
	}

	@Override
	public String toString() {
		return "TestServerMessage [groupId=" + groupId + ", experimentData="
				+ experimentData + ", testServerMsgType=" + testServerMsgType
				+ "]";
	}
	
}
