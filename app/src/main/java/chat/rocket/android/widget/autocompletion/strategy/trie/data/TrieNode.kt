package chat.rocket.android.widget.autocompletion.strategy.trie.data

import chat.rocket.android.widget.autocompletion.model.SuggestionModel

internal class TrieNode(internal var data: Char,
                                            internal var parent: TrieNode? = null,
                                            internal var isLeaf: Boolean = false,
                                            internal var item: SuggestionModel? = null) {
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

    class X : SuggestionModel("")

    fun getItems(): List<SuggestionModel> {
        val list = arrayListOf<SuggestionModel>()
        if (isLeaf) {
            list.add(item!!)
        }
        children.forEach { node ->
            node.value.let {
                list.addAll(it.getItems())
            }
        }
        return list
    }

    override fun toString(): String = if (parent == null) "" else "${parent.toString()}$data"
}