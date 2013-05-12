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
package recipesService.tsaeSessions;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

import communication.ObjectInputStream_DS;
import communication.ObjectOutputStream_DS;
import java.util.ArrayList;

import recipesService.ServerData;
import recipesService.communication.Message;
import recipesService.communication.MessageAErequest;
import recipesService.communication.MessageEndTSAE;
import recipesService.communication.MessageOperation;
import recipesService.communication.MsgType;
import recipesService.data.AddOperation;
import recipesService.data.Operation;
import recipesService.data.OperationType;
import recipesService.data.RemoveOperation;
import recipesService.tsaeDataStructures.TimestampMatrix;
import recipesService.tsaeDataStructures.TimestampVector;

/**
 * @author Joan-Manuel Marques December 2012
 *
 */
public class TSAESessionPartnerSide extends Thread {

    private Socket socket = null;
    private ServerData serverData = null;

    public TSAESessionPartnerSide(Socket socket, ServerData serverData) {
        super("TSAEPartnerSideThread");
        this.socket = socket;
        this.serverData = serverData;
    }

    public void run() {
        System.out.println("Partner starts TSAE session...");

        try {
            ObjectOutputStream_DS out = new ObjectOutputStream_DS(socket.getOutputStream());
            ObjectInputStream_DS in = new ObjectInputStream_DS(socket.getInputStream());
            System.out.println("Partner - opened streams");

            // receive originator's summary and ack
            Message msg = (Message) in.readObject();
            if (msg.type() == MsgType.AE_REQUEST) {
                MessageAErequest aeMsg = (MessageAErequest) msg;
                System.out.println("Partner - received AE Request");

                TimestampMatrix localAck;
                TimestampVector localSummary;

                for (Operation op : serverData.getLog().listNewer(aeMsg.getSummary())) {
                    out.writeObject(new MessageOperation(op));
                }
                System.out.println("Partner - sent operations");

                // TODO: Finish...
                // send operations
                // TODO: Finish...
                synchronized (serverData) {
                    localSummary = serverData.getSummary().clone();
                    serverData.getAck().update(serverData.getId(), localSummary);
                    localAck = serverData.getAck().clone();
                }
                System.out.println("Partner - collected local Summary and Ack");

                // send to originator: local's summary and ack
                // TODO: Finish...
                // TODO: define localSummary, localAck
//                msg = new MessageAErequest(localSummary, localAck);

                out.writeObject(new MessageAErequest(localSummary, localAck));
                System.out.println("Partner - sent AE Request");

                // receive operations
                List<MessageOperation> operations = new ArrayList<>();
                msg = (Message) in.readObject();
                while (msg.type() == MsgType.OPERATION) {
                    // TODO: Finish...
                    System.out.println("Partner - received operation");
                    operations.add((MessageOperation) msg);
                    System.out.println("Partner - remembered operation");

                    msg = (Message) in.readObject();
                }

                // receive message to inform about the ending of the TSAE session
                if (msg.type() == MsgType.END_TSAE) {
                    System.out.println("Partner - received EndTSAE");
                    // send and "end of TSAE session" message
                    msg = new MessageEndTSAE();
                    out.writeObject(msg);
                    System.out.println("Partner - sent EndTSAE");

                    synchronized (serverData) {
                        for (MessageOperation op : operations) {
                            if (op.getOperation().getType() == OperationType.ADD) {
                                AddOperation addOp = (AddOperation) op.getOperation();

                                if (serverData.getLog().add(addOp)) {
                                    serverData.getRecipes().add(addOp.getRecipe());
                                }
                            } else {
                                RemoveOperation removeOp = (RemoveOperation) op.getOperation();

                                if (serverData.getLog().add(removeOp)) {
                                    serverData.getRecipes().remove(removeOp.getRecipeTitle());
                                }
                            }
                        }

                        serverData.getSummary().updateMax(aeMsg.getSummary());
                        serverData.getAck().updateMax(aeMsg.getAck());
                    }
                }
            }

            socket.close();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
        }

        System.out.println("...partner finished TSAE session!");
    }
}
