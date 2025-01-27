package me.user.Utils

import me.user.Constant.cr
import me.user.Constant.lf
import me.user.Constant.newLine
import me.user.Constant.space

class StringUtils {
    companion object {
        fun Trim(string: String): String {
            return (
                string
                .replace(cr.toString(),"")
                .replace(lf.toString(),"")
                .replace(newLine,"")
                .replace(space.toString(),"")
            )
        }
    }
}