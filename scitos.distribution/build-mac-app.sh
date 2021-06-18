#!/bin/sh
#
# Special script for creating a Mac .app Bundle.
# This is not portable, needs to be triggered manually and the following parameters need to be adjusted accordingly.
#
VERSION="2.4.3"
JDK_HOME=/Library/Java/JavaVirtualMachines/adoptopenjdk-16.jdk/Contents/Home
JRE_MODULES="java.base,java.compiler,java.desktop,java.prefs,java.scripting,java.sql.rowset,jdk.unsupported"

# collect dependencies to mention them in the classpath one-by-one
LIBS=`ls -md target/lib/* | tr -d ','`
java -jar src/main/resources/packr/packr-all-2.7.0.jar \
    --platform mac \
    --jdk $JDK_HOME \
    --executable SciToS \
    --classpath target/scitos.jar \
                $LIBS \
    --mainclass org.hmx.scitos.view.swing.ScitosApp \
    --bundle org.hmx.scitos.swing \
    --icon src/main/resources/icons/scitos_application.icns \
    --vmargs Xmx1G \
    --output target/SciToS.app

echo "Removing the (ca. 300MB) JRE being included by default..."
rm -rf target/SciToS.app/Contents/Resources/jre

echo "Adding only the (ca. 43MB) JRE parts that are required..."
$JDK_HOME/bin/jlink \
    --module-path . \
    --add-modules $JRE_MODULES \
    --output target/SciToS.app/Contents/Resources/jre \
    --no-header-files \
    --no-man-pages \
    --strip-debug \
    --compress=2

echo "Overriding the generated Info.plist to include the correct version number..."
echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<\!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">
<plist version=\"1.0\">
 <dict>
  <key>CFBundleGetInfoString</key>
  <string>SciToS</string>
  <key>CFBundleExecutable</key>
  <string>SciToS</string>
  <key>CFBundleIdentifier</key>
  <string>org.hmx.scitos.swing</string>
  <key>CFBundleName</key>
  <string>SciToS</string>
  <key>CFBundleIconFile</key>
  <string>icons.icns</string>
  <key>CFBundleShortVersionString</key>
  <string>$VERSION</string>
  <key>CFBundleInfoDictionaryVersion</key>
  <string>6.0</string>
  <key>CFBundlePackageType</key>
  <string>APPL</string>
  <key>NSHighResolutionCapable</key>
  <true/>
 </dict>
</plist>" > target/SciToS.app/Contents/Info.plist

TAR_NAME="scitos-$VERSION-macOS.tar.gz"
echo "Packaging as $TAR_NAME..."
tar cJf target/$TAR_NAME target/SciToS.app

echo "SciToS.app creation completed!"
