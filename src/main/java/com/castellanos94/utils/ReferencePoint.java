package com.castellanos94.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.Solution;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This implementation is based on the code of Tsung-Che Chiang && ajnebro
 * https://github.com/jMetal/jMetal
 * 
 */
public class ReferencePoint {

    public List<Data> position;
    private int memberSize;
    private List<Pair<Solution, Data>> potentialMembers;

    public ReferencePoint() {
    }

    /** Constructor */
    public ReferencePoint(int size) {
        position = new ArrayList<>();
        for (int i = 0; i < size; i++)
            position.add(RealData.ZERO);
        memberSize = 0;
        potentialMembers = new ArrayList<>();
    }

    public ReferencePoint(ReferencePoint point) {
        position = new ArrayList<>(point.position.size());
        for (Data d : point.position) {
                position.add(d);
           
        }
        memberSize = 0;
        potentialMembers = new ArrayList<>();
    }

    public void generateReferencePoints(List<ReferencePoint> referencePoints, int numberOfObjectives,
            int numberOfDivisions) {

        ReferencePoint refPoint = new ReferencePoint(numberOfObjectives);
        generateRecursive(referencePoints, refPoint, numberOfObjectives, numberOfDivisions, numberOfDivisions, 0);
    }

    private void generateRecursive(List<ReferencePoint> referencePoints, ReferencePoint refPoint,
            int numberOfObjectives, int left, int total, int element) {
        if (element == (numberOfObjectives - 1)) {
            refPoint.position.set(element, new RealData(left / total));
            referencePoints.add(new ReferencePoint(refPoint));
        } else {
            for (int i = 0; i <= left; i += 1) {
                refPoint.position.set(element, new RealData(i / total));

                generateRecursive(referencePoints, refPoint, numberOfObjectives, left - i, total, element + 1);
            }
        }
    }

    public List<Data> pos() {
        return this.position;
    }

    public int MemberSize() {
        return memberSize;
    }

    public boolean HasPotentialMember() {
        return potentialMembers.size() > 0;
    }

    public void clear() {
        memberSize = 0;
        this.potentialMembers.clear();
    }

    public void AddMember() {
        this.memberSize++;
    }

    public void AddPotentialMember(Solution member_ind, Data distance) {
        this.potentialMembers.add(new ImmutablePair<Solution, Data>(member_ind, distance));
    }

    public Solution FindClosestMember() {
        Data minDistance = new RealData(Double.MAX_VALUE);
        Solution closetMember = null;
        for (Pair<Solution, Data> p : this.potentialMembers) {
            if (p.getRight().compareTo(minDistance) < 0) {
                minDistance = p.getRight();
                closetMember = p.getLeft();
            }
        }

        return closetMember;
    }

    public Solution RandomMember() {
        int index = this.potentialMembers.size() > 1 ? Tools.getRandom().nextInt(this.potentialMembers.size() - 1) : 0;
        return this.potentialMembers.get(index).getLeft();
    }

    public void RemovePotentialMember(Solution solution) {
        Iterator<Pair<Solution, Data>> it = this.potentialMembers.iterator();
        while (it.hasNext()) {
            if (it.next().getLeft().equals(solution)) {
                it.remove();
                break;
            }
        }
    }
    @Override
    public String toString() {
        return position.toString();
    }
}