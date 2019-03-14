package chat.rocket.android.suggestions.strategy.trie.data

import chat.rocket.android.suggestions.model.SuggestionModel

internal class TrieNode(
    internal var data: Char,
    internal var parent: TrieNode? = null,
    internal var isLeaf: Boolean = false,
    internal var item: SuggestionModel? = null
) {

    val children = hashMapOf<Char, TrieNode>()

    fun getChild(c: Char): TrieNode? {
        children.forEach {
            if (it.key == c) return it.value
        }
        return null
    }

    fun getWords(): List<String> {
        val list = arrayListOf<String>()
        if (isLeaf) {
            list.add(toString())
        }
        children.forEach { node ->
            node.value.let {
                list.addAll(it.getWords())
            }
        }
        return list
    }

    fun getItems(): Sequence<SuggestionModel> = sequence {

        if (isLeaf) {
            yield(item!!)
        }

        children.forEach { node -> yieldAll(node.value.getItems()) }
    }

    override fun toString(): String = if (parent == null) "" else "${parent.toString()}$data"
}
