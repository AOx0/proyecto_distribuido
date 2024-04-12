package com.up;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.stream.Stream;

public class ConfigBuilder {
                public static Config parseFromFileConfig(String ruta) throws ConfigError, FileNotFoundException, IOException {
                                try (BufferedReader bin = new BufferedReader(new FileReader(ruta));) {
                                                return ConfigBuilder.parseConfig(bin.lines());
                                }
                }
        
                public static Config parseConfig(Stream<String> contents) throws ConfigError {
                                final boolean collect_error[] = new boolean[] { false };
                                final int line_num[] = new int[] { 0 };

                		HashMap<String, HashMap<String, Vector<String>>> config = new HashMap<String, HashMap<String, Vector<String>>>();
                		String section[] = new String[] { null };
                                
                                contents
                                                .map(String::trim)
                                                .forEach(line -> {
                                                                line_num[0]++;

                                                                if (line.isEmpty() || line.isBlank()) return;
                                                                if (
                                                                                line.startsWith("[") 
                                                                                && line.endsWith("]") 
                                                                                && line.chars().filter(c -> c == '[').count() == 1 
                                                                                && line.chars().filter(c -> c == ']').count() == 1
                                                                ) {
                                                                                section[0] = line.replaceFirst("\\[", "").replaceFirst("]", "").trim();
                                                                                config.put(section[0], new HashMap<String, Vector<String>>());
                                                                                return;
                                                                }

                                                                long num_eq = line.chars().filter(x -> x == '=').count();
                                                                if (num_eq != 1) {
                                                                                System.err.println(
                                                                                                                "Error en la línea " + line_num[0] + " de la configuración:\n"
                                                                                                                                                + " " + line);
                                                                                collect_error[0] = true;
                                                                                return;
                                                                }

                                                                String[] keyval = line.split("=");

                                                                String key = keyval[0].trim().replace("\"", "");
                                                                Vector<String> values = new Vector<String>();
                                                                if (keyval[1].contains("[")) {
                                                                                String vals[] = keyval[1]
                                                                                                .replace("[", "")
                                                                                                .replace("]", "")
                                                                                                .split(",");

                                                                                        for (String value : vals) {
                                                                                                        values.add(value.trim().replace("\"", ""));
                                                                                        }
                                                                        } else {
                                                                                        values.add(keyval[1].trim().replace("\"", ""));
                                                                        }

                                                                if (section[0] == null) {
                                                                        System.out.println(
                                                                                "Error: no hay definida una sección en la linea " + line_num[0] + ":\n"
                                                                                                                                                + " " + line
                                                                        );
                                                                }
                                                                config.get(section[0]).merge(key, values, (curr, past) -> {
                                                                                curr.addAll(past);
                                                                                return curr;
                                                                });
                                        });


                                if (collect_error[0]) throw new ConfigError();
                                return new Config(config);
                }
        
}
