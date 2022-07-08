# Compiles the x86_64 timescale library for Windows on Linux
outdir=build/libs/timescale/shared/windows_x86_64

echo "Creating outdir"
mkdir -p $outdir

echo "Compiling"
x86_64-w64-mingw32-g++ -O3 -shared -fPIC -I"${JAVA_HOME}/include/linux" -I"${JAVA_HOME}/include" -std=c++17 -o $outdir/libtimescale.dll timescale/*.cpp timescale/soundtouch/*.cpp

echo "Copying"
cp $outdir/libtimescale.dll ../src/main/resources/natives/win-x86-64/libtimescale.dll
