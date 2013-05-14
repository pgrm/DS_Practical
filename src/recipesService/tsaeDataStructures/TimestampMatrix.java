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

import com.sun.org.apache.bcel.internal.generic.ISUB;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Joan-Manuel Marques December 2012
 *
 */
public class TimestampMatrix implements Serializable {

    private static final long serialVersionUID = 3331148113387926667L;
    private ConcurrentHashMap<String, TimestampVector> timestampMatrix = new ConcurrentHashMap<>();

    public TimestampMatrix(List<String> participants) {
        // create and empty TimestampMatrix
        for (String participant : participants) {
            timestampMatrix.put(participant, new TimestampVector(participants));
        }
    }

    private TimestampMatrix() {
    }

    /**
     * Merges two timestamp matrix taking the element-wise maximum
     *
     * @param tsMatrix
     */
    public synchronized void updateMax(TimestampMatrix tsMatrix) {
//        StringBuilder sb = new StringBuilder("TimestampVector - UpdateMax... This Matrix: ");
//        sb.append(this);
//        sb.append(" - Other Matrix: ");
//        sb.append(tsMatrix);
        
        for (Map.Entry<String, TimestampVector> entry : tsMatrix.timestampMatrix.entrySet()) {
            String key = entry.getKey();
            TimestampVector otherValue = entry.getValue();

            TimestampVector thisValue = this.timestampMatrix.get(key);
            if (thisValue != null) {
                thisValue.updateMax(otherValue);
            }
        }
        
//        sb.append("Updated This Matrix: ");
//        sb.append(this);
//        System.out.println(sb);
    }

    /**
     * substitutes current timestamp vector of node for tsVector
     *
     * @param node
     * @param tsVector
     */
    public synchronized void update(String node, TimestampVector tsVector) {
        this.timestampMatrix.replace(node, tsVector);
    }

    /**
     *
     * @return a timestamp vector containing, for each node, the timestamp known
     * by all participants
     */
    public synchronized TimestampVector minTimestampVector() {
        TimestampVector ret = null;        

        for (TimestampVector matrixVector : this.timestampMatrix.values()) {
            if (ret == null)
                ret = matrixVector;
            else
                ret.mergeMin(matrixVector);
        }
                
//        StringBuilder sb = new StringBuilder("TimestampMatrix - MinTimestampVector... Matrix: ");
//        sb.append(this);
//        sb.append(" - MinVector: ");
//        sb.append(ret);
//        System.out.println(sb);
        
        return ret;
    }

    /**
     * clone
     */
    @Override
    public TimestampMatrix clone() {
        TimestampMatrix clonedMatrix = new TimestampMatrix();

        for (Map.Entry<String, TimestampVector> entry : timestampMatrix.entrySet()) {
            clonedMatrix.timestampMatrix.put(entry.getKey(), entry.getValue().clone());
        }

        return clonedMatrix;
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
        } else if (!(obj instanceof TimestampMatrix)) {
            return false;
        }

        TimestampMatrix other = (TimestampMatrix) obj;

        if (this.timestampMatrix == other.timestampMatrix) {
            return true;
        } else if (this.timestampMatrix == null || other.timestampMatrix == null) {
            return false;
        } else {
            return this.timestampMatrix.equals(other.timestampMatrix);
        }
    }

    /**
     * toString
     */
    @Override
    public synchronized String toString() {
        String all = "";
        if (timestampMatrix == null) {
            return all;
        }
        for (String name : timestampMatrix.keySet()) {
            if (timestampMatrix.get(name) != null) {
                all += name + ":   " + timestampMatrix.get(name) + "\n";
            }
        }
        return all;
    }

    /**
     * @param node
     * @return the timestamp vector of node in this timestamp matrix
     */
    private TimestampVector getTimestampVector(String node) {
        return this.timestampMatrix.get(node);
    }
}
