package org.brainded.check.services;

import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;

import javax.management.InstanceNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

public class KripkeGenerator {
    private final int statesNumber;
    private final Set<Character> labels;
    private final List<Integer> labelsRandom;
    private final List<Integer> successorsRandom;
    private final List<Integer> initialStates;
    private final Random random;

    public KripkeGenerator(int statesNumber, Set<Character> labels) {
        this.statesNumber = statesNumber;
        this.labels = labels;
        random = new Random();
        labelsRandom = random.ints(0, labels.size() + 1)
                .limit(statesNumber)
                .boxed().toList();
        initialStates = random.ints(0, statesNumber)
                .distinct()
                .limit(random.nextInt(1,statesNumber)/2)
                .boxed().toList();
        successorsRandom = random.ints(statesNumber, 1, statesNumber).boxed().toList();
    }

    public KripkeStructure generateKripkeStructure() throws InstanceNotFoundException {
        KripkeStructure ks = randomKs();
        System.out.println(ks);
        return ks;
    }

    private KripkeStructure randomKs() throws InstanceNotFoundException {
        KripkeStructure ks = new KripkeStructure();

        Iterator<Integer> labelsRandomIt = labelsRandom.iterator();
        for (int index = 0; index < statesNumber; index++) {
            State current = new State("s" + index);
            ks.addState(current);

            int finalI = index;
            if (this.initialStates.stream().anyMatch(l -> l == finalI))
                ks.markAsInitialState(current.getStateName());

            int labelRandom = labelsRandomIt.hasNext() ? labelsRandomIt.next() : 1;
            ks.addLabeling(current.getStateName(), labelsFromRandom(labelRandom));
        }

        Iterator<Integer> successorsSizeIt = successorsRandom.iterator();
        for (State current : ks.getStates()) {
            int successorsNumber = successorsSizeIt.hasNext() ? successorsSizeIt.next() : 1;
            List<String> successors = successorsFromRandom(successorsNumber);
            ks.addSuccessors(current.getStateName(), successors);
        }
        return ks;
    }

    private List<String> successorsFromRandom(int successorNumber) {
        List<String> successors = new ArrayList<>();
        IntStream randomSuccessors = random.ints(0, statesNumber).distinct().limit(successorNumber);
        randomSuccessors.forEach(cur -> successors.add("s" + cur));
        return successors;
    }

    private List<Character> labelsFromRandom(int labelNumber) {
        List<Character> labelsRandom = new ArrayList<>();
        List<Character> currentLabel = this.labels.stream().toList();
        IntStream randomIndex = random.ints(0, labels.size()).distinct().limit(labelNumber);
        randomIndex.forEach(index -> labelsRandom.add(currentLabel.get(index)));
        return labelsRandom;
    }
}
