package cz.sigler.remotelog.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.ui.ColorUtil
import com.intellij.ui.LayeredIcon
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.GraphicsUtil
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import javax.swing.Icon

object TabIconUtil {
    val emptyIconHeight = 16
    val emptyIconWidth = 16

    fun getTabIcon(base: Icon? = AllIcons.Debugger.Console, running: Boolean, newContent: Boolean): Icon {
        return LayeredIcon(base, object : Icon {
            override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                val iSize = JBUIScale.scale(4)
                val g2d = g.create() as Graphics2D
                try {
                    GraphicsUtil.setupAAPainting(g2d)
                    if (newContent) {
                        drawIndicator(g2d, Color.YELLOW, x + iconWidth - iSize, y)
                    }
                    if (running) {
                        drawIndicator(g2d, Color.GREEN, x + iconWidth - iSize, y + iconHeight - iSize)
                    }
//                    g2d.color = color
//                    val shape = Ellipse2D.Double(
//                        (x + iconWidth - iSize).toDouble(),
//                        (y + iconHeight - iSize).toDouble(), iSize.toDouble(), iSize.toDouble()
//                    )
//                    g2d.fill(shape)
//                    g2d.color = ColorUtil.withAlpha(Color.BLACK, .40)
//                    g2d.draw(shape)
                } finally {
                    g2d.dispose()
                }
            }

            override fun getIconWidth(): Int {
                return base?.iconWidth ?: emptyIconWidth
            }

            override fun getIconHeight(): Int {
                return base?.iconHeight ?: emptyIconHeight
            }

            private fun drawIndicator(g2d: Graphics2D, color: Color, x: Int, y: Int) {
                val iSize = JBUIScale.scale(4)
                g2d.color = color
                val shape = Ellipse2D.Double(
                    x.toDouble(),
                    y.toDouble(), iSize.toDouble(), iSize.toDouble()
                )
                g2d.fill(shape)
                g2d.color = ColorUtil.withAlpha(Color.BLACK, .40)
                g2d.draw(shape)
            }
        })
    }
}