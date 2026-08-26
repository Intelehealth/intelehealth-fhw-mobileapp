
package org.intelehealth.app.models.pushRequestApiCall;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Always includes "value" in the serialized JSON, even when it's null,
 * instead of Gson's default behavior of omitting null fields entirely.
 */
public class AttributeJsonSerializer implements JsonSerializer<Attribute> {
    @Override
    public JsonElement serialize(Attribute attribute, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("attributeType", attribute.getAttributeType());
        jsonObject.addProperty("value", attribute.getValue() == null ? "" : attribute.getValue());
        return jsonObject;
    }
}
