Nella sezione esperimenti è presente il registro esperimenti che dovrà contenere scenario, tool/versione, data, parametri, valore, unità, esito e note.

Ecco un esempio di riga del registro esperimenti, ipotizzando un test dell’API di login di Diversify con Apache JMeter.

Campo			Valore
Scenario		Login API – versione originale E0
Tool/versione		Apache JMeter 5.6.3
Data			23/10/2026
Parametri		5 richieste/s, 30 s di warm-up, 120 s di misurazione, 5 esecuzioni
Valore			185
Unità			ms (latenza media)
Esito			Completato; 0% di errori HTTP
Note			Test eseguito nell’ambiente di riferimento, con database e backend avviati. Il valore 			riportato è la media delle latenze rilevate nelle richieste valide.