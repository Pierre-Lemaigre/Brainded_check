package org.brainded.check.parser;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class KripkeParser {

    public static void parse(String kripkeFilePath) {

        try {
            Gson gson = new Gson();

            Reader reader = Files.newBufferedReader(Paths.get(kripkeFilePath));

            Map<?, ?> map = gson.fromJson(reader, Map.class);

            for(Map.Entry<?, ?> entry: map.entrySet()){
                System.out.println(entry.getKey() + " :  " + entry.getValue());
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
