package com.up;

import java.util.HashMap;
import java.util.Vector;

public class Config {
                private HashMap<String, HashMap<String, Vector<String>>> config;

                public Vector<String> get_section_value(String section, String value) {
                                return config.get(section).get(value);
                }

                public Config(HashMap<String, HashMap<String, Vector<String>>> config) {
                                this.config = config;
                }
}
