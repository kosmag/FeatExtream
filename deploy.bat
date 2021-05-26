cd ../moa-release-2020.07.1/lib
dir /s /B ..\..\FeatExtream\*.java > sources.txt
javac -cp moa.jar @sources.txt
cd ..\..\FeatExtream\src
forfiles /s /m *.class /c "cmd /c echo @relpath" > classes.txt
python fixclasses.py > classes2.txt
jar -cvf feat-extream.jar @classes2.txt
mv feat-extream.jar  ../../moa-release-2020.07.1/lib
cd ..
cd ..\moa-release-2020.07.1
.\bin\moa.bat