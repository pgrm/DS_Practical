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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import lsimElement.recipesService.WorkerInitHandler;
import lsimElement.recipesService.WorkerStartHandler;

import recipesService.activitySimulation.SimulationData;
import recipesService.communication.Host;
import recipesService.communication.Hosts;
import recipesService.test.FinalResult;
import recipesService.test.PartialResult;
import recipesService.test.ServerResult;
import recipesService.test.TestServerMessage;
import recipesService.test.TestServerMsgType;

/**
 * @authors Joan-Manuel Marques, Daniel Lázaro Iglesias December 2012
 *
 */
public class Server {

    // Data to store recipes and information required by the TSAE protocol
    private ServerData serverData;
//	private String id;	
    String testServerAddress = "localhost";
    int testServerPort;

    /**
     * Initialization operations
     */
    public Server() {
    }

    /**
     * Method to start a client. Connects to a group. Obtains list of nodes that
     * form the group The timers for activity simulation and TSAE sessions are
     * set.
     *
     * @param args
     */
    public static void main(String[] args) {

        // properties
        Properties properties = new Properties();

        try {
            //load a properties file
            properties.load(new FileInputStream("config.properties"));

            Server server = new Server();

            //
            List<String> argsList = Arrays.asList(args);

            server.testServerAddress = "localhost";
            if (argsList.contains("-h")) {
                int i = argsList.indexOf("-h");
                server.testServerAddress = args[i + 1];
            }

            // ------------------------------------------------
            // Initialize and start
            // ------------------------------------------------

            boolean phase1 = false;
            if (argsList.contains("--phase1")) {
                // phase 1: run menu
                try {
                    SimulationData.getInstance().connect();
                    server.serverData = new ServerData("phase1");
                    Hosts participants = new Hosts(new Host("localhost", 9000));
                    participants.add(new Host("localhost", 9000));
                    server.serverData.setId("localhost:9000");
                    server.serverData.startTSAE(participants);
                    phase1 = true;
                    server.menu(true);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                // init
                server.initializeAndStartTSAE(Integer.parseInt(args[0]), properties.getProperty("groupId"));
                if (argsList.contains("--menu")) {
                    // menu mode
                    try {
                        server.menu(phase1);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                        System.exit(1);
                    }
                } else {
                    // simulated mode
                    try {
                        server.simulatedMode();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Server error. Incorrect arguments");
            System.err.println("arg0: TestServer port");
            System.err.println("arg1: group id");
            System.err.println("optional args:");
            System.err.println("\t-h <IP address of TestServer>: IP Address of TestServer");
            System.err.println("\t--menu: runs on interactive mode (if no '--menu' option is specified, runs on simulated mode)");
            System.exit(1);
        }
    }

    private void initializeAndStartTSAE(int port, String groupId) {
        System.out.println("Server -- Initializing ...");

        // connect to TestServer
        Host localNode = null;
        Hosts participants = null;
        try {
            Socket socket = new Socket(testServerAddress, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            //
            // 1.initialize TASE data structures and Simulation data 
            //

            out.writeObject(new TestServerMessage(TestServerMsgType.GET_PORT, groupId, null));

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            testServerPort = (int) in.readObject();
            // get initialization information from TestServer
            // (initialization is done using a WorkerInitHandler to maintain consistency with LSim mode of execution)

            in.close();
            out.close();
            socket.close();

            socket = new Socket(testServerAddress, testServerPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            WorkerInitHandler init = new WorkerInitHandler();
            init.execute(in.readObject());

            // get references to serverData and local node information
            serverData = init.getServerData();
            localNode = init.getLocalNode();

            // send localNode to TestServer
            out.writeObject(localNode);

            in.close();
            out.close();
            socket.close();
            //
            // 2. sleep (some time) to give time to all servers to get initialization data and send localNode information 
            Thread.sleep(10000); // 10 seconds
            //

            //
            // 3. obtain list of participating servers
            // 

            // connect to TestServer
            socket = new Socket(testServerAddress, testServerPort);
            in = new ObjectInputStream(socket.getInputStream());

            // get list of participating servers
            WorkerStartHandler start = new WorkerStartHandler();
            start.execute(in.readObject());

            participants = start.getparticipants(localNode);

            in.close();
            socket.close();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.err.println("Unknown server: " + testServerAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Server -- initialize and obtain list of participants -- Couldn't get I/O for "
                    + "the connection to: " + testServerAddress);
            System.exit(1);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("-- *** --> Server -- local node: " + localNode);
        System.out.println("-- *** --> Server -- participants: " + participants.getIds());

        // 4. start TSAE protocol
        //	* starts TSAE timer for TSAE sessions
        serverData.startTSAE(participants);

        // 	5. set connected state on simulation data
        SimulationData.getInstance().connect();

        // 6. Once the server is connected notifies to ServerPartnerSide that it is ready
        // to receive TSAE sessions from partner servers  
        serverData.notifyServerConnected();
    }

    private void endAndSendResults() {
//		serverData.setEnd();

        // sleep a time to finish current connections 
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // print final results
//		System.out.println("Final Result ");
//		System.out.println("============ ");
//		System.out.println("[" + serverData.getId() + "]" + serverData.getRecipes().toString());
//		System.out.println("[" + serverData.getId() + "]" + serverData.getLog().toString());
//		System.out.println("[" + serverData.getId() + "]" + "summary: " + serverData.getSummary().toString());
//		System.out.println("[" + serverData.getId() + "]" + "ack: " + serverData.getAck().toString());


        // ------------------------------------------------
        // send final results to TestServer
        // ------------------------------------------------

//		serverData.updateLocalSummaryWithCurrentTimestamp();

        // create a result's object that contains the TSAE data structures of this server
        ServerResult sr = new ServerResult(
                serverData.getGroupId(),
                serverData.getId(),
                serverData.getRecipes(),
                serverData.getLog(),
                serverData.getSummary(),
                serverData.getAck());

        // send final result to localTestServer
        try {
            Socket socket = new Socket(testServerAddress, testServerPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(new FinalResult(sr));

            out.close();
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println("Unknown server: " + testServerAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Server -- sending final results -- Couldn't get I/O for "
                    + "the connection to: " + testServerAddress);
            System.exit(1);
        }
    }

    private void simulatedMode() {
        // start activity simulation timers
        SimulationData.getInstance().startSimulation(serverData);


        // ------------------------------------------------
        // Recipes Service
        // ------------------------------------------------

        // sleep and print TSAE data structures until the end of the simulation 
        do {
            try {
                Thread.sleep(500); //120000
//				System.out.println("[" + serverData.getId() + "]" + serverData.getRecipes().toString());
//				System.out.println("[" + serverData.getId() + "]" + serverData.getLog().toString());
//				System.out.println("[" + serverData.getId() + "]" + "summary: " + serverData.getSummary().toString());
//				System.out.println("[" + serverData.getId() + "]" + "ack: " + serverData.getAck().toString());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while (SimulationData.getInstance().isSimulatingActivity());

        // ------------------------------------------------
        // Send partial results
        // ------------------------------------------------

        int numIterations = SimulationData.getInstance().getExecutionStop() / SimulationData.getInstance().getSetSamplingTime();;
        for (int iteration = 0; iteration < numIterations; iteration++) {
            // create a result's object that contains the TSAE data structures of this server
            ServerResult sr =
                    new ServerResult(
                    serverData.getGroupId(),
                    serverData.getId(),
                    serverData.getRecipes(),
                    serverData.getLog(),
                    serverData.getSummary(),
                    serverData.getAck());

            try {
                Socket socket = new Socket(testServerAddress, testServerPort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(new PartialResult(iteration, sr));

                out.close();
                socket.close();
            } catch (UnknownHostException e) {
                System.err.println("Unknown server: " + testServerAddress);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("--- Server -- send partial results --->"
                        + "Couldn't get I/O for "
                        + "the connection to: " + testServerAddress
                        + " Server: " + serverData.getId()
                        + " iteration: " + iteration);
                System.exit(1);
            }
            try {
                Thread.sleep(SimulationData.getInstance().getSetSamplingTime());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // ------------------------------------------------
        // End simulation and send final results
        // ------------------------------------------------
        endAndSendResults();

        System.exit(0);
    }

    private void menu(boolean phase1) {
        // ------------------------------------------------
        // Menu
        // ------------------------------------------------
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String read = null;
        boolean exit = false;
        while (!exit) {
            System.out.println("\nServer " + serverData.getId());
            System.out.println("\nSelect a command:");
            if (!SimulationData.getInstance().isConnected()) {
                System.out.println("Server disconnected");
            }
            System.out.println("1: Add a recipe");
            if (!phase1) {
                System.out.println("2: Remove a recipe");
            }
            System.out.println("3: Show the list of recipes");
            System.out.println("4: Show the Log");
            System.out.println("5: Show the summary");
            if (!phase1) {
                System.out.println("6: Show the ack");
                System.out.println("7: Disconnect");
                System.out.println("8: Reconnect");
                System.out.println("9: Send data structures to TestServer and finish");
            }
            System.out.println("0: Exit");
            try {
                read = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (read.equals("1")) {
                if (!SimulationData.getInstance().isConnected()) {
                    System.out.println("Server is disconnected. Try later");
                } else {
                    String read2 = null;
                    try {
                        System.out.println("Enter the title of the recipe to add");
                        read = br.readLine();

                        System.out.println("Enter the recipe to add");
                        read2 = br.readLine();
                    } catch (IOException ioe) {
                        System.out.println("IO error trying to read the name");
                        System.exit(1);
                    }
                    serverData.addRecipe(read, read2);
                }
            }
            //Remove
            if (read.equals("2") && !phase1) {
                if (!SimulationData.getInstance().isConnected()) {
                    System.out.println("Server is disconnected. Try later");
                } else {
                    System.out.println("Enter the title of the recipe to remove");
                    try {
                        read = br.readLine();
                    } catch (IOException ioe) {
                        System.out.println("IO error trying to read the name");
                        System.exit(1);
                    }
                    serverData.removeRecipe(read);
                }
            }
            //show Recipes
            if (read.equals("3")) {
                System.out.println("Recipes: \n" + serverData.getRecipes());
            }
            // Show Log
            if (read.equals("4")) {
                System.out.println("Log:\n" + serverData.getLog());
            }
            // Show Summary 
            if (read.equals("5")) {
//				serverData.updateLocalSummaryWithCurrentTimestamp();

                System.out.println("Summary: \n" + serverData.getSummary());
            }
            // Show Ack 
            if (read.equals("6") && !phase1) {
                System.out.println("Ack: \n" + serverData.getAck());
            }
            // Disconnect
            if (read.equals("7") && !phase1) {
                SimulationData.getInstance().disconnect();
            }
            // Reconnect
            if (read.equals("8") && !phase1) {
                SimulationData.getInstance().connect();
            }
            //Results
            if (read.equals("9") && !phase1) {
                serverData.setEnd();
                endAndSendResults();
                exit = true;
            }
            //Results
            if (read.equals("0")) {
                serverData.setEnd();
                exit = true;
            }
        }
        System.exit(0);
    }
}