<!-- Plugin description -->
# java-deadlock-plugin

Deze plugin is bedoeld om inzicht te krijgen of methodes risico te lopen een deadlock te veroorzaken. De plugin zal (nog) niet in staat zijn alle 
mogelijke deadlocks op te sporen. Op dit moment zal de plugin verschillende risico niveau's detecteren:

- 0: Geen risico gevonden. Al is dit niet uitsluitend omdat het algoritme niet buiten het project kan kijken
- 1: LOW: Laag risico, er zijn meerdere scopes gevonden die gesynchronizeerd worden op dezelfde lock van een object instantie. 
Het algoritme zal niet weten of dit daadwerkelijk hetzelfde object is, dus aan de gebruiker is het uiteindelijke risico te beoordelen.
- 2: MID: Gemiddeld risico, wordt geconstateerd als er meerdere verschillende locks op een code pad gedetecteerd worden.
- 3: HIGH: Hoog risico, wordt geconstateerd als meerdere locks elkaar afwisselen, of als er meerdere locks zijn en er een loop in het pad gevonden wordt.

Locks zitten op synchronized scopes. Dat zijn in dit geval:
- (static) synchronized methodes
- `synchronized()` code blocks
- Methodes in een een `@Synchronized` class zonder `@NotSynchronized` annotatie

Het algoritme behandeld wel enkel code dat zich in het project bevindt. Wanneer methodes uit een externe dependency aangeroepen wordt zal dit worden aangeduid met `EXT` 
(External). Hierop zal nog handmatig gecontroleerd moeten worden of dat geen risico's oplevert.

De plugin kan aangeroepen worden voor 1 specifieke methode, of voor een class, waarbij dan alle methodes uit die class beoordeeld worden.

Output van de plugin kan er zo uitzien:
````-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|COUNT|RISK|SYNC|WS|EXT|LOOP|DEPTH|LOCK                                              |   SCOPE                                                                                            |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|    0|MID |SYNC|WS|   |    |    1|MUTEX                                             | DipElementModelManager#deleteBuitendienststelling                                                  |
|    1|    |    |WS|EXT|    |    2|                                                  |   Valideer#notNull                                                                                 |
|    2|    |    |WS|   |    |    2|                                                  |   DipElementModel#deleteBuitendienststelling                                                       |
|    3|    |    |WS|EXT|    |    3|                                                  |     Valideer#notNull                                                                               |
|    4|    |    |WS|   |    |    3|                                                  |     DipElementModel#getBuitendienststelling                                                        |
|    5|    |    |WS|EXT|    |    4|                                                  |       Valideer#notNull                                                                             |
|    6|    |    |WS|   |    |    4|                                                  |       DipElementModel#getBuitendienststelling                                                      |
|    7|    |    |WS|EXT|    |    5|                                                  |         Map#get                                                                                    |
|    8|    |    |WS|EXT|    |    4|                                                  |       DipElementDto#getLogischId                                                                   |
|    9|    |    |WS|EXT|    |    3|                                                  |     Map#remove                                                                                     |
|   10|    |    |WS|EXT|    |    3|                                                  |     DipElementDto#getLogischId                                                                     |
|   11|    |    |WS|   |    |    2|                                                  |   new BuitendienststellingChangeEvent()                                                            |
|   12|    |    |WS|   |    |    3|                                                  |     PlanelementChangeEvent#PlanelementChangeEvent                                                  |
|   13|    |    |WS|EXT|    |    4|                                                  |       ChangeEvent#ChangeEvent                                                                      |
|   14|    |    |WS|EXT|    |    3|                                                  |     new ArrayList()                                                                                |
|   15|    |    |WS|EXT|    |    3|                                                  |     List#add                                                                                       |
|   16|    |    |WS|   |    |    2|                                                  |   DipElementModelManager#getPlanversie                                                             |
|   17|    |    |WS|EXT|    |    3|                                                  |     Valideer#notNullWithIllegalState                                                               |
|   18|    |SYNC|WS|   |    |    3|MUTEX                                             |     PlanversieModelManager#findPlanversie                                                          |
|   19|    |    |WS|   |    |    4|                                                  |       PlanversieModel#findPlanversie                                                               |
|   20|    |    |WS|EXT|    |    5|                                                  |         Map#get                                                                                    |
|   21|    |    |WS|EXT|    |    3|                                                  |     Logger#debug                                                                                   |
|   22|    |    |WS|EXT|    |    3|                                                  |     new ApplicationException()                                                                     |
|   23|    |    |WS|EXT|    |    3|                                                  |     new Object()                                                                                   |
|   24|MID |    |WS|   |    |    2|                                                  |   DipElementModelManager#fireEvent                                                                 |
|   25|    |    |WS|EXT|    |    3|                                                  |     Valideer#notNull                                                                               |
|   26|    |    |WS|EXT|    |    3|                                                  |     ChangeEventManager#fireEvent                                                                   |
|   27|MID |SYNC|WS|EXT|    |    3|ChangeEventManager.class                          |     ChangeEventManager#getInstance                                                                 |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
````

Uitleg van de kolommen:
- `COUNT`:  lengte van de calltree
- `RISK`: Het risico level op een deadlock, oplopend van leeg, LOW, MID, tot HIGH
- `SYNC`: Betekent dat de huidige scope een gesynchronizeerde scope is.
- `WS`: Staat voor `Within Synchronized` scope. Betekent dat de huidige scope aangeroepen is vanuit een huidige of bovenliggende scope die gesynchronizeerd is
- `EXT`: External method. De aangeroepen methode leeft in een externe dependency en kan dus niet 100% uitgesloten worden op deadlock risico's
- `LOOP`: Er is een recursie geconstateerd in het afgelopen code pad. Hierdoor zou een risico op deadlock kunnen ontstaan mits daar een synchronized scope zich in bevindt.
- `DEPTH`: Huidige diepte van de calltree ten opzichte van het start punt. 

Bovenstaand resultaat geeft dus de calltree aan vanuit de methode `deleteBuitendienststelling` in de `DipElementModelManager` class. 
Het hoogst geconstateerde risico is MID. Dat is in dit geval geconstateerd omdat er 2 verschillende synchronizatie locks in een code pad zitten. 
Namelijk de Mutex en een gesynchronizeerde methode. Het is aan de gebruiker om te beoordelen of dit daadwerkelijk een issue kan zijn.

### Plugin toepassen
#### Vanuit java-deadlock-plugin repo
De plugin is te gebruiken door middel van `./gradlew runIde` aan te roepen vanuit de plugin repo in Intellij. Dit zal een nieuwe Intellij instance starten waarin de plugin geinstalleerd is.
Open in deze instance de donna-pti repo. Als het project volledig geladen wordt het mogelijk door middel van rechter muisknop op een methode of class `Find Deadlocks` te selecteren. 
Dit zal output geven in de terminal van de plugin repo, maar ook schrijven naar een output bestand, zoals bijvoorbeeld `DipElementModelManager_checkBuitendienststellingen_1752486954859.txt`.

#### Geinstalleerde plugin
De plugin is ook te installeren nadat deze een keer gebouwd is door middel van `./gradlew build`. 
Ga hiervoor naar `Settings` -> `Plugins` -> `Install Plugin from Disk` en selecteer de jar uit de `build` directory van de `java-deadlock-plugin` repo
Na een herstart zouden de `Find  Deadlock` features te selecteren moeten zijn op een methode of class selectie.
<!-- Plugin description end -->