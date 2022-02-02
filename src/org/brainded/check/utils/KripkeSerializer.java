package org.brainded.check.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class KripkeSerializer {
    private final KripkeStructure kripkeStructure;
    private String filename;

    public KripkeSerializer(KripkeStructure kripkeStructure, boolean ks_alea_gen) throws IOException {
        this.kripkeStructure = kripkeStructure;
        filename =  ks_alea_gen ? setFilenameRandom() : setFilename();
        filename += ".json";
    }

    public void saveKsInFile() throws IOException {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonKripkeStructure = new JsonObject();

        JsonArray states = statesAsJsonArray(this.kripkeStructure.getStates(), gson);
        JsonArray initial_states = statesAsJsonArray(this.kripkeStructure.getInitialStates(), gson);
        JsonArray labels = labelsAsJsonArray(this.kripkeStructure.getStates(), gson);
        JsonArray successors = successorsAsJsonArray(this.kripkeStructure.getStates(), gson);

        jsonKripkeStructure.add("states", states);
        jsonKripkeStructure.add("initial_states", initial_states);
        jsonKripkeStructure.add("transitions", successors);
        jsonKripkeStructure.add("labeling", labels);
        FileWriter fl = DirectoryManager.createFileInRD(filename);
        gson.toJson(jsonKripkeStructure, fl);
        fl.flush();
    }

    private String setFilename() throws IOException {
        Path workingPath = DirectoryManager.createResourceDirectory();
        if (Files.isReadable(workingPath) && Files.isWritable(workingPath)) {
            List<String> filesInDirectory = DirectoryManager.getFilesNameInDirectory(workingPath);
            if (filesInDirectory.size() == 0) {
                return "kripke_1";
            }

            Collections.sort(filesInDirectory);
            String last = filesInDirectory.get(filesInDirectory.size() - 1);
            int numberAfter = Integer.parseInt(last.split("_")[1]) + 1;
            return "kripke_" + numberAfter;
        } else {
            throw new IOException("Cannot write in the ressource directory");
        }
    }

    private String setFilenameRandom() throws IOException {
        return setFilename() + "_alea";
    }


    private JsonArray statesAsJsonArray(Collection<State> states, Gson gson) {
        List<String> statesNames = states.stream().map(State::getStateName).toList();
        return gson.toJsonTree(statesNames).getAsJsonArray();
    }

    private JsonObject stateLabelsAsJson(State state, Gson gson) {
        JsonObject res = new JsonObject();
        res.add(state.getStateName(), gson.toJsonTree(state.getLabels()).getAsJsonArray());
        return res;
    }

    private JsonArray labelsAsJsonArray(Collection<State> states, Gson gson) {
        JsonArray jsonArray = new JsonArray();
        for (State current: states) {
            jsonArray.add(stateLabelsAsJson(current, gson));
        }
        return jsonArray;
    }

    private JsonObject stateSuccessorsAsJson(State state, Gson gson) {
        JsonObject res = new JsonObject();
        res.add(state.getStateName(), statesAsJsonArray(state.getSuccessors(), gson));
        return res;
    }

    private JsonArray successorsAsJsonArray(Collection<State> states, Gson gson) {
        JsonArray jsonArray = new JsonArray();
        for (State current: states) {
            jsonArray.add(stateSuccessorsAsJson(current, gson));
        }
        return jsonArray;
    }
}
