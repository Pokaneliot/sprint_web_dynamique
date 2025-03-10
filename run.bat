set "sourceFolder=./src"
set "lib=./lib"
set "destinationFolder=./tempBin"
set "destinationClasses=./bin"
mkdir -p "%destinationFolder%"
rem fait un copie de tous les class dans le dossier temporaire tempBin
for /r "%sourceFolder%" %%F in (*.java) do (
	echo copying "%%F"
	xcopy "%%F" "%destinationFolder%" /y
)
rem compile les class et envoie le resultat dans le dossier temporaire bin
javac -parameters -cp "%lib%/*";"%destinationClasses%" -d "%destinationClasses%"  "%destinationFolder%/"*.java

rem crée le fichier jar
cd "%destinationClasses%"
jar -cvf "../sprint2.jar" .
cd ..

rem efface les dossiers temporaires
rmdir /s /q "%destinationFolder%" "%destinationClasses%"
pause	