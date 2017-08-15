package chat.rocket.persistence.realm.models.ddp

import org.json.JSONArray
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

class RealmSpotlightTest {

    @Test
    fun customizeUserJsonObjectTest() {
        // This is a JSONArray that contains a single simulated user data returned from the R.C server.
        val userJSONArray = JSONArray("[{\"_id\":\"1234567890\",\"status\":\"offline\",\"name\":\"John Doe\",\"username\":\"John.doe\"}]")
        // We have only one JSONObject, so let's customize it.
        RealmSpotlight.customizeUserJSONObject(userJSONArray.getJSONObject(0))
        // The desired JSON array we want.
        val expectedUserJSONArray = JSONArray("[{\"_id\":\"1234567890\",\"name\":\"John.doe\",\"t\":\"d\"}]")
        JSONAssert.assertEquals(expectedUserJSONArray, userJSONArray, false)
    }
}