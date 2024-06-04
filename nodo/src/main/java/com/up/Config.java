package com.up;

import java.util.HashMap;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Config {
    private HashMap<String, HashMap<String, Vector<String>>> config;

    public Vector<String> get_section_values(String section, String value) {
        return config.get(section).get(value);
    }

    public HashMap<String, Vector<String>> get_section(String section) {
        return config.get(section);
    }

    public String get_section_value(String section, String value) {
        return config.get(section).get(value).get(0);
    }

    Config(HashMap<String, HashMap<String, Vector<String>>> config) {
        this.config = config;
    }
}

class ConfigParser {
    private static final Logger logger = LogManager.getLogger(ConfigParser.class);

    public static Config parseFromFileConfig(String ruta) throws ConfigError, FileNotFoundException, IOException {
        try (BufferedReader bin = new BufferedReader(new FileReader(ruta));) {
            return ConfigParser.parseConfig(bin.lines());
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

                    if (line.isEmpty() || line.isBlank())
                        return;

                    if (line.trim().startsWith("#")) {
                        return;
                    }

                    /* Parse section */
                    if (line.startsWith("[")
                            && line.endsWith("]")
                            && line.chars().filter(c -> c == '[').count() == 1
                            && line.chars().filter(c -> c == ']').count() == 1) {
                        section[0] = line
                                .replaceFirst("\\[", "")
                                .replaceFirst("]", "")
                                .trim();
                        config.put(section[0], new HashMap<String, Vector<String>>());
                        return;
                    }

                    /* Verify assignment basic structure */
                    long num_eq = line.chars().filter(x -> x == '=').count();
                    if (num_eq != 1) {
                        logger.error(
                                "Error en la línea " + line_num[0] + " de la configuración: `" + line + "`");
                        collect_error[0] = true;
                        return;
                    }

                    /* Verify the setting is defined within a section */
                    if (section[0] == null) {
                        logger.error(
                                "No hay definida una sección en la linea " + line_num[0] + ": `" + line + "`");
                        collect_error[0] = true;
                        return;
                    }

                    /* Parse key value with the format `key = (value | [value1, value2, ...])` */
                    String[] keyval = line.split("=");

                    String key = keyval[0].trim();
                    if (!key.matches("^[a-zA-Z0-9_]+$")) {
                        logger.error("Llave inválida \"" + key + "\" en la linea " + line_num[0] + ": `" + line + "`");
                        collect_error[0] = true;
                        return;
                    }

                    Vector<String> values = new Vector<String>();
                    if (keyval[1].contains("[") && keyval[1].contains("]")) {
                        String vals[] = keyval[1]
                                .replace("[", "")
                                .replace("]", "")
                                .split(",");

                        for (String value : vals) {
                            values.add(value.trim().replace("\"", ""));
                        }
                    } else if (!keyval[1].contains("[") && !keyval[1].contains("]")) {
                        values.add(keyval[1].trim().replace("\"", ""));
                    } else {
                        logger.error("Asignación inválida en la línea " + line_num[0] + ": `" + line + "`");
                        collect_error[0] = true;
                        return;
                    }

                    config.get(section[0]).merge(key, values, (curr, past) -> {
                        curr.addAll(past);
                        return curr;
                    });
                });

        if (collect_error[0])
            throw new ConfigError();

        return new Config(config);
    }

}

class ConfigError extends Exception {
}
