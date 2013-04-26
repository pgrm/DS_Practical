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

import recipesService.ServerData;
import recipesService.communication.Message;
import recipesService.communication.MessageAErequest;
import recipesService.communication.MessageEndTSAE;
import recipesService.communication.MessageOperation;
import recipesService.communication.MsgType;
import recipesService.data.Operation;
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

        try {
            ObjectOutputStream_DS out = new ObjectOutputStream_DS(socket.getOutputStream());
            ObjectInputStream_DS in = new ObjectInputStream_DS(socket.getInputStream());

            // receive originator's summary and ack
            Message msg = (Message) in.readObject();
            if (msg.type() == MsgType.AE_REQUEST) {
                // TODO: Finish...

                // send operations
                // TODO: Finish...

                // send to originator: local's summary and ack
                // TODO: Finish...
                // TODO: define localSummary, localAck
//                msg = new MessageAErequest(localSummary, localAck);
                msg = new MessageAErequest(null, null);
                out.writeObject(msg);

                // receive operations
                msg = (Message) in.readObject();
                while (msg.type() == MsgType.OPERATION) {
                    // TODO: Finish...
                    msg = (Message) in.readObject();
                }

                // receive message to inform about the ending of the TSAE session
                if (msg.type() == MsgType.END_TSAE) {
                    // send and "end of TSAE session" message
                    msg = new MessageEndTSAE();
                    out.writeObject(msg);
                }
            }

            socket.close();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
        }
    }
}
