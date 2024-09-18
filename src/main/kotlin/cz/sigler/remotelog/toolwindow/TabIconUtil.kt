package cz.sigler.remotelog.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.LayeredIcon
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.GraphicsUtil
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import javax.swing.Icon

object TabIconUtil {

    fun getTabIcon(base: Icon = AllIcons.Debugger.Console, running: Boolean, newContent: Boolean): Icon {
        return LayeredIcon(base, object : Icon {
            override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                val iSize = JBUIScale.scale(4)
                val g2d = g.create() as Graphics2D
                try {
                    GraphicsUtil.setupAAPainting(g2d)
                    if (newContent) {
                        drawIndicator(g2d, JBColor.YELLOW, x + iconWidth - iSize, y)
                    }
                    if (running) {
                        drawIndicator(g2d, JBColor.GREEN, x + iconWidth - iSize, y + iconHeight - iSize)
                    }
                } finally {
                    g2d.dispose()
                }
            }

            override fun getIconWidth(): Int {
                return base.iconWidth
            }

            override fun getIconHeight(): Int {
                return base.iconHeight
            }

            private fun drawIndicator(g2d: Graphics2D, color: JBColor, x: Int, y: Int) {
                val iSize = JBUIScale.scale(4)
                g2d.color = color
                val shape = Ellipse2D.Double(
                    x.toDouble(),
                    y.toDouble(), iSize.toDouble(), iSize.toDouble()
                )
                g2d.fill(shape)
                g2d.color = ColorUtil.withAlpha(JBColor.BLACK, .40)
                g2d.draw(shape)
            }
        })
    }
}