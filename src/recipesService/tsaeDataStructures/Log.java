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
package recipesService.tsaeDataStructures;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import recipesService.data.Operation;

/**
 * @author Joan-Manuel Marques December 2012
 *
 */
public class Log implements Serializable {

    private static final long serialVersionUID = -4864990265268259700L;
    /**
     * This class implements a log, that stores the operations received by a
     * client. They are stored in a ConcurrentHashMap (a hash table), that
     * stores a list of operations for each member of the group.
     */
    private ConcurrentHashMap<String, List<Operation>> log = new ConcurrentHashMap<>();

    public Log(List<String> participants) {
        // create an empty log
        for (String participant : participants) {
            log.put(participant, new Vector<Operation>());
        }
    }

    /**
     * inserts an operation into the log. Operations are inserted in order. If
     * the last operation for the user is not the previous operation than the
     * one being inserted, the insertion will fail.
     *
     * @param op
     * @return true if op is inserted, false otherwise.
     */
    public boolean add(Operation op) {
        List<Operation> operations = log.get(op.getTimestamp().getHostid());

        if (operations.isEmpty()) {
            operations.add(op);
            return true;
        } else {
            return addIfNextOperations(operations, op);
        }
    }

    /**
     * Checks the received summary (sum) and determines the operations contained
     * in the log that have not been seen by the proprietary of the summary.
     * Returns them in an ordered list.
     *
     * @param sum
     * @return list of operations
     */
    public List<Operation> listNewer(TimestampVector summary) {
        List<Operation> missingList = new Vector();

        for (String node : this.log.keySet()) {
            List<Operation> operations = this.log.get(node);
            Timestamp timestampToCompare = summary.getLast(node);

            int indexOfMostRecentReceived = getOperationsIndexOfTimestamp(operations, timestampToCompare);
            if (indexOfMostRecentReceived == -1) {
                /**
                 * The entry is not even present on Log. Must check now if it's
                 * greater or smaller than the ones Log has, and send the ones
                 * that are greater.
                 */
                for (Operation op : operations) {
                    if (op.getTimestamp().compare(timestampToCompare) > 0) {
                        missingList.add(op);
                    }
                }
            } else if (operations.get(indexOfMostRecentReceived).getTimestamp().compare(timestampToCompare) > 0) {
                /**
                 * The entry was found on the Log but is not the most recent,
                 * therefore we gotta find where is it and send the rest that is
                 * newer. We start checking if it's not newer than what's in the
                 * Log, if not just repeat the for() to send all the ones that
                 * are more recent.
                 */
                Timestamp timestampLast = operations.get(operations.size() - 1).getTimestamp();
                if (timestampLast.compare(timestampToCompare) > 0) {
                    for (Operation op : operations) {
                        Timestamp timeStamp = op.getTimestamp();
                        if (timeStamp.compare(timestampToCompare) > 0) {
                            missingList.add(op);
                        }
                    }
                }
            }
        }
        return missingList;
    }

    /**
     * Removes from the log the operations that have been acknowledged by all
     * the members of the group, according to the provided ackSummary.
     *
     * @param ack: ackSummary.
     */
    public void purgeLog(TimestampMatrix ack) {
    }

    /**
     * equals
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!(obj instanceof Log)) {
            return false;
        }

        Log other = (Log) obj;

        if (this.log == other.log) {
            return true;
        } else if (this.log == null || other.log == null) {
            return false;
        } else {
            return this.log.equals(other.log);
        }
    }

    /**
     * toString
     */
    @Override
    public synchronized String toString() {
        String name = "";
        for (List<Operation> sublog : log.values()) {
            for (Operation entry : sublog) {
                name += entry.toString() + "\n";
            }
        }

        return name;
    }

    private boolean addIfNextOperations(List<Operation> operations, Operation op) {
        Operation lastOp = operations.get(operations.size() - 1);

        if (lastOp.getTimestamp().compare(op.getTimestamp()) == -1) {
            operations.add(op);
            return true;
        } else {
            return false;
        }
    }

    private int getOperationsIndexOfTimestamp(List<Operation> operations, Timestamp timestampToCompare) {
        for (int i = operations.size() - 1; i > -1; i--) {
            if (operations.get(i).getTimestamp().compare(timestampToCompare) == 0) {
                return i;
            }
        }
        return -1;
    }
}
