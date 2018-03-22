package chat.rocket.android.weblinks.presentation

import chat.rocket.android.room.weblink.WebLinkEntity

interface WebLinksView {

    /**
     * Shows the web links.
     *
     * @param newDataSet The new data set to show.
     */
    suspend fun updateWebLinks(newDataSet: List<WebLinkEntity>)

    /**
     *  Shows no web links to display.
     */
    fun showNoWebLinksToDisplay()
}