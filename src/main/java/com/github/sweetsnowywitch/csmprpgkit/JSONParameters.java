package com.github.sweetsnowywitch.csmprpgkit;

import com.google.gson.JsonObject;

public interface JSONParameters<T> {
    T withParametersFromJSON(JsonObject jsonObject);
    JsonObject parametersToJSON();
}
