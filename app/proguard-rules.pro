# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Rules for JNA
-keep class com.sun.jna.** { *; }
-keep interface com.sun.jna.** { *; }
-keep public interface com.thanes.wardstock.services.jna.FingerVeinLib$LibFvHelper { *; }
-keep public interface com.thanes.wardstock.services.jna.FingerVeinLib$LibFvHelper$* { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.awt.AWTEvent
-dontwarn java.awt.AlphaComposite
-dontwarn java.awt.BorderLayout
-dontwarn java.awt.Color
-dontwarn java.awt.Component
-dontwarn java.awt.Composite
-dontwarn java.awt.Container
-dontwarn java.awt.Cursor
-dontwarn java.awt.Dialog
-dontwarn java.awt.Dimension
-dontwarn java.awt.Frame
-dontwarn java.awt.Graphics2D
-dontwarn java.awt.Graphics
-dontwarn java.awt.GraphicsConfiguration
-dontwarn java.awt.GraphicsDevice
-dontwarn java.awt.GraphicsEnvironment
-dontwarn java.awt.HeadlessException
-dontwarn java.awt.Image
-dontwarn java.awt.LayoutManager
-dontwarn java.awt.Point
-dontwarn java.awt.Rectangle
-dontwarn java.awt.Shape
-dontwarn java.awt.Toolkit
-dontwarn java.awt.Window
-dontwarn java.awt.datatransfer.DataFlavor
-dontwarn java.awt.datatransfer.Transferable
-dontwarn java.awt.dnd.DragGestureEvent
-dontwarn java.awt.dnd.DragGestureListener
-dontwarn java.awt.dnd.DragGestureRecognizer
-dontwarn java.awt.dnd.DragSource
-dontwarn java.awt.dnd.DragSourceContext
-dontwarn java.awt.dnd.DragSourceDragEvent
-dontwarn java.awt.dnd.DragSourceDropEvent
-dontwarn java.awt.dnd.DragSourceEvent
-dontwarn java.awt.dnd.DragSourceListener
-dontwarn java.awt.dnd.DragSourceMotionListener
-dontwarn java.awt.dnd.DropTarget
-dontwarn java.awt.dnd.DropTargetContext
-dontwarn java.awt.dnd.DropTargetDragEvent
-dontwarn java.awt.dnd.DropTargetDropEvent
-dontwarn java.awt.dnd.DropTargetEvent
-dontwarn java.awt.dnd.DropTargetListener
-dontwarn java.awt.dnd.InvalidDnDOperationException
-dontwarn java.awt.event.AWTEventListener
-dontwarn java.awt.event.ActionEvent
-dontwarn java.awt.event.ActionListener
-dontwarn java.awt.event.ComponentEvent
-dontwarn java.awt.event.ComponentListener
-dontwarn java.awt.event.ContainerEvent
-dontwarn java.awt.event.HierarchyEvent
-dontwarn java.awt.event.HierarchyListener
-dontwarn java.awt.event.InputEvent
-dontwarn java.awt.event.MouseEvent
-dontwarn java.awt.event.WindowAdapter
-dontwarn java.awt.event.WindowEvent
-dontwarn java.awt.event.WindowListener
-dontwarn java.awt.geom.AffineTransform
-dontwarn java.awt.geom.Area
-dontwarn java.awt.geom.PathIterator
-dontwarn java.awt.image.BufferedImage
-dontwarn java.awt.image.ColorModel
-dontwarn java.awt.image.DataBuffer
-dontwarn java.awt.image.DataBufferByte
-dontwarn java.awt.image.DataBufferInt
-dontwarn java.awt.image.DirectColorModel
-dontwarn java.awt.image.MultiPixelPackedSampleModel
-dontwarn java.awt.image.Raster
-dontwarn java.awt.image.SampleModel
-dontwarn java.awt.image.SinglePixelPackedSampleModel
-dontwarn java.awt.image.WritableRaster
-dontwarn javax.swing.Icon
-dontwarn javax.swing.JColorChooser
-dontwarn javax.swing.JComponent
-dontwarn javax.swing.JFileChooser
-dontwarn javax.swing.JLayeredPane
-dontwarn javax.swing.JList
-dontwarn javax.swing.JOptionPane
-dontwarn javax.swing.JPanel
-dontwarn javax.swing.JRootPane
-dontwarn javax.swing.JTable
-dontwarn javax.swing.JTree
-dontwarn javax.swing.RootPaneContainer
-dontwarn javax.swing.SwingUtilities
-dontwarn javax.swing.Timer
-dontwarn javax.swing.text.JTextComponent
-dontwarn org.slf4j.impl.StaticLoggerBinder