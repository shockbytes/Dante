package at.shockbytes.dante.util.tracking

import android.content.Context
import at.shockbytes.dante.BuildConfig
import io.keen.client.android.AndroidKeenClientBuilder
import io.keen.client.java.KeenClient
import io.keen.client.java.KeenProject


/**
 * @author Martin Macheiner
 * Date: 01.06.2017.
 */

class KeenTrackingBackend(context: Context) : TrackingBackend {

    private val projectId = "592eb8de95cfc93b3abe8c33"
    private val readKey = "D067FCFDB2B7D90D34DE0EA4E914C5B8FF695F595A00C07457519F09C8E69A60080CE16B81A868C227579683CBD1EEA6592FFEF2D729E8F746A0920AA997DB999250A503188894C6B82BE711E790F73A3852FEED4DC7EA414558E75B68DA4603"
    private val writeKey = "0B74F581F4EF0B5424333A41087800CF1CFE15D138D13BAD384B3ED66FB1F32FB725E096D872278284C7A89DF532EF58ED1B9E694D1D20EB6F0CE3A02BBF91466DF75A9FB7A2BCDD5E73916F31CBF9602BF546ADD495C89563FDA1383A267F0B"

    init {
        initializeKeen(context)
    }

    override fun createTrackEventData(vararg entries: Pair<String, Any>): Map<String, Any> {
        val data = HashMap<String, Any>()
        entries.forEach { (key, value) ->
            data[key] = value
        }
        return data
    }

    override fun trackEvent(event: String, data: Map<String, Any>) {
        // Only track in release version!
        if (!BuildConfig.DEBUG) {
            KeenClient.client().addEventAsync(event, data)
        }
    }

    private fun initializeKeen(context: Context) {
        // First initialize client
        val client = AndroidKeenClientBuilder(context).build()
        KeenClient.initialize(client)
        // Then initialize project
        val project = KeenProject(projectId, writeKey, readKey)
        KeenClient.client().defaultProject = project
    }

}
