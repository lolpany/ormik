package lol.lolpany.ormik.regression;

import com.google.gson.*;
import lol.lolpany.ormik.regression.TableComparator.Config;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TableComparatorConfigAdapter implements JsonDeserializer<Config> {
    private static final Set<String> CASE_INSENSITIVE_PROPERTIES = new HashSet<String>() {{
        add("includedColumns");
        add("exludedColumns");
        add("columnsForOrderBy");
    }};

    @Override
    public Config deserialize(JsonElement json, Type myClassType, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject originalJsonObject = json.getAsJsonObject();
        JsonObject replacementJsonObject = new JsonObject();
        for (Map.Entry<String, JsonElement> elementEntry : originalJsonObject.entrySet()) {
            String key = elementEntry.getKey();
            JsonElement value = elementEntry.getValue();
            if (CASE_INSENSITIVE_PROPERTIES.contains(key)) {
                value = new JsonPrimitive(value.getAsString().toUpperCase());
//            } else if (key.equals("tablesConfigs")) {
//                value = new JsonObject();
            }
            replacementJsonObject.add(key, value);
        }
        return new Gson().fromJson(replacementJsonObject, Config.class);
    }
}