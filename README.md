# RevXT-SS23-g08

## Kurze Beschreibung
Dieses Java Projekt ist ein KI Client für das Spiel reversixt. Wie das Spiel
funktioniert kann man in [der Kurzspezifikation](./kurzSpezifikation.pdf) nachlesen.

## Spiel starten

#### Dependencies
Damit man den Client ausführen kann muss java und gradle auf dem Rechner
installiert sein.

#### server starten
Um den Client zu testen muss zuerst ein Server mit einer Map gestartet werden.
Die Binary für der Server liegt im root Verzeichnis des Projekts und kann mit
folgendem Aufruf gestartet werden:<br>
```./server_nogl -m ./compMaps/comp2023_08_2p.map -t 2```

gegebenenfalls müssen davor die nötigen Rechte vergeben werden. Dies geschieht
mit folgendem Aufruf:<br>
```chmod +x server_nogl```

Für genauere Infos zu den Parametern, welche dem Server übergeben werden, kann
man den command ```./server_nogl``` verwenden.


#### client starten
Um den Client zu starten muss zuerst die Jar datei erzeugt werden. Dafür muss
man folgenden Befehl ausführen:<br>
```gradle build```

Danach kann man den Client starten:<br>
```java -jar ./bin/client08.jar```

