package com.github.sweetsnowywitch.csmprpgkit;

import com.google.gson.JsonObject;

/**
 * JSONParameters is quasi-functional JSON de/serialization interface for
 * various objects.
 * <p>
 * It is intended to be implemented as follows:
 * 1. withParametersFromJSON should replace object fields
 *    with ones parsed from JSON. Fields that are not present in JSON
 *    should be copied from <code>this</code>.
 * 2. parametersToJSON should create a JSON object with fields
 *    from the object.
 * </p>
 * <p>
 * Serialization/deserialization code that uses JSONParameters typically
 * assumes that there is a default/empty value for T that can be created
 * and then populated via withParametersFromJSON.
 * </p>
 * e.g.
 * <pre>
 *     var effect = effect.reactionType()
 *     effect = effect.withParametersFromJSON(json);
 *     // use "configured" effect
 * </pre>
 *
 * @param <T> The type that is serialized.
 */
public interface JSONParameters<T> {
    /**
     * Creates the object copy with parameters replaced
     * with values parsed from JSON. Parameters not present
     * in JSON are left unchanged (will be the same as in <code>this</code>).
     */
    T withParametersFromJSON(JsonObject jsonObject);

    /**
     * Saves object parameters to JSON.
     *
     * @return JSON representation of object fields.
     */
    JsonObject parametersToJSON();
}
