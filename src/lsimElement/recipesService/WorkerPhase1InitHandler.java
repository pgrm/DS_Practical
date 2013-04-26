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

package lsimElement.recipesService;

import java.io.IOException;
import java.util.List;

import recipesService.activitySimulation.SimulationData;
import recipesService.data.Operation;

import util.Serializer;


/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class WorkerPhase1InitHandler implements Handler {

	private String groupId;
	private List<String> users;
	private List<Operation> operations;
	
	@Override
	public Object execute(Object obj) {
		List<Object> params = (List<Object>) obj;
			
		// param 0: groupId
		groupId = ((String)params.get(0));

		// param 1: to indicate if all Servers will run in a single computer
		// or they will run Servers hosted in different computers (or more than one 
		// Server in a single computer but this computer having the same internal and external IP address)
		// * true: all Server run in a single computer
		// * false: Servers running in different computers (or more than one Server in a single computer but
		// 			this computer having the same internal and external IP address)
		SimulationData.getInstance().setLocalExecution(((String)params.get(1)).equals("localMode"));
		
		try {
			users = (List<String>) params.get(2);
			operations = (List<Operation>) Serializer.deserialize((byte[]) params.get(3));
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public String getGroupId(){
		return this.groupId;
	}
	
	public List<String> getUsers(){
		return this.users;
	}
	
	public List<Operation> getOperations(){
		return this.operations;
	}

}