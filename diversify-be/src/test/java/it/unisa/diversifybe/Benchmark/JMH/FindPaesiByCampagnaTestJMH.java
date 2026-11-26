package it.unisa.diversifybe.Benchmark.JMH;

import it.unisa.diversifybe.Model.CampagnaCrowdFunding;
import it.unisa.diversifybe.Model.Paese;
import it.unisa.diversifybe.Repository.PaeseRepository;
import it.unisa.diversifybe.Service.PaeseService;

import org.mockito.Mockito;
import org.openjdk.jmh.annotations.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Misura findPaesiByCampagna() di PaeseService invocando il metodo originale.
// Operazione misurata: filtro stream su tutti i paesi + anyMatch sulle campagne (caso peggiore: campagna nell'ultimo paese, ultima posizione).
// Dataset deterministici: SMALL 10x10, MEDIUM 50x50, LARGE 100x100 (paesi x campagne per paese).
// PaeseRepository simulato con Mockito (stubOnly) e iniettato via reflection; MongoDB escluso, overhead del mock incluso.

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class FindPaesiByCampagnaTestJMH {

    @Param({"SMALL", "MEDIUM", "LARGE"})
    public String datasetSize;

    private PaeseService service;
    private String idCampagna;

    @Setup(Level.Trial)
    public void setup() {

        int numeroPaesi;
        int campagnePerPaese;

        switch (datasetSize) {
            case "SMALL" -> {
                numeroPaesi = 10;
                campagnePerPaese = 10;
            }
            case "MEDIUM" -> {
                numeroPaesi = 50;
                campagnePerPaese = 50;
            }
            case "LARGE" -> {
                numeroPaesi = 100;
                campagnePerPaese = 100;
            }
            default -> throw new IllegalArgumentException(
                    "Dataset non valido: " + datasetSize
            );
        }

        List<Paese> paesi = new ArrayList<>(numeroPaesi);

        for (int i = 0; i < numeroPaesi; i++) {

            Paese paese = new Paese();
            paese.setIdPaese("paese-" + i);
            paese.setNome("Paese " + i);

            List<CampagnaCrowdFunding> campagne = new ArrayList<>(campagnePerPaese);

            for (int j = 0; j < campagnePerPaese; j++) {
                CampagnaCrowdFunding campagna = new CampagnaCrowdFunding();
                campagna.setIdCampagna("campagna-" + i + "-" + j);
                campagna.setPaese("paese-" + i);
                campagne.add(campagna);
            }

            paese.setCampagneCrowdfunding(campagne);
            paesi.add(paese);
        }

        // Caso peggiore: la campagna cercata è l'ultima dell'ultimo paese.
        idCampagna = "campagna-" + (numeroPaesi - 1) + "-" + (campagnePerPaese - 1);

        PaeseRepository paeseRepository = Mockito.mock(
                PaeseRepository.class,
                Mockito.withSettings().stubOnly()
        );
        Mockito.when(paeseRepository.findAll()).thenReturn(paesi);

        // PaeseService usa @Autowired su campo privato (nessun costruttore):
        // la dipendenza viene iniettata via reflection, senza avviare Spring.
        service = new PaeseService();
        ReflectionTestUtils.setField(service, "paeseRepository", paeseRepository);

        // Verifica preliminare di correttezza funzionale.
        List<Paese> risultato = service.findPaesiByCampagna(idCampagna);
        if (risultato.size() != 1
                || !("paese-" + (numeroPaesi - 1)).equals(risultato.get(0).getIdPaese())) {
            throw new IllegalStateException("Verifica preliminare del benchmark fallita.");
        }
    }

    @Benchmark
    public List<Paese> findPaesiByCampagna() {
        return service.findPaesiByCampagna(idCampagna);
    }
}
