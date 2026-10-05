package uz.farzandim.parent.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PassportVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // e.g. "AA1234567" -> "AA 1234567"
        val trimmed = if (text.text.length >= 9) text.text.substring(0, 9) else text.text
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 1 && trimmed.length > 2) {
                out.append(" ")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 9) return offset + 1
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 10) return offset - 1
                return 9
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}
