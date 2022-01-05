package org.brainded.check.parser;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class KripkeParser {

    public static KripkeStructure parse(String kripkeFilePath) throws ClassCastException {

        KripkeStructure ks = new KripkeStructure();

        try {
            Gson gson = new Gson();

            // TODO Remove
            kripkeFilePath = "./ressource/kripke_2.json";

            Reader reader = Files.newBufferedReader(Paths.get(kripkeFilePath));

            Map<String, List<?>> map = gson.fromJson(reader, Map.class);

            List<String> statesList = (List<String>) map.get("states");

            List<String> initialStatesList = (List<String>) map.get("initial_states");

            List<LinkedTreeMap<String, List<String>>> transitionsList = (List<LinkedTreeMap<String, List<String>>>) map.get("transitions");

            List<LinkedTreeMap<String, List<String>>> labelingList = (List<LinkedTreeMap<String, List<String>>>) map.get("labeling");

            if (statesList == null || initialStatesList == null || transitionsList == null || labelingList == null) {
                throw new ClassCastException();
            } else {
                // Add initial states to the ks
                statesList.forEach(stateName -> ks.addState(new State(stateName)));

                // Mark initial states
                for (String initialStateString : initialStatesList) {
                    ks.markAsInitialState(initialStateString);
                }

                // Add transitions to the ks
                for (LinkedTreeMap<String, List<String>> stateTransition : transitionsList) {
                    for (String stateString : stateTransition.keySet()) {
                        ks.addSuccessors(stateString, stateTransition.get(stateString));
                    }
                }

                // Add labeling to the ks
                for (LinkedTreeMap<String, List<String>> stateLabeling : labelingList) {
                    for (String stateString : stateLabeling.keySet()) {
                        ks.addLabeling(stateString, stateLabeling.get(stateString));
                    }
                }
            }

            reader.close();

        } catch (IOException | InstanceNotFoundException e) {
            e.printStackTrace();
        }
        return ks;
    }
}
