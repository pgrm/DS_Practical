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
import java.util.TimerTask;
import java.util.Vector;

import recipesService.ServerData;
import recipesService.activitySimulation.SimulationData;
import recipesService.communication.Message;
import recipesService.communication.MessageAErequest;
import recipesService.communication.MessageEndTSAE;
import recipesService.communication.MessageOperation;
import recipesService.communication.MsgType;
import recipesService.communication.Host;
import recipesService.tsaeDataStructures.TimestampMatrix;
import recipesService.tsaeDataStructures.TimestampVector;

import communication.ObjectInputStream_DS;
import communication.ObjectOutputStream_DS;
import java.util.ArrayList;
import recipesService.data.AddOperation;
import recipesService.data.Operation;
import recipesService.data.OperationType;
import recipesService.data.RemoveOperation;

/**
 * @author Joan-Manuel Marques December 2012
 *
 */
public class TSAESessionOriginatorSide extends TimerTask {

    private ServerData serverData;

    public TSAESessionOriginatorSide(ServerData serverData) {
        super();
        this.serverData = serverData;
    }

    /**
     * Implementation of the TimeStamped Anti-Entropy protocol
     */
    public void run() {
        sessionWithN(serverData.getNumberSessions());
    }

    /**
     * This method performs num TSAE sessions with num random servers
     *
     * @param num
     */
    public void sessionWithN(int num) {
        if (!SimulationData.getInstance().isConnected()) {
            return;
        }
        List<Host> partnersTSAEsession = serverData.getRandomPartners(num);
        Host n;
        for (int i = 0; i < partnersTSAEsession.size(); i++) {
            n = partnersTSAEsession.get(i);
            sessionTSAE(n);
        }
    }

    /**
     * This method perform a TSAE session with the partner server n
     *
     * @param n
     */
    private void sessionTSAE(Host n) {
        if (n == null) {
            return;
        }

        Socket socket = null;
        try {
            socket = new Socket(n.getAddress(), n.getPort());
            ObjectInputStream_DS in = new ObjectInputStream_DS(socket.getInputStream());
            ObjectOutputStream_DS out = new ObjectOutputStream_DS(socket.getOutputStream());

            // Send to partner: local's summary and ack
            // TODO: Finish...
            // TODO: define localSummary, localAck
//            Message msg = new MessageAErequest(localSummary, localAck);
            TimestampMatrix localAck;
            TimestampVector localSummary;

            synchronized (serverData) {
                localSummary = serverData.getSummary().clone();
                serverData.getAck().update(serverData.getId(), localSummary);
                localAck = serverData.getAck().clone();
            }

            Message msg = new MessageAErequest(localSummary, localAck);
            out.writeObject(msg);

            // receive operations from partner
            List<MessageOperation> operations = new ArrayList<>();
            msg = (Message) in.readObject();
            while (msg.type() == MsgType.OPERATION) {
                // TODO: Finish...
                operations.add((MessageOperation) msg);

                msg = (Message) in.readObject();
            }

            // receive partner's summary and ack
            if (msg.type() == MsgType.AE_REQUEST) {
                MessageAErequest aeMsg = (MessageAErequest) msg;

                for (Operation op : serverData.getLog().listNewer(aeMsg.getSummary())) {
                    out.writeObject(new MessageOperation(op));
                }

                // TODO: Finish...
                // send operations
                // TODO: Finish...

                // send and "end of TSAE session" message
                msg = new MessageEndTSAE();
                out.writeObject(msg);

                // receive message to inform about the ending of the TSAE session
                msg = (Message) in.readObject();
                if (msg.type() == MsgType.END_TSAE) {
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
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }
}
