package chat.rocket.android.suggestions.strategy.trie.data

import chat.rocket.android.suggestions.model.SuggestionModel

internal class Trie {
    private val root = TrieNode(' ')
    private var count = 0

    fun insert(item: SuggestionModel) {
        val sanitizedWord = item.text.trim().toLowerCase()
        // Word exists, bail out.
        if (search(sanitizedWord)) return

        var current = root
        sanitizedWord.forEach { ch ->
            val child = current.getChild(ch)
            if (child == null) {
                val node = TrieNode(ch, current)
                current.children[ch] = node
                current = node
                count++
            } else {
                current = child
            }
        }
        // Set last node as leaf.
        if (current != root) {
            current.isLeaf = true
            current.item = item
        }
    }

    fun search(word: String): Boolean {
        val sanitizedWord = word.trim().toLowerCase()
        var current = root
        sanitizedWord.forEach { ch ->
            val child = current.getChild(ch) ?: return false
            current = child
        }
        if (current.isLeaf) {
            return true
        }
        return false
    }

    fun autocomplete(prefix: String): List<String> {
        val sanitizedPrefix = prefix.trim().toLowerCase()
        var lastNode: TrieNode? = root
        sanitizedPrefix.forEach { ch ->
            lastNode = lastNode?.getChild(ch)
            if (lastNode == null) return emptyList()
        }
        return lastNode!!.getWords()
    }

    fun autocompleteItems(prefix: String): List<SuggestionModel> {
        val sanitizedPrefix = prefix.trim().toLowerCase()
        var lastNode: TrieNode? = root
        sanitizedPrefix.forEach { ch ->
            lastNode = lastNode?.getChild(ch)
            if (lastNode == null) return emptyList()
        }
        return lastNode!!.getItems().take(5).toList()
    }

    fun getCount() = count
}
