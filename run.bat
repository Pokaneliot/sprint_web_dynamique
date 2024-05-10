set "sourceFolder=./src"
set "lib=./lib"
set "destinationFolder=./tempBin"
set "destinationClasses=./bin"

for /r "%sourceFolder%" %%F in (*.java) do (
	echo copying "%%F"
	xcopy "%%F" "%destinationFolder%"
)
javac -parameters -cp "%lib%/*";"%destinationClasses%" -d "%destinationClasses%"  "%destinationFolder%/"*.java


cd "%destinationClasses%"
jar -cvf "../sprint0.jar" .
cd ..
pause	