package org.brainded.check.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.brainded.check.model.KripkeStructure;
import org.brainded.check.model.State;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class KripkeSerializer {
    private final KripkeStructure kripkeStructure;
    private String filename;
    private final boolean ks_randomized;
    private final Gson gson;

    public KripkeSerializer(KripkeStructure kripkeStructure) {
        this.kripkeStructure = kripkeStructure;
        this.ks_randomized = false;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public KripkeSerializer(KripkeStructure kripkeStructure, boolean ks_alea_gen) {
        this.kripkeStructure = kripkeStructure;
        this.ks_randomized = ks_alea_gen;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private void setFilenameSaving() throws IOException {
        filename =  ks_randomized ? setFilenameRandom() : setFilename();
        filename += ".json";
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

    public void saveKsInFile() {
        JsonObject jsonKripkeStructure = new JsonObject();

        JsonArray states = statesAsJsonArray(this.kripkeStructure.getStates());
        JsonArray initial_states = statesAsJsonArray(this.kripkeStructure.getInitialStates());

    }

    private JsonArray statesAsJsonArray(Collection<State> states) {
        List<String> statesNames = states.stream().map(State::getStateName).toList();
        return this.gson.toJsonTree(statesNames).getAsJsonArray();
    }

    //private JsonObject stateLabelsAsJson()

    public KripkeStructure loadKsFromFile(Path path) {
        return kripkeStructure;
    }
}
