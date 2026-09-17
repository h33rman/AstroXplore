package com.example.astroxplore.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme

@Composable
fun AstroNativeMathView(
    latex: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    style: TextStyle? = null
) {
    // NASA ADS often provides LaTeX without the delimiters if it's a mix.
    // However, the huarangmeng library expects LaTeX syntax.
    // We pass the raw string and let the library's parser handle the AST.
    Latex(
        latex = latex,
        modifier = modifier,
        config = LatexConfig(
            fontSize = fontSize,
            theme = LatexTheme.material3()
        )
    )
}
