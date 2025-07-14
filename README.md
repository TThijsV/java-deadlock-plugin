<!-- Plugin description -->
# java-deadlock-plugin

Deze plugin is bedoeld om inzicht te krijgen of methodes risico te lopen een deadlock te veroorzaken. De plugin zal (nog) niet in staat zijn alle 
mogelijke deadlocks op te sporen. Op dit moment zal de plugin detecteren dat er een deadlock kan optreden als een code pad een synchronized scope 
bereikt dat naar zichzelf kan loopen. Synchronized scopes zijn in dit geval:
- (static) synchronized methodes
- `synchronized()` code blocks
- Methodes in een een `@Synchronized` class zonder `@NotSynchronized` annotatie

Zonder loop gaat de plugin er vanuit dat het pad weer uit de synchronized scope zal komen en dus eventuele locking ophoudt. Hierbij wordt wel enkel
code beoordeeld dat zich in het project bevindt. Wanneer methodes uit een externe dependency aangeroepen wordt zal dit worden aangeduid met `EXT` 
(External). Hierop zal nog handmatig gecontroleerd moeten worden of dat geen risico's oplevert.

De plugin kan aangeroepen worden voor 1 specifieke methode, of voor een class, waarbij dan alle methodes uit die class beoordeeld worden.

Output van de plugin kan er zo uitzien:
````------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|COUNT|DL|SYNC|WS|EXT|LOOP|DEPTH|   SCOPE                                                                                                                                                                                                |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|    0|  |    |  |   |    |    1| DipElementModelManager#checkBuitendienststellingen                                                                                                                                                     |
|    1|  |    |  |   |    |    2|   DipElementModel#isBuitendienststellingenInitialized                                                                                                                                                  |
|    2|  |    |  |EXT|    |    2|   Planfase#isPlanfaseSD                                                                                                                                                                                |
|    3|  |    |  |EXT|    |    2|   PlanversieDto#getPlanfase                                                                                                                                                                            |
|    4|  |    |  |EXT|    |    2|   PlanvariantType#isTakVariant                                                                                                                                                                         |
|    5|  |    |  |EXT|    |    2|   PlanversieDto#getVariantType                                                                                                                                                                         |
|    6|  |    |  |EXT|    |    2|   PlanvariantType#isTakVariant                                                                                                                                                                         |
|    7|  |    |  |EXT|    |    2|   PlanversieDto#getVariantType                                                                                                                                                                         |
|    8|  |    |  |   |    |    2|   DipElementModelManagerClientFunctions#geefBuitendienststellingen                                                                                                                                     |
|    9|  |    |  |EXT|    |    3|     Valideer#notNull                                                                                                                                                                                   |
|   10|  |    |  |EXT|    |    3|     Valideer#notNull                                                                                                                                                                                   |
|   11|  |    |  |EXT|    |    3|     PlanversieDto#getDrglInfraPlanId                                                                                                                                                                   |
|   12|  |    |  |   |    |    3|     DipElementModelManagerClientFunctions#getPeriodesVoorBuitendienststellingenUitPlanversie                                                                                                           |
|   13|  |    |  |EXT|    |    4|       Valideer#notNull                                                                                                                                                                                 |
|   14|  |    |  |EXT|    |    4|       Enum#equals                                                                                                                                                                                      |
|   15|  |    |  |EXT|    |    4|       PlanversieDto#getPlanfase                                                                                                                                                                        |
|   16|  |    |  |EXT|    |    4|       PlanvariantType#isTakVariant                                                                                                                                                                     |
|   17|  |    |  |EXT|    |    4|       PlanversieDto#getVariantType                                                                                                                                                                     |
|   18|  |    |  |   |    |    4|       PtiSession#getWerkperiode                                                                                                                                                                        |
|   19|  |    |  |   |    |    5|         PlanbordInstellingen#getWerkperiode                                                                                                                                                            |
|   20|  |SYNC|WS|   |    |    4|       PtiSession#getInstance                                                                                                                                                                           |
|   21|  |    |WS|   |    |    5|         new PtiSession()                                                                                                                                                                               |
|   22|  |    |WS|   |    |    6|           new ConflictSignaleringInstellingen()                                                                                                                                                        |
|   23|  |    |WS|EXT|    |    7|             new ReentrantReadWriteLock()                                                                                                                                                               |
|   24|  |    |WS|EXT|    |    7|             Collections#synchronizedSet                                                                                                                                                                |
|   25|  |    |WS|EXT|    |    7|             new HashSet()                                                                                                                                                                              |
|   26|  |    |  |   |    |    4|       WerkPeriode#toPeriodeSet                                                                                                                                                                         |
|   27|  |    |  |EXT|    |    5|         PeriodeFactory#maakPeriodeSetHeleDagen                                                                                                                                                         |
|   28|  |    |  |EXT|    |    5|         PeriodeFactory#getInstance                                                                                                                                                                     |
|   29|  |    |  |   |    |    5|         WerkPeriode#getWpGeldigheidVerplicht                                                                                                                                                           |
|   30|  |    |  |EXT|    |    6|           Valideer#notNullWithIllegalState                                                                                                                                                             |
|   31|  |    |  |EXT|    |    3|     BuitendienststellingRaadpleegService#geefBuitendienststellingen                                                                                                                                    |
|   32|  |    |  |EXT|    |    3|     BuitendienststellingServices#getBuitendienststellingRaadpleegService                                                                                                                               |
|   33|  |    |  |EXT|    |    3|     BuitendienststellingServicesFactory#instance                                                                                                                                                       |
|   34|  |    |  |EXT|    |    3|     PlanversieDto#getDrglInfraPlanId                                                                                                                                                                   |
|   35|  |    |  |EXT|    |    3|     TransferableObject#getObject                                                                                                                                                                       |
|   36|  |    |  |   |    |    2|   DipElementModelManager#getClientFunctions                                                                                                                                                            |
|   37|  |    |  |   |    |    2|   DipElementModel#setBuitendienststellingen                                                                                                                                                            |
|   38|  |    |  |EXT|    |    3|     Valideer#notNull                                                                                                                                                                                   |
|   39|  |    |  |   |    |    3|     DipElementModel#addBuitendienststelling                                                                                                                                                            |
|   40|  |    |  |EXT|    |    4|       Valideer#notNull                                                                                                                                                                                 |
|   41|  |    |  |EXT|    |    4|       Map#put                                                                                                                                                                                          |
|   42|  |    |  |EXT|    |    4|       DipElementDto#getLogischId                                                                                                                                                                       |
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
````

Uitleg van de kolommen:
- `COUNT`:  lengte van de calltree
- `DL`: Deadlock: Is leeg als er geen deadlock gesignaleerd is
- `SYNC`: Betekent dat de huidige scope een gesynchronizeerde scope is.
- `WS`: Staat voor `Within Synchronized` scope. Betekent dat de huidige scope aangeroepen is vanuit een huidige of bovenliggende scope die gesynchronizeerd is
- `EXT`: External method. De aangeroepen methode leeft in een externe dependency en kan dus niet 100% uitgesloten worden op deadlock risico's
- `LOOP`: Er is een recursie geconstateerd in het afgelopen code pad. Hierdoor zou een risico op deadlock kunnen ontstaan mits daar een synchronized scope zich in bevindt.
- `DEPTH`: Huidige diepte van de calltree ten opzichte van het start punt. 

Bovenstaand resultaat geeft dus de calltree aan vanuit de methode `checkBuitendienststellingen` in de `DipElementModelManager` class. De `DL` (Deadlock) kolom is leeg, 
dus er is geen risico op deadlock gesignaleerd. Dat komt omdat er ook een geen loop is gesignaleerd. Vandaar dat de `LOOP` kolom ook leeg is. 
Er is slechts 1 gesynchronizeerde scope geconstateerd: `PtiSession#getInstance` De onderliggende aangeroepen methodes zijn dan ook aangeduid met `WS`

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