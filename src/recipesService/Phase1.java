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

package recipesService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import lsimElement.recipesService.WorkerPhase1InitHandler;

import recipesService.data.Operation;
import recipesService.data.Recipes;
import recipesService.test.ServerResult;
import recipesService.tsaeDataStructures.Log;
import recipesService.tsaeDataStructures.TimestampVector;
import util.Serializer;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class Phase1 {


	public static void main (String[] args){
		
		// remote node 
	    String phase1TestServerAddress = "localhost";
		int phase1TestServerPort = 39825;

		String groupId = null;
		
		try {
			List<String> argsList = Arrays.asList(args);

			if (argsList.contains("-h")){
				int i = argsList.indexOf("-h");
				phase1TestServerAddress = args[i+1];
			}

			// Phase1TestServerPort
			phase1TestServerPort = Integer.parseInt(args[0]);
			
			// groupId
			groupId = args[1];

		} catch (Exception e){
			System.err.println("Server error. Incorrect arguments");
			System.err.println("arg0: port (phase 1 test server port)");
			System.err.println("arg1: group id");
			System.err.println("optional args:");
			System.err.println("\t-h <IP address of Phase1TestServer>: IP Address of Phase1TestServer");
			System.exit(1);
		}

		// Obtain list of operations
    	List<Operation> operations = null;
    	List<String> users = null;
        try {
        	Socket socket = new Socket(phase1TestServerAddress, phase1TestServerPort);
        	ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        	// get initialization information from TestServer
        	// (initialization is done using a WorkerInitHandler to maintain consistency with LSim mode of execution)
        	WorkerPhase1InitHandler init = new WorkerPhase1InitHandler();
    		try {
				init.execute((Object) Serializer.deserialize((byte []) in.readObject()));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
            users = init.getUsers();
        	operations =init.getOperations();

        	// apply operations locally
        	Log log = new Log(users);
        	TimestampVector summary = new TimestampVector(users);
        	for (int i=0; i<operations.size(); i++){
        		log.add(operations.get(i));
        		summary.updateTimestamp(operations.get(i).getTimestamp());
        	}

        	// send result to testServer
        	ServerResult serverResult = new ServerResult(groupId, null, new Recipes(), log, summary, null);
            out.writeObject(Serializer.serialize(serverResult));
            out.close();
            in.close();
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println("Unknown server: " + phase1TestServerAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to: " + phase1TestServerAddress);
            System.exit(1);
        }
             
		System.exit(0);
	}
}
