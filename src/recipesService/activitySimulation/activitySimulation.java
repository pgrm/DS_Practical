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

package recipesService.activitySimulation;

import java.util.Random;
import java.util.TimerTask;

import recipesService.ServerData;
/**
 * @author Daniel Lázaro Iglesias, Joan-Manuel Marques
 * December 2012
 *
 */
public class activitySimulation extends TimerTask{

	static Random rnd = new Random();

	private ServerData serverData;
	/**
	 * Task activated by a timer 
	 * to simulate activity periodically
	 */
	public activitySimulation(ServerData serverData){
		super();
		this.serverData = serverData;
	}
	public void run(){
		/**
		 * Simulates random user activity (creation and removal of recipes) 
		 * and dynamicity (connections and disconnections of the node).
		 */
		SimulationData simulationData = SimulationData.getInstance(); 
		double a=rnd.nextDouble();
		if(simulationData.isConnected()){
			//probability of disconnection
			if(a<simulationData.getProbDisconnect()){
				System.out.println("["+serverData.getId()+"] >> Server Disconnection");
				simulationData.disconnect();
			}
			//probability of creating a recipe
			if(a>=simulationData.getProbDisconnect()
					&& a<simulationData.getProbDisconnect()+simulationData.getProbCreate()
			){
				System.out.println("["+serverData.getId()+"] Add");

				byte[] bytes=new byte[8];
				char[] chars=new char[8];
				byte mod=((byte)'z'-(byte)'a');
				rnd.nextBytes(bytes);
				for(int ii=0; ii<8; ii++){
					byte b=bytes[ii];
					if(b<0)
						b*=-1;
					b%=mod;
					chars[ii]=(char)((byte)'a'+b);
				}

				serverData.addRecipe(String.valueOf(chars), "Content--"+String.valueOf(chars));
				serverData.getTSAESessionOriginatorSide().sessionWithN(serverData.getPropagationDegree());
			}			
			//probability of deleting a recipe
			if(simulationData.deletionActivated()
					&& a>=simulationData.getProbDisconnect()+simulationData.getProbCreate()
					&& a<simulationData.getProbDisconnect()+simulationData.getProbCreate()+simulationData.getProbDel()
			){
				String recipeTitle = serverData.getRecipes().getRandomRecipeTitle();
				if (recipeTitle != null){
					System.out.println("["+serverData.getId()+"] Removes: "+recipeTitle);
					serverData.removeRecipe(recipeTitle);
					serverData.getTSAESessionOriginatorSide().sessionWithN(serverData.getPropagationDegree());
				}				
			}

		}else {
			//probability of reconnecting
			if(a<simulationData.getProbReconnect()){
				System.out.println("["+serverData.getId()+"] >> Server Reconnection");
				simulationData.connect();
			}
		}
	}
}
