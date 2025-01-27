package me.user.Object

class Type(vararg types: ObjectType) {
    private val types: ArrayList<ObjectType> = arrayListOf()
    init {
        for (type in types) {
            this.types.add(type)
        }
    }

    fun getTypes(): ArrayList<ObjectType> {
        return this.types
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (it::class != this::class) {
                return false
            }
        }

        val typeObject = other as Type
        typeObject.getTypes().forEach {
            if (types.contains(it)) {
                return true
            }
        }

        return false
    }

}