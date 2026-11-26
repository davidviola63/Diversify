package it.unisa.diversifybe.Benchmark.JMH;

import it.unisa.diversifybe.Model.Paese;
import it.unisa.diversifybe.Repository.PaeseRepository;
import it.unisa.diversifybe.Service.PaeseService;

import org.mockito.Mockito;
import org.openjdk.jmh.annotations.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Misura findPaesiByBenchmark() di PaeseService invocando il metodo originale.
// Operazione misurata: filtro stream su tutti i paesi + anyMatch sui benchmark (caso peggiore: benchmark nell'ultimo paese, ultima posizione).
// Dataset deterministici: SMALL 10, MEDIUM 50, LARGE 100 paesi, ciascuno con 5 benchmark (una per categoria gestita dall'app).
// PaeseRepository simulato con Mockito (stubOnly) e iniettato via reflection; MongoDB escluso, overhead del mock incluso.

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class FindPaesiByBenchmarkTestJMH {

    private static final String[] TIPI = {
            "DIRITTI_UMANI", "LGBT", "DISABILITA", "RAZZISMO", "DIRITTI_DONNE"
    };

    @Param({"SMALL", "MEDIUM", "LARGE"})
    public String datasetSize;

    private PaeseService service;
    private String idBenchmark;

    @Setup(Level.Trial)
    public void setup() {

        int numeroPaesi = switch (datasetSize) {
            case "SMALL" -> 10;
            case "MEDIUM" -> 50;
            case "LARGE" -> 100;
            default -> throw new IllegalArgumentException(
                    "Dataset non valido: " + datasetSize
            );
        };

        List<Paese> paesi = new ArrayList<>(numeroPaesi);

        for (int i = 0; i < numeroPaesi; i++) {

            Paese paese = new Paese();
            paese.setIdPaese("paese-" + i);
            paese.setNome("Paese " + i);

            // Nome qualificato: evita il conflitto con l'annotazione @Benchmark di JMH.
            List<it.unisa.diversifybe.Model.Benchmark> benchmark = new ArrayList<>(TIPI.length);

            for (int j = 0; j < TIPI.length; j++) {
                it.unisa.diversifybe.Model.Benchmark b = new it.unisa.diversifybe.Model.Benchmark();
                b.setIdBenchmark("benchmark-" + i + "-" + j);
                b.setTipo(TIPI[j]);
                b.setGravita(String.valueOf(j % 6));
                benchmark.add(b);
            }

            paese.setBenchmark(benchmark);
            paesi.add(paese);
        }

        // Caso peggiore: il benchmark cercato è l'ultimo dell'ultimo paese.
        idBenchmark = "benchmark-" + (numeroPaesi - 1) + "-" + (TIPI.length - 1);

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
        List<Paese> risultato = service.findPaesiByBenchmark(idBenchmark);
        if (risultato.size() != 1
                || !("paese-" + (numeroPaesi - 1)).equals(risultato.get(0).getIdPaese())) {
            throw new IllegalStateException("Verifica preliminare del benchmark fallita.");
        }
    }

    @Benchmark
    public List<Paese> findPaesiByBenchmark() {
        return service.findPaesiByBenchmark(idBenchmark);
    }
}
