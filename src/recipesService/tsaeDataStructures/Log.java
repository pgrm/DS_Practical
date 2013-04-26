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
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
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
    private ConcurrentHashMap<String, List<Operation>> log = new ConcurrentHashMap<String, List<Operation>>();

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
        // TODO: Implement, remove temporary return statement
        return false;
    }

    /**
     * Checks the received summary (sum) and determines the operations contained
     * in the log that have not been seen by the proprietary of the summary.
     * Returns them in an ordered list.
     *
     * @param sum
     * @return list of operations
     */
    public List<Operation> listNewer(TimestampVector sum) {
        // TODO: Implement, remove temporary return statement
        return null;
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
        // TODO: Implement, remove temporary return statement
        return false;
    }

    /**
     * toString
     */
    @Override
    public synchronized String toString() {
        String name = "";
        for (Enumeration<List<Operation>> en = log.elements();
                en.hasMoreElements();) {
            List<Operation> sublog = en.nextElement();
            for (ListIterator<Operation> en2 = sublog.listIterator(); en2.hasNext();) {
                name += en2.next().toString() + "\n";
            }
        }

        return name;
    }
}
